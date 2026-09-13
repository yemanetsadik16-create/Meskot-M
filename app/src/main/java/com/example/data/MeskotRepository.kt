package com.example.data

import android.content.Context
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID

class MeskotRepository(private val context: Context) {

    // Language state
    private val _currentLanguage = MutableStateFlow(AppLanguage.EN)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
    }

    // Current logged-in user (null until logged in via Firebase)
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Users list (populated directly from Firebase Firestore, seeded with default friends)
    private val _users = MutableStateFlow<List<User>>(createInitialUsers())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    // Posts list (populated directly from Firebase Firestore)
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    // Comments map: postId -> List<Comment>
    private val _comments = MutableStateFlow<Map<String, List<Comment>>>(emptyMap())
    val comments: StateFlow<Map<String, List<Comment>>> = _comments.asStateFlow()

    // Friendships: Set of friend uids
    private val _friends = MutableStateFlow<Set<String>>(emptySet())
    val friends: StateFlow<Set<String>> = _friends.asStateFlow()

    // Pending incoming friend requests
    private val _incomingRequests = MutableStateFlow<List<User>>(emptyList())
    val incomingRequests: StateFlow<List<User>> = _incomingRequests.asStateFlow()

    // Pending outgoing friend requests
    private val _outgoingRequests = MutableStateFlow<Set<String>>(emptySet())
    val outgoingRequests: StateFlow<Set<String>> = _outgoingRequests.asStateFlow()

    // Groups
    private val _groups = MutableStateFlow<List<GroupItem>>(createInitialGroups())
    val groups: StateFlow<List<GroupItem>> = _groups.asStateFlow()

    // Group posts map: groupId -> List<Post>
    private val _groupPosts = MutableStateFlow<Map<String, List<Post>>>(emptyMap())
    val groupPosts: StateFlow<Map<String, List<Post>>> = _groupPosts.asStateFlow()

    // Albums
    private val _albums = MutableStateFlow<List<AlbumItem>>(emptyList())
    val albums: StateFlow<List<AlbumItem>> = _albums.asStateFlow()

    // Conversations map: otherUid -> List<ChatMessage>
    private val _conversations = MutableStateFlow<Map<String, List<ChatMessage>>>(emptyMap())
    val conversations: StateFlow<Map<String, List<ChatMessage>>> = _conversations.asStateFlow()

    // Notifications
    private val _notifications = MutableStateFlow<List<NotificationItem>>(emptyList())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // Saved post IDs
    private val _savedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val savedPostIds: StateFlow<Set<String>> = _savedPostIds.asStateFlow()

    // Hidden post IDs
    private val _hiddenPostIds = MutableStateFlow<Set<String>>(emptySet())
    val hiddenPostIds: StateFlow<Set<String>> = _hiddenPostIds.asStateFlow()

    // Monetization: Ad Campaigns
    private val _adCampaigns = MutableStateFlow<List<AdCampaign>>(createInitialAdCampaigns())
    val adCampaigns: StateFlow<List<AdCampaign>> = _adCampaigns.asStateFlow()

    // Content Boosting Campaigns
    private val _boostCampaigns = MutableStateFlow<List<BoostCampaign>>(emptyList())
    val boostCampaigns: StateFlow<List<BoostCampaign>> = _boostCampaigns.asStateFlow()

    // Creator Payout Records
    private val _payoutHistory = MutableStateFlow<List<CreatorPayoutRecord>>(createInitialPayoutHistory())
    val payoutHistory: StateFlow<List<CreatorPayoutRecord>> = _payoutHistory.asStateFlow()

    private var notifListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var incomingCallsListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var activeCallListenerRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var userMessageRegistrations: List<com.google.firebase.firestore.ListenerRegistration> = emptyList()
    private var generalMessagesRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    // Incoming call
    private val _incomingCall = MutableStateFlow<CallSession?>(null)
    val incomingCall: StateFlow<CallSession?> = _incomingCall.asStateFlow()

    // Real-time Incoming Message Alert (for heads-up banner / instant toast)
    private val _incomingMessageAlert = MutableStateFlow<ChatMessage?>(null)
    val incomingMessageAlert: StateFlow<ChatMessage?> = _incomingMessageAlert.asStateFlow()

    fun clearIncomingMessageAlert() {
        _incomingMessageAlert.value = null
    }

    // Unread tracking per sender
    private val _lastReadTimestamps = MutableStateFlow<Map<String, Long>>(emptyMap())

    fun markConversationAsRead(otherUid: String) {
        _lastReadTimestamps.value = _lastReadTimestamps.value + (otherUid to System.currentTimeMillis())
    }

    // Dynamic unread messages count
    val unreadMsgCount: StateFlow<Int> = kotlinx.coroutines.flow.combine(_conversations, _lastReadTimestamps, _currentUser) { convos, readMap, user ->
        if (user == null) return@combine 0
        val partnersWithUnread = convos.filterKeys { !it.contains("_") && it != user.uid }.count { (partnerUid, messages) ->
            val lastRead = readMap[partnerUid] ?: 0L
            messages.any { it.toUid == user.uid && it.fromUid == partnerUid && it.createdAt > lastRead && !it.isCallLog }
        }
        partnersWithUnread
    }.stateIn(kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default), kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0)

    init {
        FirebaseManager.initialize(context)
        val fbAuthUser = FirebaseManager.getCurrentFirebaseUser()
        if (fbAuthUser != null) {
            val user = User(
                uid = fbAuthUser.uid,
                displayName = fbAuthUser.displayName ?: fbAuthUser.email?.substringBefore("@") ?: "User",
                email = fbAuthUser.email ?: "",
                photoUrl = fbAuthUser.photoUrl?.toString() ?: ""
            )
            _currentUser.value = user
            setupUserSpecificListeners(user.uid)
        }
        setupFirebaseListeners()
    }

    private fun setupUserSpecificListeners(uid: String) {
        notifListenerRegistration?.remove()
        notifListenerRegistration = FirebaseManager.listenToNotifications(uid) { liveNotifs ->
            val liveIds = liveNotifs.map { it.id }.toSet()
            val remainingLocal = _notifications.value.filterNot { it.id in liveIds }
            _notifications.value = (liveNotifs + remainingLocal).sortedByDescending { it.createdAt }
        }

        incomingCallsListenerRegistration?.remove()
        incomingCallsListenerRegistration = FirebaseManager.listenToIncomingCalls(uid) { session ->
            if (_incomingCall.value?.callId != session.callId && session.status == "ringing") {
                _incomingCall.value = session
            }
        }

        // Direct real-time message streams for this user
        userMessageRegistrations.forEach { it.remove() }
        userMessageRegistrations = FirebaseManager.listenToUserMessages(uid) { liveMsgs ->
            mergeMessagesIntoConversations(liveMsgs, notifyIncoming = true)
        }
    }

    private fun setupFirebaseListeners() {
        try {
            FirebaseManager.listenToPosts { livePosts ->
                _posts.value = livePosts
            }

            FirebaseManager.listenToComments { liveComments ->
                _comments.value = liveComments
            }

            FirebaseManager.listenToUsers { liveUsers ->
                _users.value = liveUsers
                val curUid = _currentUser.value?.uid ?: FirebaseManager.getCurrentFirebaseUser()?.uid
                if (curUid != null) {
                    val matching = liveUsers.find { it.uid == curUid }
                    if (matching != null) {
                        _currentUser.value = matching
                        setupUserSpecificListeners(matching.uid)
                    }
                }
            }

            // Global real-time Messages synchronization
            generalMessagesRegistration?.remove()
            generalMessagesRegistration = FirebaseManager.listenToMessages { liveMessages ->
                mergeMessagesIntoConversations(liveMessages, notifyIncoming = true)
            }

            // Real-time Friend Requests synchronization
            FirebaseManager.listenToFriendRequests { liveRequests ->
                val currentUid = _currentUser.value?.uid
                if (currentUid != null) {
                    // Incoming pending requests
                    val incoming = liveRequests.filter { it.toUid == currentUid && it.status == "pending" }
                    val incomingUserList = incoming.map { req ->
                        _users.value.find { it.uid == req.fromUid } ?: User(
                            uid = req.fromUid,
                            displayName = req.fromName,
                            photoUrl = req.fromPhoto,
                            bio = "Meskot member"
                        )
                    }
                    _incomingRequests.value = incomingUserList

                    // Outgoing pending requests
                    val outgoing = liveRequests.filter { it.fromUid == currentUid && it.status == "pending" }
                    _outgoingRequests.value = outgoing.map { it.toUid }.toSet()

                    // Newly accepted requests involving the user
                    val accepted = liveRequests.filter {
                        (it.fromUid == currentUid || it.toUid == currentUid) && it.status == "accepted"
                    }
                    val acceptedUids = accepted.map { if (it.fromUid == currentUid) it.toUid else it.fromUid }
                    if (acceptedUids.isNotEmpty()) {
                        _friends.value = _friends.value + acceptedUids
                    }
                }
            }

            // Real-time Friendships synchronization
            FirebaseManager.listenToFriendships { liveFriendships ->
                val currentUid = _currentUser.value?.uid
                if (currentUid != null) {
                    val friendsFromPairs = liveFriendships.mapNotNull { (u1, u2) ->
                        when (currentUid) {
                            u1 -> u2
                            u2 -> u1
                            else -> null
                        }
                    }
                    if (friendsFromPairs.isNotEmpty()) {
                        _friends.value = _friends.value + friendsFromPairs
                    }
                }
            }

            // Attach user-specific notifications listener if logged in
            _currentUser.value?.uid?.let { setupUserSpecificListeners(it) }

        } catch (e: Exception) {
            android.util.Log.e("MeskotRepository", "Could not setup Firebase listeners: ${e.message}")
        }
    }

    // AUTH METHODS
    fun login(email: String, pass: String, onResult: ((Boolean, String?) -> Unit)? = null): Boolean {
        FirebaseManager.signInWithEmail(
            email = email,
            pass = pass,
            onSuccess = { fbUser ->
                _currentUser.value = fbUser
                setupUserSpecificListeners(fbUser.uid)
                setupFirebaseListeners()
                if (_users.value.none { it.uid == fbUser.uid }) {
                    _users.value = _users.value + fbUser
                }
                onResult?.invoke(true, null)
            },
            onFailure = { err ->
                onResult?.invoke(false, err)
            }
        )
        return true
    }

    fun signup(
        fullName: String,
        email: String,
        pass: String,
        gender: String = "",
        birthDate: String = "",
        phoneNumber: String = "",
        onResult: ((Boolean, String?) -> Unit)? = null
    ): Boolean {
        FirebaseManager.signUpWithEmail(
            fullName = fullName,
            email = email,
            pass = pass,
            gender = gender,
            birthDate = birthDate,
            phoneNumber = phoneNumber,
            onSuccess = { fbUser ->
                _currentUser.value = fbUser
                setupUserSpecificListeners(fbUser.uid)
                setupFirebaseListeners()
                if (_users.value.none { it.uid == fbUser.uid }) {
                    _users.value = _users.value + fbUser
                }
                onResult?.invoke(true, null)
            },
            onFailure = { err ->
                onResult?.invoke(false, err)
            }
        )
        return true
    }

    fun forgotPassword(email: String, onResult: (Boolean, String?) -> Unit) {
        if (email.isBlank()) {
            onResult(false, "Please enter your email address")
            return
        }
        FirebaseManager.sendPasswordResetEmail(
            email = email,
            onSuccess = {
                onResult(true, null)
            },
            onFailure = { err ->
                onResult(false, err)
            }
        )
    }

    fun switchUser(user: User) {
        _currentUser.value = user
        setupUserSpecificListeners(user.uid)
        setupFirebaseListeners()
    }

    fun logout() {
        notifListenerRegistration?.remove()
        notifListenerRegistration = null
        incomingCallsListenerRegistration?.remove()
        incomingCallsListenerRegistration = null
        activeCallListenerRegistration?.remove()
        activeCallListenerRegistration = null
        userMessageRegistrations.forEach { it.remove() }
        userMessageRegistrations = emptyList()
        generalMessagesRegistration?.remove()
        generalMessagesRegistration = null
        _incomingMessageAlert.value = null
        _incomingCall.value = null
        FirebaseManager.signOut()
        _currentUser.value = null
    }

    fun updateProfile(
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
        val curr = _currentUser.value ?: return
        val updated = curr.copy(
            displayName = name.ifBlank { curr.displayName },
            bio = bio,
            photoUrl = photoUrl.ifBlank { curr.photoUrl },
            gender = if (gender.isNotBlank()) gender else curr.gender,
            birthDate = if (birthDate.isNotBlank()) birthDate else curr.birthDate,
            coverPhotoUrl = if (coverPhotoUrl.isNotBlank()) coverPhotoUrl else curr.coverPhotoUrl,
            profession = if (profession.isNotBlank()) profession else curr.profession,
            location = if (location.isNotBlank()) location else curr.location,
            hometown = if (hometown.isNotBlank()) hometown else curr.hometown,
            workplace = if (workplace.isNotBlank()) workplace else curr.workplace,
            workRole = if (workRole.isNotBlank()) workRole else curr.workRole,
            education = if (education.isNotBlank()) education else curr.education,
            educationClass = if (educationClass.isNotBlank()) educationClass else curr.educationClass
        )
        _currentUser.value = updated
        _users.value = _users.value.map { if (it.uid == curr.uid) updated else it }
        FirebaseManager.saveUser(updated)
    }

    // POSTS METHODS
    fun createPost(text: String, mediaUrls: List<String> = emptyList(), bgColorIndex: Int = 0, visibility: String = "public") {
        val user = _currentUser.value ?: return
        val newPost = Post(
            id = "post_" + System.currentTimeMillis(),
            uid = user.uid,
            authorName = user.displayName,
            authorPhoto = user.photoUrl,
            text = text,
            mediaUrls = mediaUrls,
            bgColorIndex = bgColorIndex,
            visibility = visibility,
            reactions = emptyMap(),
            commentCount = 0,
            createdAt = System.currentTimeMillis()
        )
        _posts.value = listOf(newPost) + _posts.value
        FirebaseManager.createPost(newPost)
    }

    fun editPost(postId: String, newText: String) {
        _posts.value = _posts.value.map {
            if (it.id == postId) it.copy(text = newText, editedAt = System.currentTimeMillis()) else it
        }
        FirebaseManager.updatePostText(postId, newText)
    }

    fun deletePost(postId: String) {
        _posts.value = _posts.value.filterNot { it.id == postId }
        FirebaseManager.deletePost(postId)
    }

    fun toggleReaction(postId: String, reactionType: String) {
        val user = _currentUser.value ?: return
        var updatedReactionsMap: Map<String, String>? = null
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val currentReaction = post.reactions[user.uid]
                val updatedReactions = post.reactions.toMutableMap()
                if (currentReaction == reactionType) {
                    updatedReactions.remove(user.uid)
                } else {
                    updatedReactions[user.uid] = reactionType
                    if (post.uid != user.uid) {
                        addNotification(
                            fromUid = user.uid,
                            fromName = user.displayName,
                            fromPhoto = user.photoUrl,
                            type = "reaction",
                            targetId = postId,
                            reactionType = reactionType
                        )
                    }
                }
                updatedReactionsMap = updatedReactions
                post.copy(reactions = updatedReactions)
            } else post
        }
        updatedReactionsMap?.let { FirebaseManager.updatePostReactions(postId, it) }
    }

    fun sharePost(postId: String) {
        val user = _currentUser.value ?: return
        val sourcePost = _posts.value.find { it.id == postId } ?: return
        val newPost = Post(
            id = "post_" + System.currentTimeMillis(),
            uid = user.uid,
            authorName = user.displayName,
            authorPhoto = user.photoUrl,
            text = "",
            sharedPost = SharedPostPreview(
                postId = sourcePost.id,
                authorName = sourcePost.authorName,
                authorPhoto = sourcePost.authorPhoto,
                text = sourcePost.text,
                mediaUrls = sourcePost.mediaUrls,
                createdAt = sourcePost.createdAt
            ),
            createdAt = System.currentTimeMillis()
        )
        _posts.value = listOf(newPost) + _posts.value
        if (sourcePost.uid != user.uid) {
            addNotification(
                fromUid = user.uid,
                fromName = user.displayName,
                fromPhoto = user.photoUrl,
                type = "share",
                targetId = postId
            )
        }
    }

    fun toggleSavePost(postId: String) {
        val currentSaved = _savedPostIds.value.toMutableSet()
        if (currentSaved.contains(postId)) {
            currentSaved.remove(postId)
        } else {
            currentSaved.add(postId)
        }
        _savedPostIds.value = currentSaved
    }

    fun hidePost(postId: String) {
        _hiddenPostIds.value = _hiddenPostIds.value + postId
    }

    fun sendTip(postId: String, amount: Double) {
        val user = _currentUser.value ?: return
        var updatedTipTotal: Double? = null
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val newTotal = post.tipTotal + amount
                updatedTipTotal = newTotal
                post.copy(tipTotal = newTotal)
            } else post
        }
        updatedTipTotal?.let { FirebaseManager.updatePostTip(postId, it) }
        val post = _posts.value.find { it.id == postId } ?: return
        if (post.uid != user.uid) {
            addNotification(
                fromUid = user.uid,
                fromName = user.displayName,
                fromPhoto = user.photoUrl,
                type = "tip",
                targetId = postId,
                amount = amount
            )
        }
    }

    // MONETIZATION: FAN SUBSCRIPTIONS
    fun subscribeToCreator(creatorUid: String, tier: MembershipTier) {
        val user = _currentUser.value ?: return
        val updatedVip = user.vipMemberships.toMutableMap()
        updatedVip[creatorUid] = tier.code
        val updatedUser = user.copy(vipMemberships = updatedVip)
        _currentUser.value = updatedUser

        // Notify creator
        addNotification(
            fromUid = user.uid,
            fromName = user.displayName,
            fromPhoto = user.photoUrl,
            type = "vip_subscription",
            targetId = creatorUid,
            customText = "${user.displayName} joined your fan club as a ${tier.label}! 🌟"
        )

        // Update creator earnings
        _users.value = _users.value.map { u ->
            if (u.uid == creatorUid) {
                val netAdd = tier.monthlyPriceEtb * 0.80
                u.copy(
                    creatorGrossEarnings = u.creatorGrossEarnings + tier.monthlyPriceEtb,
                    creatorNetBalance = u.creatorNetBalance + netAdd
                )
            } else u
        }
    }

    fun getUserMembershipTier(creatorUid: String): MembershipTier {
        val tierCode = _currentUser.value?.vipMemberships?.get(creatorUid) ?: "FREE"
        return MembershipTier.fromCode(tierCode)
    }

    // MONETIZATION: VIRTUAL STARS & GIFTS
    fun sendStars(postId: String, starCount: Int, giftName: String) {
        val user = _currentUser.value ?: return
        if (user.starBalance < starCount) return
        val updatedUser = user.copy(starBalance = user.starBalance - starCount)
        _currentUser.value = updatedUser

        val post = _posts.value.find { it.id == postId } ?: return
        val starValueEtb = starCount * 1.5
        val newStarsTotal = post.starsTotal + starCount
        val newTipTotal = post.tipTotal + starValueEtb

        _posts.value = _posts.value.map {
            if (it.id == postId) it.copy(starsTotal = newStarsTotal, tipTotal = newTipTotal) else it
        }

        if (post.uid != user.uid) {
            addNotification(
                fromUid = user.uid,
                fromName = user.displayName,
                fromPhoto = user.photoUrl,
                type = "stars_gift",
                targetId = postId,
                amount = starValueEtb,
                customText = "${user.displayName} sent you $starCount Stars ($giftName)! ⭐"
            )
        }
    }

    // CONTENT BOOSTING (POST PROMOTION)
    fun boostPost(
        postId: String,
        dailyBudgetEtb: Double,
        durationDays: Int,
        targetLocations: List<String>,
        minAge: Int,
        maxAge: Int,
        interests: List<String>
    ) {
        val user = _currentUser.value ?: return
        val multiplier = 1.0 + (dailyBudgetEtb / 50.0).coerceAtMost(5.0)
        val totalBudget = dailyBudgetEtb * durationDays
        val estimatedReach = (dailyBudgetEtb * 120).toInt()

        val campaign = BoostCampaign(
            id = "boost_" + System.currentTimeMillis(),
            postId = postId,
            creatorUid = user.uid,
            dailyBudgetEtb = dailyBudgetEtb,
            durationDays = durationDays,
            targetLocations = targetLocations,
            minAge = minAge,
            maxAge = maxAge,
            interests = interests,
            totalBudgetEtb = totalBudget,
            estimatedReachPerDay = estimatedReach,
            boostMultiplier = multiplier
        )
        _boostCampaigns.value = listOf(campaign) + _boostCampaigns.value

        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                post.copy(
                    isBoosted = true,
                    boostMultiplier = multiplier,
                    boostDailyBudget = dailyBudgetEtb,
                    boostDaysRemaining = durationDays
                )
            } else post
        }
    }

    // CREATOR PAYOUTS
    fun requestPayout(method: String, amountEtb: Double) {
        val user = _currentUser.value ?: return
        if (user.creatorNetBalance < amountEtb || amountEtb <= 0) return

        val platformFee = amountEtb * 0.02
        val netDisbursed = amountEtb - platformFee

        val record = CreatorPayoutRecord(
            id = "payout_" + System.currentTimeMillis(),
            creatorUid = user.uid,
            amountEtb = amountEtb,
            platformFeeEtb = platformFee,
            netAmountEtb = netDisbursed,
            payoutMethod = method,
            status = "COMPLETED",
            transactionRef = "TXN-" + UUID.randomUUID().toString().take(8).uppercase()
        )

        _payoutHistory.value = listOf(record) + _payoutHistory.value
        val updatedUser = user.copy(creatorNetBalance = user.creatorNetBalance - amountEtb)
        _currentUser.value = updatedUser
    }

    // ADS MANAGER: CAMPAIGNS
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
        val user = _currentUser.value
        val advertiserName = user?.displayName ?: "Meskot Partner"
        val advertiserAvatar = user?.photoUrl ?: ""

        val newCampaign = AdCampaign(
            id = "camp_" + System.currentTimeMillis(),
            name = name,
            objective = objective,
            status = "ACTIVE",
            dailyBudgetEtb = dailyBudgetEtb,
            totalSpentEtb = 0.0,
            impressions = 0,
            clicks = 0,
            conversions = 0,
            ctr = 0.0,
            avgCpcEtb = (1.20 + Math.random() * 0.80).let { String.format(java.util.Locale.US, "%.2f", it).toDouble() },
            cpmEtb = 25.0,
            headline = headline,
            primaryText = primaryText,
            mediaUrl = mediaUrl,
            ctaText = ctaText,
            destinationUrl = destinationUrl,
            advertiserName = advertiserName,
            advertiserAvatar = advertiserAvatar
        )
        _adCampaigns.value = listOf(newCampaign) + _adCampaigns.value
    }

    fun toggleAdCampaignStatus(campaignId: String) {
        _adCampaigns.value = _adCampaigns.value.map {
            if (it.id == campaignId) {
                val newStatus = if (it.status == "ACTIVE") "PAUSED" else "ACTIVE"
                it.copy(status = newStatus)
            } else it
        }
    }

    // COMMENTS
    fun getCommentsForPost(postId: String): List<Comment> {
        return _comments.value[postId] ?: emptyList()
    }

    fun addComment(postId: String, text: String, parentId: String? = null) {
        val user = _currentUser.value ?: return
        val newComment = Comment(
            id = "comment_" + System.currentTimeMillis(),
            postId = postId,
            uid = user.uid,
            authorName = user.displayName,
            authorPhoto = user.photoUrl,
            text = text,
            parentId = parentId,
            createdAt = System.currentTimeMillis()
        )
        val currentList = _comments.value[postId] ?: emptyList()
        _comments.value = _comments.value + (postId to (currentList + newComment))
        // Increment post comment count
        _posts.value = _posts.value.map {
            if (it.id == postId) it.copy(commentCount = it.commentCount + 1) else it
        }
        FirebaseManager.addComment(newComment)
        val post = _posts.value.find { it.id == postId }
        if (post != null && post.uid != user.uid) {
            addNotification(
                fromUid = user.uid,
                fromName = user.displayName,
                fromPhoto = user.photoUrl,
                type = "comment",
                targetId = postId
            )
        }
    }

    fun toggleCommentLike(postId: String, commentId: String) {
        val user = _currentUser.value ?: return
        val commentsList = _comments.value[postId] ?: return
        _comments.value = _comments.value + (postId to commentsList.map { comment ->
            if (comment.id == commentId) {
                val updatedLikes = comment.likes.toMutableMap()
                if (updatedLikes[user.uid] == true) {
                    updatedLikes.remove(user.uid)
                } else {
                    updatedLikes[user.uid] = true
                }
                comment.copy(likes = updatedLikes)
            } else comment
        })
    }

    fun editComment(postId: String, commentId: String, newText: String) {
        val commentsList = _comments.value[postId] ?: return
        _comments.value = _comments.value + (postId to commentsList.map {
            if (it.id == commentId) it.copy(text = newText, editedAt = System.currentTimeMillis()) else it
        })
    }

    fun deleteComment(postId: String, commentId: String) {
        val commentsList = _comments.value[postId] ?: return
        _comments.value = _comments.value + (postId to commentsList.filterNot { it.id == commentId })
        _posts.value = _posts.value.map {
            if (it.id == postId) it.copy(commentCount = maxOf(0, it.commentCount - 1)) else it
        }
        FirebaseManager.deleteComment(postId, commentId)
    }

    // FRIENDS
    fun sendFriendRequest(toUid: String) {
        val user = _currentUser.value ?: return
        _outgoingRequests.value = _outgoingRequests.value + toUid

        val req = FriendRequest(
            id = "${user.uid}_$toUid",
            fromUid = user.uid,
            fromName = user.displayName,
            fromPhoto = user.photoUrl,
            toUid = toUid,
            status = "pending",
            createdAt = System.currentTimeMillis()
        )
        FirebaseManager.sendFriendRequest(req)

        val notif = NotificationItem(
            id = "notif_" + System.currentTimeMillis(),
            fromUid = user.uid,
            fromName = user.displayName,
            fromPhoto = user.photoUrl,
            toUid = toUid,
            type = "friend_request",
            text = "${user.displayName} sent you a friend request",
            targetId = user.uid,
            createdAt = System.currentTimeMillis()
        )
        FirebaseManager.sendNotification(notif)
    }

    fun cancelFriendRequest(toUid: String) {
        val user = _currentUser.value ?: return
        _outgoingRequests.value = _outgoingRequests.value - toUid
        val reqId = "${user.uid}_$toUid"
        FirebaseManager.deleteFriendRequest(reqId)
    }

    fun acceptFriendRequest(fromUid: String) {
        val user = _currentUser.value ?: return
        _friends.value = _friends.value + fromUid
        _incomingRequests.value = _incomingRequests.value.filterNot { it.uid == fromUid }

        val reqId = "${fromUid}_${user.uid}"
        FirebaseManager.updateFriendRequestStatus(reqId, "accepted")
        FirebaseManager.addFriendship(user.uid, fromUid)

        val notif = NotificationItem(
            id = "notif_" + System.currentTimeMillis(),
            fromUid = user.uid,
            fromName = user.displayName,
            fromPhoto = user.photoUrl,
            toUid = fromUid,
            type = "friend_accept",
            text = "${user.displayName} accepted your friend request",
            targetId = user.uid,
            createdAt = System.currentTimeMillis()
        )
        FirebaseManager.sendNotification(notif)
    }

    fun declineFriendRequest(fromUid: String) {
        val user = _currentUser.value ?: return
        _incomingRequests.value = _incomingRequests.value.filterNot { it.uid == fromUid }
        val reqId = "${fromUid}_${user.uid}"
        FirebaseManager.updateFriendRequestStatus(reqId, "declined")
    }

    fun unfriend(uid: String) {
        val user = _currentUser.value ?: return
        _friends.value = _friends.value - uid
        FirebaseManager.removeFriendship(user.uid, uid)
    }

    // MESSAGES & CHAT
    fun mergeMessagesIntoConversations(liveMessages: List<ChatMessage>, notifyIncoming: Boolean = true) {
        if (liveMessages.isEmpty()) return
        val currentUid = _currentUser.value?.uid
        val currentMap = _conversations.value.toMutableMap()
        var hasNewIncoming = false
        var latestIncomingMsg: ChatMessage? = null

        liveMessages.forEach { msg ->
            val convoId = if (msg.convoId.isNotBlank()) msg.convoId else {
                if (msg.fromUid < msg.toUid) "${msg.fromUid}_${msg.toUid}" else "${msg.toUid}_${msg.fromUid}"
            }

            // Check if this is a newly arrived message addressed to current user
            if (currentUid != null && msg.toUid == currentUid && msg.fromUid != currentUid && !msg.isCallLog) {
                val existingList = currentMap[msg.fromUid] ?: emptyList()
                if (existingList.none { it.id == msg.id }) {
                    hasNewIncoming = true
                    if (latestIncomingMsg == null || msg.createdAt > latestIncomingMsg!!.createdAt) {
                        latestIncomingMsg = msg
                    }
                }
            }

            fun updateList(key: String) {
                val list = currentMap[key] ?: emptyList()
                val idx = list.indexOfFirst { it.id == msg.id }
                val updated = if (idx >= 0) {
                    list.toMutableList().apply { set(idx, msg) }
                } else {
                    (list + msg).sortedBy { it.createdAt }
                }
                currentMap[key] = updated
            }

            // 1. Index by convoId
            if (convoId.isNotBlank()) {
                updateList(convoId)
            }
            // 2. Index by toUid
            if (msg.toUid.isNotBlank()) {
                updateList(msg.toUid)
            }
            // 3. Index by fromUid
            if (msg.fromUid.isNotBlank()) {
                updateList(msg.fromUid)
            }
        }

        _conversations.value = currentMap

        // Play sound and trigger heads-up incoming notification in real time
        if (hasNewIncoming && notifyIncoming && latestIncomingMsg != null) {
            com.example.util.CallAudioManager.playMessageReceivedSound(context)
            _incomingMessageAlert.value = latestIncomingMsg
        }
    }

    fun getMessages(otherUid: String): List<ChatMessage> {
        val user = _currentUser.value
        val fromDirectKey = _conversations.value[otherUid] ?: emptyList()
        if (user != null) {
            val convoKey = if (user.uid < otherUid) "${user.uid}_$otherUid" else "${otherUid}_${user.uid}"
            val fromConvoKey = _conversations.value[convoKey] ?: emptyList()
            val combined = (fromDirectKey + fromConvoKey).distinctBy { it.id }.sortedBy { it.createdAt }
            if (combined.isNotEmpty()) return combined
        }
        return fromDirectKey
    }

    fun sendMessage(otherUid: String, text: String) {
        val user = _currentUser.value ?: return
        val convoId = if (user.uid < otherUid) "${user.uid}_$otherUid" else "${otherUid}_${user.uid}"
        val newMsg = ChatMessage(
            id = "msg_" + System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().take(6),
            convoId = convoId,
            fromUid = user.uid,
            toUid = otherUid,
            text = text,
            createdAt = System.currentTimeMillis()
        )
        // Zero-latency instant local update across convoId and direct UID
        mergeMessagesIntoConversations(listOf(newMsg), notifyIncoming = false)
        com.example.util.CallAudioManager.playMessageSentSound()

        // Instant write to Firestore
        FirebaseManager.sendMessage(newMsg)

        // Instant push notification to recipient
        val notif = NotificationItem(
            id = "notif_" + System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().take(6),
            fromUid = user.uid,
            fromName = user.displayName,
            fromPhoto = user.photoUrl,
            toUid = otherUid,
            type = "message",
            text = "${user.displayName}: $text",
            targetId = user.uid,
            createdAt = System.currentTimeMillis()
        )
        FirebaseManager.sendNotification(notif)
    }

    fun logCall(otherUid: String, callType: String, callStatus: String, durationSec: Int) {
        val user = _currentUser.value ?: return
        val convoId = if (user.uid < otherUid) "${user.uid}_$otherUid" else "${otherUid}_${user.uid}"
        val newMsg = ChatMessage(
            id = "call_" + System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().take(6),
            convoId = convoId,
            fromUid = user.uid,
            toUid = otherUid,
            text = if (callType == "video") "Video call" else "Audio call",
            isCallLog = true,
            callType = callType,
            callStatus = callStatus,
            callDurationSec = durationSec,
            createdAt = System.currentTimeMillis()
        )
        mergeMessagesIntoConversations(listOf(newMsg), notifyIncoming = false)
        FirebaseManager.sendMessage(newMsg)
    }

    // CALL SIGNALING METHODS
    fun initiateCall(recipient: User, callType: String, onStarted: (CallSession) -> Unit) {
        val cur = _currentUser.value ?: return
        val callId = "call_${System.currentTimeMillis()}_${cur.uid.take(4)}_${recipient.uid.take(4)}"
        val cleanRoomId = "meskot_call_${callId.replace("_", "").replace("-", "").lowercase()}"
        val session = CallSession(
            callId = callId,
            callerUid = cur.uid,
            callerName = cur.displayName,
            callerPhoto = cur.photoUrl,
            receiverUid = recipient.uid,
            receiverName = recipient.displayName,
            receiverPhoto = recipient.photoUrl,
            callType = callType,
            status = "ringing",
            roomUrl = "https://meet.jit.si/$cleanRoomId#config.prejoinPageEnabled=false&config.startWithAudioMuted=false&config.startWithVideoMuted=${if (callType == "audio") "true" else "false"}&interfaceConfig.TOOLBAR_BUTTONS=[]",
            createdAt = System.currentTimeMillis()
        )
        FirebaseManager.createCall(session) { ok ->
            if (ok) {
                onStarted(session)
            }
        }
    }

    fun listenToCallSession(callId: String, onUpdate: (CallSession?) -> Unit) {
        activeCallListenerRegistration?.remove()
        activeCallListenerRegistration = FirebaseManager.listenToCall(callId) { session ->
            onUpdate(session)
        }
    }

    fun stopListeningToCallSession() {
        activeCallListenerRegistration?.remove()
        activeCallListenerRegistration = null
    }

    fun acceptIncomingCall(callId: String) {
        _incomingCall.value = null
        FirebaseManager.updateCallStatus(callId, "accepted", startedAt = System.currentTimeMillis())
    }

    fun declineIncomingCall(callId: String) {
        val inc = _incomingCall.value
        _incomingCall.value = null
        FirebaseManager.updateCallStatus(callId, "rejected", endedAt = System.currentTimeMillis())
        if (inc != null) {
            logCall(inc.callerUid, inc.callType, "missed", 0)
        }
    }

    fun dismissIncomingCall() {
        _incomingCall.value = null
    }

    fun terminateCall(callId: String, otherUid: String, callType: String, durationSec: Int) {
        stopListeningToCallSession()
        FirebaseManager.updateCallStatus(callId, "ended", endedAt = System.currentTimeMillis())
        logCall(
            otherUid = otherUid,
            callType = callType,
            callStatus = if (durationSec > 0) "completed" else "missed",
            durationSec = durationSec
        )
    }

    fun editMessage(otherUid: String, msgId: String, newText: String) {
        val currentMap = _conversations.value.toMutableMap()
        currentMap.keys.forEach { key ->
            val list = currentMap[key] ?: emptyList()
            if (list.any { it.id == msgId }) {
                currentMap[key] = list.map {
                    if (it.id == msgId) it.copy(text = newText, editedAt = System.currentTimeMillis()) else it
                }
            }
        }
        _conversations.value = currentMap
        FirebaseManager.updateMessageText(msgId, newText)
    }

    fun deleteMessage(otherUid: String, msgId: String) {
        val currentMap = _conversations.value.toMutableMap()
        currentMap.keys.forEach { key ->
            val list = currentMap[key] ?: emptyList()
            if (list.any { it.id == msgId }) {
                currentMap[key] = list.filterNot { it.id == msgId }
            }
        }
        _conversations.value = currentMap
        FirebaseManager.deleteMessage(msgId)
    }

    // GROUPS
    fun toggleGroupJoin(groupId: String) {
        _groups.value = _groups.value.map { g ->
            if (g.id == groupId) {
                val newJoined = !g.isJoined
                g.copy(
                    isJoined = newJoined,
                    memberCount = if (newJoined) g.memberCount + 1 else maxOf(1, g.memberCount - 1)
                )
            } else g
        }
    }

    fun createGroup(name: String, description: String) {
        val user = _currentUser.value ?: return
        val colors = listOf("#B8863A", "#8C2F39", "#2A4838", "#4A3B5C", "#335577")
        val color = colors[(_groups.value.size) % colors.size]
        val newGroup = GroupItem(
            id = "group_" + System.currentTimeMillis(),
            name = name,
            description = description,
            createdBy = user.uid,
            memberCount = 1,
            isJoined = true,
            coverColorHex = color
        )
        _groups.value = listOf(newGroup) + _groups.value
    }

    fun createGroupPost(groupId: String, text: String, mediaUrls: List<String> = emptyList()) {
        val user = _currentUser.value ?: return
        val newPost = Post(
            id = "gpost_" + System.currentTimeMillis(),
            uid = user.uid,
            authorName = user.displayName,
            authorPhoto = user.photoUrl,
            text = text,
            mediaUrls = mediaUrls,
            createdAt = System.currentTimeMillis()
        )
        val currentPosts = _groupPosts.value[groupId] ?: emptyList()
        _groupPosts.value = _groupPosts.value + (groupId to (listOf(newPost) + currentPosts))
    }

    // ALBUMS
    fun createAlbum(title: String) {
        val user = _currentUser.value ?: return
        val newAlbum = AlbumItem(
            id = "album_" + System.currentTimeMillis(),
            uid = user.uid,
            title = title,
            count = 0,
            coverUrl = "https://images.unsplash.com/photo-1547471080-7cc2caa01a7e?w=500&auto=format&fit=crop&q=80",
            photos = emptyList()
        )
        _albums.value = listOf(newAlbum) + _albums.value
    }

    fun addPhotoToAlbum(albumId: String, photoUrl: String) {
        _albums.value = _albums.value.map { album ->
            if (album.id == albumId) {
                album.copy(
                    photos = album.photos + photoUrl,
                    count = album.count + 1,
                    coverUrl = photoUrl
                )
            } else album
        }
    }

    // NOTIFICATIONS
    private fun addNotification(
        fromUid: String,
        fromName: String,
        fromPhoto: String,
        type: String,
        targetId: String? = null,
        reactionType: String? = null,
        amount: Double? = null,
        customText: String? = null
    ) {
        val notifText = customText ?: when (type) {
            "reaction" -> "$fromName reacted to your post"
            "like" -> "$fromName liked your post"
            "comment" -> "$fromName commented on your post"
            "friend_req", "friend_request" -> "$fromName sent you a friend request"
            "friend_accept" -> "$fromName accepted your friend request"
            "tip" -> "$fromName sent you a tip of ${amount?.toInt() ?: 25} ETB via Chapa!"
            "share" -> "$fromName shared your post"
            else -> "$fromName interacted with your profile"
        }

        val newNotif = NotificationItem(
            id = "notif_" + System.currentTimeMillis(),
            fromUid = fromUid,
            fromName = fromName,
            fromPhoto = fromPhoto,
            text = notifText,
            type = type,
            targetId = targetId,
            reactionType = reactionType,
            amount = amount,
            isRead = false,
            createdAt = System.currentTimeMillis()
        )
        _notifications.value = listOf(newNotif) + _notifications.value
    }

    fun markAllNotificationsRead() {
        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
    }

    // ADMIN ACTIONS
    fun adminToggleSuspend(uid: String) {
        _users.value = _users.value.map {
            if (it.uid == uid) it.copy(isSuspended = !it.isSuspended) else it
        }
    }

    fun adminToggleAdmin(uid: String) {
        _users.value = _users.value.map {
            if (it.uid == uid) it.copy(isAdmin = !it.isAdmin) else it
        }
    }

    fun adminDeleteGroup(groupId: String) {
        _groups.value = _groups.value.filterNot { it.id == groupId }
    }

    // INITIAL DATA GENERATORS
    private fun createInitialUsers(): List<User> {
        return listOf(
            User(
                uid = "user_boniface",
                displayName = "Boniface Njuguna",
                photoUrl = "https://images.unsplash.com/photo-1539571696357-5a69c17a67c6?w=200&auto=format&fit=crop&q=80",
                coverPhotoUrl = "https://images.unsplash.com/photo-1547471080-7cc2caa01a7e?w=800&auto=format&fit=crop&q=80",
                bio = "Civil Engineer & Project Coordinator",
                profession = "Civil Engineer",
                location = "Calgary, Alberta",
                hometown = "Nairobi, Kenya",
                workplace = "Adigrat university _Engineering Sciences",
                workRole = "Civil Engineering",
                education = "Adigrat University",
                educationClass = "Class of 2018",
                followersCount = 7400,
                followingCount = 2100
            ),
            User(
                uid = "user_nic",
                displayName = "Ñiç Mwikà",
                photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200&auto=format&fit=crop&q=80",
                coverPhotoUrl = "https://images.unsplash.com/photo-1509785307050-d4066910ec1e?w=800&auto=format&fit=crop&q=80",
                bio = "Designer & Digital Creator",
                profession = "Digital Creator",
                location = "Calgary, Alberta",
                hometown = "Mombasa, Kenya",
                workplace = "Creative Media Hub",
                workRole = "UI/UX Lead",
                education = "University of Calgary",
                educationClass = "Class of 2019",
                followersCount = 5800,
                followingCount = 1950
            ),
            User(
                uid = "user_edson",
                displayName = "Edson Hamisi",
                photoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200&auto=format&fit=crop&q=80",
                coverPhotoUrl = "https://images.unsplash.com/photo-1518770660439-4636190af475?w=800&auto=format&fit=crop&q=80",
                bio = "Software Engineer & Open Source Builder",
                profession = "Software Developer",
                location = "Calgary, Alberta",
                hometown = "Dar es Salaam, Tanzania",
                workplace = "Tech Horizons Ltd",
                workRole = "Senior Architect",
                education = "Adigrat University",
                educationClass = "Class of 2017",
                followersCount = 6300,
                followingCount = 2400
            ),
            User(
                uid = "user_ken",
                displayName = "Ken Mutharimi",
                photoUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200&auto=format&fit=crop&q=80",
                coverPhotoUrl = "https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&auto=format&fit=crop&q=80",
                bio = "Structural Consultant & Technology Enthusiast",
                profession = "Structural Consultant",
                location = "Calgary, Alberta",
                hometown = "Nairobi, Kenya",
                workplace = "Apex Structural Designs",
                workRole = "Senior Associate",
                education = "Adigrat University",
                educationClass = "Class of 2018",
                followersCount = 9200,
                followingCount = 3800
            )
        )
    }

    private fun createInitialPosts(): List<Post> = emptyList()

    private fun createInitialComments(): Map<String, List<Comment>> = emptyMap()

    private fun createInitialGroups(): List<GroupItem> {
        return listOf(
            GroupItem(
                id = "group_tech",
                name = "Addis Tech & Startups (አዲስ ቴክ)",
                description = "Ethiopian founders, developers, engineers, and digital creators building products for Africa and the diaspora.",
                createdBy = "meskot",
                memberCount = 142,
                isJoined = true,
                coverColorHex = "#2A4838"
            ),
            GroupItem(
                id = "group_culture",
                name = "Habesha Food & Culinary Arts (የባህል ምግብ)",
                description = "Sharing traditional recipes, spice blends (berbere, mitmita), injera techniques, and diaspora cooking secrets.",
                createdBy = "meskot",
                memberCount = 89,
                isJoined = true,
                coverColorHex = "#8C2F39"
            ),
            GroupItem(
                id = "group_art",
                name = "Ethiopian Photography & Travel",
                description = "Visual journey across Simien Mountains, Danakil Depression, Omo Valley, Harar Jugol, and modern Addis nightlife.",
                createdBy = "meskot",
                memberCount = 64,
                isJoined = false,
                coverColorHex = "#B8863A"
            ),
            GroupItem(
                id = "group_literature",
                name = "Ge'ez & Ethiopian Literature",
                description = "Appreciating classic Ethiopian manuscripts, poetry (Qene), novels, and contemporary Habesha authors.",
                createdBy = "meskot",
                memberCount = 38,
                isJoined = false,
                coverColorHex = "#4A3B5C"
            )
        )
    }

    private fun createInitialGroupPosts(): Map<String, List<Post>> = emptyMap()

    private fun createInitialAlbums(): List<AlbumItem> = emptyList()

    private fun createInitialChatMessages(): Map<String, List<ChatMessage>> = emptyMap()

    private fun createInitialNotifications(): List<NotificationItem> = emptyList()

    private fun createInitialAdCampaigns(): List<AdCampaign> {
        return listOf(
            AdCampaign(
                id = "camp_coffee_01",
                name = "Habesha Coffee Roasters - Winter Single Origin",
                objective = "TRAFFIC",
                status = "ACTIVE",
                dailyBudgetEtb = 500.0,
                totalSpentEtb = 3420.0,
                impressions = 48290,
                clicks = 2140,
                conversions = 312,
                ctr = 4.43,
                avgCpcEtb = 1.60,
                cpmEtb = 70.8,
                headline = "Single-Origin Yirgacheffe & Sidama Beans ☕",
                primaryText = "Directly sourced from smallholder family farms in Sidama and Yirgacheffe. Freshly micro-roasted in Addis Ababa. Experience floral jasmine & citrus notes.",
                mediaUrl = "https://images.unsplash.com/photo-1509785307050-d4066910ec1e?w=800&auto=format&fit=crop&q=80",
                ctaText = "Shop Now",
                destinationUrl = "https://habeshacoffee.example.com",
                advertiserName = "Habesha Coffee Roasters",
                advertiserAvatar = "https://images.unsplash.com/photo-1501339847302-ac426a4a7cbb?w=150"
            ),
            AdCampaign(
                id = "camp_tech_02",
                name = "Addis Tech Summit 2026 - Early Bird Passes",
                objective = "CONVERSIONS",
                status = "ACTIVE",
                dailyBudgetEtb = 1200.0,
                totalSpentEtb = 8750.0,
                impressions = 94800,
                clicks = 4890,
                conversions = 680,
                ctr = 5.16,
                avgCpcEtb = 1.79,
                cpmEtb = 92.3,
                headline = "Early Bird Tickets Now Open! 🚀 Addis Tech Summit",
                primaryText = "Join 3,000+ engineers, founders, and venture capitalists at Millennium Hall. 40+ speakers from Silicon Valley, London, and Nairobi.",
                mediaUrl = "https://images.unsplash.com/photo-1540575467063-178a50c2df87?w=800&auto=format&fit=crop&q=80",
                ctaText = "Register Now",
                destinationUrl = "https://addistechsummit.et",
                advertiserName = "Addis Tech Network",
                advertiserAvatar = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=150"
            ),
            AdCampaign(
                id = "camp_leather_03",
                name = "Sheger Artisan Leather Goods",
                objective = "AWARENESS",
                status = "PAUSED",
                dailyBudgetEtb = 400.0,
                totalSpentEtb = 2100.0,
                impressions = 31500,
                clicks = 980,
                conversions = 95,
                ctr = 3.11,
                avgCpcEtb = 2.14,
                cpmEtb = 66.7,
                headline = "Handcrafted Ethiopian Full-Grain Leather Bags 🎒",
                primaryText = "Centuries of Ethiopian leather tanning craft combined with sleek modern functional aesthetics. Lifetime warranty on craftsmanship.",
                mediaUrl = "https://images.unsplash.com/photo-1548036328-c9fa89d128fa?w=800&auto=format&fit=crop&q=80",
                ctaText = "Learn More",
                destinationUrl = "https://shegerleather.et",
                advertiserName = "Sheger Leather Crafts",
                advertiserAvatar = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=150"
            )
        )
    }

    private fun createInitialPayoutHistory(): List<CreatorPayoutRecord> {
        val now = System.currentTimeMillis()
        return listOf(
            CreatorPayoutRecord(
                id = "pay_01",
                creatorUid = "meskot_creator",
                amountEtb = 4800.0,
                platformFeeEtb = 96.0,
                netAmountEtb = 4704.0,
                payoutMethod = "CBE Birr (Commercial Bank of Ethiopia)",
                status = "COMPLETED",
                transactionRef = "CBE-TX-98421098",
                timestamp = now - 86400000L * 4
            ),
            CreatorPayoutRecord(
                id = "pay_02",
                creatorUid = "meskot_creator",
                amountEtb = 3200.0,
                platformFeeEtb = 64.0,
                netAmountEtb = 3136.0,
                payoutMethod = "Telebirr SuperApp",
                status = "COMPLETED",
                transactionRef = "TB-DISB-77218392",
                timestamp = now - 86400000L * 12
            ),
            CreatorPayoutRecord(
                id = "pay_03",
                creatorUid = "meskot_creator",
                amountEtb = 1500.0,
                platformFeeEtb = 30.0,
                netAmountEtb = 1470.0,
                payoutMethod = "Chapa Direct Settlement",
                status = "COMPLETED",
                transactionRef = "CHP-REF-34891277",
                timestamp = now - 86400000L * 25
            )
        )
    }
}
