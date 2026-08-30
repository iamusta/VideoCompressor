package com.videocompress.core.common

enum class VideoTool {
    COMPRESS,
    CONVERT,
    EXTRACT_AUDIO,
    VIDEO_TO_GIF,
    GIF_TO_VIDEO,
    CROP,
    TRIM,
    ROTATE,
    SPEED,
    VOLUME,
    REVERSE,
    LOOP,
    MERGE,
    SOCIAL_RESIZE,
}

enum class ThemeMode { SYSTEM, LIGHT, DARK }

enum class CompressQuality { LOW, BALANCED, HIGH, CUSTOM }

enum class VideoCodec { H264, H265 }

enum class OutputResolution {
    ORIGINAL,
    P1080,
    P720,
    P480,
    P360,
}

enum class OutputFormat { MP4, MOV, MKV, WEBM, AVI, THREE_GP }

enum class AudioFormat { MP3, M4A, AAC, WAV }

enum class CropAspect { FREE, SQUARE, RATIO_16_9, RATIO_9_16, RATIO_4_3 }

enum class RotateAction { CW_90, CCW_90, ROTATE_180, FLIP_HORIZONTAL, FLIP_VERTICAL }

enum class SocialPreset {
    INSTAGRAM_REEL,
    INSTAGRAM_POST,
    INSTAGRAM_STORY,
    YOUTUBE,
    TIKTOK,
    WHATSAPP,
    TWITTER,
    FACEBOOK,
}

enum class ContentScale(val multiplier: Float) {
    SMALL(0.9f),
    NORMAL(1f),
    LARGE(1.12f),
}
