package com.d104.pnt.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import com.d104.pnt.base.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * DataStore 의존성 제공 모듈
 */
@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
        name = Constants.PREF_NAME
    )

    @Provides
    @Singleton
    fun provideDataStore(
        @ApplicationContext context: Context
    ): DataStore<Preferences> {
        return context.dataStore
    }
}