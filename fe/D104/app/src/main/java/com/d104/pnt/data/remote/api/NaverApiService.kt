package com.d104.pnt.data.remote.api

import com.d104.pnt.domain.model.NaverReverseGeocodeResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query

interface NaverApiService {

    /**
     * 주소 조회 API
     */
    @GET("map-reversegeocode/v2/gc")
    suspend fun getAddress(
        @Header("x-ncp-apigw-api-key-id") clientId: String,
        @Header("x-ncp-apigw-api-key") clientSecret: String,
        @Query("coords") coords: String,
        @Query("output") output: String = "json",
        @Query("orders") orders: String = "legalcode"
    ): NaverReverseGeocodeResponse
}