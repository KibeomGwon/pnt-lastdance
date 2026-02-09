package com.d104.pnt.di

import com.d104.pnt.base.AuthTokenInterceptor
import com.d104.pnt.base.Constants
import com.d104.pnt.data.remote.api.AuthApiService
import com.d104.pnt.data.remote.api.ProfileApiService
import com.d104.pnt.data.remote.api.ChatApiService
import com.d104.pnt.data.remote.api.GameApiService
import com.d104.pnt.data.remote.api.GameRoomApiService
import com.d104.pnt.data.remote.api.ImageApiService
import com.d104.pnt.data.remote.api.NaverApiService
import com.d104.pnt.data.remote.api.ReportApiService
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authTokenInterceptor: AuthTokenInterceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(Constants.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(Constants.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(Constants.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authTokenInterceptor)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient,
        gson: Gson
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(Constants.SPRING_SERVER_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideProfileApiService(retrofit: Retrofit): ProfileApiService {
        return retrofit.create(ProfileApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideChatApiService(retrofit: Retrofit): ChatApiService {
        return retrofit.create(ChatApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideNaverApiService(): NaverApiService {
        return Retrofit.Builder()
            // 네이버 클라우드 API
            .baseUrl("https://maps.apigw.ntruss.com/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NaverApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGameRoomApiService(retrofit: Retrofit): GameRoomApiService {
        return retrofit.create(GameRoomApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideImageApiService(retrofit: Retrofit): ImageApiService {
        return retrofit.create(ImageApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideReportApiService(retrofit: Retrofit): ReportApiService {
        return retrofit.create(ReportApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideGameApiService(retrofit: Retrofit): GameApiService{
        return retrofit.create(GameApiService::class.java)
    }

}