package com.parallax.wallpaper.island

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.wallpaper.data.api.DynamicIslandThemeItem

/**
 * Spring-morphing Dynamic Island Capsule Jetpack Compose Component.
 */
@Composable
fun DynamicIslandCapsule(
    theme: DynamicIslandThemeItem,
    event: IslandEvent,
    modifier: Modifier = Modifier,
    onCapsuleClick: () -> Unit = {}
) {
    val isCompact = event is IslandEvent.Compact

    // Target sizes with spring animation
    val targetWidth = when (event) {
        is IslandEvent.Compact -> 110.dp
        is IslandEvent.Charging -> 225.dp
        is IslandEvent.Music -> 245.dp
        is IslandEvent.Earbuds -> 230.dp
        is IslandEvent.IncomingCall -> 240.dp
        is IslandEvent.NotificationAlert -> 245.dp
    }

    val targetHeight = when (event) {
        is IslandEvent.Compact -> 32.dp
        is IslandEvent.Charging -> 56.dp
        is IslandEvent.Music -> 72.dp
        is IslandEvent.Earbuds -> 56.dp
        is IslandEvent.IncomingCall -> 66.dp
        is IslandEvent.NotificationAlert -> 60.dp
    }

    val targetCornerRadius = when (event) {
        is IslandEvent.Compact -> 18.dp
        else -> 24.dp
    }

    val animatedWidth by animateDpAsState(
        targetValue = targetWidth,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "islandWidth"
    )

    val animatedHeight by animateDpAsState(
        targetValue = targetHeight,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "islandHeight"
    )

    val animatedCornerRadius by animateDpAsState(
        targetValue = targetCornerRadius,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "islandRadius"
    )

    val accentColor = remember(theme.accentColor) { parseHexColor(theme.accentColor, Color(0xFF00E5FF)) }
    val bgColor = remember(theme.backgroundColor) { parseHexColor(theme.backgroundColor, Color(0xFF0B0E14)) }
    val textColor = remember(theme.textColor) { parseHexColor(theme.textColor, Color.White) }
    val glowColor = remember(theme.glowColor) { parseHexColor(theme.glowColor, accentColor.copy(alpha = 0.4f)) }

    Box(
        modifier = modifier
            .width(animatedWidth)
            .height(animatedHeight)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(animatedCornerRadius),
                ambientColor = glowColor,
                spotColor = accentColor
            )
            .clip(RoundedCornerShape(animatedCornerRadius))
            .background(bgColor)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.15f),
                shape = RoundedCornerShape(animatedCornerRadius)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onCapsuleClick()
            }
            .padding(horizontal = if (isCompact) 8.dp else 12.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = event,
            transitionSpec = {
                fadeIn(animationSpec = tween(220, delayMillis = 60)) togetherWith
                        fadeOut(animationSpec = tween(120))
            },
            label = "islandContentAnim"
        ) { targetEvent ->
            when (targetEvent) {
                is IslandEvent.Compact -> {
                    CompactIslandContent(accentColor = accentColor)
                }
                is IslandEvent.Charging -> {
                    ChargingIslandContent(
                        event = targetEvent,
                        accentColor = accentColor,
                        textColor = textColor
                    )
                }
                is IslandEvent.Music -> {
                    MusicIslandContent(
                        event = targetEvent,
                        accentColor = accentColor,
                        textColor = textColor
                    )
                }
                is IslandEvent.Earbuds -> {
                    EarbudsIslandContent(
                        event = targetEvent,
                        accentColor = accentColor,
                        textColor = textColor
                    )
                }
                is IslandEvent.IncomingCall -> {
                    CallIslandContent(
                        event = targetEvent,
                        accentColor = accentColor,
                        textColor = textColor
                    )
                }
                is IslandEvent.NotificationAlert -> {
                    NotificationIslandContent(
                        event = targetEvent,
                        accentColor = accentColor,
                        textColor = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun CompactIslandContent(accentColor: Color) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "⚡",
                fontSize = 11.sp,
                color = accentColor
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "88%",
                color = accentColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Monospace
            )
        }

        EqualizerWaveBars(
            accentColor = accentColor,
            barCount = 3,
            maxHeight = 12.dp
        )
    }
}

@Composable
private fun ChargingIslandContent(
    event: IslandEvent.Charging,
    accentColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.18f))
                    .border(1.dp, accentColor.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⚡",
                    fontSize = 17.sp,
                    color = accentColor
                )
            }

            Column {
                Text(
                    text = event.wattage,
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (event.isFastCharge) "Fast Charging Active" else "Cable Connected",
                    color = accentColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Mini battery gauge progress
            Box(
                modifier = Modifier
                    .width(42.dp)
                    .height(7.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Color.White.copy(alpha = 0.15f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(event.batteryPercent / 100f)
                        .clip(RoundedCornerShape(4.dp))
                        .background(accentColor)
                )
            }

            Text(
                text = "${event.batteryPercent}%",
                color = accentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}

@Composable
private fun MusicIslandContent(
    event: IslandEvent.Music,
    accentColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.linearGradient(
                            listOf(Color(0xFF311042), Color(0xFF6B21A8))
                        )
                    )
                    .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(8.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🎵",
                    fontSize = 18.sp
                )
            }

            Column {
                Text(
                    text = event.trackTitle,
                    color = textColor,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = event.artistName,
                    color = Color.White.copy(alpha = 0.65f),
                    fontSize = 10.5.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            EqualizerWaveBars(
                accentColor = accentColor,
                barCount = 4,
                maxHeight = 16.dp
            )

            Text(
                text = if (event.isPlaying) "⏸" else "▶",
                fontSize = 15.sp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun EarbudsIslandContent(
    event: IslandEvent.Earbuds,
    accentColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "🎧",
                fontSize = 20.sp
            )

            Column {
                Text(
                    text = event.deviceName,
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Connected • Bluetooth 5.3",
                    color = accentColor,
                    fontSize = 10.sp
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            BatteryPill(label = "L", percent = event.leftBattery, accentColor = accentColor)
            BatteryPill(label = "R", percent = event.rightBattery, accentColor = accentColor)
        }
    }
}

@Composable
private fun BatteryPill(label: String, percent: Int, accentColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(Color.White.copy(alpha = 0.1f))
            .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
        .padding(horizontal = 5.dp, vertical = 2.dp)
    ) {
        Text(
            text = "$label: $percent%",
            fontSize = 9.sp,
            color = Color.White,
            fontFamily = FontFamily.Monospace,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun CallIslandContent(
    event: IslandEvent.IncomingCall,
    accentColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2563EB)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "👤",
                    fontSize = 15.sp,
                    color = Color.White
                )
            }

            Column {
                Text(
                    text = event.callerName,
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Text(
                    text = event.callerLabel,
                    color = Color(0xFF22C55E),
                    fontSize = 10.sp
                )
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            // Decline (Red)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "✕", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
            // Accept (Green)
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF22C55E)),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "📞", fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun NotificationIslandContent(
    event: IslandEvent.NotificationAlert,
    accentColor: Color,
    textColor: Color
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color(0xFF25D366)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "💬", fontSize = 14.sp)
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.sender,
                color = textColor,
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = event.message,
                color = Color.White.copy(alpha = 0.75f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun EqualizerWaveBars(
    accentColor: Color,
    barCount: Int = 4,
    maxHeight: androidx.compose.ui.unit.Dp = 14.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "eqAnim")

    val h1 by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(450, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq1"
    )

    val h2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq2"
    )

    val h3 by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(520, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq3"
    )

    val h4 by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "eq4"
    )

    val heights = listOf(h1, h2, h3, h4)

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.height(maxHeight)
    ) {
        for (i in 0 until barCount) {
            val h = heights[i % heights.size]
            Box(
                modifier = Modifier
                    .width(2.5.dp)
                    .height(maxHeight * h)
                    .clip(RoundedCornerShape(2.dp))
                    .background(accentColor)
            )
        }
    }
}

private fun parseHexColor(hexString: String?, fallback: Color): Color {
    if (hexString.isNullOrBlank()) return fallback
    return try {
        val clean = hexString.trim()
        if (clean.startsWith("#")) {
            val colorLong = clean.substring(1).toLong(16)
            if (clean.length == 7) {
                Color(0xFF000000 or colorLong)
            } else {
                Color(colorLong)
            }
        } else {
            fallback
        }
    } catch (_: Exception) {
        fallback
    }
}
