package com.parallax.wallpaper.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import com.parallax.wallpaper.utils.HapticHelper
import com.parallax.wallpaper.weather.WeatherCondition
import com.parallax.wallpaper.weather.WeatherSyncManager
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import android.Manifest
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.collectAsState
import com.parallax.wallpaper.ui.theme.AppThemeMode
import com.parallax.wallpaper.ui.theme.ThemeManager
import com.parallax.wallpaper.ui.theme.*
import com.parallax.wallpaper.data.RemoteConfigManager
import com.parallax.wallpaper.data.api.LegalAndSupportConfigResponse
import com.parallax.wallpaper.utils.LiveWallpaperManager
import com.parallax.wallpaper.work.DailyWallpaperWorker

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val currentThemeMode by ThemeManager.themeMode.collectAsState()
    var isDailyNotificationEnabled by remember { mutableStateOf(true) }
    var sensitivity by remember { mutableFloatStateOf(LiveWallpaperManager.getSensitivity(context)) }
    var isDoubleTapEnabled by remember { mutableStateOf(LiveWallpaperManager.isDoubleTapEnabled(context)) }
    var isHapticsEnabled by remember { mutableStateOf(LiveWallpaperManager.isHapticsEnabled(context)) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTermsDialog by remember { mutableStateOf(false) }
    var showAutoChangerDialog by remember { mutableStateOf(false) }

    val remoteConfig by RemoteConfigManager.config.collectAsState()
    val legalSupport = remoteConfig?.legalAndSupport ?: LegalAndSupportConfigResponse()

    val weatherSyncManager = remember { WeatherSyncManager.getInstance(context) }
    val isWeatherOverlayEnabled by weatherSyncManager.isWeatherOverlayEnabled.collectAsState()
    val isAutoSyncEnabled by weatherSyncManager.isAutoSyncEnabled.collectAsState()
    val currentWeather by weatherSyncManager.currentWeather.collectAsState()

    // Android 13+ Notification Permission Launcher
    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            Toast.makeText(context, "✅ Daily Wallpaper Notifications Enabled!", Toast.LENGTH_SHORT).show()
            DailyWallpaperWorker.scheduleDaily(context)
        } else {
            Toast.makeText(context, "Notifications permission denied.", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "Settings",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 1. APPEARANCE & THEME SECTION (Dark / Light / AMOLED Pure Black)
            Text(
                text = "APPEARANCE & THEME",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = com.parallax.wallpaper.ui.theme.NeonCyan
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = null,
                            tint = com.parallax.wallpaper.ui.theme.NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Display Theme",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    AppThemeMode.values().forEach { mode ->
                        val isSelected = currentThemeMode == mode
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) com.parallax.wallpaper.ui.theme.NeonPurple.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surface,
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) com.parallax.wallpaper.ui.theme.NeonCyan else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable {
                                    ThemeManager.setThemeMode(context, mode)
                                    Toast.makeText(context, "${mode.displayName} Applied!", Toast.LENGTH_SHORT).show()
                                }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 14.dp, vertical = 12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = when (mode) {
                                            AppThemeMode.DARK -> Icons.Default.DarkMode
                                            AppThemeMode.LIGHT -> Icons.Default.LightMode
                                            AppThemeMode.AMOLED -> Icons.Default.DarkMode
                                        },
                                        contentDescription = null,
                                        tint = if (isSelected) com.parallax.wallpaper.ui.theme.NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = mode.displayName,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        )
                                        Text(
                                            text = mode.description,
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        )
                                    }
                                }

                                Icon(
                                    imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (isSelected) com.parallax.wallpaper.ui.theme.NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 2. DAILY WALLPAPER & PUSH NOTIFICATIONS
            Text(
                text = "DAILY WALLPAPERS & NOTIFICATIONS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = com.parallax.wallpaper.ui.theme.NeonCyan
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = null,
                                tint = com.parallax.wallpaper.ui.theme.NeonPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Daily Morning Pick",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                )
                                Text(
                                    text = "Curated 4K 3D wallpaper at 8:00 AM",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }

                        Switch(
                            checked = isDailyNotificationEnabled,
                            onCheckedChange = { checked ->
                                isDailyNotificationEnabled = checked
                                if (checked) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        DailyWallpaperWorker.scheduleDaily(context)
                                        Toast.makeText(context, "Daily Notifications Scheduled!", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = com.parallax.wallpaper.ui.theme.NeonPurple
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Button(
                        onClick = {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                            }
                            DailyWallpaperWorker.triggerImmediateNotification(context)
                            Toast.makeText(context, "✨ Test Daily Wallpaper Notification Sent!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = com.parallax.wallpaper.ui.theme.NeonPurple),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Notifications, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Send Test Notification Now", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 3. Parallax Sensor Sensitivity
            Text(
                text = "PARALLAX 3D ENGINE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = com.parallax.wallpaper.ui.theme.NeonCyan
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                tint = NeonPurple,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Sensor Sensitivity",
                                style = MaterialTheme.typography.titleMedium.copy(color = TextPrimary)
                            )
                        }
                        Text(
                            text = String.format("%.1fx", sensitivity),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Slider(
                        value = sensitivity,
                        onValueChange = {
                            sensitivity = it
                            LiveWallpaperManager.setSensitivity(context, it)
                        },
                        valueRange = 0.5f..2.5f,
                        steps = 8,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonPurple,
                            inactiveTrackColor = CardBorder
                        )
                    )

                    Text(
                        text = "Adjust tilt responsiveness for 3D multi-layer wallpapers.",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 4. ZERO-BATTERY OPTIMIZER
            Text(
                text = "ZERO-BATTERY OPTIMIZER",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF00E676)
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            val batteryOptimizer = remember { com.parallax.wallpaper.power.BatteryOptimizer.getInstance(context) }
            val isUltraBatteryEnabled by batteryOptimizer.isUltraBatterySaverEnabled.collectAsState()
            val isPowerSaveActive by batteryOptimizer.isSystemPowerSaveActive.collectAsState()

            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFF00E676).copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = "⚡", fontSize = 16.sp)
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Ultra-Power Saver Mode",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF00E676).copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF00E676).copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = if (isPowerSaveActive) "SYSTEM ECO ACTIVE" else "0% IDLE DRAIN",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = Color(0xFF00E676),
                                                fontWeight = FontWeight.ExtraBold,
                                                fontSize = 8.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Auto-pauses rendering when phone is motionless on desk. Caps at 30 FPS.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Switch(
                            checked = isUltraBatteryEnabled,
                            onCheckedChange = {
                                batteryOptimizer.setUltraBatterySaverEnabled(it)
                                Toast.makeText(
                                    context,
                                    if (it) "✅ Zero-Battery Optimizer Activated!" else "Standard 60 FPS Mode Active",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = Color(0xFF00E676)
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 5. GESTURES & HAPTIC FEEDBACK
            Text(
                text = "GESTURES & HAPTIC FEEDBACK",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeonPink
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Double-Tap to Switch Wallpaper
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = NeonPink.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.TouchApp,
                                        contentDescription = null,
                                        tint = NeonPink,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Double-Tap Wallpaper Switch",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Double-tap empty home screen space to switch to next 3D wallpaper.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Switch(
                            checked = isDoubleTapEnabled,
                            onCheckedChange = { checked ->
                                isDoubleTapEnabled = checked
                                LiveWallpaperManager.setDoubleTapEnabled(context, checked)
                                HapticHelper.click(context)
                                Toast.makeText(
                                    context,
                                    if (checked) "✅ Double-Tap Wallpaper Switch Enabled!" else "Double-Tap Switch Disabled",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonPink
                            )
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = CardBorder.copy(alpha = 0.5f))

                    // Tactile Haptic Feedback Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = NeonCyan.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Vibration,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Tactile Haptic Feedback",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Vibrations for double-tap, 3D tilt edge limits, and controls.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Switch(
                            checked = isHapticsEnabled,
                            onCheckedChange = { checked ->
                                isHapticsEnabled = checked
                                LiveWallpaperManager.setHapticsEnabled(context, checked)
                                if (checked) {
                                    HapticHelper.doubleTapSuccess(context)
                                }
                                Toast.makeText(
                                    context,
                                    if (checked) "✅ Haptic Feedback Enabled!" else "Haptic Feedback Disabled",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonCyan
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Test Haptic Button
                    Button(
                        onClick = {
                            HapticHelper.doubleTapSuccess(context)
                            Toast.makeText(context, "⚡ Haptic Pulse Fired!", Toast.LENGTH_SHORT).show()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.Vibration, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Test Dual-Pulse Haptics", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 6. DYNAMIC WEATHER ENGINE
            Text(
                text = "DYNAMIC WEATHER ENGINE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Weather Overlay Master Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = NeonCyan.copy(alpha = 0.15f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Cloud,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Live Weather Overlays",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = NeonCyan.copy(alpha = 0.15f),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
                                    ) {
                                        Text(
                                            text = "${currentWeather.emoji} ${currentWeather.displayName.uppercase()}",
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                color = NeonCyan,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp
                                            ),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "3D Rain, Snow, Clouds, and Lightning layered over wallpaper.",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Switch(
                            checked = isWeatherOverlayEnabled,
                            onCheckedChange = { checked ->
                                weatherSyncManager.setWeatherOverlayEnabled(checked)
                                HapticHelper.click(context)
                                Toast.makeText(
                                    context,
                                    if (checked) "✅ Live Weather Overlays Active!" else "Weather Overlays Disabled",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonCyan
                            )
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp), color = CardBorder.copy(alpha = 0.5f))

                    // Auto-Sync Switch
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Auto Diurnal Weather Sync",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Auto-matches conditions to current day/night sun cycle.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                            )
                        }

                        Switch(
                            checked = isAutoSyncEnabled,
                            onCheckedChange = { checked ->
                                weatherSyncManager.setAutoSyncEnabled(checked)
                                HapticHelper.click(context)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonPurple
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = "MANUAL WEATHER SCENE OVERRIDE",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextSecondary
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    @OptIn(ExperimentalLayoutApi::class)
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        WeatherCondition.entries.forEach { condition ->
                            val isSelected = currentWeather == condition
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    weatherSyncManager.setCondition(condition)
                                    HapticHelper.click(context)
                                    Toast.makeText(context, "${condition.emoji} ${condition.displayName} Active!", Toast.LENGTH_SHORT).show()
                                },
                                label = {
                                    Text(
                                        text = "${condition.emoji} ${condition.displayName}",
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) Color.White else TextSecondary
                                        )
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = SurfaceDark,
                                    selectedContainerColor = NeonPurple,
                                    labelColor = TextSecondary,
                                    selectedLabelColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = CardBorder,
                                    selectedBorderColor = NeonCyan,
                                    borderWidth = 1.dp,
                                    selectedBorderWidth = 1.5.dp,
                                    enabled = true,
                                    selected = isSelected
                                ),
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Auto Wallpaper Changer Section (Option B)
            Text(
                text = "AUTO ROTATION & SCHEDULE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.Schedule,
                        title = "Auto Wallpaper Changer",
                        subtitle = "Rotate wallpapers periodically in background",
                        onClick = { showAutoChangerDialog = true }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Community & Direct Support Hub
            Text(
                text = "COMMUNITY & DIRECT SUPPORT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.Share,
                        title = "Instagram Community",
                        subtitle = "Daily 3D wallpaper drops, teasers & reels",
                        onClick = {
                            val url = legalSupport.instagramUrl
                            if (url.isNotBlank()) {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Unable to open Instagram link", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    SettingsRowItem(
                        icon = Icons.Default.Cloud,
                        title = "Telegram VIP Channel",
                        subtitle = "Direct uncompressed 4K wallpapers broadcast",
                        onClick = {
                            val url = legalSupport.telegramUrl
                            if (url.isNotBlank()) {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                } catch (e: Exception) {
                                    Toast.makeText(context, "Unable to open Telegram link", Toast.LENGTH_SHORT).show()
                                }
                            }
                        }
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    SettingsRowItem(
                        icon = Icons.Default.TouchApp,
                        title = "WhatsApp Support Desk",
                        subtitle = "Chat directly with support for questions & requests",
                        onClick = {
                            val rawNumber = legalSupport.whatsappNumber
                            val cleanNumber = rawNumber.replace("[^0-9]".toRegex(), "")
                            val waUri = Uri.parse("https://wa.me/$cleanNumber?text=Hello%20ReWall%20Support")
                            try {
                                context.startActivity(Intent(Intent.ACTION_VIEW, waUri))
                            } catch (e: Exception) {
                                Toast.makeText(context, "WhatsApp not installed", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    SettingsRowItem(
                        icon = Icons.Default.Info,
                        title = "Official Support Email",
                        subtitle = legalSupport.supportEmail.ifBlank { "support@rewall.app" },
                        onClick = {
                            val email = legalSupport.supportEmail.ifBlank { "support@rewall.app" }
                            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                                data = Uri.parse("mailto:$email?subject=ReWall%20Wallpaper%20Inquiry")
                            }
                            try {
                                context.startActivity(Intent.createChooser(emailIntent, "Send Email Support"))
                            } catch (e: Exception) {
                                Toast.makeText(context, "No email app found", Toast.LENGTH_SHORT).show()
                            }
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Google Play Store Legal & Privacy Section
            Text(
                text = "LEGAL & COMPLIANCE",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column {
                    SettingsRowItem(
                        icon = Icons.Default.Policy,
                        title = "Privacy Policy",
                        subtitle = "Required for Google Play Console",
                        onClick = {
                            val url = legalSupport.privacyPolicyUrl
                            if (url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                } catch (e: Exception) {
                                    showPrivacyDialog = true
                                }
                            } else {
                                showPrivacyDialog = true
                            }
                        }
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    SettingsRowItem(
                        icon = Icons.Default.Security,
                        title = "Terms of Service",
                        subtitle = "Usage terms and conditions",
                        onClick = {
                            val url = legalSupport.termsUrl
                            if (url.isNotBlank() && (url.startsWith("http://") || url.startsWith("https://"))) {
                                try {
                                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                                } catch (e: Exception) {
                                    showTermsDialog = true
                                }
                            } else {
                                showTermsDialog = true
                            }
                        }
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    SettingsRowItem(
                        icon = Icons.Default.Star,
                        title = "Rate on Google Play",
                        subtitle = "Leave your feedback on the Play Store",
                        onClick = {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("market://details?id=${context.packageName}")
                            )
                            try {
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                val webIntent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}")
                                )
                                context.startActivity(webIntent)
                            }
                        }
                    )
                    HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                    SettingsRowItem(
                        icon = Icons.Default.Share,
                        title = "Share App",
                        subtitle = "Share with friends & family",
                        onClick = {
                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "Download Parallax 3D Wallpaper for stunning 3D tilt effects: https://play.google.com/store/apps/details?id=${context.packageName}"
                                )
                            }
                            context.startActivity(Intent.createChooser(shareIntent, "Share App"))
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // App Information
            Text(
                text = "ABOUT",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                colors = CardDefaults.cardColors(containerColor = CardDark),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Parallax 3D Wallpaper",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Version: 1.0.0 (Release)\nPackage: com.parallax.wallpaper\nTarget SDK: 35 (Android 15 Ready)\nBuilt with 100% Kotlin & Jetpack Compose",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }

            Spacer(modifier = Modifier.height(40.dp))
        }

        // Privacy Policy Dialog
        if (showPrivacyDialog) {
            AlertDialog(
                onDismissRequest = { showPrivacyDialog = false },
                containerColor = SurfaceDark,
                title = {
                    Text(
                        text = "Privacy Policy",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                },
                text = {
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        Text(
                            text = "Parallax 3D Wallpaper App respects your privacy.\n\n" +
                                    "1. Data Collection:\n" +
                                    "This app does not collect, store, or transmit any personal identifiable information (PII). All sensor data (accelerometer and gyroscope) is processed strictly locally on your device in real-time to compute the 3D parallax rendering effect and is never saved or transmitted.\n\n" +
                                    "2. Wallpaper Downloads:\n" +
                                    "When you save a wallpaper, the app uses Android's modern MediaStore API to store images in your Pictures/ParallaxWallpapers folder. No sensitive file permissions are requested.\n\n" +
                                    "3. Third-party Services:\n" +
                                    "Wallpapers and thumbnails are fetched from secure CDN image services.\n\n" +
                                    "4. Contact:\n" +
                                    "If you have any questions regarding this Privacy Policy, please contact our support email.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showPrivacyDialog = false }) {
                        Text("Close", color = NeonCyan)
                    }
                }
            )
        }

        // Terms of Service Dialog
        if (showTermsDialog) {
            AlertDialog(
                onDismissRequest = { showTermsDialog = false },
                containerColor = SurfaceDark,
                title = {
                    Text(
                        text = "Terms of Service",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                },
                text = {
                    Text(
                        text = "By downloading and using Parallax 3D Wallpaper, you agree that the wallpapers provided are for personal personalization use only. Redistribution or commercial resale of the wallpaper assets is prohibited.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showTermsDialog = false }) {
                        Text("I Agree", color = NeonCyan)
                    }
                }
            )
        }

        // Auto Wallpaper Changer Dialog
        if (showAutoChangerDialog) {
            com.parallax.wallpaper.ui.components.AutoWallpaperChangerDialog(
                onDismiss = { showAutoChangerDialog = false }
            )
        }
    }
}

@Composable
private fun SettingsRowItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = DeepObsidian,
            modifier = Modifier.size(38.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier
                    .padding(8.dp)
                    .size(20.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
            )
        }
    }
}
