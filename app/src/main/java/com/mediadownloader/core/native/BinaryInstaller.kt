package com.mediadownloader.core.native

import android.content.Context
import android.util.Log
import com.mediadownloader.core.error.AppError
import com.mediadownloader.core.result.Resource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BinaryInstaller @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val TAG = "BinaryInstaller"
        private const val BINARY_NAME = "yt-dlp"
        private const val ASSETS_PATH = "yt-dlp"
    }

    private val binDir: File by lazy {
        File(context.filesDir, "bin").also { it.mkdirs() }
    }

    val binaryFile: File get() = File(binDir, BINARY_NAME)

    suspend fun ensureInstalled(): Resource<File> = withContext(Dispatchers.IO) {
        runCatching {
            val target = binaryFile

            val needsInstall = !target.exists() || !target.canExecute() || isCorrupted(target)

            if (needsInstall) {
                Log.d(TAG, "Installing yt-dlp binary to ${target.absolutePath}")
                installFromAssets(target)
            } else {
                Log.d(TAG, "yt-dlp binary already installed at ${target.absolutePath}")
            }

            verifyExecutable(target)
            target
        }.fold(
            onSuccess = { Resource.Success(it) },
            onFailure = {
                Log.e(TAG, "Failed to install yt-dlp: ${it.message}", it)
                Resource.Error(it.message ?: "Failed to install yt-dlp binary", it)
            }
        )
    }

    private fun installFromAssets(target: File) {
        context.assets.open(ASSETS_PATH).use { input ->
            target.outputStream().use { output ->
                input.copyTo(output, bufferSize = 8192)
            }
        }
        Log.d(TAG, "Copied yt-dlp from assets, size: ${target.length()} bytes")
    }

    private fun isCorrupted(file: File): Boolean {
        return file.length() < 1024L
    }

    private fun verifyExecutable(file: File) {
        if (!file.setExecutable(true, false)) {
            Log.w(TAG, "setExecutable returned false, attempting chmod 755")
            val result = Runtime.getRuntime().exec(arrayOf("chmod", "755", file.absolutePath))
            val exitCode = result.waitFor()
            if (exitCode != 0) {
                throw AppError.ExecutionError("chmod 755 failed with exit code $exitCode")
            }
        }

        if (!file.canExecute()) {
            throw AppError.BinaryNotFound("yt-dlp binary exists but is not executable: ${file.absolutePath}")
        }

        Log.d(TAG, "yt-dlp binary verified and executable: ${file.absolutePath}")
    }

    fun getBinaryPath(): String? {
        val f = binaryFile
        return if (f.exists() && f.canExecute()) f.absolutePath else null
    }
}
