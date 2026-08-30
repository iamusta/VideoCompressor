package com.videocompress.core.video

import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.ReturnCode
import com.arthenica.ffmpegkit.StatisticsCallback
import com.videocompress.core.domain.model.ProcessOptions
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
        val command = try {
            FfmpegCommandBuilder.build(inputs, media, options, outputDir)
        } catch (error: Throwable) {
            return Result.failure(IllegalStateException(friendlyMessage(error), error))
        }
        val durationMs = media.sumOf { it.durationMs }.coerceAtLeast(1L)
        return try {
            suspendCancellableCoroutine { cont ->
                val statsCallback = StatisticsCallback { stats ->
                    val fraction = (stats.time.toFloat() / durationMs).coerceIn(0f, 0.99f)
                    onProgress(fraction)
                }
                try {
                    FFmpegKitConfig.enableStatisticsCallback(statsCallback)
                } catch (error: Throwable) {
                    if (cont.isActive) {
                        cont.resume(Result.failure(IllegalStateException(friendlyMessage(error), error)))
                    }
                    return@suspendCancellableCoroutine
                }
                val session = FFmpegKit.executeWithArgumentsAsync(command.arguments) { completed ->
                    currentSessionId = null
                    FFmpegKitConfig.enableStatisticsCallback(null)
                    if (!cont.isActive) return@executeWithArgumentsAsync
                    if (ReturnCode.isSuccess(completed.returnCode) && command.outputFile.exists()) {
                        onProgress(1f)
                        cont.resume(Result.success(command))
                    } else {
                        val message = completed.failStackTrace
                            ?.lineSequence()
                            ?.firstOrNull { it.isNotBlank() }
                            ?: completed.output
                                ?.lineSequence()
                                ?.map { it.trim() }
                                ?.firstOrNull { it.isNotBlank() && !it.startsWith("ffmpeg version") }
                            ?: "Video processing failed"
                        cont.resume(Result.failure(IllegalStateException(message.take(400))))
                    }
                }
                currentSessionId = session.sessionId
                cont.invokeOnCancellation { cancel() }
            }
        } catch (error: Throwable) {
            Result.failure(IllegalStateException(friendlyMessage(error), error))
        }
    }

    fun cancel() {
        currentSessionId?.let { FFmpegKit.cancel(it) }
        currentSessionId = null
    }

    private fun friendlyMessage(error: Throwable): String {
        val raw = sequenceOf(error.message, error.cause?.message)
            .filterNotNull()
            .firstOrNull()
            .orEmpty()
        return when {
            raw.contains("ffmpegkit", ignoreCase = true) ||
                raw.contains("UnsatisfiedLinkError") ||
                error is UnsatisfiedLinkError ||
                error is NoClassDefFoundError ||
                error is ExceptionInInitializerError ->
                "FFmpeg native engine failed to load. Reinstall the app or use an arm64 device."
            raw.isBlank() -> error.javaClass.simpleName.ifBlank { "Video processing failed" }
            else -> raw.take(400)
        }
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
