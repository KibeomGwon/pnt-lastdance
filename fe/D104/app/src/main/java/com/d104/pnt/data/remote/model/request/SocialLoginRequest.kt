package com.d104.pnt.data.remote.model.request

import com.google.gson.annotations.SerializedName

/**
 * 소셜 로그인 요청
 */
data class SocialLoginRequest(
    @SerializedName("provider")
    val provider: String,

    @SerializedName("token")
    val token: String
)