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
import com.example.util.CallAudioManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
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
    val friends: StateFlow<Set<String>> = repository.friends
    val incomingRequests: StateFlow<List<User>> = repository.incomingRequests
    val outgoingRequests: StateFlow<Set<String>> = repository.outgoingRequests
    val groups: StateFlow<List<GroupItem>> = repository.groups
    val albums: StateFlow<List<AlbumItem>> = repository.albums
    val notifications: StateFlow<List<NotificationItem>> = repository.notifications
    val conversations: StateFlow<Map<String, List<ChatMessage>>> = repository.conversations
    val savedPostIds: StateFlow<Set<String>> = repository.savedPostIds
    val incomingCall: StateFlow<CallSession?> = repository.incomingCall

    // Monetization & Ads
    val adCampaigns: StateFlow<List<AdCampaign>> = repository.adCampaigns
    val boostCampaigns: StateFlow<List<BoostCampaign>> = repository.boostCampaigns
    val payoutHistory: StateFlow<List<CreatorPayoutRecord>> = repository.payoutHistory

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

    // Chapa Payment Integration
    private val _activeChapaSession = MutableStateFlow<ChapaPaymentSession?>(null)
    val activeChapaSession: StateFlow<ChapaPaymentSession?> = _activeChapaSession.asStateFlow()

    fun launchChapaPayment(session: ChapaPaymentSession) {
        _activeChapaSession.value = session
    }

    fun closeChapaPayment() {
        _activeChapaSession.value = null
    }

    fun openTestChapaCheckout(amount: Double = 100.0) {
        val user = currentUser.value
        val fName = user?.displayName?.split(" ")?.firstOrNull() ?: "John"
        val lName = user?.displayName?.split(" ")?.drop(1)?.joinToString(" ")?.ifBlank { "Doe" } ?: "Doe"
        val email = if (user?.email?.contains("@") == true) user.email else "customer@example.com"

        launchChapaPayment(
            ChapaPaymentSession(
                amount = amount,
                title = "Meskot Test Checkout",
                email = email,
                firstName = fName,
                lastName = lName,
                onPaymentCompleted = { ref ->
                    showMessage("✅ Chapa Payment Completed! Ref: $ref · ${amount.toInt()} ETB")
                }
            )
        )
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

    fun confirmTip(amount: Double) {
        val post = _tippingPost.value ?: return
        val user = currentUser.value
        val fName = user?.displayName?.split(" ")?.firstOrNull() ?: "Meskot"
        val lName = user?.displayName?.split(" ")?.drop(1)?.joinToString(" ")?.ifBlank { "User" } ?: "User"
        val email = if (user?.email?.contains("@") == true) user.email else "customer@example.com"

        closeTipModal()

        launchChapaPayment(
            ChapaPaymentSession(
                amount = amount,
                title = "Creator Tip for ${post.authorName}",
                email = email,
                firstName = fName,
                lastName = lName,
                onPaymentCompleted = { ref ->
                    repository.sendTip(post.id, amount)
                    showMessage("✅ Tip of ${amount.toInt()} ETB sent to ${post.authorName} via Chapa! (Ref: $ref)")
                }
            )
        )
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
        repository.boostPost(postId, dailyBudgetEtb, durationDays, targetLocations, minAge, maxAge, interests)
        closeBoostModal()
        showMessage("🚀 Post promoted! Priority algorithm reach activated.")
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

        closeSubscriptionModal()

        launchChapaPayment(
            ChapaPaymentSession(
                amount = tier.monthlyPriceEtb.toDouble(),
                title = "${tier.label} for ${creator.displayName}",
                email = email,
                firstName = fName,
                lastName = lName,
                onPaymentCompleted = { ref ->
                    repository.subscribeToCreator(creator.uid, tier)
                    showMessage("🎉 Unlocked ${tier.label} for ${creator.displayName} via Chapa! (Ref: $ref)")
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
        repository.createAdCampaign(name, objective, dailyBudgetEtb, headline, primaryText, mediaUrl, ctaText, destinationUrl)
        closeCreateCampaignModal()
        showMessage("📢 Campaign \"$name\" launched into the auction pool!")
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

    fun requestPayout(method: String, amountEtb: Double) {
        repository.requestPayout(method, amountEtb)
        closePayoutModal()
        showMessage("✅ Payout request of ${amountEtb.toInt()} ETB submitted via $method!")
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

    fun sendMessage(otherUid: String, text: String) {
        if (text.isNotBlank()) {
            repository.sendMessage(otherUid, text.trim())
        }
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
    val onPaymentCompleted: (txRef: String) -> Unit = {}
)

