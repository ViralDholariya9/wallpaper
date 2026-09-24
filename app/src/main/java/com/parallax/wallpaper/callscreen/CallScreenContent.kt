package com.parallax.wallpaper.callscreen

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Main incoming call screen composable.
 */
@Composable
fun CallScreenContent(
    theme: CallScreenTheme,
    callerProfile: CallerProfile = CallerProfile(),
    buttonStyle: CallButtonStyle = theme.buttonStyle,
    isRinging: Boolean = true,
    onAcceptCall: () -> Unit = {},
    onDeclineCall: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val accentColor = try {
        Color(android.graphics.Color.parseColor(theme.accentColorHex))
    } catch (_: Exception) {
        Color(0xFF00E5FF)
    }

    val glowColor = try {
        Color(android.graphics.Color.parseColor(theme.glowColorHex))
    } catch (_: Exception) {
        Color(0xFF7000FF)
    }

    // Concentric Wave Animation
    val infiniteTransition = rememberInfiniteTransition(label = "callWaves")
    val waveScale1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )
    val waveAlpha1 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveAlpha1"
    )

    val waveScale2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.6f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2"
    )
    val waveAlpha2 by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveAlpha2"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF050014),
                        Color(0xFF0A051E),
                        Color(0xFF020008)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // Theme Background Graphic (SVG / WebP / Image)
        val bgUrl = remember(theme.backgroundUrl, theme.previewUrl) {
            CallScreenManager.resolveUrl(theme.backgroundUrl.ifBlank { theme.previewUrl })
        }
        if (bgUrl.isNotBlank()) {
            AsyncImage(
                model = bgUrl,
                contentDescription = theme.title,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            // Translucent gradient scrim for high contrast readability
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.5f),
                                Color.Black.copy(alpha = 0.25f),
                                Color.Black.copy(alpha = 0.75f)
                            )
                        )
                    )
            )
        }

        // Ambient background glow
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    listOf(glowColor.copy(alpha = 0.22f), Color.Transparent),
                    center = center,
                    radius = size.minDimension * 0.6f
                )
            )
        }

        // Overlay Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Section: Ringing Ripple & Caller Avatar
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))

                Box(
                    modifier = Modifier.size(170.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (isRinging) {
                        // Animated Ripple Ring 1
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .scale(waveScale1)
                                .border(2.dp, accentColor.copy(alpha = waveAlpha1), CircleShape)
                        )
                        // Animated Ripple Ring 2
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .scale(waveScale2)
                                .border(1.5.dp, glowColor.copy(alpha = waveAlpha2), CircleShape)
                        )
                    }

                    // Central Avatar Circle
                    Box(
                        modifier = Modifier
                            .size(92.dp)
                            .shadow(20.dp, CircleShape, spotColor = accentColor)
                            .background(Color(0xFF0F172A), CircleShape)
                            .border(3.dp, accentColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = callerProfile.avatarEmoji,
                            fontSize = 42.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Caller Name
                Text(
                    text = callerProfile.name,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    letterSpacing = 1.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                // Ringing Status Pill
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier
                        .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .border(1.dp, accentColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(if (isRinging) Color(0xFF00E676) else Color.Yellow, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isRinging) "INCOMING CALL..." else "CALL CONNECTED",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = accentColor,
                        letterSpacing = 1.5.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Phone Number & Location
                Text(
                    text = callerProfile.number,
                    fontSize = 14.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color(0xFF94A3B8)
                )
                Text(
                    text = callerProfile.location,
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                )
            }

            // Middle: Quick Action Row (Mute, Message, Speaker)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                QuickActionButton(label = "Mute", iconText = "🔇")
                QuickActionButton(label = "Message", iconText = "💬")
                QuickActionButton(label = "Speaker", iconText = "🔊")
            }

            // Bottom Section: Dynamic Accept & Decline Action Buttons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                when (buttonStyle) {
                    CallButtonStyle.GLASSMORPHISM -> {
                        GlassmorphismCallButtons(
                            onAccept = onAcceptCall,
                            onDecline = onDeclineCall
                        )
                    }
                    CallButtonStyle.RETRO_CYBER -> {
                        RetroCyberCallButtons(
                            onAccept = onAcceptCall,
                            onDecline = onDeclineCall
                        )
                    }
                    CallButtonStyle.MINIMAL_FLAT -> {
                        MinimalFlatCallButtons(
                            onAccept = onAcceptCall,
                            onDecline = onDeclineCall
                        )
                    }
                    else -> {
                        NeonGlowCallButtons(
                            accentColor = accentColor,
                            onAccept = onAcceptCall,
                            onDecline = onDeclineCall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickActionButton(label: String, iconText: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { /* Trigger quick action */ }
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .background(Color.White.copy(alpha = 0.08f), CircleShape)
                .border(1.dp, Color.White.copy(alpha = 0.12f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = iconText, fontSize = 20.sp)
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(text = label, fontSize = 11.sp, color = Color(0xFFCBD5E1))
    }
}

/**
 * ⚡ Neon Glow Buttons: Glowing green accept and red decline circles.
 */
@Composable
fun NeonGlowCallButtons(
    accentColor: Color,
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(0.85f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Decline Button
        Box(
            modifier = Modifier
                .size(68.dp)
                .shadow(16.dp, CircleShape, spotColor = Color(0xFFFF1744))
                .background(Color(0xFFFF1744), CircleShape)
                .border(2.dp, Color(0xFFFF5252), CircleShape)
                .clickable { onDecline() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "✕", fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color.White)
        }

        // Accept Button
        Box(
            modifier = Modifier
                .size(68.dp)
                .shadow(16.dp, CircleShape, spotColor = Color(0xFF00E676))
                .background(Color(0xFF00E676), CircleShape)
                .border(2.dp, Color(0xFF69F0AE), CircleShape)
                .clickable { onAccept() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "📞", fontSize = 28.sp)
        }
    }
}

/**
 * 💎 Glassmorphism Buttons: Frosted rounded rectangles.
 */
@Composable
fun GlassmorphismCallButtons(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(0.85f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 72.dp, height = 66.dp)
                .background(Color(0xFFFF1744).copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .border(1.5.dp, Color(0xFFFF5252).copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                .clickable { onDecline() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "✕", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Box(
            modifier = Modifier
                .size(width = 72.dp, height = 66.dp)
                .background(Color(0xFF00E676).copy(alpha = 0.35f), RoundedCornerShape(20.dp))
                .border(1.5.dp, Color(0xFF69F0AE).copy(alpha = 0.7f), RoundedCornerShape(20.dp))
                .clickable { onAccept() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "📞", fontSize = 26.sp)
        }
    }
}

/**
 * 🕹️ Retro Cyber Grid Buttons: Pixel rectangular arcade buttons.
 */
@Composable
fun RetroCyberCallButtons(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(0.85f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(width = 76.dp, height = 54.dp)
                .background(Color(0xFFD50000), RoundedCornerShape(8.dp))
                .border(2.dp, Color(0xFFFF1744), RoundedCornerShape(8.dp))
                .clickable { onDecline() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "END", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
        }

        Box(
            modifier = Modifier
                .size(width = 76.dp, height = 54.dp)
                .background(Color(0xFF00C853), RoundedCornerShape(8.dp))
                .border(2.dp, Color(0xFF00E676), RoundedCornerShape(8.dp))
                .clickable { onAccept() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "ANS", fontSize = 14.sp, fontWeight = FontWeight.Black, color = Color.White, letterSpacing = 1.sp)
        }
    }
}

/**
 * ⚪ Minimal Flat Buttons: Clean floating round buttons.
 */
@Composable
fun MinimalFlatCallButtons(
    onAccept: () -> Unit,
    onDecline: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(0.85f),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(62.dp)
                .background(Color(0xFFE11D48), CircleShape)
                .clickable { onDecline() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "✕", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Box(
            modifier = Modifier
                .size(62.dp)
                .background(Color(0xFF10B981), CircleShape)
                .clickable { onAccept() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "📞", fontSize = 26.sp)
        }
    }
}
