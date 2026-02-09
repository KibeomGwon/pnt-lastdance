package com.d104.pnt.domain.model
import com.d104.pnt.data.remote.model.request.Location

data class WaitingPlayer(
    val id: Long,
    val nickname: String,
    val role: GameRole,
    val isHost: Boolean = false,
    val isReady: Boolean = false,
    val isChangingRole: Boolean = false,
    val profileUrl: String? = null,
)

data class GameRoomInfoState(
    val roomCode: String = "",
    val maxCount: Int = 0,
    val policeCount: Int = 0,
    val thiefCount: Int = 0,
    val timeLimit: Int = 0,
    val missionCount: Int = 5,
    val cctvCycle: Int = 10,
    val prison: Location? = null,
    val polygon: List<Location>? = null
)

// UI 이벤트 정의
sealed interface GameRoomUiEvent {
    data class NavigateToHome(val message: String? = null) : GameRoomUiEvent
    data class NavigateToGame(
        val roomId: Long,
        val role: String,
        val isChief: Boolean
    ) : GameRoomUiEvent

}