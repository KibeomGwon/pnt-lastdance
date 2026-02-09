package com.d104.pnt.data.remote.model.response

import com.google.gson.annotations.SerializedName

/**
 * 경찰 스탯 조회 응답
 */
data class PoliceStatResponse(
    @SerializedName("memberId")
    val memberId: Long,

    @SerializedName("policeStat")
    val policeStat: PoliceStat
)

/**
 * 도둑 스탯 조회 응답
 */
data class PoliceStat(
    @SerializedName("arrestCount")
    val arrestCount: Int,

    @SerializedName("grade")
    val grade: String,

    @SerializedName("mostArrestsInGame")
    val mostArrestsInGame: Int,

    @SerializedName("totalArrestCount")
    val totalArrestCount: Int
)