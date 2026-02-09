package com.d104.pnt.data.remote.model.request

data class ReportRequest(
    val reportedNickname: String,
    val reason: String,
    val detail: String
)
