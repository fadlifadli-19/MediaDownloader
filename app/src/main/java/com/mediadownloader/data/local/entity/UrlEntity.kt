package com.mediadownloader.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "urls")
data class UrlEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "raw_url")
    val rawUrl: String,

    @ColumnInfo(name = "normalized_url")
    val normalizedUrl: String,

    @ColumnInfo(name = "title")
    val title: String = "",

    @ColumnInfo(name = "thumbnail")
    val thumbnail: String = "",

    @ColumnInfo(name = "platform")
    val platform: String = "",

    @ColumnInfo(name = "last_used")
    val lastUsed: Long = System.currentTimeMillis()
)
