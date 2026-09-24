package com.parallax.wallpaper.power

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.PowerManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Intelligent Zero-Battery Optimization Engine.
 * Features:
 * 1. Automatically detects system PowerSaveMode & Battery Low states.
 * 2. Manages User Ultra-Battery Saver preference.
 * 3. Computes adaptive frame-rate delay (60 FPS active -> 15-20 FPS still -> 0 FPS sleep).
 */
class BatteryOptimizer private constructor(private val context: Context) {

    private val prefs = context.getSharedPreferences("rewall_power_prefs", Context.MODE_PRIVATE)
    private val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager

    private val _isUltraBatterySaverEnabled = MutableStateFlow(
        prefs.getBoolean(KEY_ULTRA_BATTERY_SAVER, false)
    )
    val isUltraBatterySaverEnabled: StateFlow<Boolean> = _isUltraBatterySaverEnabled.asStateFlow()

    private val _isSystemPowerSaveActive = MutableStateFlow(
        powerManager?.isPowerSaveMode ?: false
    )
    val isSystemPowerSaveActive: StateFlow<Boolean> = _isSystemPowerSaveActive.asStateFlow()

    init {
        registerSystemPowerReceiver()
    }

    private fun registerSystemPowerReceiver() {
        try {
            val filter = IntentFilter().apply {
                addAction(PowerManager.ACTION_POWER_SAVE_MODE_CHANGED)
                addAction(Intent.ACTION_BATTERY_LOW)
                addAction(Intent.ACTION_BATTERY_OKAY)
            }
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    when (intent?.action) {
                        PowerManager.ACTION_POWER_SAVE_MODE_CHANGED -> {
                            _isSystemPowerSaveActive.value = powerManager?.isPowerSaveMode ?: false
                        }
                        Intent.ACTION_BATTERY_LOW -> {
                            _isSystemPowerSaveActive.value = true
                        }
                        Intent.ACTION_BATTERY_OKAY -> {
                            _isSystemPowerSaveActive.value = powerManager?.isPowerSaveMode ?: false
                        }
                    }
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
            } else {
                context.registerReceiver(receiver, filter)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setUltraBatterySaverEnabled(enabled: Boolean) {
        _isUltraBatterySaverEnabled.value = enabled
        prefs.edit().putBoolean(KEY_ULTRA_BATTERY_SAVER, enabled).apply()
    }

    /**
     * Determines the optimal frame delay (in milliseconds) for the render loop.
     * @param isDeviceMoving whether the gyroscope/accelerometer detects active motion
     * @return delay in milliseconds (16ms = ~60 FPS, 60ms = ~16 FPS, 120ms = eco sleep)
     */
    fun getAdaptiveFrameDelayMs(isDeviceMoving: Boolean): Long {
        val isEcoMode = _isUltraBatterySaverEnabled.value || _isSystemPowerSaveActive.value

        return when {
            // If Ultra Battery Saver is ON and device is completely still: sleep 100ms (10 FPS idle)
            isEcoMode && !isDeviceMoving -> 100L
            // If Ultra Battery Saver is ON but device is moving: cap at 30 FPS (33ms)
            isEcoMode && isDeviceMoving -> 33L
            // Normal mode but resting still on a table: drop to 20 FPS (50ms) for breathing drift
            !isEcoMode && !isDeviceMoving -> 48L
            // Active motion in normal mode: silky smooth 60 FPS (16ms)
            else -> 16L
        }
    }

    companion object {
        private const val KEY_ULTRA_BATTERY_SAVER = "ultra_battery_saver_enabled"

        @Volatile
        private var instance: BatteryOptimizer? = null

        fun getInstance(context: Context): BatteryOptimizer {
            return instance ?: synchronized(this) {
                instance ?: BatteryOptimizer(context.applicationContext).also { instance = it }
            }
        }
    }
}
