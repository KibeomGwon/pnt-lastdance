package com.d104.pnt.di

import com.d104.pnt.data.repository.AuthRepository
import com.d104.pnt.data.repository.AuthRepositoryImpl
import com.d104.pnt.data.repository.ChatRepository
import com.d104.pnt.data.repository.ChatRepositoryImpl
import com.d104.pnt.data.repository.GameRepository
import com.d104.pnt.data.repository.GameRepositoryImpl
import com.d104.pnt.data.repository.GameRoomRepository
import com.d104.pnt.data.repository.GameRoomRepositoryImpl
import com.d104.pnt.data.repository.GameSessionRepository
import com.d104.pnt.data.repository.GameSessionRepositoryImpl
import com.d104.pnt.data.repository.ImageRepository
import com.d104.pnt.data.repository.ImageRepositoryImpl
import com.d104.pnt.data.repository.LocationRepository
import com.d104.pnt.data.repository.LocationRepositoryImpl
import com.d104.pnt.data.repository.ProfileRepository
import com.d104.pnt.data.repository.ProfileRepositoryImpl
import com.d104.pnt.data.repository.ReportRepository
import com.d104.pnt.data.repository.ReportRepositoryImpl
import com.d104.pnt.data.repository.WalkieRepository
import com.d104.pnt.data.repository.WalkieRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        impl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindWalkieRepository(
        impl: WalkieRepositoryImpl
    ): WalkieRepository

    @Binds
    @Singleton
    abstract fun bindLocationRepository(
        impl: LocationRepositoryImpl
    ): LocationRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindChatRepository(
        impl: ChatRepositoryImpl
    ): ChatRepository

    @Binds
    @Singleton
    abstract fun bindGameRoomRepository(
        impl: GameRoomRepositoryImpl
    ): GameRoomRepository

    @Binds
    @Singleton
    abstract fun bindImageRepository(
        impl: ImageRepositoryImpl
    ): ImageRepository

    @Binds
    @Singleton
    abstract fun bindReportRepository(
        impl: ReportRepositoryImpl
    ): ReportRepository

    @Binds
    @Singleton
    abstract fun bindGameRepository(
        impl: GameRepositoryImpl
    ): GameRepository

    @Binds
    @Singleton
    abstract fun bindGameSessionRepository(
        impl: GameSessionRepositoryImpl
    ): GameSessionRepository
}
