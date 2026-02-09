package com.d104.pnt.domain.model

import androidx.compose.ui.graphics.Color
import com.d104.pnt.R
import com.d104.pnt.ui.theme.CustomBlue
import com.d104.pnt.ui.theme.CustomRed

/**
 * 게임 역할 모델
 */
enum class GameRole(
    val roleName: String,
    val roleNameEn: String,
    val description: String,
    val color: Color,
    val emoji: Int,
    val badge: Int
) {
    POLICE(
        roleName = "경찰",
        roleNameEn = "POLICE",
        description = "제한 시간 내에 모든 도둑을 검거하세요",
        color = CustomBlue,
        emoji = R.drawable.ic_police,
        badge = R.drawable.ic_police_badge
    ),

    THIEF(
        roleName = "도둑",
        roleNameEn = "THIEF",
        description = "제한 시간동안 경찰로부터 생존하세요",
        color = CustomRed,
        emoji = R.drawable.ic_thief,
        badge = R.drawable.ic_thief_badge
    ),

    ANY(
        roleName = "랜덤",
        roleNameEn = "ANY",
        description = "역할이 자동으로 배정됩니다",
        color = Color.Gray,
        emoji = R.drawable.ic_thief,
        badge = R.drawable.ic_thief_badge
    ),

    UNDECIDED(
        roleName = "미정",
        roleNameEn = "UNDECIDED",
        description = "역할을 선택하는 중입니다",
        color = Color.Gray,
        emoji = R.drawable.ic_thief,
        badge = R.drawable.ic_thief_badge
    );

    companion object {
        fun fromName(name: String): GameRole {
            return values().find {
                it.name.equals(name, ignoreCase = true) ||
                        it.roleNameEn.equals(name, ignoreCase = true)
            } ?: UNDECIDED
        }

        fun getAllRoleNames(): List<String> {
            return values().map { it.roleName }
        }

        fun calculateTeamSize(totalPlayers: Int, policeRatio: Float = 0.3f): Pair<Int, Int> {
            val policeCount = (totalPlayers * policeRatio).toInt().coerceAtLeast(1)
            val thiefCount = totalPlayers - policeCount
            return Pair(policeCount, thiefCount)
        }
    }

    fun opposite(): GameRole {
        return when (this) {
            POLICE -> THIEF
            THIEF -> POLICE
            else -> this
        }
    }

    fun isTeammate(other: GameRole): Boolean {
        return this == other
    }

    fun getBadgeText(): String {
        return "$emoji $roleName"
    }
}

/**
 * 플레이어 상태
 */
enum class PlayerStatus(
    val statusName: String,
    val color: Color
) {
    ACTIVE(
        statusName = "활동 중",
        color = Color.Green
    ),
    ARRESTED(
        statusName = "체포됨",
        color = Color.Red
    ),
    IN_PRISON(
        statusName = "수감 중",
        color = Color.Red
    ),
    OUT_OF_BOUND(
        statusName = "이탈",
        color = Color.Yellow
    ),
    ESCAPED(
        statusName = "탈출",
        color = Color.Cyan
    );

    companion object {
        fun fromName(name: String): PlayerStatus {
            return values().find {
                it.name.equals(name, ignoreCase = true)
            } ?: ACTIVE
        }
    }
}

/**
 * 게임 결과
 */
enum class GameResult(
    val resultName: String,
    val description: String
) {
    POLICE_WIN(
        resultName = "경찰 승리",
        description = "모든 도둑이 체포되었습니다"
    ),
    THIEF_WIN(
        resultName = "도둑 승리",
        description = "도둑이 미션을 완료했습니다"
    ),
    TIME_OVER_POLICE(
        resultName = "경찰 승리",
        description = "제한 시간 종료 (경찰 우세)"
    ),
    TIME_OVER_THIEF(
        resultName = "도둑 승리",
        description = "제한 시간 종료 (도둑 생존)"
    ),
    DRAW(
        resultName = "무승부",
        description = "게임이 무승부로 종료되었습니다"
    );

    companion object {
        fun fromName(name: String): GameResult {
            return values().find {
                it.name.equals(name, ignoreCase = true)
            } ?: DRAW
        }
    }
}