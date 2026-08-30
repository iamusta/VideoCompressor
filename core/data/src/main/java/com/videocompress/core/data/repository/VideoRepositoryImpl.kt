package com.videocompress.core.data.repository

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.videocompress.core.common.VideoTool
import com.videocompress.core.data.media.MediaStoreWriter
import com.videocompress.core.data.media.UriFileCopier
import com.videocompress.core.data.settings.SettingsDataStore
import com.videocompress.core.domain.model.ProcessOptions
import com.videocompress.core.domain.model.ProcessProgress
import com.videocompress.core.domain.model.ProcessResult
import com.videocompress.core.domain.model.VideoMedia
import com.videocompress.core.domain.repository.VideoRepository
import com.videocompress.core.video.FfmpegVideoProcessor
import com.videocompress.core.video.ensureDir
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VideoRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context,
    private val copier: UriFileCopier,
    private val writer: MediaStoreWriter,
    private val settings: SettingsDataStore,
) : VideoRepository {

    private val processor = FfmpegVideoProcessor()

    override suspend fun resolve(uri: Uri): VideoMedia = withContext(Dispatchers.IO) {
        copier.resolve(uri)
    }

    override suspend fun process(
        inputs: List<VideoMedia>,
        options: ProcessOptions,
        onProgress: (ProcessProgress) -> Unit,
    ): Result<List<ProcessResult>> = withContext(Dispatchers.IO) {
        cleanupStaleWork()
        val workDir = File(context.cacheDir, "work_${System.currentTimeMillis()}").ensureDir()
        try {
            val files = inputs.mapIndexed { index, media ->
                copier.copyToCache(media.uri, workDir, index)
            }
            val batches = if (options.tool == VideoTool.MERGE) {
                listOf(files to inputs)
            } else {
                files.zip(inputs).map { (file, media) -> listOf(file) to listOf(media) }
            }
            val results = mutableListOf<ProcessResult>()
            batches.forEachIndexed { index, (batchFiles, batchMedia) ->
                val executed = processor.execute(
                    inputs = batchFiles,
                    media = batchMedia,
                    options = options,
                    outputDir = workDir,
                ) { fraction ->
                    val overall = (index + fraction) / batches.size
                    onProgress(ProcessProgress(overall, index + 1, batches.size))
                }
                val command = executed.getOrElse { return@withContext Result.failure(it) }
                val originalSize = batchMedia.sumOf { it.sizeBytes }
                val saveToGallery = settings.current().saveToGallery
                val uri = if (saveToGallery) {
                    writer.save(command.outputFile, command.displayName, command.mimeType)
                } else {
                    FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.fileprovider",
                        command.outputFile,
                    )
                }
                results += ProcessResult(
                    outputUri = uri,
                    outputPath = command.outputFile.absolutePath,
                    displayName = command.displayName,
                    sizeBytes = command.outputFile.length(),
                    originalSizeBytes = originalSize,
                    durationMs = batchMedia.sumOf { it.durationMs },
                    mimeType = command.mimeType,
                )
            }
            Result.success(results)
        } catch (error: Throwable) {
            runCatching { workDir.deleteRecursively() }
            Result.failure(error)
        }
    }

    override fun cancel() {
        processor.cancel()
    }

    private fun cleanupStaleWork() {
        val cutoff = System.currentTimeMillis() - 3_600_000L
        context.cacheDir.listFiles()
            ?.filter { it.isDirectory && it.name.startsWith("work_") && it.lastModified() < cutoff }
            ?.forEach { runCatching { it.deleteRecursively() } }
    }
}
