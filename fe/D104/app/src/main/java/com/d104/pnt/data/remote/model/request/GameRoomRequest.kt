package com.d104.pnt.data.remote.model.request

import com.google.gson.annotations.SerializedName

/**
 * 게임 방 생성 요청
 */
data class CreateGameRoomRequest(
    @SerializedName("playerCount")
    val playerCount: Int,
    @SerializedName("timeLimit")
    val timeLimit: Int,
    @SerializedName("cctvInterval")
    val cctvInterval: Int,
    @SerializedName("missionCount")
    val missionCount: Int,
    @SerializedName("policeCount")
    val policeCount: Int,
    @SerializedName("thiefCount")
    val thiefCount: Int,
    @SerializedName("mapId")
    val mapId: Long?,
    @SerializedName("saveMap")
    val saveMap: Boolean,
    @SerializedName("mapName")
    val mapName: String?,
    @SerializedName("mapDescription")
    val mapDescription: String?,
    @SerializedName("prison")
    val prison: Location?,
    @SerializedName("polygon")
    val polygon: List<Location>?
)

data class Location(
    @SerializedName("lat")
    val lat: Double,
    @SerializedName("lng")
    val lng: Double
)

data class ToggleReadyRequest(
    @SerializedName("ready")
    val ready: Boolean
)

data class ChangePositionRequest(
    @SerializedName("preferPosition")
    val preferPosition: String
)

data class JoinGameRoomRequest(
    @SerializedName("roomCode")
    val roomCode: String
)

data class SaveMapRequest(
    @SerializedName("name")
    val name: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("prison")
    val prison: Location,

    @SerializedName("polygon")
    val polygon: List<Location>
)