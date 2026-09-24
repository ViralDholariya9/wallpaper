package com.parallax.wallpaper.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Wallpaper
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.parallax.wallpaper.ui.theme.CardBorder
import com.parallax.wallpaper.ui.theme.CardDark
import com.parallax.wallpaper.ui.theme.DeepObsidian
import com.parallax.wallpaper.ui.theme.NeonCyan
import com.parallax.wallpaper.ui.theme.NeonPurple
import com.parallax.wallpaper.ui.theme.SurfaceDark
import com.parallax.wallpaper.ui.theme.TextPrimary
import com.parallax.wallpaper.ui.theme.TextSecondary
import com.parallax.wallpaper.utils.WallpaperTarget

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetWallpaperBottomSheet(
    isParallax: Boolean,
    onDismiss: () -> Unit,
    onSetStaticTarget: (WallpaperTarget) -> Unit,
    onApplyLiveParallax: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = SurfaceDark,
        dragHandle = {
            Surface(
                modifier = Modifier.padding(vertical = 12.dp),
                color = CardBorder,
                shape = RoundedCornerShape(2.dp)
            ) {
                Spacer(modifier = Modifier.size(width = 36.dp, height = 4.dp))
            }
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp)
        ) {
            Text(
                text = "Apply Wallpaper",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Choose where to set your selected wallpaper",
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
            Spacer(modifier = Modifier.height(20.dp))

            if (isParallax) {
                WallpaperActionItem(
                    icon = Icons.Default.AutoAwesome,
                    iconTint = NeonCyan,
                    title = "Set as 3D Parallax Live Wallpaper",
                    subtitle = "Interactive multi-layer tilt on home & lock screen",
                    isHighlight = true,
                    onClick = {
                        onDismiss()
                        onApplyLiveParallax()
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = CardBorder.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))
            }

            WallpaperActionItem(
                icon = Icons.Default.Home,
                iconTint = NeonPurple,
                title = "Home Screen",
                subtitle = "Set static image on home screen",
                onClick = {
                    onDismiss()
                    onSetStaticTarget(WallpaperTarget.HOME_SCREEN)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            WallpaperActionItem(
                icon = Icons.Default.Lock,
                iconTint = NeonPurple,
                title = "Lock Screen",
                subtitle = "Set static image on lock screen",
                onClick = {
                    onDismiss()
                    onSetStaticTarget(WallpaperTarget.LOCK_SCREEN)
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            WallpaperActionItem(
                icon = Icons.Default.Wallpaper,
                iconTint = NeonPurple,
                title = "Both Screens",
                subtitle = "Apply to both Home and Lock screen",
                onClick = {
                    onDismiss()
                    onSetStaticTarget(WallpaperTarget.BOTH)
                }
            )

            Spacer(modifier = Modifier.height(28.dp))
        }
    }
}

@Composable
private fun WallpaperActionItem(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    isHighlight: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (isHighlight) NeonPurple.copy(alpha = 0.15f) else CardDark,
                shape = RoundedCornerShape(14.dp)
            )
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(10.dp),
            color = DeepObsidian,
            modifier = Modifier.size(44.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier
                    .padding(10.dp)
                    .size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    color = if (isHighlight) NeonCyan else TextPrimary
                )
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium.copy(color = TextSecondary)
            )
        }
    }
}
