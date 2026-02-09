package com.d104.pnt.data.remote.model.response


/**
 * 채팅방 참여 응답
 */
data class JoinChatRoomResponse(
    val id: Long,
    val memberId: Long,
    val chatRoomId: Long,
    val createdAt: String,
    val updatedAt: String,
    val connected: Boolean,
    val deleted: Boolean
)
