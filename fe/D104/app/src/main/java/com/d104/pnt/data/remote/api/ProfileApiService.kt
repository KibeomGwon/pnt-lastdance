package com.d104.pnt.data.remote.api

import com.d104.pnt.data.remote.model.request.UpdateNicknameRequest
import com.d104.pnt.data.remote.model.request.UpdateProfileImageRequest
import com.d104.pnt.data.remote.model.response.BaseResponse
import com.d104.pnt.data.remote.model.response.PoliceStatResponse
import com.d104.pnt.data.remote.model.response.ProfileResponse
import com.d104.pnt.data.remote.model.response.ThiefStatResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface ProfileApiService {

    /**
     * 내 프로필 조회
     */
    @GET("members/{id}")
    suspend fun getMyProfile(
        @Path("id") memberId: Long
    ): Response<BaseResponse<ProfileResponse>>

    /**
     * 도둑 스탯 조회 API
     */
    @GET("members/{id}/thief")
    suspend fun getThiefStat(
        @Path("id") memberId: Long
    ): Response<BaseResponse<ThiefStatResponse>>

    /**
     * 경찰 스탯 조회 API
     */
    @GET("members/{id}/police")
    suspend fun getPoliceStat(
        @Path("id") memberId: Long
    ): Response<BaseResponse<PoliceStatResponse>>

    /**
     * 프로필 수정 API
     */
    @PATCH("members/{id}")
    suspend fun updateProfileImage(
        @Path("id") memberId: Long,
        @Body request: UpdateProfileImageRequest
    ): Response<BaseResponse<ProfileResponse>>

    @PATCH("members/{id}")
    suspend fun updateNickname(
        @Path("id") memberId: Long,
        @Body request: UpdateNicknameRequest
    ): Response<BaseResponse<ProfileResponse>>
}
