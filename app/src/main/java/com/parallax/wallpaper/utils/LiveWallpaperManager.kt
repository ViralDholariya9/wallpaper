package com.parallax.wallpaper.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.util.Log
import com.parallax.wallpaper.gl.GLTextureHelper
import com.parallax.wallpaper.model.WallpaperItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

object LiveWallpaperManager {

    private const val TAG = "LiveWallpaperManager"
    private const val PREFS_NAME = "active_live_wallpaper_prefs"

    const val ACTION_RELOAD_WALLPAPER = "com.parallax.wallpaper.ACTION_RELOAD_WALLPAPER"
    const val ACTION_UPDATE_SENSITIVITY = "com.parallax.wallpaper.ACTION_UPDATE_SENSITIVITY"

    private const val KEY_WALLPAPER_ID = "active_id"
    private const val KEY_WALLPAPER_TITLE = "active_title"
    private const val KEY_TIMESTAMP = "active_timestamp"
    private const val KEY_SENSITIVITY = "active_sensitivity"
    const val KEY_DOUBLE_TAP_SWITCH = "double_tap_switch_enabled"
    const val KEY_HAPTICS_ENABLED = "haptics_enabled"

    private const val BASE_IMAGE_FILENAME = "active_live_base.jpg"
    private const val DEPTH_IMAGE_FILENAME = "active_live_depth.png"

    private const val MAX_TEXTURE_DIMENSION = 2048

    /**
     * Downloads/fetches the full wallpaper bitmap, pre-computes the depth map,
     * persists both files locally in app internal storage, updates metadata,
     * and broadcasts a reload signal to ParallaxWallpaperService.
     */
    suspend fun setActiveWallpaper(
        context: Context,
        wallpaper: WallpaperItem
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val url = wallpaper.fullUrl.ifBlank { wallpaper.previewUrl }
            val originalBitmap = WallpaperHelper.fetchBitmap(context, url)
                ?: WallpaperHelper.fetchBitmap(context, wallpaper.previewUrl)
                ?: return@withContext false

            return@withContext processAndSaveActiveWallpaper(
                context = context,
                sourceBitmap = originalBitmap,
                wallpaperId = wallpaper.id,
                title = wallpaper.title
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error setting active live wallpaper", e)
            false
        }
    }

    /**
     * Directly persists an in-memory Bitmap (e.g., from WallpaperEditorScreen or AI Studio),
     * generates its depth map, and updates the live wallpaper service.
     */
    suspend fun setActiveBitmap(
        context: Context,
        bitmap: Bitmap,
        title: String
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val wallpaperId = "custom_${System.currentTimeMillis()}"
            return@withContext processAndSaveActiveWallpaper(
                context = context,
                sourceBitmap = bitmap,
                wallpaperId = wallpaperId,
                title = title
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error setting active bitmap for live wallpaper", e)
            false
        }
    }

    private fun processAndSaveActiveWallpaper(
        context: Context,
        sourceBitmap: Bitmap,
        wallpaperId: String,
        title: String
    ): Boolean {
        // 1. Scale down safely if exceeding max OpenGL texture dimension (prevents OutOfMemory on budget devices)
        val scaledBaseBitmap = scaleBitmapToMaxDimension(sourceBitmap, MAX_TEXTURE_DIMENSION)

        // 2. Synthesize high-fidelity depth map
        val depthBitmap = GLTextureHelper.generateDepthMap(scaledBaseBitmap)

        // 3. Persist base image file
        val baseFile = File(context.filesDir, BASE_IMAGE_FILENAME)
        FileOutputStream(baseFile).use { out ->
            scaledBaseBitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }

        // 4. Persist depth map file (lossless PNG to preserve depth gradient fidelity)
        val depthFile = File(context.filesDir, DEPTH_IMAGE_FILENAME)
        FileOutputStream(depthFile).use { out ->
            depthBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }

        val newTimestamp = System.currentTimeMillis()

        // 5. Update metadata in SharedPreferences
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_WALLPAPER_ID, wallpaperId)
            .putString(KEY_WALLPAPER_TITLE, title)
            .putLong(KEY_TIMESTAMP, newTimestamp)
            .apply()

        // 6. Broadcast reload signal to active ParallaxWallpaperService instance
        notifyWallpaperReload(context)

        return true
    }

    /**
     * Loads the active base bitmap and depth map from disk for OpenGL texture creation.
     */
    fun getActiveWallpaperBitmap(context: Context): Pair<Bitmap?, Bitmap?> {
        val baseFile = File(context.filesDir, BASE_IMAGE_FILENAME)
        val depthFile = File(context.filesDir, DEPTH_IMAGE_FILENAME)

        if (!baseFile.exists()) {
            return Pair(null, null)
        }

        return try {
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inMutable = false
            }

            val baseBitmap = BitmapFactory.decodeFile(baseFile.absolutePath, options)
            val depthBitmap = if (depthFile.exists()) {
                BitmapFactory.decodeFile(depthFile.absolutePath, options)
            } else {
                baseBitmap?.let { GLTextureHelper.generateDepthMap(it) }
            }

            Pair(baseBitmap, depthBitmap)
        } catch (e: Exception) {
            Log.e(TAG, "Error loading active live wallpaper bitmaps from disk", e)
            Pair(null, null)
        }
    }

    fun hasActiveWallpaper(context: Context): Boolean {
        val baseFile = File(context.filesDir, BASE_IMAGE_FILENAME)
        return baseFile.exists() && baseFile.length() > 0
    }

    fun getSavedTimestamp(context: Context): Long {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_TIMESTAMP, 0L)
    }

    fun getActiveWallpaperTitle(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_WALLPAPER_TITLE, "Custom Parallax 3D") ?: "Custom Parallax 3D"
    }

    fun getSensitivity(context: Context): Float {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getFloat(KEY_SENSITIVITY, 1.2f)
    }

    fun setSensitivity(context: Context, sensitivity: Float) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putFloat(KEY_SENSITIVITY, sensitivity)
            .apply()

        notifySensitivityChanged(context)
    }

    fun getActiveWallpaperId(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_WALLPAPER_ID, "") ?: ""
    }

    fun isDoubleTapEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_DOUBLE_TAP_SWITCH, true)
    }

    fun setDoubleTapEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_DOUBLE_TAP_SWITCH, enabled)
            .apply()
    }

    fun isHapticsEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_HAPTICS_ENABLED, true)
    }

    fun setHapticsEnabled(context: Context, enabled: Boolean) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_HAPTICS_ENABLED, enabled)
            .apply()
    }

    /**
     * Cycles to the next available 3D parallax wallpaper in the collection,
     * fetches its high-res source, generates depth map, saves active files,
     * and broadcasts reload signal.
     */
    suspend fun cycleToNextWallpaper(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val repository = com.parallax.wallpaper.data.WallpaperRepository()
            val candidateWallpapers = repository.wallpapers.value
                .filter { it.isParallax }
                .ifEmpty { repository.wallpapers.value }

            if (candidateWallpapers.isEmpty()) return@withContext false

            val currentId = getActiveWallpaperId(context)
            val currentIndex = candidateWallpapers.indexOfFirst { it.id == currentId }
            val nextIndex = if (currentIndex in candidateWallpapers.indices) {
                (currentIndex + 1) % candidateWallpapers.size
            } else {
                0
            }

            val nextWallpaper = candidateWallpapers[nextIndex]
            Log.d(TAG, "Cycling to next wallpaper: ${nextWallpaper.title} (${nextWallpaper.id})")
            return@withContext setActiveWallpaper(context, nextWallpaper)
        } catch (e: Exception) {
            Log.e(TAG, "Error cycling live wallpaper", e)
            false
        }
    }

    fun notifyWallpaperReload(context: Context) {
        try {
            val intent = Intent(ACTION_RELOAD_WALLPAPER).apply {
                setPackage(context.packageName)
            }
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to broadcast ACTION_RELOAD_WALLPAPER", e)
        }
    }

    fun notifySensitivityChanged(context: Context) {
        try {
            val intent = Intent(ACTION_UPDATE_SENSITIVITY).apply {
                setPackage(context.packageName)
            }
            context.sendBroadcast(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to broadcast ACTION_UPDATE_SENSITIVITY", e)
        }
    }

    private fun scaleBitmapToMaxDimension(source: Bitmap, maxDim: Int): Bitmap {
        val w = source.width
        val h = source.height
        if (w <= maxDim && h <= maxDim) return source

        val scale = if (w > h) maxDim.toFloat() / w else maxDim.toFloat() / h
        val matrix = Matrix().apply { postScale(scale, scale) }
        return Bitmap.createBitmap(source, 0, 0, w, h, matrix, true)
    }
}
