package com.d104.pnt.data.repository

import com.d104.pnt.data.remote.model.request.Location
import com.d104.pnt.data.remote.model.request.SaveMapRequest
import com.d104.pnt.data.remote.model.response.CreateGameRoomResponse
import com.d104.pnt.data.remote.model.response.GameMemberListResponse
import com.d104.pnt.data.remote.model.response.GameRoomSettingsResponse
import com.d104.pnt.data.remote.model.response.MapData
import com.d104.pnt.domain.model.CurrentGameRoomData
import com.d104.pnt.domain.model.common.BaseResult
import kotlinx.coroutines.flow.StateFlow

interface GameRoomRepository {

    fun getCurrentGameRoom(): StateFlow<CurrentGameRoomData?>

    /**
     * 게임방 생성
     */
    suspend fun createGameRoom(
        playerCount: Int,
        timeLimit: Int,
        cctvInterval: Int,
        missionCount: Int,
        policeCount: Int,
        thiefCount: Int,
        prison: Location,
        polygon: List<Location>,
        mapId: Long? = null,
        saveMap: Boolean = false,
        mapName: String? = null,
        mapDescription: String? = null
    ): BaseResult<CreateGameRoomResponse>

    /**
     * 게임방 정보 저장
     */
    suspend fun updateLocalGameRoom(
        roomId: Long,
        roomCode: String,
        status: String,
        prison: Location? = null,
        polygon: List<Location>? = null
    )

    /**
     * 게임방 입장
     */
    suspend fun joinGameRoom(roomCode: String): BaseResult<CreateGameRoomResponse>

    /**
     * 게임방 설정 변경
     */
    suspend fun updateRoomSettings(
        roomId: Long,
        playerCount: Int,
        timeLimit: Int,
        cctvInterval: Int,
        policeCount: Int,
        thiefCount: Int,
        missionCount: Int,
        prison: Location?,
        polygon: List<Location>?
    ): BaseResult<Unit>

    /**
     * 플레이어 강퇴
     */
    suspend fun kickPlayer(roomId: Long, targetMemberId: Long, reason: String): BaseResult<Unit>

    /**
     * 게임방 멤버 조회
     */
    suspend fun getRoomMembers(roomId: Long): BaseResult<GameMemberListResponse>

    /**
     * 게임방 설정 조회
     */
    suspend fun getRoomSettings(roomId: Long): BaseResult<GameRoomSettingsResponse>

    /**
     * 준비 상태 변경
     */
    suspend fun toggleReady(roomId: Long, isReady: Boolean): BaseResult<Unit>

    /**
     * 역할 변경
     */
    suspend fun changePosition(roomId: Long, position: String): BaseResult<Unit>

    /**
     * 게임 시작
     */
    suspend fun startGame(roomId: Long): BaseResult<Unit>

    /**
     * 게임방 나가기
     */
    suspend fun leaveRoom(roomId: Long): BaseResult<Unit>

    /**
     * 맵 저장
     */
    suspend fun saveMap(request: SaveMapRequest): BaseResult<Long>

    /**
     * 내가 저장한 맵 조회
     */
    suspend fun getMyMaps(): BaseResult<List<MapData>>

    /**
     * 맵 상세 조회
     */
    suspend fun getMap(mapId: Long): BaseResult<MapData>

    /**
     * 맵 삭제
     */
    suspend fun deleteMap(mapId: Long): BaseResult<Unit>
}