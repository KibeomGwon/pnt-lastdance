package com.d104.pnt.ui.chatroom.create

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.pnt.data.remote.model.response.ChatCreateResponse
import com.d104.pnt.data.repository.AuthRepository
import com.d104.pnt.data.repository.ChatRepository
import com.d104.pnt.data.repository.LocationRepository
import com.d104.pnt.domain.model.GeoLocationInfo
import com.d104.pnt.domain.model.common.BaseResult
import com.d104.pnt.domain.model.common.UiState
import com.d104.pnt.util.getSingleLocation
import com.d104.pnt.util.socket.ChatSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRoomCreateViewModel @Inject constructor(
    private val locationRepository: LocationRepository,
    private val chatRepository: ChatRepository,
    private val chatSocketManager: ChatSocketManager,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _maxMember = MutableStateFlow(100)
    val maxMember: StateFlow<Int> = _maxMember.asStateFlow()

    private val _currentAddress = MutableStateFlow(GeoLocationInfo("현재 위치", "찾는중..."))
    val currentAddress: StateFlow<GeoLocationInfo> = _currentAddress.asStateFlow()

    private val _createChatRoomStats = MutableStateFlow<UiState<ChatCreateResponse>>(UiState.Idle)
    val createChatRoomStats: StateFlow<UiState<ChatCreateResponse>> =
        _createChatRoomStats.asStateFlow()

    private val _joinRoomState = MutableStateFlow<JoinRoomState>(JoinRoomState.Idle)
    val joinRoomState: StateFlow<JoinRoomState> = _joinRoomState.asStateFlow()

    sealed class JoinRoomState {
        object Idle : JoinRoomState()
        object Loading : JoinRoomState()
        data class Success(val chatRoomId: Long, val message: String) : JoinRoomState()
        data class Error(val message: String) : JoinRoomState()
    }

    fun updateTitle(newTitle: String) {
        _title.value = newTitle
    }

    fun updateDescription(newDescription: String) {
        _description.value = newDescription
    }

    fun updateMaxMember(plus: Boolean) {
        if (plus && maxMember.value < 200) {
            _maxMember.value += 1
        } else if (!plus && maxMember.value > 50) {
            _maxMember.value -= 1
        }
    }

    fun getLocationInfo(context: Context) {
        viewModelScope.launch {
            val location = context.getSingleLocation()
            if (location != null) {
                val address = locationRepository.getAddressFromLatLng(
                    location.latitude,
                    location.longitude
                )
                _currentAddress.value = address
            }
            if (_currentAddress.value.major != "세종특별자치시") {
                _currentAddress.value = GeoLocationInfo(
                    major = _currentAddress.value.major,
                    middle = _currentAddress.value.middle,
                    code = _currentAddress.value.code / 10000 * 10000
                )
            }
        }
    }

    /**
     * 입력 유효성 검사
     */
    fun isValid(): Boolean {
        if (title.value.isEmpty()) {
            return false
        }
        if (description.value.isEmpty()) {
            return false
        }
        return true
    }

    /**
     * 채팅방 생성
     */
    fun createChatRoom() {
        viewModelScope.launch {
            _createChatRoomStats.value = UiState.Loading

            // 채팅방 생성 API 호출
            when (val result = chatRepository.createChatRoom(
                title.value,
                _currentAddress.value.code,
                description.value,
                maxMember.value
            )) {
                is BaseResult.Success -> {
                    _createChatRoomStats.value = UiState.Success(result.data)
                    val chatRoomId = result.data.chatRoomId

                    joinChatRoomSequence(chatRoomId)
                }

                is BaseResult.Error -> {
                    _createChatRoomStats.value = UiState.Error(result.error.message)
                }
            }
        }
    }

    /**
     * 채팅방 참여 → 연결 → 소켓 입장 순차 처리
     */
    private fun joinChatRoomSequence(chatRoomId: Long) {
        viewModelScope.launch {
            _joinRoomState.value = JoinRoomState.Loading

            when (val joinResult = chatRepository.joinChatRoom(chatRoomId)) {
                is BaseResult.Success -> {

                    when (val connectResult = chatRepository.connectChatRoom(chatRoomId)) {
                        is BaseResult.Success -> {
                            joinChatRoomViaSocket(chatRoomId)
                        }

                        is BaseResult.Error -> {
                            _joinRoomState.value = JoinRoomState.Error(connectResult.error.message)
                        }
                    }
                }

                is BaseResult.Error -> {
                    _joinRoomState.value = JoinRoomState.Error(joinResult.error.message)
                }
            }
        }
    }

    /**
     * 소켓을 통한 채팅방 입장
     */
    private fun joinChatRoomViaSocket(chatRoomId: Long) {

        viewModelScope.launch {
            // 토큰 확보
            val token = authRepository.getAccessToken().first()
            if (token.isEmpty()) {
                _joinRoomState.value = JoinRoomState.Error("인증 정보가 없습니다.")
                return@launch
            }

            // 소켓 연결 확인 및 대기
            if (!chatSocketManager.isConnected()) {
                chatSocketManager.connect(token)

                var retryCount = 0
                while (!chatSocketManager.isConnected() && retryCount < 50) {
                    delay(100)
                    retryCount++
                }
            }

            if (chatSocketManager.isConnected()) {
                joinRoomInternal(chatRoomId)
            } else {
                _joinRoomState.value = JoinRoomState.Error("채팅 서버 연결에 실패했습니다.")
            }
        }
    }

    /**
     * 실제 채팅방 소켓 입장 처리
     */
    private fun joinRoomInternal(chatRoomId: Long) {
        chatSocketManager.joinRoom(chatRoomId) { success, message ->
            if (success) {
                _joinRoomState.value = JoinRoomState.Success(chatRoomId, message)
            } else {
                _joinRoomState.value = JoinRoomState.Error(message)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
    }
}