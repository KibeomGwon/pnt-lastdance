package com.d104.pnt.data.remote.model.response

import org.json.JSONObject

// 도둑 상태
enum class ThiefStatus(val key: String?, val uiText: String) {
    FREE("FREE", "수배"),
    TRANSFER("TRANSFER", "이송"),
    PRISON("PRISON", "검거"),
    UNKNOWN(null, "-");

    companion object {
        fun fromKey(key: String?): ThiefStatus {
            if (key.isNullOrBlank()) return FREE

            return entries.find { it.key == key } ?: UNKNOWN
        }
    }
}

// 플레이어 정보 소켓
data class GameMemberSocketDto(
    val memberId: Long,
    val nickname: String,
    val avatarUrl: String,
    val position: String,
    val rawStatus: String?,
    val isConnected: Boolean
) {

    val status: ThiefStatus
        get() = ThiefStatus.fromKey(rawStatus)

    companion object {
        fun fromJson(json: JSONObject): GameMemberSocketDto {
            return GameMemberSocketDto(
                memberId = json.optLong("memberId"),
                nickname = json.optString("nickname", "알수없음"),
                avatarUrl = json.optString("avatarUrl", ""),
                position = json.optString("position", "THIEF"),
                rawStatus = json.optString("status", "FREE"),
                isConnected = json.optBoolean("isConnected", true)
            )
        }
    }
}

// 플레이어 위치 정보 소켓
data class MemberLocationSocketDto(
    val memberId: Long,
    val gameId: Long,
    val lat: Double,
    val lng: Double,
    val walk: Int,
    val longestSurvived: Int,
    val position: String,
    val status: String,
    val penalty: Int,
    val missionCompleted: Boolean,
    val timestamp: String
) {
    val statusEnum: ThiefStatus
        get() = ThiefStatus.fromKey(status)

    companion object {
        fun fromJson(json: JSONObject): MemberLocationSocketDto {
            return MemberLocationSocketDto(
                memberId = json.optLong("memberId"),
                gameId = json.optLong("gameId"),
                lat = json.optDouble("lat"),
                lng = json.optDouble("lng"),
                walk = json.optInt("walk"),
                longestSurvived = json.optInt("longestSurvived"),
                position = json.optString("position"),
                status = json.optString("status").ifEmpty { "FREE" },
                penalty = json.optInt("penalty"),
                missionCompleted = json.optBoolean("missionCompleted"),
                timestamp = json.optString("timestamp")
            )
        }
    }
}