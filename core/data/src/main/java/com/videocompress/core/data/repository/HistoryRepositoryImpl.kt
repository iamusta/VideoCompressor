package com.videocompress.core.data.repository

import com.videocompress.core.common.VideoTool
import com.videocompress.core.data.local.HistoryDao
import com.videocompress.core.data.local.HistoryEntity
import com.videocompress.core.domain.model.HistoryItem
import com.videocompress.core.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HistoryRepositoryImpl @Inject constructor(
    private val dao: HistoryDao,
) : HistoryRepository {

    override fun observe(): Flow<List<HistoryItem>> = dao.observe().map { rows ->
        rows.map { it.toModel() }
    }

    override suspend fun add(item: HistoryItem) {
        dao.insert(
            HistoryEntity(
                tool = item.tool.name,
                outputUri = item.outputUri,
                displayName = item.displayName,
                originalSizeBytes = item.originalSizeBytes,
                resultSizeBytes = item.resultSizeBytes,
                createdAt = item.createdAt,
                mimeType = item.mimeType,
            ),
        )
    }

    override suspend fun delete(id: Long) = dao.delete(id)

    override suspend fun clear() = dao.clear()

    override suspend fun count(): Int = dao.count()
}

private fun HistoryEntity.toModel() = HistoryItem(
    id = id,
    tool = runCatching { VideoTool.valueOf(tool) }.getOrDefault(VideoTool.COMPRESS),
    outputUri = outputUri,
    displayName = displayName,
    originalSizeBytes = originalSizeBytes,
    resultSizeBytes = resultSizeBytes,
    createdAt = createdAt,
    mimeType = mimeType,
)
