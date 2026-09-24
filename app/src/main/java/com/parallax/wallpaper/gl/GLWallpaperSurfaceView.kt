package com.parallax.wallpaper.gl

import android.content.Context
import android.graphics.Bitmap
import android.opengl.GLSurfaceView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.SurfaceHolder

class GLWallpaperSurfaceView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    private val customHolder: SurfaceHolder? = null
) : GLSurfaceView(context, attrs) {

    val renderer = GLParallaxRenderer()

    init {
        setEGLContextClientVersion(2)
        setRenderer(renderer)
        renderMode = RENDERMODE_CONTINUOUSLY
    }

    override fun getHolder(): SurfaceHolder {
        return customHolder ?: super.getHolder()
    }

    private val batteryOptimizer = com.parallax.wallpaper.power.BatteryOptimizer.getInstance(context)
    private var baseSensorX = 0f
    private var baseSensorY = 0f
    private var touchDragX = 0f
    private var touchDragY = 0f
    private var isAtEdgeBoundary = false

    fun setIsDeviceMoving(moving: Boolean) {
        val isEco = batteryOptimizer.isUltraBatterySaverEnabled.value || batteryOptimizer.isSystemPowerSaveActive.value
        if (isEco && !moving) {
            renderMode = RENDERMODE_WHEN_DIRTY
        } else {
            renderMode = RENDERMODE_CONTINUOUSLY
        }
    }

    fun setSensorOffset(x: Float, y: Float) {
        this.baseSensorX = x
        this.baseSensorY = y
        val totalX = (baseSensorX + touchDragX).coerceIn(-1.5f, 1.5f)
        val totalY = (baseSensorY + touchDragY).coerceIn(-1.5f, 1.5f)
        renderer.setSensorOffset(totalX, totalY)

        val isNearEdge = kotlin.math.abs(totalX) >= 0.95f || kotlin.math.abs(totalY) >= 0.95f
        if (isNearEdge && !isAtEdgeBoundary) {
            isAtEdgeBoundary = true
            com.parallax.wallpaper.utils.HapticHelper.edgeLimit(context)
        } else if (!isNearEdge && isAtEdgeBoundary) {
            isAtEdgeBoundary = false
        }

        if (renderMode == RENDERMODE_WHEN_DIRTY) {
            requestRender()
        }
    }

    fun setBitmap(bitmap: Bitmap, depthBitmap: Bitmap? = null) {
        renderer.loadBitmaps(bitmap, depthBitmap)
    }

    fun setDepthIntensity(intensity: Float) {
        renderer.depthIntensity = intensity
    }

    fun setParticleTheme(theme: ParticleTheme) {
        renderer.setParticleTheme(theme)
    }

    fun setFlareIntensity(intensity: Float) {
        renderer.flareIntensity = intensity
    }

    fun setHoloIntensity(intensity: Float) {
        renderer.holoIntensity = intensity
    }

    fun setWeatherCondition(condition: com.parallax.wallpaper.weather.WeatherCondition) {
        renderer.weatherCondition = condition
        if (renderMode == RENDERMODE_WHEN_DIRTY) {
            requestRender()
        }
    }

    fun setWeatherOverlayEnabled(enabled: Boolean) {
        renderer.isWeatherOverlayEnabled = enabled
        if (renderMode == RENDERMODE_WHEN_DIRTY) {
            requestRender()
        }
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event == null) return super.onTouchEvent(event)
        val w = width.coerceAtLeast(1).toFloat()
        val h = height.coerceAtLeast(1).toFloat()

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                val normX = (event.x / w).coerceIn(0f, 1f)
                val normY = (event.y / h).coerceIn(0f, 1f)
                renderer.triggerTouchRipple(normX, normY)
                touchDragX = ((event.x - w * 0.5f) / (w * 0.5f)) * 0.65f
                touchDragY = ((event.y - h * 0.5f) / (h * 0.5f)) * 0.65f
                renderer.setSensorOffset(baseSensorX + touchDragX, baseSensorY + touchDragY)
                return true
            }
            MotionEvent.ACTION_MOVE -> {
                touchDragX = ((event.x - w * 0.5f) / (w * 0.5f)) * 0.65f
                touchDragY = ((event.y - h * 0.5f) / (h * 0.5f)) * 0.65f
                renderer.setSensorOffset(baseSensorX + touchDragX, baseSensorY + touchDragY)
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                touchDragX = 0f
                touchDragY = 0f
                renderer.setSensorOffset(baseSensorX, baseSensorY)
                return true
            }
        }
        return super.onTouchEvent(event)
    }
}
