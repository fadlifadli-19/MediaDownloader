package com.mediadownloader.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.mediadownloader.domain.model.Download
import com.mediadownloader.domain.model.DownloadStatus
import com.mediadownloader.domain.repository.DownloadRepository
import com.mediadownloader.domain.usecase.ManageQueueUseCase
import com.mediadownloader.presentation.state.DownloadState
import com.mediadownloader.presentation.state.UiEvent
import com.mediadownloader.service.worker.DownloadWorker
import com.mediadownloader.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DownloadViewModel @Inject constructor(
    private val downloadRepository: DownloadRepository,
    private val manageQueueUseCase: ManageQueueUseCase,
    private val workManager: WorkManager
) : ViewModel() {

    private val _events = Channel<UiEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    val downloadState: StateFlow<DownloadState> = combine(
        downloadRepository.getDownloadsByStatus(DownloadStatus.DOWNLOADING),
        downloadRepository.getDownloadsByStatus(DownloadStatus.QUEUED),
        downloadRepository.getDownloadsByStatus(DownloadStatus.PAUSED)
    ) { active, queued, paused ->
        DownloadState(
            activeDownloads = active,
            queuedDownloads = queued,
            pausedDownloads = paused,
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        DownloadState()
    )

    fun enqueuePendingDownloads() {
        viewModelScope.launch {
            val queued = downloadRepository.getDownloadsByStatus(DownloadStatus.QUEUED)
            queued.collect { list ->
                list.take(Constants.DEFAULT_MAX_CONCURRENT_DOWNLOADS).forEach { download ->
                    scheduleWorker(download)
                }
                return@collect
            }
        }
    }

    fun scheduleWorker(download: Download) {
        val data = Data.Builder()
            .putString(Constants.WORKER_INPUT_DOWNLOAD_ID, download.id)
            .putString(Constants.WORKER_INPUT_URL, download.url)
            .putString(Constants.WORKER_INPUT_FORMAT, download.format)
            .putString(Constants.WORKER_INPUT_QUALITY, download.quality)
            .putString(Constants.WORKER_INPUT_OUTPUT_PATH, download.outputPath)
            .build()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<DownloadWorker>()
            .setInputData(data)
            .setConstraints(constraints)
            .addTag(Constants.WORKER_TAG_DOWNLOAD)
            .addTag("${Constants.WORKER_TAG_DOWNLOAD}_${download.id}")
            .build()

        workManager.enqueue(request)
    }

    fun cancelDownload(id: String) {
        viewModelScope.launch {
            manageQueueUseCase.cancelDownload(id)
            workManager.cancelAllWorkByTag("${Constants.WORKER_TAG_DOWNLOAD}_$id")
            _events.send(UiEvent.ShowSnackbar("Download cancelled"))
        }
    }

    fun pauseDownload(id: String) {
        viewModelScope.launch {
            manageQueueUseCase.pauseDownload(id)
            workManager.cancelAllWorkByTag("${Constants.WORKER_TAG_DOWNLOAD}_$id")
            _events.send(UiEvent.ShowSnackbar("Download paused"))
        }
    }

    fun resumeDownload(download: Download) {
        viewModelScope.launch {
            manageQueueUseCase.resumeDownload(download.id)
            scheduleWorker(download)
            _events.send(UiEvent.ShowSnackbar("Download resumed"))
        }
    }
}
