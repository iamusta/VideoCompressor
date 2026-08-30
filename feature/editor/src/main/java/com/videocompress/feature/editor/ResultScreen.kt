package com.videocompress.feature.editor

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.videocompress.core.common.FileSizeFormatter
import com.videocompress.core.domain.model.ProcessResult
import com.videocompress.core.resources.R
import com.videocompress.core.ui.components.PrimaryButton
import com.videocompress.core.ui.components.SecondaryButton
import com.videocompress.core.ui.components.StatCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    results: List<ProcessResult>,
    onBack: () -> Unit,
    onDone: () -> Unit,
) {
    val context = LocalContext.current
    val first = results.first()
    val saved = FileSizeFormatter.percentSaved(first.originalSizeBytes, first.sizeBytes)
    Scaffold(
        topBar = { TopAppBar(title = { Text(stringResource(R.string.result_title)) }) },
        containerColor = MaterialTheme.colorScheme.background,
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Icon(
                Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 12.dp, bottom = 12.dp),
            )
            Text(
                text = stringResource(R.string.result_ready),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                text = first.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp),
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                StatCard(
                    label = stringResource(R.string.comparison_original_label),
                    value = FileSizeFormatter.format(first.originalSizeBytes),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = stringResource(R.string.comparison_result_label),
                    value = FileSizeFormatter.format(first.sizeBytes),
                    modifier = Modifier.weight(1f),
                )
                StatCard(
                    label = stringResource(R.string.comparison_saved),
                    value = "$saved%",
                    modifier = Modifier.weight(1f),
                )
            }
            PrimaryButton(
                text = stringResource(R.string.share),
                onClick = { share(context, first.outputUri, first.mimeType) },
                modifier = Modifier.padding(top = 28.dp),
            )
            SecondaryButton(
                text = stringResource(R.string.done),
                onClick = onDone,
                modifier = Modifier.padding(top = 10.dp),
            )
            SecondaryButton(
                text = stringResource(R.string.process_another),
                onClick = onBack,
                modifier = Modifier.padding(top = 10.dp, bottom = 20.dp),
            )
        }
    }
}

private fun share(context: android.content.Context, uri: Uri, mime: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mime
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.share)))
}
