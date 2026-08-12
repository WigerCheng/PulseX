package io.wiger.pulsex.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import io.wiger.pulsex.data.local.pref.AppPreference
import io.wiger.pulsex.data.local.pref.AppPreferenceSerializer
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {

    @Provides
    @Singleton
    fun provideAppPreferenceDataStore(
        @ApplicationContext context: Context
    ): DataStore<AppPreference> = context.appPreferenceDataStore
}

private val Context.appPreferenceDataStore: DataStore<AppPreference> by dataStore(
    fileName = "app_preference.pb",
    serializer = AppPreferenceSerializer
)
