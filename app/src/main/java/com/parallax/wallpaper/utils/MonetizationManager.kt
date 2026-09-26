package com.parallax.wallpaper.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.parallax.wallpaper.data.api.AdmobConfigResponse

/**
 * 💰 MonetizationManager
 * Central controller for Google AdMob Live Monetization:
 * - Dynamic Over-the-Air Ad Unit IDs (App ID, Banner, Interstitial, Rewarded Video, App Open)
 * - Interstitial Frequency Capping (trigger full-screen ad after every N wallpaper views)
 * - Safe Google Sample Test IDs fallback to prevent account bans during development / review
 */
object MonetizationManager {
    private const val TAG = "MonetizationManager"
    private const val PREFS_NAME = "rewall_monetization_prefs"
    private const val KEY_VIEW_COUNT = "key_wallpaper_view_count"
    private const val KEY_REWARD_UNLOCK_COUNT = "key_reward_unlock_count"

    // Official Google AdMob sample test IDs
    const val GOOGLE_TEST_APP_ID = "ca-app-pub-3940256099942544~3347511713"
    const val GOOGLE_TEST_BANNER_ID = "ca-app-pub-3940256099942544/6300978111"
    const val GOOGLE_TEST_INTERSTITIAL_ID = "ca-app-pub-3940256099942544/1033173712"
    const val GOOGLE_TEST_REWARDED_ID = "ca-app-pub-3940256099942544/5224354917"
    const val GOOGLE_TEST_APP_OPEN_ID = "ca-app-pub-3940256099942544/9257395921"
    const val GOOGLE_TEST_NATIVE_ID = "ca-app-pub-3940256099942544/2247696110"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * Resolves the active AdMob App ID.
     */
    fun getAppId(config: AdmobConfigResponse?): String {
        return config?.appId?.takeIf { it.isNotBlank() } ?: GOOGLE_TEST_APP_ID
    }

    /**
     * Resolves the active Banner Ad Unit ID.
     */
    fun getBannerAdUnitId(config: AdmobConfigResponse?): String {
        if (isTestMode(config)) return GOOGLE_TEST_BANNER_ID
        return config?.bannerId?.takeIf { it.isNotBlank() } ?: GOOGLE_TEST_BANNER_ID
    }

    /**
     * Resolves the active Interstitial Ad Unit ID.
     */
    fun getInterstitialAdUnitId(config: AdmobConfigResponse?): String {
        if (isTestMode(config)) return GOOGLE_TEST_INTERSTITIAL_ID
        return config?.interstitialId?.takeIf { it.isNotBlank() } ?: GOOGLE_TEST_INTERSTITIAL_ID
    }

    /**
     * Resolves the active Rewarded Video Ad Unit ID.
     */
    fun getRewardedAdUnitId(config: AdmobConfigResponse?): String {
        if (isTestMode(config)) return GOOGLE_TEST_REWARDED_ID
        return config?.rewardedId?.takeIf { it.isNotBlank() } ?: GOOGLE_TEST_REWARDED_ID
    }

    /**
     * Resolves the active App Open Ad Unit ID.
     */
    fun getAppOpenAdUnitId(config: AdmobConfigResponse?): String {
        if (isTestMode(config)) return GOOGLE_TEST_APP_OPEN_ID
        return config?.appOpenId?.takeIf { it.isNotBlank() } ?: GOOGLE_TEST_APP_OPEN_ID
    }

    /**
     * Resolves the active Native Advanced Ad Unit ID.
     */
    fun getNativeAdUnitId(config: AdmobConfigResponse?): String {
        if (isTestMode(config)) return GOOGLE_TEST_NATIVE_ID
        return config?.nativeId?.takeIf { it.isNotBlank() } ?: GOOGLE_TEST_NATIVE_ID
    }

    /**
     * Checks if current AdMob monetization is operating in Test Mode.
     */
    fun isTestMode(config: AdmobConfigResponse?): Boolean {
        if (config == null) return true
        if (config.isTestMode) return true
        val appId = config.appId
        return appId.contains("3940256099942544")
    }

    /**
     * Checks if banner ads are globally enabled in remote configuration.
     */
    fun isBannerEnabled(config: AdmobConfigResponse?): Boolean {
        return config?.bannerEnabled ?: true
    }

    /**
     * Checks if interstitial ads are globally enabled in remote configuration.
     */
    fun isInterstitialEnabled(config: AdmobConfigResponse?): Boolean {
        return config?.interstitialEnabled ?: true
    }

    /**
     * Checks if native ads are globally enabled in remote configuration.
     */
    fun isNativeEnabled(config: AdmobConfigResponse?): Boolean {
        return config?.nativeEnabled ?: true
    }

    /**
     * Gets native ad frequency interval (e.g. show 1 ad every N items).
     */
    fun getNativeInterval(config: AdmobConfigResponse?): Int {
        return (config?.nativeInterval ?: 6).coerceAtLeast(2)
    }

    /**
     * Records a wallpaper view event and checks whether an Interstitial Ad should trigger
     * based on the Admin Panel frequency capping interval (e.g. every 2, 3, 4, 5, 8 views).
     */
    fun recordWallpaperView(context: Context, config: AdmobConfigResponse?): Boolean {
        val prefs = getPrefs(context)
        val currentCount = prefs.getInt(KEY_VIEW_COUNT, 0) + 1
        prefs.edit().putInt(KEY_VIEW_COUNT, currentCount).apply()

        val interval = (config?.interstitialInterval ?: 3).coerceAtLeast(1)
        val enabled = config?.interstitialEnabled ?: true

        val shouldTrigger = enabled && (currentCount % interval == 0)

        Log.d(TAG, "Wallpaper view #$currentCount | Interval: $interval | Should Trigger Interstitial: $shouldTrigger")
        return shouldTrigger
    }

    /**
     * Gets current total wallpaper view count.
     */
    fun getWallpaperViewCount(context: Context): Int {
        return getPrefs(context).getInt(KEY_VIEW_COUNT, 0)
    }

    /**
     * Trigger simulated or SDK interstitial ad presentation.
     */
    fun showInterstitial(
        context: Context,
        config: AdmobConfigResponse?,
        onDismissed: (() -> Unit)? = null
    ) {
        val unitId = getInterstitialAdUnitId(config)
        val isTest = isTestMode(config)
        Log.i(TAG, "🎬 Presenting Interstitial Ad [Unit: $unitId | TestMode: $isTest]")
        onDismissed?.invoke()
    }

    /**
     * Trigger simulated or SDK rewarded video ad presentation.
     */
    fun showRewarded(
        context: Context,
        config: AdmobConfigResponse?,
        onRewardEarned: (() -> Unit)? = null
    ) {
        val unitId = getRewardedAdUnitId(config)
        val isTest = isTestMode(config)
        Log.i(TAG, "🎁 Presenting Rewarded Video Ad [Unit: $unitId | TestMode: $isTest]")
        onRewardEarned?.invoke()
    }
}
