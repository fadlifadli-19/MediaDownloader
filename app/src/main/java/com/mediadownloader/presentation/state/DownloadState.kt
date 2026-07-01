package com.mediadownloader.presentation.state

import com.mediadownloader.domain.model.Download
import com.mediadownloader.domain.model.DownloadStatus

data class DownloadState(
    val activeDownloads: List<Download> = emptyList(),
    val queuedDownloads: List<Download> = emptyList(),
    val pausedDownloads: List<Download> = emptyList(),
    val isLoading: Boolean = false
) {
    val allVisible: List<Download>
        get() = (activeDownloads + queuedDownloads + pausedDownloads)
            .sortedByDescending { it.createdAt }

    val isEmpty: Boolean get() = allVisible.isEmpty()
}
