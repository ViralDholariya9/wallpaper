package com.parallax.wallpaper.data

import com.parallax.wallpaper.data.api.ApiClient
import com.parallax.wallpaper.model.DuoWallpaperItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Repository for Duo / Double Wallpapers (Lock & Home Magic Pairs)
 * Fetches from backend API with offline fallback defaults.
 */
class DuoWallpaperRepository {

    private val defaultPairs = listOf(
        DuoWallpaperItem(
            id = "duo_cyber_samurai",
            title = "⚡ Cyber Samurai: Dormant to Awakened",
            description = "Meditating cyber warrior on lock screen awakens with glowing neon eyes and blazing energy katana on home screen.",
            category = "CYBERPUNK",
            lockImageUrl = "/uploads/duo/duo_cyber_samurai_lock.svg",
            homeImageUrl = "/uploads/duo/duo_cyber_samurai_home.svg",
            accentColor = "#00E5FF",
            downloads = 15420,
            isPremium = false,
            sortOrder = 1
        ),
        DuoWallpaperItem(
            id = "duo_skyline_timelapse",
            title = "🌆 Metropolis 4K: Sunset to Cyberpunk",
            description = "Golden hour warm sunset skyline transforms into vibrant midnight cyberpunk with glowing neon billboards.",
            category = "LANDSCAPE",
            lockImageUrl = "/uploads/duo/duo_skyline_timelapse_lock.svg",
            homeImageUrl = "/uploads/duo/duo_skyline_timelapse_home.svg",
            accentColor = "#FF9800",
            downloads = 22150,
            isPremium = false,
            sortOrder = 2
        ),
        DuoWallpaperItem(
            id = "duo_celestial_portal",
            title = "🌌 Stargate: Cosmic Ring to Wormhole",
            description = "Ancient dormant celestial ring on lock screen flares into an open hyperspace interstellar wormhole on home screen.",
            category = "SPACE",
            lockImageUrl = "/uploads/duo/duo_celestial_portal_lock.svg",
            homeImageUrl = "/uploads/duo/duo_celestial_portal_home.svg",
            accentColor = "#B388FF",
            downloads = 18900,
            isPremium = true,
            sortOrder = 3
        ),
        DuoWallpaperItem(
            id = "duo_super_saiyan",
            title = "🐉 Dragon Aura: Base to Super Saiyan God",
            description = "Quiet calm aura in black and white on lock screen erupts into blazing crimson & gold Super Saiyan god aura on home screen.",
            category = "ANIME",
            lockImageUrl = "/uploads/duo/duo_super_saiyan_lock.svg",
            homeImageUrl = "/uploads/duo/duo_super_saiyan_home.svg",
            accentColor = "#FF1744",
            downloads = 31200,
            isPremium = true,
            sortOrder = 4
        ),
        DuoWallpaperItem(
            id = "duo_soulmate_connection",
            title = "💞 Soulmates: Moon Maiden & Sun King",
            description = "Celestial Moon Maiden reaching out on lock screen connects hands with the radiant Sun King on home screen.",
            category = "COUPLE",
            lockImageUrl = "/uploads/duo/duo_soulmate_connection_lock.svg",
            homeImageUrl = "/uploads/duo/duo_soulmate_connection_home.svg",
            accentColor = "#FF4081",
            downloads = 26780,
            isPremium = false,
            sortOrder = 5
        ),
        DuoWallpaperItem(
            id = "duo_neon_wildlife",
            title = "🐅 Apex Predator: Shadow to Cyber Tiger",
            description = "Shadow panther prowling in the dark on lock screen transforms into an electric cyber tiger with laser stripes on home screen.",
            category = "NATURE",
            lockImageUrl = "/uploads/duo/duo_neon_wildlife_lock.svg",
            homeImageUrl = "/uploads/duo/duo_neon_wildlife_home.svg",
            accentColor = "#FF9100",
            downloads = 12430,
            isPremium = false,
            sortOrder = 6
        )
    )

    private val _pairs = MutableStateFlow<List<DuoWallpaperItem>>(defaultPairs)
    val pairs: StateFlow<List<DuoWallpaperItem>> = _pairs.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    suspend fun fetchPairs(category: String? = null, search: String? = null) {
        _isLoading.value = true
        try {
            val res = ApiClient.service.getDuoWallpapers(
                category = if (category == "ALL") null else category,
                search = search
            )
            if (res.success && res.data != null && res.data.isNotEmpty()) {
                _pairs.value = res.data
            } else {
                _pairs.value = defaultPairs
            }
        } catch (_: Exception) {
            _pairs.value = defaultPairs
        } finally {
            _isLoading.value = false
        }
    }

    suspend fun applyPair(id: String) {
        try {
            ApiClient.service.applyDuoWallpaper(id)
        } catch (_: Exception) {}
    }
}
