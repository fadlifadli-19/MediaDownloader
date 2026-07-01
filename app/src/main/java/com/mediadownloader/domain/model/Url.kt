package com.mediadownloader.domain.model

data class Url(
    val id: Long = 0,
    val rawUrl: String,
    val normalizedUrl: String,
    val title: String = "",
    val thumbnail: String = "",
    val platform: String = "",
    val lastUsed: Long = System.currentTimeMillis()
)
