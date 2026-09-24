package com.parallax.wallpaper.model

data class WallpaperItem(
    val id: String,
    val title: String,
    val category: Category,
    val previewUrl: String,
    val fullUrl: String = previewUrl,
    val isParallax: Boolean = false,
    val layers: List<WallpaperLayer> = emptyList(),
    val downloads: Int = 0,
    val likes: Int = 0,
    val isFavorite: Boolean = false,
    val isPremium: Boolean = false,
    val isUnlocked: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
