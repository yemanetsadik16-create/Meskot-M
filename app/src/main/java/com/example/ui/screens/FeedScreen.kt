package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.VideoCameraBack
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.example.data.AppLanguage
import com.example.data.MeskotStrings
import com.example.data.Post
import com.example.data.StoryItem
import com.example.data.User
import com.example.ui.MeskotViewModel
import com.example.ui.components.MeskotReelsRail
import com.example.ui.components.PostCard
import com.example.ui.components.StoryAvatarRingItem
import com.example.ui.components.UserAvatar
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: MeskotViewModel,
    currentUser: User?,
    feedPosts: List<Post>,
    friends: List<User>,
    currentLanguage: AppLanguage
) {
    val stories by viewModel.stories.collectAsState()
    val tickerTimeMs by viewModel.tickerTimeMs.collectAsState()
    val isRefreshing by viewModel.isFeedRefreshing.collectAsState()

    // Filter active (non-expired) stories
    val activeStories = remember(stories, tickerTimeMs) {
        stories.filter { !it.isExpired(tickerTimeMs) }
    }

    val myStories = remember(activeStories, currentUser?.uid) {
        if (currentUser == null) emptyList()
        else activeStories.filter { it.uid == currentUser.uid }
    }

    val otherStoriesByUser = remember(activeStories, currentUser?.uid) {
        activeStories.filter { it.uid != currentUser?.uid }
            .groupBy { it.uid }
    }

    val pullToRefreshState = rememberPullToRefreshState()

    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = { viewModel.refreshFeed() },
        modifier = Modifier
            .fillMaxSize()
            .testTag("feed_pull_to_refresh"),
        state = pullToRefreshState,
        indicator = {
            PullToRefreshDefaults.Indicator(
                state = pullToRefreshState,
                isRefreshing = isRefreshing,
                modifier = Modifier.align(Alignment.TopCenter),
                containerColor = Color.White,
                color = GoldDeep
            )
        }
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .testTag("feed_screen")
        ) {
            // Top Stories Horizontal Rail
            item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 6.dp)
            ) {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp),
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Current User's "Your Story" (Camera / Add Story / View My Story)
                    if (currentUser != null) {
                        item {
                            StoryAvatarRingItem(
                                user = currentUser,
                                hasStory = myStories.isNotEmpty(),
                                isCurrentUser = true,
                                isStoryViewed = false,
                                expirationText = myStories.firstOrNull()?.formattedRemaining(tickerTimeMs),
                                onClick = {
                                    if (myStories.isNotEmpty()) {
                                        viewModel.viewStory(myStories.first())
                                    } else {
                                        viewModel.openCreateStory()
                                    }
                                }
                            )
                        }
                    }

                    // 2. Friends & Community Members with active stories
                    otherStoriesByUser.forEach { (authorUid, userStories) ->
                        val latestStory = userStories.first()
                        val friendObj = friends.find { it.uid == authorUid } ?: User(
                            uid = authorUid,
                            displayName = latestStory.authorName,
                            photoUrl = latestStory.authorPhoto
                        )
                        val isAllViewed = userStories.all { it.viewers.contains(currentUser?.uid) }

                        item(key = "story_user_$authorUid") {
                            StoryAvatarRingItem(
                                user = friendObj,
                                hasStory = true,
                                isCurrentUser = false,
                                isStoryViewed = isAllViewed,
                                expirationText = latestStory.formattedRemaining(tickerTimeMs),
                                onClick = { viewModel.viewStory(latestStory) }
                            )
                        }
                    }

                    // 3. Other online friends without active stories
                    val friendsWithoutStories = friends.filterNot { otherStoriesByUser.containsKey(it.uid) }
                    items(friendsWithoutStories, key = { "friend_${it.uid}" }) { friend ->
                        StoryAvatarRingItem(
                            user = friend,
                            hasStory = false,
                            isCurrentUser = false,
                            onClick = { viewModel.openChat(friend) }
                        )
                    }
                }

                HorizontalDivider(
                    color = LineBorder,
                    modifier = Modifier.padding(top = 10.dp, start = 14.dp, end = 14.dp)
                )
            }
        }

        // Composer & Quick Story Trigger Cards
        if (currentUser != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                        .testTag("composer_trigger_card"),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = androidx.compose.foundation.BorderStroke(1.2.dp, GoldBorder.copy(alpha = 0.7f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Main post text prompt
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openComposer() },
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            UserAvatar(
                                photoUrl = currentUser.photoUrl,
                                name = currentUser.displayName,
                                size = 40
                            )

                            Spacer(modifier = Modifier.width(10.dp))

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(GoldSurface)
                                    .border(1.dp, GoldBorder.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                                    .padding(horizontal = 16.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = MeskotStrings.get("composerPh", currentLanguage),
                                    color = GoldDeep,
                                    fontSize = 14.sp
                                )
                            }
                        }

                        HorizontalDivider(color = LineBorder.copy(alpha = 0.6f), thickness = 0.8.dp)

                        // Quick actions row: Live, Story, Reel & Photo
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Quick "Live" Action
                            Surface(
                                onClick = { viewModel.openLiveStream() },
                                shape = RoundedCornerShape(12.dp),
                                color = Color(0xFFE53935),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("feed_quick_live_btn")
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Live",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Quick "Create Story" Action
                            Surface(
                                onClick = { viewModel.openCreateStory() },
                                shape = RoundedCornerShape(12.dp),
                                color = GoldSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoCamera,
                                        contentDescription = "Create Story",
                                        tint = GoldDeep,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Story",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldDeep
                                    )
                                }
                            }

                            // Quick "Create Reel" Action
                            Surface(
                                onClick = { viewModel.openCreateReel() },
                                shape = RoundedCornerShape(12.dp),
                                color = GoldDeep,
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VideoCameraBack,
                                        contentDescription = "Create Reel",
                                        tint = Color.White,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Reel",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }

                            // Share Post / Photo Action
                            Surface(
                                onClick = { viewModel.openComposer() },
                                shape = RoundedCornerShape(12.dp),
                                color = Paper,
                                border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = "Add Photo",
                                        tint = Ink,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Spacer(modifier = Modifier.width(3.dp))
                                    Text(
                                        text = "Photo",
                                        fontSize = 11.5.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Ink
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Trending Meskot Reels Showcase Rail
        item {
            MeskotReelsRail(
                reels = feedPosts.filter { it.postType == "REEL" || it.videoUrl.isNotBlank() },
                onReelClick = { viewModel.viewReel(it) },
                onCreateReelClick = { viewModel.openCreateReel() }
            )
        }

        // Posts List
        if (feedPosts.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 60.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = "🪟", fontSize = 48.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = MeskotStrings.get("emptyFeed", currentLanguage),
                        color = MutedText,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 32.dp)
                    )
                }
            }
        } else {
            items(feedPosts, key = { it.id }) { post ->
                val comments = viewModel.getComments(post.id)
                val userTier = viewModel.getUserMembershipTier(post.uid)
                PostCard(
                    post = post,
                    currentUserId = currentUser?.uid,
                    comments = comments,
                    currentLanguage = currentLanguage,
                    currentUserTier = userTier,
                    onBoostClick = { viewModel.openBoostModal(it) },
                    onUnlockVip = { viewModel.openSubscriptionModal(it) },
                    onAuthorClick = { viewModel.openProfileByUid(it) },
                    onToggleReaction = { pid, type -> viewModel.toggleReaction(pid, type) },
                    onShare = { viewModel.sharePost(it) },
                    onToggleSave = { viewModel.toggleSavePost(it) },
                    onTipClick = { viewModel.openTipModal(it) },
                    onOpenMenu = { viewModel.openPostMenu(it) },
                    onAddComment = { pid, text, parentId -> viewModel.addComment(pid, text, parentId) },
                    onToggleCommentLike = { pid, cid -> viewModel.toggleCommentLike(pid, cid) },
                    onDeleteComment = { pid, cid -> viewModel.deleteComment(pid, cid) },
                    onReelClick = { viewModel.viewReel(it) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(80.dp))
        }
    }
}
}

@Composable
fun StoryAvatarItem(
    user: User,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(60.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.size(56.dp)
        ) {
            UserAvatar(
                photoUrl = user.photoUrl,
                name = user.displayName,
                size = 56
            )
            // Green online badge
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(ActiveGreen)
                    .border(2.dp, Color.White, CircleShape)
                    .align(Alignment.BottomEnd)
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = user.displayName.split(" ").firstOrNull() ?: user.displayName,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
