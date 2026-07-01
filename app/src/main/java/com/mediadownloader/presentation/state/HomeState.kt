package com.mediadownloader.presentation.state

import com.mediadownloader.domain.model.Download
import com.mediadownloader.domain.model.MediaInfo

data class HomeState(
    val urlInput: String = "",
    val analyzeState: UiState<MediaInfo> = UiState.Idle,
    val recentDownloads: List<Download> = emptyList(),
    val isAnalyzing: Boolean = false,
    val sharedUrl: String = ""
)
