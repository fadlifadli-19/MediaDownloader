package com.mediadownloader.domain.usecase

import com.mediadownloader.domain.model.Download
import com.mediadownloader.domain.model.DownloadStatus
import com.mediadownloader.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetHistoryUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository
) {
    fun getAllHistory(): Flow<List<Download>> = downloadRepository.getAllDownloads()

    fun getCompletedHistory(): Flow<List<Download>> =
        downloadRepository.getDownloadsByStatus(DownloadStatus.COMPLETED)

    fun getFailedHistory(): Flow<List<Download>> =
        downloadRepository.getDownloadsByStatus(DownloadStatus.FAILED)

    fun searchHistory(query: String): Flow<List<Download>> =
        downloadRepository.searchDownloads(query)

    suspend fun clearCompleted() = downloadRepository.deleteAllCompleted()
}
