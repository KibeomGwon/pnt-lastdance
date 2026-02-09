package com.d104.pnt.data.repository

import com.d104.pnt.data.remote.model.response.BaseResponse
import com.d104.pnt.domain.model.common.ApiError
import com.d104.pnt.domain.model.common.BaseResult
import com.google.gson.Gson
import retrofit2.Response
import java.io.IOException
import java.net.SocketTimeoutException

abstract class BaseRepository {
    protected suspend fun <T> safeApiCall(
        onSuccess: (suspend (T) -> Unit)? = null,
        apiCall: suspend () -> Response<BaseResponse<T>>
    ): BaseResult<T> {
        return try {
            val response = apiCall()

            if (response.isSuccessful) {
                val body = response.body()

                when {
                    body == null -> {
                        BaseResult.Error(
                            ApiError(
                                message = "응답 데이터가 없습니다",
                                code = response.code(),
                                type = ApiError.ErrorType.UNKNOWN
                            )
                        )
                    }

                    body.code in 200..299 -> {
                        val data = body.data ?: Unit as T
                        onSuccess?.invoke(data)
                        BaseResult.Success(data)
                    }

                    body.data != null -> {
                        onSuccess?.invoke(body.data)
                        BaseResult.Success(body.data)
                    }

                    else -> {
                        BaseResult.Error(
                            ApiError(
                                message = body.message ?: "알 수 없는 오류",
                                code = body.code ?: response.code(),
                                type = ApiError.getErrorType(body.code)
                            )
                        )
                    }
                }
            } else {
                val errorBodyString = response.errorBody()?.string()
                val errorResponse = try {
                    Gson().fromJson(errorBodyString, BaseResponse::class.java)
                } catch (e: Exception) {
                    null
                }

                val serverMessage = errorResponse?.message ?: "서버 오류가 발생했습니다"
                val serverCode = errorResponse?.code ?: response.code()

                BaseResult.Error(
                    ApiError(
                        message = serverMessage,
                        code = serverCode,
                        type = ApiError.getErrorType(serverCode)
                    )
                )
            }
        } catch (e: SocketTimeoutException) {
            BaseResult.Error(ApiError.timeoutError())
        } catch (e: IOException) {
            BaseResult.Error(ApiError.networkError())
        } catch (e: Exception) {
            BaseResult.Error(ApiError.unknownError(e.message ?: "알 수 없는 오류"))
        }
    }
}