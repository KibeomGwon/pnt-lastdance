package com.d104.pnt.data.repository

import kotlinx.coroutines.flow.StateFlow

/**
 * 무전기 Repository 인터페이스
 */
interface WalkieRepository {
    val isConnected: StateFlow<Boolean>
    val isMicEnabled: StateFlow<Boolean>
    val participantCount: StateFlow<Int>

    suspend fun connect(serverUrl: String, token: String, roomName: String)
    suspend fun disconnect()
    suspend fun enableMic()
    suspend fun disableMic()
}