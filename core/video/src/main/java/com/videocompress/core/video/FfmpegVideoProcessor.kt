package com.videocompress.core.video

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.ReturnCode
import com.arthenica.ffmpegkit.StatisticsCallback
import com.videocompress.core.domain.model.ProcessOptions
import com.videocompress.core.domain.model.ProcessProgress
import com.videocompress.core.domain.model.VideoMedia
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import kotlin.coroutines.resume
import kotlin.math.max

class FfmpegVideoProcessor {

    @Volatile
    private var currentSessionId: Long? = null

    suspend fun execute(
        inputs: List<File>,
        media: List<VideoMedia>,
        options: ProcessOptions,
        outputDir: File,
        onProgress: (Float) -> Unit,
    ): Result<PreparedCommand> {
        val command = FfmpegCommandBuilder.build(inputs, media, options, outputDir)
        val durationMs = media.sumOf { it.durationMs }.coerceAtLeast(1L)
        return suspendCancellableCoroutine { cont ->
            val statsCallback = StatisticsCallback { stats ->
                val fraction = (stats.time.toFloat() / durationMs).coerceIn(0f, 0.99f)
                onProgress(fraction)
            }
            FFmpegKitConfig.enableStatisticsCallback(statsCallback)
            val session = FFmpegKit.executeAsync(command.arguments) { completed ->
                currentSessionId = null
                FFmpegKitConfig.enableStatisticsCallback(null)
                if (!cont.isActive) return@executeAsync
                if (ReturnCode.isSuccess(completed.returnCode) && command.outputFile.exists()) {
                    onProgress(1f)
                    cont.resume(Result.success(command))
                } else {
                    val message = completed.failStackTrace
                        ?: completed.output
                        ?: "FFmpeg failed"
                    cont.resume(Result.failure(IllegalStateException(message.take(400))))
                }
            }
            currentSessionId = session.sessionId
            cont.invokeOnCancellation { cancel() }
        }
    }

    fun cancel() {
        currentSessionId?.let { FFmpegKit.cancel(it) }
        currentSessionId = null
    }
}

fun File.ensureDir(): File {
    if (!exists()) mkdirs()
    return this
}

fun estimatedDuration(media: List<VideoMedia>, options: ProcessOptions): Long {
    val first = media.firstOrNull() ?: return 1L
    return max(first.durationMs, 1L)
}
