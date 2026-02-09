package com.d104.pnt.data.remote.model.response

import com.google.gson.annotations.SerializedName

/**
 * 채팅방 생성 응답
 */
data class CreateGameRoomResponse(
    @SerializedName("roomId")
    val roomId: Long,

    @SerializedName("roomCode")
    val roomCode: String,

    @SerializedName("status")
    val status: String?,
)

data class GameRoomSettingsResponse(
    @SerializedName("roomId")
    val roomId: Long,

    @SerializedName("roomCode")
    val roomCode: String,

    @SerializedName("status")
    val status: String?,

    @SerializedName("timeLimit")
    val timeLimit: Int,

    @SerializedName("playerCount")
    val playerCount: Int,

    @SerializedName("policeCount")
    val policeCount: Int,

    @SerializedName("thiefCount")
    val thiefCount: Int,

    @SerializedName("cctvInterval")
    val cctvInterval: Int,

    @SerializedName("missionCount")
    val missionCount: Int,

    @SerializedName("prisonLat")
    val prisonLat: Double,

    @SerializedName("prisonLng")
    val prisonLng: Double,
)

data class GameMemberListResponse(
    @SerializedName("roomId")
    val roomId: Long,

    @SerializedName("items")
    val items: List<Item>
)

data class Item(
    @SerializedName("memberId")
    val memberId: Long,

    @SerializedName("nickname")
    val nickname: String,

    @SerializedName("preferPosition")
    val preferPosition: String,

    @SerializedName("givenPosition")
    val givenPosition: String?,

    @SerializedName("avatarUrl")
    val avatarUrl: String?,

    @SerializedName("host")
    val host: Boolean,

    @SerializedName("ready")
    val ready: Boolean
)