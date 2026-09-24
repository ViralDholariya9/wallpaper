package com.parallax.wallpaper.data.api

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success") val success: Boolean,
    @SerializedName("data") val data: T? = null,
    @SerializedName("error") val error: String? = null,
    @SerializedName("pagination") val pagination: PaginationInfo? = null
)

data class PaginationInfo(
    @SerializedName("page") val page: Int,
    @SerializedName("limit") val limit: Int,
    @SerializedName("total") val total: Int,
    @SerializedName("totalPages") val totalPages: Int
)

data class WallpaperResponse(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("category") val category: String,
    @SerializedName("previewUrl") val previewUrl: String,
    @SerializedName("fullUrl") val fullUrl: String? = null,
    @SerializedName("isParallax") val isParallax: Boolean = false,
    @SerializedName("layers") val layers: List<LayerResponse> = emptyList(),
    @SerializedName("downloads") val downloads: Int = 0,
    @SerializedName("likes") val likes: Int = 0,
    @SerializedName("isPremium") val isPremium: Boolean = false,
    @SerializedName("isUnlocked") val isUnlocked: Boolean = true,
    @SerializedName("createdAt") val createdAt: Long = System.currentTimeMillis()
)

data class LayerResponse(
    @SerializedName("id") val id: String,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("depth") val depth: Float
)

data class CategoryResponse(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("iconUrl") val iconUrl: String? = null,
    @SerializedName("count") val count: Int = 0
)

data class WeatherConfigResponse(
    @SerializedName("enabled") val enabled: Boolean = false,
    @SerializedName("effect") val effect: String = "rain",
    @SerializedName("intensity") val intensity: String = "medium",
    @SerializedName("categories") val categories: List<String> = listOf("all"),
    @SerializedName("sfxEnabled") val sfxEnabled: Boolean = false
)

data class ForceUpdateConfigResponse(
    @SerializedName("enabled") val enabled: Boolean = false,
    @SerializedName("latestVersion") val latestVersion: String = "1.0.0",
    @SerializedName("minSupportedVersion") val minSupportedVersion: String = "1.0.0",
    @SerializedName("title") val title: String = "🚀 New 3D Engine Update Available!",
    @SerializedName("message") val message: String = "Please update to enjoy the latest 3D wallpapers and smooth performance.",
    @SerializedName("storeUrl") val storeUrl: String = "https://play.google.com/store",
    @SerializedName("buttonText") val buttonText: String = "Update to Latest Version"
)

data class AnnouncementConfigResponse(
    @SerializedName("enabled") val enabled: Boolean = false,
    @SerializedName("text") val text: String = "",
    @SerializedName("type") val type: String = "festival",
    @SerializedName("theme") val theme: String = "diwali_gold",
    @SerializedName("actionTarget") val actionTarget: String = "",
    @SerializedName("dismissable") val dismissable: Boolean = true
)

data class AdmobConfigResponse(
    @SerializedName("bannerEnabled") val bannerEnabled: Boolean = true,
    @SerializedName("interstitialEnabled") val interstitialEnabled: Boolean = true,
    @SerializedName("rewardedInterval") val rewardedInterval: Int = 3,
    @SerializedName("interstitialInterval") val interstitialInterval: Int = 3,
    @SerializedName("appId") val appId: String = "ca-app-pub-3940256099942544~3347511713",
    @SerializedName("bannerId") val bannerId: String = "ca-app-pub-3940256099942544/6300978111",
    @SerializedName("interstitialId") val interstitialId: String = "ca-app-pub-3940256099942544/1033173712",
    @SerializedName("rewardedId") val rewardedId: String = "ca-app-pub-3940256099942544/5224354917",
    @SerializedName("appOpenId") val appOpenId: String = "ca-app-pub-3940256099942544/9257395921",
    @SerializedName("isTestMode") val isTestMode: Boolean = true
)

data class FeatureFlagsResponse(
    @SerializedName("statusSaver") val statusSaver: Boolean = true,
    @SerializedName("aiStudio") val aiStudio: Boolean = true,
    @SerializedName("musicVisualizer") val musicVisualizer: Boolean = true,
    @SerializedName("ringtones") val ringtones: Boolean = true,
    @SerializedName("custom3dMaker") val custom3dMaker: Boolean = true,
    @SerializedName("weatherSync") val weatherSync: Boolean = true,
    @SerializedName("doubleWallpaper") val doubleWallpaper: Boolean = true,
    @SerializedName("wallpaperChanger") val wallpaperChanger: Boolean = true,
    @SerializedName("chargingAnimations") val chargingAnimations: Boolean = true,
    @SerializedName("edgeLighting") val edgeLighting: Boolean = true,
    @SerializedName("dynamicIsland") val dynamicIsland: Boolean = true,
    @SerializedName("alwaysOnDisplay") val alwaysOnDisplay: Boolean = true,
    @SerializedName("callScreen") val callScreen: Boolean = true,
    @SerializedName("touchEffects") val touchEffects: Boolean = true,
    @SerializedName("fingerprintAnimations") val fingerprintAnimations: Boolean = true
)

data class LegalAndSupportConfigResponse(
    @SerializedName("privacyPolicyUrl") val privacyPolicyUrl: String = "https://rewall-3d.web.app/privacy-policy",
    @SerializedName("termsUrl") val termsUrl: String = "https://rewall-3d.web.app/terms",
    @SerializedName("instagramUrl") val instagramUrl: String = "https://instagram.com/rewall.3d",
    @SerializedName("telegramUrl") val telegramUrl: String = "https://t.me/rewall_wallpapers",
    @SerializedName("whatsappNumber") val whatsappNumber: String = "+919876543210",
    @SerializedName("supportEmail") val supportEmail: String = "support@rewall.app"
)

data class RatingPromptConfigResponse(
    @SerializedName("enabled") val enabled: Boolean = true,
    @SerializedName("triggerDownloads") val triggerDownloads: Int = 3,
    @SerializedName("title") val title: String = "Enjoying ReWall 3D Wallpapers?",
    @SerializedName("message") val message: String = "You have downloaded awesome 4K 3D wallpapers! A quick 5-star rating on Google Play Store helps our team keep adding free wallpapers.",
    @SerializedName("positiveBtn") val positiveBtn: String = "⭐ Rate 5 Stars on Google Play",
    @SerializedName("dismissBtn") val dismissBtn: String = "Maybe Later"
)

data class AppConfigResponse(
    @SerializedName("features") val features: FeatureFlagsResponse? = null,
    @SerializedName("weather") val weather: WeatherConfigResponse? = null,
    @SerializedName("forceUpdate") val forceUpdate: ForceUpdateConfigResponse? = null,
    @SerializedName("announcement") val announcement: AnnouncementConfigResponse? = null,
    @SerializedName("admob") val admob: AdmobConfigResponse? = null,
    @SerializedName("legalAndSupport") val legalAndSupport: LegalAndSupportConfigResponse? = null,
    @SerializedName("ratingPrompt") val ratingPrompt: RatingPromptConfigResponse? = null,
    @SerializedName("admobBannerEnabled") val admobBannerEnabled: Boolean = true,
    @SerializedName("admobInterstitialEnabled") val admobInterstitialEnabled: Boolean = true,
    @SerializedName("admobRewardedClicksInterval") val admobRewardedClicksInterval: Int = 3,
    @SerializedName("appVersion") val appVersion: String = "1.0.0",
    @SerializedName("updateUrl") val updateUrl: String = "",
    @SerializedName("maintenanceMode") val maintenanceMode: Boolean = false,
    @SerializedName("dailyPickId") val dailyPickId: String? = null,
    @SerializedName("personalizationSuite") val personalizationSuite: List<PersonalizationSuiteItemResponse>? = null
)

data class PersonalizationSuiteItemResponse(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("gujaratiTitle") val gujaratiTitle: String? = null,
    @SerializedName("subtitle") val subtitle: String? = null,
    @SerializedName("badge") val badge: String? = null,
    @SerializedName("iconEmoji") val iconEmoji: String? = null,
    @SerializedName("enabled") val enabled: Boolean = true,
    @SerializedName("order") val order: Int = 0
)

data class HeroBannerResponse(
    @SerializedName("id") val id: String,
    @SerializedName("title") val title: String,
    @SerializedName("subtitle") val subtitle: String? = null,
    @SerializedName("badgeText") val badgeText: String? = null,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("actionType") val actionType: String = "category",
    @SerializedName("actionTarget") val actionTarget: String? = null,
    @SerializedName("sortOrder") val sortOrder: Int = 0,
    @SerializedName("isActive") val isActive: Boolean = true,
    @SerializedName("createdAt") val createdAt: Long = System.currentTimeMillis()
)

data class TrendingTagResponse(
    @SerializedName("id") val id: String,
    @SerializedName("tag") val tag: String,
    @SerializedName("icon") val icon: String = "🔥",
    @SerializedName("sortOrder") val sortOrder: Int = 0,
    @SerializedName("clickCount") val clickCount: Int = 0,
    @SerializedName("isActive") val isActive: Boolean = true
)

data class FingerprintPresetsResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("count") val count: Int = 0,
    @SerializedName("data") val data: List<com.parallax.wallpaper.model.FingerprintPreset>
)
