package com.d104.pnt.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.d104.pnt.base.Constants
import com.d104.pnt.data.remote.api.AuthApiService
import com.d104.pnt.data.remote.model.request.CheckDuplicateRequest
import com.d104.pnt.data.remote.model.request.FcmTokenRequest
import com.d104.pnt.data.remote.model.request.LoginRequest
import com.d104.pnt.data.remote.model.request.RefreshRequest
import com.d104.pnt.data.remote.model.request.SignupRequest
import com.d104.pnt.data.remote.model.request.SocialLoginRequest
import com.d104.pnt.data.remote.model.response.DuplicateCheckResponse
import com.d104.pnt.data.remote.model.response.LoginResponse
import com.d104.pnt.data.remote.model.response.SignupResponse
import com.d104.pnt.domain.model.common.BaseResult
import com.d104.pnt.util.AuthEventBus
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Provider
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class AuthRepositoryImpl @Inject constructor(
    private val apiService: AuthApiService,
    private val dataStore: DataStore<Preferences>,
    private val authAPiServiceProvider: Provider<AuthApiService>,
    private val authEventBus: AuthEventBus
) : AuthRepository, BaseRepository() {

    // DataStore Keys
    private val KEY_ACCESS_TOKEN = stringPreferencesKey(Constants.KEY_ACCESS_TOKEN)
    private val KEY_REFRESH_TOKEN = stringPreferencesKey(Constants.KEY_REFRESH_TOKEN)
    private val KEY_USER_ID = stringPreferencesKey(Constants.KEY_USER_ID) // userId
    private val KEY_MEMBER_ID = longPreferencesKey(Constants.KEY_MEMBER_ID) // memberId
    private val KEY_IS_LOGGED_IN = booleanPreferencesKey(Constants.KEY_IS_LOGGED_IN)
    private val KEY_LAST_REFRESH = longPreferencesKey(Constants.KEY_LAST_REFRESH)
    private val _EXPIER_TIME = 14400000L
    private val _5MINUTE = 300000L


    override suspend fun sendFcmToken(active: Boolean): BaseResult<Unit> {
        return safeApiCall {
            val token = suspendCoroutine<String?> { continuation ->
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
                    if (task.isSuccessful) continuation.resume(task.result)
                    else continuation.resume(null)
                }
            }

            if (token != null) {
                apiService.postFcmToken(FcmTokenRequest(value = token, active = active))
            } else {
                throw Exception("FCM 토큰을 가져올 수 없습니다.")
            }
        }
    }

    override suspend fun login(id: String, password: String): BaseResult<LoginResponse> {
        return safeApiCall(
            onSuccess = { loginResponse ->
                saveLoginData(
                    accessToken = loginResponse.accessToken,
                    refreshToken = loginResponse.refreshToken,
                    userId = loginResponse.member.id,
                    memberId = loginResponse.member.memberId
                )

                CoroutineScope(Dispatchers.IO).launch {
                    sendFcmToken(true)
                }
            }
        ) {
            apiService.login(LoginRequest(id, password))
        }
    }

    override suspend fun socialLogin(
        provider: String,
        token: String
    ): BaseResult<LoginResponse> {
        return safeApiCall(
            onSuccess = { response ->
                saveLoginData(
                    accessToken = response.accessToken,
                    refreshToken = response.refreshToken,
                    userId = response.member.id,
                    memberId = response.member.memberId
                )
            }
        ) {
            apiService.socialLogin(SocialLoginRequest(provider, token))
        }
    }


    override suspend fun checkDuplicate(id: String): BaseResult<DuplicateCheckResponse> {
        return safeApiCall {
            apiService.checkDuplicate(CheckDuplicateRequest(id))
        }
    }

    override suspend fun signup(
        id: String,
        password: String,
        passwordConfirm: String,
        nickname: String,
        email: String,
        birth: String,
        avatarUrl: String?
    ): BaseResult<SignupResponse> {
        return safeApiCall {
            apiService.signup(
                SignupRequest(
                    id,
                    password,
                    passwordConfirm,
                    nickname,
                    email,
                    birth,
                    avatarUrl
                )
            )
        }
    }

    override suspend fun logout(): BaseResult<String> {
        CoroutineScope(Dispatchers.IO).launch {
            sendFcmToken(false)
        }

        return safeApiCall(
            onSuccess = { clearAuthData() }
        ) {
            apiService.logout()

        }
    }


    // DataStore 읽기
    override fun isLoggedIn(): Flow<Boolean> {
        return dataStore.data.map { preferences ->
            preferences[KEY_IS_LOGGED_IN] ?: false
        }
    }

    override fun getUserId(): Flow<String> {
        return dataStore.data.map { it[KEY_USER_ID] ?: "" }
    }

    override fun getMemberId(): Flow<Long> {
        return dataStore.data.map { it[KEY_MEMBER_ID] ?: 0L }
    }

    override suspend fun getUserIdSync(): String {
        return dataStore.data.first()[KEY_USER_ID] ?: ""
    }

    override fun getAccessToken(): Flow<String> {
        return dataStore.data.transform { preferences ->
            val lastRefresh = preferences[KEY_LAST_REFRESH] ?: 0L
            val currentTime = System.currentTimeMillis()
            val expireTime = lastRefresh + _EXPIER_TIME
            if (currentTime + _5MINUTE > expireTime) {
                try {
                    val refreshCall = authAPiServiceProvider.get().refreshTokenCall(
                        RefreshRequest(
                            grantType = "Bearer",
                            accessToken = preferences[KEY_ACCESS_TOKEN] ?: "",
                            refreshToken = preferences[KEY_REFRESH_TOKEN] ?: "",
                            accessTokenExpiresIn = 0,
                            refreshTokenExpiresIn = 0
                        )
                    )
                    val refreshResponse = refreshCall.execute()
                    if (refreshResponse.isSuccessful && refreshResponse.body() != null) {
                        val newTokens = refreshResponse.body()!!
                        refreshTokens(newTokens.data!!.accessToken, newTokens.data.refreshToken)
                        emit(newTokens.data.accessToken)
                    }
                    else {
                        sessionExpired()
                    }
                } catch (e: Exception) {
                    sessionExpired()
                }
            } else {
                emit(preferences[KEY_ACCESS_TOKEN] ?: "")
            }
        }
    }

    private suspend fun sessionExpired(){
        dataStore.edit { preferences ->
            preferences.remove(stringPreferencesKey(Constants.KEY_ACCESS_TOKEN))
            preferences.remove(stringPreferencesKey(Constants.KEY_REFRESH_TOKEN))
            preferences.remove(stringPreferencesKey(Constants.KEY_USER_ID))
            preferences.remove(longPreferencesKey(Constants.KEY_MEMBER_ID))
            preferences[booleanPreferencesKey(Constants.KEY_IS_LOGGED_IN)] = false
            preferences[KEY_LAST_REFRESH] = 0L
        }
        authEventBus.emit(AuthEventBus.AuthEvent.TokenExpired)
    }

    override fun getRefreshToken(): Flow<String> {
        return dataStore.data.map { it[KEY_REFRESH_TOKEN] ?: "" }
    }

    override suspend fun saveLoginData(
        accessToken: String,
        refreshToken: String,
        userId: String,
        memberId: Long,
        lastRefresh: Long
    ) {
        dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = accessToken
            preferences[KEY_REFRESH_TOKEN] = refreshToken
            preferences[KEY_USER_ID] = userId
            preferences[KEY_MEMBER_ID] = memberId
            preferences[KEY_IS_LOGGED_IN] = true
            preferences[KEY_LAST_REFRESH] = lastRefresh
        }
    }

    override suspend fun refreshTokens(
        accessToken: String,
        refreshToken: String,
        lastRepository: Long
    ) {
        dataStore.edit { preferences ->
            preferences[KEY_ACCESS_TOKEN] = accessToken
            preferences[KEY_REFRESH_TOKEN] = refreshToken
            preferences[KEY_LAST_REFRESH] = lastRepository
        }
    }

    override suspend fun clearAuthData() {
        dataStore.edit { preferences ->
            preferences.remove(KEY_ACCESS_TOKEN)
            preferences.remove(KEY_REFRESH_TOKEN)
            preferences.remove(KEY_USER_ID)
            preferences.remove(KEY_MEMBER_ID)
            preferences[KEY_IS_LOGGED_IN] = false
        }
    }
}