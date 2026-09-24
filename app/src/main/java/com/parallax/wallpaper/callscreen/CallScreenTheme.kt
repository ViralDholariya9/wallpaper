package com.parallax.wallpaper.callscreen

/**
 * Button design styles for accepting and rejecting incoming calls.
 */
enum class CallButtonStyle(val displayName: String) {
    NEON_GLOW("Neon Glowing Halo"),
    GLASSMORPHISM("Frosted Glass"),
    RETRO_CYBER("Retro Cyber Grid"),
    MINIMAL_FLAT("Minimal Flat")
}

/**
 * Flashlight strobe speed when phone is ringing.
 */
enum class FlashSpeed(val displayName: String, val intervalMs: Long) {
    SLOW("Slow Beacon (1Hz)", 600L),
    NORMAL("Normal Pulse (2Hz)", 250L),
    STROBE("Fast Strobe (10Hz)", 100L)
}

/**
 * Categories for Color Call Screen themes.
 */
enum class CallScreenCategory(val displayName: String) {
    ALL("All"),
    NEON("🌈 Neon Glow"),
    LUXURY("✨ Royal Luxury"),
    PARALLAX_3D("🌌 3D Parallax"),
    ANIME("⚡ Anime Thunder"),
    CYBERPUNK("🤖 Cyberpunk")
}

/**
 * Data model representing a 3D Color Call Screen theme.
 */
data class CallScreenTheme(
    val id: String,
    val title: String,
    val category: String = "NEON",
    val backgroundUrl: String,
    val previewUrl: String,
    val buttonStyle: CallButtonStyle = CallButtonStyle.NEON_GLOW,
    val accentColorHex: String = "#00E5FF",
    val glowColorHex: String = "#7000FF",
    val flashAlertEnabled: Boolean = true,
    val flashSpeed: FlashSpeed = FlashSpeed.NORMAL,
    val ringtoneUrl: String? = null,
    val isPremium: Boolean = false,
    val downloads: Int = 0
)

/**
 * Caller profile info for preview and simulated call display.
 */
data class CallerProfile(
    val name: String = "SARAH CONNOR",
    val number: String = "+1 (555) 019-2834",
    val location: String = "California, USA",
    val avatarEmoji: String = "👤"
)
