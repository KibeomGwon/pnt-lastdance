package com.d104.pnt.data.remote.api

import com.d104.pnt.data.remote.model.request.CheckDuplicateRequest
import com.d104.pnt.data.remote.model.request.FcmTokenRequest
import com.d104.pnt.data.remote.model.request.LoginRequest
import com.d104.pnt.data.remote.model.request.RefreshRequest
import com.d104.pnt.data.remote.model.request.SignupRequest
import com.d104.pnt.data.remote.model.request.SocialLoginRequest
import com.d104.pnt.data.remote.model.response.BaseResponse
import com.d104.pnt.data.remote.model.response.DuplicateCheckResponse
import com.d104.pnt.data.remote.model.response.LoginResponse
import com.d104.pnt.data.remote.model.response.RefreshResponse
import com.d104.pnt.data.remote.model.response.SignupResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApiService {

    /**
     * FCM Token 전송 API
     */
    @POST("members/fcm-token")
    suspend fun postFcmToken(
        @Body request: FcmTokenRequest
    ): Response<BaseResponse<Unit>>

    /**
     * 로그인 API
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<BaseResponse<LoginResponse>>

    /**
     * 소셜 로그인 API
     */
    @POST("auth/social-login")
    suspend fun socialLogin(
        @Body request: SocialLoginRequest
    ): Response<BaseResponse<LoginResponse>>

    /**
     * 아이디 중복 체크 API
     */
    @POST("auth/duplicate")
    suspend fun checkDuplicate(
        @Body request: CheckDuplicateRequest
    ): Response<BaseResponse<DuplicateCheckResponse>>

    /**
     * 회원가입 API
     */
    @POST("auth/signup")
    suspend fun signup(
        @Body request: SignupRequest
    ): Response<BaseResponse<SignupResponse>>

    /**
     * 로그아웃 API
     */
    @POST("auth/logout")
    suspend fun logout(): Response<BaseResponse<String>>

    /**
     * 토큰 재발급 동기 API
     */
    @POST("auth/reissue")
    fun refreshTokenCall(
        @Body request: RefreshRequest
    ): Call<BaseResponse<RefreshResponse>>
}
