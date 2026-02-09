package com.d104.pnt.domain.model.common

sealed class BaseResult<out T> {
    data class Success<T>(val data: T) : BaseResult<T>()
    data class Error(val error: ApiError) : BaseResult<Nothing>()

    /**
     * 성공 여부 확인
     */
    val isSuccess: Boolean
        get() = this is Success

    /**
     * 실패 여부 확인
     */
    val isError: Boolean
        get() = this is Error

    /**
     * 성공 시 데이터 가져오기
     */
    fun getOrNull(): T? = when (this) {
        is Success -> data
        is Error -> null
    }

    /**
     * 실패 시 에러 가져오기
     */
    fun errorOrNull(): ApiError? = when (this) {
        is Success -> null
        is Error -> error
    }
}