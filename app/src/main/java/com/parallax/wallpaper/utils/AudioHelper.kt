package com.parallax.wallpaper.utils

import android.content.ContentValues
import android.content.Context
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.io.OutputStream
import java.net.URL

object AudioHelper {

    private var mediaPlayer: MediaPlayer? = null
    private var currentPlayingUrl: String? = null

    fun playPreview(
        context: Context,
        audioUrl: String,
        onCompletion: () -> Unit
    ) {
        try {
            stopPreview()
            currentPlayingUrl = audioUrl
            mediaPlayer = MediaPlayer().apply {
                setDataSource(audioUrl)
                setOnPreparedListener { start() }
                setOnCompletionListener {
                    stopPreview()
                    onCompletion()
                }
                setOnErrorListener { _, _, _ ->
                    stopPreview()
                    onCompletion()
                    true
                }
                prepareAsync()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            stopPreview()
            onCompletion()
        }
    }

    fun stopPreview() {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
        } catch (e: Exception) {
            // Ignored
        } finally {
            mediaPlayer = null
            currentPlayingUrl = null
        }
    }

    suspend fun saveRingtoneToStorage(
        context: Context,
        audioUrl: String,
        title: String
    ): Uri? = withContext(Dispatchers.IO) {
        try {
            val filename = "ReWall_${title.replace(" ", "_")}.mp3"
            val contentValues = ContentValues().apply {
                put(MediaStore.Audio.Media.DISPLAY_NAME, filename)
                put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp3")
                put(MediaStore.Audio.Media.IS_RINGTONE, 1)
                put(MediaStore.Audio.Media.IS_NOTIFICATION, 1)
                put(MediaStore.Audio.Media.IS_ALARM, 1)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_RINGTONES + "/ReWall")
                    put(MediaStore.Audio.Media.IS_PENDING, 1)
                }
            }

            val resolver = context.contentResolver
            val uri = resolver.insert(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, contentValues)

            if (uri != null) {
                val inputStream: InputStream = URL(audioUrl).openStream()
                val outputStream: OutputStream? = resolver.openOutputStream(uri)
                if (outputStream != null) {
                    inputStream.use { input ->
                        outputStream.use { output ->
                            input.copyTo(output)
                        }
                    }
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        contentValues.clear()
                        contentValues.put(MediaStore.Audio.Media.IS_PENDING, 0)
                        resolver.update(uri, contentValues, null, null)
                    }
                    return@withContext uri
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        null
    }
}
