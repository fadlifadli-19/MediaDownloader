package com.mediadownloader.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediadownloader.domain.model.Download
import com.mediadownloader.domain.repository.DownloadRepository
import com.mediadownloader.domain.usecase.DownloadMediaUseCase
import com.mediadownloader.domain.usecase.GetHistoryUseCase
import com.mediadownloader.presentation.state.UiEvent
import com.mediadownloader.util.FileHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getHistoryUseCase: GetHistoryUseCase,
    private val downloadRepository: DownloadRepository,
    private val downloadMediaUseCase: DownloadMediaUseCase,
    private val fileHelper: FileHelper
) : ViewModel() {

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val history: StateFlow<List<Download>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) getHistoryUseCase.getAllHistory()
            else getHistoryUseCase.searchHistory(query)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onSearchChanged(query: String) {
        _searchQuery.value = query
    }

    fun deleteDownload(id: String) {
        viewModelScope.launch {
            downloadRepository.deleteDownload(id)
            _events.send(UiEvent.ShowSnackbar("Removed from history"))
        }
    }

    fun clearCompleted() {
        viewModelScope.launch {
            getHistoryUseCase.clearCompleted()
            _events.send(UiEvent.ShowSnackbar("History cleared"))
        }
    }

    fun openFile(download: Download) {
        viewModelScope.launch {
            if (download.outputPath.isNotBlank() && fileHelper.fileExists(download.outputPath)) {
                _events.send(UiEvent.OpenFile(download.outputPath))
            } else {
                _events.send(UiEvent.ShowSnackbar("File not found"))
            }
        }
    }

    fun shareFile(download: Download) {
        viewModelScope.launch {
            if (download.outputPath.isNotBlank() && fileHelper.fileExists(download.outputPath)) {
                _events.send(UiEvent.ShareFile(download.outputPath))
            } else {
                _events.send(UiEvent.ShowSnackbar("File not found"))
            }
        }
    }
}
