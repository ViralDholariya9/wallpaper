package com.parallax.wallpaper.data

import com.parallax.wallpaper.data.api.ApiClient
import com.parallax.wallpaper.model.RingtoneItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RingtoneRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val sampleRingtones = listOf(
        RingtoneItem(
            id = "r1",
            title = "Cyberpunk Neon Drive",
            artist = "ReWall Sounds",
            audioUrl = "https://assets.mixkit.co/active_storage/sfx/2874/2874-preview.mp3",
            durationSeconds = 28,
            category = "Cyberpunk",
            downloads = 48200
        ),
        RingtoneItem(
            id = "r2",
            title = "Cosmic Starlight Ambient",
            artist = "Astro Beats",
            audioUrl = "https://assets.mixkit.co/active_storage/sfx/2869/2869-preview.mp3",
            durationSeconds = 34,
            category = "Ambient",
            downloads = 62100
        ),
        RingtoneItem(
            id = "r3",
            title = "Marimba Tropical Vibe",
            artist = "Summer Vibes",
            audioUrl = "https://assets.mixkit.co/active_storage/sfx/2873/2873-preview.mp3",
            durationSeconds = 24,
            category = "Marimba",
            downloads = 39800
        ),
        RingtoneItem(
            id = "r4",
            title = "Future Bass Drop",
            artist = "EDM Pulse",
            audioUrl = "https://assets.mixkit.co/active_storage/sfx/2871/2871-preview.mp3",
            durationSeconds = 30,
            category = "EDM",
            downloads = 74500
        ),
        RingtoneItem(
            id = "r5",
            title = "Anime Lo-Fi Sunset",
            artist = "Chill Tokyo",
            audioUrl = "https://assets.mixkit.co/active_storage/sfx/2872/2872-preview.mp3",
            durationSeconds = 42,
            category = "Lo-Fi",
            downloads = 83900
        ),
        RingtoneItem(
            id = "r6",
            title = "Acoustic Forest Whistle",
            artist = "Nature Melody",
            audioUrl = "https://assets.mixkit.co/active_storage/sfx/2870/2870-preview.mp3",
            durationSeconds = 20,
            category = "Nature",
            downloads = 29300
        )
    )

    private val _ringtones = MutableStateFlow(sampleRingtones)
    val ringtones: StateFlow<List<RingtoneItem>> = _ringtones.asStateFlow()

    private val _currentlyPlayingId = MutableStateFlow<String?>(null)
    val currentlyPlayingId: StateFlow<String?> = _currentlyPlayingId.asStateFlow()

    init {
        fetchCloudRingtones()
    }

    fun fetchCloudRingtones() {
        scope.launch {
            try {
                val response = ApiClient.service.getRingtones()
                if (response.success && !response.data.isNullOrEmpty()) {
                    _ringtones.value = response.data
                }
            } catch (e: Exception) {
                // Graceful fallback to sample ringtones
                android.util.Log.w("RingtoneRepo", "Cloud ringtone sync fallback: ${e.message}")
            }
        }
    }

    fun setPlaying(id: String?) {
        _currentlyPlayingId.value = id
        _ringtones.update { list ->
            list.map { it.copy(isPlaying = it.id == id) }
        }
    }
}

