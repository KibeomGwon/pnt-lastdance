package com.d104.pnt.data.remote.model.request

import com.google.gson.annotations.SerializedName

data class UpdateRoomSettingsRequest(
    @SerializedName("playerCount")
    val playerCount: Int,

    @SerializedName("timeLimit")
    val timeLimit: Int,

    @SerializedName("policeCount")
    val policeCount: Int,

    @SerializedName("thiefCount")
    val thiefCount: Int,

    @SerializedName("cctvInterval")
    val cctvInterval: Int,

    @SerializedName("missionCount")
    val missionCount: Int,

    @SerializedName("prison")
    val prison: Location? = null,

    @SerializedName("polygon")
    val polygon: List<Location>? = null
)