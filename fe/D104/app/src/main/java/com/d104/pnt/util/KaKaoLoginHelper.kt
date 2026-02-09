package com.d104.pnt.util

import android.content.Context
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.sdk.common.model.ClientError
import com.kakao.sdk.common.model.ClientErrorCause
import com.kakao.sdk.user.UserApiClient
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

/**
 * 카카오 로그인 헬퍼
 */
object KakaoLoginHelper {

    /**
     * 카카오 로그인
     */
    suspend fun login(context: Context): String? = suspendCancellableCoroutine { continuation ->
        val callback: (OAuthToken?, Throwable?) -> Unit = { token, error ->
            if (error != null) {
                continuation.resume(null)
            } else if (token != null) {
                val tokenToSend = token.accessToken

                continuation.resume(tokenToSend)
            } else {
                continuation.resume(null)
            }
        }

        // 카카오톡 설치 확인
        if (UserApiClient.instance.isKakaoTalkLoginAvailable(context)) {
            UserApiClient.instance.loginWithKakaoTalk(context) { token, error ->
                if (error != null) {
                    if (error is ClientError && error.reason == ClientErrorCause.Cancelled) {
                        continuation.resume(null)
                        return@loginWithKakaoTalk
                    }

                    UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
                } else if (token != null) {
                    val tokenToSend = token.accessToken
                    continuation.resume(tokenToSend)
                } else {
                    continuation.resume(null)
                }
            }
        } else {
            UserApiClient.instance.loginWithKakaoAccount(context, callback = callback)
        }
    }

    /**
     * 카카오 로그아웃
     */
    suspend fun logout(): Boolean = suspendCancellableCoroutine { continuation ->
        UserApiClient.instance.logout { error ->
            if (error != null) {
                continuation.resume(false)
            } else {
                continuation.resume(true)
            }
        }
    }

    /**
     * 카카오 연결 해제
     */
    suspend fun unlink(): Boolean = suspendCancellableCoroutine { continuation ->
        UserApiClient.instance.unlink { error ->
            if (error != null) {
                continuation.resume(false)
            } else {
                continuation.resume(true)
            }
        }
    }

    /**
     * 현재 로그인된 사용자 정보 가져오기
     */
    suspend fun getUserInfo(): Map<String, String?>? = suspendCancellableCoroutine { continuation ->
        UserApiClient.instance.me { user, error ->
            if (error != null) {
                continuation.resume(null)
            } else if (user != null) {
                val userInfo = mapOf(
                    "id" to user.id.toString(),
                    "nickname" to user.kakaoAccount?.profile?.nickname,
                    "email" to user.kakaoAccount?.email,
                    "profileImage" to user.kakaoAccount?.profile?.profileImageUrl
                )
                continuation.resume(userInfo)
            } else {
                continuation.resume(null)
            }
        }
    }
}