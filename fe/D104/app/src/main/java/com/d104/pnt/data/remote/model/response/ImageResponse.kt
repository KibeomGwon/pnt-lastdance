package com.d104.pnt.data.remote.model.response

import com.google.gson.annotations.SerializedName

data class PresignedUrlResponse (
    @SerializedName("presignedUrl")
    val presignedUrl: String,
    @SerializedName("imageKey")
    val imageKey: String
)

data class MissionPresignedUrlResponse (
    @SerializedName("presignedUrl")
    val presignedUrl: String,
    @SerializedName("downloadUrl")
    val downloadUrl: String,
    @SerializedName("imageKey")
    val imageKey: String
)