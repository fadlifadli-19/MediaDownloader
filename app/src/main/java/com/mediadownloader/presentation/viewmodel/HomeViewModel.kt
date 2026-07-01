package com.mediadownloader.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediadownloader.core.result.Resource
import com.mediadownloader.domain.model.Download
import com.mediadownloader.domain.model.DownloadStatus
import com.mediadownloader.domain.model.MediaInfo
import com.mediadownloader.domain.repository.DownloadRepository
import com.mediadownloader.domain.usecase.AnalyzeUrlUseCase
import com.mediadownloader.domain.usecase.DownloadMediaUseCase
import com.mediadownloader.presentation.state.HomeState
import com.mediadownloader.presentation.state.UiEvent
import com.mediadownloader.presentation.state.UiState
import com.mediadownloader.util.FileHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val analyzeUrlUseCase: AnalyzeUrlUseCase,
    private val downloadMediaUseCase: DownloadMediaUseCase,
    private val downloadRepository: DownloadRepository,
    private val fileHelper: FileHelper
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state.asStateFlow()

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val recentDownloads: StateFlow<List<Download>> = downloadRepository
        .getDownloadsByStatus(DownloadStatus.COMPLETED)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun onUrlChanged(url: String) {
        _state.update { it.copy(urlInput = url) }
    }

    fun analyzeUrl() {
        val url = _state.value.urlInput.trim()
        if (url.isBlank()) {
            viewModelScope.launch { _events.send(UiEvent.ShowSnackbar("Please enter a URL")) }
            return
        }
        viewModelScope.launch {
            _state.update { it.copy(analyzeState = UiState.Loading, isAnalyzing = true) }
            when (val result = analyzeUrlUseCase(url)) {
                is Resource.Success -> _state.update {
                    it.copy(analyzeState = UiState.Success(result.data), isAnalyzing = false)
                }
                is Resource.Error -> {
                    _state.update { it.copy(analyzeState = UiState.Error(result.message), isAnalyzing = false) }
                    _events.send(UiEvent.ShowSnackbar(result.message))
                }
                is Resource.Loading -> Unit
            }
        }
    }

    fun startDownload(mediaInfo: MediaInfo, formatId: String, quality: String) {
        viewModelScope.launch {
            val outputPath = fileHelper.getDefaultDownloadDir().absolutePath
            when (val result = downloadMediaUseCase(mediaInfo, formatId, quality, outputPath)) {
                is Resource.Success -> {
                    _state.update { it.copy(analyzeState = UiState.Idle, urlInput = "") }
                    _events.send(UiEvent.DownloadStarted(result.data))
                    _events.send(UiEvent.ShowSnackbar("Download queued"))
                }
                is Resource.Error -> _events.send(UiEvent.ShowSnackbar(result.message))
                is Resource.Loading -> Unit
            }
        }
    }

    fun dismissAnalysis() {
        _state.update { it.copy(analyzeState = UiState.Idle) }
    }

    fun setSharedUrl(url: String) {
        _state.update { it.copy(urlInput = url, sharedUrl = url) }
    }
}
