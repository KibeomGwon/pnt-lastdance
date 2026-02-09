package com.d104.pnt.data.remote.model.response

data class LiveKitTokenResponse(
    val token: String,
    val roomCode: String,
    val identity: String,
    val durationMinutes: Int
)