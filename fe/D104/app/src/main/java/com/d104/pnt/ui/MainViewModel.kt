package com.d104.pnt.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.pnt.data.repository.AuthRepository
import com.d104.pnt.util.AuthEventBus
import com.d104.pnt.util.SoundPlayer
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class MainViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val authEventBus: AuthEventBus,
    val soundPlayer: SoundPlayer
) : ViewModel() {

    val isLoggedIn: StateFlow<Boolean> = authRepository.isLoggedIn()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    private val _pendingChatRoomId = MutableStateFlow<Long?>(null)
    val pendingChatRoomId = _pendingChatRoomId.asStateFlow()

    // ID 설정 함수
    fun setPendingChatRoomId(roomId: Long?) {
        _pendingChatRoomId.value = roomId
    }

    // 이동 완료 후 초기화
    fun clearPendingChatRoomId() {
        _pendingChatRoomId.value = null
    }
}
