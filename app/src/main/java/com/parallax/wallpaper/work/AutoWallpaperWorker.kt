package com.parallax.wallpaper.work

import android.app.WallpaperManager
import android.content.Context
import android.os.Build
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.parallax.wallpaper.data.WallpaperRepository
import com.parallax.wallpaper.utils.WallpaperHelper

class AutoWallpaperWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        return try {
            val repository = WallpaperRepository()
            val prefs = context.getSharedPreferences("auto_wallpaper_prefs", Context.MODE_PRIVATE)
            val onlyFavorites = prefs.getBoolean("only_favorites", false)
            val targetScreen = prefs.getString("target_screen", "BOTH") ?: "BOTH"

            val allWallpapers = repository.wallpapers.value
            val candidateWallpapers = if (onlyFavorites) {
                val favIds = repository.favorites.value
                allWallpapers.filter { favIds.contains(it.id) }.ifEmpty { allWallpapers }
            } else {
                allWallpapers
            }

            if (candidateWallpapers.isEmpty()) return Result.success()

            // Pick a random or next wallpaper
            val selected = candidateWallpapers.random()
            val bitmap = WallpaperHelper.fetchBitmap(context, selected.previewUrl)

            if (bitmap != null) {
                val wallpaperManager = WallpaperManager.getInstance(context)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    val flags = when (targetScreen) {
                        "HOME" -> WallpaperManager.FLAG_SYSTEM
                        "LOCK" -> WallpaperManager.FLAG_LOCK
                        else -> WallpaperManager.FLAG_SYSTEM or WallpaperManager.FLAG_LOCK
                    }
                    wallpaperManager.setBitmap(bitmap, null, true, flags)
                } else {
                    wallpaperManager.setBitmap(bitmap)
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }
}
