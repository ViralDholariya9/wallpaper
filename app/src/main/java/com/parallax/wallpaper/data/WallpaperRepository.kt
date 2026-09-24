package com.parallax.wallpaper.data

import coil.imageLoader
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.parallax.wallpaper.ParallaxApp
import com.parallax.wallpaper.data.api.ApiClient
import com.parallax.wallpaper.data.api.HeroBannerResponse
import com.parallax.wallpaper.data.api.TrendingTagResponse
import com.parallax.wallpaper.data.api.WallpaperResponse
import com.parallax.wallpaper.data.local.FavoritesManager
import com.parallax.wallpaper.model.Category
import com.parallax.wallpaper.model.ReWallTab
import com.parallax.wallpaper.model.WallpaperItem
import com.parallax.wallpaper.model.WallpaperLayer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar

class WallpaperRepository {

    private val repositoryScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    private val favoritesManager by lazy {
        try {
            FavoritesManager(ParallaxApp.instance)
        } catch (e: Exception) {
            null
        }
    }

    private val _heroBanners = MutableStateFlow<List<HeroBannerResponse>>(emptyList())
    val heroBanners: StateFlow<List<HeroBannerResponse>> = _heroBanners.asStateFlow()

    private val _trendingTags = MutableStateFlow<List<TrendingTagResponse>>(emptyList())
    val trendingTags: StateFlow<List<TrendingTagResponse>> = _trendingTags.asStateFlow()

    // Curated Base Wallpapers (Page 1)
    private val initialWallpapers = listOf(
        WallpaperItem(
            id = "w1",
            title = "Cyber Neon Horizon",
            category = Category.CYBERPUNK,
            previewUrl = "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=800&auto=format&fit=crop",
            isParallax = true,
            layers = listOf(
                WallpaperLayer("l1_bg", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=800&auto=format&fit=crop", 0.2f),
                WallpaperLayer("l1_fg", "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?q=80&w=800&auto=format&fit=crop", 0.7f)
            ),
            downloads = 34200,
            likes = 18900,
            isPremium = false,
            isUnlocked = true,
            createdAt = System.currentTimeMillis() - 86400000L * 2
        ),
        WallpaperItem(
            id = "w2",
            title = "Deep Space Nebula",
            category = Category.SPACE,
            previewUrl = "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?q=80&w=800&auto=format&fit=crop",
            isParallax = true,
            layers = listOf(
                WallpaperLayer("l2_bg", "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?q=80&w=800&auto=format&fit=crop", 0.15f),
                WallpaperLayer("l2_fg", "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=800&auto=format&fit=crop", 0.6f)
            ),
            downloads = 41500,
            likes = 24500,
            isPremium = true,
            isUnlocked = false,
            createdAt = System.currentTimeMillis() - 86400000L * 1
        ),
        WallpaperItem(
            id = "w3",
            title = "Mystic AMOLED Mountain",
            category = Category.AMOLED,
            previewUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?q=80&w=800&auto=format&fit=crop",
            isParallax = true,
            layers = listOf(
                WallpaperLayer("l3_bg", "https://images.unsplash.com/photo-1506744038136-46273834b3fb?q=80&w=800&auto=format&fit=crop", 0.25f),
                WallpaperLayer("l3_fg", "https://images.unsplash.com/photo-1519681393784-d120267933ba?q=80&w=800&auto=format&fit=crop", 0.8f)
            ),
            downloads = 52800,
            likes = 31400,
            isPremium = false,
            isUnlocked = true,
            createdAt = System.currentTimeMillis() - 86400000L * 5
        ),
        WallpaperItem(
            id = "w4",
            title = "Tokyo Neon Rain",
            category = Category.CYBERPUNK,
            previewUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=80&w=800&auto=format&fit=crop",
            isParallax = false,
            downloads = 19800,
            likes = 9400,
            isPremium = false,
            isUnlocked = true,
            createdAt = System.currentTimeMillis() - 86400000L * 3
        ),
        WallpaperItem(
            id = "w5",
            title = "Emerald Forest Flow",
            category = Category.NATURE,
            previewUrl = "https://images.unsplash.com/photo-1448375240586-882707db888b?q=80&w=800&auto=format&fit=crop",
            isParallax = true,
            layers = listOf(
                WallpaperLayer("l5_bg", "https://images.unsplash.com/photo-1448375240586-882707db888b?q=80&w=800&auto=format&fit=crop", 0.2f),
                WallpaperLayer("l5_fg", "https://images.unsplash.com/photo-1511497584788-87676104235f?q=80&w=800&auto=format&fit=crop", 0.75f)
            ),
            downloads = 28600,
            likes = 17300,
            isPremium = true,
            isUnlocked = false,
            createdAt = System.currentTimeMillis() - 86400000L * 6
        ),
        WallpaperItem(
            id = "w6",
            title = "Dark Horizon Eclipse",
            category = Category.AMOLED,
            previewUrl = "https://images.unsplash.com/photo-1532693322450-2cb5c511067d?q=80&w=800&auto=format&fit=crop",
            isParallax = true,
            layers = listOf(
                WallpaperLayer("l6_bg", "https://images.unsplash.com/photo-1532693322450-2cb5c511067d?q=80&w=800&auto=format&fit=crop", 0.2f),
                WallpaperLayer("l6_fg", "https://images.unsplash.com/photo-1538370965046-79c0d6907d47?q=80&w=800&auto=format&fit=crop", 0.7f)
            ),
            downloads = 47400,
            likes = 29800,
            isPremium = false,
            isUnlocked = true,
            createdAt = System.currentTimeMillis() - 86400000L * 4
        ),
        WallpaperItem(
            id = "w7",
            title = "Golden Sunset Dunes",
            category = Category.MINIMAL,
            previewUrl = "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?q=80&w=800&auto=format&fit=crop",
            isParallax = false,
            downloads = 15200,
            likes = 8700,
            isPremium = false,
            isUnlocked = true,
            createdAt = System.currentTimeMillis() - 86400000L * 7
        ),
        WallpaperItem(
            id = "w8",
            title = "Anime Sky Sanctuary",
            category = Category.ANIME,
            previewUrl = "https://images.unsplash.com/photo-1534447677768-be436bb09401?q=80&w=800&auto=format&fit=crop",
            isParallax = true,
            layers = listOf(
                WallpaperLayer("l8_bg", "https://images.unsplash.com/photo-1534447677768-be436bb09401?q=80&w=800&auto=format&fit=crop", 0.15f),
                WallpaperLayer("l8_fg", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=800&auto=format&fit=crop", 0.8f)
            ),
            downloads = 69100,
            likes = 48400,
            isPremium = true,
            isUnlocked = false,
            createdAt = System.currentTimeMillis()
        )
    )

    // Additional Wallpapers for Pagination (Infinite Scroll Batches)
    private val pagedWallpapersPool = listOf(
        // Batch 2
        WallpaperItem(
            id = "w9",
            title = "Cybernetic Ronin 4K",
            category = Category.CYBERPUNK,
            previewUrl = "https://images.unsplash.com/photo-1578632767115-351597cf2477?q=80&w=800&auto=format&fit=crop",
            isParallax = true,
            layers = listOf(
                WallpaperLayer("l9_bg", "https://images.unsplash.com/photo-1578632767115-351597cf2477?q=80&w=800&auto=format&fit=crop", 0.2f),
                WallpaperLayer("l9_fg", "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=80&w=800&auto=format&fit=crop", 0.7f)
            ),
            downloads = 38400,
            likes = 21000,
            isPremium = false,
            isUnlocked = true,
            createdAt = System.currentTimeMillis() - 86400000L * 8
        ),
        WallpaperItem(
            id = "w10",
            title = "Quantum Singularity 3D",
            category = Category.SPACE,
            previewUrl = "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?q=80&w=800&auto=format&fit=crop",
            isParallax = true,
            layers = listOf(
                WallpaperLayer("l10_bg", "https://images.unsplash.com/photo-1506703719100-a0f3a48c0f86?q=80&w=800&auto=format&fit=crop", 0.15f),
                WallpaperLayer("l10_fg", "https://images.unsplash.com/photo-1462331940025-496dfbfc7564?q=80&w=800&auto=format&fit=crop", 0.75f)
            ),
            downloads = 59000,
            likes = 34900,
            isPremium = true,
            isUnlocked = false,
            createdAt = System.currentTimeMillis() - 86400000L * 9
        ),
        WallpaperItem(
            id = "w11",
            title = "Pure Obsidian Geometry",
            category = Category.AMOLED,
            previewUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=800&auto=format&fit=crop",
            isParallax = false,
            downloads = 22100,
            likes = 12400,
            isPremium = false,
            isUnlocked = true,
            createdAt = System.currentTimeMillis() - 86400000L * 10
        ),
        WallpaperItem(
            id = "w12",
            title = "Bioluminescent Valley",
            category = Category.NATURE,
            previewUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?q=80&w=800&auto=format&fit=crop",
            isParallax = true,
            layers = listOf(
                WallpaperLayer("l12_bg", "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?q=80&w=800&auto=format&fit=crop", 0.2f),
                WallpaperLayer("l12_fg", "https://images.unsplash.com/photo-1511497584788-87676104235f?q=80&w=800&auto=format&fit=crop", 0.65f)
            ),
            downloads = 43200,
            likes = 26500,
            isPremium = false,
            isUnlocked = true,
            createdAt = System.currentTimeMillis() - 86400000L * 11
        ),
        WallpaperItem(
            id = "w13",
            title = "Sakura Cyber Shrine",
            category = Category.ANIME,
            previewUrl = "https://images.unsplash.com/photo-1528164344705-475426879c0d?q=80&w=800&auto=format&fit=crop",
            isParallax = true,
            layers = listOf(
                WallpaperLayer("l13_bg", "https://images.unsplash.com/photo-1528164344705-475426879c0d?q=80&w=800&auto=format&fit=crop", 0.25f),
                WallpaperLayer("l13_fg", "https://images.unsplash.com/photo-1579546929518-9e396f3cc809?q=80&w=800&auto=format&fit=crop", 0.8f)
            ),
            downloads = 74300,
            likes = 51200,
            isPremium = true,
            isUnlocked = false,
            createdAt = System.currentTimeMillis() - 86400000L * 12
        ),
        WallpaperItem(
            id = "w14",
            title = "Minimal Aurora Arch",
            category = Category.MINIMAL,
            previewUrl = "https://images.unsplash.com/photo-1517411032315-54ef2cb783bb?q=80&w=800&auto=format&fit=crop",
            isParallax = false,
            downloads = 18600,
            likes = 10100,
            isPremium = false,
            isUnlocked = true,
            createdAt = System.currentTimeMillis() - 86400000L * 13
        ),

        // Batch 3
        WallpaperItem(
            id = "w15",
            title = "Supernova Core 4D",
            category = Category.SPACE,
            previewUrl = "https://images.unsplash.com/photo-1447433589675-4aaa569f3e05?q=80&w=800&auto=format&fit=crop",
            isParallax = true,
            layers = listOf(
                WallpaperLayer("l15_bg", "https://images.unsplash.com/photo-1447433589675-4aaa569f3e05?q=80&w=800&auto=format&fit=crop", 0.15f),
                WallpaperLayer("l15_fg", "https://images.unsplash.com/photo-1451187580459-43490279c0fa?q=80&w=800&auto=format&fit=crop", 0.7f)
            ),
            downloads = 63800,
            likes = 39200,
            isPremium = false,
            isUnlocked = true,
            createdAt = System.currentTimeMillis() - 86400000L * 14
        ),
        WallpaperItem(
            id = "w16",
            title = "Neon Highway Drive",
            category = Category.CYBERPUNK,
            previewUrl = "https://images.unsplash.com/photo-1514565131-fce0801e5785?q=80&w=800&auto=format&fit=crop",
            isParallax = true,
            layers = listOf(
                WallpaperLayer("l16_bg", "https://images.unsplash.com/photo-1514565131-fce0801e5785?q=80&w=800&auto=format&fit=crop", 0.2f),
                WallpaperLayer("l16_fg", "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=800&auto=format&fit=crop", 0.75f)
            ),
            downloads = 45900,
            likes = 27800,
            isPremium = true,
            isUnlocked = false,
            createdAt = System.currentTimeMillis() - 86400000L * 15
        )
    )

    private val _wallpapers = MutableStateFlow(initialWallpapers)
    val wallpapers: StateFlow<List<WallpaperItem>> = _wallpapers.asStateFlow()

    // Persistent favorites backed by SharedPreferences
    private val initialFavs = favoritesManager?.getFavorites() ?: setOf("w1", "w3", "w5")
    private val _favorites = MutableStateFlow<Set<String>>(initialFavs)
    val favorites: StateFlow<Set<String>> = _favorites.asStateFlow()

    // Pagination State
    private var currentPoolIndex = 0
    private var apiCurrentPage = 1
    private val pageSize = 8
    private val _isLoadingMore = MutableStateFlow(false)
    val isLoadingMore: StateFlow<Boolean> = _isLoadingMore.asStateFlow()

    private val _hasMore = MutableStateFlow(true)
    val hasMore: StateFlow<Boolean> = _hasMore.asStateFlow()

    private val _dailyWallpaperState = MutableStateFlow<WallpaperItem?>(null)

    private var isServerAvailable = true
    private var lastServerFailureTime = 0L

    init {
        preloadBitmaps(initialWallpapers)
        fetchCloudWallpapers()
        fetchHeroBanners()
        fetchTrendingTags()
    }

    fun fetchTrendingTags() {
        repositoryScope.launch {
            try {
                val response = ApiClient.service.getTrendingTags()
                if (response.success && !response.data.isNullOrEmpty()) {
                    _trendingTags.value = response.data.filter { it.isActive }.sortedBy { it.sortOrder }
                }
            } catch (e: Exception) {
                android.util.Log.w("WallpaperRepo", "Trending tags offline fallback: ${e.message}")
            }
        }
    }

    fun recordTagClick(tagId: String) {
        repositoryScope.launch {
            try {
                ApiClient.service.recordTagClick(tagId)
            } catch (e: Exception) {
                // Non-blocking telemetry
            }
        }
    }

    fun fetchHeroBanners() {
        repositoryScope.launch {
            try {
                val response = ApiClient.service.getHeroBanners()
                if (response.success && !response.data.isNullOrEmpty()) {
                    _heroBanners.value = response.data.filter { it.isActive }.sortedBy { it.sortOrder }
                }
            } catch (e: Exception) {
                android.util.Log.w("WallpaperRepo", "Hero banners offline fallback: ${e.message}")
            }
        }
    }

    fun fetchCloudWallpapers() {
        fetchHeroBanners()
        fetchTrendingTags()
        repositoryScope.launch {
            try {
                val response = ApiClient.service.getWallpapers(page = 1, limit = 16)
                if (response.success && !response.data.isNullOrEmpty()) {
                    isServerAvailable = true
                    val favSet = _favorites.value
                    val mapped = response.data.map { it.toWallpaperItem(favSet.contains(it.id)) }
                    _wallpapers.value = mapped
                    preloadBitmaps(mapped)
                    apiCurrentPage = 1
                    _hasMore.value = (response.pagination?.page ?: 1) < (response.pagination?.totalPages ?: 1)
                }
            } catch (e: Exception) {
                isServerAvailable = false
                lastServerFailureTime = System.currentTimeMillis()
                android.util.Log.w("WallpaperRepo", "Cloud sync offline fallback: ${e.message}")
            }

            try {
                val dailyResp = ApiClient.service.getDailyPick()
                if (dailyResp.success && dailyResp.data != null) {
                    _dailyWallpaperState.value = dailyResp.data.toWallpaperItem(_favorites.value.contains(dailyResp.data.id))
                }
            } catch (e: Exception) {
                // Fallback handled in dailyWallpaper property
            }
        }
    }

    // Featured Wallpaper of the Day
    val dailyWallpaper: WallpaperItem
        get() {
            _dailyWallpaperState.value?.let { return it }
            val dayOfYear = Calendar.getInstance().get(Calendar.DAY_OF_YEAR)
            val candidate = _wallpapers.value.firstOrNull { it.isParallax && !it.isPremium }
                ?: _wallpapers.value.getOrElse(dayOfYear % _wallpapers.value.size) { initialWallpapers[0] }
            return candidate.copy(
                title = "⭐ Daily Pick: ${candidate.title}"
            )
        }

    suspend fun loadMoreWallpapers() {
        if (_isLoadingMore.value || !_hasMore.value) return
        _isLoadingMore.value = true

        val now = System.currentTimeMillis()
        val canAttemptServer = isServerAvailable || (now - lastServerFailureTime > 20_000L)

        if (canAttemptServer) {
            try {
                val nextPage = apiCurrentPage + 1
                val response = ApiClient.service.getWallpapers(page = nextPage, limit = pageSize)
                if (response.success && !response.data.isNullOrEmpty()) {
                    isServerAvailable = true
                    val favSet = _favorites.value
                    val nextItems = response.data.map { it.toWallpaperItem(favSet.contains(it.id)) }
                    _wallpapers.update { current ->
                        val existingIds = current.map { it.id }.toSet()
                        current + nextItems.filter { !existingIds.contains(it.id) }
                    }
                    preloadBitmaps(nextItems)
                    apiCurrentPage = nextPage
                    _hasMore.value = nextPage < (response.pagination?.totalPages ?: 0)
                    _isLoadingMore.value = false
                    return
                }
            } catch (e: Exception) {
                isServerAvailable = false
                lastServerFailureTime = now
                android.util.Log.w("WallpaperRepo", "API pagination failed, fast switching to offline pool: ${e.message}")
            }
        }

        // Fallback to local pagination pool (Zero artificial latency)
        if (currentPoolIndex < pagedWallpapersPool.size) {
            val nextBatch = pagedWallpapersPool.drop(currentPoolIndex).take(pageSize)
            currentPoolIndex += nextBatch.size

            _wallpapers.update { current ->
                val existingIds = current.map { it.id }.toSet()
                current + nextBatch.filter { !existingIds.contains(it.id) }
            }
            preloadBitmaps(nextBatch)

            if (currentPoolIndex >= pagedWallpapersPool.size) {
                _hasMore.value = false
            }
        } else {
            _hasMore.value = false
        }

        _isLoadingMore.value = false
    }

    private fun preloadBitmaps(items: List<WallpaperItem>) {
        try {
            val context = ParallaxApp.instance
            val loader = context.imageLoader
            items.forEach { item ->
                val req = ImageRequest.Builder(context)
                    .data(item.previewUrl)
                    .size(coil.size.Size(360, 540))
                    .precision(coil.size.Precision.INEXACT)
                    .allowHardware(true)
                    .bitmapConfig(android.graphics.Bitmap.Config.RGB_565)
                    .memoryCachePolicy(CachePolicy.ENABLED)
                    .diskCachePolicy(CachePolicy.ENABLED)
                    .build()
                loader.enqueue(req)
            }
        } catch (e: Exception) {
            // Silently ignore preload network errors
        }
    }

    fun toggleFavorite(wallpaperId: String) {
        val willBeFavorite = !_favorites.value.contains(wallpaperId)
        _favorites.update { current ->
            val newSet = if (current.contains(wallpaperId)) {
                current - wallpaperId
            } else {
                current + wallpaperId
            }
            favoritesManager?.saveFavorites(newSet)
            newSet
        }
        _wallpapers.update { list ->
            list.map {
                if (it.id == wallpaperId) it.copy(isFavorite = !it.isFavorite) else it
            }
        }

        // Sync like count to backend
        if (willBeFavorite) {
            repositoryScope.launch {
                try {
                    ApiClient.service.incrementLike(wallpaperId)
                } catch (e: Exception) {
                    // Ignore offline like count sync
                }
            }
        }
    }

    fun recordDownload(wallpaperId: String) {
        _wallpapers.update { list ->
            list.map {
                if (it.id == wallpaperId) it.copy(downloads = it.downloads + 1) else it
            }
        }
        repositoryScope.launch {
            try {
                ApiClient.service.incrementDownload(wallpaperId)
            } catch (e: Exception) {
                // Ignore offline download counter sync
            }
        }
    }

    fun unlockWallpaper(wallpaperId: String) {
        _wallpapers.update { list ->
            list.map {
                if (it.id == wallpaperId) it.copy(isUnlocked = true) else it
            }
        }
    }

    fun getWallpaperById(id: String): WallpaperItem? {
        return _wallpapers.value.find { it.id == id }
    }

    suspend fun fetchOrGetWallpaperById(id: String): WallpaperItem? {
        val cached = getWallpaperById(id)
        if (cached != null) return cached

        return try {
            val response = ApiClient.service.getWallpaperById(id)
            if (response.success && response.data != null) {
                val item = response.data.toWallpaperItem(_favorites.value.contains(id))
                _wallpapers.update { current ->
                    if (current.none { it.id == id }) current + item else current
                }
                item
            } else {
                null
            }
        } catch (e: Exception) {
            android.util.Log.w("WallpaperRepo", "Failed to fetch wallpaper $id: ${e.message}")
            null
        }
    }

    fun filterByTab(tab: ReWallTab, items: List<WallpaperItem>): List<WallpaperItem> {
        return when (tab) {
            ReWallTab.PARALLAX -> items.filter { it.isParallax }
            ReWallTab.THREE_D -> items.filter { it.isParallax || it.layers.isNotEmpty() }
            ReWallTab.TWO_D -> items.filter { !it.isParallax }
            ReWallTab.POPULAR -> items.sortedByDescending { it.likes }
            ReWallTab.RECENT -> items.sortedByDescending { it.createdAt }
        }
    }

    private fun WallpaperResponse.toWallpaperItem(isFav: Boolean): WallpaperItem {
        return WallpaperItem(
            id = this.id,
            title = this.title,
            category = parseCategory(this.category),
            previewUrl = this.previewUrl,
            fullUrl = this.fullUrl ?: this.previewUrl,
            isParallax = this.isParallax,
            layers = this.layers.map { WallpaperLayer(it.id, it.imageUrl, it.depth) },
            downloads = this.downloads,
            likes = this.likes,
            isFavorite = isFav,
            isPremium = this.isPremium,
            isUnlocked = this.isUnlocked,
            createdAt = this.createdAt
        )
    }

    private fun parseCategory(catStr: String?): Category {
        if (catStr.isNullOrBlank()) return Category.AMOLED
        return try {
            Category.valueOf(catStr.uppercase())
        } catch (e: Exception) {
            Category.values().find { it.displayName.equals(catStr, ignoreCase = true) } ?: Category.AMOLED
        }
    }
}
