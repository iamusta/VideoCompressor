package com.videocompress.core.data.settings

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.videocompress.core.common.CompressQuality
import com.videocompress.core.common.ContentScale
import com.videocompress.core.common.ThemeMode
import com.videocompress.core.common.VideoCodec
import com.videocompress.core.domain.model.AppSettings
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsStore by preferencesDataStore(name = "app_settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val theme = stringPreferencesKey("theme")
    private val language = stringPreferencesKey("language")
    private val saveGallery = booleanPreferencesKey("save_gallery")
    private val haptic = booleanPreferencesKey("haptic")
    private val scale = stringPreferencesKey("content_scale")
    private val quality = stringPreferencesKey("default_quality")
    private val codec = stringPreferencesKey("default_codec")

    val settings: Flow<AppSettings> = context.settingsStore.data.map { prefs ->
        AppSettings(
            themeMode = prefs[theme]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() } ?: ThemeMode.SYSTEM,
            languageCode = prefs[language] ?: "system",
            saveToGallery = prefs[saveGallery] ?: true,
            hapticEnabled = prefs[haptic] ?: true,
            contentScale = prefs[scale]?.let { runCatching { ContentScale.valueOf(it) }.getOrNull() } ?: ContentScale.NORMAL,
            defaultQuality = prefs[quality]?.let { runCatching { CompressQuality.valueOf(it) }.getOrNull() } ?: CompressQuality.BALANCED,
            defaultCodec = prefs[codec]?.let { runCatching { VideoCodec.valueOf(it) }.getOrNull() } ?: VideoCodec.H264,
        )
    }

    suspend fun current(): AppSettings = settings.first()

    suspend fun update(transform: (AppSettings) -> AppSettings) {
        val next = transform(current())
        context.settingsStore.edit { prefs ->
            prefs[theme] = next.themeMode.name
            prefs[language] = next.languageCode
            prefs[saveGallery] = next.saveToGallery
            prefs[haptic] = next.hapticEnabled
            prefs[scale] = next.contentScale.name
            prefs[quality] = next.defaultQuality.name
            prefs[codec] = next.defaultCodec.name
        }
    }
}
