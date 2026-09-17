package com.example.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AlbumItem
import com.example.data.AppLanguage
import com.example.data.CallSession
import com.example.data.ChatMessage
import com.example.data.Comment
import com.example.data.GroupItem
import com.example.data.MeskotRepository
import com.example.data.NotificationItem
import com.example.data.Post
import com.example.data.User
import com.example.data.AdCampaign
import com.example.data.BoostCampaign
import com.example.data.CreatorPayoutRecord
import com.example.data.MembershipTier
import com.example.data.MonetizationTool
import com.example.data.EarningsLedgerEntry
import com.example.data.ContentFormatMetric
import com.example.data.DailyEarningsMetric
import com.example.data.CreatorPayoutAccount
import com.example.data.ChapaGatewayConfig
import com.example.data.ProgramStatus
import com.example.util.CallAudioManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class ScreenTab {
    FEED,
    FRIENDS,
    MESSAGES,
    GROUPS,
    PHOTOS,
    NOTIFICATIONS,
    DASHBOARD,
    MENU,
    SAVED,
    ADMIN,
    PROFILE,
    CHAT,
    GROUP_DETAIL,
    ALBUM_DETAIL,
    ADS_MANAGER,
    CREATOR_STUDIO
}

data class ActiveCall(
    val callId: String = "",
    val otherUser: User,
    val callType: String, // "audio" or "video"
    val isOutgoing: Boolean = false,
    val isRinging: Boolean = true,
    val durationSec: Int = 0,
    val isMuted: Boolean = false,
    val isCameraOff: Boolean = false,
    val isSpeakerOn: Boolean = false,
    val isFrontCamera: Boolean = true,
    val roomUrl: String = ""
)

class MeskotViewModel(private val repository: MeskotRepository) : ViewModel() {

    val currentLanguage: StateFlow<AppLanguage> = repository.currentLanguage
    val currentUser: StateFlow<User?> = repository.currentUser
    val users: StateFlow<List<User>> = repository.users
    val isUsersLoading: StateFlow<Boolean> = repository.isUsersLoading
    val usersError: StateFlow<String?> = repository.usersError
    val friends: StateFlow<Set<String>> = repository.friends
    val incomingRequests: StateFlow<List<User>> = repository.incomingRequests
    val outgoingRequests: StateFlow<Set<String>> = repository.outgoingRequests
    val groups: StateFlow<List<GroupItem>> = repository.groups
    val albums: StateFlow<List<AlbumItem>> = repository.albums
    val notifications: StateFlow<List<NotificationItem>> = repository.notifications
    val conversations: StateFlow<Map<String, List<ChatMessage>>> = repository.conversations
    val savedPostIds: StateFlow<Set<String>> = repository.savedPostIds
    val subscribedPostIds: StateFlow<Set<String>> = repository.subscribedPostIds
    val incomingCall: StateFlow<CallSession?> = repository.incomingCall

    // Real-time ticking state & Presence Heartbeat
    private val _tickerTimeMs = MutableStateFlow(System.currentTimeMillis())
    val tickerTimeMs: StateFlow<Long> = _tickerTimeMs.asStateFlow()

    init {
        viewModelScope.launch {
            while (isActive) {
                delay(30_000L) // tick every 30 seconds for live UI relative times
                _tickerTimeMs.value = System.currentTimeMillis()
                repository.updateCurrentUserLastSeen()
            }
        }
    }

    fun refreshPresence() {
        _tickerTimeMs.value = System.currentTimeMillis()
        repository.updateCurrentUserLastSeen()
    }

    // Monetization & Ads
    val adCampaigns: StateFlow<List<AdCampaign>> = repository.adCampaigns
    val boostCampaigns: StateFlow<List<BoostCampaign>> = repository.boostCampaigns
    val payoutHistory: StateFlow<List<CreatorPayoutRecord>> = repository.payoutHistory
    val monetizationTools: StateFlow<List<MonetizationTool>> = repository.monetizationTools
    val earningsLedger: StateFlow<List<EarningsLedgerEntry>> = repository.earningsLedger
    val contentFormatMetrics: StateFlow<List<ContentFormatMetric>> = repository.contentFormatMetrics
    val dailyEarnings: StateFlow<List<DailyEarningsMetric>> = repository.dailyEarnings
    val payoutAccounts: StateFlow<List<CreatorPayoutAccount>> = repository.payoutAccounts

    // Current screen navigation
    private val _currentTab = MutableStateFlow(ScreenTab.FEED)
    val currentTab: StateFlow<ScreenTab> = _currentTab.asStateFlow()

    // Navigation sub-targets
    private val _viewingUser = MutableStateFlow<User?>(null)
    val viewingUser: StateFlow<User?> = _viewingUser.asStateFlow()

    private val _chattingWithUser = MutableStateFlow<User?>(null)
    val chattingWithUser: StateFlow<User?> = _chattingWithUser.asStateFlow()

    private val _viewingGroup = MutableStateFlow<GroupItem?>(null)
    val viewingGroup: StateFlow<GroupItem?> = _viewingGroup.asStateFlow()

    private val _viewingAlbum = MutableStateFlow<AlbumItem?>(null)
    val viewingAlbum: StateFlow<AlbumItem?> = _viewingAlbum.asStateFlow()

    // Modals & Overlays
    private val _isComposerOpen = MutableStateFlow(false)
    val isComposerOpen: StateFlow<Boolean> = _isComposerOpen.asStateFlow()

    private val _composerGroupId = MutableStateFlow<String?>(null)
    val composerGroupId: StateFlow<String?> = _composerGroupId.asStateFlow()

    private val _activeCall = MutableStateFlow<ActiveCall?>(null)
    val activeCall: StateFlow<ActiveCall?> = _activeCall.asStateFlow()

    private val _tippingPost = MutableStateFlow<Post?>(null)
    val tippingPost: StateFlow<Post?> = _tippingPost.asStateFlow()

    private val _postMenuTarget = MutableStateFlow<Post?>(null)
    val postMenuTarget: StateFlow<Post?> = _postMenuTarget.asStateFlow()

    private val _editingPost = MutableStateFlow<Post?>(null)
    val editingPost: StateFlow<Post?> = _editingPost.asStateFlow()

    private val _isEditProfileOpen = MutableStateFlow(false)
    val isEditProfileOpen: StateFlow<Boolean> = _isEditProfileOpen.asStateFlow()

    // Monetization & Boosting Modals
    private val _boostModalPost = MutableStateFlow<Post?>(null)
    val boostModalPost: StateFlow<Post?> = _boostModalPost.asStateFlow()
    val boostingPost: StateFlow<Post?> = _boostModalPost.asStateFlow()

    private val _subscriptionModalCreator = MutableStateFlow<User?>(null)
    val subscriptionModalCreator: StateFlow<User?> = _subscriptionModalCreator.asStateFlow()
    val subscribingToCreator: StateFlow<User?> = _subscriptionModalCreator.asStateFlow()

    val creatorGrossEarnings: StateFlow<Double> = repository.currentUser.map { it?.creatorGrossEarnings ?: 0.0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val creatorNetBalance: StateFlow<Double> = repository.currentUser.map { it?.creatorNetBalance ?: 0.0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _isCreateCampaignOpen = MutableStateFlow(false)
    val isCreateCampaignOpen: StateFlow<Boolean> = _isCreateCampaignOpen.asStateFlow()

    private val _isPayoutModalOpen = MutableStateFlow(false)
    val isPayoutModalOpen: StateFlow<Boolean> = _isPayoutModalOpen.asStateFlow()

    // Chapa Payment Gateway Integration & Real Funds
    val chapaConfig: StateFlow<ChapaGatewayConfig> = repository.chapaConfig

    private val _isChapaConfigOpen = MutableStateFlow(false)
    val isChapaConfigOpen: StateFlow<Boolean> = _isChapaConfigOpen.asStateFlow()

    private val _isChapaDepositOpen = MutableStateFlow(false)
    val isChapaDepositOpen: StateFlow<Boolean> = _isChapaDepositOpen.asStateFlow()

    private val _isBuyStarsOpen = MutableStateFlow(false)
    val isBuyStarsOpen: StateFlow<Boolean> = _isBuyStarsOpen.asStateFlow()

    fun openChapaConfig() { _isChapaConfigOpen.value = true }
    fun closeChapaConfig() { _isChapaConfigOpen.value = false }

    fun openChapaDeposit() { _isChapaDepositOpen.value = true }
    fun closeChapaDeposit() { _isChapaDepositOpen.value = false }

    fun openBuyStars() { _isBuyStarsOpen.value = true }
    fun closeBuyStars() { _isBuyStarsOpen.value = false }

    private val _activeChapaSession = MutableStateFlow<ChapaPaymentSession?>(null)
    val activeChapaSession: StateFlow<ChapaPaymentSession?> = _activeChapaSession.asStateFlow()

    fun launchChapaPayment(session: ChapaPaymentSession) {
        _activeChapaSession.value = session
    }

    fun closeChapaPayment() {
        _activeChapaSession.value = null
    }

    fun depositViaChapa(amount: Double) {
        closeChapaDeposit()
        val user = currentUser.value
        val fName = user?.displayName?.split(" ")?.firstOrNull() ?: "Meskot"
        val lName = user?.displayName?.split(" ")?.drop(1)?.joinToString(" ")?.ifBlank { "User" } ?: "User"
        val email = if (user?.email?.contains("@") == true) user.email else "customer@example.com"
        val cfg = repository.chapaConfig.value
        val ref = "CHP-DEP-" + System.currentTimeMillis()

        launchChapaPayment(
            ChapaPaymentSession(
                amount = amount,
                title = "Add Real Funds (Chapa Pay)",
                txRef = ref,
                email = email,
                firstName = fName,
                lastName = lName,
                publicKey = cfg.publicKey,
                isLiveMode = cfg.isLiveMode,
                onPaymentCompleted = { completedRef ->
                    repository.depositViaChapa(amount, completedRef)
                    showMessage("✅ Successfully added ${amount.toInt()} ETB to balance via Chapa! Ref: $completedRef")
                }
            )
        )
    }

    fun buyStarsViaChapa(starCount: Int, priceEtb: Double) {
        closeBuyStars()
        val user = currentUser.value
        val fName = user?.displayName?.split(" ")?.firstOrNull() ?: "Meskot"
        val lName = user?.displayName?.split(" ")?.drop(1)?.joinToString(" ")?.ifBlank { "User" } ?: "User"
        val email = if (user?.email?.contains("@") == true) user.email else "customer@example.com"
        val cfg = repository.chapaConfig.value
        val ref = "CHP-STAR-" + System.currentTimeMillis()

        launchChapaPayment(
            ChapaPaymentSession(
                amount = priceEtb,
                title = "$starCount Stars Pack (Chapa Pay)",
                txRef = ref,
                email = email,
                firstName = fName,
                lastName = lName,
                publicKey = cfg.publicKey,
                isLiveMode = cfg.isLiveMode,
                onPaymentCompleted = { completedRef ->
                    repository.buyStarsWithChapa(starCount, priceEtb, completedRef)
                    showMessage("⭐ Added $starCount Stars to your balance via Chapa! Ref: $completedRef")
                }
            )
        )
    }

    fun updateChapaConfig(publicKey: String, secretKey: String, isLiveMode: Boolean) {
        repository.updateChapaConfig(publicKey, secretKey, isLiveMode)
        closeChapaConfig()
        val modeStr = if (isLiveMode) "LIVE" else "SANDBOX TEST"
        showMessage("🔐 Chapa Gateway set to $modeStr mode.")
    }

    fun resetToRealChapaBalance() {
        repository.resetToRealChapaBalance()
        showMessage("🧹 Reset to zero demo money. Creator balance is now 100% real Chapa funds.")
    }

    fun openTestChapaCheckout(amount: Double = 100.0) {
        depositViaChapa(amount)
    }

    // Toast message for user feedback
    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage.asStateFlow()

    fun clearUserMessage() {
        _userMessage.value = null
    }

    fun showMessage(msg: String) {
        _userMessage.value = msg
    }

    // All unfiltered posts
    val posts: StateFlow<List<Post>> = repository.posts

    // Filtered Feed posts with Algorithmic Boost Weighting
    val feedPosts: StateFlow<List<Post>> = combine(
        repository.posts,
        repository.hiddenPostIds,
        repository.currentUser,
        repository.friends
    ) { posts, hidden, user, friendsSet ->
        posts.filter { post ->
            if (hidden.contains(post.id)) return@filter false
            when (post.visibility) {
                "public" -> true
                "friends" -> post.uid == user?.uid || friendsSet.contains(post.uid)
                "onlyme" -> post.uid == user?.uid
                else -> true
            }
        }.sortedWith(
            compareByDescending<Post> { if (it.isBoosted) 1 else 0 }
                .thenByDescending { if (it.isBoosted) it.createdAt + (it.boostMultiplier * 3600000).toLong() else it.createdAt }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotifsCount: StateFlow<Int> = combine(repository.notifications) { notifs ->
        notifs[0].count { !it.isRead }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val unreadMsgCount: StateFlow<Int> = repository.unreadMsgCount

    val incomingMessageAlert: StateFlow<ChatMessage?> = repository.incomingMessageAlert

    fun clearIncomingMessageAlert() {
        repository.clearIncomingMessageAlert()
    }

    fun markConversationAsRead(otherUid: String) {
        repository.markConversationAsRead(otherUid)
    }

    // Navigation
    fun navigateTo(tab: ScreenTab) {
        _currentTab.value = tab
    }

    fun openProfile(user: User) {
        _viewingUser.value = user
        _currentTab.value = ScreenTab.PROFILE
    }

    fun openProfileByUid(uid: String) {
        val user = repository.users.value.find { it.uid == uid } ?: return
        openProfile(user)
    }

    fun openChat(user: User) {
        _chattingWithUser.value = user
        _currentTab.value = ScreenTab.CHAT
        repository.markConversationAsRead(user.uid)
        clearIncomingMessageAlert()
    }

    fun openChatByUid(uid: String) {
        val user = repository.users.value.find { it.uid == uid } ?: User(uid = uid, displayName = "Meskot User", bio = "")
        openChat(user)
    }

    fun openGroupDetail(group: GroupItem) {
        _viewingGroup.value = group
        _currentTab.value = ScreenTab.GROUP_DETAIL
    }

    fun openAlbumDetail(album: AlbumItem) {
        _viewingAlbum.value = album
        _currentTab.value = ScreenTab.ALBUM_DETAIL
    }

    // Composer
    fun openComposer(groupId: String? = null) {
        _composerGroupId.value = groupId
        _isComposerOpen.value = true
    }

    fun closeComposer() {
        _isComposerOpen.value = false
        _composerGroupId.value = null
    }

    fun submitPost(text: String, mediaUrls: List<String>, bgColorIndex: Int, visibility: String) {
        val gid = _composerGroupId.value
        if (gid != null) {
            repository.createGroupPost(gid, text, mediaUrls)
        } else {
            repository.createPost(text, mediaUrls, bgColorIndex, visibility)
        }
        closeComposer()
        showMessage("Posted successfully")
    }

    // Post Actions
    fun toggleReaction(postId: String, reactionType: String) {
        repository.toggleReaction(postId, reactionType)
    }

    fun sharePost(postId: String) {
        repository.sharePost(postId)
        showMessage("Post shared to your feed")
    }

    fun toggleSavePost(postId: String) {
        val wasSaved = repository.savedPostIds.value.contains(postId)
        repository.toggleSavePost(postId)
        showMessage(if (wasSaved) "Removed from Saved" else "Saved to your bookmarks")
    }

    fun togglePostNotifications(postId: String) {
        val isNowSubscribed = repository.togglePostNotifications(postId)
        showMessage(if (isNowSubscribed) "🔔 Notifications turned on for this post" else "🔕 Notifications turned off for this post")
    }

    fun hidePost(postId: String) {
        repository.hidePost(postId)
        showMessage("Post hidden")
    }

    fun reportPost(postId: String) {
        showMessage("Thank you. Post reported for review.")
    }

    fun openPostMenu(post: Post) {
        _postMenuTarget.value = post
    }

    fun closePostMenu() {
        _postMenuTarget.value = null
    }

    fun startEditingPost(post: Post) {
        _editingPost.value = post
        closePostMenu()
    }

    fun cancelEditingPost() {
        _editingPost.value = null
    }

    fun savePostEdit(postId: String, newText: String) {
        repository.editPost(postId, newText)
        _editingPost.value = null
        showMessage("Post updated")
    }

    fun deletePost(postId: String) {
        repository.deletePost(postId)
        closePostMenu()
        showMessage("Post deleted")
    }

    // Comments
    fun getComments(postId: String): List<Comment> {
        return repository.getCommentsForPost(postId)
    }

    fun addComment(postId: String, text: String, parentId: String? = null) {
        if (text.isNotBlank()) {
            repository.addComment(postId, text.trim(), parentId)
        }
    }

    fun toggleCommentLike(postId: String, commentId: String) {
        repository.toggleCommentLike(postId, commentId)
    }

    fun deleteComment(postId: String, commentId: String) {
        repository.deleteComment(postId, commentId)
    }

    // Tipping
    fun openTipModal(post: Post) {
        _tippingPost.value = post
    }

    fun closeTipModal() {
        _tippingPost.value = null
    }

    fun confirmTip(amount: Double, payFromBalance: Boolean = true) {
        val post = _tippingPost.value ?: return
        val user = currentUser.value
        val currentBalance = user?.creatorNetBalance ?: 0.0

        if (payFromBalance) {
            if (currentBalance < amount || amount <= 0) {
                showMessage("⚠️ Insufficient balance (${String.format(java.util.Locale.US, "%,.2f", currentBalance)} ETB). Required: ${amount.toInt()} ETB. Please deposit funds first.")
                return
            }
            val success = repository.sendTip(post.id, amount, payFromBalance = true)
            if (success) {
                closeTipModal()
                val remaining = (currentBalance - amount).coerceAtLeast(0.0)
                showMessage("✅ Real Tip of ${amount.toInt()} ETB sent to ${post.authorName}! Deducted from balance. Remaining: ${String.format(java.util.Locale.US, "%,.2f", remaining)} ETB.")
            } else {
                showMessage("⚠️ Insufficient balance to send tip. Please deposit funds.")
            }
        } else {
            val fName = user?.displayName?.split(" ")?.firstOrNull() ?: "Meskot"
            val lName = user?.displayName?.split(" ")?.drop(1)?.joinToString(" ")?.ifBlank { "User" } ?: "User"
            val email = if (user?.email?.contains("@") == true) user.email else "customer@example.com"
            val cfg = repository.chapaConfig.value
            val ref = "CHP-TIP-" + System.currentTimeMillis()

            closeTipModal()

            launchChapaPayment(
                ChapaPaymentSession(
                    amount = amount,
                    title = "Creator Tip for ${post.authorName}",
                    txRef = ref,
                    email = email,
                    firstName = fName,
                    lastName = lName,
                    publicKey = cfg.publicKey,
                    isLiveMode = cfg.isLiveMode,
                    onPaymentCompleted = { completedRef ->
                        repository.sendTip(post.id, amount, completedRef, payFromBalance = false)
                        showMessage("✅ Real Tip of ${amount.toInt()} ETB sent to ${post.authorName} via Chapa! (Ref: $completedRef)")
                    }
                )
            )
        }
    }

    fun sendStars(postId: String, count: Int, giftName: String) {
        val currUser = currentUser.value
        if (currUser != null && currUser.starBalance < count) {
            showMessage("⚠️ Insufficient Star balance (${currUser.starBalance} ⭐). Please top up.")
            return
        }
        repository.sendStars(postId, count, giftName)
        closeTipModal()
        showMessage("⭐ Sent $count Stars ($giftName) to creator!")
    }

    // Meskot Verified Subscription
    fun subscribeMetaVerified(paymentMethod: String = "GOOGLE_PLAY", planId: String = "meta_verified_monthly") {
        val success = repository.subscribeMetaVerified(paymentMethod, planId)
        if (success) {
            val providerName = when (paymentMethod) {
                "GOOGLE_PLAY" -> "Google Play Billing"
                "APPLE_IAP" -> "Apple App Store"
                "CHAPA" -> "Chapa Gateway"
                else -> paymentMethod
            }
            showMessage("🎉 Welcome to Meskot Verified! Golden badge activated via $providerName.")
        } else {
            showMessage("Failed to activate Meskot Verified.")
        }
    }

    fun cancelMetaVerified() {
        val success = repository.cancelMetaVerified()
        if (success) {
            showMessage("Meskot Verified subscription cancelled.")
        }
    }

    // Content Boosting (Post Promotion)
    fun openBoostModal(post: Post) {
        _boostModalPost.value = post
        closePostMenu()
    }

    fun closeBoostModal() {
        _boostModalPost.value = null
    }

    fun confirmBoost(
        postId: String,
        dailyBudgetEtb: Double,
        durationDays: Int,
        targetLocations: List<String>,
        minAge: Int,
        maxAge: Int,
        interests: List<String>
    ) {
        val totalCost = dailyBudgetEtb * durationDays
        val currentBalance = currentUser.value?.creatorNetBalance ?: 0.0

        if (currentBalance < totalCost || totalCost <= 0) {
            showMessage("⚠️ Insufficient balance (${String.format(java.util.Locale.US, "%,.2f", currentBalance)} ETB). Total required: ${totalCost.toInt()} ETB. Please deposit funds first.")
            return
        }

        val success = repository.boostPost(postId, dailyBudgetEtb, durationDays, targetLocations, minAge, maxAge, interests)
        if (success) {
            closeBoostModal()
            val remaining = (currentBalance - totalCost).coerceAtLeast(0.0)
            showMessage("🚀 Post promoted! Deducted ${totalCost.toInt()} ETB from your balance. Remaining: ${String.format(java.util.Locale.US, "%,.2f", remaining)} ETB.")
        } else {
            showMessage("⚠️ Insufficient balance to boost post. Please add funds.")
        }
    }

    // Fan Subscriptions & VIP Memberships
    fun getUserMembershipTier(creatorUid: String): MembershipTier = repository.getUserMembershipTier(creatorUid)

    fun openSubscriptionModal(creator: User) {
        _subscriptionModalCreator.value = creator
    }

    fun openSubscriptionModal(creatorUid: String) {
        val creator = users.value.find { it.uid == creatorUid }
        if (creator != null) {
            _subscriptionModalCreator.value = creator
        }
    }

    fun closeSubscriptionModal() {
        _subscriptionModalCreator.value = null
    }

    fun subscribeToTier(creatorUid: String, tier: MembershipTier) = subscribeToCreator(creatorUid, tier)

    fun subscribeToCreator(creatorUid: String, tier: MembershipTier) {
        val creator = users.value.find { it.uid == creatorUid } ?: _subscriptionModalCreator.value
        if (creator != null) {
            initiateSubscriptionPayment(creator, tier)
        } else {
            repository.subscribeToCreator(creatorUid, tier)
            closeSubscriptionModal()
            showMessage("🎉 Congratulations! You unlocked ${tier.label} membership.")
        }
    }

    fun initiateSubscriptionPayment(creator: User, tier: MembershipTier) {
        val user = currentUser.value
        val fName = user?.displayName?.split(" ")?.firstOrNull() ?: "Meskot"
        val lName = user?.displayName?.split(" ")?.drop(1)?.joinToString(" ")?.ifBlank { "User" } ?: "User"
        val email = if (user?.email?.contains("@") == true) user.email else "customer@example.com"
        val cfg = repository.chapaConfig.value
        val ref = "CHP-SUB-" + System.currentTimeMillis()

        closeSubscriptionModal()

        launchChapaPayment(
            ChapaPaymentSession(
                amount = tier.monthlyPriceEtb.toDouble(),
                title = "${tier.label} for ${creator.displayName}",
                txRef = ref,
                email = email,
                firstName = fName,
                lastName = lName,
                publicKey = cfg.publicKey,
                isLiveMode = cfg.isLiveMode,
                onPaymentCompleted = { completedRef ->
                    repository.subscribeToCreator(creator.uid, tier, completedRef)
                    showMessage("🎉 Unlocked ${tier.label} for ${creator.displayName} via Chapa! (Ref: $completedRef)")
                }
            )
        )
    }

    // Ads Manager & Campaigns
    fun openCreateCampaignModal() {
        _isCreateCampaignOpen.value = true
    }

    fun closeCreateCampaignModal() {
        _isCreateCampaignOpen.value = false
    }

    fun createAdCampaign(
        name: String,
        objective: String,
        dailyBudgetEtb: Double,
        headline: String,
        primaryText: String,
        mediaUrl: String,
        ctaText: String,
        destinationUrl: String
    ) {
        val currentBalance = currentUser.value?.creatorNetBalance ?: 0.0
        if (currentBalance < dailyBudgetEtb || dailyBudgetEtb <= 0) {
            showMessage("⚠️ Insufficient balance (${String.format(java.util.Locale.US, "%,.2f", currentBalance)} ETB). Required daily budget: ${dailyBudgetEtb.toInt()} ETB. Please deposit funds first.")
            return
        }
        val success = repository.createAdCampaign(name, objective, dailyBudgetEtb, headline, primaryText, mediaUrl, ctaText, destinationUrl)
        if (success) {
            closeCreateCampaignModal()
            val remaining = (currentBalance - dailyBudgetEtb).coerceAtLeast(0.0)
            showMessage("📢 Campaign \"$name\" launched! Deducted ${dailyBudgetEtb.toInt()} ETB from your balance. Remaining: ${String.format(java.util.Locale.US, "%,.2f", remaining)} ETB.")
        } else {
            showMessage("⚠️ Insufficient balance to launch campaign.")
        }
    }

    fun toggleAdCampaignStatus(campaignId: String) {
        repository.toggleAdCampaignStatus(campaignId)
    }

    // Creator Payouts
    fun openPayoutModal() {
        _isPayoutModalOpen.value = true
    }

    fun closePayoutModal() {
        _isPayoutModalOpen.value = false
    }

    fun requestPayout(method: String, amountEtb: Double, destinationAccount: String = "") {
        repository.requestPayout(method, amountEtb, destinationAccount)
        closePayoutModal()
        showMessage("💸 Payout request of ${amountEtb.toInt()} ETB submitted via $method! Transferred through Chapa rails.")
    }

    fun applyForMonetizationTool(toolId: String, payoutMethod: String = "Telebirr", accountNumber: String = ""): Boolean {
        val res = repository.applyForMonetizationTool(toolId, payoutMethod, accountNumber)
        if (res) {
            showMessage("✅ Program Onboarding Complete! Payout connected via $payoutMethod.")
        }
        return res
    }

    // REAL-TIME AUDIENCE & WATCH TIME ENGINE
    val followingUids: StateFlow<Set<String>> = repository.followingUids

    fun toggleFollow(targetUid: String) {
        val isNowFollowing = !repository.isFollowing(targetUid)
        repository.toggleFollow(targetUid)
        if (isNowFollowing) {
            showMessage("👤 Following creator! You'll see their latest posts and reels.")
        } else {
            showMessage("Unfollowed.")
        }
    }

    fun isFollowing(targetUid: String): Boolean = repository.isFollowing(targetUid)

    fun incrementFollowers(count: Int = 1) {
        repository.incrementFollowers(count)
        showMessage("📈 Followers updated! +$count followers gained.")
    }

    fun incrementWatchHours(hours: Double = 1.0) {
        repository.incrementWatchHours(hours)
        showMessage("⏱️ Watch time updated! +${String.format(java.util.Locale.US, "%.1f", hours)} hrs accumulated.")
    }

    fun recordWatchTime(seconds: Long, creatorUid: String? = null) {
        repository.recordWatchTime(seconds, creatorUid)
    }

    fun syncPartnerMetrics() {
        repository.syncPartnerMetrics()
        showMessage("✅ Partner Program standing synced with real live audience metrics!")
    }

    fun redeemInviteCode(toolId: String, code: String): Boolean {
        val success = repository.redeemInviteCode(toolId, code)
        if (success) {
            showMessage("🎉 Program Unlocked! 500 ETB Activation Bonus credited to your ledger.")
        } else {
            showMessage("❌ Invalid invite code. Check your token and try again.")
        }
        return success
    }

    fun registerInterest(toolId: String) {
        repository.registerInterest(toolId)
        showMessage("🌟 Interest registered! You'll be notified when program capacity expands.")
    }

    fun simulateDailySettlementCron() {
        repository.simulateDailySettlementCron()
        showMessage("⚡ Automated daily settlement completed: +280.00 ETB credited to ledger!")
    }

    // Friends
    fun sendFriendRequest(user: User) {
        repository.sendFriendRequest(user.uid)
        showMessage("Friend request sent to ${user.displayName}")
    }

    fun cancelFriendRequest(user: User) {
        repository.cancelFriendRequest(user.uid)
    }

    fun acceptFriendRequest(user: User) {
        repository.acceptFriendRequest(user.uid)
        showMessage("Accepted ${user.displayName}'s request")
    }

    fun declineFriendRequest(user: User) {
        repository.declineFriendRequest(user.uid)
    }

    fun unfriend(user: User) {
        repository.unfriend(user.uid)
        showMessage("Removed from friends")
    }

    // Messages
    fun getMessagesForUser(otherUid: String): List<ChatMessage> {
        return repository.getMessages(otherUid)
    }

    fun sendMessage(
        otherUid: String,
        text: String,
        mediaUrl: String? = null,
        mediaType: String? = null,
        fileName: String? = null,
        fileSize: String? = null
    ) {
        if (text.isNotBlank() || !mediaUrl.isNullOrBlank() || mediaType != null) {
            repository.sendMessage(
                otherUid = otherUid,
                text = text.trim(),
                mediaUrl = mediaUrl,
                mediaType = mediaType,
                fileName = fileName,
                fileSize = fileSize
            )
        }
    }

    fun clearConversation(otherUid: String) {
        repository.clearConversation(otherUid)
        showMessage("Conversation cleared")
    }

    fun deleteMessage(otherUid: String, msgId: String) {
        repository.deleteMessage(otherUid, msgId)
    }

    // Audio / Video Calls
    private var callTimerJob: Job? = null

    fun startCall(otherUser: User, callType: String) {
        CallAudioManager.stopRinging()
        CallAudioManager.startOutgoingRing()

        repository.initiateCall(otherUser, callType) { session ->
            _activeCall.value = ActiveCall(
                callId = session.callId,
                otherUser = otherUser,
                callType = callType,
                isOutgoing = true,
                isRinging = true,
                roomUrl = session.roomUrl,
                isSpeakerOn = (callType == "video")
            )

            // Listen to remote changes on this call session in Firestore
            repository.listenToCallSession(session.callId) { updatedSession ->
                if (updatedSession == null) return@listenToCallSession
                when (updatedSession.status) {
                    "accepted" -> {
                        CallAudioManager.stopRinging()
                        CallAudioManager.startCommunication(isSpeakerDefault = (callType == "video"))
                        _activeCall.value = _activeCall.value?.copy(isRinging = false)
                        startCallTimer()
                    }
                    "rejected" -> {
                        CallAudioManager.stopRinging()
                        CallAudioManager.endCall()
                        _userMessage.value = "${otherUser.displayName} declined the call"
                        _activeCall.value = null
                        repository.stopListeningToCallSession()
                    }
                    "ended" -> {
                        CallAudioManager.stopRinging()
                        CallAudioManager.endCall()
                        _activeCall.value = null
                        repository.stopListeningToCallSession()
                    }
                }
            }
        }
    }

    fun acceptIncomingCall() {
        val inc = incomingCall.value ?: return
        CallAudioManager.stopRinging()
        CallAudioManager.startCommunication(isSpeakerDefault = (inc.callType == "video"))

        val callerUser = users.value.find { it.uid == inc.callerUid } ?: User(
            uid = inc.callerUid,
            displayName = inc.callerName,
            photoUrl = inc.callerPhoto
        )

        repository.acceptIncomingCall(inc.callId)

        _activeCall.value = ActiveCall(
            callId = inc.callId,
            otherUser = callerUser,
            callType = inc.callType,
            isOutgoing = false,
            isRinging = false,
            roomUrl = inc.roomUrl,
            isSpeakerOn = (inc.callType == "video")
        )
        startCallTimer()

        // Listen for caller ending the call
        repository.listenToCallSession(inc.callId) { updatedSession ->
            if (updatedSession != null && updatedSession.status == "ended") {
                CallAudioManager.endCall()
                _activeCall.value = null
                repository.stopListeningToCallSession()
            }
        }
    }

    fun declineIncomingCall() {
        val inc = incomingCall.value ?: return
        CallAudioManager.stopRinging()
        CallAudioManager.endCall()
        repository.declineIncomingCall(inc.callId)
    }

    fun dismissIncomingCall() {
        CallAudioManager.stopRinging()
        repository.dismissIncomingCall()
    }

    private fun startCallTimer() {
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (_activeCall.value != null && !_activeCall.value!!.isRinging) {
                delay(1000)
                _activeCall.value = _activeCall.value?.let { it.copy(durationSec = it.durationSec + 1) }
            }
        }
    }

    fun toggleCallMute() {
        val isMuted = CallAudioManager.toggleMicrophoneMute()
        _activeCall.value = _activeCall.value?.copy(isMuted = isMuted)
    }

    fun toggleCallSpeaker() {
        val isSpeaker = CallAudioManager.toggleSpeakerphone()
        _activeCall.value = _activeCall.value?.copy(isSpeakerOn = isSpeaker)
    }

    fun toggleCallCamera() {
        _activeCall.value = _activeCall.value?.let { it.copy(isCameraOff = !it.isCameraOff) }
    }

    fun flipCamera() {
        _activeCall.value = _activeCall.value?.let { it.copy(isFrontCamera = !it.isFrontCamera) }
    }

    fun endCall() {
        val call = _activeCall.value
        callTimerJob?.cancel()
        callTimerJob = null
        CallAudioManager.stopRinging()
        CallAudioManager.endCall()
        if (call != null) {
            repository.terminateCall(
                callId = call.callId,
                otherUid = call.otherUser.uid,
                callType = call.callType,
                durationSec = call.durationSec
            )
        }
        _activeCall.value = null
    }

    // Groups
    fun toggleGroupJoin(group: GroupItem) {
        repository.toggleGroupJoin(group.id)
    }

    fun createGroup(name: String, desc: String) {
        if (name.isNotBlank()) {
            repository.createGroup(name.trim(), desc.trim())
            showMessage("Group created")
        }
    }

    fun getGroupPosts(groupId: String): List<Post> {
        return repository.groupPosts.value[groupId] ?: emptyList()
    }

    // Albums
    fun createAlbum(title: String) {
        if (title.isNotBlank()) {
            repository.createAlbum(title.trim())
            showMessage("Album created")
        }
    }

    fun addPhotoToAlbum(albumId: String, photoUrl: String) {
        repository.addPhotoToAlbum(albumId, photoUrl)
        showMessage("Photo added to album")
    }

    // Profile Edit
    fun openEditProfile() {
        _isEditProfileOpen.value = true
    }

    fun closeEditProfile() {
        _isEditProfileOpen.value = false
    }

    fun saveProfile(
        name: String,
        bio: String,
        photoUrl: String,
        gender: String = "",
        birthDate: String = "",
        coverPhotoUrl: String = "",
        profession: String = "",
        location: String = "",
        hometown: String = "",
        workplace: String = "",
        workRole: String = "",
        education: String = "",
        educationClass: String = ""
    ) {
        repository.updateProfile(
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
        closeEditProfile()
        showMessage("Profile updated")
    }

    // Language
    fun toggleLanguage() {
        val next = if (currentLanguage.value == AppLanguage.EN) AppLanguage.AM else AppLanguage.EN
        repository.setLanguage(next)
    }

    // Notifications
    fun markAllNotifsRead() {
        repository.markAllNotificationsRead()
    }

    // Admin
    fun adminToggleSuspend(uid: String) {
        repository.adminToggleSuspend(uid)
        showMessage("Account status updated")
    }

    fun adminToggleAdmin(uid: String) {
        repository.adminToggleAdmin(uid)
        showMessage("Admin privileges updated")
    }

    fun adminDeleteGroup(groupId: String) {
        repository.adminDeleteGroup(groupId)
        showMessage("Group deleted")
    }

    // Auth
    fun login(email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        repository.login(email, pass) { ok, err ->
            if (ok) showMessage("Welcome back to Meskot!")
            onResult(ok, err)
        }
    }

    fun signup(
        fullName: String,
        email: String,
        pass: String,
        gender: String = "",
        birthDate: String = "",
        phoneNumber: String = "",
        onResult: (Boolean, String?) -> Unit
    ) {
        repository.signup(fullName, email, pass, gender, birthDate, phoneNumber) { ok, err ->
            if (ok) showMessage("Welcome to Meskot!")
            onResult(ok, err)
        }
    }

    fun signup(fullName: String, email: String, pass: String, onResult: (Boolean, String?) -> Unit) {
        signup(fullName, email, pass, "", "", "", onResult)
    }

    fun forgotPassword(email: String, onResult: (Boolean, String?) -> Unit) {
        repository.forgotPassword(email) { ok, err ->
            if (ok) showMessage("Password reset link sent to $email")
            onResult(ok, err)
        }
    }

    fun login(email: String, pass: String): Boolean {
        return repository.login(email, pass)
    }

    fun signup(fullName: String, email: String, pass: String): Boolean {
        return repository.signup(fullName, email, pass)
    }

    fun logout() {
        repository.logout()
        _currentTab.value = ScreenTab.FEED
        showMessage("Logged out")
    }

    fun switchUser(user: User) {
        repository.switchUser(user)
    }

    fun refreshUsers() {
        repository.refreshUsers()
    }
}

/**
 * Encapsulates an active Chapa Inline Checkout session in Meskot.
 * Powered by public key CHAPUBK_TEST-1PW1FKvNMh2tx4k5hHPibEZA4A6GPpRc
 */
data class ChapaPaymentSession(
    val amount: Double,
    val title: String,
    val txRef: String = "meskot-tx-" + System.currentTimeMillis(),
    val email: String = "customer@example.com",
    val firstName: String = "Meskot",
    val lastName: String = "User",
    val publicKey: String = "CHAPUBK_TEST-1PW1FKvNMh2tx4k5hHPibEZA4A6GPpRc",
    val isLiveMode: Boolean = false,
    val onPaymentCompleted: (txRef: String) -> Unit = {}
)

