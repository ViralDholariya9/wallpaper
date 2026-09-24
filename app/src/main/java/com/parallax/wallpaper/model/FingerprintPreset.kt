package com.parallax.wallpaper.model

enum class FingerprintAnimationType(val code: String, val displayName: String, val icon: String) {
    CYBER_MATRIX("CYBER_MATRIX", "Cyber Matrix HUD", "⚡"),
    SUPERNOVA_FLARE("SUPERNOVA_FLARE", "Supernova Cosmic Flare", "🌌"),
    NEON_PORTAL("NEON_PORTAL", "Neon Hyper Portal", "🌀"),
    MYSTIC_RUNES("MYSTIC_RUNES", "Mystic Arcane Runes", "✨"),
    CIRCUIT_OVERLOAD("CIRCUIT_OVERLOAD", "Quantum Circuit Overload", "🔌"),
    SOLAR_FLARE("SOLAR_FLARE", "Solar Fusion Flare", "🔥");

    companion object {
        fun fromString(type: String?): FingerprintAnimationType {
            return entries.firstOrNull { it.code.equals(type, ignoreCase = true) || it.name.equals(type, ignoreCase = true) }
                ?: CYBER_MATRIX
        }
    }
}

data class FingerprintPreset(
    val id: String,
    val title: String,
    val category: String = "CYBERPUNK",
    val animationType: String = "CYBER_MATRIX",
    val primaryColor: String = "#00E5FF",
    val secondaryColor: String = "#7000FF",
    val accentGlow: String = "#00FFAA",
    val animationSpeed: Float = 1.2f,
    val sensorScale: Float = 1.0f,
    val yPositionPercent: Int = 78,
    val hapticEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val isPremium: Boolean = false,
    val previewUrl: String = "",
    val description: String = "",
    val downloads: Int = 0,
    val isActive: Boolean = true,
    val sortOrder: Int = 1
) {
    val typeEnum: FingerprintAnimationType
        get() = FingerprintAnimationType.fromString(animationType)
}
