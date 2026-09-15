package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.AppLanguage
import com.example.data.MeskotStrings
import com.example.data.User
import com.example.ui.ScreenTab
import com.example.ui.theme.CrossRed
import com.example.ui.theme.Gold
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldDeep
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldSurface
import com.example.ui.theme.Ink
import com.example.ui.theme.LineBorder
import com.example.ui.theme.MeskotLogoBrush
import com.example.ui.theme.MeskotLogoHorizontalBrush
import com.example.ui.theme.MutedText
import com.example.ui.theme.Paper
import com.example.ui.theme.Paper2

@Composable
fun TopNavBar(
    currentUser: User?,
    currentLanguage: AppLanguage,
    onToggleLanguage: () -> Unit,
    onOpenComposer: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenMenu: () -> Unit,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Paper.copy(alpha = 0.98f),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Meskot brand emblem top decorative micro-line
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(MeskotLogoHorizontalBrush)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left: Brand Logo & Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.weight(1f, fill = false)
                ) {
                    // Modern, Attractive Meskot Brand Logo & Subtitle
                    MeskotLogoHeader(
                        size = 32.dp,
                        onClick = { onProfileClick() }
                    )
                }

                // Right Action Controls: [+] [🔍] [☰]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // [+] Create Post button in radiant Meskot Gold
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(MeskotLogoBrush)
                            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(10.dp))
                            .clickable { onOpenComposer() }
                            .testTag("create_post_nav_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Create Post",
                            tint = Color.White,
                            modifier = Modifier.size(21.dp)
                        )
                    }

                    // [🔍] Search button with brand gold tint
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GoldSurface)
                            .border(1.dp, GoldBorder, RoundedCornerShape(10.dp))
                            .clickable { onOpenSearch() }
                            .testTag("search_nav_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = GoldDeep,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    // [☰] Menu button with brand gold tint
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(GoldSurface)
                            .border(1.dp, GoldBorder, RoundedCornerShape(10.dp))
                            .clickable { onOpenMenu() }
                            .testTag("menu_nav_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = GoldDeep,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IconNavBar(
    currentTab: ScreenTab,
    unreadReqCount: Int,
    unreadMsgCount: Int,
    unreadNotifCount: Int,
    isAdmin: Boolean,
    onTabSelected: (ScreenTab) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Paper.copy(alpha = 0.98f),
        shadowElevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavIconButton(
                emoji = "🏠",
                label = "Feed",
                isSelected = currentTab == ScreenTab.FEED,
                onClick = { onTabSelected(ScreenTab.FEED) },
                testTag = "tab_feed"
            )

            NavIconButton(
                emoji = "👥",
                label = "Friends",
                badgeCount = unreadReqCount,
                isSelected = currentTab == ScreenTab.FRIENDS,
                onClick = { onTabSelected(ScreenTab.FRIENDS) },
                testTag = "tab_friends"
            )

            NavIconButton(
                emoji = "💬",
                label = "Messages",
                badgeCount = unreadMsgCount,
                isSelected = currentTab == ScreenTab.MESSAGES || currentTab == ScreenTab.CHAT,
                onClick = { onTabSelected(ScreenTab.MESSAGES) },
                testTag = "tab_messages"
            )

            NavIconButton(
                emoji = "👪",
                label = "Groups",
                isSelected = currentTab == ScreenTab.GROUPS || currentTab == ScreenTab.GROUP_DETAIL,
                onClick = { onTabSelected(ScreenTab.GROUPS) },
                testTag = "tab_groups"
            )

            NavIconButton(
                emoji = "🖼️",
                label = "Photos",
                isSelected = currentTab == ScreenTab.PHOTOS || currentTab == ScreenTab.ALBUM_DETAIL,
                onClick = { onTabSelected(ScreenTab.PHOTOS) },
                testTag = "tab_photos"
            )

            NavIconButton(
                emoji = "🔔",
                label = "Notifications",
                badgeCount = unreadNotifCount,
                isSelected = currentTab == ScreenTab.NOTIFICATIONS,
                onClick = { onTabSelected(ScreenTab.NOTIFICATIONS) },
                testTag = "tab_notifications"
            )

            NavIconButton(
                emoji = "📊",
                label = "Dashboard",
                isSelected = currentTab == ScreenTab.DASHBOARD,
                onClick = { onTabSelected(ScreenTab.DASHBOARD) },
                testTag = "tab_dashboard"
            )

            if (isAdmin) {
                NavIconButton(
                    emoji = "🛡️",
                    label = "Admin",
                    isSelected = currentTab == ScreenTab.ADMIN,
                    onClick = { onTabSelected(ScreenTab.ADMIN) },
                    testTag = "tab_admin"
                )
            }
        }
    }
}

@Composable
private fun NavIconButton(
    emoji: String,
    label: String,
    isSelected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) GoldSurface else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp)
            .testTag(testTag)
    ) {
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge(
                        containerColor = CrossRed,
                        contentColor = Color.White
                    ) {
                        Text(text = badgeCount.toString(), fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        ) {
            Text(
                text = emoji,
                fontSize = 20.sp,
                modifier = Modifier.padding(bottom = 2.dp)
            )
        }

        // Active bottom bar indicator with luminous Meskot logo gradient
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(3.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(if (isSelected) MeskotLogoBrush else Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
        )
    }
}

@Composable
fun UserAvatar(
    photoUrl: String?,
    name: String,
    size: Int = 40,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(GoldDeep, Gold)))
            .border(1.5.dp, Color.White, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = name,
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            val initials = name.trim().split("\\s+".toRegex()).take(2).mapNotNull { it.firstOrNull()?.uppercase() }.joinToString("")
            Text(
                text = if (initials.isNotBlank()) initials else "?",
                color = Color.White,
                fontSize = (size * 0.4f).sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif
            )
        }
    }
}
