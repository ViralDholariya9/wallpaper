package com.parallax.wallpaper.island

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import com.google.gson.Gson
import com.parallax.wallpaper.data.api.DynamicIslandThemeItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

object DynamicIslandManager {

    private const val PREFS_NAME = "rewall_dynamic_island_prefs"
    private const val KEY_ENABLED = "island_enabled"
    private const val KEY_ACTIVE_THEME_JSON = "active_island_theme_json"
    private const val KEY_OFFSET_X = "punch_hole_offset_x"
    private const val KEY_OFFSET_Y = "punch_hole_offset_y"
    private const val KEY_CAMERA_POS = "camera_hole_position"
    private const val KEY_EXPAND_ON_TOUCH = "expand_on_touch"
    private const val KEY_TRIGGER_CHARGING = "trigger_charging"
    private const val KEY_TRIGGER_MUSIC = "trigger_music"
    private const val KEY_TRIGGER_EARBUDS = "trigger_earbuds"
    private const val KEY_TRIGGER_CALL = "trigger_call"
    private const val KEY_TRIGGER_NOTIF = "trigger_notif"

    private val gson = Gson()
    private var prefs: SharedPreferences? = null
    private val scope = CoroutineScope(Dispatchers.Main)
    private var autoDismissJob: Job? = null

    // Curated default theme
    val defaultTheme = DynamicIslandThemeItem(
        id = "di_apple_obsidian",
        title = "🖤 Apple Minimalist Obsidian",
        styleType = "minimal",
        backgroundColor = "#000000",
        textColor = "#FFFFFF",
        accentColor = "#00E5FF",
        glowColor = "rgba(255,255,255,0.15)",
        cornerRadius = 24,
        compactWidth = 120,
        compactHeight = 36,
        expandedWidth = 320,
        expandedHeight = 84,
        previewUrl = "/uploads/island/thumb_apple_obsidian.svg",
        isPremium = false,
        downloads = 4520,
        isActive = true,
        sortOrder = 1,
        createdAt = 0L
    )

    private val _isEnabled = MutableStateFlow(true)
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _activeTheme = MutableStateFlow(defaultTheme)
    val activeTheme: StateFlow<DynamicIslandThemeItem> = _activeTheme.asStateFlow()

    private val _currentEvent = MutableStateFlow<IslandEvent>(IslandEvent.Music())
    val currentEvent: StateFlow<IslandEvent> = _currentEvent.asStateFlow()

    private val _xOffsetDp = MutableStateFlow(0)
    val xOffsetDp: StateFlow<Int> = _xOffsetDp.asStateFlow()

    private val _yOffsetDp = MutableStateFlow(16)
    val yOffsetDp: StateFlow<Int> = _yOffsetDp.asStateFlow()

    private val _cameraPosition = MutableStateFlow(CameraHolePosition.CENTER)
    val cameraPosition: StateFlow<CameraHolePosition> = _cameraPosition.asStateFlow()

    private val _expandOnTouch = MutableStateFlow(true)
    val expandOnTouch: StateFlow<Boolean> = _expandOnTouch.asStateFlow()

    private val _triggerCharging = MutableStateFlow(true)
    val triggerCharging: StateFlow<Boolean> = _triggerCharging.asStateFlow()

    private val _triggerMusic = MutableStateFlow(true)
    val triggerMusic: StateFlow<Boolean> = _triggerMusic.asStateFlow()

    private val _triggerEarbuds = MutableStateFlow(true)
    val triggerEarbuds: StateFlow<Boolean> = _triggerEarbuds.asStateFlow()

    private val _triggerCall = MutableStateFlow(true)
    val triggerCall: StateFlow<Boolean> = _triggerCall.asStateFlow()

    private val _triggerNotif = MutableStateFlow(true)
    val triggerNotif: StateFlow<Boolean> = _triggerNotif.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            val p = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs = p

            _isEnabled.value = p.getBoolean(KEY_ENABLED, true)
            _xOffsetDp.value = p.getInt(KEY_OFFSET_X, 0)
            _yOffsetDp.value = p.getInt(KEY_OFFSET_Y, 16)
            _expandOnTouch.value = p.getBoolean(KEY_EXPAND_ON_TOUCH, true)

            _triggerCharging.value = p.getBoolean(KEY_TRIGGER_CHARGING, true)
            _triggerMusic.value = p.getBoolean(KEY_TRIGGER_MUSIC, true)
            _triggerEarbuds.value = p.getBoolean(KEY_TRIGGER_EARBUDS, true)
            _triggerCall.value = p.getBoolean(KEY_TRIGGER_CALL, true)
            _triggerNotif.value = p.getBoolean(KEY_TRIGGER_NOTIF, true)

            val posName = p.getString(KEY_CAMERA_POS, CameraHolePosition.CENTER.name)
            _cameraPosition.value = try {
                CameraHolePosition.valueOf(posName ?: CameraHolePosition.CENTER.name)
            } catch (_: Exception) {
                CameraHolePosition.CENTER
            }

            val json = p.getString(KEY_ACTIVE_THEME_JSON, null)
            if (!json.isNullOrBlank()) {
                try {
                    val saved = gson.fromJson(json, DynamicIslandThemeItem::class.java)
                    if (saved != null) _activeTheme.value = saved
                } catch (_: Exception) {
                    _activeTheme.value = defaultTheme
                }
            }

            if (_isEnabled.value && canDrawOverlays(context)) {
                startService(context)
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

    fun openOverlaySettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                val intent = Intent(
                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    android.net.Uri.parse("package:${context.packageName}")
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)
            } catch (_: Exception) {
                val fallback = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(fallback)
            }
        }
    }

    fun setEnabled(context: Context, enabled: Boolean) {
        _isEnabled.value = enabled
        prefs?.edit()?.putBoolean(KEY_ENABLED, enabled)?.apply()

        if (enabled) {
            if (canDrawOverlays(context)) {
                startService(context)
            }
        } else {
            stopService(context)
        }
    }

    fun setActiveTheme(context: Context, theme: DynamicIslandThemeItem) {
        _activeTheme.value = theme
        prefs?.edit()?.putString(KEY_ACTIVE_THEME_JSON, gson.toJson(theme))?.apply()
    }

    fun setOffsets(context: Context, xDp: Int, yDp: Int) {
        _xOffsetDp.value = xDp
        _yOffsetDp.value = yDp
        prefs?.edit()
            ?.putInt(KEY_OFFSET_X, xDp)
            ?.putInt(KEY_OFFSET_Y, yDp)
            ?.apply()
    }

    fun setCameraPosition(context: Context, position: CameraHolePosition) {
        _cameraPosition.value = position
        prefs?.edit()?.putString(KEY_CAMERA_POS, position.name)?.apply()

        when (position) {
            CameraHolePosition.LEFT -> setOffsets(context, -70, _yOffsetDp.value)
            CameraHolePosition.CENTER -> setOffsets(context, 0, _yOffsetDp.value)
            CameraHolePosition.RIGHT -> setOffsets(context, 70, _yOffsetDp.value)
        }
    }

    fun triggerEvent(event: IslandEvent, autoDismissSeconds: Long = 5) {
        autoDismissJob?.cancel()
        _currentEvent.value = event

        if (event !is IslandEvent.Compact && autoDismissSeconds > 0) {
            autoDismissJob = scope.launch {
                delay(autoDismissSeconds * 1000)
                _currentEvent.value = IslandEvent.Compact
            }
        }
    }

    fun toggleCompactExpanded() {
        if (_currentEvent.value is IslandEvent.Compact) {
            triggerEvent(IslandEvent.Music())
        } else {
            triggerEvent(IslandEvent.Compact)
        }
    }

    fun startService(context: Context) {
        try {
            val intent = Intent(context, DynamicIslandOverlayService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        } catch (_: Exception) {}
    }

    fun stopService(context: Context) {
        try {
            val intent = Intent(context, DynamicIslandOverlayService::class.java)
            context.stopService(intent)
        } catch (_: Exception) {}
    }
}
