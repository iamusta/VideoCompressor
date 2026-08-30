package com.videocompress.app

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.videocompress.core.domain.model.AppSettings
import com.videocompress.core.domain.usecase.ObserveSettingsUseCase
import com.videocompress.core.ui.locale.LocaleHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    observeSettings: ObserveSettingsUseCase,
) : ViewModel() {

    val settings: StateFlow<AppSettings> = observeSettings()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AppSettings())

    private val _incomingUris = MutableStateFlow<List<Uri>>(emptyList())
    val incomingUris: StateFlow<List<Uri>> = _incomingUris.asStateFlow()

    init {
        val language = settings.value.languageCode
        if (language != "system") {
            LocaleHelper.applyLanguage(language)
        }
    }

    fun setIncomingVideos(uris: List<Uri>) {
        _incomingUris.value = uris
    }

    fun consumeIncoming() {
        _incomingUris.value = emptyList()
    }
}
