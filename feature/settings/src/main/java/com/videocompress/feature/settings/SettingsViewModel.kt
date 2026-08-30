package com.videocompress.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.videocompress.core.common.CompressQuality
import com.videocompress.core.common.ThemeMode
import com.videocompress.core.common.VideoCodec
import com.videocompress.core.domain.model.AppSettings
import com.videocompress.core.domain.usecase.ObserveSettingsUseCase
import com.videocompress.core.domain.usecase.UpdateSettingsUseCase
import com.videocompress.core.ui.locale.LocaleHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    observeSettings: ObserveSettingsUseCase,
    private val updateSettings: UpdateSettingsUseCase,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    fun setTheme(mode: ThemeMode) = update { it.copy(themeMode = mode) }

    fun setLanguage(code: String) {
        LocaleHelper.applyLanguage(code)
        update { it.copy(languageCode = code) }
    }

    fun setSaveToGallery(enabled: Boolean) = update { it.copy(saveToGallery = enabled) }

    fun setHaptic(enabled: Boolean) = update { it.copy(hapticEnabled = enabled) }

    fun setQuality(quality: CompressQuality) = update { it.copy(defaultQuality = quality) }

    fun setCodec(codec: VideoCodec) = update { it.copy(defaultCodec = codec) }

    private fun update(transform: (AppSettings) -> AppSettings) {
        viewModelScope.launch { updateSettings(transform) }
    }
}
