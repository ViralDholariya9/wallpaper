package com.parallax.wallpaper.gl

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Rect
import android.graphics.Shader

enum class DepthPreset(val title: String, val subtitle: String) {
    PORTRAIT_SUBJECT("Portrait & Subject", "Center-weighted depth for people, pets & cars"),
    LANDSCAPE_HORIZON("Landscape & Nature", "Perspective depth ground to horizon"),
    SPHERICAL_VORTEX("Spherical 3D Vortex", "Radial circular pop & tunnel curvature"),
    LUMINANCE_EDGE("Smart Luminance", "Depth derived from light contours & brightness")
}

object DepthMapGenerator {

    private const val DEPTH_MAP_WIDTH = 256
    private const val DEPTH_MAP_HEIGHT = 512

    /**
     * Synthesizes an optimized, hardware-accelerated 3D Depth Map texture from a source photo.
     */
    fun generate(
        sourceBitmap: Bitmap,
        preset: DepthPreset = DepthPreset.PORTRAIT_SUBJECT,
        focusY: Float = 0.45f,
        invert: Boolean = false,
        contrastBoost: Float = 1.0f
    ): Bitmap {
        val depthBitmap = Bitmap.createBitmap(DEPTH_MAP_WIDTH, DEPTH_MAP_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(depthBitmap)
        val width = DEPTH_MAP_WIDTH.toFloat()
        val height = DEPTH_MAP_HEIGHT.toFloat()

        when (preset) {
            DepthPreset.PORTRAIT_SUBJECT -> {
                // 1. Subtle background perspective layer
                val basePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        0f, 0f, 0f, height,
                        intArrayOf(Color.rgb(40, 40, 40), Color.rgb(80, 80, 80), Color.rgb(150, 150, 150)),
                        floatArrayOf(0f, 0.5f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, width, height, basePaint)

                // 2. Focused subject radial sphere at user-chosen focal height
                val focalYPos = height * focusY.coerceIn(0.15f, 0.85f)
                val subjectPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = RadialGradient(
                        width * 0.5f, focalYPos,
                        width * 0.65f,
                        intArrayOf(Color.WHITE, Color.rgb(140, 140, 140), Color.TRANSPARENT),
                        floatArrayOf(0f, 0.55f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawCircle(width * 0.5f, focalYPos, width * 0.65f, subjectPaint)
            }

            DepthPreset.LANDSCAPE_HORIZON -> {
                val horizonY = height * focusY.coerceIn(0.2f, 0.8f)
                val landscapePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = LinearGradient(
                        0f, 0f, 0f, height,
                        intArrayOf(
                            Color.rgb(100, 100, 100), // Sky far
                            Color.rgb(30, 30, 30),     // Horizon furthest back
                            Color.rgb(180, 180, 180),  // Mid-ground
                            Color.WHITE                // Foreground closest
                        ),
                        floatArrayOf(0f, focusY * 0.9f, (focusY + 1f) * 0.5f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, width, height, landscapePaint)
            }

            DepthPreset.SPHERICAL_VORTEX -> {
                val focalYPos = height * focusY.coerceIn(0.1f, 0.9f)
                val vortexPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    shader = RadialGradient(
                        width * 0.5f, focalYPos,
                        width * 0.75f,
                        intArrayOf(
                            Color.WHITE,
                            Color.rgb(160, 160, 160),
                            Color.rgb(70, 70, 70),
                            Color.rgb(10, 10, 10)
                        ),
                        floatArrayOf(0f, 0.35f, 0.7f, 1f),
                        Shader.TileMode.CLAMP
                    )
                }
                canvas.drawRect(0f, 0f, width, height, vortexPaint)
            }

            DepthPreset.LUMINANCE_EDGE -> {
                // High-performance hardware grayscale & contrast boost matrix
                val cm = ColorMatrix().apply {
                    setSaturation(0f)
                    val scale = contrastBoost.coerceIn(0.8f, 2.2f)
                    val translate = (-64f * (scale - 1f))
                    val contrastMatrix = floatArrayOf(
                        scale, 0f, 0f, 0f, translate,
                        0f, scale, 0f, 0f, translate,
                        0f, 0f, scale, 0f, translate,
                        0f, 0f, 0f, 1f, 0f
                    )
                    postConcat(ColorMatrix(contrastMatrix))
                }
                val lumPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                    colorFilter = ColorMatrixColorFilter(cm)
                }
                canvas.drawBitmap(
                    sourceBitmap,
                    Rect(0, 0, sourceBitmap.width, sourceBitmap.height),
                    Rect(0, 0, DEPTH_MAP_WIDTH, DEPTH_MAP_HEIGHT),
                    lumPaint
                )
            }
        }

        // Apply Invert if requested (e.g. Bring background forward / push center back)
        if (invert) {
            val invertMatrix = ColorMatrix(
                floatArrayOf(
                    -1f, 0f, 0f, 0f, 255f,
                    0f, -1f, 0f, 0f, 255f,
                    0f, 0f, -1f, 0f, 255f,
                    0f, 0f, 0f, 1f, 0f
                )
            )
            val invertPaint = Paint().apply { colorFilter = ColorMatrixColorFilter(invertMatrix) }
            canvas.drawBitmap(depthBitmap, 0f, 0f, invertPaint)
        }

        return depthBitmap
    }
}
