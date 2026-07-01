package com.mediadownloader.util

object Constants {
    const val NOTIFICATION_CHANNEL_ID = "download_channel"
    const val NOTIFICATION_CHANNEL_NAME = "Downloads"
    const val NOTIFICATION_CHANNEL_DESC = "Shows active download progress"
    const val NOTIFICATION_ID_BASE = 1000

    const val WORKER_TAG_DOWNLOAD = "download_worker"
    const val WORKER_INPUT_DOWNLOAD_ID = "download_id"
    const val WORKER_INPUT_URL = "url"
    const val WORKER_INPUT_FORMAT = "format"
    const val WORKER_INPUT_QUALITY = "quality"
    const val WORKER_INPUT_OUTPUT_PATH = "output_path"

    const val ACTION_CANCEL_DOWNLOAD = "com.mediadownloader.ACTION_CANCEL_DOWNLOAD"
    const val ACTION_PAUSE_DOWNLOAD = "com.mediadownloader.ACTION_PAUSE_DOWNLOAD"
    const val ACTION_RESUME_DOWNLOAD = "com.mediadownloader.ACTION_RESUME_DOWNLOAD"
    const val EXTRA_DOWNLOAD_ID = "extra_download_id"

    const val DEFAULT_MAX_CONCURRENT_DOWNLOADS = 2
    const val MAX_RETRY_ATTEMPTS = 3

    const val PREFS_THEME_KEY = "theme_mode"
    const val PREFS_DOWNLOAD_PATH_KEY = "download_path"
    const val PREFS_DEFAULT_QUALITY_KEY = "default_quality"
    const val PREFS_DEFAULT_AUDIO_FORMAT_KEY = "default_audio_format"
    const val PREFS_MAX_CONCURRENT_KEY = "max_concurrent_downloads"

    const val YTDLP_BINARY_NAME = "yt-dlp"
    const val YTDLP_PROGRESS_PATTERN = "[download]"
    const val YTDLP_JSON_FLAG = "-J"

    val SUPPORTED_AUDIO_FORMATS = listOf("mp3", "m4a", "opus", "wav", "flac", "aac")
    val SUPPORTED_QUALITIES = listOf("best", "1080p", "720p", "480p", "360p", "240p", "144p", "audio_only")
}
