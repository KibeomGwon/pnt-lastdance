package com.d104.pnt.domain.model

data class ChatMessage(
    val id: Int,
    val chatRoomId: Long,
    val memberId: Long,
    val senderNickname: String,
    val avatarUrl: String,
    val content: String,
)