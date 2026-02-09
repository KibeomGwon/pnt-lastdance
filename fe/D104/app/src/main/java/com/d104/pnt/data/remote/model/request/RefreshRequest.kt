package com.d104.pnt.data.remote.model.request

import com.google.gson.annotations.SerializedName

data class RefreshRequest(
    @SerializedName("grantType")
    val grantType: String,
    @SerializedName("accessToken")
    val accessToken: String,
    @SerializedName("refreshToken")
    val refreshToken: String,
    @SerializedName("accessTokenExpiresIn")
    val accessTokenExpiresIn: Long,
    @SerializedName("refreshTokenExpiresIn")
    val refreshTokenExpiresIn: Long
)
