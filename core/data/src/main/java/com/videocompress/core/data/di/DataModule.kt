package com.videocompress.core.data.di

import android.content.Context
import androidx.room.Room
import com.videocompress.core.data.local.HistoryDao
import com.videocompress.core.data.local.HistoryDatabase
import com.videocompress.core.data.repository.HistoryRepositoryImpl
import com.videocompress.core.data.repository.SettingsRepositoryImpl
import com.videocompress.core.data.repository.VideoRepositoryImpl
import com.videocompress.core.domain.repository.HistoryRepository
import com.videocompress.core.domain.repository.SettingsRepository
import com.videocompress.core.domain.repository.VideoRepository
import com.videocompress.core.domain.usecase.DeleteHistoryUseCase
import com.videocompress.core.domain.usecase.ObserveHistoryUseCase
import com.videocompress.core.domain.usecase.ObserveSettingsUseCase
import com.videocompress.core.domain.usecase.ProcessVideosUseCase
import com.videocompress.core.domain.usecase.ResolveVideoUseCase
import com.videocompress.core.domain.usecase.UpdateSettingsUseCase
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun videoRepository(impl: VideoRepositoryImpl): VideoRepository

    @Binds
    @Singleton
    abstract fun historyRepository(impl: HistoryRepositoryImpl): HistoryRepository

    @Binds
    @Singleton
    abstract fun settingsRepository(impl: SettingsRepositoryImpl): SettingsRepository
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun database(@ApplicationContext context: Context): HistoryDatabase =
        Room.databaseBuilder(context, HistoryDatabase::class.java, "video_compressor.db").build()

    @Provides
    fun historyDao(database: HistoryDatabase): HistoryDao = database.historyDao()

    @Provides
    fun resolveVideo(repository: VideoRepository) = ResolveVideoUseCase(repository)

    @Provides
    fun processVideos(
        videoRepository: VideoRepository,
        historyRepository: HistoryRepository,
        settingsRepository: SettingsRepository,
    ) = ProcessVideosUseCase(videoRepository, historyRepository, settingsRepository)

    @Provides
    fun observeHistory(repository: HistoryRepository) = ObserveHistoryUseCase(repository)

    @Provides
    fun deleteHistory(repository: HistoryRepository) = DeleteHistoryUseCase(repository)

    @Provides
    fun observeSettings(repository: SettingsRepository) = ObserveSettingsUseCase(repository)

    @Provides
    fun updateSettings(repository: SettingsRepository) = UpdateSettingsUseCase(repository)
}
