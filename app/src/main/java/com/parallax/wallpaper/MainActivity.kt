package com.parallax.wallpaper

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import com.parallax.wallpaper.data.ChargingRepository
import com.parallax.wallpaper.data.RingtoneRepository
import com.parallax.wallpaper.data.StatusRepository
import com.parallax.wallpaper.data.WallpaperRepository
import com.parallax.wallpaper.data.api.ChargingAnimationItem
import com.parallax.wallpaper.model.RingtoneItem
import com.parallax.wallpaper.model.WallpaperItem
import com.parallax.wallpaper.ui.components.DrawerItem
import com.parallax.wallpaper.ui.components.MusicVisualizerDialog
import com.parallax.wallpaper.ui.components.ReWallDrawer
import com.parallax.wallpaper.ui.components.WeatherSyncDialog
import com.parallax.wallpaper.weather.WeatherSyncManager
import com.parallax.wallpaper.ui.screens.AiWallpaperScreen
import com.parallax.wallpaper.ui.screens.ChargingAnimationsScreen
import com.parallax.wallpaper.ui.screens.ChargingPreviewScreen
import com.parallax.wallpaper.ui.screens.Custom3DParallaxMakerScreen
import com.parallax.wallpaper.ui.screens.DetailScreen
import com.parallax.wallpaper.ui.screens.FavoritesScreen
import com.parallax.wallpaper.ui.screens.HomeScreen
import com.parallax.wallpaper.ui.screens.RingtoneCutterScreen
import com.parallax.wallpaper.ui.screens.RingtonesScreen
import com.parallax.wallpaper.ui.screens.SettingsScreen
import com.parallax.wallpaper.ui.screens.StatusSaverScreen
import com.parallax.wallpaper.ui.theme.CardBorder
import com.parallax.wallpaper.ui.theme.DeepObsidian
import com.parallax.wallpaper.ui.theme.NeonCyan
import com.parallax.wallpaper.ui.theme.NeonPurple
import com.parallax.wallpaper.ui.theme.ParallaxWallpaperTheme
import com.parallax.wallpaper.ui.theme.SurfaceDark
import com.parallax.wallpaper.ui.theme.TextPrimary
import com.parallax.wallpaper.ui.theme.TextSecondary
import com.parallax.wallpaper.visualizer.MusicVisualizerManager
import com.parallax.wallpaper.ui.theme.ThemeManager
import com.parallax.wallpaper.work.DailyWallpaperWorker
import androidx.compose.runtime.collectAsState
import kotlinx.coroutines.launch

enum class NavTab(
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    EXPLORE("Explore", Icons.Filled.Explore, Icons.Outlined.Explore),
    FAVORITES("Favorites", Icons.Filled.Favorite, Icons.Outlined.FavoriteBorder),
    SETTINGS("Settings", Icons.Filled.Settings, Icons.Outlined.Settings)
}

class MainActivity : ComponentActivity() {

    companion object {
        const val ACTION_LIVE_TEST = "com.parallax.wallpaper.ACTION_LIVE_TEST"
    }

    private val repository = WallpaperRepository()
    private val liveTestWallpaper = kotlinx.coroutines.flow.MutableStateFlow<WallpaperItem?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        ThemeManager.init(this)
        com.parallax.wallpaper.island.DynamicIslandManager.init(this)
        com.parallax.wallpaper.edge.EdgeLightingManager.init(this)
        com.parallax.wallpaper.aod.AODManager.init(this)
        com.parallax.wallpaper.callscreen.CallScreenManager.init(this)
        com.parallax.wallpaper.touch.TouchEffectManager.init(this)
        com.parallax.wallpaper.fingerprint.FingerprintAnimationManager.init(this)
        DailyWallpaperWorker.scheduleDaily(this)
        syncDeviceTelemetry()
        com.parallax.wallpaper.data.RemoteConfigManager.sync(this)

        handleIncomingIntent(intent)

        setContent {
            val themeMode by ThemeManager.themeMode.collectAsState()
            ParallaxWallpaperTheme(themeMode = themeMode) {
                MainAppContent(
                    repository = repository,
                    liveTestWallpaperFlow = liveTestWallpaper
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleIncomingIntent(intent)
    }

    private fun handleIncomingIntent(intent: Intent?) {
        val targetIntent = intent ?: return
        val action = targetIntent.action
        val previewType = targetIntent.getStringExtra("preview_type")
        val previewId = targetIntent.getStringExtra("preview_id") ?: targetIntent.getStringExtra("wallpaper_id")
        val themeId = targetIntent.getStringExtra("theme_id")
        val clockId = targetIntent.getStringExtra("clock_id")

        if (action == ACTION_LIVE_TEST || previewType != null || previewId != null) {
            wakeScreen()

            when (previewType) {
                "wallpaper" -> {
                    val id = previewId ?: return
                    kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Main).launch {
                        val wallpaper = repository.fetchOrGetWallpaperById(id)
                        if (wallpaper != null) {
                            liveTestWallpaper.value = wallpaper
                            Toast.makeText(this@MainActivity, "📲 Live Test: Previewing ${wallpaper.title}", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(this@MainActivity, "📲 Wallpaper ID not found: $id", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                "call_screen", "callscreen" -> {
                    val id = themeId ?: previewId ?: "call_cyber_matrix_2077"
                    val callIntent = Intent(this, com.parallax.wallpaper.callscreen.IncomingCallActivity::class.java).apply {
                        putExtra("theme_id", id)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    startActivity(callIntent)
                }
                "aod" -> {
                    val id = clockId ?: previewId ?: "aod_cyberpunk_2077"
                    val aodIntent = Intent(this, com.parallax.wallpaper.aod.AODActivity::class.java).apply {
                        putExtra("clock_id", id)
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    }
                    startActivity(aodIntent)
                }
            }
        }
    }

    private fun wakeScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                android.view.WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onResume() {
        super.onResume()
        com.parallax.wallpaper.data.RemoteConfigManager.sync(this)
    }

    private fun syncDeviceTelemetry() {
        val prefs = getSharedPreferences("parallax_fcm_prefs", Context.MODE_PRIVATE)
        val savedToken = prefs.getString("fcm_token", null) ?: "dev_${Build.MODEL.replace("\\s+".toRegex(), "_")}"

        val deviceId = try {
            Settings.Secure.getString(contentResolver, Settings.Secure.ANDROID_ID) ?: "dev_${Build.MODEL.replace("\\s+".toRegex(), "_")}"
        } catch (e: Exception) {
            "dev_${Build.MODEL.replace("\\s+".toRegex(), "_")}"
        }

        val displayMetrics = resources.displayMetrics
        val screenResolution = "${displayMetrics.widthPixels}x${displayMetrics.heightPixels}"
        val screenDpi = displayMetrics.densityDpi

        val batteryManager = getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val batteryLevel = batteryManager?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: 85

        kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.IO).launch {
            try {
                val model = "${Build.MANUFACTURER} ${Build.MODEL}"
                com.parallax.wallpaper.data.api.ApiClient.service.sendDeviceTelemetry(
                    com.parallax.wallpaper.data.api.DeviceTelemetryRequest(
                        deviceId = deviceId,
                        deviceModel = Build.MODEL,
                        manufacturer = Build.MANUFACTURER,
                        brand = Build.BRAND,
                        androidVersion = "Android ${Build.VERSION.RELEASE}",
                        apiLevel = Build.VERSION.SDK_INT,
                        appVersion = "1.0.0",
                        screenResolution = screenResolution,
                        screenDpi = screenDpi,
                        batteryLevel = batteryLevel,
                        fcmToken = savedToken
                    )
                )
                android.util.Log.d("MainActivity", "Device telemetry synced with ReWall backend: $model ($deviceId)")
            } catch (e: Exception) {
                android.util.Log.w("MainActivity", "Device telemetry sync fallback: ${e.message}")
            }
        }
    }
}

@Composable
fun MainAppContent(
    repository: WallpaperRepository,
    liveTestWallpaperFlow: kotlinx.coroutines.flow.StateFlow<WallpaperItem?>
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    val ringtoneRepository = remember { RingtoneRepository() }
    val statusRepository = remember { StatusRepository() }
    val chargingRepository = remember { ChargingRepository() }
    val edgeLightingRepository = remember { com.parallax.wallpaper.data.EdgeLightingRepository() }
    val dynamicIslandRepository = remember { com.parallax.wallpaper.data.DynamicIslandRepository() }
    val aodRepository = remember { com.parallax.wallpaper.data.AODRepository() }
    val callScreenRepository = remember { com.parallax.wallpaper.data.CallScreenRepository() }
    val duoWallpaperRepository = remember { com.parallax.wallpaper.data.DuoWallpaperRepository() }
    val touchEffectsRepository = remember { com.parallax.wallpaper.data.TouchEffectsRepository() }
    val fingerprintRepository = remember { com.parallax.wallpaper.data.FingerprintRepository() }
    val weatherSyncManager = remember { WeatherSyncManager(context) }
    val visualizerManager = remember { MusicVisualizerManager(context) }

    var currentTab by remember { mutableStateOf(NavTab.EXPLORE) }
    var selectedWallpaper by remember { mutableStateOf<WallpaperItem?>(null) }

    val liveTestItem by liveTestWallpaperFlow.collectAsState()
    androidx.compose.runtime.LaunchedEffect(liveTestItem) {
        if (liveTestItem != null) {
            selectedWallpaper = liveTestItem
        }
    }
    var showAutoChangerDialog by remember { mutableStateOf(false) }
    var showRingtonesScreen by remember { mutableStateOf(false) }
    var selectedRingtoneForCut by remember { mutableStateOf<RingtoneItem?>(null) }
    var showStatusSaverScreen by remember { mutableStateOf(false) }
    var showChargingScreen by remember { mutableStateOf(false) }
    var selectedChargingAnimForPreview by remember { mutableStateOf<ChargingAnimationItem?>(null) }
    var showEdgeLightingScreen by remember { mutableStateOf(false) }
    var showDynamicIslandScreen by remember { mutableStateOf(false) }
    var showAodScreen by remember { mutableStateOf(false) }
    var showCallScreenScreen by remember { mutableStateOf(false) }
    var showDuoWallpaperScreen by remember { mutableStateOf(false) }
    var showTouchEffectsScreen by remember { mutableStateOf(false) }
    var showFingerprintScreen by remember { mutableStateOf(false) }
    var showAiWallpaperScreen by remember { mutableStateOf(false) }
    var show3DMakerScreen by remember { mutableStateOf(false) }
    var showWeatherDialog by remember { mutableStateOf(false) }
    var showVisualizerDialog by remember { mutableStateOf(false) }
    var showCategoryExplorerDirectly by remember { mutableStateOf(false) }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ReWallDrawer(
                onItemSelected = { item ->
                    scope.launch { drawerState.close() }
                    when (item) {
                        DrawerItem.ALL_WALLPAPERS -> currentTab = NavTab.EXPLORE
                        DrawerItem.PARALLAX_3D -> currentTab = NavTab.EXPLORE
                        DrawerItem.CATEGORIES_EXPLORER -> {
                            currentTab = NavTab.EXPLORE
                            showCategoryExplorerDirectly = true
                        }
                        DrawerItem.ALWAYS_ON_DISPLAY -> showAodScreen = true
                        DrawerItem.CALL_SCREEN -> showCallScreenScreen = true
                        DrawerItem.DYNAMIC_ISLAND -> showDynamicIslandScreen = true
                        DrawerItem.EDGE_LIGHTING -> showEdgeLightingScreen = true
                        DrawerItem.CHARGING_ANIMATIONS -> showChargingScreen = true
                        DrawerItem.CUSTOM_3D_MAKER -> show3DMakerScreen = true
                        DrawerItem.AI_STUDIO -> showAiWallpaperScreen = true
                        DrawerItem.WEATHER_SYNC -> showWeatherDialog = true
                        DrawerItem.MUSIC_VISUALIZER -> showVisualizerDialog = true
                        DrawerItem.RINGTONES -> showRingtonesScreen = true
                        DrawerItem.STATUS_SAVER -> showStatusSaverScreen = true
                        DrawerItem.DOUBLE_WALLPAPER -> showDuoWallpaperScreen = true
                        DrawerItem.TOUCH_EFFECTS -> showTouchEffectsScreen = true
                        DrawerItem.FINGERPRINT_ANIMATIONS -> showFingerprintScreen = true
                        DrawerItem.WALLPAPER_CHANGER -> showAutoChangerDialog = true
                        DrawerItem.FAVORITES -> currentTab = NavTab.FAVORITES
                        DrawerItem.DOWNLOADS -> {
                            Toast.makeText(context, "Viewing Pictures/ParallaxWallpapers folder", Toast.LENGTH_SHORT).show()
                        }
                        DrawerItem.REMOVE_ADS -> {
                            Toast.makeText(context, "ReWall PRO VIP - Ads Removed!", Toast.LENGTH_SHORT).show()
                        }
                        DrawerItem.RATE_US -> {
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=${context.packageName}")))
                            } catch (e: Exception) {
                                context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")))
                            }
                        }
                        DrawerItem.SHARE_APP -> {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "Download ReWall 3D Parallax Wallpaper App: https://play.google.com/store/apps/details?id=${context.packageName}")
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share ReWall"))
                        }
                        DrawerItem.PRIVACY_POLICY -> currentTab = NavTab.SETTINGS
                    }
                }
            )
        }
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(DeepObsidian)
        ) {
            Scaffold(
                bottomBar = {
                    if (selectedWallpaper == null) {
                        NavigationBar(
                            containerColor = SurfaceDark,
                            contentColor = TextPrimary
                        ) {
                            NavTab.values().forEach { tab ->
                                val isSelected = tab == currentTab
                                NavigationBarItem(
                                    selected = isSelected,
                                    onClick = { currentTab = tab },
                                    icon = {
                                        Icon(
                                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title
                                        )
                                    },
                                    label = { Text(text = tab.title) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = NeonCyan,
                                        selectedTextColor = NeonCyan,
                                        unselectedIconColor = TextSecondary,
                                        unselectedTextColor = TextSecondary,
                                        indicatorColor = NeonPurple.copy(alpha = 0.25f)
                                    )
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (currentTab) {
                        NavTab.EXPLORE -> {
                            HomeScreen(
                                repository = repository,
                                onWallpaperClick = { selectedWallpaper = it },
                                onOpenDrawer = { scope.launch { drawerState.open() } },
                                onOpenAiStudio = { showAiWallpaperScreen = true },
                                onOpen3DMaker = { show3DMakerScreen = true },
                                onOpenEdgeLighting = { showEdgeLightingScreen = true },
                                onOpenDynamicIsland = { showDynamicIslandScreen = true },
                                onOpenAod = { showAodScreen = true },
                                onOpenCallScreen = { showCallScreenScreen = true },
                                onOpenDuoWallpapers = { showDuoWallpaperScreen = true },
                                onOpenTouchEffects = { showTouchEffectsScreen = true },
                                onOpenFingerprint = { showFingerprintScreen = true },
                                openCategoryExplorerDirectly = showCategoryExplorerDirectly,
                                onCategoryExplorerClosed = { showCategoryExplorerDirectly = false }
                            )
                        }
                        NavTab.FAVORITES -> {
                            FavoritesScreen(
                                repository = repository,
                                onWallpaperClick = { selectedWallpaper = it }
                            )
                        }
                        NavTab.SETTINGS -> {
                            SettingsScreen()
                        }
                    }
                }
            }

            // Full-screen Wallpaper Detail Overlay with Lock Screen simulation
            selectedWallpaper?.let { wallpaper ->
                DetailScreen(
                    wallpaper = wallpaper,
                    repository = repository,
                    onBack = { selectedWallpaper = null }
                )
            }

            // Auto Wallpaper Changer Dialog from drawer
            if (showAutoChangerDialog) {
                com.parallax.wallpaper.ui.components.AutoWallpaperChangerDialog(
                    onDismiss = { showAutoChangerDialog = false }
                )
            }

            // Option C: Ringtones Screen
            if (showRingtonesScreen) {
                RingtonesScreen(
                    repository = ringtoneRepository,
                    onBack = { showRingtonesScreen = false },
                    onCutRingtone = { ringtone -> selectedRingtoneForCut = ringtone }
                )
            }

            // Option C: Ringtone Cutter Screen
            selectedRingtoneForCut?.let { ringtone ->
                RingtoneCutterScreen(
                    ringtone = ringtone,
                    onBack = { selectedRingtoneForCut = null }
                )
            }

            // Option C: Status Saver Screen
            if (showStatusSaverScreen) {
                StatusSaverScreen(
                    repository = statusRepository,
                    onBack = { showStatusSaverScreen = false }
                )
            }

            // Charging Animations Screen
            if (showChargingScreen) {
                ChargingAnimationsScreen(
                    repository = chargingRepository,
                    onBackClick = { showChargingScreen = false },
                    onPreviewClick = { anim ->
                        selectedChargingAnimForPreview = anim
                    }
                )
            }

            // Charging Animation Interactive Live Preview
            selectedChargingAnimForPreview?.let { anim ->
                ChargingPreviewScreen(
                    animationItem = anim,
                    repository = chargingRepository,
                    onBackClick = { selectedChargingAnimForPreview = null }
                )
            }

            // 🌈 Dynamic Edge Lighting Screen
            if (showEdgeLightingScreen) {
                com.parallax.wallpaper.ui.screens.EdgeLightingScreen(
                    repository = edgeLightingRepository,
                    onBackClick = { showEdgeLightingScreen = false }
                )
            }

            // 🏝️ Dynamic Island Smart Notch Screen
            if (showDynamicIslandScreen) {
                com.parallax.wallpaper.ui.screens.DynamicIslandScreen(
                    repository = dynamicIslandRepository,
                    onBackClick = { showDynamicIslandScreen = false }
                )
            }

            // 🕒 Always-On Display (AOD) Clocks & Widgets Screen
            if (showAodScreen) {
                com.parallax.wallpaper.ui.screens.AODScreen(
                    repository = aodRepository,
                    onBackClick = { showAodScreen = false }
                )
            }

            // 📞 3D Color Call Screen & Flash Themes Screen
            if (showCallScreenScreen) {
                com.parallax.wallpaper.ui.screens.CallScreenScreen(
                    repository = callScreenRepository,
                    onBackClick = { showCallScreenScreen = false }
                )
            }

            // 👥 Option 5: Duo / Double Wallpapers (Lock & Home Magic Pair)
            if (showDuoWallpaperScreen) {
                com.parallax.wallpaper.ui.screens.DuoWallpaperScreen(
                    repository = duoWallpaperRepository,
                    onBackClick = { showDuoWallpaperScreen = false }
                )
            }

            // 👆 Option 6: Interactive Touch Fluid & Ripple Effects Studio
            if (showTouchEffectsScreen) {
                com.parallax.wallpaper.ui.screens.TouchEffectsScreen(
                    repository = touchEffectsRepository,
                    onBackClick = { showTouchEffectsScreen = false }
                )
            }

            // 🔓 Option 7: In-Display Fingerprint Animations Studio
            if (showFingerprintScreen) {
                com.parallax.wallpaper.ui.screens.FingerprintAnimationsScreen(
                    repository = fingerprintRepository,
                    onBackClick = { showFingerprintScreen = false }
                )
            }

            // Sprint 3: Gemini AI Wallpaper Studio Screen
            if (showAiWallpaperScreen) {
                AiWallpaperScreen(
                    repository = repository,
                    onBack = { showAiWallpaperScreen = false }
                )
            }

            // DIY Custom 3D Parallax Maker Screen
            if (show3DMakerScreen) {
                Custom3DParallaxMakerScreen(
                    onBack = { show3DMakerScreen = false }
                )
            }

            // Sprint 3: Real-time Weather Sync Dialog
            if (showWeatherDialog) {
                WeatherSyncDialog(
                    weatherSyncManager = weatherSyncManager,
                    onDismiss = { showWeatherDialog = false }
                )
            }

            // Sprint 3: Music Beat Visualizer Dialog
            if (showVisualizerDialog) {
                MusicVisualizerDialog(
                    visualizerManager = visualizerManager,
                    onDismiss = { showVisualizerDialog = false }
                )
            }

            // Remote Config Mandatory Force Update Dialog
            val remoteConfig by com.parallax.wallpaper.data.RemoteConfigManager.config.collectAsState()
            if (com.parallax.wallpaper.data.RemoteConfigManager.isForceUpdateRequired(context)) {
                remoteConfig?.forceUpdate?.let { forceConfig ->
                    com.parallax.wallpaper.ui.components.ForceUpdateDialog(forceUpdateConfig = forceConfig)
                }
            }
        }
    }
}
