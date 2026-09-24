package com.parallax.wallpaper.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

enum class ChangeInterval(val title: String, val minutes: Long) {
    MIN_15("15 Minutes", 15),
    HOUR_1("1 Hour", 60),
    HOUR_3("3 Hours", 180),
    DAILY("Daily (24h)", 1440)
}

object AutoWallpaperManager {

    private const val WORK_NAME = "auto_wallpaper_work"
    private const val PREFS_NAME = "auto_wallpaper_prefs"

    fun scheduleAutoChanger(
        context: Context,
        interval: ChangeInterval,
        onlyFavorites: Boolean,
        targetScreen: String
    ) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putBoolean("enabled", true)
            .putLong("interval_minutes", interval.minutes)
            .putBoolean("only_favorites", onlyFavorites)
            .putString("target_screen", targetScreen)
            .apply()

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val periodicWork = PeriodicWorkRequestBuilder<AutoWallpaperWorker>(
            interval.minutes,
            TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            periodicWork
        )
    }

    fun triggerImmediateChange(context: Context) {
        val oneTimeWork = OneTimeWorkRequestBuilder<AutoWallpaperWorker>().build()
        WorkManager.getInstance(context).enqueue(oneTimeWork)
    }

    fun cancelAutoChanger(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean("enabled", false).apply()
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    fun isEnabled(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("enabled", false)
    }

    fun getSavedInterval(context: Context): ChangeInterval {
        val minutes = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getLong("interval_minutes", 60)
        return ChangeInterval.values().find { it.minutes == minutes } ?: ChangeInterval.HOUR_1
    }

    fun isOnlyFavorites(context: Context): Boolean {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean("only_favorites", false)
    }

    fun getTargetScreen(context: Context): String {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString("target_screen", "BOTH") ?: "BOTH"
    }
}
