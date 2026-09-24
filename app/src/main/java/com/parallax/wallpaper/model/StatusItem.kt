package com.parallax.wallpaper.model

data class StatusItem(
    val id: String,
    val title: String,
    val mediaUrl: String,
    val isVideo: Boolean = false,
    val durationText: String? = null,
    val timeAgo: String = "Just now",
    val fileSizeText: String? = null,
    val isRealStatus: Boolean = false,
    val uriString: String? = null,
    val lastModified: Long = 0L
)
