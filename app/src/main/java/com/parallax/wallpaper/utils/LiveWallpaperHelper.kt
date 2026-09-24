package com.parallax.wallpaper.utils

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.parallax.wallpaper.service.ParallaxWallpaperService

object LiveWallpaperHelper {

    fun launchLiveWallpaperChooser(context: Context): Boolean {
        return try {
            val componentName = ComponentName(context, ParallaxWallpaperService::class.java)
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT, componentName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            true
        } catch (e: Exception) {
            try {
                // Fallback for devices where ACTION_CHANGE_LIVE_WALLPAPER is not supported
                val fallbackIntent = Intent(WallpaperManager.ACTION_LIVE_WALLPAPER_CHOOSER).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallbackIntent)
                true
            } catch (fallbackEx: Exception) {
                fallbackEx.printStackTrace()
                false
            }
        }
    }
}
