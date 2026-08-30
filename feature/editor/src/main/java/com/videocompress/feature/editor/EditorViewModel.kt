package com.videocompress.feature.editor

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
import dagger.hilt.android.lifecycle.HiltViewModel
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
    private val resolveVideo: ResolveVideoUseCase,
    private val processVideos: ProcessVideosUseCase,
    private val observeSettings: ObserveSettingsUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(EditorUiState())
    val state: StateFlow<EditorUiState> = _state.asStateFlow()

    fun start(tool: VideoTool) {
        viewModelScope.launch {
            val settings = observeSettings().first()
            _state.value = EditorUiState(
                tool = tool,
                options = ProcessOptions(
                    tool = tool,
                    quality = settings.defaultQuality,
                    codec = settings.defaultCodec,
                ),
            )
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
                    val merged = if (current.tool == VideoTool.MERGE || current.tool == VideoTool.COMPRESS) {
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
                _state.update { it.copy(isResolving = false, errorMessage = error.message) }
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
            _state.update { it.copy(isProcessing = true, progress = ProcessProgress(0f, 1, snapshot.media.size), errorMessage = null) }
            val result = processVideos(snapshot.media, snapshot.options) { progress ->
                _state.update { it.copy(progress = progress) }
            }
            result.onSuccess { outputs ->
                _state.update { it.copy(isProcessing = false, results = outputs, progress = null) }
            }.onFailure { error ->
                _state.update { it.copy(isProcessing = false, progress = null, errorMessage = error.message) }
            }
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
