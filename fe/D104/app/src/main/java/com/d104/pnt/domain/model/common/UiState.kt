package com.d104.pnt.domain.model.common

sealed class UiState<out T> {
    /**
     * 초기 상태
     */
    data object Idle : UiState<Nothing>()

    /**
     * 로딩 중
     */
    data object Loading : UiState<Nothing>()

    /**
     * 성공
     * @param data 성공 시 받은 데이터
     */
    data class Success<T>(val data: T) : UiState<T>()

    /**
     * 에러
     * @param message 에러 메시지
     */
    data class Error(val message: String) : UiState<Nothing>()
}