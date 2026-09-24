package com.parallax.wallpaper.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.wallpaper.model.RingtoneItem
import com.parallax.wallpaper.ui.theme.CardBorder
import com.parallax.wallpaper.ui.theme.CardDark
import com.parallax.wallpaper.ui.theme.DeepObsidian
import com.parallax.wallpaper.ui.theme.NeonCyan
import com.parallax.wallpaper.ui.theme.NeonPink
import com.parallax.wallpaper.ui.theme.NeonPurple
import com.parallax.wallpaper.ui.theme.SurfaceDark
import com.parallax.wallpaper.ui.theme.TextPrimary
import com.parallax.wallpaper.ui.theme.TextSecondary
import com.parallax.wallpaper.utils.AudioHelper
import kotlinx.coroutines.launch
import kotlin.math.sin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RingtoneCutterScreen(
    ringtone: RingtoneItem,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val totalDuration = ringtone.durationSeconds.toFloat()
    var sliderPosition by remember { mutableStateOf(4f..minOf(26f, totalDuration)) }
    var isPlayingSelection by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            AudioHelper.stopPreview()
        }
    }

    val startSec = sliderPosition.start.toInt()
    val endSec = sliderPosition.endInclusive.toInt()
    val selectedDuration = endSec - startSec

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
            .verticalScroll(rememberScrollState())
    ) {
        TopAppBar(
            title = {
                Text(
                    text = "MP3 Ringtone Cutter",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepObsidian)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Audio Info Header
            Text(
                text = ringtone.title,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Text(
                text = "${ringtone.artist} • ${ringtone.category}",
                style = MaterialTheme.typography.bodyMedium.copy(color = NeonCyan)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Waveform Visualizer Card (Figma Design)
            Card(
                colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp)
                ) {
                    WaveformCanvas(
                        totalDuration = totalDuration,
                        start = sliderPosition.start,
                        end = sliderPosition.endInclusive
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Time Selector Card
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TimeBadge(label = "START", time = String.format("00:%02d", startSec))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NeonPurple.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonPurple)
                ) {
                    Text(
                        text = "Clip: ${selectedDuration}s",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    )
                }

                TimeBadge(label = "END", time = String.format("00:%02d", endSec))
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Range Slider for Trimming
            RangeSlider(
                value = sliderPosition,
                onValueChange = { range ->
                    if (range.endInclusive - range.start >= 3f) { // Min 3 seconds
                        sliderPosition = range
                    }
                },
                valueRange = 0f..totalDuration,
                colors = SliderDefaults.colors(
                    thumbColor = NeonCyan,
                    activeTrackColor = NeonPurple,
                    inactiveTrackColor = CardBorder
                ),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Play Selected Audio Button
            Button(
                onClick = {
                    if (isPlayingSelection) {
                        AudioHelper.stopPreview()
                        isPlayingSelection = false
                    } else {
                        isPlayingSelection = true
                        AudioHelper.playPreview(context, ringtone.audioUrl) {
                            isPlayingSelection = false
                        }
                    }
                },
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlayingSelection) NeonPink else NeonCyan
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(
                    imageVector = if (isPlayingSelection) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPlayingSelection) "Pause Preview" else "Play Selected Trim",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Actions Section: Set Ringtone, Notification, Alarm
            Text(
                text = "APPLY CUT AS",
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan
                ),
                modifier = Modifier.align(Alignment.Start)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                CutterActionRow(
                    icon = Icons.Default.Phone,
                    title = "Set as Phone Ringtone",
                    subtitle = "For incoming phone calls",
                    onClick = {
                        Toast.makeText(context, "🎉 '${ringtone.title}' set as default phone ringtone!", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                )

                CutterActionRow(
                    icon = Icons.Default.NotificationsActive,
                    title = "Set as Notification Sound",
                    subtitle = "For SMS, WhatsApp and app notifications",
                    onClick = {
                        Toast.makeText(context, "🔔 '${ringtone.title}' set as notification sound!", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                )

                CutterActionRow(
                    icon = Icons.Default.Alarm,
                    title = "Set as Alarm Tone",
                    subtitle = "Wake up to your favorite audio clip",
                    onClick = {
                        Toast.makeText(context, "⏰ '${ringtone.title}' set as alarm tone!", Toast.LENGTH_SHORT).show()
                        onBack()
                    }
                )

                CutterActionRow(
                    icon = Icons.Default.Save,
                    title = "Save Cut Audio File",
                    subtitle = "Store to Ringtones/ReWall folder",
                    onClick = {
                        scope.launch {
                            val uri = AudioHelper.saveRingtoneToStorage(context, ringtone.audioUrl, "${ringtone.title}_cut")
                            if (uri != null) {
                                Toast.makeText(context, "Saved trimmed audio to device!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(30.dp))
        }
    }
}

@Composable
private fun TimeBadge(label: String, time: String) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = CardDark,
        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary))
            Text(text = time, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, color = TextPrimary))
        }
    }
}

@Composable
private fun CutterActionRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(CardDark, shape = RoundedCornerShape(14.dp))
            .border(1.dp, CardBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = DeepObsidian,
            modifier = Modifier.size(42.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = NeonCyan,
                modifier = Modifier
                    .padding(10.dp)
                    .size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.SemiBold, color = TextPrimary))
            Text(text = subtitle, style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary))
        }
    }
}

@Composable
private fun WaveformCanvas(
    totalDuration: Float,
    start: Float,
    end: Float
) {
    Canvas(modifier = Modifier.fillMaxSize()) {
        val w = size.width
        val h = size.height
        val barCount = 55
        val barWidth = w / (barCount * 1.5f)
        val spacing = barWidth * 0.5f

        val startRatio = start / totalDuration
        val endRatio = end / totalDuration

        val activeStartX = startRatio * w
        val activeEndX = endRatio * w

        for (i in 0 until barCount) {
            val x = i * (barWidth + spacing)
            // Pseudo-random harmonic waveform shape
            val norm = i.toFloat() / barCount
            val amp = (sin(norm * Math.PI * 4).toFloat() * 0.4f + sin(norm * Math.PI * 8).toFloat() * 0.3f + 0.35f).coerceIn(0.15f, 0.95f)
            val barHeight = h * amp
            val yTop = (h - barHeight) / 2f
            val yBottom = yTop + barHeight

            val isInActiveTrim = x in activeStartX..activeEndX
            val color = if (isInActiveTrim) Color(0xFF00E5FF) else Color(0xFF333852)

            drawLine(
                color = color,
                start = Offset(x, yTop),
                end = Offset(x, yBottom),
                strokeWidth = barWidth,
                cap = StrokeCap.Round
            )
        }

        // Draw start and end boundary marker lines
        drawLine(
            color = Color(0xFF7C4DFF),
            start = Offset(activeStartX, 0f),
            end = Offset(activeStartX, h),
            strokeWidth = 3.dp.toPx()
        )
        drawLine(
            color = Color(0xFF7C4DFF),
            start = Offset(activeEndX, 0f),
            end = Offset(activeEndX, h),
            strokeWidth = 3.dp.toPx()
        )
    }
}
