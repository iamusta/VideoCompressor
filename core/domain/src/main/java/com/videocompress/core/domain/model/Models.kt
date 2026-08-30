package com.videocompress.core.domain.model

import android.net.Uri
import com.videocompress.core.common.AudioFormat
import com.videocompress.core.common.CompressQuality
import com.videocompress.core.common.ContentScale
import com.videocompress.core.common.CropAspect
import com.videocompress.core.common.OutputFormat
import com.videocompress.core.common.OutputResolution
import com.videocompress.core.common.RotateAction
import com.videocompress.core.common.SocialPreset
import com.videocompress.core.common.ThemeMode
import com.videocompress.core.common.VideoCodec
import com.videocompress.core.common.VideoTool

data class VideoMedia(
    val uri: Uri,
    val displayName: String,
    val sizeBytes: Long,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val mimeType: String,
    val hasAudio: Boolean = true,
)

data class ProcessOptions(
    val tool: VideoTool,
    val quality: CompressQuality = CompressQuality.BALANCED,
    val targetSizeMb: Float? = null,
    val codec: VideoCodec = VideoCodec.H264,
    val resolution: OutputResolution = OutputResolution.ORIGINAL,
    val outputFormat: OutputFormat = OutputFormat.MP4,
    val audioFormat: AudioFormat = AudioFormat.MP3,
    val gifFps: Int = 10,
    val gifWidth: Int = 480,
    val cropAspect: CropAspect = CropAspect.FREE,
    val cropLeft: Float = 0f,
    val cropTop: Float = 0f,
    val cropRight: Float = 0f,
    val cropBottom: Float = 0f,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long? = null,
    val rotateAction: RotateAction = RotateAction.CW_90,
    val speed: Float = 1f,
    val volumePercent: Int = 100,
    val mute: Boolean = false,
    val removeAudio: Boolean = false,
    val loopCount: Int = 2,
    val socialPreset: SocialPreset = SocialPreset.INSTAGRAM_REEL,
)

data class ProcessProgress(
    val fraction: Float,
    val currentIndex: Int,
    val total: Int,
)

data class ProcessResult(
    val outputUri: Uri,
    val outputPath: String,
    val displayName: String,
    val sizeBytes: Long,
    val originalSizeBytes: Long,
    val durationMs: Long,
    val mimeType: String,
)

data class HistoryItem(
    val id: Long,
    val tool: VideoTool,
    val outputUri: String,
    val displayName: String,
    val originalSizeBytes: Long,
    val resultSizeBytes: Long,
    val createdAt: Long,
    val mimeType: String,
)

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val languageCode: String = "system",
    val saveToGallery: Boolean = true,
    val hapticEnabled: Boolean = true,
    val contentScale: ContentScale = ContentScale.NORMAL,
    val defaultQuality: CompressQuality = CompressQuality.BALANCED,
    val defaultCodec: VideoCodec = VideoCodec.H264,
)
