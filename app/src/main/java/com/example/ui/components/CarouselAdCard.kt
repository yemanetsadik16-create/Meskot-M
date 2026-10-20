package com.example.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.AdCampaign
import com.example.data.CarouselAdCard
import com.example.ui.theme.*

/**
 * Facebook-style Horizontal Carousel Sponsored Ad Card
 * Features:
 * 1. HorizontalPager with item snapping and adjacent card peek.
 * 2. Independent card destinations, headlines, and CTA pill buttons.
 * 3. Animated dot indicators showing current carousel position.
 * 4. Interoperable nested touch scrolling inside vertical Feed LazyColumns.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun SponsoredCarouselAdCard(
    campaign: AdCampaign,
    onImpression: () -> Unit = {},
    onClickAd: (cardIndex: Int, isCta: Boolean) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current

    // Trigger impression once when ad enters feed composition
    LaunchedEffect(campaign.id) {
        onImpression()
    }

    val cards = remember(campaign) {
        if (campaign.carouselCards.isNotEmpty()) {
            campaign.carouselCards
        } else {
            // Fallback: If only single media was provided, wrap as a single-card carousel
            listOf(
                CarouselAdCard(
                    id = campaign.id,
                    mediaUrl = campaign.mediaUrl,
                    headline = campaign.headline.ifBlank { campaign.name },
                    description = campaign.primaryText,
                    ctaText = campaign.ctaText.ifBlank { "Shop Now" },
                    destinationUrl = campaign.destinationUrl
                )
            )
        }
    }

    val pagerState = rememberPagerState(pageCount = { cards.size })

    val handleCardClick = { index: Int, isCta: Boolean ->
        val card = cards.getOrNull(index)
        onClickAd(index, isCta)
        val rawUrl = card?.destinationUrl?.trim()?.ifBlank { campaign.destinationUrl.trim() } ?: campaign.destinationUrl.trim()
        if (rawUrl.isNotBlank()) {
            val fullUrl = if (rawUrl.startsWith("http://", true) || rawUrl.startsWith("https://", true)) {
                rawUrl
            } else {
                "https://$rawUrl"
            }
            try {
                uriHandler.openUri(fullUrl)
            } catch (_: Exception) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(fullUrl)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                } catch (_: Exception) {}
            }
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("sponsored_carousel_ad_${campaign.id}"),
        shape = androidx.compose.ui.graphics.RectangleShape,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Meskot brand luxury decorative gold bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(GoldDeep, GoldLight, Gold, GoldDeep)
                        )
                    )
            )

            // Advertiser Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    UserAvatar(
                        photoUrl = campaign.advertiserAvatar,
                        name = campaign.advertiserName,
                        size = 40
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = campaign.advertiserName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = GoldSurface,
                                border = BorderStroke(0.8.dp, GoldBorder)
                            ) {
                                Text(
                                    text = "Sponsored",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldDeep,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                            Text(
                                text = " · ",
                                fontSize = 12.sp,
                                color = MutedText
                            )
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = "Public sponsored ad",
                                tint = GoldDeep,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                IconButton(onClick = {}) {
                    Icon(
                        imageVector = Icons.Default.MoreHoriz,
                        contentDescription = "Ad options",
                        tint = MutedText
                    )
                }
            }

            // Primary Lead Text
            if (campaign.primaryText.isNotBlank()) {
                Text(
                    text = campaign.primaryText,
                    fontSize = 14.sp,
                    color = Ink,
                    lineHeight = 20.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // ==========================================
            // HORIZONTAL CAROUSEL PAGER (Peek & Snap)
            // ==========================================
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(370.dp),
                // Peek configuration: 14dp start padding + 48dp end padding exposes the next card on the right
                contentPadding = PaddingValues(start = 14.dp, end = 48.dp),
                pageSpacing = 12.dp
            ) { pageIndex ->
                val card = cards[pageIndex]
                CarouselCardItem(
                    card = card,
                    campaign = campaign,
                    onCardClick = { handleCardClick(pageIndex, false) },
                    onCtaClick = { handleCardClick(pageIndex, true) }
                )
            }

            // ==========================================
            // CAROUSEL DOT INDICATORS & PAGE COUNTER
            // ==========================================
            if (cards.size > 1) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Indicator Dots
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        repeat(cards.size) { index ->
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
                                    .background(if (isSelected) GoldDeep else Color(0xFFD8D8D8))
                            )
                        }
                    }

                    // Card Count Badge (e.g. 1/4)
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFF0F2F5)
                    ) {
                        Text(
                            text = "${pagerState.currentPage + 1}/${cards.size}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MutedText,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }
                }
            } else {
                Spacer(modifier = Modifier.height(10.dp))
            }
        }
    }
}

/**
 * Individual Carousel Card Item
 */
@Composable
private fun CarouselCardItem(
    card: CarouselAdCard,
    campaign: AdCampaign,
    onCardClick: () -> Unit,
    onCtaClick: () -> Unit
) {
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight()
            .clickable { onCardClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = BorderStroke(1.dp, Color(0xFFE4E6EB)),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.5.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Square 1:1 Media Image
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(Color(0xFFF0F2F5))
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(card.mediaUrl.ifBlank { campaign.mediaUrl })
                        .crossfade(true)
                        .build(),
                    contentDescription = card.headline,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // 2. Card Content & Bottom Action Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color(0xFFFAFAFA))
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    // Small URL Domain
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Language,
                            contentDescription = null,
                            tint = MutedText,
                            modifier = Modifier.size(11.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = extractDomain(card.destinationUrl.ifBlank { campaign.destinationUrl }),
                            fontSize = 11.sp,
                            color = MutedText,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    // Card Headline
                    Text(
                        text = card.headline,
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Optional short subtitle / price / desc
                    if (card.description.isNotBlank()) {
                        Text(
                            text = card.description,
                            fontSize = 12.sp,
                            color = MutedText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }
                }

                // CTA Action Pill Button
                val ctaLabel = card.ctaText.ifBlank { campaign.ctaText.ifBlank { "Shop Now" } }
                Button(
                    onClick = { onCtaClick() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(34.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GoldDeep,
                        contentColor = Color.White
                    ),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp)
                ) {
                    Text(
                        text = ctaLabel,
                        fontSize = 12.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }
        }
    }
}

private fun extractDomain(url: String): String {
    return try {
        if (url.isBlank()) return "MESKOT.COM"
        val clean = url.replace("http://", "").replace("https://", "").split("/").firstOrNull() ?: url
        clean.uppercase()
    } catch (_: Exception) {
        "MESKOT.COM"
    }
}
