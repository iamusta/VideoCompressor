package com.videocompress.core.domain.usecase

import android.net.Uri
import com.videocompress.core.domain.model.HistoryItem
import com.videocompress.core.domain.model.ProcessOptions
import com.videocompress.core.domain.model.ProcessProgress
import com.videocompress.core.domain.model.ProcessResult
import com.videocompress.core.domain.model.VideoMedia
import com.videocompress.core.domain.repository.HistoryRepository
import com.videocompress.core.domain.repository.SettingsRepository
import com.videocompress.core.domain.repository.VideoRepository
import javax.inject.Inject

class ResolveVideoUseCase @Inject constructor(
    private val repository: VideoRepository,
) {
    suspend operator fun invoke(uri: Uri): VideoMedia = repository.resolve(uri)
}

class ProcessVideosUseCase @Inject constructor(
    private val videoRepository: VideoRepository,
    private val historyRepository: HistoryRepository,
    private val settingsRepository: SettingsRepository,
) {
    suspend operator fun invoke(
        inputs: List<VideoMedia>,
        options: ProcessOptions,
        onProgress: (ProcessProgress) -> Unit,
    ): Result<List<ProcessResult>> {
        val result = videoRepository.process(inputs, options, onProgress)
        result.getOrNull()?.forEach { item ->
            runCatching {
                historyRepository.add(
                    HistoryItem(
                        id = 0,
                        tool = options.tool,
                        outputUri = item.outputUri.toString(),
                        displayName = item.displayName,
                        originalSizeBytes = item.originalSizeBytes,
                        resultSizeBytes = item.sizeBytes,
                        createdAt = System.currentTimeMillis(),
                        mimeType = item.mimeType,
                    ),
                )
            }
        }
        return result
    }

    fun cancel() = videoRepository.cancel()
}

class ObserveHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository,
) {
    operator fun invoke() = repository.observe()
}

class DeleteHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository,
) {
    suspend operator fun invoke(id: Long) = repository.delete(id)
    suspend fun clear() = repository.clear()
}

class ObserveSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {
    operator fun invoke() = repository.observe()
}

class UpdateSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository,
) {
    suspend operator fun invoke(transform: (com.videocompress.core.domain.model.AppSettings) -> com.videocompress.core.domain.model.AppSettings) {
        repository.update(transform)
    }
}
