package com.mediadownloader.domain.repository

import com.mediadownloader.core.result.Resource
import com.mediadownloader.domain.model.Download
import com.mediadownloader.domain.model.DownloadStatus
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    fun getAllDownloads(): Flow<List<Download>>
    fun getDownloadsByStatus(status: DownloadStatus): Flow<List<Download>>
    fun getDownloadById(id: String): Flow<Download?>
    suspend fun insertDownload(download: Download): Resource<String>
    suspend fun updateDownload(download: Download): Resource<Unit>
    suspend fun updateStatus(id: String, status: DownloadStatus): Resource<Unit>
    suspend fun updateProgress(
        id: String,
        percentage: Float,
        speed: String,
        eta: String
    ): Resource<Unit>
    suspend fun deleteDownload(id: String): Resource<Unit>
    suspend fun deleteAllCompleted(): Resource<Unit>
    fun searchDownloads(query: String): Flow<List<Download>>
    suspend fun getActiveDownloadCount(): Int
}
