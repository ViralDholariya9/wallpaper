package com.parallax.wallpaper.model

enum class TouchEffectType(val code: String, val displayName: String, val icon: String) {
    WATER_RIPPLE("WATER_RIPPLE", "Crystal Water Ripples", "🌊"),
    NEON_FLUID("NEON_FLUID", "Cyber Neon Fluid Dye", "🌌"),
    ELECTRIC_SPARKS("ELECTRIC_SPARKS", "Hyper Electric Arcs", "⚡"),
    MAGIC_STARDUST("MAGIC_STARDUST", "Celestial Stardust Aura", "✨"),
    MAGMA_BURST("MAGMA_BURST", "Solar Magma Flare", "🔥"),
    GRAVITY_VORTEX("GRAVITY_VORTEX", "Quantum Gravity Vortex", "🌀");

    companion object {
        fun fromString(type: String?): TouchEffectType {
            return entries.firstOrNull { it.code.equals(type, ignoreCase = true) || it.name.equals(type, ignoreCase = true) }
                ?: WATER_RIPPLE
        }
    }
}

data class TouchEffectPreset(
    val id: String,
    val title: String,
    val effectType: String = "WATER_RIPPLE",
    val primaryColor: String = "#00E5FF",
    val secondaryColor: String = "#7000FF",
    val particleCount: Int = 80,
    val rippleRadius: Float = 250f,
    val speed: Float = 1.0f,
    val glowIntensity: Float = 1.2f,
    val isHapticEnabled: Boolean = true,
    val isPremium: Boolean = false,
    val previewUrl: String = "",
    val downloads: Int = 0,
    val isActive: Boolean = true,
    val sortOrder: Int = 1
) {
    val typeEnum: TouchEffectType
        get() = TouchEffectType.fromString(effectType)
}
