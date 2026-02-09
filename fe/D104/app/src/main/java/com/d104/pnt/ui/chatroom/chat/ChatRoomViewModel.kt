package com.d104.pnt.ui.chatroom.chat

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.pnt.data.remote.model.response.ChatRoomResponse
import com.d104.pnt.data.repository.AuthRepository
import com.d104.pnt.data.repository.ChatRepository
import com.d104.pnt.data.repository.ProfileRepository
import com.d104.pnt.domain.model.ChatMessage
import com.d104.pnt.domain.model.common.BaseResult
import com.d104.pnt.domain.model.common.UiState
import com.d104.pnt.navigation.NavArgs
import com.d104.pnt.util.socket.ChatSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class ChatRoomViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
    private val chatRepository: ChatRepository,
    private val chatSocketManager: ChatSocketManager,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val chatRoomId: Long = savedStateHandle.get<Long>(NavArgs.CHAT_ID) ?: 0
    private val _roomInfo = MutableStateFlow<ChatRoomResponse?>(null)
    val roomInfo: StateFlow<ChatRoomResponse?> = _roomInfo.asStateFlow()

    private val _message = MutableStateFlow("")
    val message: StateFlow<String> = _message.asStateFlow()

    private val _chatMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val chatMessages: StateFlow<List<ChatMessage>> = _chatMessages.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val myMemberId = MutableStateFlow<Long>(0)

    private val _members = MutableStateFlow<List<ChatRoomMemberUi>>(emptyList())
    val members: StateFlow<List<ChatRoomMemberUi>> = _members.asStateFlow()

    private val _uiState = MutableStateFlow<UiState<String>>(UiState.Idle)
    val uiState: StateFlow<UiState<String>> = _uiState.asStateFlow()

    private val _selectedProfile = MutableStateFlow<ProfileData?>(null)
    val selectedProfile: StateFlow<ProfileData?> = _selectedProfile.asStateFlow()

    private val _isProfileLoading = MutableStateFlow(false)
    val isProfileLoading: StateFlow<Boolean> = _isProfileLoading.asStateFlow()

    init {
        setupChatCallbacks()
        fetchRoomInfo()

        viewModelScope.launch {
            authRepository.getMemberId().collect { id ->
                myMemberId.value = id
            }
        }

        connectToChatRoom()
    }

    private fun fetchRoomInfo() {
        viewModelScope.launch {
            when (val result = chatRepository.getChatRoomInfo(chatRoomId)) {
                is BaseResult.Success -> _roomInfo.value = result.data
                is BaseResult.Error -> {}
            }
        }
    }

    private fun connectToChatRoom() {
        viewModelScope.launch {
            authRepository.getAccessToken().collect { token ->
                if (token.isNotEmpty()) {
                    if (!chatSocketManager.isConnected()) {
                        chatSocketManager.connect(token)

                        var attempts = 0
                        val maxAttempts = 30

                        while (!chatSocketManager.isConnected() && attempts < maxAttempts) {
                            delay(100)
                            attempts++
                        }

                        if (!chatSocketManager.isConnected()) {
                            _uiState.value = UiState.Error("채팅 서버 연결 실패")
                            return@collect
                        }

                    }

                    when (val result = chatRepository.connectChatRoom(chatRoomId)) {
                        is BaseResult.Success -> {
                            _uiState.value = UiState.Success("HTTP 연결 성공")
                        }

                        is BaseResult.Error -> {
                            _uiState.value = UiState.Error("HTTP 연결 실패")
                        }
                    }

                    if (chatSocketManager.getCurrentChatRoomId() == chatRoomId) {
                        loadInitialMessages()
                    } else {
                        chatSocketManager.joinRoom(chatRoomId) { success, message ->
                            if (success) {
                                viewModelScope.launch {
                                    chatRepository.joinChatRoom(chatRoomId)
                                    loadInitialMessages()
                                }
                            } else {
                                _uiState.value = UiState.Error("채팅방 입장 실패: $message")
                            }
                        }
                    }
                }
                return@collect
            }
        }
    }

    private fun setupChatCallbacks() {
        chatSocketManager.setOnNewMessage { data ->
            val newMessage = parseMessage(data) ?: return@setOnNewMessage
            viewModelScope.launch {
                if (_chatMessages.value.none { it.id == newMessage.id }) {
                    _chatMessages.value = (_chatMessages.value + newMessage).sortedBy { it.id }
                }
            }
        }

        // 이전 메시지 조회
        chatSocketManager.setOnPreviousMessages { messages, count ->
            val parsedMessages = messages.mapNotNull { parseMessage(it) }

            viewModelScope.launch {
                val currentIds = _chatMessages.value.map { it.id }.toSet()
                val newMessages = parsedMessages.filter { it.id !in currentIds }

                if (newMessages.isNotEmpty()) {
                    _chatMessages.value = (_chatMessages.value + newMessages).sortedBy { it.id }
                }
                _isLoading.value = false
            }
        }

        // 동기화 메시지
        chatSocketManager.setOnSyncMessages { messages, count ->
            val parsedMessages = messages.mapNotNull { parseMessage(it) }

            viewModelScope.launch {
                val currentIds = _chatMessages.value.map { it.id }.toSet()
                val newMessages = parsedMessages.filter { it.id !in currentIds }

                if (newMessages.isNotEmpty()) {
                    _chatMessages.value = (_chatMessages.value + newMessages).sortedBy { it.id }
                }
            }
        }

        // 재연결 처리
        chatSocketManager.setOnReconnected { reconnectedRoomId ->

            viewModelScope.launch {
                val lastMessageId = _chatMessages.value.lastOrNull()?.id

                if (lastMessageId != null && lastMessageId > 0) {
                    chatSocketManager.syncMessages(lastMessageId)
                } else {
                    loadInitialMessages()
                }
            }
        }

        // 방장 위임 결과 수신
        chatSocketManager.setOnOwnerDelegated { data ->
            val roomId = data.optLong("roomId")
            val newOwnerId = data.optLong("newOwnerId")

            viewModelScope.launch {
                loadMembers()

                if (newOwnerId == myMemberId.value) {
                    _uiState.value = UiState.Success("방장이 되었습니다")
                }
            }
        }

        // 강퇴 이벤트 수신
        chatSocketManager.setOnMemberKicked { data ->
            val chatRoomId = data.optLong("chatRoomId")
            val kickedMemberId = data.optLong("kickMemberId")


            viewModelScope.launch {
                if (kickedMemberId == myMemberId.value) {

                    chatSocketManager.disconnect()

                    _uiState.value = UiState.Error("채팅방에서 강퇴되었습니다")

                } else {
                    loadMembers()
                }
            }
        }
    }

    private fun parseMessage(data: JSONObject): ChatMessage? {
        return try {
            ChatMessage(
                id = data.getInt("id"),
                chatRoomId = chatRoomId,
                memberId = data.optLong("senderId", data.optLong("memberId", 0)),
                senderNickname = data.optString("senderNickname", "익명"),
                avatarUrl = data.optString("avatarUrl", ""),
                content = data.getString("content"),
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun loadInitialMessages() {
        _isLoading.value = true
        chatSocketManager.loadPreviousMessages(cursor = -1, limit = 50)
    }

    fun loadMoreMessages() {

        if (_isLoading.value) {
            return
        }

        val oldestMessageId = _chatMessages.value.firstOrNull()?.id
        val messageCount = _chatMessages.value.size

        if (oldestMessageId != null && oldestMessageId > 0) {
            _isLoading.value = true
            chatSocketManager.loadPreviousMessages(cursor = oldestMessageId, limit = 50)
        } else {

        }
    }

    fun writeMessage(newMessage: String) {
        _message.value = newMessage
    }

    fun sendMessage() {
        val messageText = _message.value.trim()
        if (messageText.isEmpty()) return

        chatSocketManager.sendMessage(messageText)
        _message.value = ""
    }

    /**
     * 멤버 목록 갱신
     */
    fun loadMembers() {
        viewModelScope.launch {
            when (val result = chatRepository.getChatRoomMembers(chatRoomId)) {
                is BaseResult.Success -> {
                    _members.value = result.data.map { m ->
                        ChatRoomMemberUi(
                            memberId = m.memberId,
                            nickname = m.nickname,
//                            avatarUrl = m.avatarUrl,
                            isHost = m.owner
                        )
                    }
                }

                is BaseResult.Error -> {
                }
            }
        }
    }

    fun leaveRoom(
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            when (val result = chatRepository.leaveChatRoom(chatRoomId)) {
                is BaseResult.Success -> {
                    onSuccess()
                }

                is BaseResult.Error -> {
                }
            }
        }
    }

    fun disconnectRoom() {
        viewModelScope.launch(Dispatchers.IO) {
            withContext(NonCancellable) {
                chatRepository.disconnectChatRoom(chatRoomId)
                chatSocketManager.disconnect()
            }
        }
    }


    override fun onCleared() {
        super.onCleared()
        disconnectRoom()
    }

    // 방장 위임
    fun delegateHost(targetMemberId: Long) {
        chatSocketManager.delegateOwner(targetMemberId)
    }

    // 강퇴하기
    fun kickMember(targetMemberId: Long, reason: String) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result =
                chatRepository.kickChatRoomMember(chatRoomId, targetMemberId, reason)) {
                is BaseResult.Success -> {

                    chatSocketManager.notifyKickMember(targetMemberId)
                }

                is BaseResult.Error -> {
                    _uiState.value = UiState.Error(result.error.message ?: "강퇴 실패")
                }
            }
            _isLoading.value = false
        }
    }

    fun clearUiState() {
        _uiState.value = UiState.Idle
    }

    fun loadUserProfile(memberId: Long) {
        viewModelScope.launch {
            _isProfileLoading.value = true

            when (val result = profileRepository.getMyProfile(memberId)) {
                is BaseResult.Success -> {
                    val profile = result.data
                    _selectedProfile.value = ProfileData(
                        nickname = profile.nickname ?: "알 수 없음",
                        avatarUrl = profile.avatarUrl,
                        policeGrade = profile.stat.policeGrade,
                        thiefGrade = profile.stat.thiefGrade
                    )
                }

                is BaseResult.Error -> {
                    _selectedProfile.value = null
                }
            }

            _isProfileLoading.value = false
        }
    }

    fun clearSelectedProfile() {
        _selectedProfile.value = null
    }
}

data class ProfileData(
    val nickname: String,
    val avatarUrl: String?,
    val policeGrade: String,
    val thiefGrade: String
)