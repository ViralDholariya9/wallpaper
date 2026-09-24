package com.parallax.wallpaper.data

import android.content.Context
import android.util.Log
import com.parallax.wallpaper.data.api.ApiClient
import com.parallax.wallpaper.data.api.AppConfigResponse
import com.parallax.wallpaper.weather.WeatherCondition
import com.parallax.wallpaper.weather.WeatherSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object RemoteConfigManager {

    private const val TAG = "RemoteConfigManager"

    private val _config = MutableStateFlow<AppConfigResponse?>(null)
    val config: StateFlow<AppConfigResponse?> = _config.asStateFlow()

    private val _isAnnouncementDismissed = MutableStateFlow(false)
    val isAnnouncementDismissed: StateFlow<Boolean> = _isAnnouncementDismissed.asStateFlow()

    fun dismissAnnouncement() {
        _isAnnouncementDismissed.value = true
    }

    fun sync(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.service.getAppConfig()
                if (response.success && response.data != null) {
                    val appConfig = response.data
                    _config.value = appConfig
                    Log.d(TAG, "Remote config fetched successfully: weather=${appConfig.weather?.effect}, announcement=${appConfig.announcement?.enabled}")

                    // 1. Sync Dynamic Weather Overlays with WeatherSyncManager
                    appConfig.weather?.let { weatherConfig ->
                        val weatherSyncManager = WeatherSyncManager.getInstance(context)
                        if (weatherConfig.enabled) {
                            val condition = when (weatherConfig.effect.lowercase().trim()) {
                                "rain" -> WeatherCondition.RAINY
                                "snow" -> WeatherCondition.SNOW
                                "thunder", "thunderstorm" -> WeatherCondition.THUNDERSTORM
                                "fog", "clouds", "cloudy" -> WeatherCondition.CLOUDY
                                else -> WeatherCondition.SUNNY
                            }
                            weatherSyncManager.setCondition(condition)
                            weatherSyncManager.setWeatherOverlayEnabled(true)
                            Log.d(TAG, "Weather sync applied: ${condition.name}, overlay=true")
                        } else {
                            weatherSyncManager.setWeatherOverlayEnabled(false)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to sync remote config: ${e.message}")
            }
        }
    }

    /**
     * Checks if a mandatory Play Store update is required based on minSupportedVersion.
     * Compares semantic versions (e.g., "1.0.0" vs "1.1.0").
     */
    fun isForceUpdateRequired(context: Context): Boolean {
        val forceConfig = _config.value?.forceUpdate ?: return false
        if (!forceConfig.enabled) return false

        val installedVersion = try {
            val pInfo = context.packageManager.getPackageInfo(context.packageName, 0)
            pInfo.versionName ?: "1.0.0"
        } catch (e: Exception) {
            "1.0.0"
        }

        return compareVersions(installedVersion, forceConfig.minSupportedVersion) < 0
    }

    /**
     * Compares two semantic version strings (e.g., "1.0.0" vs "1.1.0").
     */
    private fun compareVersions(v1: String, v2: String): Int {
        val parts1 = v1.split(".").mapNotNull { it.trim().toIntOrNull() }
        val parts2 = v2.split(".").mapNotNull { it.trim().toIntOrNull() }
        val maxLen = maxOf(parts1.size, parts2.size)

        for (i in 0 until maxLen) {
            val num1 = parts1.getOrElse(i) { 0 }
            val num2 = parts2.getOrElse(i) { 0 }
            if (num1 != num2) {
                return num1.compareTo(num2)
            }
        }
        return 0
    }
}
