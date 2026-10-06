package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FactCheck
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AppLanguage
import com.example.data.GroupItem
import com.example.data.UserGroupRole
import com.example.ui.MeskotViewModel
import com.example.ui.theme.ActiveGreen
import com.example.ui.theme.CardBg
import com.example.ui.theme.CrossRed
import com.example.ui.theme.Gold
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldDeep
import com.example.ui.theme.GoldSurface
import com.example.ui.theme.Ink
import com.example.ui.theme.LineBorder
import com.example.ui.theme.MutedText
import com.example.ui.theme.Paper
import com.example.ui.theme.Paper2

data class AdminToolShortcutItem(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val badgeText: String? = null
)

@Composable
fun AdminDashboardScreen(
    viewModel: MeskotViewModel,
    group: GroupItem,
    currentLanguage: AppLanguage = AppLanguage.EN,
    onBack: () -> Unit = { viewModel.navigateBack() }
) {
    val allGroups by viewModel.groups.collectAsState()
    val liveGroup = allGroups.find { it.id == group.id } ?: group
    val currentUser by viewModel.currentUser.collectAsState()
    val userRole = viewModel.getUserRoleForGroup(liveGroup, currentUser)

    var selectedToolDialog by remember { mutableStateOf<AdminToolShortcutItem?>(null) }
    var isPrivateState by remember(liveGroup.isPrivate) { mutableStateOf(liveGroup.isPrivate) }
    var allowMemberPostsState by remember(liveGroup.allowMemberPosts) { mutableStateOf(liveGroup.allowMemberPosts) }

    val toolShortcuts = remember(liveGroup.memberCount) {
        listOf(
            AdminToolShortcutItem(
                id = "people",
                title = "People",
                subtitle = "${liveGroup.memberCount} members, admins, and blocked users",
                icon = Icons.Default.People,
                badgeText = "${liveGroup.memberCount}"
            ),
            AdminToolShortcutItem(
                id = "activity_log",
                title = "Activity Log",
                subtitle = "Audit trail of admin and moderator actions",
                icon = Icons.Default.History
            ),
            AdminToolShortcutItem(
                id = "scheduled_posts",
                title = "Scheduled Posts",
                subtitle = "Manage upcoming community announcements",
                icon = Icons.Default.Schedule,
                badgeText = "2"
            ),
            AdminToolShortcutItem(
                id = "admin_assist",
                title = "Admin Assist",
                subtitle = "Automated rules for spam and post approvals",
                icon = Icons.Default.AutoAwesome,
                badgeText = "Active"
            ),
            AdminToolShortcutItem(
                id = "community_roles",
                title = "Community Roles",
                subtitle = "Assign Admin, Moderator, or Member permissions",
                icon = Icons.Default.AdminPanelSettings
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .testTag("admin_dashboard_screen")
    ) {
        // Top App Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = CardBg,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier.testTag("admin_dashboard_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back to group",
                        tint = Ink
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Admin Assist & Manage",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 18.sp,
                            color = Ink
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = liveGroup.name,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = MutedText,
                            fontSize = 12.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = GoldSurface,
                    border = BorderStroke(1.dp, GoldBorder)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Role Badge",
                            tint = GoldDeep,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = userRole.name,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldDeep
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Section 1: To Review (Review Queues)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "To review",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.ExtraBold,
                            fontFamily = FontFamily.Serif,
                            color = Ink
                        )
                        Text(
                            text = "Tap a queue to review & clear",
                            fontSize = 12.sp,
                            color = MutedText
                        )
                    }

                    // 2x2 Grid of Review Queue Cards
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ReviewQueueCard(
                            title = "Pending approvals",
                            subtitle = "Posts & member requests",
                            count = liveGroup.pendingApprovalsCount,
                            icon = Icons.Default.FactCheck,
                            accentColor = GoldDeep,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("queue_pending_approvals"),
                            onClick = {
                                viewModel.resolveGroupModerationQueue(liveGroup.id, "PENDING")
                            }
                        )

                        ReviewQueueCard(
                            title = "Reported content",
                            subtitle = "Member-flagged posts",
                            count = liveGroup.reportedContentCount,
                            icon = Icons.Default.Flag,
                            accentColor = CrossRed,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("queue_reported_content"),
                            onClick = {
                                viewModel.resolveGroupModerationQueue(liveGroup.id, "REPORTED")
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        ReviewQueueCard(
                            title = "Potential spam",
                            subtitle = "Filtered by Admin Assist",
                            count = liveGroup.potentialSpamCount,
                            icon = Icons.Default.ReportProblem,
                            accentColor = Color(0xFFD97706),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("queue_potential_spam"),
                            onClick = {
                                viewModel.resolveGroupModerationQueue(liveGroup.id, "SPAM")
                            }
                        )

                        ReviewQueueCard(
                            title = "Moderation alerts",
                            subtitle = "Keyword & conflict triggers",
                            count = liveGroup.moderationAlertsCount,
                            icon = Icons.Default.Security,
                            accentColor = ActiveGreen,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("queue_moderation_alerts"),
                            onClick = {
                                viewModel.resolveGroupModerationQueue(liveGroup.id, "ALERTS")
                            }
                        )
                    }
                }
            }

            // Section 2: Group Permissions & Quick Governance Controls
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = BorderStroke(1.dp, LineBorder)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Publishing & Privacy Permissions",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Allow members to post",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Ink
                                )
                                Text(
                                    text = "When off, only Admins and Moderators can publish posts",
                                    fontSize = 12.sp,
                                    color = MutedText
                                )
                            }
                            Switch(
                                checked = allowMemberPostsState,
                                onCheckedChange = { enabled ->
                                    allowMemberPostsState = enabled
                                    viewModel.updateGroupPrivacyAndPermissions(
                                        groupId = liveGroup.id,
                                        isPrivate = isPrivateState,
                                        allowMemberPosts = enabled
                                    )
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = Gold,
                                    uncheckedThumbColor = MutedText,
                                    uncheckedTrackColor = Paper2
                                ),
                                modifier = Modifier.testTag("switch_allow_member_posts")
                            )
                        }

                        HorizontalDivider(color = LineBorder.copy(alpha = 0.6f))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Private group mode",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Ink
                                )
                                Text(
                                    text = if (isPrivateState) "Private group • Only members see posts" else "Public group • Anyone can see posts",
                                    fontSize = 12.sp,
                                    color = MutedText
                                )
                            }
                            Switch(
                                checked = isPrivateState,
                                onCheckedChange = { priv ->
                                    isPrivateState = priv
                                    viewModel.updateGroupPrivacyAndPermissions(
                                        groupId = liveGroup.id,
                                        isPrivate = priv,
                                        allowMemberPosts = allowMemberPostsState
                                    )
                                },
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = Color.White,
                                    checkedTrackColor = GoldDeep,
                                    uncheckedThumbColor = MutedText,
                                    uncheckedTrackColor = Paper2
                                ),
                                modifier = Modifier.testTag("switch_private_group")
                            )
                        }
                    }
                }
            }

            // Section 3: Tool Shortcuts Vertical List
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Admin tools & shortcuts",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.ExtraBold,
                        fontFamily = FontFamily.Serif,
                        color = Ink
                    )

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = CardBg),
                        border = BorderStroke(1.dp, LineBorder)
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            toolShortcuts.forEachIndexed { index, tool ->
                                AdminToolShortcutRow(
                                    tool = tool,
                                    onClick = { selectedToolDialog = tool }
                                )
                                if (index < toolShortcuts.lastIndex) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = LineBorder.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(64.dp))
            }
        }
    }

    // Interactive Modal for Selected Admin Tool Shortcut
    selectedToolDialog?.let { tool ->
        Dialog(onDismissRequest = { selectedToolDialog = null }) {
            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.dp, GoldBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(GoldSurface)
                                .border(1.dp, GoldBorder, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = tool.icon,
                                contentDescription = tool.title,
                                tint = GoldDeep,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Column {
                            Text(
                                text = tool.title,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = Ink
                            )
                            Text(
                                text = liveGroup.name,
                                fontSize = 12.sp,
                                color = MutedText
                            )
                        }
                    }

                    HorizontalDivider(color = LineBorder)

                    when (tool.id) {
                        "community_roles" -> {
                            Text(
                                text = "Preview or switch your active role in this group to test role-based UI states:",
                                fontSize = 13.sp,
                                color = MutedText
                            )
                            UserGroupRole.values().forEach { roleOption ->
                                val isCurrent = userRole == roleOption
                                Surface(
                                    onClick = {
                                        viewModel.setGroupRoleOverride(liveGroup.id, roleOption)
                                        selectedToolDialog = null
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isCurrent) GoldSurface else Paper,
                                    border = BorderStroke(1.dp, if (isCurrent) GoldDeep else LineBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = roleOption.name,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isCurrent) GoldDeep else Ink
                                            )
                                            Text(
                                                text = when (roleOption) {
                                                    UserGroupRole.ADMIN -> "Full control, Manage button & Edit Cover visible"
                                                    UserGroupRole.MODERATOR -> "Moderation queues & Manage button visible"
                                                    UserGroupRole.MEMBER -> "Standard member: Join/Member & Invite buttons"
                                                    UserGroupRole.NON_MEMBER -> "Visitor: Join Group button, composer hidden"
                                                },
                                                fontSize = 11.sp,
                                                color = MutedText
                                            )
                                        }
                                        if (isCurrent) {
                                            Icon(
                                                imageVector = Icons.Default.CheckCircle,
                                                contentDescription = "Selected",
                                                tint = GoldDeep,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        "people" -> {
                            Text(
                                text = "• ${liveGroup.memberCount} Active community members\n• 2 Group Admins & 3 Moderators\n• 0 Blocked accounts",
                                fontSize = 13.5.sp,
                                color = Ink,
                                lineHeight = 20.sp
                            )
                        }
                        "activity_log" -> {
                            Text(
                                text = "• Admin Assist enabled spam protection (Today)\n• Cover photo updated to 16:9 HD banner\n• Pinned welcome announcement post",
                                fontSize = 13.5.sp,
                                color = Ink,
                                lineHeight = 20.sp
                            )
                        }
                        "scheduled_posts" -> {
                            Text(
                                text = "• Weekly Community Q&A Thread — Scheduled for Friday 6:00 PM\n• Monthly Member Spotlight — Scheduled for 1st of next month",
                                fontSize = 13.5.sp,
                                color = Ink,
                                lineHeight = 20.sp
                            )
                        }
                        else -> {
                            Text(
                                text = "• Automatically decline incoming posts with spam links\n• Hold posts with flagged keywords for moderator review\n• Auto-approve posts from trusted members with 3+ approved posts",
                                fontSize = 13.5.sp,
                                color = Ink,
                                lineHeight = 20.sp
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { selectedToolDialog = null }) {
                            Text("Close", color = GoldDeep, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ReviewQueueCard(
    title: String,
    subtitle: String,
    count: Int,
    icon: ImageVector,
    accentColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, if (count > 0) accentColor.copy(alpha = 0.45f) else LineBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(accentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Surface(
                    shape = CircleShape,
                    color = if (count > 0) accentColor else Paper2
                ) {
                    Text(
                        text = count.toString(),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (count > 0) Color.White else MutedText,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            Column {
                Text(
                    text = title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (count == 0) "All caught up ✓" else subtitle,
                    fontSize = 11.5.sp,
                    color = if (count == 0) ActiveGreen else MutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun AdminToolShortcutRow(
    tool: AdminToolShortcutItem,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("admin_tool_${tool.id}"),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(GoldSurface)
                .border(1.dp, GoldBorder.copy(alpha = 0.7f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = tool.icon,
                contentDescription = tool.title,
                tint = GoldDeep,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = tool.title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
            Text(
                text = tool.subtitle,
                fontSize = 12.sp,
                color = MutedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        if (tool.badgeText != null) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = GoldSurface,
                border = BorderStroke(1.dp, GoldBorder),
                modifier = Modifier.padding(end = 6.dp)
            ) {
                Text(
                    text = tool.badgeText,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldDeep,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = "Open ${tool.title}",
            tint = MutedText,
            modifier = Modifier.size(22.dp)
        )
    }
}
