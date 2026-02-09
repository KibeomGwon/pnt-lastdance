package com.d104.pnt.data.repository

import com.d104.pnt.domain.model.common.BaseResult
import com.d104.pnt.data.remote.model.response.ReportResponse

interface ReportRepository {
    suspend fun createReport(
        reportedNickname: String,
        reason: String,
        detail: String
    ): BaseResult<ReportResponse>
}
