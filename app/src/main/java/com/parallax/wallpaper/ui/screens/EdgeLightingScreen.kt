package com.parallax.wallpaper.ui.screens

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.wallpaper.data.EdgeLightingRepository
import com.parallax.wallpaper.data.api.EdgeLightingPresetItem
import com.parallax.wallpaper.edge.EdgeLightingCanvas
import com.parallax.wallpaper.edge.EdgeLightingManager
import com.parallax.wallpaper.ui.theme.CardBorder
import com.parallax.wallpaper.ui.theme.DeepObsidian
import com.parallax.wallpaper.ui.theme.NeonCyan
import com.parallax.wallpaper.ui.theme.NeonPink
import com.parallax.wallpaper.ui.theme.NeonPurple
import com.parallax.wallpaper.ui.theme.SurfaceDark
import com.parallax.wallpaper.ui.theme.TextPrimary
import com.parallax.wallpaper.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EdgeLightingScreen(
    repository: EdgeLightingRepository,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val presets by repository.presets.collectAsState()
    val isLoading by repository.isLoading.collectAsState()

    val isEnabled by EdgeLightingManager.isEnabled.collectAsState()
    val activePreset by EdgeLightingManager.activePreset.collectAsState()
    val borderThickness by EdgeLightingManager.borderThicknessDp.collectAsState()
    val speedMultiplier by EdgeLightingManager.speedMultiplier.collectAsState()
    val cornerRadius by EdgeLightingManager.cornerRadiusDp.collectAsState()
    val punchHoleRadius by EdgeLightingManager.punchHoleRadiusDp.collectAsState()

    val triggerAlways by EdgeLightingManager.triggerAlwaysOn.collectAsState()
    val triggerNotif by EdgeLightingManager.triggerOnNotification.collectAsState()
    val triggerCall by EdgeLightingManager.triggerOnCall.collectAsState()
    val triggerMusic by EdgeLightingManager.triggerOnMusic.collectAsState()

    var selectedCategory by remember { mutableStateOf("ALL") }
    var hasOverlayPermission by remember {
        mutableStateOf(EdgeLightingManager.canDrawOverlays(context))
    }

    val categories = remember {
        listOf(
            "ALL" to "All",
            "RAINBOW" to "🌈 Rainbow",
            "CYBER" to "⚡ Cyber Snake",
            "NEON" to "💖 Neon Pulse",
            "GALAXY" to "🌌 Galaxy",
            "FIRE" to "🔥 Fire",
            "NOTCH" to "🎯 Notch"
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🌈 Edge Lighting Studio",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Text(
                            text = "Hardware-accelerated screen border glow",
                            color = TextSecondary,
                            fontSize = 11.5.sp
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
                    IconButton(onClick = { repository.fetchPresets() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh",
                            tint = NeonCyan
                        )
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { checked ->
                            EdgeLightingManager.setEnabled(context, checked)
                            if (checked && !hasOverlayPermission) {
                                Toast.makeText(context, "Please grant overlay permission for system-wide edge lights", Toast.LENGTH_LONG).show()
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = NeonCyan,
                            checkedTrackColor = NeonPurple.copy(alpha = 0.5f)
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = SurfaceDark)
            )
        },
        containerColor = DeepObsidian
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Permission Alert Banner if missing
            if (!hasOverlayPermission) {
                item(span = { GridItemSpan(2) }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF2D1828)),
                        border = BorderStroke(1.dp, NeonPink.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Filled.Security,
                                        contentDescription = null,
                                        tint = NeonPink,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "System Overlay Permission Required",
                                        color = Color.White,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                }
                                Text(
                                    text = "Enable 'Display over other apps' to enjoy glowing border lights everywhere.",
                                    color = TextSecondary,
                                    fontSize = 11.5.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }
                            Button(
                                onClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        val intent = Intent(
                                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                            Uri.parse("package:${context.packageName}")
                                        )
                                        context.startActivity(intent)
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text("Grant", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Interactive Smartphone Mockup Preview
            item(span = { GridItemSpan(2) }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, CardBorder),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "📱 Live Edge Glow Preview",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        Text(
                            text = activePreset.title,
                            color = NeonCyan,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                        )

                        // Smartphone Shell
                        Box(
                            modifier = Modifier
                                .width(220.dp)
                                .height(380.dp)
                                .clip(RoundedCornerShape(cornerRadius.dp))
                                .background(Color(0xFF07090E))
                                .border(
                                    width = 1.dp,
                                    color = Color.White.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(cornerRadius.dp)
                                )
                        ) {
                            // The 60FPS Hardware-Accelerated Canvas
                            EdgeLightingCanvas(
                                colors = activePreset.colors,
                                animationType = activePreset.animationType,
                                speed = speedMultiplier,
                                borderThickness = borderThickness.dp,
                                cornerRadius = cornerRadius.dp,
                                punchHoleRadius = punchHoleRadius.dp
                            )

                            // Phone Mockup Inner Screen
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "12:45",
                                    color = Color.White,
                                    fontSize = 28.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "ReWall 3D Edge Engine",
                                    color = TextSecondary,
                                    fontSize = 10.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )

                                Spacer(modifier = Modifier.height(16.dp))

                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    activePreset.colors.take(5).forEach { colorHex ->
                                        Box(
                                            modifier = Modifier
                                                .size(12.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    try {
                                                        Color(android.graphics.Color.parseColor(colorHex))
                                                    } catch (_: Exception) {
                                                        NeonCyan
                                                    }
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Real-Time Customizer Controls (Sliders)
            item(span = { GridItemSpan(2) }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, CardBorder),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🎛️ Border Customization",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        // 1. Thickness
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("📏 Border Thickness", color = TextSecondary, fontSize = 12.sp)
                            Text("${borderThickness} dp", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Slider(
                            value = borderThickness.toFloat(),
                            onValueChange = { EdgeLightingManager.setThickness(context, it.toInt()) },
                            valueRange = 2f..14f,
                            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                        )

                        // 2. Speed
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("⚡ Animation Speed", color = TextSecondary, fontSize = 12.sp)
                            Text("${String.format("%.1f", speedMultiplier)}x", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Slider(
                            value = speedMultiplier,
                            onValueChange = { EdgeLightingManager.setSpeed(context, it) },
                            valueRange = 0.3f..3.0f,
                            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                        )

                        // 3. Corner Radius
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("🔲 Corner Radius", color = TextSecondary, fontSize = 12.sp)
                            Text("${cornerRadius} dp", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Slider(
                            value = cornerRadius.toFloat(),
                            onValueChange = { EdgeLightingManager.setCornerRadius(context, it.toInt()) },
                            valueRange = 8f..48f,
                            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                        )

                        // 4. Punch-Hole Light
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("🎯 Camera Punch-Hole Light", color = TextSecondary, fontSize = 12.sp)
                            Text(if (punchHoleRadius > 0) "${punchHoleRadius} dp" else "Off", color = NeonCyan, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }
                        Slider(
                            value = punchHoleRadius.toFloat(),
                            onValueChange = { EdgeLightingManager.setPunchHoleRadius(context, it.toInt()) },
                            valueRange = 0f..24f,
                            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                        )
                    }
                }
            }

            // Trigger Modes Selector
            item(span = { GridItemSpan(2) }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, CardBorder),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "🔔 Illumination Triggers",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            FilterChip(
                                selected = triggerAlways,
                                onClick = {
                                    EdgeLightingManager.setTriggers(context, !triggerAlways, triggerNotif, triggerCall, triggerMusic)
                                },
                                label = { Text("Always On", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonPurple.copy(alpha = 0.35f),
                                    selectedLabelColor = NeonCyan
                                )
                            )
                            FilterChip(
                                selected = triggerNotif,
                                onClick = {
                                    EdgeLightingManager.setTriggers(context, triggerAlways, !triggerNotif, triggerCall, triggerMusic)
                                },
                                label = { Text("Notifications", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonPurple.copy(alpha = 0.35f),
                                    selectedLabelColor = NeonCyan
                                )
                            )
                            FilterChip(
                                selected = triggerCall,
                                onClick = {
                                    EdgeLightingManager.setTriggers(context, triggerAlways, triggerNotif, !triggerCall, triggerMusic)
                                },
                                label = { Text("Incoming Call", fontSize = 11.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonPurple.copy(alpha = 0.35f),
                                    selectedLabelColor = NeonCyan
                                )
                            )
                        }
                    }
                }
            }

            // Categories Filter Chips
            item(span = { GridItemSpan(2) }) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    items(categories) { (key, label) ->
                        val isSelected = selectedCategory == key
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategory = key
                                repository.fetchPresets(category = key)
                            },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = Color.Black,
                                containerColor = SurfaceDark,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }
            }

            // Presets Catalog Title
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🌈 Curated Presets (${presets.size})",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = NeonCyan,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    }
                }
            }

            // Presets Grid
            items(presets) { preset ->
                val isCurrent = activePreset.id == preset.id

                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(
                        width = if (isCurrent) 2.dp else 1.dp,
                        color = if (isCurrent) NeonCyan else CardBorder
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            EdgeLightingManager.setActivePreset(context, preset)
                            repository.applyPreset(preset)
                            Toast.makeText(context, "${preset.title} applied!", Toast.LENGTH_SHORT).show()
                        }
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        // Mini preview box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF080B12)),
                            contentAlignment = Alignment.Center
                        ) {
                            EdgeLightingCanvas(
                                colors = preset.colors,
                                animationType = preset.animationType,
                                speed = preset.speed,
                                borderThickness = 4.dp,
                                cornerRadius = 10.dp
                            )
                            Text(
                                text = "📱",
                                fontSize = 22.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = preset.title,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Text(
                            text = "${preset.category} • ${preset.speed}x • ${preset.borderSize}px",
                            color = TextSecondary,
                            fontSize = 10.5.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Button(
                            onClick = {
                                EdgeLightingManager.setActivePreset(context, preset)
                                repository.applyPreset(preset)
                                Toast.makeText(context, "${preset.title} applied!", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isCurrent) NeonCyan else SurfaceDark
                            ),
                            border = if (!isCurrent) BorderStroke(1.dp, NeonCyan) else null,
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isCurrent) "✓ Active" else "Apply",
                                color = if (isCurrent) Color.Black else NeonCyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
    }
}
