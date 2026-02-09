package com.d104.pnt.data.remote.model.response

import com.d104.pnt.domain.model.ChatRoomData
import com.google.gson.annotations.SerializedName

/**
 * 채팅방 생성 응답
 */
data class ChatCreateResponse(
    @SerializedName("id")
    val chatRoomId: Long,
    @SerializedName("ownerId")
    val ownerId: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("regionCode")
    val regionCode: Int,
    @SerializedName("maxMembers")
    val maxMembers: Int,
    @SerializedName("currentMembers")
    val currentMembers: Int,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("deleted")
    val deleted: Boolean
)

data class ChatSearchResponse(
    @SerializedName("chats")
    val chats: List<ChatRoomData>,
    @SerializedName("totalCount")
    val totalCount: Int
)