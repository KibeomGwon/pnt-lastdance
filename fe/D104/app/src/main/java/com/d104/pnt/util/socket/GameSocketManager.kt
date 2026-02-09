package com.d104.pnt.util.socket

import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 실제 게임 플레이 중 이벤트 처리
 */
@Singleton
class GameSocketManager @Inject constructor() : BaseSocketManager("game") {

    var currentGameId: Long? = null

    companion object {
        // Request Events (req)
        private const val EVENT_POST_JOIN_ROOM = "post join room"
        private const val EVENT_POST_GPS = "post gps"
        private const val EVENT_POST_ARREST = "post arrest"
        private const val EVENT_POST_SKILL_USE = "post skill use"
        private const val EVENT_POST_MISSION_IMAGE = "post mission image"
        private const val EVENT_POST_SYNC_GAME_INFO = "post sync game info"
        private const val EVENT_POST_RADIO = "post radio"
        private const val EVENT_POST_RESET_GAME = "post reset game"
        private const val EVENT_POST_AFTER_GAME_END = "post after game end"
        private const val EVENT_POST_DISCONNECT = "post disconnect"

        // Response Events (res)
        private const val EVENT_GET_JOIN_ROOM = "get join room"
        private const val EVENT_GET_GPS = "get gps"
        private const val EVENT_GET_WILL_START_GAME = "get will start game"
        private const val EVENT_GET_START_GAME = "get start game"
        private const val EVENT_GET_ESCAPE = "get escape"
        private const val EVENT_OUT_OF_BOUNDARY = "out of boundary"
        private const val EVENT_GET_ARREST = "get arrest"
        private const val EVENT_MODIFY_MEMBER_STATUS = "modify member status"
        private const val EVENT_GET_BEEP_USE = "get beep use"
        private const val EVENT_GET_SYNC_GAME_INFO = "get sync game info"
        private const val EVENT_GET_SKILL_USE = "get skill use"
        private const val EVENT_GET_RADIO = "get radio"
        private const val EVENT_GET_END_GAME = "get end game"
        private const val EVENT_GET_END_GAME_AFTER = "get end game after"
        private const val EVENT_GET_NEWS = "get news"
        private const val EVENT_GET_RECONNECT = "reconnect"
        private const val EVENT_GET_MISSION_RESULT = "get mission result"
    }

    // Callbacks
    private var onJoinedRoom: ((Long, Long, String, Int) -> Unit)? = null
    private var onGpsReceived: ((Long?, String?, Int, JSONArray) -> Unit)? = null
    private var onWillStartGame: ((Long, String) -> Unit)? = null
    private var onGameStarted: ((Long, String) -> Unit)? = null
    private var onThiefEscaped: ((gameId: Long, thiefId: Long, escapedAt: String) -> Unit)? = null
    private var onOutOfBoundary: ((Long, Long) -> Unit)? = null
    private var onArrestResult: ((String, String?, Long, Long, String?) -> Unit)? = null
    private var onMemberStatusChanged: ((Long, Long, String, String) -> Unit)? = null
    private var onBeepReceived: ((Long, Long, Double) -> Unit)? = null
    private var onGameInfoSynced: ((JSONObject) -> Unit)? = null
    private var onSkillResult: ((String, String?, Long, String?) -> Unit)? = null
    private var onRadioReceived: ((Long, Long) -> Unit)? = null
    private var onEndGameAfter: ((JSONObject) -> Unit)? = null
    private var onGameEnded: ((String, JSONObject) -> Unit)? = null
    private var onNewsReceived: ((Long, Long) -> Unit)? = null
    private var onReconnected: ((Long) -> Unit)? = null

    private var onBeepUse: ((JSONObject) -> Unit)? = null
    private var onMissionResult: ((Long, Long, Long, Boolean, String, String) -> Unit)? = null
    private var onHelicopterSkillReceived:
            ((gameId: Long, policeId: Long, result: String, reason: String?, startedAt: String?, usedAt: String?) -> Unit)? =
        null

    override fun setupCustomListeners() {
        // 게임 입장 확인
        on(EVENT_GET_JOIN_ROOM) { args ->
            try {
                val data = args[0] as JSONObject
                val gameId = data.getLong("gameId")
                val memberId = data.getLong("memberId")
                val message = data.getString("message")
                val connectedMembers = data.getInt("connectedMembers")

                onJoinedRoom?.invoke(gameId, memberId, message, connectedMembers)
            } catch (e: Exception) {

            }
        }

        // GPS 위치 정보 수신
        on(EVENT_GET_GPS) { args ->
            try {
                val data = args[0] as JSONObject
                val gameId = data.getInt("gameId")
                val sec = data.getInt("sec")
                val locations = data.getJSONArray("locations")

                val cctvThiefIdRaw = data.optLong("cctvThiefId", -1L)
                val cctvThiefId = cctvThiefIdRaw.takeIf { it > 0L }

                val skillUsedAtRaw = data.optString("skillUsedAt", null)
                val skillUsedAt = skillUsedAtRaw
                    ?.takeIf { it.isNotBlank() && it.lowercase() != "null" }

                onGpsReceived?.invoke(cctvThiefId, skillUsedAt, sec, locations)
            } catch (e: Exception) {

            }
        }

        // 게임 시작 예정 알림
        on(EVENT_GET_WILL_START_GAME) { args ->
            try {
                val data = args[0] as JSONObject
                val gameId = data.getLong("gameId")
                val willStartAt = data.getString("willStartAt")
                onWillStartGame?.invoke(gameId, willStartAt)
            } catch (e: Exception) {

            }
        }

        // 게임 시작
        on(EVENT_GET_START_GAME) { args ->
            try {
                val data = args[0] as JSONObject
                val gameId = data.getLong("gameId")
                val startTime = data.getString("startTime")
                onGameStarted?.invoke(gameId, startTime)
            } catch (e: Exception) {

            }
        }

        // 도둑 탈출
        on(EVENT_GET_ESCAPE) { args ->
            try {
                val data = args[0] as JSONObject
                val gameId = data.getLong("gameId")
                val thiefId = data.getLong("thiefId")
                val escapedAt = data.getString("escapedAt")
                onThiefEscaped?.invoke(gameId, thiefId, escapedAt)
            } catch (e: Exception) {

            }
        }

        // 경계 이탈
        on(EVENT_OUT_OF_BOUNDARY) { args ->
            try {
                val data = args[0] as JSONObject
                val gameId = data.getLong("gameId")
                val memberId = data.getLong("memberId")
                onOutOfBoundary?.invoke(gameId, memberId)
            } catch (e: Exception) {

            }
        }

        // 체포 결과
        on(EVENT_GET_ARREST) { args ->
            try {
                val data = args[0] as JSONObject
                val result = data.getString("result")
                val reason = data.optString("reason", null)
                val policeId = data.optLong("policeId", -1L)
                val thiefId = data.getLong("thiefId")
                val arrestedAt = data.optString("arrestedAt", null)
                onArrestResult?.invoke(result, reason, policeId, thiefId, arrestedAt)
            } catch (e: Exception) {

            }
        }

        // 멤버 상태 변경
        on(EVENT_MODIFY_MEMBER_STATUS) { args ->
            try {
                val data = args[0] as JSONObject
                val gameId = data.getLong("gameId")
                val thiefId = data.getLong("thiefId")
                val status = data.getString("status")
                val arrestedAt = data.getString("arrestedAt")
                onMemberStatusChanged?.invoke(gameId, thiefId, status, arrestedAt)
            } catch (e: Exception) {

            }
        }

        // Beep 알림
        on(EVENT_GET_BEEP_USE) { args ->
            try {
                val data = args[0] as JSONObject
                val policeId = data.getLong("policeId")
                val thiefId = data.getLong("thiefId")
                val distance = data.getDouble("distance")
                onBeepReceived?.invoke(policeId, thiefId, distance)
            } catch (e: Exception) {

            }
        }

        // 게임 정보 동기화
        on(EVENT_GET_SYNC_GAME_INFO) { args ->
            try {
                val data = args[0] as JSONObject
                onGameInfoSynced?.invoke(data)
            } catch (e: Exception) {

            }
        }

        // 스킬 사용 결과
        on(EVENT_GET_SKILL_USE) { args ->
            try {
                val data = args[0] as JSONObject
                val result = data.getString("result")
                val reason = data.optString("reason", null)
                val policeId = data.getLong("policeId")
                val startedAt = data.optString("startedAt", null)
                onSkillResult?.invoke(result, reason, policeId, startedAt)
            } catch (e: Exception) {

            }
        }

        on(EVENT_GET_RADIO) { args ->
            try {
                val data = args[0] as JSONObject
                val gameId = data.getLong("gameId")
                val memberId = data.getLong("memberId")
                onRadioReceived?.invoke(gameId, memberId)
            } catch (e: Exception) {

            }
        }

        // 게임 종료
        on(EVENT_GET_END_GAME) { args ->
            try {
                val data = args[0] as JSONObject
                val winTeam = data.getString("winTeam")
                val message = data.optString("message", "게임이 종료되었습니다.")

                onGameEnded?.invoke(winTeam, data)

            } catch (e: Exception) {

            }
        }

        // 게임 종료 후 상세 결과 수신
        on(EVENT_GET_END_GAME_AFTER) { args ->
            try {
                val data = args[0] as JSONObject
                onEndGameAfter?.invoke(data)
            } catch (e: Exception) {

            }
        }

        // 뉴스
        on(EVENT_GET_NEWS) { args ->
            try {
                val data = args[0] as JSONObject
                val gameId = data.getLong("gameId")
                val newsId = data.getLong("newsId")

                onNewsReceived?.invoke(gameId, newsId)
            } catch (e: Exception) {

            }
        }

        // 재연결
        on(EVENT_GET_RECONNECT) { args ->
            try {
                val data = args[0] as JSONObject
                val gameId = data.optLong("gameId")

                onReconnect(data)
                onReconnected?.invoke(gameId)
            } catch (e: Exception) {

            }
        }

        on(EVENT_GET_MISSION_RESULT) { args ->
            try {
                val data = args[0] as JSONObject
                val gameId = data.optLong("gameId")
                val missionId = data.optLong("missionId")
                val thiefId = data.optLong("thiefId")
                val success = data.optBoolean("success")
                val reason = data.optString("reason", null)
                val completedAt = data.optString("completedAt", null)

                onMissionResult?.invoke(gameId, missionId, thiefId, success, reason, completedAt)
            } catch (e: Exception) {

            }
        }
    }

    override fun onReconnect(data: JSONObject) {
        super.onReconnect(data)
        val gameId = data.optLong("gameId")
        if (gameId > 0) {
            currentGameId = gameId
            syncGameInfo()
        }
    }

    override fun onDisconnectCleanup() {
        currentGameId = null
        clearCallbacks()
    }

    // Request Methods

    /**
     * 게임 입장
     */
    fun joinGame(gameId: Long) {
        currentGameId = gameId

        val data = JSONObject().apply {
            put("gameId", gameId)
        }

        emit(EVENT_POST_JOIN_ROOM, data)
    }

    /**
     * GPS 위치 전송
     */
    fun sendGPS(lat: Double, lng: Double, walk: Int, longestSurvived: Int) {
        val gameId = currentGameId ?: run {
            return
        }

        val data = JSONObject().apply {
            put("gameId", gameId)
            put("lat", lat)
            put("lng", lng)
            put("walk", walk)
            put("longestSurvived", longestSurvived)
        }

        emit(EVENT_POST_GPS, data)
    }

    /**
     * 체포 시도
     */
    fun arrestThief(policeId: Long, thiefId: Long) {
        val gameId = currentGameId ?: run {
            return
        }

        val data = JSONObject().apply {
            put("gameId", gameId)
            put("policeId", policeId)
            put("thiefId", thiefId)
        }

        emit(EVENT_POST_ARREST, data)
    }

    /**
     * 스킬 사용
     */
    fun useSkill(policeId: Long) {
        val gameId = currentGameId ?: run {
            return
        }

        val data = JSONObject().apply {
            put("gameId", gameId)
            put("policeId", policeId)
        }

        emit(EVENT_POST_SKILL_USE, data)
    }

    /**
     * 미션 이미지 제출
     */
    fun submitMissionImage(memberId: Long, gameMissionId: Long, image: String) {
        val gameId = currentGameId ?: run {
            return
        }

        val data = JSONObject().apply {
            put("gameId", gameId)
            put("memberId", memberId)
            put("gameMissionId", gameMissionId)
            put("image", image)
        }

        emit(EVENT_POST_MISSION_IMAGE, data)
    }

    /**
     * 게임 정보 동기화 요청
     */
    fun syncGameInfo() {
        val gameId = currentGameId ?: run {
            return
        }

        val data = JSONObject().apply {
            put("gameId", gameId)
        }

        emit(EVENT_POST_SYNC_GAME_INFO, data)
    }

    /**
     * 게임 리셋
     */
    @Deprecated("개발용이므로 프로덕션에서 사용 금지")
    fun resetGame() {
        val gameId = currentGameId ?: run {
            return
        }

        val data = JSONObject().apply {
            put("gameId", gameId)
        }

        emit(EVENT_POST_RESET_GAME, data)
    }

    /**
     * 게임 나가기
     */
    fun leaveGame() {
        val gameId = currentGameId ?: run {
            return
        }

        val data = JSONObject().apply {
            put("gameId", gameId)
        }

        emit(EVENT_POST_DISCONNECT, data)

        // 연결 해제
        disconnect()
    }

    /**
     * Radio 송신
     */
    fun sendRadio() {
        val gameId = currentGameId ?: run {
            return
        }

        val data = JSONObject().apply {
            put("gameId", gameId)
        }

        emit(EVENT_POST_RADIO, data)
    }

    /**
     * 게임 종료 후 상세 결과 요청
     */
    fun postAfterGameEnd(gameId: Long) {
        val data = JSONObject().apply {
            put("gameId", gameId)
        }
        emit(EVENT_POST_AFTER_GAME_END, data)
    }

    fun setOnReconnected(callback: (gameId: Long) -> Unit) {
        onReconnected = callback
    }

    // Callback Setters

    fun setOnJoinedRoom(callback: (gameId: Long, memberId: Long, message: String, connectedMembers: Int) -> Unit) {
        onJoinedRoom = callback
    }

    fun setOnGpsReceived(callback: (cctvThiefId: Long?, skillUsedAt: String?, sec: Int, locations: JSONArray) -> Unit) {
        onGpsReceived = callback
    }

    fun setOnWillStartGame(callback: (gameId: Long, willStartAt: String) -> Unit) {
        onWillStartGame = callback
    }

    fun setOnGameStarted(callback: (gameId: Long, startTime: String) -> Unit) {
        onGameStarted = callback
    }

    fun setOnThiefEscaped(callback: (gameId: Long, thiefId: Long, escapedAt: String) -> Unit) {
        onThiefEscaped = callback
    }

    fun setOnOutOfBoundary(callback: (gameId: Long, memberId: Long) -> Unit) {
        onOutOfBoundary = callback
    }

    fun setOnArrestResult(callback: (result: String, reason: String?, policeId: Long?, thiefId: Long, arrestedAt: String?) -> Unit) {
        onArrestResult = callback
    }

    fun setOnMemberStatusChanged(callback: (gameId: Long, thiefId: Long, status: String, arrestedAt: String) -> Unit) {
        onMemberStatusChanged = callback
    }

    fun setOnBeepReceived(callback: (policeId: Long, thiefId: Long, distance: Double) -> Unit) {
        onBeepReceived = callback
    }

    fun setOnGameInfoSynced(callback: (JSONObject) -> Unit) {
        onGameInfoSynced = callback
    }

    fun setOnSkillResult(callback: (result: String, reason: String?, policeId: Long, startedAt: String?) -> Unit) {
        onSkillResult = callback
    }

    fun setOnRadioReceived(callback: (gameId: Long, memberId: Long) -> Unit) {
        onRadioReceived = callback
    }

    fun setOnEndGameAfter(callback: (JSONObject) -> Unit) {
        onEndGameAfter = callback
    }

    fun setOnGameEnded(callback: (winTeam: String, data: JSONObject) -> Unit) {
        onGameEnded = callback
    }

    fun setOnNewsReceived(callback: (gameId: Long, newsId: Long) -> Unit) {
        onNewsReceived = callback
    }

    fun setOnMissionResult(callback: (gameId: Long, missionId: Long, thiefId: Long, success: Boolean, reason: String?, completedAt: String?) -> Unit) {
        onMissionResult = callback
    }

    fun cleanup() {
        clearCallbacks()
        if (isConnected()) {
            disconnect()
        }
    }

    private fun clearCallbacks() {
        onJoinedRoom = null
        onGpsReceived = null
        onWillStartGame = null
        onGameStarted = null
        onThiefEscaped = null
        onOutOfBoundary = null
        onArrestResult = null
        onMemberStatusChanged = null
        onBeepReceived = null
        onGameInfoSynced = null
        onSkillResult = null
        onRadioReceived = null
        onGameEnded = null
        onEndGameAfter = null
        onNewsReceived = null
        onReconnected = null
        onBeepUse = null
        onHelicopterSkillReceived = null
        onMissionResult = null
    }

    public override fun removeAllListeners() {
        super.removeAllListeners()
        off(EVENT_GET_JOIN_ROOM)
        off(EVENT_GET_GPS)
        off(EVENT_GET_WILL_START_GAME)
        off(EVENT_GET_START_GAME)
        off(EVENT_GET_ESCAPE)
        off(EVENT_OUT_OF_BOUNDARY)
        off(EVENT_GET_ARREST)
        off(EVENT_MODIFY_MEMBER_STATUS)
        off(EVENT_GET_BEEP_USE)
        off(EVENT_GET_SYNC_GAME_INFO)
        off(EVENT_GET_SKILL_USE)
        off(EVENT_GET_RADIO)
        off(EVENT_GET_END_GAME)
        off(EVENT_GET_END_GAME_AFTER)
        off(EVENT_GET_NEWS)
        off(EVENT_GET_RECONNECT)
        off(EVENT_GET_MISSION_RESULT)
    }
}