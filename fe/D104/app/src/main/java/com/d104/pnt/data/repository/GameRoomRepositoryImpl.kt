package com.d104.pnt.data.repository

import com.d104.pnt.data.remote.api.GameRoomApiService
import com.d104.pnt.data.remote.model.request.ChangePositionRequest
import com.d104.pnt.data.remote.model.request.CreateGameRoomRequest
import com.d104.pnt.data.remote.model.request.JoinGameRoomRequest
import com.d104.pnt.data.remote.model.request.Location
import com.d104.pnt.data.remote.model.request.SaveMapRequest
import com.d104.pnt.data.remote.model.request.ToggleReadyRequest
import com.d104.pnt.data.remote.model.request.UpdateRoomSettingsRequest
import com.d104.pnt.data.remote.model.response.CreateGameRoomResponse
import com.d104.pnt.data.remote.model.response.GameMemberListResponse
import com.d104.pnt.data.remote.model.response.GameRoomSettingsResponse
import com.d104.pnt.data.remote.model.response.MapData
import com.d104.pnt.domain.model.CurrentGameRoomData
import com.d104.pnt.domain.model.common.BaseResult
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class GameRoomRepositoryImpl @Inject constructor(
    private val apiService: GameRoomApiService,
) : GameRoomRepository, BaseRepository() {
    private val _currentGameRoom = MutableStateFlow<CurrentGameRoomData?>(null)
    override fun getCurrentGameRoom(): StateFlow<CurrentGameRoomData?> {
        return _currentGameRoom.asStateFlow()
    }

    override suspend fun createGameRoom(
        playerCount: Int,
        timeLimit: Int,
        cctvInterval: Int,
        missionCount: Int,
        policeCount: Int,
        thiefCount: Int,
        prison: Location,
        polygon: List<Location>,
        mapId: Long?,
        saveMap: Boolean,
        mapName: String?,
        mapDescription: String?
    ): BaseResult<CreateGameRoomResponse> {
        return safeApiCall(
            onSuccess = { createGameRoomResponse ->
                updateLocalGameRoom(
                    roomId = createGameRoomResponse.roomId,
                    roomCode = createGameRoomResponse.roomCode,
                    status = createGameRoomResponse.status ?: "WAITING",
                    prison = prison,
                    polygon = polygon
                )
            }
        ) {
            apiService.createGameRoom(
                CreateGameRoomRequest(
                    playerCount,
                    timeLimit,
                    cctvInterval,
                    missionCount,
                    policeCount,
                    thiefCount,
                    mapId,
                    saveMap,
                    mapName,
                    mapDescription,
                    prison,
                    polygon
                )
            )
        }
    }

    override suspend fun joinGameRoom(roomCode: String): BaseResult<CreateGameRoomResponse> {
        return safeApiCall(
            onSuccess = { response ->
                updateLocalGameRoom(
                    roomId = response.roomId,
                    roomCode = response.roomCode,
                    status = response.status ?: "WAITING"
                )
            }
        ) {
            apiService.joinGameRoom(JoinGameRoomRequest(roomCode))
        }
    }

    override suspend fun updateLocalGameRoom(
        roomId: Long,
        roomCode: String,
        status: String,
        prison: Location?,
        polygon: List<Location>?
    ) {
        val prisonLatLng = if (prison != null) LatLng(prison.lat, prison.lng) else LatLng(0.0, 0.0)

        val polygonLatLng = polygon?.map { LatLng(it.lat, it.lng) } ?: emptyList()

        val roomData = CurrentGameRoomData(
            roomId = roomId,
            roomCode = roomCode,
            status = status,
            timeLimit = 0,
            cctvFrequency = 0,
            missionCount = 5,
            playerCount = 0,
            maxPlayerCount = 0,
            myMemberId = 0,
            roomManagerId = 0,
            policeCount = 0,
            thiefCount = 0,
            missionCompleteCount = 0,
            preferPosition = "",

            prison = prisonLatLng,
            polygon = polygonLatLng
        )
        _currentGameRoom.value = roomData
    }

    override suspend fun getRoomMembers(roomId: Long): BaseResult<GameMemberListResponse> {
        return safeApiCall { apiService.getRoomMembers(roomId) }
    }

    override suspend fun getRoomSettings(roomId: Long): BaseResult<GameRoomSettingsResponse> {
        return safeApiCall { apiService.getRoomSettings(roomId) }
    }

    override suspend fun toggleReady(roomId: Long, isReady: Boolean): BaseResult<Unit> {
        return safeApiCall {
            apiService.toggleReady(roomId, ToggleReadyRequest(isReady))
        }
    }

    override suspend fun changePosition(roomId: Long, position: String): BaseResult<Unit> {
        return safeApiCall {
            apiService.changePosition(roomId, ChangePositionRequest(position))
        }
    }

    override suspend fun startGame(roomId: Long): BaseResult<Unit> {
        return safeApiCall { apiService.startGame(roomId) }
    }

    override suspend fun leaveRoom(roomId: Long): BaseResult<Unit> {
        return safeApiCall { apiService.leaveRoom(roomId) }
    }

    override suspend fun updateRoomSettings(
        roomId: Long,
        playerCount: Int,
        timeLimit: Int,
        cctvInterval: Int,
        policeCount: Int,
        thiefCount: Int,
        missionCount: Int,
        prison: Location?,
        polygon: List<Location>?
    ): BaseResult<Unit> {
        return safeApiCall {
            apiService.updateRoomSettings(
                roomId = roomId,
                request = UpdateRoomSettingsRequest(
                    playerCount = playerCount,
                    timeLimit = timeLimit,
                    cctvInterval = cctvInterval,
                    policeCount = policeCount,
                    thiefCount = thiefCount,
                    missionCount = missionCount,
                    prison = prison,
                    polygon = polygon
                )
            )
        }
    }

    override suspend fun kickPlayer(
        roomId: Long,
        targetMemberId: Long,
        reason: String
    ): BaseResult<Unit> {
        return safeApiCall {
            apiService.kickPlayer(
                roomId,
                com.d104.pnt.data.remote.model.request.KickRequest(targetMemberId, reason)
            )
        }
    }

    /**
     * 새로운 맵 저장
     */
    override suspend fun saveMap(request: SaveMapRequest): BaseResult<Long> {
        return safeApiCall {
            apiService.saveMap(request)
        }
    }

    /**
     * 내가 생성한 맵 목록 조회
     */
    override suspend fun getMyMaps(): BaseResult<List<MapData>> {
        return safeApiCall {
            apiService.getMyMaps()
        }
    }

    /**
     * 특정 ID의 맵 상세 정보 조회
     */
    override suspend fun getMap(mapId: Long): BaseResult<MapData> {
        return safeApiCall {
            apiService.getMap(mapId)
        }
    }

    /**
     * 특정 맵 삭제
     */
    override suspend fun deleteMap(mapId: Long): BaseResult<Unit> {
        return safeApiCall {
            apiService.deleteMap(mapId)
        }
    }
}