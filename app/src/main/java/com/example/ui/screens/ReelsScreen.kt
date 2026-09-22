package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
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
import com.example.data.Post
import com.example.data.User
import com.example.ui.MeskotViewModel
import com.example.ui.components.EqualizerBarsAnimation
import com.example.ui.components.PresetReelsCollection
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Facebook-style Reels Screen:
 * - Dedicated top header with "Reels" branding and "+ Create Reel" action
 * - Filter pills ("🔥 For You", "🇪🇹 Habesha Heritage", "👤 My Reels")
 * - Immersive vertical feed of Facebook-style 9:16 short video reels
 * - Integrated right action bar (Like, Comment, Share, Tip Birr, Save)
 * - Music audio marquee with soundwave equalizer and rotating vinyl disc
 */
@Composable
fun ReelsScreen(
    viewModel: MeskotViewModel,
    posts: List<Post>,
    currentUser: User?,
    currentLanguage: AppLanguage
) {
    val context = LocalContext.current
    var selectedFilterIndex by remember { mutableIntStateOf(0) }
    val followingUids by viewModel.followingUids.collectAsState()
    val savedPostIds by viewModel.savedPostIds.collectAsState()
    val allComments by viewModel.allComments.collectAsState()

    // Aggregate user-created reels + preset cultural reels
    val communityReels = remember(posts) {
        posts.filter { it.postType == "REEL" || it.videoUrl.isNotBlank() }
    }

    // Convert cultural presets into Post model for unified feed rendering
    val presetReelPosts = remember(posts) {
        PresetReelsCollection.mapIndexed { idx, preset ->
            val presetId = "preset_${preset.id}"
            val existing = posts.find { it.id == presetId }
            existing ?: Post(
                id = presetId,
                uid = "meskot_culture_$idx",
                authorName = when (preset.category) {
                    "Culture" -> "Addis Coffee House"
                    "Dance" -> "Gondar Eskista Troupe"
                    "Nature" -> "Simien Highlands Park"
                    else -> "Lalibela Heritage"
                },
                authorPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
                isAuthorVerified = true,
                text = preset.description,
                postType = "REEL",
                videoUrl = preset.mediaUrl,
                audioTrackTitle = preset.audioTitle,
                viewsCount = 14200 + (idx * 3150),
                createdAt = System.currentTimeMillis() - (idx * 3600000L * 4)
            )
        }
    }

    val displayReels = remember(selectedFilterIndex, communityReels, currentUser?.uid) {
        when (selectedFilterIndex) {
            0 -> {
                // "For You" - Combined feed of community reels + curated presets
                val combined = mutableListOf<Post>()
                combined.addAll(communityReels)
                combined.addAll(presetReelPosts)
                if (combined.isEmpty()) presetReelPosts else combined
            }
            1 -> {
                // "Habesha Heritage" - presets and culture-tagged reels
                presetReelPosts
            }
            2 -> {
                // "My Reels" - Only reels created by current user
                communityReels.filter { it.uid == currentUser?.uid }
            }
            else -> communityReels + presetReelPosts
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F0E0C))
            .testTag("reels_screen")
    ) {
        // Facebook-style Reels Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF181512),
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(GoldLight, LuxuryGoldAccent, GoldDeep)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = "Reels",
                                tint = Ink,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Reels",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Gold)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "LIVE FEED",
                                        fontSize = 8.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Ink
                                    )
                                }
                            }
                            Text(
                                text = "Habesha creators & viral videos",
                                fontSize = 11.5.sp,
                                color = Gold.copy(alpha = 0.8f)
                            )
                        }
                    }

                    // "+ Create Reel" Quick Action Button
                    Surface(
                        onClick = { viewModel.openCreateReel() },
                        shape = RoundedCornerShape(20.dp),
                        color = GoldSurface,
                        border = androidx.compose.foundation.BorderStroke(1.2.dp, GoldBorder),
                        modifier = Modifier.testTag("create_reel_header_btn")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create Reel",
                                tint = GoldDeep,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Create",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldDeep
                            )
                        }
                    }
                }

                // Facebook Reels category filter tabs
                val filterLabels = listOf("🔥 For You", "🇪🇹 Heritage", "👤 My Reels")
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filterLabels.forEachIndexed { index, label ->
                        val isSelected = selectedFilterIndex == index
                        Surface(
                            onClick = { selectedFilterIndex = index },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isSelected) GoldDeep else Color(0xFF29241E),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isSelected) Gold else Color.White.copy(alpha = 0.15f)
                            )
                        ) {
                            Text(
                                text = label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else Color.White.copy(alpha = 0.85f),
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }

        // Empty state for My Reels tab
        if (selectedFilterIndex == 2 && displayReels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF26201A))
                            .border(1.5.dp, Gold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoCameraBack,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Share Your First Reel",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Create captivating short-form videos with traditional music, filters, and reach thousands of Habesha viewers.",
                        fontSize = 13.sp,
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.openCreateReel() },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldDeep),
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier.padding(horizontal = 16.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Create Reel Now", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        } else {
            // Vertical Facebook-style Reels Feed
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(displayReels, key = { index, reel -> "fb_reel_${reel.id}_$index" }) { _, reel ->
                    val liveReel = posts.find { it.id == reel.id } ?: reel
                    val isSaved = savedPostIds.contains(liveReel.id)
                    val commentCount = allComments[liveReel.id]?.size ?: liveReel.commentCount
                    FacebookReelItemCard(
                        reel = liveReel,
                        currentUser = currentUser,
                        isFollowing = followingUids.contains(liveReel.uid),
                        isSaved = isSaved,
                        commentCount = commentCount,
                        onToggleFollow = { viewModel.toggleFollow(liveReel.uid) },
                        onToggleLike = {
                            viewModel.toggleReaction(liveReel.id, "like")
                        },
                        onToggleSave = {
                            viewModel.toggleSavePost(liveReel.id)
                        },
                        onCommentClick = {
                            viewModel.viewReel(liveReel)
                        },
                        onShareClick = {
                            viewModel.sharePost(liveReel.id)
                            Toast.makeText(context, "🔗 Reel link copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        onTipClick = {
                            viewModel.openTipModal(liveReel)
                        },
                        onOpenFullViewer = {
                            viewModel.viewReel(liveReel)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

/**
 * Facebook-style Reel Item Card:
 * - 9:16 vertical card with Ken Burns motion thumbnail
 * - Tap to toggle Play/Pause
 * - Right floating action column (Heart, Comment, Share, Tip, Save, Spinning Disc)
 * - Bottom overlay with creator details, follow button, caption, and music track
 */
@Composable
fun FacebookReelItemCard(
    reel: Post,
    currentUser: User?,
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
    var isLiked by remember(reel.reactions, currentUser?.uid) {
        mutableStateOf(currentUser != null && reel.reactions.containsKey(currentUser.uid))
    }
    var likesCount by remember(reel.reactions) {
        mutableIntStateOf(if (reel.reactions.isNotEmpty()) reel.reactions.size else 128)
    }
    var showHeartPop by remember { mutableStateOf(false) }

    LaunchedEffect(showHeartPop) {
        if (showHeartPop) {
            kotlinx.coroutines.delay(750)
            showHeartPop = false
        }
    }

    // Vinyl record rotation
    val infiniteTransition = rememberInfiniteTransition(label = "disc_anim")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "vinyl_rotate"
    )

    // Progress simulation
    var progress by remember { mutableFloatStateOf(0f) }
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            kotlinx.coroutines.delay(120)
            progress += 0.015f
            if (progress >= 1f) progress = 0f
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(560.dp)
            .padding(horizontal = 8.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(Color.Black)
            .border(1.dp, Color(0xFF33291F), RoundedCornerShape(18.dp))
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { isPlaying = !isPlaying },
                    onDoubleTap = {
                        if (!isLiked) {
                            isLiked = true
                            likesCount++
                            onToggleLike()
                        }
                        showHeartPop = true
                    }
                )
            }
    ) {
        // Video / Visual Background
        val mediaUrl = reel.videoUrl.ifBlank { reel.mediaUrls.firstOrNull() }
        if (!mediaUrl.isNullOrBlank()) {
            AsyncImage(
                model = mediaUrl,
                contentDescription = "Reel Backdrop",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFF2E2419), Color(0xFF15120E))
                        )
                    )
            )
        }

        // Dark Gradients (Top & Bottom readability overlays)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.55f),
                        0.25f to Color.Transparent,
                        0.60f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.90f)
                    )
                )
        )

        // Top Header inside Reel
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.6f))
                        .border(0.8.dp, Gold.copy(alpha = 0.8f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PlayCircle,
                            contentDescription = null,
                            tint = Gold,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "REEL",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.45f))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "👁️ ${if (reel.viewsCount > 0) reel.viewsCount else 3400} views",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.9f)
                    )
                }
            }

            // Expand to full-screen viewer button
            IconButton(
                onClick = onOpenFullViewer,
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.5f))
            ) {
                Icon(
                    imageVector = Icons.Default.Fullscreen,
                    contentDescription = "Expand",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        // Center Pause/Play Indicator when paused
        if (!isPlaying) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.6f))
                    .border(2.dp, Gold, CircleShape),
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

        // Double tap Heart Pop
        androidx.compose.animation.AnimatedVisibility(
            visible = showHeartPop,
            enter = scaleIn(tween(250)) + fadeIn(),
            exit = scaleOut(tween(250)) + fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Icon(
                imageVector = Icons.Default.Favorite,
                contentDescription = null,
                tint = Color(0xFFE53935),
                modifier = Modifier.size(72.dp)
            )
        }

        // Right-side Facebook Floating Action Bar
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Like (Heart) Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = {
                        isLiked = !isLiked
                        likesCount += if (isLiked) 1 else -1
                        if (isLiked) showHeartPop = true
                        onToggleLike()
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like",
                        tint = if (isLiked) Color(0xFFE53935) else Color.White,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Text(
                    text = "$likesCount",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Comment Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onCommentClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ChatBubbleOutline,
                        contentDescription = "Comments",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "$commentCount",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Share Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onShareClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.45f))
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = "Share",
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = "Share",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // Tip / Cultural Gift Button
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                IconButton(
                    onClick = onTipClick,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(GoldDeep)
                        .border(1.2.dp, Gold, CircleShape)
                ) {
                    Text(text = "💰", fontSize = 20.sp)
                }
                Text(
                    text = "Tip",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Gold
                )
            }

            // Save / Bookmark Button
            IconButton(
                onClick = onToggleSave,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                    contentDescription = "Save",
                    tint = if (isSaved) Gold else Color.White,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Spinning Vinyl Record
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .rotate(if (isPlaying) rotationAngle else 0f)
                    .clip(CircleShape)
                    .background(Color(0xFF1B1B1B))
                    .border(1.8.dp, Gold, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(Gold)
                )
            }
        }

        // Bottom Left Content Overlay (Author, Follow, Caption, Audio Track)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(0.78f)
                .padding(start = 14.dp, bottom = 18.dp)
        ) {
            // Author info with Follow button
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    photoUrl = reel.authorPhoto,
                    name = reel.authorName,
                    size = 36
                )
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = reel.authorName,
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (reel.isAuthorVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Gold,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                // Follow / Following Pill
                if (currentUser?.uid != reel.uid) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        onClick = onToggleFollow,
                        shape = RoundedCornerShape(12.dp),
                        color = if (isFollowing) Color.White.copy(alpha = 0.2f) else GoldDeep,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isFollowing) Color.White.copy(alpha = 0.4f) else Gold
                        )
                    ) {
                        Text(
                            text = if (isFollowing) "Following" else "Follow",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            // Caption
            if (reel.text.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = reel.text,
                    color = Color.White,
                    fontSize = 12.5.sp,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 17.sp
                )
            }

            // Audio track pill with equalizer
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .border(0.8.dp, Gold.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                    .padding(horizontal = 8.dp, vertical = 3.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = Gold,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = reel.audioTrackTitle.ifBlank { "Original Audio" },
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 180.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                EqualizerBarsAnimation(isPlaying = isPlaying)
            }
        }

        // Bottom Progress Indicator Scrubber
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
}
