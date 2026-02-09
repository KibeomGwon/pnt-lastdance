package com.d104.pnt.data.remote.model.response

data class ErrorResponse(
    val data: Any?,
    val message: String,
    val code: Int
)