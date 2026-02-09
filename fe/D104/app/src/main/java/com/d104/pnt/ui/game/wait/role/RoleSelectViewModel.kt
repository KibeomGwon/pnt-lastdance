package com.d104.pnt.ui.game.wait.role

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.pnt.data.repository.AuthRepository
import com.d104.pnt.data.repository.GameRoomRepository
import com.d104.pnt.domain.model.GameRole
import com.d104.pnt.domain.model.common.BaseResult
import com.d104.pnt.domain.model.common.UiState
import com.d104.pnt.navigation.NavArgs
import com.d104.pnt.util.socket.RoomSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoleSelectViewModel @Inject constructor(
    private val gameRoomRepository: GameRoomRepository,
    private val authRepository: AuthRepository,
    private val roomSocketManager: RoomSocketManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {
    private val roomId: Long = savedStateHandle.get<Long>(NavArgs.ROOM_ID) ?: 0L

    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    init {
        connect()
    }

    private fun connect() {
        viewModelScope.launch {
            authRepository.getAccessToken().collect { token ->
                if (token.isNotEmpty()) {
                    roomSocketManager.currentRoomId = roomId
                    roomSocketManager.connect(token)

                    while (!roomSocketManager.isConnected()) {
                        delay(100)
                    }

                    roomSocketManager.joinRoom(roomId) { _, _ ->
                    }
                }
            }
        }
    }

    fun selectRole(role: GameRole, onSuccess: (GameRole) -> Unit) {
        viewModelScope.launch {
            val roleName = if (role == GameRole.ANY) "THIEF" else role.name
            val result = gameRoomRepository.changePosition(roomId, roleName)
            if (result is BaseResult.Success) {
                roomSocketManager.updatePosition(roleName)
                onSuccess(role)
            }
        }
    }

    fun leaveRoom() {
        viewModelScope.launch {
            try {
                roomSocketManager.leaveRoom()
                gameRoomRepository.leaveRoom(roomId)

            } catch (e: Exception) {

            }
        }
    }
}