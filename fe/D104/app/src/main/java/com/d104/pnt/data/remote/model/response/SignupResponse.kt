package com.d104.pnt.data.remote.model.response

import com.google.gson.annotations.SerializedName

/**
 * 회원가입 응답
 */
data class SignupResponse(
    @SerializedName("memberId")
    val memberId: Long,

    @SerializedName("id")
    val id: String,

    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("createdAt")
    val createdAt: String
)

/**
 * 아이디 중복 체크 응답
 */
data class DuplicateCheckResponse(
    @SerializedName("duplicated")
    val duplicated: Boolean
)