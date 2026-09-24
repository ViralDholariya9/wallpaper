package com.parallax.wallpaper.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.animateDpAsState
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
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.wallpaper.data.DynamicIslandRepository
import com.parallax.wallpaper.data.api.DynamicIslandThemeItem
import com.parallax.wallpaper.island.CameraHolePosition
import com.parallax.wallpaper.island.DynamicIslandCapsule
import com.parallax.wallpaper.island.DynamicIslandManager
import com.parallax.wallpaper.island.IslandEvent
import com.parallax.wallpaper.ui.theme.DeepObsidian
import com.parallax.wallpaper.ui.theme.NeonCyan
import com.parallax.wallpaper.ui.theme.NeonPink
import com.parallax.wallpaper.ui.theme.NeonPurple
import com.parallax.wallpaper.ui.theme.SurfaceDark
import com.parallax.wallpaper.ui.theme.TextPrimary
import com.parallax.wallpaper.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DynamicIslandScreen(
    repository: DynamicIslandRepository,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val themes by repository.themes.collectAsState()
    val isEnabled by DynamicIslandManager.isEnabled.collectAsState()
    val activeTheme by DynamicIslandManager.activeTheme.collectAsState()
    val currentEvent by DynamicIslandManager.currentEvent.collectAsState()
    val xOffset by DynamicIslandManager.xOffsetDp.collectAsState()
    val yOffset by DynamicIslandManager.yOffsetDp.collectAsState()
    val cameraPos by DynamicIslandManager.cameraPosition.collectAsState()

    var hasOverlayPermission by remember {
        mutableStateOf(DynamicIslandManager.canDrawOverlays(context))
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "🏝️ Dynamic Island Studio",
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp
                            )
                        }
                        Text(
                            text = "Smart punch-hole notch notification capsule",
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
                    IconButton(onClick = { repository.fetchThemes() }) {
                        Icon(
                            imageVector = Icons.Filled.Refresh,
                            contentDescription = "Refresh",
                            tint = NeonCyan
                        )
                    }
                    Switch(
                        checked = isEnabled,
                        onCheckedChange = { checked ->
                            DynamicIslandManager.setEnabled(context, checked)
                            if (checked && !hasOverlayPermission) {
                                Toast.makeText(
                                    context,
                                    "Please grant overlay permission for system-wide Dynamic Island",
                                    Toast.LENGTH_LONG
                                ).show()
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
            // Permission Alert Banner
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
                                    text = "Allow ReWall to display Dynamic Island over all other apps and screens.",
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.5.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                            Button(
                                onClick = {
                                    DynamicIslandManager.openOverlaySettings(context)
                                    hasOverlayPermission = DynamicIslandManager.canDrawOverlays(context)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "Enable",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // Phone Simulator Stage Card
            item(span = { GridItemSpan(2) }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF10B981))
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Live Capsule Simulator",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.5.sp
                                )
                            }
                            Text(
                                text = "Tap capsule to morph",
                                color = NeonCyan,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Smartphone Bezel Frame
                        Box(
                            modifier = Modifier
                                .width(280.dp)
                                .height(200.dp)
                                .shadow(16.dp, RoundedCornerShape(26.dp), spotColor = NeonCyan)
                                .clip(RoundedCornerShape(26.dp))
                                .background(Color(0xFF07090F))
                                .border(2.dp, Color(0xFF1E293B), RoundedCornerShape(26.dp))
                                .padding(top = 10.dp),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            // Camera Lens Hole
                            val punchHoleX by animateDpAsState(targetValue = xOffset.dp, label = "punchX")
                            val punchHoleY by animateDpAsState(targetValue = yOffset.dp, label = "punchY")

                            Box(
                                modifier = Modifier
                                    .offset(x = punchHoleX, y = punchHoleY)
                                    .size(11.dp)
                                    .clip(CircleShape)
                                    .background(Color.Black)
                                    .border(1.dp, Color(0xFF334155), CircleShape)
                            )

                            // Dynamic Island Capsule
                            DynamicIslandCapsule(
                                theme = activeTheme,
                                event = currentEvent,
                                modifier = Modifier.offset(x = punchHoleX, y = punchHoleY - 6.dp),
                                onCapsuleClick = {
                                    DynamicIslandManager.toggleCompactExpanded()
                                }
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Event simulation buttons row
                        Text(
                            text = "TEST SMART EVENT TRIGGERS",
                            color = TextSecondary,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            EventPillButton(
                                label = "⚡ Charge",
                                isSelected = currentEvent is IslandEvent.Charging,
                                modifier = Modifier.weight(1f)
                            ) {
                                DynamicIslandManager.triggerEvent(IslandEvent.Charging())
                            }
                            EventPillButton(
                                label = "🎵 Music",
                                isSelected = currentEvent is IslandEvent.Music,
                                modifier = Modifier.weight(1f)
                            ) {
                                DynamicIslandManager.triggerEvent(IslandEvent.Music())
                            }
                            EventPillButton(
                                label = "🎧 Buds",
                                isSelected = currentEvent is IslandEvent.Earbuds,
                                modifier = Modifier.weight(1f)
                            ) {
                                DynamicIslandManager.triggerEvent(IslandEvent.Earbuds())
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            EventPillButton(
                                label = "📞 Call",
                                isSelected = currentEvent is IslandEvent.IncomingCall,
                                modifier = Modifier.weight(1f)
                            ) {
                                DynamicIslandManager.triggerEvent(IslandEvent.IncomingCall())
                            }
                            EventPillButton(
                                label = "💬 Alert",
                                isSelected = currentEvent is IslandEvent.NotificationAlert,
                                modifier = Modifier.weight(1f)
                            ) {
                                DynamicIslandManager.triggerEvent(IslandEvent.NotificationAlert())
                            }
                            EventPillButton(
                                label = "👁️ Compact",
                                isSelected = currentEvent is IslandEvent.Compact,
                                modifier = Modifier.weight(1f)
                            ) {
                                DynamicIslandManager.triggerEvent(IslandEvent.Compact)
                            }
                        }
                    }
                }
            }

            // Punch-Hole Calibration Card
            item(span = { GridItemSpan(2) }) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = SurfaceDark),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Filled.Tune,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Camera Hole Calibration",
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Text(
                                text = "X: ${xOffset}dp | Y: ${yOffset}dp",
                                color = NeonCyan,
                                fontSize = 11.5.sp,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Camera Position Presets
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            CameraPresetChip(
                                label = "Left Cutout",
                                isSelected = cameraPos == CameraHolePosition.LEFT,
                                modifier = Modifier.weight(1f)
                            ) {
                                DynamicIslandManager.setCameraPosition(context, CameraHolePosition.LEFT)
                            }
                            CameraPresetChip(
                                label = "Center Notch",
                                isSelected = cameraPos == CameraHolePosition.CENTER,
                                modifier = Modifier.weight(1f)
                            ) {
                                DynamicIslandManager.setCameraPosition(context, CameraHolePosition.CENTER)
                            }
                            CameraPresetChip(
                                label = "Right Cutout",
                                isSelected = cameraPos == CameraHolePosition.RIGHT,
                                modifier = Modifier.weight(1f)
                            ) {
                                DynamicIslandManager.setCameraPosition(context, CameraHolePosition.RIGHT)
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Fine Adjustment Slider: X Position
                        Text(
                            text = "Horizontal Offset (X): ${xOffset} dp",
                            color = TextSecondary,
                            fontSize = 11.5.sp
                        )
                        Slider(
                            value = xOffset.toFloat(),
                            onValueChange = {
                                DynamicIslandManager.setOffsets(context, it.toInt(), yOffset)
                            },
                            valueRange = -80f..80f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonCyan,
                                activeTrackColor = NeonCyan
                            )
                        )

                        // Fine Adjustment Slider: Y Position
                        Text(
                            text = "Vertical Offset (Y): ${yOffset} dp",
                            color = TextSecondary,
                            fontSize = 11.5.sp
                        )
                        Slider(
                            value = yOffset.toFloat(),
                            onValueChange = {
                                DynamicIslandManager.setOffsets(context, xOffset, it.toInt())
                            },
                            valueRange = 0f..50f,
                            colors = SliderDefaults.colors(
                                thumbColor = NeonPurple,
                                activeTrackColor = NeonPurple
                            )
                        )
                    }
                }
            }

            // Catalog Header
            item(span = { GridItemSpan(2) }) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "🎨 Capsule Themes & Skins",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${themes.size} Available",
                        color = TextSecondary,
                        fontSize = 12.sp
                    )
                }
            }

            // Theme Cards Grid
            items(themes, key = { it.id }) { theme ->
                val isSelected = activeTheme.id == theme.id

                ThemeCardItem(
                    theme = theme,
                    isSelected = isSelected,
                    onSelect = {
                        DynamicIslandManager.setActiveTheme(context, theme)
                        repository.applyTheme(theme)
                        Toast.makeText(context, "Applied ${theme.title}", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
private fun EventPillButton(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) NeonCyan.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)
            )
            .border(
                1.dp,
                if (isSelected) NeonCyan else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) NeonCyan else Color.White
        )
    }
}

@Composable
private fun CameraPresetChip(
    label: String,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(
                if (isSelected) NeonPurple.copy(alpha = 0.25f) else Color.White.copy(alpha = 0.04f)
            )
            .border(
                1.dp,
                if (isSelected) NeonPurple else Color.White.copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontSize = 11.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) NeonPurple else TextSecondary
        )
    }
}

@Composable
private fun ThemeCardItem(
    theme: DynamicIslandThemeItem,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceDark),
        border = BorderStroke(
            1.dp,
            if (isSelected) NeonCyan else Color.White.copy(alpha = 0.08f)
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Mini capsule preview badge
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFF080B12)),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .width(90.dp)
                        .height(24.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "⚡", fontSize = 9.sp)
                        Text(
                            text = "88%",
                            fontSize = 8.5.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = theme.title,
                color = Color.White,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = theme.styleType.uppercase(),
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold
                )

                if (isSelected) {
                    Icon(
                        imageVector = Icons.Filled.Check,
                        contentDescription = "Active",
                        tint = NeonCyan,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
