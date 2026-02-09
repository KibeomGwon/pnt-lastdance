package com.d104.pnt.data.remote.api

import com.d104.pnt.data.remote.model.response.BaseResponse
import com.d104.pnt.data.remote.model.response.MissionPresignedUrlResponse
import com.d104.pnt.data.remote.model.response.PresignedUrlResponse
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Url

interface ImageApiService {

    /**
     * 이미지 업로드 API
     */
    @Headers("X-No-Auth: true")
    @PUT
    suspend fun uploadImage(
        @Url url: String,
        @Body requestBody: RequestBody,
    ): Response<Unit>

    /**
     * 프로필 이미지용 Presigned URL 발급 API
     */
    @GET("members/{id}/presigned-url")
    suspend fun getPresignedUrlForProfile(
        @Path("id") id: Long,
        @Query("fileName") fileName: String,
    ): Response<BaseResponse<PresignedUrlResponse>>

    /**
     * 미션 이미지용 Presigned URL 발급 API
     */
    @GET("games/missions/upload-url")
    suspend fun getPresignedUrlForMission(
        @Query("fileName") fileName: String,
    ): Response<BaseResponse<MissionPresignedUrlResponse>>
}