package com.parallax.wallpaper.data

import com.parallax.wallpaper.data.api.ApiClient
import com.parallax.wallpaper.data.api.EdgeLightingPresetItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EdgeLightingRepository {

    private val _presets = MutableStateFlow<List<EdgeLightingPresetItem>>(emptyList())
    val presets: StateFlow<List<EdgeLightingPresetItem>> = _presets.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchPresets()
    }

    fun fetchPresets(category: String? = null, search: String? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val cat = if (category == "ALL") null else category
                val response = ApiClient.service.getEdgeLightingPresets(category = cat, search = search)
                if (response.success && response.data != null && response.data.isNotEmpty()) {
                    _presets.value = response.data
                } else {
                    _presets.value = getCuratedFallbackPresets()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                if (_presets.value.isEmpty()) {
                    _presets.value = getCuratedFallbackPresets()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun applyPreset(preset: EdgeLightingPresetItem) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ApiClient.service.applyEdgeLightingPreset(preset.id)
            } catch (_: Exception) {}
        }
    }

    private fun getCuratedFallbackPresets(): List<EdgeLightingPresetItem> {
        return listOf(
            EdgeLightingPresetItem(
                id = "el_rgb_rainbow",
                title = "🌈 RGB 360° Rainbow Wave",
                category = "RAINBOW",
                animationType = "rainbow_wave",
                colors = listOf("#FF0055", "#FF7700", "#FFE600", "#00FF66", "#00E5FF", "#7000FF", "#FF0055"),
                speed = 1.2f,
                borderSize = 6,
                cornerRadius = 32,
                punchHoleRadius = 0,
                glowSpread = 14,
                isPremium = false,
                downloads = 3820
            ),
            EdgeLightingPresetItem(
                id = "el_cyber_snake",
                title = "⚡ Cyber Snake Comet Dual-Head",
                category = "CYBER",
                animationType = "snake_comet",
                colors = listOf("#00E5FF", "#7000FF", "#00E5FF"),
                speed = 1.5f,
                borderSize = 5,
                cornerRadius = 30,
                punchHoleRadius = 0,
                glowSpread = 16,
                isPremium = false,
                downloads = 2910
            ),
            EdgeLightingPresetItem(
                id = "el_neon_pulse",
                title = "💖 Electric Neon Breathing Pulse",
                category = "NEON",
                animationType = "pulse_glow",
                colors = listOf("#FF007F", "#9B00FF", "#00FFFF"),
                speed = 0.9f,
                borderSize = 7,
                cornerRadius = 32,
                punchHoleRadius = 0,
                glowSpread = 20,
                isPremium = false,
                downloads = 2450
            ),
            EdgeLightingPresetItem(
                id = "el_galaxy_glow",
                title = "🌌 Deep Cosmic Galaxy Drift",
                category = "GALAXY",
                animationType = "galaxy_flow",
                colors = listOf("#4A00E0", "#8E2DE2", "#00C9FF", "#92FE9D"),
                speed = 0.8f,
                borderSize = 5,
                cornerRadius = 28,
                punchHoleRadius = 0,
                glowSpread = 15,
                isPremium = false,
                downloads = 1980
            ),
            EdgeLightingPresetItem(
                id = "el_solar_flare",
                title = "🔥 Solar Flare & Golden Flame",
                category = "FIRE",
                animationType = "rainbow_wave",
                colors = listOf("#FF3E00", "#FF8500", "#FFD200", "#FF1E56"),
                speed = 1.3f,
                borderSize = 6,
                cornerRadius = 30,
                punchHoleRadius = 0,
                glowSpread = 18,
                isPremium = false,
                downloads = 3120
            ),
            EdgeLightingPresetItem(
                id = "el_punch_aura",
                title = "🎯 Camera Punch-Hole Aura + Dual Edge",
                category = "NOTCH",
                animationType = "punch_hole",
                colors = listOf("#00FFCC", "#0072FF", "#00FFCC"),
                speed = 1.1f,
                borderSize = 4,
                cornerRadius = 34,
                punchHoleRadius = 18,
                glowSpread = 16,
                isPremium = false,
                downloads = 1650
            )
        )
    }
}
