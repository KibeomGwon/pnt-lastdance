package com.d104.pnt.data.remote.model.response

import com.google.gson.annotations.SerializedName
import org.json.JSONObject

data class MissionSocketDto(
    @SerializedName("id")
    val id: Long,
    @SerializedName("gameId")
    val gameId: Long,
    @SerializedName("missionId")
    val missionId: Long,
    @SerializedName("completedBy")
    val completedBy: Long,
    @SerializedName("completedAt")
    val completedAt: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("Mission")
    val Mission: Mission,
) {
    companion object {
        fun fromJson(json: JSONObject): MissionSocketDto {
            val missionObj = json.getJSONObject("Mission")
            val missionData = Mission(
                id = missionObj.optLong("id"),
                title = missionObj.optString("title"),
                description = missionObj.optString("description"),
                keyword = missionObj.optString("keyword")
            )
            return MissionSocketDto(
                id = json.optLong("id"),
                gameId = json.optLong("gameId"),
                missionId = json.optLong("missionId"),
                completedBy = json.optLong("completedBy"),
                completedAt = json.optString("completedAt"),
                status = json.optString("status"),
                Mission = missionData
            )
        }
    }
}

data class Mission(
    val id: Long,
    val title: String,
    val description: String,
    val keyword: String
)