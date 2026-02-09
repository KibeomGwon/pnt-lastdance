package com.d104.pnt.data.remote.api

import com.d104.pnt.data.remote.model.request.ChangePositionRequest
import com.d104.pnt.data.remote.model.request.CreateGameRoomRequest
import com.d104.pnt.data.remote.model.request.JoinGameRoomRequest
import com.d104.pnt.data.remote.model.request.KickRequest
import com.d104.pnt.data.remote.model.request.SaveMapRequest
import com.d104.pnt.data.remote.model.request.ToggleReadyRequest
import com.d104.pnt.data.remote.model.response.BaseResponse
import com.d104.pnt.data.remote.model.response.CreateGameRoomResponse
import com.d104.pnt.data.remote.model.response.GameMemberListResponse
import com.d104.pnt.data.remote.model.response.GameRoomSettingsResponse
import retrofit2.Response
import retrofit2.http.*
import com.d104.pnt.data.remote.model.request.UpdateRoomSettingsRequest
import com.d104.pnt.data.remote.model.response.MapData
interface GameRoomApiService {
    /**
     * 게임 생성 API
     */
    @POST("rooms")
    suspend fun createGameRoom(
        @Body request: CreateGameRoomRequest
    ): Response<BaseResponse<CreateGameRoomResponse>>

    /**
     * 게임 방 입장 API
     */
    @POST("rooms/join")
    suspend fun joinGameRoom(
        @Body request: JoinGameRoomRequest
    ): Response<BaseResponse<CreateGameRoomResponse>>

    /**
     * 방 멤버 조회 API
     */
    @GET("rooms/{roomId}/members")
    suspend fun getRoomMembers(
        @Path("roomId") roomId: Long
    ): Response<BaseResponse<GameMemberListResponse>>

    /**
     * 방 설정 조회 API
     */
    @GET("rooms/{roomId}/settings")
    suspend fun getRoomSettings(
        @Path("roomId") roomId: Long
    ): Response<BaseResponse<GameRoomSettingsResponse>>

    /**
     * 준비 상태 변경 API
     */
    @PATCH("rooms/{roomId}/ready")
    suspend fun toggleReady(
        @Path("roomId") roomId: Long,
        @Body request: ToggleReadyRequest
    ): Response<BaseResponse<Unit>>

    /**
     * 포지션 변경 API
     */
    @POST("rooms/{roomId}/position")
    suspend fun changePosition(
        @Path("roomId") roomId: Long,
        @Body request: ChangePositionRequest
    ): Response<BaseResponse<Unit>>

    /**
     * 게임 시작 API
     */
    @POST("rooms/{roomId}/start")
    suspend fun startGame(
        @Path("roomId") roomId: Long
    ): Response<BaseResponse<Unit>>

    /**
     * 방 나가기 API
     */
    @DELETE("rooms/{roomId}/members/me")
    suspend fun leaveRoom(
        @Path("roomId") roomId: Long
    ): Response<BaseResponse<Unit>>

    /**
     * 방 설정 변경 API
     */
    @PATCH("rooms/{roomId}/settings")
    suspend fun updateRoomSettings(
        @Path("roomId") roomId: Long,
        @Body request: UpdateRoomSettingsRequest
    ): Response<BaseResponse<Unit>>

    /**
     * 플레이어 강퇴 API
     */
    @POST("rooms/{roomId}/members/kick")
    suspend fun kickPlayer(
        @Path("roomId") roomId: Long,
        @Body request: KickRequest
    ): Response<BaseResponse<Unit>>

    /**
     * 맵 저장 API
     */
    @POST("maps")
    suspend fun saveMap(
        @Body request: SaveMapRequest
    ): Response<BaseResponse<Long>>

    /**
     * 내 맵 조회 API
     */
    @GET("maps")
    suspend fun getMyMaps(): Response<BaseResponse<List<MapData>>>

    /**
     * 맵 단건 조회 API
     */
    @GET("maps/{mapId}")
    suspend fun getMap(
        @Path("mapId") mapId: Long
    ): Response<BaseResponse<MapData>>

    /**
     * 맵 삭제 API
     */
    @DELETE("maps/{mapId}")
    suspend fun deleteMap(
        @Path("mapId") mapId: Long
    ): Response<BaseResponse<Unit>>
}