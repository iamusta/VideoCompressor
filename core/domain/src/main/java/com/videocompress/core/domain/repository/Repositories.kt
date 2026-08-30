package com.videocompress.core.domain.repository

import android.net.Uri
import com.videocompress.core.domain.model.AppSettings
import com.videocompress.core.domain.model.HistoryItem
import com.videocompress.core.domain.model.ProcessOptions
import com.videocompress.core.domain.model.ProcessProgress
import com.videocompress.core.domain.model.ProcessResult
import com.videocompress.core.domain.model.VideoMedia
import kotlinx.coroutines.flow.Flow

interface VideoRepository {
    suspend fun resolve(uri: Uri): VideoMedia
    suspend fun process(
        inputs: List<VideoMedia>,
        options: ProcessOptions,
        onProgress: (ProcessProgress) -> Unit,
    ): Result<List<ProcessResult>>
    fun cancel()
}

interface HistoryRepository {
    fun observe(): Flow<List<HistoryItem>>
    suspend fun add(item: HistoryItem)
    suspend fun delete(id: Long)
    suspend fun clear()
    suspend fun count(): Int
}

interface SettingsRepository {
    fun observe(): Flow<AppSettings>
    suspend fun current(): AppSettings
    suspend fun update(transform: (AppSettings) -> AppSettings)
}
