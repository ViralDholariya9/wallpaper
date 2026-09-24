package com.parallax.wallpaper.model

data class RingtoneItem(
    val id: String,
    val title: String,
    val artist: String,
    val audioUrl: String,
    val durationSeconds: Int,
    val category: String,
    val downloads: Int = 1200,
    val isFavorite: Boolean = false,
    val isPlaying: Boolean = false
)
