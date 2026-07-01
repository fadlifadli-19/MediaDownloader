package com.mediadownloader.domain.model

import java.util.UUID

enum class DownloadStatus {
    QUEUED, DOWNLOADING, PAUSED, COMPLETED, FAILED, CANCELLED
}

enum class MediaType {
    VIDEO, AUDIO, PLAYLIST, UNKNOWN
}

data class DownloadProgress(
    val percentage: Float = 0f,
    val speed: String = "",
    val eta: String = "",
    val downloadedBytes: Long = 0L,
    val totalBytes: Long = 0L
)

data class Download(
    val id: String = UUID.randomUUID().toString(),
    val url: String,
    val title: String,
    val thumbnail: String = "",
    val outputPath: String = "",
    val fileName: String = "",
    val format: String = "",
    val quality: String = "",
    val mediaType: MediaType = MediaType.VIDEO,
    val status: DownloadStatus = DownloadStatus.QUEUED,
    val progress: DownloadProgress = DownloadProgress(),
    val fileSize: Long = 0L,
    val duration: Long = 0L,
    val errorMessage: String = "",
    val workerId: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long = 0L
)
