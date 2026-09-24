package com.parallax.wallpaper.edge

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import com.google.gson.Gson
import com.parallax.wallpaper.data.api.EdgeLightingPresetItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object EdgeLightingManager {

    private const val PREFS_NAME = "rewall_edge_lighting_prefs"
    private const val KEY_ENABLED = "edge_lighting_enabled"
    private const val KEY_ACTIVE_PRESET_JSON = "active_preset_json"
    private const val KEY_THICKNESS = "custom_thickness"
    private const val KEY_SPEED = "custom_speed"
    private const val KEY_RADIUS = "custom_radius"
    private const val KEY_PUNCH_HOLE = "custom_punch_hole"
    private const val KEY_TRIGGER_ALWAYS = "trigger_always_on"
    private const val KEY_TRIGGER_NOTIF = "trigger_notification"
    private const val KEY_TRIGGER_CALL = "trigger_call"
    private const val KEY_TRIGGER_MUSIC = "trigger_music"

    private val gson = Gson()
    private var prefs: SharedPreferences? = null

    // Curated default preset if offline
    val defaultPreset = EdgeLightingPresetItem(
        id = "el_rgb_rainbow",
        title = "🌈 RGB 360° Rainbow Wave",
        category = "RAINBOW",
        animationType = "rainbow_wave",
        colors = listOf("#FF0055", "#FF7700", "#FFE600", "#00FF66", "#00E5FF", "#7000FF", "#FF0055"),
        speed = 1.2f,
        borderSize = 6,
        cornerRadius = 32,
        punchHoleRadius = 0,
        glowSpread = 14,
        previewUrl = "/uploads/edge/thumb_rgb_rainbow.svg",
        isPremium = false,
        downloads = 3820,
        isActive = true,
        sortOrder = 1,
        createdAt = 0L
    )

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _activePreset = MutableStateFlow(defaultPreset)
    val activePreset: StateFlow<EdgeLightingPresetItem> = _activePreset.asStateFlow()

    private val _borderThicknessDp = MutableStateFlow(6)
    val borderThicknessDp: StateFlow<Int> = _borderThicknessDp.asStateFlow()

    private val _speedMultiplier = MutableStateFlow(1.2f)
    val speedMultiplier: StateFlow<Float> = _speedMultiplier.asStateFlow()

    private val _cornerRadiusDp = MutableStateFlow(32)
    val cornerRadiusDp: StateFlow<Int> = _cornerRadiusDp.asStateFlow()

    private val _punchHoleRadiusDp = MutableStateFlow(0)
    val punchHoleRadiusDp: StateFlow<Int> = _punchHoleRadiusDp.asStateFlow()

    private val _triggerAlwaysOn = MutableStateFlow(true)
    val triggerAlwaysOn: StateFlow<Boolean> = _triggerAlwaysOn.asStateFlow()

    private val _triggerOnNotification = MutableStateFlow(true)
    val triggerOnNotification: StateFlow<Boolean> = _triggerOnNotification.asStateFlow()

    private val _triggerOnCall = MutableStateFlow(true)
    val triggerOnCall: StateFlow<Boolean> = _triggerOnCall.asStateFlow()

    private val _triggerOnMusic = MutableStateFlow(false)
    val triggerOnMusic: StateFlow<Boolean> = _triggerOnMusic.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            val p = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs = p

            _isEnabled.value = p.getBoolean(KEY_ENABLED, true)
            _borderThicknessDp.value = p.getInt(KEY_THICKNESS, 6)
            _speedMultiplier.value = p.getFloat(KEY_SPEED, 1.2f)
            _cornerRadiusDp.value = p.getInt(KEY_RADIUS, 32)
            _punchHoleRadiusDp.value = p.getInt(KEY_PUNCH_HOLE, 0)

            _triggerAlwaysOn.value = p.getBoolean(KEY_TRIGGER_ALWAYS, true)
            _triggerOnNotification.value = p.getBoolean(KEY_TRIGGER_NOTIF, true)
            _triggerOnCall.value = p.getBoolean(KEY_TRIGGER_CALL, true)
            _triggerOnMusic.value = p.getBoolean(KEY_TRIGGER_MUSIC, false)

            val json = p.getString(KEY_ACTIVE_PRESET_JSON, null)
            if (!json.isNullOrBlank()) {
                try {
                    val saved = gson.fromJson(json, EdgeLightingPresetItem::class.java)
                    if (saved != null) {
                        _activePreset.value = saved
                    }
                } catch (_: Exception) {
                    _activePreset.value = defaultPreset
                }
            } else {
                _activePreset.value = defaultPreset
            }

            // Sync overlay if permission is granted
            if (_isEnabled.value && canDrawOverlays(context) && _triggerAlwaysOn.value) {
                startOverlayService(context)
            }
        }
    }

    fun canDrawOverlays(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        init(context)
        prefs?.edit()?.putBoolean(KEY_ENABLED, enabled)?.apply()
        _isEnabled.value = enabled
        if (enabled && canDrawOverlays(context) && _triggerAlwaysOn.value) {
            startOverlayService(context)
        } else {
            stopOverlayService(context)
        }
    }

    fun setActivePreset(context: Context, preset: EdgeLightingPresetItem) {
        init(context)
        try {
            val json = gson.toJson(preset)
            prefs?.edit()?.putString(KEY_ACTIVE_PRESET_JSON, json)?.apply()
            _activePreset.value = preset
            _borderThicknessDp.value = preset.borderSize
            _speedMultiplier.value = preset.speed
            _cornerRadiusDp.value = preset.cornerRadius
            _punchHoleRadiusDp.value = preset.punchHoleRadius

            if (_isEnabled.value && canDrawOverlays(context) && _triggerAlwaysOn.value) {
                startOverlayService(context)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun setThickness(context: Context, thickness: Int) {
        init(context)
        prefs?.edit()?.putInt(KEY_THICKNESS, thickness)?.apply()
        _borderThicknessDp.value = thickness
        refreshOverlay(context)
    }

    fun setSpeed(context: Context, speed: Float) {
        init(context)
        prefs?.edit()?.putFloat(KEY_SPEED, speed)?.apply()
        _speedMultiplier.value = speed
        refreshOverlay(context)
    }

    fun setCornerRadius(context: Context, radius: Int) {
        init(context)
        prefs?.edit()?.putInt(KEY_RADIUS, radius)?.apply()
        _cornerRadiusDp.value = radius
        refreshOverlay(context)
    }

    fun setPunchHoleRadius(context: Context, radius: Int) {
        init(context)
        prefs?.edit()?.putInt(KEY_PUNCH_HOLE, radius)?.apply()
        _punchHoleRadiusDp.value = radius
        refreshOverlay(context)
    }

    fun setTriggers(
        context: Context,
        alwaysOn: Boolean,
        onNotif: Boolean,
        onCall: Boolean,
        onMusic: Boolean
    ) {
        init(context)
        prefs?.edit()
            ?.putBoolean(KEY_TRIGGER_ALWAYS, alwaysOn)
            ?.putBoolean(KEY_TRIGGER_NOTIF, onNotif)
            ?.putBoolean(KEY_TRIGGER_CALL, onCall)
            ?.putBoolean(KEY_TRIGGER_MUSIC, onMusic)
            ?.apply()

        _triggerAlwaysOn.value = alwaysOn
        _triggerOnNotification.value = onNotif
        _triggerOnCall.value = onCall
        _triggerOnMusic.value = onMusic

        if (_isEnabled.value && canDrawOverlays(context)) {
            if (alwaysOn) {
                startOverlayService(context)
            } else {
                stopOverlayService(context)
            }
        }
    }

    fun startOverlayService(context: Context) {
        if (!canDrawOverlays(context)) return
        try {
            val intent = Intent(context, EdgeLightingOverlayService::class.java).apply {
                action = EdgeLightingOverlayService.ACTION_START
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun stopOverlayService(context: Context) {
        try {
            val intent = Intent(context, EdgeLightingOverlayService::class.java).apply {
                action = EdgeLightingOverlayService.ACTION_STOP
            }
            context.startService(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun refreshOverlay(context: Context) {
        if (_isEnabled.value && canDrawOverlays(context) && _triggerAlwaysOn.value) {
            startOverlayService(context)
        }
    }
}
