package com.videocompress.core.data.media

import android.content.Context
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.provider.OpenableColumns
import com.videocompress.core.domain.model.VideoMedia
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UriFileCopier @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    fun resolve(uri: Uri): VideoMedia {
        val name = queryName(uri) ?: "video"
        val size = querySize(uri)
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(context, uri)
            val duration = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)?.toLongOrNull() ?: 0L
            val width = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)?.toIntOrNull() ?: 0
            val height = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)?.toIntOrNull() ?: 0
            val mime = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
                ?: context.contentResolver.getType(uri)
                ?: "video/mp4"
            VideoMedia(
                uri = uri,
                displayName = name,
                sizeBytes = size,
                durationMs = duration,
                width = width,
                height = height,
                mimeType = mime,
            )
        } finally {
            runCatching { retriever.release() }
        }
    }

    fun copyToCache(uri: Uri, directory: File, index: Int): File {
        val ext = queryName(uri)?.substringAfterLast('.', "mp4") ?: "mp4"
        val target = File(directory, "input_${index}_${System.nanoTime()}.$ext")
        context.contentResolver.openInputStream(uri)?.use { input ->
            target.outputStream().use { input.copyTo(it) }
        } ?: error("Unable to read selected file")
        return target
    }

    private fun queryName(uri: Uri): String? {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) return cursor.getString(index)
            }
        }
        return uri.lastPathSegment
    }

    private fun querySize(uri: Uri): Long {
        context.contentResolver.query(uri, arrayOf(OpenableColumns.SIZE), null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (index >= 0) return cursor.getLong(index)
            }
        }
        return 0L
    }
}
