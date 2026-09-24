package com.parallax.wallpaper.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.parallax.wallpaper.data.api.HeroBannerResponse
import com.parallax.wallpaper.ui.theme.CardDark
import com.parallax.wallpaper.ui.theme.NeonCyan
import com.parallax.wallpaper.ui.theme.NeonPink
import com.parallax.wallpaper.ui.theme.NeonPurple
import kotlinx.coroutines.delay

@Composable
fun HeroBannerCarousel(
    banners: List<HeroBannerResponse>,
    onBannerClick: (HeroBannerResponse) -> Unit,
    modifier: Modifier = Modifier
) {
    if (banners.isEmpty()) return

    val pagerState = rememberPagerState(pageCount = { banners.size })

    // Auto-advance banner carousel every 4.5 seconds
    LaunchedEffect(banners.size, pagerState.currentPage) {
        if (banners.size > 1) {
            delay(4500L)
            val next = (pagerState.currentPage + 1) % banners.size
            pagerState.animateScrollToPage(next)
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Card(
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .border(
                    width = 1.2.dp,
                    brush = Brush.horizontalGradient(
                        listOf(
                            NeonCyan.copy(alpha = 0.6f),
                            NeonPurple.copy(alpha = 0.7f),
                            NeonPink.copy(alpha = 0.5f)
                        )
                    ),
                    shape = RoundedCornerShape(22.dp)
                )
                .clip(RoundedCornerShape(22.dp))
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier.fillMaxSize()
                ) { page ->
                    val banner = banners[page]
                    HeroBannerSlideItem(
                        banner = banner,
                        onClick = { onBannerClick(banner) }
                    )
                }

                // Page Indicator Dots (Bottom End)
                if (banners.size > 1) {
                    Row(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(end = 16.dp, bottom = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(banners.size) { index ->
                            val isSelected = pagerState.currentPage == index
                            val width by animateDpAsState(
                                targetValue = if (isSelected) 18.dp else 6.dp,
                                label = "dot_width"
                            )
                            Box(
                                modifier = Modifier
                                    .height(6.dp)
                                    .width(width)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) NeonCyan
                                        else Color.White.copy(alpha = 0.35f)
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroBannerSlideItem(
    banner: HeroBannerResponse,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onClick() }
    ) {
        // High-Quality Banner Backdrop Image
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(banner.imageUrl)
                .crossfade(true)
                .memoryCachePolicy(CachePolicy.ENABLED)
                .diskCachePolicy(CachePolicy.ENABLED)
                .build(),
            contentDescription = banner.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Dark Gradient Scrim Overlay for Text Readability
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.15f),
                            Color.Black.copy(alpha = 0.45f),
                            Color(0xFF070C16).copy(alpha = 0.95f)
                        )
                    )
                )
        )

        // Content Column
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.Bottom
        ) {
            // Optional Glowing Highlight Badge (e.g. FESTIVAL SPECIAL, POPULAR 3D)
            if (!banner.badgeText.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(999.dp),
                    color = Color.Transparent,
                    modifier = Modifier
                        .border(
                            1.dp,
                            Brush.horizontalGradient(listOf(Color(0xFFF59E0B), Color(0xFFEF4444))),
                            RoundedCornerShape(999.dp)
                        )
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    Color(0xFFF59E0B).copy(alpha = 0.85f),
                                    Color(0xFFEF4444).copy(alpha = 0.85f)
                                )
                            ),
                            shape = RoundedCornerShape(999.dp)
                        )
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = banner.badgeText.uppercase(),
                            color = Color.White,
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 0.6.sp
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
            }

            // Headline Title
            Text(
                text = banner.title,
                color = Color.White,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Subtitle Tagline
            if (!banner.subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = banner.subtitle,
                    color = Color(0xFFCBD5E1),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Interactive Action Chip
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = NeonCyan,
                modifier = Modifier.clickable { onClick() }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "EXPLORE NOW",
                        color = Color(0xFF030712),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = Color(0xFF030712),
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }
    }
}
