package com.mediadownloader.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FileHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val authority: String get() = "${context.packageName}.fileprovider"

    fun getDefaultDownloadDir(): File {
        val external = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        if (external.exists() || external.mkdirs()) return external
        val internal = File(context.filesDir, "downloads")
        internal.mkdirs()
        return internal
    }

    fun getDownloadDirForPath(path: String): File {
        return if (path.isBlank()) getDefaultDownloadDir()
        else File(path).also { it.mkdirs() }
    }

    fun getUriForFile(file: File): Uri =
        FileProvider.getUriForFile(context, authority, file)

    fun openFile(file: File): Intent {
        val uri = getUriForFile(file)
        val mime = getMimeType(file.extension)
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mime)
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun shareFile(file: File): Intent {
        val uri = getUriForFile(file)
        val mime = getMimeType(file.extension)
        return Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = mime
                putExtra(Intent.EXTRA_STREAM, uri)
                flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            },
            "Share via"
        ).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun openFolder(file: File): Intent {
        val dir = if (file.isDirectory) file else file.parentFile ?: getDefaultDownloadDir()
        val uri = getUriForFile(dir)
        return Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "resource/folder")
            flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }

    fun deleteFile(path: String): Boolean {
        val file = File(path)
        return if (file.exists()) file.delete() else true
    }

    fun fileExists(path: String): Boolean = File(path).exists()

    fun formatFileSize(bytes: Long): String = when {
        bytes < 1024 -> "$bytes B"
        bytes < 1024 * 1024 -> "%.1f KB".format(bytes / 1024.0)
        bytes < 1024 * 1024 * 1024 -> "%.1f MB".format(bytes / (1024.0 * 1024))
        else -> "%.2f GB".format(bytes / (1024.0 * 1024 * 1024))
    }

    fun sanitizeFileName(name: String): String =
        name.replace(Regex("[\\\\/:*?\"<>|]"), "_")
            .trim()
            .take(200)

    private fun getMimeType(ext: String): String = when (ext.lowercase()) {
        "mp4", "mkv", "webm", "avi", "mov" -> "video/*"
        "mp3", "m4a", "opus", "flac", "wav", "aac" -> "audio/*"
        else -> "*/*"
    }
}
