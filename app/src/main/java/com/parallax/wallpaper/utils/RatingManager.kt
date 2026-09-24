package com.parallax.wallpaper.utils

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.parallax.wallpaper.data.api.RatingPromptConfigResponse

object RatingManager {
    private const val PREFS_NAME = "rewall_rating_prefs"
    private const val KEY_DOWNLOAD_APPLY_COUNT = "download_apply_count"
    private const val KEY_HAS_RATED = "has_rated_or_never_ask"
    private const val KEY_LAST_DISMISSED = "last_prompt_dismissed_time"

    /**
     * Call this whenever a user successfully downloads or applies a wallpaper.
     * Returns true if the rating prompt condition is satisfied and the popup should be displayed.
     */
    fun recordDownloadOrApply(context: Context, config: RatingPromptConfigResponse?): Boolean {
        if (config == null || !config.enabled) return false

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val hasRated = prefs.getBoolean(KEY_HAS_RATED, false)
        if (hasRated) return false

        val currentCount = prefs.getInt(KEY_DOWNLOAD_APPLY_COUNT, 0) + 1
        prefs.edit().putInt(KEY_DOWNLOAD_APPLY_COUNT, currentCount).apply()

        // Check threshold
        val target = if (config.triggerDownloads > 0) config.triggerDownloads else 3
        if (currentCount >= target) {
            // Check if dismissed recently (cooldown of at least 1 day)
            val lastDismissed = prefs.getLong(KEY_LAST_DISMISSED, 0L)
            val oneDayMillis = 24 * 60 * 60 * 1000L
            if (System.currentTimeMillis() - lastDismissed >= oneDayMillis) {
                return true
            }
        }
        return false
    }

    /**
     * User clicked the positive "Rate 5 Stars" button.
     * Sets permanent flag and launches Google Play Store review page.
     */
    fun markRated(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_HAS_RATED, true).apply()

        val packageName = context.packageName
        val marketIntent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName")).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(marketIntent)
        } catch (e: Exception) {
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName")).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            try {
                context.startActivity(webIntent)
            } catch (ignored: Exception) { }
        }
    }

    /**
     * User clicked "Maybe Later" or dismissed the dialog.
     * Records timestamp and resets count for next interval.
     */
    fun markDismissed(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putLong(KEY_LAST_DISMISSED, System.currentTimeMillis())
            .putInt(KEY_DOWNLOAD_APPLY_COUNT, 0)
            .apply()
    }
}
