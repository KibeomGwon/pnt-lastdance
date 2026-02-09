package com.d104.pnt.data.remote.api

import com.d104.pnt.data.remote.model.request.ReportRequest
import com.d104.pnt.data.remote.model.response.BaseResponse
import com.d104.pnt.data.remote.model.response.ReportResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ReportApiService {

    /**
     * 멤버 신고 API
     */
    @POST("reports")
    suspend fun createReport(
        @Body request: ReportRequest
    ): Response<BaseResponse<ReportResponse>>
}
