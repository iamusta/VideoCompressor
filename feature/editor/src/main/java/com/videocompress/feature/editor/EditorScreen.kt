package com.videocompress.feature.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.decode.VideoFrameDecoder
import coil.request.ImageRequest
import com.videocompress.core.common.FileSizeFormatter
import com.videocompress.core.common.VideoTool
import com.videocompress.core.common.toDurationLabel
import com.videocompress.core.resources.R
import com.videocompress.core.ui.components.EmptyState
import com.videocompress.core.ui.components.PrimaryButton
import com.videocompress.core.ui.components.SecondaryButton
import com.videocompress.core.ui.util.allowsMultiple
import com.videocompress.core.ui.util.prefersGif
import com.videocompress.core.ui.util.subtitleRes
import com.videocompress.core.ui.util.titleRes

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorRoute(
    tool: VideoTool,
    incomingUris: List<Uri>,
    onBack: () -> Unit,
    viewModel: EditorViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    LaunchedEffect(tool) { viewModel.start(tool) }
    LaunchedEffect(incomingUris) {
        if (incomingUris.isNotEmpty()) viewModel.addUris(incomingUris)
    }
    EditorScreen(
        state = state,
        onBack = onBack,
        onPick = viewModel::addUris,
        onRemove = viewModel::removeMedia,
        onOptions = viewModel::updateOptions,
        onProcess = viewModel::process,
        onCancel = viewModel::cancel,
        onClearError = viewModel::clearError,
        onResetResults = viewModel::resetResults,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditorScreen(
    state: EditorUiState,
    onBack: () -> Unit,
    onPick: (List<Uri>) -> Unit,
    onRemove: (Uri) -> Unit,
    onOptions: ((com.videocompress.core.domain.model.ProcessOptions) -> com.videocompress.core.domain.model.ProcessOptions) -> Unit,
    onProcess: () -> Unit,
    onCancel: () -> Unit,
    onClearError: () -> Unit,
    onResetResults: () -> Unit,
) {
    val snackbar = remember { SnackbarHostState() }
    LaunchedEffect(state.errorMessage) {
        state.errorMessage?.let {
            snackbar.showSnackbar(it)
            onClearError()
        }
    }
    if (state.results.isNotEmpty()) {
        ResultScreen(results = state.results, onBack = onResetResults, onDone = onBack)
        return
    }

    val multiple = state.tool.allowsMultiple()
    val mime = if (state.tool.prefersGif()) {
        ActivityResultContracts.PickVisualMedia.ImageOnly
    } else {
        ActivityResultContracts.PickVisualMedia.VideoOnly
    }
    val singlePicker = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { onPick(listOf(it)) }
    }
    val multiPicker = rememberLauncherForActivityResult(ActivityResultContracts.PickMultipleVisualMedia(10)) { uris ->
        onPick(uris)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(state.tool.titleRes())) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background),
            )
        },
        snackbarHost = { SnackbarHost(snackbar) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Box(modifier = Modifier
            .fillMaxSize()
            .padding(padding)) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
            ) {
                Text(
                    text = stringResource(state.tool.subtitleRes()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (state.media.isEmpty()) {
                    EmptyState(
                        icon = Icons.Outlined.VideoLibrary,
                        title = stringResource(R.string.editor_empty_title),
                        message = stringResource(R.string.editor_empty_message),
                        modifier = Modifier.padding(top = 24.dp),
                    )
                    PrimaryButton(
                        text = stringResource(if (multiple) R.string.select_videos else R.string.select_video),
                        onClick = {
                            val request = PickVisualMediaRequest(mime)
                            if (multiple) multiPicker.launch(request) else singlePicker.launch(request)
                        },
                        modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
                    )
                } else {
                    MediaStrip(
                        mediaUris = state.media.map { it.uri },
                        onRemove = onRemove,
                        onAdd = if (multiple) {
                            { multiPicker.launch(PickVisualMediaRequest(mime)) }
                        } else null,
                    )
                    val first = state.media.first()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        com.videocompress.core.ui.components.StatCard(
                            label = stringResource(R.string.stat_size),
                            value = FileSizeFormatter.format(first.sizeBytes),
                            modifier = Modifier.weight(1f),
                        )
                        com.videocompress.core.ui.components.StatCard(
                            label = stringResource(R.string.stat_duration),
                            value = first.durationMs.toDurationLabel(),
                            modifier = Modifier.weight(1f),
                        )
                        if (first.width > 0) {
                            com.videocompress.core.ui.components.StatCard(
                                label = stringResource(R.string.stat_resolution),
                                value = "${first.width}×${first.height}",
                                modifier = Modifier.weight(1f),
                            )
                        }
                    }
                    OptionsPanel(state = state, onOptions = onOptions)
                    PrimaryButton(
                        text = stringResource(R.string.process),
                        onClick = onProcess,
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
                    )
                    SecondaryButton(
                        text = stringResource(R.string.change_videos),
                        onClick = {
                            val request = PickVisualMediaRequest(mime)
                            if (multiple) multiPicker.launch(request) else singlePicker.launch(request)
                        },
                        modifier = Modifier.padding(bottom = 28.dp),
                    )
                }
            }
            if (state.isResolving || state.isProcessing) {
                ProcessingOverlay(
                    progress = state.progress,
                    onCancel = if (state.isProcessing) onCancel else null,
                )
            }
        }
    }
}

@Composable
private fun MediaStrip(
    mediaUris: List<Uri>,
    onRemove: (Uri) -> Unit,
    onAdd: (() -> Unit)?,
) {
    val context = LocalContext.current
    LazyRow(
        contentPadding = PaddingValues(vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        items(mediaUris, key = { it.toString() }) { uri ->
            Box {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(uri)
                        .decoderFactory(VideoFrameDecoder.Factory())
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(width = 120.dp, height = 80.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                )
                IconButton(
                    onClick = { onRemove(uri) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(28.dp),
                ) {
                    Icon(Icons.Outlined.Close, contentDescription = stringResource(R.string.delete), tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        }
        if (onAdd != null) {
            item {
                IconButton(
                    onClick = onAdd,
                    modifier = Modifier
                        .height(80.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 16.dp),
                ) {
                    Icon(Icons.Outlined.Add, contentDescription = stringResource(R.string.select_videos))
                }
            }
        }
    }
}
