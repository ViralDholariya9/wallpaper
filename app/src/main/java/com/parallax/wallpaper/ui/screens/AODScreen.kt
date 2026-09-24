package com.parallax.wallpaper.ui.screens

import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.wallpaper.aod.AODActivity
import com.parallax.wallpaper.aod.AODClockFace
import com.parallax.wallpaper.aod.AODClockType
import com.parallax.wallpaper.aod.AODDisplayContent
import com.parallax.wallpaper.aod.AODManager
import com.parallax.wallpaper.data.AODRepository
import com.parallax.wallpaper.data.api.AODClockItem
import com.parallax.wallpaper.ui.theme.DeepObsidian
import com.parallax.wallpaper.ui.theme.NeonCyan
import com.parallax.wallpaper.ui.theme.NeonPink
import com.parallax.wallpaper.ui.theme.SurfaceDark
import com.parallax.wallpaper.ui.theme.TextPrimary
import com.parallax.wallpaper.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AODScreen(
    repository: AODRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val aodManager = remember { AODManager.getInstance(context) }

    val isEnabled by aodManager.isEnabled.collectAsState()
    val activeClock by aodManager.activeClock.collectAsState()
    val telemetry by aodManager.telemetry.collectAsState()
    val burnInOffset by aodManager.burnInOffset.collectAsState()
    val showBattery by aodManager.showBattery.collectAsState()
    val showDate by aodManager.showDate.collectAsState()
    val showSteps by aodManager.showSteps.collectAsState()
    val showWeather by aodManager.showWeather.collectAsState()
    val brightness by aodManager.brightness.collectAsState()

    val clocks by repository.clocks.collectAsState()
    val isLoading by repository.isLoading.collectAsState()

    var selectedFilter by remember { mutableStateOf("ALL") }

    val filteredClocks = remember(clocks, selectedFilter) {
        if (selectedFilter == "ALL") clocks else clocks.filter { it.clockType == selectedFilter }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DeepObsidian,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Always-On Display (AOD)",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "AMOLED 0W Battery Saving Clocks",
                            fontSize = 11.sp,
                            color = Color(0xFFFFD700)
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { repository.fetchClocks() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = TextPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = SurfaceDark
                )
            )
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. Interactive Phone Simulator View
            item(span = { GridItemSpan(2) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(SurfaceDark)
                        .border(1.dp, Color.White.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "📱 LIVE AMOLED SIMULATOR",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFFFFD700),
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Miniature Phone Frame
                    Box(
                        modifier = Modifier
                            .width(264.dp)
                            .height(390.dp)
                            .clip(RoundedCornerShape(36.dp))
                            .background(Color.Black) // AMOLED Black
                            .border(3.5.dp, Color(0xFF1E293B), RoundedCornerShape(36.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        AODDisplayContent(
                            clock = activeClock,
                            telemetry = telemetry,
                            burnInOffset = burnInOffset,
                            showBattery = showBattery,
                            showDate = showDate,
                            showSteps = showSteps,
                            showWeather = showWeather,
                            isMiniature = true
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Quick Launch Fullscreen AOD Mode
                    Button(
                        onClick = {
                            val intent = Intent(context, AODActivity::class.java)
                            context.startActivity(intent)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD700)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth(0.85f)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = "Launch Fullscreen AOD Mode", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            // 2. Master Toggle & Anti-Burn-In Protection Card
            item(span = { GridItemSpan(2) }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Always-On Display Engine",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Render black screen clock on standby",
                                    fontSize = 12.sp,
                                    color = TextSecondary
                                )
                            }

                            Switch(
                                checked = isEnabled,
                                onCheckedChange = { aodManager.setAODEnabled(it) },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color(0xFFFFD700),
                                    checkedTrackColor = Color(0xFFFFD700).copy(alpha = 0.35f)
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Anti-Burn-In Pixel Shift test button
                        Button(
                            onClick = {
                                aodManager.triggerManualPixelShift()
                                Toast.makeText(context, "Anti-Burn-In Pixel Shift triggered!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.06f)),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth(),
                            border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f))
                        ) {
                            Icon(imageVector = Icons.Default.Shield, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Test Anti-Burn-In Pixel Shift (60s loop)", color = NeonCyan, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // 3. Smart Widgets & Brightness Controls Card
            item(span = { GridItemSpan(2) }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "SMART WIDGETS DISPLAY",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = NeonCyan,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Widget Checkbox toggles in 2 rows
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            AODCheckboxItem(
                                title = "🔋 Battery %",
                                checked = showBattery,
                                onCheckedChange = { aodManager.setShowBattery(it) },
                                modifier = Modifier.weight(1f)
                            )
                            AODCheckboxItem(
                                title = "📅 Date & Day",
                                checked = showDate,
                                onCheckedChange = { aodManager.setShowDate(it) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            AODCheckboxItem(
                                title = "👣 Steps Pedometer",
                                checked = showSteps,
                                onCheckedChange = { aodManager.setShowSteps(it) },
                                modifier = Modifier.weight(1f)
                            )
                            AODCheckboxItem(
                                title = "☀️ Weather & Temp",
                                checked = showWeather,
                                onCheckedChange = { aodManager.setShowWeather(it) },
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Screen Brightness slider
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = "Display Brightness", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                            Text(text = "${(brightness * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFFD700))
                        }
                        Slider(
                            value = brightness,
                            onValueChange = { aodManager.setBrightness(it) },
                            valueRange = 0.15f..1.0f,
                            colors = SliderDefaults.colors(
                                thumbColor = Color(0xFFFFD700),
                                activeTrackColor = Color(0xFFFFD700)
                            )
                        )
                    }
                }
            }

            // 4. Clock Faces Filter Chips
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val filters = listOf(
                        "ALL" to "All Styles",
                        "cyberpunk_digital" to "Cyberpunk",
                        "minimalist_analog" to "Analog",
                        "typography_word" to "Typography",
                        "neon_animal" to "Animals",
                        "gaming_hud" to "Gamer HUD"
                    )

                    filters.forEach { (key, label) ->
                        FilterChip(
                            selected = selectedFilter == key,
                            onClick = { selectedFilter = key },
                            label = { Text(text = label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color(0xFFFFD700),
                                selectedLabelColor = Color.Black
                            )
                        )
                    }
                }
            }

            // 5. Clock Faces Grid Items
            items(filteredClocks) { clockItem ->
                val isSelected = activeClock.id == clockItem.id

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val face = AODClockFace(
                                id = clockItem.id,
                                title = clockItem.title,
                                clockType = AODClockType.fromString(clockItem.clockType),
                                accentColorHex = clockItem.accentColor,
                                glowColorHex = clockItem.glowColor,
                                textColorHex = clockItem.textColor,
                                backgroundColorHex = clockItem.backgroundColor,
                                dialStyle = clockItem.dialStyle,
                                hasBatteryWidget = clockItem.hasBatteryWidget,
                                hasDateWidget = clockItem.hasDateWidget,
                                hasStepsWidget = clockItem.hasStepsWidget,
                                hasWeatherWidget = clockItem.hasWeatherWidget,
                                previewUrl = clockItem.previewUrl ?: "",
                                isPremium = clockItem.isPremium,
                                downloads = clockItem.downloads
                            )
                            aodManager.setActiveClock(face)
                            repository.applyClock(clockItem)
                            Toast.makeText(context, "Applied: ${clockItem.title}", Toast.LENGTH_SHORT).show()
                        },
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) Color(0xFFFFD700) else Color.White.copy(alpha = 0.08f)
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Mini Dark Screen Preview Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(110.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color.Black),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (clockItem.clockType) {
                                    "cyberpunk_digital" -> "10:45\nSEC : 28"
                                    "minimalist_analog" -> "🕒 ANALOG\n10:10"
                                    "typography_word" -> "IT IS PAST\nTEN IN NIGHT"
                                    "neon_animal" -> "🦊\n09:41"
                                    "gaming_hud" -> "LVL 99\n12:30"
                                    else -> "10:45"
                                },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = try { Color(android.graphics.Color.parseColor(clockItem.accentColor)) } catch (_: Exception) { NeonCyan },
                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                            )

                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(6.dp)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFFFD700)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Active",
                                        tint = Color.Black,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = clockItem.title,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "🔥 ${clockItem.downloads} applied",
                            fontSize = 11.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AODCheckboxItem(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.clickable { onCheckedChange(!checked) },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFFFFD700),
                checkmarkColor = Color.Black
            )
        )
        Text(text = title, fontSize = 11.5.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
    }
}
