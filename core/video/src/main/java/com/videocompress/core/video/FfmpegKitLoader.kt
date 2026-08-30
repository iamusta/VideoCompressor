package com.videocompress.core.video

import com.arthenica.ffmpegkit.FFmpegKitConfig
import com.arthenica.ffmpegkit.Level
import java.util.concurrent.atomic.AtomicBoolean

object FfmpegKitLoader {
    private val attempted = AtomicBoolean(false)
    @Volatile
    var lastError: String? = null
        private set

    fun preload() {
        if (!attempted.compareAndSet(false, true)) return
        try {
            FFmpegKitConfig.setLogLevel(Level.AV_LOG_ERROR)
            FFmpegKitConfig.getFFmpegVersion()
            lastError = null
        } catch (error: Throwable) {
            lastError = error.message ?: error.javaClass.simpleName
        }
    }
}
