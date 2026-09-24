package com.parallax.wallpaper.aod

import android.os.Build
import android.os.Bundle
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Fullscreen Always-On Display (AOD) Activity that runs on pure AMOLED black (#000000)
 * when locked or tested, with double-tap to wake gesture.
 */
class AODActivity : ComponentActivity() {

    private lateinit var aodManager: AODManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        aodManager = AODManager.getInstance(this)

        setupLockScreenFlags()
        hideSystemBars()
        applyBrightness()

        val clockId = intent.getStringExtra("clock_id") ?: intent.getStringExtra("EXTRA_CLOCK_ID")
        if (!clockId.isNullOrBlank()) {
            lifecycleScope.launch {
                val aodRepo = com.parallax.wallpaper.data.AODRepository()
                val clockItem = aodRepo.getClockById(clockId)
                if (clockItem != null) {
                    val clockType = try {
                        AODClockType.valueOf(clockItem.clockType.uppercase())
                    } catch (_: Exception) {
                        AODClockType.CYBERPUNK_DIGITAL
                    }
                    val face = AODClockFace(
                        id = clockItem.id,
                        title = clockItem.title,
                        clockType = clockType,
                        accentColorHex = clockItem.accentColor,
                        glowColorHex = clockItem.glowColor,
                        textColorHex = clockItem.textColor,
                        backgroundColorHex = clockItem.backgroundColor,
                        dialStyle = clockItem.dialStyle,
                        hasBatteryWidget = clockItem.hasBatteryWidget,
                        hasDateWidget = clockItem.hasDateWidget,
                        hasStepsWidget = clockItem.hasStepsWidget,
                        hasWeatherWidget = clockItem.hasWeatherWidget,
                        downloads = clockItem.downloads
                    )
                    aodManager.setActiveClock(face)
                    android.widget.Toast.makeText(this@AODActivity, "📲 Live Test: AOD (${clockItem.title})", android.widget.Toast.LENGTH_SHORT).show()
                }
            }
        }

        setContent {
            val clock by aodManager.activeClock.collectAsState()
            val telemetry by aodManager.telemetry.collectAsState()
            val burnInOffset by aodManager.burnInOffset.collectAsState()
            val showBattery by aodManager.showBattery.collectAsState()
            val showDate by aodManager.showDate.collectAsState()
            val showSteps by aodManager.showSteps.collectAsState()
            val showWeather by aodManager.showWeather.collectAsState()

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onDoubleTap = {
                                // Double tap wakes / dismisses AOD
                                finish()
                            }
                        )
                    }
            ) {
                AODDisplayContent(
                    clock = clock,
                    telemetry = telemetry,
                    burnInOffset = burnInOffset,
                    showBattery = showBattery,
                    showDate = showDate,
                    showSteps = showSteps,
                    showWeather = showWeather
                )
            }
        }
    }

    private fun setupLockScreenFlags() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    private fun hideSystemBars() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        val controller = WindowCompat.getInsetsController(window, window.decorView)
        controller.hide(WindowInsetsCompat.Type.systemBars())
        controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }

    private fun applyBrightness() {
        val brightness = aodManager.brightness.value
        val lp = window.attributes
        lp.screenBrightness = brightness
        window.attributes = lp
    }
}
