package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.data.MeskotRepository
import com.example.ui.MeskotViewModel
import com.example.ui.ScreenTab
import com.example.ui.components.BoostPostModal
import com.example.ui.components.CallOverlay
import com.example.ui.components.ComposerDialog
import com.example.ui.components.CreateReelDialog
import com.example.ui.components.CreateStoryDialog
import com.example.ui.components.EditProfileDialog
import com.example.ui.components.IconNavBar
import com.example.ui.components.IncomingCallOverlay
import com.example.ui.components.PostOptionsMenu
import com.example.ui.components.ReelViewerDialog
import com.example.ui.components.StoryViewerDialog
import com.example.ui.components.SubscriptionModal
import com.example.ui.components.TipModal
import com.example.ui.components.ChapaDepositModal
import com.example.ui.components.ChapaPaymentModal
import com.example.ui.components.MeskotLiveStreamModal
import com.example.ui.components.MeskotSplashScreen
import com.example.ui.components.TopNavBar
import com.example.util.CallAudioManager
import com.example.ui.screens.AdminDashboardScreen
import com.example.ui.screens.AdminScreen
import com.example.ui.screens.AdsManagerScreen
import com.example.ui.screens.AlbumDetailScreen
import com.example.ui.screens.AuthScreen
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.CreatorDashboardScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.MarketplaceScreen
import com.example.ui.screens.FeedScreen
import com.example.ui.screens.FriendsScreen
import com.example.ui.screens.GroupDetailScreen
import com.example.ui.screens.GroupsScreen
import com.example.ui.screens.MenuScreen
import com.example.ui.screens.MessagesScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.PhotosScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ReelsScreen
import com.example.ui.screens.SavedScreen
import com.example.ui.components.UserInterestAnalysisModal
import com.example.recommendation.UserInterestTracker
import com.example.notifications.NotificationHelper
import com.example.ui.theme.MeskotTheme
import com.example.ui.theme.Paper

class MainActivity : ComponentActivity() {
    companion object {
        const val PERMISSIONS_REQUEST_CODE = 101
    }

    private var pendingNotificationTarget by mutableStateOf<Pair<String, String>?>(null)

    fun requestCallPermissions() {
        val permissions = arrayOf(
            android.Manifest.permission.CAMERA,
            android.Manifest.permission.RECORD_AUDIO
        )
        androidx.core.app.ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        CallAudioManager.init(applicationContext)
        NotificationHelper.createNotificationChannel(applicationContext)
        extractNotificationIntent(intent)

        val repository = MeskotRepository(applicationContext)

        setContent {
            val viewModel = remember { MeskotViewModel(repository) }
            val target = pendingNotificationTarget

            LaunchedEffect(target) {
                if (target != null) {
                    val (targetType, targetId) = target
                    when (targetType) {
                        "chat", "message" -> {
                            if (targetId.isNotBlank()) {
                                viewModel.openChatByUid(targetId)
                            } else {
                                viewModel.navigateTo(ScreenTab.MESSAGES)
                            }
                        }
                        "friends", "friend_request" -> viewModel.navigateTo(ScreenTab.FRIENDS)
                        "notifications" -> viewModel.navigateTo(ScreenTab.NOTIFICATIONS)
                        else -> viewModel.navigateTo(ScreenTab.FEED)
                    }
                    pendingNotificationTarget = null
                }
            }

            MeskotTheme {
                MeskotApp(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: android.content.Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        extractNotificationIntent(intent)
    }

    private fun extractNotificationIntent(intent: android.content.Intent?) {
        val targetType = intent?.getStringExtra(NotificationHelper.EXTRA_TARGET_TYPE)
        val targetId = intent?.getStringExtra(NotificationHelper.EXTRA_TARGET_ID) ?: ""
        if (!targetType.isNullOrBlank()) {
            pendingNotificationTarget = targetType to targetId
            intent.removeExtra(NotificationHelper.EXTRA_TARGET_TYPE)
            intent.removeExtra(NotificationHelper.EXTRA_TARGET_ID)
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    androidx.compose.material3.Text(text = "Hello $name!", modifier = modifier)
}

@Composable
fun MeskotApp(viewModel: MeskotViewModel) {
    val currentUser by viewModel.currentUser.collectAsState()
    val currentTab by viewModel.currentTab.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()

    val users by viewModel.users.collectAsState()
    val friendsSet by viewModel.friends.collectAsState()
    val incomingReqs by viewModel.incomingRequests.collectAsState()
    val outgoingReqs by viewModel.outgoingRequests.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val albums by viewModel.albums.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val savedPostIds by viewModel.savedPostIds.collectAsState()
    val subscribedPostIds by viewModel.subscribedPostIds.collectAsState()
    val followingUids by viewModel.followingUids.collectAsState()
    val allComments by viewModel.allComments.collectAsState()
    val feedPosts by viewModel.feedPosts.collectAsState()
    val allPosts by viewModel.posts.collectAsState()
    val reelReactions by viewModel.reelReactions.collectAsState()

    val unreadNotifsCount by viewModel.unreadNotifsCount.collectAsState()
    val unreadMsgCount by viewModel.unreadMsgCount.collectAsState()

    val viewingUser by viewModel.viewingUser.collectAsState()
    val chattingWithUser by viewModel.chattingWithUser.collectAsState()
    val viewingGroup by viewModel.viewingGroup.collectAsState()
    val viewingAlbum by viewModel.viewingAlbum.collectAsState()

    val isComposerOpen by viewModel.isComposerOpen.collectAsState()
    val activeCall by viewModel.activeCall.collectAsState()
    val incomingCall by viewModel.incomingCall.collectAsState()
    val incomingMessageAlert by viewModel.incomingMessageAlert.collectAsState()
    val tippingPost by viewModel.tippingPost.collectAsState()
    val activeChapaSession by viewModel.activeChapaSession.collectAsState()
    val isChapaDepositOpen by viewModel.isChapaDepositOpen.collectAsState()
    val chapaConfig by viewModel.chapaConfig.collectAsState()
    val boostingPost by viewModel.boostingPost.collectAsState()
    val subscribingToCreator by viewModel.subscribingToCreator.collectAsState()
    val postMenuTarget by viewModel.postMenuTarget.collectAsState()
    val editingPost by viewModel.editingPost.collectAsState()
    val isEditProfileOpen by viewModel.isEditProfileOpen.collectAsState()
    val isCreateStoryOpen by viewModel.isCreateStoryOpen.collectAsState()
    val activeStoryToView by viewModel.activeStoryToView.collectAsState()
    val allStories by viewModel.stories.collectAsState()
    val isCreateReelOpen by viewModel.isCreateReelOpen.collectAsState()
    val activeReelToView by viewModel.activeReelToView.collectAsState()
    val isLiveStreamOpen by viewModel.isLiveStreamOpen.collectAsState()
    val currentLiveSession by viewModel.currentLiveSession.collectAsState()
    val currentLiveMessages by viewModel.currentLiveMessages.collectAsState()
    val isInterestModalOpen by viewModel.isInterestModalOpen.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    val activity = context as? android.app.Activity
    val clipboardManager = androidx.compose.ui.platform.LocalClipboardManager.current

    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Handled gracefully */ }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* Handled gracefully */ }

    // Request Android 13+ (API 33+) POST_NOTIFICATIONS permission once logged in
    LaunchedEffect(currentUser?.uid) {
        if (currentUser != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (!NotificationHelper.hasNotificationPermission(context)) {
                notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    // Dispatch Facebook-style rich push notification when a new message arrives
    LaunchedEffect(incomingMessageAlert) {
        val msg = incomingMessageAlert ?: return@LaunchedEffect
        val sender = users.find { it.uid == msg.fromUid }
        val senderName = sender?.displayName?.takeIf { it.isNotBlank() } ?: "Meskot Member"
        val senderAvatar = sender?.photoUrl ?: ""
        NotificationHelper.showSocialPushNotificationAsync(
            context = context,
            notificationId = (msg.fromUid.hashCode() and 0x7FFFFFFF),
            senderName = senderName,
            messageBody = msg.text.ifBlank { "Sent you an attachment" },
            senderAvatarUrl = senderAvatar,
            timestampMs = msg.createdAt,
            targetType = "chat",
            targetId = msg.fromUid
        )
        viewModel.clearIncomingMessageAlert()
    }

    // Step 2: Request runtime permissions before opening the call screen
    LaunchedEffect(activeCall) {
        if (activeCall != null) {
            val permissions = arrayOf(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.RECORD_AUDIO
            )
            activity?.let {
                androidx.core.app.ActivityCompat.requestPermissions(it, permissions, 101)
            } ?: run {
                callPermissionLauncher.launch(permissions)
            }
        }
    }

    val userMessage by viewModel.userMessage.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearUserMessage()
        }
    }

    // Facebook-style Android system back navigation:
    // Always enabled when logged in so pressing Back returns to the previous screen and refreshes,
    // or scrolls to top & refreshes the Feed instead of exiting the app.
    BackHandler(enabled = currentUser != null) {
        viewModel.navigateBack()
    }

    // Facebook-style startup splash animation when Meskot opens
    var showStartupSplash by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(2200L)
        showStartupSplash = false
    }

    Box(modifier = Modifier.fillMaxSize()) {
    if (currentUser == null) {
        AuthScreen(
            viewModel = viewModel,
            currentLanguage = currentLanguage
        )
    } else {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = { SnackbarHost(snackbarHostState) },
            topBar = {
                if (currentTab != ScreenTab.PROFILE && currentTab != ScreenTab.CHAT && currentTab != ScreenTab.ADS_MANAGER && currentTab != ScreenTab.CREATOR_STUDIO && currentTab != ScreenTab.GROUP_DETAIL && currentTab != ScreenTab.GROUP_ADMIN_DASHBOARD && currentTab != ScreenTab.MARKETPLACE) {
                    Column {
                        TopNavBar(
                            currentUser = currentUser,
                            currentLanguage = currentLanguage,
                            onToggleLanguage = { viewModel.toggleLanguage() },
                            onOpenComposer = { viewModel.openComposer() },
                            onOpenLive = { viewModel.openLiveStream() },
                            onOpenSearch = { viewModel.navigateTo(ScreenTab.FRIENDS) },
                            onOpenMenu = { viewModel.navigateTo(ScreenTab.MENU) },
                            onProfileClick = { currentUser?.let { viewModel.openProfile(it) } },
                            onLogout = { viewModel.logout() }
                        )

                        // Web App's exact Icon Nav Bar with Badges
                        IconNavBar(
                            currentTab = currentTab,
                            unreadReqCount = incomingReqs.size,
                            unreadMsgCount = unreadMsgCount,
                            unreadNotifCount = unreadNotifsCount,
                            isAdmin = currentUser?.isAdmin == true,
                            onTabSelected = { viewModel.navigateTo(it) }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(Paper)
            ) {
                // Navigation Screen Router
                when (currentTab) {
                    ScreenTab.FEED -> {
                        val friendsList = users.filter { friendsSet.contains(it.uid) }
                        FeedScreen(
                            viewModel = viewModel,
                            currentUser = currentUser,
                            feedPosts = feedPosts,
                            friends = friendsList,
                            currentLanguage = currentLanguage
                        )
                    }

                    ScreenTab.FRIENDS -> {
                        FriendsScreen(
                            viewModel = viewModel,
                            currentUser = currentUser,
                            allUsers = users,
                            friendUids = friendsSet,
                            incomingRequests = incomingReqs,
                            outgoingRequests = outgoingReqs,
                            currentLanguage = currentLanguage
                        )
                    }

                    ScreenTab.MESSAGES -> {
                        MessagesScreen(
                            viewModel = viewModel,
                            currentUser = currentUser,
                            allUsers = users,
                            friendUids = friendsSet,
                            currentLanguage = currentLanguage
                        )
                    }

                    ScreenTab.CHAT -> {
                        chattingWithUser?.let { other ->
                            val liveRecipient = users.find { it.uid == other.uid } ?: other
                            ChatScreen(
                                viewModel = viewModel,
                                currentUser = currentUser,
                                recipient = liveRecipient,
                                currentLanguage = currentLanguage
                            )
                        } ?: viewModel.navigateTo(ScreenTab.MESSAGES)
                    }

                    ScreenTab.GROUPS -> {
                        GroupsScreen(
                            viewModel = viewModel,
                            groups = groups,
                            currentLanguage = currentLanguage
                        )
                    }

                    ScreenTab.GROUP_DETAIL -> {
                        viewingGroup?.let { group ->
                            GroupDetailScreen(
                                viewModel = viewModel,
                                group = group,
                                currentUser = currentUser,
                                currentLanguage = currentLanguage
                            )
                        } ?: viewModel.navigateTo(ScreenTab.GROUPS)
                    }

                    ScreenTab.GROUP_ADMIN_DASHBOARD -> {
                        viewingGroup?.let { group ->
                            AdminDashboardScreen(
                                viewModel = viewModel,
                                group = group,
                                currentLanguage = currentLanguage,
                                onBack = { viewModel.navigateBack() }
                            )
                        } ?: viewModel.navigateTo(ScreenTab.GROUPS)
                    }

                    ScreenTab.PHOTOS -> {
                        PhotosScreen(
                            viewModel = viewModel,
                            albums = albums,
                            currentLanguage = currentLanguage
                        )
                    }

                    ScreenTab.REELS -> {
                        ReelsScreen(
                            viewModel = viewModel,
                            posts = feedPosts,
                            currentUser = currentUser,
                            currentLanguage = currentLanguage
                        )
                    }

                    ScreenTab.ALBUM_DETAIL -> {
                        viewingAlbum?.let { album ->
                            AlbumDetailScreen(
                                viewModel = viewModel,
                                album = album,
                                currentLanguage = currentLanguage
                            )
                        } ?: viewModel.navigateTo(ScreenTab.PHOTOS)
                    }

                    ScreenTab.NOTIFICATIONS -> {
                        NotificationsScreen(
                            viewModel = viewModel,
                            notifications = notifications,
                            currentLanguage = currentLanguage
                        )
                    }

                    ScreenTab.DASHBOARD -> {
                        val myPosts = feedPosts.filter { it.uid == currentUser?.uid }
                        DashboardScreen(
                            viewModel = viewModel,
                            currentUser = currentUser,
                            myPosts = myPosts,
                            friendsCount = friendsSet.size,
                            currentLanguage = currentLanguage
                        )
                    }

                    ScreenTab.ADMIN -> {
                        AdminScreen(
                            viewModel = viewModel,
                            users = users,
                            posts = feedPosts,
                            groups = groups,
                            currentLanguage = currentLanguage
                        )
                    }

                    ScreenTab.SAVED -> {
                        val savedList = feedPosts.filter { savedPostIds.contains(it.id) }
                        SavedScreen(
                            viewModel = viewModel,
                            currentUser = currentUser,
                            savedPosts = savedList,
                            currentLanguage = currentLanguage
                        )
                    }

                    ScreenTab.PROFILE -> {
                        val baseTarget = viewingUser ?: currentUser
                        val targetUser = baseTarget?.let { t ->
                            if (t.uid == currentUser?.uid) currentUser else (users.find { it.uid == t.uid } ?: t)
                        }
                        if (targetUser != null) {
                            val userPosts = feedPosts.filter { it.uid == targetUser.uid }
                            ProfileScreen(
                                viewModel = viewModel,
                                user = targetUser,
                                currentUser = currentUser,
                                userPosts = userPosts,
                                friendUids = friendsSet,
                                currentLanguage = currentLanguage,
                                allUsers = users
                            )
                        } else {
                            viewModel.navigateTo(ScreenTab.FEED)
                        }
                    }

                    ScreenTab.MENU -> {
                        MenuScreen(
                            viewModel = viewModel,
                            currentUser = currentUser,
                            allUsers = users,
                            currentLanguage = currentLanguage
                        )
                    }

                    ScreenTab.ADS_MANAGER -> {
                        AdsManagerScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    ScreenTab.CREATOR_STUDIO -> {
                        CreatorDashboardScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    ScreenTab.MARKETPLACE -> {
                        MarketplaceScreen(
                            viewModel = viewModel,
                            onBack = { viewModel.navigateBack() }
                        )
                    }

                    ScreenTab.LIVE -> {
                        val friendsList = users.filter { friendsSet.contains(it.uid) }
                        FeedScreen(
                            viewModel = viewModel,
                            currentUser = currentUser,
                            feedPosts = feedPosts,
                            friends = friendsList,
                            currentLanguage = currentLanguage
                        )
                        LaunchedEffect(Unit) {
                            viewModel.openLiveStream()
                        }
                    }
                }

                // Global Modals & Overlays
                if (isCreateStoryOpen && currentUser != null) {
                    CreateStoryDialog(
                        currentUser = currentUser!!,
                        currentLanguage = currentLanguage,
                        onDismiss = { viewModel.closeCreateStory() },
                        onSubmitStory = { mediaUrl, caption, filterName, expirationHours ->
                            viewModel.createStory(
                                mediaUrl = mediaUrl,
                                caption = caption,
                                filterName = filterName,
                                expirationHours = expirationHours,
                                onComplete = { success ->
                                    if (success) {
                                        viewModel.showMessage("Story posted! Expiring in ${expirationHours}h 🔥")
                                    } else {
                                        viewModel.showMessage("Could not post story. Please try again.")
                                    }
                                }
                            )
                        }
                    )
                }

                activeStoryToView?.let { story ->
                    val liveStory = allStories.find { it.id == story.id } ?: story
                    val storyComments = allComments[liveStory.id] ?: viewModel.getComments(liveStory.id)
                    StoryViewerDialog(
                        story = liveStory,
                        allStories = allStories,
                        currentUser = currentUser,
                        users = users,
                        comments = storyComments,
                        onDismiss = { viewModel.closeStoryViewer() },
                        onSelectStory = { nextStory -> viewModel.viewStory(nextStory) },
                        onDeleteStory = { storyId ->
                            viewModel.deleteStory(storyId)
                            viewModel.showMessage("Story deleted.")
                        },
                        onToggleLike = { storyId ->
                            viewModel.toggleStoryLike(storyId)
                        },
                        onReactStory = { storyId, emoji ->
                            viewModel.reactToStory(storyId, emoji)
                        },
                        onAddComment = { storyId, commentText ->
                            viewModel.addComment(storyId, commentText)
                        },
                        onReplyStory = { targetStory, replyText ->
                            viewModel.replyToStory(targetStory, replyText)
                        },
                        onToggleCommentLike = { storyId, commentId ->
                            viewModel.toggleCommentLike(storyId, commentId)
                        },
                        onDeleteComment = { storyId, commentId ->
                            viewModel.deleteComment(storyId, commentId)
                        }
                    )
                }

                if (isCreateReelOpen && currentUser != null) {
                    CreateReelDialog(
                        currentUser = currentUser!!,
                        currentLanguage = currentLanguage,
                        onDismiss = { viewModel.closeCreateReel() },
                        onSubmitReel = { videoUrl, caption, audioTrackTitle, thumbUrl, visibility ->
                            viewModel.submitReel(
                                videoUrl = videoUrl,
                                caption = caption,
                                audioTrackTitle = audioTrackTitle,
                                thumbnailUrl = thumbUrl,
                                visibility = visibility
                            )
                        }
                    )
                }

                activeReelToView?.let { reel ->
                    val currentReel = allPosts.find { it.id == reel.id } ?: reel
                    val reelComments = allComments[currentReel.id] ?: viewModel.getComments(currentReel.id)
                    val isFollowing = followingUids.contains(currentReel.uid)
                    val isSaved = savedPostIds.contains(currentReel.id)
                    val user = currentUser
                    val isLiked = (user != null && currentReel.reactions.containsKey(user.uid)) ||
                            (user != null && reelReactions[currentReel.id]?.containsKey(user.uid) == true)

                    ReelViewerDialog(
                        reel = currentReel,
                        currentUser = currentUser,
                        currentLanguage = currentLanguage,
                        comments = reelComments,
                        isFollowing = isFollowing,
                        isSaved = isSaved,
                        isLiked = isLiked,
                        onDismiss = { viewModel.closeReelViewer() },
                        onToggleLike = {
                            viewModel.toggleReaction(currentReel.id, "heart")
                        },
                        onToggleFollow = {
                            viewModel.toggleFollow(currentReel.uid)
                        },
                        onToggleSave = {
                            viewModel.toggleSavePost(currentReel.id)
                        },
                        onShare = {
                            viewModel.sharePost(currentReel.id)
                            val sendIntent = android.content.Intent().apply {
                                action = android.content.Intent.ACTION_SEND
                                putExtra(android.content.Intent.EXTRA_TEXT, "Watch this reel by ${currentReel.authorName} on Meskot: ${currentReel.text}")
                                type = "text/plain"
                            }
                            val shareIntent = android.content.Intent.createChooser(sendIntent, "Share Reel")
                            context.startActivity(shareIntent)
                        },
                        onTip = {
                            viewModel.openTipModal(currentReel)
                        },
                        onComment = {
                            viewModel.getComments(currentReel.id)
                        },
                        onAddComment = { text, parentId ->
                            viewModel.addComment(currentReel.id, text, parentId)
                        },
                        onToggleCommentLike = { commentId ->
                            viewModel.toggleCommentLike(currentReel.id, commentId)
                        },
                        onDeleteComment = { commentId ->
                            viewModel.deleteComment(currentReel.id, commentId)
                        },
                        onAuthorClick = { uid ->
                            val user = users.find { it.uid == uid }
                            if (user != null) {
                                viewModel.closeReelViewer()
                                viewModel.openProfile(user)
                            }
                        }
                    )
                }

                if (isComposerOpen && currentUser != null) {
                    ComposerDialog(
                        currentUser = currentUser!!,
                        currentLanguage = currentLanguage,
                        onDismiss = { viewModel.closeComposer() },
                        onSubmit = { text, media, bgIdx, vis, locName, lat, lng ->
                            viewModel.submitPost(
                                text = text,
                                mediaUrls = media,
                                bgColorIndex = bgIdx,
                                visibility = vis,
                                locationName = locName,
                                latitude = lat,
                                longitude = lng
                            )
                        },
                        onOpenCreateReel = {
                            viewModel.openCreateReel()
                        }
                    )
                }

                editingPost?.let { postToEdit ->
                    if (currentUser != null) {
                        ComposerDialog(
                            currentUser = currentUser!!,
                            currentLanguage = currentLanguage,
                            editingPost = postToEdit,
                            onDismiss = { viewModel.cancelEditingPost() },
                            onSubmit = { text, media, bgIdx, vis, locName, lat, lng ->
                                viewModel.savePostEdit(
                                    postId = postToEdit.id,
                                    newText = text,
                                    newMediaUrls = media,
                                    newBgColorIndex = bgIdx,
                                    newVisibility = vis,
                                    newLocationName = locName,
                                    newLatitude = lat,
                                    newLongitude = lng
                                )
                            }
                        )
                    }
                }

                postMenuTarget?.let { post ->
                    PostOptionsMenu(
                        post = post,
                        isAuthor = post.uid == currentUser?.uid,
                        isAdmin = currentUser?.isAdmin == true,
                        isSaved = savedPostIds.contains(post.id),
                        isNotifSubscribed = subscribedPostIds.contains(post.id),
                        currentLanguage = currentLanguage,
                        onDismiss = { viewModel.closePostMenu() },
                        onBoostPost = { viewModel.openBoostModal(post) },
                        onSaveToggle = { viewModel.toggleSavePost(post.id) },
                        onShare = { viewModel.sharePost(post.id) },
                        onEdit = { viewModel.startEditingPost(post) },
                        onDelete = { viewModel.deletePost(post.id) },
                        onHide = { viewModel.hidePost(post.id) },
                        onReport = { viewModel.reportPost(post.id) },
                        onInterested = {
                            UserInterestTracker.recordEvent(
                                itemId = post.id,
                                tags = post.effectiveTags(),
                                watchPercentage = 0.95,
                                dwellTimeSec = 15,
                                action = "EXPLICIT_LIKE",
                                userId = currentUser?.uid ?: "usr_current"
                            )
                            viewModel.showMessage("We'll tune your feed for more posts like this.")
                        },
                        onNotInterested = {
                            UserInterestTracker.recordEvent(
                                itemId = post.id,
                                tags = post.effectiveTags(),
                                watchPercentage = 0.05,
                                dwellTimeSec = 1,
                                action = "FAST_SKIP",
                                userId = currentUser?.uid ?: "usr_current"
                            )
                            viewModel.showMessage("We'll show fewer posts like this.")
                        },
                        onToggleNotifs = { viewModel.togglePostNotifications(post.id) },
                        onCopyText = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString(post.text))
                            viewModel.showMessage(com.example.data.MeskotStrings.get("textCopied", currentLanguage))
                        },
                        onCopyLink = {
                            clipboardManager.setText(androidx.compose.ui.text.AnnotatedString("https://meskot.app/posts/${post.id}"))
                            viewModel.showMessage(com.example.data.MeskotStrings.get("linkCopied", currentLanguage))
                        }
                    )
                }

                tippingPost?.let { post ->
                    TipModal(
                        post = post,
                        currentLanguage = currentLanguage,
                        userBalance = currentUser?.creatorNetBalance ?: 0.0,
                        userStarBalance = currentUser?.starBalance ?: 0,
                        onDismiss = { viewModel.closeTipModal() },
                        onConfirmTip = { amount, payFromBalance, currencyCode -> viewModel.confirmTip(amount, payFromBalance, currencyCode) },
                        onSendStars = { count, gift -> viewModel.sendStars(post.id, count, gift) },
                        onDepositClick = {
                            viewModel.closeTipModal()
                            viewModel.openChapaDeposit()
                        }
                    )
                }

                boostingPost?.let { post ->
                    BoostPostModal(
                        post = post,
                        currentLanguage = currentLanguage,
                        userBalance = currentUser?.creatorNetBalance ?: 0.0,
                        onDismiss = { viewModel.closeBoostModal() },
                        onConfirmBoost = { dailyBudget, duration, locations, minAge, maxAge, interests ->
                            viewModel.confirmBoost(post.id, dailyBudget, duration, locations, minAge, maxAge, interests)
                        },
                        onDepositClick = {
                            viewModel.closeBoostModal()
                            viewModel.openChapaDeposit()
                        }
                    )
                }

                if (isChapaDepositOpen) {
                    ChapaDepositModal(
                        config = chapaConfig,
                        currentLanguage = currentLanguage,
                        onDismiss = { viewModel.closeChapaDeposit() },
                        onProceed = { amount, currencyCode ->
                            viewModel.depositViaChapa(amount, currencyCode)
                        }
                    )
                }

                subscribingToCreator?.let { creator ->
                    SubscriptionModal(
                        creator = creator,
                        currentLanguage = currentLanguage,
                        currentUser = currentUser,
                        onDismiss = { viewModel.closeSubscriptionModal() },
                        onSubscribe = { tier, currencyCode ->
                            viewModel.subscribeToTier(creator.uid, tier, currencyCode)
                            viewModel.closeSubscriptionModal()
                        }
                    )
                }

                activeChapaSession?.let { session ->
                    ChapaPaymentModal(
                        amount = session.amount,
                        title = session.title,
                        txRef = session.txRef,
                        email = session.email,
                        firstName = session.firstName,
                        lastName = session.lastName,
                        initialCurrency = session.initialCurrency,
                        publicKey = session.publicKey,
                        isLiveMode = session.isLiveMode,
                        onDismiss = { viewModel.closeChapaPayment() },
                        onPaymentSuccess = { txRef ->
                            session.onPaymentCompleted(txRef)
                            viewModel.closeChapaPayment()
                        }
                    )
                }

                if (isEditProfileOpen && currentUser != null) {
                    EditProfileDialog(
                        currentUser = currentUser!!,
                        currentLanguage = currentLanguage,
                        onDismiss = { viewModel.closeEditProfile() },
                        onSave = { name, bio, photoUrl, gender, birthDate, coverPhotoUrl, profession, location, hometown, workplace, workRole, education, educationClass ->
                            viewModel.saveProfile(
                                name = name,
                                bio = bio,
                                photoUrl = photoUrl,
                                gender = gender,
                                birthDate = birthDate,
                                coverPhotoUrl = coverPhotoUrl,
                                profession = profession,
                                location = location,
                                hometown = hometown,
                                workplace = workplace,
                                workRole = workRole,
                                education = education,
                                educationClass = educationClass
                            )
                        }
                    )
                }

                if (isInterestModalOpen) {
                    UserInterestAnalysisModal(
                        userId = currentUser?.uid ?: "usr_7821",
                        onDismiss = { viewModel.closeInterestEngine() }
                    )
                }

                // Incoming Call Overlay
                if (activeCall == null) {
                    incomingCall?.let { session ->
                        IncomingCallOverlay(
                            session = session,
                            currentLanguage = currentLanguage,
                            onAccept = {
                                val permissions = arrayOf(
                                    android.Manifest.permission.CAMERA,
                                    android.Manifest.permission.RECORD_AUDIO
                                )
                                activity?.let {
                                    androidx.core.app.ActivityCompat.requestPermissions(it, permissions, 101)
                                } ?: run {
                                    callPermissionLauncher.launch(permissions)
                                }
                                viewModel.acceptIncomingCall()
                            },
                            onDecline = {
                                viewModel.declineIncomingCall()
                            }
                        )
                    }
                }

                // Active Audio / Video Call Overlay
                activeCall?.let { call ->
                    CallOverlay(
                        activeCall = call,
                        currentLanguage = currentLanguage,
                        onToggleMute = { viewModel.toggleCallMute() },
                        onToggleSpeaker = { viewModel.toggleCallSpeaker() },
                        onToggleCamera = { viewModel.toggleCallCamera() },
                        onFlipCamera = { viewModel.flipCamera() },
                        onEndCall = { viewModel.endCall() }
                    )
                }

                // Interactive Live Stream & 100 Cultural Gifts Engine connected with Firebase
                if (isLiveStreamOpen) {
                    MeskotLiveStreamModal(
                        currentUser = currentUser,
                        liveSession = currentLiveSession,
                        liveMessages = currentLiveMessages,
                        onDismiss = { viewModel.closeLiveStream() },
                        onSendMessage = { text -> viewModel.sendLiveComment(text) },
                        onSendLike = { viewModel.sendLiveHeart() },
                        onGiftSent = { giftId, giftName, giftIcon, cost ->
                            viewModel.onGiftSentFromLive(giftId, giftName, giftIcon, cost)
                        },
                        onDepositCompleted = { coins, amountEtb, txRef ->
                            viewModel.buyStarsWithChapa(coins, amountEtb, txRef)
                        },
                        onEndLive = { viewModel.closeLiveStream() }
                    )
                }
            }
        }
    }

    AnimatedVisibility(
        visible = showStartupSplash,
        enter = fadeIn(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(420))
    ) {
        MeskotSplashScreen()
    }
    }
}
