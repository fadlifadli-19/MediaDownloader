package com.mediadownloader.core.executor

import android.util.Log
import com.mediadownloader.core.error.AppError
import com.mediadownloader.core.result.Resource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import javax.inject.Inject
import javax.inject.Singleton

data class ExecutionResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String
)

@Singleton
class MediaCommandExecutor @Inject constructor() {

    companion object {
        private const val TAG = "MediaCommandExecutor"
        private const val TIMEOUT_MS = 60_000L
    }

    suspend fun execute(command: List<String>, workDir: File? = null): Resource<ExecutionResult> =
        withContext(Dispatchers.IO) {
            runCatching {
                val pb = ProcessBuilder(command).apply {
                    workDir?.let { directory(it) }
                    environment()["PATH"] = System.getenv("PATH") ?: "/system/bin:/system/xbin"
                    redirectErrorStream(false)
                }

                Log.d(TAG, "Executing: ${command.joinToString(" ")}")

                val process = pb.start()

                val stdoutBuilder = StringBuilder()
                val stderrBuilder = StringBuilder()

                val stdoutThread = Thread {
                    BufferedReader(InputStreamReader(process.inputStream)).use { reader ->
                        reader.forEachLine { line ->
                            stdoutBuilder.appendLine(line)
                            Log.v(TAG, "STDOUT: $line")
                        }
                    }
                }

                val stderrThread = Thread {
                    BufferedReader(InputStreamReader(process.errorStream)).use { reader ->
                        reader.forEachLine { line ->
                            stderrBuilder.appendLine(line)
                            Log.v(TAG, "STDERR: $line")
                        }
                    }
                }

                stdoutThread.start()
                stderrThread.start()

                val exitCode = process.waitFor()
                stdoutThread.join(TIMEOUT_MS)
                stderrThread.join(TIMEOUT_MS)

                ExecutionResult(
                    exitCode = exitCode,
                    stdout = stdoutBuilder.toString(),
                    stderr = stderrBuilder.toString()
                )
            }.fold(
                onSuccess = { Resource.Success(it) },
                onFailure = { Resource.Error(it.message ?: "Execution failed", it) }
            )
        }
}
