package com.videocompress.core.ui.util

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AspectRatio
import androidx.compose.material.icons.outlined.Audiotrack
import androidx.compose.material.icons.outlined.CallMerge
import androidx.compose.material.icons.outlined.ContentCut
import androidx.compose.material.icons.outlined.Crop
import androidx.compose.material.icons.outlined.GifBox
import androidx.compose.material.icons.outlined.Loop
import androidx.compose.material.icons.outlined.MovieFilter
import androidx.compose.material.icons.outlined.Repeat
import androidx.compose.material.icons.outlined.Rotate90DegreesCcw
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material.icons.outlined.Transform
import androidx.compose.material.icons.outlined.VolumeUp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.videocompress.core.common.VideoTool
import com.videocompress.core.resources.R

data class ToolVisual(
    val icon: ImageVector,
    val tint: Color,
    val background: Color,
)

fun VideoTool.visual(): ToolVisual = when (this) {
    VideoTool.COMPRESS -> ToolVisual(Icons.Outlined.MovieFilter, Color(0xFF0F766E), Color(0xFFCCFBF1))
    VideoTool.CONVERT -> ToolVisual(Icons.Outlined.SwapHoriz, Color(0xFF0369A1), Color(0xFFE0F2FE))
    VideoTool.EXTRACT_AUDIO -> ToolVisual(Icons.Outlined.Audiotrack, Color(0xFF7C3AED), Color(0xFFEDE9FE))
    VideoTool.VIDEO_TO_GIF -> ToolVisual(Icons.Outlined.GifBox, Color(0xFFC2410C), Color(0xFFFFEDD5))
    VideoTool.GIF_TO_VIDEO -> ToolVisual(Icons.Outlined.Transform, Color(0xFFB45309), Color(0xFFFEF3C7))
    VideoTool.CROP -> ToolVisual(Icons.Outlined.Crop, Color(0xFF047857), Color(0xFFD1FAE5))
    VideoTool.TRIM -> ToolVisual(Icons.Outlined.ContentCut, Color(0xFFBE123C), Color(0xFFFFE4E6))
    VideoTool.ROTATE -> ToolVisual(Icons.Outlined.Rotate90DegreesCcw, Color(0xFF4338CA), Color(0xFFE0E7FF))
    VideoTool.SPEED -> ToolVisual(Icons.Outlined.Speed, Color(0xFFB45309), Color(0xFFFEF3C7))
    VideoTool.VOLUME -> ToolVisual(Icons.Outlined.VolumeUp, Color(0xFF0E7490), Color(0xFFCFFAFE))
    VideoTool.REVERSE -> ToolVisual(Icons.Outlined.Repeat, Color(0xFF9D174D), Color(0xFFFCE7F3))
    VideoTool.LOOP -> ToolVisual(Icons.Outlined.Loop, Color(0xFF0F766E), Color(0xFFCCFBF1))
    VideoTool.MERGE -> ToolVisual(Icons.Outlined.CallMerge, Color(0xFF1D4ED8), Color(0xFFDBEAFE))
    VideoTool.SOCIAL_RESIZE -> ToolVisual(Icons.Outlined.AspectRatio, Color(0xFFDB2777), Color(0xFFFCE7F3))
}

fun VideoTool.titleRes(): Int = when (this) {
    VideoTool.COMPRESS -> R.string.feature_compress
    VideoTool.CONVERT -> R.string.feature_convert
    VideoTool.EXTRACT_AUDIO -> R.string.feature_extract
    VideoTool.VIDEO_TO_GIF -> R.string.feature_video_gif
    VideoTool.GIF_TO_VIDEO -> R.string.feature_gif_video
    VideoTool.CROP -> R.string.feature_crop
    VideoTool.TRIM -> R.string.feature_trim
    VideoTool.ROTATE -> R.string.feature_rotate
    VideoTool.SPEED -> R.string.feature_speed
    VideoTool.VOLUME -> R.string.feature_volume
    VideoTool.REVERSE -> R.string.feature_reverse
    VideoTool.LOOP -> R.string.feature_loop
    VideoTool.MERGE -> R.string.feature_merge
    VideoTool.SOCIAL_RESIZE -> R.string.feature_social
}

fun VideoTool.subtitleRes(): Int = when (this) {
    VideoTool.COMPRESS -> R.string.feature_compress_desc
    VideoTool.CONVERT -> R.string.feature_convert_desc
    VideoTool.EXTRACT_AUDIO -> R.string.feature_extract_desc
    VideoTool.VIDEO_TO_GIF -> R.string.feature_video_gif_desc
    VideoTool.GIF_TO_VIDEO -> R.string.feature_gif_video_desc
    VideoTool.CROP -> R.string.feature_crop_desc
    VideoTool.TRIM -> R.string.feature_trim_desc
    VideoTool.ROTATE -> R.string.feature_rotate_desc
    VideoTool.SPEED -> R.string.feature_speed_desc
    VideoTool.VOLUME -> R.string.feature_volume_desc
    VideoTool.REVERSE -> R.string.feature_reverse_desc
    VideoTool.LOOP -> R.string.feature_loop_desc
    VideoTool.MERGE -> R.string.feature_merge_desc
    VideoTool.SOCIAL_RESIZE -> R.string.feature_social_desc
}

fun VideoTool.allowsMultiple(): Boolean = this == VideoTool.MERGE || this == VideoTool.COMPRESS || this == VideoTool.CONVERT

fun VideoTool.prefersGif(): Boolean = this == VideoTool.GIF_TO_VIDEO
