package com.parallax.wallpaper.fingerprint

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.google.gson.Gson
import com.parallax.wallpaper.model.FingerprintPreset
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object FingerprintAnimationManager {

    private const val PREFS_NAME = "rewall_fingerprint_prefs"
    private const val KEY_ENABLED = "fingerprint_effects_enabled"
    private const val KEY_ACTIVE_PRESET_JSON = "active_preset_json"
    private const val KEY_HAPTIC = "fingerprint_haptic_enabled"
    private const val KEY_SOUND = "fingerprint_sound_enabled"
    private const val KEY_Y_POS = "fingerprint_y_pos"
    private const val KEY_SCALE = "fingerprint_scale"

    private val gson = Gson()
    private var prefs: SharedPreferences? = null

    val defaultPreset = FingerprintPreset(
        id = "fp_cyber_matrix",
        title = "⚡ Cyber Matrix Biometric Scanner",
        category = "CYBERPUNK",
        animationType = "CYBER_MATRIX",
        primaryColor = "#00E5FF",
        secondaryColor = "#7000FF",
        accentGlow = "#00FFAA",
        animationSpeed = 1.2f,
        sensorScale = 1.0f,
        yPositionPercent = 78,
        hapticEnabled = true,
        soundEnabled = true,
        isPremium = false,
        previewUrl = "/uploads/fingerprint/fp_cyber_matrix.svg",
        description = "Holographic cyberpunk HUD reticle with rotating concentric rings, laser sweep line, and neon particle burst.",
        downloads = 48500,
        sortOrder = 1
    )

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _activePreset = MutableStateFlow(defaultPreset)
    val activePreset: StateFlow<FingerprintPreset> = _activePreset.asStateFlow()

    private val _isHapticEnabled = MutableStateFlow(true)
    val isHapticEnabled: StateFlow<Boolean> = _isHapticEnabled.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private val _yPositionPercent = MutableStateFlow(78)
    val yPositionPercent: StateFlow<Int> = _yPositionPercent.asStateFlow()

    private val _sensorScale = MutableStateFlow(1.0f)
    val sensorScale: StateFlow<Float> = _sensorScale.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            val p = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs = p

            _isEnabled.value = p.getBoolean(KEY_ENABLED, true)
            _isHapticEnabled.value = p.getBoolean(KEY_HAPTIC, true)
            _isSoundEnabled.value = p.getBoolean(KEY_SOUND, true)
            _yPositionPercent.value = p.getInt(KEY_Y_POS, 78)
            _sensorScale.value = p.getFloat(KEY_SCALE, 1.0f)

            val json = p.getString(KEY_ACTIVE_PRESET_JSON, null)
            if (!json.isNullOrBlank()) {
                try {
                    val saved = gson.fromJson(json, FingerprintPreset::class.java)
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

    fun setActivePreset(preset: FingerprintPreset) {
        _activePreset.value = preset
        _yPositionPercent.value = preset.yPositionPercent
        _sensorScale.value = preset.sensorScale
        _isHapticEnabled.value = preset.hapticEnabled
        _isSoundEnabled.value = preset.soundEnabled

        prefs?.edit()
            ?.putString(KEY_ACTIVE_PRESET_JSON, gson.toJson(preset))
            ?.putInt(KEY_Y_POS, preset.yPositionPercent)
            ?.putFloat(KEY_SCALE, preset.sensorScale)
            ?.putBoolean(KEY_HAPTIC, preset.hapticEnabled)
            ?.putBoolean(KEY_SOUND, preset.soundEnabled)
            ?.apply()
    }

    fun setYPositionPercent(yPos: Int) {
        val clamped = yPos.coerceIn(50, 95)
        _yPositionPercent.value = clamped
        prefs?.edit()?.putInt(KEY_Y_POS, clamped)?.apply()
    }

    fun setSensorScale(scale: Float) {
        val clamped = scale.coerceIn(0.6f, 1.8f)
        _sensorScale.value = clamped
        prefs?.edit()?.putFloat(KEY_SCALE, clamped)?.apply()
    }

    fun setHapticEnabled(enabled: Boolean) {
        _isHapticEnabled.value = enabled
        prefs?.edit()?.putBoolean(KEY_HAPTIC, enabled)?.apply()
    }

    fun setSoundEnabled(enabled: Boolean) {
        _isSoundEnabled.value = enabled
        prefs?.edit()?.putBoolean(KEY_SOUND, enabled)?.apply()
    }

    fun performHapticScan(context: Context) {
        if (!_isHapticEnabled.value) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(20, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (_: Exception) {}
    }

    fun performHapticUnlock(context: Context) {
        if (!_isHapticEnabled.value) return
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 35, 50, 55)
                val amplitudes = intArrayOf(0, 180, 0, 255)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, amplitudes, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 35, 50, 55), -1)
            }
        } catch (_: Exception) {}
    }
}
