package com.parallax.wallpaper.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RadialGradient
import android.graphics.Shader
import android.os.Build
import android.service.wallpaper.WallpaperService
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.SurfaceHolder
import com.parallax.wallpaper.gl.GLWallpaperSurfaceView
import com.parallax.wallpaper.sensor.ParallaxSensorManager
import com.parallax.wallpaper.utils.HapticHelper
import com.parallax.wallpaper.utils.LiveWallpaperManager
import com.parallax.wallpaper.weather.WeatherSyncManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ParallaxWallpaperService : WallpaperService() {

    override fun onCreateEngine(): Engine {
        return OpenGLEngine()
    }

    private inner class OpenGLEngine : Engine() {

        private var glSurfaceView: GLWallpaperSurfaceView? = null
        private lateinit var sensorManager: ParallaxSensorManager
        private var sensorJob: Job? = null
        private val scope = CoroutineScope(Dispatchers.Main)

        private var currentLoadedTimestamp: Long = 0L
        private var reloadReceiver: BroadcastReceiver? = null
        private var gestureDetector: GestureDetector? = null
        private var lastDoubleTapTime: Long = 0L

        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            setTouchEventsEnabled(true)
            sensorManager = ParallaxSensorManager(applicationContext)

            glSurfaceView = GLWallpaperSurfaceView(
                context = applicationContext,
                customHolder = surfaceHolder
            )

            initGestureDetector()

            // Register dynamic broadcast receiver for immediate wallpaper switch & sensitivity adjustments
            registerLiveWallpaperReceiver()

            // Load user-selected active wallpaper (or cosmic fallback if first launch)
            loadActiveWallpaper()

            // Initialize dynamic weather overlay state
            updateWeatherOverlayState()
        }

        private fun updateWeatherOverlayState() {
            val isOverlay = WeatherSyncManager.isOverlayEnabled(applicationContext)
            val condition = WeatherSyncManager.getActiveCondition(applicationContext)
            glSurfaceView?.setWeatherOverlayEnabled(isOverlay)
            glSurfaceView?.setWeatherCondition(condition)
        }

        private fun initGestureDetector() {
            gestureDetector = GestureDetector(applicationContext, object : GestureDetector.SimpleOnGestureListener() {
                override fun onDown(e: MotionEvent): Boolean = true

                override fun onDoubleTap(e: MotionEvent): Boolean {
                    if (!LiveWallpaperManager.isDoubleTapEnabled(applicationContext)) {
                        return false
                    }

                    val now = System.currentTimeMillis()
                    if (now - lastDoubleTapTime < 1200L) {
                        return true // Cooldown debounce
                    }
                    lastDoubleTapTime = now

                    // 1. Tactile haptic feedback
                    HapticHelper.doubleTapSuccess(applicationContext)

                    // 2. Asynchronously cycle to next wallpaper
                    scope.launch(Dispatchers.IO) {
                        LiveWallpaperManager.cycleToNextWallpaper(applicationContext)
                    }

                    return true
                }
            })
        }

        private fun registerLiveWallpaperReceiver() {
            try {
                val filter = IntentFilter().apply {
                    addAction(LiveWallpaperManager.ACTION_RELOAD_WALLPAPER)
                    addAction(LiveWallpaperManager.ACTION_UPDATE_SENSITIVITY)
                    addAction(WeatherSyncManager.ACTION_UPDATE_WEATHER)
                    addAction(Intent.ACTION_SCREEN_OFF)
                    addAction(Intent.ACTION_SCREEN_ON)
                    addAction(Intent.ACTION_USER_PRESENT)
                }

                val receiver = object : BroadcastReceiver() {
                    override fun onReceive(context: Context?, intent: Intent?) {
                        when (intent?.action) {
                            LiveWallpaperManager.ACTION_RELOAD_WALLPAPER -> {
                                loadActiveWallpaper()
                            }
                            LiveWallpaperManager.ACTION_UPDATE_SENSITIVITY -> {
                                val sensitivity = LiveWallpaperManager.getSensitivity(applicationContext)
                                glSurfaceView?.setDepthIntensity(0.09f * sensitivity)
                            }
                            WeatherSyncManager.ACTION_UPDATE_WEATHER -> {
                                updateWeatherOverlayState()
                            }
                            Intent.ACTION_SCREEN_OFF -> {
                                // Zero battery drain when screen is off
                                sensorJob?.cancel()
                                sensorManager.stopListening()
                                glSurfaceView?.onPause()
                            }
                            Intent.ACTION_SCREEN_ON, Intent.ACTION_USER_PRESENT -> {
                                if (isVisible) {
                                    glSurfaceView?.onResume()
                                    sensorManager.startListening()
                                }
                            }
                        }
                    }
                }
                reloadReceiver = receiver

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    registerReceiver(receiver, filter, Context.RECEIVER_NOT_EXPORTED)
                } else {
                    registerReceiver(receiver, filter)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        private fun loadActiveWallpaper() {
            scope.launch(Dispatchers.IO) {
                val timestamp = LiveWallpaperManager.getSavedTimestamp(applicationContext)
                val sensitivity = LiveWallpaperManager.getSensitivity(applicationContext)
                val (baseBitmap, depthBitmap) = LiveWallpaperManager.getActiveWallpaperBitmap(applicationContext)

                withContext(Dispatchers.Main) {
                    glSurfaceView?.let { view ->
                        if (baseBitmap != null) {
                            view.setBitmap(baseBitmap, depthBitmap)
                        } else {
                            view.setBitmap(createCosmicBaseBitmap())
                        }
                        view.setDepthIntensity(0.09f * sensitivity)
                        currentLoadedTimestamp = timestamp
                    }
                }
            }
        }

        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            if (visible) {
                glSurfaceView?.onResume()
                sensorManager.startListening()

                // Check if user changed wallpaper while screen was off or app was backgrounded
                val latestTimestamp = LiveWallpaperManager.getSavedTimestamp(applicationContext)
                if (latestTimestamp > 0L && latestTimestamp != currentLoadedTimestamp) {
                    loadActiveWallpaper()
                }

                sensorJob = scope.launch {
                    sensorManager.parallaxOffset.collect { offset ->
                        glSurfaceView?.setSensorOffset(offset.x, offset.y)
                    }
                }

                // Stillness collection for zero battery waste
                scope.launch {
                    sensorManager.isMoving.collect { moving ->
                        glSurfaceView?.setIsDeviceMoving(moving)
                    }
                }
            } else {
                sensorJob?.cancel()
                sensorManager.stopListening()
                glSurfaceView?.onPause() // 0% battery consumption when not visible
            }
        }

        override fun onTouchEvent(event: MotionEvent?) {
            super.onTouchEvent(event)
            event?.let {
                gestureDetector?.onTouchEvent(it)
                glSurfaceView?.onTouchEvent(it)
            }
        }

        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            cleanup()
        }

        override fun onDestroy() {
            super.onDestroy()
            cleanup()
        }

        private fun cleanup() {
            try {
                reloadReceiver?.let {
                    unregisterReceiver(it)
                    reloadReceiver = null
                }
            } catch (e: Exception) {
                // Already unregistered
            }
            sensorJob?.cancel()
            sensorManager.stopListening()
            glSurfaceView?.onPause()
            glSurfaceView = null
            scope.cancel()
        }

        /**
         * Generates a stunning 4K Cyber-Space texture as default fallback
         */
        private fun createCosmicBaseBitmap(): Bitmap {
            val width = 1080
            val height = 2400
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    intArrayOf(
                        Color.parseColor("#06070E"),
                        Color.parseColor("#150D2B"),
                        Color.parseColor("#04091A")
                    ),
                    null,
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    width * 0.5f, height * 0.44f,
                    width * 0.65f,
                    intArrayOf(
                        Color.parseColor("#807C4DFF"),
                        Color.parseColor("#4000E5FF"),
                        Color.TRANSPARENT
                    ),
                    floatArrayOf(0f, 0.55f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(width * 0.5f, height * 0.44f, width * 0.65f, glowPaint)

            val planetPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = RadialGradient(
                    width * 0.42f, height * 0.40f,
                    width * 0.32f,
                    intArrayOf(
                        Color.parseColor("#00E5FF"),
                        Color.parseColor("#651FFF"),
                        Color.parseColor("#08091B")
                    ),
                    floatArrayOf(0f, 0.65f, 1f),
                    Shader.TileMode.CLAMP
                )
            }
            canvas.drawCircle(width * 0.5f, height * 0.45f, width * 0.28f, planetPaint)

            return bitmap
        }
    }
}
