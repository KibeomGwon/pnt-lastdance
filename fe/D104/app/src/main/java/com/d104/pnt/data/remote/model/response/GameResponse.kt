package com.d104.pnt.data.remote.model.response

import com.google.gson.annotations.SerializedName

data class MissionResponse(
    @SerializedName("missionId")
    val missionId: Long,
    @SerializedName("title")
    val title: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("keyword")
    val keyword: String,
)