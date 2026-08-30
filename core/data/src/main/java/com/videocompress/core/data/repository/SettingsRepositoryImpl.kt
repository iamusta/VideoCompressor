package com.videocompress.core.data.repository

import com.videocompress.core.data.settings.SettingsDataStore
import com.videocompress.core.domain.model.AppSettings
import com.videocompress.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore,
) : SettingsRepository {
    override fun observe(): Flow<AppSettings> = dataStore.settings
    override suspend fun current(): AppSettings = dataStore.current()
    override suspend fun update(transform: (AppSettings) -> AppSettings) = dataStore.update(transform)
}
