package com.parallax.wallpaper.edge

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max

@Composable
fun EdgeLightingCanvas(
    modifier: Modifier = Modifier,
    colors: List<String> = listOf("#FF0055", "#FF7700", "#FFE600", "#00FF66", "#00E5FF", "#7000FF"),
    animationType: String = "rainbow_wave",
    speed: Float = 1.0f,
    borderThickness: Dp = 6.dp,
    cornerRadius: Dp = 32.dp,
    punchHoleRadius: Dp = 0.dp
) {
    val parsedColors = if (colors.size >= 2) {
        colors.map { parseHexColor(it) }
    } else {
        listOf(Color(0xFF00E5FF), Color(0xFF7000FF), Color(0xFFFF0055))
    }

    val infiniteTransition = rememberInfiniteTransition(label = "edgeLightingAnim")

    // Rotation angle: 0 to 360 degrees
    val durationMs = max(600, (3000 / max(0.3f, speed)).toInt())
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotationAnim"
    )

    // Pulse alpha: 0.35f to 1.0f
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = durationMs / 2, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAnim"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val width = size.width
        val height = size.height
        val thicknessPx = borderThickness.toPx()
        val cornerRadiusPx = cornerRadius.toPx()
        val halfThickness = thicknessPx / 2f

        val stroke = Stroke(width = thicknessPx)
        val roundRect = RoundRect(
            left = halfThickness,
            top = halfThickness,
            right = width - halfThickness,
            bottom = height - halfThickness,
            cornerRadius = CornerRadius(cornerRadiusPx, cornerRadiusPx)
        )

        val path = Path().apply {
            addRoundRect(roundRect)
        }

        when (animationType) {
            "pulse_glow" -> {
                // Breathing Neon Pulse
                val sweepColors = parsedColors + parsedColors.first()
                val sweepBrush = Brush.sweepGradient(
                    colors = sweepColors,
                    center = Offset(width / 2f, height / 2f)
                )

                // Outer glow layer
                drawPath(
                    path = path,
                    brush = sweepBrush,
                    style = Stroke(width = thicknessPx * 1.8f),
                    alpha = pulseAlpha * 0.45f,
                    blendMode = BlendMode.Screen
                )

                // Inner core layer
                drawPath(
                    path = path,
                    brush = sweepBrush,
                    style = stroke,
                    alpha = pulseAlpha
                )
            }

            "snake_comet" -> {
                // Travelling Dual-Head Comet
                val cometColors = listOf(
                    Color.Transparent,
                    parsedColors[0].copy(alpha = 0.2f),
                    parsedColors[0],
                    parsedColors.getOrElse(1) { parsedColors[0] },
                    Color.Transparent,
                    Color.Transparent,
                    parsedColors.getOrElse(1) { parsedColors[0] }.copy(alpha = 0.2f),
                    parsedColors.getOrElse(1) { parsedColors[0] },
                    parsedColors[0],
                    Color.Transparent
                )

                rotate(degrees = rotation, pivot = Offset(width / 2f, height / 2f)) {
                    val sweep = Brush.sweepGradient(
                        colors = cometColors,
                        center = Offset(width / 2f, height / 2f)
                    )
                    // Glow
                    drawPath(
                        path = path,
                        brush = sweep,
                        style = Stroke(width = thicknessPx * 1.6f),
                        alpha = 0.5f
                    )
                    // Core
                    drawPath(
                        path = path,
                        brush = sweep,
                        style = stroke
                    )
                }
            }

            else -> {
                // 360° Sweep Rainbow Wave
                val sweepColors = parsedColors + parsedColors.first()
                rotate(degrees = rotation, pivot = Offset(width / 2f, height / 2f)) {
                    val sweep = Brush.sweepGradient(
                        colors = sweepColors,
                        center = Offset(width / 2f, height / 2f)
                    )
                    // Glow blur simulation
                    drawPath(
                        path = path,
                        brush = sweep,
                        style = Stroke(width = thicknessPx * 1.6f),
                        alpha = 0.4f,
                        blendMode = BlendMode.Screen
                    )
                    // Core Crisp Border
                    drawPath(
                        path = path,
                        brush = sweep,
                        style = stroke
                    )
                }
            }
        }

        // Camera Punch-Hole Ring Light
        val punchRadiusPx = punchHoleRadius.toPx()
        if (punchRadiusPx > 0f) {
            val cameraCenter = Offset(width / 2f, punchRadiusPx + 14.dp.toPx())
            val ringColors = parsedColors + parsedColors.first()

            rotate(degrees = rotation, pivot = cameraCenter) {
                drawCircle(
                    brush = Brush.sweepGradient(colors = ringColors, center = cameraCenter),
                    radius = punchRadiusPx + thicknessPx / 2f,
                    center = cameraCenter,
                    style = Stroke(width = thicknessPx)
                )
            }
        }
    }
}

private fun parseHexColor(hex: String): Color {
    return try {
        val clean = hex.trim().removePrefix("#")
        val colorInt = when (clean.length) {
            6 -> 0xFF000000.toInt() or clean.toLong(16).toInt()
            8 -> clean.toLong(16).toInt()
            3 -> {
                val r = clean[0].toString().repeat(2).toInt(16)
                val g = clean[1].toString().repeat(2).toInt(16)
                val b = clean[2].toString().repeat(2).toInt(16)
                (0xFF shl 24) or (r shl 16) or (g shl 8) or b
            }
            else -> 0xFF00E5FF.toInt()
        }
        Color(colorInt)
    } catch (_: Exception) {
        Color(0xFF00E5FF)
    }
}
