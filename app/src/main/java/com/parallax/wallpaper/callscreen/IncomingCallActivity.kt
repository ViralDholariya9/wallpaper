package com.parallax.wallpaper.callscreen

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

/**
 * Fullscreen Incoming Call Activity displayed over lockscreen with
 * flashlight strobe and animated 3D call theme.
 */
class IncomingCallActivity : ComponentActivity() {

    private var vibrator: Vibrator? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Turn screen on and show over keyguard/lock screen
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                        WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                        WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
            )
        }
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? android.os.VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }

        val manager = CallScreenManager.getInstance(this)
        manager.startFlashlightStrobe()
        triggerCallVibration()

        val themeId = intent.getStringExtra("theme_id") ?: intent.getStringExtra("EXTRA_THEME_ID")
        if (!themeId.isNullOrBlank()) {
            lifecycleScope.launch {
                val callRepo = com.parallax.wallpaper.data.CallScreenRepository()
                val themeItem = callRepo.getThemeById(themeId)
                if (themeItem != null) {
                    manager.setActiveTheme(themeItem)
                    Toast.makeText(this@IncomingCallActivity, "📲 Live Test: Call Screen (${themeItem.title})", Toast.LENGTH_SHORT).show()
                }
            }
        }

        val callerName = intent.getStringExtra(EXTRA_CALLER_NAME) ?: "SARAH CONNOR"
        val callerNumber = intent.getStringExtra(EXTRA_CALLER_NUMBER) ?: "+1 (555) 019-2834"
        val profile = CallerProfile(name = callerName, number = callerNumber)

        setContent {
            val theme by manager.activeTheme.collectAsState()
            val buttonStyle by manager.buttonStyle.collectAsState()
            var isRinging by remember { mutableStateOf(true) }

            CallScreenContent(
                theme = theme,
                callerProfile = profile,
                buttonStyle = buttonStyle,
                isRinging = isRinging,
                onAcceptCall = {
                    isRinging = false
                    manager.stopFlashlightStrobe()
                    vibrator?.cancel()
                    Toast.makeText(this, "📞 Call Connected", Toast.LENGTH_SHORT).show()
                },
                onDeclineCall = {
                    manager.stopFlashlightStrobe()
                    vibrator?.cancel()
                    Toast.makeText(this, "Call Ended", Toast.LENGTH_SHORT).show()
                    finish()
                },
                modifier = Modifier.fillMaxSize()
            )
        }
    }

    private fun triggerCallVibration() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val timings = longArrayOf(0, 400, 200, 400, 1000)
                vibrator?.vibrate(VibrationEffect.createWaveform(timings, 0))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(longArrayOf(0, 400, 200, 400, 1000), 0)
            }
        } catch (_: Exception) {}
    }

    override fun onDestroy() {
        super.onDestroy()
        CallScreenManager.getInstance(this).stopFlashlightStrobe()
        try { vibrator?.cancel() } catch (_: Exception) {}
    }

    companion object {
        const val EXTRA_CALLER_NAME = "extra_caller_name"
        const val EXTRA_CALLER_NUMBER = "extra_caller_number"
    }
}
