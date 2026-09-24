package com.parallax.wallpaper.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.parallax.wallpaper.fingerprint.FingerprintAnimationManager
import com.parallax.wallpaper.model.FingerprintAnimationType
import com.parallax.wallpaper.model.FingerprintPreset
import kotlinx.coroutines.delay
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class FpParticle(
    var x: Float,
    var y: Float,
    val vx: Float,
    val vy: Float,
    var alpha: Float = 1.0f,
    val color: Color,
    val radius: Float,
    val decay: Float
)

private data class FpWave(
    var radius: Float,
    val maxRadius: Float,
    var alpha: Float = 1.0f,
    val color: Color,
    val strokeWidth: Float,
    val speed: Float
)

@Composable
fun InDisplayFingerprintView(
    preset: FingerprintPreset,
    modifier: Modifier = Modifier,
    yPositionPercent: Int = 78,
    scale: Float = 1.0f,
    interactive: Boolean = true,
    onUnlocked: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val primaryColor = remember(preset.primaryColor) {
        parseHexColor(preset.primaryColor, Color(0xFF00E5FF))
    }
    val secondaryColor = remember(preset.secondaryColor) {
        parseHexColor(preset.secondaryColor, Color(0xFF7000FF))
    }
    val accentGlow = remember(preset.accentGlow) {
        parseHexColor(preset.accentGlow, Color(0xFF00FFAA))
    }

    val speed = preset.animationSpeed.coerceIn(0.5f, 3.0f)
    val outerDuration = (2200 / speed).toInt()
    val innerDuration = (1400 / speed).toInt()

    val infiniteTransition = rememberInfiniteTransition(label = "fingerprint_anim")

    val outerRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(outerDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "outer_rot"
    )

    val innerRotation by infiniteTransition.animateFloat(
        initialValue = 360f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(innerDuration, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "inner_rot"
    )

    val laserProgress by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween((900 / speed).toInt(), easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "laser_sweep"
    )

    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    var isScanning by remember { mutableStateOf(false) }
    var isUnlocked by remember { mutableStateOf(false) }

    val particles = remember { mutableStateListOf<FpParticle>() }
    val waves = remember { mutableStateListOf<FpWave>() }

    // Particle animation frame ticker
    var frameTick by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(particles.size, waves.size) {
        while (particles.isNotEmpty() || waves.isNotEmpty()) {
            delay(16)
            // Update waves
            val waveIterator = waves.iterator()
            while (waveIterator.hasNext()) {
                val w = waveIterator.next()
                w.radius += w.speed
                w.alpha -= 0.024f * speed
                if (w.alpha <= 0f || w.radius >= w.maxRadius) {
                    waveIterator.remove()
                }
            }

            // Update particles
            val partIterator = particles.iterator()
            while (partIterator.hasNext()) {
                val p = partIterator.next()
                p.x += p.vx
                p.y += p.vy
                p.alpha -= p.decay
                if (p.alpha <= 0f) {
                    partIterator.remove()
                }
            }
            frameTick += 1f
        }
    }

    fun triggerUnlock() {
        isUnlocked = true
        isScanning = false
        FingerprintAnimationManager.performHapticUnlock(context)
        onUnlocked?.invoke()

        // Spawn shockwave waves
        waves.add(
            FpWave(
                radius = 20f * scale,
                maxRadius = 380f * scale,
                alpha = 1.0f,
                color = primaryColor,
                strokeWidth = 5f,
                speed = 8.5f * speed
            )
        )
        waves.add(
            FpWave(
                radius = 10f * scale,
                maxRadius = 260f * scale,
                alpha = 0.85f,
                color = secondaryColor,
                strokeWidth = 3.5f,
                speed = 6.0f * speed
            )
        )

        // Spawn particles based on animation type
        val count = 48
        for (i in 0 until count) {
            val angle = (Math.PI * 2 * i / count).toFloat() + (Random.nextFloat() - 0.5f) * 0.4f
            val pSpeed = (Random.nextFloat() * 7f + 2.5f) * speed
            val pColor = when (preset.typeEnum) {
                FingerprintAnimationType.CYBER_MATRIX -> if (Random.nextBoolean()) primaryColor else accentGlow
                FingerprintAnimationType.SUPERNOVA_FLARE -> if (Random.nextBoolean()) primaryColor else secondaryColor
                FingerprintAnimationType.NEON_PORTAL -> if (Random.nextBoolean()) accentGlow else secondaryColor
                FingerprintAnimationType.MYSTIC_RUNES -> if (Random.nextBoolean()) primaryColor else Color(0xFFFFD700)
                FingerprintAnimationType.CIRCUIT_OVERLOAD -> if (Random.nextBoolean()) primaryColor else Color(0xFFFFEB3B)
                FingerprintAnimationType.SOLAR_FLARE -> if (Random.nextBoolean()) primaryColor else Color(0xFFFF3D00)
            }
            particles.add(
                FpParticle(
                    x = 0f,
                    y = 0f,
                    vx = cos(angle) * pSpeed,
                    vy = sin(angle) * pSpeed,
                    alpha = 1.0f,
                    color = pColor,
                    radius = Random.nextFloat() * 5f + 2.5f,
                    decay = (Random.nextFloat() * 0.025f + 0.018f) * speed
                )
            )
        }
    }

    val sensorSizeDp = (76 * scale).dp

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        // Position hotspot dynamically using yPositionPercent
        val yFraction = (yPositionPercent / 100f).coerceIn(0.2f, 0.95f)

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(interactive, preset) {
                        if (interactive) {
                            detectTapGestures(
                                onPress = {
                                    isScanning = true
                                    isUnlocked = false
                                    FingerprintAnimationManager.performHapticScan(context)
                                    val holdSuccess = tryAwaitRelease()
                                    if (holdSuccess) {
                                        // Quick tap or release
                                        triggerUnlock()
                                    } else {
                                        isScanning = false
                                    }
                                }
                            )
                        }
                    }
            ) {
                val cx = size.width / 2f
                val cy = size.height * yFraction
                val baseRadius = 36f * density.density * scale * if (isScanning) 1.08f else pulseScale

                // 1. Draw Shockwave Waves
                waves.forEach { wave ->
                    drawCircle(
                        color = wave.color.copy(alpha = wave.alpha.coerceIn(0f, 1f)),
                        radius = wave.radius,
                        center = Offset(cx, cy),
                        style = Stroke(width = wave.strokeWidth)
                    )
                }

                // 2. Draw Burst Particles
                particles.forEach { p ->
                    drawCircle(
                        color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                        radius = p.radius,
                        center = Offset(cx + p.x, cy + p.y)
                    )
                }

                // 3. Sensor Background Glow
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = if (isScanning) 0.35f else 0.12f),
                            secondaryColor.copy(alpha = if (isScanning) 0.18f else 0.04f),
                            Color.Transparent
                        ),
                        center = Offset(cx, cy),
                        radius = baseRadius * 1.8f
                    ),
                    center = Offset(cx, cy),
                    radius = baseRadius * 1.8f
                )

                // 4. Outer Rotating Concentric HUD Ring
                rotate(degrees = outerRotation, pivot = Offset(cx, cy)) {
                    drawCircle(
                        color = primaryColor.copy(alpha = 0.85f),
                        radius = baseRadius * 1.25f,
                        center = Offset(cx, cy),
                        style = Stroke(
                            width = 2.5f * density.density,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(16f, 12f, 28f, 12f), 0f)
                        )
                    )
                }

                // 5. Inner Rotating Concentric Ring
                rotate(degrees = innerRotation, pivot = Offset(cx, cy)) {
                    drawCircle(
                        color = secondaryColor.copy(alpha = 0.9f),
                        radius = baseRadius * 1.02f,
                        center = Offset(cx, cy),
                        style = Stroke(
                            width = 1.8f * density.density,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(24f, 14f), 0f)
                        )
                    )
                }

                // 6. Biometric Fingerprint Ridges Mockup
                val ridgeRadius = baseRadius * 0.72f
                for (r in 1..4) {
                    val currentR = ridgeRadius * (r / 4f)
                    drawArc(
                        color = (if (isScanning) accentGlow else primaryColor).copy(alpha = 0.75f - (r * 0.08f)),
                        startAngle = 140f,
                        sweepAngle = 260f,
                        useCenter = false,
                        topLeft = Offset(cx - currentR, cy - currentR),
                        size = androidx.compose.ui.geometry.Size(currentR * 2f, currentR * 2f),
                        style = Stroke(width = 2.0f * density.density, cap = StrokeCap.Round)
                    )
                }

                // 7. Scanning Laser Sweep Line
                val laserY = cy + (baseRadius * 0.8f * laserProgress)
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            (if (isScanning) accentGlow else primaryColor).copy(alpha = 0.95f),
                            Color.Transparent
                        ),
                        startX = cx - baseRadius,
                        endX = cx + baseRadius
                    ),
                    start = Offset(cx - baseRadius * 0.9f, laserY),
                    end = Offset(cx + baseRadius * 0.9f, laserY),
                    strokeWidth = 3f * density.density
                )
            }
        }
    }
}

private fun parseHexColor(hex: String?, fallback: Color): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        val clean = hex.trim().removePrefix("#")
        when (clean.length) {
            6 -> Color(clean.toLong(16) or 0x00000000FF000000)
            8 -> Color(clean.toLong(16))
            else -> fallback
        }
    } catch (_: Exception) {
        fallback
    }
}
