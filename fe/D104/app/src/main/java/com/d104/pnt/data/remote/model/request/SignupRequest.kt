package com.d104.pnt.data.remote.model.request

import com.google.gson.annotations.SerializedName

/**
 * 회원가입 요청
 */
data class SignupRequest(
    @SerializedName("id")
    val id: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("passwordConfirm")
    val passwordConfirm: String,

    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("birth")
    val birth: String,

    @SerializedName("avatarUrl")
    val avatarUrl: String? = null
)


/**
 * 아이디 중복 체크 요청
 */
data class CheckDuplicateRequest(
    @SerializedName("id")
    val id: String,
)