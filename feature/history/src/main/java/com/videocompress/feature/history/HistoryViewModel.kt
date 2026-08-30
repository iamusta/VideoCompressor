package com.videocompress.feature.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.videocompress.core.domain.model.HistoryItem
import com.videocompress.core.domain.usecase.DeleteHistoryUseCase
import com.videocompress.core.domain.usecase.ObserveHistoryUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    observeHistory: ObserveHistoryUseCase,
    private val deleteHistory: DeleteHistoryUseCase,
) : ViewModel() {

    val items: StateFlow<List<HistoryItem>> = observeHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(id: Long) {
        viewModelScope.launch { deleteHistory(id) }
    }

    fun clear() {
        viewModelScope.launch { deleteHistory.clear() }
    }
}
