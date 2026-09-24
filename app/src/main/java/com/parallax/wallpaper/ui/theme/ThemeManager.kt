package com.parallax.wallpaper.ui.theme

import android.content.Context
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode(val displayName: String, val description: String) {
    DARK("Dark Mode", "Deep Obsidian & Neon Glow"),
    LIGHT("Light Mode", "Crisp Modern White"),
    AMOLED("AMOLED Black", "100% Pure OLED Black")
}

object ThemeManager {

    private const val PREFS_NAME = "rewall_theme_prefs"
    private const val KEY_THEME_MODE = "app_theme_mode"

    private val _themeMode = MutableStateFlow(AppThemeMode.DARK)
    val themeMode: StateFlow<AppThemeMode> = _themeMode.asStateFlow()

    fun init(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedName = prefs.getString(KEY_THEME_MODE, AppThemeMode.DARK.name) ?: AppThemeMode.DARK.name
        val mode = try {
            AppThemeMode.valueOf(savedName)
        } catch (e: Exception) {
            AppThemeMode.DARK
        }
        _themeMode.value = mode
    }

    fun setThemeMode(context: Context, mode: AppThemeMode) {
        _themeMode.value = mode
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
    }
}
