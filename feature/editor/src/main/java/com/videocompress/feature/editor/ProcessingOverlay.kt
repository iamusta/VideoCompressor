package com.videocompress.feature.editor

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.videocompress.core.domain.model.ProcessProgress
import com.videocompress.core.resources.R

@Composable
fun ProcessingOverlay(
    progress: ProcessProgress?,
    onCancel: (() -> Unit)?,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background.copy(alpha = 0.92f))
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(R.string.processing),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = if (progress != null) {
                    stringResource(R.string.progress_count, progress.currentIndex, progress.total)
                } else {
                    stringResource(R.string.processing_prepare)
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 8.dp, bottom = 16.dp),
            )
            LinearProgressIndicator(
                progress = { progress?.fraction ?: 0f },
                modifier = Modifier.fillMaxWidth(),
            )
            if (onCancel != null) {
                TextButton(onClick = onCancel, modifier = Modifier.padding(top = 8.dp)) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}
