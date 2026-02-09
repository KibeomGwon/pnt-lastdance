package com.d104.pnt.domain.model

import com.google.gson.annotations.SerializedName

data class ChatRoomData(
    @SerializedName("id")
    val id: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("ownerId")
    val ownerId: Long,
    @SerializedName("regionCode")
    val regionCode: Int,
    @SerializedName("description")
    val description: String,
    @SerializedName("maxMembers")
    val maxMember: Int,
    @SerializedName("currentMembers")
    val currentMember: Int,
    @SerializedName("createdAt")
    val createdAt: String,
    @SerializedName("updatedAt")
    val updatedAt: String,
    @SerializedName("deleted")
    val deleted: Boolean
)
