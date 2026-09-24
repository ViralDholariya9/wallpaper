package com.parallax.wallpaper.ui.screens

import android.graphics.Bitmap
import android.graphics.Matrix
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.CropOriginal
import androidx.compose.material.icons.filled.CropPortrait
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.parallax.wallpaper.model.WallpaperItem
import com.parallax.wallpaper.ui.components.SetWallpaperBottomSheet
import com.parallax.wallpaper.ui.theme.CardBorder
import com.parallax.wallpaper.ui.theme.CardDark
import com.parallax.wallpaper.ui.theme.DeepObsidian
import com.parallax.wallpaper.ui.theme.NeonCyan
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

enum class CropPreset(val title: String, val ratio: Float, val icon: ImageVector) {
    FULL_SCREEN("9:16 Full", 9f / 16f, Icons.Default.CropPortrait),
    SQUARE("1:1 Square", 1f, Icons.Default.CropSquare),
    PORTRAIT("4:5 Post", 4f / 5f, Icons.Default.CropOriginal)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WallpaperEditorScreen(
    wallpaper: WallpaperItem,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var rotationAngle by remember { mutableFloatStateOf(0f) }
    var scaleX by remember { mutableFloatStateOf(1f) }
    var scaleY by remember { mutableFloatStateOf(1f) }
    var selectedPreset by remember { mutableStateOf(CropPreset.FULL_SCREEN) }

    var showBottomSheet by remember { mutableStateOf(false) }
    var isProcessing by remember { mutableStateOf(false) }
    var loadedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(wallpaper.previewUrl) {
        withContext(Dispatchers.IO) {
            loadedBitmap = WallpaperHelper.fetchBitmap(context, wallpaper.previewUrl)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Bar
            TopAppBar(
                title = {
                    Text(
                        text = "Crop & Edit Wallpaper",
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
                actions = {
                    IconButton(
                        onClick = {
                            scope.launch {
                                isProcessing = true
                                loadedBitmap?.let { bmp ->
                                    val matrix = Matrix().apply {
                                        postRotate(rotationAngle)
                                        postScale(scaleX, scaleY)
                                    }
                                    val transformed = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                                    val uri = WallpaperHelper.saveToGallery(context, transformed, "${wallpaper.title}_edited")
                                    if (uri != null) {
                                        Toast.makeText(context, "Saved edited wallpaper!", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Failed to save", Toast.LENGTH_SHORT).show()
                                    }
                                }
                                isProcessing = false
                            }
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Save",
                            tint = NeonCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepObsidian)
            )

            // Image Preview Canvas with Crop Box
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .aspectRatio(selectedPreset.ratio)
                        .clip(RoundedCornerShape(16.dp))
                        .border(2.dp, NeonCyan, RoundedCornerShape(16.dp))
                        .background(Color.Black)
                ) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(wallpaper.previewUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "Editor Preview",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxSize()
                            .rotate(rotationAngle)
                            .scale(scaleX, scaleY)
                    )

                    // Crop Grid Rule-of-Thirds overlay
                    CropGridOverlay()
                }
            }

            // Tools & Presets Controls Bottom Sheet Container
            Surface(
                color = SurfaceDark,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Presets Row (Full Body, Square, Half Length)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        CropPreset.values().forEach { preset ->
                            val isSelected = preset == selectedPreset
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedPreset = preset },
                                leadingIcon = {
                                    Icon(
                                        imageVector = preset.icon,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp)
                                    )
                                },
                                label = { Text(preset.title) },
                                colors = FilterChipDefaults.filterChipColors(
                                    containerColor = CardDark,
                                    labelColor = TextSecondary,
                                    selectedContainerColor = NeonPurple,
                                    selectedLabelColor = Color.White
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    borderColor = if (isSelected) NeonCyan else CardBorder,
                                    selectedBorderColor = NeonCyan,
                                    enabled = true,
                                    selected = isSelected
                                ),
                                shape = RoundedCornerShape(12.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Editing Tools: Rotate 90°, Flip Horizontal, Reset
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EditorToolButton(
                            icon = Icons.AutoMirrored.Filled.RotateRight,
                            title = "Rotate 90°",
                            onClick = { rotationAngle = (rotationAngle + 90f) % 360f }
                        )

                        EditorToolButton(
                            icon = Icons.Default.Flip,
                            title = "Flip H",
                            onClick = { scaleX = -scaleX }
                        )

                        EditorToolButton(
                            icon = Icons.Default.Crop,
                            title = "Reset",
                            onClick = {
                                rotationAngle = 0f
                                scaleX = 1f
                                scaleY = 1f
                            }
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Set As Wallpaper CTA Button
                    Button(
                        onClick = { showBottomSheet = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                        enabled = !isProcessing
                    ) {
                        if (isProcessing) {
                            CircularProgressIndicator(
                                color = Color.White,
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Wallpaper,
                                contentDescription = null,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Set As Wallpaper",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }
                }
            }
        }

        // Apply Wallpaper Dialog
        if (showBottomSheet) {
            SetWallpaperBottomSheet(
                isParallax = true,
                onDismiss = { showBottomSheet = false },
                onSetStaticTarget = { target ->
                    scope.launch {
                        isProcessing = true
                        loadedBitmap?.let { bmp ->
                            val matrix = Matrix().apply {
                                postRotate(rotationAngle)
                                postScale(scaleX, scaleY)
                            }
                            val transformed = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                            val success = WallpaperHelper.setWallpaper(context, transformed, target)
                            if (success) {
                                Toast.makeText(context, "Cropped Wallpaper Applied Successfully!", Toast.LENGTH_SHORT).show()
                                onBack()
                            } else {
                                Toast.makeText(context, "Failed to set wallpaper", Toast.LENGTH_SHORT).show()
                            }
                        }
                        isProcessing = false
                    }
                },
                onApplyLiveParallax = {
                    scope.launch {
                        isProcessing = true
                        loadedBitmap?.let { bmp ->
                            Toast.makeText(context, "Preparing 3D Live Wallpaper...", Toast.LENGTH_SHORT).show()
                            val matrix = Matrix().apply {
                                postRotate(rotationAngle)
                                postScale(scaleX, scaleY)
                            }
                            val transformed = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, matrix, true)
                            val success = LiveWallpaperManager.setActiveBitmap(context, transformed, wallpaper.title)
                            if (success) {
                                val launched = LiveWallpaperHelper.launchLiveWallpaperChooser(context)
                                if (launched) {
                                    Toast.makeText(context, "Tap 'Set Wallpaper' in system preview", Toast.LENGTH_LONG).show()
                                }
                            } else {
                                Toast.makeText(context, "Failed to prepare live wallpaper", Toast.LENGTH_SHORT).show()
                            }
                        }
                        isProcessing = false
                    }
                }
            )
        }
    }
}

@Composable
private fun EditorToolButton(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Surface(
            shape = CircleShape,
            color = CardDark,
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
            modifier = Modifier.size(46.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = NeonCyan,
                modifier = Modifier
                    .padding(10.dp)
                    .size(22.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
        )
    }
}

@Composable
private fun CropGridOverlay() {
    Column(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.25f))
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(Color.White.copy(alpha = 0.25f))
        )
        Spacer(modifier = Modifier.weight(1f))
    }
    Row(modifier = Modifier.fillMaxSize()) {
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(Color.White.copy(alpha = 0.25f))
        )
        Spacer(modifier = Modifier.weight(1f))
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(Color.White.copy(alpha = 0.25f))
        )
        Spacer(modifier = Modifier.weight(1f))
    }
}
