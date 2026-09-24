package com.parallax.wallpaper.utils

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.text.format.DateUtils
import android.text.format.Formatter
import android.util.Log
import androidx.documentfile.provider.DocumentFile
import com.parallax.wallpaper.model.StatusItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream
import java.io.OutputStream

object WhatsAppStatusManager {

    private const val TAG = "WhatsAppStatusManager"
    private const val PREFS_NAME = "whatsapp_status_saver_prefs"
    private const val KEY_WHATSAPP_URI = "saved_whatsapp_tree_uri"
    private const val KEY_WA_BUSINESS_URI = "saved_wa_business_tree_uri"

    /**
     * Creates an Intent to launch Android's Storage Access Framework (SAF) system picker
     * directly focused on the WhatsApp / WhatsApp Business .Statuses directory.
     */
    fun createOpenDocumentTreeIntent(isBusiness: Boolean = false): Intent {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT_TREE).apply {
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION or
                Intent.FLAG_GRANT_PREFIX_URI_PERMISSION
            )
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val targetFolder = if (isBusiness) {
                "Android%2Fmedia%2Fcom.whatsapp.w4b%2FWhatsApp%20Business%2FMedia%2F.Statuses"
            } else {
                "Android%2Fmedia%2Fcom.whatsapp%2FWhatsApp%2FMedia%2F.Statuses"
            }
            val initialUri = Uri.parse("content://com.android.externalstorage.documents/document/primary:$targetFolder")
            intent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, initialUri)
        }

        return intent
    }

    /**
     * Takes persistable URI permission and saves the Tree URI for subsequent launches.
     */
    fun takeAndSaveUriPermission(context: Context, treeUri: Uri, isBusiness: Boolean = false): Boolean {
        return try {
            val flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
            context.contentResolver.takePersistableUriPermission(treeUri, flags)

            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val key = if (isBusiness) KEY_WA_BUSINESS_URI else KEY_WHATSAPP_URI
            prefs.edit().putString(key, treeUri.toString()).apply()
            true
        } catch (e: Exception) {
            Log.e(TAG, "Error persisting URI permission", e)
            false
        }
    }

    /**
     * Checks if active SAF read permission is retained for the selected WhatsApp version.
     */
    fun hasPermission(context: Context, isBusiness: Boolean = false): Boolean {
        // On Android 10 and below, check if legacy direct file reading is possible
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.Q) {
            val legacyDir = getLegacyStatusFolder(isBusiness)
            if (legacyDir.exists() && legacyDir.canRead()) {
                return true
            }
        }

        val savedUriStr = getSavedUriString(context, isBusiness) ?: return false
        val savedUri = Uri.parse(savedUriStr)

        val persisted = context.contentResolver.persistedUriPermissions
        return persisted.any { it.uri == savedUri && it.isReadPermission }
    }

    /**
     * Fetches real WhatsApp status media files asynchronously from the SAF Document tree or legacy directory.
     */
    suspend fun fetchStatuses(context: Context, isBusiness: Boolean = false): List<StatusItem> = withContext(Dispatchers.IO) {
        val resultList = mutableListOf<StatusItem>()

        try {
            // 1. First attempt: Modern Storage Access Framework (SAF) via DocumentFile (Android 11+)
            val savedUriStr = getSavedUriString(context, isBusiness)
            if (savedUriStr != null) {
                val treeUri = Uri.parse(savedUriStr)
                val rootDoc = DocumentFile.fromTreeUri(context, treeUri)

                if (rootDoc != null && rootDoc.exists()) {
                    // If user selected parent 'Media' or 'com.whatsapp' folder, locate the '.Statuses' child
                    val targetDoc = findStatusesDirectory(rootDoc)
                    val files = targetDoc.listFiles()

                    for (file in files) {
                        if (!file.isFile || file.name.isNullOrBlank()) continue
                        val filename = file.name!!

                        if (filename.equals(".nomedia", ignoreCase = true)) continue

                        val isVideo = filename.endsWith(".mp4", ignoreCase = true) || file.type?.startsWith("video/") == true
                        val isImage = filename.endsWith(".jpg", ignoreCase = true) ||
                                filename.endsWith(".jpeg", ignoreCase = true) ||
                                filename.endsWith(".png", ignoreCase = true) ||
                                file.type?.startsWith("image/") == true

                        if (isVideo || isImage) {
                            val lastMod = file.lastModified()
                            val timeAgoStr = if (lastMod > 0) {
                                DateUtils.getRelativeTimeSpanString(
                                    lastMod,
                                    System.currentTimeMillis(),
                                    DateUtils.MINUTE_IN_MILLIS,
                                    DateUtils.FORMAT_ABBREV_RELATIVE
                                ).toString()
                            } else "Recent"

                            val sizeStr = Formatter.formatFileSize(context, file.length())

                            resultList.add(
                                StatusItem(
                                    id = file.uri.toString(),
                                    title = filename,
                                    mediaUrl = file.uri.toString(),
                                    isVideo = isVideo,
                                    durationText = if (isVideo) "VIDEO" else null,
                                    timeAgo = timeAgoStr,
                                    fileSizeText = sizeStr,
                                    isRealStatus = true,
                                    uriString = file.uri.toString(),
                                    lastModified = lastMod
                                )
                            )
                        }
                    }
                }
            }

            // 2. Fallback attempt: Direct legacy path (Android 10 and below, or custom ROMs)
            if (resultList.isEmpty()) {
                val legacyFolder = getLegacyStatusFolder(isBusiness)
                if (legacyFolder.exists() && legacyFolder.canRead()) {
                    val rawFiles = legacyFolder.listFiles()
                    if (rawFiles != null) {
                        for (f in rawFiles) {
                            if (!f.isFile) continue
                            val fname = f.name
                            if (fname.equals(".nomedia", ignoreCase = true)) continue

                            val isVideo = fname.endsWith(".mp4", ignoreCase = true)
                            val isImage = fname.endsWith(".jpg", ignoreCase = true) || fname.endsWith(".jpeg", ignoreCase = true) || fname.endsWith(".png", ignoreCase = true)

                            if (isVideo || isImage) {
                                val uri = Uri.fromFile(f)
                                val lastMod = f.lastModified()
                                val timeAgoStr = DateUtils.getRelativeTimeSpanString(
                                    lastMod,
                                    System.currentTimeMillis(),
                                    DateUtils.MINUTE_IN_MILLIS,
                                    DateUtils.FORMAT_ABBREV_RELATIVE
                                ).toString()

                                resultList.add(
                                    StatusItem(
                                        id = uri.toString(),
                                        title = fname,
                                        mediaUrl = uri.toString(),
                                        isVideo = isVideo,
                                        durationText = if (isVideo) "VIDEO" else null,
                                        timeAgo = timeAgoStr,
                                        fileSizeText = Formatter.formatFileSize(context, f.length()),
                                        isRealStatus = true,
                                        uriString = uri.toString(),
                                        lastModified = lastMod
                                    )
                                )
                            }
                        }
                    }
                }
            }

            // Sort by latest modified first
            resultList.sortByDescending { it.lastModified }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load WhatsApp statuses", e)
        }

        resultList
    }

    /**
     * Resolves the actual '.Statuses' DocumentFile node if user granted access to a parent folder.
     */
    private fun findStatusesDirectory(root: DocumentFile): DocumentFile {
        val name = root.name.orEmpty()
        if (name.equals(".Statuses", ignoreCase = true)) return root

        // Search one level down for .Statuses
        val child = root.findFile(".Statuses")
        if (child != null && child.isDirectory) return child

        // Search two levels down if user selected Media folder
        val mediaChild = root.findFile("Media")
        if (mediaChild != null && mediaChild.isDirectory) {
            val statusInMedia = mediaChild.findFile(".Statuses")
            if (statusInMedia != null && statusInMedia.isDirectory) return statusInMedia
        }

        return root
    }

    /**
     * Saves a status image or video to public Gallery storage (Pictures/StatusSaver or Movies/StatusSaver)
     */
    suspend fun saveStatusToGallery(context: Context, statusItem: StatusItem): Uri? = withContext(Dispatchers.IO) {
        try {
            val sourceUri = Uri.parse(statusItem.mediaUrl)
            val resolver = context.contentResolver

            val isVideo = statusItem.isVideo
            val ext = if (isVideo) ".mp4" else ".jpg"
            val mimeType = if (isVideo) "video/mp4" else "image/jpeg"
            val filename = "Status_${System.currentTimeMillis()}$ext"

            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, filename)
                put(MediaStore.MediaColumns.MIME_TYPE, mimeType)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    val targetDir = if (isVideo) Environment.DIRECTORY_MOVIES + "/StatusSaver" else Environment.DIRECTORY_PICTURES + "/StatusSaver"
                    put(MediaStore.MediaColumns.RELATIVE_PATH, targetDir)
                    put(MediaStore.MediaColumns.IS_PENDING, 1)
                }
            }

            val collectionUri = if (isVideo) {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }

            val destinationUri = resolver.insert(collectionUri, contentValues) ?: return@withContext null

            resolver.openInputStream(sourceUri)?.use { input: InputStream ->
                resolver.openOutputStream(destinationUri)?.use { output: OutputStream ->
                    input.copyTo(output)
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0)
                resolver.update(destinationUri, contentValues, null, null)
            }

            destinationUri
        } catch (e: Exception) {
            Log.e(TAG, "Error saving status to gallery", e)
            null
        }
    }

    /**
     * Shares status media to any external app (WhatsApp, Instagram, Telegram)
     */
    fun shareStatus(context: Context, statusItem: StatusItem) {
        try {
            val uri = Uri.parse(statusItem.mediaUrl)
            val mime = if (statusItem.isVideo) "video/*" else "image/*"

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = mime
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Status"))
        } catch (e: Exception) {
            Log.e(TAG, "Failed to share status", e)
        }
    }

    /**
     * Plays video status in device's native video player
     */
    fun playVideo(context: Context, statusItem: StatusItem) {
        try {
            val uri = Uri.parse(statusItem.mediaUrl)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, "video/*")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to play video", e)
        }
    }

    private fun getSavedUriString(context: Context, isBusiness: Boolean): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val key = if (isBusiness) KEY_WA_BUSINESS_URI else KEY_WHATSAPP_URI
        return prefs.getString(key, null)
    }

    private fun getLegacyStatusFolder(isBusiness: Boolean): File {
        val root = Environment.getExternalStorageDirectory()
        val waDir = if (isBusiness) "WhatsApp Business" else "WhatsApp"
        return File(root, "$waDir/Media/.Statuses")
    }
}
