package com.mediadownloader.presentation.viewmodel

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mediadownloader.util.Constants
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SettingsState(
    val themeMode: String = "system",
    val downloadPath: String = "",
    val defaultQuality: String = "best",
    val defaultAudioFormat: String = "mp3",
    val maxConcurrentDownloads: Int = Constants.DEFAULT_MAX_CONCURRENT_DOWNLOADS
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : ViewModel() {

    companion object {
        val THEME_KEY = stringPreferencesKey(Constants.PREFS_THEME_KEY)
        val DOWNLOAD_PATH_KEY = stringPreferencesKey(Constants.PREFS_DOWNLOAD_PATH_KEY)
        val DEFAULT_QUALITY_KEY = stringPreferencesKey(Constants.PREFS_DEFAULT_QUALITY_KEY)
        val DEFAULT_AUDIO_FORMAT_KEY = stringPreferencesKey(Constants.PREFS_DEFAULT_AUDIO_FORMAT_KEY)
        val MAX_CONCURRENT_KEY = intPreferencesKey(Constants.PREFS_MAX_CONCURRENT_KEY)
    }

    val settings: StateFlow<SettingsState> = dataStore.data.map { prefs ->
        SettingsState(
            themeMode = prefs[THEME_KEY] ?: "system",
            downloadPath = prefs[DOWNLOAD_PATH_KEY] ?: "",
            defaultQuality = prefs[DEFAULT_QUALITY_KEY] ?: "best",
            defaultAudioFormat = prefs[DEFAULT_AUDIO_FORMAT_KEY] ?: "mp3",
            maxConcurrentDownloads = prefs[MAX_CONCURRENT_KEY] ?: Constants.DEFAULT_MAX_CONCURRENT_DOWNLOADS
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsState())

    fun setThemeMode(mode: String) {
        viewModelScope.launch {
            dataStore.edit { it[THEME_KEY] = mode }
        }
    }

    fun setDownloadPath(path: String) {
        viewModelScope.launch {
            dataStore.edit { it[DOWNLOAD_PATH_KEY] = path }
        }
    }

    fun setDefaultQuality(quality: String) {
        viewModelScope.launch {
            dataStore.edit { it[DEFAULT_QUALITY_KEY] = quality }
        }
    }

    fun setDefaultAudioFormat(format: String) {
        viewModelScope.launch {
            dataStore.edit { it[DEFAULT_AUDIO_FORMAT_KEY] = format }
        }
    }

    fun setMaxConcurrentDownloads(count: Int) {
        viewModelScope.launch {
            dataStore.edit { it[MAX_CONCURRENT_KEY] = count.coerceIn(1, 5) }
        }
    }
}
