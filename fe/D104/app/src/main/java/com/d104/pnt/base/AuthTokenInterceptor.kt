package com.d104.pnt.base

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.d104.pnt.data.remote.api.AuthApiService
import com.d104.pnt.data.remote.model.request.RefreshRequest
import com.d104.pnt.data.repository.AuthRepository
import com.d104.pnt.util.AuthEventBus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Provider

class AuthTokenInterceptor @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val authEventBus: AuthEventBus,
    private val authAPiServiceProvider: Provider<AuthApiService>,
    private val authRepositoryProvider: Provider<AuthRepository>
) : Interceptor {

    companion object {
        private val PUBLIC_URLS = listOf(
            "/auth/login",
            "/auth/social-login",
            "/auth/signup",
            "/auth/duplicate",
            "/auth/reissue"
        )
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val path = request.url.encodedPath
        val noAuthHeader = request.header("X-No-Auth")

        if (PUBLIC_URLS.any { path.contains(it) }) {
            return chain.proceed(request)
        }

        if (noAuthHeader != null) {
            val newRequest = request.newBuilder()
                .removeHeader("X-No-Auth")
                .build()
            return chain.proceed(newRequest)
        }

        val token = runBlocking {
            dataStore.data.first()[stringPreferencesKey(Constants.KEY_ACCESS_TOKEN)]
        }

        val newRequest = if (!token.isNullOrEmpty()) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            request
        }

        val response = chain.proceed(newRequest)

        if (response.code == 401) {
            if (path.contains("/auth/reissue")) {
                runBlocking {
                    clearAuthData()
                    authEventBus.emit(AuthEventBus.AuthEvent.TokenExpired)
                }
                return response
            }
            response.close()

            synchronized(this) {
                val currentToken = runBlocking { dataStore.data.first()[stringPreferencesKey(Constants.KEY_ACCESS_TOKEN)] }

                if (token != currentToken && !currentToken.isNullOrEmpty()) {
                    val newerRequest = newRequest.newBuilder()
                        .header("Authorization", "Bearer $currentToken")
                        .build()
                    return chain.proceed(newerRequest)
                }

                val refreshToken = runBlocking { dataStore.data.first()[stringPreferencesKey(Constants.KEY_REFRESH_TOKEN)] }
                if (refreshToken.isNullOrEmpty()) {
                    runBlocking {
                        clearAuthData()
                        authEventBus.emit(AuthEventBus.AuthEvent.TokenExpired)
                    }
                }

                try {
                    val refreshCall = authAPiServiceProvider.get().refreshTokenCall(
                        RefreshRequest(
                            grantType = "Bearer",
                            accessToken = currentToken ?: "",
                            refreshToken = refreshToken ?: "",
                            accessTokenExpiresIn = 0,
                            refreshTokenExpiresIn = 0
                        )
                    )
                    val refreshResponse = refreshCall.execute()

                    if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                        val newTokens = refreshResponse.body()!!

                        runBlocking {
                            authRepositoryProvider.get().refreshTokens(newTokens.data!!.accessToken, newTokens.data.refreshToken)
                        }

                        val newerRequest = newRequest.newBuilder()
                            .header("Authorization", "Bearer ${newTokens.data!!.accessToken}")
                            .build()

                        return chain.proceed(newerRequest)
                    } else {
                        runBlocking {
                            clearAuthData()
                            authEventBus.emit(AuthEventBus.AuthEvent.TokenExpired)
                        }
                        return chain.proceed(newRequest)
                    }
                } catch (e: Exception) {
                    runBlocking {
                        clearAuthData()
                        authEventBus.emit(AuthEventBus.AuthEvent.TokenExpired)
                    }
                    return chain.proceed(newRequest)
                }
            }
        }

        return response
    }

    /**
     * 인증 데이터 삭제
     */
    private suspend fun clearAuthData() {
        dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey(Constants.KEY_ACCESS_TOKEN))
            preferences.remove(stringPreferencesKey(Constants.KEY_REFRESH_TOKEN))
            preferences.remove(stringPreferencesKey(Constants.KEY_USER_ID))
            preferences.remove(longPreferencesKey(Constants.KEY_MEMBER_ID))
            preferences[booleanPreferencesKey(Constants.KEY_IS_LOGGED_IN)] = false
        }
    }
}