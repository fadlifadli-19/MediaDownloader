package com.mediadownloader.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.mediadownloader.domain.model.DownloadStatus
import com.mediadownloader.domain.model.MediaType

@Entity(tableName = "downloads")
data class DownloadEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "url")
    val url: String,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "thumbnail")
    val thumbnail: String = "",

    @ColumnInfo(name = "output_path")
    val outputPath: String = "",

    @ColumnInfo(name = "file_name")
    val fileName: String = "",

    @ColumnInfo(name = "format")
    val format: String = "",

    @ColumnInfo(name = "quality")
    val quality: String = "",

    @ColumnInfo(name = "media_type")
    val mediaType: String = MediaType.VIDEO.name,

    @ColumnInfo(name = "status")
    val status: String = DownloadStatus.QUEUED.name,

    @ColumnInfo(name = "progress_percentage")
    val progressPercentage: Float = 0f,

    @ColumnInfo(name = "progress_speed")
    val progressSpeed: String = "",

    @ColumnInfo(name = "progress_eta")
    val progressEta: String = "",

    @ColumnInfo(name = "file_size")
    val fileSize: Long = 0L,

    @ColumnInfo(name = "duration")
    val duration: Long = 0L,

    @ColumnInfo(name = "error_message")
    val errorMessage: String = "",

    @ColumnInfo(name = "worker_id")
    val workerId: String = "",

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),

    @ColumnInfo(name = "completed_at")
    val completedAt: Long = 0L
)
