package com.parallax.wallpaper.data

import com.parallax.wallpaper.data.api.ApiClient
import com.parallax.wallpaper.model.FingerprintPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for In-Display Fingerprint Animation Effects.
 * Connects to backend REST API with rich offline defaults.
 */
class FingerprintRepository {

    val defaultPresets = listOf(
        FingerprintPreset(
            id = "fp_cyber_matrix",
            title = "⚡ Cyber Matrix Biometric Scanner",
            category = "CYBERPUNK",
            animationType = "CYBER_MATRIX",
            primaryColor = "#00E5FF",
            secondaryColor = "#7000FF",
            accentGlow = "#00FFAA",
            animationSpeed = 1.2f,
            sensorScale = 1.0f,
            yPositionPercent = 78,
            hapticEnabled = true,
            soundEnabled = true,
            isPremium = false,
            previewUrl = "/uploads/fingerprint/fp_cyber_matrix.svg",
            description = "Holographic cyberpunk HUD reticle with rotating concentric rings, laser sweep line, and neon particle burst.",
            downloads = 48500,
            sortOrder = 1
        ),
        FingerprintPreset(
            id = "fp_supernova_flare",
            title = "🌌 Supernova Cosmic Flare",
            category = "COSMIC",
            animationType = "SUPERNOVA_FLARE",
            primaryColor = "#FF9100",
            secondaryColor = "#FF1744",
            accentGlow = "#FFEA00",
            animationSpeed = 1.4f,
            sensorScale = 1.05f,
            yPositionPercent = 78,
            hapticEnabled = true,
            soundEnabled = true,
            isPremium = false,
            previewUrl = "/uploads/fingerprint/fp_supernova_flare.svg",
            description = "Blazing starburst shockwave expanding into cosmic solar embers upon biometric unlock.",
            downloads = 39200,
            sortOrder = 2
        ),
        FingerprintPreset(
            id = "fp_neon_portal",
            title = "🌀 Neon Hyper Portal",
            category = "NEON",
            animationType = "NEON_PORTAL",
            primaryColor = "#00E5FF",
            secondaryColor = "#E040FB",
            accentGlow = "#76FF03",
            animationSpeed = 1.3f,
            sensorScale = 1.0f,
            yPositionPercent = 78,
            hapticEnabled = true,
            soundEnabled = true,
            isPremium = false,
            previewUrl = "/uploads/fingerprint/fp_neon_portal.svg",
            description = "Multi-colored glowing spiral portal vortex sucking biometric data into the hyper-space dimension.",
            downloads = 35100,
            sortOrder = 3
        ),
        FingerprintPreset(
            id = "fp_mystic_runes",
            title = "✨ Mystic Arcane Runes",
            category = "MAGIC",
            animationType = "MYSTIC_RUNES",
            primaryColor = "#B388FF",
            secondaryColor = "#7C4DFF",
            accentGlow = "#FFD700",
            animationSpeed = 1.0f,
            sensorScale = 1.0f,
            yPositionPercent = 78,
            hapticEnabled = true,
            soundEnabled = true,
            isPremium = true,
            previewUrl = "/uploads/fingerprint/fp_mystic_runes.svg",
            description = "Ancient glowing runic seal with rotating sacred geometry and enchanted gold stardust.",
            downloads = 27800,
            sortOrder = 4
        ),
        FingerprintPreset(
            id = "fp_quantum_circuit",
            title = "⚡ Quantum Circuit Overload",
            category = "ENERGY",
            animationType = "CIRCUIT_OVERLOAD",
            primaryColor = "#00E676",
            secondaryColor = "#00B0FF",
            accentGlow = "#FFD600",
            animationSpeed = 1.5f,
            sensorScale = 1.0f,
            yPositionPercent = 78,
            hapticEnabled = true,
            soundEnabled = true,
            isPremium = false,
            previewUrl = "/uploads/fingerprint/fp_quantum_circuit.svg",
            description = "High-voltage PCB motherboard traces illuminating in branching electric lightning pulses.",
            downloads = 31400,
            sortOrder = 5
        ),
        FingerprintPreset(
            id = "fp_solar_fusion",
            title = "🔥 Solar Fusion Flare",
            category = "COSMIC",
            animationType = "SOLAR_FLARE",
            primaryColor = "#FF3D00",
            secondaryColor = "#FF9100",
            accentGlow = "#FFFF00",
            animationSpeed = 1.3f,
            sensorScale = 1.08f,
            yPositionPercent = 78,
            hapticEnabled = true,
            soundEnabled = true,
            isPremium = true,
            previewUrl = "/uploads/fingerprint/fp_solar_fusion.svg",
            description = "Violent thermonuclear plasma bursts radiating outward in golden flame filaments.",
            downloads = 22000,
            sortOrder = 6
        )
    )

    private val _presets = MutableStateFlow<List<FingerprintPreset>>(defaultPresets)
    val presets: StateFlow<List<FingerprintPreset>> = _presets.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    suspend fun fetchPresets(category: String? = null, animationType: String? = null, search: String? = null) {
        _isLoading.value = true
        try {
            val res = ApiClient.service.getFingerprintPresets(
                category = if (category == "ALL") null else category,
                animationType = if (animationType == "ALL") null else animationType,
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
            ApiClient.service.applyFingerprintPreset(id)
        } catch (_: Exception) {}
    }
}
