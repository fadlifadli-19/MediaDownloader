package com.mediadownloader.domain.usecase

import com.mediadownloader.core.result.Resource
import com.mediadownloader.data.source.yt.YtDlpManager
import com.mediadownloader.domain.model.MediaInfo
import javax.inject.Inject

class AnalyzeUrlUseCase @Inject constructor(
    private val ytDlpManager: YtDlpManager
) {
    suspend operator fun invoke(url: String): Resource<MediaInfo> {
        val trimmed = url.trim()
        if (trimmed.isBlank()) {
            return Resource.Error("URL cannot be empty")
        }
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            return Resource.Error("Invalid URL: must start with http:// or https://")
        }
        return ytDlpManager.analyzeUrl(trimmed)
    }
}
