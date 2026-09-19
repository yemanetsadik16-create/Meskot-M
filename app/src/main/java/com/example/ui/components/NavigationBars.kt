package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.outlined.People
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.outlined.PhotoLibrary
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.Security
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.example.ui.theme.InkDark
import com.example.ui.theme.LineBorder
import com.example.ui.theme.LuxuryDarkGlass
import com.example.ui.theme.LuxuryGlassGradient
import com.example.ui.theme.LuxuryGoldAccent
import com.example.ui.theme.LuxuryGoldBorder
import com.example.ui.theme.LuxuryGoldGradient
import com.example.ui.theme.LuxuryGoldShimmer
import com.example.ui.theme.LuxuryLightGlass
import com.example.ui.theme.LuxuryTabActiveBrush
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
    onOpenLive: () -> Unit = {},
    onOpenSearch: () -> Unit,
    onOpenMenu: () -> Unit,
    onProfileClick: () -> Unit,
    onLogout: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.96f),
        shadowElevation = 3.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Meskot brand emblem top decorative luxury gold gradient bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(
                        Brush.horizontalGradient(
                            listOf(
                                GoldLight,
                                LuxuryGoldAccent,
                                GoldDeep,
                                LuxuryGoldAccent,
                                GoldLight
                            )
                        )
                    )
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 9.dp),
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
                        size = 34.dp,
                        onClick = { onProfileClick() }
                    )
                }

                // Right Action Controls: [+] [🔍] [☰]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // [🔴 Live] Functional Live Stream & Gifts Button with pulsating badge styling
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFE53935), Color(0xFFC62828), Color(0xFFB71C1C))
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                            .clickable { onOpenLive() }
                            .padding(horizontal = 11.dp)
                            .testTag("live_stream_nav_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color.White)
                            )
                            Text(
                                text = "Live",
                                color = Color.White,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 0.2.sp
                            )
                        }
                    }

                    // [+] Create Post button in radiant Meskot Gold with luxury drop glow
                    Box(
                        modifier = Modifier
                            .height(36.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(GoldLight, LuxuryGoldAccent, GoldDeep)
                                )
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                            .clickable { onOpenComposer() }
                            .padding(horizontal = 11.dp)
                            .testTag("create_post_nav_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create Post",
                                tint = Color.White,
                                modifier = Modifier.size(17.dp)
                            )
                            Text(
                                text = "Post",
                                color = Color.White,
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 0.2.sp
                            )
                        }
                    }

                    // [🔍] Search button with luxury frosted gold tint & soft contour
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldSurface)
                            .border(1.dp, GoldBorder.copy(alpha = 0.8f), CircleShape)
                            .clickable { onOpenSearch() }
                            .testTag("search_nav_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = GoldDeep,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // [☰] Menu button with luxury frosted gold tint
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldSurface)
                            .border(1.dp, GoldBorder.copy(alpha = 0.8f), CircleShape)
                            .clickable { onOpenMenu() }
                            .testTag("menu_nav_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "Menu",
                            tint = GoldDeep,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // Micro hairline bottom divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(LineBorder.copy(alpha = 0.6f))
            )
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
        color = Color(0xFFFCFBF8).copy(alpha = 0.98f),
        shadowElevation = 2.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavLuxuryItem(
                    activeIcon = Icons.Filled.Home,
                    inactiveIcon = Icons.Outlined.Home,
                    label = "Feed",
                    isSelected = currentTab == ScreenTab.FEED,
                    onClick = { onTabSelected(ScreenTab.FEED) },
                    testTag = "tab_feed"
                )

                NavLuxuryItem(
                    activeIcon = Icons.Filled.People,
                    inactiveIcon = Icons.Outlined.People,
                    label = "Friends",
                    badgeCount = unreadReqCount,
                    isSelected = currentTab == ScreenTab.FRIENDS,
                    onClick = { onTabSelected(ScreenTab.FRIENDS) },
                    testTag = "tab_friends"
                )

                NavLuxuryItem(
                    activeIcon = Icons.Filled.ChatBubble,
                    inactiveIcon = Icons.Outlined.ChatBubbleOutline,
                    label = "Messages",
                    badgeCount = unreadMsgCount,
                    isSelected = currentTab == ScreenTab.MESSAGES || currentTab == ScreenTab.CHAT,
                    onClick = { onTabSelected(ScreenTab.MESSAGES) },
                    testTag = "tab_messages"
                )

                NavLuxuryItem(
                    activeIcon = Icons.Filled.Groups,
                    inactiveIcon = Icons.Outlined.Groups,
                    label = "Groups",
                    isSelected = currentTab == ScreenTab.GROUPS || currentTab == ScreenTab.GROUP_DETAIL,
                    onClick = { onTabSelected(ScreenTab.GROUPS) },
                    testTag = "tab_groups"
                )

                NavLuxuryItem(
                    activeIcon = Icons.Filled.PhotoLibrary,
                    inactiveIcon = Icons.Outlined.PhotoLibrary,
                    label = "Photos",
                    isSelected = currentTab == ScreenTab.PHOTOS || currentTab == ScreenTab.ALBUM_DETAIL,
                    onClick = { onTabSelected(ScreenTab.PHOTOS) },
                    testTag = "tab_photos"
                )

                NavLuxuryItem(
                    activeIcon = Icons.Filled.Notifications,
                    inactiveIcon = Icons.Outlined.Notifications,
                    label = "Alerts",
                    badgeCount = unreadNotifCount,
                    isSelected = currentTab == ScreenTab.NOTIFICATIONS,
                    onClick = { onTabSelected(ScreenTab.NOTIFICATIONS) },
                    testTag = "tab_notifications"
                )

                NavLuxuryItem(
                    activeIcon = Icons.Filled.BarChart,
                    inactiveIcon = Icons.Outlined.BarChart,
                    label = "Stats",
                    isSelected = currentTab == ScreenTab.DASHBOARD,
                    onClick = { onTabSelected(ScreenTab.DASHBOARD) },
                    testTag = "tab_dashboard"
                )

                if (isAdmin) {
                    NavLuxuryItem(
                        activeIcon = Icons.Filled.Security,
                        inactiveIcon = Icons.Outlined.Security,
                        label = "Admin",
                        isSelected = currentTab == ScreenTab.ADMIN,
                        onClick = { onTabSelected(ScreenTab.ADMIN) },
                        testTag = "tab_admin"
                    )
                }
            }

            // Micro hairline bottom divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.5.dp)
                    .background(LineBorder.copy(alpha = 0.5f))
            )
        }
    }
}

@Composable
private fun NavLuxuryItem(
    activeIcon: ImageVector,
    inactiveIcon: ImageVector,
    label: String,
    isSelected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit,
    testTag: String
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.06f else 1.0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow),
        label = "nav_item_scale"
    )

    val iconTint by animateColorAsState(
        targetValue = if (isSelected) GoldDeep else MutedText.copy(alpha = 0.85f),
        label = "nav_item_tint"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) LuxuryGoldShimmer.copy(alpha = 0.7f) else Color.Transparent
            )
            .border(
                width = 1.dp,
                color = if (isSelected) GoldBorder.copy(alpha = 0.65f) else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 7.dp, vertical = 5.dp)
            .scale(scale)
            .testTag(testTag)
    ) {
        BadgedBox(
            badge = {
                if (badgeCount > 0) {
                    Badge(
                        containerColor = CrossRed,
                        contentColor = Color.White,
                        modifier = Modifier.offset(x = (-2).dp, y = (2).dp)
                    ) {
                        Text(
                            text = if (badgeCount > 99) "99+" else badgeCount.toString(),
                            fontSize = 8.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        ) {
            Icon(
                imageVector = if (isSelected) activeIcon else inactiveIcon,
                contentDescription = label,
                tint = iconTint,
                modifier = Modifier.size(21.dp)
            )
        }

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = label,
            fontSize = 9.5.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) Ink else MutedText,
            letterSpacing = (-0.2).sp
        )

        Spacer(modifier = Modifier.height(2.dp))

        // Active indicator pill with radiant Meskot gold gradient
        Box(
            modifier = Modifier
                .width(if (isSelected) 18.dp else 0.dp)
                .height(2.5.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(
                    if (isSelected) {
                        Brush.horizontalGradient(listOf(GoldLight, LuxuryGoldAccent, GoldDeep))
                    } else {
                        Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    }
                )
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
