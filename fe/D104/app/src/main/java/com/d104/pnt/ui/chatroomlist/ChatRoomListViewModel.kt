package com.d104.pnt.ui.chatroomlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.pnt.data.remote.model.response.ChatSearchResponse
import com.d104.pnt.data.repository.AuthRepository
import com.d104.pnt.data.repository.ChatRepository
import com.d104.pnt.data.repository.LocationRepository
import com.d104.pnt.data.source.local.RegionCodeManager
import com.d104.pnt.domain.model.common.BaseResult
import com.d104.pnt.domain.model.common.UiState
import com.d104.pnt.util.socket.ChatSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatRoomListViewModel @Inject constructor(
    private val regionManager: RegionCodeManager,
    private val locationRepository: LocationRepository,
    private val chatRepository: ChatRepository,
    private val authRepository: AuthRepository,
    private val chatSocketManager: ChatSocketManager
) : ViewModel() {
    val majorList = regionManager.majorRegions

    private val _selectedMajor = MutableStateFlow("")
    val selectedMajor = _selectedMajor.asStateFlow()

    private val _selectedMiddle = MutableStateFlow("")
    val selectedMiddle = _selectedMiddle.asStateFlow()

    private val _middleList = MutableStateFlow<List<String>>(emptyList())
    val middleList = _middleList.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery = _searchQuery.asStateFlow()

    private val _searchRegionQuery = MutableStateFlow(-1)
    val searchRegionQuery = _searchRegionQuery.asStateFlow()

    private val _listState = MutableStateFlow<UiState<ChatSearchResponse>>(UiState.Idle)
    val listState = _listState.asStateFlow()

    private val _viewMode = MutableStateFlow(ViewMode.Me)
    val viewMode = _viewMode.asStateFlow()

    init {
        setupGlobalChatCallbacks()
        connectSocket()
        getJoinedChatRoom()
    }

    private fun connectSocket() {
        viewModelScope.launch {
            authRepository.getAccessToken().collect { token ->
                if (token.isNotEmpty() && !chatSocketManager.isConnected()) {
                    chatSocketManager.connect(token)
                }
                return@collect
            }
        }
    }

    // 전역 채팅 콜백 설정
    private fun setupGlobalChatCallbacks() {
        chatSocketManager.setOnReconnected { chatRoomId ->

        }
    }

    fun updateSearchQuery(newQuery: String) {
        _searchQuery.value = newQuery
    }

    // 시/도 선택 시 호출
    fun selectMajor(major: String) {
        _selectedMajor.value = major

        _middleList.value = regionManager.getMiddleRegions(major)
        _selectedMiddle.value = ""

    }

    // 시/군/구 선택 시 호출
    fun selectMiddle(middle: String) {
        _selectedMiddle.value = middle
        _viewMode.value = ViewMode.Region

        val code = locationRepository.getRegionCode(_selectedMajor.value, middle)
        _searchRegionQuery.value = code

        fetchChatRoomsByRegion(code)
    }

    private fun clearSearchState() {
        _searchQuery.value = ""
    }

    fun searchChatRoom(
        title: String?,
        regionCode: Int?
    ) {
        if (_viewMode.value == ViewMode.Me) _viewMode.value = ViewMode.Title

        viewModelScope.launch {
            _listState.value = UiState.Loading
            when (val result = chatRepository.searchChatRoom(title, regionCode)) {
                is BaseResult.Success -> {
                    _listState.value = UiState.Success(result.data)
                }

                is BaseResult.Error -> {
                    _listState.value = UiState.Error(result.error.message)
                }
            }
        }
    }

    fun getJoinedChatRoom() {
        clearSearchState()

        _viewMode.value = ViewMode.Me
        viewModelScope.launch {
            _listState.value = UiState.Loading
            when (val result = chatRepository.getJoinedChatRoom()) {
                is BaseResult.Success -> _listState.value = UiState.Success(result.data)
                is BaseResult.Error -> _listState.value = UiState.Error(result.error.message)
            }
        }
    }

    fun switchToRegionMode() {
        val code = _searchRegionQuery.value
        if (code != -1) {
            clearSearchState()
            _viewMode.value = ViewMode.Region
            fetchChatRoomsByRegion(code)
        }
    }

    private fun fetchChatRoomsByRegion(code: Int) {
        _viewMode.value = ViewMode.Region
        viewModelScope.launch {
            _listState.value = UiState.Loading
            when (val result = chatRepository.searchChatRoom(title = null, regionCode = code)) {
                is BaseResult.Success -> _listState.value = UiState.Success(result.data)
                is BaseResult.Error -> _listState.value = UiState.Error(result.error.message)
            }
        }

    }

    fun searchByTitle(title: String) {
        if (title.isBlank()) {
            if (_viewMode.value == ViewMode.Me) getJoinedChatRoom()
            else switchToRegionMode()
            return
        }

        // 검색 직전의 모드 저장
        val currentMode = _viewMode.value

        viewModelScope.launch {
            _listState.value = UiState.Loading

            when (currentMode) {
                ViewMode.Me -> {
                    when (val result = chatRepository.getJoinedChatRoom()) {
                        is BaseResult.Success -> {
                            val filtered = result.data.chats.filter {
                                it.title.contains(
                                    title,
                                    ignoreCase = true
                                )
                            }
                            _listState.value = UiState.Success(result.data.copy(chats = filtered))
                        }

                        is BaseResult.Error -> _listState.value =
                            UiState.Error(result.error.message)
                    }
                }

                else -> {
                    val code =
                        if (currentMode == ViewMode.Region || _selectedMiddle.value.isNotEmpty()) {
                            _searchRegionQuery.value.takeIf { it != -1 }
                        } else {
                            null
                        }

                    when (val result = chatRepository.searchChatRoom(title, code)) {
                        is BaseResult.Success -> _listState.value = UiState.Success(result.data)
                        is BaseResult.Error -> _listState.value =
                            UiState.Error(result.error.message)
                    }
                }
            }
        }
    }


    fun joinChatRoomFromList(
        chatRoomId: Long,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {

            when (val joinResult = chatRepository.joinChatRoom(chatRoomId)) {
                is BaseResult.Success -> {

                    when (val connectResult = chatRepository.connectChatRoom(chatRoomId)) {
                        is BaseResult.Success -> {

                            if (!chatSocketManager.isConnected()) {
                                authRepository.getAccessToken().collect { token ->
                                    if (token.isNotEmpty()) {
                                        chatSocketManager.connect(token)
                                        kotlinx.coroutines.delay(500)
                                    }
                                    return@collect
                                }
                            }

                            onSuccess()
                        }

                        is BaseResult.Error -> {
                        }
                    }
                }

                is BaseResult.Error -> {
                }
            }
        }
    }

    fun refreshCurrentView() {
        when (_viewMode.value) {
            ViewMode.Me -> {
                getJoinedChatRoom()
            }

            ViewMode.Region -> {
                val code = _searchRegionQuery.value
                if (code != -1) {
                    searchChatRoom(title = null, regionCode = code)
                }
            }

            ViewMode.Title -> {
                val code = if (_searchRegionQuery.value != -1) _searchRegionQuery.value else null
                searchChatRoom(title = _searchQuery.value.ifEmpty { null }, regionCode = code)
            }
        }
    }

    enum class ViewMode(val value: String) {
        Me("Me"),
        Title("title"),
        Region("region");
    }
}