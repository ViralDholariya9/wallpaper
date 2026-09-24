package com.parallax.wallpaper.data

import android.content.Context
import android.net.Uri
import com.parallax.wallpaper.model.StatusItem
import com.parallax.wallpaper.utils.WhatsAppStatusManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class StatusRepository {

    private val scope = CoroutineScope(Dispatchers.IO)

    // Demo samples to display as interactive preview before user grants SAF permission
    private val sampleStatuses = listOf(
        StatusItem(
            id = "s1",
            title = "Sunset at Bali.jpg",
            mediaUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?q=80&w=800&auto=format&fit=crop",
            isVideo = false,
            timeAgo = "10 mins ago",
            fileSizeText = "1.2 MB",
            isRealStatus = false
        ),
        StatusItem(
            id = "s2",
            title = "Cyber City Neon Night.mp4",
            mediaUrl = "https://images.unsplash.com/photo-1509198397868-475647b2a1e5?q=80&w=800&auto=format&fit=crop",
            isVideo = true,
            durationText = "0:15",
            timeAgo = "25 mins ago",
            fileSizeText = "4.8 MB",
            isRealStatus = false
        ),
        StatusItem(
            id = "s3",
            title = "Mountain Waterfall Flow.mp4",
            mediaUrl = "https://images.unsplash.com/photo-1432405972618-c60b0225b8f9?q=80&w=800&auto=format&fit=crop",
            isVideo = true,
            durationText = "0:30",
            timeAgo = "1 hour ago",
            fileSizeText = "6.1 MB",
            isRealStatus = false
        ),
        StatusItem(
            id = "s4",
            title = "Minimalist Coffee Morning.jpg",
            mediaUrl = "https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?q=80&w=800&auto=format&fit=crop",
            isVideo = false,
            timeAgo = "2 hours ago",
            fileSizeText = "980 KB",
            isRealStatus = false
        ),
        StatusItem(
            id = "s5",
            title = "Northern Lights Aurora.jpg",
            mediaUrl = "https://images.unsplash.com/photo-1517411032315-54ef2cb783bb?q=80&w=800&auto=format&fit=crop",
            isVideo = false,
            timeAgo = "3 hours ago",
            fileSizeText = "1.5 MB",
            isRealStatus = false
        ),
        StatusItem(
            id = "s6",
            title = "Tokyo Street Festival.mp4",
            mediaUrl = "https://images.unsplash.com/photo-1493976040374-85c8e12f0c0e?q=80&w=800&auto=format&fit=crop",
            isVideo = true,
            durationText = "0:22",
            timeAgo = "5 hours ago",
            fileSizeText = "3.4 MB",
            isRealStatus = false
        )
    )

    private val _isBusiness = MutableStateFlow(false)
    val isBusiness: StateFlow<Boolean> = _isBusiness.asStateFlow()

    private val _isPermissionGranted = MutableStateFlow(false)
    val isPermissionGranted: StateFlow<Boolean> = _isPermissionGranted.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _statuses = MutableStateFlow(sampleStatuses)
    val statuses: StateFlow<List<StatusItem>> = _statuses.asStateFlow()

    /**
     * Initializes status repository with current context: checks permissions & loads real files
     */
    fun refresh(context: Context) {
        val hasPerm = WhatsAppStatusManager.hasPermission(context, _isBusiness.value)
        _isPermissionGranted.value = hasPerm

        if (hasPerm) {
            loadRealStatuses(context)
        } else {
            // Display sample statuses as demo preview until permission is granted
            _statuses.value = sampleStatuses
        }
    }

    /**
     * Scans real WhatsApp status folder via Storage Access Framework
     */
    fun loadRealStatuses(context: Context) {
        scope.launch {
            _isLoading.value = true
            val realList = WhatsAppStatusManager.fetchStatuses(context, _isBusiness.value)
            if (realList.isNotEmpty()) {
                _statuses.value = realList
            } else {
                // If permission is granted but user hasn't viewed statuses yet, empty list
                _statuses.value = emptyList()
            }
            _isLoading.value = false
        }
    }

    /**
     * Persists URI permission after user selects folder in Android system picker
     */
    fun onFolderSelected(context: Context, treeUri: Uri) {
        val success = WhatsAppStatusManager.takeAndSaveUriPermission(context, treeUri, _isBusiness.value)
        if (success) {
            _isPermissionGranted.value = true
            loadRealStatuses(context)
        }
    }

    /**
     * Toggles between standard WhatsApp and WhatsApp Business
     */
    fun switchWhatsAppType(context: Context, isBusiness: Boolean) {
        _isBusiness.value = isBusiness
        refresh(context)
    }

    /**
     * Saves status to Gallery storage
     */
    suspend fun saveStatus(context: Context, item: StatusItem): Uri? {
        return WhatsAppStatusManager.saveStatusToGallery(context, item)
    }

    /**
     * Shares status to other apps
     */
    fun shareStatus(context: Context, item: StatusItem) {
        WhatsAppStatusManager.shareStatus(context, item)
    }

    /**
     * Plays video in native video player
     */
    fun playVideo(context: Context, item: StatusItem) {
        WhatsAppStatusManager.playVideo(context, item)
    }
}
