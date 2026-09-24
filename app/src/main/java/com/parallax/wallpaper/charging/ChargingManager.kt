package com.parallax.wallpaper.charging

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.parallax.wallpaper.data.api.ApiClient
import com.parallax.wallpaper.data.api.ChargingAnimationItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object ChargingManager {

    private const val PREFS_NAME = "rewall_charging_prefs"
    private const val KEY_ENABLED = "charging_enabled"
    private const val KEY_SOUND_ENABLED = "charging_sound_enabled"
    private const val KEY_DURATION = "charging_duration"
    private const val KEY_ACTIVE_ANIMATION_JSON = "active_animation_json"

    private val gson = Gson()
    private var prefs: SharedPreferences? = null

    // Curated default animation if offline or none selected
    val defaultAnimation = ChargingAnimationItem(
        id = "ca_neon_arc",
        title = "⚡ Neon Cyber Arc Reactor",
        category = "NEON",
        previewUrl = "/uploads/charging/thumb_neon_arc.svg",
        animationUrl = "/uploads/charging/neon_cyber_arc.json",
        animationType = "lottie",
        soundUrl = null,
        textColor = "#00E5FF",
        isPremium = false,
        downloads = 1850,
        sortOrder = 1,
        createdAt = 0L
    )

    private val _isChargingEnabled = MutableStateFlow(true)
    val isChargingEnabled: StateFlow<Boolean> = _isChargingEnabled.asStateFlow()

    private val _isSoundEnabled = MutableStateFlow(true)
    val isSoundEnabled: StateFlow<Boolean> = _isSoundEnabled.asStateFlow()

    private val _durationSeconds = MutableStateFlow(10)
    val durationSeconds: StateFlow<Int> = _durationSeconds.asStateFlow()

    private val _activeAnimation = MutableStateFlow(defaultAnimation)
    val activeAnimation: StateFlow<ChargingAnimationItem> = _activeAnimation.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            val p = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs = p

            _isChargingEnabled.value = p.getBoolean(KEY_ENABLED, true)
            _isSoundEnabled.value = p.getBoolean(KEY_SOUND_ENABLED, true)
            _durationSeconds.value = p.getInt(KEY_DURATION, 10)

            val json = p.getString(KEY_ACTIVE_ANIMATION_JSON, null)
            if (!json.isNullOrBlank()) {
                try {
                    val saved = gson.fromJson(json, ChargingAnimationItem::class.java)
                    if (saved != null) {
                        _activeAnimation.value = saved
                    }
                } catch (e: Exception) {
                    _activeAnimation.value = defaultAnimation
                }
            } else {
                _activeAnimation.value = defaultAnimation
            }
        }
    }

    fun setChargingEnabled(context: Context, enabled: Boolean) {
        init(context)
        prefs?.edit()?.putBoolean(KEY_ENABLED, enabled)?.apply()
        _isChargingEnabled.value = enabled
    }

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        init(context)
        prefs?.edit()?.putBoolean(KEY_SOUND_ENABLED, enabled)?.apply()
        _isSoundEnabled.value = enabled
    }

    fun setDurationSeconds(context: Context, seconds: Int) {
        init(context)
        prefs?.edit()?.putInt(KEY_DURATION, seconds)?.apply()
        _durationSeconds.value = seconds
    }

    fun setActiveAnimation(context: Context, animation: ChargingAnimationItem) {
        init(context)
        try {
            val json = gson.toJson(animation)
            prefs?.edit()?.putString(KEY_ACTIVE_ANIMATION_JSON, json)?.apply()
            _activeAnimation.value = animation
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getActiveAnimation(context: Context): ChargingAnimationItem {
        init(context)
        return _activeAnimation.value
    }

    /**
     * Resolves relative paths like /uploads/charging/... to absolute server URL: http://127.0.0.1:3000/uploads/...
     */
    fun resolveUrl(url: String?): String {
        if (url.isNullOrBlank()) return ""
        if (url.startsWith("http://") || url.startsWith("https://")) return url
        val base = ApiClient.baseUrl.trimEnd('/')
        val path = if (url.startsWith("/")) url else "/$url"
        return "$base$path"
    }
}
