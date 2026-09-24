package com.parallax.wallpaper.ui.screens

import android.widget.Toast
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.parallax.wallpaper.data.DuoWallpaperRepository
import com.parallax.wallpaper.data.api.ApiClient
import com.parallax.wallpaper.model.DuoWallpaperItem
import com.parallax.wallpaper.ui.theme.CardBorder
import com.parallax.wallpaper.ui.theme.CardDark
import com.parallax.wallpaper.ui.theme.DeepObsidian
import com.parallax.wallpaper.ui.theme.NeonCyan
import com.parallax.wallpaper.ui.theme.NeonPink
import com.parallax.wallpaper.ui.theme.NeonPurple
import com.parallax.wallpaper.ui.theme.TextPrimary
import com.parallax.wallpaper.ui.theme.TextSecondary
import com.parallax.wallpaper.utils.WallpaperHelper
import com.parallax.wallpaper.utils.WallpaperTarget
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DuoWallpaperScreen(
    repository: DuoWallpaperRepository,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val pairs by repository.pairs.collectAsState()
    val isLoading by repository.isLoading.collectAsState()

    var selectedPair by remember { mutableStateOf<DuoWallpaperItem?>(null) }
    var selectedCategory by remember { mutableStateOf("ALL") }
    var unlockProgress by remember { mutableFloatStateOf(0f) }
    var isSimulatingUnlock by remember { mutableStateOf(false) }
    var isApplying by remember { mutableStateOf(false) }

    // Initialize with first pair
    LaunchedEffect(pairs) {
        if (selectedPair == null && pairs.isNotEmpty()) {
            selectedPair = pairs.first()
        }
    }

    LaunchedEffect(Unit) {
        repository.fetchPairs()
    }

    // Helper to resolve full API URL
    fun resolveUrl(url: String): String {
        return if (url.startsWith("http://") || url.startsWith("https://")) {
            url
        } else {
            val base = ApiClient.baseUrl.removeSuffix("/")
            val path = if (url.startsWith("/")) url else "/$url"
            "$base$path"
        }
    }

    val categories = listOf("ALL", "CYBERPUNK", "LANDSCAPE", "SPACE", "ANIME", "COUPLE", "NATURE")
    val filteredPairs = pairs.filter {
        selectedCategory == "ALL" || it.category.equals(selectedCategory, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "👥 Duo Wallpapers",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "Lock & Home Magic Pairs",
                            style = MaterialTheme.typography.labelSmall.copy(color = NeonCyan)
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
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepObsidian
                )
            )
        },
        containerColor = DeepObsidian
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Concept Banner
            item {
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardDark),
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = NeonCyan.copy(alpha = 0.15f),
                            modifier = Modifier.size(42.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Synchronized Twin Magic",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                            )
                            Text(
                                text = "Complementary wallpapers that awaken and evolve when you unlock your phone.",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                                maxLines = 2
                            )
                        }
                    }
                }
            }

            // 2. Dual-Phone Live Simulator
            selectedPair?.let { currentPair ->
                item {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = CardDark),
                        border = BorderStroke(1.5.dp, NeonCyan.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Title row
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = currentPair.title,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        ),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (currentPair.description.isNotBlank()) {
                                        Text(
                                            text = currentPair.description,
                                            style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = NeonCyan,
                                    modifier = Modifier.padding(start = 8.dp)
                                ) {
                                    Text(
                                        text = currentPair.category,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Dual-Phone Frames Display
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(230.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Phone 1: Lock Screen
                                DuoPhoneMockup(
                                    label = "🔒 LOCK SCREEN",
                                    imageUrl = resolveUrl(currentPair.lockImageUrl),
                                    isLockScreen = true,
                                    scale = 1f - (unlockProgress * 0.05f),
                                    alpha = 1f - (unlockProgress * 0.35f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                // Phone 2: Home Screen
                                DuoPhoneMockup(
                                    label = "📱 HOME SCREEN",
                                    imageUrl = resolveUrl(currentPair.homeImageUrl),
                                    isLockScreen = false,
                                    scale = 0.95f + (unlockProgress * 0.05f),
                                    alpha = 0.7f + (unlockProgress * 0.3f),
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Transition Slider & Test Button
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "✨ Magic Unlock Preview",
                                    style = MaterialTheme.typography.labelMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                                OutlinedButton(
                                    onClick = {
                                        if (!isSimulatingUnlock) {
                                            isSimulatingUnlock = true
                                            scope.launch {
                                                val anim = Animatable(0f)
                                                anim.animateTo(1f, animationSpec = tween(700)) {
                                                    unlockProgress = value
                                                }
                                                kotlinx.coroutines.delay(400)
                                                anim.animateTo(0f, animationSpec = tween(500)) {
                                                    unlockProgress = value
                                                }
                                                isSimulatingUnlock = false
                                            }
                                        }
                                    },
                                    border = BorderStroke(1.dp, NeonPurple),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                    modifier = Modifier.height(30.dp)
                                ) {
                                    Text(
                                        text = if (isSimulatingUnlock) "Testing..." else "⚡ Test Magic",
                                        fontSize = 11.sp,
                                        color = NeonCyan,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Slider(
                                value = unlockProgress,
                                onValueChange = { unlockProgress = it },
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonCyan,
                                    activeTrackColor = NeonPurple,
                                    inactiveTrackColor = CardBorder
                                ),
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Primary Action: 1-Tap Set Both (Lock & Home)
                            Button(
                                onClick = {
                                    if (!isApplying) {
                                        isApplying = true
                                        scope.launch {
                                            try {
                                                val lockBmp = WallpaperHelper.fetchBitmap(context, resolveUrl(currentPair.lockImageUrl))
                                                val homeBmp = WallpaperHelper.fetchBitmap(context, resolveUrl(currentPair.homeImageUrl))

                                                if (lockBmp != null && homeBmp != null) {
                                                    val success = WallpaperHelper.applyDuoPair(context, lockBmp, homeBmp)
                                                    if (success) {
                                                        repository.applyPair(currentPair.id)
                                                        Toast.makeText(context, "✨ Both Lock & Home Wallpapers Applied Successfully!", Toast.LENGTH_LONG).show()
                                                    } else {
                                                        Toast.makeText(context, "Failed to apply duo wallpapers", Toast.LENGTH_SHORT).show()
                                                    }
                                                } else {
                                                    Toast.makeText(context, "Failed to download wallpaper images", Toast.LENGTH_SHORT).show()
                                                }
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                            } finally {
                                                isApplying = false
                                            }
                                        }
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .background(
                                        Brush.horizontalGradient(listOf(NeonCyan, NeonPurple)),
                                        RoundedCornerShape(12.dp)
                                    )
                            ) {
                                if (isApplying) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(20.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(text = "Applying Twin Pair...", color = Color.White, fontWeight = FontWeight.Bold)
                                } else {
                                    Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = Color.Black)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Set Both (Lock & Home Magic Pair)",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            // Sub-actions: Set Lock Only, Set Home Only
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            val bmp = WallpaperHelper.fetchBitmap(context, resolveUrl(currentPair.lockImageUrl))
                                            if (bmp != null) {
                                                val success = WallpaperHelper.setWallpaper(context, bmp, WallpaperTarget.LOCK_SCREEN)
                                                if (success) {
                                                    Toast.makeText(context, "🔒 Lock Screen Wallpaper Applied!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, CardBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Lock Only", color = TextPrimary, fontSize = 12.sp)
                                }

                                OutlinedButton(
                                    onClick = {
                                        scope.launch {
                                            val bmp = WallpaperHelper.fetchBitmap(context, resolveUrl(currentPair.homeImageUrl))
                                            if (bmp != null) {
                                                val success = WallpaperHelper.setWallpaper(context, bmp, WallpaperTarget.HOME_SCREEN)
                                                if (success) {
                                                    Toast.makeText(context, "📱 Home Screen Wallpaper Applied!", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        }
                                    },
                                    shape = RoundedCornerShape(10.dp),
                                    border = BorderStroke(1.dp, CardBorder),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = NeonPurple, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = "Home Only", color = TextPrimary, fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            // 3. Category Filter Chips
            item {
                Column {
                    Text(
                        text = "Curated Magic Pairs Catalog",
                        style = MaterialTheme.typography.titleMedium.copy(
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
                        categories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedCategory = cat },
                                label = { Text(text = cat, fontSize = 12.sp) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonPurple,
                                    selectedLabelColor = Color.White,
                                    containerColor = CardDark,
                                    labelColor = TextSecondary
                                )
                            )
                        }
                    }
                }
            }

            // 4. Pairs Catalog Grid (Smooth unified scrolling without nested scroll trap)
            items(filteredPairs.chunked(2)) { pairRow ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pairRow.forEach { pair ->
                        Box(modifier = Modifier.weight(1f)) {
                            DuoCatalogCard(
                                pair = pair,
                                isSelected = selectedPair?.id == pair.id,
                                onSelect = {
                                    selectedPair = pair
                                    unlockProgress = 0f
                                },
                                resolveUrl = ::resolveUrl
                            )
                        }
                    }
                    if (pairRow.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

/**
 * Smartphone Frame Mockup with Screen UI Overlay
 */
@Composable
private fun DuoPhoneMockup(
    label: String,
    imageUrl: String,
    isLockScreen: Boolean,
    scale: Float,
    alpha: Float,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Black),
        border = BorderStroke(1.dp, CardBorder),
        modifier = modifier
            .scale(scale)
            .alpha(alpha)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Wallpaper Image
            AsyncImage(
                model = imageUrl,
                contentDescription = label,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Screen Tint Overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.2f))
            )

            // Notch / Punch Hole at Top
            Surface(
                shape = RoundedCornerShape(bottomStart = 8.dp, bottomEnd = 8.dp),
                color = Color.Black,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .size(width = 40.dp, height = 10.dp)
            ) {}

            // Screen-specific Widgets
            if (isLockScreen) {
                // Lock screen: Lock icon, Date, Clock, Swipe hint
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(top = 16.dp)
                    ) {
                        Text(text = "🔒", fontSize = 12.sp)
                        Text(
                            text = "Monday, Sep 14",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color.White.copy(alpha = 0.8f)
                        )
                        Text(
                            text = "09:41",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Swipe hint
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.4f),
                        modifier = Modifier.padding(bottom = 6.dp)
                    ) {
                        Text(
                            text = "Swipe to unlock ▲",
                            fontSize = 8.sp,
                            color = Color.White.copy(alpha = 0.7f),
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            } else {
                // Home screen: Search pill, mini App icons, Dock bar
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // Mini Search Pill
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = Color.White.copy(alpha = 0.2f),
                        modifier = Modifier
                            .fillMaxWidth(0.85f)
                            .padding(top = 16.dp)
                            .height(16.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 6.dp)
                        ) {
                            Text(text = "🔍", fontSize = 7.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(text = "Search", fontSize = 7.sp, color = Color.White.copy(alpha = 0.8f))
                        }
                    }

                    // Mini App Grid (2 rows x 4 icons)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            MiniAppIcon(color = NeonCyan)
                            MiniAppIcon(color = NeonPurple)
                            MiniAppIcon(color = NeonPink)
                            MiniAppIcon(color = Color(0xFFFFD700))
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            MiniAppIcon(color = Color(0xFF00E676))
                            MiniAppIcon(color = Color(0xFFFF3D00))
                            MiniAppIcon(color = Color(0xFF2979FF))
                            MiniAppIcon(color = Color(0xFFAA00FF))
                        }
                    }

                    // Mini Dock
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.4f),
                        modifier = Modifier
                            .fillMaxWidth(0.9f)
                            .padding(bottom = 6.dp)
                            .height(18.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = "📞", fontSize = 8.sp)
                            Text(text = "✉️", fontSize = 8.sp)
                            Text(text = "🌐", fontSize = 8.sp)
                            Text(text = "📷", fontSize = 8.sp)
                        }
                    }
                }
            }

            // Top Tag Pill
            Surface(
                shape = RoundedCornerShape(bottomStart = 6.dp, bottomEnd = 6.dp),
                color = Color.Black.copy(alpha = 0.7f),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
            ) {
                Text(
                    text = label,
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonCyan,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun MiniAppIcon(color: Color) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = color.copy(alpha = 0.85f),
        modifier = Modifier.size(14.dp)
    ) {}
}

@Composable
private fun DuoCatalogCard(
    pair: DuoWallpaperItem,
    isSelected: Boolean,
    onSelect: () -> Unit,
    resolveUrl: (String) -> String
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(
            if (isSelected) 2.dp else 1.dp,
            if (isSelected) NeonCyan else CardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelect() }
    ) {
        Column(modifier = Modifier.padding(8.dp)) {
            // Split Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Row(modifier = Modifier.fillMaxSize()) {
                    // Left half: Lock
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        AsyncImage(
                            model = resolveUrl(pair.lockImageUrl),
                            contentDescription = "${pair.title} Lock",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(topStart = 6.dp, bottomEnd = 6.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text = "🔒 LOCK",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }

                    // Divider Line
                    Box(
                        modifier = Modifier
                            .width(1.5.dp)
                            .fillMaxHeight()
                            .background(NeonCyan)
                    )

                    // Right half: Home
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    ) {
                        AsyncImage(
                            model = resolveUrl(pair.homeImageUrl),
                            contentDescription = "${pair.title} Home",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Surface(
                            color = Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(topStart = 6.dp, bottomEnd = 6.dp),
                            modifier = Modifier.align(Alignment.TopStart)
                        ) {
                            Text(
                                text = "📱 HOME",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // Checkmark badge if selected
                if (isSelected) {
                    Surface(
                        shape = CircleShape,
                        color = NeonCyan,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(6.dp)
                            .size(20.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.Black,
                            modifier = Modifier.padding(2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = pair.title,
                style = MaterialTheme.typography.bodySmall.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = pair.category,
                    style = MaterialTheme.typography.labelSmall.copy(
                        color = NeonCyan,
                        fontWeight = FontWeight.SemiBold
                    ),
                    fontSize = 10.sp
                )
                Text(
                    text = "⬇ ${pair.downloads}",
                    style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                    fontSize = 10.sp
                )
            }
        }
    }
}
