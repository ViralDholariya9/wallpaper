package com.parallax.wallpaper.data

import com.parallax.wallpaper.data.api.ApiClient
import com.parallax.wallpaper.data.api.DynamicIslandThemeItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DynamicIslandRepository {

    private val _themes = MutableStateFlow<List<DynamicIslandThemeItem>>(emptyList())
    val themes: StateFlow<List<DynamicIslandThemeItem>> = _themes.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    init {
        fetchThemes()
    }

    fun fetchThemes(search: String? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = ApiClient.service.getDynamicIslandThemes(search = search)
                if (response.success && response.data != null && response.data.isNotEmpty()) {
                    _themes.value = response.data
                } else {
                    _themes.value = getCuratedFallbackThemes()
                }
            } catch (e: Exception) {
                _errorMessage.value = e.message
                if (_themes.value.isEmpty()) {
                    _themes.value = getCuratedFallbackThemes()
                }
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun applyTheme(theme: DynamicIslandThemeItem) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                ApiClient.service.applyDynamicIslandTheme(theme.id)
            } catch (_: Exception) {}
        }
    }

    private fun getCuratedFallbackThemes(): List<DynamicIslandThemeItem> {
        return listOf(
            DynamicIslandThemeItem(
                id = "di_apple_obsidian",
                title = "🖤 Apple Minimalist Obsidian",
                styleType = "minimal",
                backgroundColor = "#000000",
                textColor = "#FFFFFF",
                accentColor = "#00E5FF",
                glowColor = "rgba(255,255,255,0.15)",
                cornerRadius = 24,
                compactWidth = 120,
                compactHeight = 36,
                expandedWidth = 320,
                expandedHeight = 84,
                previewUrl = "/uploads/island/thumb_apple_obsidian.svg",
                isPremium = false,
                downloads = 4520,
                isActive = true,
                sortOrder = 1
            ),
            DynamicIslandThemeItem(
                id = "di_cyberpunk_neon",
                title = "⚡ Cyberpunk Neon Arc Reactor",
                styleType = "cyberpunk",
                backgroundColor = "#080C16",
                textColor = "#00E5FF",
                accentColor = "#7000FF",
                glowColor = "rgba(0,229,255,0.5)",
                cornerRadius = 26,
                compactWidth = 128,
                compactHeight = 38,
                expandedWidth = 330,
                expandedHeight = 88,
                previewUrl = "/uploads/island/thumb_cyberpunk_neon.svg",
                isPremium = false,
                downloads = 3890,
                isActive = true,
                sortOrder = 2
            ),
            DynamicIslandThemeItem(
                id = "di_frosted_glass",
                title = "🧊 Frosted Glassmorphism Aero",
                styleType = "glassmorphic",
                backgroundColor = "#191E2D",
                textColor = "#FFFFFF",
                accentColor = "#00FF88",
                glowColor = "rgba(0,255,136,0.3)",
                cornerRadius = 28,
                compactWidth = 124,
                compactHeight = 36,
                expandedWidth = 324,
                expandedHeight = 86,
                previewUrl = "/uploads/island/thumb_frosted_glass.svg",
                isPremium = false,
                downloads = 3140,
                isActive = true,
                sortOrder = 3
            ),
            DynamicIslandThemeItem(
                id = "di_amoled_void",
                title = "🌌 AMOLED Pure Zero-Battery Void",
                styleType = "amoled",
                backgroundColor = "#000000",
                textColor = "#E0E0E0",
                accentColor = "#FFFFFF",
                glowColor = "rgba(255,255,255,0.08)",
                cornerRadius = 22,
                compactWidth = 115,
                compactHeight = 34,
                expandedWidth = 310,
                expandedHeight = 80,
                previewUrl = "/uploads/island/thumb_amoled_void.svg",
                isPremium = false,
                downloads = 2780,
                isActive = true,
                sortOrder = 4
            ),
            DynamicIslandThemeItem(
                id = "di_pastel_aurora",
                title = "🌸 Pastel Aurora Cloud",
                styleType = "gradient",
                backgroundColor = "#1A1428",
                textColor = "#FFD6E8",
                accentColor = "#FF007F",
                glowColor = "rgba(255,0,127,0.4)",
                cornerRadius = 28,
                compactWidth = 126,
                compactHeight = 38,
                expandedWidth = 326,
                expandedHeight = 88,
                previewUrl = "/uploads/island/thumb_pastel_aurora.svg",
                isPremium = false,
                downloads = 2340,
                isActive = true,
                sortOrder = 5
            ),
            DynamicIslandThemeItem(
                id = "di_golden_vip",
                title = "👑 Golden VIP Sovereign Crown",
                styleType = "luxury",
                backgroundColor = "#120F08",
                textColor = "#FFD700",
                accentColor = "#FF9900",
                glowColor = "rgba(255,215,0,0.45)",
                cornerRadius = 25,
                compactWidth = 130,
                compactHeight = 38,
                expandedWidth = 330,
                expandedHeight = 90,
                previewUrl = "/uploads/island/thumb_golden_vip.svg",
                isPremium = true,
                downloads = 1920,
                isActive = true,
                sortOrder = 6
            )
        )
    }
}
