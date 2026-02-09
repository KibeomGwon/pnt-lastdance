package com.d104.pnt.data.remote.model.request

data class KickRequest(
    val targetMemberId: Long,
    val reason: String
)