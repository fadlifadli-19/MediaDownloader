package com.mediadownloader.service.worker

import android.content.Context
import android.util.Log
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.mediadownloader.data.source.yt.DownloadEvent
import com.mediadownloader.data.source.yt.YtDlpManager
import com.mediadownloader.domain.model.DownloadStatus
import com.mediadownloader.domain.repository.DownloadRepository
import com.mediadownloader.service.notification.NotificationHelper
import com.mediadownloader.util.Constants
import com.mediadownloader.util.FileHelper
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext

@HiltWorker
class DownloadWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted workerParams: WorkerParameters,
    private val ytDlpManager: YtDlpManager,
    private val downloadRepository: DownloadRepository,
    private val notificationHelper: NotificationHelper,
    private val fileHelper: FileHelper
) : CoroutineWorker(context, workerParams) {

    companion object {
        private const val TAG = "DownloadWorker"
    }

    private val downloadId: String
        get() = inputData.getString(Constants.WORKER_INPUT_DOWNLOAD_ID) ?: ""

    private val url: String
        get() = inputData.getString(Constants.WORKER_INPUT_URL) ?: ""

    private val formatId: String
        get() = inputData.getString(Constants.WORKER_INPUT_FORMAT) ?: "best"

    private val quality: String
        get() = inputData.getString(Constants.WORKER_INPUT_QUALITY) ?: "best"

    private val outputPath: String
        get() = inputData.getString(Constants.WORKER_INPUT_OUTPUT_PATH)
            ?: fileHelper.getDefaultDownloadDir().absolutePath

    private val notificationId: Int
        get() = Math.abs(downloadId.hashCode() % 10000) + Constants.NOTIFICATION_ID_BASE

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        if (downloadId.isBlank() || url.isBlank()) {
            Log.e(TAG, "Missing downloadId or url")
            return@withContext Result.failure()
        }

        Log.d(TAG, "Starting download: $downloadId url=$url")

        downloadRepository.updateStatus(downloadId, DownloadStatus.DOWNLOADING)

        val download = downloadRepository.getDownloadById(downloadId).first()
        val title = download?.title ?: "Downloading…"

        setForeground(createForegroundInfo(title, 0, "", ""))

        var destination: String? = null
        var lastErrorLine: String? = null

        try {
            val isAudioOnly = quality == "audio_only"
            val downloadFlow = if (isAudioOnly) {
                ytDlpManager.downloadAudio(
                    url = url,
                    audioFormat = "mp3",
                    outputDir = outputPath
                )
            } else {
                ytDlpManager.download(
                    url = url,
                    formatId = formatId.ifBlank { "best" },
                    outputDir = outputPath
                )
            }

            downloadFlow.collect { event: DownloadEvent ->
                when {
                    event.progress != null -> {
                        val pct = event.progress.percentage.toInt()
                        downloadRepository.updateProgress(
                            id = downloadId,
                            percentage = event.progress.percentage,
                            speed = event.progress.speed,
                            eta = event.progress.eta
                        )
                        setForeground(createForegroundInfo(title, pct, event.progress.speed, event.progress.eta))
                    }
                    event.destination != null -> destination = event.destination
                    event.mergeDestination != null -> destination = event.mergeDestination
                    event.errorLine != null -> {
                        lastErrorLine = event.errorLine
                        Log.e(TAG, "yt-dlp error: ${event.errorLine}")
                    }
                }
            }

            if (lastErrorLine != null && destination == null) {
                handleFailure(title, lastErrorLine ?: "Download failed")
                return@withContext if (runAttemptCount < Constants.MAX_RETRY_ATTEMPTS) Result.retry()
                else Result.failure()
            }

            downloadRepository.updateStatus(downloadId, DownloadStatus.COMPLETED)
            val finalPath = destination ?: outputPath
            notificationHelper.cancel(notificationId)
            notificationHelper.notifyCompleted(notificationId, title, finalPath)
            Log.d(TAG, "Download completed: $downloadId -> $finalPath")
            Result.success()

        } catch (e: Exception) {
            Log.e(TAG, "Worker exception: ${e.message}", e)
            handleFailure(title, e.message ?: "Unknown error")
            if (runAttemptCount < Constants.MAX_RETRY_ATTEMPTS) Result.retry()
            else Result.failure()
        }
    }

    private suspend fun handleFailure(title: String, error: String) {
        downloadRepository.updateStatus(downloadId, DownloadStatus.FAILED)
        notificationHelper.cancel(notificationId)
        notificationHelper.notifyFailed(notificationId, title, error)
    }

    private fun createForegroundInfo(title: String, progress: Int, speed: String, eta: String): ForegroundInfo {
        val notification = notificationHelper.buildProgressNotification(
            downloadId = downloadId,
            title = title,
            progress = progress,
            speed = speed,
            eta = eta,
            notificationId = notificationId
        )
        return ForegroundInfo(notificationId, notification)
    }

    override suspend fun getForegroundInfo(): ForegroundInfo =
        createForegroundInfo("Downloading…", 0, "", "")
}
