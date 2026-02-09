package com.d104.pnt.domain.model

import com.google.gson.annotations.SerializedName

data class PlayerData(
    @SerializedName("id")
    val id: Long,
    @SerializedName("gameId")
    val gameId: Long,
    @SerializedName("memberId")
    val memberId: Long,
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lng")
    val lng: Double,
    @SerializedName("walk")
    val walk: Int,
    @SerializedName("longestSurvived")
    val longestSurvived: Int,
    @SerializedName("position")
    val position: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("penalty")
    val penalty: Int,
    @SerializedName("timeStamp")
    val timeStamp: String
)