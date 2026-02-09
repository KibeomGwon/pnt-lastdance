package com.d104.pnt.domain.model

import com.google.android.gms.maps.model.LatLng

data class CurrentGameRoomData(
    val roomId: Long,
    val roomCode: String,

    // Info
    val status: String,
    val timeLimit: Int,
    val cctvFrequency: Int,
    val missionCount: Int,
    val playerCount: Int,
    val maxPlayerCount: Int,

    // Member
    val myMemberId: Long,
    val roomManagerId: Long,

    val policeCount: Int,
    val thiefCount: Int,
    val missionCompleteCount: Int,

    val preferPosition: String,
    val prison: LatLng,
    val polygon: List<LatLng>,
    )
