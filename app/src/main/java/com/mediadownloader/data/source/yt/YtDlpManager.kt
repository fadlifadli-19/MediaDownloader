package com.mediadownloader.data.source.yt

import com.mediadownloader.core.error.AppError
import com.mediadownloader.core.executor.YtDlpProcessExecutor
import com.mediadownloader.core.native.BinaryInstaller
import com.mediadownloader.core.result.Resource
import com.mediadownloader.domain.model.DownloadProgress
import com.mediadownloader.domain.model.MediaInfo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YtDlpManager @Inject constructor(
    private val binaryInstaller: BinaryInstaller,
    private val parser: YtDlpParser,
    private val processHandler: YtDlpProcessHandler,
    private val processExecutor: YtDlpProcessExecutor
) {
    suspend fun analyzeUrl(url: String): Resource<MediaInfo> {
        val binaryResult = binaryInstaller.ensureInstalled()
        if (binaryResult is Resource.Error) return binaryResult

        val binaryPath = (binaryResult as Resource.Success).data.absolutePath

        val command = listOf(
            binaryPath,
            "--no-playlist",
            "--dump-json",
            "--no-warnings",
            url
        )

        return when (val result = processExecutor.executeAndGetJson(command)) {
            is Resource.Success -> {
                runCatching {
                    parser.parseMediaInfo(result.data, url)
                }.fold(
                    onSuccess = { Resource.Success(it) },
                    onFailure = { Resource.Error(it.message ?: "Parse error", it) }
                )
            }
            is Resource.Error -> result
            is Resource.Loading -> Resource.Error("Unexpected loading state")
        }
    }

    fun download(
        url: String,
        formatId: String,
        outputDir: String,
        outputTemplate: String = "%(title)s.%(ext)s",
        extraArgs: List<String> = emptyList()
    ): Flow<DownloadEvent> = flow {
        val binaryResult = binaryInstaller.ensureInstalled()
        if (binaryResult is Resource.Error) {
            emit(DownloadEvent(errorLine = binaryResult.message))
            return@flow
        }

        val binaryPath = (binaryResult as Resource.Success).data.absolutePath

        val command = mutableListOf(
            binaryPath,
            "--no-playlist",
            "--progress",
            "--newline",
            "--no-warnings",
            "-f", formatId,
            "-o", "$outputDir/$outputTemplate"
        )

        if (extraArgs.isNotEmpty()) command.addAll(extraArgs)
        command.add(url)

        processHandler.streamDownload(command).collect { event ->
            emit(event)
        }
    }

    fun downloadAudio(
        url: String,
        audioFormat: String = "mp3",
        outputDir: String,
        outputTemplate: String = "%(title)s.%(ext)s"
    ): Flow<DownloadEvent> = flow {
        val binaryResult = binaryInstaller.ensureInstalled()
        if (binaryResult is Resource.Error) {
            emit(DownloadEvent(errorLine = binaryResult.message))
            return@flow
        }

        val binaryPath = (binaryResult as Resource.Success).data.absolutePath

        val command = listOf(
            binaryPath,
            "--no-playlist",
            "--progress",
            "--newline",
            "--no-warnings",
            "-x",
            "--audio-format", audioFormat,
            "--audio-quality", "0",
            "-o", "$outputDir/$outputTemplate",
            url
        )

        processHandler.streamDownload(command).collect { event ->
            emit(event)
        }
    }

    fun cancelCurrent() = processHandler.cancel()
}
