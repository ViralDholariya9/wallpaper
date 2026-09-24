package com.parallax.wallpaper.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.parallax.wallpaper.data.api.AnnouncementConfigResponse

@Composable
fun AnnouncementBanner(
    announcement: AnnouncementConfigResponse,
    onDismiss: () -> Unit,
    onActionClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (!announcement.enabled || announcement.text.isBlank()) return

    val theme = announcement.theme.lowercase().trim()
    val (gradient, textColor, iconEmoji, borderColor) = when (theme) {
        "diwali_gold" -> Quadruple(
            Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706), Color(0xFFFBBF24))),
            Color(0xFFFFFBEB),
            "✨",
            Color(0xFFFDE68A).copy(alpha = 0.6f)
        )
        "cyber_neon" -> Quadruple(
            Brush.horizontalGradient(listOf(Color(0xFF00E5FF), Color(0xFF8B5CF6))),
            Color(0xFF030712),
            "⚡",
            Color(0xFF00E5FF).copy(alpha = 0.7f)
        )
        "aurora_emerald" -> Quadruple(
            Brush.horizontalGradient(listOf(Color(0xFF10B981), Color(0xFF047857))),
            Color(0xFFECFDF5),
            "🌲",
            Color(0xFFA7F3D0).copy(alpha = 0.5f)
        )
        "crimson_fire" -> Quadruple(
            Brush.horizontalGradient(listOf(Color(0xFFEF4444), Color(0xFF991B1B))),
            Color(0xFFFEF2F2),
            "🔥",
            Color(0xFFFECACA).copy(alpha = 0.5f)
        )
        else -> Quadruple(
            Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFD97706))),
            Color.White,
            "🎉",
            Color(0xFFFDE68A).copy(alpha = 0.5f)
        )
    }

    AnimatedVisibility(
        visible = true,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
    ) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp)
                .shadow(elevation = 6.dp, shape = RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(gradient)
                .border(1.dp, borderColor, RoundedCornerShape(14.dp))
                .clickable {
                    if (announcement.actionTarget.isNotBlank()) {
                        onActionClick(announcement.actionTarget)
                    }
                }
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = iconEmoji,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = announcement.text,
                        style = MaterialTheme.typography.bodySmall.copy(
                            fontWeight = FontWeight.Bold,
                            color = textColor,
                            fontSize = 12.sp,
                            letterSpacing = 0.2.sp
                        ),
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (announcement.dismissable) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = textColor.copy(alpha = 0.85f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

private data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
