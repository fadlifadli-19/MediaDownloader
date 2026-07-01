package com.mediadownloader.presentation.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mediadownloader.domain.model.MediaInfo
import com.mediadownloader.domain.model.VideoFormat
import com.mediadownloader.util.Constants

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConfirmDownloadDialog(
    mediaInfo: MediaInfo,
    onConfirm: (formatId: String, quality: String) -> Unit,
    onDismiss: () -> Unit
) {
    val qualities = Constants.SUPPORTED_QUALITIES
    val formats = mediaInfo.formats.filter { it.hasVideo || it.hasAudio }
    val displayFormats: List<VideoFormat> = formats.ifEmpty { emptyList() }

    var selectedQuality by remember { mutableStateOf(qualities.first()) }
    var selectedFormat by remember { mutableStateOf(displayFormats.firstOrNull()?.formatId ?: "best") }
    var qualityExpanded by remember { mutableStateOf(false) }
    var formatExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Download Options") },
        text = {
            Column {
                MediaCard(mediaInfo = mediaInfo)
                Spacer(Modifier.height(16.dp))

                Text("Quality", style = MaterialTheme.typography.labelMedium)
                Spacer(Modifier.height(4.dp))
                ExposedDropdownMenuBox(
                    expanded = qualityExpanded,
                    onExpandedChange = { qualityExpanded = it }
                ) {
                    OutlinedTextField(
                        value = selectedQuality,
                        onValueChange = {},
                        readOnly = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(qualityExpanded) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = qualityExpanded,
                        onDismissRequest = { qualityExpanded = false }
                    ) {
                        qualities.forEach { q ->
                            DropdownMenuItem(
                                text = { Text(q) },
                                onClick = {
                                    selectedQuality = q
                                    qualityExpanded = false
                                }
                            )
                        }
                    }
                }

                if (displayFormats.isNotEmpty()) {
                    Spacer(Modifier.height(12.dp))
                    Text("Format", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    ExposedDropdownMenuBox(
                        expanded = formatExpanded,
                        onExpandedChange = { formatExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = displayFormats.find { it.formatId == selectedFormat }?.let {
                                "${it.formatId} · ${it.resolution} · ${it.ext}"
                            } ?: selectedFormat,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(formatExpanded) },
                            modifier = Modifier
                                .menuAnchor()
                                .fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = formatExpanded,
                            onDismissRequest = { formatExpanded = false }
                        ) {
                            displayFormats.forEach { fmt ->
                                DropdownMenuItem(
                                    text = { Text("${fmt.formatId} · ${fmt.resolution} · ${fmt.ext}") },
                                    onClick = {
                                        selectedFormat = fmt.formatId
                                        formatExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) { Text("Cancel") }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = {
                    onConfirm(
                        if (selectedQuality == "audio_only") "bestaudio" else selectedFormat.ifBlank { "best" },
                        selectedQuality
                    )
                }) { Text("Download") }
            }
        },
        dismissButton = null
    )
}
