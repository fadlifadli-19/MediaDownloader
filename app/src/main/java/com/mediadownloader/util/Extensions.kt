package com.mediadownloader.util

import com.mediadownloader.data.local.entity.DownloadEntity
import com.mediadownloader.data.local.entity.UrlEntity
import com.mediadownloader.domain.model.Download
import com.mediadownloader.domain.model.DownloadProgress
import com.mediadownloader.domain.model.DownloadStatus
import com.mediadownloader.domain.model.MediaType
import com.mediadownloader.domain.model.Url

fun DownloadEntity.toDomain(): Download = Download(
    id = id,
    url = url,
    title = title,
    thumbnail = thumbnail,
    outputPath = outputPath,
    fileName = fileName,
    format = format,
    quality = quality,
    mediaType = runCatching { MediaType.valueOf(mediaType) }.getOrDefault(MediaType.UNKNOWN),
    status = runCatching { DownloadStatus.valueOf(status) }.getOrDefault(DownloadStatus.QUEUED),
    progress = DownloadProgress(
        percentage = progressPercentage,
        speed = progressSpeed,
        eta = progressEta
    ),
    fileSize = fileSize,
    duration = duration,
    errorMessage = errorMessage,
    workerId = workerId,
    createdAt = createdAt,
    completedAt = completedAt
)

fun Download.toEntity(): DownloadEntity = DownloadEntity(
    id = id,
    url = url,
    title = title,
    thumbnail = thumbnail,
    outputPath = outputPath,
    fileName = fileName,
    format = format,
    quality = quality,
    mediaType = mediaType.name,
    status = status.name,
    progressPercentage = progress.percentage,
    progressSpeed = progress.speed,
    progressEta = progress.eta,
    fileSize = fileSize,
    duration = duration,
    errorMessage = errorMessage,
    workerId = workerId,
    createdAt = createdAt,
    completedAt = completedAt
)

fun UrlEntity.toDomain(): Url = Url(
    id = id,
    rawUrl = rawUrl,
    normalizedUrl = normalizedUrl,
    title = title,
    thumbnail = thumbnail,
    platform = platform,
    lastUsed = lastUsed
)

fun Url.toEntity(): UrlEntity = UrlEntity(
    id = id,
    rawUrl = rawUrl,
    normalizedUrl = normalizedUrl,
    title = title,
    thumbnail = thumbnail,
    platform = platform,
    lastUsed = lastUsed
)

fun String.normalizeUrl(): String = trim().trimEnd('/')

fun Long.formatDuration(): String {
    val hours = this / 3600
    val minutes = (this % 3600) / 60
    val seconds = this % 60
    return if (hours > 0) "%d:%02d:%02d".format(hours, minutes, seconds)
    else "%d:%02d".format(minutes, seconds)
}
