package com.parallax.wallpaper.callscreen

import android.content.Context
import android.content.SharedPreferences
import android.hardware.camera2.CameraManager
import android.os.Build
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
 * Singleton manager for Color Call Screen preferences and Camera Flashlight alerts.
 */
class CallScreenManager private constructor(private val context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("rewall_callscreen_prefs", Context.MODE_PRIVATE)
    private val scope = CoroutineScope(Dispatchers.Default + Job())

    private var flashJob: Job? = null
    private var cameraManager: CameraManager? = null
    private var cameraId: String? = null

    init {
        try {
            cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            cameraId = cameraManager?.cameraIdList?.firstOrNull { id ->
                val chars = cameraManager?.getCameraCharacteristics(id)
                chars?.get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (_: Exception) {}
    }

    // Default Fallback Theme
    private val defaultTheme = CallScreenTheme(
        id = "call_cyber_matrix_2077",
        title = "⚡ Cyberpunk Matrix 2077",
        category = "CYBERPUNK",
        backgroundUrl = "/uploads/callscreen/call_cyber_matrix_2077.svg",
        previewUrl = "/uploads/callscreen/call_cyber_matrix_2077.svg",
        buttonStyle = CallButtonStyle.NEON_GLOW,
        accentColorHex = "#00E5FF",
        glowColorHex = "#7000FF",
        flashAlertEnabled = true,
        flashSpeed = FlashSpeed.STROBE,
        ringtoneUrl = null,
        isPremium = false,
        downloads = 14280
    )

    private val _isEnabled = MutableStateFlow(prefs.getBoolean(KEY_CALL_ENABLED, true))
    val isEnabled: StateFlow<Boolean> = _isEnabled.asStateFlow()

    private val _activeTheme = MutableStateFlow(loadSavedTheme())
    val activeTheme: StateFlow<CallScreenTheme> = _activeTheme.asStateFlow()

    private val _flashAlertEnabled = MutableStateFlow(prefs.getBoolean(KEY_FLASH_ALERT, true))
    val flashAlertEnabled: StateFlow<Boolean> = _flashAlertEnabled.asStateFlow()

    private val _flashSpeed = MutableStateFlow(loadSavedFlashSpeed())
    val flashSpeed: StateFlow<FlashSpeed> = _flashSpeed.asStateFlow()

    private val _buttonStyle = MutableStateFlow(loadSavedButtonStyle())
    val buttonStyle: StateFlow<CallButtonStyle> = _buttonStyle.asStateFlow()

    fun setEnabled(enabled: Boolean) {
        _isEnabled.value = enabled
        prefs.edit().putBoolean(KEY_CALL_ENABLED, enabled).apply()
    }

    fun setActiveTheme(theme: CallScreenTheme) {
        _activeTheme.value = theme
        _buttonStyle.value = theme.buttonStyle
        prefs.edit()
            .putString(KEY_THEME_ID, theme.id)
            .putString(KEY_THEME_TITLE, theme.title)
            .putString(KEY_THEME_CATEGORY, theme.category)
            .putString(KEY_THEME_BG, theme.backgroundUrl)
            .putString(KEY_THEME_PREV, theme.previewUrl)
            .putString(KEY_BUTTON_STYLE, theme.buttonStyle.name)
            .putString(KEY_ACCENT_COLOR, theme.accentColorHex)
            .putString(KEY_GLOW_COLOR, theme.glowColorHex)
            .putBoolean(KEY_FLASH_ALERT, theme.flashAlertEnabled)
            .putString(KEY_FLASH_SPEED, theme.flashSpeed.name)
            .apply()
    }

    fun setFlashAlertEnabled(enabled: Boolean) {
        _flashAlertEnabled.value = enabled
        prefs.edit().putBoolean(KEY_FLASH_ALERT, enabled).apply()
    }

    fun setFlashSpeed(speed: FlashSpeed) {
        _flashSpeed.value = speed
        prefs.edit().putString(KEY_FLASH_SPEED, speed.name).apply()
    }

    fun setButtonStyle(style: CallButtonStyle) {
        _buttonStyle.value = style
        prefs.edit().putString(KEY_BUTTON_STYLE, style.name).apply()
    }

    /**
     * Starts camera flashlight strobe pulse when phone is ringing.
     */
    fun startFlashlightStrobe() {
        if (!_flashAlertEnabled.value) return
        stopFlashlightStrobe()

        val cm = cameraManager ?: return
        val camId = cameraId ?: return
        val interval = _flashSpeed.value.intervalMs

        flashJob = scope.launch {
            var torchOn = false
            try {
                while (isActive) {
                    torchOn = !torchOn
                    try {
                        cm.setTorchMode(camId, torchOn)
                    } catch (_: Exception) {}
                    delay(interval)
                }
            } finally {
                try {
                    cm.setTorchMode(camId, false)
                } catch (_: Exception) {}
            }
        }
    }

    /**
     * Safely stops flashlight strobe and ensures torch is turned OFF.
     */
    fun stopFlashlightStrobe() {
        flashJob?.cancel()
        flashJob = null
        try {
            val cm = cameraManager
            val camId = cameraId
            if (cm != null && camId != null) {
                cm.setTorchMode(camId, false)
            }
        } catch (_: Exception) {}
    }

    private fun loadSavedTheme(): CallScreenTheme {
        val id = prefs.getString(KEY_THEME_ID, defaultTheme.id) ?: defaultTheme.id
        val title = prefs.getString(KEY_THEME_TITLE, defaultTheme.title) ?: defaultTheme.title
        val category = prefs.getString(KEY_THEME_CATEGORY, defaultTheme.category) ?: defaultTheme.category
        val bg = prefs.getString(KEY_THEME_BG, defaultTheme.backgroundUrl) ?: defaultTheme.backgroundUrl
        val prev = prefs.getString(KEY_THEME_PREV, defaultTheme.previewUrl) ?: defaultTheme.previewUrl
        val styleStr = prefs.getString(KEY_BUTTON_STYLE, defaultTheme.buttonStyle.name)
        val style = try { CallButtonStyle.valueOf(styleStr ?: "") } catch (_: Exception) { CallButtonStyle.NEON_GLOW }
        val accent = prefs.getString(KEY_ACCENT_COLOR, defaultTheme.accentColorHex) ?: defaultTheme.accentColorHex
        val glow = prefs.getString(KEY_GLOW_COLOR, defaultTheme.glowColorHex) ?: defaultTheme.glowColorHex
        val flashAlert = prefs.getBoolean(KEY_FLASH_ALERT, defaultTheme.flashAlertEnabled)
        val speedStr = prefs.getString(KEY_FLASH_SPEED, defaultTheme.flashSpeed.name)
        val speed = try { FlashSpeed.valueOf(speedStr ?: "") } catch (_: Exception) { FlashSpeed.NORMAL }

        return CallScreenTheme(
            id = id,
            title = title,
            category = category,
            backgroundUrl = bg,
            previewUrl = prev,
            buttonStyle = style,
            accentColorHex = accent,
            glowColorHex = glow,
            flashAlertEnabled = flashAlert,
            flashSpeed = speed
        )
    }

    private fun loadSavedFlashSpeed(): FlashSpeed {
        val speedStr = prefs.getString(KEY_FLASH_SPEED, FlashSpeed.NORMAL.name)
        return try { FlashSpeed.valueOf(speedStr ?: "") } catch (_: Exception) { FlashSpeed.NORMAL }
    }

    private fun loadSavedButtonStyle(): CallButtonStyle {
        val styleStr = prefs.getString(KEY_BUTTON_STYLE, CallButtonStyle.NEON_GLOW.name)
        return try { CallButtonStyle.valueOf(styleStr ?: "") } catch (_: Exception) { CallButtonStyle.NEON_GLOW }
    }

    companion object {
        private const val KEY_CALL_ENABLED = "call_screen_enabled"
        private const val KEY_THEME_ID = "call_theme_id"
        private const val KEY_THEME_TITLE = "call_theme_title"
        private const val KEY_THEME_CATEGORY = "call_theme_category"
        private const val KEY_THEME_BG = "call_theme_bg"
        private const val KEY_THEME_PREV = "call_theme_prev"
        private const val KEY_BUTTON_STYLE = "call_button_style"
        private const val KEY_ACCENT_COLOR = "call_accent_color"
        private const val KEY_GLOW_COLOR = "call_glow_color"
        private const val KEY_FLASH_ALERT = "call_flash_alert"
        private const val KEY_FLASH_SPEED = "call_flash_speed"

        @Volatile
        private var instance: CallScreenManager? = null

        fun resolveUrl(url: String?): String {
            if (url.isNullOrBlank()) return ""
            if (url.startsWith("http://") || url.startsWith("https://")) return url
            val base = com.parallax.wallpaper.data.api.ApiClient.baseUrl.trimEnd('/')
            val path = if (url.startsWith("/")) url else "/$url"
            return "$base$path"
        }

        fun init(context: Context): CallScreenManager = getInstance(context)

        fun getInstance(context: Context): CallScreenManager {
            return instance ?: synchronized(this) {
                instance ?: CallScreenManager(context.applicationContext).also { instance = it }
            }
        }
    }
}
