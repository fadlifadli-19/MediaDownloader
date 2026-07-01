package com.mediadownloader.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.WorkManager
import com.mediadownloader.domain.model.DownloadStatus
import com.mediadownloader.domain.repository.DownloadRepository
import com.mediadownloader.util.Constants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class DownloadActionReceiver : BroadcastReceiver() {

    companion object {
        private const val TAG = "DownloadActionReceiver"
    }

    @Inject
    lateinit var downloadRepository: DownloadRepository

    @Inject
    lateinit var workManager: WorkManager

    private val scope = CoroutineScope(Dispatchers.IO)

    override fun onReceive(context: Context, intent: Intent) {
        val downloadId = intent.getStringExtra(Constants.EXTRA_DOWNLOAD_ID) ?: run {
            Log.w(TAG, "Received action without downloadId")
            return
        }

        when (intent.action) {
            Constants.ACTION_CANCEL_DOWNLOAD -> {
                Log.d(TAG, "Cancel requested for $downloadId")
                scope.launch {
                    downloadRepository.updateStatus(downloadId, DownloadStatus.CANCELLED)
                    workManager.cancelAllWorkByTag("${Constants.WORKER_TAG_DOWNLOAD}_$downloadId")
                }
            }
            Constants.ACTION_PAUSE_DOWNLOAD -> {
                Log.d(TAG, "Pause requested for $downloadId")
                scope.launch {
                    downloadRepository.updateStatus(downloadId, DownloadStatus.PAUSED)
                    workManager.cancelAllWorkByTag("${Constants.WORKER_TAG_DOWNLOAD}_$downloadId")
                }
            }
            Constants.ACTION_RESUME_DOWNLOAD -> {
                Log.d(TAG, "Resume requested for $downloadId")
                scope.launch {
                    downloadRepository.updateStatus(downloadId, DownloadStatus.QUEUED)
                }
            }
            else -> Log.w(TAG, "Unknown action: ${intent.action}")
        }
    }
}
