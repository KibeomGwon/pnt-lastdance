package com.d104.pnt.data.remote.model.response

import com.google.gson.annotations.SerializedName

data class ChatRoomMemberResponse(
    @SerializedName("memberId")
    val memberId: Long,

    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("avatarUrl")
    val avatarUrl: String? = null,

    @SerializedName("connected")
    val connected: Boolean,

    @SerializedName("owner")
    val owner: Boolean
)
