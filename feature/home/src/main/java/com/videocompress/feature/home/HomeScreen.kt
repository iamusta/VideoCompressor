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
import com.videocompress.core.ui.components.FeatureHeroCard
import com.videocompress.core.ui.components.FeatureSpotlightCard
import com.videocompress.core.ui.util.subtitleRes
import com.videocompress.core.ui.util.titleRes
import com.videocompress.core.ui.util.visual

private val essentialTools = listOf(
    VideoTool.CONVERT,
    VideoTool.EXTRACT_AUDIO,
    VideoTool.VIDEO_TO_GIF,
    VideoTool.MERGE,
)

private val editTools = listOf(
    VideoTool.TRIM,
    VideoTool.CROP,
    VideoTool.ROTATE,
    VideoTool.SPEED,
    VideoTool.VOLUME,
    VideoTool.REVERSE,
    VideoTool.LOOP,
    VideoTool.SOCIAL_RESIZE,
    VideoTool.GIF_TO_VIDEO,
)

@Composable
fun HomeScreen(
    onOpenTool: (VideoTool) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(start = 20.dp, end = 20.dp, top = 8.dp, bottom = 28.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        item {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    text = stringResource(R.string.home_title),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                )
                Text(
                    text = stringResource(R.string.home_subtitle),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        item {
            val visual = VideoTool.COMPRESS.visual()
            FeatureHeroCard(
                title = stringResource(VideoTool.COMPRESS.titleRes()),
                subtitle = stringResource(VideoTool.COMPRESS.subtitleRes()),
                action = stringResource(R.string.home_hero_cta),
                badge = stringResource(R.string.home_on_device),
                icon = visual.icon,
                onClick = { onOpenTool(VideoTool.COMPRESS) },
            )
        }
        item {
            SectionLabel(stringResource(R.string.home_section_essentials))
        }
        items(essentialTools.chunked(2)) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                row.forEach { tool ->
                    val visual = tool.visual()
                    FeatureSpotlightCard(
                        title = stringResource(tool.titleRes()),
                        subtitle = stringResource(tool.subtitleRes()),
                        icon = visual.icon,
                        iconTint = visual.tint,
                        iconBackgroundColor = visual.background,
                        onClick = { onOpenTool(tool) },
                        modifier = Modifier.weight(1f),
                    )
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        item {
            SectionLabel(stringResource(R.string.home_section_edit))
        }
        items(editTools.chunked(3)) { row ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                row.forEach { tool ->
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
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 4.dp),
    )
}
