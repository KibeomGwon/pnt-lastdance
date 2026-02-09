package com.d104.pnt.ui.game.wait

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.d104.pnt.data.remote.model.request.Location
import com.d104.pnt.data.repository.AuthRepository
import com.d104.pnt.data.repository.GameRoomRepository
import com.d104.pnt.data.repository.GameSessionRepository
import com.d104.pnt.data.repository.LocationRepository
import com.d104.pnt.data.repository.ProfileRepository
import com.d104.pnt.domain.model.DraggableLatLng
import com.d104.pnt.domain.model.GameRole
import com.d104.pnt.domain.model.GameRoomInfoState
import com.d104.pnt.domain.model.GameRoomUiEvent
import com.d104.pnt.domain.model.RoomInfoResponse
import com.d104.pnt.domain.model.WaitingPlayer
import com.d104.pnt.domain.model.common.BaseResult
import com.d104.pnt.domain.model.common.UiState
import com.d104.pnt.navigation.NavArgs
import com.d104.pnt.ui.chatroom.chat.ProfileData
import com.d104.pnt.util.socket.RoomSocketManager
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import org.json.JSONObject
import javax.inject.Inject

@HiltViewModel
class GameRoomViewModel @Inject constructor(
    private val gameRoomRepository: GameRoomRepository,
    private val authRepository: AuthRepository,
    private val locationRepository: LocationRepository,
    private val roomSocketManager: RoomSocketManager,
    private val gameSessionRepository: GameSessionRepository,
    private val profileRepository: ProfileRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val roomId: Long = savedStateHandle.get<Long>(NavArgs.ROOM_ID) ?: 0L

    // 내 Member ID
    private val _myMemberId = MutableStateFlow(0L)
    val myMemberId: StateFlow<Long> = _myMemberId.asStateFlow()

    // 참가자 리스트
    private val _players = MutableStateFlow<List<WaitingPlayer>>(emptyList())
    val players: StateFlow<List<WaitingPlayer>> = _players.asStateFlow()

    // 방 정보
    private val _roomInfo = MutableStateFlow(GameRoomInfoState())
    val roomInfo: StateFlow<GameRoomInfoState> = _roomInfo.asStateFlow()

    private val _isHost = MutableStateFlow(false)
    val isHost: StateFlow<Boolean> = _isHost.asStateFlow()

    private val _isMeReady = MutableStateFlow(false)
    val isMeReady: StateFlow<Boolean> = _isMeReady.asStateFlow()

    // 로딩/에러 상태 관리
    private val _uiState = MutableStateFlow<UiState<Unit>>(UiState.Idle)
    val uiState: StateFlow<UiState<Unit>> = _uiState.asStateFlow()

    private val _uiEvent = MutableSharedFlow<GameRoomUiEvent>()
    val uiEvent: SharedFlow<GameRoomUiEvent> = _uiEvent.asSharedFlow()

    // 프로필
    private val _selectedProfile = MutableStateFlow<ProfileData?>(null)
    val selectedProfile: StateFlow<ProfileData?> = _selectedProfile.asStateFlow()

    private val _isProfileLoading = MutableStateFlow(false)
    val isProfileLoading: StateFlow<Boolean> = _isProfileLoading.asStateFlow()

    // 역할 변경 중 상태
    private val _changingRoleMemberIds = MutableStateFlow<Set<Long>>(emptySet())

    private var isJoined = false

    init {
        roomSocketManager.currentRoomId = roomId
        setupRoomCallbacks()

        viewModelScope.launch {
            authRepository.getMemberId().collect { id ->
                if (id != 0L) {
                    _myMemberId.value = id
                    syncData()
                }
            }
        }

        loadRoomSettings()
    }

    private fun syncData() {
        if (isJoined) return
        isJoined = true

        viewModelScope.launch {
            val token = authRepository.getAccessToken().first()

            if (token.isNotEmpty()) {
                roomSocketManager.currentRoomId = roomId
                roomSocketManager.connect(token)

                var retryCount = 0
                while (!roomSocketManager.isConnected() && retryCount < 50) {
                    delay(200)
                    retryCount++
                }

                if (roomSocketManager.isConnected()) {
                    roomSocketManager.joinRoom(roomId) { success, _ ->
                        if (success) {

                        }
                    }
                } else {
                    _uiState.value = UiState.Error("소켓 연결에 실패했습니다.")
                }
            }
        }
    }

    fun resetToUndecided() {
        viewModelScope.launch {
            val result = gameRoomRepository.changePosition(roomId, "ANY")
            if (result is BaseResult.Success) {
                roomSocketManager.updatePosition("ANY")
            }
        }
    }

    /**
     * Socket 콜백 설정
     */
    private fun setupRoomCallbacks() {
        // 전체 방 정보 수신
        roomSocketManager.setOnFullRoomInfoReceived { data ->
            parseFullRoomInfo(data)
            _uiState.value = UiState.Idle
        }

        // 방 설정 업데이트
        roomSocketManager.setOnRoomInfoUpdated { data ->
            parseRoomSettings(data)
        }

        // Ready 상태 업데이트
        roomSocketManager.setOnReadyUpdated { roomId, memberId, isReady ->
            updatePlayerReady(memberId, isReady)
        }

        // 포지션 업데이트
        roomSocketManager.setOnPositionUpdated { roomId, memberId, position ->
            updatePlayerPosition(memberId, position)
        }

        // 멤버 강퇴
        roomSocketManager.setOnMemberKicked { memberId ->
            if (memberId == _myMemberId.value) {
                viewModelScope.launch {
                    _uiEvent.emit(GameRoomUiEvent.NavigateToHome("방에서 강퇴되었습니다."))
                }
            } else {
                removePlayer(memberId)
            }
        }

        roomSocketManager.setOnMemberLeft { memberId ->
            if (memberId == _myMemberId.value) {
                viewModelScope.launch {
                    _uiEvent.emit(GameRoomUiEvent.NavigateToHome("연결이 종료되었습니다."))
                }
            } else {
                removePlayer(memberId)
                roomSocketManager.requestRoomInfo(roomId)
            }
        }

        // 토큰 갱신
        roomSocketManager.setOnTokenUpdated { newToken ->
            viewModelScope.launch {
                val refreshToken = authRepository.getRefreshToken().first()
                authRepository.refreshTokens(newToken, refreshToken)
                roomSocketManager.updateAccessToken(newToken)
            }
        }

        // 방장 위임
        roomSocketManager.setOnOwnerDelegated { data ->
            val code = data.optInt("code", 200)
            if (code == 200) {
                roomSocketManager.requestRoomInfo(roomId)
            } else {
                val msg = data.optString("message", "위임 실패")
                _uiState.value = UiState.Error(msg)
            }
        }

        // 게임 시작
        roomSocketManager.setOnGameStarted { data ->
            viewModelScope.launch {
                try {
                    val currentCode = _roomInfo.value.roomCode
                    gameSessionRepository.setRoomCode(currentCode)

                    val currentNicknames = _players.value.map { it.nickname }
                    gameSessionRepository.setPlayerNicknames(currentNicknames)

                    // 경찰청장
                    val chiefId =
                        data.optLong("chiefMemberId", 0L).let { if (it == 0L) null else it }
                    gameSessionRepository.setChiefMemberId(chiefId)

                    val membersArray = data.optJSONArray("members") ?: return@launch
                    val myId = _myMemberId.value
                    var myFinalRole = "ANY"

                    for (i in 0 until membersArray.length()) {
                        val member = membersArray.getJSONObject(i)
                        if (member.optLong("memberId") == myId) {
                            myFinalRole = member.optString("givenPosition", "ANY")
                            break
                        }
                    }

                    val amIChief = (chiefId != null && chiefId == myId)

                    gameSessionRepository.setFinalRole(myFinalRole)
                    gameSessionRepository.setMemberId(myId)
                    gameSessionRepository.setTotalTime(_roomInfo.value.timeLimit)
                    gameSessionRepository.setCctvInterval(roomInfo.value.cctvCycle)
                    locationRepository.setPrisonLocation(
                        LatLng(
                            _roomInfo.value.prison!!.lat,
                            _roomInfo.value.prison!!.lng
                        )
                    )
                    locationRepository.setPolygonPoints(_roomInfo.value.polygon!!.map {
                        LatLng(
                            it.lat,
                            it.lng
                        )
                    })


                    delay(300)
                    roomSocketManager.disconnect()

                    _uiEvent.emit(
                        GameRoomUiEvent.NavigateToGame(
                            roomId = roomId,
                            role = myFinalRole,
                            isChief = amIChief
                        )
                    )
                } catch (e: Exception) {

                }
            }
        }

        // 멤버 퇴장
        roomSocketManager.setOnMemberLeft { memberId ->
            removePlayer(memberId)
        }
    }

    // 방 설정 조회
    private fun loadRoomSettings() {
        viewModelScope.launch {
            when (val result = gameRoomRepository.getRoomSettings(roomId)) {
                is BaseResult.Success -> {
                    val data = result.data

                    val cachedRoom = gameRoomRepository.getCurrentGameRoom().value

                    val finalRoomCode = if (!data.roomCode.isNullOrEmpty()) {
                        data.roomCode
                    } else {
                        cachedRoom?.roomCode ?: ""
                    }

                    val prisonLocation = Location(data.prisonLat, data.prisonLng)

                    _roomInfo.value = _roomInfo.value.copy(
                        roomCode = finalRoomCode,

                        maxCount = data.playerCount,
                        timeLimit = data.timeLimit,
                        missionCount = data.missionCount,
                        cctvCycle = data.cctvInterval,
                        policeCount = data.policeCount,
                        thiefCount = data.thiefCount,

                        prison = prisonLocation,
                        polygon = _roomInfo.value.polygon
                    )
                }

                is BaseResult.Error -> {

                }
            }
        }
    }


    /**
     * 준비 상태 변경
     */
    fun toggleReady() {
        viewModelScope.launch {
            val nextState = !_isMeReady.value

            when (gameRoomRepository.toggleReady(roomId, nextState)) {
                is BaseResult.Success -> {
                    roomSocketManager.updateReady(roomId, nextState)
                }

                is BaseResult.Error -> {
                    _uiState.value = UiState.Error("준비 상태 변경 실패")
                }
            }
        }
    }

    /**
     * 전체 방 정보 파싱
     */
    private fun parseFullRoomInfo(data: RoomInfoResponse) {
        try {
            val hostId = data.room.hostMemberId
            val myId = _myMemberId.value
            val changingMemberIds = _changingRoleMemberIds.value

            val isMeInList = data.members.any { it.memberId == myId }
            if (myId != 0L && !isMeInList) {
                viewModelScope.launch { _uiEvent.emit(GameRoomUiEvent.NavigateToHome("강퇴되었습니다!")) }
                return
            }

            _players.value = data.members.distinctBy { it.memberId }.map { member ->

                val isMe = member.memberId == myId

                val isThisMemberHost = (member.memberId == hostId)

                // 포지션 결정
                val displayRoleString =
                    if (!member.givenPosition.isNullOrEmpty() && member.givenPosition != "ANY") {
                        member.givenPosition
                    } else {
                        member.preferPosition
                    }

                val isChangingRole = changingMemberIds.contains(member.memberId)
                val adjustedReady = if (isThisMemberHost) {
                    !isChangingRole
                } else {
                    member.ready
                }

                // 내 상태 업데이트
                if (isMe) {
                    _isHost.value = isThisMemberHost
                    _isMeReady.value = adjustedReady
                }

                val avatarUrl = member.memberDetail.profile.avatarUrl


                WaitingPlayer(
                    id = member.memberId,
                    nickname = member.memberDetail.profile.nickname,
                    role = GameRole.fromName(displayRoleString ?: "ANY"),
                    isReady = adjustedReady,
                    profileUrl = member.memberDetail.profile.avatarUrl,
                    isChangingRole = isChangingRole,
                    isHost = isThisMemberHost
                )
            }

            if (myId != 0L && _players.value.none { it.id == myId }) {
                viewModelScope.launch {
                    _uiEvent.emit(GameRoomUiEvent.NavigateToHome("방에서 강퇴되었습니다."))
                }
                return
            }

            _roomInfo.value = _roomInfo.value.copy(
                roomCode = data.room.roomCode,

                maxCount = data.roomSetting.playerCount,
                timeLimit = data.roomSetting.timeLimit,
                missionCount = _roomInfo.value.missionCount,
                cctvCycle = _roomInfo.value.cctvCycle,
                policeCount = data.roomSetting.policeCount,
                thiefCount = data.roomSetting.thiefCount,

                prison = Location(data.roomSetting.prisonLat, data.roomSetting.prisonLng),
                polygon = data.roomSetting.boundaryGeo.coordinates[0].map { Location(it[1], it[0]) }
            )

            val currentCode = _roomInfo.value.roomCode
            gameSessionRepository.setRoomCode(currentCode)

        } catch (e: Exception) {
            _uiState.value = UiState.Error("방 정보 파싱 실패")
        }
    }

    /**
     * 방 설정 파싱
     */
    private fun parseRoomSettings(data: JSONObject) {
        try {
            if (data.has("code") && data.getInt("code") != 200) {
                val msg = data.optString("message", "설정 변경 실패")
                return
            }

            val actualData = data.optJSONObject("data") ?: data

            _roomInfo.value.prison?.let { currentPrison ->
                val polygonArray = actualData.optJSONArray("polygon")
                val updatedPolygon = if (polygonArray != null) {
                    val list = mutableListOf<Location>()
                    for (i in 0 until polygonArray.length()) {
                        val obj = polygonArray.getJSONObject(i)
                        list.add(Location(obj.getDouble("lat"), obj.getDouble("lng")))
                    }
                    list
                } else {
                    _roomInfo.value.polygon
                }

                _roomInfo.value = _roomInfo.value.copy(
                    maxCount = actualData.optInt("playerCount", _roomInfo.value.maxCount),
                    policeCount = actualData.optInt("policeCount", _roomInfo.value.policeCount),
                    thiefCount = actualData.optInt("thiefCount", _roomInfo.value.thiefCount),
                    timeLimit = actualData.optInt("timeLimit", _roomInfo.value.timeLimit),
                    missionCount = actualData.optInt("missionCount", _roomInfo.value.missionCount),
                    cctvCycle = actualData.optInt("cctvInterval", _roomInfo.value.cctvCycle),
                    prison = Location(
                        lat = actualData.optDouble("prisonLat", currentPrison.lat),
                        lng = actualData.optDouble("prisonLng", currentPrison.lng)
                    ),
                    polygon = updatedPolygon
                )
            }


        } catch (e: Exception) {

        }
    }

    /**
     * 플레이어 Ready 상태 업데이트
     */
    private fun updatePlayerReady(memberId: Long, isReady: Boolean) {
        val currentPlayers = _players.value
        _players.value = currentPlayers.map { player ->
            if (player.id == memberId) {
                if (memberId == _myMemberId.value) {
                    _isMeReady.value = isReady
                }
                player.copy(isReady = isReady)
            } else {
                player
            }
        }
    }

    /**
     * 플레이어 포지션 업데이트
     */
    private fun updatePlayerPosition(memberId: Long, position: String) {
        _players.value = _players.value.map { player ->
            if (player.id == memberId) {
                player.copy(
                    role = when (position) {
                        "POLICE" -> GameRole.POLICE
                        "THIEF" -> GameRole.THIEF
                        else -> GameRole.ANY
                    },
                    isChangingRole = false
                )
            } else {
                player
            }
        }
    }

    /**
     * 게임 시작
     */
    fun startGame() {
        if (!_isHost.value) return

        viewModelScope.launch {
            _uiState.value = UiState.Loading

            when (val result = gameRoomRepository.startGame(roomId)) {
                is BaseResult.Success -> {
                    roomSocketManager.gameStart(roomId)

                    delay(500)

                    _uiState.value = UiState.Success(Unit)
                }

                is BaseResult.Error -> {
                    _uiState.value = UiState.Error(result.error.message)
                }
            }
        }
    }

    /**
     * 플레이어 제거
     */
    private fun removePlayer(memberId: Long) {
        _players.value = _players.value.filter { it.id != memberId }.toList()
    }


    /**
     * 방 설정 변경
     */
    fun updateRoomSettings(
        maxCount: Int,
        timeLimit: Int,
        missionCount: Int,
        cctvCycle: Int,
        policeCount: Int,
        prisonLocation: Location,
        polygonPoints: List<Location>
    ) {
        if (!_isHost.value) return

        viewModelScope.launch {
            val safeMaxCount = maxCount.coerceAtLeast(5)
            val safePoliceCount = policeCount.coerceIn(1, safeMaxCount - 1)
            val thiefCount = safeMaxCount - safePoliceCount

            val result = gameRoomRepository.updateRoomSettings(
                roomId = roomId,
                playerCount = safeMaxCount,
                timeLimit = timeLimit,
                cctvInterval = cctvCycle,
                policeCount = safePoliceCount,
                thiefCount = thiefCount,
                missionCount = missionCount,
                prison = prisonLocation,
                polygon = polygonPoints
            )

            when (result) {
                is BaseResult.Success -> {
                    roomSocketManager.updateRoomInfo(
                        playerCount = safeMaxCount,
                        timeLimit = timeLimit,
                        policeCount = safePoliceCount,
                        thiefCount = thiefCount,
                        cctvInterval = cctvCycle,
                        missionCount = missionCount,
                        prison = prisonLocation,
                        polygon = polygonPoints
                    )
                }

                is BaseResult.Error -> {
                    _uiState.emit(UiState.Error("설정 변경 실패: ${result.error.message}"))
                }
            }
        }
    }

    /**
     * 강퇴
     */
    fun kickPlayer(targetMemberId: Long, reason: String) {
        if (!_isHost.value) return

        roomSocketManager.kickMember(targetMemberId, reason) { kickedId ->
            removePlayer(kickedId)
            roomSocketManager.requestRoomInfo(roomId)
        }
    }


    /**
     * 방 나가기
     */
    fun leaveRoom() {
        viewModelScope.launch {
            roomSocketManager.leaveRoom()

            gameRoomRepository.leaveRoom(roomId)

            _uiEvent.emit(GameRoomUiEvent.NavigateToHome())
        }
    }

    fun setInitialRole(role: GameRole) {
        viewModelScope.launch {
            val position = role.name

            val result = gameRoomRepository.changePosition(roomId, position)

            if (result is BaseResult.Success) {
                while (!roomSocketManager.isConnected()) {
                    delay(200)
                }
                roomSocketManager.updatePosition(position)
            }
        }
    }

    // 방장 위임하기
    fun delegateHost(targetMemberId: Long) {
        if (!_isHost.value) return

        viewModelScope.launch {
            _uiState.value = UiState.Loading
            roomSocketManager.delegateOwner(targetMemberId)
        }
    }

    fun setPolygonPoints(points: List<LatLng>) {
        locationRepository.setPolygonPoints(points)
    }

    fun setPrisonLocation(location: LatLng?) {
        locationRepository.setPrisonLocation(location)
    }

    fun deletePolygonPoint(targetList: MutableList<DraggableLatLng>, index: Int): Boolean {
        return locationRepository.deletePolygonPoint(targetList, index)
    }

    fun addPointToList(targetList: MutableList<DraggableLatLng>, newPoint: LatLng) {
        locationRepository.addPointToList(targetList, newPoint)
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