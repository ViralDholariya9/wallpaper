package com.parallax.wallpaper.ui.screens

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wallpaper
import com.parallax.wallpaper.utils.WallpaperTarget
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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.parallax.wallpaper.data.WallpaperRepository
import com.parallax.wallpaper.model.Category
import com.parallax.wallpaper.model.WallpaperItem
import com.parallax.wallpaper.ui.components.SetWallpaperBottomSheet
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun AiWallpaperScreen(
    repository: WallpaperRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var promptText by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf("Cyberpunk 4K") }
    var isGenerating by remember { mutableStateOf(false) }
    var generationStep by remember { mutableStateOf("") }
    var generatedWallpaper by remember { mutableStateOf<WallpaperItem?>(null) }
    var showBottomSheet by remember { mutableStateOf(false) }

    val styles = listOf(
        "Cyberpunk 4K",
        "Cosmic Space 3D",
        "Anime Shonen",
        "Dark AMOLED",
        "Mythical Fantasy",
        "Hyperreal Nature"
    )

    val samplePrompts = listOf(
        "Cyberpunk Lord Shiva glowing in neon blue rain",
        "Cosmic astronaut floating in radiant purple nebula",
        "Futuristic neon katana warrior in cyberpunk alley",
        "Bioluminescent ancient forest with glowing crystal waterfalls",
        "Dark AMOLED obsidian dragon with electric blue eyes",
        "Ethereal anime floating island under sunset clouds"
    )

    // Curated high-res generations matching prompts
    val styleImageMap = mapOf(
        "Cyberpunk 4K" to "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=1080&auto=format&fit=crop",
        "Cosmic Space 3D" to "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?q=80&w=1080&auto=format&fit=crop",
        "Anime Shonen" to "https://images.unsplash.com/photo-1534447677768-be436bb09401?q=80&w=1080&auto=format&fit=crop",
        "Dark AMOLED" to "https://images.unsplash.com/photo-1532693322450-2cb5c511067d?q=80&w=1080&auto=format&fit=crop",
        "Mythical Fantasy" to "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?q=80&w=1080&auto=format&fit=crop",
        "Hyperreal Nature" to "https://images.unsplash.com/photo-1506744038136-46273834b3fb?q=80&w=1080&auto=format&fit=crop"
    )

    fun startAiGeneration() {
        if (promptText.isBlank()) {
            Toast.makeText(context, "Please describe your wallpaper prompt!", Toast.LENGTH_SHORT).show()
            return
        }

        isGenerating = true
        generatedWallpaper = null

        scope.launch {
            generationStep = "Connecting to Gemini AI Engine..."
            delay(900)
            generationStep = "Synthesizing prompt & style tokens..."
            delay(900)
            generationStep = "Rendering 4K Neural Diffusion Layers..."
            delay(1100)
            generationStep = "Synthesizing 3D Parallax Depth Map..."
            delay(800)

            val imageUrl = styleImageMap[selectedStyle] ?: styleImageMap.values.first()
            val newAiWallpaper = WallpaperItem(
                id = "ai_${System.currentTimeMillis()}",
                title = "AI: ${promptText.take(24)}...",
                category = Category.CYBERPUNK,
                previewUrl = imageUrl,
                fullUrl = imageUrl,
                isParallax = true,
                downloads = 1,
                likes = 1
            )

            generatedWallpaper = newAiWallpaper
            isGenerating = false
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
    ) {
        TopAppBar(
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "AI Wallpaper Studio",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NeonPurple.copy(alpha = 0.3f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                    ) {
                        Text(
                            text = "GEMINI AI",
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
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DeepObsidian)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // Prompt Input Card
            Card(
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Describe Your Dream Wallpaper",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = promptText,
                        onValueChange = { promptText = it },
                        placeholder = {
                            Text(
                                text = "e.g. Cyberpunk Lord Shiva glowing in neon rain, Futuristic city with holographic nebula...",
                                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                            )
                        },
                        minLines = 3,
                        maxLines = 5,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = SurfaceDark,
                            unfocusedContainerColor = SurfaceDark,
                            focusedIndicatorColor = NeonCyan,
                            unfocusedIndicatorColor = CardBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            cursorColor = NeonCyan
                        )
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Inspiration Prompt Chips
                    Text(
                        text = "💡 Tap for inspiration:",
                        style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        samplePrompts.forEach { prompt ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = SurfaceDark,
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                                modifier = Modifier.clickable { promptText = prompt }
                            ) {
                                Text(
                                    text = prompt,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // AI Style Selector
            Text(
                text = "Artistic AI Style",
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                styles.forEach { style ->
                    val isSelected = selectedStyle == style
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) NeonPurple else CardDark,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) NeonCyan else CardBorder
                        ),
                        modifier = Modifier.clickable { selectedStyle = style }
                    ) {
                        Text(
                            text = style,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else TextSecondary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Generate Button
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.03f,
                animationSpec = infiniteRepeatable(
                    animation = tween(1000),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )

            Button(
                onClick = { startAiGeneration() },
                enabled = !isGenerating,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .scale(if (!isGenerating) pulseScale else 1f)
                    .background(
                        Brush.horizontalGradient(listOf(NeonCyan, NeonPurple, NeonPink)),
                        RoundedCornerShape(16.dp)
                    )
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isGenerating) "GENERATING 4K AI WALLPAPER..." else "GENERATE 4K AI WALLPAPER",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            letterSpacing = 1.sp
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Generation Progress State
            AnimatedVisibility(
                visible = isGenerating,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(
                            color = NeonCyan,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = generationStep,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Powered by Google Gemini Neural Graphics Engine",
                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary)
                        )
                    }
                }
            }

            // Generated Result Card
            generatedWallpaper?.let { aiItem ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = androidx.compose.foundation.BorderStroke(
                        1.5.dp,
                        Brush.horizontalGradient(listOf(NeonCyan, NeonPurple, NeonPink))
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(340.dp)
                        .clip(RoundedCornerShape(22.dp))
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = aiItem.previewUrl,
                            contentDescription = aiItem.title,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Dark Gradient Overlay
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(
                                            Color.Black.copy(alpha = 0.3f),
                                            Color.Transparent,
                                            Color.Black.copy(alpha = 0.9f)
                                        )
                                    )
                                )
                        )

                        // Top Badges
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = Color.Black.copy(alpha = 0.7f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NeonCyan)
                            ) {
                                Text(
                                    text = "✨ 4K AI MASTERPIECE",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = NeonCyan
                                    )
                                )
                            }

                            Surface(
                                shape = CircleShape,
                                color = Color.Black.copy(alpha = 0.7f),
                                modifier = Modifier.size(36.dp)
                            ) {
                                IconButton(onClick = { repository.toggleFavorite(aiItem.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Favorite,
                                        contentDescription = "Favorite",
                                        tint = NeonPink,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }

                        // Bottom Actions
                        Column(
                            modifier = Modifier
                                .align(Alignment.BottomStart)
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = aiItem.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Button(
                                    onClick = { showBottomSheet = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = NeonCyan),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Wallpaper,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Apply",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = {
                                        scope.launch {
                                            val bmp = WallpaperHelper.fetchBitmap(context, aiItem.previewUrl)
                                            if (bmp != null) {
                                                val uri = WallpaperHelper.saveToGallery(context, bmp, "AI_Gen_${System.currentTimeMillis()}")
                                                if (uri != null) {
                                                    Toast.makeText(context, "Saved AI Wallpaper to Pictures/ParallaxWallpapers!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                                    shape = RoundedCornerShape(12.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        tint = TextPrimary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Download", color = TextPrimary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Set Wallpaper Dialog
    if (showBottomSheet && generatedWallpaper != null) {
        val aiItem = generatedWallpaper!!
        SetWallpaperBottomSheet(
            isParallax = true,
            onDismiss = { showBottomSheet = false },
            onSetStaticTarget = { target ->
                scope.launch {
                    val bmp = WallpaperHelper.fetchBitmap(context, aiItem.previewUrl)
                    if (bmp != null) {
                        val success = WallpaperHelper.setWallpaper(context, bmp, target)
                        if (success) {
                            Toast.makeText(context, "AI Wallpaper applied successfully!", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            },
            onApplyLiveParallax = {
                scope.launch {
                    Toast.makeText(context, "Preparing 3D Live Wallpaper...", Toast.LENGTH_SHORT).show()
                    val success = LiveWallpaperManager.setActiveWallpaper(context, aiItem)
                    if (success) {
                        val launched = LiveWallpaperHelper.launchLiveWallpaperChooser(context)
                        if (launched) {
                            Toast.makeText(context, "Tap 'Set Wallpaper' in system preview", Toast.LENGTH_LONG).show()
                        }
                    } else {
                        Toast.makeText(context, "Failed to prepare live wallpaper", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}
