package com.mediadownloader.presentation.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.mediadownloader.domain.model.Download
import com.mediadownloader.domain.model.DownloadStatus
import com.mediadownloader.presentation.ui.theme.DownloadActive
import com.mediadownloader.presentation.ui.theme.DownloadFailed
import com.mediadownloader.presentation.ui.theme.DownloadPaused
import com.mediadownloader.presentation.ui.theme.DownloadQueued

@Composable
fun DownloadProgressItem(
    download: Download,
    onCancel: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = download.thumbnail,
                contentDescription = null,
                modifier = Modifier
                    .size(56.dp)
                    .clip(MaterialTheme.shapes.small),
                contentScale = ContentScale.Crop
            )
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = download.title.ifBlank { download.url },
                    style = MaterialTheme.typography.bodyMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(4.dp))
                when (download.status) {
                    DownloadStatus.DOWNLOADING -> {
                        LinearProgressIndicator(
                            progress = { download.progress.percentage / 100f },
                            modifier = Modifier.fillMaxWidth(),
                            color = DownloadActive
                        )
                        val info = buildString {
                            append("${download.progress.percentage.toInt()}%")
                            if (download.progress.speed.isNotBlank()) append("  ${download.progress.speed}")
                            if (download.progress.eta.isNotBlank()) append("  ETA ${download.progress.eta}")
                        }
                        Text(info, style = MaterialTheme.typography.labelSmall, color = DownloadActive)
                    }
                    DownloadStatus.QUEUED ->
                        Text("Queued", style = MaterialTheme.typography.labelSmall, color = DownloadQueued)
                    DownloadStatus.PAUSED ->
                        Text("Paused", style = MaterialTheme.typography.labelSmall, color = DownloadPaused)
                    DownloadStatus.FAILED ->
                        Text(
                            "Failed: ${download.errorMessage.take(60)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = DownloadFailed
                        )
                    else -> {}
                }
            }
            when (download.status) {
                DownloadStatus.DOWNLOADING -> {
                    IconButton(onClick = onPause) {
                        Icon(Icons.Default.Pause, contentDescription = "Pause")
                    }
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Cancel, contentDescription = "Cancel")
                    }
                }
                DownloadStatus.PAUSED -> {
                    IconButton(onClick = onResume) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Resume")
                    }
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Cancel, contentDescription = "Cancel")
                    }
                }
                DownloadStatus.QUEUED -> {
                    IconButton(onClick = onCancel) {
                        Icon(Icons.Default.Cancel, contentDescription = "Cancel")
                    }
                }
                else -> {}
            }
        }
    }
}
