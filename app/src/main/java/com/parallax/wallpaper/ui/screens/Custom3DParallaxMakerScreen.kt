package com.parallax.wallpaper.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wallpaper
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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.parallax.wallpaper.gl.DepthMapGenerator
import com.parallax.wallpaper.gl.DepthPreset
import com.parallax.wallpaper.gl.GLWallpaperTextureView
import com.parallax.wallpaper.sensor.ParallaxSensorManager
import com.parallax.wallpaper.ui.theme.CardBorder
import com.parallax.wallpaper.ui.theme.CardDark
import com.parallax.wallpaper.ui.theme.DeepObsidian
import com.parallax.wallpaper.ui.theme.NeonCyan
import com.parallax.wallpaper.ui.theme.NeonPink
import com.parallax.wallpaper.ui.theme.NeonPurple
import com.parallax.wallpaper.ui.theme.SurfaceDark
import com.parallax.wallpaper.ui.theme.TextPrimary
import com.parallax.wallpaper.ui.theme.TextSecondary
import com.parallax.wallpaper.utils.LiveWallpaperHelper
import com.parallax.wallpaper.utils.LiveWallpaperManager
import com.parallax.wallpaper.utils.WallpaperHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

enum class ParticleTheme(val title: String, val rgb: Triple<Float, Float, Float>?) {
    OFF("No Particles", null),
    CYAN_NEON("Cyan Stardust", Triple(0.0f, 0.9f, 1.0f)),
    GOLDEN_SPARK("Gold Aura", Triple(1.0f, 0.85f, 0.2f)),
    VIOLET_PULSE("Cyber Violet", Triple(0.85f, 0.25f, 1.0f)),
    EMERALD("Emerald Glow", Triple(0.2f, 1.0f, 0.45f))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Custom3DParallaxMakerScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val sensorManager = remember { ParallaxSensorManager(context) }
    val tiltOffset by sensorManager.parallaxOffset.collectAsState()

    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var depthBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    // Studio Settings
    var selectedPreset by remember { mutableStateOf(DepthPreset.PORTRAIT_SUBJECT) }
    var focusY by remember { mutableFloatStateOf(0.45f) }
    var depthIntensity by remember { mutableFloatStateOf(0.05f) }
    var invertDepth by remember { mutableStateOf(false) }
    var selectedParticleTheme by remember { mutableStateOf(ParticleTheme.CYAN_NEON) }

    fun recomputeDepthMap(bmp: Bitmap) {
        scope.launch(Dispatchers.Default) {
            val newDepth = DepthMapGenerator.generate(
                sourceBitmap = bmp,
                preset = selectedPreset,
                focusY = focusY,
                invert = invertDepth
            )
            withContext(Dispatchers.Main) {
                depthBitmap = newDepth
            }
        }
    }

    // Modern Android Photo Picker (zero invasive permissions)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            scope.launch {
                isProcessing = true
                val loaded = decodeUriToBitmap(context, uri)
                if (loaded != null) {
                    val scaled = scaleBitmap(loaded, 2048)
                    selectedBitmap = scaled
                    recomputeDepthMap(scaled)
                    Toast.makeText(context, "Photo loaded! Tilt phone to preview 3D depth", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Failed to load selected photo", Toast.LENGTH_SHORT).show()
                }
                isProcessing = false
            }
        }
    }

    // Sample Photos for testing if user has no photos ready
    val samples = listOf(
        "Cyberpunk Neon" to "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=1080&auto=format&fit=crop",
        "Deep Galaxy" to "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?q=80&w=1080&auto=format&fit=crop",
        "Nature Waterfall" to "https://images.unsplash.com/photo-1448375240586-882707db888b?q=80&w=1080&auto=format&fit=crop",
        "Obsidian Peaks" to "https://images.unsplash.com/photo-1506744038136-46273834b3fb?q=80&w=1080&auto=format&fit=crop"
    )

    DisposableEffect(Unit) {
        sensorManager.startListening()
        onDispose {
            sensorManager.stopListening()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
    ) {
        // TOP APP BAR
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "DIY 3D Parallax Studio",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NeonPurple.copy(alpha = 0.25f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                    ) {
                        Text(
                            text = "MAKER",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        )
                    }
                }
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
            actions = {
                if (selectedBitmap != null) {
                    IconButton(
                        onClick = {
                            photoPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Change Photo",
                            tint = NeonCyan
                        )
                    }
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepObsidian)
        )

        if (selectedBitmap == null) {
            // STEP 1: EMPTY STATE / PICK PHOTO FROM GALLERY
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Glassmorphism Hero Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(
                            1.dp,
                            Brush.linearGradient(listOf(NeonCyan, NeonPurple, NeonPink)),
                            RoundedCornerShape(24.dp)
                        ),
                    colors = CardDefaults.cardColors(containerColor = CardDark)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = NeonPurple.copy(alpha = 0.25f),
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, NeonCyan),
                            modifier = Modifier.size(80.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier
                                    .padding(20.dp)
                                    .size(40.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "Turn Any Photo into 3D",
                            style = MaterialTheme.typography.headlineSmall.copy(
                                fontWeight = FontWeight.ExtraBold,
                                color = TextPrimary
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Select any photo from your gallery. Our OpenGL engine will generate depth maps, gyroscope tracking, and floating stardust to make it move in 3D!",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AddPhotoAlternate,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Pick Photo from Gallery",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(28.dp))

                // Quick Demo Presets
                Text(
                    text = "OR TRY WITH SAMPLE PHOTOS",
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextSecondary
                    )
                )

                Spacer(modifier = Modifier.height(14.dp))

                samples.chunked(2).forEach { pair ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        pair.forEach { (name, url) ->
                            Card(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(60.dp)
                                    .clickable {
                                        scope.launch {
                                            isProcessing = true
                                            val bmp = WallpaperHelper.fetchBitmap(context, url)
                                            if (bmp != null) {
                                                selectedBitmap = bmp
                                                recomputeDepthMap(bmp)
                                            }
                                            isProcessing = false
                                        }
                                    },
                                colors = CardDefaults.cardColors(containerColor = CardDark),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = name,
                                        style = MaterialTheme.typography.bodySmall.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            color = TextPrimary
                                        )
                                    )
                                }
                            }
                        }
                    }
                }

                if (isProcessing) {
                    Spacer(modifier = Modifier.height(20.dp))
                    CircularProgressIndicator(color = NeonCyan)
                }
            }
        } else {
            // STEP 2: INTERACTIVE 3D STUDIO CANVAS & CONTROLS
            val currentBmp = selectedBitmap!!

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // 3D PREVIEW CONTAINER
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .border(1.5.dp, CardBorder, RoundedCornerShape(24.dp))
                        .background(Color.Black)
                ) {
                    AndroidView(
                        factory = { ctx ->
                            GLWallpaperTextureView(ctx).apply {
                                setBitmap(currentBmp, depthBitmap)
                                setDepthIntensity(depthIntensity)
                                setParticlesEnabled(selectedParticleTheme.rgb != null)
                                selectedParticleTheme.rgb?.let { (r, g, b) ->
                                    setParticleColor(r, g, b)
                                }
                            }
                        },
                        update = { glView ->
                            glView.setBitmap(currentBmp, depthBitmap)
                            glView.setSensorOffset(tiltOffset.x, tiltOffset.y)
                            glView.setDepthIntensity(depthIntensity)
                            glView.setParticlesEnabled(selectedParticleTheme.rgb != null)
                            selectedParticleTheme.rgb?.let { (r, g, b) ->
                                glView.setParticleColor(r, g, b)
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )

                    // Interactive Tip Overlay Banner
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.65f),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Sensors,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tilt device to test 3D • Tap to ripple",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            )
                        }
                    }
                }

                // CONTROLS & ADJUSTMENTS PANEL
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                ) {
                    // Section 1: 3D Depth Presets
                    Text(
                        text = "3D DEPTH ALGORITHM",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        DepthPreset.values().forEach { preset ->
                            val isSelected = preset == selectedPreset
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedPreset = preset
                                    recomputeDepthMap(currentBmp)
                                },
                                label = { Text(preset.title) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonPurple,
                                    selectedLabelColor = Color.White,
                                    containerColor = CardDark,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) NeonCyan else CardBorder,
                                    selectedBorderColor = NeonCyan,
                                    enabled = true,
                                    selected = isSelected
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Section 2: Subject Focal Center Height
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Subject Focal Point",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = if (focusY < 0.35f) "Top (Face)" else if (focusY > 0.65f) "Bottom" else "Center",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Slider(
                        value = focusY,
                        onValueChange = {
                            focusY = it
                            recomputeDepthMap(currentBmp)
                        },
                        valueRange = 0.2f..0.8f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonPurple,
                            inactiveTrackColor = CardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Section 3: 3D Depth Intensity Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Parallax Depth Intensity",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = String.format("%.0f%%", (depthIntensity / 0.10f) * 100),
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                    Slider(
                        value = depthIntensity,
                        onValueChange = { depthIntensity = it },
                        valueRange = 0.02f..0.10f,
                        colors = SliderDefaults.colors(
                            thumbColor = NeonCyan,
                            activeTrackColor = NeonPurple,
                            inactiveTrackColor = CardBorder
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Section 4: Stardust Particle Overlay
                    Text(
                        text = "FLOATING PARTICLES",
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        ParticleTheme.values().forEach { theme ->
                            val isSelected = theme == selectedParticleTheme
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedParticleTheme = theme },
                                label = { Text(theme.title) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonPurple,
                                    selectedLabelColor = Color.White,
                                    containerColor = CardDark,
                                    labelColor = TextSecondary
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) NeonCyan else CardBorder,
                                    selectedBorderColor = NeonCyan,
                                    enabled = true,
                                    selected = isSelected
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Section 5: Invert Depth Toggle
                    Card(
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        shape = RoundedCornerShape(14.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Flip,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Invert Depth",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                    )
                                    Text(
                                        text = "Flip foreground and background layers",
                                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                                    )
                                }
                            }
                            Switch(
                                checked = invertDepth,
                                onCheckedChange = {
                                    invertDepth = it
                                    recomputeDepthMap(currentBmp)
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = NeonPurple
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ACTION BUTTON: Apply as 3D Live Wallpaper
                    Button(
                        onClick = {
                            scope.launch {
                                isProcessing = true
                                Toast.makeText(context, "Applying your custom 3D Live Wallpaper...", Toast.LENGTH_SHORT).show()
                                val success = LiveWallpaperManager.setActiveBitmap(
                                    context = context,
                                    bitmap = currentBmp,
                                    title = "My Custom 3D Wallpaper"
                                )
                                isProcessing = false
                                if (success) {
                                    val launched = LiveWallpaperHelper.launchLiveWallpaperChooser(context)
                                    if (launched) {
                                        Toast.makeText(context, "Tap 'Set Wallpaper' in system preview", Toast.LENGTH_LONG).show()
                                    }
                                } else {
                                    Toast.makeText(context, "Failed to apply live wallpaper", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(color = Color.Black, modifier = Modifier.size(24.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.Wallpaper,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "Set as 3D Live Wallpaper",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Black
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Secondary Action: Save to Gallery
                    Button(
                        onClick = {
                            scope.launch {
                                isProcessing = true
                                val uri = WallpaperHelper.saveToGallery(context, currentBmp, "DIY_3D_${System.currentTimeMillis()}")
                                isProcessing = false
                                if (uri != null) {
                                    Toast.makeText(context, "Saved to Pictures/ParallaxWallpapers!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Failed to save photo", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Save Photo to Gallery",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(30.dp))
                }
            }
        }
    }
}

private suspend fun decodeUriToBitmap(context: Context, uri: Uri): Bitmap? = withContext(Dispatchers.IO) {
    try {
        val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
        inputStream?.use {
            val options = BitmapFactory.Options().apply {
                inPreferredConfig = Bitmap.Config.ARGB_8888
                inMutable = true
            }
            BitmapFactory.decodeStream(it, null, options)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private fun scaleBitmap(source: Bitmap, maxDim: Int): Bitmap {
    val w = source.width
    val h = source.height
    if (w <= maxDim && h <= maxDim) return source

    val scale = if (w > h) maxDim.toFloat() / w else maxDim.toFloat() / h
    val matrix = Matrix().apply { postScale(scale, scale) }
    return Bitmap.createBitmap(source, 0, 0, w, h, matrix, true)
}
