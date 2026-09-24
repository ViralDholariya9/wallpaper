package com.parallax.wallpaper.weather

import android.content.Context
import android.content.Intent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Calendar

enum class WeatherCondition(val displayName: String, val emoji: String) {
    SUNNY("Sunny Clear", "☀️"),
    RAINY("Rain Storm", "🌧️"),
    SNOW("Winter Snow", "❄️"),
    CLOUDY("Misty Clouds", "☁️"),
    THUNDERSTORM("Thunderstorm", "⚡")
}

class WeatherSyncManager(private val context: Context) {

    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _isWeatherOverlayEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_WEATHER_OVERLAY_ENABLED, true)
    )
    val isWeatherOverlayEnabled: StateFlow<Boolean> = _isWeatherOverlayEnabled.asStateFlow()
    val isWeatherSyncEnabled: StateFlow<Boolean> get() = _isWeatherOverlayEnabled.asStateFlow()

    private val _isAutoSyncEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_AUTO_SYNC_ENABLED, true)
    )
    val isAutoSyncEnabled: StateFlow<Boolean> = _isAutoSyncEnabled.asStateFlow()

    private val _currentWeather = MutableStateFlow(loadInitialCondition())
    val currentWeather: StateFlow<WeatherCondition> = _currentWeather.asStateFlow()

    private fun loadInitialCondition(): WeatherCondition {
        val isAuto = prefs.getBoolean(KEY_AUTO_SYNC_ENABLED, true)
        if (isAuto) {
            return computeDiurnalCondition()
        }
        val savedName = prefs.getString(KEY_SELECTED_CONDITION, WeatherCondition.RAINY.name)
        return try {
            WeatherCondition.valueOf(savedName ?: WeatherCondition.RAINY.name)
        } catch (e: Exception) {
            WeatherCondition.RAINY
        }
    }

    private fun computeDiurnalCondition(): WeatherCondition {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour in 6..10 -> WeatherCondition.SUNNY
            hour in 11..15 -> WeatherCondition.CLOUDY
            hour in 16..19 -> WeatherCondition.RAINY
            hour in 20..22 -> WeatherCondition.THUNDERSTORM
            else -> WeatherCondition.SNOW
        }
    }

    fun setWeatherOverlayEnabled(enabled: Boolean) {
        _isWeatherOverlayEnabled.value = enabled
        prefs.edit().putBoolean(KEY_WEATHER_OVERLAY_ENABLED, enabled).apply()
        broadcastWeatherUpdate(context)
    }

    fun setWeatherSyncEnabled(enabled: Boolean) {
        setWeatherOverlayEnabled(enabled)
    }

    fun setAutoSyncEnabled(enabled: Boolean) {
        _isAutoSyncEnabled.value = enabled
        prefs.edit().putBoolean(KEY_AUTO_SYNC_ENABLED, enabled).apply()
        if (enabled) {
            val autoCondition = computeDiurnalCondition()
            _currentWeather.value = autoCondition
            prefs.edit().putString(KEY_SELECTED_CONDITION, autoCondition.name).apply()
        }
        broadcastWeatherUpdate(context)
    }

    fun setCondition(condition: WeatherCondition) {
        _isAutoSyncEnabled.value = false
        prefs.edit().putBoolean(KEY_AUTO_SYNC_ENABLED, false).apply()
        _currentWeather.value = condition
        prefs.edit().putString(KEY_SELECTED_CONDITION, condition.name).apply()
        broadcastWeatherUpdate(context)
    }

    companion object {
        const val ACTION_UPDATE_WEATHER = "com.parallax.wallpaper.ACTION_UPDATE_WEATHER"
        private const val PREFS_NAME = "rewall_weather_prefs"
        private const val KEY_WEATHER_OVERLAY_ENABLED = "weather_overlay_enabled"
        private const val KEY_AUTO_SYNC_ENABLED = "weather_auto_sync_enabled"
        private const val KEY_SELECTED_CONDITION = "weather_selected_condition"

        @Volatile
        private var INSTANCE: WeatherSyncManager? = null

        fun getInstance(context: Context): WeatherSyncManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: WeatherSyncManager(context.applicationContext).also { INSTANCE = it }
            }
        }

        fun isOverlayEnabled(context: Context): Boolean {
            return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_WEATHER_OVERLAY_ENABLED, true)
        }

        fun getActiveCondition(context: Context): WeatherCondition {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val isAuto = prefs.getBoolean(KEY_AUTO_SYNC_ENABLED, true)
            if (isAuto) {
                val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
                return when {
                    hour in 6..10 -> WeatherCondition.SUNNY
                    hour in 11..15 -> WeatherCondition.CLOUDY
                    hour in 16..19 -> WeatherCondition.RAINY
                    hour in 20..22 -> WeatherCondition.THUNDERSTORM
                    else -> WeatherCondition.SNOW
                }
            }
            val savedName = prefs.getString(KEY_SELECTED_CONDITION, WeatherCondition.RAINY.name)
            return try {
                WeatherCondition.valueOf(savedName ?: WeatherCondition.RAINY.name)
            } catch (e: Exception) {
                WeatherCondition.RAINY
            }
        }

        fun broadcastWeatherUpdate(context: Context) {
            try {
                val intent = Intent(ACTION_UPDATE_WEATHER).apply {
                    setPackage(context.packageName)
                }
                context.sendBroadcast(intent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
