package com.d104.pnt.data.repository

import com.d104.pnt.base.Constants
import com.d104.pnt.data.remote.model.response.BeepUseResponse
import com.d104.pnt.data.remote.model.response.GameMemberSocketDto
import com.d104.pnt.data.remote.model.response.MemberLocationSocketDto
import com.d104.pnt.data.remote.model.response.MissionSocketDto
import com.d104.pnt.domain.model.common.BaseResult
import com.d104.pnt.util.StepSensorManager
import com.d104.pnt.util.socket.GameSocketManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.max

@Singleton
class GameSessionRepositoryImpl @Inject constructor(
    private val gameSocketManager: GameSocketManager,
    private val stepSensorManager: StepSensorManager,
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository,
    private val imageRepository: ImageRepository,
    private val walkieRepository: WalkieRepository,
    private val gameRepository: GameRepository
) : GameSessionRepository {
    private val repositoryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val WARNING_TIME = 5
    private val HELICOPTER_TIME = 10
    private val CCTV_TIME = 10

    private val _gameId = MutableStateFlow(0L)
    override val gameId = _gameId.asStateFlow()
    private val _gameStatus = MutableStateFlow("")
    override val gameStatus = _gameStatus.asStateFlow()

    private val _TotalTime = MutableStateFlow(0)
    override val TotalTime = _TotalTime.asStateFlow()

    private val _remainingTime = MutableStateFlow(0)
    override val remainingTime = _remainingTime.asStateFlow()

    private val _gameTime = MutableStateFlow(0)
    override val gameTime = _gameTime.asStateFlow()

    private val _cctvInterval = MutableStateFlow(0)
    override val cctvInterval = _cctvInterval.asStateFlow()

    private val _cctvPhase = MutableStateFlow(CctvPhase.IDLE)
    override val cctvPhase = _cctvPhase.asStateFlow()

    private val _cctvThiefId = MutableStateFlow<Long?>(null)
    override val cctvThiefId = _cctvThiefId.asStateFlow()

    private val _boundaryWarningTargets = MutableStateFlow<MutableList<Long>>(mutableListOf())

    private val _onBoundaryWarning = MutableStateFlow<List<Long>>(emptyList())
    override val onBoundaryWarning = _onBoundaryWarning.asStateFlow()

    private val _missions = MutableStateFlow<List<MissionSocketDto>>(emptyList())
    override val missions = _missions.asStateFlow()

    private val _members = MutableStateFlow<List<GameMemberSocketDto>>(emptyList())
    override val members = _members.asStateFlow()

    override val memberCount: StateFlow<Int> = _members
        .map { it.size }
        .stateIn(
            scope = repositoryScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )
    private val _myState = MutableStateFlow<String?>(null)
    override val myState = _myState.asStateFlow()

    private val _memberLocation = MutableStateFlow<List<MemberLocationSocketDto>>(emptyList())
    override val memberLocation = _memberLocation.asStateFlow()

    override val thiefMembers = combine(_members, _memberLocation) { members, locations ->
        members.filter { it.position.equals("THIEF", ignoreCase = true) }
            .map { member ->
                val location = locations.find { it.memberId == member.memberId }
                member.copy(rawStatus = location?.status ?: "FREE")
            }
    }.stateIn(
        scope = repositoryScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    private val _eventFlow = MutableSharedFlow<GameSessionEvent>(
        replay = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    override val eventFlow = _eventFlow.asSharedFlow()

    private val _chiefMemberId = MutableStateFlow<Long?>(null)
    override val chiefMemberId = _chiefMemberId.asStateFlow()

    private val _missionState = MutableStateFlow(MissionStatus.IDLE)
    override val missionState = _missionState.asStateFlow()

    private val _missionFailReason = MutableStateFlow("")
    override val missionFailReason = _missionFailReason.asStateFlow()

    private val _arrestState = MutableStateFlow(ArrestStatus.IDLE)
    override val arrestState = _arrestState.asStateFlow()

    private val _arrestFailReason = MutableStateFlow("")
    override val arrestFailReason = _arrestFailReason.asStateFlow()

    private var isConnecting = false

    private val _myMemberId = MutableStateFlow(0L)
    override val myMemberId = _myMemberId.asStateFlow()

    private val _myRole = MutableStateFlow("")
    override val myRole = _myRole.asStateFlow()

    private val _roomCode = MutableStateFlow("")
    override val roomCode = _roomCode.asStateFlow()

    private val _escapeQueue = MutableStateFlow<List<String>>(emptyList())
    override val escapeQueue = _escapeQueue.asStateFlow()

    private val _beepEvent = MutableSharedFlow<BeepUseResponse>(extraBufferCapacity = 16)
    override val beepEvent = _beepEvent.asSharedFlow()

    private val _lastEscapeTime = MutableStateFlow(60)
    private val _survivalTime = MutableStateFlow(0)
    override val survivalTime = _survivalTime.asStateFlow()

    private val _longestSurvivalTime = MutableStateFlow(0)
    override val longestSurvivalTime = _longestSurvivalTime.asStateFlow()

    private val _skillUsedAt = MutableStateFlow<Int?>(null)
    override val skillUsedAt = _skillUsedAt.asStateFlow()

    private val _helicopterUsed = MutableStateFlow(false)
    override val helicopterUsed: StateFlow<Boolean> = _helicopterUsed.asStateFlow()

    override val isChief: StateFlow<Boolean> =
        combine(myMemberId, chiefMemberId) { myId, chiefId ->
            chiefId != null && myId != 0L && myId == chiefId
        }.stateIn(repositoryScope, SharingStarted.WhileSubscribed(5000), false)


    override val helicopterButtonEnabled: StateFlow<Boolean> =
        combine(isChief, helicopterUsed) { chief, used ->
            chief && !used
        }.stateIn(repositoryScope, SharingStarted.WhileSubscribed(5000), false)
    private val _helicopterState = MutableStateFlow(HelicopterPhase.IDLE)
    override val helicopterState = _helicopterState.asStateFlow()

    private val _connectedCount = MutableStateFlow(1)
    override val connectedCount: StateFlow<Int> = _connectedCount

    // 무전기
    private val _walkieState = MutableStateFlow<WalkieConnectionState>(WalkieConnectionState.Idle)
    override val walkieState = _walkieState.asStateFlow()

    private val _isSomeoneTalking = MutableStateFlow(false)
    override val isSomeoneTalking = _isSomeoneTalking.asStateFlow()


    private val _isTransmitting = MutableStateFlow(false)
    override val isTransmitting = _isTransmitting.asStateFlow()

    private val _talkingMemberId = MutableStateFlow<Long?>(null)
    override val talkingMemberId = _talkingMemberId.asStateFlow()

    override fun setMemberId(memberId: Long) {
        _myMemberId.value = memberId
    }

    override fun setFinalRole(role: String) {
        _myRole.value = role
    }

    override fun setRoomCode(code: String) {
        _roomCode.value = code
    }

    override fun setTotalTime(minutes: Int) {
        _TotalTime.value = minutes
    }

    override fun setCctvInterval(interval: Int) {
        _cctvInterval.value = interval
    }

    private var gpsJob: Job? = null
    private var warningJob: Job? = null
    private var gameStartTime: Long = 0L
    private var radioTimeoutJob: Job? = null
    private var pttHeartbeatJob: Job? = null


    override fun gameInit() {
        repositoryScope.launch {
            setupSocketListeners()

            delay(500L)
            gameSocketManager.syncGameInfo()
        }
        stepSensorManager.startListening()
    }

    override fun connectAndJoin(gameId: Long) {
        if (isConnecting || gameSocketManager.isConnected()) return

        repositoryScope.launch {
            isConnecting = true

            try {
                gameSocketManager.removeAllListeners()
                setupSocketListeners()

                val token = authRepository.getAccessToken().first()
                gameSocketManager.connect(token)

                var retry = 0
                while (!gameSocketManager.isConnected() && retry < 30) {
                    delay(100)
                    retry++
                }

                if (gameSocketManager.isConnected()) {
                    gameSocketManager.joinGame(gameId)
                }
            } finally {
                isConnecting = false
            }
        }
    }

    private fun setupSocketListeners() {
        gameSocketManager.setOnGameStarted { gameId, startTime ->
            _gameId.value = gameId
            repositoryScope.launch {
                _eventFlow.emit(GameSessionEvent.GameStarted(gameId, startTime))
            }
        }
        gameSocketManager.setOnGpsReceived {cctvThiefId, skillUsedAt, sec, locations ->
            _gameTime.value = sec
            _cctvThiefId.value = cctvThiefId
            _remainingTime.value = (_TotalTime.value*60) - sec
            _onBoundaryWarning.value = _boundaryWarningTargets.value.toList()
            _boundaryWarningTargets.value.clear()

            if (_myRole.value == "THIEF" && (_myState.value == "null" || _myState.value == "FREE")) {
                _survivalTime.value = sec - _lastEscapeTime.value
                _longestSurvivalTime.value = max(_longestSurvivalTime.value, _survivalTime.value)
            }

            if (_cctvInterval.value != 0 && _gameTime.value > 100) {
                if (_gameTime.value % (_cctvInterval.value * 60) < WARNING_TIME) {
                    _cctvPhase.value = CctvPhase.NOTIFY
                } else if (_gameTime.value % (_cctvInterval.value * 60) < WARNING_TIME + CCTV_TIME) {
                    _cctvPhase.value = CctvPhase.REVEAL
                } else {
                    _cctvPhase.value = CctvPhase.IDLE
                }
            }

            // 경찰 헬기
            try {
                if (skillUsedAt != null && _skillUsedAt.value == null) {
                    _skillUsedAt.value = sec
                    _helicopterState.value = HelicopterPhase.NOTIFY
                }
                if (_helicopterState.value == HelicopterPhase.NOTIFY &&
                    sec >= _skillUsedAt.value!! + WARNING_TIME
                ) {
                    _helicopterState.value = HelicopterPhase.REVEAL
                }
                if (_helicopterState.value == HelicopterPhase.REVEAL &&
                    sec > _skillUsedAt.value!! + WARNING_TIME + HELICOPTER_TIME
                ) {
                    _helicopterState.value = HelicopterPhase.IDLE
                }
            } catch (e: Exception) {
                _skillUsedAt.value = sec
                _helicopterState.value = HelicopterPhase.IDLE
            }

            try {
                if (locations.length() != 0) {
                    val newMemberLocation = mutableListOf<MemberLocationSocketDto>()
                    for (i in 0 until locations.length()) {
                        val locationJson = locations.getJSONObject(i)
                        if (locationJson.optLong("memberId") == _myMemberId.value) {
                            _myState.value = locationJson.optString("status")
                        }
                        if (cctvThiefId != null
                            && locationJson.optLong("memberId") == cctvThiefId
                            && _cctvPhase.value == CctvPhase.REVEAL
                        ) {
                            locationJson.put("status", "CCTV")
                        }
                        if (_helicopterState.value == HelicopterPhase.REVEAL
                            && locationJson.optString("position") != "POLICE"
                            && (locationJson.optString("status") == "null"
                                    || locationJson.optString("status") == "FREE")) {
                            locationJson.put("status", "CCTV")
                        }
                        newMemberLocation.add(MemberLocationSocketDto.fromJson(locationJson))
                    }
                    _memberLocation.value = newMemberLocation
                }
            }
            catch (e:Exception) {
            }
        }

        gameSocketManager.setOnJoinedRoom { gameId, memberId, message, count ->
            _gameId.value = gameId
            _connectedCount.value = count
        }

        gameSocketManager.setOnWillStartGame { gameId, willStartAt ->
        }

        // 전체 게임 정보 동기화
        gameSocketManager.setOnGameInfoSynced { data ->
            try {
                val gameId = data.optLong("gameId")
                if (gameId != 0L) _gameId.value = gameId

                val gameStatus = data.optString("gameStatus")
                if (gameStatus != "") _gameStatus.value = gameStatus

                val membersArray = data.optJSONArray("members")
                if (membersArray != null) {
                    val newMembers = mutableListOf<GameMemberSocketDto>()
                    for (i in 0 until membersArray.length()) {
                        val memberJson = membersArray.getJSONObject(i)
                        newMembers.add(GameMemberSocketDto.fromJson(memberJson))
                    }

                    _members.value = newMembers
                }
                val missionArray = data.optJSONArray("missions")
                if (missionArray != null) {
                    val newMissions = mutableListOf<MissionSocketDto>()
                    for (i in 0 until missionArray.length()) {
                        val missionJson = missionArray.getJSONObject(i)
                        newMissions.add(MissionSocketDto.fromJson(missionJson))
                    }
                    _missions.value = newMissions
                }
            } catch (e: Exception) {
            }
        }

        // 실시간 상태 변경
        gameSocketManager.setOnMemberStatusChanged { gameId, thiefId, status, arrestedAt ->
            repositoryScope.launch {
                _members.update { currentList ->
                    currentList.map { member ->
                        if (member.memberId == thiefId) {
                            member.copy(rawStatus = status)
                        }
                        else member
                    }
                }
            }
        }

        // 도둑 탈출
        gameSocketManager.setOnThiefEscaped { gameId, thiefId, escapedAt ->
            repositoryScope.launch {

                val escapedThief = members.value.find { it.memberId == thiefId }
                val thiefNickname = escapedThief?.nickname ?: "도둑"

                if (_myMemberId.value == thiefId) {
                    _lastEscapeTime.value = _gameTime.value
                }

                _escapeQueue.update {it + thiefNickname}
            }
        }

        // 비프음
        gameSocketManager.setOnBeepReceived { policeId, thiefId, distance ->
            _beepEvent.tryEmit(
                BeepUseResponse(
                    policeId = policeId,
                    thiefId = thiefId,
                    distance = distance
                )
            )
        }

        // 게임 종료
        gameSocketManager.setOnGameEnded { winnerPosition, _ ->
            repositoryScope.launch {
                // PTT heartbeat 즉시 중지
                pttHeartbeatJob?.cancel()
                pttHeartbeatJob = null

                walkieRepository.disableMic()

                _isTransmitting.value = false
                _isSomeoneTalking.value = false

            }

            stopGameSession()
            gameSocketManager.postAfterGameEnd(_gameId.value)
        }

        // 상세 결과
        gameSocketManager.setOnEndGameAfter { data ->
            repositoryScope.launch {
                _eventFlow.emit(GameSessionEvent.NavigateToLoading(_gameId.value))
            }
        }

        // 뉴스 생성 완료
        gameSocketManager.setOnNewsReceived { gameId, newsId ->
            repositoryScope.launch {
                _eventFlow.emit(GameSessionEvent.NavigateToNews(gameId, newsId))
            }
        }

        // 경게 벗어남 이벤트
        gameSocketManager.setOnOutOfBoundary { gameId, memberId ->
            _boundaryWarningTargets.value.add(memberId)
        }

        // 게임 미션 결과
        gameSocketManager.setOnMissionResult { gameId, missionId, thiefId, success, reason, completedAt ->
            repositoryScope.launch {
                if (success) {
                    if (_myMemberId.value == thiefId) {
                        _missionState.value = MissionStatus.SUCCESS
                    }
                    val missionList = mutableListOf<MissionSocketDto>()
                    _missions.value.forEach { mission ->
                        if (mission.id == missionId) {
                            val newMission = mission.copy(status = "SUCCESS")
                            missionList.add(newMission)
                        } else {
                            missionList.add(mission)
                        }
                    }
                    _missions.value = missionList
                } else {
                    if (_myMemberId.value == thiefId) {
                        _missionState.value = MissionStatus.FAIL
                        _missionFailReason.value = when (reason) {
                            "ALREADY_COMPLETED" -> "다른 도둑에게 이 미션을 빼앗겼습니다!!"
                            "NOT_MATCHED" -> "목표를 찾지 못했습니다!"
                            else -> "사진을 인식하지 못했습니다!"
                        }
                    }
                }
                delay(5000L)
                _missionState.value = MissionStatus.IDLE
            }

        }

        // 스킬 결과
        gameSocketManager.setOnSkillResult { result, reason, policeId, startedAt ->
            val success = result.equals("SUCCESS", ignoreCase = true)

            if (success) {
                _helicopterUsed.value = true
            } else {
                val myId = _myMemberId.value
                if (policeId == myId) {
                    _helicopterUsed.value = false
                }
            }
        }

        gameSocketManager.setOnRadioReceived { _, memberId ->
            handleRadioSignal(memberId)
        }

        gameSocketManager.setOnArrestResult { result, reason, policeId, thiefId, _ ->
            if (policeId == null || policeId != myMemberId.value) {
                return@setOnArrestResult
            }
            if (result == "SUCCESS") {
                repositoryScope.launch{
                    _arrestState.value = ArrestStatus.SUCCESS
                    delay(5000L)
                    _arrestState.value = ArrestStatus.IDLE
                }
            }
            else {
                repositoryScope.launch{
                    _arrestState.value = ArrestStatus.FAIL
                    delay(5000L)
                    _arrestState.value = ArrestStatus.IDLE
                }
            }
        }
    }

    override fun startGameSession() {
        if (gpsJob?.isActive == true) return

        gpsJob?.cancel()

        stepSensorManager.resetGameSteps()
        gameStartTime = System.currentTimeMillis()

        gpsJob = repositoryScope.launch {
            gameId.first { it > 0 }
            while (isActive) {
                val location = locationRepository.currentLocation.value
                val steps = stepSensorManager.stepCountFlow.value

                if (location != null) {

                    gameSocketManager.sendGPS(
                        lat = location.latitude,
                        lng = location.longitude,
                        walk = steps,
                        longestSurvived = _longestSurvivalTime.value
                    )
                }
                delay(1000L)
            }
        }
    }

    private var _cachedPlayerNicknames: List<String> = emptyList()

    override fun uploadMissionImage(image: File, missionId: Long) {
        repositoryScope.launch {
            _missionState.value = MissionStatus.IN_ANALYZE
            delay(3000L)
            when (val result = imageRepository.getPresignedUrlToMission(image.name)) {
                is BaseResult.Success -> {
                    imageRepository.uploadImage(result.data.presignedUrl, image)
                    gameSocketManager.submitMissionImage(
                        authRepository.getMemberId().first(),
                        missionId,
                        result.data.downloadUrl
                    )
                }
                is BaseResult.Error -> {
                }
            }
        }
    }

    override fun arrestThief(thiefId: Long) {
        gameSocketManager.arrestThief(
            policeId = _myMemberId.value,
            thiefId = thiefId,
        )
    }

    override fun stopGameSession() {
        gpsJob?.cancel()
        gpsJob = null
        gameStartTime = 0L
        stepSensorManager.stopListening()
    }

    override fun leaveGame() {
        stopGameSession()

        gameSocketManager.leaveGame()
        gameSocketManager.removeAllListeners()
        _members.value = emptyList()
        _gameId.value = 0L
        _gameStatus.value = ""
        _gameTime.value = 0
        _cctvInterval.value = 0
        _cctvPhase.value = CctvPhase.IDLE
        _cctvThiefId.value = null
        _missions.value = emptyList()
        _myMemberId.value = 0L
        _myRole.value = ""
        _roomCode.value = ""
        _memberLocation.value = emptyList()
        _chiefMemberId.value = null
        _missionState.value = MissionStatus.IDLE
        _missionFailReason.value = ""
        _arrestState.value = ArrestStatus.IDLE
        _arrestFailReason.value = ""
        _helicopterUsed.value = false
        isConnecting = false
        _lastEscapeTime.value = 0
        _survivalTime.value = 0
        _longestSurvivalTime.value = 0
        _skillUsedAt.value = null
        _helicopterState.value = HelicopterPhase.IDLE
        _escapeQueue.value = emptyList()
        _walkieState.value = WalkieConnectionState.Idle
        _isSomeoneTalking.value = false
        _isTransmitting.value = false
        _talkingMemberId.value = null
        gpsJob = null
        warningJob?.cancel()
        warningJob = null
        radioTimeoutJob?.cancel()
        radioTimeoutJob = null
        pttHeartbeatJob?.cancel()
        pttHeartbeatJob = null
    }

    override fun setChiefMemberId(id: Long?) {
        _chiefMemberId.value = id
    }

    override fun dequeEscape() {
        _escapeQueue.update { currentQueue ->
            if (currentQueue.isNotEmpty()) {
                currentQueue.drop(1)
            } else {
                currentQueue
            }
        }
    }

    override fun connectWalkie() {
        if (_myRole.value != "POLICE") {
            return
        }

        repositoryScope.launch {
            try {
                _walkieState.value = WalkieConnectionState.Connecting

                val roomCode = roomCode.first()
                if (roomCode.isBlank()) {
                    _walkieState.value = WalkieConnectionState.Error("방 코드가 없습니다")
                    return@launch
                }

                when (val result = gameRepository.getLiveKitToken(roomCode)) {
                    is BaseResult.Success -> {
                        val tokenResponse = result.data

                        walkieRepository.connect(
                            serverUrl = Constants.LIVEKIT_URL,
                            token = tokenResponse.token,
                            roomName = tokenResponse.roomCode
                        )

                        _walkieState.value = WalkieConnectionState.Connected
                    }

                    is BaseResult.Error -> {
                        val errorMsg = result.error.message ?: "토큰 발급 실패"
                        _walkieState.value = WalkieConnectionState.Error(errorMsg)
                    }
                }
            } catch (e: Exception) {
                _walkieState.value = WalkieConnectionState.Error(e.message ?: "연결 실패")
            }
        }
    }

    override fun disconnectWalkie() {
        repositoryScope.launch {
            try {
                walkieRepository.disconnect()
                _walkieState.value = WalkieConnectionState.Idle
            } catch (e: Exception) {
            }
        }
    }
    private fun handleRadioSignal(memberId: Long) {
        if (_isTransmitting.value) {
            return
        }

        if (memberId == myMemberId.value || myMemberId.value == 0L) {
            return
        }

        repositoryScope.launch {
            _isSomeoneTalking.value = true
            _talkingMemberId.value = memberId

            radioTimeoutJob?.cancel()
            radioTimeoutJob = launch {
                delay(1500)
                _isSomeoneTalking.value = false
                _talkingMemberId.value = null
            }
        }
    }

    override fun startTalking() {
        if (_myRole.value != "POLICE") return

        if (_isSomeoneTalking.value) {
            return
        }

        repositoryScope.launch {
            try {
                _isTransmitting.value = true

                walkieRepository.enableMic()

                pttHeartbeatJob?.cancel()
                pttHeartbeatJob = launch {
                    while (isActive) {
                        gameSocketManager.sendRadio()
                        delay(1000)
                    }
                }
            } catch (e: Exception) {
                _isTransmitting.value = false
            }
        }
    }

    override fun stopTalking() {
        if (_myRole.value != "POLICE") return

        repositoryScope.launch {
            try {
                if (pttHeartbeatJob == null && !_isTransmitting.value) {
                    return@launch
                }

                pttHeartbeatJob?.cancel()
                pttHeartbeatJob = null

                walkieRepository.disableMic()

                _isTransmitting.value = false

            } catch (e: Exception) {
            }
        }
    }

    override fun useHelicopterSkill() {
        if (_myMemberId.value == 0L || _myMemberId.value != _chiefMemberId.value) {
            return
        }

        if (_helicopterUsed.value) {
            return
        }

        _helicopterUsed.value = true

        gameSocketManager.useSkill(policeId = _myMemberId.value)
    }

    override fun setPlayerNicknames(list: List<String>) {
        _cachedPlayerNicknames = list
    }

    override fun getPlayerNicknames(): List<String> {
        return _cachedPlayerNicknames
    }
}

sealed class GameSessionEvent {

    // 게임 시작
    data class GameStarted(
        val gameId: Long,
        val startTime: String
    ) : GameSessionEvent()

    // 게임 종료
    data object GameEnded : GameSessionEvent()

    // 에러 발생
    data class ErrorOccurred(val message: String) : GameSessionEvent()

    data class NavigateToLoading(val gameId: Long) : GameSessionEvent()
    data class NavigateToNews(val gameId: Long, val newsId: Long) : GameSessionEvent()
}

sealed class WalkieConnectionState {
    object Idle : WalkieConnectionState()
    object Connecting : WalkieConnectionState()
    object Connected : WalkieConnectionState()
    data class Error(val message: String) : WalkieConnectionState()
}
enum class HelicopterPhase {
    IDLE,
    NOTIFY,
    REVEAL
}

enum class CctvPhase {
    IDLE,
    NOTIFY,
    REVEAL
}

enum class MissionStatus {
    IDLE,
    IN_ANALYZE,
    SUCCESS,
    FAIL
}

enum class ArrestStatus {
    IDLE,
    SUCCESS,
    FAIL
}