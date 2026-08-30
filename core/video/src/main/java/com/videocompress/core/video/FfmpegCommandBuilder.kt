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
    val arguments: List<String>,
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
        require(inputs.isNotEmpty() && media.isNotEmpty()) { "Select a video first." }
        val first = media.first()
        val firstFile = inputs.first()
        require(firstFile.exists() && firstFile.length() > 0L) { "Could not read the selected video." }
        val stamp = System.currentTimeMillis()
        return when (options.tool) {
            VideoTool.COMPRESS -> compress(firstFile, first, options, outputDir, stamp)
            VideoTool.CONVERT -> convert(firstFile, first, options, outputDir, stamp)
            VideoTool.EXTRACT_AUDIO -> extractAudio(firstFile, first, options, outputDir, stamp)
            VideoTool.VIDEO_TO_GIF -> videoToGif(firstFile, first, options, outputDir, stamp)
            VideoTool.GIF_TO_VIDEO -> gifToVideo(firstFile, outputDir, stamp)
            VideoTool.CROP -> crop(firstFile, first, options, outputDir, stamp)
            VideoTool.TRIM -> trim(firstFile, first, options, outputDir, stamp)
            VideoTool.ROTATE -> rotate(firstFile, first, options, outputDir, stamp)
            VideoTool.SPEED -> speed(firstFile, first, options, outputDir, stamp)
            VideoTool.VOLUME -> volume(firstFile, first, options, outputDir, stamp)
            VideoTool.REVERSE -> reverse(firstFile, first, outputDir, stamp)
            VideoTool.LOOP -> loop(firstFile, first, options, outputDir, stamp)
            VideoTool.MERGE -> merge(inputs, media, outputDir, stamp)
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
        val vf = listOfNotNull(scaleFilter(media, options.resolution), "format=yuv420p").joinToString(",")
        val args = buildList {
            addAll(inputArgs(input))
            addAll(mapVideoOptionalAudio())
            addAll(videoEncoder(options.codec))
            addAll(listOf("-b:v", "${bitrate}k", "-maxrate", "${bitrate}k", "-bufsize", "${bitrate * 2}k"))
            addAll(listOf("-vf", vf, "-pix_fmt", "yuv420p"))
            addAll(aac())
            addAll(faststart())
            add(output.absolutePath)
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
            OutputFormat.WEBM -> buildList {
                addAll(inputArgs(input))
                addAll(mapVideoOptionalAudio())
                addAll(listOf("-c:v", "libvpx-vp9", "-b:v", "1M", "-pix_fmt", "yuv420p", "-c:a", "libopus"))
                add(output.absolutePath)
            }
            OutputFormat.AVI -> buildList {
                addAll(inputArgs(input))
                addAll(mapVideoOptionalAudio())
                addAll(listOf("-c:v", "mpeg4", "-q:v", "5", "-pix_fmt", "yuv420p"))
                addAll(aac())
                add(output.absolutePath)
            }
            OutputFormat.THREE_GP -> buildList {
                addAll(inputArgs(input))
                addAll(mapVideoOptionalAudio())
                addAll(listOf("-c:v", "libx264", "-preset", "veryfast", "-profile:v", "baseline", "-pix_fmt", "yuv420p"))
                addAll(listOf("-c:a", "aac", "-ac", "1", "-ar", "16000"))
                add(output.absolutePath)
            }
            else -> buildList {
                addAll(inputArgs(input))
                addAll(mapVideoOptionalAudio())
                addAll(h264())
                addAll(aac())
                addAll(faststart())
                add(output.absolutePath)
            }
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
        if (!media.hasAudio) {
            error("This video has no audio track.")
        }
        val ext = options.audioFormat.extension
        val output = File(outputDir, "${baseName(media)}_audio_$stamp.$ext")
        val encode = when (options.audioFormat) {
            AudioFormat.MP3 -> listOf("-c:a", "libmp3lame", "-q:a", "2")
            AudioFormat.WAV -> listOf("-c:a", "pcm_s16le")
            AudioFormat.AAC, AudioFormat.M4A -> listOf("-c:a", "aac", "-b:a", "192k")
        }
        val args = buildList {
            addAll(inputArgs(input))
            addAll(listOf("-vn", "-map", "0:a:0"))
            addAll(encode)
            add(output.absolutePath)
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
        val duration = ((options.trimEndMs ?: media.durationMs.coerceAtLeast(1L)) - options.trimStartMs)
            .coerceAtLeast(500L) / 1000.0
        val args = buildList {
            addAll(listOf("-y", "-hide_banner", "-ss", start.toString(), "-t", duration.toString(), "-i", input.absolutePath))
            addAll(listOf("-vf", "fps=${options.gifFps},scale=${options.gifWidth}:-1:flags=lanczos", "-loop", "0"))
            add(output.absolutePath)
        }
        return PreparedCommand(args, output, "image/gif", output.name)
    }

    private fun gifToVideo(input: File, outputDir: File, stamp: Long): PreparedCommand {
        val output = File(outputDir, "gif_video_$stamp.mp4")
        val args = buildList {
            addAll(inputArgs(input))
            addAll(h264())
            addAll(faststart())
            add(output.absolutePath)
        }
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
        val box = cropBox(media, options)
        val args = buildList {
            addAll(inputArgs(input))
            addAll(mapVideoOptionalAudio())
            addAll(listOf("-vf", "crop=${box[0]}:${box[1]}:${box[2]}:${box[3]},format=yuv420p"))
            addAll(h264())
            addAll(aac())
            add(output.absolutePath)
        }
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
        val end = (options.trimEndMs ?: media.durationMs.coerceAtLeast(options.trimStartMs + 500L)) / 1000.0
        val args = buildList {
            addAll(listOf("-y", "-hide_banner", "-ss", start.toString(), "-to", end.toString(), "-i", input.absolutePath))
            addAll(mapVideoOptionalAudio())
            addAll(h264())
            addAll(aac())
            addAll(faststart())
            add(output.absolutePath)
        }
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
            RotateAction.CW_90 -> "transpose=1,format=yuv420p"
            RotateAction.CCW_90 -> "transpose=2,format=yuv420p"
            RotateAction.ROTATE_180 -> "transpose=1,transpose=1,format=yuv420p"
            RotateAction.FLIP_HORIZONTAL -> "hflip,format=yuv420p"
            RotateAction.FLIP_VERTICAL -> "vflip,format=yuv420p"
        }
        val args = buildList {
            addAll(inputArgs(input))
            addAll(mapVideoOptionalAudio())
            addAll(listOf("-vf", vf))
            addAll(h264())
            addAll(aac())
            add(output.absolutePath)
        }
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
        val args = buildList {
            addAll(inputArgs(input))
            addAll(listOf("-map", "0:v:0", "-filter:v", "setpts=${videoPts}*PTS"))
            if (media.hasAudio) {
                addAll(listOf("-map", "0:a:0", "-filter:a", atempoChain(factor)))
                addAll(aac())
            } else {
                add("-an")
            }
            addAll(h264())
            add(output.absolutePath)
        }
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
        val args = if (options.removeAudio || options.mute || !media.hasAudio) {
            buildList {
                addAll(inputArgs(input))
                addAll(listOf("-map", "0:v:0", "-c:v", "copy", "-an"))
                add(output.absolutePath)
            }
        } else {
            val gain = options.volumePercent / 100.0
            buildList {
                addAll(inputArgs(input))
                addAll(listOf("-map", "0:v:0", "-map", "0:a:0", "-c:v", "copy", "-af", "volume=$gain"))
                addAll(aac())
                add(output.absolutePath)
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
        val args = buildList {
            addAll(inputArgs(input))
            addAll(listOf("-map", "0:v:0", "-vf", "reverse,format=yuv420p"))
            if (media.hasAudio) {
                addAll(listOf("-map", "0:a:0", "-af", "areverse"))
                addAll(aac())
            } else {
                add("-an")
            }
            addAll(h264())
            add(output.absolutePath)
        }
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
        val args = buildList {
            addAll(listOf("-y", "-hide_banner", "-stream_loop", repeats.toString(), "-i", input.absolutePath))
            addAll(mapVideoOptionalAudio())
            addAll(h264())
            addAll(aac())
            add(output.absolutePath)
        }
        return PreparedCommand(args, output, "video/mp4", output.name)
    }

    private fun merge(
        inputs: List<File>,
        media: List<VideoMedia>,
        outputDir: File,
        stamp: Long,
    ): PreparedCommand {
        require(inputs.size >= 2) { "Select at least two videos to merge." }
        val output = File(outputDir, "merged_$stamp.mp4")
        val count = inputs.size
        val withAudio = media.size == inputs.size && media.all { it.hasAudio }
        val filter = if (withAudio) {
            (0 until count).joinToString("") { "[$it:v:0][$it:a:0]" } +
                "concat=n=$count:v=1:a=1[v][a]"
        } else {
            (0 until count).joinToString("") { "[$it:v:0]" } +
                "concat=n=$count:v=1:a=0[v]"
        }
        val args = buildList {
            addAll(listOf("-y", "-hide_banner"))
            inputs.forEach { addAll(listOf("-i", it.absolutePath)) }
            addAll(listOf("-filter_complex", filter, "-map", "[v]"))
            if (withAudio) {
                addAll(listOf("-map", "[a]"))
                addAll(aac())
            } else {
                add("-an")
            }
            addAll(h264())
            addAll(faststart())
            add(output.absolutePath)
        }
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
        val vf = "scale=$w:$h:force_original_aspect_ratio=decrease,pad=$w:$h:(ow-iw)/2:(oh-ih)/2,format=yuv420p"
        val args = buildList {
            addAll(inputArgs(input))
            addAll(mapVideoOptionalAudio())
            addAll(listOf("-vf", vf))
            addAll(h264())
            addAll(aac())
            addAll(faststart())
            add(output.absolutePath)
        }
        return PreparedCommand(args, output, "video/mp4", output.name)
    }

    private fun inputArgs(input: File) = listOf("-y", "-hide_banner", "-i", input.absolutePath)

    private fun mapVideoOptionalAudio() = listOf("-map", "0:v:0", "-map", "0:a?")

    private fun aac() = listOf("-c:a", "aac", "-b:a", "128k", "-ac", "2", "-ar", "44100")

    private fun h264() = listOf("-c:v", "libx264", "-preset", "veryfast", "-pix_fmt", "yuv420p")

    private fun faststart() = listOf("-movflags", "+faststart")

    private fun videoEncoder(codec: VideoCodec): List<String> = when (codec) {
        VideoCodec.H264 -> listOf("-c:v", "libx264", "-preset", "veryfast")
        VideoCodec.H265 -> listOf("-c:v", "libx265", "-preset", "veryfast", "-tag:v", "hvc1")
    }

    private fun videoBitrate(media: VideoMedia, options: ProcessOptions): Int {
        options.targetSizeMb?.let { target ->
            val durationSec = max(media.durationMs / 1000.0, 1.0)
            val totalKbit = target * 8192.0
            return ((totalKbit / durationSec) - 128.0).toInt().coerceIn(150, 20_000)
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
