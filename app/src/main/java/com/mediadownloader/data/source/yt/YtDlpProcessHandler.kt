package com.mediadownloader.data.source.yt

import android.util.Log
import com.mediadownloader.core.executor.ProcessLine
import com.mediadownloader.core.executor.YtDlpProcessExecutor
import com.mediadownloader.domain.model.DownloadProgress
import com.mediadownloader.util.ProgressParser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

data class DownloadEvent(
    val progress: DownloadProgress? = null,
    val destination: String? = null,
    val mergeDestination: String? = null,
    val errorLine: String? = null,
    val logLine: String? = null,
    val isCompleted: Boolean = false
)

@Singleton
class YtDlpProcessHandler @Inject constructor(
    private val executor: YtDlpProcessExecutor
) {
    companion object {
        private const val TAG = "YtDlpProcessHandler"
    }

    fun streamDownload(command: List<String>): Flow<DownloadEvent> =
        executor.executeStreaming(command).map { line ->
            mapLineToEvent(line)
        }

    private fun mapLineToEvent(line: ProcessLine): DownloadEvent {
        val text = line.line
        Log.v(TAG, "${if (line.isError) "ERR" else "OUT"}: $text")

        return when {
            ProgressParser.isCompleted(text) -> DownloadEvent(
                progress = DownloadProgress(percentage = 100f),
                isCompleted = true,
                logLine = text
            )

            ProgressParser.parseProgressLine(text) != null -> DownloadEvent(
                progress = ProgressParser.parseProgressLine(text),
                logLine = text
            )

            ProgressParser.parseDestination(text) != null -> DownloadEvent(
                destination = ProgressParser.parseDestination(text),
                logLine = text
            )

            ProgressParser.parseMergeDestination(text) != null -> DownloadEvent(
                mergeDestination = ProgressParser.parseMergeDestination(text),
                logLine = text
            )

            line.isError && ProgressParser.isError(text) -> DownloadEvent(
                errorLine = text,
                logLine = text
            )

            else -> DownloadEvent(logLine = text)
        }
    }

    fun cancel() = executor.cancel()
}
