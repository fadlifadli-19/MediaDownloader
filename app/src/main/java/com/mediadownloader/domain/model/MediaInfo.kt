package com.mediadownloader.domain.model

data class VideoFormat(
    val formatId: String,
    val formatNote: String,
    val ext: String,
    val resolution: String,
    val fps: Int,
    val filesize: Long,
    val tbr: Double,
    val vcodec: String,
    val acodec: String,
    val hasVideo: Boolean,
    val hasAudio: Boolean
)

data class MediaInfo(
    val id: String,
    val url: String,
    val title: String,
    val uploader: String,
    val uploaderUrl: String,
    val thumbnail: String,
    val duration: Long,
    val uploadDate: String,
    val platform: String,
    val description: String,
    val filesize: Long,
    val channel: String,
    val channelUrl: String,
    val viewCount: Long,
    val likeCount: Long,
    val formats: List<VideoFormat>,
    val isPlaylist: Boolean,
    val playlistCount: Int,
    val playlistTitle: String
)
