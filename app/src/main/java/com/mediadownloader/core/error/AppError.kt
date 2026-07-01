package com.mediadownloader.core.error

sealed class AppError(override val message: String, override val cause: Throwable? = null) : Exception(message, cause) {
    class NetworkError(message: String, cause: Throwable? = null) : AppError(message, cause)
    class StorageError(message: String, cause: Throwable? = null) : AppError(message, cause)
    class ParseError(message: String, cause: Throwable? = null) : AppError(message, cause)
    class ExecutionError(message: String, cause: Throwable? = null) : AppError(message, cause)
    class BinaryNotFound(message: String = "yt-dlp binary not found or not executable") : AppError(message)
    class UnsupportedUrl(message: String) : AppError(message)
    class DatabaseError(message: String, cause: Throwable? = null) : AppError(message, cause)
    class UnknownError(message: String, cause: Throwable? = null) : AppError(message, cause)

    fun toUserMessage(): String = when (this) {
        is NetworkError -> "Network error: $message"
        is StorageError -> "Storage error: $message"
        is ParseError -> "Failed to parse media info: $message"
        is ExecutionError -> "Execution failed: $message"
        is BinaryNotFound -> "yt-dlp binary not found. Please reinstall the app."
        is UnsupportedUrl -> "Unsupported URL: $message"
        is DatabaseError -> "Database error: $message"
        is UnknownError -> "An unexpected error occurred: $message"
    }
}
