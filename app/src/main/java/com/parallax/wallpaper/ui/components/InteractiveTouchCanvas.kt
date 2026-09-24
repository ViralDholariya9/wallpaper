package com.parallax.wallpaper.ui.components

import android.content.Context
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.parallax.wallpaper.model.TouchEffectPreset
import com.parallax.wallpaper.model.TouchEffectType
import com.parallax.wallpaper.touch.TouchEffectManager
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

private data class ActiveRipple(
    val x: Float,
    val y: Float,
    var radius: Float,
    val maxRadius: Float,
    var alpha: Float,
    val speed: Float,
    val color: Color
)

private data class ActiveParticle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var radius: Float,
    var alpha: Float,
    val color: Color,
    val decay: Float,
    var rotation: Float = 0f
)

private data class ActiveLightning(
    val segments: List<Offset>,
    var alpha: Float,
    val color: Color
)

@Composable
fun InteractiveTouchCanvas(
    preset: TouchEffectPreset,
    modifier: Modifier = Modifier,
    shockwaveTrigger: Int = 0,
    onInteractiveTouch: (() -> Unit)? = null
) {
    val context = LocalContext.current

    val primaryColor = remember(preset.primaryColor) {
        try {
            Color(android.graphics.Color.parseColor(preset.primaryColor))
        } catch (_: Exception) {
            Color(0xFF00E5FF)
        }
    }

    val secondaryColor = remember(preset.secondaryColor) {
        try {
            Color(android.graphics.Color.parseColor(preset.secondaryColor))
        } catch (_: Exception) {
            Color(0xFF7000FF)
        }
    }

    val ripples = remember { mutableStateListOf<ActiveRipple>() }
    val particles = remember { mutableStateListOf<ActiveParticle>() }
    val lightnings = remember { mutableStateListOf<ActiveLightning>() }

    // Helper functions to spawn effects
    fun spawnTouch(x: Float, y: Float, isInitialTap: Boolean) {
        if (isInitialTap) {
            TouchEffectManager.triggerHaptic(context)
            onInteractiveTouch?.invoke()
        }

        when (preset.typeEnum) {
            TouchEffectType.WATER_RIPPLE -> {
                // Expanding water wave rings
                ripples.add(
                    ActiveRipple(
                        x = x,
                        y = y,
                        radius = 12f,
                        maxRadius = preset.rippleRadius * 1.2f,
                        alpha = 0.95f,
                        speed = 6f * preset.speed,
                        color = primaryColor
                    )
                )
                if (isInitialTap) {
                    ripples.add(
                        ActiveRipple(
                            x = x,
                            y = y,
                            radius = 6f,
                            maxRadius = preset.rippleRadius * 0.8f,
                            alpha = 0.7f,
                            speed = 4f * preset.speed,
                            color = secondaryColor
                        )
                    )
                }
            }

            TouchEffectType.NEON_FLUID -> {
                // Neon fluid smoke dye puffs
                val count = if (isInitialTap) (preset.particleCount / 4).coerceIn(12, 35) else 4
                for (i in 0 until count) {
                    val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
                    val speed = Random.nextFloat() * 5f * preset.speed + 1.5f
                    val col = if (Random.nextBoolean()) primaryColor else secondaryColor
                    particles.add(
                        ActiveParticle(
                            x = x + Random.nextFloat() * 20f - 10f,
                            y = y + Random.nextFloat() * 20f - 10f,
                            vx = cos(angle) * speed,
                            vy = sin(angle) * speed,
                            radius = Random.nextFloat() * 24f + 12f,
                            alpha = 0.85f,
                            color = col,
                            decay = Random.nextFloat() * 0.025f + 0.018f
                        )
                    )
                }
            }

            TouchEffectType.ELECTRIC_SPARKS -> {
                // Lightning branches
                val branches = if (isInitialTap) 3 else 1
                for (b in 0 until branches) {
                    val pts = mutableListOf<Offset>()
                    var curX = x
                    var curY = y
                    pts.add(Offset(curX, curY))
                    val segments = Random.nextInt(4, 8)
                    val baseAngle = Random.nextFloat() * 2f * Math.PI.toFloat()
                    val segLen = (preset.rippleRadius / segments) * 0.8f
                    for (s in 0 until segments) {
                        val jitter = (Random.nextFloat() - 0.5f) * 45f
                        curX += cos(baseAngle) * segLen + jitter
                        curY += sin(baseAngle) * segLen + jitter
                        pts.add(Offset(curX, curY))
                    }
                    lightnings.add(
                        ActiveLightning(
                            segments = pts,
                            alpha = 1.0f,
                            color = if (Random.nextBoolean()) primaryColor else secondaryColor
                        )
                    )
                }

                // Spark sparks
                val sparkCount = if (isInitialTap) 15 else 3
                for (i in 0 until sparkCount) {
                    val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
                    val spd = Random.nextFloat() * 9f * preset.speed + 3f
                    particles.add(
                        ActiveParticle(
                            x = x,
                            y = y,
                            vx = cos(angle) * spd,
                            vy = sin(angle) * spd,
                            radius = Random.nextFloat() * 4f + 2f,
                            alpha = 1f,
                            color = primaryColor,
                            decay = 0.045f
                        )
                    )
                }
            }

            TouchEffectType.MAGIC_STARDUST -> {
                // Celestial star twinkling aura
                val count = if (isInitialTap) 20 else 5
                for (i in 0 until count) {
                    val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
                    val speed = Random.nextFloat() * 3.5f * preset.speed + 0.5f
                    val col = if (Random.nextBoolean()) primaryColor else secondaryColor
                    particles.add(
                        ActiveParticle(
                            x = x,
                            y = y,
                            vx = cos(angle) * speed,
                            vy = sin(angle) * speed - (Random.nextFloat() * 2f + 1f), // float upwards
                            radius = Random.nextFloat() * 10f + 4f,
                            alpha = 0.95f,
                            color = col,
                            decay = 0.02f,
                            rotation = Random.nextFloat() * 360f
                        )
                    )
                }
            }

            TouchEffectType.MAGMA_BURST -> {
                // Solar magma flare & fire ember burst
                val count = if (isInitialTap) 28 else 6
                for (i in 0 until count) {
                    val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
                    val speed = Random.nextFloat() * 7f * preset.speed + 2f
                    val col = if (Random.nextBoolean()) primaryColor else secondaryColor
                    particles.add(
                        ActiveParticle(
                            x = x,
                            y = y,
                            vx = cos(angle) * speed,
                            vy = sin(angle) * speed,
                            radius = Random.nextFloat() * 18f + 8f,
                            alpha = 1.0f,
                            color = col,
                            decay = 0.035f
                        )
                    )
                }
                ripples.add(
                    ActiveRipple(
                        x = x,
                        y = y,
                        radius = 8f,
                        maxRadius = preset.rippleRadius * 0.9f,
                        alpha = 0.8f,
                        speed = 8f * preset.speed,
                        color = primaryColor
                    )
                )
            }

            TouchEffectType.GRAVITY_VORTEX -> {
                // Quantum gravity vortex swirl
                val count = if (isInitialTap) 30 else 7
                for (i in 0 until count) {
                    val angle = Random.nextFloat() * 2f * Math.PI.toFloat()
                    val dist = Random.nextFloat() * 140f + 30f
                    val col = if (Random.nextBoolean()) primaryColor else secondaryColor
                    particles.add(
                        ActiveParticle(
                            x = x + cos(angle) * dist,
                            y = y + sin(angle) * dist,
                            vx = -sin(angle) * (3f * preset.speed), // tangential orbit
                            vy = cos(angle) * (3f * preset.speed),
                            radius = Random.nextFloat() * 9f + 4f,
                            alpha = 0.9f,
                            color = col,
                            decay = 0.022f
                        )
                    )
                }
            }
        }
    }

    // Shockwave pulse trigger
    LaunchedEffect(shockwaveTrigger) {
        if (shockwaveTrigger > 0) {
            TouchEffectManager.triggerHaptic(context)
            val cx = 540f
            val cy = 960f
            ripples.add(
                ActiveRipple(
                    x = cx,
                    y = cy,
                    radius = 20f,
                    maxRadius = 750f,
                    alpha = 1.0f,
                    speed = 14f * preset.speed,
                    color = primaryColor
                )
            )
            ripples.add(
                ActiveRipple(
                    x = cx,
                    y = cy,
                    radius = 10f,
                    maxRadius = 600f,
                    alpha = 0.85f,
                    speed = 10f * preset.speed,
                    color = secondaryColor
                )
            )
        }
    }

    // 60fps Continuous physics update loop
    var frameTick by remember { mutableLongStateOf(0L) }
    LaunchedEffect(Unit) {
        while (isActive) {
            kotlinx.coroutines.android.awaitFrame()
            frameTick++

            // Update ripples
            if (ripples.isNotEmpty()) {
                val iter = ripples.iterator()
                while (iter.hasNext()) {
                    val r = iter.next()
                    r.radius += r.speed
                    r.alpha = ((1f - (r.radius / r.maxRadius)) * 0.9f).coerceIn(0f, 1f)
                    if (r.radius >= r.maxRadius || r.alpha <= 0.01f) {
                        iter.remove()
                    }
                }
            }

            // Update particles
            if (particles.isNotEmpty()) {
                val iter = particles.iterator()
                while (iter.hasNext()) {
                    val p = iter.next()
                    p.x += p.vx
                    p.y += p.vy
                    p.vx *= 0.96f
                    p.vy *= 0.96f
                    p.radius *= 0.985f
                    p.alpha -= p.decay
                    p.rotation += 4f
                    if (p.alpha <= 0.01f || p.radius <= 1f) {
                        iter.remove()
                    }
                }
            }

            // Update lightning arcs
            if (lightnings.isNotEmpty()) {
                val iter = lightnings.iterator()
                while (iter.hasNext()) {
                    val l = iter.next()
                    l.alpha -= 0.12f
                    if (l.alpha <= 0.01f) {
                        iter.remove()
                    }
                }
            }
        }
    }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(preset) {
                detectTapGestures(
                    onPress = { offset ->
                        spawnTouch(offset.x, offset.y, isInitialTap = true)
                    }
                )
            }
            .pointerInput(preset) {
                detectDragGestures(
                    onDragStart = { offset ->
                        spawnTouch(offset.x, offset.y, isInitialTap = true)
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        spawnTouch(change.position.x, change.position.y, isInitialTap = false)
                    }
                )
            }
    ) {
        // Read frameTick to trigger redraw
        @Suppress("UNUSED_VARIABLE")
        val tick = frameTick

        // Draw ripples
        for (r in ripples) {
            drawCircle(
                color = r.color.copy(alpha = r.alpha),
                radius = r.radius,
                center = Offset(r.x, r.y),
                style = Stroke(width = (4f * (1f - (r.radius / r.maxRadius).coerceIn(0f, 0.9f))).coerceAtLeast(1.5f))
            )
        }

        // Draw fluid and star particles
        for (p in particles) {
            drawCircle(
                color = p.color.copy(alpha = p.alpha.coerceIn(0f, 1f)),
                radius = p.radius.coerceAtLeast(1f),
                center = Offset(p.x, p.y),
                blendMode = BlendMode.SrcOver
            )
        }

        // Draw electric arcs
        for (l in lightnings) {
            if (l.segments.size > 1) {
                for (i in 0 until l.segments.size - 1) {
                    drawLine(
                        color = l.color.copy(alpha = l.alpha.coerceIn(0f, 1f)),
                        start = l.segments[i],
                        end = l.segments[i + 1],
                        strokeWidth = 3f * l.alpha
                    )
                }
            }
        }
    }
}
