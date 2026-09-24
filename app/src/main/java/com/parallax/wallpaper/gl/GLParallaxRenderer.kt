package com.parallax.wallpaper.gl

import android.graphics.Bitmap
import android.opengl.GLES20
import android.opengl.GLSurfaceView
import com.parallax.wallpaper.weather.WeatherCondition
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.opengles.GL10
import kotlin.math.cos
import kotlin.math.sin

/**
 * Hardware-Accelerated 4D Holographic & Living 3D Parallax Renderer.
 * Features:
 * - 8-Layer Parallax Occlusion Mapping (POM) with 3D perspective quad tilt.
 * - Dynamic Ambient Breathing Motion (keeps wallpaper floating and alive even when stationary).
 * - Anamorphic Lens Flare, Specular Sheen, Holographic Rainbow Glare, and Edge Anti-Stretching.
 * - Multi-theme floating stardust / cyber rain / neon embers / digital sparks with touch blast.
 */
class GLParallaxRenderer : GLSurfaceView.Renderer {

    private val depthShader = DepthMapShader()
    private val particleSystem = ParticleSystem(120)
    private val weatherOverlaySystem = WeatherOverlaySystem(180)

    private var baseTextureId = 0
    private var depthMapId = 0

    private var currentBaseBitmapRef: Bitmap? = null
    private var pendingBaseBitmap: Bitmap? = null
    private var pendingDepthBitmap: Bitmap? = null

    // Sensor tilt offset
    private var offsetX = 0f
    private var offsetY = 0f

    // Interactive Touch Ripple state
    private var touchX = 0.5f
    private var touchY = 0.5f
    private var touchTime = -1.0f
    private var lastFrameTimestamp = 0L
    private var totalElapsedTimeSec = 0f

    // Enhanced default depth intensity for pronounced 3D pop
    var depthIntensity = 0.09f
    var flareIntensity = 1.0f
    var holoIntensity = 1.0f

    var isParticlesEnabled: Boolean = true
    var particleColorR: Float = 0.0f
    var particleColorG: Float = 0.9f
    var particleColorB: Float = 1.0f

    fun setSensorOffset(x: Float, y: Float) {
        this.offsetX = x
        this.offsetY = y
    }

    fun triggerTouchRipple(normalizedX: Float, normalizedY: Float) {
        this.touchX = normalizedX
        this.touchY = normalizedY
        this.touchTime = 0.0f
    }

    fun setParticleTheme(theme: ParticleTheme) {
        particleSystem.currentTheme = theme
        isParticlesEnabled = theme != ParticleTheme.NONE
    }

    fun setParticleColor(r: Float, g: Float, b: Float) {
        this.particleColorR = r
        this.particleColorG = g
        this.particleColorB = b
    }

    fun loadBitmaps(baseBitmap: Bitmap, depthBitmap: Bitmap? = null) {
        synchronized(this) {
            if (currentBaseBitmapRef === baseBitmap && pendingBaseBitmap == null && baseTextureId != 0) {
                return
            }
            this.currentBaseBitmapRef = baseBitmap
            this.pendingBaseBitmap = baseBitmap
            this.pendingDepthBitmap = depthBitmap ?: GLTextureHelper.generateDepthMap(baseBitmap)
        }
    }

    override fun onSurfaceCreated(gl: GL10?, config: EGLConfig?) {
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f)
        GLES20.glDisable(GLES20.GL_DEPTH_TEST)
        GLES20.glEnable(GLES20.GL_BLEND)
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA)

        depthShader.initGL()
        particleSystem.initGL()
        weatherOverlaySystem.initGL()

        uploadPendingTextures()
    }

    override fun onSurfaceChanged(gl: GL10?, width: Int, height: Int) {
        GLES20.glViewport(0, 0, width, height)
    }

    override fun onDrawFrame(gl: GL10?) {
        uploadPendingTextures()

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT)

        val currentTime = System.currentTimeMillis()
        val deltaSec = if (lastFrameTimestamp > 0L) {
            ((currentTime - lastFrameTimestamp) / 1000f).coerceIn(0.001f, 0.1f)
        } else {
            0.016f
        }
        lastFrameTimestamp = currentTime
        totalElapsedTimeSec += deltaSec

        if (touchTime >= 0f && touchTime < 3.0f) {
            touchTime += deltaSec
            if (touchTime >= 3.0f) touchTime = -1.0f
        }

        // Ambient Harmonic Breathing: gentle floating drift
        val ambientFloatX = sin(totalElapsedTimeSec * 1.15f) * 0.065f
        val ambientFloatY = cos(totalElapsedTimeSec * 0.85f) * 0.045f

        val blendedOffsetX = (offsetX + ambientFloatX).coerceIn(-1.5f, 1.5f)
        val blendedOffsetY = (offsetY + ambientFloatY).coerceIn(-1.5f, 1.5f)

        // 1. Draw 4D Holographic Depth Map Wallpaper Plane
        if (baseTextureId != 0 && depthMapId != 0) {
            depthShader.draw(
                baseTextureId = baseTextureId,
                depthMapId = depthMapId,
                offsetX = blendedOffsetX,
                offsetY = blendedOffsetY,
                depthFactor = depthIntensity,
                touchX = touchX,
                touchY = touchY,
                touchTime = touchTime,
                elapsedTimeSec = totalElapsedTimeSec,
                flareIntensity = flareIntensity,
                holoIntensity = holoIntensity
            )
        }

        // 2. Draw Multi-Theme Particle Engine
        if (isParticlesEnabled) {
            particleSystem.draw(
                offsetX = blendedOffsetX,
                offsetY = blendedOffsetY,
                elapsedTimeSec = totalElapsedTimeSec,
                touchX = touchX,
                touchY = touchY,
                touchTime = touchTime,
                colorR = particleColorR,
                colorG = particleColorG,
                colorB = particleColorB
            )
        }

        // 3. Draw Dynamic Weather Atmospheric Overlay (Rain / Snow / Clouds / Lightning)
        if (weatherOverlaySystem.isEnabled) {
            weatherOverlaySystem.draw(
                offsetX = blendedOffsetX,
                offsetY = blendedOffsetY,
                elapsedTimeSec = totalElapsedTimeSec,
                deltaSec = deltaSec
            )
        }
    }

    var isWeatherOverlayEnabled: Boolean
        get() = weatherOverlaySystem.isEnabled
        set(value) { weatherOverlaySystem.isEnabled = value }

    var weatherCondition: WeatherCondition
        get() = weatherOverlaySystem.currentCondition
        set(value) { weatherOverlaySystem.currentCondition = value }

    private fun uploadPendingTextures() {
        var base: Bitmap? = null
        var depth: Bitmap? = null

        synchronized(this) {
            if (pendingBaseBitmap != null) {
                base = pendingBaseBitmap
                depth = pendingDepthBitmap
                pendingBaseBitmap = null
                pendingDepthBitmap = null
            }
        }

        base?.let {
            if (baseTextureId != 0) {
                GLES20.glDeleteTextures(1, intArrayOf(baseTextureId), 0)
            }
            baseTextureId = GLTextureHelper.loadTexture(it)
        }

        depth?.let {
            if (depthMapId != 0) {
                GLES20.glDeleteTextures(1, intArrayOf(depthMapId), 0)
            }
            depthMapId = GLTextureHelper.loadTexture(it)
        }
    }
}
