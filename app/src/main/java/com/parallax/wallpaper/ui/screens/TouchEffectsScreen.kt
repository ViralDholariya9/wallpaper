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
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Waves
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
import com.parallax.wallpaper.data.TouchEffectsRepository
import com.parallax.wallpaper.model.TouchEffectPreset
import com.parallax.wallpaper.model.TouchEffectType
import com.parallax.wallpaper.touch.TouchEffectManager
import com.parallax.wallpaper.ui.components.InteractiveTouchCanvas
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
fun TouchEffectsScreen(
    repository: TouchEffectsRepository,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        TouchEffectManager.init(context)
        repository.fetchPresets()
    }

    val isTouchEnabled by TouchEffectManager.isEnabled.collectAsState()
    val activePreset by TouchEffectManager.activePreset.collectAsState()
    val isHapticEnabled by TouchEffectManager.isHapticEnabled.collectAsState()
    val customRadius by TouchEffectManager.rippleRadius.collectAsState()
    val customSpeed by TouchEffectManager.speedMultiplier.collectAsState()
    val customParticles by TouchEffectManager.particleCount.collectAsState()

    val presets by repository.presets.collectAsState()

    // Temporary working preset for the live interactive playground
    var simPreset by remember(activePreset) { mutableStateOf(activePreset) }
    var shockwaveCounter by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf("ALL") }

    val categories = listOf("ALL", "WATER_RIPPLE", "NEON_FLUID", "ELECTRIC_SPARKS", "MAGIC_STARDUST", "MAGMA_BURST", "GRAVITY_VORTEX")

    val filteredPresets = remember(presets, selectedCategory) {
        if (selectedCategory == "ALL") presets
        else presets.filter { it.effectType.equals(selectedCategory, ignoreCase = true) }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = DeepObsidian,
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "👆 Touch & Fluid Effects",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NeonCyan.copy(alpha = 0.2f),
                            border = BorderStroke(1.dp, NeonCyan)
                        ) {
                            Text(
                                text = "4D LIVE",
                                color = NeonCyan,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
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
                        Text(
                            text = if (isTouchEnabled) "ACTIVE" else "OFF",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isTouchEnabled) NeonCyan else TextSecondary,
                            modifier = Modifier.padding(end = 6.dp)
                        )
                        Switch(
                            checked = isTouchEnabled,
                            onCheckedChange = { TouchEffectManager.setEnabled(it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = NeonCyan,
                                uncheckedThumbColor = TextSecondary,
                                uncheckedTrackColor = SurfaceDark
                            )
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepObsidian)
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Intro Banner
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        NeonPurple.copy(alpha = 0.25f),
                                        NeonCyan.copy(alpha = 0.15f),
                                        Color.Transparent
                                    )
                                )
                            )
                            .padding(16.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "🌊 4D Touch Hydrodynamic Playground",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Touch, swirl, and swipe anywhere inside the interactive preview below to experience real-time fluid simulation, water ripples, lightning arcs, and quantum stardust.",
                                fontSize = 12.sp,
                                color = TextSecondary,
                                lineHeight = 16.sp
                            )
                        }
                    }
                }
            }

            // Interactive Live Phone Simulator
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = BorderStroke(1.5.dp, NeonCyan.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Phone frame with dynamic background and touch canvas
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(320.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF1B1838),
                                            Color(0xFF0D0B18),
                                            Color(0xFF05050A)
                                        )
                                    )
                                )
                                .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                        ) {
                            // Subtle background stars / grid aesthetic
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                NeonCyan.copy(alpha = 0.08f),
                                                Color.Transparent,
                                                NeonPurple.copy(alpha = 0.12f)
                                            )
                                        )
                                    )
                            )

                            // Interactive Touch Canvas
                            InteractiveTouchCanvas(
                                preset = simPreset.copy(
                                    rippleRadius = customRadius,
                                    speed = customSpeed,
                                    particleCount = customParticles
                                ),
                                shockwaveTrigger = shockwaveCounter,
                                modifier = Modifier.fillMaxSize()
                            )

                            // Top instruction pill
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = Color.Black.copy(alpha = 0.65f),
                                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 10.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${simPreset.typeEnum.icon} ${simPreset.title}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan
                                    )
                                }
                            }

                            // Bottom touch hint
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color.Black.copy(alpha = 0.5f),
                                modifier = Modifier
                                    .align(Alignment.BottomCenter)
                                    .padding(bottom = 10.dp)
                            ) {
                                Text(
                                    text = "👆 Touch & drag to test fluid flow",
                                    fontSize = 10.sp,
                                    color = Color.White.copy(alpha = 0.75f),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Quick interactive playground controls
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { shockwaveCounter++ },
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Shockwave", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { shockwaveCounter = 0 },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, CardBorder),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
                            ) {
                                Icon(Icons.Default.CleaningServices, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Clear", fontSize = 12.sp)
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Vibration,
                                    contentDescription = null,
                                    tint = if (isHapticEnabled) NeonCyan else TextSecondary,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Haptic", fontSize = 11.sp, color = TextSecondary)
                                Spacer(modifier = Modifier.width(6.dp))
                                Switch(
                                    checked = isHapticEnabled,
                                    onCheckedChange = { TouchEffectManager.setHapticEnabled(it) },
                                    modifier = Modifier.height(24.dp),
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color.White,
                                        checkedTrackColor = NeonCyan
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Quick Effect Chips
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TouchEffectType.entries.forEach { type ->
                                val isSelected = simPreset.typeEnum == type
                                FilterChip(
                                    selected = isSelected,
                                    onClick = {
                                        simPreset = simPreset.copy(
                                            effectType = type.code,
                                            title = "${type.icon} ${type.displayName}"
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = "${type.icon} ${type.displayName}",
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonCyan.copy(alpha = 0.25f),
                                        selectedLabelColor = NeonCyan,
                                        containerColor = SurfaceDark,
                                        labelColor = TextSecondary
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = isSelected,
                                        selectedBorderColor = NeonCyan,
                                        borderColor = CardBorder
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Physics Sliders Tuning Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, CardBorder)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Waves, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Fluid Dynamics & Physics Tuning",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Ripple Radius
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Wave & Flare Radius", fontSize = 12.sp, color = TextSecondary)
                            Text("${customRadius.toInt()} px", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonCyan)
                        }
                        Slider(
                            value = customRadius,
                            onValueChange = { TouchEffectManager.setRippleRadius(it) },
                            valueRange = 100f..450f,
                            colors = SliderDefaults.colors(thumbColor = NeonCyan, activeTrackColor = NeonCyan)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Flow Speed
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Flow & Wave Velocity", fontSize = 12.sp, color = TextSecondary)
                            Text(String.format("%.1fx", customSpeed), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonPurple)
                        }
                        Slider(
                            value = customSpeed,
                            onValueChange = { TouchEffectManager.setSpeedMultiplier(it) },
                            valueRange = 0.5f..3.0f,
                            colors = SliderDefaults.colors(thumbColor = NeonPurple, activeTrackColor = NeonPurple)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Particle Density
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Fluid Particle Density", fontSize = 12.sp, color = TextSecondary)
                            Text("$customParticles particles", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = NeonPink)
                        }
                        Slider(
                            value = customParticles.toFloat(),
                            onValueChange = { TouchEffectManager.setParticleCount(it.toInt()) },
                            valueRange = 20f..150f,
                            colors = SliderDefaults.colors(thumbColor = NeonPink, activeTrackColor = NeonPink)
                        )
                    }
                }
            }

            // Presets Catalog Section Header
            item {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Curated Flagship Presets",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "${filteredPresets.size} Presets",
                            fontSize = 12.sp,
                            color = NeonCyan,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Category horizontal filter chips
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = {
                                    Text(
                                        text = when (cat) {
                                            "ALL" -> "🔥 All"
                                            "WATER_RIPPLE" -> "🌊 Water"
                                            "NEON_FLUID" -> "🌌 Fluid"
                                            "ELECTRIC_SPARKS" -> "⚡ Sparks"
                                            "MAGIC_STARDUST" -> "✨ Stardust"
                                            "MAGMA_BURST" -> "🔥 Magma"
                                            "GRAVITY_VORTEX" -> "🌀 Vortex"
                                            else -> cat
                                        },
                                        fontSize = 12.sp
                                    )
                                },
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
            }

            // Presets Catalog Cards
            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    filteredPresets.forEach { preset ->
                        val isActive = activePreset.id == preset.id

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    simPreset = preset
                                    shockwaveCounter++
                                },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                            border = BorderStroke(
                                1.5.dp,
                                if (isActive) NeonCyan else CardBorder
                            )
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Effect Type Badge Icon
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = Color.Black.copy(alpha = 0.5f),
                                        border = BorderStroke(1.dp, CardBorder),
                                        modifier = Modifier.size(48.dp)
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.Center,
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Text(
                                                text = preset.typeEnum.icon,
                                                fontSize = 24.sp
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = preset.title,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 14.sp,
                                                color = TextPrimary,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            if (preset.isPremium) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Surface(
                                                    shape = RoundedCornerShape(4.dp),
                                                    color = Color(0xFFFFD700)
                                                ) {
                                                    Text(
                                                        text = "PRO",
                                                        fontSize = 9.sp,
                                                        fontWeight = FontWeight.ExtraBold,
                                                        color = Color.Black,
                                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(4.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = preset.typeEnum.displayName,
                                                fontSize = 11.sp,
                                                color = TextSecondary
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "• ${preset.downloads} users",
                                                fontSize = 11.sp,
                                                color = NeonCyan
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.width(10.dp))

                                // Apply button
                                if (isActive) {
                                    Button(
                                        onClick = {},
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan.copy(alpha = 0.2f)),
                                        border = BorderStroke(1.dp, NeonCyan),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = NeonCyan,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "Active",
                                            color = NeonCyan,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                } else {
                                    Button(
                                        onClick = {
                                            TouchEffectManager.setActivePreset(preset)
                                            simPreset = preset
                                            shockwaveCounter++
                                            scope.launch {
                                                repository.applyPreset(preset.id)
                                            }
                                            Toast.makeText(context, "✅ Applied: ${preset.title}", Toast.LENGTH_SHORT).show()
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = NeonCyan,
                                            contentColor = Color.Black
                                        ),
                                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                                    ) {
                                        Text(
                                            text = "Apply",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}
