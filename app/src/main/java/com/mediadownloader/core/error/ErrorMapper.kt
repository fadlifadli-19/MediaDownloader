package com.mediadownloader.core.error

import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ErrorMapper @Inject constructor() {

    fun map(throwable: Throwable): AppError = when (throwable) {
        is AppError -> throwable
        is UnknownHostException -> AppError.NetworkError("No internet connection", throwable)
        is SocketTimeoutException -> AppError.NetworkError("Connection timed out", throwable)
        is IOException -> AppError.NetworkError("Network error: ${throwable.message}", throwable)
        else -> AppError.UnknownError(throwable.message ?: "Unknown error", throwable)
    }

    fun mapExitCode(exitCode: Int, stderr: String): AppError = when (exitCode) {
        1 -> AppError.ExecutionError("yt-dlp exited with code 1: $stderr")
        2 -> AppError.UnsupportedUrl(stderr.ifBlank { "URL not supported" })
        100 -> AppError.ExecutionError("yt-dlp requires update: $stderr")
        101 -> AppError.ExecutionError("Video unavailable or private: $stderr")
        else -> AppError.ExecutionError("yt-dlp exited with code $exitCode: $stderr")
    }
}
