package com.d104.pnt.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 인증 관련 전역 이벤트 버스
 */
@Singleton
class AuthEventBus @Inject constructor() {

    sealed class AuthEvent {
        object TokenExpired : AuthEvent()
        object Unauthorized : AuthEvent()
    }

    private val _events = MutableSharedFlow<AuthEvent>()
    val events = _events.asSharedFlow()

    suspend fun emit(event: AuthEvent) {
        _events.emit(event)
    }
}