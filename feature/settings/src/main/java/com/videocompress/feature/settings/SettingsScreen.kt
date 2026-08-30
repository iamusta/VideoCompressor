package com.videocompress.feature.settings

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.HighQuality
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VideoSettings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.videocompress.core.common.CompressQuality
import com.videocompress.core.common.ThemeMode
import com.videocompress.core.common.VideoCodec
import com.videocompress.core.domain.model.AppSettings
import com.videocompress.core.resources.R
import com.videocompress.core.ui.locale.supportedLanguages

@Composable
fun SettingsRoute(
    viewModel: SettingsViewModel = hiltViewModel(),
) {
    val settings by viewModel.settings.collectAsStateWithLifecycle()
    SettingsScreen(
        settings = settings,
        onTheme = viewModel::setTheme,
        onLanguage = viewModel::setLanguage,
        onSaveGallery = viewModel::setSaveToGallery,
        onHaptic = viewModel::setHaptic,
        onQuality = viewModel::setQuality,
        onCodec = viewModel::setCodec,
    )
}

@Composable
fun SettingsScreen(
    settings: AppSettings,
    onTheme: (ThemeMode) -> Unit,
    onLanguage: (String) -> Unit,
    onSaveGallery: (Boolean) -> Unit,
    onHaptic: (Boolean) -> Unit,
    onQuality: (CompressQuality) -> Unit,
    onCodec: (VideoCodec) -> Unit,
) {
    val context = LocalContext.current
    var showTheme by remember { mutableStateOf(false) }
    var showLanguage by remember { mutableStateOf(false) }
    var showQuality by remember { mutableStateOf(false) }
    var showCodec by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item {
            Text(
                text = stringResource(R.string.nav_settings),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(top = 2.dp, bottom = 2.dp),
            )
        }
        item {
            SettingsGroup(stringResource(R.string.settings_section_app)) {
                SettingsNavRow(
                    icon = Icons.Outlined.DarkMode,
                    title = stringResource(R.string.settings_theme),
                    value = themeLabel(settings.themeMode),
                    onClick = { showTheme = true },
                    showDivider = true,
                )
                SettingsNavRow(
                    icon = Icons.Outlined.Language,
                    title = stringResource(R.string.settings_language),
                    value = languageLabel(settings.languageCode),
                    onClick = { showLanguage = true },
                    showDivider = false,
                )
            }
        }
        item {
            SettingsGroup(stringResource(R.string.settings_section_defaults)) {
                SettingsNavRow(
                    icon = Icons.Outlined.HighQuality,
                    title = stringResource(R.string.settings_default_quality),
                    value = qualityLabel(settings.defaultQuality),
                    onClick = { showQuality = true },
                    showDivider = true,
                )
                SettingsNavRow(
                    icon = Icons.Outlined.VideoSettings,
                    title = stringResource(R.string.settings_default_codec),
                    value = if (settings.defaultCodec == VideoCodec.H265) "H.265" else "H.264",
                    onClick = { showCodec = true },
                    showDivider = false,
                )
            }
        }
        item {
            SettingsGroup(stringResource(R.string.settings_section_privacy)) {
                SettingsSwitchRow(
                    title = stringResource(R.string.settings_save_to_gallery),
                    checked = settings.saveToGallery,
                    onChecked = onSaveGallery,
                    showDivider = true,
                )
                SettingsSwitchRow(
                    title = stringResource(R.string.settings_haptic_feedback),
                    checked = settings.hapticEnabled,
                    onChecked = onHaptic,
                    showDivider = false,
                )
            }
        }
        item {
            Text(
                text = stringResource(R.string.settings_privacy),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 4.dp),
            )
        }
        item {
            SettingsGroup(stringResource(R.string.settings_section_help)) {
                SettingsNavRow(
                    icon = Icons.Outlined.Share,
                    title = stringResource(R.string.settings_share_app),
                    value = stringResource(R.string.settings_share_app_desc),
                    onClick = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, context.getString(R.string.settings_share_message))
                        }
                        context.startActivity(Intent.createChooser(send, context.getString(R.string.settings_share_app)))
                    },
                    showDivider = false,
                )
            }
        }
        item {
            Text(
                text = stringResource(R.string.settings_version, "1.0.0"),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp, start = 4.dp),
            )
        }
    }

    if (showTheme) {
        ChoiceDialog(
            title = stringResource(R.string.settings_theme),
            options = ThemeMode.entries.map { it.name to themeLabel(it) },
            selected = settings.themeMode.name,
            onSelect = {
                onTheme(ThemeMode.valueOf(it))
                showTheme = false
            },
            onDismiss = { showTheme = false },
        )
    }
    if (showLanguage) {
        LanguageDialog(
            selected = settings.languageCode,
            onSelect = {
                onLanguage(it)
                showLanguage = false
            },
            onDismiss = { showLanguage = false },
        )
    }
    if (showQuality) {
        ChoiceDialog(
            title = stringResource(R.string.settings_default_quality),
            options = listOf(
                CompressQuality.LOW.name to stringResource(R.string.preset_low),
                CompressQuality.BALANCED.name to stringResource(R.string.preset_balanced),
                CompressQuality.HIGH.name to stringResource(R.string.preset_high),
            ),
            selected = settings.defaultQuality.name,
            onSelect = {
                onQuality(CompressQuality.valueOf(it))
                showQuality = false
            },
            onDismiss = { showQuality = false },
        )
    }
    if (showCodec) {
        ChoiceDialog(
            title = stringResource(R.string.settings_default_codec),
            options = listOf(VideoCodec.H264.name to "H.264", VideoCodec.H265.name to "H.265"),
            selected = settings.defaultCodec.name,
            onSelect = {
                onCodec(VideoCodec.valueOf(it))
                showCodec = false
            },
            onDismiss = { showCodec = false },
        )
    }
}

@Composable
private fun SettingsGroup(
    title: String,
    content: @Composable () -> Unit,
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface),
        ) {
            content()
        }
    }
}

@Composable
private fun SettingsNavRow(
    icon: ImageVector,
    title: String,
    value: String,
    onClick: () -> Unit,
    showDivider: Boolean,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .clickable(onClick = onClick)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp),
            )
            Text(
                title,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .padding(start = 12.dp)
                    .weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                value,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.padding(end = 2.dp),
            )
            Icon(
                Icons.AutoMirrored.Outlined.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 44.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    checked: Boolean,
    onChecked: (Boolean) -> Unit,
    showDivider: Boolean,
) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
            Switch(checked = checked, onCheckedChange = onChecked)
        }
        if (showDivider) {
            HorizontalDivider(
                modifier = Modifier.padding(start = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant,
            )
        }
    }
}

@Composable
private fun ChoiceDialog(
    title: String,
    options: List<Pair<String, String>>,
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column {
                options.forEach { (value, label) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(value) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = value == selected, onClick = { onSelect(value) })
                        Text(label, modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
private fun LanguageDialog(
    selected: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.settings_language)) },
        text = {
            LazyColumn {
                items(supportedLanguages, key = { it.code }) { language ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(language.code) }
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        RadioButton(selected = language.code == selected, onClick = { onSelect(language.code) })
                        Text(stringResource(language.nameRes), modifier = Modifier.padding(start = 8.dp))
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel)) }
        },
    )
}

@Composable
private fun themeLabel(mode: ThemeMode): String = when (mode) {
    ThemeMode.SYSTEM -> stringResource(R.string.theme_system)
    ThemeMode.LIGHT -> stringResource(R.string.theme_light)
    ThemeMode.DARK -> stringResource(R.string.theme_dark)
}

@Composable
private fun qualityLabel(quality: CompressQuality): String = when (quality) {
    CompressQuality.LOW -> stringResource(R.string.preset_low)
    CompressQuality.BALANCED -> stringResource(R.string.preset_balanced)
    CompressQuality.HIGH, CompressQuality.CUSTOM -> stringResource(R.string.preset_high)
}

@Composable
private fun languageLabel(code: String): String {
    val match = supportedLanguages.firstOrNull { it.code == code } ?: supportedLanguages.first()
    return stringResource(match.nameRes)
}
