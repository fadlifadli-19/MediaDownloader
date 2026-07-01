package com.mediadownloader.presentation.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.mediadownloader.domain.model.MediaInfo
import com.mediadownloader.presentation.state.UiEvent
import com.mediadownloader.presentation.state.UiState
import com.mediadownloader.presentation.ui.component.ConfirmDownloadDialog
import com.mediadownloader.presentation.ui.component.MediaCard
import com.mediadownloader.presentation.ui.component.UrlInputBox
import com.mediadownloader.presentation.viewmodel.HomeViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToDownloads: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var showDialog by remember { mutableStateOf(false) }
    var pendingMediaInfo: MediaInfo? by remember { mutableStateOf(null) }

    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is UiEvent.ShowSnackbar -> snackbarHostState.showSnackbar(event.message)
                is UiEvent.DownloadStarted -> onNavigateToDownloads()
                else -> Unit
            }
        }
    }

    if (showDialog && pendingMediaInfo != null) {
        ConfirmDownloadDialog(
            mediaInfo = pendingMediaInfo!!,
            onConfirm = { formatId, quality ->
                showDialog = false
                viewModel.startDownload(pendingMediaInfo!!, formatId, quality)
                pendingMediaInfo = null
            },
            onDismiss = {
                showDialog = false
                pendingMediaInfo = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("MediaDownloader") })
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                UrlInputBox(
                    value = state.urlInput,
                    onValueChange = viewModel::onUrlChanged,
                    onAnalyze = viewModel::analyzeUrl,
                    isLoading = state.isAnalyzing
                )
            }

            item {
                Button(
                    onClick = viewModel::analyzeUrl,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = state.urlInput.isNotBlank() && !state.isAnalyzing
                ) {
                    Text(if (state.isAnalyzing) "Analyzing…" else "Analyze URL")
                }
            }

            when (val analyzeState = state.analyzeState) {
                is UiState.Loading -> item {
                    Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
                is UiState.Success -> item {
                    Column {
                        MediaCard(mediaInfo = analyzeState.data)
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = {
                                pendingMediaInfo = analyzeState.data
                                showDialog = true
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Download")
                        }
                    }
                }
                is UiState.Error -> item {
                    Text(
                        text = analyzeState.message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                else -> Unit
            }

            if (state.recentDownloads.isNotEmpty()) {
                item {
                    Spacer(Modifier.height(8.dp))
                    Text("Recent Downloads", style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    }
}
