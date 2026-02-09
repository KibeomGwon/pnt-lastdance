package com.d104.pnt.data.remote.api

import com.d104.pnt.data.remote.model.request.LiveKitTokenRequest
import com.d104.pnt.data.remote.model.response.BaseResponse
import com.d104.pnt.data.remote.model.response.GameNewsResponse
import com.d104.pnt.data.remote.model.response.GameResultResponse
import com.d104.pnt.data.remote.model.response.LiveKitTokenResponse
import com.d104.pnt.data.remote.model.response.MissionResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface GameApiService {

    /**
     * 미션 목록 조회 API
     */
    @GET("games/{gameId}/missions")
    suspend fun getMissionList(
        @Path("gameId") gameId: Long,
    ): Response<BaseResponse<List<MissionResponse>>>

    /**
     * 미션 상세 조회 API
     */
    @GET("games/{gameId}/missions/{missionsId}")
    suspend fun getMissionDetail(
        @Path("gameId") gameId: Long,
        @Path("missionsId") missionsId: Long,
    ): Response<BaseResponse<MissionResponse>>

    /**
     * 게임 삭제 API
     */
    @DELETE("dev/games/{gameId}")
    suspend fun deleteGame(
        @Path("gameId") gameId: Long
    ): Response<BaseResponse<Unit>>

    /**
     * 게임 뉴스 조회 API
     */
    @GET("games/news/{gameId}")
    suspend fun getGameNews(
        @Path("gameId") gameId: Long
    ): Response<BaseResponse<GameNewsResponse>>

    /**
     * 게임 결과 조회 API
     */
    @GET("games/{gameId}/result")
    suspend fun getGameResult(
        @Path("gameId") gameId: Long
    ): Response<BaseResponse<GameResultResponse>>

    /**
     * LiveKit 토큰 발급 API
     */
    @POST("livekit/token")
    suspend fun getLiveKitToken(
        @Body request: LiveKitTokenRequest
    ): Response<BaseResponse<LiveKitTokenResponse>>
}