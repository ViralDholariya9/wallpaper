package com.parallax.wallpaper.data.api

import android.content.Context
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // Ultra-fast direct ADB reverse tunnel & local connection (0.01s latency)
    // For live production deployment, replace with your domain: https://api.yourdomain.com/
    var baseUrl: String = "http://127.0.0.1:3000/"
        private set

    private var cachedService: WallpaperApiService? = null

    private val okHttpClient by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }
        OkHttpClient.Builder()
            .connectTimeout(2, TimeUnit.SECONDS) // Fail-fast in 2s if offline to prevent UI lag
            .readTimeout(3, TimeUnit.SECONDS)
            .writeTimeout(3, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .addInterceptor(logging)
            .build()
    }

    fun init(context: Context) {
        val prefs = context.getSharedPreferences("rewall_api_prefs", Context.MODE_PRIVATE)
        val savedUrl = prefs.getString("custom_api_url", null)
        if (!savedUrl.isNullOrBlank()) {
            setBaseUrl(savedUrl)
        }
    }

    fun setBaseUrl(newUrl: String) {
        val formatted = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        baseUrl = formatted
        cachedService = null
    }

    val service: WallpaperApiService
        get() {
            return cachedService ?: synchronized(this) {
                cachedService ?: Retrofit.Builder()
                    .baseUrl(baseUrl)
                    .client(okHttpClient)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                    .create(WallpaperApiService::class.java)
                    .also { cachedService = it }
            }
        }
}
