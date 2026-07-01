package com.mediadownloader.util

import com.mediadownloader.domain.model.DownloadProgress
import java.util.regex.Pattern

object ProgressParser {

    private val PROGRESS_PATTERN: Pattern = Pattern.compile(
        "\\[download\\]\\s+(\\d+\\.?\\d*)%\\s+of\\s+[~]?([\\d.]+)(\\w+)\\s+at\\s+([\\d.]+)(\\w+/s)\\s+ETA\\s+([\\d:]+)"
    )

    private val PERCENTAGE_PATTERN: Pattern = Pattern.compile(
        "\\[download\\]\\s+(\\d+\\.?\\d*)%"
    )

    private val DESTINATION_PATTERN: Pattern = Pattern.compile(
        "\\[download\\] Destination: (.+)"
    )

    private val MERGE_PATTERN: Pattern = Pattern.compile(
        "\\[Merger\\] Merging formats into \"(.+)\""
    )

    fun parseProgressLine(line: String): DownloadProgress? {
        val matcher = PROGRESS_PATTERN.matcher(line)
        if (matcher.find()) {
            return DownloadProgress(
                percentage = matcher.group(1)?.toFloatOrNull() ?: 0f,
                speed = "${matcher.group(4)}${matcher.group(5)}",
                eta = matcher.group(6) ?: ""
            )
        }

        val pctMatcher = PERCENTAGE_PATTERN.matcher(line)
        if (pctMatcher.find()) {
            return DownloadProgress(
                percentage = pctMatcher.group(1)?.toFloatOrNull() ?: 0f,
                speed = "",
                eta = ""
            )
        }

        return null
    }

    fun parseDestination(line: String): String? {
        val matcher = DESTINATION_PATTERN.matcher(line)
        return if (matcher.find()) matcher.group(1) else null
    }

    fun parseMergeDestination(line: String): String? {
        val matcher = MERGE_PATTERN.matcher(line)
        return if (matcher.find()) matcher.group(1) else null
    }

    fun isError(line: String): Boolean =
        line.contains("ERROR:") || line.contains("error:") && !line.contains("[download]")

    fun isCompleted(line: String): Boolean =
        line.contains("[download] 100%") ||
                line.contains("has already been downloaded") ||
                line.contains("Deleting original file")
}
