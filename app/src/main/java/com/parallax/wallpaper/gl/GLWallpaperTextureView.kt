package com.parallax.wallpaper.gl

import android.content.Context
import android.graphics.Bitmap
import android.graphics.SurfaceTexture
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.TextureView
import javax.microedition.khronos.egl.EGL10
import javax.microedition.khronos.egl.EGLConfig
import javax.microedition.khronos.egl.EGLContext
import javax.microedition.khronos.egl.EGLDisplay
import javax.microedition.khronos.egl.EGLSurface

/**
 * Hardware-Accelerated OpenGL ES 2.0 TextureView specifically optimized for Jetpack Compose.
 * Unlike GLSurfaceView, TextureView participates directly in Compose hardware drawing passes,
 * eliminating black-surface punch-through bugs, z-order fighting, and Compose overlay clipping.
 */
class GLWallpaperTextureView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : TextureView(context, attrs), TextureView.SurfaceTextureListener {

    val renderer = GLParallaxRenderer()
    private var renderThread: RenderThread? = null

    init {
        isOpaque = false // Allows transparency so underlying base image shows seamlessly
        surfaceTextureListener = this
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        stopRenderThread()
        val optimizer = com.parallax.wallpaper.power.BatteryOptimizer.getInstance(context)
        val thread = RenderThread(surface, width, height, renderer, optimizer)
        renderThread = thread
        thread.start()
    }

    fun setIsDeviceMoving(moving: Boolean) {
        renderThread?.setIsMoving(moving)
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {
        renderThread?.onSizeChanged(width, height)
    }

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
        stopRenderThread()
        return true
    }

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
        // Frame available notification
    }

    private fun stopRenderThread() {
        renderThread?.let {
            it.requestStop()
            try {
                it.join(800)
            } catch (e: InterruptedException) {
                Thread.currentThread().interrupt()
            }
        }
        renderThread = null
    }

    private var baseSensorX = 0f
    private var baseSensorY = 0f
    private var touchDragX = 0f
    private var touchDragY = 0f
    private var isAtEdgeBoundary = false

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

    fun setParticlesEnabled(enabled: Boolean) {
        renderer.isParticlesEnabled = enabled
    }

    fun setWeatherCondition(condition: com.parallax.wallpaper.weather.WeatherCondition) {
        renderer.weatherCondition = condition
    }

    fun setWeatherOverlayEnabled(enabled: Boolean) {
        renderer.isWeatherOverlayEnabled = enabled
    }

    fun setParticleColor(r: Float, g: Float, b: Float) {
        renderer.setParticleColor(r, g, b)
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
                // Initialize touch drag offset relative to screen center
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
                // Elastic release: snap back to true device sensor tilt
                touchDragX = 0f
                touchDragY = 0f
                renderer.setSensorOffset(baseSensorX, baseSensorY)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    private class RenderThread(
        private val surfaceTexture: SurfaceTexture,
        @Volatile private var width: Int,
        @Volatile private var height: Int,
        private val renderer: GLParallaxRenderer,
        private val batteryOptimizer: com.parallax.wallpaper.power.BatteryOptimizer
    ) : Thread("GLWallpaperTextureRenderThread") {

        @Volatile
        private var isRunning = true
        @Volatile
        private var sizeChanged = true
        @Volatile
        private var isMoving = true

        fun setIsMoving(moving: Boolean) {
            isMoving = moving
        }

        fun onSizeChanged(newWidth: Int, newHeight: Int) {
            width = newWidth
            height = newHeight
            sizeChanged = true
        }

        fun requestStop() {
            isRunning = false
            interrupt()
        }

        override fun run() {
            val egl = EGLContext.getEGL() as EGL10
            val eglDisplay = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY)
            if (eglDisplay == EGL10.EGL_NO_DISPLAY) {
                Log.e("GLWallpaperTextureView", "eglGetDisplay failed")
                return
            }

            val version = IntArray(2)
            if (!egl.eglInitialize(eglDisplay, version)) {
                Log.e("GLWallpaperTextureView", "eglInitialize failed")
                return
            }

            // EGL Configuration: RGBA 8888, OpenGL ES 2.0 compatible
            val configAttribs = intArrayOf(
                EGL10.EGL_RENDERABLE_TYPE, 4, // EGL_OPENGL_ES2_BIT
                EGL10.EGL_RED_SIZE, 8,
                EGL10.EGL_GREEN_SIZE, 8,
                EGL10.EGL_BLUE_SIZE, 8,
                EGL10.EGL_ALPHA_SIZE, 8,
                EGL10.EGL_DEPTH_SIZE, 16,
                EGL10.EGL_NONE
            )

            val configs = arrayOfNulls<EGLConfig>(1)
            val numConfigs = IntArray(1)
            if (!egl.eglChooseConfig(eglDisplay, configAttribs, configs, 1, numConfigs) || numConfigs[0] == 0) {
                Log.e("GLWallpaperTextureView", "eglChooseConfig failed")
                return
            }
            val eglConfig = configs[0]

            val contextAttribs = intArrayOf(
                0x3098, 2, // EGL_CONTEXT_CLIENT_VERSION = 2
                EGL10.EGL_NONE
            )
            val eglContext = egl.eglCreateContext(eglDisplay, eglConfig, EGL10.EGL_NO_CONTEXT, contextAttribs)
            if (eglContext == EGL10.EGL_NO_CONTEXT) {
                Log.e("GLWallpaperTextureView", "eglCreateContext failed")
                return
            }

            val eglSurface = egl.eglCreateWindowSurface(eglDisplay, eglConfig, surfaceTexture, null)
            if (eglSurface == null || eglSurface == EGL10.EGL_NO_SURFACE) {
                Log.e("GLWallpaperTextureView", "eglCreateWindowSurface failed")
                egl.eglDestroyContext(eglDisplay, eglContext)
                return
            }

            if (!egl.eglMakeCurrent(eglDisplay, eglSurface, eglSurface, eglContext)) {
                Log.e("GLWallpaperTextureView", "eglMakeCurrent failed")
                egl.eglDestroySurface(eglDisplay, eglSurface)
                egl.eglDestroyContext(eglDisplay, eglContext)
                return
            }

            try {
                renderer.onSurfaceCreated(null, eglConfig)
                renderer.onSurfaceChanged(null, width, height)

                while (isRunning) {
                    if (sizeChanged) {
                        renderer.onSurfaceChanged(null, width, height)
                        sizeChanged = false
                    }

                    renderer.onDrawFrame(null)

                    if (!egl.eglSwapBuffers(eglDisplay, eglSurface)) {
                        val error = egl.eglGetError()
                        if (error == 0x300E || error != EGL10.EGL_SUCCESS) {
                            Log.w("GLWallpaperTextureView", "EGL swap error: $error")
                            break
                        }
                    }

                    // Zero-Battery Adaptive Frame Sleep (16ms moving -> 48ms still -> 100ms eco)
                    val sleepMs = batteryOptimizer.getAdaptiveFrameDelayMs(isMoving)
                    try {
                        sleep(sleepMs)
                    } catch (e: InterruptedException) {
                        break
                    }
                }
            } catch (e: Exception) {
                Log.e("GLWallpaperTextureView", "Render error", e)
            } finally {
                egl.eglMakeCurrent(eglDisplay, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_SURFACE, EGL10.EGL_NO_CONTEXT)
                egl.eglDestroySurface(eglDisplay, eglSurface)
                egl.eglDestroyContext(eglDisplay, eglContext)
                egl.eglTerminate(eglDisplay)
            }
        }
    }
}
