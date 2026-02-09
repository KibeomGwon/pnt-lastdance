package com.d104.pnt.data.remote.model.response

import com.google.gson.annotations.SerializedName

/**
 * 프로필 조회 응답
 */
data class ProfileResponse(
    @SerializedName("avatarUrl")
    val avatarUrl: String?,

    @SerializedName("createdAt")
    val createdAt: String,

    @SerializedName("loginId")
    val loginId: String,

    @SerializedName("memberId")
    val memberId: Long,

    @SerializedName("nickname")
    val nickname: String?,

    @SerializedName("stat")
    val stat: Stat
)

data class Stat(
    @SerializedName("loses")
    val loses: Int,

    @SerializedName("policeGame")
    val policeGame: Int,

    @SerializedName("policeGrade")
    val policeGrade: String,

    @SerializedName("thiefGame")
    val thiefGame: Int,

    @SerializedName("thiefGrade")
    val thiefGrade: String,

    @SerializedName("totalGames")
    val totalGames: Int,

    @SerializedName("wins")
    val wins: Int
)
