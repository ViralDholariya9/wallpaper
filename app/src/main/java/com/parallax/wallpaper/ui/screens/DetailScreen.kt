package com.parallax.wallpaper.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.parallax.wallpaper.data.WallpaperRepository
import com.parallax.wallpaper.gl.GLWallpaperTextureView
import com.parallax.wallpaper.gl.ParticleTheme
import com.parallax.wallpaper.model.WallpaperItem
import com.parallax.wallpaper.sensor.ParallaxSensorManager
import com.parallax.wallpaper.data.RemoteConfigManager
import com.parallax.wallpaper.ui.components.LockScreenOverlay
import com.parallax.wallpaper.ui.components.ParallaxStudioSheet
import com.parallax.wallpaper.ui.components.ReWallRatingDialog
import com.parallax.wallpaper.ui.components.SetWallpaperBottomSheet
import com.parallax.wallpaper.ui.theme.CardBorder
import com.parallax.wallpaper.ui.theme.DeepObsidian
import com.parallax.wallpaper.ui.theme.NeonCyan
import com.parallax.wallpaper.ui.theme.NeonPink
import com.parallax.wallpaper.ui.theme.NeonPurple
import com.parallax.wallpaper.ui.theme.SurfaceDark
import com.parallax.wallpaper.ui.theme.TextPrimary
import com.parallax.wallpaper.ui.theme.TextSecondary
import com.parallax.wallpaper.utils.HapticHelper
import com.parallax.wallpaper.utils.LiveWallpaperHelper
import com.parallax.wallpaper.utils.LiveWallpaperManager
import com.parallax.wallpaper.utils.MonetizationManager
import com.parallax.wallpaper.utils.RatingManager
import com.parallax.wallpaper.utils.WallpaperHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun DetailScreen(
    wallpaper: WallpaperItem,
    repository: WallpaperRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val favorites by repository.favorites.collectAsState()
    val isFavorite = favorites.contains(wallpaper.id)

    val sensorManager = remember { ParallaxSensorManager(context) }
    val tiltOffset by sensorManager.parallaxOffset.collectAsState()

    var showLockScreenSimulation by remember { mutableStateOf(false) }
    var showBottomSheet by remember { mutableStateOf(false) }
    var showEditor by remember { mutableStateOf(false) }
    var showRatingDialog by remember { mutableStateOf(false) }
    val remoteConfig by RemoteConfigManager.config.collectAsState()
    var isProcessing by remember { mutableStateOf(false) }
    var showAdRewardDialog by remember { mutableStateOf(false) }
    var isWatchingAd by remember { mutableStateOf(false) }

    var is4DShaderEnabled by remember { mutableStateOf(wallpaper.isParallax) }
    var loadedBitmap by remember { mutableStateOf<android.graphics.Bitmap?>(null) }

    // 3D Hologram Studio Live Controls State
    var showStudioSheet by remember { mutableStateOf(false) }
    var studioDepthMultiplier by remember { mutableFloatStateOf(1.0f) }
    var studioParticleTheme by remember { mutableStateOf(ParticleTheme.STARDUST) }
    var isFlareEnabled by remember { mutableStateOf(true) }
    var isHoloEnabled by remember { mutableStateOf(true) }
    var isSpringEnabled by remember { mutableStateOf(true) }
    var isDoubleTapEnabled by remember { mutableStateOf(LiveWallpaperManager.isDoubleTapEnabled(context)) }
    var isHapticsEnabled by remember { mutableStateOf(LiveWallpaperManager.isHapticsEnabled(context)) }
    val weatherSyncManager = remember { com.parallax.wallpaper.weather.WeatherSyncManager.getInstance(context) }
    var isWeatherOverlayEnabled by remember { mutableStateOf(com.parallax.wallpaper.weather.WeatherSyncManager.isOverlayEnabled(context)) }
    var selectedWeather by remember { mutableStateOf(com.parallax.wallpaper.weather.WeatherSyncManager.getActiveCondition(context)) }

    // Zero-Battery Optimizer Integration
    val batteryOptimizer = remember { com.parallax.wallpaper.power.BatteryOptimizer.getInstance(context) }
    val isZeroBatteryEnabled by batteryOptimizer.isUltraBatterySaverEnabled.collectAsState()
    val isDeviceMoving by sensorManager.isMoving.collectAsState()

    LaunchedEffect(wallpaper.previewUrl) {
        val bmp = WallpaperHelper.fetchBitmap(context, wallpaper.previewUrl)
        loadedBitmap = bmp
    }

    // AdMob Frequency Capping & Interstitial Trigger Point
    LaunchedEffect(wallpaper.id) {
        val shouldShowInterstitial = MonetizationManager.recordWallpaperView(context, remoteConfig?.admob)
        if (shouldShowInterstitial) {
            MonetizationManager.showInterstitial(context, remoteConfig?.admob)
        }
    }

    DisposableEffect(Unit) {
        sensorManager.startListening()
        onDispose {
            sensorManager.stopListening()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
    ) {
        // Multi-Layer / 3D Parallax Interactive Canvas
        DetailParallaxCanvas(
            wallpaper = wallpaper,
            tiltOffset = tiltOffset,
            is4DShaderEnabled = is4DShaderEnabled,
            isDeviceMoving = isDeviceMoving,
            loadedBitmap = loadedBitmap,
            studioDepthMultiplier = studioDepthMultiplier,
            studioParticleTheme = studioParticleTheme,
            isFlareEnabled = isFlareEnabled,
            isHoloEnabled = isHoloEnabled,
            isWeatherOverlayEnabled = isWeatherOverlayEnabled,
            selectedWeather = selectedWeather
        )

        // Lock Screen Simulation Overlay
        LockScreenOverlay(isVisible = showLockScreenSimulation)

        // Top Navigation Bar
        DetailTopBar(
            is4DShaderEnabled = is4DShaderEnabled,
            onToggle4D = { is4DShaderEnabled = !is4DShaderEnabled },
            isParallax = wallpaper.isParallax,
            onOpenStudio = { showStudioSheet = true },
            isPremium = wallpaper.isPremium,
            isUnlocked = wallpaper.isUnlocked,
            showLockScreenSimulation = showLockScreenSimulation,
            onToggleLockSimulation = { showLockScreenSimulation = !showLockScreenSimulation },
            onBack = onBack
        )

        // Right-Hand Floating Actions Column
        DetailFloatingActions(
            isFavorite = isFavorite,
            onToggleFavorite = { repository.toggleFavorite(wallpaper.id) },
            onOpenStudio = { showStudioSheet = true },
            onShare = {
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Check out '${wallpaper.title}' on ReWall 3D Parallax Wallpaper App!"
                    )
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Wallpaper"))
            },
            onEdit = { showEditor = true },
            onDownload = {
                scope.launch {
                    isProcessing = true
                    val bitmap = WallpaperHelper.fetchBitmap(context, wallpaper.previewUrl)
                    if (bitmap != null) {
                        val uri = WallpaperHelper.saveToGallery(context, bitmap, wallpaper.title)
                        if (uri != null) {
                            Toast.makeText(context, "Saved to Pictures/ParallaxWallpapers!", Toast.LENGTH_SHORT).show()
                            val shouldPrompt = RatingManager.recordDownloadOrApply(context, remoteConfig?.ratingPrompt)
                            if (shouldPrompt) {
                                showRatingDialog = true
                            }
                        } else {
                            Toast.makeText(context, "Failed to save wallpaper", Toast.LENGTH_SHORT).show()
                        }
                    }
                    isProcessing = false
                }
            },
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        )

        // Bottom Details & Apply / Unlock Bar
        DetailBottomBar(
            wallpaper = wallpaper,
            isProcessing = isProcessing,
            isUnlocked = wallpaper.isUnlocked,
            onWatchAd = { showAdRewardDialog = true },
            onApply = { showBottomSheet = true },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(24.dp)
        )

        // Apply Bottom Sheet
        if (showBottomSheet) {
            SetWallpaperBottomSheet(
                isParallax = wallpaper.isParallax,
                onDismiss = { showBottomSheet = false },
                onSetStaticTarget = { target ->
                    scope.launch {
                        isProcessing = true
                        val bitmap = WallpaperHelper.fetchBitmap(context, wallpaper.previewUrl)
                        if (bitmap != null) {
                            val success = WallpaperHelper.setWallpaper(context, bitmap, target)
                            if (success) {
                                Toast.makeText(context, "Wallpaper Applied Successfully!", Toast.LENGTH_SHORT).show()
                                val shouldPrompt = RatingManager.recordDownloadOrApply(context, remoteConfig?.ratingPrompt)
                                if (shouldPrompt) {
                                    showRatingDialog = true
                                }
                            } else {
                                Toast.makeText(context, "Failed to set wallpaper", Toast.LENGTH_SHORT).show()
                            }
                        }
                        isProcessing = false
                    }
                },
                onApplyLiveParallax = {
                    scope.launch {
                        isProcessing = true
                        Toast.makeText(context, "Preparing 3D Parallax Live Wallpaper...", Toast.LENGTH_SHORT).show()
                        val success = LiveWallpaperManager.setActiveWallpaper(context, wallpaper)
                        isProcessing = false
                        if (success) {
                            val launched = LiveWallpaperHelper.launchLiveWallpaperChooser(context)
                            if (launched) {
                                Toast.makeText(context, "Tap 'Set Wallpaper' in system preview", Toast.LENGTH_LONG).show()
                                val shouldPrompt = RatingManager.recordDownloadOrApply(context, remoteConfig?.ratingPrompt)
                                if (shouldPrompt) {
                                    showRatingDialog = true
                                }
                            }
                        } else {
                            Toast.makeText(context, "Failed to prepare 3D wallpaper", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            )
        }

        // Rewarded Ad Simulation Dialog
        if (showAdRewardDialog) {
            DetailAdRewardDialog(
                isWatchingAd = isWatchingAd,
                onConfirm = {
                    isWatchingAd = true
                    MonetizationManager.showRewarded(context, remoteConfig?.admob) {
                        scope.launch {
                            delay(3000)
                            isWatchingAd = false
                            showAdRewardDialog = false
                            repository.unlockWallpaper(wallpaper.id)
                            Toast.makeText(context, "🎉 Wallpaper Unlocked!", Toast.LENGTH_SHORT).show()
                        }
                    }
                },
                onDismiss = { showAdRewardDialog = false }
            )
        }

        // 3D Hologram FX Studio Sheet
        if (showStudioSheet) {
            DetailStudioSheet(
                studioDepthMultiplier = studioDepthMultiplier,
                onDepthChange = { studioDepthMultiplier = it },
                studioParticleTheme = studioParticleTheme,
                onThemeChange = { studioParticleTheme = it },
                isFlareEnabled = isFlareEnabled,
                onFlareToggle = { isFlareEnabled = it },
                isHoloEnabled = isHoloEnabled,
                onHoloToggle = { isHoloEnabled = it },
                isSpringEnabled = isSpringEnabled,
                onSpringToggle = {
                    isSpringEnabled = it
                    sensorManager.isSpringPhysicsEnabled = it
                },
                isZeroBatteryEnabled = isZeroBatteryEnabled,
                onZeroBatteryToggle = {
                    batteryOptimizer.setUltraBatterySaverEnabled(it)
                },
                isDoubleTapEnabled = isDoubleTapEnabled,
                onDoubleTapToggle = {
                    isDoubleTapEnabled = it
                    LiveWallpaperManager.setDoubleTapEnabled(context, it)
                    HapticHelper.click(context)
                },
                isHapticsEnabled = isHapticsEnabled,
                onHapticsToggle = {
                    isHapticsEnabled = it
                    LiveWallpaperManager.setHapticsEnabled(context, it)
                    if (it) HapticHelper.doubleTapSuccess(context)
                },
                isWeatherOverlayEnabled = isWeatherOverlayEnabled,
                onWeatherOverlayToggle = {
                    isWeatherOverlayEnabled = it
                    weatherSyncManager.setWeatherOverlayEnabled(it)
                },
                selectedWeather = selectedWeather,
                onWeatherConditionChange = {
                    selectedWeather = it
                    weatherSyncManager.setCondition(it)
                },
                onDismiss = { showStudioSheet = false }
            )
        }

        // Wallpaper Editor & Cropper Overlay
        if (showEditor) {
            WallpaperEditorScreen(
                wallpaper = wallpaper,
                onBack = { showEditor = false }
            )
        }

        // Smart Rate Us 5 Stars In-App Review Dialog
        if (showRatingDialog) {
            val ratingCfg = remoteConfig?.ratingPrompt ?: com.parallax.wallpaper.data.api.RatingPromptConfigResponse()
            ReWallRatingDialog(
                config = ratingCfg,
                onDismiss = { showRatingDialog = false }
            )
        }
    }
}

@Composable
private fun DetailParallaxCanvas(
    wallpaper: WallpaperItem,
    tiltOffset: com.parallax.wallpaper.sensor.ParallaxOffset,
    is4DShaderEnabled: Boolean,
    isDeviceMoving: Boolean,
    loadedBitmap: android.graphics.Bitmap?,
    studioDepthMultiplier: Float,
    studioParticleTheme: ParticleTheme,
    isFlareEnabled: Boolean,
    isHoloEnabled: Boolean,
    isWeatherOverlayEnabled: Boolean,
    selectedWeather: com.parallax.wallpaper.weather.WeatherCondition,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .fillMaxSize()
            .scale(1.12f)
    ) {
        val baseShiftX = (tiltOffset.x * 28f).roundToInt()
        val baseShiftY = (tiltOffset.y * 28f).roundToInt()

        AsyncImage(
            model = ImageRequest.Builder(context)
                .data(wallpaper.previewUrl)
                .crossfade(true)
                .build(),
            contentDescription = wallpaper.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(baseShiftX, baseShiftY) }
        )

        if (wallpaper.isParallax && wallpaper.layers.size > 1 && !is4DShaderEnabled) {
            wallpaper.layers.drop(1).forEach { layer ->
                val layerShiftX = (tiltOffset.x * layer.depth * 55f).roundToInt()
                val layerShiftY = (tiltOffset.y * layer.depth * 55f).roundToInt()

                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(layer.imageUrl)
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .offset { IntOffset(layerShiftX, layerShiftY) }
                )
            }
        }

        if (is4DShaderEnabled && loadedBitmap != null) {
            val userSensitivity = remember { LiveWallpaperManager.getSensitivity(context) }
            AndroidView(
                factory = { ctx ->
                    GLWallpaperTextureView(ctx).apply {
                        setDepthIntensity(0.09f * userSensitivity * studioDepthMultiplier)
                        setParticleTheme(studioParticleTheme)
                        setFlareIntensity(if (isFlareEnabled) 1.0f else 0.0f)
                        setHoloIntensity(if (isHoloEnabled) 1.0f else 0.0f)
                        loadedBitmap.let { setBitmap(it) }
                    }
                },
                update = { glView ->
                    loadedBitmap.let { glView.setBitmap(it) }
                    glView.setDepthIntensity(0.09f * userSensitivity * studioDepthMultiplier)
                    glView.setParticleTheme(studioParticleTheme)
                    glView.setFlareIntensity(if (isFlareEnabled) 1.0f else 0.0f)
                    glView.setHoloIntensity(if (isHoloEnabled) 1.0f else 0.0f)
                    glView.setIsDeviceMoving(isDeviceMoving)
                    glView.setSensorOffset(tiltOffset.x, tiltOffset.y)
                    glView.setWeatherOverlayEnabled(isWeatherOverlayEnabled)
                    glView.setWeatherCondition(selectedWeather)
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.55f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.85f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun DetailTopBar(
    is4DShaderEnabled: Boolean,
    onToggle4D: () -> Unit,
    isParallax: Boolean,
    onOpenStudio: () -> Unit,
    isPremium: Boolean,
    isUnlocked: Boolean,
    showLockScreenSimulation: Boolean,
    onToggleLockSimulation: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 24.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = Color.Black.copy(alpha = 0.5f),
            modifier = Modifier.size(44.dp)
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = if (is4DShaderEnabled) NeonCyan else Color.Black.copy(alpha = 0.6f),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (is4DShaderEnabled) NeonPurple else CardBorder
                ),
                modifier = Modifier.clickable { onToggle4D() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = if (is4DShaderEnabled) Color.Black else NeonCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (is4DShaderEnabled) "4D SHADER: ON" else "4D: OFF",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = if (is4DShaderEnabled) Color.Black else TextSecondary
                        )
                    )
                }
            }

            if (isParallax) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = NeonPurple.copy(alpha = 0.85f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.AutoAwesome,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "3D TILT",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = NeonCyan.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                    modifier = Modifier.clickable { onOpenStudio() }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Tune,
                            contentDescription = "3D FX Studio",
                            tint = NeonCyan,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "FX STUDIO",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White
                            )
                        )
                    }
                }
            }

            if (isPremium && !isUnlocked) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFFFD700).copy(alpha = 0.85f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LOCKED",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.Black
                            )
                        )
                    }
                }
            }
        }

        Surface(
            shape = CircleShape,
            color = if (showLockScreenSimulation) NeonCyan else Color.Black.copy(alpha = 0.5f),
            modifier = Modifier.size(44.dp)
        ) {
            IconButton(onClick = onToggleLockSimulation) {
                Icon(
                    imageVector = if (showLockScreenSimulation) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                    contentDescription = "Lock Screen Simulation",
                    tint = if (showLockScreenSimulation) Color.Black else TextPrimary
                )
            }
        }
    }
}

@Composable
private fun DetailFloatingActions(
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onOpenStudio: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDownload: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        FloatingActionCircle(
            icon = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
            iconTint = if (isFavorite) NeonPink else Color.White,
            onClick = onToggleFavorite
        )

        FloatingActionCircle(
            icon = Icons.Default.Tune,
            iconTint = NeonCyan,
            onClick = onOpenStudio
        )

        FloatingActionCircle(
            icon = Icons.Default.Share,
            onClick = onShare
        )

        FloatingActionCircle(
            icon = Icons.Default.Crop,
            onClick = onEdit
        )

        FloatingActionCircle(
            icon = Icons.Default.Download,
            iconTint = NeonCyan,
            onClick = onDownload
        )
    }
}

@Composable
private fun DetailBottomBar(
    wallpaper: WallpaperItem,
    isProcessing: Boolean,
    isUnlocked: Boolean,
    onWatchAd: () -> Unit,
    onApply: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = wallpaper.title,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = wallpaper.category.displayName,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = NeonCyan,
                    fontWeight = FontWeight.Medium
                )
            )
            Text(text = "•", color = TextSecondary)
            Text(
                text = "Ultra HD 4K",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
            Text(text = "•", color = TextSecondary)
            Text(
                text = "${wallpaper.downloads} downloads",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (wallpaper.isPremium && !isUnlocked) {
            Button(
                onClick = onWatchAd,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFFD700)
                )
            ) {
                Icon(
                    imageVector = Icons.Default.PlayCircle,
                    contentDescription = null,
                    tint = Color.Black,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Watch Ad & Unlock Wallpaper",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.Black
                    )
                )
            }
        } else {
            Button(
                onClick = onApply,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonPurple
                ),
                enabled = !isProcessing
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Wallpaper,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Apply Wallpaper",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailAdRewardDialog(
    isWatchingAd: Boolean,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = {
            if (!isWatchingAd) onDismiss()
        },
        containerColor = SurfaceDark,
        icon = {
            Icon(
                imageVector = Icons.Default.LockOpen,
                contentDescription = null,
                tint = Color(0xFFFFD700),
                modifier = Modifier.size(40.dp)
            )
        },
        title = {
            Text(
                text = if (isWatchingAd) "Playing Sponsored Video..." else "Unlock Premium Wallpaper",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
        },
        text = {
            if (isWatchingAd) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFFFFD700),
                        strokeWidth = 3.dp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Unlocking in 3 seconds...",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                }
            } else {
                Text(
                    text = "Watch a quick short video sponsor to permanently unlock this 4K Parallax wallpaper on your device.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                )
            }
        },
        confirmButton = {
            if (!isWatchingAd) {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700))
                ) {
                    Text("Watch & Unlock", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!isWatchingAd) {
                TextButton(onClick = onDismiss) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        }
    )
}

@Composable
private fun DetailStudioSheet(
    studioDepthMultiplier: Float,
    onDepthChange: (Float) -> Unit,
    studioParticleTheme: ParticleTheme,
    onThemeChange: (ParticleTheme) -> Unit,
    isFlareEnabled: Boolean,
    onFlareToggle: (Boolean) -> Unit,
    isHoloEnabled: Boolean,
    onHoloToggle: (Boolean) -> Unit,
    isSpringEnabled: Boolean,
    onSpringToggle: (Boolean) -> Unit,
    isZeroBatteryEnabled: Boolean,
    onZeroBatteryToggle: (Boolean) -> Unit,
    isDoubleTapEnabled: Boolean,
    onDoubleTapToggle: (Boolean) -> Unit,
    isHapticsEnabled: Boolean,
    onHapticsToggle: (Boolean) -> Unit,
    isWeatherOverlayEnabled: Boolean,
    onWeatherOverlayToggle: (Boolean) -> Unit,
    selectedWeather: com.parallax.wallpaper.weather.WeatherCondition,
    onWeatherConditionChange: (com.parallax.wallpaper.weather.WeatherCondition) -> Unit,
    onDismiss: () -> Unit
) {
    ParallaxStudioSheet(
        depthMultiplier = studioDepthMultiplier,
        onDepthChange = onDepthChange,
        selectedTheme = studioParticleTheme,
        onThemeChange = onThemeChange,
        isFlareEnabled = isFlareEnabled,
        onFlareToggle = onFlareToggle,
        isHoloEnabled = isHoloEnabled,
        onHoloToggle = onHoloToggle,
        isSpringPhysicsEnabled = isSpringEnabled,
        onSpringPhysicsToggle = onSpringToggle,
        isZeroBatteryEnabled = isZeroBatteryEnabled,
        onZeroBatteryToggle = onZeroBatteryToggle,
        isDoubleTapEnabled = isDoubleTapEnabled,
        onDoubleTapToggle = onDoubleTapToggle,
        isHapticsEnabled = isHapticsEnabled,
        onHapticsToggle = onHapticsToggle,
        isWeatherOverlayEnabled = isWeatherOverlayEnabled,
        onWeatherOverlayToggle = onWeatherOverlayToggle,
        selectedWeather = selectedWeather,
        onWeatherConditionChange = onWeatherConditionChange,
        onDismiss = onDismiss
    )
}

@Composable
private fun FloatingActionCircle(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color = Color.White,
    onClick: () -> Unit
) {
    Surface(
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.55f),
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
        modifier = Modifier.size(46.dp)
    ) {
        IconButton(onClick = onClick) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
