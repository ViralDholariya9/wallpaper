package com.parallax.wallpaper.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.wallpaper.R
import com.parallax.wallpaper.ui.theme.NeonCyan
import com.parallax.wallpaper.ui.theme.NeonPink
import com.parallax.wallpaper.ui.theme.NeonPurple
import kotlinx.coroutines.delay

/**
 * 🌟 SplashScreen
 * Ultra-fluid animated brand launch splash screen.
 * Displays user's glowing 3D ReWall app icon with neon ambience.
 */
@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit
) {
    var startAnimation by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.72f,
        animationSpec = tween(durationMillis = 900, easing = FastOutSlowInEasing),
        label = "logo_scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 750),
        label = "logo_alpha"
    )

    val infiniteTransition = rememberInfiniteTransition(label = "pulse_glow")
    val glowPulse by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow_pulse"
    )

    var isFinished by remember { mutableStateOf(false) }
    fun finishSplash() {
        if (!isFinished) {
            isFinished = true
            onSplashFinished()
        }
    }

    LaunchedEffect(Unit) {
        startAnimation = true
        delay(900)
        finishSplash()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF050811))
            .clickable { finishSplash() },
        contentAlignment = Alignment.Center
    ) {
        // Ambient Cyber Glow in background
        Box(
            modifier = Modifier
                .size(280.dp)
                .scale(glowPulse)
                .blur(50.dp)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NeonPurple.copy(alpha = 0.35f),
                            NeonCyan.copy(alpha = 0.25f),
                            Color.Transparent
                        )
                    ),
                    shape = CircleShape
                )
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            // Center: Glowing App Logo & Brand Title
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .scale(scale)
                    .alpha(alpha)
            ) {
                // Outer squircle container with neon rainbow border
                Box(
                    modifier = Modifier
                        .size(148.dp)
                        .clip(RoundedCornerShape(34.dp))
                        .border(
                            width = 2.dp,
                            brush = Brush.sweepGradient(
                                listOf(
                                    NeonCyan,
                                    NeonPurple,
                                    NeonPink,
                                    Color(0xFFFFB300),
                                    NeonCyan
                                )
                            ),
                            shape = RoundedCornerShape(34.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "ReWall 3D App Logo",
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(32.dp))
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Brand Title with Gradient
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "ReWall",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.5.sp,
                            color = Color.White
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = NeonPurple.copy(alpha = 0.28f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan.copy(alpha = 0.6f))
                    ) {
                        Text(
                            text = "3D",
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelMedium.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = NeonCyan
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "4K LIVE PARALLAX ENGINE",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp,
                        color = NeonCyan.copy(alpha = 0.85f)
                    )
                )
            }

            // Bottom Brand Footnote & Loading dots
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .alpha(alpha)
                    .padding(bottom = 36.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(NeonCyan)
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(NeonPurple)
                    )
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(NeonPink)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Crafted for 60FPS Gyroscope Tracking",
                    style = MaterialTheme.typography.bodySmall.copy(
                        color = Color.White.copy(alpha = 0.45f),
                        letterSpacing = 0.5.sp
                    )
                )
            }
        }
    }
}
