package com.parallax.wallpaper.data.api

import com.parallax.wallpaper.model.RingtoneItem
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.POST
import retrofit2.http.Query

interface WallpaperApiService {

    @GET("api/wallpapers")
    suspend fun getWallpapers(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 12,
        @Query("category") category: String? = null,
        @Query("tab") tab: String? = null,
        @Query("search") search: String? = null
    ): ApiResponse<List<WallpaperResponse>>

    @GET("api/wallpapers/{id}")
    suspend fun getWallpaperById(
        @Path("id") id: String
    ): ApiResponse<WallpaperResponse>

    @POST("api/wallpapers/{id}/download")
    suspend fun incrementDownload(
        @Path("id") id: String
    ): ApiResponse<Any>

    @POST("api/wallpapers/{id}/like")
    suspend fun incrementLike(
        @Path("id") id: String
    ): ApiResponse<Any>

    @GET("api/categories")
    suspend fun getCategories(): ApiResponse<List<CategoryResponse>>

    @GET("api/ringtones")
    suspend fun getRingtones(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): ApiResponse<List<RingtoneItem>>

    @GET("api/daily-pick")
    suspend fun getDailyPick(): ApiResponse<WallpaperResponse>

    @GET("api/banners")
    suspend fun getHeroBanners(): ApiResponse<List<HeroBannerResponse>>

    @GET("api/trending-tags")
    suspend fun getTrendingTags(): ApiResponse<List<TrendingTagResponse>>

    @POST("api/trending-tags/{id}/click")
    suspend fun recordTagClick(
        @Path("id") id: String
    ): ApiResponse<Any>

    @GET("api/app-config")
    suspend fun getAppConfig(): ApiResponse<AppConfigResponse>

    @POST("api/device/register-token")
    suspend fun registerDeviceToken(
        @retrofit2.http.Body request: DeviceTokenRequest
    ): ApiResponse<Any>

    @POST("api/device/telemetry")
    suspend fun sendDeviceTelemetry(
        @retrofit2.http.Body request: DeviceTelemetryRequest
    ): ApiResponse<Any>

    // ⚡ Dynamic Charging Animations
    @GET("api/charging-animations")
    suspend fun getChargingAnimations(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): ApiResponse<List<ChargingAnimationItem>>

    @GET("api/charging-animations/categories")
    suspend fun getChargingCategories(): ApiResponse<List<ChargingCategoryItem>>

    @GET("api/charging-animations/{id}")
    suspend fun getChargingAnimationById(
        @Path("id") id: String
    ): ApiResponse<ChargingAnimationItem>

    @POST("api/charging-animations/{id}/apply")
    suspend fun applyChargingAnimation(
        @Path("id") id: String
    ): ApiResponse<Any>

    // 🌈 Dynamic Edge Lighting & Border Glow
    @GET("api/edge-lighting")
    suspend fun getEdgeLightingPresets(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): ApiResponse<List<EdgeLightingPresetItem>>

    @GET("api/edge-lighting/categories")
    suspend fun getEdgeLightingCategories(): ApiResponse<List<EdgeLightingCategoryItem>>

    @GET("api/edge-lighting/{id}")
    suspend fun getEdgeLightingPresetById(
        @Path("id") id: String
    ): ApiResponse<EdgeLightingPresetItem>

    @POST("api/edge-lighting/{id}/apply")
    suspend fun applyEdgeLightingPreset(
        @Path("id") id: String
    ): ApiResponse<Any>

    // 🏝️ Dynamic Island Smart Punch-Hole Capsule
    @GET("api/dynamic-island/themes")
    suspend fun getDynamicIslandThemes(
        @Query("search") search: String? = null
    ): ApiResponse<List<DynamicIslandThemeItem>>

    @GET("api/dynamic-island/themes/{id}")
    suspend fun getDynamicIslandThemeById(
        @Path("id") id: String
    ): ApiResponse<DynamicIslandThemeItem>

    @POST("api/dynamic-island/themes/{id}/apply")
    suspend fun applyDynamicIslandTheme(
        @Path("id") id: String
    ): ApiResponse<Any>

    // 🕒 Always-On Display (AOD) Clocks & Widgets
    @GET("api/aod/clocks")
    suspend fun getAODClocks(
        @Query("clockType") clockType: String? = null,
        @Query("search") search: String? = null
    ): ApiResponse<List<AODClockItem>>

    @GET("api/aod/clocks/{id}")
    suspend fun getAODClockById(
        @Path("id") id: String
    ): ApiResponse<AODClockItem>

    @POST("api/aod/clocks/{id}/apply")
    suspend fun applyAODClock(
        @Path("id") id: String
    ): ApiResponse<Any>

    // 📞 3D Color Call Screen & Flash Themes
    @GET("api/call-screen/themes")
    suspend fun getCallThemes(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): ApiResponse<List<CallScreenThemeItem>>

    @GET("api/call-screen/themes/{id}")
    suspend fun getCallThemeById(
        @Path("id") id: String
    ): ApiResponse<CallScreenThemeItem>

    @POST("api/call-screen/themes/{id}/apply")
    suspend fun applyCallTheme(
        @Path("id") id: String
    ): ApiResponse<Any>

    // 👥 Duo / Double Wallpapers (Lock & Home Magic Pair)
    @GET("api/duo/wallpapers")
    suspend fun getDuoWallpapers(
        @Query("category") category: String? = null,
        @Query("search") search: String? = null
    ): ApiResponse<List<com.parallax.wallpaper.model.DuoWallpaperItem>>

    @GET("api/duo/wallpapers/{id}")
    suspend fun getDuoWallpaperById(
        @Path("id") id: String
    ): ApiResponse<com.parallax.wallpaper.model.DuoWallpaperItem>

    @POST("api/duo/wallpapers/{id}/apply")
    suspend fun applyDuoWallpaper(
        @Path("id") id: String
    ): ApiResponse<Any>

    // 👆 Interactive Touch Fluid & Ripple Effects
    @GET("api/touch-effects/presets")
    suspend fun getTouchPresets(
        @Query("effectType") effectType: String? = null,
        @Query("search") search: String? = null
    ): ApiResponse<List<com.parallax.wallpaper.model.TouchEffectPreset>>

    @GET("api/touch-effects/presets/{id}")
    suspend fun getTouchPresetById(
        @Path("id") id: String
    ): ApiResponse<com.parallax.wallpaper.model.TouchEffectPreset>

    @POST("api/touch-effects/presets/{id}/apply")
    suspend fun applyTouchPreset(
        @Path("id") id: String
    ): ApiResponse<Any>

    // 🔓 In-Display Fingerprint Animation Effects
    @GET("api/fingerprint/presets")
    suspend fun getFingerprintPresets(
        @Query("category") category: String? = null,
        @Query("animationType") animationType: String? = null,
        @Query("search") search: String? = null
    ): ApiResponse<List<com.parallax.wallpaper.model.FingerprintPreset>>

    @GET("api/fingerprint/presets/{id}")
    suspend fun getFingerprintPresetById(
        @Path("id") id: String
    ): ApiResponse<com.parallax.wallpaper.model.FingerprintPreset>

    @POST("api/fingerprint/presets/{id}/apply")
    suspend fun applyFingerprintPreset(
        @Path("id") id: String
    ): ApiResponse<Any>
}

data class CallScreenThemeItem(
    val id: String,
    val title: String,
    val category: String = "NEON",
    val backgroundUrl: String,
    val previewUrl: String,
    val buttonStyle: String = "neon_glow",
    val accentColor: String = "#00E5FF",
    val glowColor: String = "#7000FF",
    val flashAlertEnabled: Boolean = true,
    val flashSpeed: String = "normal",
    val ringtoneUrl: String? = null,
    val isPremium: Boolean = false,
    val downloads: Int = 0,
    val isActive: Boolean = true
)

data class AODClockItem(
    val id: String,
    val title: String,
    val clockType: String = "cyberpunk_digital",
    val accentColor: String = "#00E5FF",
    val glowColor: String = "#7000FF",
    val textColor: String = "#FFFFFF",
    val backgroundColor: String = "#000000",
    val dialStyle: String = "futuristic_hud",
    val hasBatteryWidget: Boolean = true,
    val hasDateWidget: Boolean = true,
    val hasStepsWidget: Boolean = true,
    val hasWeatherWidget: Boolean = true,
    val previewUrl: String? = null,
    val assetUrl: String? = null,
    val isPremium: Boolean = false,
    val downloads: Int = 0,
    val isActive: Boolean = true,
    val sortOrder: Int = 0
)

data class DynamicIslandThemeItem(
    val id: String,
    val title: String,
    val styleType: String = "minimal",
    val backgroundColor: String = "#000000",
    val textColor: String = "#FFFFFF",
    val accentColor: String = "#00E5FF",
    val glowColor: String = "rgba(0,229,255,0.4)",
    val cornerRadius: Int = 24,
    val compactWidth: Int = 120,
    val compactHeight: Int = 36,
    val expandedWidth: Int = 320,
    val expandedHeight: Int = 84,
    val previewUrl: String? = null,
    val isPremium: Boolean = false,
    val downloads: Int = 0,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = 0L
)

data class EdgeLightingPresetItem(
    val id: String,
    val title: String,
    val category: String = "RAINBOW",
    val animationType: String = "rainbow_wave",
    val colors: List<String> = listOf("#00E5FF", "#7000FF"),
    val speed: Float = 1.0f,
    val borderSize: Int = 5,
    val cornerRadius: Int = 28,
    val punchHoleRadius: Int = 0,
    val glowSpread: Int = 12,
    val previewUrl: String? = null,
    val isPremium: Boolean = false,
    val downloads: Int = 0,
    val isActive: Boolean = true,
    val sortOrder: Int = 0,
    val createdAt: Long = 0L
)

data class EdgeLightingCategoryItem(
    val category: String,
    val count: Int
)

data class ChargingAnimationItem(
    val id: String,
    val title: String,
    val category: String,
    val previewUrl: String,
    val animationUrl: String,
    val animationType: String = "lottie",
    val soundUrl: String? = null,
    val textColor: String = "#00E5FF",
    val isPremium: Boolean = false,
    val downloads: Int = 0,
    val sortOrder: Int = 0,
    val createdAt: Long = 0L
)

data class ChargingCategoryItem(
    val name: String,
    val count: Int
)

data class DeviceTokenRequest(
    val token: String,
    val deviceModel: String? = null,
    val appVersion: String? = null
)

data class DeviceTelemetryRequest(
    val deviceId: String,
    val deviceModel: String? = null,
    val manufacturer: String? = null,
    val brand: String? = null,
    val androidVersion: String? = null,
    val apiLevel: Int? = null,
    val appVersion: String? = null,
    val screenResolution: String? = null,
    val screenDpi: Int? = null,
    val batteryLevel: Int? = null,
    val fcmToken: String? = null
)
