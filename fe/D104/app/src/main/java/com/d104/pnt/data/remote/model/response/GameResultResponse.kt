package com.d104.pnt.data.remote.model.response

data class GameResultResponse(
    val gameId: Long,
    val winner: String,
    val endedAt: String,
    val stats: GameStats,
    val mvp: PlayerResult?,
    val winningSecond: PlayerResult?,
    val losingFirst: PlayerResult?,
    val myStat: GameResultMyStat
)

data class GameStats(
    val arrests: Int,
    val missionsCleared: Int,
    val durationSec: Long
)

data class PlayerResult(
    val memberId: Long,
    val nickname: String,
    val role: String,
    val description: String,
    val walk: Int,
    val arrestCount: Int,
    val longestSurvived: Int,
    val rank: String,
    val maxArrestCount: Int,
    val maxSurvivalTime: Int
)

data class GameResultMyStat(
    val memberId: Long,
    val nickname: String,
    val role: String,
    val walk: Int,
    val arrestCount: Int,
    val longestSurvived: Int,
    val rank: String,
    val maxArrestCount: Int,
    val maxSurvivalTime: Int
)