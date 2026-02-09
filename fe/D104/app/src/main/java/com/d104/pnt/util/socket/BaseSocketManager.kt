package com.d104.pnt.util.socket

import com.d104.pnt.base.Constants
import io.socket.client.IO
import io.socket.client.Socket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

abstract class BaseSocketManager(
    private val namespace: String
) {
    protected var socket: Socket? = null
    private var isManualDisconnect = false


    private val retryScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    private val recentEmits = ConcurrentHashMap<String, EmitRecord>()

    private var lastErrorTime = 0L

    companion object {
        const val EVENT_RECONNECT = "reconnect"
        const val EVENT_ERROR = "error"

        const val MAX_RETRY_ATTEMPTS = 3
        const val RETRY_DELAY_MS = 1500L
        const val EMIT_TRACKING_DURATION_MS = 5000L
    }

    data class EmitRecord(
        val event: String,
        val data: JSONObject,
        val timestamp: Long,
        var retryCount: Int = 0
    )

    /**
     * 소켓 연결
     */
    fun connect(authToken: String) {
        if (socket != null && (socket!!.connected() || !isManualDisconnect)) {
            return
        }

        socket?.let {
            it.off()
            it.disconnect()
            it.close()
        }
        socket = null

        try {
            val socketUrl = "${Constants.BASE_URL}$namespace"

            val options = IO.Options().apply {
                auth = mapOf("token" to authToken)
                reconnection = true
                reconnectionAttempts = 5
                reconnectionDelay = 1000
                forceNew = true
            }

            socket = IO.socket(socketUrl, options)

            setupBaseListeners()
            setupCustomListeners()

            socket?.connect()

        } catch (e: Exception) {

        }
    }

    /**
     * 기본 소켓 이벤트 리스너 설정
     */
    private fun setupBaseListeners() {
        socket?.apply {
            on(Socket.EVENT_CONNECT) {
                isManualDisconnect = false
                onConnect()
            }

            on(Socket.EVENT_DISCONNECT) { args ->
                onDisconnect()
            }

            on(Socket.EVENT_CONNECT_ERROR) { args ->
                onError(args.firstOrNull()?.toString() ?: "Unknown error")
            }

            on(EVENT_RECONNECT) { args ->
                try {
                    val data = args[0] as JSONObject
                    onReconnect(data)
                } catch (e: Exception) {

                }
            }

            on(EVENT_ERROR) { args ->
                try {
                    val error = args[0] as JSONObject
                    val message = error.getString("message")
                    val code = error.getInt("code")

                    if (code == 500) {
                        lastErrorTime = System.currentTimeMillis()
                        retryRecentEmits()
                    } else {
                        onError("$message (code: $code)")
                    }
                } catch (e: Exception) {

                }
            }
        }
    }

    /**
     * 최근 전송한 이벤트 재시도
     */
    private fun retryRecentEmits() {
        val now = System.currentTimeMillis()

        val recentEvents = recentEmits.values.filter {
            now - it.timestamp < EMIT_TRACKING_DURATION_MS
        }

        if (recentEvents.isEmpty()) {
            return
        }

        recentEvents.forEach { record ->
            if (record.retryCount < MAX_RETRY_ATTEMPTS) {
                retryScope.launch {
                    delay(RETRY_DELAY_MS * (record.retryCount + 1)) // 점진적 지연

                    if (isConnected()) {
                        record.retryCount++
                        socket?.emit(record.event, record.data)
                    } else {

                    }
                }
            } else {

            }
        }
    }

    /**
     * 커스텀 이벤트 리스너 설정
     */
    protected abstract fun setupCustomListeners()

    /**
     * 연결 성공 시 콜백
     */
    protected open fun onConnect() {
    }

    /**
     * 연결 해제 시 콜백
     */
    protected open fun onDisconnect() {
    }

    /**
     * 재연결 시 콜백
     */
    protected open fun onReconnect(data: JSONObject) {
    }

    /**
     * 에러 발생 시 콜백
     */
    protected open fun onError(message: String) {
    }

    /**
     * 정상 연결 해제
     */
    fun disconnect() {
        isManualDisconnect = true

        socket?.let {
            if (it.connected()) {
                removeAllListeners()
                it.disconnect()
            }
        }

        socket = null
        onDisconnectCleanup()
    }

    /**
     * 연결 해제 시 정리 작업
     */
    protected open fun onDisconnectCleanup() {

    }

    /**
     * 모든 리스너 제거
     */
    protected open fun removeAllListeners() {
        socket?.off()
    }

    /**
     * 연결 상태 확인
     */
    fun isConnected(): Boolean = socket?.connected() ?: false

    /**
     * 이벤트 전송
     */
    protected fun emit(event: String, data: JSONObject) {
        if (!isConnected()) {
            return
        }
        socket?.emit(event, data)
        if (isRetryableEvent(event)) {
            val record = EmitRecord(
                event = event,
                data = data,
                timestamp = System.currentTimeMillis()
            )
            recentEmits[event] = record

            cleanupOldEmits()
        }
    }


    /**
     * 재시도 가능한 이벤트인지 확인
     */
    private fun isRetryableEvent(event: String): Boolean {
        return when {
            event.contains("post now room info") -> true
            event.contains("post now ready info") -> true
            event.contains("post join room") -> true
            event.contains("post game info sync") -> true
            else -> false
        }
    }

    /**
     * 오래된 emit 기록 정리
     */
    private fun cleanupOldEmits() {
        val now = System.currentTimeMillis()
        recentEmits.entries.removeIf {
            now - it.value.timestamp > EMIT_TRACKING_DURATION_MS
        }
    }


    /**
     * 이벤트 리스너 등록
     */
    protected fun on(event: String, handler: (Array<Any>) -> Unit) {
        socket?.on(event) { args ->
            handler(args)
        }
    }

    /**
     * 이벤트 리스너 제거
     */
    protected fun off(event: String) {
        socket?.off(event)
    }
}