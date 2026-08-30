package com.videocompress.core.video

import android.os.Handler
import android.os.Looper
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.ReturnCode
import com.arthenica.ffmpegkit.StatisticsCallback
import com.videocompress.core.domain.model.ProcessOptions
import com.videocompress.core.domain.model.VideoMedia
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.coroutines.resume

class FfmpegVideoProcessor {

    @Volatile
    private var currentSessionId: Long? = null
    private val mainHandler = Handler(Looper.getMainLooper())

    suspend fun execute(
        inputs: List<File>,
        media: List<VideoMedia>,
        options: ProcessOptions,
        outputDir: File,
        onProgress: (Float) -> Unit,
    ): Result<PreparedCommand> {
        val command = try {
            FfmpegKitLoader.preload()
            FfmpegKitLoader.lastError?.let { loadError ->
                return Result.failure(IllegalStateException(friendlyMessage(IllegalStateException(loadError))))
            }
            FfmpegCommandBuilder.build(inputs, media, options, outputDir)
        } catch (error: Throwable) {
            return Result.failure(IllegalStateException(friendlyMessage(error), error))
        }
        val durationMs = media.sumOf { it.durationMs }.coerceAtLeast(1L)
        return suspendCancellableCoroutine { cont ->
            val finished = AtomicBoolean(false)
            fun complete(result: Result<PreparedCommand>) {
                if (!finished.compareAndSet(false, true)) return
                currentSessionId = null
                runCatching { FFmpegKitConfig.enableStatisticsCallback(emptyStats) }
                if (cont.isActive) cont.resume(result)
            }

            val statsCallback = StatisticsCallback { stats ->
                val fraction = (stats.time.toFloat() / durationMs).coerceIn(0f, 0.99f)
                mainHandler.post { runCatching { onProgress(fraction) } }
            }

            try {
                FFmpegKitConfig.enableStatisticsCallback(statsCallback)
                val session = FFmpegKit.executeWithArgumentsAsync(command.arguments.toTypedArray()) { completed ->
                    val ok = ReturnCode.isSuccess(completed.returnCode) &&
                        command.outputFile.exists() &&
                        command.outputFile.length() > 0L
                    if (ok) {
                        mainHandler.post { runCatching { onProgress(1f) } }
                        complete(Result.success(command))
                    } else {
                        val logs = runCatching { completed.output }.getOrNull().orEmpty()
                        val fail = runCatching { completed.failStackTrace }.getOrNull().orEmpty()
                        val raw = fail.ifBlank { logs }
                        val line = raw.lineSequence()
                            .map { it.trim() }
                            .firstOrNull { it.isNotBlank() && !it.startsWith("ffmpeg version") }
                            ?: "Video processing failed"
                        complete(Result.failure(IllegalStateException(friendlyFfmpegLine(line))))
                    }
                }
                currentSessionId = session.sessionId
            } catch (error: Throwable) {
                complete(Result.failure(IllegalStateException(friendlyMessage(error), error)))
            }

            cont.invokeOnCancellation { cancel() }
        }
    }

    fun cancel() {
        currentSessionId?.let { runCatching { FFmpegKit.cancel(it) } }
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
                "FFmpeg native engine failed to load. Reinstall the app on an arm64 device."
            raw.isBlank() -> "Video processing failed. Try another video."
            else -> raw.take(400)
        }
    }

    private fun friendlyFfmpegLine(line: String): String {
        val lower = line.lowercase()
        return when {
            "does not contain any stream" in lower || "matches no streams" in lower ->
                "This file has no usable video or audio track."
            "no audio" in lower || "output with label 'a'" in lower ->
                "This video has no audio track."
            "invalid argument" in lower || "error opening" in lower ->
                "Could not process this file. Try another video."
            else -> line.take(400)
        }
    }

    companion object {
        private val emptyStats = StatisticsCallback { }
    }
}

fun File.ensureDir(): File {
    if (!exists()) mkdirs()
    return this
}
