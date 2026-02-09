package com.d104.pnt.util.socket

import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 채팅 관련 이벤트 처리
 */
@Singleton
class ChatSocketManager @Inject constructor() : BaseSocketManager("chat") {

    private var currentChatRoomId: Long? = null
    fun getCurrentChatRoomId(): Long? = currentChatRoomId

    companion object {
        // Request Events (req)
        private const val EVENT_POST_JOIN_ROOM = "post join room"
        private const val EVENT_POST_MESSAGE = "post message"
        private const val EVENT_POST_PREV_CHAT = "post prev chat"
        private const val EVENT_POST_SYNC_CHAT = "post sync chat"
        private const val EVENT_POST_DISCONNECT = "post disconnect"

        // Response Events (res)
        private const val EVENT_GET_JOIN_ROOM = "get join room"
        private const val EVENT_GET_MESSAGE = "get message"
        private const val EVENT_GET_PREV_CHAT = "get prev chat"
        private const val EVENT_GET_SYNC_CHAT = "get sync chat"
    }

    // Callbacks
    private var onJoinedRoom: ((Long, String) -> Unit)? = null
    private var onNewMessage: ((JSONObject) -> Unit)? = null
    private var onPreviousMessages: ((List<JSONObject>, Int) -> Unit)? = null
    private var onSyncMessages: ((List<JSONObject>, Int) -> Unit)? = null
    private var onReconnected: ((Long) -> Unit)? = null
    private var onOwnerDelegated: ((JSONObject) -> Unit)? = null
    private var onMemberKicked: ((JSONObject) -> Unit)? = null

    override fun setupCustomListeners() {
        // 채팅방 입장 확인
        on(EVENT_GET_JOIN_ROOM) { args ->
            try {
                val response = args[0] as JSONObject
                val message = response.optString("message", "")
                val roomId = response.optLong("chatRoomId", 0)

                if (message == "joined room" && roomId > 0) {
                    onJoinedRoom?.invoke(roomId, "채팅방에 입장하였습니다.")
                } else {
                    onJoinedRoom?.invoke(roomId, message)
                }
            } catch (e: Exception) {

            }
        }

        // 새 메시지 수신
        on(EVENT_GET_MESSAGE) { args ->
            try {
                val data = args[0] as JSONObject
                onNewMessage?.invoke(data)
            } catch (e: Exception) {

            }
        }

        // 이전 메시지 수신
        on(EVENT_GET_PREV_CHAT) { args ->
            try {
                val messageList = mutableListOf<JSONObject>()
                val items = args[0]

                when (items) {
                    is JSONArray -> {
                        for (i in 0 until items.length()) {
                            messageList.add(items.getJSONObject(i))
                        }
                    }

                    is JSONObject -> {
                        if (items.has("items")) {
                            val innerItems = items.opt("items")
                            if (innerItems is JSONArray) {
                                for (i in 0 until innerItems.length()) {
                                    messageList.add(innerItems.getJSONObject(i))
                                }
                            }
                        }
                    }
                }

                val count = messageList.size
                onPreviousMessages?.invoke(messageList, count)

            } catch (e: Exception) {
                onPreviousMessages?.invoke(emptyList(), 0)
            }
        }

        // 동기화 메시지 수신
        on(EVENT_GET_SYNC_CHAT) { args ->
            try {

                val messageList = mutableListOf<JSONObject>()
                val items = args[0]

                when (items) {
                    is JSONArray -> {
                        for (i in 0 until items.length()) {
                            messageList.add(items.getJSONObject(i))
                        }
                    }

                    is JSONObject -> {
                        if (items.has("items")) {
                            val innerItems = items.opt("items")
                            if (innerItems is JSONArray) {
                                for (i in 0 until innerItems.length()) {
                                    messageList.add(innerItems.getJSONObject(i))
                                }
                            }
                        }
                    }
                }

                val count = messageList.size
                onSyncMessages?.invoke(messageList, count)

            } catch (e: Exception) {
                onSyncMessages?.invoke(emptyList(), 0)
            }
        }

        on("get delegate owner") { args ->
            try {
                val data = args[0] as JSONObject
                onOwnerDelegated?.invoke(data)
            } catch (e: Exception) {

            }
        }

        // 강퇴 이벤트
        on("get kick member") { args ->
            try {
                val data = args[0] as JSONObject
                onMemberKicked?.invoke(data)
            } catch (e: Exception) {

            }
        }
    }

    override fun onReconnect(data: JSONObject) {
        super.onReconnect(data)
        val roomId = data.optLong("chatRoomId", currentChatRoomId ?: 0)

        if (roomId > 0) {
            currentChatRoomId = roomId

            joinRoom(roomId) { success, message ->
                if (success) {
                    onReconnected?.invoke(roomId)
                }
            }
        }
    }

    override fun onDisconnectCleanup() {
        currentChatRoomId = null
        clearCallbacks()
    }

    // Request Methods

    /**
     * 채팅방 입장
     */
    fun joinRoom(chatRoomId: Long, onResponse: (Boolean, String) -> Unit) {
        currentChatRoomId = chatRoomId

        val data = JSONObject().apply {
            put("chatRoomId", chatRoomId)
        }

        emit(EVENT_POST_JOIN_ROOM, data)

        // 콜백 등록
        setOnJoinedRoom { roomId, message ->
            if (roomId > 0 && message.contains("입장")) {
                onResponse(true, message)
            } else {
                onResponse(false, message)
            }
        }
    }

    /**
     * 채팅 메시지 전송
     */
    fun sendMessage(content: String, type: String = "TEXT") {
        val roomId = currentChatRoomId ?: run {
            return
        }

        val data = JSONObject().apply {
            put("chatRoomId", roomId)
            put("content", content)
            put("type", type)
        }

        emit(EVENT_POST_MESSAGE, data)
    }

    /**
     * 이전 메시지 조회
     */
    fun loadPreviousMessages(cursor: Int, limit: Int = 50) {
        val roomId = currentChatRoomId ?: run {
            return
        }

        val data = JSONObject().apply {
            put("cursor", if (cursor == -1) JSONObject.NULL else cursor)
            put("limit", limit)
        }

        emit(EVENT_POST_PREV_CHAT, data)
    }

    /**
     * 메시지 동기화
     */
    fun syncMessages(cursor: Int) {
        val roomId = currentChatRoomId ?: run {
            return
        }

        val data = JSONObject().apply {
            put("cursor", cursor)
        }

        emit(EVENT_POST_SYNC_CHAT, data)
    }


    /**
     * 채팅방 나가기
     */
    fun leaveRoom() {
        if (currentChatRoomId == null) {
            return
        }

        val data = JSONObject()
        emit(EVENT_POST_DISCONNECT, data)

        disconnect()
    }

    // Callback Setters
    fun setOnJoinedRoom(callback: (chatRoomId: Long, message: String) -> Unit) {
        onJoinedRoom = callback
    }

    fun setOnNewMessage(callback: (JSONObject) -> Unit) {
        onNewMessage = callback
    }

    fun setOnPreviousMessages(callback: (messages: List<JSONObject>, count: Int) -> Unit) {
        onPreviousMessages = callback
    }

    fun setOnSyncMessages(callback: (messages: List<JSONObject>, count: Int) -> Unit) {
        onSyncMessages = callback
    }

    fun setOnReconnected(callback: (chatRoomId: Long) -> Unit) {
        onReconnected = callback
    }


    private fun clearCallbacks() {
        onJoinedRoom = null
        onNewMessage = null
        onPreviousMessages = null
        onSyncMessages = null
        onReconnected = null
        onOwnerDelegated = null
        onMemberKicked = null
    }

    override fun removeAllListeners() {
        super.removeAllListeners()
        off(EVENT_GET_JOIN_ROOM)
        off(EVENT_GET_MESSAGE)
        off(EVENT_GET_PREV_CHAT)
        off(EVENT_GET_SYNC_CHAT)
        off("get delegate owner")
        off("get kick member")
    }

    // 방장 위임 요청
    fun delegateOwner(targetMemberId: Long) {
        if (!isConnected()) {
            return
        }

        val data = JSONObject().apply {
            put("targetMemberId", targetMemberId)
        }

        socket?.emit("post delegate owner", data)
    }

    // 방장 위임 결과 수신
    fun setOnOwnerDelegated(callback: (JSONObject) -> Unit) {
        onOwnerDelegated = callback
    }

    // 강퇴 이벤트 수신
    fun setOnMemberKicked(callback: (JSONObject) -> Unit) {
        onMemberKicked = callback
    }

    // 강퇴 확인 websocket event
    fun notifyKickMember(kickMemberId: Long) {
        if (!isConnected()) {
            return
        }

        val data = JSONObject().apply {
            put("kickMemberId", kickMemberId)
        }

        socket?.emit("post kick member", data)
    }
}