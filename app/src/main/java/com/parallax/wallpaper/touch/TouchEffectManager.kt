package com.parallax.wallpaper.touch

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.google.gson.Gson
import com.parallax.wallpaper.model.TouchEffectPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object TouchEffectManager {

    private const val PREFS_NAME = "rewall_touch_effects_prefs"
    private const val KEY_ENABLED = "touch_effects_enabled"
    private const val KEY_ACTIVE_PRESET_JSON = "active_preset_json"
    private const val KEY_HAPTIC = "touch_haptic_enabled"
    private const val KEY_RADIUS = "touch_ripple_radius"
    private const val KEY_PARTICLES = "touch_particle_count"
    private const val KEY_SPEED = "touch_speed_multiplier"
    private const val KEY_GLOW = "touch_glow_intensity"

    private val gson = Gson()
    private var prefs: SharedPreferences? = null

    val defaultPreset = TouchEffectPreset(
        id = "touch_water_ripple",
        title = "🌊 Crystal Water Ripples",
        effectType = "WATER_RIPPLE",
        primaryColor = "#00E5FF",
        secondaryColor = "#0077FF",
        particleCount = 60,
        rippleRadius = 260f,
        speed = 1.0f,
        glowIntensity = 1.2f,
        isHapticEnabled = true,
        isPremium = false,
        previewUrl = "/uploads/touch/touch_water_ripple.svg",
        downloads = 28400,
        sortOrder = 1
    )

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _activePreset = MutableStateFlow(defaultPreset)
    val activePreset: StateFlow<TouchEffectPreset> = _activePreset.asStateFlow()

    private val _isHapticEnabled = MutableStateFlow(true)
    val isHapticEnabled: StateFlow<Boolean> = _isHapticEnabled.asStateFlow()

    private val _rippleRadius = MutableStateFlow(260f)
    val rippleRadius: StateFlow<Float> = _rippleRadius.asStateFlow()

    private val _particleCount = MutableStateFlow(60)
    val particleCount: StateFlow<Int> = _particleCount.asStateFlow()

    private val _speedMultiplier = MutableStateFlow(1.0f)
    val speedMultiplier: StateFlow<Float> = _speedMultiplier.asStateFlow()

    private val _glowIntensity = MutableStateFlow(1.2f)
    val glowIntensity: StateFlow<Float> = _glowIntensity.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            val p = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs = p

            _isEnabled.value = p.getBoolean(KEY_ENABLED, true)
            _isHapticEnabled.value = p.getBoolean(KEY_HAPTIC, true)
            _rippleRadius.value = p.getFloat(KEY_RADIUS, 260f)
            _particleCount.value = p.getInt(KEY_PARTICLES, 60)
            _speedMultiplier.value = p.getFloat(KEY_SPEED, 1.0f)
            _glowIntensity.value = p.getFloat(KEY_GLOW, 1.2f)

            val json = p.getString(KEY_ACTIVE_PRESET_JSON, null)
            if (!json.isNullOrBlank()) {
                try {
                    val saved = gson.fromJson(json, TouchEffectPreset::class.java)
                    if (saved != null) {
                        _activePreset.value = saved
                    }
                } catch (_: Exception) {
                    _activePreset.value = defaultPreset
                }
            } else {
                _activePreset.value = defaultPreset
            }
        }
    }

    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        prefs?.edit()?.putBoolean(KEY_ENABLED, enabled)?.apply()
    }

    fun setActivePreset(preset: TouchEffectPreset) {
        _activePreset.value = preset
        _rippleRadius.value = preset.rippleRadius
        _particleCount.value = preset.particleCount
        _speedMultiplier.value = preset.speed
        _glowIntensity.value = preset.glowIntensity
        _isHapticEnabled.value = preset.isHapticEnabled

        prefs?.edit()
            ?.putString(KEY_ACTIVE_PRESET_JSON, gson.toJson(preset))
            ?.putFloat(KEY_RADIUS, preset.rippleRadius)
            ?.putInt(KEY_PARTICLES, preset.particleCount)
            ?.putFloat(KEY_SPEED, preset.speed)
            ?.putFloat(KEY_GLOW, preset.glowIntensity)
            ?.putBoolean(KEY_HAPTIC, preset.isHapticEnabled)
            ?.apply()
    }

    fun setHapticEnabled(enabled: Boolean) {
        _isHapticEnabled.value = enabled
        prefs?.edit()?.putBoolean(KEY_HAPTIC, enabled)?.apply()
    }

    fun setRippleRadius(radius: Float) {
        _rippleRadius.value = radius
        prefs?.edit()?.putFloat(KEY_RADIUS, radius)?.apply()
    }

    fun setParticleCount(count: Int) {
        _particleCount.value = count
        prefs?.edit()?.putInt(KEY_PARTICLES, count)?.apply()
    }

    fun setSpeedMultiplier(speed: Float) {
        _speedMultiplier.value = speed
        prefs?.edit()?.putFloat(KEY_SPEED, speed)?.apply()
    }

    fun setGlowIntensity(intensity: Float) {
        _glowIntensity.value = intensity
        prefs?.edit()?.putFloat(KEY_GLOW, intensity)?.apply()
    }

    fun triggerHaptic(context: Context) {
        if (!_isHapticEnabled.value) return
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator?.vibrate(
                    VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE)
                )
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator?.vibrate(VibrationEffect.createOneShot(18, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator?.vibrate(18)
                }
            }
        } catch (_: Exception) {}
    }
}
