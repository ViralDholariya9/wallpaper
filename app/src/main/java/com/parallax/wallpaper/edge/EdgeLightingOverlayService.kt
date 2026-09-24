package com.parallax.wallpaper.edge

import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PixelFormat
import android.graphics.RectF
import android.graphics.SweepGradient
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.view.animation.LinearInterpolator
import androidx.core.app.NotificationCompat
import com.parallax.wallpaper.MainActivity
import com.parallax.wallpaper.R
import kotlin.math.max

class EdgeLightingOverlayService : Service() {

    companion object {
        const val ACTION_START = "com.parallax.wallpaper.edge.ACTION_START"
        const val ACTION_STOP = "com.parallax.wallpaper.edge.ACTION_STOP"
        private const val NOTIFICATION_ID = 20261
        private const val CHANNEL_ID = "rewall_edge_lighting_channel"
    }

    private var windowManager: WindowManager? = null
    private var overlayView: EdgeGlowOverlayView? = null
    private var isOverlayAttached = false

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(Context.WINDOW_SERVICE) as? WindowManager
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action ?: ACTION_START

        if (action == ACTION_STOP) {
            stopOverlay()
            stopSelf()
            return START_NOT_STICKY
        }

        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        showOverlay()
        return START_STICKY
    }

    private fun showOverlay() {
        if (!EdgeLightingManager.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        if (isOverlayAttached && overlayView != null) {
            overlayView?.refreshSettings()
            return
        }

        try {
            val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.START
            }

            val view = EdgeGlowOverlayView(this)
            windowManager?.addView(view, params)
            overlayView = view
            isOverlayAttached = true
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopOverlay() {
        if (isOverlayAttached && overlayView != null) {
            try {
                overlayView?.stopAnimation()
                windowManager?.removeView(overlayView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            overlayView = null
            isOverlayAttached = false
        }
    }

    override fun onDestroy() {
        stopOverlay()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Edge Lighting Active",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Displays RGB edge border lights on screen"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("🌈 Edge Lighting Active")
            .setContentText("ReWall 3D screen border illumination is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    /**
     * Highly optimized, battery-efficient 60FPS Surface/View for drawing smooth SweepGradient
     * edge borders with hardware acceleration and zero external dependencies.
     */
    @SuppressLint("ViewConstructor")
    class EdgeGlowOverlayView(context: Context) : View(context) {

        private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.STROKE
        }
        private val rectF = RectF()
        private var animator: ValueAnimator? = null
        private var currentRotation = 0f
        private var colorArray: IntArray = intArrayOf(0xFFFF0055.toInt(), 0xFF00E5FF.toInt(), 0xFF7000FF.toInt(), 0xFFFF0055.toInt())

        init {
            refreshSettings()
            startAnimation()
        }

        fun refreshSettings() {
            val preset = EdgeLightingManager.activePreset.value
            val colors = preset.colors

            colorArray = if (colors.size >= 2) {
                val parsed = colors.map { parseColorSafe(it) }
                (parsed + parsed.first()).toIntArray()
            } else {
                intArrayOf(0xFFFF0055.toInt(), 0xFF00E5FF.toInt(), 0xFF7000FF.toInt(), 0xFFFF0055.toInt())
            }

            val thicknessDp = EdgeLightingManager.borderThicknessDp.value.toFloat()
            val density = resources.displayMetrics.density
            paint.strokeWidth = thicknessDp * density

            postInvalidate()
        }

        fun startAnimation() {
            animator?.cancel()
            val speed = max(0.3f, EdgeLightingManager.speedMultiplier.value)
            val duration = (3000 / speed).toLong()

            animator = ValueAnimator.ofFloat(0f, 360f).apply {
                this.duration = duration
                repeatCount = ValueAnimator.INFINITE
                interpolator = LinearInterpolator()
                addUpdateListener {
                    currentRotation = it.animatedValue as Float
                    postInvalidateOnAnimation()
                }
                start()
            }
        }

        fun stopAnimation() {
            animator?.cancel()
            animator = null
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val w = width.toFloat()
            val h = height.toFloat()
            if (w <= 0 || h <= 0) return

            val halfStroke = paint.strokeWidth / 2f
            rectF.set(halfStroke, halfStroke, w - halfStroke, h - halfStroke)

            val density = resources.displayMetrics.density
            val cornerRadiusPx = EdgeLightingManager.cornerRadiusDp.value.toFloat() * density

            val centerX = w / 2f
            val centerY = h / 2f

            canvas.save()
            canvas.rotate(currentRotation, centerX, centerY)

            paint.shader = SweepGradient(centerX, centerY, colorArray, null)
            canvas.restore()

            canvas.drawRoundRect(rectF, cornerRadiusPx, cornerRadiusPx, paint)
        }

        private fun parseColorSafe(hex: String): Int {
            return try {
                val clean = hex.trim().removePrefix("#")
                if (clean.length == 6) {
                    0xFF000000.toInt() or clean.toLong(16).toInt()
                } else if (clean.length == 8) {
                    clean.toLong(16).toInt()
                } else {
                    0xFF00E5FF.toInt()
                }
            } catch (_: Exception) {
                0xFF00E5FF.toInt()
            }
        }
    }
}
