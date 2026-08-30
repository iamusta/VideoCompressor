package com.videocompress.feature.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.videocompress.core.common.VideoTool
import com.videocompress.core.resources.R
import com.videocompress.core.ui.components.FeatureGridCard
import com.videocompress.core.ui.util.titleRes
import com.videocompress.core.ui.util.visual

private val primaryTools = listOf(
    VideoTool.COMPRESS,
    VideoTool.CONVERT,
    VideoTool.EXTRACT_AUDIO,
    VideoTool.VIDEO_TO_GIF,
)

private val editTools = listOf(
    VideoTool.TRIM,
    VideoTool.CROP,
    VideoTool.ROTATE,
    VideoTool.SPEED,
    VideoTool.VOLUME,
    VideoTool.REVERSE,
    VideoTool.LOOP,
    VideoTool.MERGE,
    VideoTool.SOCIAL_RESIZE,
    VideoTool.GIF_TO_VIDEO,
)

@Composable
fun HomeScreen(
    onOpenTool: (VideoTool) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Column(modifier = Modifier.padding(bottom = 8.dp, top = 4.dp)) {
                Text(
                    text = stringResource(R.string.home_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = stringResource(R.string.home_subtitle),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 6.dp),
                )
            }
        }
        item {
            Text(
                text = stringResource(R.string.home_section_essentials),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 4.dp, bottom = 2.dp),
            )
        }
        items(primaryTools.chunked(2)) { row ->
            ToolRow(row, onOpenTool)
        }
        item {
            Text(
                text = stringResource(R.string.home_section_edit),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 12.dp, bottom = 2.dp),
            )
        }
        items(editTools.chunked(2)) { row ->
            ToolRow(row, onOpenTool)
        }
    }
}

@Composable
private fun ToolRow(tools: List<VideoTool>, onOpenTool: (VideoTool) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        tools.forEach { tool ->
            val visual = tool.visual()
            FeatureGridCard(
                title = stringResource(tool.titleRes()),
                icon = visual.icon,
                iconTint = visual.tint,
                iconBackgroundColor = visual.background,
                onClick = { onOpenTool(tool) },
                modifier = Modifier.weight(1f),
            )
        }
        if (tools.size == 1) Spacer(Modifier.weight(1f))
    }
}
