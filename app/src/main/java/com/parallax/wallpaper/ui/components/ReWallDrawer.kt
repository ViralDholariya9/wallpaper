package com.parallax.wallpaper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.PhoneInTalk
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.SaveAlt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material.icons.filled.Waves
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.wallpaper.ui.theme.CardBorder
import com.parallax.wallpaper.ui.theme.DeepObsidian
import com.parallax.wallpaper.ui.theme.NeonCyan
import com.parallax.wallpaper.ui.theme.NeonPink
import com.parallax.wallpaper.ui.theme.NeonPurple
import com.parallax.wallpaper.ui.theme.SurfaceDark
import com.parallax.wallpaper.ui.theme.TextPrimary
import com.parallax.wallpaper.ui.theme.TextSecondary

enum class DrawerItem {
    ALL_WALLPAPERS,
    PARALLAX_3D,
    CATEGORIES_EXPLORER,
    CALL_SCREEN,
    ALWAYS_ON_DISPLAY,
    DYNAMIC_ISLAND,
    EDGE_LIGHTING,
    CHARGING_ANIMATIONS,
    CUSTOM_3D_MAKER,
    AI_STUDIO,
    WEATHER_SYNC,
    MUSIC_VISUALIZER,
    RINGTONES,
    STATUS_SAVER,
    DOUBLE_WALLPAPER,
    TOUCH_EFFECTS,
    FINGERPRINT_ANIMATIONS,
    WALLPAPER_CHANGER,
    FAVORITES,
    DOWNLOADS,
    REMOVE_ADS,
    RATE_US,
    SHARE_APP,
    PRIVACY_POLICY
}

@Composable
fun ReWallDrawer(
    onItemSelected: (DrawerItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val remoteConfig by com.parallax.wallpaper.data.RemoteConfigManager.config.collectAsState()
    val features = remoteConfig?.features ?: com.parallax.wallpaper.data.api.FeatureFlagsResponse()

    ModalDrawerSheet(
        modifier = modifier
            .fillMaxHeight()
            .width(310.dp),
        drawerContainerColor = SurfaceDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .verticalScroll(rememberScrollState())
        ) {
            // ReWall Brand Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(
                                NeonPurple.copy(alpha = 0.35f),
                                Color.Transparent
                            )
                        )
                    )
                    .padding(horizontal = 24.dp, vertical = 28.dp)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = NeonPurple,
                            modifier = Modifier.size(42.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.AutoAwesome,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier
                                    .padding(8.dp)
                                    .size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "ReWall 3D",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    letterSpacing = 1.sp,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Version 1.2.0 • Ultra 4K",
                                style = MaterialTheme.typography.bodySmall.copy(color = NeonCyan)
                            )
                        }
                    }
                }
            }

            HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))

            Spacer(modifier = Modifier.height(6.dp))

            // ==========================================
            // SECTION 1: WALLPAPERS & CATEGORIES
            // ==========================================
            DrawerSectionHeader(title = "Wallpapers & Categories", icon = "🏷️")

            DrawerRow(
                icon = Icons.Default.Wallpaper,
                title = "All Wallpapers",
                onClick = { onItemSelected(DrawerItem.ALL_WALLPAPERS) }
            )

            DrawerRow(
                icon = Icons.Default.AutoAwesome,
                title = "3D Parallax",
                iconTint = NeonCyan,
                badge = "HOT",
                onClick = { onItemSelected(DrawerItem.PARALLAX_3D) }
            )

            DrawerRow(
                icon = Icons.Default.Category,
                title = "Browse All Categories",
                iconTint = NeonPurple,
                badge = "NEW",
                onClick = { onItemSelected(DrawerItem.CATEGORIES_EXPLORER) }
            )

            if (features.doubleWallpaper) {
                DrawerRow(
                    icon = Icons.Default.Layers,
                    title = "Duo Wallpapers",
                    iconTint = Color(0xFF00E5FF),
                    badge = "PAIR",
                    onClick = { onItemSelected(DrawerItem.DOUBLE_WALLPAPER) }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = CardBorder.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.height(6.dp))

            // ==========================================
            // SECTION 2: SCREEN FX & BIOMETRICS
            // ==========================================
            DrawerSectionHeader(title = "Screen FX & Biometrics", icon = "✨")

            if (features.fingerprintAnimations) {
                DrawerRow(
                    icon = Icons.Default.Fingerprint,
                    title = "In-Display Fingerprint FX",
                    iconTint = NeonCyan,
                    badge = "VIP",
                    onClick = { onItemSelected(DrawerItem.FINGERPRINT_ANIMATIONS) }
                )
            }

            if (features.dynamicIsland) {
                DrawerRow(
                    icon = Icons.Default.AutoAwesome,
                    title = "Dynamic Island Capsule",
                    iconTint = NeonCyan,
                    badge = "HOT",
                    onClick = { onItemSelected(DrawerItem.DYNAMIC_ISLAND) }
                )
            }

            if (features.alwaysOnDisplay) {
                DrawerRow(
                    icon = Icons.Default.Schedule,
                    title = "Always-On Display (AOD)",
                    iconTint = Color(0xFFFFD700),
                    badge = "AMOLED",
                    onClick = { onItemSelected(DrawerItem.ALWAYS_ON_DISPLAY) }
                )
            }

            if (features.edgeLighting) {
                DrawerRow(
                    icon = Icons.Default.AutoAwesome,
                    title = "Edge Lighting & Borders",
                    iconTint = NeonPink,
                    badge = "HOT",
                    onClick = { onItemSelected(DrawerItem.EDGE_LIGHTING) }
                )
            }

            if (features.callScreen) {
                DrawerRow(
                    icon = Icons.Default.PhoneInTalk,
                    title = "Color Call Screen",
                    iconTint = Color(0xFF00E5FF),
                    badge = "4K",
                    onClick = { onItemSelected(DrawerItem.CALL_SCREEN) }
                )
            }

            if (features.touchEffects) {
                DrawerRow(
                    icon = Icons.Default.Waves,
                    title = "Touch Fluid & Ripples",
                    iconTint = NeonCyan,
                    badge = "4D",
                    onClick = { onItemSelected(DrawerItem.TOUCH_EFFECTS) }
                )
            }

            if (features.chargingAnimations) {
                DrawerRow(
                    icon = Icons.Default.Bolt,
                    title = "Charging Animations",
                    iconTint = NeonCyan,
                    badge = "NEW",
                    onClick = { onItemSelected(DrawerItem.CHARGING_ANIMATIONS) }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = CardBorder.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.height(6.dp))

            // ==========================================
            // SECTION 3: CREATIVE LAB & TOOLS
            // ==========================================
            DrawerSectionHeader(title = "Creative Lab & Tools", icon = "🎨")

            if (features.aiStudio) {
                DrawerRow(
                    icon = Icons.Default.AutoAwesome,
                    title = "AI Wallpaper Studio",
                    iconTint = NeonPurple,
                    badge = "GEMINI",
                    onClick = { onItemSelected(DrawerItem.AI_STUDIO) }
                )
            }

            if (features.custom3dMaker) {
                DrawerRow(
                    icon = Icons.Default.Layers,
                    title = "DIY 3D Parallax Maker",
                    iconTint = NeonCyan,
                    badge = "NEW",
                    onClick = { onItemSelected(DrawerItem.CUSTOM_3D_MAKER) }
                )
            }

            if (features.wallpaperChanger) {
                DrawerRow(
                    icon = Icons.Default.Schedule,
                    title = "Wallpaper Auto-Changer",
                    onClick = { onItemSelected(DrawerItem.WALLPAPER_CHANGER) }
                )
            }

            if (features.weatherSync) {
                DrawerRow(
                    icon = Icons.Default.Cloud,
                    title = "Weather Live Sync",
                    iconTint = NeonCyan,
                    badge = "LIVE",
                    onClick = { onItemSelected(DrawerItem.WEATHER_SYNC) }
                )
            }

            if (features.musicVisualizer) {
                DrawerRow(
                    icon = Icons.Default.GraphicEq,
                    title = "Music Beat Visualizer",
                    iconTint = NeonPink,
                    badge = "4D",
                    onClick = { onItemSelected(DrawerItem.MUSIC_VISUALIZER) }
                )
            }

            if (features.ringtones) {
                DrawerRow(
                    icon = Icons.Default.GraphicEq,
                    title = "Ringtones & Sounds",
                    iconTint = NeonCyan,
                    onClick = { onItemSelected(DrawerItem.RINGTONES) }
                )
            }

            if (features.statusSaver) {
                DrawerRow(
                    icon = Icons.Default.SaveAlt,
                    title = "Status Saver",
                    iconTint = NeonPurple,
                    badge = "NEW",
                    onClick = { onItemSelected(DrawerItem.STATUS_SAVER) }
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = CardBorder.copy(alpha = 0.35f))
            Spacer(modifier = Modifier.height(6.dp))

            // ==========================================
            // SECTION 4: PREFERENCES & VIP
            // ==========================================
            DrawerSectionHeader(title = "Preferences & App", icon = "💎")

            DrawerRow(
                icon = Icons.Default.Favorite,
                title = "Favourites",
                iconTint = NeonPink,
                onClick = { onItemSelected(DrawerItem.FAVORITES) }
            )

            DrawerRow(
                icon = Icons.Default.Download,
                title = "Downloads",
                onClick = { onItemSelected(DrawerItem.DOWNLOADS) }
            )

            DrawerRow(
                icon = Icons.Default.Block,
                title = "Remove Ads",
                iconTint = Color(0xFFFFD700),
                badge = "PRO",
                onClick = { onItemSelected(DrawerItem.REMOVE_ADS) }
            )

            DrawerRow(
                icon = Icons.Default.Star,
                title = "Rate Us",
                onClick = { onItemSelected(DrawerItem.RATE_US) }
            )

            DrawerRow(
                icon = Icons.Default.Share,
                title = "Share ReWall",
                onClick = { onItemSelected(DrawerItem.SHARE_APP) }
            )

            DrawerRow(
                icon = Icons.Default.Policy,
                title = "Privacy Policy",
                onClick = { onItemSelected(DrawerItem.PRIVACY_POLICY) }
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun DrawerSectionHeader(title: String, icon: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 6.dp)
    ) {
        Text(text = icon, fontSize = 12.sp)
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.2.sp,
                color = NeonCyan.copy(alpha = 0.8f)
            )
        )
    }
}

@Composable
private fun DrawerRow(
    icon: ImageVector,
    title: String,
    iconTint: Color = TextSecondary,
    badge: String? = null,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 24.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = title,
            tint = iconTint,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = TextPrimary
            ),
            modifier = Modifier.weight(1f)
        )
        badge?.let {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = if (it == "PRO") Color(0xFFFFD700).copy(alpha = 0.2f) else NeonCyan.copy(alpha = 0.2f)
            ) {
                Text(
                    text = it,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.Bold,
                        color = if (it == "PRO") Color(0xFFFFD700) else NeonCyan
                    )
                )
            }
        }
    }
}
