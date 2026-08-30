package com.videocompress.feature.editor

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.videocompress.core.common.VideoTool
import com.videocompress.core.domain.model.ProcessOptions
import com.videocompress.core.domain.model.ProcessProgress
import com.videocompress.core.domain.model.ProcessResult
import com.videocompress.core.domain.model.VideoMedia
import com.videocompress.core.domain.usecase.ObserveSettingsUseCase
import com.videocompress.core.domain.usecase.ProcessVideosUseCase
import com.videocompress.core.domain.usecase.ResolveVideoUseCase
import com.videocompress.core.resources.R
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditorUiState(
    val tool: VideoTool = VideoTool.COMPRESS,
    val media: List<VideoMedia> = emptyList(),
    val options: ProcessOptions = ProcessOptions(VideoTool.COMPRESS),
    val isResolving: Boolean = false,
    val isProcessing: Boolean = false,
    val progress: ProcessProgress? = null,
    val results: List<ProcessResult> = emptyList(),
    val errorMessage: String? = null,
)

@HiltViewModel
class EditorViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val resolveVideo: ResolveVideoUseCase,
    private val processVideos: ProcessVideosUseCase,
    private val observeSettings: ObserveSettingsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(EditorUiState())
    val state: StateFlow<EditorUiState> = _state.asStateFlow()

    fun start(tool: VideoTool) {
        viewModelScope.launch {
            val settings = observeSettings().first()
            _state.update { current ->
                if (current.tool == tool && (current.media.isNotEmpty() || current.isProcessing)) {
                    current
                } else {
                    EditorUiState(
                        tool = tool,
                        options = ProcessOptions(
                            tool = tool,
                            quality = settings.defaultQuality,
                            codec = settings.defaultCodec,
                        ),
                    )
                }
            }
        }
    }

    fun addUris(uris: List<Uri>) {
        if (uris.isEmpty()) return
        viewModelScope.launch {
            _state.update { it.copy(isResolving = true, errorMessage = null, results = emptyList()) }
            runCatching {
                uris.map { resolveVideo(it) }
            }.onSuccess { media ->
                _state.update { current ->
                    val merged = if (
                        current.tool == VideoTool.MERGE ||
                        current.tool == VideoTool.COMPRESS ||
                        current.tool == VideoTool.CONVERT
                    ) {
                        (current.media + media).distinctBy { it.uri }
                    } else {
                        media.take(1)
                    }
                    val first = merged.firstOrNull()
                    current.copy(
                        media = merged,
                        isResolving = false,
                        options = current.options.copy(
                            trimEndMs = first?.durationMs,
                        ),
                    )
                }
            }.onFailure { error ->
                _state.update { it.copy(isResolving = false, errorMessage = friendlyError(error)) }
            }
        }
    }

    fun updateOptions(transform: (ProcessOptions) -> ProcessOptions) {
        _state.update { it.copy(options = transform(it.options)) }
    }

    fun removeMedia(uri: Uri) {
        _state.update { it.copy(media = it.media.filterNot { item -> item.uri == uri }) }
    }

    fun process() {
        val snapshot = _state.value
        if (snapshot.media.isEmpty() || snapshot.isProcessing) return
        viewModelScope.launch {
            _state.update { it.copy(isProcessing = true, progress = ProcessProgress(0f, 1, snapshot.media.size.coerceAtLeast(1)), errorMessage = null) }
            try {
                val result = processVideos(snapshot.media, snapshot.options) { progress ->
                    _state.update { it.copy(progress = progress) }
                }
                result.onSuccess { outputs ->
                    _state.update { it.copy(isProcessing = false, results = outputs, progress = null) }
                }.onFailure { error ->
                    _state.update {
                        it.copy(
                            isProcessing = false,
                            progress = null,
                            errorMessage = friendlyError(error),
                        )
                    }
                }
            } catch (error: Throwable) {
                _state.update {
                    it.copy(
                        isProcessing = false,
                        progress = null,
                        errorMessage = friendlyError(error),
                    )
                }
            }
        }
    }

    private fun friendlyError(error: Throwable): String {
        val raw = sequenceOf(error.message, error.cause?.message)
            .filterNotNull()
            .firstOrNull()
            .orEmpty()
        val lower = raw.lowercase()
        return when {
            "no audio" in lower -> context.getString(R.string.error_no_audio)
            "failed to load" in lower || "unsatisfiedlink" in lower || "ffmpegkit" in lower ->
                context.getString(R.string.error_ffmpeg_load)
            "could not read" in lower || "unable to read" in lower ->
                context.getString(R.string.error_read_video)
            "select a video" in lower -> context.getString(R.string.error_select_video)
            "empty or unreadable" in lower -> context.getString(R.string.error_empty_file)
            raw.isBlank() -> context.getString(R.string.error_process_failed)
            else -> raw.take(400)
        }
    }

    fun cancel() {
        processVideos.cancel()
        _state.update { it.copy(isProcessing = false, progress = null) }
    }

    fun clearError() {
        _state.update { it.copy(errorMessage = null) }
    }

    fun resetResults() {
        _state.update { it.copy(results = emptyList()) }
    }
}
