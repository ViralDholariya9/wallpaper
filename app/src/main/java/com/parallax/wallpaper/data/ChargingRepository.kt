package com.parallax.wallpaper.data

import android.util.Log
import com.parallax.wallpaper.charging.ChargingManager
import com.parallax.wallpaper.data.api.ApiClient
import com.parallax.wallpaper.data.api.ChargingAnimationItem
import com.parallax.wallpaper.data.api.ChargingCategoryItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChargingRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val _animations = MutableStateFlow<List<ChargingAnimationItem>>(emptyList())
    val animations: StateFlow<List<ChargingAnimationItem>> = _animations.asStateFlow()

    private val _categories = MutableStateFlow<List<ChargingCategoryItem>>(emptyList())
    val categories: StateFlow<List<ChargingCategoryItem>> = _categories.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Offline fallback baseline animations
    private val defaultFallbackAnimations = listOf(
        ChargingManager.defaultAnimation,
        ChargingAnimationItem(
            id = "ca_quantum_vortex",
            title = "🌌 Quantum Cosmic Vortex",
            category = "3D_PARTICLE",
            previewUrl = "/uploads/charging/thumb_quantum_vortex.svg",
            animationUrl = "/uploads/charging/quantum_vortex.json",
            animationType = "lottie",
            soundUrl = null,
            textColor = "#B026FF",
            isPremium = false,
            downloads = 2420,
            sortOrder = 2,
            createdAt = 0L
        ),
        ChargingAnimationItem(
            id = "ca_amoled_liquid",
            title = "🧪 AMOLED Toxic Green Liquid",
            category = "LIQUID",
            previewUrl = "/uploads/charging/thumb_liquid_bubble.svg",
            animationUrl = "/uploads/charging/liquid_bubble_flow.json",
            animationType = "lottie",
            soundUrl = null,
            textColor = "#00FF88",
            isPremium = false,
            downloads = 3100,
            sortOrder = 3,
            createdAt = 0L
        ),
        ChargingAnimationItem(
            id = "ca_speed_nitro",
            title = "🔥 High-Voltage Lightning Bolt",
            category = "CYBERPUNK",
            previewUrl = "/uploads/charging/thumb_lightning.svg",
            animationUrl = "/uploads/charging/lightning_turbo.json",
            animationType = "lottie",
            soundUrl = null,
            textColor = "#FFB800",
            isPremium = false,
            downloads = 1670,
            sortOrder = 4,
            createdAt = 0L
        ),
        ChargingAnimationItem(
            id = "ca_minimal_zen",
            title = "⚪ Minimalist Pure Zen Ring",
            category = "MINIMAL",
            previewUrl = "/uploads/charging/thumb_zen_ring.svg",
            animationUrl = "/uploads/charging/minimal_zen.json",
            animationType = "lottie",
            soundUrl = null,
            textColor = "#FFFFFF",
            isPremium = false,
            downloads = 920,
            sortOrder = 5,
            createdAt = 0L
        )
    )

    init {
        _animations.value = defaultFallbackAnimations
        refresh()
    }

    fun refresh(category: String? = null, search: String? = null) {
        scope.launch {
            _isLoading.value = true
            try {
                val resp = ApiClient.service.getChargingAnimations(category = category, search = search)
                if (resp.success && !resp.data.isNullOrEmpty()) {
                    _animations.value = resp.data
                } else if (_animations.value.isEmpty()) {
                    _animations.value = defaultFallbackAnimations
                }
            } catch (e: Exception) {
                Log.w("ChargingRepository", "Network fetch fallback: ${e.message}")
                if (_animations.value.isEmpty()) {
                    _animations.value = defaultFallbackAnimations
                }
            }

            try {
                val catResp = ApiClient.service.getChargingCategories()
                if (catResp.success && !catResp.data.isNullOrEmpty()) {
                    _categories.value = catResp.data
                }
            } catch (e: Exception) {
                Log.w("ChargingRepository", "Category fetch error: ${e.message}")
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun applyAnimation(animationId: String) {
        scope.launch {
            try {
                ApiClient.service.applyChargingAnimation(animationId)
            } catch (e: Exception) {
                Log.w("ChargingRepository", "Apply count sync error: ${e.message}")
            }
        }
    }
}
