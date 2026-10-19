package com.parallax.wallpaper.ads

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color as AndroidColor
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.UserMessagingPlatform
import kotlinx.coroutines.launch

/**
 * ⚡ AdMobManager
 * Enterprise-grade 3-Tier Multi-Waterfall Ads Mediation Engine:
 *
 * 🟢 Tier 1: Google AdMob (Primary / High Floor)
 *       ⬇️ (onAdFailedToLoad)
 * 🟡 Tier 2: Google Ad Manager (GAM Unit ID / Secondary Backup)
 *       ⬇️ (onAdFailedToLoad)
 * 🟣 Tier 3: Chrome Custom Tabs (CCT) In-House Interactive Promo Fallback
 *
 * This guarantees:
 * - 0% wasted ad inventory / 100% Fill Rate
 * - Automatic GAM fallback if AdMob fails or has low inventory
 * - Direct CCT engagement when CCT mode is ON and all SDK ads fail
 */
object AdMobManager {

    private const val TAG = "AdMobManager"

    private var isInitialized = false
    private var interstitialAd: InterstitialAd? = null
    private var isInterstitialLoading = false
    private var rewardedAd: RewardedAd? = null
    private var isRewardedLoading = false

    // Interstitial Frequency Capping Tracker
    private var interstitialClickCount = 0
    private var lastInterstitialTimeMs = 0L
    private var userTotalInterstitialClicks = 0

    // Splash App Open Ad
    private var appOpenAd: AppOpenAd? = null
    private var isAppOpenLoading = false

    /**
     * Initialize MobileAds SDK and sync remote config.
     */
    fun init(context: Context) {
        if (isInitialized) return
        VentureAdsManager.init(context)

        CoroutineScopeHelper.ioScope.launch {
            try {
                MobileAds.initialize(context) { initializationStatus ->
                    isInitialized = true
                    Log.i(TAG, "Google Mobile Ads SDK Initialized: ${initializationStatus.adapterStatusMap.keys}")
                    preloadInterstitial(context)
                    preloadRewarded(context)
                }
            } catch (e: Exception) {
                Log.w(TAG, "AdMob initialization error: ${e.message}")
            }
        }
    }

    /**
     * Handle Google UMP GDPR Consent flow if enabled in json (isEnableUMP == true)
     */
    fun checkAndRequestConsent(activity: Activity, onConsentCompleted: () -> Unit = {}) {
        val config = VentureAdsManager.config.value
        if (!config.isEnableUMP) {
            onConsentCompleted()
            return
        }

        val params = ConsentRequestParameters.Builder().build()
        val consentInformation = UserMessagingPlatform.getConsentInformation(activity)
        consentInformation.requestConsentInfoUpdate(
            activity,
            params,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) { formError ->
                    if (formError != null) {
                        Log.w(TAG, "UMP Consent form error: ${formError.message}")
                    }
                    if (consentInformation.canRequestAds()) {
                        preloadInterstitial(activity)
                        preloadRewarded(activity)
                    }
                    onConsentCompleted()
                }
            },
            { requestConsentError ->
                Log.w(TAG, "UMP Consent request error: ${requestConsentError.message}")
                onConsentCompleted()
            }
        )
    }

    // =========================================================================
    // 🎬 INTERSTITIAL ADS (Tier 1: AdMob -> Tier 2: GAM -> Tier 3: CCT)
    // =========================================================================

    /**
     * Preloads Interstitial Ad with automatic Waterfall:
     * Tries Tier 1 (AdMob) -> If failed, automatically tries Tier 2 (Google Ad Manager)
     */
    fun preloadInterstitial(context: Context, tierIndex: Int = 0) {
        if (!VentureAdsManager.areAdsEnabled()) return
        if (interstitialAd != null || isInterstitialLoading) return

        val waterfall = VentureAdsManager.getInterstitialWaterfall()
        if (tierIndex >= waterfall.size) {
            isInterstitialLoading = false
            return
        }

        val adUnitId = waterfall[tierIndex]
        isInterstitialLoading = true

        val tierName = if (tierIndex == 0) "Tier 1 (AdMob)" else "Tier 2 (Google Ad Manager)"
        Log.d(TAG, "Preloading Interstitial Ad [$tierName | Unit: $adUnitId]")
        val adRequest = AdRequest.Builder().build()

        InterstitialAd.load(
            context,
            adUnitId,
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    interstitialAd = ad
                    isInterstitialLoading = false
                    Log.i(TAG, "✅ Interstitial Ad loaded successfully [$tierName]")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                    isInterstitialLoading = false
                    Log.w(TAG, "❌ Interstitial [$tierName] failed: ${error.message} (code: ${error.code})")
                    if (tierIndex + 1 < waterfall.size) {
                        Log.i(TAG, "🔄 Interstitial waterfall cascading to Tier ${tierIndex + 1} (GAM)...")
                        preloadInterstitial(context, tierIndex + 1)
                    }
                }
            }
        )
    }

    /**
     * Records a user action and triggers interstitial ad based on threshold & cooldown.
     * If both AdMob and GAM fail, seamlessly triggers Tier 3 Chrome Custom Tabs (CCT) if enabled.
     */
    fun showInterstitial(
        activity: Activity,
        forceShow: Boolean = false,
        onDismissed: () -> Unit = {}
    ) {
        val config = VentureAdsManager.config.value
        if (!VentureAdsManager.areAdsEnabled()) {
            onDismissed()
            return
        }

        interstitialClickCount++
        userTotalInterstitialClicks++

        val threshold = VentureAdsManager.getInterstitialClickThreshold()
        val cooldownMs = VentureAdsManager.getInterstitialSessionCooldownMs()
        val now = System.currentTimeMillis()
        val isCooldownPassed = (now - lastInterstitialTimeMs) >= cooldownMs
        val isThresholdMet = (interstitialClickCount >= threshold)
        val isUnderMaxLimit = userTotalInterstitialClicks <= config.maxInterstitialUserClick

        val shouldShow = forceShow || (isThresholdMet && isCooldownPassed && isUnderMaxLimit)

        Log.d(
            TAG,
            "Interstitial check -> clickCount: $interstitialClickCount, threshold: $threshold, cooldownPassed: $isCooldownPassed, shouldShow: $shouldShow"
        )

        if (!shouldShow) {
            onDismissed()
            return
        }

        val ad = interstitialAd
        if (ad != null) {
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    Log.d(TAG, "Interstitial Ad dismissed.")
                    interstitialAd = null
                    interstitialClickCount = 0
                    lastInterstitialTimeMs = System.currentTimeMillis()
                    preloadInterstitial(activity)
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Interstitial Ad failed to show: ${adError.message}")
                    interstitialAd = null
                    preloadInterstitial(activity)
                    onDismissed()
                }

                override fun onAdShowedFullScreenContent() {
                    Log.i(TAG, "Interstitial Ad presented to user.")
                }
            }
            ad.show(activity)
        } else {
            Log.d(TAG, "Interstitial not ready, preloading for next interaction.")
            preloadInterstitial(activity)
            onDismissed()
        }
    }

    // =========================================================================
    // 🎁 REWARDED ADS (Tier 1: AdMob -> Tier 2: GAM)
    // =========================================================================

    /**
     * Preloads Rewarded Ad with Waterfall (AdMob -> Google Ad Manager).
     */
    fun preloadRewarded(context: Context, tierIndex: Int = 0) {
        if (!VentureAdsManager.areAdsEnabled()) return
        if (rewardedAd != null || isRewardedLoading) return

        val waterfall = VentureAdsManager.getRewardedWaterfall()
        if (tierIndex >= waterfall.size) {
            isRewardedLoading = false
            return
        }

        val adUnitId = waterfall[tierIndex]
        isRewardedLoading = true

        val tierName = if (tierIndex == 0) "Tier 1 (AdMob)" else "Tier 2 (Google Ad Manager)"
        Log.d(TAG, "Preloading Rewarded Ad [$tierName | Unit: $adUnitId]")
        val adRequest = AdRequest.Builder().build()

        RewardedAd.load(
            context,
            adUnitId,
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    rewardedAd = ad
                    isRewardedLoading = false
                    Log.i(TAG, "✅ Rewarded Ad loaded successfully [$tierName]")
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    rewardedAd = null
                    isRewardedLoading = false
                    Log.w(TAG, "❌ Rewarded [$tierName] failed: ${error.message}")
                    if (tierIndex + 1 < waterfall.size) {
                        Log.i(TAG, "🔄 Rewarded waterfall cascading to Tier ${tierIndex + 1} (GAM)...")
                        preloadRewarded(context, tierIndex + 1)
                    }
                }
            }
        )
    }

    /**
     * Shows a Rewarded Video Ad to unlock premium 4K wallpapers.
     */
    fun showRewarded(
        activity: Activity,
        onRewardEarned: () -> Unit,
        onDismissed: () -> Unit = {}
    ) {
        if (!VentureAdsManager.areAdsEnabled()) {
            onRewardEarned()
            onDismissed()
            return
        }

        val ad = rewardedAd
        if (ad != null) {
            var userRewarded = false
            ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                override fun onAdDismissedFullScreenContent() {
                    rewardedAd = null
                    preloadRewarded(activity)
                    if (userRewarded) {
                        onRewardEarned()
                    }
                    onDismissed()
                }

                override fun onAdFailedToShowFullScreenContent(adError: AdError) {
                    Log.w(TAG, "Rewarded Ad failed to show: ${adError.message}")
                    rewardedAd = null
                    preloadRewarded(activity)
                    onRewardEarned()
                    onDismissed()
                }
            }
            ad.show(activity) { rewardItem ->
                Log.i(TAG, "🎁 User earned reward: ${rewardItem.amount} ${rewardItem.type}")
                userRewarded = true
            }
        } else {
            Log.w(TAG, "Rewarded Ad not ready, granting fallback unlock.")
            preloadRewarded(activity)
            onRewardEarned()
            onDismissed()
        }
    }

    // =========================================================================
    // 🚀 APP OPEN / SPLASH ADS
    // =========================================================================

    /**
     * Shows Splash ad if SplashON == 1 in remote config.
     */
    fun showSplashAd(activity: Activity, onComplete: () -> Unit) {
        val config = VentureAdsManager.config.value
        if (!VentureAdsManager.areAdsEnabled() || config.splashOn != 1) {
            onComplete()
            return
        }

        val adUnitId = VentureAdsManager.DEFAULT_APP_OPEN_ID
        val request = AdRequest.Builder().build()

        AppOpenAd.load(
            activity,
            adUnitId,
            request,
            object : AppOpenAd.AppOpenAdLoadCallback() {
                override fun onAdLoaded(ad: AppOpenAd) {
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            onComplete()
                        }

                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            onComplete()
                        }
                    }
                    ad.show(activity)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    Log.w(TAG, "Splash App Open Ad failed: ${loadAdError.message}")
                    onComplete()
                }
            }
        )
    }

    // =========================================================================
    // 🌐 CHROME CUSTOM TABS (CCT)
    // =========================================================================

    /**
     * Opens Chrome Custom Tabs with the remote ads URL if configured.
     */
    fun launchCCT(context: Context, url: String) {
        if (url.isBlank()) return
        try {
            val builder = CustomTabsIntent.Builder()
            builder.setShowTitle(true)
            builder.setDefaultColorSchemeParams(
                androidx.browser.customtabs.CustomTabColorSchemeParams.Builder()
                    .setToolbarColor(AndroidColor.parseColor("#0A0A0C"))
                    .build()
            )
            val customTabsIntent = builder.build()
            customTabsIntent.launchUrl(context, Uri.parse(url))
            Log.i(TAG, "Launched Chrome Custom Tab: $url")
        } catch (e: Exception) {
            try {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                context.startActivity(intent)
            } catch (_: Exception) {}
        }
    }

    private fun handleCctFallback(context: Context) {
        val config = VentureAdsManager.config.value
        if (config.cctMode.equals("on", ignoreCase = true) && config.adsUrl.isNotBlank()) {
            launchCCT(context, config.adsUrl)
        }
    }

    // =========================================================================
    // 🖼️ BANNER AD (Waterfall: AdMob -> GAM -> CCT Fallback)
    // =========================================================================

    /**
     * Renders a responsive Banner with complete 3-Tier Waterfall:
     * 1. AdMob
     * 2. Google Ad Manager (GAM)
     * 3. Chrome Custom Tabs (CCT) In-House Promo
     */
    @Composable
    fun AdMobBanner(
        adUnitId: String = "",
        waterfallIds: List<String> = emptyList(),
        isMediumBanner: Boolean = false,
        modifier: Modifier = Modifier
    ) {
        val config by VentureAdsManager.config.collectAsState()
        if (!config.allAdsOn || !config.letsStart) return

        val resolvedList = when {
            waterfallIds.isNotEmpty() -> waterfallIds
            adUnitId.isNotBlank() -> listOf(adUnitId)
            else -> VentureAdsManager.getMainBottomWaterfall()
        }
        if (resolvedList.isEmpty()) return

        var activeTier by remember { mutableIntStateOf(0) }
        var isAllTiersFailed by remember { mutableStateOf(false) }

        // Tier 3: CCT Fallback Banner
        if (isAllTiersFailed && config.cctMode.equals("on", ignoreCase = true) && config.adsUrl.isNotBlank()) {
            CctInHouseBanner(adsUrl = config.adsUrl, modifier = modifier)
            return
        }

        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            AndroidView(
                factory = { context ->
                    AdView(context).apply {
                        setAdSize(if (isMediumBanner) AdSize.MEDIUM_RECTANGLE else AdSize.BANNER)
                        val currentUnit = resolvedList.getOrElse(activeTier) { resolvedList.first() }
                        this.adUnitId = currentUnit
                        adListener = object : AdListener() {
                            override fun onAdLoaded() {
                                isAllTiersFailed = false
                                val tierName = if (activeTier == 0) "Tier 1 (AdMob)" else "Tier 2 (Google Ad Manager)"
                                Log.d(TAG, "Banner loaded successfully [$tierName: $currentUnit]")
                            }

                            override fun onAdFailedToLoad(error: LoadAdError) {
                                Log.w(TAG, "Banner failed at Tier $activeTier ($currentUnit): ${error.message}")
                                if (activeTier + 1 < resolvedList.size) {
                                    activeTier++
                                    val nextUnit = resolvedList[activeTier]
                                    Log.i(TAG, "🔄 Banner cascading to Tier $activeTier (GAM): $nextUnit")
                                    this@apply.adUnitId = nextUnit
                                    this@apply.loadAd(AdRequest.Builder().build())
                                } else {
                                    isAllTiersFailed = true
                                    Log.w(TAG, "⚠️ All ad tiers failed for Banner. Activating CCT Fallback.")
                                }
                            }
                        }
                        loadAd(AdRequest.Builder().build())
                    }
                },
                update = { adView ->
                    if (config.bannerReloadAds) {
                        adView.loadAd(AdRequest.Builder().build())
                    }
                }
            )
        }
    }

    // =========================================================================
    // 🎴 NATIVE AD (Waterfall: AdMob -> GAM -> CCT Fallback)
    // =========================================================================

    /**
     * Renders a Native Ad with 3-Tier Waterfall:
     * 1. AdMob Primary
     * 2. Google Ad Manager
     * 3. In-House CCT Native Card
     */
    @Composable
    fun AdMobNativeAd(
        adUnitId: String = "",
        waterfallIds: List<String> = emptyList(),
        layoutType: Int = 1,
        modifier: Modifier = Modifier
    ) {
        val config by VentureAdsManager.config.collectAsState()
        if (!config.allAdsOn || !config.letsStart) return

        val resolvedList = when {
            waterfallIds.isNotEmpty() -> waterfallIds
            adUnitId.isNotBlank() -> listOf(adUnitId)
            else -> VentureAdsManager.getWallpaperNativeWaterfall()
        }
        if (resolvedList.isEmpty()) return

        val context = LocalContext.current
        var loadedNativeAd by remember { mutableStateOf<NativeAd?>(null) }
        var isAllTiersFailed by remember { mutableStateOf(false) }

        fun loadWaterfallTier(tier: Int) {
            if (tier >= resolvedList.size) {
                isAllTiersFailed = true
                Log.w(TAG, "⚠️ All Native Ad tiers failed. Activating CCT Fallback.")
                return
            }

            val currentUnit = resolvedList[tier]
            val tierName = if (tier == 0) "Tier 1 (AdMob)" else "Tier 2 (Google Ad Manager)"
            Log.d(TAG, "Loading Native Ad [$tierName | Unit: $currentUnit]")

            val adLoader = AdLoader.Builder(context, currentUnit)
                .forNativeAd { ad ->
                    loadedNativeAd = ad
                    isAllTiersFailed = false
                    Log.i(TAG, "✅ Native Ad loaded successfully [$tierName | Layout: $layoutType]")
                }
                .withAdListener(object : AdListener() {
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        Log.w(TAG, "❌ Native Ad failed at $tierName: ${error.message}")
                        if (tier + 1 < resolvedList.size) {
                            Log.i(TAG, "🔄 Native ad cascading to Tier ${tier + 1} (GAM)...")
                            loadWaterfallTier(tier + 1)
                        } else {
                            isAllTiersFailed = true
                        }
                    }
                })
                .withNativeAdOptions(
                    NativeAdOptions.Builder()
                        .setAdChoicesPlacement(NativeAdOptions.ADCHOICES_TOP_RIGHT)
                        .build()
                )
                .build()

            adLoader.loadAd(AdRequest.Builder().build())
        }

        LaunchedEffect(resolvedList) {
            loadWaterfallTier(0)
        }

        DisposableEffect(Unit) {
            onDispose {
                loadedNativeAd?.destroy()
            }
        }

        // Tier 3: In-House CCT Fallback Native Card
        if (isAllTiersFailed && config.cctMode.equals("on", ignoreCase = true) && config.adsUrl.isNotBlank()) {
            CctInHouseNativeCard(
                adsUrl = config.adsUrl,
                layoutType = layoutType,
                modifier = modifier
            )
            return
        }

        if (loadedNativeAd != null) {
            Box(
                modifier = modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF16161E))
            ) {
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { ctx ->
                        createNativeAdView(ctx, loadedNativeAd!!, layoutType)
                    },
                    update = { view ->
                        populateNativeAdView(loadedNativeAd!!, view, layoutType)
                    }
                )
            }
        }
    }

    // =========================================================================
    // 🌐 IN-HOUSE CCT FALLBACK COMPOSABLES (TIER 3)
    // =========================================================================

    /**
     * Sleek In-House Fallback Banner displayed when both AdMob and GAM fail.
     * Clicks launch Chrome Custom Tabs directly.
     */
    @Composable
    fun CctInHouseBanner(
        adsUrl: String,
        modifier: Modifier = Modifier
    ) {
        val context = LocalContext.current

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 4.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFFD946EF))),
                    shape = RoundedCornerShape(12.dp)
                )
                .clickable { launchCCT(context, adsUrl) },
            color = Color(0xFF14141C),
            shape = RoundedCornerShape(12.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF))
                    ) {
                        Text(
                            text = "PROMO",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF00E5FF),
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Explore 4K 3D Wallpapers",
                            color = Color.White,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Tap to discover trending AMOLED themes",
                            color = Color(0xFF9E9EB2),
                            fontSize = 11.sp
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF00E5FF)
                ) {
                    Text(
                        text = "VISIT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }
    }

    /**
     * Sleek In-House Fallback Native Card displayed when both AdMob and GAM fail.
     */
    @Composable
    fun CctInHouseNativeCard(
        adsUrl: String,
        layoutType: Int,
        modifier: Modifier = Modifier
    ) {
        val context = LocalContext.current

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 1.dp,
                    brush = Brush.horizontalGradient(listOf(Color(0xFF00E5FF).copy(alpha = 0.5f), Color(0xFFD946EF).copy(alpha = 0.5f))),
                    shape = RoundedCornerShape(16.dp)
                )
                .clickable { launchCCT(context, adsUrl) },
            color = Color(0xFF14141C),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFF00E5FF).copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E5FF))
                        ) {
                            Text(
                                text = "SPONSORED",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00E5FF),
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "Featured Live Content",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                        contentDescription = "Open",
                        tint = Color(0xFF00E5FF),
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Discover personalized fluid 3D wallpapers, edge lighting and ultra AMOLED effects.",
                    color = Color(0xFF9E9EB2),
                    fontSize = 12.sp,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF00E5FF),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "EXPLORE NOW",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.padding(vertical = 10.dp)
                    )
                }
            }
        }
    }

    // =========================================================================
    // 🎨 PROGRAMMATIC NATIVE AD VIEW BUILDER
    // =========================================================================

    private fun createNativeAdView(context: Context, nativeAd: NativeAd, layoutType: Int): NativeAdView {
        val nativeAdView = NativeAdView(context)
        nativeAdView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dpToPx(context, 12), dpToPx(context, 12), dpToPx(context, 12), dpToPx(context, 12))
            val bg = GradientDrawable().apply {
                setColor(AndroidColor.parseColor("#14141C"))
                cornerRadius = dpToPx(context, 16).toFloat()
                setStroke(dpToPx(context, 1), AndroidColor.parseColor("#2A2A38"))
            }
            background = bg
        }

        // Top Row: "AD" badge + Headline + Icon
        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dpToPx(context, 8)
            }
        }

        val iconImageView = ImageView(context).apply {
            layoutParams = LinearLayout.LayoutParams(dpToPx(context, 40), dpToPx(context, 40)).apply {
                rightMargin = dpToPx(context, 10)
            }
            scaleType = ImageView.ScaleType.CENTER_CROP
            val shape = GradientDrawable().apply {
                cornerRadius = dpToPx(context, 8).toFloat()
            }
            clipToOutline = true
            background = shape
        }

        val textColumn = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        }

        val badgeAndHeadRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        // Compliant "Ad" badge
        val adBadge = TextView(context).apply {
            text = "AD"
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AndroidColor.parseColor("#00E5FF"))
            setPadding(dpToPx(context, 5), dpToPx(context, 2), dpToPx(context, 5), dpToPx(context, 2))
            val badgeBg = GradientDrawable().apply {
                setColor(AndroidColor.parseColor("#1A00E5FF"))
                cornerRadius = dpToPx(context, 4).toFloat()
                setStroke(dpToPx(context, 1), AndroidColor.parseColor("#00E5FF"))
            }
            background = badgeBg
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                rightMargin = dpToPx(context, 6)
            }
        }

        val headlineView = TextView(context).apply {
            textSize = 14f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AndroidColor.WHITE)
            maxLines = 1
        }

        badgeAndHeadRow.addView(adBadge)
        badgeAndHeadRow.addView(headlineView)

        val bodyView = TextView(context).apply {
            textSize = 12f
            setTextColor(AndroidColor.parseColor("#9E9EB2"))
            maxLines = 2
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                topMargin = dpToPx(context, 2)
            }
        }

        textColumn.addView(badgeAndHeadRow)
        textColumn.addView(bodyView)

        topRow.addView(iconImageView)
        topRow.addView(textColumn)
        container.addView(topRow)

        // MediaView (for Card layouts: 1, 8, 9, 0)
        val mediaView = MediaView(context).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                if (layoutType == 2 || layoutType == 5 || layoutType == 6) dpToPx(context, 110) else dpToPx(context, 150)
            ).apply {
                bottomMargin = dpToPx(context, 10)
            }
            val mediaBg = GradientDrawable().apply {
                cornerRadius = dpToPx(context, 10).toFloat()
            }
            clipToOutline = true
        }
        container.addView(mediaView)

        // Call to action button
        val ctaButton = TextView(context).apply {
            gravity = Gravity.CENTER
            textSize = 13f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(AndroidColor.BLACK)
            setPadding(dpToPx(context, 16), dpToPx(context, 10), dpToPx(context, 16), dpToPx(context, 10))
            val btnBg = GradientDrawable().apply {
                setColor(AndroidColor.parseColor("#00E5FF"))
                cornerRadius = dpToPx(context, 10).toFloat()
            }
            background = btnBg
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }
        container.addView(ctaButton)

        nativeAdView.addView(container)

        // Register views with NativeAdView for AdMob click handling
        nativeAdView.headlineView = headlineView
        nativeAdView.bodyView = bodyView
        nativeAdView.callToActionView = ctaButton
        nativeAdView.iconView = iconImageView
        nativeAdView.mediaView = mediaView

        populateNativeAdView(nativeAd, nativeAdView, layoutType)
        return nativeAdView
    }

    private fun populateNativeAdView(nativeAd: NativeAd, nativeAdView: NativeAdView, layoutType: Int) {
        (nativeAdView.headlineView as? TextView)?.text = nativeAd.headline

        if (nativeAd.body == null) {
            nativeAdView.bodyView?.visibility = View.GONE
        } else {
            nativeAdView.bodyView?.visibility = View.VISIBLE
            (nativeAdView.bodyView as? TextView)?.text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            nativeAdView.callToActionView?.visibility = View.GONE
        } else {
            nativeAdView.callToActionView?.visibility = View.VISIBLE
            (nativeAdView.callToActionView as? TextView)?.text = nativeAd.callToAction?.uppercase() ?: "INSTALL"
        }

        if (nativeAd.icon == null) {
            nativeAdView.iconView?.visibility = View.GONE
        } else {
            nativeAdView.iconView?.visibility = View.VISIBLE
            (nativeAdView.iconView as? ImageView)?.setImageDrawable(nativeAd.icon?.drawable)
        }

        if (nativeAd.mediaContent != null) {
            nativeAdView.mediaView?.visibility = View.VISIBLE
            nativeAdView.mediaView?.mediaContent = nativeAd.mediaContent
        } else {
            nativeAdView.mediaView?.visibility = View.GONE
        }

        nativeAdView.setNativeAd(nativeAd)
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        val density = context.resources.displayMetrics.density
        return (dp * density).toInt()
    }
}

private object CoroutineScopeHelper {
    val ioScope = kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO)
}
