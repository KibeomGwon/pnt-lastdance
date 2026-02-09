package com.d104.pnt.data.repository

import com.d104.pnt.data.remote.api.ReportApiService
import com.d104.pnt.data.remote.model.request.ReportRequest
import com.d104.pnt.data.remote.model.response.ReportResponse
import com.d104.pnt.domain.model.common.ApiError
import com.d104.pnt.domain.model.common.BaseResult
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val reportApiService: ReportApiService
) : ReportRepository {

    override suspend fun createReport(
        reportedNickname: String,
        reason: String,
        detail: String
    ): BaseResult<ReportResponse> {
        return try {
            val response = reportApiService.createReport(
                ReportRequest(
                    reportedNickname = reportedNickname,
                    reason = reason,
                    detail = detail
                )
            )

            if (response.isSuccessful) {
                val body = response.body()
                val data = body?.data
                    ?: return BaseResult.Error(ApiError(message = "응답 데이터가 없습니다."))

                BaseResult.Success(data)
            } else {
                BaseResult.Error(ApiError(message = "신고 실패: ${response.code()}"))
            }
        } catch (e: Exception) {
            BaseResult.Error(ApiError(message = e.message ?: "신고 요청 실패"))
        }
    }
}
