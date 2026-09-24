package com.parallax.wallpaper.data.local

import android.content.Context
import android.content.SharedPreferences

class FavoritesManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun getFavorites(): Set<String> {
        val stored = prefs.getStringSet(KEY_FAVORITES, null)
        return if (stored != null) {
            HashSet(stored)
        } else {
            // Default pre-populated favorites for fresh app installs
            val defaults = setOf("w1", "w3", "w5")
            saveFavorites(defaults)
            defaults
        }
    }

    fun saveFavorites(ids: Set<String>) {
        prefs.edit().putStringSet(KEY_FAVORITES, HashSet(ids)).apply()
    }

    fun toggleFavorite(id: String): Boolean {
        val current = HashSet(getFavorites())
        val isNowFav = if (current.contains(id)) {
            current.remove(id)
            false
        } else {
            current.add(id)
            true
        }
        saveFavorites(current)
        return isNowFav
    }

    fun isFavorite(id: String): Boolean {
        return getFavorites().contains(id)
    }

    companion object {
        private const val PREFS_NAME = "rewall_favorites_prefs"
        private const val KEY_FAVORITES = "key_favorite_wallpapers"
    }
}
