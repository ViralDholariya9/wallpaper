package com.parallax.wallpaper

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.disk.DiskCache
import coil.memory.MemoryCache
import coil.request.CachePolicy
import com.parallax.wallpaper.work.DailyWallpaperWorker

class ParallaxApp : Application(), ImageLoaderFactory {

    companion object {
        lateinit var instance: ParallaxApp
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        com.parallax.wallpaper.data.api.ApiClient.init(this)
        com.parallax.wallpaper.charging.ChargingManager.init(this)
        DailyWallpaperWorker.scheduleDaily(this)

        try {
            val filter = android.content.IntentFilter().apply {
                addAction(android.content.Intent.ACTION_POWER_CONNECTED)
                addAction(android.content.Intent.ACTION_POWER_DISCONNECTED)
            }
            androidx.core.content.ContextCompat.registerReceiver(
                this,
                com.parallax.wallpaper.charging.ChargingReceiver(),
                filter,
                androidx.core.content.ContextCompat.RECEIVER_EXPORTED
            )
        } catch (e: Exception) {
            android.util.Log.w("ParallaxApp", "Could not register dynamic ChargingReceiver: ${e.message}")
        }
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .components {
                add(coil.decode.SvgDecoder.Factory())
            }
            .memoryCache {
                MemoryCache.Builder(this)
                    .maxSizePercent(0.25) // Utilize 25% of available JVM heap for ultra-smooth 60-120fps scrolling
                    .build()
            }
            .diskCache {
                DiskCache.Builder()
                    .directory(cacheDir.resolve("rewall_image_cache"))
                    .maxSizeBytes(250L * 1024 * 1024) // 250 MB persistent high-speed disk cache
                    .build()
            }
            .memoryCachePolicy(CachePolicy.ENABLED)
            .diskCachePolicy(CachePolicy.ENABLED)
            .networkCachePolicy(CachePolicy.ENABLED)
            .respectCacheHeaders(false) // Immediately serve from flash cache without slow HTTP revalidation
            .crossfade(120) // Snappy fade-in transition
            .build()
    }
}
