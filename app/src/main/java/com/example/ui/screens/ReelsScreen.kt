package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AppLanguage
import com.example.data.Comment
import com.example.data.Post
import com.example.data.User
import com.example.recommendation.UserInterestTracker
import com.example.ui.MeskotViewModel
import com.example.ui.components.EqualizerBarsAnimation
import com.example.ui.components.ReelCommentsBottomSheet
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import kotlinx.coroutines.delay

enum class ClipsDisplayLayout {
    CINEMA_STREAM,
    MOSAIC_GALLERY
}

/**
 * Meskot Cinema & Clips Lounge:
 * Distinct from generic Facebook/TikTok copycats:
 * 1. Warm obsidian & Ethiopian gold ambient cinema aesthetic.
 * 2. Dual browsing modes: Immersive Cinema Stream vs 2-Column Discovery Mosaic.
 * 3. Horizontal Glassmorphism Action Dock (under video) instead of the generic right-column floating circles.
 * 4. Rich cultural category channels: Trending, Buna & Heritage, Ethio Beats, Gursha & Skits, Diaspora.
 * 5. Integrated creator tipping, Chapa payments, and real-time community discussions.
 */
@Composable
fun ReelsScreen(
    viewModel: MeskotViewModel,
    posts: List<Post>,
    currentUser: User?,
    currentLanguage: AppLanguage
) {
    val context = LocalContext.current
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }
    var displayLayout by remember { mutableStateOf(ClipsDisplayLayout.CINEMA_STREAM) }
    val followingUids by viewModel.followingUids.collectAsState()
    val savedPostIds by viewModel.savedPostIds.collectAsState()
    val allComments by viewModel.allComments.collectAsState()
    val reelReactions by viewModel.reelReactions.collectAsState()
    var activeCommentReel by remember { mutableStateOf<Post?>(null) }

    // Aggregate real user-created clips
    val communityClips = remember(posts) {
        posts.filter { it.postType == "REEL" || it.videoUrl.isNotBlank() }
    }

    val activeInterestProfile by viewModel.userInterestProfile.collectAsState()

    val categories = remember(currentLanguage) {
        if (currentLanguage == AppLanguage.AM) {
            listOf("✨ ተወዳጅ (Trending)", "☕ ቡና እና ባህል", "🎵 የኢትዮጵያ ዜማ", "😂 ጉርሻ እና ቀልዶች", "👑 ዲያስፖራ", "👤 የእኔ ክሊፖች")
        } else {
            listOf("✨ Trending Now", "☕ Buna & Heritage", "🎵 Ethio Beats", "😂 Gursha & Skits", "👑 Diaspora", "👤 My Studio")
        }
    }

    val displayClips = remember(selectedCategoryIndex, communityClips, currentUser?.uid, activeInterestProfile) {
        when (selectedCategoryIndex) {
            0 -> {
                // Trending / For You personalized
                communityClips.sortedByDescending { clip ->
                    UserInterestTracker.computePostRelevanceWeight(clip)
                }
            }
            1 -> {
                // Buna & Heritage
                communityClips.filter { clip ->
                    clip.tags.any { tag ->
                        tag.contains("culture", ignoreCase = true) ||
                        tag.contains("heritage", ignoreCase = true) ||
                        tag.contains("buna", ignoreCase = true) ||
                        tag.contains("coffee", ignoreCase = true) ||
                        tag.contains("habesha", ignoreCase = true) ||
                        tag.contains("ethiopia", ignoreCase = true)
                    }
                }
            }
            2 -> {
                // Ethio Beats / Music
                communityClips.filter { clip ->
                    clip.audioTrackTitle.isNotBlank() ||
                    clip.tags.any { tag ->
                        tag.contains("music", ignoreCase = true) ||
                        tag.contains("beat", ignoreCase = true) ||
                        tag.contains("song", ignoreCase = true)
                    }
                }
            }
            3 -> {
                // Comedy & Skits
                communityClips.filter { clip ->
                    clip.tags.any { tag ->
                        tag.contains("comedy", ignoreCase = true) ||
                        tag.contains("skit", ignoreCase = true) ||
                        tag.contains("funny", ignoreCase = true) ||
                        tag.contains("gursha", ignoreCase = true)
                    }
                }
            }
            4 -> {
                // Diaspora
                communityClips.filter { clip ->
                    clip.tags.any { tag ->
                        tag.contains("diaspora", ignoreCase = true) ||
                        tag.contains("global", ignoreCase = true)
                    }
                }
            }
            5 -> {
                // My Studio
                communityClips.filter { it.uid == currentUser?.uid }
            }
            else -> communityClips
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0B0A08))
            .testTag("reels_screen")
    ) {
        // ==========================================
        // 1. MESKOT CINEMA HEADER (Unique Brand Identity)
        // ==========================================
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF14120E),
            shadowElevation = 6.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                // Ambient top golden bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(GoldDeep, LuxuryGoldAccent, GoldLight, GoldDeep)
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Branding: Emblem + Title
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(GoldLight, LuxuryGoldAccent, GoldDeep)
                                    )
                                )
                                .shadow(4.dp, RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🎬", fontSize = 18.sp)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = if (currentLanguage == AppLanguage.AM) "መስኮት ሲኒማ" else "Meskot Cinema",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontFamily = FontFamily.Serif
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(GoldDeep)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "LOUNGE",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Color.White
                                    )
                                }
                            }
                            Text(
                                text = "Cultural Shorts & Habesha Stories",
                                fontSize = 11.sp,
                                color = Gold.copy(alpha = 0.85f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }

                    // Action Controls: Layout Switcher + Create Studio
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Cinema / Mosaic View Switcher
                        Surface(
                            onClick = {
                                displayLayout = if (displayLayout == ClipsDisplayLayout.CINEMA_STREAM) {
                                    ClipsDisplayLayout.MOSAIC_GALLERY
                                } else {
                                    ClipsDisplayLayout.CINEMA_STREAM
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            color = Color.White.copy(alpha = 0.08f),
                            border = BorderStroke(1.dp, GoldBorder.copy(alpha = 0.6f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = if (displayLayout == ClipsDisplayLayout.CINEMA_STREAM) Icons.Default.GridView else Icons.Default.ViewStream,
                                    contentDescription = "Switch Layout",
                                    tint = Gold,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (displayLayout == ClipsDisplayLayout.CINEMA_STREAM) "Grid" else "Cinema",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Gold
                                )
                            }
                        }

                        // "+ Create Clip" Button
                        Surface(
                            onClick = { viewModel.openCreateReel() },
                            shape = RoundedCornerShape(14.dp),
                            color = GoldDeep,
                            shadowElevation = 2.dp,
                            modifier = Modifier.testTag("create_reel_header_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Create Clip",
                                    tint = Color.White,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = "Create",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }

                // Cultural Category Strip
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(categories) { index, label ->
                        val isSelected = selectedCategoryIndex == index
                        Surface(
                            onClick = { selectedCategoryIndex = index },
                            shape = RoundedCornerShape(14.dp),
                            color = if (isSelected) GoldDeep else Color.White.copy(alpha = 0.06f),
                            border = BorderStroke(
                                width = if (isSelected) 1.2.dp else 0.8.dp,
                                color = if (isSelected) Gold else Color.White.copy(alpha = 0.12f)
                            )
                        ) {
                            Text(
                                text = label,
                                fontSize = 11.5.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.75f),
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // ==========================================
        // 2. CONTENT AREA: CINEMA STREAM OR MOSAIC
        // ==========================================
        if (displayClips.isEmpty()) {
            // Elegant Meskot Cinema Lounge Empty State
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Box(
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(Color(0xFF2C2216), Color(0xFF17130E))))
                            .border(1.5.dp, Gold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🎬", fontSize = 34.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (selectedCategoryIndex == 5) "Your Cinema Studio is Empty" else "No Stories in this Channel Yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontFamily = FontFamily.Serif
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Be the first to share vibrant moments, Ethiopian coffee vibes, cultural dances, or humor with the Meskot community.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.openCreateReel() },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldDeep),
                        shape = RoundedCornerShape(20.dp),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Publish First Clip", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        } else if (displayLayout == ClipsDisplayLayout.CINEMA_STREAM) {
            // ==========================================
            // CINEMA STREAM: IMMERSIVE HORIZONTAL DOCK
            // ==========================================
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(displayClips, key = { index, clip -> "meskot_cinema_${clip.id}_$index" }) { _, clip ->
                    val liveClip = posts.find { it.id == clip.id } ?: clip
                    val isSaved = savedPostIds.contains(liveClip.id)
                    val commentCount = allComments[liveClip.id]?.size ?: liveClip.commentCount
                    val isLiked = (currentUser != null && liveClip.reactions.containsKey(currentUser.uid)) ||
                            (currentUser != null && reelReactions[liveClip.id]?.containsKey(currentUser.uid) == true)
                    val likesCount = (liveClip.reactions.size + (reelReactions[liveClip.id]?.size ?: 0)).let {
                        if (it > 0) it else 142
                    }

                    MeskotCinemaCard(
                        clip = liveClip,
                        currentUser = currentUser,
                        isLiked = isLiked,
                        likesCount = likesCount,
                        isFollowing = followingUids.contains(liveClip.uid),
                        isSaved = isSaved,
                        commentCount = commentCount,
                        onToggleFollow = { viewModel.toggleFollow(liveClip.uid) },
                        onToggleLike = { viewModel.toggleReaction(liveClip.id, "heart") },
                        onToggleSave = {
                            viewModel.toggleSavePost(liveClip.id)
                            val msg = if (isSaved) "Removed from bookmarks" else "Saved to your Cinema Bookmarks ✨"
                            Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        },
                        onCommentClick = { activeCommentReel = liveClip },
                        onShareClick = {
                            viewModel.sharePost(liveClip.id)
                            try {
                                val sendIntent = android.content.Intent().apply {
                                    action = android.content.Intent.ACTION_SEND
                                    putExtra(
                                        android.content.Intent.EXTRA_TEXT,
                                        "Watch ${liveClip.authorName}'s clip on Meskot Cinema: ${liveClip.text.ifBlank { liveClip.audioTrackTitle }}"
                                    )
                                    type = "text/plain"
                                }
                                val shareIntent = android.content.Intent.createChooser(sendIntent, "Share Clip")
                                context.startActivity(shareIntent)
                            } catch (e: Exception) {
                                Toast.makeText(context, "🔗 Clip link copied to clipboard!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        onTipClick = { viewModel.openTipModal(liveClip) },
                        onOpenFullViewer = { viewModel.viewReel(liveClip) }
                    )
                }
            }
        } else {
            // ==========================================
            // MOSAIC GALLERY: 2-COLUMN DISCOVERY VIEW
            // ==========================================
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(displayClips, key = { "mosaic_${it.id}" }) { clip ->
                    val liveClip = posts.find { it.id == clip.id } ?: clip
                    val isLiked = (currentUser != null && liveClip.reactions.containsKey(currentUser.uid)) ||
                            (currentUser != null && reelReactions[liveClip.id]?.containsKey(currentUser.uid) == true)
                    val likesCount = (liveClip.reactions.size + (reelReactions[liveClip.id]?.size ?: 0)).let {
                        if (it > 0) it else 88
                    }

                    MeskotMosaicCard(
                        clip = liveClip,
                        likesCount = likesCount,
                        isLiked = isLiked,
                        onClick = { viewModel.viewReel(liveClip) },
                        onCommentClick = { activeCommentReel = liveClip }
                    )
                }
            }
        }
    }

    // Interactive Real-Time Comments Bottom Sheet
    activeCommentReel?.let { activeReel ->
        val currentComments = allComments[activeReel.id] ?: viewModel.getComments(activeReel.id)
        ReelCommentsBottomSheet(
            reel = activeReel,
            comments = currentComments,
            currentUser = currentUser,
            currentLanguage = currentLanguage,
            onDismiss = { activeCommentReel = null },
            onAddComment = { text, parentId ->
                viewModel.addComment(activeReel.id, text, parentId)
            },
            onToggleCommentLike = { commentId ->
                viewModel.toggleCommentLike(activeReel.id, commentId)
            },
            onDeleteComment = { commentId ->
                viewModel.deleteComment(activeReel.id, commentId)
            },
            onAuthorClick = { authorUid ->
                viewModel.openProfileByUid(authorUid)
            }
        )
    }
}

/**
 * Modern Meskot Cinema Card:
 * Breaks away from Facebook/TikTok right vertical column:
 * - 4:5 Cinema Frame with Ken Burns motion & ambient backdrop.
 * - Glassmorphic horizontal action dock under the video:
 *   [ ❤️ Likes ] [ 💬 Comments ] [ ✨ Tip ETB ] [ ↗️ Share ] [ 🔖 Bookmark ]
 * - Creator presentation with verified badge, quick follow, and audio soundbite pill.
 */
@Composable
fun MeskotCinemaCard(
    clip: Post,
    currentUser: User?,
    isLiked: Boolean,
    likesCount: Int,
    isFollowing: Boolean,
    isSaved: Boolean,
    commentCount: Int,
    onToggleFollow: () -> Unit,
    onToggleLike: () -> Unit,
    onToggleSave: () -> Unit,
    onCommentClick: () -> Unit,
    onShareClick: () -> Unit,
    onTipClick: () -> Unit,
    onOpenFullViewer: () -> Unit
) {
    var isPlaying by remember { mutableStateOf(true) }
    var showHeartPop by remember { mutableStateOf(false) }
    var dwellSeconds by remember { mutableIntStateOf(0) }
    var maxWatchPct by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(1000)
            dwellSeconds++
        }
    }

    LaunchedEffect(showHeartPop) {
        if (showHeartPop) {
            delay(750)
            showHeartPop = false
        }
    }

    // Simulated progress scrubber
    var progress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(120)
            progress += 0.015f
            if (progress > maxWatchPct) maxWatchPct = progress
            if (progress >= 1f) progress = 0f
        }
    }

    // UserInterest dwell tracking
    DisposableEffect(clip.id) {
        val enterTime = System.currentTimeMillis()
        onDispose {
            val elapsedSec = ((System.currentTimeMillis() - enterTime) / 1000).toInt().coerceAtLeast(dwellSeconds)
            val watchPct = maxWatchPct.toDouble()
            val tags = clip.effectiveTags()

            if (elapsedSec < 2 || watchPct < 0.15) {
                UserInterestTracker.recordEvent(
                    itemId = clip.id,
                    tags = tags,
                    watchPercentage = watchPct,
                    dwellTimeSec = elapsedSec,
                    action = "FAST_SKIP",
                    userId = currentUser?.uid ?: "usr_current"
                )
            } else if (watchPct >= 0.90) {
                UserInterestTracker.recordEvent(
                    itemId = clip.id,
                    tags = tags,
                    watchPercentage = watchPct,
                    dwellTimeSec = elapsedSec,
                    action = "FULL_WATCH",
                    userId = currentUser?.uid ?: "usr_current"
                )
            } else if (elapsedSec >= 5) {
                UserInterestTracker.recordEvent(
                    itemId = clip.id,
                    tags = tags,
                    watchPercentage = watchPct,
                    dwellTimeSec = elapsedSec,
                    action = "DWELL",
                    userId = currentUser?.uid ?: "usr_current"
                )
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, GoldBorder.copy(alpha = 0.45f), RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14120F)),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // ==========================================
            // 1. VIDEO CINEMA CANVAS (4:5 Ratio)
            // ==========================================
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(0.85f)
                    .background(Color.Black)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { isPlaying = !isPlaying },
                            onDoubleTap = {
                                if (!isLiked) {
                                    showHeartPop = true
                                    onToggleLike()
                                }
                            }
                        )
                    }
            ) {
                val mediaUrl = clip.videoUrl.ifBlank { clip.mediaUrls.firstOrNull() }
                if (!mediaUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = mediaUrl,
                        contentDescription = "Cinema Clip Visual",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    listOf(Color(0xFF2C2216), Color(0xFF120F0C))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = clip.text,
                            color = Color.White,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }

                // Cinematic vignette overlays
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.0f to Color.Black.copy(alpha = 0.55f),
                                0.22f to Color.Transparent,
                                0.65f to Color.Transparent,
                                1.0f to Color.Black.copy(alpha = 0.85f)
                            )
                        )
                )

                // Top Header Pill Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Category & Views
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .border(0.8.dp, Gold.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "✨ CINEMA",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldLight
                            )
                        }

                        Spacer(modifier = Modifier.width(6.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.45f))
                                .padding(horizontal = 7.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "👁️ ${if (clip.viewsCount > 0) clip.viewsCount else 2840} views",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.White.copy(alpha = 0.9f)
                            )
                        }
                    }

                    // Fullscreen / Expand Trigger
                    IconButton(
                        onClick = onOpenFullViewer,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.55f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.Fullscreen,
                            contentDescription = "Fullscreen",
                            tint = Color.White,
                            modifier = Modifier.size(19.dp)
                        )
                    }
                }

                // Play / Pause Indicator overlay
                if (!isPlaying) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .border(1.5.dp, Gold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = "Play",
                            tint = Gold,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Double tap Heart Burst
                if (showHeartPop) {
                    Box(
                        modifier = Modifier.align(Alignment.Center),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color(0xFFE53935),
                            modifier = Modifier.size(76.dp)
                        )
                    }
                }

                // Bottom Soundwave Pill inside video
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(12.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(0.8.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(horizontal = 9.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.MusicNote,
                        contentDescription = null,
                        tint = Gold,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = clip.audioTrackTitle.ifBlank { "Traditional Habesha Melody" },
                        color = Color.White,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.widthIn(max = 200.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    EqualizerBarsAnimation(isPlaying = isPlaying)
                }

                // Progress line
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = Gold,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )
            }

            // ==========================================
            // 2. HORIZONTAL GLASS ACTION DOCK (Distinctive!)
            // ==========================================
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFF1B1713)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Like Pill
                    Surface(
                        onClick = onToggleLike,
                        shape = RoundedCornerShape(16.dp),
                        color = if (isLiked) Color(0xFF331414) else Color.White.copy(alpha = 0.07f),
                        border = BorderStroke(
                            1.dp,
                            if (isLiked) Color(0xFFE53935).copy(alpha = 0.6f) else Color.White.copy(alpha = 0.12f)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (isLiked) Color(0xFFE53935) else Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "$likesCount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLiked) Color(0xFFFF8A80) else Color.White
                            )
                        }
                    }

                    // Comments Pill
                    Surface(
                        onClick = onCommentClick,
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.07f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Comments",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = "$commentCount",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Chapa Tip Creator Pill (Ethiopian Innovation)
                    Surface(
                        onClick = onTipClick,
                        shape = RoundedCornerShape(16.dp),
                        color = GoldSurface,
                        border = BorderStroke(1.dp, Gold)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("💰", fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Tip ETB",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldDeep
                            )
                        }
                    }

                    // Share Pill
                    Surface(
                        onClick = onShareClick,
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White.copy(alpha = 0.07f),
                        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.12f))
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share",
                                tint = Color.White,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Share",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }

                    // Bookmark Button
                    IconButton(
                        onClick = onToggleSave,
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Save",
                            tint = if (isSaved) Gold else Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // ==========================================
            // 3. CREATOR & NARRATIVE FOOTER
            // ==========================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        UserAvatar(
                            photoUrl = clip.authorPhoto,
                            name = clip.authorName,
                            size = 36
                        )
                        Spacer(modifier = Modifier.width(9.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = clip.authorName,
                                    color = Color.White,
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                if (clip.isAuthorVerified) {
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Verified",
                                        tint = Gold,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Meskot Creator",
                                fontSize = 10.5.sp,
                                color = Gold.copy(alpha = 0.75f)
                            )
                        }
                    }

                    // Follow Button
                    if (currentUser?.uid != clip.uid) {
                        Surface(
                            onClick = onToggleFollow,
                            shape = RoundedCornerShape(14.dp),
                            color = if (isFollowing) Color.White.copy(alpha = 0.12f) else GoldDeep,
                            border = BorderStroke(
                                1.dp,
                                if (isFollowing) Color.White.copy(alpha = 0.3f) else Gold
                            )
                        ) {
                            Text(
                                text = if (isFollowing) "Following" else "+ Follow",
                                color = Color.White,
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
                            )
                        }
                    }
                }

                // Story Narrative Caption
                if (clip.text.isNotBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = clip.text,
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

/**
 * Mosaic Card for 2-column Discovery Grid
 */
@Composable
fun MeskotMosaicCard(
    clip: Post,
    likesCount: Int,
    isLiked: Boolean,
    onClick: () -> Unit,
    onCommentClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.68f)
            .clip(RoundedCornerShape(16.dp))
            .border(1.dp, GoldBorder.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14120E))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            val mediaUrl = clip.videoUrl.ifBlank { clip.mediaUrls.firstOrNull() }
            if (!mediaUrl.isNullOrBlank()) {
                AsyncImage(
                    model = mediaUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF2C2216), Color(0xFF120E0A))
                            )
                        )
                )
            }

            // Vignette gradient
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color.Black.copy(alpha = 0.4f),
                            0.5f to Color.Transparent,
                            1.0f to Color.Black.copy(alpha = 0.85f)
                        )
                    )
            )

            // Top: Views badge
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(Color.Black.copy(alpha = 0.6f))
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            ) {
                Text(
                    text = "👁️ ${if (clip.viewsCount > 0) clip.viewsCount else "1.8K"}",
                    fontSize = 9.5.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Medium
                )
            }

            // Center: Play Icon Glow
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.55f))
                    .border(1.2.dp, Gold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play",
                    tint = Gold,
                    modifier = Modifier.size(24.dp)
                )
            }

            // Bottom metadata
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                Text(
                    text = clip.authorName,
                    color = Color.White,
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (clip.text.isNotBlank()) {
                    Text(
                        text = clip.text,
                        color = Color.White.copy(alpha = 0.75f),
                        fontSize = 10.5.sp,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(3.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "❤️ $likesCount",
                        fontSize = 10.sp,
                        color = if (isLiked) Color(0xFFFF8A80) else Gold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "💬 ${clip.commentCount}",
                        fontSize = 10.sp,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
            }
        }
    }
}
