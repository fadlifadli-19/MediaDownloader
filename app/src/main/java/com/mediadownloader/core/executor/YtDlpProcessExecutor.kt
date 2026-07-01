package com.mediadownloader.core.executor

import android.util.Log
import com.mediadownloader.core.error.AppError
import com.mediadownloader.core.result.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

data class ProcessLine(val line: String, val isError: Boolean)

@Singleton
class YtDlpProcessExecutor @Inject constructor() {

    companion object {
        private const val TAG = "YtDlpProcessExecutor"
    }

    private var currentProcess: Process? = null

    fun executeStreaming(command: List<String>, workDir: File? = null): Flow<ProcessLine> =
        callbackFlow {
            val pb = ProcessBuilder(command).apply {
                workDir?.let { directory(it) }
                val env = environment()
                env["PATH"] = System.getenv("PATH") ?: "/system/bin:/system/xbin"
                env["PYTHONDONTWRITEBYTECODE"] = "1"
                redirectErrorStream(false)
            }

            Log.d(TAG, "Streaming: ${command.joinToString(" ")}")

            val process = pb.start()
            currentProcess = process

            val stdoutThread = Thread {
                BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                    reader.forEachLine { line ->
                        trySend(ProcessLine(line, false))
                    }
                }
            }

            val stderrThread = Thread {
                BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                    reader.forEachLine { line ->
                        trySend(ProcessLine(line, true))
                        Log.v(TAG, "STDERR: $line")
                    }
                }
            }

            stdoutThread.start()
            stderrThread.start()

            process.waitFor()
            stdoutThread.join(5_000)
            stderrThread.join(5_000)

            close()
            awaitClose { process.destroyForcibly() }
        }

    fun cancel() {
        currentProcess?.destroyForcibly()
        currentProcess = null
    }

    suspend fun executeAndGetJson(command: List<String>): Resource<String> =
        withContext(Dispatchers.IO) {
            runCatching {
                val pb = ProcessBuilder(command).apply {
                    val env = environment()
                    env["PATH"] = System.getenv("PATH") ?: "/system/bin:/system/xbin"
                    env["PYTHONDONTWRITEBYTECODE"] = "1"
                    redirectErrorStream(false)
                }

                val process = pb.start()
                val stdout = process.inputStream.bufferedReader().readText()
                val stderr = process.errorStream.bufferedReader().readText()
                val exitCode = process.waitFor()

                if (exitCode != 0) {
                    throw AppError.ExecutionError("yt-dlp failed (exit $exitCode): $stderr")
                }
                stdout
            }.fold(
                onSuccess = { Resource.Success(it) },
                onFailure = { Resource.Error(it.message ?: "Failed", it) }
            )
        }
}
