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
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
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
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.outlined.DynamicFeed
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.outlined.Diversity3
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.outlined.Forum
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.outlined.Campaign
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.outlined.GridView
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
import androidx.compose.ui.text.style.TextOverflow
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
        color = Color.White,
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

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 9.dp)
            ) {
                // Left: User profile avatar
                if (currentUser != null) {
                    UserAvatar(
                        photoUrl = currentUser.photoUrl,
                        name = currentUser.displayName,
                        size = 34,
                        modifier = Modifier
                            .align(Alignment.CenterStart)
                            .clickable { onProfileClick() }
                    )
                }

                // Center Top: Meskot Brand Logo & Title
                MeskotLogoHeader(
                    size = 34.dp,
                    onClick = { onProfileClick() },
                    modifier = Modifier.align(Alignment.Center)
                )

                // Right: Language Switch + Search Button
                Row(
                    modifier = Modifier.align(Alignment.CenterEnd),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .height(34.dp)
                            .clip(RoundedCornerShape(17.dp))
                            .background(GoldSurface)
                            .border(1.dp, GoldBorder.copy(alpha = 0.8f), RoundedCornerShape(17.dp))
                            .clickable { onToggleLanguage() }
                            .padding(horizontal = 9.dp)
                            .testTag("nav_lang_toggle_btn"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (currentLanguage == AppLanguage.AM) "አማ" else "EN",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldDeep
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(34.dp)
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
    currentLanguage: AppLanguage = AppLanguage.EN,
    onTabSelected: (ScreenTab) -> Unit
) {
    val isAm = currentLanguage == AppLanguage.AM
    val homeLabel = if (isAm) "መነሻ" else "Home"
    val connectionsLabel = if (isAm) "ወዳጆች" else "Connections"
    val chatsLabel = if (isAm) "ውይይት" else "Chats"
    val clipsLabel = if (isAm) "ክሊፖች" else "Clips"
    val bazaarLabel = if (isAm) "ባዛር" else "Bazaar"
    val activityLabel = if (isAm) "እንቅስቃሴ" else "Activity"
    val hubLabel = if (isAm) "ማዕከል" else "Hub"
    val adminLabel = if (isAm) "አስተዳዳሪ" else "Admin"

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding(),
        color = Color.White,
        shadowElevation = 8.dp
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Micro hairline top divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.6.dp)
                    .background(LineBorder.copy(alpha = 0.7f))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                NavLuxuryItem(
                    activeIcon = Icons.Filled.DynamicFeed,
                    inactiveIcon = Icons.Outlined.DynamicFeed,
                    label = homeLabel,
                    isSelected = currentTab == ScreenTab.FEED,
                    onClick = { onTabSelected(ScreenTab.FEED) },
                    testTag = "tab_feed"
                )

                NavLuxuryItem(
                    activeIcon = Icons.Filled.Diversity3,
                    inactiveIcon = Icons.Outlined.Diversity3,
                    label = connectionsLabel,
                    badgeCount = unreadReqCount,
                    isSelected = currentTab == ScreenTab.FRIENDS,
                    onClick = { onTabSelected(ScreenTab.FRIENDS) },
                    testTag = "tab_friends"
                )

                NavLuxuryItem(
                    activeIcon = Icons.Filled.Forum,
                    inactiveIcon = Icons.Outlined.Forum,
                    label = chatsLabel,
                    badgeCount = unreadMsgCount,
                    isSelected = currentTab == ScreenTab.MESSAGES || currentTab == ScreenTab.CHAT,
                    onClick = { onTabSelected(ScreenTab.MESSAGES) },
                    testTag = "tab_messages"
                )

                NavLuxuryItem(
                    activeIcon = Icons.Filled.PlayCircle,
                    inactiveIcon = Icons.Outlined.PlayCircle,
                    label = clipsLabel,
                    isSelected = currentTab == ScreenTab.REELS,
                    onClick = { onTabSelected(ScreenTab.REELS) },
                    testTag = "tab_reels"
                )

                NavLuxuryItem(
                    activeIcon = Icons.Filled.ShoppingBag,
                    inactiveIcon = Icons.Outlined.ShoppingBag,
                    label = bazaarLabel,
                    isSelected = currentTab == ScreenTab.MARKETPLACE,
                    onClick = { onTabSelected(ScreenTab.MARKETPLACE) },
                    testTag = "tab_marketplace"
                )

                NavLuxuryItem(
                    activeIcon = Icons.Filled.Campaign,
                    inactiveIcon = Icons.Outlined.Campaign,
                    label = activityLabel,
                    badgeCount = unreadNotifCount,
                    isSelected = currentTab == ScreenTab.NOTIFICATIONS,
                    onClick = { onTabSelected(ScreenTab.NOTIFICATIONS) },
                    testTag = "tab_notifications"
                )

                NavLuxuryItem(
                    activeIcon = Icons.Filled.GridView,
                    inactiveIcon = Icons.Outlined.GridView,
                    label = hubLabel,
                    isSelected = currentTab == ScreenTab.MENU,
                    onClick = { onTabSelected(ScreenTab.MENU) },
                    testTag = "tab_menu"
                )

                if (isAdmin) {
                    NavLuxuryItem(
                        activeIcon = Icons.Filled.Security,
                        inactiveIcon = Icons.Outlined.Security,
                        label = adminLabel,
                        isSelected = currentTab == ScreenTab.ADMIN,
                        onClick = { onTabSelected(ScreenTab.ADMIN) },
                        testTag = "tab_admin"
                    )
                }
            }
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

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .defaultMinSize(minWidth = 44.dp, minHeight = 44.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                if (isSelected) LuxuryGoldShimmer.copy(alpha = 0.5f) else Color.Transparent
            )
            .border(
                width = if (isSelected) 1.5.dp else 0.dp,
                color = if (isSelected) Color(0xFFC48F37) else Color.Transparent,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 10.dp, vertical = 7.dp)
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
                modifier = Modifier.size(24.dp)
            )
        }
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

