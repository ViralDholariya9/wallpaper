package com.parallax.wallpaper.island

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.ComposeView
import androidx.core.app.NotificationCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.parallax.wallpaper.MainActivity
import com.parallax.wallpaper.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class DynamicIslandOverlayService : Service(), LifecycleOwner, ViewModelStoreOwner, SavedStateRegistryOwner {

    companion object {
        const val ACTION_START = "com.parallax.wallpaper.island.ACTION_START"
        const val ACTION_STOP = "com.parallax.wallpaper.island.ACTION_STOP"
        private const val NOTIFICATION_ID = 20262
        private const val CHANNEL_ID = "rewall_dynamic_island_channel"
    }

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val savedStateRegistryController = SavedStateRegistryController.create(this)
    private val store = ViewModelStore()

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private var windowManager: WindowManager? = null
    private var composeView: ComposeView? = null
    private var isOverlayAttached = false
    private val scope = CoroutineScope(Dispatchers.Main)
    private var offsetCollectorJob: Job? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(Bundle())
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

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

        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        startForeground(NOTIFICATION_ID, buildForegroundNotification())
        showOverlay()
        return START_STICKY
    }

    private fun showOverlay() {
        if (!DynamicIslandManager.canDrawOverlays(this)) {
            stopSelf()
            return
        }

        if (isOverlayAttached && composeView != null) {
            updateWindowPosition()
            return
        }

        try {
            val layoutFlag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }

            val density = resources.displayMetrics.density
            val xPx = (DynamicIslandManager.xOffsetDp.value * density).toInt()
            val yPx = (DynamicIslandManager.yOffsetDp.value * density).toInt()

            val params = WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                PixelFormat.TRANSLUCENT
            ).apply {
                gravity = Gravity.TOP or Gravity.CENTER_HORIZONTAL
                x = xPx
                y = yPx
            }

            val view = ComposeView(this).apply {
                setViewTreeLifecycleOwner(this@DynamicIslandOverlayService)
                setViewTreeSavedStateRegistryOwner(this@DynamicIslandOverlayService)
                setViewTreeViewModelStoreOwner(this@DynamicIslandOverlayService)

                setContent {
                    val theme by DynamicIslandManager.activeTheme.collectAsState()
                    val event by DynamicIslandManager.currentEvent.collectAsState()

                    DynamicIslandCapsule(
                        theme = theme,
                        event = event,
                        onCapsuleClick = {
                            DynamicIslandManager.toggleCompactExpanded()
                        }
                    )
                }
            }

            windowManager?.addView(view, params)
            composeView = view
            isOverlayAttached = true

            // Listen for offset calibration changes to smoothly adjust overlay position
            offsetCollectorJob?.cancel()
            offsetCollectorJob = scope.launch {
                DynamicIslandManager.xOffsetDp.collect {
                    updateWindowPosition()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun updateWindowPosition() {
        if (!isOverlayAttached || composeView == null) return
        try {
            val density = resources.displayMetrics.density
            val xPx = (DynamicIslandManager.xOffsetDp.value * density).toInt()
            val yPx = (DynamicIslandManager.yOffsetDp.value * density).toInt()

            val params = composeView?.layoutParams as? WindowManager.LayoutParams ?: return
            params.x = xPx
            params.y = yPx
            windowManager?.updateViewLayout(composeView, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun stopOverlay() {
        offsetCollectorJob?.cancel()
        if (isOverlayAttached && composeView != null) {
            try {
                windowManager?.removeView(composeView)
            } catch (e: Exception) {
                e.printStackTrace()
            }
            composeView = null
            isOverlayAttached = false
        }
    }

    override fun onDestroy() {
        stopOverlay()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_STOP)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Dynamic Island Service",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Runs the smart camera punch-hole capsule overlay"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun buildForegroundNotification(): android.app.Notification {
        val launchIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Dynamic Island Active")
            .setContentText("Smart punch-hole notification capsule is running")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }
}
