package com.videocompress.feature.editor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.videocompress.core.common.AudioFormat
import com.videocompress.core.common.CompressQuality
import com.videocompress.core.common.CropAspect
import com.videocompress.core.common.OutputFormat
import com.videocompress.core.common.OutputResolution
import com.videocompress.core.common.RotateAction
import com.videocompress.core.common.SocialPreset
import com.videocompress.core.common.VideoCodec
import com.videocompress.core.common.VideoTool
import com.videocompress.core.common.toDurationLabel
import com.videocompress.core.domain.model.ProcessOptions
import com.videocompress.core.resources.R
import com.videocompress.core.ui.components.ChoiceChipRow
import com.videocompress.core.ui.components.SectionTitle

@Composable
fun OptionsPanel(
    state: EditorUiState,
    onOptions: ((ProcessOptions) -> ProcessOptions) -> Unit,
) {
    val options = state.options
    Column(modifier = Modifier.padding(top = 8.dp)) {
        when (state.tool) {
            VideoTool.COMPRESS -> CompressOptions(options, onOptions)
            VideoTool.CONVERT -> ConvertOptions(options, onOptions)
            VideoTool.EXTRACT_AUDIO -> AudioOptions(options, onOptions)
            VideoTool.VIDEO_TO_GIF -> GifOptions(state, options, onOptions)
            VideoTool.CROP -> CropOptions(options, onOptions)
            VideoTool.TRIM -> TrimOptions(state, options, onOptions)
            VideoTool.ROTATE -> RotateOptions(options, onOptions)
            VideoTool.SPEED -> SpeedOptions(options, onOptions)
            VideoTool.VOLUME -> VolumeOptions(options, onOptions)
            VideoTool.LOOP -> LoopOptions(options, onOptions)
            VideoTool.SOCIAL_RESIZE -> SocialOptions(options, onOptions)
            VideoTool.GIF_TO_VIDEO, VideoTool.REVERSE, VideoTool.MERGE -> Unit
        }
    }
}

@Composable
private fun CompressOptions(
    options: ProcessOptions,
    onOptions: ((ProcessOptions) -> ProcessOptions) -> Unit,
) {
    SectionTitle(stringResource(R.string.quality))
    val qualities = listOf(CompressQuality.LOW, CompressQuality.BALANCED, CompressQuality.HIGH, CompressQuality.CUSTOM)
    val qualityLabels = listOf(
        stringResource(R.string.preset_low),
        stringResource(R.string.preset_balanced),
        stringResource(R.string.preset_high),
        stringResource(R.string.preset_custom),
    )
    ChoiceChipRow(
        items = qualities.mapIndexed { i, q -> qualityLabels[i] to (options.quality == q) },
        onSelect = { index ->
            onOptions { current ->
                current.copy(
                    quality = qualities[index],
                    targetSizeMb = if (qualities[index] == CompressQuality.CUSTOM) current.targetSizeMb ?: 8f else null,
                )
            }
        },
    )
    if (options.quality == CompressQuality.CUSTOM) {
        val target = options.targetSizeMb ?: 8f
        Text(
            text = stringResource(R.string.target_size_value, String.format("%.1f", target)),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 12.dp),
        )
        Slider(
            value = target,
            onValueChange = { value -> onOptions { it.copy(targetSizeMb = value) } },
            valueRange = 1f..50f,
        )
    }
    SectionTitle(stringResource(R.string.codec), modifier = Modifier.padding(top = 12.dp))
    ChoiceChipRow(
        items = listOf(
            "H.264" to (options.codec == VideoCodec.H264),
            "H.265" to (options.codec == VideoCodec.H265),
        ),
        onSelect = { index -> onOptions { it.copy(codec = if (index == 0) VideoCodec.H264 else VideoCodec.H265) } },
    )
    SectionTitle(stringResource(R.string.resolution), modifier = Modifier.padding(top = 12.dp))
    val resolutions = OutputResolution.entries
    val labels = listOf(
        stringResource(R.string.res_original),
        "1080p",
        "720p",
        "480p",
        "360p",
    )
    ChoiceChipRow(
        items = resolutions.mapIndexed { i, res -> labels[i] to (options.resolution == res) },
        onSelect = { index -> onOptions { current -> current.copy(resolution = resolutions[index]) } },
    )
}

@Composable
private fun ConvertOptions(
    options: ProcessOptions,
    onOptions: ((ProcessOptions) -> ProcessOptions) -> Unit,
) {
    SectionTitle(stringResource(R.string.output_format))
    val formats = OutputFormat.entries
    val labels = listOf("MP4", "MOV", "MKV", "WEBM", "AVI", "3GP")
    ChoiceChipRow(
        items = formats.mapIndexed { i, format -> labels[i] to (options.outputFormat == format) },
        onSelect = { index -> onOptions { current -> current.copy(outputFormat = formats[index]) } },
    )
}

@Composable
private fun AudioOptions(
    options: ProcessOptions,
    onOptions: ((ProcessOptions) -> ProcessOptions) -> Unit,
) {
    SectionTitle(stringResource(R.string.audio_format))
    val formats = AudioFormat.entries
    ChoiceChipRow(
        items = formats.map { it.name to (options.audioFormat == it) },
        onSelect = { index -> onOptions { current -> current.copy(audioFormat = formats[index]) } },
    )
}

@Composable
private fun GifOptions(
    state: EditorUiState,
    options: ProcessOptions,
    onOptions: ((ProcessOptions) -> ProcessOptions) -> Unit,
) {
    SectionTitle(stringResource(R.string.gif_fps))
    val fps = listOf(8, 10, 15, 24)
    ChoiceChipRow(
        items = fps.map { "$it" to (options.gifFps == it) },
        onSelect = { index -> onOptions { current -> current.copy(gifFps = fps[index]) } },
    )
    SectionTitle(stringResource(R.string.gif_width), modifier = Modifier.padding(top = 12.dp))
    val widths = listOf(240, 320, 480, 720)
    ChoiceChipRow(
        items = widths.map { "${it}px" to (options.gifWidth == it) },
        onSelect = { index -> onOptions { current -> current.copy(gifWidth = widths[index]) } },
    )
    TrimOptions(state, options, onOptions)
}

@Composable
private fun CropOptions(
    options: ProcessOptions,
    onOptions: ((ProcessOptions) -> ProcessOptions) -> Unit,
) {
    SectionTitle(stringResource(R.string.crop_aspect))
    val aspects = CropAspect.entries
    val labels = listOf(
        stringResource(R.string.crop_free),
        "1:1",
        "16:9",
        "9:16",
        "4:3",
    )
    ChoiceChipRow(
        items = aspects.mapIndexed { i, aspect -> labels[i] to (options.cropAspect == aspect) },
        onSelect = { index -> onOptions { current -> current.copy(cropAspect = aspects[index]) } },
    )
}

@Composable
private fun TrimOptions(
    state: EditorUiState,
    options: ProcessOptions,
    onOptions: ((ProcessOptions) -> ProcessOptions) -> Unit,
) {
    val duration = state.media.firstOrNull()?.durationMs?.toFloat()?.coerceAtLeast(1000f) ?: 1000f
    val start = options.trimStartMs.toFloat().coerceIn(0f, duration)
    val end = (options.trimEndMs ?: duration.toLong()).toFloat().coerceIn(start + 250f, duration)
    SectionTitle(stringResource(R.string.trim_range), modifier = Modifier.padding(top = 12.dp))
    Text(
        text = "${start.toLong().toDurationLabel()}  →  ${end.toLong().toDurationLabel()}",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
    Text(stringResource(R.string.trim_start), style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(top = 8.dp))
    Slider(
        value = start,
        onValueChange = { value -> onOptions { it.copy(trimStartMs = value.toLong().coerceAtMost((end - 250).toLong())) } },
        valueRange = 0f..(duration - 250f),
    )
    Text(stringResource(R.string.trim_end), style = MaterialTheme.typography.labelMedium)
    Slider(
        value = end,
        onValueChange = { value -> onOptions { it.copy(trimEndMs = value.toLong().coerceAtLeast((start + 250).toLong())) } },
        valueRange = 250f..duration,
    )
}

@Composable
private fun RotateOptions(
    options: ProcessOptions,
    onOptions: ((ProcessOptions) -> ProcessOptions) -> Unit,
) {
    SectionTitle(stringResource(R.string.rotate_action))
    val actions = RotateAction.entries
    val labels = listOf(
        stringResource(R.string.rotate_cw),
        stringResource(R.string.rotate_ccw),
        stringResource(R.string.rotate_180),
        stringResource(R.string.flip_h),
        stringResource(R.string.flip_v),
    )
    ChoiceChipRow(
        items = actions.mapIndexed { i, action -> labels[i] to (options.rotateAction == action) },
        onSelect = { index -> onOptions { current -> current.copy(rotateAction = actions[index]) } },
    )
}

@Composable
private fun SpeedOptions(
    options: ProcessOptions,
    onOptions: ((ProcessOptions) -> ProcessOptions) -> Unit,
) {
    SectionTitle(stringResource(R.string.speed))
    val speeds = listOf(0.25f, 0.5f, 1f, 1.5f, 2f, 3f)
    ChoiceChipRow(
        items = speeds.map { "${it}x" to (options.speed == it) },
        onSelect = { index -> onOptions { current -> current.copy(speed = speeds[index]) } },
    )
}

@Composable
private fun VolumeOptions(
    options: ProcessOptions,
    onOptions: ((ProcessOptions) -> ProcessOptions) -> Unit,
) {
    SectionTitle(stringResource(R.string.volume))
    Text("${options.volumePercent}%", style = MaterialTheme.typography.bodyMedium)
    Slider(
        value = options.volumePercent.toFloat(),
        onValueChange = { onOptions { current -> current.copy(volumePercent = it.toInt(), mute = false, removeAudio = false) } },
        valueRange = 0f..200f,
        enabled = !options.mute && !options.removeAudio,
    )
    SwitchRow(stringResource(R.string.mute), options.mute) {
        onOptions { current -> current.copy(mute = it, removeAudio = if (it) false else current.removeAudio) }
    }
    SwitchRow(stringResource(R.string.remove_audio), options.removeAudio) {
        onOptions { current -> current.copy(removeAudio = it, mute = if (it) false else current.mute) }
    }
}

@Composable
private fun LoopOptions(
    options: ProcessOptions,
    onOptions: ((ProcessOptions) -> ProcessOptions) -> Unit,
) {
    SectionTitle(stringResource(R.string.loop_times))
    Text("${options.loopCount}×", style = MaterialTheme.typography.bodyMedium)
    Slider(
        value = options.loopCount.toFloat(),
        onValueChange = { onOptions { current -> current.copy(loopCount = it.toInt()) } },
        valueRange = 2f..10f,
        steps = 7,
    )
}

@Composable
private fun SocialOptions(
    options: ProcessOptions,
    onOptions: ((ProcessOptions) -> ProcessOptions) -> Unit,
) {
    SectionTitle(stringResource(R.string.social_preset))
    val presets = SocialPreset.entries
    val labels = listOf(
        stringResource(R.string.social_ig_reel),
        stringResource(R.string.social_ig_post),
        stringResource(R.string.social_ig_story),
        stringResource(R.string.social_youtube),
        stringResource(R.string.social_tiktok),
        stringResource(R.string.social_whatsapp),
        stringResource(R.string.social_twitter),
        stringResource(R.string.social_facebook),
    )
    ChoiceChipRow(
        items = presets.mapIndexed { i, preset -> labels[i] to (options.socialPreset == preset) },
        onSelect = { index -> onOptions { current -> current.copy(socialPreset = presets[index]) } },
    )
}

@Composable
private fun SwitchRow(label: String, checked: Boolean, onChecked: (Boolean) -> Unit) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier
            .padding(vertical = 4.dp)
            .then(Modifier),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text(label, style = MaterialTheme.typography.bodyLarge, modifier = Modifier.weight(1f))
        Switch(checked = checked, onCheckedChange = onChecked)
    }
}
