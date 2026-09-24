package com.parallax.wallpaper.aod

import androidx.compose.ui.graphics.Color

/**
 * 🕒 Always-On Display (AOD) Clock Style Types
 */
enum class AODClockType {
    CYBERPUNK_DIGITAL,
    MINIMALIST_ANALOG,
    TYPOGRAPHY_WORD,
    NEON_ANIMAL,
    GAMING_HUD;

    companion object {
        fun fromString(value: String?): AODClockType {
            return when (value?.lowercase()) {
                "minimalist_analog", "analog" -> MINIMALIST_ANALOG
                "typography_word", "typography", "word" -> TYPOGRAPHY_WORD
                "neon_animal", "animal" -> NEON_ANIMAL
                "gaming_hud", "gaming" -> GAMING_HUD
                else -> CYBERPUNK_DIGITAL
            }
        }
    }
}

/**
 * Represents an AOD Clock Face Model with custom styling & widget configuration
 */
data class AODClockFace(
    val id: String,
    val title: String,
    val clockType: AODClockType = AODClockType.CYBERPUNK_DIGITAL,
    val accentColorHex: String = "#00E5FF",
    val glowColorHex: String = "#7000FF",
    val textColorHex: String = "#FFFFFF",
    val backgroundColorHex: String = "#000000",
    val dialStyle: String = "futuristic_hud",
    val hasBatteryWidget: Boolean = true,
    val hasDateWidget: Boolean = true,
    val hasStepsWidget: Boolean = true,
    val hasWeatherWidget: Boolean = true,
    val previewUrl: String = "",
    val assetUrl: String = "",
    val isPremium: Boolean = false,
    val downloads: Int = 0,
    val isActive: Boolean = true,
    val sortOrder: Int = 0
) {
    val accentColor: Color
        get() = try { Color(android.graphics.Color.parseColor(accentColorHex)) } catch (_: Exception) { Color(0xFF00E5FF) }

    val glowColor: Color
        get() = try { Color(android.graphics.Color.parseColor(glowColorHex)) } catch (_: Exception) { Color(0xFF7000FF) }

    val textColor: Color
        get() = try { Color(android.graphics.Color.parseColor(textColorHex)) } catch (_: Exception) { Color.White }

    val backgroundColor: Color
        get() = Color.Black // Always pure AMOLED black for 0W battery efficiency
}

/**
 * Live Real-Time Telemetry rendered on AOD Screen
 */
data class AODTelemetry(
    val batteryPercent: Int = 88,
    val isCharging: Boolean = false,
    val stepCount: Int = 7240,
    val tempCelsius: Int = 28,
    val weatherDesc: String = "Sunny",
    val unreadNotifications: Int = 3
)

/**
 * Anti-Burn-In Pixel Shift Offset coordinates in dp
 */
data class AODBurnInOffset(
    val xOffsetDp: Float = 0f,
    val yOffsetDp: Float = 0f
)
