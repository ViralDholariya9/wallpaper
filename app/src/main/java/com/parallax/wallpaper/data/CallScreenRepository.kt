package com.parallax.wallpaper.data

import com.parallax.wallpaper.callscreen.CallButtonStyle
import com.parallax.wallpaper.callscreen.CallScreenTheme
import com.parallax.wallpaper.callscreen.FlashSpeed
import com.parallax.wallpaper.data.api.ApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for fetching 3D Color Call Screen themes from backend API
 * with robust offline defaults.
 */
class CallScreenRepository {

    private val defaultThemes = listOf(
        CallScreenTheme(
            id = "call_cyber_matrix_2077",
            title = "⚡ Cyberpunk Matrix 2077",
            category = "CYBERPUNK",
            backgroundUrl = "/uploads/callscreen/call_cyber_matrix_2077.svg",
            previewUrl = "/uploads/callscreen/call_cyber_matrix_2077.svg",
            buttonStyle = CallButtonStyle.NEON_GLOW,
            accentColorHex = "#00E5FF",
            glowColorHex = "#7000FF",
            flashAlertEnabled = true,
            flashSpeed = FlashSpeed.STROBE,
            downloads = 14280
        ),
        CallScreenTheme(
            id = "call_royal_gold_wave",
            title = "✨ Royal Gold Silk Wave",
            category = "LUXURY",
            backgroundUrl = "/uploads/callscreen/call_royal_gold_wave.svg",
            previewUrl = "/uploads/callscreen/call_royal_gold_wave.svg",
            buttonStyle = CallButtonStyle.GLASSMORPHISM,
            accentColorHex = "#FFD700",
            glowColorHex = "#FFA000",
            flashAlertEnabled = true,
            flashSpeed = FlashSpeed.NORMAL,
            isPremium = true,
            downloads = 19850
        ),
        CallScreenTheme(
            id = "call_galaxy_supernova",
            title = "🌌 Deep Galaxy Supernova",
            category = "3D_PARALLAX",
            backgroundUrl = "/uploads/callscreen/call_galaxy_supernova.svg",
            previewUrl = "/uploads/callscreen/call_galaxy_supernova.svg",
            buttonStyle = CallButtonStyle.NEON_GLOW,
            accentColorHex = "#B388FF",
            glowColorHex = "#7C4DFF",
            flashAlertEnabled = true,
            flashSpeed = FlashSpeed.NORMAL,
            downloads = 11340
        ),
        CallScreenTheme(
            id = "call_anime_thunder_god",
            title = "⚡ Anime Thunder God",
            category = "ANIME",
            backgroundUrl = "/uploads/callscreen/call_anime_thunder_god.svg",
            previewUrl = "/uploads/callscreen/call_anime_thunder_god.svg",
            buttonStyle = CallButtonStyle.RETRO_CYBER,
            accentColorHex = "#FF1744",
            glowColorHex = "#FF5252",
            flashAlertEnabled = true,
            flashSpeed = FlashSpeed.STROBE,
            downloads = 16920
        ),
        CallScreenTheme(
            id = "call_aurora_borealis",
            title = "🌈 Mystic Northern Lights",
            category = "NEON",
            backgroundUrl = "/uploads/callscreen/call_aurora_borealis.svg",
            previewUrl = "/uploads/callscreen/call_aurora_borealis.svg",
            buttonStyle = CallButtonStyle.MINIMAL_FLAT,
            accentColorHex = "#00E676",
            glowColorHex = "#1DE9B6",
            flashAlertEnabled = true,
            flashSpeed = FlashSpeed.SLOW,
            downloads = 8750
        ),
        CallScreenTheme(
            id = "call_synthwave_sunset_80s",
            title = "🌴 Retro 80s Synthwave Grid",
            category = "NEON",
            backgroundUrl = "/uploads/callscreen/call_synthwave_sunset_80s.svg",
            previewUrl = "/uploads/callscreen/call_synthwave_sunset_80s.svg",
            buttonStyle = CallButtonStyle.RETRO_CYBER,
            accentColorHex = "#FF007F",
            glowColorHex = "#7928CA",
            flashAlertEnabled = true,
            flashSpeed = FlashSpeed.STROBE,
            downloads = 13410
        )
    )

    private val _themes = MutableStateFlow<List<CallScreenTheme>>(defaultThemes)
    val themes: StateFlow<List<CallScreenTheme>> = _themes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    suspend fun fetchThemes(category: String? = null, search: String? = null) {
        _isLoading.value = true
        try {
            val res = ApiClient.service.getCallThemes(
                category = if (category == "ALL") null else category,
                search = search
            )
            if (res.success && res.data != null) {
                _themes.value = res.data.map { item ->
                    val style = try {
                        CallButtonStyle.valueOf(item.buttonStyle.uppercase())
                    } catch (_: Exception) {
                        CallButtonStyle.NEON_GLOW
                    }
                    val speed = try {
                        FlashSpeed.valueOf(item.flashSpeed.uppercase())
                    } catch (_: Exception) {
                        FlashSpeed.NORMAL
                    }

                    CallScreenTheme(
                        id = item.id,
                        title = item.title,
                        category = item.category,
                        backgroundUrl = item.backgroundUrl,
                        previewUrl = item.previewUrl,
                        buttonStyle = style,
                        accentColorHex = item.accentColor,
                        glowColorHex = item.glowColor,
                        flashAlertEnabled = item.flashAlertEnabled,
                        flashSpeed = speed,
                        ringtoneUrl = item.ringtoneUrl,
                        isPremium = item.isPremium,
                        downloads = item.downloads
                    )
                }
            } else {
                _themes.value = defaultThemes
            }
        } catch (_: Exception) {
            _themes.value = defaultThemes
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun applyTheme(id: String) {
        try {
            ApiClient.service.applyCallTheme(id)
        } catch (_: Exception) {}
    }

    suspend fun getThemeById(id: String): CallScreenTheme? {
        val found = _themes.value.find { it.id == id } ?: defaultThemes.find { it.id == id }
        if (found != null) return found
        return try {
            val res = ApiClient.service.getCallThemeById(id)
            if (res.success && res.data != null) {
                val item = res.data
                val style = try { CallButtonStyle.valueOf(item.buttonStyle.uppercase()) } catch (_: Exception) { CallButtonStyle.NEON_GLOW }
                val speed = try { FlashSpeed.valueOf(item.flashSpeed.uppercase()) } catch (_: Exception) { FlashSpeed.NORMAL }
                CallScreenTheme(
                    id = item.id,
                    title = item.title,
                    category = item.category,
                    backgroundUrl = item.backgroundUrl,
                    previewUrl = item.previewUrl,
                    buttonStyle = style,
                    accentColorHex = item.accentColor,
                    glowColorHex = item.glowColor,
                    flashAlertEnabled = item.flashAlertEnabled,
                    flashSpeed = speed,
                    ringtoneUrl = item.ringtoneUrl,
                    isPremium = item.isPremium,
                    downloads = item.downloads
                )
            } else null
        } catch (_: Exception) {
            null
        }
    }
}
