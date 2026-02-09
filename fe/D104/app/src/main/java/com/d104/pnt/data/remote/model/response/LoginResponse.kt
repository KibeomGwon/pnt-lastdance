package com.d104.pnt.data.remote.model.response

import com.google.gson.annotations.SerializedName

/**
 * 로그인 응답
 */
data class LoginResponse(
    @SerializedName("accessToken")
    val accessToken: String,

    @SerializedName("refreshToken")
    val refreshToken: String,

    @SerializedName("member")
    val member: MemberData
)

/**
 * 회원 정보
 */
data class MemberData(
    @SerializedName("memberId")
    val memberId: Long,

    @SerializedName("id")
    val id: String,

    @SerializedName("nickname")
    val nickname: String?,

    @SerializedName("avatarUrl")
    val avatarUrl: String?,

    @SerializedName("role")
    val role: String?
)