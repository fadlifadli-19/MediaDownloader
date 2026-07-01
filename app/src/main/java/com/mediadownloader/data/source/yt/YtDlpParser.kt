package com.mediadownloader.data.source.yt

import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.mediadownloader.core.error.AppError
import com.mediadownloader.domain.model.MediaInfo
import com.mediadownloader.domain.model.VideoFormat
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class YtDlpParser @Inject constructor() {

    fun parseMediaInfo(json: String, originalUrl: String): MediaInfo {
        val root: JsonObject = runCatching {
            JsonParser.parseString(json).asJsonObject
        }.getOrElse { throw AppError.ParseError("Invalid JSON from yt-dlp: ${it.message}") }

        val formats = parseFormats(root)
        val isPlaylist = root.has("_type") && root.get("_type").asString == "playlist"

        return MediaInfo(
            id = root.getStringOrEmpty("id"),
            url = originalUrl,
            title = root.getStringOrEmpty("title"),
            uploader = root.getStringOrEmpty("uploader"),
            uploaderUrl = root.getStringOrEmpty("uploader_url"),
            thumbnail = root.getStringOrEmpty("thumbnail"),
            duration = root.getLongOrZero("duration"),
            uploadDate = root.getStringOrEmpty("upload_date"),
            platform = root.getStringOrEmpty("extractor_key"),
            description = root.getStringOrEmpty("description").take(500),
            filesize = root.getLongOrZero("filesize_approx"),
            channel = root.getStringOrEmpty("channel"),
            channelUrl = root.getStringOrEmpty("channel_url"),
            viewCount = root.getLongOrZero("view_count"),
            likeCount = root.getLongOrZero("like_count"),
            formats = formats,
            isPlaylist = isPlaylist,
            playlistCount = if (isPlaylist) root.getIntOrZero("playlist_count") else 0,
            playlistTitle = if (isPlaylist) root.getStringOrEmpty("title") else ""
        )
    }

    private fun parseFormats(root: JsonObject): List<VideoFormat> {
        if (!root.has("formats")) return emptyList()
        val formatsArray = runCatching { root.getAsJsonArray("formats") }.getOrNull()
            ?: return emptyList()

        return formatsArray.mapNotNull { element ->
            runCatching {
                val fmt = element.asJsonObject
                val vcodec = fmt.getStringOrEmpty("vcodec")
                val acodec = fmt.getStringOrEmpty("acodec")
                VideoFormat(
                    formatId = fmt.getStringOrEmpty("format_id"),
                    formatNote = fmt.getStringOrEmpty("format_note"),
                    ext = fmt.getStringOrEmpty("ext"),
                    resolution = fmt.getStringOrEmpty("resolution").ifBlank {
                        val w = fmt.getIntOrZero("width")
                        val h = fmt.getIntOrZero("height")
                        if (w > 0 && h > 0) "${w}x${h}" else "unknown"
                    },
                    fps = fmt.getIntOrZero("fps"),
                    filesize = fmt.getLongOrZero("filesize"),
                    tbr = fmt.getDoubleOrZero("tbr"),
                    vcodec = vcodec,
                    acodec = acodec,
                    hasVideo = vcodec.isNotBlank() && vcodec != "none",
                    hasAudio = acodec.isNotBlank() && acodec != "none"
                )
            }.getOrNull()
        }
    }

    private fun JsonObject.getStringOrEmpty(key: String): String =
        runCatching { get(key)?.takeIf { !it.isJsonNull }?.asString ?: "" }.getOrDefault("")

    private fun JsonObject.getLongOrZero(key: String): Long =
        runCatching { get(key)?.takeIf { !it.isJsonNull }?.asLong ?: 0L }.getOrDefault(0L)

    private fun JsonObject.getIntOrZero(key: String): Int =
        runCatching { get(key)?.takeIf { !it.isJsonNull }?.asInt ?: 0 }.getOrDefault(0)

    private fun JsonObject.getDoubleOrZero(key: String): Double =
        runCatching { get(key)?.takeIf { !it.isJsonNull }?.asDouble ?: 0.0 }.getOrDefault(0.0)
}
