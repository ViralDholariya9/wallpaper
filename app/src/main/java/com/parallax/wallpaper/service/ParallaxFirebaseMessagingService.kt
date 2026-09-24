package com.parallax.wallpaper.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import coil.ImageLoader
import coil.request.ImageRequest
import coil.request.SuccessResult
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.parallax.wallpaper.MainActivity
import com.parallax.wallpaper.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles Firebase Cloud Messaging (FCM) push notifications.
 * Supports:
 * - Direct Console Push messages (Title + Body)
 * - Data payload messages (custom wallpaper IDs, deep links, image URLs)
 * - Rich BigPictureStyle notifications with wallpaper previews
 */
class ParallaxFirebaseMessagingService : FirebaseMessagingService() {

    companion object {
        private const val TAG = "ParallaxFCM"
        const val CHANNEL_ID = "daily_wallpaper_channel"
        const val CHANNEL_NAME = "Daily Featured Wallpaper"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "From: ${remoteMessage.from}")

        // 1. Extract Title & Body
        val title = remoteMessage.notification?.title
            ?: remoteMessage.data["title"]
            ?: "✨ Daily 4K Wallpaper Pick!"

        val body = remoteMessage.notification?.body
            ?: remoteMessage.data["body"]
            ?: "A stunning new 3D parallax wallpaper is waiting for your screen."

        val imageUrl = remoteMessage.notification?.imageUrl?.toString()
            ?: remoteMessage.data["image_url"]

        val wallpaperId = remoteMessage.data["wallpaper_id"]

        // 2. Fetch image in background if present for BigPictureStyle notification
        if (!imageUrl.isNullOrEmpty()) {
            CoroutineScope(Dispatchers.IO).launch {
                val bitmap = fetchBitmap(imageUrl)
                showNotification(title, body, bitmap, wallpaperId)
            }
        } else {
            showNotification(title, body, null, wallpaperId)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "New FCM Registration Token: $token")
        // Token is stored in SharedPreferences for easy retrieval
        val prefs = getSharedPreferences("parallax_fcm_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("fcm_token", token).apply()

        // Sync with backend server database in background
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val model = "${Build.MANUFACTURER} ${Build.MODEL}"
                com.parallax.wallpaper.data.api.ApiClient.service.registerDeviceToken(
                    com.parallax.wallpaper.data.api.DeviceTokenRequest(
                        token = token,
                        deviceModel = model,
                        appVersion = "1.0.0"
                    )
                )
                Log.d(TAG, "Successfully registered device token with ReWall backend")
            } catch (e: Exception) {
                Log.w(TAG, "Could not sync device token with server: ${e.message}")
            }
        }
    }

    private suspend fun fetchBitmap(url: String): Bitmap? {
        return try {
            val loader = ImageLoader(applicationContext)
            val request = ImageRequest.Builder(applicationContext)
                .data(url)
                .allowHardware(false)
                .build()
            val result = (loader.execute(request) as? SuccessResult)?.drawable
            (result as? BitmapDrawable)?.bitmap
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load notification bitmap: ${e.message}")
            null
        }
    }

    private fun showNotification(
        title: String,
        body: String,
        imageBitmap: Bitmap?,
        wallpaperId: String?
    ) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create high importance notification channel on Android 8+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Daily 4D and 3D parallax wallpaper notifications"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (!wallpaperId.isNullOrEmpty()) {
                putExtra("extra_wallpaper_id", wallpaperId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            System.currentTimeMillis().toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)

        // If rich image preview is available, display BigPictureStyle
        if (imageBitmap != null) {
            notificationBuilder.setStyle(
                NotificationCompat.BigPictureStyle()
                    .bigPicture(imageBitmap)
                    .setSummaryText(body)
            )
            notificationBuilder.setLargeIcon(imageBitmap)
        } else {
            notificationBuilder.setStyle(NotificationCompat.BigTextStyle().bigText(body))
        }

        notificationManager.notify((System.currentTimeMillis() % 100000).toInt(), notificationBuilder.build())
    }
}
