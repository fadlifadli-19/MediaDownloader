package com.mediadownloader.presentation.state

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
    data class NavigateTo(val route: String) : UiEvent()
    data object NavigateBack : UiEvent()
    data class ShowError(val message: String) : UiEvent()
    data class DownloadStarted(val downloadId: String) : UiEvent()
    data class OpenFile(val path: String) : UiEvent()
    data class ShareFile(val path: String) : UiEvent()
}
