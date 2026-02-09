package com.d104.pnt.data.remote.model.request

import com.google.gson.annotations.SerializedName

/**
 * 로그인 요청
 */
data class LoginRequest(
    @SerializedName("id")
    val id: String,

    @SerializedName("password")
    val password: String
)