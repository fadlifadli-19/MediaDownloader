package com.mediadownloader.service.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.mediadownloader.MainActivity
import com.mediadownloader.util.Constants
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager: NotificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createChannel()
    }

    private fun createChannel() {
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            Constants.NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = Constants.NOTIFICATION_CHANNEL_DESC
            setShowBadge(false)
        }
        notificationManager.createNotificationChannel(channel)
    }

    fun buildProgressNotification(
        downloadId: String,
        title: String,
        progress: Int,
        speed: String,
        eta: String,
        notificationId: Int
    ): android.app.Notification {
        val cancelIntent = Intent(Constants.ACTION_CANCEL_DOWNLOAD).apply {
            setPackage(context.packageName)
            putExtra(Constants.EXTRA_DOWNLOAD_ID, downloadId)
        }
        val cancelPi = PendingIntent.getBroadcast(
            context,
            notificationId,
            cancelIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentIntent = PendingIntent.getActivity(
            context,
            0,
            Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            },
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val contentText = when {
            speed.isNotBlank() && eta.isNotBlank() -> "$speed · ETA $eta"
            speed.isNotBlank() -> speed
            eta.isNotBlank() -> "ETA $eta"
            else -> "Downloading..."
        }

        return NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(title.take(60))
            .setContentText(contentText)
            .setProgress(100, progress.coerceIn(0, 100), progress == 0)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setContentIntent(contentIntent)
            .addAction(
                android.R.drawable.ic_delete,
                "Cancel",
                cancelPi
            )
            .build()
    }

    fun buildCompletedNotification(title: String, filePath: String): android.app.Notification =
        NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Download complete")
            .setContentText(title.take(60))
            .setAutoCancel(true)
            .build()

    fun buildFailedNotification(title: String, error: String): android.app.Notification =
        NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_notify_error)
            .setContentTitle("Download failed")
            .setContentText(title.take(60))
            .setSubText(error.take(100))
            .setAutoCancel(true)
            .build()

    fun cancel(notificationId: Int) = notificationManager.cancel(notificationId)

    fun notifyCompleted(id: Int, title: String, filePath: String) =
        notificationManager.notify(id, buildCompletedNotification(title, filePath))

    fun notifyFailed(id: Int, title: String, error: String) =
        notificationManager.notify(id, buildFailedNotification(title, error))
}
