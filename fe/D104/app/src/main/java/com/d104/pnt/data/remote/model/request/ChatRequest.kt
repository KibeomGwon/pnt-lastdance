package com.d104.pnt.data.remote.model.request

import com.google.gson.annotations.SerializedName

// 채팅방 생성 요청
data class ChatCreateRequest(
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("regionCode")
    val regionCode: Int,
    @SerializedName("maxMembers")
    val maxMembers: Int
)

// 방장 위임 요청
data class ChatDelegateRequest(
    @SerializedName("targetMemberId")
    val targetMemberId: Long
)

// 멤버 강퇴 요청
data class ChatKickRequest(
    @SerializedName("targetMemberId")
    val targetMemberId: Long,
    @SerializedName("reason")
    val reason: String
)