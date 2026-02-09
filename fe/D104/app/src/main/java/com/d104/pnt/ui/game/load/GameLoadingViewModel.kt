package com.d104.pnt.ui.game.load

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.pnt.domain.model.GameRole
import com.d104.pnt.util.socket.GameSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GameLoadingViewModel @Inject constructor(
    private val gameSocketManager: GameSocketManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val KEY_ROLE = "role"
        private const val TOTAL_SECONDS = 60
    }

    val role: GameRole = savedStateHandle.get<String>(KEY_ROLE)?.let {
        GameRole.fromName(it)
    } ?: GameRole.THIEF

    private val _remainingTime = MutableStateFlow(TOTAL_SECONDS)
    val remainingTime: StateFlow<Int> = _remainingTime

    private val _isFinished = MutableStateFlow(false)
    val isFinished: StateFlow<Boolean> = _isFinished

    private val _message = MutableStateFlow(getInitialMessage())
    val message: StateFlow<String> = _message

    private var gameStartTime: Long? = null

    init {
        setupGameStartListener()

        startCountdown()
    }

    private fun setupGameStartListener() {
        gameSocketManager.setOnGameStarted { gameId, startTime ->
            gameStartTime = System.currentTimeMillis()
            _remainingTime.value = TOTAL_SECONDS
        }

        gameSocketManager.setOnGameInfoSynced { data ->
            val startTime = data.optLong("startTime")
            val currentTime = System.currentTimeMillis()

            val elapsedSeconds = (currentTime - startTime) / 1000
            val remaining = (TOTAL_SECONDS - elapsedSeconds).toInt()

            if (remaining > 0) {
                _remainingTime.value = remaining
            } else {
                _isFinished.value = true
            }
        }
    }

    private fun startCountdown() {
        viewModelScope.launch {
            while (_remainingTime.value > 0) {
                delay(1000L)
                _remainingTime.value--
                updateMessage()
            }
            _isFinished.value = true
        }
    }

    private fun getInitialMessage(): String {
        return when (role) {
            GameRole.POLICE -> "경찰 팀원들과 무전기로 소통하며\n도둑들을 체포하세요!"
            GameRole.THIEF -> "경찰을 피해 미션을 완수하고\n생존하세요!"
            else -> "곧 게임이 시작됩니다!\n역할을 확인하세요."
        }
    }

    private fun updateMessage() {
        _message.value = when {
            _remainingTime.value > 45 -> getInitialMessage()
            _remainingTime.value > 30 -> when (role) {
                GameRole.POLICE -> "도둑들의 위치를 파악하세요"
                GameRole.THIEF -> "은신 장소를 찾으세요"
                else -> "주변을 탐색하세요"
            }

            _remainingTime.value > 10 -> when (role) {
                GameRole.POLICE -> "팀원들과 협력하세요"
                GameRole.THIEF -> "경찰의 포위망을 조심하세요"
                else -> "준비하세요"
            }

            else -> "곧 게임이 시작됩니다!"
        }
    }
}