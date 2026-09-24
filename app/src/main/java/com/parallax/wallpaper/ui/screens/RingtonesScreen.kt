package com.parallax.wallpaper.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.wallpaper.data.RingtoneRepository
import com.parallax.wallpaper.model.RingtoneItem
import com.parallax.wallpaper.ui.theme.CardBorder
import com.parallax.wallpaper.ui.theme.CardDark
import com.parallax.wallpaper.ui.theme.DeepObsidian
import com.parallax.wallpaper.ui.theme.NeonCyan
import com.parallax.wallpaper.ui.theme.NeonPurple
import com.parallax.wallpaper.ui.theme.SurfaceDark
import com.parallax.wallpaper.ui.theme.TextPrimary
import com.parallax.wallpaper.ui.theme.TextSecondary
import com.parallax.wallpaper.utils.AudioHelper
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RingtonesScreen(
    repository: RingtoneRepository,
    onBack: () -> Unit,
    onCutRingtone: (RingtoneItem) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val ringtones by repository.ringtones.collectAsState()
    val playingId by repository.currentlyPlayingId.collectAsState()

    var selectedCategory by remember { mutableStateOf("All") }
    val categories = listOf("All", "Cyberpunk", "Ambient", "Marimba", "EDM", "Lo-Fi", "Nature")

    val filteredList = remember(ringtones, selectedCategory) {
        if (selectedCategory == "All") ringtones else ringtones.filter { it.category == selectedCategory }
    }

    DisposableEffect(Unit) {
        onDispose {
            AudioHelper.stopPreview()
            repository.setPlaying(null)
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
                    Icon(
                        imageVector = Icons.Default.GraphicEq,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Ringtones & Sounds",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    )
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

        // Categories Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categories.forEach { cat ->
                val isSelected = cat == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedCategory = cat },
                    label = { Text(cat) },
                    colors = FilterChipDefaults.filterChipColors(
                        containerColor = CardDark,
                        labelColor = TextSecondary,
                        selectedContainerColor = NeonPurple,
                        selectedLabelColor = Color.White
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        borderColor = if (isSelected) NeonCyan else CardBorder,
                        selectedBorderColor = NeonCyan,
                        enabled = true,
                        selected = isSelected
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Ringtone Items List
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredList, key = { it.id }) { item ->
                val isPlaying = item.id == playingId
                RingtoneCardItem(
                    item = item,
                    isPlaying = isPlaying,
                    onPlayToggle = {
                        if (isPlaying) {
                            AudioHelper.stopPreview()
                            repository.setPlaying(null)
                        } else {
                            repository.setPlaying(item.id)
                            AudioHelper.playPreview(context, item.audioUrl) {
                                repository.setPlaying(null)
                            }
                        }
                    },
                    onCutClick = { onCutRingtone(item) },
                    onDownloadClick = {
                        scope.launch {
                            val uri = AudioHelper.saveRingtoneToStorage(context, item.audioUrl, item.title)
                            if (uri != null) {
                                Toast.makeText(context, "Saved to Ringtones/ReWall!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to save ringtone", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    onSetRingtoneClick = {
                        Toast.makeText(context, "Ringtone '${item.title}' set for notifications & calls!", Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }
}

@Composable
private fun RingtoneCardItem(
    item: RingtoneItem,
    isPlaying: Boolean,
    onPlayToggle: () -> Unit,
    onCutClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onSetRingtoneClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CardDark),
        shape = RoundedCornerShape(16.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isPlaying) NeonCyan else CardBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Play / Pause Circle
            Surface(
                shape = CircleShape,
                color = if (isPlaying) NeonCyan else NeonPurple,
                modifier = Modifier
                    .size(46.dp)
                    .clickable { onPlayToggle() }
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = if (isPlaying) Color.Black else Color.White,
                    modifier = Modifier
                        .padding(10.dp)
                        .size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.artist,
                        style = MaterialTheme.typography.bodySmall.copy(color = NeonCyan)
                    )
                    Text(text = "•", color = TextSecondary, fontSize = 10.sp)
                    Text(
                        text = "${item.durationSeconds}s",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextSecondary)
                    )
                }
            }

            // Actions: Cut & Set Ringtone
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onCutClick) {
                    Icon(
                        imageVector = Icons.Default.ContentCut,
                        contentDescription = "Cut Ringtone",
                        tint = NeonCyan,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onDownloadClick) {
                    Icon(
                        imageVector = Icons.Default.Download,
                        contentDescription = "Download",
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                IconButton(onClick = onSetRingtoneClick) {
                    Icon(
                        imageVector = Icons.Default.NotificationsActive,
                        contentDescription = "Set Ringtone",
                        tint = NeonPurple,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}
