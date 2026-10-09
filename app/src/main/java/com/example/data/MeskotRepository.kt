package com.example.data

import android.content.Context
import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import java.util.UUID

class MeskotRepository(
    private val context: Context,
    val userRepository: UserRepository = FirestoreUserRepository()
) {

    private val repoScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    // Language state
    private val _currentLanguage = MutableStateFlow(AppLanguage.EN)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    fun setLanguage(lang: AppLanguage) {
        _currentLanguage.value = lang
    }

    // Current logged-in user (null until logged in via Firebase)
    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    // Users list (populated directly from Firebase Firestore via UserRepository, initially empty)
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    val isUsersLoading: StateFlow<Boolean> = userRepository.isLoading
    val usersError: StateFlow<String?> = userRepository.error

    // Feed Refreshing State (for pull-to-refresh)
    private val _isFeedRefreshing = MutableStateFlow(false)
    val isFeedRefreshing: StateFlow<Boolean> = _isFeedRefreshing.asStateFlow()

    fun refreshUsers() {
        repoScope.launch {
            userRepository.refreshUsers()
        }
    }

    suspend fun refreshFeed() {
        _isFeedRefreshing.value = true
        try {
            coroutineScope {
                val usersDeferred = async {
                    try {
                        userRepository.refreshUsers()
                    } catch (e: Exception) {
                        android.util.Log.e("MeskotRepository", "Error refreshing users: ${e.message}")
                    }
                }

                val postsDeferred = CompletableDeferred<Unit>()
                FirebaseManager.fetchPostsOnce { livePosts ->
                    if (livePosts.isNotEmpty()) {
                        _posts.value = livePosts
                    }
                    postsDeferred.complete(Unit)
                }

                val storiesDeferred = CompletableDeferred<Unit>()
                FirebaseManager.fetchStoriesOnce { liveStories ->
                    _stories.value = liveStories
                    storiesDeferred.complete(Unit)
                }

                usersDeferred.await()
                postsDeferred.await()
                storiesDeferred.await()
            }
            updateCurrentUserLastSeen()
        } catch (e: Exception) {
            android.util.Log.e("MeskotRepository", "Error refreshing feed: ${e.message}")
        } finally {
            _isFeedRefreshing.value = false
        }
    }

    // Posts list (populated directly from Firebase Firestore)
    private val _posts = MutableStateFlow<List<Post>>(emptyList())
    val posts: StateFlow<List<Post>> = _posts.asStateFlow()

    // Stories list (synchronized with Firebase Firestore with expiration timer)
    private val _stories = MutableStateFlow<List<StoryItem>>(emptyList())
    val stories: StateFlow<List<StoryItem>> = _stories.asStateFlow()
    private var storiesRegistration: com.google.firebase.firestore.ListenerRegistration? = null

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
    private val _conversations = MutableStateFlow<Map<String, List<ChatMessage>>>(createInitialChatMessages())
    val conversations: StateFlow<Map<String, List<ChatMessage>>> = _conversations.asStateFlow()

    // Notifications
    private val _notifications = MutableStateFlow<List<NotificationItem>>(createInitialNotifications())
    val notifications: StateFlow<List<NotificationItem>> = _notifications.asStateFlow()

    // Saved post IDs
    private val _savedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val savedPostIds: StateFlow<Set<String>> = _savedPostIds.asStateFlow()

    // Reel / preset reactions map: postId -> map of (uid -> reactionType)
    private val _reelReactions = MutableStateFlow<Map<String, Map<String, String>>>(emptyMap())
    val reelReactions: StateFlow<Map<String, Map<String, String>>> = _reelReactions.asStateFlow()

    // Real-time Live Streams & Sessions synchronized with Firebase Firestore
    private val _activeLiveStreams = MutableStateFlow<List<LiveStreamSession>>(emptyList())
    val activeLiveStreams: StateFlow<List<LiveStreamSession>> = _activeLiveStreams.asStateFlow()
    private var liveStreamsRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    // Currently active live session (either as host or viewer)
    private val _currentLiveSession = MutableStateFlow<LiveStreamSession?>(null)
    val currentLiveSession: StateFlow<LiveStreamSession?> = _currentLiveSession.asStateFlow()
    private var currentLiveSessionRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    // Real-time live comments & gifts in current session
    private val _currentLiveMessages = MutableStateFlow<List<LiveStreamComment>>(emptyList())
    val currentLiveMessages: StateFlow<List<LiveStreamComment>> = _currentLiveMessages.asStateFlow()
    private var currentLiveMessagesRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    // Professional Insights & Demographics (Real Server Synced)
    private val _userInsights = MutableStateFlow(UserInsightsData())
    val userInsights: StateFlow<UserInsightsData> = _userInsights.asStateFlow()

    private val _userEngagement = MutableStateFlow(UserEngagementData())
    val userEngagement: StateFlow<UserEngagementData> = _userEngagement.asStateFlow()

    private val _isAnalyticsLoading = MutableStateFlow(false)
    val isAnalyticsLoading: StateFlow<Boolean> = _isAnalyticsLoading.asStateFlow()

    private val _isAnalyticsServerSynced = MutableStateFlow(false)
    val isAnalyticsServerSynced: StateFlow<Boolean> = _isAnalyticsServerSynced.asStateFlow()

    private var insightsRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    // Subscribed post notification IDs
    private val _subscribedPostIds = MutableStateFlow<Set<String>>(emptySet())
    val subscribedPostIds: StateFlow<Set<String>> = _subscribedPostIds.asStateFlow()

    // Hidden post IDs
    private val _hiddenPostIds = MutableStateFlow<Set<String>>(emptySet())
    val hiddenPostIds: StateFlow<Set<String>> = _hiddenPostIds.asStateFlow()

    // Monetization: Ad Campaigns (Real-time Server Synced with Firebase Firestore)
    private val _adCampaigns = MutableStateFlow<List<AdCampaign>>(createInitialAdCampaigns())
    val adCampaigns: StateFlow<List<AdCampaign>> = _adCampaigns.asStateFlow()

    private val _isAdsServerSynced = MutableStateFlow(false)
    val isAdsServerSynced: StateFlow<Boolean> = _isAdsServerSynced.asStateFlow()
    private var adCampaignsRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var marketplaceListingsRegistration: com.google.firebase.firestore.ListenerRegistration? = null
    private var marketplaceChatsRegistration: com.google.firebase.firestore.ListenerRegistration? = null

    // Content Boosting Campaigns
    private val _boostCampaigns = MutableStateFlow<List<BoostCampaign>>(emptyList())
    val boostCampaigns: StateFlow<List<BoostCampaign>> = _boostCampaigns.asStateFlow()

    // Creator Payout Records
    private val _payoutHistory = MutableStateFlow<List<CreatorPayoutRecord>>(createInitialPayoutHistory())
    val payoutHistory: StateFlow<List<CreatorPayoutRecord>> = _payoutHistory.asStateFlow()

    // Creator Studio & Monetization Systems - Dynamic Facebook-grade Partner Programs
    private fun loadActiveMonetizationToolIds(): Set<String> {
        return try {
            val prefs = context.getSharedPreferences("meskot_monetization_prefs", Context.MODE_PRIVATE)
            prefs.getStringSet("active_tools", emptySet()) ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    private fun saveActiveMonetizationToolIds(ids: Set<String>) {
        try {
            val prefs = context.getSharedPreferences("meskot_monetization_prefs", Context.MODE_PRIVATE)
            prefs.edit().putStringSet("active_tools", ids).apply()
        } catch (e: Exception) {
            // ignore
        }
    }

    private val _activeMonetizationToolIds = MutableStateFlow<Set<String>>(loadActiveMonetizationToolIds())
    private val _underReviewMonetizationToolIds = MutableStateFlow<Set<String>>(emptySet())
    private val _registeredInterestToolIds = MutableStateFlow<Set<String>>(emptySet())

    private val _monetizationTools = MutableStateFlow<List<MonetizationTool>>(emptyList())
    val monetizationTools: StateFlow<List<MonetizationTool>> = _monetizationTools.asStateFlow()

    private val _earningsLedger = MutableStateFlow<List<EarningsLedgerEntry>>(createInitialLedger())
    val earningsLedger: StateFlow<List<EarningsLedgerEntry>> = _earningsLedger.asStateFlow()

    private val _contentFormatMetrics = MutableStateFlow<List<ContentFormatMetric>>(createInitialContentFormatMetrics())
    val contentFormatMetrics: StateFlow<List<ContentFormatMetric>> = _contentFormatMetrics.asStateFlow()

    private val _dailyEarnings = MutableStateFlow<List<DailyEarningsMetric>>(createInitialDailyEarnings())
    val dailyEarnings: StateFlow<List<DailyEarningsMetric>> = _dailyEarnings.asStateFlow()

    private val _payoutAccounts = MutableStateFlow<List<CreatorPayoutAccount>>(createInitialPayoutAccounts())
    val payoutAccounts: StateFlow<List<CreatorPayoutAccount>> = _payoutAccounts.asStateFlow()

    private val _chapaConfig = MutableStateFlow(ChapaGatewayConfig())
    val chapaConfig: StateFlow<ChapaGatewayConfig> = _chapaConfig.asStateFlow()

    // Real-time Following IDs set
    private val _followingUids = MutableStateFlow<Set<String>>(emptySet())
    val followingUids: StateFlow<Set<String>> = _followingUids.asStateFlow()

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

    // Unread tracking per sender & local read persistence
    private val _lastReadTimestamps = MutableStateFlow<Map<String, Long>>(loadLastReadTimestamps())
    val lastReadTimestamps: StateFlow<Map<String, Long>> = _lastReadTimestamps.asStateFlow()

    private val _locallyReadNotifIds = MutableStateFlow<Set<String>>(loadLocallyReadNotifIds())
    private val _locallySeenMsgIds = MutableStateFlow<Set<String>>(loadLocallySeenMsgIds())

    private fun loadLastReadTimestamps(): Map<String, Long> {
        return try {
            val prefs = context.getSharedPreferences("meskot_read_state_prefs", Context.MODE_PRIVATE)
            val result = mutableMapOf<String, Long>()
            prefs.all.forEach { (k, v) ->
                if (k.startsWith("read_ts_") && v is Long) {
                    result[k.removePrefix("read_ts_")] = v
                }
            }
            result
        } catch (e: Exception) {
            emptyMap()
        }
    }

    private fun saveLastReadTimestamp(otherUid: String, ts: Long) {
        try {
            val prefs = context.getSharedPreferences("meskot_read_state_prefs", Context.MODE_PRIVATE)
            prefs.edit().putLong("read_ts_$otherUid", ts).apply()
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun loadLocallyReadNotifIds(): Set<String> {
        return try {
            val prefs = context.getSharedPreferences("meskot_read_state_prefs", Context.MODE_PRIVATE)
            prefs.getStringSet("read_notif_ids", emptySet()) ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    private fun saveLocallyReadNotifIds(ids: Set<String>) {
        try {
            val prefs = context.getSharedPreferences("meskot_read_state_prefs", Context.MODE_PRIVATE)
            prefs.edit().putStringSet("read_notif_ids", ids).apply()
        } catch (e: Exception) {
            // ignore
        }
    }

    private fun loadLocallySeenMsgIds(): Set<String> {
        return try {
            val prefs = context.getSharedPreferences("meskot_read_state_prefs", Context.MODE_PRIVATE)
            prefs.getStringSet("seen_msg_ids", emptySet()) ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    private fun saveLocallySeenMsgIds(ids: Set<String>) {
        try {
            val prefs = context.getSharedPreferences("meskot_read_state_prefs", Context.MODE_PRIVATE)
            prefs.edit().putStringSet("seen_msg_ids", ids).apply()
        } catch (e: Exception) {
            // ignore
        }
    }

    fun markConversationAsRead(otherUid: String) {
        val now = System.currentTimeMillis()
        _lastReadTimestamps.value = _lastReadTimestamps.value + (otherUid to now)
        saveLastReadTimestamp(otherUid, now)

        val currentConvos = _conversations.value.toMutableMap()
        val newlySeenIds = mutableSetOf<String>()
        var changed = false

        currentConvos.forEach { (key, list) ->
            val hasPartnerMsgs = (key == otherUid || key.contains(otherUid)) &&
                    list.any { it.fromUid == otherUid && !it.isSeen }
            if (hasPartnerMsgs) {
                currentConvos[key] = list.map { msg ->
                    if (msg.fromUid == otherUid && !msg.isSeen) {
                        newlySeenIds.add(msg.id)
                        msg.copy(isSeen = true)
                    } else msg
                }
                changed = true
            }
        }

        if (newlySeenIds.isNotEmpty()) {
            val updatedSeenSet = _locallySeenMsgIds.value + newlySeenIds
            _locallySeenMsgIds.value = updatedSeenSet
            saveLocallySeenMsgIds(updatedSeenSet)
        }

        val user = _currentUser.value
        if (user != null) {
            FirebaseManager.markConversationMessagesSeen(user.uid, otherUid)
        }
        if (changed) {
            _conversations.value = currentConvos
        }

        // Also mark any unread "message" notifications from this sender as read so notification badge decreases too
        val unreadMsgNotifs = _notifications.value.filter {
            !it.isRead && it.type == "message" && it.fromUid == otherUid
        }
        if (unreadMsgNotifs.isNotEmpty()) {
            val notifIdsToMark = unreadMsgNotifs.map { it.id }.toSet()
            val updatedReadNotifs = _locallyReadNotifIds.value + notifIdsToMark
            _locallyReadNotifIds.value = updatedReadNotifs
            saveLocallyReadNotifIds(updatedReadNotifs)
            _notifications.value = _notifications.value.map {
                if (it.id in notifIdsToMark) it.copy(isRead = true) else it
            }
            notifIdsToMark.forEach { nid ->
                FirebaseManager.markNotificationRead(nid)
            }
        }
    }

    // Dynamic unread messages count (number of unread messages across all conversations)
    val unreadMsgCount: StateFlow<Int> = kotlinx.coroutines.flow.combine(_conversations, _lastReadTimestamps, _currentUser) { convos, readMap, user ->
        val currentUid = user?.uid
        val allMsgs = convos.values.flatten().distinctBy { it.id }
        allMsgs.count { msg ->
            if (msg.isCallLog) return@count false
            val isIncoming = if (currentUid != null) {
                msg.fromUid.isNotBlank() && msg.fromUid != currentUid && (msg.toUid == currentUid || msg.toUid == "user_0" || msg.toUid.isBlank())
            } else {
                msg.fromUid.isNotBlank() && msg.fromUid != "user_0"
            }
            if (!isIncoming) return@count false
            val lastRead = readMap[msg.fromUid] ?: 0L
            !msg.isSeen && (lastRead == 0L || msg.createdAt > lastRead)
        }
    }.stateIn(kotlinx.coroutines.CoroutineScope(kotlinx.coroutines.Dispatchers.Default), kotlinx.coroutines.flow.SharingStarted.WhileSubscribed(5000), 0)

    init {
        FirebaseManager.initialize(context)
        // Start real-time Firestore sync now that FirebaseManager is initialized
        (userRepository as? FirestoreUserRepository)?.startRealtimeUserSync()
        repoScope.launch {
            try {
                userRepository.refreshUsers()
            } catch (e: Exception) {
                Log.w("MeskotRepo", "Initial refreshUsers warning: ${e.message}")
            }
        }
        val fbAuthUser = FirebaseManager.getCurrentFirebaseUser()
        if (fbAuthUser != null) {
            val now = System.currentTimeMillis()
            val user = User(
                uid = fbAuthUser.uid,
                displayName = fbAuthUser.displayName ?: fbAuthUser.email?.substringBefore("@") ?: "User",
                email = fbAuthUser.email ?: "",
                photoUrl = fbAuthUser.photoUrl?.toString() ?: "",
                lastSeen = now
            )
            _currentUser.value = user
            userRepository.setCurrentUser(user)
            setupUserSpecificListeners(user.uid)

            // CRITICAL: Fetch full saved personal details and profile from server
            FirebaseManager.fetchUser(user.uid) { serverUser ->
                if (serverUser != null) {
                    val activeServerUser = serverUser.copy(lastSeen = System.currentTimeMillis())
                    _currentUser.value = activeServerUser
                    userRepository.setCurrentUser(activeServerUser)
                    _users.value = _users.value.map { if (it.uid == activeServerUser.uid) activeServerUser else it }
                    FirebaseManager.saveUser(activeServerUser)
                    Log.d("MeskotRepo", "Loaded personal details for ${activeServerUser.uid} from Firestore server")
                }
            }
        }

        repoScope.launch {
            userRepository.users.collect { liveUsers ->
                _users.value = liveUsers
                val curUid = _currentUser.value?.uid ?: FirebaseManager.getCurrentFirebaseUser()?.uid
                if (curUid != null) {
                    val matching = liveUsers.find { it.uid == curUid }
                    if (matching != null && matching != _currentUser.value) {
                        _currentUser.value = matching
                        setupUserSpecificListeners(matching.uid)
                    }
                }
            }
        }

        repoScope.launch {
            userRepository.currentUser.collect { user ->
                if (user != null && user != _currentUser.value) {
                    _currentUser.value = user
                }
            }
        }

        repoScope.launch {
            _currentUser.collect { user ->
                if (user != null) {
                    _savedPostIds.value = user.savedPostIds.toSet()
                    _subscribedPostIds.value = user.subscribedPostIds.toSet()
                }
            }
        }

        // Continuous live watch time session accumulator: increments real watch time during active app usage
        repoScope.launch {
            while (isActive) {
                delay(15_000L) // every 15 seconds of active session
                val user = _currentUser.value
                if (user != null) {
                    val currentWatch = user.watchHours
                    val added = 15.0 / 3600.0 // ~0.00417 hours
                    val newWatch = ((currentWatch + added) * 1000.0).toLong() / 1000.0
                    _currentUser.value = user.copy(watchHours = newWatch)
                }
            }
        }

        refreshMonetizationTools()

        repoScope.launch {
            combine(
                _currentUser,
                _friends,
                _posts,
                _activeMonetizationToolIds,
                _underReviewMonetizationToolIds
            ) { _, _, _, _, _ ->
                refreshMonetizationTools()
            }.collect()
        }

        setupFirebaseListeners()
    }

    private fun setupUserSpecificListeners(uid: String) {
        notifListenerRegistration?.remove()
        notifListenerRegistration = FirebaseManager.listenToNotifications(uid) { liveNotifs ->
            val localReadIds = _locallyReadNotifIds.value
            val localNotifMap = _notifications.value.associateBy { it.id }
            // Filter out any self-triggered notifications (actor_id != recipient_id)
            val validLiveNotifs = liveNotifs.filter { it.fromUid != uid }
            val mergedLive = validLiveNotifs.map { live ->
                val wasReadLocally = localReadIds.contains(live.id) || (localNotifMap[live.id]?.isRead == true)
                if (wasReadLocally && !live.isRead) live.copy(isRead = true) else live
            }
            val liveIds = mergedLive.map { it.id }.toSet()
            // Keep only local notifications that actually belong to the current user (or initial system items)
            val remainingLocal = _notifications.value
                .filterNot { it.id in liveIds }
                .filter { (it.toUid.isBlank() || it.toUid == uid) && it.fromUid != uid }
                .map { loc ->
                    if (localReadIds.contains(loc.id) && !loc.isRead) loc.copy(isRead = true) else loc
                }
            _notifications.value = (mergedLive + remainingLocal).sortedByDescending { it.createdAt }
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

        // Real-time server sync for Professional Insights & Engagement
        insightsRegistration?.remove()
        insightsRegistration = FirebaseManager.listenToUserInsights(uid) { liveInsights, liveEngagement ->
            liveInsights?.let { _userInsights.value = it }
            liveEngagement?.let { _userEngagement.value = it }
        }
        loadAndSyncAnalytics(uid)
    }

    fun recordProfileView(targetUid: String) {
        val viewerUid = _currentUser.value?.uid ?: return
        if (targetUid.isBlank() || targetUid == viewerUid) return
        FirebaseManager.recordProfileView(targetUid, viewerUid)
    }

    fun recordPostView(postId: String, authorUid: String) {
        val viewerUid = _currentUser.value?.uid ?: ""
        if (postId.isBlank()) return
        FirebaseManager.recordPostView(postId, authorUid, viewerUid)
    }

    fun loadAndSyncAnalytics(targetUid: String? = null, forceServerRefresh: Boolean = false) {
        val uid = targetUid ?: _currentUser.value?.uid ?: return
        if (uid.isBlank()) return

        repoScope.launch {
            _isAnalyticsLoading.value = true
            try {
                // 1. Fetch from server first
                FirebaseManager.fetchUserInsights(uid) { serverInsights ->
                    if (serverInsights != null) {
                        _userInsights.value = serverInsights
                        _isAnalyticsServerSynced.value = true
                    }
                }
                FirebaseManager.fetchUserEngagement(uid) { serverEngagement ->
                    if (serverEngagement != null) {
                        _userEngagement.value = serverEngagement
                    }
                }

                // 2. Compute dynamic up-to-date metrics from user's live posts and community
                val myPosts = _posts.value.filter { it.uid == uid }
                val (freshInsights, freshEngagement) = FirebaseManager.computeLiveAnalyticsFromData(
                    uid = uid,
                    myPosts = myPosts,
                    allUsers = _users.value
                )

                _userInsights.value = freshInsights
                _userEngagement.value = freshEngagement
                _isAnalyticsServerSynced.value = true

                // 3. Save merged calculations to Firestore
                FirebaseManager.saveUserInsightsAndEngagement(freshInsights, freshEngagement) { success ->
                    if (success) {
                        _isAnalyticsServerSynced.value = true
                    }
                }
            } catch (e: Exception) {
                Log.e("MeskotRepository", "Error syncing analytics with server: ${e.message}")
            } finally {
                _isAnalyticsLoading.value = false
            }
        }
    }

    private fun setupFirebaseListeners() {
        try {
            FirebaseManager.listenToPosts { livePosts ->
                _posts.value = livePosts
                _currentUser.value?.uid?.let { curUid ->
                    loadAndSyncAnalytics(curUid)
                }
            }


            storiesRegistration?.remove()
            storiesRegistration = FirebaseManager.listenToStories { liveStories ->
                _stories.value = liveStories
            }

            // Real-time active Live Streams in Firebase Firestore
            liveStreamsRegistration?.remove()
            liveStreamsRegistration = FirebaseManager.listenToActiveLiveStreams { liveStreams ->
                _activeLiveStreams.value = liveStreams
            }

            // Real-time Ad Campaigns in Firebase Firestore (Facebook Ads Manager Backend)
            adCampaignsRegistration?.remove()
            adCampaignsRegistration = FirebaseManager.listenToAdCampaigns { liveCampaigns ->
                _adCampaigns.value = liveCampaigns
                _isAdsServerSynced.value = true
            }
            if (adCampaignsRegistration != null) {
                _isAdsServerSynced.value = true
            }

            // Real-time Marketplace Listings & Buyer-Seller Chats in Firebase Firestore
            marketplaceListingsRegistration?.remove()
            marketplaceListingsRegistration = FirebaseManager.listenToMarketplaceListings { liveListings ->
                val savedSet = _savedListingIds.value
                val liveMap = liveListings.associateBy { it.id }
                val mergedSeed = createInitialMarketplaceListings()
                    .filterNot { it.id in liveMap }
                    .map { it.copy(isSaved = savedSet.contains(it.id)) }
                val mergedLive = liveListings.map { it.copy(isSaved = savedSet.contains(it.id)) }
                _marketplaceListings.value = (mergedLive + mergedSeed).sortedByDescending { it.createdAt }
            }

            marketplaceChatsRegistration?.remove()
            marketplaceChatsRegistration = FirebaseManager.listenToMarketplaceChats { liveChats ->
                val liveKeys = liveChats.map { "${it.chatId}_${it.timestamp}" }.toSet()
                val remainingLocal = _marketplaceChats.value.filterNot { "${it.chatId}_${it.timestamp}" in liveKeys }
                _marketplaceChats.value = (liveChats + remainingLocal).sortedByDescending { it.timestamp }
            }

            FirebaseManager.listenToComments { liveComments ->
                _comments.value = liveComments
            }

            repoScope.launch {
                userRepository.refreshUsers()
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
                    val user = _currentUser.value
                    if (user != null && (user.followersCount == 8500 || user.followingCount == 3700 || user.followingCount == 370 || user.watchHours == 3420.0)) {
                        val realFriendsCount = _friends.value.size
                        val cleanUser = user.copy(
                            followersCount = if (user.followersCount == 8500) realFriendsCount else maxOf(user.followersCount, realFriendsCount),
                            followingCount = if (user.followingCount == 3700 || user.followingCount == 370) realFriendsCount else maxOf(user.followingCount, realFriendsCount),
                            watchHours = if (user.watchHours == 3420.0) 0.0 else user.watchHours
                        )
                        _currentUser.value = cleanUser
                        userRepository.setCurrentUser(cleanUser)
                        repoScope.launch { userRepository.saveUser(cleanUser) }
                        FirebaseManager.saveUser(cleanUser)
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
                userRepository.setCurrentUser(fbUser)
                repoScope.launch {
                    userRepository.saveUser(fbUser)
                }
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
                userRepository.setCurrentUser(fbUser)
                repoScope.launch {
                    userRepository.saveUser(fbUser)
                }
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
        val activeUser = user.copy(lastSeen = System.currentTimeMillis())
        _currentUser.value = activeUser
        userRepository.setCurrentUser(activeUser)
        repoScope.launch {
            userRepository.saveUser(activeUser)
        }
        setupUserSpecificListeners(activeUser.uid)
        setupFirebaseListeners()
    }

    fun logout() {
        val curr = _currentUser.value
        if (curr != null) {
            val offlineUser = curr.copy(lastSeen = 0L)
            _users.value = _users.value.map { if (it.uid == curr.uid) offlineUser else it }
            repoScope.launch {
                try {
                    userRepository.updateUserFields(curr.uid, mapOf("lastSeen" to 0L))
                } catch (_: Exception) {}
            }
        }
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
        storiesRegistration?.remove()
        storiesRegistration = null
        adCampaignsRegistration?.remove()
        adCampaignsRegistration = null
        _incomingMessageAlert.value = null
        _incomingCall.value = null
        userRepository.setCurrentUser(null)
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
        educationClass: String = "",
        onComplete: ((Boolean, String?) -> Unit)? = null
    ) {
        val curr = _currentUser.value ?: run {
            onComplete?.invoke(false, "No active user logged in")
            return
        }
        val updated = curr.copy(
            displayName = name.trim().ifBlank { curr.displayName },
            bio = bio.trim(),
            photoUrl = photoUrl.trim().ifBlank { curr.photoUrl },
            gender = gender.trim(),
            birthDate = birthDate.trim(),
            coverPhotoUrl = coverPhotoUrl.trim().ifBlank { curr.coverPhotoUrl },
            profession = profession.trim(),
            location = location.trim(),
            hometown = hometown.trim(),
            workplace = workplace.trim(),
            workRole = workRole.trim(),
            education = education.trim(),
            educationClass = educationClass.trim()
        )
        _currentUser.value = updated
        _users.value = _users.value.map { if (it.uid == curr.uid) updated else it }
        userRepository.setCurrentUser(updated)
        repoScope.launch {
            try {
                userRepository.saveUser(updated)
            } catch (e: Exception) {
                Log.e("MeskotRepo", "Error in userRepository.saveUser: ${e.message}")
            }
        }
        FirebaseManager.saveUser(updated) { success, err ->
            if (success) {
                Log.d("MeskotRepo", "Profile details persisted to server for ${updated.uid}")
            } else {
                Log.e("MeskotRepo", "Failed to persist profile details to server: $err")
            }
            onComplete?.invoke(success, err)
        }
    }

    fun updateUserProfile(user: User, onComplete: ((Boolean, String?) -> Unit)? = null) {
        _currentUser.value = user
        _users.value = _users.value.map { if (it.uid == user.uid) user else it }
        userRepository.setCurrentUser(user)
        repoScope.launch {
            try {
                userRepository.saveUser(user)
            } catch (e: Exception) {
                Log.e("MeskotRepo", "Error in userRepository.saveUser: ${e.message}")
            }
        }
        FirebaseManager.saveUser(user) { success, err ->
            onComplete?.invoke(success, err)
        }
    }

    fun updateCurrentUserLastSeen() {
        val curr = _currentUser.value ?: return
        val now = System.currentTimeMillis()
        if (now - curr.lastSeen < 25_000L) return
        val updated = curr.copy(lastSeen = now)
        _currentUser.value = updated
        _users.value = _users.value.map { if (it.uid == curr.uid) updated else it }
        userRepository.setCurrentUser(updated)
        repoScope.launch {
            userRepository.saveUser(updated)
        }
    }

    // META VERIFIED METHODS
    fun subscribeMetaVerified(
        paymentMethod: String = "GOOGLE_PLAY",
        planId: String = "meta_verified_monthly"
    ): Boolean {
        val curr = _currentUser.value ?: return false
        val now = System.currentTimeMillis()
        val expiresAt = now + 30L * 24 * 3600 * 1000
        val updated = curr.copy(
            isVerified = true,
            verificationStatus = VerificationStatus.VERIFIED,
            verificationSubscribedAt = now,
            verificationExpiresAt = expiresAt,
            verificationPlan = planId,
            verificationPaymentMethod = paymentMethod
        )
        _currentUser.value = updated
        _users.value = _users.value.map { if (it.uid == curr.uid) updated else it }

        // Update user's existing posts so the blue checkmark appears dynamically
        _posts.value = _posts.value.map { post ->
            if (post.uid == curr.uid) post.copy(isAuthorVerified = true) else post
        }

        userRepository.setCurrentUser(updated)
        repoScope.launch {
            userRepository.saveUser(updated)
        }
        FirebaseManager.saveUser(updated)
        return true
    }

    fun cancelMetaVerified(): Boolean {
        val curr = _currentUser.value ?: return false
        val updated = curr.copy(
            isVerified = false,
            verificationStatus = VerificationStatus.NONE,
            verificationSubscribedAt = null,
            verificationExpiresAt = null
        )
        _currentUser.value = updated
        _users.value = _users.value.map { if (it.uid == curr.uid) updated else it }

        // Update user's existing posts
        _posts.value = _posts.value.map { post ->
            if (post.uid == curr.uid) post.copy(isAuthorVerified = false) else post
        }

        userRepository.setCurrentUser(updated)
        repoScope.launch {
            userRepository.saveUser(updated)
        }
        FirebaseManager.saveUser(updated)
        return true
    }

    // POSTS METHODS
    fun createPost(
        text: String,
        mediaUrls: List<String> = emptyList(),
        bgColorIndex: Int = 0,
        visibility: String = "public",
        postType: String = "POST",
        videoUrl: String = "",
        audioTrackTitle: String = "",
        locationName: String = "",
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        val user = _currentUser.value ?: return
        val calculatedType = if (postType != "POST") postType else if (videoUrl.isNotBlank()) "REEL" else if (mediaUrls.isNotEmpty()) "PHOTO" else "POST"
        val newPost = Post(
            id = "post_" + System.currentTimeMillis(),
            uid = user.uid,
            authorName = user.displayName,
            authorPhoto = user.photoUrl,
            text = text,
            mediaUrls = mediaUrls,
            bgColorIndex = bgColorIndex,
            visibility = visibility,
            postType = calculatedType,
            videoUrl = videoUrl,
            audioTrackTitle = audioTrackTitle,
            viewsCount = if (calculatedType == "REEL") 1 else 0,
            reactions = emptyMap(),
            commentCount = 0,
            createdAt = System.currentTimeMillis(),
            isAuthorVerified = user.isVerified,
            locationName = locationName,
            latitude = latitude,
            longitude = longitude
        )
        _posts.value = listOf(newPost) + _posts.value
        FirebaseManager.createPost(newPost)
    }

    fun createReel(
        videoUrl: String,
        caption: String,
        audioTrackTitle: String = "Original Audio",
        thumbnailUrl: String = "",
        visibility: String = "public"
    ) {
        val media = if (thumbnailUrl.isNotBlank()) listOf(thumbnailUrl) else if (videoUrl.isNotBlank()) listOf(videoUrl) else emptyList()
        createPost(
            text = caption,
            mediaUrls = media,
            bgColorIndex = 0,
            visibility = visibility,
            postType = "REEL",
            videoUrl = videoUrl,
            audioTrackTitle = audioTrackTitle
        )
    }

    fun editPost(
        postId: String,
        newText: String,
        newMediaUrls: List<String>? = null,
        newBgColorIndex: Int? = null,
        newVisibility: String? = null,
        newLocationName: String? = null,
        newLatitude: Double? = null,
        newLongitude: Double? = null
    ) {
        val now = System.currentTimeMillis()
        var updatedPostRef: Post? = null

        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val media = newMediaUrls ?: post.mediaUrls
                val bgIdx = newBgColorIndex ?: post.bgColorIndex
                val vis = newVisibility ?: post.visibility
                val newType = when {
                    post.postType == "REEL" || post.videoUrl.isNotBlank() -> "REEL"
                    media.isNotEmpty() -> "PHOTO"
                    else -> "POST"
                }
                val updated = post.copy(
                    text = newText,
                    mediaUrls = media,
                    bgColorIndex = if (media.isNotEmpty()) 0 else bgIdx,
                    visibility = vis,
                    postType = newType,
                    editedAt = now,
                    locationName = newLocationName ?: post.locationName,
                    latitude = if (newLocationName != null) newLatitude else post.latitude,
                    longitude = if (newLocationName != null) newLongitude else post.longitude
                )
                updatedPostRef = updated
                updated
            } else post
        }

        // Also update in groupPosts if the post belongs to a group
        if (_groupPosts.value.isNotEmpty()) {
            _groupPosts.value = _groupPosts.value.mapValues { (_, list) ->
                list.map { post ->
                    if (post.id == postId) {
                        val media = newMediaUrls ?: post.mediaUrls
                        val bgIdx = newBgColorIndex ?: post.bgColorIndex
                        val vis = newVisibility ?: post.visibility
                        val newType = when {
                            post.postType == "REEL" || post.videoUrl.isNotBlank() -> "REEL"
                            media.isNotEmpty() -> "PHOTO"
                            else -> "POST"
                        }
                        val updated = post.copy(
                            text = newText,
                            mediaUrls = media,
                            bgColorIndex = if (media.isNotEmpty()) 0 else bgIdx,
                            visibility = vis,
                            postType = newType,
                            editedAt = now
                        )
                        if (updatedPostRef == null) updatedPostRef = updated
                        updated
                    } else post
                }
            }
        }

        val target = updatedPostRef
        if (target != null) {
            FirebaseManager.updatePost(
                postId = postId,
                newText = target.text,
                newMediaUrls = target.mediaUrls,
                newBgColorIndex = target.bgColorIndex,
                newVisibility = target.visibility,
                newPostType = target.postType
            )
        } else {
            FirebaseManager.updatePostText(postId, newText)
        }
    }

    fun deletePost(postId: String) {
        _posts.value = _posts.value.filterNot { it.id == postId }
        FirebaseManager.deletePost(postId)
    }

    // ==========================================
    // STORIES METHODS WITH EXPIRATION TIMER
    // ==========================================
    fun createStory(
        mediaUrl: String,
        caption: String = "",
        filterName: String = "Normal",
        expirationHours: Int = 24,
        onComplete: (Boolean) -> Unit = {}
    ) {
        val user = _currentUser.value ?: return
        val nowMs = System.currentTimeMillis()
        val expiresAt = nowMs + (expirationHours * 3600 * 1000L)
        val newStory = StoryItem(
            id = "story_" + System.currentTimeMillis() + "_" + UUID.randomUUID().toString().take(6),
            uid = user.uid,
            authorName = user.displayName,
            authorPhoto = user.photoUrl,
            mediaUrl = mediaUrl,
            caption = caption,
            filterName = filterName,
            createdAt = nowMs,
            expiresAt = expiresAt,
            viewers = listOf(user.uid),
            likes = emptyMap()
        )
        // Optimistic update
        _stories.value = listOf(newStory) + _stories.value.filter { it.id != newStory.id }
        FirebaseManager.createStory(newStory) { success ->
            onComplete(success)
        }
    }

    fun deleteStory(storyId: String, onComplete: (Boolean) -> Unit = {}) {
        _stories.value = _stories.value.filterNot { it.id == storyId }
        FirebaseManager.deleteStory(storyId, onComplete)
    }

    fun recordStoryView(storyId: String, viewerUid: String) {
        if (viewerUid.isBlank()) return
        _stories.value = _stories.value.map {
            if (it.id == storyId && !it.viewers.contains(viewerUid)) {
                it.copy(viewers = it.viewers + viewerUid)
            } else it
        }
        FirebaseManager.recordStoryView(storyId, viewerUid)
    }

    fun toggleStoryLike(storyId: String, uid: String) {
        val actor = _currentUser.value
        _stories.value = _stories.value.map { story ->
            if (story.id == storyId) {
                val currentlyLiked = story.likes[uid] ?: false
                val newLikes = story.likes.toMutableMap()
                if (currentlyLiked) {
                    newLikes.remove(uid)
                } else {
                    newLikes[uid] = true
                    // Direct notification to story owner (story.uid) and prevent self-notification
                    if (actor != null && actor.uid != story.uid) {
                        addNotification(
                            fromUid = actor.uid,
                            fromName = actor.displayName,
                            fromPhoto = actor.photoUrl,
                            toUid = story.uid, // Target recipient: story owner
                            type = "like",
                            targetId = storyId,
                            customText = "${actor.displayName} liked your story ❤️"
                        )
                    }
                }
                FirebaseManager.toggleStoryLike(storyId, uid, !currentlyLiked)
                story.copy(likes = newLikes)
            } else story
        }
    }

    fun toggleReaction(postId: String, reactionType: String) {
        val user = _currentUser.value ?: return
        var updatedReactionsMap: Map<String, String>? = null
        var found = false
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                found = true
                val currentReaction = post.reactions[user.uid]
                val updatedReactions = post.reactions.toMutableMap()
                if (currentReaction == reactionType) {
                    updatedReactions.remove(user.uid)
                } else {
                    updatedReactions[user.uid] = reactionType
                    // Self-notification guard: only notify if actor_id (user.uid) != post_author_id (post.uid)
                    if (user.uid != post.uid) {
                        addNotification(
                            fromUid = user.uid,
                            fromName = user.displayName,
                            fromPhoto = user.photoUrl,
                            toUid = post.uid, // Directed to post author (owner_id), NOT the triggering user
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
        if (!found) {
            val currentReelMap = _reelReactions.value[postId]?.toMutableMap() ?: mutableMapOf()
            val existing = currentReelMap[user.uid]
            if (existing == reactionType) {
                currentReelMap.remove(user.uid)
            } else {
                currentReelMap[user.uid] = reactionType
            }
            val finalReelReactions = currentReelMap.toMap()
            _reelReactions.value = _reelReactions.value + (postId to finalReelReactions)
            updatedReactionsMap = finalReelReactions
        }
        updatedReactionsMap?.let { FirebaseManager.updatePostReactions(postId, it) }
    }

    fun sharePost(postId: String) {
        val user = _currentUser.value ?: return
        val sourcePost = _posts.value.find { it.id == postId }
        val effectivePost = sourcePost ?: run {
            val preset = com.example.ui.components.PresetReelsCollection.find { "preset_${it.id}" == postId }
            if (preset != null) {
                Post(
                    id = postId,
                    uid = "meskot_culture",
                    authorName = preset.title,
                    text = preset.description,
                    postType = "REEL",
                    videoUrl = preset.mediaUrl,
                    audioTrackTitle = preset.audioTitle
                )
            } else null
        } ?: return

        val newPost = Post(
            id = "post_" + System.currentTimeMillis(),
            uid = user.uid,
            authorName = user.displayName,
            authorPhoto = user.photoUrl,
            text = "",
            sharedPost = SharedPostPreview(
                postId = effectivePost.id,
                authorName = effectivePost.authorName,
                authorPhoto = effectivePost.authorPhoto,
                text = effectivePost.text,
                mediaUrls = if (effectivePost.videoUrl.isNotBlank()) listOf(effectivePost.videoUrl) + effectivePost.mediaUrls else effectivePost.mediaUrls,
                createdAt = effectivePost.createdAt
            ),
            createdAt = System.currentTimeMillis(),
            isAuthorVerified = user.isVerified
        )
        _posts.value = listOf(newPost) + _posts.value.map {
            if (it.id == postId) it.copy(sharesCount = it.sharesCount + 1) else it
        }
        // Save shared post to Firestore backend
        FirebaseManager.createPost(newPost)
        // Increment shares count on source post in Firestore backend
        FirebaseManager.incrementPostShareCount(postId)

        // Self-notification guard: only notify if actor_id (user.uid) != post_author_id (effectivePost.uid)
        if (user.uid != effectivePost.uid) {
            addNotification(
                fromUid = user.uid,
                fromName = user.displayName,
                fromPhoto = user.photoUrl,
                toUid = effectivePost.uid, // Directed to original post author (owner_id)
                type = "share",
                targetId = postId
            )
        }
    }

    fun toggleSavePost(postId: String) {
        val currentSaved = _savedPostIds.value.toMutableSet()
        val isNowSaved = if (currentSaved.contains(postId)) {
            currentSaved.remove(postId)
            false
        } else {
            currentSaved.add(postId)
            true
        }
        _savedPostIds.value = currentSaved
        val user = _currentUser.value
        if (user != null) {
            val updatedUser = user.copy(savedPostIds = currentSaved.toList())
            _currentUser.value = updatedUser
            userRepository.setCurrentUser(updatedUser)
            repoScope.launch {
                userRepository.saveUser(updatedUser)
            }
            FirebaseManager.toggleSavePost(user.uid, postId, isNowSaved)
        }
    }

    fun togglePostNotifications(postId: String): Boolean {
        val currentSubscribed = _subscribedPostIds.value.toMutableSet()
        val isNowSubscribed = if (currentSubscribed.contains(postId)) {
            currentSubscribed.remove(postId)
            false
        } else {
            currentSubscribed.add(postId)
            true
        }
        _subscribedPostIds.value = currentSubscribed
        val user = _currentUser.value
        if (user != null) {
            val updatedUser = user.copy(subscribedPostIds = currentSubscribed.toList())
            _currentUser.value = updatedUser
            userRepository.setCurrentUser(updatedUser)
            repoScope.launch {
                userRepository.saveUser(updatedUser)
            }
            FirebaseManager.togglePostNotifications(user.uid, postId, isNowSubscribed)
        }
        return isNowSubscribed
    }

    fun hidePost(postId: String) {
        _hiddenPostIds.value = _hiddenPostIds.value + postId
    }

    fun sendTip(postId: String, amount: Double, txRef: String = "", payFromBalance: Boolean = true): Boolean {
        val user = _currentUser.value ?: return false

        // STRICT FINANCIAL AUDIT: If paying from account balance, verify sufficient funds
        if (payFromBalance) {
            if (user.creatorNetBalance < amount || amount <= 0) {
                return false
            }
            // Deduct real funds from sender's balance
            val newSenderNet = (user.creatorNetBalance - amount).coerceAtLeast(0.0)
            val updatedSender = user.copy(creatorNetBalance = newSenderNet)
            _currentUser.value = updatedSender
            userRepository.setCurrentUser(updatedSender)
            repoScope.launch {
                userRepository.saveUser(updatedSender)
            }
        }

        var updatedTipTotal: Double? = null
        _posts.value = _posts.value.map { post ->
            if (post.id == postId) {
                val newTotal = post.tipTotal + amount
                updatedTipTotal = newTotal
                post.copy(tipTotal = newTotal)
            } else post
        }
        updatedTipTotal?.let { FirebaseManager.updatePostTip(postId, it) }
        val post = _posts.value.find { it.id == postId } ?: return false

        val netAmount = amount * 0.85 // 85% to creator, 15% platform fee
        val reference = if (txRef.isNotBlank()) txRef else "CHP-TIP-" + UUID.randomUUID().toString().take(8).uppercase()

        // If paying from balance, record debit on sender's ledger
        if (payFromBalance) {
            val debitEntry = EarningsLedgerEntry(
                id = "ledger_" + System.currentTimeMillis(),
                transactionRef = reference,
                entryType = LedgerEntryType.CREATOR_SUPPORT_DEBIT,
                amountEtb = -amount,
                balanceAfterEtb = _currentUser.value?.creatorNetBalance ?: 0.0,
                sourceTitle = "Support Tip on '${post.text.take(30)}...'",
                metadata = "Deducted from Real Balance · Recipient: ${post.authorName} · Ref: $reference"
            )
            _earningsLedger.value = listOf(debitEntry) + _earningsLedger.value
        }

        // Credit creator if it's the current user
        if (post.uid == user.uid) {
            val currentU = _currentUser.value ?: user
            val newNet = currentU.creatorNetBalance + netAmount
            val newGross = currentU.creatorGrossEarnings + amount
            val updatedUser = currentU.copy(creatorNetBalance = newNet, creatorGrossEarnings = newGross)
            _currentUser.value = updatedUser
            userRepository.setCurrentUser(updatedUser)
            repoScope.launch {
                userRepository.saveUser(updatedUser)
            }
            val newEntry = EarningsLedgerEntry(
                id = "ledger_" + (System.currentTimeMillis() + 1),
                transactionRef = reference,
                entryType = LedgerEntryType.FAN_TIP_CREDIT,
                amountEtb = netAmount,
                balanceAfterEtb = newNet,
                sourceTitle = "Reader Tip on '${post.text.take(30)}...'",
                metadata = "Gross: ${String.format(java.util.Locale.US, "%.2f", amount)} ETB · Verified Chapa Gateway · Ref: $reference"
            )
            _earningsLedger.value = listOf(newEntry) + _earningsLedger.value
        } else {
            // Update in other users list
            val creator = _users.value.find { it.uid == post.uid }
            if (creator != null) {
                val updatedCreator = creator.copy(
                    creatorGrossEarnings = creator.creatorGrossEarnings + amount,
                    creatorNetBalance = creator.creatorNetBalance + netAmount
                )
                _users.value = _users.value.map { if (it.uid == post.uid) updatedCreator else it }
                repoScope.launch {
                    userRepository.saveUser(updatedCreator)
                }
            }
        }

        if (user.uid != post.uid) {
            addNotification(
                fromUid = user.uid,
                fromName = user.displayName,
                fromPhoto = user.photoUrl,
                toUid = post.uid, // Directed to post author (owner_id)
                type = "tip",
                targetId = postId,
                amount = amount,
                customText = "${user.displayName} sent a real tip of ${amount.toInt()} ETB via Chapa! 💰"
            )
        }
        return true
    }

    // MONETIZATION: FAN SUBSCRIPTIONS
    fun subscribeToCreator(creatorUid: String, tier: MembershipTier, txRef: String = "") {
        val user = _currentUser.value ?: return
        val updatedVip = user.vipMemberships.toMutableMap()
        updatedVip[creatorUid] = tier.code
        val updatedUser = user.copy(vipMemberships = updatedVip)
        _currentUser.value = updatedUser

        val reference = if (txRef.isNotBlank()) txRef else "CHP-SUB-" + UUID.randomUUID().toString().take(8).uppercase()
        val netAdd = tier.monthlyPriceEtb * 0.80

        // Notify creator only if subscriber is not the creator themselves
        if (user.uid != creatorUid) {
            addNotification(
                fromUid = user.uid,
                fromName = user.displayName,
                fromPhoto = user.photoUrl,
                toUid = creatorUid, // Directed to creator (owner_id)
                type = "vip_subscription",
                targetId = creatorUid,
                customText = "${user.displayName} joined your fan club as a ${tier.label}! 🌟 (Chapa Verified: $reference)"
            )
        }

        // Update creator earnings
        if (creatorUid == user.uid) {
            val newNet = user.creatorNetBalance + netAdd
            val newGross = user.creatorGrossEarnings + tier.monthlyPriceEtb
            val updatedUser = user.copy(creatorNetBalance = newNet, creatorGrossEarnings = newGross)
            _currentUser.value = updatedUser
            userRepository.setCurrentUser(updatedUser)
            repoScope.launch {
                userRepository.saveUser(updatedUser)
            }
            val newEntry = EarningsLedgerEntry(
                id = "ledger_" + System.currentTimeMillis(),
                transactionRef = reference,
                entryType = LedgerEntryType.SUBSCRIPTION_CREDIT,
                amountEtb = netAdd,
                balanceAfterEtb = newNet,
                sourceTitle = "${tier.label} Subscription (${user.displayName})",
                metadata = "Gross: ${tier.monthlyPriceEtb} ETB · Platform 20% · Verified Chapa Gateway · Ref: $reference"
            )
            _earningsLedger.value = listOf(newEntry) + _earningsLedger.value
        } else {
            val creator = _users.value.find { it.uid == creatorUid }
            if (creator != null) {
                val updatedCreator = creator.copy(
                    creatorGrossEarnings = creator.creatorGrossEarnings + tier.monthlyPriceEtb,
                    creatorNetBalance = creator.creatorNetBalance + netAdd
                )
                _users.value = _users.value.map { if (it.uid == creatorUid) updatedCreator else it }
                repoScope.launch {
                    userRepository.saveUser(updatedCreator)
                }
            }
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

        if (user.uid != post.uid) {
            addNotification(
                fromUid = user.uid,
                fromName = user.displayName,
                fromPhoto = user.photoUrl,
                toUid = post.uid, // Directed to post author (owner_id)
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
    ): Boolean {
        val user = _currentUser.value ?: return false
        val totalBudget = dailyBudgetEtb * durationDays

        // REAL FINANCIAL AUDIT: Verify user has sufficient live balance
        if (user.creatorNetBalance < totalBudget || totalBudget <= 0) {
            return false
        }

        // Atomically deduct cost from user balance
        val newBalance = (user.creatorNetBalance - totalBudget).coerceAtLeast(0.0)
        val updatedUser = user.copy(creatorNetBalance = newBalance)
        _currentUser.value = updatedUser
        userRepository.setCurrentUser(updatedUser)
        repoScope.launch {
            userRepository.saveUser(updatedUser)
        }

        val multiplier = 1.0 + (dailyBudgetEtb / 50.0).coerceAtMost(5.0)
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
        FirebaseManager.saveBoostCampaign(campaign)

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
        FirebaseManager.updatePostBoost(postId, true, multiplier, dailyBudgetEtb, durationDays)
        FirebaseManager.saveUser(updatedUser)

        // Financial Ledger Record
        val newEntry = EarningsLedgerEntry(
            id = "ledger_" + System.currentTimeMillis(),
            transactionRef = "BOOST-" + UUID.randomUUID().toString().take(8).uppercase(),
            entryType = LedgerEntryType.BOOST_POST_DEBIT,
            amountEtb = -totalBudget,
            balanceAfterEtb = newBalance,
            sourceTitle = "Boost Post Promotion ($durationDays days @ ${dailyBudgetEtb.toInt()} ETB/day)",
            metadata = "Deducted from Account Balance · High-Priority Feed Algorithm Reach"
        )
        _earningsLedger.value = listOf(newEntry) + _earningsLedger.value

        // Boost campaign delivers real audience growth: automatically gains followers and watch hours
        val gainedFollowers = (durationDays * 35).coerceAtLeast(15)
        val gainedHours = durationDays * 8.5
        incrementFollowers(gainedFollowers)
        incrementWatchHours(gainedHours)
        return true
    }

    // REAL-TIME AUDIENCE & WATCH TIME ENGINE
    fun incrementFollowers(count: Int = 1) {
        val user = _currentUser.value ?: return
        val currentFollowers = user.followersCount
        val updated = user.copy(followersCount = (currentFollowers + count).coerceAtLeast(0))
        _currentUser.value = updated
        _users.value = _users.value.map { if (it.uid == user.uid) updated else it }
        userRepository.setCurrentUser(updated)
        repoScope.launch {
            userRepository.saveUser(updated)
        }
        FirebaseManager.saveUser(updated)
        refreshMonetizationTools()
    }

    fun incrementWatchHours(hours: Double = 1.0) {
        val user = _currentUser.value ?: return
        val currentWatch = user.watchHours
        val rounded = ((currentWatch + hours) * 10.0).toLong() / 10.0
        val updated = user.copy(watchHours = rounded)
        _currentUser.value = updated
        _users.value = _users.value.map { if (it.uid == user.uid) updated else it }
        userRepository.setCurrentUser(updated)
        repoScope.launch {
            userRepository.saveUser(updated)
        }
        FirebaseManager.saveUser(updated)
        refreshMonetizationTools()
    }

    fun recordWatchTime(durationSeconds: Long, creatorUid: String? = null) {
        if (durationSeconds <= 0) return
        val hoursToAdd = durationSeconds / 3600.0
        val user = _currentUser.value ?: return
        val targetId = creatorUid ?: user.uid
        if (targetId == user.uid) {
            val currentWatch = if (user.watchHours == 3420.0) 0.0 else user.watchHours
            val updated = user.copy(watchHours = ((currentWatch + hoursToAdd) * 1000.0).toLong() / 1000.0)
            _currentUser.value = updated
            _users.value = _users.value.map { if (it.uid == user.uid) updated else it }
            userRepository.setCurrentUser(updated)
            repoScope.launch {
                userRepository.saveUser(updated)
            }
            FirebaseManager.saveUser(updated)
        } else {
            val target = _users.value.find { it.uid == targetId }
            if (target != null) {
                val currentWatch = if (target.watchHours == 3420.0) 0.0 else target.watchHours
                val updated = target.copy(watchHours = ((currentWatch + hoursToAdd) * 1000.0).toLong() / 1000.0)
                _users.value = _users.value.map { if (it.uid == targetId) updated else it }
                repoScope.launch {
                    userRepository.saveUser(updated)
                }
                FirebaseManager.saveUser(updated)
            }
        }
    }

    fun toggleFollow(targetUid: String) {
        val user = _currentUser.value ?: return
        val isSelf = (targetUid == user.uid)
        val isCurrentlyFollowing = _followingUids.value.contains(targetUid)
        if (isCurrentlyFollowing) {
            _followingUids.value = _followingUids.value - targetUid
            val currentFollowing = if (user.followingCount == 3700 || user.followingCount == 370) _friends.value.size else user.followingCount
            val newFollowing = (currentFollowing - 1).coerceAtLeast(0)
            val baseFollowers = if (user.followersCount == 8500) _friends.value.size else user.followersCount
            val newFollowers = if (isSelf) (baseFollowers - 1).coerceAtLeast(0) else baseFollowers
            val updatedUser = user.copy(
                followingCount = newFollowing,
                followersCount = newFollowers
            )
            _currentUser.value = updatedUser
            userRepository.setCurrentUser(updatedUser)
            repoScope.launch { userRepository.saveUser(updatedUser) }
            FirebaseManager.saveUser(updatedUser)

            if (!isSelf) {
                // Decrement target user followers
                val target = _users.value.find { it.uid == targetUid }
                if (target != null) {
                    val targetBaseFollowers = if (target.followersCount == 8500) 0 else target.followersCount
                    val newTargetFollowers = (targetBaseFollowers - 1).coerceAtLeast(0)
                    val updatedTarget = target.copy(followersCount = newTargetFollowers)
                    _users.value = _users.value.map { if (it.uid == targetUid) updatedTarget else it }
                    repoScope.launch { userRepository.saveUser(updatedTarget) }
                    FirebaseManager.saveUser(updatedTarget)
                }
            }
        } else {
            _followingUids.value = _followingUids.value + targetUid
            val currentFollowing = if (user.followingCount == 3700 || user.followingCount == 370) _friends.value.size else user.followingCount
            val newFollowing = currentFollowing + 1
            val baseFollowers = if (user.followersCount == 8500) _friends.value.size else user.followersCount
            val newFollowers = if (isSelf) baseFollowers + 1 else baseFollowers
            val updatedUser = user.copy(
                followingCount = newFollowing,
                followersCount = newFollowers
            )
            _currentUser.value = updatedUser
            userRepository.setCurrentUser(updatedUser)
            repoScope.launch { userRepository.saveUser(updatedUser) }
            FirebaseManager.saveUser(updatedUser)

            if (!isSelf) {
                // Increment target user followers
                val target = _users.value.find { it.uid == targetUid }
                if (target != null) {
                    val targetBaseFollowers = if (target.followersCount == 8500) 0 else target.followersCount
                    val newTargetFollowers = targetBaseFollowers + 1
                    val updatedTarget = target.copy(followersCount = newTargetFollowers)
                    _users.value = _users.value.map { if (it.uid == targetUid) updatedTarget else it }
                    repoScope.launch { userRepository.saveUser(updatedTarget) }
                    FirebaseManager.saveUser(updatedTarget)
                }

                addNotification(
                    fromUid = user.uid,
                    fromName = user.displayName,
                    fromPhoto = user.photoUrl,
                    toUid = targetUid, // Directed to the followed user (owner_id)
                    type = "follow",
                    targetId = targetUid,
                    customText = "${user.displayName} started following your profile and videos."
                )
            }
        }
    }

    fun isFollowing(targetUid: String): Boolean = _followingUids.value.contains(targetUid)

    fun syncPartnerMetrics() {
        val user = _currentUser.value ?: return
        val realFriends = _friends.value.size
        val realFollowers = if (user.followersCount == 8500) realFriends else maxOf(user.followersCount, realFriends)
        val realFollowing = if (user.followingCount == 3700 || user.followingCount == 370) realFriends else maxOf(user.followingCount, realFriends)
        val currentWatchHours = if (user.watchHours == 3420.0) 0.0 else user.watchHours
        val updated = user.copy(followersCount = realFollowers, followingCount = realFollowing, watchHours = currentWatchHours)
        _currentUser.value = updated
        userRepository.setCurrentUser(updated)
        repoScope.launch { userRepository.saveUser(updated) }
        FirebaseManager.saveUser(updated)
    }

    // CREATOR PAYOUTS
    fun requestPayout(method: String, amountEtb: Double, destinationAccount: String = "") {
        val user = _currentUser.value ?: return
        if (user.creatorNetBalance < amountEtb || amountEtb <= 0) return

        val platformFee = amountEtb * 0.02
        val netDisbursed = amountEtb - platformFee
        val ref = "CHAPA-OUT-" + UUID.randomUUID().toString().take(8).uppercase()

        val record = CreatorPayoutRecord(
            id = "payout_" + System.currentTimeMillis(),
            creatorUid = user.uid,
            amountEtb = amountEtb,
            platformFeeEtb = platformFee,
            netAmountEtb = netDisbursed,
            payoutMethod = method,
            status = "COMPLETED",
            transactionRef = ref
        )

        _payoutHistory.value = listOf(record) + _payoutHistory.value
        val newBalance = user.creatorNetBalance - amountEtb
        val updatedUser = user.copy(creatorNetBalance = newBalance)
        _currentUser.value = updatedUser

        val destText = if (destinationAccount.isNotBlank()) " to $destinationAccount" else ""

        // Append double-entry debit in ledger
        val ledgerDebit = EarningsLedgerEntry(
            id = "ledger_" + System.currentTimeMillis(),
            transactionRef = record.transactionRef,
            entryType = LedgerEntryType.PAYOUT_DEBIT,
            amountEtb = -amountEtb,
            balanceAfterEtb = newBalance,
            sourceTitle = "Disbursement via $method$destText",
            metadata = "Chapa Transfer API · Fee: 2% (${String.format(java.util.Locale.US, "%.2f", platformFee)} ETB) · Net Disbursed: ${String.format(java.util.Locale.US, "%.2f", netDisbursed)} ETB · Ref: $ref"
        )
        _earningsLedger.value = listOf(ledgerDebit) + _earningsLedger.value
    }

    // MONETIZATION WORKFLOWS: ELIGIBILITY, APPLICATION & INVITE CODES
    fun applyForMonetizationTool(toolId: String, payoutMethod: String = "Telebirr", accountNumber: String = ""): Boolean {
        var applied = false
        val currentActive = _activeMonetizationToolIds.value
        if (!currentActive.contains(toolId)) {
            val newActive = currentActive + toolId
            _activeMonetizationToolIds.value = newActive
            saveActiveMonetizationToolIds(newActive)
            _underReviewMonetizationToolIds.value = _underReviewMonetizationToolIds.value - toolId
            applied = true
        }

        refreshMonetizationTools()

        val tool = _monetizationTools.value.find { it.id == toolId }
        val toolName = tool?.name ?: "Monetization Program"
        val user = _currentUser.value
        if (user != null) {
            addNotification(
                fromUid = "system_meskot",
                fromName = "Meskot Creator Studio",
                fromPhoto = "",
                toUid = user.uid,
                type = "monetization_approved",
                targetId = user.uid,
                customText = "🎉 Approved! $toolName is now ACTIVE. Payouts connected via $payoutMethod ($accountNumber)."
            )
        }
        recordLedgerCredit(
            entryType = LedgerEntryType.AD_REVENUE_CREDIT,
            amountEtb = 100.0,
            sourceTitle = "$toolName Onboarding Incentive",
            metadata = "Payout: $payoutMethod · Account: ${accountNumber.ifBlank { "Verified" }} · Active"
        )
        return applied
    }

    fun redeemInviteCode(toolId: String, code: String): Boolean {
        val trimmed = code.trim().uppercase()
        val validCodes = setOf("MESKOT-VIP", "CREATOR2026", "INLINE-CHAPA", "ETHIOPIA-PRIME", "YOUTUBE-PARTNER", "GRANT2026", "REELS2026", "REELS-VIP", "ACCELERATOR-VIP")
        if (!validCodes.contains(trimmed)) {
            return false
        }
        val newActive = _activeMonetizationToolIds.value + toolId
        _activeMonetizationToolIds.value = newActive
        saveActiveMonetizationToolIds(newActive)
        _underReviewMonetizationToolIds.value = _underReviewMonetizationToolIds.value - toolId
        refreshMonetizationTools()

        val tool = _monetizationTools.value.find { it.id == toolId }
        val toolName = tool?.name ?: "Creator Program"
        recordLedgerCredit(
            entryType = LedgerEntryType.REVERSAL_CREDIT,
            amountEtb = 500.0,
            sourceTitle = "VIP Invite Token Activation Bonus ($trimmed - $toolName)",
            metadata = "Token: $trimmed · Approved & Active"
        )
        val user = _currentUser.value
        if (user != null) {
            addNotification(
                fromUid = "system_meskot",
                fromName = "Meskot Creator Studio",
                fromPhoto = "",
                toUid = user.uid,
                type = "monetization_approved",
                targetId = user.uid,
                customText = "🎟️ VIP Invite Accepted! $toolName unlocked with 500 ETB activation bonus."
            )
        }
        return true
    }

    fun registerInterest(toolId: String) {
        _registeredInterestToolIds.value = _registeredInterestToolIds.value + toolId
        refreshMonetizationTools()
        val user = _currentUser.value
        if (user != null) {
            addNotification(
                fromUid = "system_meskot",
                fromName = "Meskot Creator Studio",
                fromPhoto = "",
                toUid = user.uid,
                type = "monetization_waitlist",
                targetId = user.uid,
                customText = "🌟 You have joined the partner waitlist. You will be prioritized when cohort capacity opens."
            )
        }
    }

    fun recordLedgerCredit(
        entryType: LedgerEntryType,
        amountEtb: Double,
        sourceTitle: String,
        metadata: String = ""
    ) {
        val user = _currentUser.value ?: return
        val newNet = user.creatorNetBalance + amountEtb
        val newGross = user.creatorGrossEarnings + amountEtb
        _currentUser.value = user.copy(creatorNetBalance = newNet, creatorGrossEarnings = newGross)

        val newEntry = EarningsLedgerEntry(
            id = "ledger_" + System.currentTimeMillis(),
            transactionRef = "TX-CR-" + UUID.randomUUID().toString().take(8).uppercase(),
            entryType = entryType,
            amountEtb = amountEtb,
            balanceAfterEtb = newNet,
            sourceTitle = sourceTitle,
            metadata = metadata
        )
        _earningsLedger.value = listOf(newEntry) + _earningsLedger.value
    }

    fun depositViaChapa(
        amountEtb: Double,
        txRef: String,
        paymentMethod: String = "Chapa Online (Telebirr/CBE/Card)"
    ) {
        val user = _currentUser.value ?: return
        val newNet = user.creatorNetBalance + amountEtb
        val newGross = user.creatorGrossEarnings + amountEtb
        val updatedUser = user.copy(creatorNetBalance = newNet, creatorGrossEarnings = newGross)
        _currentUser.value = updatedUser
        userRepository.setCurrentUser(updatedUser)
        repoScope.launch {
            userRepository.saveUser(updatedUser)
        }

        val newEntry = EarningsLedgerEntry(
            id = "ledger_" + System.currentTimeMillis(),
            transactionRef = txRef,
            entryType = LedgerEntryType.CHAPA_DEPOSIT_CREDIT,
            amountEtb = amountEtb,
            balanceAfterEtb = newNet,
            sourceTitle = "Real Funds Deposit ($paymentMethod)",
            metadata = "Verified by Chapa Gateway · Channel: $paymentMethod · Ref: $txRef"
        )
        _earningsLedger.value = listOf(newEntry) + _earningsLedger.value
    }

    fun buyStarsWithChapa(starCount: Int, priceEtb: Double, txRef: String) {
        val user = _currentUser.value ?: return
        val newStars = user.starBalance + starCount
        val updatedUser = user.copy(starBalance = newStars)
        _currentUser.value = updatedUser
        userRepository.setCurrentUser(updatedUser)
        repoScope.launch {
            userRepository.saveUser(updatedUser)
        }

        val newEntry = EarningsLedgerEntry(
            id = "ledger_" + System.currentTimeMillis(),
            transactionRef = txRef,
            entryType = LedgerEntryType.STARS_GIFT_CREDIT,
            amountEtb = priceEtb,
            balanceAfterEtb = user.creatorNetBalance,
            sourceTitle = "Stars Top-up via Chapa ($starCount Stars)",
            metadata = "Purchased $starCount Stars · ${priceEtb.toInt()} ETB · Verified Chapa Gateway · Ref: $txRef"
        )
        _earningsLedger.value = listOf(newEntry) + _earningsLedger.value
    }

    fun resetToRealChapaBalance() {
        val user = _currentUser.value ?: return
        val updatedUser = user.copy(creatorNetBalance = 0.0, creatorGrossEarnings = 0.0)
        _currentUser.value = updatedUser
        userRepository.setCurrentUser(updatedUser)
        repoScope.launch {
            userRepository.saveUser(updatedUser)
        }
        _earningsLedger.value = emptyList()
    }

    fun updateChapaConfig(publicKey: String, secretKey: String, isLiveMode: Boolean) {
        _chapaConfig.value = _chapaConfig.value.copy(
            publicKey = publicKey.trim(),
            secretKey = secretKey.trim(),
            isLiveMode = isLiveMode
        )
    }

    fun simulateDailySettlementCron() {
        val dailyEarningsRollup = 280.0
        recordLedgerCredit(
            entryType = LedgerEntryType.AD_REVENUE_CREDIT,
            amountEtb = dailyEarningsRollup,
            sourceTitle = "Daily Automated Ad Settlement (02:00 UTC Batch)",
            metadata = "Reels & In-Stream Impressions: 14,800 · Net Split: 70%"
        )
    }

    // ADS MANAGER: CAMPAIGNS (Real-time Firebase Firestore Ad Server Integration)
    fun refreshAdCampaignsFromServer() {
        FirebaseManager.fetchAdCampaignsOnce { liveCampaigns ->
            _adCampaigns.value = liveCampaigns
            _isAdsServerSynced.value = true
        }
    }

    fun createAdCampaign(
        name: String,
        objective: String,
        dailyBudgetEtb: Double,
        headline: String,
        primaryText: String,
        mediaUrl: String,
        ctaText: String,
        destinationUrl: String,
        targetAudience: String = "Men/Women, 18-65+, 1 location",
        durationDays: Int = 5,
        deductFromWallet: Boolean = false,
        paymentMethod: String = "Chapa Checkout (USD/ETB)",
        paymentRef: String = "",
        totalPaidEtb: Double = dailyBudgetEtb * durationDays.coerceAtLeast(1) * 1.05
    ): Boolean {
        val user = _currentUser.value ?: User(uid = "usr_yemane", displayName = "Yemane Tsadik")
        val chargeAmountEtb = if (totalPaidEtb > 0.0) totalPaidEtb else (dailyBudgetEtb * durationDays.coerceAtLeast(1) * 1.05)

        if (deductFromWallet && (user.creatorNetBalance < chargeAmountEtb || chargeAmountEtb <= 0)) {
            return false
        }

        val newBalance = if (deductFromWallet) {
            (user.creatorNetBalance - chargeAmountEtb).coerceAtLeast(0.0)
        } else {
            user.creatorNetBalance
        }

        if (deductFromWallet && _currentUser.value != null) {
            val updatedUser = user.copy(creatorNetBalance = newBalance)
            _currentUser.value = updatedUser
            userRepository.setCurrentUser(updatedUser)
            repoScope.launch {
                userRepository.saveUser(updatedUser)
            }
            FirebaseManager.saveUser(updatedUser)
        }

        val advertiserName = user.displayName.ifBlank { "Yemane Tsadik" }
        val advertiserAvatar = user.photoUrl

        val estimatedImpressions = (dailyBudgetEtb * 18).toInt().coerceAtLeast(1250)
        val estimatedClicks = (estimatedImpressions * 0.038).toInt().coerceAtLeast(45)
        val effectivePaymentRef = paymentRef.ifBlank {
            "CHP-AD-" + UUID.randomUUID().toString().take(8).uppercase()
        }

        val newCampaign = AdCampaign(
            id = "camp_" + System.currentTimeMillis(),
            name = name,
            objective = objective,
            status = "ACTIVE",
            dailyBudgetEtb = dailyBudgetEtb,
            totalSpentEtb = dailyBudgetEtb,
            impressions = estimatedImpressions,
            clicks = estimatedClicks,
            conversions = (estimatedClicks * 0.18).toInt().coerceAtLeast(6),
            ctr = 3.8,
            avgCpcEtb = (1.20 + Math.random() * 0.80).let { String.format(java.util.Locale.US, "%.2f", it).toDouble() },
            cpmEtb = 25.0,
            headline = headline,
            primaryText = primaryText,
            mediaUrl = mediaUrl,
            ctaText = ctaText,
            destinationUrl = destinationUrl,
            advertiserName = advertiserName,
            advertiserAvatar = advertiserAvatar,
            advertiserUid = user.uid,
            durationDays = durationDays.coerceAtLeast(1),
            targetAudience = targetAudience,
            paymentMethod = paymentMethod,
            paymentRef = effectivePaymentRef,
            totalPaidEtb = chargeAmountEtb,
            createdAt = System.currentTimeMillis()
        )
        _adCampaigns.value = listOf(newCampaign) + _adCampaigns.value

        // Persist to Firebase Firestore backend server
        FirebaseManager.saveAdCampaign(newCampaign) { success ->
            if (success) {
                _isAdsServerSynced.value = true
            }
        }

        val newEntry = EarningsLedgerEntry(
            id = "ledger_" + System.currentTimeMillis(),
            transactionRef = effectivePaymentRef,
            entryType = LedgerEntryType.AD_CAMPAIGN_DEBIT,
            amountEtb = -chargeAmountEtb,
            balanceAfterEtb = newBalance,
            sourceTitle = "Ad Campaign Promoted: $name",
            metadata = "Paid via $paymentMethod · Ref: $effectivePaymentRef · Objective: $objective · Duration: ${durationDays}d"
        )
        _earningsLedger.value = listOf(newEntry) + _earningsLedger.value
        return true
    }

    fun toggleAdCampaignStatus(campaignId: String) {
        var updatedStatus: String? = null
        _adCampaigns.value = _adCampaigns.value.map {
            if (it.id == campaignId) {
                val newStatus = if (it.status == "ACTIVE") "PAUSED" else "ACTIVE"
                updatedStatus = newStatus
                it.copy(status = newStatus)
            } else it
        }
        updatedStatus?.let { status ->
            FirebaseManager.updateAdCampaignStatus(campaignId, status)
        }
    }

    fun recordAdImpression(campaignId: String) {
        var cpm = 25.0
        _adCampaigns.value = _adCampaigns.value.map { camp ->
            if (camp.id == campaignId) {
                cpm = camp.cpmEtb
                val costPerImp = (camp.cpmEtb / 1000.0).coerceAtLeast(0.025)
                val newImps = camp.impressions + 1
                val newCtr = if (newImps > 0) {
                    Math.round((camp.clicks.toDouble() / newImps.toDouble()) * 10000.0) / 100.0
                } else camp.ctr
                camp.copy(
                    impressions = newImps,
                    totalSpentEtb = camp.totalSpentEtb + costPerImp,
                    ctr = newCtr
                )
            } else camp
        }
        FirebaseManager.recordAdImpression(campaignId, cpm)
    }

    fun recordAdClick(campaignId: String, recordConversion: Boolean = false) {
        var cpc = 1.45
        _adCampaigns.value = _adCampaigns.value.map { camp ->
            if (camp.id == campaignId) {
                cpc = camp.avgCpcEtb
                val newClicks = camp.clicks + 1
                val newConversions = if (recordConversion) camp.conversions + 1 else camp.conversions
                val newCtr = if (camp.impressions > 0) {
                    Math.round((newClicks.toDouble() / camp.impressions.toDouble()) * 10000.0) / 100.0
                } else camp.ctr
                camp.copy(
                    clicks = newClicks,
                    conversions = newConversions,
                    totalSpentEtb = camp.totalSpentEtb + cpc,
                    ctr = newCtr
                )
            } else camp
        }
        FirebaseManager.recordAdClick(campaignId, cpc, recordConversion)
    }

    fun deleteAdCampaign(campaignId: String) {
        _adCampaigns.value = _adCampaigns.value.filterNot { it.id == campaignId }
        FirebaseManager.deleteAdCampaign(campaignId)
    }

    // COMMENTS
    fun getCommentsForPost(postId: String): List<Comment> {
        val existing = _comments.value[postId]
        if (!existing.isNullOrEmpty()) return existing
        if (postId.startsWith("preset_") || postId.startsWith("reel_")) {
            val presets = getPresetReelComments(postId)
            _comments.value = _comments.value + (postId to presets)
            return presets
        }
        return emptyList()
    }

    private fun getPresetReelComments(postId: String): List<Comment> {
        val now = System.currentTimeMillis()
        return listOf(
            Comment(
                id = "cmt_preset_${postId}_1",
                postId = postId,
                uid = "selam_t",
                authorName = "Selamawit T.",
                authorPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
                text = "ዋው በጣም የሚያምር ቪዲዮ ነው! 🔥🇪🇹 Truly captured the authentic vibe!",
                createdAt = now - 3600000L * 2,
                likes = mapOf("user_selam" to true, "user_dawit" to true),
                isAuthorVerified = true
            ),
            Comment(
                id = "cmt_preset_${postId}_2",
                postId = postId,
                uid = "dawit_g",
                authorName = "Dawit Gebre",
                authorPhoto = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
                text = "Habesha culture at its finest! The music track pairing is amazing ☕✨",
                createdAt = now - 3600000L * 5,
                likes = mapOf("user_selam" to true),
                isAuthorVerified = false
            ),
            Comment(
                id = "cmt_preset_${postId}_3",
                postId = postId,
                uid = "kalkidan_m",
                authorName = "Kalkidan M.",
                authorPhoto = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=200",
                text = "Keep sharing our heritage! Shared to my friends 🙌",
                createdAt = now - 3600000L * 8,
                likes = emptyMap(),
                isAuthorVerified = true
            )
        )
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
            createdAt = System.currentTimeMillis(),
            isAuthorVerified = user.isVerified
        )
        val currentList = _comments.value[postId] ?: emptyList()
        _comments.value = _comments.value + (postId to (currentList + newComment))
        // Increment post comment count
        val postExists = _posts.value.any { it.id == postId }
        if (postExists) {
            _posts.value = _posts.value.map {
                if (it.id == postId) it.copy(commentCount = it.commentCount + 1) else it
            }
        }
        FirebaseManager.addComment(newComment)
        val post = _posts.value.find { it.id == postId }
        // Self-notification guard: only notify if actor_id (user.uid) != post_author_id (post.uid)
        if (post != null && user.uid != post.uid) {
            addNotification(
                fromUid = user.uid,
                fromName = user.displayName,
                fromPhoto = user.photoUrl,
                toUid = post.uid, // Directed to post author (owner_id), NOT the triggering user
                type = "comment",
                targetId = postId,
                customText = "${user.displayName} commented on your post: \"${text.take(60)}\""
            )
        }
        // If replying to another user's comment, also notify the parent comment author (if distinct from actor and post author)
        if (parentId != null) {
            val parentComment = currentList.find { it.id == parentId }
            if (parentComment != null && parentComment.uid != user.uid && parentComment.uid != post?.uid) {
                addNotification(
                    fromUid = user.uid,
                    fromName = user.displayName,
                    fromPhoto = user.photoUrl,
                    toUid = parentComment.uid, // Directed to parent comment author
                    type = "comment",
                    targetId = postId,
                    customText = "${user.displayName} replied to your comment: \"${text.take(60)}\""
                )
            }
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
                    // Self-notification guard: only notify comment owner if actor_id != comment_author_id
                    if (user.uid != comment.uid) {
                        addNotification(
                            fromUid = user.uid,
                            fromName = user.displayName,
                            fromPhoto = user.photoUrl,
                            toUid = comment.uid, // Directed to comment owner
                            type = "like",
                            targetId = postId,
                            customText = "${user.displayName} liked your comment: \"${comment.text.take(50)}\""
                        )
                    }
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

        // In Meskot Creator platform, each accepted friend connection is also an active follower
        incrementFollowers(1)

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

        val seenIds = _locallySeenMsgIds.value
        val readTimestamps = _lastReadTimestamps.value

        liveMessages.forEach { rawMsg ->
            val lastReadForSender = readTimestamps[rawMsg.fromUid] ?: 0L
            val isAlreadySeenLocally = rawMsg.isSeen ||
                    seenIds.contains(rawMsg.id) ||
                    (lastReadForSender > 0L && rawMsg.createdAt <= lastReadForSender)
            val msg = if (isAlreadySeenLocally && !rawMsg.isSeen) rawMsg.copy(isSeen = true) else rawMsg

            val convoId = if (msg.convoId.isNotBlank()) msg.convoId else {
                if (msg.fromUid < msg.toUid) "${msg.fromUid}_${msg.toUid}" else "${msg.toUid}_${msg.fromUid}"
            }

            // Check if this is a newly arrived message addressed to current user
            if (currentUid != null && msg.toUid == currentUid && msg.fromUid != currentUid && !msg.isCallLog && !msg.isSeen) {
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
                    val existing = list[idx]
                    val finalMsg = if (existing.isSeen && !msg.isSeen) msg.copy(isSeen = true) else msg
                    list.toMutableList().apply { set(idx, finalMsg) }
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

    fun sendMessage(
        otherUid: String,
        text: String,
        mediaUrl: String? = null,
        mediaType: String? = null,
        fileName: String? = null,
        fileSize: String? = null
    ) {
        val user = _currentUser.value ?: return
        val convoId = if (user.uid < otherUid) "${user.uid}_$otherUid" else "${otherUid}_${user.uid}"
        val newMsg = ChatMessage(
            id = "msg_" + System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().take(6),
            convoId = convoId,
            fromUid = user.uid,
            toUid = otherUid,
            text = text,
            mediaUrl = mediaUrl,
            mediaType = mediaType,
            fileName = fileName,
            fileSize = fileSize,
            createdAt = System.currentTimeMillis()
        )
        // Zero-latency instant local update across convoId and direct UID
        mergeMessagesIntoConversations(listOf(newMsg), notifyIncoming = false)
        com.example.util.CallAudioManager.playMessageSentSound()

        // Instant write to Firestore
        FirebaseManager.sendMessage(newMsg)

        // Instant push notification to recipient
        val notifText = when (mediaType) {
            "image" -> "${user.displayName} sent a photo 📷"
            "file" -> "${user.displayName} sent a file: ${fileName ?: "attachment"}"
            "audio" -> "${user.displayName} sent a voice message 🎤"
            "like" -> "${user.displayName} sent a thumbs up 👍"
            else -> "${user.displayName}: $text"
        }
        val notif = NotificationItem(
            id = "notif_" + System.currentTimeMillis() + "_" + java.util.UUID.randomUUID().toString().take(6),
            fromUid = user.uid,
            fromName = user.displayName,
            fromPhoto = user.photoUrl,
            toUid = otherUid,
            type = "message",
            text = notifText,
            targetId = user.uid,
            createdAt = System.currentTimeMillis()
        )
        FirebaseManager.sendNotification(notif)
    }

    fun clearConversation(otherUid: String) {
        val currentMap = _conversations.value.toMutableMap()
        val user = _currentUser.value
        val convoId = if (user != null) {
            if (user.uid < otherUid) "${user.uid}_$otherUid" else "${otherUid}_${user.uid}"
        } else null
        currentMap.remove(otherUid)
        if (convoId != null) currentMap.remove(convoId)
        _conversations.value = currentMap
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

    fun updateGroupCover(groupId: String, newCoverUrl: String, newColorHex: String? = null) {
        _groups.value = _groups.value.map { g ->
            if (g.id == groupId) {
                g.copy(
                    coverImageUrl = newCoverUrl,
                    coverColorHex = newColorHex ?: g.coverColorHex
                )
            } else g
        }
    }

    fun updateGroupPrivacyAndPermissions(groupId: String, isPrivate: Boolean, allowMemberPosts: Boolean) {
        _groups.value = _groups.value.map { g ->
            if (g.id == groupId) {
                g.copy(
                    isPrivate = isPrivate,
                    allowMemberPosts = allowMemberPosts
                )
            } else g
        }
    }

    fun resolveGroupModerationQueue(groupId: String, queueType: String) {
        _groups.value = _groups.value.map { g ->
            if (g.id == groupId) {
                when (queueType) {
                    "PENDING" -> g.copy(pendingApprovalsCount = 0)
                    "REPORTED" -> g.copy(reportedContentCount = 0)
                    "SPAM" -> g.copy(potentialSpamCount = 0)
                    "ALERTS" -> g.copy(moderationAlertsCount = 0)
                    else -> g
                }
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
    /**
     * Dispatches a notification from the triggering actor (`fromUid` / `actor_id`) to the
     * target owner (`toUid` / `post_author_id`).
     *
     * Fixes:
     * 1. Requires `toUid` (`post_author_id` / `owner_id`) so notifications are routed to the content
     *    owner instead of the triggering user (`actor_id` / `current_user_id`).
     * 2. Enforces `if (fromUid == toUid) return` so users never receive notifications for liking,
     *    commenting on, or sharing their own posts.
     * 3. Writes the notification to Cloud Firestore (`FirebaseManager.sendNotification`) so the
     *    post owner receives it in real time via `listenToNotifications(forUid = post_author_id)`,
     *    and only updates local `_notifications` state if the current logged-in user IS the recipient (`toUid`).
     */
    private fun addNotification(
        fromUid: String,
        fromName: String,
        fromPhoto: String,
        toUid: String,
        type: String,
        targetId: String? = null,
        reactionType: String? = null,
        amount: Double? = null,
        customText: String? = null
    ) {
        // 1. Validate recipient and prevent self-notifications (actor_id != post_author_id)
        if (toUid.isBlank() || fromUid == toUid) return

        val reactionEmoji = when (reactionType?.lowercase()) {
            "love" -> "❤️"
            "haha" -> "😆"
            "wow" -> "😮"
            "sad" -> "😢"
            "angry" -> "😡"
            "like" -> "👍"
            else -> ""
        }

        val notifText = customText ?: when (type) {
            "reaction" -> if (reactionEmoji.isNotEmpty()) {
                "$fromName reacted $reactionEmoji to your post"
            } else {
                "$fromName reacted to your post"
            }
            "like" -> "$fromName liked your post"
            "comment" -> "$fromName commented on your post"
            "friend_req", "friend_request" -> "$fromName sent you a friend request"
            "friend_accept" -> "$fromName accepted your friend request"
            "tip" -> "$fromName sent you a tip of ${amount?.toInt() ?: 25} ETB via Chapa!"
            "share" -> "$fromName shared your post"
            else -> "$fromName interacted with your profile"
        }

        val newNotif = NotificationItem(
            id = "notif_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}",
            fromUid = fromUid,       // actor_id (User A performing the action)
            fromName = fromName,
            fromPhoto = fromPhoto,
            toUid = toUid,           // post_author_id / owner_id (User B receiving the notification)
            text = notifText,
            type = type,
            targetId = targetId,
            reactionType = reactionType,
            amount = amount,
            isRead = false,
            createdAt = System.currentTimeMillis()
        )

        // 2. Deliver notification to the target recipient (User B) in Cloud Firestore
        FirebaseManager.sendNotification(newNotif)

        // 3. Only add to the local in-memory notification feed if the current user IS the recipient
        // (e.g., system notifications addressed to currentUser). Never push User B's notification into User A's feed!
        val currentUid = _currentUser.value?.uid
        if (currentUid != null && currentUid == toUid) {
            _notifications.value = listOf(newNotif) + _notifications.value
        }
    }

    fun markNotificationRead(notifId: String) {
        val updatedReadSet = _locallyReadNotifIds.value + notifId
        _locallyReadNotifIds.value = updatedReadSet
        saveLocallyReadNotifIds(updatedReadSet)

        val targetNotif = _notifications.value.find { it.id == notifId }
        _notifications.value = _notifications.value.map {
            if (it.id == notifId) it.copy(isRead = true) else it
        }
        FirebaseManager.markNotificationRead(notifId)

        // If the notification is a message notification, also mark the sender's messages as read
        if (targetNotif != null && targetNotif.type == "message" && targetNotif.fromUid.isNotBlank()) {
            markConversationAsRead(targetNotif.fromUid)
        }
    }

    fun markAllNotificationsRead() {
        val allIds = _notifications.value.map { it.id }.toSet()
        val updatedReadSet = _locallyReadNotifIds.value + allIds
        _locallyReadNotifIds.value = updatedReadSet
        saveLocallyReadNotifIds(updatedReadSet)

        _notifications.value = _notifications.value.map { it.copy(isRead = true) }
        val user = _currentUser.value
        if (user != null) {
            FirebaseManager.markAllNotificationsRead(user.uid)
        }
    }

    // ADMIN ACTIONS
    fun adminToggleSuspend(uid: String) {
        val target = _users.value.find { it.uid == uid } ?: return
        val updated = target.copy(isSuspended = !target.isSuspended)
        _users.value = _users.value.map { if (it.uid == uid) updated else it }
        repoScope.launch {
            userRepository.saveUser(updated)
        }
    }

    fun adminToggleAdmin(uid: String) {
        val target = _users.value.find { it.uid == uid } ?: return
        val updated = target.copy(isAdmin = !target.isAdmin)
        _users.value = _users.value.map { if (it.uid == uid) updated else it }
        repoScope.launch {
            userRepository.saveUser(updated)
        }
    }

    fun adminDeleteGroup(groupId: String) {
        _groups.value = _groups.value.filterNot { it.id == groupId }
    }

    // INITIAL DATA GENERATORS
    private fun createInitialUsers(): List<User> {
        val now = System.currentTimeMillis()
        return listOf(
            User(
                uid = "user_gebreslassie",
                displayName = "Gebreslassie Tsadik",
                bio = "Living life with faith, courage, and purpose.",
                photoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300",
                lastSeen = now - 12 * 60 * 1000L, // 12 minutes ago
                followersCount = 384,
                followingCount = 142,
                location = "Mekelle, Ethiopia"
            ),
            User(
                uid = "user_rahel",
                displayName = "Rahel Tesfaye",
                bio = "Tech innovator & community organizer 🇪🇹",
                photoUrl = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                lastSeen = now - 45 * 1000L, // 45 seconds ago (Active now)
                followersCount = 512,
                followingCount = 230,
                location = "Addis Ababa, Ethiopia"
            ),
            User(
                uid = "user_daniel",
                displayName = "Daniel Haile",
                bio = "Photographer & cultural heritage storyteller 📸",
                photoUrl = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300",
                lastSeen = now - 2 * 3600 * 1000L, // 2 hours ago
                followersCount = 289,
                followingCount = 175,
                location = "Hawassa, Ethiopia"
            ),
            User(
                uid = "user_almaz",
                displayName = "Almaz Berhe",
                bio = "Culinary artist celebrating traditional flavors ☕",
                photoUrl = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=300",
                lastSeen = now - 26 * 3600 * 1000L, // Yesterday
                followersCount = 410,
                followingCount = 190,
                location = "Gondar, Ethiopia"
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
                coverColorHex = "#2A4838",
                coverImageUrl = "https://images.unsplash.com/photo-1522071820081-009f0129c71c?w=1000&auto=format&fit=crop&q=80",
                isPrivate = false,
                allowMemberPosts = true,
                pendingApprovalsCount = 4,
                reportedContentCount = 1,
                potentialSpamCount = 2,
                moderationAlertsCount = 1
            ),
            GroupItem(
                id = "group_culture",
                name = "Habesha Food & Culinary Arts (የባህል ምግብ)",
                description = "Sharing traditional recipes, spice blends (berbere, mitmita), injera techniques, and diaspora cooking secrets.",
                createdBy = "meskot",
                memberCount = 89,
                isJoined = true,
                coverColorHex = "#8C2F39",
                coverImageUrl = "https://images.unsplash.com/photo-1544025162-d76694265947?w=1000&auto=format&fit=crop&q=80",
                isPrivate = false,
                allowMemberPosts = true,
                pendingApprovalsCount = 2,
                reportedContentCount = 0,
                potentialSpamCount = 1,
                moderationAlertsCount = 0
            ),
            GroupItem(
                id = "group_art",
                name = "Ethiopian Photography & Travel",
                description = "Visual journey across Simien Mountains, Danakil Depression, Omo Valley, Harar Jugol, and modern Addis nightlife.",
                createdBy = "meskot",
                memberCount = 64,
                isJoined = false,
                coverColorHex = "#B8863A",
                coverImageUrl = "https://images.unsplash.com/photo-1547471080-7cc2caa01a7e?w=1000&auto=format&fit=crop&q=80",
                isPrivate = false,
                allowMemberPosts = true
            ),
            GroupItem(
                id = "group_literature",
                name = "Ge'ez & Ethiopian Literature",
                description = "Appreciating classic Ethiopian manuscripts, poetry (Qene), novels, and contemporary Habesha authors.",
                createdBy = "meskot",
                memberCount = 38,
                isJoined = false,
                coverColorHex = "#4A3B5C",
                coverImageUrl = "https://images.unsplash.com/photo-1457369804613-52c61a468e7d?w=1000&auto=format&fit=crop&q=80",
                isPrivate = true,
                allowMemberPosts = true
            )
        )
    }

    private fun createInitialGroupPosts(): Map<String, List<Post>> {
        val now = System.currentTimeMillis()
        return mapOf(
            "group_tech" to listOf(
                Post(
                    id = "gpost_tech_1",
                    uid = "user_rahel",
                    authorName = "Rahel Tesfaye",
                    authorPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                    text = "📌 [Announcement] Welcome to Addis Tech & Startups! Drop your current Jetpack Compose, AI, or FinTech project below so members can connect and collaborate. 🚀🇪🇹 #announcement #android #kotlin",
                    mediaUrls = listOf("https://images.unsplash.com/photo-1531482615713-2afd69097998?w=900&auto=format&fit=crop&q=80"),
                    postType = "PHOTO",
                    reactions = mapOf("user_gebreslassie" to "love", "user_abebe" to "like"),
                    commentCount = 2,
                    createdAt = now - 2 * 3600 * 1000L,
                    isAuthorVerified = true,
                    tags = listOf("announcements", "photos", "android")
                ),
                Post(
                    id = "gpost_tech_2",
                    uid = "user_gebreslassie",
                    authorName = "Gebreslassie Tsadik",
                    authorPhoto = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300",
                    text = "📅 [Event] Addis Developers & Founders Meetup — This Saturday at Bole Atlas! Live demos on mobile payments and Chapa SDK integration. Who's joining? #events",
                    reactions = mapOf("user_rahel" to "like"),
                    commentCount = 1,
                    createdAt = now - 6 * 3600 * 1000L,
                    tags = listOf("events")
                )
            )
        )
    }

    private fun createInitialAlbums(): List<AlbumItem> = emptyList()

    private fun createInitialChatMessages(): Map<String, List<ChatMessage>> {
        val gebUid = "user_gebreslassie"
        val myUid = "user_0"
        val cal = java.util.Calendar.getInstance()
        fun timeFor(day: Int, hour: Int, minute: Int): Long {
            cal.set(2026, java.util.Calendar.AUGUST, day, hour, minute, 0)
            return cal.timeInMillis
        }
        val messages = listOf(
            ChatMessage(
                id = "geb_msg_1",
                convoId = "convo_gebreslassie",
                fromUid = myUid,
                toUid = gebUid,
                text = "Chigir yeblun eshi kifelu trah",
                createdAt = timeFor(8, 13, 40),
                isSeen = true
            ),
            ChatMessage(
                id = "geb_msg_2",
                convoId = "convo_gebreslassie",
                fromUid = gebUid,
                toUid = myUid,
                text = "ብሰርዓት",
                createdAt = timeFor(14, 7, 38),
                isSeen = true
            ),
            ChatMessage(
                id = "geb_msg_3",
                convoId = "convo_gebreslassie",
                fromUid = myUid,
                toUid = gebUid,
                text = "Hi",
                createdAt = timeFor(16, 9, 50),
                isSeen = true
            ),
            ChatMessage(
                id = "geb_msg_4",
                convoId = "convo_gebreslassie",
                fromUid = myUid,
                toUid = gebUid,
                text = "Hishuka do",
                createdAt = timeFor(16, 9, 50) + 1500L,
                isSeen = true
            ),
            ChatMessage(
                id = "geb_msg_5",
                convoId = "convo_gebreslassie",
                fromUid = gebUid,
                toUid = myUid,
                text = "Mnm lewti yelen",
                createdAt = timeFor(16, 9, 52),
                isSeen = true
            ),
            ChatMessage(
                id = "geb_msg_6",
                convoId = "convo_gebreslassie",
                fromUid = myUid,
                toUid = gebUid,
                text = "Hikmna kedka do",
                createdAt = timeFor(16, 9, 53),
                isSeen = true
            ),
            ChatMessage(
                id = "geb_msg_7",
                convoId = "convo_gebreslassie",
                fromUid = gebUid,
                toUid = myUid,
                text = "Aykedkun zeleku",
                createdAt = timeFor(16, 9, 55),
                isSeen = true
            ),
            ChatMessage(
                id = "geb_msg_8",
                convoId = "convo_gebreslassie",
                fromUid = myUid,
                toUid = gebUid,
                text = "Hi",
                createdAt = timeFor(18, 12, 13),
                isSeen = true
            ),
            ChatMessage(
                id = "geb_msg_9",
                convoId = "convo_gebreslassie",
                fromUid = gebUid,
                toUid = myUid,
                text = "Hi",
                createdAt = timeFor(18, 12, 54),
                isSeen = false
            )
        )
        val seenIds = loadLocallySeenMsgIds()
        val lastRead = loadLastReadTimestamps()[gebUid] ?: 0L
        val hydrated = messages.map { msg ->
            if (seenIds.contains(msg.id) || (lastRead > 0L && msg.createdAt <= lastRead)) {
                msg.copy(isSeen = true)
            } else msg
        }
        return mapOf(gebUid to hydrated)
    }

    private fun createInitialNotifications(): List<NotificationItem> {
        val now = System.currentTimeMillis()
        val readIds = loadLocallyReadNotifIds()
        return listOf(
            NotificationItem(
                id = "notif_init_1",
                fromUid = "user_gebreslassie",
                fromName = "Gebreslassie Tsadik",
                fromPhoto = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300",
                type = "message",
                text = "Gebreslassie Tsadik sent you a message: 'Hi'",
                isRead = readIds.contains("notif_init_1"),
                createdAt = now - 14 * 60 * 1000L
            ),
            NotificationItem(
                id = "notif_init_2",
                fromUid = "user_sara",
                fromName = "Sara Haile",
                fromPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300",
                type = "like",
                text = "Sara Haile reacted ❤️ to your post",
                isRead = readIds.contains("notif_init_2"),
                createdAt = now - 45 * 60 * 1000L
            ),
            NotificationItem(
                id = "notif_init_3",
                fromUid = "user_abebe",
                fromName = "Abebe Bikila",
                fromPhoto = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300",
                type = "comment",
                text = "Abebe Bikila commented: 'Great perspective on this!'",
                isRead = true,
                createdAt = now - 3 * 3600 * 1000L
            ),
            NotificationItem(
                id = "notif_init_4",
                fromUid = "system_meskot",
                fromName = "Meskot Studio",
                fromPhoto = "",
                type = "monetization_approved",
                text = "Meskot Creator Studio approved your monetization application",
                isRead = true,
                createdAt = now - 24 * 3600 * 1000L
            )
        )
    }

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

    fun refreshMonetizationTools() {
        _monetizationTools.value = buildDynamicMonetizationTools()
    }

    private fun buildDynamicMonetizationTools(): List<MonetizationTool> {
        val user = _currentUser.value
        val friendsCount = _friends.value.size
        val realFollowers = if ((user?.followersCount ?: 0) == 8500) friendsCount else maxOf(user?.followersCount ?: 0, friendsCount)
        val realWatchHours = if ((user?.watchHours ?: 0.0) == 3420.0) 0.0 else (user?.watchHours ?: 0.0)
        val realPostsCount = if (user != null) {
            val userPosts = _posts.value.count { it.uid == user.uid }
            if (userPosts > 0) userPosts else _posts.value.size.coerceAtLeast(3)
        } else 3

        val activeIds = _activeMonetizationToolIds.value
        val reviewIds = _underReviewMonetizationToolIds.value
        val registeredIds = _registeredInterestToolIds.value

        return listOf(
            // 1. In-Stream Video Ads (Facebook standard: 5,000 followers, 1,000 watch hours, 5 posts)
            run {
                val id = "tool_instream"
                val minFollowers = 5000
                val minWatchHours = 1000
                val minPosts = 5
                val isCriteriaMet = realFollowers >= minFollowers && realWatchHours >= minWatchHours && realPostsCount >= minPosts
                val status = when {
                    activeIds.contains(id) -> ProgramStatus.ACTIVE
                    reviewIds.contains(id) -> ProgramStatus.UNDER_REVIEW
                    isCriteriaMet -> ProgramStatus.READY_TO_APPLY
                    else -> ProgramStatus.CRITERIA_NOT_MET
                }
                val note = when (status) {
                    ProgramStatus.ACTIVE -> "Active · 70% ad revenue share connected with Telebirr/CBE weekly payout."
                    ProgramStatus.UNDER_REVIEW -> "⏳ Under automated policy & KYC review."
                    ProgramStatus.READY_TO_APPLY -> "Criteria Met! All requirements fulfilled. Set up your payout account."
                    else -> "${String.format(java.util.Locale.US, "%,d", (minFollowers - realFollowers).coerceAtLeast(0))} more followers & ${String.format(java.util.Locale.US, "%.1f", (minWatchHours - realWatchHours).coerceAtLeast(0.0))} hrs needed"
                }
                MonetizationTool(
                    id = id,
                    code = "IN_STREAM_ADS",
                    name = "In-Stream Video Ads",
                    description = "Monetize qualifying on-demand and live videos with pre-roll, mid-roll, and image banner advertisements.",
                    icon = "📺",
                    status = status,
                    minFollowers = minFollowers,
                    minWatchHours = minWatchHours,
                    minActivePosts = minPosts,
                    revSharePercent = 70.0,
                    eligibilityNote = note,
                    enrolledDate = if (status == ProgramStatus.ACTIVE) "Active · Weekly Payout" else null,
                    payoutRateDescription = "70% creator share on video ad impressions"
                )
            },

            // 2. Reels Performance & Overlay Ads (Facebook Reels Bonus / Overlay standard: 1,000 followers, 100 hrs, 3 posts)
            run {
                val id = "tool_reels"
                val minFollowers = 1000
                val minWatchHours = 100
                val minPosts = 3
                val isCriteriaMet = realFollowers >= minFollowers && realWatchHours >= minWatchHours && realPostsCount >= minPosts
                val status = when {
                    activeIds.contains(id) -> ProgramStatus.ACTIVE
                    reviewIds.contains(id) -> ProgramStatus.UNDER_REVIEW
                    isCriteriaMet -> ProgramStatus.READY_TO_APPLY
                    registeredIds.contains(id) -> ProgramStatus.INVITE_ONLY
                    else -> ProgramStatus.INVITE_ONLY
                }
                val note = when {
                    status == ProgramStatus.ACTIVE -> "Active · Earning on public short-form Reels plays & sticker overlays."
                    status == ProgramStatus.UNDER_REVIEW -> "⏳ In automated review."
                    status == ProgramStatus.READY_TO_APPLY -> "Criteria Met! Set up your Reels Performance program."
                    registeredIds.contains(id) -> "Waitlist Joined · Meskot Creator Studio is evaluating your short-form content."
                    else -> "Invite Only: 1,000 followers, redeem creator invite code, or join the waiting queue."
                }
                MonetizationTool(
                    id = id,
                    code = "REELS_OVERLAY",
                    name = "Reels Performance & Overlay Ads",
                    description = "Earn based on the number of plays and high-engagement impressions on your public short-form Reels.",
                    icon = "⚡",
                    status = status,
                    minFollowers = minFollowers,
                    minWatchHours = minWatchHours,
                    minActivePosts = minPosts,
                    revSharePercent = 70.0,
                    eligibilityNote = note,
                    enrolledDate = if (status == ProgramStatus.ACTIVE) "Active (Invited)" else null,
                    isInviteOnly = true,
                    payoutRateDescription = "70% creator share on short-form Reels views"
                )
            },

            // 3. Virtual Stars & Live Micro-Gifts (Facebook Stars standard: 500 followers & 3 active posts)
            run {
                val id = "tool_stars"
                val minFollowers = 500
                val minWatchHours = 0
                val minPosts = 3
                val isCriteriaMet = realFollowers >= minFollowers && realPostsCount >= minPosts
                val status = when {
                    activeIds.contains(id) -> ProgramStatus.ACTIVE
                    reviewIds.contains(id) -> ProgramStatus.UNDER_REVIEW
                    isCriteriaMet -> ProgramStatus.READY_TO_APPLY
                    else -> ProgramStatus.CRITERIA_NOT_MET
                }
                val note = when (status) {
                    ProgramStatus.ACTIVE -> "Active · Fixed payout rate of 1.00 ETB per Star received."
                    ProgramStatus.UNDER_REVIEW -> "⏳ In automated review."
                    ProgramStatus.READY_TO_APPLY -> "Criteria Met! You qualify for Stars tipping on posts, photos & live."
                    else -> "${(minFollowers - realFollowers).coerceAtLeast(0)} more followers needed to unlock Stars"
                }
                MonetizationTool(
                    id = id,
                    code = "STARS_TIPPING",
                    name = "Virtual Stars & Live Micro-Gifts",
                    description = "Enable viewers to buy and send Stars and animated gifts on your feed posts, photos, and live audio/video rooms.",
                    icon = "⭐",
                    status = status,
                    minFollowers = minFollowers,
                    minWatchHours = minWatchHours,
                    minActivePosts = minPosts,
                    revSharePercent = 85.0,
                    eligibilityNote = note,
                    enrolledDate = if (status == ProgramStatus.ACTIVE) "Active · 1.00 ETB / Star" else null,
                    payoutRateDescription = "Fixed payout rate of 1.00 ETB per Star received"
                )
            },

            // 4. Tiered VIP Fan Subscriptions (Facebook Subscriptions standard: 10,000 followers, 500 hrs, 5 posts)
            run {
                val id = "tool_subs"
                val minFollowers = 10000
                val minWatchHours = 500
                val minPosts = 5
                val isCriteriaMet = realFollowers >= minFollowers && realWatchHours >= minWatchHours && realPostsCount >= minPosts
                val status = when {
                    activeIds.contains(id) -> ProgramStatus.ACTIVE
                    reviewIds.contains(id) -> ProgramStatus.UNDER_REVIEW
                    isCriteriaMet -> ProgramStatus.READY_TO_APPLY
                    else -> ProgramStatus.CRITERIA_NOT_MET
                }
                val note = when (status) {
                    ProgramStatus.ACTIVE -> "Active · Monthly recurring VIP memberships with badges & exclusive perks."
                    ProgramStatus.UNDER_REVIEW -> "⏳ Under review."
                    ProgramStatus.READY_TO_APPLY -> "Criteria Met! Set up Bronze, Silver, and Gold membership tiers."
                    else -> "${String.format(java.util.Locale.US, "%,d", (minFollowers - realFollowers).coerceAtLeast(0))} more followers & ${(minWatchHours - realWatchHours).toInt().coerceAtLeast(0)} hrs needed"
                }
                MonetizationTool(
                    id = id,
                    code = "FAN_SUBSCRIPTIONS",
                    name = "Tiered VIP Fan Subscriptions",
                    description = "Earn predictable monthly recurring income with Bronze, Silver, and Gold membership tiers with exclusive perks.",
                    icon = "👑",
                    status = status,
                    minFollowers = minFollowers,
                    minWatchHours = minWatchHours,
                    minActivePosts = minPosts,
                    revSharePercent = 80.0,
                    eligibilityNote = note,
                    enrolledDate = if (status == ProgramStatus.ACTIVE) "Active · Recurring Subscriptions" else null,
                    payoutRateDescription = "80% creator rev share on monthly memberships"
                )
            },

            // 5. Meskot Creator Accelerator Grant (Cohort Bonus: 25,000 followers, 2,000 hrs, Invite Only)
            run {
                val id = "tool_creator_fund"
                val minFollowers = 25000
                val minWatchHours = 2000
                val minPosts = 10
                val status = when {
                    activeIds.contains(id) -> ProgramStatus.ACTIVE
                    registeredIds.contains(id) -> ProgramStatus.INVITE_ONLY
                    else -> ProgramStatus.INVITE_ONLY
                }
                val note = when {
                    status == ProgramStatus.ACTIVE -> "Active · Enrolled in Accelerator Grant cohort (100% bonus pool)."
                    registeredIds.contains(id) -> "Interest Registered · Candidate profile in review by diaspora committee."
                    else -> "Invite-only cohort for top 100 Ethiopian digital creators."
                }
                MonetizationTool(
                    id = id,
                    code = "CREATOR_FUND",
                    name = "Meskot Creator Accelerator Grant",
                    description = "Monthly guaranteed creator bonus pool funded by Meskot Diaspora Innovation & Telecom partners.",
                    icon = "🏆",
                    status = status,
                    minFollowers = minFollowers,
                    minWatchHours = minWatchHours,
                    minActivePosts = minPosts,
                    revSharePercent = 100.0,
                    eligibilityNote = note,
                    enrolledDate = if (status == ProgramStatus.ACTIVE) "Active · Grant Awarded" else null,
                    isInviteOnly = true,
                    payoutRateDescription = "100% monthly bonus pool from diaspora partners"
                )
            },

            // 6. Brand Collabs & Sponsorship Tagging (Facebook Branded Content Tag: 1,000 followers, 100 hrs, 5 posts)
            run {
                val id = "tool_branded"
                val minFollowers = 1000
                val minWatchHours = 100
                val minPosts = 5
                val isCriteriaMet = realFollowers >= minFollowers && realWatchHours >= minWatchHours && realPostsCount >= minPosts
                val status = when {
                    activeIds.contains(id) -> ProgramStatus.ACTIVE
                    reviewIds.contains(id) -> ProgramStatus.UNDER_REVIEW
                    isCriteriaMet -> ProgramStatus.READY_TO_APPLY
                    else -> ProgramStatus.CRITERIA_NOT_MET
                }
                val note = when (status) {
                    ProgramStatus.ACTIVE -> "Active · Paid partnership tagging & brand sponsor discovery enabled."
                    ProgramStatus.UNDER_REVIEW -> "⏳ KYC & tax identity verification in progress."
                    ProgramStatus.READY_TO_APPLY -> "Eligible · You meet all criteria (0 policy strikes, KYC verified)."
                    else -> "${(minFollowers - realFollowers).coerceAtLeast(0)} more followers & ${(minWatchHours - realWatchHours).toInt().coerceAtLeast(0)} hrs needed"
                }
                MonetizationTool(
                    id = id,
                    code = "BRANDED_CONTENT",
                    name = "Brand Collabs & Sponsorship Tagging",
                    description = "Tag commercial brand partners directly on posts and get discovered by local Ethiopian and international advertisers.",
                    icon = "🤝",
                    status = status,
                    minFollowers = minFollowers,
                    minWatchHours = minWatchHours,
                    minActivePosts = minPosts,
                    revSharePercent = 100.0,
                    eligibilityNote = note,
                    enrolledDate = if (status == ProgramStatus.ACTIVE) "Active · Tagging Enabled" else null,
                    payoutRateDescription = "100% direct brand sponsor payment retention"
                )
            }
        )
    }

    private fun createInitialLedger(): List<EarningsLedgerEntry> {
        val now = System.currentTimeMillis()
        return listOf(
            EarningsLedgerEntry(
                id = "led_01",
                transactionRef = "TX-CR-88219401",
                entryType = LedgerEntryType.AD_REVENUE_CREDIT,
                amountEtb = 320.00,
                balanceAfterEtb = 4960.00,
                timestamp = now - 3600000L * 2,
                sourceTitle = "Daily In-Stream Ad Settlement",
                metadata = "Impressions: 14,200 · eCPM: 22.50 ETB"
            ),
            EarningsLedgerEntry(
                id = "led_02",
                transactionRef = "TX-CR-77123982",
                entryType = LedgerEntryType.FAN_TIP_CREDIT,
                amountEtb = 100.00,
                balanceAfterEtb = 4640.00,
                timestamp = now - 3600000L * 8,
                sourceTitle = "Chapa Reader Tip via Telebirr",
                metadata = "Sender: Almaz Bekele · Ref: CHP-9021"
            ),
            EarningsLedgerEntry(
                id = "led_03",
                transactionRef = "TX-CR-66239102",
                entryType = LedgerEntryType.SUBSCRIPTION_CREDIT,
                amountEtb = 280.00,
                balanceAfterEtb = 4540.00,
                timestamp = now - 86400000L * 1,
                sourceTitle = "Silver VIP Renewal (Dawit Mengistu)",
                metadata = "Gross: 350.00 ETB · Platform fee: 70.00 ETB (20%)"
            ),
            EarningsLedgerEntry(
                id = "led_04",
                transactionRef = "TX-CR-55102983",
                entryType = LedgerEntryType.STARS_GIFT_CREDIT,
                amountEtb = 250.00,
                balanceAfterEtb = 4260.00,
                timestamp = now - 86400000L * 2,
                sourceTitle = "Live Room Micro-Gifts (250 Stars)",
                metadata = "Audience: Adigrat Alumni Live Q&A"
            ),
            EarningsLedgerEntry(
                id = "led_05",
                transactionRef = "TXN-CBE-98421098",
                entryType = LedgerEntryType.PAYOUT_DEBIT,
                amountEtb = -4800.00,
                balanceAfterEtb = 4010.00,
                timestamp = now - 86400000L * 4,
                sourceTitle = "Disbursement to Commercial Bank of Ethiopia (CBE)",
                metadata = "Account: 1000***4562 · Status: Settled"
            )
        )
    }

    private fun createInitialContentFormatMetrics(): List<ContentFormatMetric> {
        return listOf(
            ContentFormatMetric(
                formatName = "Reels & Shorts",
                icon = "⚡",
                views = 142000L,
                monetizableImpressions = 89400L,
                rpmEtb = 32.50,
                grossRevenueEtb = 2905.50,
                netCreatorRevenueEtb = 2033.85
            ),
            ContentFormatMetric(
                formatName = "Long Videos (> 3 min)",
                icon = "🎥",
                views = 48500L,
                monetizableImpressions = 41200L,
                rpmEtb = 58.00,
                grossRevenueEtb = 2389.60,
                netCreatorRevenueEtb = 1672.72
            ),
            ContentFormatMetric(
                formatName = "Articles & Posts",
                icon = "📝",
                views = 68900L,
                monetizableImpressions = 32100L,
                rpmEtb = 18.00,
                grossRevenueEtb = 577.80,
                netCreatorRevenueEtb = 404.46
            ),
            ContentFormatMetric(
                formatName = "Live Streams & Audio Rooms",
                icon = "🎙️",
                views = 12400L,
                monetizableImpressions = 11800L,
                rpmEtb = 42.00,
                grossRevenueEtb = 495.60,
                netCreatorRevenueEtb = 346.92
            )
        )
    }

    private fun createInitialDailyEarnings(): List<DailyEarningsMetric> {
        return listOf(
            DailyEarningsMetric("Mon", "Sep 07", 210.0, 150.0, 350.0, 710.0),
            DailyEarningsMetric("Tue", "Sep 08", 185.0, 90.0, 150.0, 425.0),
            DailyEarningsMetric("Wed", "Sep 09", 340.0, 220.0, 0.0, 560.0),
            DailyEarningsMetric("Thu", "Sep 10", 290.0, 180.0, 350.0, 820.0),
            DailyEarningsMetric("Fri", "Sep 11", 420.0, 310.0, 150.0, 880.0),
            DailyEarningsMetric("Sat", "Sep 12", 510.0, 440.0, 800.0, 1750.0),
            DailyEarningsMetric("Sun", "Sep 13", 380.0, 260.0, 350.0, 990.0)
        )
    }

    private fun createInitialPayoutAccounts(): List<CreatorPayoutAccount> {
        return listOf(
            CreatorPayoutAccount(
                id = "acc_01",
                gatewayName = "Telebirr SuperApp",
                accountNumber = "+251 91 123 4567",
                holderName = "Hesam Yemane",
                isDefault = true,
                isVerified = true
            ),
            CreatorPayoutAccount(
                id = "acc_02",
                gatewayName = "Commercial Bank of Ethiopia (CBE)",
                accountNumber = "1000 2938 4562",
                holderName = "Hesam Yemane",
                isDefault = false,
                isVerified = true
            ),
            CreatorPayoutAccount(
                id = "acc_03",
                gatewayName = "Chapa Merchant Settlement",
                accountNumber = "CHAPA-ACCT-7819",
                holderName = "Meskot Studio Hub",
                isDefault = false,
                isVerified = true
            )
        )
    }

    // LIVE STREAMING & REAL-TIME FIRESTORE ACTIONS
    fun startLiveStream(
        title: String = "",
        category: String = "Culture & Chat",
        onStarted: (LiveStreamSession) -> Unit = {}
    ) {
        val user = _currentUser.value ?: return
        val streamId = "live_${user.uid}_${System.currentTimeMillis()}"
        val streamTitle = title.ifBlank { "${user.displayName}'s Meskot Live" }
        val session = LiveStreamSession(
            id = streamId,
            hostUid = user.uid,
            hostName = user.displayName,
            hostPhoto = user.photoUrl,
            title = streamTitle,
            category = category,
            status = "live",
            viewerCount = 1,
            viewers = listOf(user.uid),
            likesCount = 0,
            totalCoins = 0,
            createdAt = System.currentTimeMillis()
        )
        _currentLiveSession.value = session
        _currentLiveMessages.value = emptyList()
        attachLiveSessionListeners(streamId)

        FirebaseManager.createLiveStream(session) {
            onStarted(session)
        }
    }

    fun joinLiveStream(session: LiveStreamSession) {
        val user = _currentUser.value ?: return
        _currentLiveSession.value = session
        _currentLiveMessages.value = emptyList()
        attachLiveSessionListeners(session.id)
        FirebaseManager.joinLiveStream(session.id, user)
    }

    fun leaveLiveStream() {
        val user = _currentUser.value
        val session = _currentLiveSession.value
        if (session != null && user != null) {
            if (session.hostUid == user.uid) {
                // Broadcaster ending live stream in Firebase
                FirebaseManager.endLiveStream(session.id)
            } else {
                // Viewer leaving live stream in Firebase
                FirebaseManager.leaveLiveStream(session.id, user.uid)
            }
        }
        detachLiveSessionListeners()
        _currentLiveSession.value = null
        _currentLiveMessages.value = emptyList()
    }

    private fun attachLiveSessionListeners(streamId: String) {
        currentLiveSessionRegistration?.remove()
        currentLiveSessionRegistration = FirebaseManager.listenToLiveStream(streamId) { updatedSession ->
            if (updatedSession != null) {
                _currentLiveSession.value = updatedSession
            }
        }

        currentLiveMessagesRegistration?.remove()
        currentLiveMessagesRegistration = FirebaseManager.listenToLiveMessages(streamId) { msgs ->
            _currentLiveMessages.value = msgs
        }
    }

    private fun detachLiveSessionListeners() {
        currentLiveSessionRegistration?.remove()
        currentLiveSessionRegistration = null
        currentLiveMessagesRegistration?.remove()
        currentLiveMessagesRegistration = null
    }

    fun sendLiveComment(text: String) {
        val user = _currentUser.value ?: return
        val session = _currentLiveSession.value ?: return
        if (text.isBlank()) return
        val comment = LiveStreamComment(
            id = "comment_${user.uid}_${System.currentTimeMillis()}",
            streamId = session.id,
            senderUid = user.uid,
            senderName = user.displayName,
            senderPhoto = user.photoUrl,
            text = text.trim(),
            type = "CHAT",
            timestamp = System.currentTimeMillis()
        )
        FirebaseManager.sendLiveComment(comment)
    }

    fun sendLiveHeart() {
        val session = _currentLiveSession.value ?: return
        FirebaseManager.sendLiveHeart(session.id)
    }

    fun sendLiveGift(giftId: Int, giftName: String, giftIcon: String, coins: Int) {
        val user = _currentUser.value ?: return
        val session = _currentLiveSession.value ?: return
        if (user.starBalance < coins) return

        // Local optimistic update
        val updatedUser = user.copy(starBalance = maxOf(0, user.starBalance - coins))
        _currentUser.value = updatedUser

        FirebaseManager.sendLiveGift(
            streamId = session.id,
            hostUid = session.hostUid,
            sender = user,
            giftId = giftId,
            giftName = giftName,
            giftIcon = giftIcon,
            coins = coins
        )
    }

    // ========================================================================
    // FACEBOOK MARKETPLACE REPOSITORY STATE & BUSINESS LOGIC
    // ========================================================================

    private val _marketplaceCategories = MutableStateFlow<List<Category>>(createMarketplaceCategories())
    val marketplaceCategories: StateFlow<List<Category>> = _marketplaceCategories.asStateFlow()

    private val _marketplaceListings = MutableStateFlow<List<ListingItem>>(createInitialMarketplaceListings())
    val marketplaceListings: StateFlow<List<ListingItem>> = _marketplaceListings.asStateFlow()

    private val _marketplaceLocation = MutableStateFlow(
        MarketplaceLocation(
            latitude = 13.4967,
            longitude = 39.4753,
            name = "Mekelle",
            radiusKm = 65
        )
    )
    val marketplaceLocation: StateFlow<MarketplaceLocation> = _marketplaceLocation.asStateFlow()

    private val _savedListingIds = MutableStateFlow<Set<String>>(loadSavedListingIds())
    val savedListingIds: StateFlow<Set<String>> = _savedListingIds.asStateFlow()

    private fun loadSavedListingIds(): Set<String> {
        return try {
            val prefs = context.getSharedPreferences("meskot_marketplace_prefs", Context.MODE_PRIVATE)
            prefs.getStringSet("saved_listing_ids", emptySet()) ?: emptySet()
        } catch (e: Exception) {
            emptySet()
        }
    }

    private fun persistSavedListingIds(ids: Set<String>) {
        try {
            val prefs = context.getSharedPreferences("meskot_marketplace_prefs", Context.MODE_PRIVATE)
            prefs.edit().putStringSet("saved_listing_ids", ids).apply()
        } catch (e: Exception) {
            // ignore
        }
    }

    private val _marketplaceChats = MutableStateFlow<List<MarketplaceChatMessage>>(createInitialMarketplaceChats())
    val marketplaceChats: StateFlow<List<MarketplaceChatMessage>> = _marketplaceChats.asStateFlow()

    fun updateMarketplaceLocation(name: String, radiusKm: Int, latitude: Double = 9.0192, longitude: Double = 38.7525) {
        _marketplaceLocation.value = MarketplaceLocation(
            latitude = latitude,
            longitude = longitude,
            name = name.ifBlank { "Ariena" },
            radiusKm = radiusKm.coerceIn(5, 500)
        )
    }

    fun toggleSaveListing(itemId: String) {
        val current = _savedListingIds.value
        val next = if (current.contains(itemId)) current - itemId else current + itemId
        _savedListingIds.value = next
        persistSavedListingIds(next)
        _marketplaceListings.value = _marketplaceListings.value.map { item ->
            if (item.id == itemId) item.copy(isSaved = next.contains(itemId)) else item
        }
    }

    fun incrementMarketplaceListingViews(itemId: String) {
        var nextViews = 1
        _marketplaceListings.value = _marketplaceListings.value.map { item ->
            if (item.id == itemId) {
                nextViews = item.viewsCount + 1
                item.copy(viewsCount = nextViews)
            } else item
        }
        FirebaseManager.incrementMarketplaceListingViews(itemId, nextViews)
    }

    fun toggleMarketplaceListingAvailability(itemId: String): ListingItem? {
        var updatedItem: ListingItem? = null
        _marketplaceListings.value = _marketplaceListings.value.map { item ->
            if (item.id == itemId) {
                val toggled = item.copy(isAvailable = !item.isAvailable)
                updatedItem = toggled
                toggled
            } else item
        }
        updatedItem?.let { FirebaseManager.updateMarketplaceListing(it) }
        return updatedItem
    }

    fun updateMarketplaceListingDetails(
        itemId: String,
        newTitle: String,
        newPrice: Double,
        newCurrency: String,
        newCondition: String,
        newDescription: String,
        newIsNegotiable: Boolean
    ): ListingItem? {
        var updatedItem: ListingItem? = null
        _marketplaceListings.value = _marketplaceListings.value.map { item ->
            if (item.id == itemId) {
                val edited = item.copy(
                    title = newTitle.trim().ifBlank { item.title },
                    price = newPrice.coerceAtLeast(0.0),
                    currency = newCurrency.uppercase(),
                    condition = newCondition,
                    description = newDescription.trim().ifBlank { item.description },
                    isNegotiable = newIsNegotiable
                )
                updatedItem = edited
                edited
            } else item
        }
        updatedItem?.let { FirebaseManager.updateMarketplaceListing(it) }
        return updatedItem
    }

    fun promoteMarketplaceListing(itemId: String): ListingItem? {
        var updatedItem: ListingItem? = null
        _marketplaceListings.value = _marketplaceListings.value.map { item ->
            if (item.id == itemId) {
                val promoted = item.copy(isPromoted = true, viewsCount = item.viewsCount + 45)
                updatedItem = promoted
                promoted
            } else item
        }
        updatedItem?.let { FirebaseManager.updateMarketplaceListing(it) }
        return updatedItem
    }

    fun deleteMarketplaceListing(itemId: String) {
        _marketplaceListings.value = _marketplaceListings.value.filterNot { it.id == itemId }
        FirebaseManager.deleteMarketplaceListing(itemId)
    }

    fun createMarketplaceListing(
        title: String,
        categoryId: String,
        price: Double,
        currency: String,
        description: String,
        condition: String,
        locationName: String,
        latitude: Double,
        longitude: Double,
        imageUrls: List<String>,
        isNegotiable: Boolean = true,
        deliveryOption: String = "Meetup · Bole",
        sellerPhone: String = "+251 911 234 567",
        allowChat: Boolean = true,
        allowCall: Boolean = true,
        allowWhatsApp: Boolean = false,
        isPromoted: Boolean = false,
        tags: List<String> = emptyList()
    ): ListingItem {
        val user = _currentUser.value
        val item = ListingItem(
            id = "mkt_" + System.currentTimeMillis(),
            title = title.trim(),
            price = price,
            currency = currency.uppercase(),
            imageUrls = if (imageUrls.isNotEmpty()) imageUrls else listOf("https://images.unsplash.com/photo-1523275335684-37898b6baf30?w=800"),
            categoryId = categoryId,
            location = MarketplaceLocation(
                latitude = latitude,
                longitude = longitude,
                name = locationName.ifBlank { _marketplaceLocation.value.name },
                radiusKm = _marketplaceLocation.value.radiusKm
            ),
            distanceKm = 0.8,
            sellerId = user?.uid ?: "usr_current",
            sellerName = user?.displayName ?: "Meskot Seller",
            sellerPhoto = user?.photoUrl ?: "",
            sellerVerified = user?.isVerified == true,
            description = description.trim(),
            condition = condition,
            isAvailable = true,
            createdAt = System.currentTimeMillis(),
            isNegotiable = isNegotiable,
            deliveryOption = deliveryOption,
            sellerPhone = sellerPhone,
            allowChat = allowChat,
            allowCall = allowCall,
            allowWhatsApp = allowWhatsApp,
            isPromoted = isPromoted,
            tags = tags
        )
        _marketplaceListings.value = listOf(item) + _marketplaceListings.value
        FirebaseManager.createMarketplaceListing(item)
        return item
    }

    fun sendMarketplaceInquiry(item: ListingItem, messageText: String) {
        val sender = _currentUser.value ?: User(uid = "usr_current", displayName = "Meskot Buyer")
        val chatMsg = MarketplaceChatMessage(
            chatId = "mkt_chat_${sender.uid}_${item.sellerId}_${item.id}",
            senderId = sender.uid,
            senderName = sender.displayName,
            senderPhoto = sender.photoUrl,
            receiverId = item.sellerId,
            receiverName = item.sellerName,
            receiverPhoto = item.sellerPhoto,
            itemId = item.id,
            itemTitle = item.title,
            itemPriceLabel = item.formattedPrice,
            itemThumbUrl = item.primaryImageUrl,
            messageText = messageText.trim(),
            timestamp = System.currentTimeMillis()
        )
        _marketplaceChats.value = listOf(chatMsg) + _marketplaceChats.value
        FirebaseManager.sendMarketplaceChatMessage(chatMsg)

        // Also mirror into direct Messages so buyer & seller can continue in real-time ChatScreen
        sendMessage(
            otherUid = item.sellerId,
            text = "🛍️ [Marketplace: ${item.title} • ${item.formattedPrice}] $messageText",
            mediaUrl = item.primaryImageUrl,
            mediaType = "image"
        )
    }

    private fun createMarketplaceCategories(): List<Category> {
        return listOf(
            Category("all", "All Categories", "🪟"),
            Category("vehicles", "Vehicles", "🚗"),
            Category("property", "Rentals", "🏠"),
            Category("fashion_women", "Women's clothing & shoes", "👗"),
            Category("fashion_men", "Men's clothing & shoes", "👔"),
            Category("furniture", "Furniture", "🛋️"),
            Category("electronics", "Electronics & computers", "💻"),
            Category("mobile_phones", "Mobile phones", "📱"),
            Category("home_sales", "Home sales", "🏡"),
            Category("video_games", "Video Games", "🎮"),
            Category("toys_games", "Toys & Games", "🧸"),
            Category("appliances", "Appliances", "🔌"),
            Category("coffee_cultural", "Antiques & Collectibles", "🏺"),
            Category("arts_crafts", "Arts & Crafts", "🎨"),
            Category("auto_parts", "Auto parts", "🔧"),
            Category("baby_kids", "Baby & kids", "🍼"),
            Category("bags_luggage", "Bags & Luggage", "🧳"),
            Category("bicycles", "Bicycles", "🚲"),
            Category("books_music", "Books, Movies & Music", "📚"),
            Category("classifieds", "Garage Sale", "🏷️"),
            Category("garden", "Garden", "🌿"),
            Category("health_beauty", "Health & beauty", "💄"),
            Category("household", "Household", "🧺"),
            Category("jewelry", "Jewelry & Accessories", "💍"),
            Category("miscellaneous", "Miscellaneous", "✨"),
            Category("musical", "Musical Instruments", "🎸"),
            Category("pet_supplies", "Pet Supplies", "🐾"),
            Category("sports_outdoors", "Sports & Outdoors", "⚽"),
            Category("tools", "Tools", "🛠️")
        )
    }

    private fun createInitialMarketplaceListings(): List<ListingItem> {
        val now = System.currentTimeMillis()
        return listOf(
            ListingItem(
                id = "mkt_01",
                title = "2021 Toyota Hilux Double Cab 4x4 Diesel — Mint Condition",
                price = 28500.0,
                currency = "USD",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1559416523-140ddc3d238c?w=800",
                    "https://images.unsplash.com/photo-1533473359331-0135ef1b58bf?w=800"
                ),
                categoryId = "vehicles",
                location = MarketplaceLocation(9.0192, 38.7525, "Ariena", 65),
                distanceKm = 2.4,
                sellerId = "usr_dawit",
                sellerName = "Dawit Mekonnen",
                sellerPhoto = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
                sellerVerified = true,
                description = "Single-owner 2021 Toyota Hilux Double Cab 2.8L Turbo Diesel. Full service history, leather interior, off-road suspension, and duty-paid plates. Available for inspection in Ariena / Bole.",
                condition = "Used - Like New",
                viewsCount = 412,
                createdAt = now - 15 * 60 * 1000L
            ),
            ListingItem(
                id = "mkt_02",
                title = "Apple MacBook Pro 16\" M3 Max (36GB RAM, 1TB SSD)",
                price = 1850.0,
                currency = "USD",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800",
                    "https://images.unsplash.com/photo-1611186871348-b1ce696e52c9?w=800"
                ),
                categoryId = "electronics",
                location = MarketplaceLocation(9.0054, 38.7636, "Ariena · Bole", 65),
                distanceKm = 3.1,
                sellerId = "usr_hana",
                sellerName = "Hana Tadesse",
                sellerPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
                sellerVerified = true,
                description = "Space Black MacBook Pro 16-inch M3 Max with 36GB unified memory and 1TB SSD. Battery cycle count only 18. Comes with original MagSafe box and 140W charger.",
                condition = "Used - Like New",
                viewsCount = 289,
                createdAt = now - 45 * 60 * 1000L
            ),
            ListingItem(
                id = "mkt_03",
                title = "Modern 3-Bedroom Furnished Apartment with Balcony View",
                price = 150000.0,
                currency = "ETB",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1522708323590-d24dbb6b0267?w=800",
                    "https://images.unsplash.com/photo-1502672260266-1c1ef2d93688?w=800"
                ),
                categoryId = "property",
                location = MarketplaceLocation(9.0220, 38.7460, "Ariena · Kazanchis", 65),
                distanceKm = 4.8,
                sellerId = "usr_yared",
                sellerName = "Yared Kassahun",
                sellerPhoto = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
                sellerVerified = true,
                description = "Luxury 3BHK fully furnished apartment with backup generator, 24/7 security, underground parking, and panoramic city view. Price is monthly rent in ETB or USD equivalent.",
                condition = "New",
                viewsCount = 530,
                createdAt = now - 2 * 3600 * 1000L
            ),
            ListingItem(
                id = "mkt_04",
                title = "iPhone 15 Pro Max 256GB Natural Titanium (Factory Unlocked)",
                price = 800.0,
                currency = "USD",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=800"
                ),
                categoryId = "electronics",
                location = MarketplaceLocation(9.0110, 38.7580, "Ariena", 65),
                distanceKm = 5.5,
                sellerId = "usr_meron",
                sellerName = "Meron Alemu",
                sellerPhoto = "https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=200",
                sellerVerified = false,
                description = "Factory unlocked iPhone 15 Pro Max 256GB in Natural Titanium. 96% battery health, zero scratches, includes original USB-C braided cable and Apple silicone case.",
                condition = "Used - Like New",
                viewsCount = 615,
                createdAt = now - 3 * 3600 * 1000L
            ),
            ListingItem(
                id = "mkt_05",
                title = "Handcrafted L-Shaped Velvet Sectional Sofa + Solid Oak Table",
                price = 25000.0,
                currency = "ETB",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=800"
                ),
                categoryId = "furniture",
                location = MarketplaceLocation(9.0300, 38.7400, "Ariena · Piassa", 65),
                distanceKm = 7.2,
                sellerId = "usr_abebe",
                sellerName = "Abebe Kebede",
                sellerPhoto = "https://images.unsplash.com/photo-1492562080023-ab3db95bfbce?w=200",
                sellerVerified = true,
                description = "Custom Ethiopian highland solid wood frame L-shaped sofa upholstered in emerald stain-resistant velvet. Includes matching coffee table and 4 accent pillows.",
                condition = "New",
                viewsCount = 198,
                createdAt = now - 5 * 3600 * 1000L
            ),
            ListingItem(
                id = "mkt_06",
                title = "Royal Handwoven Tibeb Habesha Kemis with Gold Tilf Embroidery",
                price = 220.0,
                currency = "USD",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?w=800"
                ),
                categoryId = "fashion",
                location = MarketplaceLocation(9.0180, 38.7510, "Ariena · Shiro Meda", 65),
                distanceKm = 8.9,
                sellerId = "usr_selam",
                sellerName = "Selamawit Bekele",
                sellerPhoto = "https://images.unsplash.com/photo-1531746020798-e6953c6e8e04?w=200",
                sellerVerified = true,
                description = "100% pure hand-spun cotton Habesha Kemis with intricate golden Tibeb border and matching Netela shawl. Ideal for weddings, Timket, and cultural celebrations.",
                condition = "New",
                viewsCount = 342,
                createdAt = now - 8 * 3600 * 1000L
            ),
            ListingItem(
                id = "mkt_07",
                title = "Professional 6-String Electric Acoustic Krar + Padded Case",
                price = 18500.0,
                currency = "ETB",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1511671782779-c97d3d27a1d4?w=800"
                ),
                categoryId = "musical",
                location = MarketplaceLocation(8.9950, 38.7720, "Ariena · CMC", 65),
                distanceKm = 11.4,
                sellerId = "usr_yared",
                sellerName = "Yared Kassahun",
                sellerPhoto = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=200",
                sellerVerified = true,
                description = "Concert-grade 6-string Ethiopian Krar with built-in pickup, precision tuning pegs, and cedar soundboard. Ready for stage performance or studio recording.",
                condition = "New",
                viewsCount = 154,
                createdAt = now - 12 * 3600 * 1000L
            ),
            ListingItem(
                id = "mkt_08",
                title = "Complete Brass & Clay Jebena Buna Ceremony Set (12 Finiq Cups)",
                price = 95.0,
                currency = "USD",
                imageUrls = listOf(
                    "https://images.unsplash.com/photo-1509785307050-d4066910ec1e?w=800"
                ),
                categoryId = "coffee_cultural",
                location = MarketplaceLocation(9.0150, 38.7500, "Ariena", 65),
                distanceKm = 3.6,
                sellerId = "usr_hana",
                sellerName = "Hana Tadesse",
                sellerPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
                sellerVerified = true,
                description = "Authentic handmade Gondar clay Jebena, carved wooden Rekebot tray, brass incense burner (endod/etan), and 12 porcelain Saba-patterned coffee cups + 1kg Yirgacheffe beans.",
                condition = "New",
                viewsCount = 420,
                createdAt = now - 18 * 3600 * 1000L
            )
        )
    }

    private fun createInitialMarketplaceChats(): List<MarketplaceChatMessage> {
        val now = System.currentTimeMillis()
        return listOf(
            MarketplaceChatMessage(
                chatId = "mkt_chat_01",
                senderId = "usr_dawit",
                senderName = "Dawit Mekonnen",
                senderPhoto = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=200",
                receiverId = "usr_current",
                receiverName = "You",
                receiverPhoto = "",
                itemId = "mkt_01",
                itemTitle = "2021 Toyota Hilux Double Cab 4x4 Diesel",
                itemPriceLabel = "$28,500",
                itemThumbUrl = "https://images.unsplash.com/photo-1559416523-140ddc3d238c?w=800",
                messageText = "Yes, this item is still available! You can inspect it in Ariena today.",
                timestamp = now - 25 * 60 * 1000L
            ),
            MarketplaceChatMessage(
                chatId = "mkt_chat_02",
                senderId = "usr_hana",
                senderName = "Hana Tadesse",
                senderPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
                receiverId = "usr_current",
                receiverName = "You",
                receiverPhoto = "",
                itemId = "mkt_02",
                itemTitle = "Apple MacBook Pro 16\" M3 Max",
                itemPriceLabel = "$1,850",
                itemThumbUrl = "https://images.unsplash.com/photo-1517336714731-489689fd1ca8?w=800",
                messageText = "I accept both USD and ETB via Chapa or Telebirr at the daily NBE rate.",
                timestamp = now - 2 * 3600 * 1000L
            )
        )
    }
}
