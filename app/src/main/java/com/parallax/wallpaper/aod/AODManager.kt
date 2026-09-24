package com.parallax.wallpaper.aod

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.SharedPreferences
import android.os.BatteryManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Singleton manager for Always-On Display (AOD) settings, real-time telemetry,
 * and anti-burn-in pixel shifting.
 */
class AODManager private constructor(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("rewall_aod_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    // Default Fallback Clock
    private val defaultClock = AODClockFace(
        id = "aod_cyberpunk_2077",
        title = "⚡ Cyberpunk Neon HUD 2077",
        clockType = AODClockType.CYBERPUNK_DIGITAL,
        accentColorHex = "#00E5FF",
        glowColorHex = "#7000FF",
        textColorHex = "#FFFFFF",
        backgroundColorHex = "#000000",
        dialStyle = "futuristic_hud",
        hasBatteryWidget = true,
        hasDateWidget = true,
        hasStepsWidget = true,
        hasWeatherWidget = true,
        downloads = 6840
    )

    private val _isEnabled = MutableStateFlow(prefs.getBoolean(KEY_AOD_ENABLED, false))
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _activeClock = MutableStateFlow(defaultClock)
    val activeClock: StateFlow<AODClockFace> = _activeClock.asStateFlow()

    private val _showBattery = MutableStateFlow(prefs.getBoolean(KEY_SHOW_BATTERY, true))
    val showBattery: StateFlow<Boolean> = _showBattery.asStateFlow()

    private val _showDate = MutableStateFlow(prefs.getBoolean(KEY_SHOW_DATE, true))
    val showDate: StateFlow<Boolean> = _showDate.asStateFlow()

    private val _showSteps = MutableStateFlow(prefs.getBoolean(KEY_SHOW_STEPS, true))
    val showSteps: StateFlow<Boolean> = _showSteps.asStateFlow()

    private val _showWeather = MutableStateFlow(prefs.getBoolean(KEY_SHOW_WEATHER, true))
    val showWeather: StateFlow<Boolean> = _showWeather.asStateFlow()

    private val _brightness = MutableStateFlow(prefs.getFloat(KEY_BRIGHTNESS, 0.7f))
    val brightness: StateFlow<Float> = _brightness.asStateFlow()

    private val _burnInOffset = MutableStateFlow(AODBurnInOffset(0f, 0f))
    val burnInOffset: StateFlow<AODBurnInOffset> = _burnInOffset.asStateFlow()

    private val _telemetry = MutableStateFlow(AODTelemetry())
    val telemetry: StateFlow<AODTelemetry> = _telemetry.asStateFlow()

    private var burnInJob: Job? = null
    private var batteryReceiver: BroadcastReceiver? = null

    init {
        registerBatteryReceiver()
        startAntiBurnInLoop()
    }

    fun setAODEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        prefs.edit().putBoolean(KEY_AOD_ENABLED, enabled).apply()
    }

    fun setActiveClock(clock: AODClockFace) {
        _activeClock.value = clock
        _showBattery.value = clock.hasBatteryWidget
        _showDate.value = clock.hasDateWidget
        _showSteps.value = clock.hasStepsWidget
        _showWeather.value = clock.hasWeatherWidget
        prefs.edit()
            .putString(KEY_ACTIVE_CLOCK_ID, clock.id)
            .putString(KEY_ACTIVE_CLOCK_TITLE, clock.title)
            .putString(KEY_ACTIVE_CLOCK_TYPE, clock.clockType.name)
            .putString(KEY_ACTIVE_CLOCK_ACCENT, clock.accentColorHex)
            .putString(KEY_ACTIVE_CLOCK_GLOW, clock.glowColorHex)
            .apply()
    }

    fun setShowBattery(show: Boolean) {
        _showBattery.value = show
        prefs.edit().putBoolean(KEY_SHOW_BATTERY, show).apply()
    }

    fun setShowDate(show: Boolean) {
        _showDate.value = show
        prefs.edit().putBoolean(KEY_SHOW_DATE, show).apply()
    }

    fun setShowSteps(show: Boolean) {
        _showSteps.value = show
        prefs.edit().putBoolean(KEY_SHOW_STEPS, show).apply()
    }

    fun setShowWeather(show: Boolean) {
        _showWeather.value = show
        prefs.edit().putBoolean(KEY_SHOW_WEATHER, show).apply()
    }

    fun setBrightness(value: Float) {
        val clamped = value.coerceIn(0.1f, 1.0f)
        _brightness.value = clamped
        prefs.edit().putFloat(KEY_BRIGHTNESS, clamped).apply()
    }

    /**
     * Manually triggers pixel shift offset cycle for testing
     */
    fun triggerManualPixelShift() {
        val offsets = listOf(
            AODBurnInOffset(0f, 0f),
            AODBurnInOffset(6f, 5f),
            AODBurnInOffset(-5f, 7f),
            AODBurnInOffset(7f, -4f),
            AODBurnInOffset(-6f, -6f)
        )
        val current = _burnInOffset.value
        val currentIndex = offsets.indexOf(current)
        val nextIndex = if (currentIndex in 0 until offsets.size - 1) currentIndex + 1 else 0
        _burnInOffset.value = offsets[nextIndex]
    }

    /**
     * Shifting pixel loop to protect AMOLED displays from image burn-in.
     * Shifts 4-8 pixels every 60 seconds.
     */
    private fun startAntiBurnInLoop() {
        burnInJob?.cancel()
        burnInJob = scope.launch {
            val shifts = listOf(
                AODBurnInOffset(0f, 0f),
                AODBurnInOffset(5f, 4f),
                AODBurnInOffset(-4f, 6f),
                AODBurnInOffset(6f, -3f),
                AODBurnInOffset(-5f, -5f)
            )
            var idx = 0
            while (isActive) {
                delay(60_000L) // Shift every 1 minute
                idx = (idx + 1) % shifts.size
                _burnInOffset.value = shifts[idx]
            }
        }
    }

    private fun registerBatteryReceiver() {
        try {
            batteryReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent?) {
                    if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1)
                        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
                                status == BatteryManager.BATTERY_STATUS_FULL

                        val batteryPct = if (level >= 0 && scale > 0) {
                            (level * 100 / scale)
                        } else 88

                        _telemetry.value = _telemetry.value.copy(
                            batteryPercent = batteryPct,
                            isCharging = isCharging
                        )
                    }
                }
            }
            context.registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        } catch (_: Exception) {
            // Safe fallback
        }
    }

    companion object {
        private const val KEY_AOD_ENABLED = "aod_enabled"
        private const val KEY_ACTIVE_CLOCK_ID = "active_clock_id"
        private const val KEY_ACTIVE_CLOCK_TITLE = "active_clock_title"
        private const val KEY_ACTIVE_CLOCK_TYPE = "active_clock_type"
        private const val KEY_ACTIVE_CLOCK_ACCENT = "active_clock_accent"
        private const val KEY_ACTIVE_CLOCK_GLOW = "active_clock_glow"
        private const val KEY_SHOW_BATTERY = "show_battery"
        private const val KEY_SHOW_DATE = "show_date"
        private const val KEY_SHOW_STEPS = "show_steps"
        private const val KEY_SHOW_WEATHER = "show_weather"
        private const val KEY_BRIGHTNESS = "brightness"

        @Volatile
        private var instance: AODManager? = null

        fun init(context: Context): AODManager = getInstance(context)

        fun getInstance(context: Context): AODManager {
            return instance ?: synchronized(this) {
                instance ?: AODManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
