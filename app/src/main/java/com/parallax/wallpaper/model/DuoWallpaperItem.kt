package com.parallax.wallpaper.model

data class DuoWallpaperItem(
    val id: String,
    val title: String,
    val description: String = "",
    val category: String = "CYBERPUNK",
    val lockImageUrl: String,
    val homeImageUrl: String,
    val accentColor: String = "#00E5FF",
    val downloads: Int = 0,
    val isPremium: Boolean = false,
    val isActive: Boolean = true,
    val sortOrder: Int = 1
)
