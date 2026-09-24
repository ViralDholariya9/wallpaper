package com.parallax.wallpaper.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material.icons.filled.Tune
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
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.wallpaper.data.WallpaperRepository
import com.parallax.wallpaper.data.api.HeroBannerResponse
import com.parallax.wallpaper.data.api.TrendingTagResponse
import com.parallax.wallpaper.model.Category
import androidx.compose.foundation.BorderStroke
import com.parallax.wallpaper.model.ReWallTab
import com.parallax.wallpaper.model.WallpaperItem
import com.parallax.wallpaper.ui.components.CategoryExplorerSheet
import com.parallax.wallpaper.ui.components.DailyWallpaperCard
import com.parallax.wallpaper.ui.components.HeroBannerCarousel
import com.parallax.wallpaper.ui.components.ParallaxCard
import com.parallax.wallpaper.ui.components.PersonalizationSuiteHub
import com.parallax.wallpaper.ui.components.rememberFlagshipFeatures
import com.parallax.wallpaper.ui.theme.CardBorder
import com.parallax.wallpaper.ui.theme.CardDark
import com.parallax.wallpaper.ui.theme.DeepObsidian
import com.parallax.wallpaper.ui.theme.NeonCyan
import com.parallax.wallpaper.ui.theme.NeonPink
import com.parallax.wallpaper.ui.theme.NeonPurple
import com.parallax.wallpaper.ui.theme.SurfaceDark
import com.parallax.wallpaper.ui.theme.TextPrimary
import com.parallax.wallpaper.ui.theme.TextSecondary

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HomeScreen(
    repository: WallpaperRepository,
    onWallpaperClick: (WallpaperItem) -> Unit,
    onOpenDrawer: () -> Unit,
    onOpenAiStudio: () -> Unit = {},
    onOpen3DMaker: () -> Unit = {},
    onOpenEdgeLighting: () -> Unit = {},
    onOpenDynamicIsland: () -> Unit = {},
    onOpenAod: () -> Unit = {},
    onOpenCallScreen: () -> Unit = {},
    onOpenDuoWallpapers: () -> Unit = {},
    onOpenTouchEffects: () -> Unit = {},
    onOpenFingerprint: () -> Unit = {},
    openCategoryExplorerDirectly: Boolean = false,
    onCategoryExplorerClosed: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val wallpapers by repository.wallpapers.collectAsState()
    val favorites by repository.favorites.collectAsState()
    val heroBanners by repository.heroBanners.collectAsState()
    val trendingTagsList by repository.trendingTags.collectAsState()
    val isLoadingMore by repository.isLoadingMore.collectAsState()
    val hasMore by repository.hasMore.collectAsState()
    val remoteConfig by com.parallax.wallpaper.data.RemoteConfigManager.config.collectAsState()
    val gridState = rememberLazyGridState()
    val context = LocalContext.current

    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var searchFilterMode by remember { mutableStateOf("ALL") } // "ALL", "3D", "FREE"
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    val flagshipFeatures = rememberFlagshipFeatures(
        remoteSuiteConfig = remoteConfig?.personalizationSuite,
        onOpenAiStudio = onOpenAiStudio,
        onOpenEdgeLighting = onOpenEdgeLighting,
        onOpenDynamicIsland = onOpenDynamicIsland,
        onOpenAod = onOpenAod,
        onOpenCallScreen = onOpenCallScreen,
        onOpenDuoWallpapers = onOpenDuoWallpapers,
        onOpenTouchEffects = onOpenTouchEffects,
        onOpenFingerprint = onOpenFingerprint
    )

    // Infinite Scrolling auto-trigger with proactive early prefetching (3 rows in advance)
    LaunchedEffect(gridState, isSearchActive) {
        snapshotFlow {
            val layoutInfo = gridState.layoutInfo
            val totalItems = layoutInfo.totalItemsCount
            val lastVisibleIndex = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleIndex >= totalItems - 6
        }.collect { shouldLoadMore ->
            if (shouldLoadMore && !isLoadingMore && hasMore && !isSearchActive) {
                repository.loadMoreWallpapers()
            }
        }
    }

    // Recent Searches history
    val recentSearches = remember {
        mutableStateListOf("3D Parallax", "Cyber Neon", "AMOLED", "Space")
    }

    // Handle physical/gesture back button when search is open
    BackHandler(enabled = isSearchActive) {
        isSearchActive = false
        searchQuery = ""
        focusManager.clearFocus()
    }

    var selectedTab by remember { mutableStateOf(ReWallTab.PARALLAX) }
    var selectedCategory by remember { mutableStateOf(Category.ALL) }
    var showCategoryExplorerSheet by remember { mutableStateOf(false) }

    LaunchedEffect(openCategoryExplorerDirectly) {
        if (openCategoryExplorerDirectly) {
            showCategoryExplorerSheet = true
        }
    }

    val handleBannerClick: (HeroBannerResponse) -> Unit = { banner ->
        when (banner.actionType.lowercase()) {
            "category" -> {
                val targetName = banner.actionTarget ?: ""
                val matchedCat = Category.entries.find {
                    it.name.equals(targetName, ignoreCase = true) ||
                    it.displayName.equals(targetName, ignoreCase = true)
                }
                if (matchedCat != null) {
                    selectedCategory = matchedCat
                }
            }
            "wallpaper" -> {
                val targetId = banner.actionTarget ?: ""
                val matchedWp = wallpapers.find { it.id == targetId }
                if (matchedWp != null) {
                    onWallpaperClick(matchedWp)
                }
            }
            "screen" -> {
                when (banner.actionTarget?.lowercase()) {
                    "custom_3d" -> onOpen3DMaker()
                    "ai_generator" -> onOpenAiStudio()
                    "dynamic_island" -> onOpenDynamicIsland()
                    "edge_lighting" -> onOpenEdgeLighting()
                    "aod", "always_on_display" -> onOpenAod()
                    "call_screen", "callscreen" -> onOpenCallScreen()
                    "duo", "duo_wallpapers", "double_wallpaper" -> onOpenDuoWallpapers()
                    "touch", "touch_effects", "fluid", "ripples" -> onOpenTouchEffects()
                    "fingerprint", "biometrics", "fingerprint_animations" -> onOpenFingerprint()
                    "daily" -> onWallpaperClick(repository.dailyWallpaper)
                    else -> onOpen3DMaker()
                }
            }
            "url" -> {
                val urlStr = banner.actionTarget
                if (!urlStr.isNullOrBlank()) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(urlStr))
                        context.startActivity(intent)
                    } catch (e: Exception) {
                        android.util.Log.e("HomeScreen", "Cannot open banner URL: $urlStr", e)
                    }
                }
            }
        }
    }

    // Trending Search Keywords / Tags (Dynamic from Server with rich fallback)
    val fallbackTags = listOf(
        TrendingTagResponse("t1", "#3D Parallax", "🌀", 1),
        TrendingTagResponse("t2", "#Diwali Special", "🪔", 2),
        TrendingTagResponse("t3", "#Cyberpunk", "⚡", 3),
        TrendingTagResponse("t4", "#Anime", "⛩️", 4),
        TrendingTagResponse("t5", "#Mahadev", "🔱", 5),
        TrendingTagResponse("t6", "#Cars 4K", "🏎️", 6),
        TrendingTagResponse("t7", "#AMOLED Dark", "✨", 7),
        TrendingTagResponse("t8", "#Deep Space", "🌌", 8)
    )
    val displayTrendingTags = if (trendingTagsList.isNotEmpty()) trendingTagsList else fallbackTags

    fun executeSearch(query: String) {
        searchQuery = query
        val clean = query.trim()
        if (clean.isNotBlank()) {
            recentSearches.remove(clean)
            recentSearches.add(0, clean)
            if (recentSearches.size > 8) {
                recentSearches.removeAt(recentSearches.lastIndex)
            }
        }
        focusManager.clearFocus()
    }

    // Multi-criteria tab filtering for normal home
    val tabFiltered = remember(wallpapers, selectedTab) {
        repository.filterByTab(selectedTab, wallpapers)
    }

    val normalFilteredWallpapers = remember(tabFiltered, selectedCategory) {
        if (selectedCategory == Category.ALL) {
            tabFiltered
        } else {
            tabFiltered.filter { it.category == selectedCategory }
        }
    }

    // Dynamic Live Multi-token Search across all wallpapers
    val searchResults = remember(wallpapers, searchQuery, searchFilterMode) {
        if (searchQuery.isBlank()) {
            emptyList()
        } else {
            val q = searchQuery.trim().lowercase()
            val tokens = q.split("\\s+".toRegex()).filter { it.isNotBlank() }
            wallpapers.filter { item ->
                val matchesQuery = tokens.all { token ->
                    item.title.lowercase().contains(token) ||
                            item.category.displayName.lowercase().contains(token) ||
                            item.category.name.lowercase().contains(token) ||
                            (token == "3d" && item.isParallax) ||
                            (token == "parallax" && item.isParallax) ||
                            (token == "2d" && !item.isParallax) ||
                            (token == "free" && !item.isPremium) ||
                            (token == "vip" && item.isPremium) ||
                            (token == "premium" && item.isPremium) ||
                            (token == "popular" && item.downloads >= 20000) ||
                            (token == "dark" && (item.title.lowercase().contains("dark") || item.category == Category.AMOLED))
                }
                val matchesFilter = when (searchFilterMode) {
                    "3D" -> item.isParallax
                    "FREE" -> !item.isPremium
                    else -> true
                }
                matchesQuery && matchesFilter
            }
        }
    }

    LaunchedEffect(isSearchActive) {
        if (isSearchActive) {
            focusRequester.requestFocus()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DeepObsidian)
    ) {
        // TOP BAR: Normal ReWall Bar OR Active Search Bar
        if (isSearchActive) {
            // Interactive Search Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        isSearchActive = false
                        searchQuery = ""
                        focusManager.clearFocus()
                    }
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                TextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = "Search 4K, 3D & Live wallpapers...",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { executeSearch(searchQuery) }),
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = CardDark,
                        unfocusedContainerColor = CardDark,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = NeonCyan
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp)
                        .focusRequester(focusRequester)
                        .border(1.dp, CardBorder, RoundedCornerShape(16.dp))
                )
            }

            // Quick Trending Search Chips (Dynamic from Admin Panel)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                displayTrendingTags.forEach { item ->
                    val cleanTag = item.tag.removePrefix("#")
                    val isTagActive = searchQuery.equals(cleanTag, ignoreCase = true) || searchQuery.equals(item.tag, ignoreCase = true)
                    val displayLabel = if (item.icon.isNotBlank()) "${item.icon} ${item.tag}" else item.tag
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (isTagActive) NeonPurple else CardDark,
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isTagActive) NeonCyan else CardBorder),
                        modifier = Modifier.clickable {
                            executeSearch(cleanTag)
                            repository.recordTagClick(item.id)
                        }
                    ) {
                        Text(
                            text = displayLabel,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            style = MaterialTheme.typography.bodySmall.copy(
                                fontWeight = if (isTagActive) FontWeight.Bold else FontWeight.Normal,
                                color = if (isTagActive) Color.White else TextSecondary
                            )
                        )
                    }
                }
            }

            // Search Content / Results Grid
            if (searchQuery.isBlank()) {
                // Empty query - Show Recent Searches, Trending, and Categories Explorer
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Recent Searches Section
                    if (recentSearches.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Recent Searches",
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                )
                            }
                            Text(
                                text = "Clear All",
                                style = MaterialTheme.typography.labelSmall.copy(color = TextSecondary),
                                modifier = Modifier
                                    .clickable { recentSearches.clear() }
                                    .padding(4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            recentSearches.forEach { term ->
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = CardDark,
                                    border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder),
                                    modifier = Modifier.clickable { executeSearch(term) }
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    ) {
                                        Text(
                                            text = term,
                                            style = MaterialTheme.typography.bodySmall.copy(color = TextPrimary)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Remove",
                                            tint = TextSecondary,
                                            modifier = Modifier
                                                .size(14.dp)
                                                .clickable { recentSearches.remove(term) }
                                        )
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // Browse by Category Explorer
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = NeonPurple,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Explore Categories",
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val exploreCats = Category.entries.filter { it != Category.ALL }
                    exploreCats.chunked(2).forEach { pair ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            pair.forEach { cat ->
                                val catCount = wallpapers.count { it.category == cat }
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = CardDark,
                                    border = BorderStroke(
                                        1.dp,
                                        Brush.horizontalGradient(
                                            listOf(cat.primaryComposeColor.copy(alpha = 0.4f), CardBorder)
                                        )
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { executeSearch(cat.displayName) }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .background(cat.primaryComposeColor.copy(alpha = 0.25f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(text = cat.emoji, fontSize = 16.sp)
                                        }

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = cat.displayName,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = FontWeight.Bold,
                                                    color = TextPrimary
                                                ),
                                                maxLines = 1
                                            )
                                            Text(
                                                text = "${cat.gujaratiName} • $catCount",
                                                style = MaterialTheme.typography.labelSmall.copy(
                                                    color = TextSecondary,
                                                    fontSize = 10.sp
                                                ),
                                                maxLines = 1
                                            )
                                        }
                                    }
                                }
                            }
                            if (pair.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else if (searchResults.isEmpty()) {
                // No matching wallpapers found
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.SearchOff,
                            contentDescription = null,
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No Wallpapers Found",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "No wallpapers matching '$searchQuery'. Try checking for typos or searching a category like AMOLED or Space.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            Button(
                                onClick = { searchQuery = "" },
                                colors = ButtonDefaults.buttonColors(containerColor = CardDark),
                                shape = RoundedCornerShape(12.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CardBorder)
                            ) {
                                Text("Clear Search", color = NeonCyan)
                            }
                            Button(
                                onClick = { executeSearch("3D Parallax") },
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Popular 3D", color = Color.White)
                            }
                        }
                    }
                }
            } else {
                // Results Header, Filters & 2-column Grid
                Column(modifier = Modifier.fillMaxSize()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Results for '$searchQuery'",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        )
                        Text(
                            text = "${searchResults.size} found",
                            style = MaterialTheme.typography.labelSmall.copy(
                                color = NeonCyan,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    // Refinement quick filters (All, 3D Only, Free Only)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("ALL" to "All Results", "3D" to "3D Parallax Only", "FREE" to "Free Wallpapers").forEach { (mode, label) ->
                            val isSelected = searchFilterMode == mode
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (isSelected) NeonCyan.copy(alpha = 0.2f) else CardDark,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) NeonCyan else CardBorder
                                ),
                                modifier = Modifier.clickable { searchFilterMode = mode }
                            ) {
                                Text(
                                    text = label,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = if (isSelected) NeonCyan else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                )
                            }
                        }
                    }

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(14.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(searchResults, key = { it.id }) { item ->
                            ParallaxCard(
                                wallpaper = item,
                                isFavorite = favorites.contains(item.id),
                                onWallpaperClick = { onWallpaperClick(item) },
                                onFavoriteClick = { repository.toggleFavorite(item.id) }
                            )
                        }
                    }
                }
            }
        } else {
            // NORMAL HOME SCREEN WITH REWALL TOP TABS & CATEGORIES
            val isBannerDismissed by com.parallax.wallpaper.data.RemoteConfigManager.isAnnouncementDismissed.collectAsState()

            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "ReWall",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.2.sp,
                                color = TextPrimary
                            )
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NeonPurple.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = "3D",
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
                    IconButton(onClick = onOpenDrawer) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu Drawer",
                            tint = TextPrimary
                        )
                    }
                },
                actions = {
                    if (remoteConfig?.features?.custom3dMaker != false) {
                        IconButton(onClick = onOpen3DMaker) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "DIY 3D Maker",
                                tint = NeonPurple
                            )
                        }
                    }
                    IconButton(onClick = { isSearchActive = true }) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = NeonCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = DeepObsidian
                )
            )

            // Dynamic In-App Announcement & Festival Banner (Remote Config)
            remoteConfig?.announcement?.let { announcement ->
                if (announcement.enabled && !isBannerDismissed) {
                    com.parallax.wallpaper.ui.components.AnnouncementBanner(
                        announcement = announcement,
                        onDismiss = {
                            com.parallax.wallpaper.data.RemoteConfigManager.dismissAnnouncement()
                        },
                        onActionClick = { target ->
                            if (target.startsWith("category:", ignoreCase = true)) {
                                val catName = target.removePrefix("category:").trim()
                                val matchedCat = Category.entries.find { 
                                    it.name.equals(catName, ignoreCase = true) || it.displayName.equals(catName, ignoreCase = true) 
                                }
                                if (matchedCat != null) {
                                    selectedCategory = matchedCat
                                } else {
                                    isSearchActive = true
                                    executeSearch(catName)
                                }
                            } else if (target.isNotBlank()) {
                                isSearchActive = true
                                executeSearch(target)
                            }
                        }
                    )
                }
            }

            // Figma ReWall Top Tabs: Parallax | 3D | 2D | Popular | Recent
            ScrollableTabRow(
                selectedTabIndex = selectedTab.ordinal,
                containerColor = DeepObsidian,
                contentColor = NeonCyan,
                edgePadding = 16.dp,
                divider = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(CardBorder.copy(alpha = 0.4f))
                    )
                },
                indicator = { tabPositions ->
                    if (selectedTab.ordinal < tabPositions.size) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[selectedTab.ordinal]),
                            height = 3.dp,
                            color = NeonCyan
                        )
                    }
                }
            ) {
                ReWallTab.values().forEach { tab ->
                    val isSelected = tab == selectedTab
                    val textColor by animateColorAsState(
                        targetValue = if (isSelected) Color.White else TextSecondary,
                        label = "tabTextColor"
                    )

                    Tab(
                        selected = isSelected,
                        onClick = { selectedTab = tab },
                        text = {
                            Text(
                                text = tab.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = textColor
                                )
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Modern Professional Category Bar Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🏷️",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.titleSmall.copy(
                            fontWeight = FontWeight.ExtraBold,
                            color = TextPrimary
                        )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    if (selectedCategory != Category.ALL) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = NeonPurple.copy(alpha = 0.25f)
                        ) {
                            Text(
                                text = selectedCategory.displayName,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp
                                )
                            )
                        }
                    }
                }

                // Interactive "Browse All ⊞" button
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = CardDark,
                    border = BorderStroke(1.dp, CardBorder),
                    modifier = Modifier.clickable { showCategoryExplorerSheet = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Browse All ⊞",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                        )
                    }
                }
            }

            // Glassmorphic Category Horizontal Pills
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // ⚡ Personalization Suite Quick Launch Pill
                if (flagshipFeatures.isNotEmpty()) {
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = NeonPurple.copy(alpha = 0.22f),
                        border = BorderStroke(
                            1.dp,
                            Brush.horizontalGradient(listOf(NeonCyan, NeonPurple, NeonPink))
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { showCategoryExplorerSheet = true }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = "⚡", fontSize = 14.sp)
                            Text(
                                text = "Personalization Suite",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    color = NeonCyan
                                )
                            )
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = NeonPurple.copy(alpha = 0.45f)
                            ) {
                                Text(
                                    text = "${flagshipFeatures.size} PRO",
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                Category.values().forEach { category ->
                    val isSelected = category == selectedCategory
                    val primaryColor = category.primaryComposeColor

                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) primaryColor.copy(alpha = 0.25f) else CardDark,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            brush = if (isSelected) {
                                Brush.horizontalGradient(listOf(NeonCyan, NeonPurple))
                            } else {
                                Brush.horizontalGradient(listOf(CardBorder, CardBorder))
                            }
                        ),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { selectedCategory = category }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(text = category.emoji, fontSize = 14.sp)
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                                    color = if (isSelected) Color.White else TextSecondary
                                )
                            )
                            category.badge?.let { badgeText ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = if (isSelected) NeonCyan.copy(alpha = 0.3f) else primaryColor.copy(alpha = 0.3f)
                                ) {
                                    Text(
                                        text = badgeText,
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            fontSize = 8.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = if (isSelected) NeonCyan else Color.White
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Normal Wallpapers Grid with Daily Highlight Card & Infinite Scroll
            LazyVerticalGrid(
                state = gridState,
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Active Category Filter Context Banner
                if (selectedCategory != Category.ALL) {
                    item(span = { GridItemSpan(2) }) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = CardDark,
                            border = BorderStroke(
                                1.dp,
                                Brush.horizontalGradient(listOf(selectedCategory.primaryComposeColor, CardBorder))
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        Brush.horizontalGradient(
                                            listOf(
                                                selectedCategory.primaryComposeColor.copy(alpha = 0.2f),
                                                Color.Transparent
                                            )
                                        )
                                    )
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(text = selectedCategory.emoji, fontSize = 24.sp)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = selectedCategory.displayName,
                                                    style = MaterialTheme.typography.titleMedium.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        color = Color.White
                                                    )
                                                )
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text(
                                                    text = "(${selectedCategory.gujaratiName})",
                                                    style = MaterialTheme.typography.bodySmall.copy(color = NeonCyan)
                                                )
                                            }
                                            Text(
                                                text = "${selectedCategory.subtitle} • ${normalFilteredWallpapers.size} Wallpapers",
                                                style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                            )
                                        }
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = NeonPurple.copy(alpha = 0.3f),
                                        border = BorderStroke(1.dp, NeonPurple),
                                        modifier = Modifier.clickable { selectedCategory = Category.ALL }
                                    ) {
                                        Text(
                                            text = "✕ All",
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                // Top Featured: Dynamic Cloud Hero Banners Carousel
                if (selectedCategory == Category.ALL && selectedTab == ReWallTab.PARALLAX && heroBanners.isNotEmpty()) {
                    item(span = { GridItemSpan(2) }) {
                        HeroBannerCarousel(
                            banners = heroBanners,
                            onBannerClick = handleBannerClick,
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }
                }

                // Top Featured: Wallpaper of the Day Hero Banner
                if (selectedCategory == Category.ALL && selectedTab == ReWallTab.PARALLAX) {
                    item(span = { GridItemSpan(2) }) {
                        DailyWallpaperCard(
                            wallpaper = repository.dailyWallpaper,
                            isFavorite = favorites.contains(repository.dailyWallpaper.id),
                            onClick = { onWallpaperClick(repository.dailyWallpaper) },
                            onFavoriteClick = { repository.toggleFavorite(repository.dailyWallpaper.id) },
                            modifier = Modifier.padding(bottom = 6.dp)
                        )
                    }

                    // ⚡ Flagship Personalization Suite Hub (8 Flagship Customization Modules)
                    item(span = { GridItemSpan(2) }) {
                        PersonalizationSuiteHub(
                            features = flagshipFeatures,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                    }
                }

                items(normalFilteredWallpapers, key = { it.id }) { item ->
                    ParallaxCard(
                        wallpaper = item,
                        isFavorite = favorites.contains(item.id),
                        onWallpaperClick = { onWallpaperClick(item) },
                        onFavoriteClick = { repository.toggleFavorite(item.id) }
                    )
                }

                // Loading Shimmer / Footer for Infinite Scrolling
                if (isLoadingMore) {
                    item(span = { GridItemSpan(2) }) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 18.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                CircularProgressIndicator(
                                    color = NeonCyan,
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.5.dp
                                )
                                Text(
                                    text = "Loading more 4K 3D wallpapers...",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showCategoryExplorerSheet) {
        CategoryExplorerSheet(
            selectedCategory = selectedCategory,
            wallpapers = wallpapers,
            flagshipFeatures = flagshipFeatures,
            onCategorySelected = { cat ->
                selectedCategory = cat
            },
            onDismiss = {
                showCategoryExplorerSheet = false
                onCategoryExplorerClosed()
            }
        )
    }
}
