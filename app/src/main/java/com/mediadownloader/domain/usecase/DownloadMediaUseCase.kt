package com.mediadownloader.domain.usecase

import com.mediadownloader.core.result.Resource
import com.mediadownloader.domain.model.Download
import com.mediadownloader.domain.model.DownloadStatus
import com.mediadownloader.domain.model.MediaInfo
import com.mediadownloader.domain.repository.DownloadRepository
import java.util.UUID
import javax.inject.Inject

class DownloadMediaUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository
) {
    suspend operator fun invoke(
        mediaInfo: MediaInfo,
        formatId: String,
        quality: String,
        outputPath: String
    ): Resource<String> {
        val download = Download(
            id = UUID.randomUUID().toString(),
            url = mediaInfo.url,
            title = mediaInfo.title,
            thumbnail = mediaInfo.thumbnail,
            outputPath = outputPath,
            format = formatId,
            quality = quality,
            status = DownloadStatus.QUEUED,
            fileSize = mediaInfo.filesize,
            duration = mediaInfo.duration,
            createdAt = System.currentTimeMillis()
        )
        return downloadRepository.insertDownload(download)
    }

    suspend fun cancelDownload(id: String): Resource<Unit> =
        downloadRepository.updateStatus(id, DownloadStatus.CANCELLED)

    suspend fun retryDownload(download: Download): Resource<String> {
        val retried = download.copy(
            id = UUID.randomUUID().toString(),
            status = DownloadStatus.QUEUED,
            createdAt = System.currentTimeMillis(),
            completedAt = 0L,
            errorMessage = ""
        )
        return downloadRepository.insertDownload(retried)
    }
}
