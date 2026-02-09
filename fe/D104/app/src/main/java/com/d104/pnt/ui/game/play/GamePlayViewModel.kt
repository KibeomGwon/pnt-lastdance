package com.d104.pnt.ui.game.play

import android.content.Context
import android.content.Intent
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.pnt.R
import com.d104.pnt.data.repository.AuthRepository
import com.d104.pnt.data.repository.GameSessionEvent
import com.d104.pnt.data.repository.GameSessionRepository
import com.d104.pnt.data.repository.LocationRepository
import com.d104.pnt.data.repository.WalkieRepository
import com.d104.pnt.service.game.GameActiveService
import com.d104.pnt.util.SoundPlayer
import com.d104.pnt.util.StepSensorManager
import com.d104.pnt.util.socket.GameSocketManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GamePlayViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val locationRepository: LocationRepository,
    private val authRepository: AuthRepository,
    private val gameSocketManager: GameSocketManager,
    savedStateHandle: SavedStateHandle,
    private val gameSessionRepository: GameSessionRepository,
    private val stepSensorManager: StepSensorManager,
    private val walkieRepository: WalkieRepository,
    val soundPlayer: SoundPlayer
) : ViewModel() {
    // UI 이벤트
    private val _uiEvent = MutableSharedFlow<GameSessionEvent>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val uiEvent = _uiEvent.asSharedFlow()

    // 게임 위치정보
    val userLocation = locationRepository.currentLocation
    val polygonPoints = locationRepository.polygonPoints
    val prisonLocation = locationRepository.prisonLocation

    // 게임 스탯 정보
    val gameId = gameSessionRepository.gameId
    val remainingTime = gameSessionRepository.remainingTime
    val myMemberId = gameSessionRepository.myMemberId
    val myRole = gameSessionRepository.myRole
    val myState = gameSessionRepository.myState
    val members = gameSessionRepository.members
    val memberLocation = gameSessionRepository.memberLocation
    val missions = gameSessionRepository.missions
    val missionFailReason = gameSessionRepository.missionFailReason
    val thiefMembers = gameSessionRepository.thiefMembers
    val escapeQueue = gameSessionRepository.escapeQueue
    val isChief = gameSessionRepository.isChief
    val cctvThief = gameSessionRepository.cctvThiefId

    // 게임 상태(Flow) 정보
    val gameStatus = gameSessionRepository.gameStatus
    val missionState = gameSessionRepository.missionState
    val helicopterPhase = gameSessionRepository.helicopterState
    val cctvPhase = gameSessionRepository.cctvPhase
    val arrestStatus = gameSessionRepository.arrestState
    val arrestFailReason = gameSessionRepository.arrestFailReason
    val onBoundaryWarning = gameSessionRepository.onBoundaryWarning
    val beepEvent = gameSessionRepository.beepEvent

    // 무전기
    val walkieConnected = walkieRepository.isConnected
    val walkieMicEnabled = walkieRepository.isMicEnabled
    val walkieParticipantCount = walkieRepository.participantCount
    val walkieState = gameSessionRepository.walkieState
    val isSomeoneTalking = gameSessionRepository.isSomeoneTalking
    val isTransmitting = gameSessionRepository.isTransmitting
    val talkingMemberId = gameSessionRepository.talkingMemberId

    // 경찰 헬기 스킬
    val helicopterState = gameSessionRepository.helicopterState
    val helicopterUsed = gameSessionRepository.helicopterUsed
    val helicopterButtonEnabled = gameSessionRepository.helicopterButtonEnabled

    init {
        gameSessionRepository.gameInit()
        observeRepositoryEvents()
        startService(GameActiveService.ACTION_START)
    }

    private fun observeRepositoryEvents() {
        viewModelScope.launch {
            gameSessionRepository.eventFlow.collect { event ->
                when (event) {
                    is GameSessionEvent.NavigateToLoading -> {
                        _uiEvent.emit(event)
                    }

                    is GameSessionEvent.NavigateToNews -> {
                        _uiEvent.emit(event)
                    }

                    is GameSessionEvent.GameStarted -> {
                        startService(GameActiveService.ACTION_START)
                    }

                    else -> {

                    }
                }
            }
        }
    }

    fun initGame() {
        viewModelScope.launch {
            val token = authRepository.getAccessToken().first()
            if (token.isNotEmpty() && !gameSocketManager.isConnected()) {
                gameSocketManager.connect(token)
                while (!gameSocketManager.isConnected()) {
                    delay(100)
                }
            }
            delay(300)
            gameSocketManager.syncGameInfo()
        }
    }

    fun dequeEscape() {
        viewModelScope.launch {
            gameSessionRepository.dequeEscape()
        }
    }


    private fun startService(action: String) {
        Intent(context, GameActiveService::class.java).also { intent ->
            intent.action = action

            if (action == GameActiveService.ACTION_STOP) {
                context.startService(intent)
            } else {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    context.startForegroundService(intent)
                } else {
                    context.startService(intent)
                }
            }
        }
    }

    fun arrestThief(thiefId: Long) {
        viewModelScope.launch {
            delay(1000L)
            gameSessionRepository.arrestThief(thiefId)
        }
    }

    fun connectWalkie() {
        viewModelScope.launch {
            gameSessionRepository.connectWalkie()
        }
    }

    fun disconnectWalkie() {
        viewModelScope.launch {
            gameSessionRepository.disconnectWalkie()
        }
    }

    fun useHelicopterSkill() {
        viewModelScope.launch {
            gameSessionRepository.useHelicopterSkill()
        }
    }

    fun startTalking() {
        viewModelScope.launch {
            gameSessionRepository.startTalking()
        }
    }

    fun stopTalking() {
        viewModelScope.launch {
            gameSessionRepository.stopTalking()
        }
    }

    fun playWalkieOpenSound() {
        soundPlayer.playSound(R.raw.walkie_open)
    }

    fun playWalkieCloseSound() {
        soundPlayer.playSound(R.raw.walkie_close)
    }

    fun playHelicopterSound() {
        soundPlayer.playSoundWithRepeat(R.raw.helicopter, 2)
    }

    fun manualLeaveGame() {
        viewModelScope.launch {
            startService(GameActiveService.ACTION_STOP)
            gameSessionRepository.leaveGame()
        }
    }

    override fun onCleared() {
        super.onCleared()

        gameSessionRepository.stopTalking()
        gameSessionRepository.disconnectWalkie()

        startService(GameActiveService.ACTION_STOP)
    }
}