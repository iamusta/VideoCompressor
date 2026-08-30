package com.videocompress.core.common

import java.util.Locale
import kotlin.math.ln
import kotlin.math.pow

object FileSizeFormatter {
    fun format(bytes: Long): String {
        if (bytes < 1024) return "$bytes B"
        val exp = (ln(bytes.toDouble()) / ln(1024.0)).toInt().coerceAtMost(3)
        val value = bytes / 1024.0.pow(exp.toDouble())
        val unit = arrayOf("KB", "MB", "GB", "TB")[exp - 1]
        return String.format(Locale.US, if (value >= 100) "%.0f %s" else "%.1f %s", value, unit)
    }

    fun percentSaved(original: Long, result: Long): Int {
        if (original <= 0L) return 0
        return (((original - result).toDouble() / original) * 100).toInt().coerceIn(0, 99)
    }
}

fun Long.toDurationLabel(): String {
    val totalSeconds = (this / 1000).coerceAtLeast(0)
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return if (hours > 0) {
        String.format(Locale.US, "%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format(Locale.US, "%d:%02d", minutes, seconds)
    }
}
