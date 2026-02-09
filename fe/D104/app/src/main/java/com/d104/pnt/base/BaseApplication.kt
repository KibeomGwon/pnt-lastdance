package com.d104.pnt.base

import android.app.Application
import android.content.Context
import com.d104.pnt.BuildConfig
import com.d104.pnt.util.AuthEventBus
import com.kakao.sdk.common.KakaoSdk
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject

@HiltAndroidApp
class BaseApplication : Application() {

    @Inject
    lateinit var authEventBus: AuthEventBus

    companion object {
        private var instance: BaseApplication? = null

        fun getContext(): Context {
            return instance?.applicationContext
                ?: throw IllegalStateException("Application not initialized")
        }
    }


    override fun onCreate() {
        super.onCreate()
        instance = this

        KakaoSdk.init(this, BuildConfig.KAKAO_NATIVE_APP_KEY)
    }
}