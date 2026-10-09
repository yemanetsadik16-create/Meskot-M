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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Mood
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.OndemandVideo
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Poll
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import com.example.data.AppLanguage
import com.example.data.GroupItem
import com.example.data.GroupUiState
import com.example.data.MeskotStrings
import com.example.data.Post
import com.example.data.User
import com.example.data.UserGroupRole
import com.example.ui.MeskotViewModel
import com.example.ui.components.PostCard
import com.example.ui.components.UserAvatar
import com.example.ui.theme.ActiveGreen
import com.example.ui.theme.CardBg
import com.example.ui.theme.CrossRed
import com.example.ui.theme.Gold
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldDeep
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldSurface
import com.example.ui.theme.Ink
import com.example.ui.theme.LineBorder
import com.example.ui.theme.MutedText
import com.example.ui.theme.Paper
import com.example.ui.theme.Paper2
import com.example.ui.theme.meskotTextFieldColors

private data class GroupCategoryPill(
    val label: String,
    val icon: ImageVector
)

private val GroupCategoryPills = listOf(
    GroupCategoryPill("Videos", Icons.Default.OndemandVideo),
    GroupCategoryPill("Photos", Icons.Default.PhotoLibrary),
    GroupCategoryPill("Announcements", Icons.Default.PushPin),
    GroupCategoryPill("Events", Icons.Default.Event)
)

private val CoverPhotoPresets = listOf(
    "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=1000&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1544025162-d76694265947?w=1000&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1547471080-7cc2caa01a7e?w=1000&auto=format&fit=crop&q=80",
    "https://images.unsplash.com/photo-1511632765486-a01980e01a18?w=1000&auto=format&fit=crop&q=80"
)

@Composable
fun GroupDetailScreen(
    viewModel: MeskotViewModel,
    group: GroupItem,
    currentUser: User?,
    currentLanguage: AppLanguage
) {
    val allGroups by viewModel.groups.collectAsState()
    val allUsers by viewModel.users.collectAsState()
    val groupPostsMap by viewModel.groupPostsMap.collectAsState()
    val roleOverrides by viewModel.groupRoleOverrides.collectAsState()

    val liveGroup = allGroups.find { it.id == group.id } ?: group
    val rawPosts = groupPostsMap[liveGroup.id] ?: viewModel.getGroupPosts(liveGroup.id)
    val effectiveRole = roleOverrides[liveGroup.id] ?: viewModel.getUserRoleForGroup(liveGroup, currentUser)

    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var selectedFeedFilter by remember { mutableStateOf("Most relevant") }
    var isFeedFilterMenuOpen by remember { mutableStateOf(false) }
    var isRoleSwitcherOpen by remember { mutableStateOf(false) }
    var isEditCoverDialogOpen by remember { mutableStateOf(false) }
    var isInviteDialogOpen by remember { mutableStateOf(false) }
    var isQuickPollDialogOpen by remember { mutableStateOf(false) }
    var isQuickFeelingDialogOpen by remember { mutableStateOf(false) }

    // Filter & sort posts based on selected category pill and feed filter
    val filteredPosts = remember(rawPosts, selectedCategory, selectedFeedFilter) {
        val categoryFiltered = when (selectedCategory) {
            "Videos" -> rawPosts.filter {
                it.postType.equals("REEL", ignoreCase = true) ||
                    it.videoUrl.isNotBlank() ||
                    it.text.contains("video", ignoreCase = true)
            }
            "Photos" -> rawPosts.filter {
                it.mediaUrls.isNotEmpty() || it.postType.equals("PHOTO", ignoreCase = true)
            }
            "Announcements" -> rawPosts.filter {
                it.tags.any { tag -> tag.contains("announcement", ignoreCase = true) } ||
                    it.text.contains("Announcement", ignoreCase = true) ||
                    it.text.contains("📌")
            }
            "Events" -> rawPosts.filter {
                it.tags.any { tag -> tag.contains("event", ignoreCase = true) } ||
                    it.text.contains("Event", ignoreCase = true) ||
                    it.text.contains("📅")
            }
            else -> rawPosts
        }

        when (selectedFeedFilter) {
            "Newest activity" -> categoryFiltered.sortedByDescending { it.createdAt }
            "Top posts" -> categoryFiltered.sortedByDescending { it.reactions.size * 2 + it.commentCount }
            else -> categoryFiltered.sortedWith(
                compareByDescending<Post> { it.reactions.size + it.commentCount }
                    .thenByDescending { it.createdAt }
            )
        }
    }

    val memberAvatars = remember(allUsers, currentUser) {
        val list = mutableListOf<User>()
        if (currentUser != null) list.add(currentUser)
        list.addAll(allUsers.filter { it.uid != currentUser?.uid })
        list.take(6)
    }

    val canUserPost = remember(effectiveRole, liveGroup.allowMemberPosts) {
        when (effectiveRole) {
            UserGroupRole.ADMIN, UserGroupRole.MODERATOR -> true
            UserGroupRole.MEMBER -> liveGroup.allowMemberPosts
            UserGroupRole.NON_MEMBER -> false
        }
    }

    val uiState = remember(
        liveGroup,
        effectiveRole,
        memberAvatars,
        selectedCategory,
        selectedFeedFilter,
        canUserPost,
        filteredPosts
    ) {
        GroupUiState(
            group = liveGroup,
            userRole = effectiveRole,
            isPrivate = liveGroup.isPrivate,
            memberAvatars = memberAvatars,
            selectedCategory = selectedCategory,
            selectedFeedFilter = selectedFeedFilter,
            canPost = canUserPost,
            posts = filteredPosts,
            pendingApprovalsCount = liveGroup.pendingApprovalsCount,
            reportedContentCount = liveGroup.reportedContentCount,
            potentialSpamCount = liveGroup.potentialSpamCount,
            moderationAlertsCount = liveGroup.moderationAlertsCount
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .testTag("group_detail_screen")
    ) {
        // 1. GROUP COVER HEADER (Full-width 16:9 aspect ratio with floating Back, Role Switcher, and "Edit Cover" badge overlay)
        item(key = "group_cover_header") {
            GroupCoverHeaderSection(
                uiState = uiState,
                onBackClick = { viewModel.navigateBack() },
                onEditCoverClick = { isEditCoverDialogOpen = true },
                isRoleMenuOpen = isRoleSwitcherOpen,
                onToggleRoleMenu = { isRoleSwitcherOpen = !isRoleSwitcherOpen },
                onSelectRole = { newRole ->
                    isRoleSwitcherOpen = false
                    viewModel.setGroupRoleOverride(liveGroup.id, newRole)
                }
            )
        }

        // 2. GROUP METADATA SECTION + 3. DYNAMIC ACTION BAR + 4. CATEGORY FILTER PILLS
        item(key = "group_metadata_actions_categories") {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CardBg,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(3.dp)
                            .background(com.example.ui.theme.MeskotLogoHorizontalBrush)
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp)
                    ) {
                        // Metadata: Title, Globe/Lock + Privacy + Bullet + Member count, Overlapping Avatar Stack
                        GroupMetadataSection(
                            uiState = uiState,
                            currentLanguage = currentLanguage,
                            onMemberAvatarClick = { user -> viewModel.openProfile(user) }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Dynamic Action Bar: Button 1 ("Manage" with Shield icon OR "Join Group" / "Joined") + Button 2 ("Invite" with User-plus icon)
                        GroupDynamicActionBar(
                            uiState = uiState,
                            onManageClick = { viewModel.openGroupAdminDashboard(liveGroup) },
                            onJoinToggleClick = { viewModel.toggleGroupJoin(liveGroup) },
                            onInviteClick = { isInviteDialogOpen = true }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Category Filter Pills: Horizontal scrollable LazyRow (Videos, Photos, Announcements, Events)
                        GroupCategoryFilterPillsRow(
                            selectedCategory = uiState.selectedCategory,
                            onCategoryClick = { category ->
                                selectedCategory = if (selectedCategory == category) null else category
                            }
                        )
                    }
                }
            }
        }

        // 5. POST COMPOSER SECTION (Toggled based on post permissions in GroupUiState)
        item(key = "group_post_composer_section") {
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.canPost) {
                GroupPostComposerCard(
                    currentUser = currentUser,
                    onWriteSomethingClick = { viewModel.openComposer(liveGroup.id) },
                    onPhotoClick = { viewModel.openComposer(liveGroup.id) },
                    onFeelingClick = { isQuickFeelingDialogOpen = true },
                    onPollClick = { isQuickPollDialogOpen = true }
                )
            } else {
                GroupRestrictedComposerBanner(
                    uiState = uiState,
                    currentLanguage = currentLanguage,
                    onJoinClick = { viewModel.toggleGroupJoin(liveGroup) }
                )
            }
        }

        // 6. FEED HEADER ("Most relevant" with tune icon)
        item(key = "group_feed_header") {
            GroupFeedFilterHeader(
                selectedFilter = uiState.selectedFeedFilter,
                activeCategory = uiState.selectedCategory,
                isMenuExpanded = isFeedFilterMenuOpen,
                onOpenMenu = { isFeedFilterMenuOpen = true },
                onDismissMenu = { isFeedFilterMenuOpen = false },
                onSelectFilter = { filter ->
                    selectedFeedFilter = filter
                    isFeedFilterMenuOpen = false
                },
                onClearCategory = { selectedCategory = null }
            )
        }

        // 7. FEED STREAM OR EMPTY STATE PLACEHOLDER
        if (uiState.posts.isEmpty()) {
            item(key = "group_empty_feed") {
                GroupFeedEmptyPlaceholder(
                    selectedCategory = uiState.selectedCategory,
                    canPost = uiState.canPost,
                    currentLanguage = currentLanguage,
                    onCreateFirstPost = { viewModel.openComposer(liveGroup.id) },
                    onResetCategory = { selectedCategory = null }
                )
            }
        } else {
            items(uiState.posts, key = { it.id }) { post ->
                val comments = viewModel.getComments(post.id)
                PostCard(
                    post = post,
                    currentUserId = currentUser?.uid,
                    comments = comments,
                    currentLanguage = currentLanguage,
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

        item(key = "group_bottom_spacer") {
            Spacer(modifier = Modifier.height(84.dp))
        }
    }

    // Edit Cover Dialog
    if (isEditCoverDialogOpen) {
        EditGroupCoverDialog(
            currentCoverUrl = liveGroup.coverImageUrl,
            onDismiss = { isEditCoverDialogOpen = false },
            onSaveCover = { newUrl ->
                viewModel.updateGroupCover(liveGroup.id, newUrl)
                isEditCoverDialogOpen = false
            }
        )
    }

    // Invite Members Dialog
    if (isInviteDialogOpen) {
        InviteGroupMembersDialog(
            group = liveGroup,
            users = allUsers.filter { it.uid != currentUser?.uid },
            onDismiss = { isInviteDialogOpen = false },
            onInviteSent = { invitedUser ->
                viewModel.showMessage("📩 Invitation to join ${liveGroup.name} sent to ${invitedUser.displayName}!")
            }
        )
    }

    // Quick Feeling / Activity Post Dialog
    if (isQuickFeelingDialogOpen) {
        QuickGroupFeelingDialog(
            groupName = liveGroup.name,
            onDismiss = { isQuickFeelingDialogOpen = false },
            onPublishFeeling = { feelingEmoji, feelingLabel, note ->
                viewModel.openComposer(liveGroup.id)
                viewModel.submitPost(
                    text = "$feelingEmoji Feeling $feelingLabel — $note",
                    mediaUrls = emptyList(),
                    bgColorIndex = 0,
                    visibility = "public"
                )
                isQuickFeelingDialogOpen = false
            }
        )
    }

    // Quick Poll Creation Dialog
    if (isQuickPollDialogOpen) {
        QuickGroupPollDialog(
            groupName = liveGroup.name,
            onDismiss = { isQuickPollDialogOpen = false },
            onPublishPoll = { question, optA, optB ->
                viewModel.openComposer(liveGroup.id)
                viewModel.submitPost(
                    text = "📊 [Community Poll] $question\n\n1️⃣ $optA\n2️⃣ $optB\n\nVote in the comments below!",
                    mediaUrls = emptyList(),
                    bgColorIndex = 0,
                    visibility = "public"
                )
                isQuickPollDialogOpen = false
            }
        )
    }
}

@Composable
private fun GroupCoverHeaderSection(
    uiState: GroupUiState,
    onBackClick: () -> Unit,
    onEditCoverClick: () -> Unit,
    isRoleMenuOpen: Boolean,
    onToggleRoleMenu: () -> Unit,
    onSelectRole: (UserGroupRole) -> Unit
) {
    val group = uiState.group
    val fallbackColor = remember(group.coverColorHex) {
        try {
            Color(android.graphics.Color.parseColor(group.coverColorHex))
        } catch (e: Exception) {
            GoldDeep
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(fallbackColor)
            .testTag("group_cover_header")
    ) {
        if (group.coverImageUrl.isNotBlank()) {
            AsyncImage(
                model = group.coverImageUrl,
                contentDescription = "${group.name} cover image",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(fallbackColor, GoldDeep, Ink)
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = group.name.take(1).uppercase(),
                    color = Color.White.copy(alpha = 0.25f),
                    fontSize = 72.sp,
                    fontWeight = FontWeight.ExtraBold,
                    fontFamily = FontFamily.Serif
                )
            }
        }

        // Top & Bottom subtle scrim gradients for legibility of floating controls
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.45f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.50f)
                        )
                    )
                )
        )

        // Top Bar Overlay: Back button & Role Switcher pill
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .testTag("group_detail_back_btn")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }

            // Role Switcher Badge so user/tester can inspect ADMIN, MODERATOR, MEMBER, NON_MEMBER states live
            Box {
                Surface(
                    onClick = onToggleRoleMenu,
                    shape = RoundedCornerShape(20.dp),
                    color = Color.Black.copy(alpha = 0.52f),
                    border = BorderStroke(1.dp, GoldLight.copy(alpha = 0.6f)),
                    modifier = Modifier.testTag("group_role_selector_chip")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = "Role",
                            tint = GoldLight,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "Role: ${uiState.userRole.name}",
                            color = Color.White,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                DropdownMenu(
                    expanded = isRoleMenuOpen,
                    onDismissRequest = onToggleRoleMenu,
                    modifier = Modifier.background(CardBg)
                ) {
                    UserGroupRole.values().forEach { role ->
                        DropdownMenuItem(
                            text = {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = role.name,
                                        fontWeight = if (uiState.userRole == role) FontWeight.ExtraBold else FontWeight.Normal,
                                        color = if (uiState.userRole == role) GoldDeep else Ink
                                    )
                                    if (uiState.userRole == role) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = GoldDeep,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            },
                            onClick = { onSelectRole(role) }
                        )
                    }
                }
            }
        }

        // Floating "Edit Cover" Badge Overlay (Bottom-Right)
        Surface(
            onClick = onEditCoverClick,
            shape = RoundedCornerShape(20.dp),
            color = Color.Black.copy(alpha = 0.65f),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.4f)),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(12.dp)
                .testTag("edit_group_cover_badge")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Edit Cover",
                    tint = Color.White,
                    modifier = Modifier.size(15.dp)
                )
                Text(
                    text = "Edit Cover",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun GroupMetadataSection(
    uiState: GroupUiState,
    currentLanguage: AppLanguage,
    onMemberAvatarClick: (User) -> Unit
) {
    val group = uiState.group
    val privacyLabel = if (uiState.isPrivate) "Private group" else "Public group"
    val privacyIcon = if (uiState.isPrivate) Icons.Default.Lock else Icons.Default.Public

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Group Title (Bold headline font)
        Text(
            text = group.name,
            style = MaterialTheme.typography.headlineSmall.copy(
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.Serif,
                color = Ink,
                fontSize = 23.sp,
                lineHeight = 28.sp
            ),
            modifier = Modifier.testTag("group_detail_title")
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Subtitle Row: Globe/Lock icon + Privacy status + bullet separator + Member count ("142 members")
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.testTag("group_detail_subtitle_row")
        ) {
            Icon(
                imageVector = privacyIcon,
                contentDescription = privacyLabel,
                tint = MutedText,
                modifier = Modifier.size(15.dp)
            )
            Spacer(modifier = Modifier.width(5.dp))
            Text(
                text = privacyLabel,
                fontSize = 13.5.sp,
                color = MutedText,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = " • ",
                fontSize = 13.5.sp,
                color = MutedText,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${group.memberCount} ${MeskotStrings.get("members", currentLanguage)}",
                fontSize = 13.5.sp,
                color = Ink,
                fontWeight = FontWeight.Bold
            )
        }

        if (group.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = group.description,
                fontSize = 13.5.sp,
                color = MutedText,
                lineHeight = 19.sp
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Overlapping Row of Member Profile Avatars
        if (uiState.memberAvatars.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("group_member_avatar_stack")
            ) {
                Box(modifier = Modifier.height(36.dp)) {
                    uiState.memberAvatars.forEachIndexed { index, member ->
                        Box(
                            modifier = Modifier
                                .offset(x = (index * 24).dp)
                                .zIndex((uiState.memberAvatars.size - index).toFloat())
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CardBg)
                                .border(2.dp, CardBg, CircleShape)
                                .clickable { onMemberAvatarClick(member) }
                        ) {
                            UserAvatar(
                                photoUrl = member.photoUrl,
                                name = member.displayName,
                                size = 32
                            )
                        }
                    }
                }

                val stackWidth = ((uiState.memberAvatars.size - 1).coerceAtLeast(0) * 24 + 42).dp
                Spacer(modifier = Modifier.width(stackWidth - 28.dp))

                Text(
                    text = if (group.memberCount > uiState.memberAvatars.size) {
                        "+${group.memberCount - uiState.memberAvatars.size} community members"
                    } else {
                        "Active community members"
                    },
                    fontSize = 12.sp,
                    color = MutedText,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@Composable
private fun GroupDynamicActionBar(
    uiState: GroupUiState,
    onManageClick: () -> Unit,
    onJoinToggleClick: () -> Unit,
    onInviteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .testTag("group_dynamic_action_bar"),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Button 1: "Manage" (Shield icon) if ADMIN or MODERATOR; otherwise "Join Group" (or "Joined" for MEMBER)
        if (uiState.canManage) {
            Button(
                onClick = onManageClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("group_manage_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Manage Group",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Manage",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                if (uiState.pendingApprovalsCount + uiState.reportedContentCount > 0) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = CircleShape,
                        color = Color.White.copy(alpha = 0.25f)
                    ) {
                        Text(
                            text = "${uiState.pendingApprovalsCount + uiState.reportedContentCount}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                        )
                    }
                }
            }
        } else if (uiState.userRole == UserGroupRole.MEMBER) {
            OutlinedButton(
                onClick = onJoinToggleClick,
                shape = RoundedCornerShape(10.dp),
                border = BorderStroke(1.dp, LineBorder),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Paper2,
                    contentColor = Ink
                ),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("group_joined_member_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Joined",
                    tint = ActiveGreen,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Joined",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        } else {
            Button(
                onClick = onJoinToggleClick,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
                    .testTag("group_join_btn")
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = "Join Group",
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Join Group",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Button 2: "Invite" (User-plus icon) for active members
        Button(
            onClick = {
                if (uiState.isMember) {
                    onInviteClick()
                } else {
                    onJoinToggleClick()
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (uiState.isMember) GoldSurface else Paper2,
                contentColor = if (uiState.isMember) GoldDeep else MutedText
            ),
            border = BorderStroke(1.dp, if (uiState.isMember) GoldBorder else LineBorder),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier
                .weight(1f)
                .height(48.dp)
                .testTag("group_invite_btn")
        ) {
            Icon(
                imageVector = Icons.Default.PersonAdd,
                contentDescription = "Invite",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Invite",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun GroupCategoryFilterPillsRow(
    selectedCategory: String?,
    onCategoryClick: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("group_category_pills_row"),
        contentPadding = PaddingValues(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(GroupCategoryPills, key = { it.label }) { pill ->
            val isSelected = selectedCategory == pill.label
            FilterChip(
                selected = isSelected,
                onClick = { onCategoryClick(pill.label) },
                label = {
                    Text(
                        text = pill.label,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = pill.icon,
                        contentDescription = pill.label,
                        modifier = Modifier.size(16.dp)
                    )
                },
                shape = RoundedCornerShape(20.dp),
                colors = FilterChipDefaults.filterChipColors(
                    containerColor = Paper,
                    labelColor = Ink,
                    iconColor = GoldDeep,
                    selectedContainerColor = Gold,
                    selectedLabelColor = Color.White,
                    selectedLeadingIconColor = Color.White
                ),
                border = FilterChipDefaults.filterChipBorder(
                    enabled = true,
                    selected = isSelected,
                    borderColor = LineBorder,
                    selectedBorderColor = GoldDeep
                ),
                modifier = Modifier.testTag("category_pill_${pill.label.lowercase()}")
            )
        }
    }
}

@Composable
private fun GroupPostComposerCard(
    currentUser: User?,
    onWriteSomethingClick: () -> Unit,
    onPhotoClick: () -> Unit,
    onFeelingClick: () -> Unit,
    onPollClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .testTag("group_post_composer_card"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.2.dp, GoldBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Top Row: User profile avatar + Rounded full-width text trigger button ("Write something...")
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                UserAvatar(
                    photoUrl = currentUser?.photoUrl ?: "",
                    name = currentUser?.displayName ?: "Meskot Member",
                    size = 42
                )

                Spacer(modifier = Modifier.width(10.dp))

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(24.dp))
                        .background(GoldSurface)
                        .border(1.dp, GoldBorder, RoundedCornerShape(24.dp))
                        .clickable { onWriteSomethingClick() }
                        .padding(horizontal = 16.dp, vertical = 11.dp)
                        .testTag("group_write_something_trigger")
                ) {
                    Text(
                        text = "Write something...",
                        color = GoldDeep,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            HorizontalDivider(color = GoldBorder.copy(alpha = 0.65f))

            // Bottom Row: 3 action items (Photo, Feeling, Poll)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ComposerActionPill(
                    icon = Icons.Default.PhotoLibrary,
                    label = "Photo",
                    iconTint = ActiveGreen,
                    onClick = onPhotoClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("group_composer_photo_btn")
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(22.dp)
                        .background(LineBorder)
                )

                ComposerActionPill(
                    icon = Icons.Default.EmojiEmotions,
                    label = "Feeling",
                    iconTint = Gold,
                    onClick = onFeelingClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("group_composer_feeling_btn")
                )

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(22.dp)
                        .background(LineBorder)
                )

                ComposerActionPill(
                    icon = Icons.Default.Poll,
                    label = "Poll",
                    iconTint = CrossRed,
                    onClick = onPollClick,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("group_composer_poll_btn")
                )
            }
        }
    }
}

@Composable
private fun ComposerActionPill(
    icon: ImageVector,
    label: String,
    iconTint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconTint,
            modifier = Modifier.size(19.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = Ink
        )
    }
}

@Composable
private fun GroupRestrictedComposerBanner(
    uiState: GroupUiState,
    currentLanguage: AppLanguage,
    onJoinClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .testTag("group_restricted_composer_banner"),
        shape = RoundedCornerShape(16.dp),
        color = CardBg,
        border = BorderStroke(1.dp, LineBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (!uiState.isMember) {
                        MeskotStrings.get("joinToPost", currentLanguage)
                    } else {
                        "Only Admins and Moderators can post in this group"
                    },
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Ink
                )
                Text(
                    text = if (!uiState.isMember) {
                        "Join ${uiState.group.name} to share photos, polls, and updates."
                    } else {
                        "Posting permissions are managed by group admins."
                    },
                    fontSize = 12.sp,
                    color = MutedText
                )
            }

            if (!uiState.isMember) {
                Spacer(modifier = Modifier.width(12.dp))
                Button(
                    onClick = onJoinClick,
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Join", fontWeight = FontWeight.Bold, fontSize = 12.5.sp)
                }
            }
        }
    }
}

@Composable
private fun GroupFeedFilterHeader(
    selectedFilter: String,
    activeCategory: String?,
    isMenuExpanded: Boolean,
    onOpenMenu: () -> Unit,
    onDismissMenu: () -> Unit,
    onSelectFilter: (String) -> Unit,
    onClearCategory: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .testTag("group_feed_filter_header"),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box {
            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onOpenMenu() }
                    .padding(vertical = 4.dp, horizontal = 4.dp)
                    .testTag("group_most_relevant_filter_btn"),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = selectedFilter,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Ink
                )
                Icon(
                    imageVector = Icons.Default.Tune,
                    contentDescription = "Filter posts",
                    tint = GoldDeep,
                    modifier = Modifier.size(18.dp)
                )
            }

            DropdownMenu(
                expanded = isMenuExpanded,
                onDismissRequest = onDismissMenu,
                modifier = Modifier.background(CardBg)
            ) {
                listOf("Most relevant", "Newest activity", "Top posts").forEach { option ->
                    DropdownMenuItem(
                        text = {
                            Text(
                                text = option,
                                fontWeight = if (selectedFilter == option) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedFilter == option) GoldDeep else Ink
                            )
                        },
                        onClick = { onSelectFilter(option) }
                    )
                }
            }
        }

        if (activeCategory != null) {
            Surface(
                onClick = onClearCategory,
                shape = RoundedCornerShape(14.dp),
                color = GoldSurface,
                border = BorderStroke(1.dp, GoldBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = activeCategory,
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldDeep
                    )
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear filter",
                        tint = GoldDeep,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun GroupFeedEmptyPlaceholder(
    selectedCategory: String?,
    canPost: Boolean,
    currentLanguage: AppLanguage,
    onCreateFirstPost: () -> Unit,
    onResetCategory: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 8.dp)
            .testTag("group_empty_state_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.dp, LineBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(GoldSurface)
                    .border(1.dp, GoldBorder, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = GoldDeep,
                    modifier = Modifier.size(28.dp)
                )
            }

            Text(
                text = if (selectedCategory != null) {
                    "No $selectedCategory in this group yet"
                } else {
                    MeskotStrings.get("noGroupPosts", currentLanguage)
                },
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Ink,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (selectedCategory != null) {
                    "Try clearing the $selectedCategory filter or share the first item with the community."
                } else {
                    "Start the conversation by sharing an update, photo, announcement, or community poll."
                },
                fontSize = 13.sp,
                color = MutedText,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(4.dp))

            if (selectedCategory != null) {
                OutlinedButton(
                    onClick = onResetCategory,
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, GoldBorder)
                ) {
                    Text("Show All Posts", color = GoldDeep, fontWeight = FontWeight.Bold)
                }
            } else if (canPost) {
                Button(
                    onClick = onCreateFirstPost,
                    colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.White),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Write First Post", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun EditGroupCoverDialog(
    currentCoverUrl: String,
    onDismiss: () -> Unit,
    onSaveCover: (String) -> Unit
) {
    var customUrl by remember { mutableStateOf(currentCoverUrl) }

    Dialog(onDismissRequest = onDismiss) {
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
                Text(
                    text = "Update Group Cover (16:9)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Ink
                )

                Text(
                    text = "Choose a preset 16:9 banner or paste an image URL:",
                    fontSize = 12.5.sp,
                    color = MutedText
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(CoverPhotoPresets) { presetUrl ->
                        val isSelected = customUrl == presetUrl
                        Box(
                            modifier = Modifier
                                .width(120.dp)
                                .aspectRatio(16f / 9f)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    width = if (isSelected) 2.5.dp else 1.dp,
                                    color = if (isSelected) Gold else LineBorder,
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .clickable { customUrl = presetUrl }
                        ) {
                            AsyncImage(
                                model = presetUrl,
                                contentDescription = "Preset Cover",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = customUrl,
                    onValueChange = { customUrl = it },
                    label = { Text("Cover Image URL") },
                    singleLine = true,
                    colors = meskotTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MutedText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onSaveCover(customUrl.trim()) },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Save Cover", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun InviteGroupMembersDialog(
    group: GroupItem,
    users: List<User>,
    onDismiss: () -> Unit,
    onInviteSent: (User) -> Unit
) {
    var invitedUids by remember { mutableStateOf(setOf<String>()) }

    Dialog(onDismissRequest = onDismiss) {
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
                Text(
                    text = "Invite Friends to ${group.name}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Ink
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    users.take(5).forEach { user ->
                        val isInvited = invitedUids.contains(user.uid)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                UserAvatar(photoUrl = user.photoUrl, name = user.displayName, size = 38)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = user.displayName,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink
                                    )
                                    if (user.location.isNotBlank()) {
                                        Text(
                                            text = user.location,
                                            fontSize = 11.5.sp,
                                            color = MutedText
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    if (!isInvited) {
                                        invitedUids = invitedUids + user.uid
                                        onInviteSent(user)
                                    }
                                },
                                enabled = !isInvited,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isInvited) Paper2 else Gold,
                                    contentColor = if (isInvited) MutedText else Color.White
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                modifier = Modifier.height(34.dp)
                            ) {
                                Text(
                                    text = if (isInvited) "Invited ✓" else "Invite",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Done", color = GoldDeep, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickGroupFeelingDialog(
    groupName: String,
    onDismiss: () -> Unit,
    onPublishFeeling: (emoji: String, label: String, note: String) -> Unit
) {
    val feelings = listOf(
        "😊" to "Happy",
        "🔥" to "Excited",
        "🙏" to "Blessed",
        "💡" to "Inspired",
        "☕" to "Enjoying Buna",
        "🎉" to "Celebrating"
    )
    var selected by remember { mutableStateOf(feelings.first()) }
    var note by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
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
                Text(
                    text = "Share Feeling in $groupName",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Ink
                )

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(feelings) { item ->
                        val isChosen = selected == item
                        Surface(
                            onClick = { selected = item },
                            shape = RoundedCornerShape(16.dp),
                            color = if (isChosen) GoldSurface else Paper,
                            border = BorderStroke(1.dp, if (isChosen) GoldDeep else LineBorder)
                        ) {
                            Text(
                                text = "${item.first} ${item.second}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isChosen) GoldDeep else Ink,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Add a message...") },
                    colors = meskotTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MutedText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onPublishFeeling(
                                selected.first,
                                selected.second,
                                note.ifBlank { "Sharing good vibes with the community!" }
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Post", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickGroupPollDialog(
    groupName: String,
    onDismiss: () -> Unit,
    onPublishPoll: (question: String, optionA: String, optionB: String) -> Unit
) {
    var question by remember { mutableStateOf("") }
    var optionA by remember { mutableStateOf("") }
    var optionB by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = BorderStroke(1.dp, GoldBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Create Poll in $groupName",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Ink
                )

                OutlinedTextField(
                    value = question,
                    onValueChange = { question = it },
                    label = { Text("Ask the community a question...") },
                    colors = meskotTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                OutlinedTextField(
                    value = optionA,
                    onValueChange = { optionA = it },
                    label = { Text("Option 1") },
                    colors = meskotTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                OutlinedTextField(
                    value = optionB,
                    onValueChange = { optionB = it },
                    label = { Text("Option 2") },
                    colors = meskotTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel", color = MutedText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onPublishPoll(
                                question.trim(),
                                optionA.trim().ifBlank { "Yes" },
                                optionB.trim().ifBlank { "No" }
                            )
                        },
                        enabled = question.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Publish Poll", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
