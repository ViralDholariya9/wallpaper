package com.parallax.wallpaper.ui.screens

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.wallpaper.data.FingerprintRepository
import com.parallax.wallpaper.fingerprint.FingerprintAnimationManager
import com.parallax.wallpaper.model.FingerprintPreset
import com.parallax.wallpaper.ui.components.InDisplayFingerprintView
import com.parallax.wallpaper.ui.theme.CardBorder
import com.parallax.wallpaper.ui.theme.CardDark
import com.parallax.wallpaper.ui.theme.DeepObsidian
import com.parallax.wallpaper.ui.theme.NeonCyan
import com.parallax.wallpaper.ui.theme.NeonPink
import com.parallax.wallpaper.ui.theme.NeonPurple
import com.parallax.wallpaper.ui.theme.SurfaceDark
import com.parallax.wallpaper.ui.theme.TextPrimary
import com.parallax.wallpaper.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FingerprintAnimationsScreen(
    repository: FingerprintRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        FingerprintAnimationManager.init(context)
        repository.fetchPresets()
    }

    val isFpEnabled by FingerprintAnimationManager.isEnabled.collectAsState()
    val activePreset by FingerprintAnimationManager.activePreset.collectAsState()
    val presets by repository.presets.collectAsState()
    val isLoading by repository.isLoading.collectAsState()

    val yPosVal by FingerprintAnimationManager.yPositionPercent.collectAsState()
    val scaleVal by FingerprintAnimationManager.sensorScale.collectAsState()
    val hapticVal by FingerprintAnimationManager.isHapticEnabled.collectAsState()

    var selectedPreset by remember(activePreset) { mutableStateOf(activePreset) }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var showCalibration by remember { mutableStateOf(false) }

    val categories = listOf("ALL", "CYBERPUNK", "COSMIC", "NEON", "MAGIC", "ENERGY")

    val filteredPresets = remember(presets, selectedCategory) {
        if (selectedCategory == "ALL") presets
        else presets.filter { it.category.equals(selectedCategory, ignoreCase = true) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "In-Display Fingerprint FX",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = "બાયોમેટ્રિક ફિંગરપ્રિન્ટ એનિમેશન્સ",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonCyan,
                            fontSize = 11.sp
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
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 12.dp)
                    ) {
                        Switch(
                            checked = isFpEnabled,
                            onCheckedChange = {
                                FingerprintAnimationManager.setEnabled(it)
                                Toast.makeText(
                                    context,
                                    if (it) "🔓 Fingerprint FX સક્રિય થયું" else "Fingerprint FX બંધ થયું",
                                    Toast.LENGTH_SHORT
                                ).show()
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = NeonCyan,
                                checkedTrackColor = NeonCyan.copy(alpha = 0.35f),
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = SurfaceDark
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepObsidian
                )
            )
        },
        containerColor = DeepObsidian,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // 1. Live Interactive Phone Simulator Card
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(NeonCyan, CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "LIVE PHONE SIMULATOR",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeonCyan,
                                    letterSpacing = 1.sp
                                )
                            }

                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = CardBorder
                            ) {
                                Text(
                                    text = selectedPreset.title,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Smartphone Screen Mockup Viewport
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF070B14))
                                .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                        ) {
                            // AMOLED subtle background gradient
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.radialGradient(
                                            colors = listOf(
                                                Color(0xFF0F172A),
                                                Color(0xFF020617)
                                            )
                                        )
                                    )
                            )

                            // Interactive In-Display Fingerprint Sensor
                            InDisplayFingerprintView(
                                preset = selectedPreset,
                                yPositionPercent = yPosVal,
                                scale = scaleVal,
                                interactive = true,
                                onUnlocked = {
                                    Toast.makeText(
                                        context,
                                        "🔓 Biometric Unlocked! (${selectedPreset.typeEnum.displayName})",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            )

                            // Instructions hint
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.65f),
                                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 12.dp)
                            ) {
                                Text(
                                    text = "👆 સેન્સર પર આંગળી મૂકી ટેસ્ટ કરો (Hold to Test)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Toggle Calibration Settings
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showCalibration = !showCalibration }
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Sensor Calibration (સ્થાન અને સાઈઝ સેટિંગ)",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary
                                )
                            }
                            Text(
                                text = if (showCalibration) "▲ છુપાવો" else "▼ બદલો",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonCyan
                            )
                        }

                        if (showCalibration) {
                            Spacer(modifier = Modifier.height(8.dp))
                            // Y-Position Slider
                            Text(
                                text = "Sensor Vertical Position: $yPosVal%",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Slider(
                                value = yPosVal.toFloat(),
                                onValueChange = { FingerprintAnimationManager.setYPositionPercent(it.toInt()) },
                                valueRange = 55f..90f,
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonCyan,
                                    activeTrackColor = NeonCyan
                                )
                            )

                            // Scale Slider
                            Text(
                                text = "Sensor Size Scale: ${String.format("%.2f", scaleVal)}x",
                                style = MaterialTheme.typography.labelSmall,
                                color = TextSecondary
                            )
                            Slider(
                                value = scaleVal,
                                onValueChange = { FingerprintAnimationManager.setSensorScale(it) },
                                valueRange = 0.7f..1.5f,
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonPurple,
                                    activeTrackColor = NeonPurple
                                )
                            )

                            // Haptic Vibration Row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Vibration,
                                        contentDescription = null,
                                        tint = NeonPink,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Haptic Vibration Feedback",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextPrimary
                                    )
                                }
                                Switch(
                                    checked = hapticVal,
                                    onCheckedChange = { FingerprintAnimationManager.setHapticEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = NeonPink,
                                        checkedTrackColor = NeonPink.copy(alpha = 0.4f)
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // 2. Category Filter Chips
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { cat ->
                        val isSelected = selectedCategory == cat
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = Color.Black,
                                containerColor = CardDark,
                                labelColor = TextSecondary
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isSelected) NeonCyan else CardBorder
                            )
                        )
                    }
                }
            }

            // 3. Section Title
            item {
                PaddingValues(horizontal = 16.dp, vertical = 4.dp)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "ANIMATION PRESETS (${filteredPresets.size})",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                }
            }

            // 4. Presets Cards Grid (2 items per row in items chunked)
            items(filteredPresets.chunked(2).size) { rowIndex ->
                val chunk = filteredPresets.chunked(2)[rowIndex]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    chunk.forEach { preset ->
                        val isApplied = activePreset.id == preset.id
                        val isSimulating = selectedPreset.id == preset.id

                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .clickable {
                                    selectedPreset = preset
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSimulating) Color(0xFF131D33) else CardDark
                            ),
                            border = BorderStroke(
                                if (isSimulating) 1.5.dp else 1.dp,
                                if (isSimulating) NeonCyan else CardBorder
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                // Mini Sensor Preview Box
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color(0xFF080D1A)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    // Mini concentric rings mockup
                                    Box(
                                        modifier = Modifier
                                            .size(56.dp)
                                            .border(
                                                1.5.dp,
                                                parseHexColor(preset.primaryColor, NeonCyan),
                                                CircleShape
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .border(
                                                    1.dp,
                                                    parseHexColor(preset.secondaryColor, NeonPurple),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = preset.typeEnum.icon,
                                                fontSize = 18.sp
                                            )
                                        }
                                    }

                                    // Category badge
                                    Surface(
                                        shape = RoundedCornerShape(4.dp),
                                        color = parseHexColor(preset.primaryColor, NeonCyan),
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(6.dp)
                                    ) {
                                        Text(
                                            text = preset.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.Black,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 9.sp,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }

                                    if (preset.isPremium) {
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = Color(0xFFFFD700),
                                            modifier = Modifier
                                                .align(Alignment.TopEnd)
                                                .padding(6.dp)
                                        ) {
                                            Text(
                                                text = "💎 VIP",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = Color.Black,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 9.sp,
                                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Text(
                                    text = preset.title,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSimulating) NeonCyan else TextPrimary,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = "${preset.downloads} users applied",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Apply button
                                Button(
                                    onClick = {
                                        selectedPreset = preset
                                        FingerprintAnimationManager.setActivePreset(preset)
                                        scope.launch {
                                            repository.applyPreset(preset.id)
                                        }
                                        Toast.makeText(
                                            context,
                                            "✅ ${preset.title} સેટ થઈ ગયું!",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isApplied) Color(0xFF10B981) else NeonCyan,
                                        contentColor = Color.Black
                                    ),
                                    contentPadding = PaddingValues(vertical = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = if (isApplied) Icons.Default.Check else Icons.Default.Fingerprint,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isApplied) "Applied" else "Apply",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 11.sp
                                    )
                                }
                            }
                        }
                    }

                    // Fill space if row only has 1 item
                    if (chunk.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

private fun parseHexColor(hex: String?, fallback: Color): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        val clean = hex.trim().removePrefix("#")
        when (clean.length) {
            6 -> Color(clean.toLong(16) or 0x00000000FF000000)
            8 -> Color(clean.toLong(16))
            else -> fallback
        }
    } catch (_: Exception) {
        fallback
    }
}
