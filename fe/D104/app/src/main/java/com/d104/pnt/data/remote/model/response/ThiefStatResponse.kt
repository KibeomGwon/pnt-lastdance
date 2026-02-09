package com.d104.pnt.data.remote.model.response

import com.google.gson.annotations.SerializedName

/**
 * 도둑 스탯 조회 응답
 */
data class ThiefStatResponse(
    @SerializedName("memberId")
    val memberId: Long,

    @SerializedName("thiefStat")
    val thiefStat: ThiefStat
)

data class ThiefStat(
    @SerializedName("averageSurvivalTimeSec")
    val averageSurvivalTimeSec: Double,

    @SerializedName("escapeCount")
    val escapeCount: Int,

    @SerializedName("grade")
    val grade: String,

    @SerializedName("longestSurvivalSec")
    val longestSurvivalSec: Int,

    @SerializedName("missionClearCount")
    val missionClearCount: Int
)