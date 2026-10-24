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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.MeskotStrings
import com.example.data.NotificationItem
import com.example.data.Post
import com.example.data.User
import com.example.ui.MeskotViewModel
import com.example.ui.ScreenTab
import com.example.ui.components.PostCard
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CardBg
import com.example.ui.theme.CrossRed
import com.example.ui.theme.Gold
import com.example.ui.theme.GoldDeep
import com.example.ui.theme.Ink
import com.example.ui.theme.LineBorder
import com.example.ui.theme.MutedText
import com.example.ui.theme.Paper2

@Composable
fun NotificationsScreen(
    viewModel: MeskotViewModel,
    notifications: List<NotificationItem>,
    currentLanguage: AppLanguage
) {
    val nowMs by viewModel.tickerTimeMs.collectAsStateWithLifecycle()
    val unreadCount = notifications.count { !it.isRead }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("notifications_screen")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = MeskotStrings.get("notifications", currentLanguage),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Ink
                )
                if (unreadCount > 0) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(CrossRed)
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = unreadCount.toString(),
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            if (unreadCount > 0) {
                TextButton(onClick = { viewModel.markAllNotifsRead() }) {
                    Text(
                        text = MeskotStrings.get("markAllRead", currentLanguage),
                        fontSize = 13.sp,
                        color = GoldDeep,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            if (notifications.isEmpty()) {
                item {
                    EmptyNotice(text = MeskotStrings.get("noNotifs", currentLanguage))
                }
            } else {
                items(notifications) { notif ->
                    NotificationRow(
                        notif = notif,
                        currentLanguage = currentLanguage,
                        nowMs = nowMs,
                        onClick = {
                            viewModel.markNotificationRead(notif.id)
                            when (notif.type) {
                                "friend_req", "friend_request" -> viewModel.navigateTo(ScreenTab.FRIENDS)
                                "message" -> {
                                    if (!notif.fromUid.isNullOrBlank() && notif.fromUid != "system_meskot") {
                                        viewModel.openChatByUid(notif.fromUid)
                                    } else {
                                        viewModel.navigateTo(ScreenTab.MESSAGES)
                                    }
                                }
                                "live", "live_stream" -> {
                                    val streamId = notif.targetId
                                    if (!streamId.isNullOrBlank()) {
                                        viewModel.joinLiveStreamById(streamId)
                                    } else {
                                        viewModel.openLiveStream()
                                    }
                                }
                                else -> viewModel.navigateTo(ScreenTab.FEED)
                            }
                        }
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
fun NotificationRow(
    notif: NotificationItem,
    currentLanguage: AppLanguage,
    nowMs: Long = System.currentTimeMillis(),
    onClick: () -> Unit
) {
    val isLiveNotif = notif.type == "live" || notif.type == "live_stream"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() }
            .testTag("notification_row_${notif.id}"),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isLiveNotif && !notif.isRead -> Color(0xFFFFF0F3) // Subtle live rose tint
                notif.isRead -> CardBg
                else -> Color(0xFFEBF5FF)
            }
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            when {
                isLiveNotif && !notif.isRead -> Color(0xFFFF8599)
                notif.isRead -> LineBorder
                else -> Color(0xFFC7E0FF)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Sender Avatar with notification type badge
            Box {
                UserAvatar(
                    photoUrl = notif.fromPhoto,
                    name = notif.fromName,
                    size = 46,
                    modifier = if (isLiveNotif) Modifier.border(1.5.dp, Color(0xFFFF1744), CircleShape) else Modifier
                )

                val badgeEmoji = when (notif.type) {
                    "live", "live_stream" -> "🔴"
                    "like" -> "❤️"
                    "comment" -> "💬"
                    "friend_req", "friend_request", "friend_accept" -> "👥"
                    "tip" -> "💰"
                    "call" -> "📞"
                    "message" -> "💬"
                    else -> "🔔"
                }

                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(if (isLiveNotif) Color(0xFFFF1744) else Color.White)
                        .border(1.dp, if (isLiveNotif) Color.White else LineBorder, CircleShape)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = if (isLiveNotif) "🔴" else badgeEmoji, fontSize = 10.sp)
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = notif.text,
                    fontSize = 14.sp,
                    color = if (notif.isRead) MutedText else Ink,
                    lineHeight = 18.sp,
                    fontWeight = if (notif.isRead) FontWeight.Normal else FontWeight.Bold
                )
                Text(
                    text = MeskotStrings.formatNotificationTime(notif.createdAt, currentLanguage, nowMs),
                    fontSize = 11.sp,
                    color = when {
                        isLiveNotif -> Color(0xFFFF1744)
                        notif.isRead -> MutedText
                        else -> Color(0xFF1877F2)
                    },
                    fontWeight = if (notif.isRead) FontWeight.Normal else FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp)
                )
            }

            if (isLiveNotif) {
                // TikTok-style "Watch" action button
                Button(
                    onClick = onClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFF1744),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier
                        .padding(start = 6.dp)
                        .testTag("notification_watch_live_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = MeskotStrings.get("watchLive", currentLanguage),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (!notif.isRead) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1877F2))
                )
            }
        }
    }
}

@Composable
fun SavedScreen(
    viewModel: MeskotViewModel,
    currentUser: User?,
    savedPosts: List<Post>,
    currentLanguage: AppLanguage
) {
    val savedPostIds by viewModel.savedPostIds.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("saved_screen")
    ) {
        Text(
            text = "🔖 " + MeskotStrings.get("savedPosts", currentLanguage),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = Ink,
            modifier = Modifier.padding(16.dp)
        )

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (savedPosts.isEmpty()) {
                item {
                    EmptyNotice(text = MeskotStrings.get("noSaved", currentLanguage))
                }
            } else {
                items(savedPosts) { post ->
                    val comments = viewModel.getComments(post.id)
                    PostCard(
                        post = post,
                        isSaved = savedPostIds.contains(post.id),
                        currentUserId = currentUser?.uid,
                        comments = comments,
                        currentLanguage = currentLanguage,
                        onAuthorClick = { viewModel.openProfileByUid(it) },
                        onHashtagClick = { tag -> viewModel.filterByHashtag(tag) },
                        onMentionClick = { mention -> viewModel.openProfileByMention(mention) },
                        onToggleReaction = { pid, type -> viewModel.toggleReaction(pid, type) },
                        onShare = { viewModel.sharePost(it) },
                        onToggleSave = { viewModel.toggleSavePost(it) },
                        onTipClick = { viewModel.openTipModal(it) },
                        onOpenMenu = { viewModel.openPostMenu(it) },
                        onAddComment = { pid, text, parentId -> viewModel.addComment(pid, text, parentId) },
                        onToggleCommentLike = { pid, cid -> viewModel.toggleCommentLike(pid, cid) },
                        onDeleteComment = { pid, cid -> viewModel.deleteComment(pid, cid) }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
