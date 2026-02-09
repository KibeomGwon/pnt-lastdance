package com.d104.pnt.domain.model.common

import com.d104.pnt.data.remote.model.response.ErrorResponse

data class ApiError(
    val message: String,
    val code: Int? = null,
    val type: ErrorType = ErrorType.UNKNOWN
) {
    enum class ErrorType {
        NETWORK,
        SERVER,
        CLIENT,
        UNAUTHORIZED,
        FORBIDDEN,
        NOT_FOUND,
        TIMEOUT,
        UNKNOWN
    }

    companion object {
        /**
         * HTTP 상태 코드로부터 ErrorType 결정
         */
        fun getErrorType(code: Int?): ErrorType {
            return when (code) {
                in 400..499 -> when (code) {
                    401 -> ErrorType.UNAUTHORIZED
                    403 -> ErrorType.FORBIDDEN
                    404 -> ErrorType.NOT_FOUND
                    else -> ErrorType.CLIENT
                }

                in 500..599 -> ErrorType.SERVER
                else -> ErrorType.UNKNOWN
            }
        }

        /**
         * 네트워크 에러 생성
         */
        fun networkError(message: String = "네트워크 연결을 확인해주세요"): ApiError {
            return ApiError(message = message, type = ErrorType.NETWORK)
        }

        /**
         * 타임아웃 에러 생성
         */
        fun timeoutError(message: String = "요청 시간이 초과되었습니다"): ApiError {
            return ApiError(message = message, type = ErrorType.TIMEOUT)
        }

        /**
         * 알 수 없는 에러 생성
         */
        fun unknownError(message: String = "알 수 없는 오류가 발생했습니다"): ApiError {
            return ApiError(message = message, type = ErrorType.UNKNOWN)
        }
    }

    fun fromErrorBody(errorBody: String?): String {
        return try {
            val gson = com.google.gson.Gson()
            val errorResponse = gson.fromJson(errorBody, ErrorResponse::class.java)
            errorResponse.message
        } catch (e: Exception) {
            "알 수 없는 오류가 발생했습니다"
        }
    }
}