package com.videocompress.core.video

import com.videocompress.core.common.AudioFormat
import com.videocompress.core.common.CompressQuality
import com.videocompress.core.common.CropAspect
import com.videocompress.core.common.OutputFormat
import com.videocompress.core.common.OutputResolution
import com.videocompress.core.common.RotateAction
import com.videocompress.core.common.SocialPreset
import com.videocompress.core.common.VideoCodec
import com.videocompress.core.common.VideoTool
import com.videocompress.core.domain.model.ProcessOptions
import com.videocompress.core.domain.model.VideoMedia
import java.io.File
import java.util.Locale
import kotlin.math.max
import kotlin.math.roundToInt

data class PreparedCommand(
    val arguments: String,
    val outputFile: File,
    val mimeType: String,
    val displayName: String,
)

object FfmpegCommandBuilder {

    fun build(
        inputs: List<File>,
        media: List<VideoMedia>,
        options: ProcessOptions,
        outputDir: File,
    ): PreparedCommand {
        val first = media.first()
        val firstFile = inputs.first()
        val stamp = System.currentTimeMillis()
        return when (options.tool) {
            VideoTool.COMPRESS -> compress(firstFile, first, options, outputDir, stamp)
            VideoTool.CONVERT -> convert(firstFile, first, options, outputDir, stamp)
            VideoTool.EXTRACT_AUDIO -> extractAudio(firstFile, first, options, outputDir, stamp)
            VideoTool.VIDEO_TO_GIF -> videoToGif(firstFile, first, options, outputDir, stamp)
            VideoTool.GIF_TO_VIDEO -> gifToVideo(firstFile, first, outputDir, stamp)
            VideoTool.CROP -> crop(firstFile, first, options, outputDir, stamp)
            VideoTool.TRIM -> trim(firstFile, first, options, outputDir, stamp)
            VideoTool.ROTATE -> rotate(firstFile, first, options, outputDir, stamp)
            VideoTool.SPEED -> speed(firstFile, first, options, outputDir, stamp)
            VideoTool.VOLUME -> volume(firstFile, first, options, outputDir, stamp)
            VideoTool.REVERSE -> reverse(firstFile, first, outputDir, stamp)
            VideoTool.LOOP -> loop(firstFile, first, options, outputDir, stamp)
            VideoTool.MERGE -> merge(inputs, first, outputDir, stamp)
            VideoTool.SOCIAL_RESIZE -> social(firstFile, first, options, outputDir, stamp)
        }
    }

    private fun compress(
        input: File,
        media: VideoMedia,
        options: ProcessOptions,
        outputDir: File,
        stamp: Long,
    ): PreparedCommand {
        val output = File(outputDir, "compressed_$stamp.mp4")
        val bitrate = videoBitrate(media, options)
        val scale = scaleFilter(media, options.resolution)
        val codec = encoder(options.codec)
        val vf = listOfNotNull(scale).joinToString(",")
        val args = buildString {
            append("-y -i ${q(input.absolutePath)} ")
            append("-c:v $codec -b:v ${bitrate}k -maxrate ${bitrate}k -bufsize ${bitrate * 2}k ")
            if (vf.isNotEmpty()) append("-vf $vf ")
            append("-c:a aac -b:a 128k -movflags +faststart ")
            append(q(output.absolutePath))
        }
        return PreparedCommand(args, output, "video/mp4", output.name)
    }

    private fun convert(
        input: File,
        media: VideoMedia,
        options: ProcessOptions,
        outputDir: File,
        stamp: Long,
    ): PreparedCommand {
        val ext = options.outputFormat.extension
        val output = File(outputDir, "converted_$stamp.$ext")
        val args = when (options.outputFormat) {
            OutputFormat.WEBM ->
                "-y -i ${q(input.absolutePath)} -c:v libvpx-vp9 -b:v 1M -c:a libopus ${q(output.absolutePath)}"
            OutputFormat.AVI ->
                "-y -i ${q(input.absolutePath)} -c:v mpeg4 -q:v 5 -c:a libmp3lame ${q(output.absolutePath)}"
            OutputFormat.THREE_GP ->
                "-y -i ${q(input.absolutePath)} -c:v libx264 -profile:v baseline -level 3.0 -c:a aac -ac 1 -ar 16000 ${q(output.absolutePath)}"
            else ->
                "-y -i ${q(input.absolutePath)} -c:v libx264 -preset veryfast -crf 23 -c:a aac -movflags +faststart ${q(output.absolutePath)}"
        }
        return PreparedCommand(args, output, options.outputFormat.mime, output.name)
    }

    private fun extractAudio(
        input: File,
        media: VideoMedia,
        options: ProcessOptions,
        outputDir: File,
        stamp: Long,
    ): PreparedCommand {
        val ext = options.audioFormat.extension
        val output = File(outputDir, "${baseName(media)}_audio_$stamp.$ext")
        val args = when (options.audioFormat) {
            AudioFormat.MP3 -> "-y -i ${q(input.absolutePath)} -vn -acodec libmp3lame -q:a 2 ${q(output.absolutePath)}"
            AudioFormat.WAV -> "-y -i ${q(input.absolutePath)} -vn -acodec pcm_s16le ${q(output.absolutePath)}"
            AudioFormat.AAC, AudioFormat.M4A ->
                "-y -i ${q(input.absolutePath)} -vn -c:a aac -b:a 192k ${q(output.absolutePath)}"
        }
        return PreparedCommand(args, output, options.audioFormat.mime, output.name)
    }

    private fun videoToGif(
        input: File,
        media: VideoMedia,
        options: ProcessOptions,
        outputDir: File,
        stamp: Long,
    ): PreparedCommand {
        val output = File(outputDir, "clip_$stamp.gif")
        val start = options.trimStartMs / 1000.0
        val duration = ((options.trimEndMs ?: media.durationMs) - options.trimStartMs)
            .coerceAtLeast(500L) / 1000.0
        val args = "-y -ss $start -t $duration -i ${q(input.absolutePath)} " +
            "-vf fps=${options.gifFps},scale=${options.gifWidth}:-1:flags=lanczos " +
            "-loop 0 ${q(output.absolutePath)}"
        return PreparedCommand(args, output, "image/gif", output.name)
    }

    private fun gifToVideo(
        input: File,
        media: VideoMedia,
        outputDir: File,
        stamp: Long,
    ): PreparedCommand {
        val output = File(outputDir, "gif_video_$stamp.mp4")
        val args = "-y -i ${q(input.absolutePath)} -movflags +faststart -pix_fmt yuv420p " +
            "-c:v libx264 -preset veryfast ${q(output.absolutePath)}"
        return PreparedCommand(args, output, "video/mp4", output.name)
    }

    private fun crop(
        input: File,
        media: VideoMedia,
        options: ProcessOptions,
        outputDir: File,
        stamp: Long,
    ): PreparedCommand {
        val output = File(outputDir, "cropped_$stamp.mp4")
        val (w, h, x, y) = cropBox(media, options)
        val args = "-y -i ${q(input.absolutePath)} -vf crop=$w:$h:$x:$y -c:v libx264 -preset veryfast " +
            "-c:a copy ${q(output.absolutePath)}"
        return PreparedCommand(args, output, "video/mp4", output.name)
    }

    private fun trim(
        input: File,
        media: VideoMedia,
        options: ProcessOptions,
        outputDir: File,
        stamp: Long,
    ): PreparedCommand {
        val output = File(outputDir, "trimmed_$stamp.mp4")
        val start = options.trimStartMs / 1000.0
        val end = (options.trimEndMs ?: media.durationMs) / 1000.0
        val args = "-y -ss $start -to $end -i ${q(input.absolutePath)} -c copy ${q(output.absolutePath)}"
        return PreparedCommand(args, output, "video/mp4", output.name)
    }

    private fun rotate(
        input: File,
        media: VideoMedia,
        options: ProcessOptions,
        outputDir: File,
        stamp: Long,
    ): PreparedCommand {
        val output = File(outputDir, "rotated_$stamp.mp4")
        val vf = when (options.rotateAction) {
            RotateAction.CW_90 -> "transpose=1"
            RotateAction.CCW_90 -> "transpose=2"
            RotateAction.ROTATE_180 -> "transpose=1,transpose=1"
            RotateAction.FLIP_HORIZONTAL -> "hflip"
            RotateAction.FLIP_VERTICAL -> "vflip"
        }
        val args = "-y -i ${q(input.absolutePath)} -vf $vf -c:v libx264 -preset veryfast -c:a copy ${q(output.absolutePath)}"
        return PreparedCommand(args, output, "video/mp4", output.name)
    }

    private fun speed(
        input: File,
        media: VideoMedia,
        options: ProcessOptions,
        outputDir: File,
        stamp: Long,
    ): PreparedCommand {
        val output = File(outputDir, "speed_$stamp.mp4")
        val factor = options.speed.coerceIn(0.25f, 4f)
        val videoPts = 1.0 / factor
        val audioFilters = atempoChain(factor)
        val args = "-y -i ${q(input.absolutePath)} -filter:v setpts=$videoPts*PTS " +
            "-filter:a $audioFilters -c:v libx264 -preset veryfast ${q(output.absolutePath)}"
        return PreparedCommand(args, output, "video/mp4", output.name)
    }

    private fun volume(
        input: File,
        media: VideoMedia,
        options: ProcessOptions,
        outputDir: File,
        stamp: Long,
    ): PreparedCommand {
        val output = File(outputDir, "volume_$stamp.mp4")
        val args = when {
            options.removeAudio || options.mute ->
                "-y -i ${q(input.absolutePath)} -c:v copy -an ${q(output.absolutePath)}"
            else -> {
                val gain = options.volumePercent / 100.0
                "-y -i ${q(input.absolutePath)} -c:v copy -af volume=$gain ${q(output.absolutePath)}"
            }
        }
        return PreparedCommand(args, output, "video/mp4", output.name)
    }

    private fun reverse(
        input: File,
        media: VideoMedia,
        outputDir: File,
        stamp: Long,
    ): PreparedCommand {
        val output = File(outputDir, "reversed_$stamp.mp4")
        val args = "-y -i ${q(input.absolutePath)} -vf reverse -af areverse -c:v libx264 -preset veryfast ${q(output.absolutePath)}"
        return PreparedCommand(args, output, "video/mp4", output.name)
    }

    private fun loop(
        input: File,
        media: VideoMedia,
        options: ProcessOptions,
        outputDir: File,
        stamp: Long,
    ): PreparedCommand {
        val output = File(outputDir, "looped_$stamp.mp4")
        val repeats = (options.loopCount - 1).coerceIn(1, 19)
        val args = "-y -stream_loop $repeats -i ${q(input.absolutePath)} -c copy ${q(output.absolutePath)}"
        return PreparedCommand(args, output, "video/mp4", output.name)
    }

    private fun merge(
        inputs: List<File>,
        media: VideoMedia,
        outputDir: File,
        stamp: Long,
    ): PreparedCommand {
        val listFile = File(outputDir, "concat_$stamp.txt")
        listFile.writeText(inputs.joinToString("\n") { "file '${it.absolutePath.replace("'", "'\\''")}'" })
        val output = File(outputDir, "merged_$stamp.mp4")
        val args = "-y -f concat -safe 0 -i ${q(listFile.absolutePath)} -c copy ${q(output.absolutePath)}"
        return PreparedCommand(args, output, "video/mp4", output.name)
    }

    private fun social(
        input: File,
        media: VideoMedia,
        options: ProcessOptions,
        outputDir: File,
        stamp: Long,
    ): PreparedCommand {
        val (w, h) = options.socialPreset.size
        val output = File(outputDir, "${options.socialPreset.name.lowercase()}_$stamp.mp4")
        val vf = "scale=$w:$h:force_original_aspect_ratio=decrease,pad=$w:$h:(ow-iw)/2:(oh-ih)/2"
        val args = "-y -i ${q(input.absolutePath)} -vf $vf -c:v libx264 -preset veryfast -c:a aac " +
            "-movflags +faststart ${q(output.absolutePath)}"
        return PreparedCommand(args, output, "video/mp4", output.name)
    }

    private fun videoBitrate(media: VideoMedia, options: ProcessOptions): Int {
        options.targetSizeMb?.let { target ->
            val durationSec = max(media.durationMs / 1000.0, 1.0)
            val audioBits = 128.0
            val totalKbit = target * 8192.0
            return ((totalKbit / durationSec) - audioBits).toInt().coerceIn(150, 20_000)
        }
        val base = when (options.quality) {
            CompressQuality.LOW -> 600
            CompressQuality.BALANCED -> 1400
            CompressQuality.HIGH -> 2800
            CompressQuality.CUSTOM -> 1800
        }
        return when (options.resolution) {
            OutputResolution.P360 -> (base * 0.45).roundToInt()
            OutputResolution.P480 -> (base * 0.65).roundToInt()
            OutputResolution.P720 -> base
            OutputResolution.P1080 -> (base * 1.6).roundToInt()
            OutputResolution.ORIGINAL -> base
        }
    }

    private fun scaleFilter(media: VideoMedia, resolution: OutputResolution): String? {
        val targetH = when (resolution) {
            OutputResolution.ORIGINAL -> return null
            OutputResolution.P1080 -> 1080
            OutputResolution.P720 -> 720
            OutputResolution.P480 -> 480
            OutputResolution.P360 -> 360
        }
        if (media.height in 1 until targetH && media.width in 1 until targetH) return null
        return "scale=-2:$targetH"
    }

    private fun encoder(codec: VideoCodec): String = when (codec) {
        VideoCodec.H264 -> "libx264 -preset veryfast"
        VideoCodec.H265 -> "libx265 -preset veryfast -tag:v hvc1"
    }

    private fun cropBox(media: VideoMedia, options: ProcessOptions): IntArray {
        val srcW = media.width.coerceAtLeast(2)
        val srcH = media.height.coerceAtLeast(2)
        val (ratioW, ratioH) = when (options.cropAspect) {
            CropAspect.FREE -> {
                val left = (srcW * options.cropLeft).roundToInt()
                val top = (srcH * options.cropTop).roundToInt()
                val right = (srcW * options.cropRight).roundToInt()
                val bottom = (srcH * options.cropBottom).roundToInt()
                val w = (srcW - left - right).coerceAtLeast(2) / 2 * 2
                val h = (srcH - top - bottom).coerceAtLeast(2) / 2 * 2
                return intArrayOf(w, h, left / 2 * 2, top / 2 * 2)
            }
            CropAspect.SQUARE -> 1 to 1
            CropAspect.RATIO_16_9 -> 16 to 9
            CropAspect.RATIO_9_16 -> 9 to 16
            CropAspect.RATIO_4_3 -> 4 to 3
        }
        val targetRatio = ratioW.toFloat() / ratioH
        val srcRatio = srcW.toFloat() / srcH
        val (w, h) = if (srcRatio > targetRatio) {
            val height = srcH / 2 * 2
            val width = (height * targetRatio).roundToInt() / 2 * 2
            width to height
        } else {
            val width = srcW / 2 * 2
            val height = (width / targetRatio).roundToInt() / 2 * 2
            width to height
        }
        val x = ((srcW - w) / 2) / 2 * 2
        val y = ((srcH - h) / 2) / 2 * 2
        return intArrayOf(w.coerceAtLeast(2), h.coerceAtLeast(2), x.coerceAtLeast(0), y.coerceAtLeast(0))
    }

    private fun atempoChain(speed: Float): String {
        var remaining = speed
        val parts = mutableListOf<String>()
        while (remaining > 2.0001f) {
            parts += "atempo=2.0"
            remaining /= 2f
        }
        while (remaining < 0.4999f) {
            parts += "atempo=0.5"
            remaining /= 0.5f
        }
        parts += "atempo=${String.format(Locale.US, "%.3f", remaining)}"
        return parts.joinToString(",")
    }

    private fun q(path: String): String = "\"${path.replace("\"", "\\\"")}\""

    private fun baseName(media: VideoMedia): String =
        media.displayName.substringBeforeLast('.').ifBlank { "video" }
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .take(40)
}

private val OutputFormat.extension: String
    get() = when (this) {
        OutputFormat.MP4 -> "mp4"
        OutputFormat.MOV -> "mov"
        OutputFormat.MKV -> "mkv"
        OutputFormat.WEBM -> "webm"
        OutputFormat.AVI -> "avi"
        OutputFormat.THREE_GP -> "3gp"
    }

private val OutputFormat.mime: String
    get() = when (this) {
        OutputFormat.MP4 -> "video/mp4"
        OutputFormat.MOV -> "video/quicktime"
        OutputFormat.MKV -> "video/x-matroska"
        OutputFormat.WEBM -> "video/webm"
        OutputFormat.AVI -> "video/x-msvideo"
        OutputFormat.THREE_GP -> "video/3gpp"
    }

private val AudioFormat.extension: String
    get() = when (this) {
        AudioFormat.MP3 -> "mp3"
        AudioFormat.M4A -> "m4a"
        AudioFormat.AAC -> "aac"
        AudioFormat.WAV -> "wav"
    }

private val AudioFormat.mime: String
    get() = when (this) {
        AudioFormat.MP3 -> "audio/mpeg"
        AudioFormat.M4A -> "audio/mp4"
        AudioFormat.AAC -> "audio/aac"
        AudioFormat.WAV -> "audio/wav"
    }

val SocialPreset.size: Pair<Int, Int>
    get() = when (this) {
        SocialPreset.INSTAGRAM_REEL, SocialPreset.INSTAGRAM_STORY, SocialPreset.TIKTOK -> 1080 to 1920
        SocialPreset.INSTAGRAM_POST -> 1080 to 1080
        SocialPreset.YOUTUBE -> 1920 to 1080
        SocialPreset.WHATSAPP -> 640 to 640
        SocialPreset.TWITTER, SocialPreset.FACEBOOK -> 1280 to 720
    }
