package com.d104.pnt.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import io.livekit.android.LiveKit
import io.livekit.android.events.RoomEvent
import io.livekit.android.events.collect
import io.livekit.android.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WalkieRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : WalkieRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private var room: Room? = null

    // 연결 상태
    private val _isConnected = MutableStateFlow(false)
    override val isConnected: StateFlow<Boolean> = _isConnected.asStateFlow()

    // 마이크 상태
    private val _isMicEnabled = MutableStateFlow(false)
    override val isMicEnabled: StateFlow<Boolean> = _isMicEnabled.asStateFlow()

    // 참여자 수
    private val _participantCount = MutableStateFlow(0)
    override val participantCount: StateFlow<Int> = _participantCount.asStateFlow()

    override suspend fun connect(serverUrl: String, token: String, roomName: String) {
        try {
            room = LiveKit.create(appContext = context)
            observeRoomEvents()
            room?.connect(url = serverUrl, token = token)
        } catch (e: Exception) {
            _isConnected.value = false
            throw e
        }
    }

    private fun observeRoomEvents() {
        scope.launch {
            room?.events?.collect { event ->
                when (event) {
                    is RoomEvent.Connected -> {
                        _isConnected.value = true
                        updateParticipantCount()
                    }

                    is RoomEvent.Disconnected -> {
                        _isConnected.value = false
                        _participantCount.value = 0
                    }

                    is RoomEvent.ParticipantConnected -> {
                        updateParticipantCount()
                    }

                    is RoomEvent.ParticipantDisconnected -> {
                        updateParticipantCount()
                    }

                    is RoomEvent.FailedToConnect -> {
                        _isConnected.value = false
                    }

                    else -> {}
                }
            }
        }
    }

    override suspend fun disconnect() {
        try {
            room?.disconnect()
            room = null
            _isConnected.value = false
            _isMicEnabled.value = false
            _participantCount.value = 0
        } catch (e: Exception) {
        }
    }

    override suspend fun enableMic() {
        try {
            room?.localParticipant?.setMicrophoneEnabled(true)
            _isMicEnabled.value = true
        } catch (e: Exception) {
        }
    }

    override suspend fun disableMic() {
        try {
            room?.localParticipant?.setMicrophoneEnabled(false)
            _isMicEnabled.value = false
        } catch (e: Exception) {
        }
    }

    private fun updateParticipantCount() {
        _participantCount.value = room?.remoteParticipants?.values?.size ?: 0
    }
}