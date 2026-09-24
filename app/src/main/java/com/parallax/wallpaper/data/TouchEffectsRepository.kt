package com.parallax.wallpaper.data

import com.parallax.wallpaper.data.api.ApiClient
import com.parallax.wallpaper.model.TouchEffectPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for Interactive Touch Fluid & Ripple Effects.
 * Connects to backend REST API with rich offline defaults.
 */
class TouchEffectsRepository {

    val defaultPresets = listOf(
        TouchEffectPreset(
            id = "touch_water_ripple",
            title = "🌊 Crystal Water Ripples",
            effectType = "WATER_RIPPLE",
            primaryColor = "#00E5FF",
            secondaryColor = "#0077FF",
            particleCount = 60,
            rippleRadius = 260f,
            speed = 1.0f,
            glowIntensity = 1.2f,
            isHapticEnabled = true,
            isPremium = false,
            previewUrl = "/uploads/touch/touch_water_ripple.svg",
            downloads = 28400,
            sortOrder = 1
        ),
        TouchEffectPreset(
            id = "touch_neon_fluid",
            title = "🌌 Cyber Neon Fluid Dye",
            effectType = "NEON_FLUID",
            primaryColor = "#FF007F",
            secondaryColor = "#7000FF",
            particleCount = 100,
            rippleRadius = 220f,
            speed = 1.4f,
            glowIntensity = 1.6f,
            isHapticEnabled = true,
            isPremium = false,
            previewUrl = "/uploads/touch/touch_neon_fluid.svg",
            downloads = 34500,
            sortOrder = 2
        ),
        TouchEffectPreset(
            id = "touch_electric_sparks",
            title = "⚡ Hyper Electric Lightning",
            effectType = "ELECTRIC_SPARKS",
            primaryColor = "#FFE600",
            secondaryColor = "#00E5FF",
            particleCount = 120,
            rippleRadius = 280f,
            speed = 1.8f,
            glowIntensity = 1.8f,
            isHapticEnabled = true,
            isPremium = false,
            previewUrl = "/uploads/touch/touch_electric_sparks.svg",
            downloads = 41200,
            sortOrder = 3
        ),
        TouchEffectPreset(
            id = "touch_magic_stardust",
            title = "✨ Celestial Stardust Aura",
            effectType = "MAGIC_STARDUST",
            primaryColor = "#FFD700",
            secondaryColor = "#FF69B4",
            particleCount = 90,
            rippleRadius = 200f,
            speed = 0.9f,
            glowIntensity = 1.4f,
            isHapticEnabled = true,
            isPremium = true,
            previewUrl = "/uploads/touch/touch_magic_stardust.svg",
            downloads = 21900,
            sortOrder = 4
        ),
        TouchEffectPreset(
            id = "touch_solar_magma",
            title = "🔥 Solar Magma Flare",
            effectType = "MAGMA_BURST",
            primaryColor = "#FF3D00",
            secondaryColor = "#FFD600",
            particleCount = 110,
            rippleRadius = 240f,
            speed = 1.5f,
            glowIntensity = 1.7f,
            isHapticEnabled = true,
            isPremium = false,
            previewUrl = "/uploads/touch/touch_solar_magma.svg",
            downloads = 18600,
            sortOrder = 5
        ),
        TouchEffectPreset(
            id = "touch_gravity_vortex",
            title = "🌀 Quantum Gravity Vortex",
            effectType = "GRAVITY_VORTEX",
            primaryColor = "#9D00FF",
            secondaryColor = "#00FFCC",
            particleCount = 130,
            rippleRadius = 300f,
            speed = 1.3f,
            glowIntensity = 1.9f,
            isHapticEnabled = true,
            isPremium = true,
            previewUrl = "/uploads/touch/touch_gravity_vortex.svg",
            downloads = 29300,
            sortOrder = 6
        )
    )

    private val _presets = MutableStateFlow<List<TouchEffectPreset>>(defaultPresets)
    val presets: StateFlow<List<TouchEffectPreset>> = _presets.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    suspend fun fetchPresets(effectType: String? = null, search: String? = null) {
        _isLoading.value = true
        try {
            val res = ApiClient.service.getTouchPresets(
                effectType = if (effectType == "ALL") null else effectType,
                search = search
            )
            if (res.success && res.data != null && res.data.isNotEmpty()) {
                _presets.value = res.data
            } else {
                _presets.value = defaultPresets
            }
        } catch (_: Exception) {
            _presets.value = defaultPresets
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun applyPreset(id: String) {
        try {
            ApiClient.service.applyTouchPreset(id)
        } catch (_: Exception) {}
    }
}
