package com.parallax.wallpaper.model

data class WallpaperLayer(
    val id: String,
    val imageUrl: String,
    val depth: Float // Multiplier for parallax offset: 0.1f (distant background) to 1.0f (foreground)
)
