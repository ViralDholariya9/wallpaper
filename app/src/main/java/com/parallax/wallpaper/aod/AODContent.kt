package com.parallax.wallpaper.aod

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.cos
import kotlin.math.sin

/**
 * Main Composable rendering Always-On Display (AOD) screen on pure #000000 AMOLED background.
 */
@Composable
fun AODDisplayContent(
    clock: AODClockFace,
    telemetry: AODTelemetry,
    burnInOffset: AODBurnInOffset,
    showBattery: Boolean = true,
    showDate: Boolean = true,
    showSteps: Boolean = true,
    showWeather: Boolean = true,
    isMiniature: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Current time state updating every second
    var calendar by remember { mutableStateOf(Calendar.getInstance()) }

    LaunchedEffect(Unit) {
        while (true) {
            calendar = Calendar.getInstance()
            delay(1000L)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black), // Pure AMOLED Pitch Black - 0W
        contentAlignment = Alignment.Center
    ) {
        // Shift container using anti-burn-in offset coordinates
        Column(
            modifier = Modifier
                .offset(
                    x = (if (isMiniature) burnInOffset.xOffsetDp * 0.5f else burnInOffset.xOffsetDp).dp,
                    y = (if (isMiniature) burnInOffset.yOffsetDp * 0.5f else burnInOffset.yOffsetDp).dp
                )
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Render active Clock Style
            when (clock.clockType) {
                AODClockType.CYBERPUNK_DIGITAL -> {
                    CyberpunkDigitalClock(calendar = calendar, clock = clock, isMiniature = isMiniature)
                }
                AODClockType.MINIMALIST_ANALOG -> {
                    MinimalistAnalogClock(calendar = calendar, clock = clock, isMiniature = isMiniature)
                }
                AODClockType.TYPOGRAPHY_WORD -> {
                    TypographyWordClock(calendar = calendar, clock = clock, isMiniature = isMiniature)
                }
                AODClockType.NEON_ANIMAL -> {
                    NeonAnimalClock(calendar = calendar, clock = clock, isMiniature = isMiniature)
                }
                AODClockType.GAMING_HUD -> {
                    GamingHudClock(calendar = calendar, clock = clock, telemetry = telemetry, isMiniature = isMiniature)
                }
            }

            Spacer(modifier = Modifier.height(if (isMiniature) 14.dp else 24.dp))

            // Smart Widgets Row
            AODWidgetsRow(
                calendar = calendar,
                telemetry = telemetry,
                showBattery = showBattery,
                showDate = showDate,
                showSteps = showSteps,
                showWeather = showWeather,
                accentColor = clock.accentColor,
                isMiniature = isMiniature
            )

            Spacer(modifier = Modifier.height(if (isMiniature) 10.dp else 18.dp))

            // Notification Indicators
            Row(
                horizontalArrangement = Arrangement.spacedBy(if (isMiniature) 8.dp else 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "💬", fontSize = if (isMiniature) 11.sp else 13.sp, color = Color.White.copy(alpha = 0.7f))
                Text(text = "📞", fontSize = if (isMiniature) 11.sp else 13.sp, color = Color.White.copy(alpha = 0.7f))
                Text(text = "✉️", fontSize = if (isMiniature) 11.sp else 13.sp, color = Color.White.copy(alpha = 0.7f))
            }
        }

        // Bottom Double-Tap To Wake Hint
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = if (isMiniature) 12.dp else 32.dp)
        ) {
            Text(
                text = "•• Double-tap screen to wake ••",
                fontSize = if (isMiniature) 9.sp else 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.35f),
                letterSpacing = if (isMiniature) 0.5.sp else 1.sp
            )
        }
    }
}

// ========================================================
// 1. ⚡ 3D Cyberpunk Digital Clock Face
// ========================================================
@Composable
fun CyberpunkDigitalClock(
    calendar: Calendar,
    clock: AODClockFace,
    isMiniature: Boolean = false
) {
    val context = LocalContext.current
    val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
    val timePattern = if (is24Hour) "HH:mm" else "hh:mm"
    val timeFormat = SimpleDateFormat(timePattern, Locale.getDefault())
    val secFormat = SimpleDateFormat("ss", Locale.getDefault())
    val timeStr = timeFormat.format(calendar.time)
    val secStr = secFormat.format(calendar.time)

    Box(
        modifier = Modifier
            .padding(horizontal = if (isMiniature) 8.dp else 24.dp)
            .border(1.dp, clock.accentColor.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(14.dp))
            .padding(
                horizontal = if (isMiniature) 14.dp else 28.dp,
                vertical = if (isMiniature) 10.dp else 20.dp
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = timeStr,
                fontSize = if (isMiniature) 34.sp else 58.sp,
                fontWeight = FontWeight.Black,
                color = clock.textColor,
                letterSpacing = if (isMiniature) 1.sp else 2.sp,
                maxLines = 1,
                softWrap = false
            )
            Spacer(modifier = Modifier.height(if (isMiniature) 2.dp else 4.dp))
            Text(
                text = "SEC : $secStr // CYBER 2077",
                fontSize = if (isMiniature) 8.5.sp else 11.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Monospace,
                color = clock.accentColor,
                letterSpacing = if (isMiniature) 0.8.sp else 2.sp,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}

// ========================================================
// 2. ⌚ Zenith Minimalist Luxury Analog Clock Face
// ========================================================
@Composable
fun MinimalistAnalogClock(
    calendar: Calendar,
    clock: AODClockFace,
    isMiniature: Boolean = false
) {
    val hours = calendar.get(Calendar.HOUR)
    val minutes = calendar.get(Calendar.MINUTE)
    val seconds = calendar.get(Calendar.SECOND)

    val hourAngle = (hours + minutes / 60f) * 30f
    val minuteAngle = (minutes + seconds / 60f) * 6f
    val secondAngle = seconds * 6f

    val clockSize = if (isMiniature) 130.dp else 190.dp

    Box(
        modifier = Modifier.size(clockSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.minDimension / 2 - 8.dp.toPx()

            // Outer tick ring
            drawCircle(
                color = Color.White.copy(alpha = 0.1f),
                radius = radius,
                style = Stroke(width = (if (isMiniature) 1.dp else 1.5.dp).toPx())
            )

            // 12 Hour Ticks
            for (i in 0 until 12) {
                val angleRad = Math.toRadians((i * 30.0) - 90.0)
                val dotRadius = if (i % 3 == 0) (if (isMiniature) 2.5.dp else 3.5.dp).toPx() else (if (isMiniature) 1.2.dp else 1.8.dp).toPx()
                val dotColor = if (i % 3 == 0) clock.accentColor else Color.White.copy(alpha = 0.4f)
                val x = center.x + (radius - (if (isMiniature) 7.dp else 10.dp).toPx()) * cos(angleRad).toFloat()
                val y = center.y + (radius - (if (isMiniature) 7.dp else 10.dp).toPx()) * sin(angleRad).toFloat()
                drawCircle(color = dotColor, radius = dotRadius, center = Offset(x, y))
            }

            // Hour Hand
            val hourRad = Math.toRadians((hourAngle - 90).toDouble())
            val hourLen = radius * 0.52f
            drawLine(
                color = clock.textColor,
                start = center,
                end = Offset(
                    center.x + hourLen * cos(hourRad).toFloat(),
                    center.y + hourLen * sin(hourRad).toFloat()
                ),
                strokeWidth = (if (isMiniature) 3.dp else 4.5.dp).toPx(),
                cap = StrokeCap.Round
            )

            // Minute Hand
            val minRad = Math.toRadians((minuteAngle - 90).toDouble())
            val minLen = radius * 0.74f
            drawLine(
                color = clock.accentColor,
                start = center,
                end = Offset(
                    center.x + minLen * cos(minRad).toFloat(),
                    center.y + minLen * sin(minRad).toFloat()
                ),
                strokeWidth = (if (isMiniature) 2.dp else 3.dp).toPx(),
                cap = StrokeCap.Round
            )

            // Second Hand Needle
            val secRad = Math.toRadians((secondAngle - 90).toDouble())
            val secLen = radius * 0.82f
            drawLine(
                color = clock.glowColor,
                start = center,
                end = Offset(
                    center.x + secLen * cos(secRad).toFloat(),
                    center.y + secLen * sin(secRad).toFloat()
                ),
                strokeWidth = (if (isMiniature) 1.dp else 1.5.dp).toPx(),
                cap = StrokeCap.Round
            )

            // Center Pin
            drawCircle(color = Color.Black, radius = (if (isMiniature) 3.5.dp else 5.dp).toPx(), center = center)
            drawCircle(color = clock.accentColor, radius = (if (isMiniature) 1.8.dp else 2.5.dp).toPx(), center = center)
        }
    }
}

// ========================================================
// 3. 🔤 Matrix Typography Word Clock Face
// ========================================================
@Composable
fun TypographyWordClock(
    calendar: Calendar,
    clock: AODClockFace,
    isMiniature: Boolean = false
) {
    val hour = calendar.get(Calendar.HOUR)
    val hourWord = when (if (hour == 0) 12 else hour) {
        1 -> "ONE"
        2 -> "TWO"
        3 -> "THREE"
        4 -> "FOUR"
        5 -> "FIVE"
        6 -> "SIX"
        7 -> "SEVEN"
        8 -> "EIGHT"
        9 -> "NINE"
        10 -> "TEN"
        11 -> "ELEVEN"
        else -> "TWELVE"
    }

    val regularFontSize = if (isMiniature) 11.sp else 16.sp
    val heroFontSize = if (isMiniature) 14.sp else 20.sp
    val wordSpacing = if (isMiniature) 6.dp else 10.dp
    val rowSpacing = if (isMiniature) 3.dp else 6.dp

    Column(
        modifier = Modifier.padding(horizontal = if (isMiniature) 8.dp else 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(rowSpacing)
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(wordSpacing)) {
            Text(text = "IT", fontSize = regularFontSize, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = clock.accentColor)
            Text(text = "IS", fontSize = regularFontSize, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = clock.accentColor)
            Text(text = "HALF", fontSize = regularFontSize, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = Color.White.copy(alpha = 0.15f))
            Text(text = "A", fontSize = regularFontSize, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = Color.White.copy(alpha = 0.15f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(wordSpacing)) {
            Text(text = "QUARTER", fontSize = regularFontSize, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = Color.White.copy(alpha = 0.15f))
            Text(text = "TWENTY", fontSize = regularFontSize, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = Color.White.copy(alpha = 0.15f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(wordSpacing)) {
            Text(text = "TO", fontSize = regularFontSize, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = Color.White.copy(alpha = 0.15f))
            Text(text = "PAST", fontSize = regularFontSize, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = clock.accentColor)
            Text(text = "SIX", fontSize = regularFontSize, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = Color.White.copy(alpha = 0.15f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(wordSpacing)) {
            Text(text = hourWord, fontSize = heroFontSize, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = clock.textColor)
            Text(text = "ELEVEN", fontSize = regularFontSize, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = Color.White.copy(alpha = 0.15f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(wordSpacing)) {
            Text(text = "IN", fontSize = regularFontSize, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = Color.White.copy(alpha = 0.15f))
            Text(text = "THE", fontSize = regularFontSize, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = clock.accentColor)
            Text(text = "NIGHT", fontSize = regularFontSize, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace, color = clock.accentColor)
        }
    }
}

// ========================================================
// 4. 🦊 Neon Cyber Animal Glyph Clock Face
// ========================================================
@Composable
fun NeonAnimalClock(
    calendar: Calendar,
    clock: AODClockFace,
    isMiniature: Boolean = false
) {
    val context = LocalContext.current
    val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
    val timePattern = if (is24Hour) "HH:mm" else "hh:mm"
    val timeFormat = SimpleDateFormat(timePattern, Locale.getDefault())
    val timeStr = timeFormat.format(calendar.time)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = "🦊",
            fontSize = if (isMiniature) 32.sp else 48.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(if (isMiniature) 2.dp else 6.dp))
        Text(
            text = timeStr,
            fontSize = if (isMiniature) 30.sp else 48.sp,
            fontWeight = FontWeight.Black,
            color = clock.textColor,
            letterSpacing = if (isMiniature) 1.sp else 2.sp,
            maxLines = 1,
            softWrap = false
        )
        Spacer(modifier = Modifier.height(if (isMiniature) 2.dp else 4.dp))
        Text(
            text = "NEON KITSUNE • 84%",
            fontSize = if (isMiniature) 8.5.sp else 11.sp,
            fontWeight = FontWeight.Bold,
            color = clock.accentColor,
            letterSpacing = if (isMiniature) 0.8.sp else 2.sp,
            maxLines = 1,
            softWrap = false
        )
    }
}

// ========================================================
// 5. 🎮 Gamer HUD Stamina Dial Clock Face
// ========================================================
@Composable
fun GamingHudClock(
    calendar: Calendar,
    clock: AODClockFace,
    telemetry: AODTelemetry,
    isMiniature: Boolean = false
) {
    val context = LocalContext.current
    val is24Hour = android.text.format.DateFormat.is24HourFormat(context)
    val timePattern = if (is24Hour) "HH:mm" else "hh:mm"
    val timeFormat = SimpleDateFormat(timePattern, Locale.getDefault())
    val timeStr = timeFormat.format(calendar.time)

    val hudSize = if (isMiniature) 130.dp else 190.dp
    val barWidth = if (isMiniature) 56.dp else 90.dp

    Box(
        modifier = Modifier
            .size(hudSize)
            .border(if (isMiniature) 1.5.dp else 2.5.dp, Color.White.copy(alpha = 0.1f), CircleShape)
            .padding(if (isMiniature) 8.dp else 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "LVL 99 // MAX",
                fontSize = if (isMiniature) 7.5.sp else 10.sp,
                fontWeight = FontWeight.Black,
                fontFamily = FontFamily.Monospace,
                color = clock.accentColor
            )
            Spacer(modifier = Modifier.height(if (isMiniature) 1.dp else 2.dp))
            Text(
                text = timeStr,
                fontSize = if (isMiniature) 26.sp else 44.sp,
                fontWeight = FontWeight.Black,
                color = clock.textColor,
                maxLines = 1,
                softWrap = false
            )
            Spacer(modifier = Modifier.height(if (isMiniature) 3.dp else 6.dp))

            // HP Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(text = "HP", fontSize = if (isMiniature) 6.5.sp else 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), fontFamily = FontFamily.Monospace)
                Box(modifier = Modifier.width(barWidth).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.1f))) {
                    Box(modifier = Modifier.fillMaxWidth(0.85f).height(4.dp).background(Color(0xFFEF4444)))
                }
                Text(text = "${telemetry.batteryPercent}%", fontSize = if (isMiniature) 6.5.sp else 8.sp, fontWeight = FontWeight.Bold, color = Color(0xFFEF4444), fontFamily = FontFamily.Monospace)
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Stamina Bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(text = "STM", fontSize = if (isMiniature) 6.5.sp else 8.sp, fontWeight = FontWeight.Bold, color = clock.accentColor, fontFamily = FontFamily.Monospace)
                Box(modifier = Modifier.width(barWidth).height(4.dp).clip(RoundedCornerShape(2.dp)).background(Color.White.copy(alpha = 0.1f))) {
                    Box(modifier = Modifier.fillMaxWidth(0.72f).height(4.dp).background(clock.accentColor))
                }
                Text(text = "7.2K", fontSize = if (isMiniature) 6.5.sp else 8.sp, fontWeight = FontWeight.Bold, color = clock.accentColor, fontFamily = FontFamily.Monospace)
            }
        }
    }
}

// ========================================================
// 📊 Smart Real-Time Widgets Row
// ========================================================
@Composable
fun AODWidgetsRow(
    calendar: Calendar,
    telemetry: AODTelemetry,
    showBattery: Boolean,
    showDate: Boolean,
    showSteps: Boolean,
    showWeather: Boolean,
    accentColor: Color,
    isMiniature: Boolean = false
) {
    val dateFormat = SimpleDateFormat(if (isMiniature) "EEE, d MMM" else "EEE, dd MMM", Locale.getDefault())
    val dateStr = dateFormat.format(calendar.time)

    val widgetComposables = mutableListOf<@Composable () -> Unit>()
    if (showBattery) {
        widgetComposables.add {
            AODPillWidget(
                icon = if (telemetry.isCharging) "⚡" else "🔋",
                text = "${telemetry.batteryPercent}%",
                isMiniature = isMiniature
            )
        }
    }
    if (showDate) {
        widgetComposables.add {
            AODPillWidget(
                icon = "📅",
                text = dateStr,
                isMiniature = isMiniature
            )
        }
    }
    if (showSteps) {
        widgetComposables.add {
            AODPillWidget(
                icon = "👣",
                text = "%,d".format(telemetry.stepCount),
                isMiniature = isMiniature
            )
        }
    }
    if (showWeather) {
        widgetComposables.add {
            AODPillWidget(
                icon = "☀️",
                text = "${telemetry.tempCelsius}°C",
                isMiniature = isMiniature
            )
        }
    }

    if (widgetComposables.isEmpty()) return

    if (isMiniature && widgetComposables.size > 2) {
        // In miniature frame, distribute pills cleanly across 2 rows so they never clip
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                widgetComposables.take(2).forEach { it() }
            }
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                widgetComposables.drop(2).forEach { it() }
            }
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(if (isMiniature) 6.dp else 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            widgetComposables.forEach { it() }
        }
    }
}

@Composable
fun AODPillWidget(
    icon: String,
    text: String,
    isMiniature: Boolean = false
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(if (isMiniature) 12.dp else 20.dp))
            .background(Color.White.copy(alpha = 0.06f))
            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(if (isMiniature) 12.dp else 20.dp))
            .padding(
                horizontal = if (isMiniature) 7.dp else 10.dp,
                vertical = if (isMiniature) 2.5.dp else 4.dp
            )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(if (isMiniature) 3.dp else 4.dp)
        ) {
            Text(text = icon, fontSize = if (isMiniature) 9.sp else 11.sp)
            Text(
                text = text,
                fontSize = if (isMiniature) 9.sp else 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                maxLines = 1,
                softWrap = false
            )
        }
    }
}
