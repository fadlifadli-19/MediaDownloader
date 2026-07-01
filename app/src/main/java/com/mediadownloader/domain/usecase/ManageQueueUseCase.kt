package com.mediadownloader.domain.usecase

import com.mediadownloader.core.result.Resource
import com.mediadownloader.domain.model.Download
import com.mediadownloader.domain.model.DownloadStatus
import com.mediadownloader.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ManageQueueUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository
) {
    fun getQueue(): Flow<List<Download>> =
        downloadRepository.getDownloadsByStatus(DownloadStatus.QUEUED)

    fun getActiveDownloads(): Flow<List<Download>> =
        downloadRepository.getDownloadsByStatus(DownloadStatus.DOWNLOADING)

    suspend fun pauseDownload(id: String): Resource<Unit> =
        downloadRepository.updateStatus(id, DownloadStatus.PAUSED)

    suspend fun resumeDownload(id: String): Resource<Unit> =
        downloadRepository.updateStatus(id, DownloadStatus.QUEUED)

    suspend fun cancelDownload(id: String): Resource<Unit> =
        downloadRepository.updateStatus(id, DownloadStatus.CANCELLED)

    suspend fun removeFromQueue(id: String): Resource<Unit> =
        downloadRepository.deleteDownload(id)

    suspend fun getActiveCount(): Int =
        downloadRepository.getActiveDownloadCount()
}
