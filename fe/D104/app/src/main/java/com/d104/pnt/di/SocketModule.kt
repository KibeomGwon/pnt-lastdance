package com.d104.pnt.di

import com.d104.pnt.util.socket.ChatSocketManager
import com.d104.pnt.util.socket.GameSocketManager
import com.d104.pnt.util.socket.RoomSocketManager
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Socket Manager 의존성 주입 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object SocketModule {

    /**
     * 게임 로비(대기방) 관련 소켓 통신
     */
    @Provides
    @Singleton
    fun provideRoomSocketManager(gson: Gson): RoomSocketManager {
        return RoomSocketManager(gson)
    }

    /**
     * 실제 게임 플레이 중 소켓 통신
     */
    @Provides
    @Singleton
    fun provideGameSocketManager(): GameSocketManager {
        return GameSocketManager()
    }

    /**
     * 채팅 관련 소켓 통신
     */
    @Provides
    @Singleton
    fun provideChatSocketManager(): ChatSocketManager {
        return ChatSocketManager()
    }
}