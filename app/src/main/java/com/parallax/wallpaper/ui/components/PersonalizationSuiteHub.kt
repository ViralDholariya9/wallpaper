package com.parallax.wallpaper.ui.components

import androidx.compose.animation.AnimatedContent
import com.parallax.wallpaper.data.api.PersonalizationSuiteItemResponse
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewCarousel
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.wallpaper.ui.theme.CardBorder
import com.parallax.wallpaper.ui.theme.CardDark
import com.parallax.wallpaper.ui.theme.NeonCyan
import com.parallax.wallpaper.ui.theme.NeonPink
import com.parallax.wallpaper.ui.theme.NeonPurple
import com.parallax.wallpaper.ui.theme.TextPrimary
import com.parallax.wallpaper.ui.theme.TextSecondary

/**
 * Data model representing each of the 8 flagship personalization modules.
 */
data class FlagshipFeature(
    val id: String,
    val title: String,
    val gujaratiTitle: String,
    val subtitle: String,
    val badge: String,
    val badgeColor: Color,
    val iconEmoji: String,
    val gradientColors: List<Color>,
    val actionText: String = "OPEN",
    val onClick: () -> Unit
)

/**
 * Helper to construct the flagship personalization features list with live OTA Admin Panel overrides.
 */
@Composable
fun rememberFlagshipFeatures(
    remoteSuiteConfig: List<PersonalizationSuiteItemResponse>? = null,
    onOpenAiStudio: () -> Unit,
    onOpenEdgeLighting: () -> Unit,
    onOpenDynamicIsland: () -> Unit,
    onOpenAod: () -> Unit,
    onOpenCallScreen: () -> Unit,
    onOpenDuoWallpapers: () -> Unit,
    onOpenTouchEffects: () -> Unit,
    onOpenFingerprint: () -> Unit
): List<FlagshipFeature> {
    return remember(
        remoteSuiteConfig,
        onOpenAiStudio,
        onOpenEdgeLighting,
        onOpenDynamicIsland,
        onOpenAod,
        onOpenCallScreen,
        onOpenDuoWallpapers,
        onOpenTouchEffects,
        onOpenFingerprint
    ) {
        val baseFeatures = listOf(
            FlagshipFeature(
                id = "ai_studio",
                title = "AI Studio",
                gujaratiTitle = "AI સ્ટુડિયો",
                subtitle = "Prompt to 4K 3D Art",
                badge = "GEMINI AI",
                badgeColor = NeonCyan,
                iconEmoji = "🎨",
                gradientColors = listOf(Color(0xFF7928CA), Color(0xFFFF007F)),
                actionText = "CREATE",
                onClick = onOpenAiStudio
            ),
            FlagshipFeature(
                id = "edge_lighting",
                title = "Edge Lighting",
                gujaratiTitle = "એજ લાઈટિંગ",
                subtitle = "RGB Screen & Notch Glow",
                badge = "RGB NEON",
                badgeColor = NeonPink,
                iconEmoji = "⚡",
                gradientColors = listOf(Color(0xFFFF007F), Color(0xFF00E5FF)),
                actionText = "GLOW",
                onClick = onOpenEdgeLighting
            ),
            FlagshipFeature(
                id = "dynamic_island",
                title = "Dynamic Island",
                gujaratiTitle = "ડાયનેમિક આઈલેન્ડ",
                subtitle = "Camera Notch Smart Capsule",
                badge = "SMART HUD",
                badgeColor = NeonCyan,
                iconEmoji = "🏝️",
                gradientColors = listOf(Color(0xFF00E5FF), Color(0xFF4A00E0)),
                actionText = "SETUP",
                onClick = onOpenDynamicIsland
            ),
            FlagshipFeature(
                id = "aod",
                title = "Always-On Display",
                gujaratiTitle = "AOD ક્લોક્સ",
                subtitle = "AMOLED Clocks & Battery HUD",
                badge = "AMOLED",
                badgeColor = Color(0xFFFFD700),
                iconEmoji = "🕒",
                gradientColors = listOf(Color(0xFFFFB300), Color(0xFF1E293B)),
                actionText = "CLOCKS",
                onClick = onOpenAod
            ),
            FlagshipFeature(
                id = "call_screen",
                title = "Color Call Screen",
                gujaratiTitle = "કલર કોલ સ્ક્રીન",
                subtitle = "3D Video Themes & Flash",
                badge = "3D CALL",
                badgeColor = Color(0xFF00E676),
                iconEmoji = "📞",
                gradientColors = listOf(Color(0xFF00E676), Color(0xFF00B0FF)),
                actionText = "THEMES",
                onClick = onOpenCallScreen
            ),
            FlagshipFeature(
                id = "duo_wallpapers",
                title = "Duo Wallpapers",
                gujaratiTitle = "ડ્યૂઓ જોડી",
                subtitle = "Matching Lock & Home Pairs",
                badge = "MAGIC PAIR",
                badgeColor = Color(0xFFFF6584),
                iconEmoji = "👥",
                gradientColors = listOf(Color(0xFFFF6B6B), Color(0xFF7928CA)),
                actionText = "PAIRS",
                onClick = onOpenDuoWallpapers
            ),
            FlagshipFeature(
                id = "touch_fluid",
                title = "Touch Fluid FX",
                gujaratiTitle = "ટચ ફ્લુઈડ",
                subtitle = "Water Ripples & Smoke Swirls",
                badge = "4D LIVE",
                badgeColor = Color(0xFF00F2FE),
                iconEmoji = "👆",
                gradientColors = listOf(Color(0xFF00F2FE), Color(0xFF0B1728)),
                actionText = "FEEL FX",
                onClick = onOpenTouchEffects
            ),
            FlagshipFeature(
                id = "fingerprint_fx",
                title = "Fingerprint FX",
                gujaratiTitle = "ફિંગરપ્રિન્ટ FX",
                subtitle = "Biometric HUD & Supernova",
                badge = "BIOMETRIC",
                badgeColor = Color(0xFF9D4EDD),
                iconEmoji = "🔓",
                gradientColors = listOf(Color(0xFF9D4EDD), Color(0xFF00E5FF)),
                actionText = "SCAN FX",
                onClick = onOpenFingerprint
            )
        )

        if (remoteSuiteConfig.isNullOrEmpty()) {
            baseFeatures
        } else {
            val baseMap = baseFeatures.associateBy { it.id }
            val sortedConfigs = remoteSuiteConfig.sortedBy { it.order }
            val resolvedList = mutableListOf<FlagshipFeature>()

            for (cfg in sortedConfigs) {
                if (!cfg.enabled) continue
                val base = baseMap[cfg.id] ?: continue
                resolvedList.add(
                    base.copy(
                        title = cfg.title.ifBlank { base.title },
                        gujaratiTitle = cfg.gujaratiTitle?.ifBlank { null } ?: base.gujaratiTitle,
                        subtitle = cfg.subtitle ?: base.subtitle,
                        badge = cfg.badge ?: base.badge,
                        iconEmoji = cfg.iconEmoji?.ifBlank { null } ?: base.iconEmoji
                    )
                )
            }
            resolvedList
        }
    }
}

/**
 * World-class Personalization Suite Hub for the Home Screen.
 * Showcases the flagship customization modules in an ultra-sleek,
 * professional horizontal carousel or expandable bento grid.
 */
@Composable
fun PersonalizationSuiteHub(
    features: List<FlagshipFeature>,
    modifier: Modifier = Modifier
) {
    if (features.isEmpty()) return

    var isGridView by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize(animationSpec = tween(300))
    ) {
        // --- Section Header Bar ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(
                            Brush.linearGradient(
                                listOf(NeonCyan.copy(alpha = 0.25f), NeonPurple.copy(alpha = 0.25f))
                            )
                        )
                        .border(
                            1.dp,
                            Brush.linearGradient(listOf(NeonCyan.copy(alpha = 0.6f), NeonPink.copy(alpha = 0.4f))),
                            RoundedCornerShape(10.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "⚡", fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Personalization Suite",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary,
                                letterSpacing = 0.2.sp
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = NeonPurple.copy(alpha = 0.25f),
                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
                        ) {
                            Text(
                                text = "${features.size} PRO TOOLS",
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeonCyan,
                                    fontSize = 8.5.sp
                                )
                            )
                        }
                    }
                    Text(
                        text = "Customize notch, edges, AOD, calls & touch physics",
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            // View Mode Switcher: Carousel (⇄) vs Bento Grid (⊞)
            Surface(
                shape = RoundedCornerShape(10.dp),
                color = CardDark,
                border = BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { isGridView = !isGridView }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.ViewCarousel else Icons.Default.GridView,
                        contentDescription = "Toggle view",
                        tint = NeonCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Text(
                        text = if (isGridView) "Scroll" else "Grid",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan,
                            fontSize = 10.5.sp
                        )
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // --- Content Area: Carousel vs Bento Grid ---
        AnimatedContent(
            targetState = isGridView,
            transitionSpec = {
                fadeIn(animationSpec = tween(220)) togetherWith fadeOut(animationSpec = tween(150))
            },
            label = "PersonalizationSuiteHubView"
        ) { gridActive ->
            if (!gridActive) {
                // Horizontal Showcase Carousel (Default, sleek & non-intrusive)
                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(features, key = { it.id }) { feature ->
                        FlagshipFeatureCarouselCard(
                            feature = feature,
                            onClick = feature.onClick
                        )
                    }
                }
            } else {
                // 4x2 Compact Bento Grid (Expanded Matrix View)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    features.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            pair.forEach { feature ->
                                FlagshipFeatureGridCard(
                                    feature = feature,
                                    onClick = feature.onClick,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * High-end Glassmorphic Carousel Card for an individual flagship feature.
 */
@Composable
fun FlagshipFeatureCarouselCard(
    feature: FlagshipFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = feature.gradientColors.first()
    val secondaryColor = feature.gradientColors.last()

    Card(
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(
            1.2.dp,
            Brush.linearGradient(
                listOf(
                    primaryColor.copy(alpha = 0.65f),
                    secondaryColor.copy(alpha = 0.25f)
                )
            )
        ),
        modifier = modifier
            .width(150.dp)
            .height(168.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(
                            primaryColor.copy(alpha = 0.18f),
                            Color.Transparent,
                            secondaryColor.copy(alpha = 0.08f)
                        )
                    )
                )
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Row: Icon + Badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(11.dp))
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        primaryColor.copy(alpha = 0.45f),
                                        Color.Black.copy(alpha = 0.3f)
                                    )
                                )
                            )
                            .border(
                                1.dp,
                                primaryColor.copy(alpha = 0.5f),
                                RoundedCornerShape(11.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = feature.iconEmoji, fontSize = 18.sp)
                    }

                    if (feature.badge.isNotBlank()) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = feature.badgeColor.copy(alpha = 0.18f),
                            border = BorderStroke(0.8.dp, feature.badgeColor.copy(alpha = 0.45f))
                        ) {
                            Text(
                                text = feature.badge,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = feature.badgeColor,
                                    fontSize = 8.sp,
                                    letterSpacing = 0.4.sp
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Middle: Title and Subtitle
                Column {
                    Text(
                        text = feature.title,
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 13.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = feature.subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary,
                            fontSize = 10.sp,
                            lineHeight = 13.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom: Action Micro-Pill
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = primaryColor.copy(alpha = 0.2f),
                        border = BorderStroke(0.8.dp, primaryColor.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) {
                            Text(
                                text = feature.actionText,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = primaryColor,
                                    fontSize = 8.5.sp,
                                    letterSpacing = 0.4.sp
                                )
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = null,
                                tint = primaryColor,
                                modifier = Modifier.size(9.dp)
                            )
                        }
                    }

                    // Subtle Gujarati indicator dot / text
                    Text(
                        text = feature.gujaratiTitle,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = TextSecondary.copy(alpha = 0.6f),
                            fontSize = 8.5.sp
                        ),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Compact Bento Grid Card for an individual flagship feature (used when grid mode is active).
 */
@Composable
fun FlagshipFeatureGridCard(
    feature: FlagshipFeature,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val primaryColor = feature.gradientColors.first()

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, primaryColor.copy(alpha = 0.35f)),
        modifier = modifier
            .height(68.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(
                            primaryColor.copy(alpha = 0.15f),
                            Color.Transparent
                        )
                    )
                )
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(primaryColor.copy(alpha = 0.25f))
                    .border(1.dp, primaryColor.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = feature.iconEmoji, fontSize = 18.sp)
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = feature.title,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 12.5.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (feature.badge.isNotBlank()) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Surface(
                            shape = RoundedCornerShape(3.dp),
                            color = feature.badgeColor.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = feature.badge,
                                modifier = Modifier.padding(horizontal = 3.dp, vertical = 1.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = feature.badgeColor,
                                    fontSize = 7.sp
                                )
                            )
                        }
                    }
                }
                Text(
                    text = feature.subtitle,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = TextSecondary,
                        fontSize = 9.5.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                tint = primaryColor,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
