package com.parallax.wallpaper.charging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.parallax.wallpaper.data.api.ChargingAnimationItem
import kotlinx.coroutines.delay

class ChargingActivity : ComponentActivity() {

    private var mediaPlayer: MediaPlayer? = null

    private val powerDisconnectReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            finishAndRemoveTask()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Configure window to awaken and show on lock screen
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

        ChargingManager.init(this)
        val activeAnim = ChargingManager.getActiveAnimation(this)

        // Optional plug-in sound playback
        if (ChargingManager.isSoundEnabled.value && !activeAnim.soundUrl.isNullOrBlank()) {
            playChargingSound(ChargingManager.resolveUrl(activeAnim.soundUrl))
        }

        // Register power disconnect receivers
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(ChargingReceiver.ACTION_CHARGER_DISCONNECTED_LOCAL)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(powerDisconnectReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(powerDisconnectReceiver, filter)
        }

        setContent {
            ChargingFullScreenContent(
                activeAnimation = activeAnim,
                onDismiss = { finishAndRemoveTask() }
            )
        }
    }

    private fun playChargingSound(soundUrl: String) {
        try {
            mediaPlayer = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                        .build()
                )
                setDataSource(soundUrl)
                setOnPreparedListener { start() }
                setOnCompletionListener {
                    release()
                    mediaPlayer = null
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(powerDisconnectReceiver)
        } catch (e: Exception) {
            // Ignored if already unregistered
        }
        mediaPlayer?.release()
        mediaPlayer = null
    }
}

@Composable
fun ChargingFullScreenContent(
    activeAnimation: ChargingAnimationItem,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var batteryPercent by remember { mutableIntStateOf(85) }
    var batteryTemp by remember { mutableFloatStateOf(32.5f) }
    var batteryVoltage by remember { mutableFloatStateOf(4.1f) }
    var chargingStatus by remember { mutableStateOf("SUPER DART CHARGE 66W") }

    val durationSeconds by ChargingManager.durationSeconds.collectAsState()

    // Auto-dismiss timer if configured (> 0)
    LaunchedEffect(durationSeconds) {
        if (durationSeconds > 0) {
            delay(durationSeconds * 1000L)
            onDismiss()
        }
    }

    // Live Battery Manager Listener
    DisposableEffect(Unit) {
        val batteryReceiver = object : BroadcastReceiver() {
            override fun onReceive(c: Context?, intent: Intent?) {
                intent?.let {
                    val level = it.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
                    val scale = it.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
                    if (level >= 0 && scale > 0) {
                        batteryPercent = (level * 100) / scale
                    }
                    val temp = it.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0)
                    if (temp > 0) {
                        batteryTemp = temp / 10.0f
                    }
                    val volt = it.getIntExtra(BatteryManager.EXTRA_VOLTAGE, 0)
                    if (volt > 0) {
                        batteryVoltage = volt / 1000.0f
                    }
                    val plugged = it.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0)
                    chargingStatus = when (plugged) {
                        BatteryManager.BATTERY_PLUGGED_AC -> "ULTRA FAST CHARGE 66W"
                        BatteryManager.BATTERY_PLUGGED_WIRELESS -> "FAST WIRELESS CHARGE"
                        else -> "USB CHARGING ACTIVE"
                    }
                }
            }
        }
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        context.registerReceiver(batteryReceiver, intentFilter)
        onDispose {
            try {
                context.unregisterReceiver(batteryReceiver)
            } catch (e: Exception) {}
        }
    }

    val glowColor = remember(activeAnimation.textColor) {
        try {
            Color(android.graphics.Color.parseColor(activeAnimation.textColor))
        } catch (e: Exception) {
            Color(0xFF00E5FF)
        }
    }

    // Pulsing animations for charging effect
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glow"
    )

    // Lottie Composition
    val animResolvedUrl = remember(activeAnimation.animationUrl) {
        ChargingManager.resolveUrl(activeAnimation.animationUrl)
    }
    val composition by rememberLottieComposition(
        LottieCompositionSpec.Url(animResolvedUrl)
    )
    val lottieProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = LottieConstants.IterateForever,
        speed = 1.0f
    )

    // Double-tap or Swipe-up to dismiss
    var lastTapTime by remember { mutableStateOf(0L) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                val now = System.currentTimeMillis()
                if (now - lastTapTime < 400) {
                    onDismiss()
                }
                lastTapTime = now
            }
            .pointerInput(Unit) {
                detectVerticalDragGestures { _, dragAmount ->
                    if (dragAmount < -50) { // Swipe up
                        onDismiss()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Ambient background neon glow radial gradient
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor.copy(alpha = 0.22f), Color.Transparent),
                    radius = size.minDimension * 0.7f
                )
            )
        }

        // Close Button top-end
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 44.dp, end = 20.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(40.dp)
                    .background(Color.White.copy(alpha = 0.12f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = Color.White
                )
            }
        }

        // Center Content: Animation + Battery HUD
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
        ) {
            // Lottie Animation Container
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                if (composition != null) {
                    LottieAnimation(
                        composition = composition,
                        progress = { lottieProgress },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Fallback animated glowing energy ring if loading
                    Canvas(modifier = Modifier.size(200.dp)) {
                        drawCircle(
                            color = glowColor.copy(alpha = 0.3f),
                            radius = size.minDimension / 2f,
                            style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Dynamic Charging Ring HUD
            Box(
                modifier = Modifier.size(150.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeW = 8.dp.toPx()
                    // Background track
                    drawCircle(
                        color = Color.White.copy(alpha = 0.08f),
                        radius = (size.minDimension - strokeW) / 2f,
                        style = Stroke(width = strokeW)
                    )
                    // Progress arc
                    val sweep = (batteryPercent / 100f) * 360f
                    drawArc(
                        color = glowColor,
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeW, cap = StrokeCap.Round)
                    )
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = "⚡",
                        fontSize = 24.sp,
                        color = glowColor,
                        modifier = Modifier.alpha(glowAlpha)
                    )
                    Text(
                        text = "$batteryPercent%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = (-0.5).sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Charging Mode Title
            Text(
                text = chargingStatus,
                fontSize = 13.sp,
                fontWeight = FontWeight.ExtraBold,
                color = glowColor,
                letterSpacing = 2.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Live telemetry specs: Wattage, Temp, Voltage
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "⚡ ${String.format("%.1f", 66.0)}W",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(text = "•", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                Text(
                    text = "🌡️ ${batteryTemp}°C",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.7f)
                )
                Text(text = "•", color = Color.White.copy(alpha = 0.4f), fontSize = 12.sp)
                Text(
                    text = "🔋 ${batteryVoltage}V",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Gesture hint
            Text(
                text = "Double tap or swipe up to unlock",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.35f),
                letterSpacing = 0.5.sp
            )
        }
    }
}
