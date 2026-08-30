package com.videocompress.app

import com.videocompress.core.video.FfmpegKitLoader
import dagger.hilt.android.HiltAndroidApp
import android.app.Application

@HiltAndroidApp
class VideoCompressorApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        Thread { FfmpegKitLoader.preload() }.apply {
            name = "ffmpeg-preload"
            isDaemon = true
            start()
        }
    }
}
