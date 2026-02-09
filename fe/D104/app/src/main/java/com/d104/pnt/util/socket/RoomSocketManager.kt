package com.d104.pnt.util.socket

import com.d104.pnt.data.remote.model.request.Location
import com.d104.pnt.domain.model.RoomInfoResponse
import com.google.gson.Gson
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 게임 로비(대기방) 관련 이벤트 처리
 */
@Singleton
class RoomSocketManager @Inject constructor(private val gson: Gson) : BaseSocketManager("room") {

    var currentRoomId: Long? = null

    companion object {
        // Request Events (req)
        private const val EVENT_POST_JOIN_ROOM = "post join room"
        private const val EVENT_POST_UPDATE_ROOM_INFO = "post update room info"
        private const val EVENT_POST_UPDATE_READY = "post update ready"
        private const val EVENT_POST_NOW_READY_INFO = "post now ready info"
        private const val EVENT_POST_UPDATE_POSITION = "post update position"
        private const val EVENT_POST_NOW_ROOM_INFO = "post now room info"
        private const val EVENT_POST_MEMBER_KICK = "post member kick"
        private const val EVENT_POST_DELEGATE_OWNER = "post delegate owner"
        private const val EVENT_POST_UPDATE_ACCESS_TOKEN = "post update access token"
        private const val EVENT_POST_GAME_START = "post game start"
        private const val EVENT_POST_DISCONNECT = "post disconnect"

        // Response Events (res)
        private const val EVENT_GET_UPDATE_ROOM_INFO = "get update room info"
        private const val EVENT_GET_UPDATE_READY = "get update ready"
        private const val EVENT_GET_UPDATE_PREFER_POSITION = "get update prefer position"
        private const val EVENT_GET_NOW_READY_INFO = "get now ready info"
        private const val EVENT_GET_NOW_ROOM_INFO = "get now room info"
        private const val EVENT_GET_MEMBER_KICK = "get member kick"
        private const val EVENT_GET_DELEGATE_OWNER = "get delegate owner"
        private const val EVENT_GET_UPDATE_ACCESS_TOKEN = "get update access token"
        private const val EVENT_GET_GAME_START = "get game start"
        private const val EVENT_GET_USER_LEFT = "get user left"

        // Reconnect
        private const val EVENT_ROOM_RECONNECT = "reconnect"
    }

    // Callbacks
    private var onRoomInfoUpdated: ((JSONObject) -> Unit)? = null
    private var onReadyUpdated: ((Long, Long, Boolean) -> Unit)? = null
    private var onPositionUpdated: ((Long, Long, String) -> Unit)? = null
    private var onReadyInfoReceived: ((JSONArray) -> Unit)? = null
    private var onFullRoomInfoReceived: ((RoomInfoResponse) -> Unit)? = null
    private var onMemberKicked: ((Long) -> Unit)? = null
    private var onMemberLeft: ((Long) -> Unit)? = null
    private var onGameStarted: ((JSONObject) -> Unit)? = null
    private var onOwnerDelegated: ((JSONObject) -> Unit)? = null
    private var onTokenUpdated: ((String) -> Unit)? = null
    private var onReconnected: ((Long) -> Unit)? = null

    override fun setupCustomListeners() {
        on(EVENT_ROOM_RECONNECT) { args ->
            try {
                val root = args[0] as JSONObject
                val roomId = root.optLong("roomId", 0L)

                if (roomId != 0L) {
                    currentRoomId = roomId
                    onReconnected?.invoke(roomId)
                }
            } catch (e: Exception) {

            }
        }

        // Room info 업데이트 수신
        on(EVENT_GET_UPDATE_ROOM_INFO) { args ->
            try {
                val data = args[0] as JSONObject
                onRoomInfoUpdated?.invoke(data)
            } catch (e: Exception) {

            }
        }

        // Ready 상태 업데이트 수신
        on(EVENT_GET_UPDATE_READY) { args ->
            try {
                val root = args[0] as JSONObject
                val data = root.optJSONObject("data") ?: root

                val memberId = data.optLong("memberId", 0L)
                val isReady = when {
                    data.has("ready") -> data.getBoolean("ready")
                    data.has("isReady") -> data.getBoolean("isReady")
                    else -> false
                }

                if (memberId != 0L) {
                    onReadyUpdated?.invoke(currentRoomId ?: 0L, memberId, isReady)
                }
            } catch (e: Exception) {

            }
        }

        // 선호 포지션 업데이트 수신
        on(EVENT_GET_UPDATE_PREFER_POSITION) { args ->
            try {
                val root = args[0] as JSONObject
                val data = root.optJSONObject("data") ?: root

                val roomId = data.optLong("roomId", currentRoomId ?: 0L)
                val memberId = data.optLong("memberId", 0L)

                val preferPosition = data.optString("preferPosition", "THIEF")

                if (memberId != 0L) {
                    onPositionUpdated?.invoke(roomId, memberId, preferPosition)
                }
            } catch (e: Exception) {

            }
        }

        // Ready 정보 리스트 수신
        on(EVENT_GET_NOW_READY_INFO) { args ->
            try {
                val data = args[0] as JSONArray
                onReadyInfoReceived?.invoke(data)
            } catch (e: Exception) {

            }
        }


        // 전체 방 정보 수신
        on(EVENT_GET_NOW_ROOM_INFO) { args ->
            try {
                val jsonString = args[0].toString()
                val roomInfo = gson.fromJson(jsonString, RoomInfoResponse::class.java)
                onFullRoomInfoReceived?.invoke(roomInfo)
            } catch (e: Exception) {

            }
        }

        // 멤버 강퇴 알림
        on(EVENT_GET_MEMBER_KICK) { args ->
            try {
                val root = args[0] as JSONObject
                val data = root.optJSONObject("data") ?: root

                val kickedId = when {
                    data.has("memberId") -> data.getLong("memberId")
                    data.has("targetMemberId") -> data.getLong("targetMemberId")
                    root.has("memberId") -> root.getLong("memberId")
                    else -> 0L
                }

                if (kickedId != 0L) {
                    onMemberKicked?.invoke(kickedId)
                }
            } catch (e: Exception) {

            }
        }

        // 게임 시작 수신
        on(EVENT_GET_GAME_START) { args ->
            try {
                val data = args[0] as JSONObject
                onGameStarted?.invoke(data)
            } catch (e: Exception) {

            }
        }

        // 방장 위임 수신
        on(EVENT_GET_DELEGATE_OWNER) { args ->
            try {
                val data = args[0] as JSONObject
                onOwnerDelegated?.invoke(data)
            } catch (e: Exception) {

            }
        }

        // 토큰 갱신 수신
        on(EVENT_GET_UPDATE_ACCESS_TOKEN) { args ->
            val data = args[0] as JSONObject
            val newToken = data.optString("accessToken")
            if (newToken.isNotEmpty()) {
                onTokenUpdated?.invoke(newToken)
            }
        }

        // 멤버 퇴장 알림
        on(EVENT_GET_USER_LEFT) { args ->
            try {
                val root = args[0] as JSONObject
                val data = root.optJSONObject("data") ?: root
                val leftMemberId = data.optLong("memberId", 0L)

                if (leftMemberId != 0L) {
                    onMemberLeft?.invoke(leftMemberId)
                }
            } catch (e: Exception) {

            }
        }
    }

    override fun onReconnect(data: JSONObject) {
        super.onReconnect(data)
        val roomId = data.optLong("roomId")
        if (roomId > 0) {
            currentRoomId = roomId
            requestRoomInfo(roomId)
        }
    }

    // Request Methods

    fun cleanup() {
        currentRoomId = null
        clearCallbacks()
        if (isConnected()) {
            disconnect()
        }
    }

    /**
     * 방 입장
     */
    fun joinRoom(roomId: Long, onResponse: (Boolean, String) -> Unit) {
        if (!isConnected()) {
            onResponse(false, "소켓 연결 안됨")
            return
        }

        currentRoomId = roomId

        val joinData = JSONObject().apply {
            put("roomId", roomId)
        }

        emit(EVENT_POST_JOIN_ROOM, joinData)

        requestRoomInfo(roomId)
        requestReadyInfo(roomId)
        onResponse(true, "입장 요청 완료")
    }

    /**
     * 현재 방 정보 요청
     */
    fun requestRoomInfo(roomId: Long = currentRoomId ?: 0) {
        if (roomId == 0L) {
            return
        }

        val data = JSONObject().apply {
            put("roomId", roomId)
        }

        emit(EVENT_POST_NOW_ROOM_INFO, data)
    }

    /**
     * 현재 Ready 정보 요청
     */
    fun requestReadyInfo(roomId: Long = currentRoomId ?: 0) {
        if (roomId == 0L) {
            return
        }

        val data = JSONObject().apply {
            put("roomId", roomId)
        }

        emit(EVENT_POST_NOW_READY_INFO, data)
    }

    /**
     * 방 설정 업데이트
     */
    fun updateRoomInfo(
        playerCount: Int,
        timeLimit: Int,
        policeCount: Int,
        thiefCount: Int,
        cctvInterval: Int,
        missionCount: Int,
        prison: Location?,
        polygon: List<Location>?
    ) {
        val roomId = currentRoomId ?: return
        val data = JSONObject().apply {
            put("roomId", roomId)
            put("playerCount", playerCount)
            put("timeLimit", timeLimit)
            put("policeCount", policeCount)
            put("thiefCount", thiefCount)
            put("cctvInterval", cctvInterval)
            put("missionCount", missionCount)

            // 감옥 좌표
            put("prison", JSONObject().apply {
                put("lat", prison?.lat)
                put("lng", prison?.lng)
            })

            // 폴리곤 좌표 배열
            val polyArray = JSONArray()
            polygon?.forEach { loc ->
                polyArray.put(JSONObject().apply {
                    put("lat", loc.lat)
                    put("lng", loc.lng)
                })
            }
            put("polygon", polyArray)
        }

        emit(EVENT_POST_UPDATE_ROOM_INFO, data)
    }

    /**
     * Ready 상태 업데이트
     */
    fun updateReady(roomId: Long, ready: Boolean) {
        val currentRoomId = roomId

        val data = JSONObject().apply {
            put("roomId", roomId)
            put("ready", ready)
        }

        emit(EVENT_POST_UPDATE_READY, data)
    }

    /**
     * 선호 포지션 업데이트
     */
    fun updatePosition(preferPosition: String) {
        val roomId = currentRoomId ?: run {
            return
        }


        val data = JSONObject().apply {
            put("roomId", roomId)
            put("preferPosition", preferPosition)
        }

        emit(EVENT_POST_UPDATE_POSITION, data)
    }


    /**
     * 멤버 강퇴
     */
    fun kickMember(targetMemberId: Long, reason: String, onSuccess: (Long) -> Unit) {
        val roomId = currentRoomId ?: return
        val data = JSONObject().apply {
            put("roomId", roomId)
            put("targetMemberId", targetMemberId)
            put("reason", reason)
        }

        emit(EVENT_POST_MEMBER_KICK, data)

        onSuccess(targetMemberId)
    }

    /**
     * 게임 시작 소켓 신호 발행
     */
    fun gameStart(roomId: Long) {
        val data = JSONObject().apply {
            put("roomId", roomId)
        }
        emit(EVENT_POST_GAME_START, data)
    }

    /**
     * 방장 위임
     */
    fun delegateOwner(targetMemberId: Long) {
        val roomId = currentRoomId ?: return
        val data = JSONObject().apply {
            put("roomId", roomId)
            put("targetMemberId", targetMemberId)
        }
        emit(EVENT_POST_DELEGATE_OWNER, data)
    }

    /**
     * 서버에 갱신된 액세스 토큰 전송
     */
    fun updateAccessToken(newToken: String) {
        val data = JSONObject().apply {
            put("accessToken", newToken)
        }
        emit(EVENT_POST_UPDATE_ACCESS_TOKEN, data)
    }

    /**
     * 방 나가기
     */
    fun leaveRoom() {
        val roomId = currentRoomId ?: return

        val data = JSONObject().apply { put("roomId", roomId) }
        emit(EVENT_POST_DISCONNECT, data)

        disconnect()
    }

    // Callback Setters

    fun setOnRoomInfoUpdated(callback: (JSONObject) -> Unit) {
        onRoomInfoUpdated = callback
    }

    fun setOnReadyUpdated(callback: (roomId: Long, memberId: Long, isReady: Boolean) -> Unit) {
        onReadyUpdated = callback
    }

    fun setOnPositionUpdated(callback: (roomId: Long, memberId: Long, position: String) -> Unit) {
        onPositionUpdated = callback
    }

    fun setOnReadyInfoReceived(callback: (JSONArray) -> Unit) {
        onReadyInfoReceived = callback
    }

    fun setOnFullRoomInfoReceived(callback: (RoomInfoResponse) -> Unit) {
        onFullRoomInfoReceived = callback
    }

    fun setOnMemberKicked(callback: (memberId: Long) -> Unit) {
        onMemberKicked = callback
    }

    fun setOnMemberLeft(callback: (memberId: Long) -> Unit) {
        onMemberLeft = callback
    }

    fun setOnGameStarted(callback: (JSONObject) -> Unit) {
        onGameStarted = callback
    }

    fun setOnOwnerDelegated(callback: (JSONObject) -> Unit) {
        onOwnerDelegated = callback
    }

    fun setOnTokenUpdated(callback: (String) -> Unit) {
        onTokenUpdated = callback
    }

    private fun clearCallbacks() {
        onRoomInfoUpdated = null
        onReadyUpdated = null
        onPositionUpdated = null
        onReadyInfoReceived = null
        onFullRoomInfoReceived = null
        onMemberKicked = null
        onMemberLeft = null
        onTokenUpdated = null
        onReconnected = null
    }

    override fun removeAllListeners() {
        super.removeAllListeners()
        off(EVENT_GET_UPDATE_ROOM_INFO)
        off(EVENT_GET_UPDATE_READY)
        off(EVENT_GET_UPDATE_PREFER_POSITION)
        off(EVENT_GET_NOW_READY_INFO)
        off(EVENT_GET_NOW_ROOM_INFO)
        off(EVENT_GET_MEMBER_KICK)
        off(EVENT_GET_GAME_START)
        off(EVENT_GET_DELEGATE_OWNER)
        off(EVENT_GET_UPDATE_ACCESS_TOKEN)
        off(EVENT_GET_USER_LEFT)
    }
}
