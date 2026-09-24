package com.parallax.wallpaper.data

import com.parallax.wallpaper.data.api.ApiClient
import com.parallax.wallpaper.data.api.AODClockItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AODRepository {

    private val _clocks = MutableStateFlow<List<AODClockItem>>(emptyList())
    val clocks: StateFlow<List<AODClockItem>> = _clocks.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchClocks()
    }

    fun fetchClocks(clockType: String? = null, search: String? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = ApiClient.service.getAODClocks(clockType = clockType, search = search)
                if (response.success && response.data != null && response.data.isNotEmpty()) {
                    _clocks.value = response.data
                } else {
                    _clocks.value = getCuratedFallbackClocks()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                if (_clocks.value.isEmpty()) {
                    _clocks.value = getCuratedFallbackClocks()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun applyClock(clock: AODClockItem) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ApiClient.service.applyAODClock(clock.id)
            } catch (_: Exception) {}
        }
    }

    suspend fun getClockById(id: String): AODClockItem? {
        val found = _clocks.value.find { it.id == id } ?: getCuratedFallbackClocks().find { it.id == id }
        if (found != null) return found
        return try {
            val response = ApiClient.service.getAODClockById(id)
            if (response.success && response.data != null) {
                response.data
            } else null
        } catch (_: Exception) {
            null
        }
    }

    private fun getCuratedFallbackClocks(): List<AODClockItem> {
        return listOf(
            AODClockItem(
                id = "aod_cyberpunk_2077",
                title = "⚡ Cyberpunk Neon HUD 2077",
                clockType = "cyberpunk_digital",
                accentColor = "#00E5FF",
                glowColor = "#7000FF",
                textColor = "#FFFFFF",
                backgroundColor = "#000000",
                dialStyle = "futuristic_hud",
                hasBatteryWidget = true,
                hasDateWidget = true,
                hasStepsWidget = true,
                hasWeatherWidget = true,
                downloads = 6840
            ),
            AODClockItem(
                id = "aod_zenith_analog",
                title = "⌚ Zenith Minimalist Luxury Analog",
                clockType = "minimalist_analog",
                accentColor = "#E2E8F0",
                glowColor = "#38BDF8",
                textColor = "#F8FAFC",
                backgroundColor = "#000000",
                dialStyle = "minimalist_ticks",
                hasBatteryWidget = true,
                hasDateWidget = true,
                hasStepsWidget = true,
                hasWeatherWidget = true,
                downloads = 5420
            ),
            AODClockItem(
                id = "aod_matrix_typography",
                title = "🔤 Matrix Typography Word Clock",
                clockType = "typography_word",
                accentColor = "#22C55E",
                glowColor = "#16A34A",
                textColor = "#DCFCE7",
                backgroundColor = "#000000",
                dialStyle = "word_matrix",
                hasBatteryWidget = true,
                hasDateWidget = true,
                hasStepsWidget = false,
                hasWeatherWidget = true,
                downloads = 4190
            ),
            AODClockItem(
                id = "aod_neon_kitsune",
                title = "🦊 Neon Cyber Kitsune",
                clockType = "neon_animal",
                accentColor = "#FF2A85",
                glowColor = "#FF7170",
                textColor = "#FFFFFF",
                backgroundColor = "#000000",
                dialStyle = "polygonal_fox",
                hasBatteryWidget = true,
                hasDateWidget = true,
                hasStepsWidget = true,
                hasWeatherWidget = false,
                downloads = 7250
            ),
            AODClockItem(
                id = "aod_gamer_stamina",
                title = "🎮 Gamer HUD Stamina Dial",
                clockType = "gaming_hud",
                accentColor = "#F59E0B",
                glowColor = "#EF4444",
                textColor = "#FEF3C7",
                backgroundColor = "#000000",
                dialStyle = "sci_fi_stamina",
                hasBatteryWidget = true,
                hasDateWidget = true,
                hasStepsWidget = true,
                hasWeatherWidget = true,
                downloads = 6110
            ),
            AODClockItem(
                id = "aod_celestial_void",
                title = "👑 Celestial VIP Gold Constellation",
                clockType = "minimalist_analog",
                accentColor = "#FFD700",
                glowColor = "#F59E0B",
                textColor = "#FFFBEB",
                backgroundColor = "#000000",
                dialStyle = "celestial_ring",
                hasBatteryWidget = true,
                hasDateWidget = true,
                hasStepsWidget = false,
                hasWeatherWidget = true,
                isPremium = true,
                downloads = 8900
            )
        )
    }
}
