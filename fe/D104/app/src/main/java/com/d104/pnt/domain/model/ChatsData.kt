package com.d104.pnt.domain.model

data class ChatsData(
    val id: Long,
    val title: String,
    val description: String,
    val maxMember: Int,
    val currentMember: Int,

)
