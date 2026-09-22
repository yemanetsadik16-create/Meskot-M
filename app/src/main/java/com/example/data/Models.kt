package com.example.data

enum class MembershipTier(
    val code: String,
    val label: String,
    val badge: String,
    val monthlyPriceEtb: Int,
    val monthlyPriceUsd: Double,
    val perks: List<String>
) {
    FREE("FREE", "Free Fan", "", 0, 0.0, listOf("Public posts", "Comments & likes")),
    BRONZE("BRONZE", "Bronze VIP", "🥉", 150, 4.99, listOf("Bronze VIP Badge", "Exclusive VIP feed posts", "Supporter recognition")),
    SILVER("SILVER", "Silver VIP", "🥈", 350, 9.99, listOf("All Bronze perks", "Silver VIP Badge", "Priority DM replies", "Monthly creator Q&A")),
    GOLD("GOLD", "Gold VIP", "🥇", 800, 24.99, listOf("All Silver perks", "Gold VIP Crown Badge", "1-on-1 direct audio call", "Early access to videos"));

    val perksSummary: String get() = perks.joinToString(" • ")

    companion object {
        fun fromCode(code: String): MembershipTier = values().find { it.code.equals(code, ignoreCase = true) } ?: FREE
    }
}

data class User(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val bio: String = "",
    val photoUrl: String = "",
    val coverPhotoUrl: String = "",
    val isAdmin: Boolean = false,
    val isSuspended: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val gender: String = "",
    val birthDate: String = "",
    val phoneNumber: String = "",
    val starBalance: Int = 0,
    val creatorGrossEarnings: Double = 0.0,
    val creatorNetBalance: Double = 0.0,
    val vipMemberships: Map<String, String> = emptyMap(), // creatorUid -> Tier code
    val followersCount: Int = 0,
    val followingCount: Int = 0,
    val profession: String = "",
    val location: String = "",
    val hometown: String = "",
    val workplace: String = "",
    val workRole: String = "",
    val education: String = "",
    val educationClass: String = "",
    val watchHours: Double = 0.0,
    val kycVerified: Boolean = false,
    val policyStrikes: Int = 0,
    val payoutDestinationAccount: String = "",
    val isVerified: Boolean = false,
    val verificationStatus: VerificationStatus = VerificationStatus.NONE,
    val verificationSubscribedAt: Long? = null,
    val verificationExpiresAt: Long? = null,
    val verificationPlan: String = "MONTHLY_STANDARD",
    val verificationPaymentMethod: String = "",
    val savedPostIds: List<String> = emptyList(),
    val subscribedPostIds: List<String> = emptyList()
)

enum class VerificationStatus {
    NONE,
    PENDING,
    VERIFIED,
    EXPIRED
}

data class MetaVerifiedSubscription(
    val id: String,
    val userId: String,
    val status: VerificationStatus,
    val planId: String = "meta_verified_monthly",
    val priceUsd: Double = 14.99,
    val priceEtb: Double = 499.0,
    val paymentProvider: String = "GOOGLE_PLAY",
    val purchaseToken: String = "",
    val orderId: String = "",
    val subscribedAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 30L * 24 * 3600 * 1000,
    val autoRenew: Boolean = true
)

data class SharedPostPreview(
    val postId: String,
    val authorName: String,
    val authorPhoto: String,
    val text: String,
    val mediaUrls: List<String> = emptyList(),
    val createdAt: Long
)

data class Post(
    val id: String,
    val uid: String,
    val authorName: String,
    val authorPhoto: String = "",
    val text: String = "",
    val mediaUrls: List<String> = emptyList(),
    val bgColorIndex: Int = 0, // 0: Normal card, 1..5: Gradient backgrounds
    val visibility: String = "public", // "public", "friends", "onlyme"
    val minTierRequired: String = "FREE", // "FREE", "BRONZE", "SILVER", "GOLD"
    val isBoosted: Boolean = false,
    val boostMultiplier: Double = 1.0,
    val boostDailyBudget: Double = 0.0,
    val boostDaysRemaining: Int = 0,
    val reactions: Map<String, String> = emptyMap(), // uid -> "like", "love", "haha", "wow", "sad", "angry"
    val commentCount: Int = 0,
    val tipTotal: Double = 0.0,
    val starsTotal: Int = 0,
    val sharedPost: SharedPostPreview? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val editedAt: Long? = null,
    val isSaved: Boolean = false,
    val isAuthorVerified: Boolean = false,
    val sharesCount: Int = 0,
    val postType: String = "POST", // "POST", "PHOTO", "REEL"
    val videoUrl: String = "",
    val audioTrackTitle: String = "",
    val viewsCount: Int = 0
)

data class StoryItem(
    val id: String,
    val uid: String,
    val authorName: String,
    val authorPhoto: String = "",
    val mediaUrl: String,
    val caption: String = "",
    val filterName: String = "Normal",
    val createdAt: Long = System.currentTimeMillis(),
    val expiresAt: Long = System.currentTimeMillis() + 24 * 60 * 60 * 1000L,
    val viewers: List<String> = emptyList(),
    val likes: Map<String, Boolean> = emptyMap()
) {
    fun isExpired(nowMs: Long = System.currentTimeMillis()): Boolean = nowMs >= expiresAt

    fun formattedRemaining(nowMs: Long = System.currentTimeMillis()): String {
        val remainingMs = expiresAt - nowMs
        if (remainingMs <= 0) return "Expired"
        val hours = remainingMs / (3600 * 1000L)
        val minutes = (remainingMs % (3600 * 1000L)) / (60 * 1000L)
        return when {
            hours > 0 -> "${hours}h left"
            minutes > 0 -> "${minutes}m left"
            else -> "< 1m left"
        }
    }
}

data class Comment(
    val id: String,
    val postId: String,
    val uid: String,
    val authorName: String,
    val authorPhoto: String = "",
    val text: String,
    val parentId: String? = null,
    val likes: Map<String, Boolean> = emptyMap(),
    val createdAt: Long = System.currentTimeMillis(),
    val editedAt: Long? = null,
    val isAuthorVerified: Boolean = false
)

data class GroupItem(
    val id: String,
    val name: String,
    val description: String,
    val createdBy: String,
    val memberCount: Int = 1,
    val isJoined: Boolean = false,
    val coverColorHex: String = "#B8863A"
)

data class AlbumItem(
    val id: String,
    val uid: String,
    val title: String,
    val count: Int = 0,
    val coverUrl: String = "",
    val photos: List<String> = emptyList()
)

data class ConversationItem(
    val convoId: String,
    val otherUser: User,
    val lastMessage: String,
    val lastMessageAt: Long,
    val unreadCount: Int = 0
)

data class ChatMessage(
    val id: String,
    val convoId: String,
    val fromUid: String,
    val toUid: String = "",
    val text: String,
    val mediaUrl: String? = null,
    val mediaType: String? = null, // "image", "file", "audio", "like"
    val fileName: String? = null,
    val fileSize: String? = null,
    val isCallLog: Boolean = false,
    val callType: String = "audio", // "audio", "video"
    val callStatus: String = "completed", // "missed", "completed"
    val callDurationSec: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    val editedAt: Long? = null
)

data class FriendRequest(
    val id: String,
    val fromUid: String,
    val fromName: String,
    val fromPhoto: String = "",
    val toUid: String,
    val status: String = "pending", // "pending", "accepted", "declined"
    val createdAt: Long = System.currentTimeMillis()
)

data class NotificationItem(
    val id: String,
    val fromUid: String,
    val fromName: String,
    val fromPhoto: String = "",
    val toUid: String = "",
    val text: String = "",
    val type: String, // "like", "comment", "friend_request", "friend_accept", "message", "share", "tip", "reaction"
    val targetId: String? = null,
    val reactionType: String? = null,
    val amount: Double? = null,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ReactionType(val code: String, val emoji: String, val labelKey: String) {
    LIKE("like", "👍", "reactLike"),
    LOVE("love", "❤️", "reactLove"),
    HAHA("haha", "😆", "reactHaha"),
    WOW("wow", "😮", "reactWow"),
    SAD("sad", "😢", "reactSad"),
    ANGRY("angry", "😡", "reactAngry")
}

data class CallSession(
    val callId: String = "",
    val callerUid: String = "",
    val callerName: String = "",
    val callerPhoto: String = "",
    val receiverUid: String = "",
    val receiverName: String = "",
    val receiverPhoto: String = "",
    val callType: String = "audio", // "audio", "video"
    val status: String = "ringing", // "ringing", "accepted", "rejected", "ended", "missed"
    val roomUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val startedAt: Long? = null,
    val endedAt: Long? = null
)

data class BoostCampaign(
    val id: String,
    val postId: String,
    val creatorUid: String,
    val dailyBudgetEtb: Double,
    val durationDays: Int,
    val targetLocations: List<String>,
    val minAge: Int = 18,
    val maxAge: Int = 45,
    val interests: List<String> = emptyList(),
    val totalBudgetEtb: Double,
    val estimatedReachPerDay: Int,
    val boostMultiplier: Double,
    val createdAt: Long = System.currentTimeMillis()
)

data class AdCampaign(
    val id: String,
    val name: String,
    val objective: String, // "AWARENESS", "TRAFFIC", "CONVERSIONS"
    val status: String, // "ACTIVE", "PAUSED"
    val dailyBudgetEtb: Double,
    val totalSpentEtb: Double,
    val impressions: Int,
    val clicks: Int,
    val conversions: Int,
    val ctr: Double,
    val avgCpcEtb: Double,
    val cpmEtb: Double,
    val headline: String,
    val primaryText: String,
    val mediaUrl: String,
    val ctaText: String,
    val destinationUrl: String,
    val advertiserName: String,
    val advertiserAvatar: String,
    val targetAudience: String = "Ethiopia & Global Diaspora (18-50)",
    val createdAt: Long = System.currentTimeMillis()
)

data class CreatorPayoutRecord(
    val id: String,
    val creatorUid: String,
    val amountEtb: Double,
    val platformFeeEtb: Double,
    val netAmountEtb: Double,
    val payoutMethod: String, // "Telebirr", "CBE Birr", "Chapa Direct", "Stripe Connect"
    val status: String, // "COMPLETED", "PROCESSING"
    val transactionRef: String,
    val timestamp: Long = System.currentTimeMillis()
) {
    val method: String get() = payoutMethod
    val referenceId: String get() = transactionRef
    val createdAt: String get() = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.US).format(java.util.Date(timestamp))
}

enum class ProgramStatus(val label: String) {
    ACTIVE("Active"),
    INVITE_ONLY("Invite Only"),
    READY_TO_APPLY("Criteria Met"),
    UNDER_REVIEW("Under Review"),
    CRITERIA_NOT_MET("Ineligible"),
    SUSPENDED("Suspended")
}

data class MonetizationTool(
    val id: String,
    val code: String,
    val name: String,
    val description: String,
    val icon: String,
    val status: ProgramStatus,
    val minFollowers: Int,
    val minWatchHours: Int,
    val minActivePosts: Int = 5,
    val revSharePercent: Double = 70.0,
    val eligibilityNote: String = "",
    val enrolledDate: String? = null,
    val isInviteOnly: Boolean = false,
    val payoutRateDescription: String = "",
    val policyCheckPassed: Boolean = true
) {
    fun isFollowersMet(followers: Int): Boolean = followers >= minFollowers
    fun isWatchHoursMet(watchHours: Double): Boolean = minWatchHours == 0 || watchHours >= minWatchHours
    fun isPostsMet(postsCount: Int): Boolean = postsCount >= minActivePosts
    fun isAllCriteriaMet(followers: Int, watchHours: Double, postsCount: Int): Boolean =
        isFollowersMet(followers) && isWatchHoursMet(watchHours) && isPostsMet(postsCount) && policyCheckPassed
}

enum class LedgerEntryType(val label: String, val isCredit: Boolean) {
    AD_REVENUE_CREDIT("In-Stream & Reels Ad Share", true),
    FAN_TIP_CREDIT("Direct Chapa & Telebirr Tips", true),
    CHAPA_DEPOSIT_CREDIT("Chapa Real Funds Deposit", true),
    SUBSCRIPTION_CREDIT("VIP Fan Subscriptions", true),
    STARS_GIFT_CREDIT("Virtual Stars & Gifts", true),
    BOOST_POST_DEBIT("Post Promotion Boost", false),
    CREATOR_SUPPORT_DEBIT("Creator Support Tip", false),
    AD_CAMPAIGN_DEBIT("Ad Campaign Budget", false),
    PAYOUT_DEBIT("Disbursement / Cashout", false),
    PLATFORM_FEE_DEBIT("Platform Maintenance Fee", false),
    REVERSAL_CREDIT("Settlement Reversal / Refund", true)
}

data class EarningsLedgerEntry(
    val id: String,
    val transactionRef: String,
    val entryType: LedgerEntryType,
    val amountEtb: Double,
    val balanceAfterEtb: Double,
    val timestamp: Long = System.currentTimeMillis(),
    val sourceTitle: String,
    val metadata: String = ""
) {
    val formattedDate: String get() = java.text.SimpleDateFormat("MMM dd, HH:mm", java.util.Locale.US).format(java.util.Date(timestamp))
}

data class ContentFormatMetric(
    val formatName: String,
    val icon: String,
    val views: Long,
    val monetizableImpressions: Long,
    val rpmEtb: Double,
    val grossRevenueEtb: Double,
    val netCreatorRevenueEtb: Double
)

data class DailyEarningsMetric(
    val dayLabel: String,
    val dateStr: String,
    val adShareEtb: Double,
    val starsEtb: Double,
    val subsEtb: Double,
    val totalEtb: Double
)

data class CreatorPayoutAccount(
    val id: String,
    val gatewayName: String,
    val accountNumber: String,
    val holderName: String,
    val isDefault: Boolean = true,
    val isVerified: Boolean = true
)

data class ChapaGatewayConfig(
    val publicKey: String = "CHAPUBK_TEST-1PW1FKvNMh2tx4k5hHPibEZA4A6GPpRc",
    val secretKey: String = "",
    val isLiveMode: Boolean = false,
    val merchantName: String = "Meskot Media & Creator Studio",
    val defaultCurrency: String = "ETB",
    val isConnected: Boolean = true,
    val supportedMethods: List<String> = listOf("Telebirr", "CBE Birr", "eBirr", "M-Pesa", "Cards")
)

/**
 * Firebase Firestore Live Stream Session Model
 */
data class LiveStreamSession(
    val id: String = "",
    val hostUid: String = "",
    val hostName: String = "",
    val hostPhoto: String = "",
    val title: String = "Meskot Live",
    val category: String = "Culture & Chat",
    val status: String = "live", // "live", "ended"
    val viewerCount: Int = 1,
    val viewers: List<String> = emptyList(),
    val likesCount: Int = 0,
    val totalCoins: Int = 0,
    val roomUrl: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val endedAt: Long? = null
) {
    val isLive: Boolean get() = status == "live"
    val formattedViewers: String get() {
        return if (viewerCount >= 1000) {
            String.format("%.1fk", viewerCount / 1000.0) + " Viewers"
        } else {
            "$viewerCount Viewers"
        }
    }
}

/**
 * Real-time Live Stream Message / Comment / Gift Model in Firestore
 */
data class LiveStreamComment(
    val id: String = "",
    val streamId: String = "",
    val senderUid: String = "",
    val senderName: String = "",
    val senderPhoto: String = "",
    val text: String = "",
    val type: String = "CHAT", // "CHAT", "GIFT", "JOIN", "LIKE"
    val giftId: Int? = null,
    val giftName: String? = null,
    val giftIcon: String? = null,
    val giftCoins: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * Server-backed Real-time User Insights Model
 */
data class UserInsightsData(
    val uid: String = "",
    val postReach: Long = 2480L,
    val postReachGrowthPercent: Double = 128.0,
    val profileViewsWeek: Int = 84,
    val profileViewsTotal: Long = 1420L,
    val profileViewsGrowthPercent: Double = 34.5,
    val interactionRate: Double = 9.4,
    val interactionRateStatus: String = "Above average",
    val topLanguages: List<Pair<String, Int>> = listOf("Amharic" to 64, "English" to 36),
    val impressionsTotal: Long = 6840L,
    val followersReachPercent: Int = 42,
    val nonFollowersReachPercent: Int = 58,
    val postReachCount: Long = 1350L,
    val reelsReachCount: Long = 3890L,
    val storiesReachCount: Long = 920L,
    val isServerSynced: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)

data class DemographicsItem(
    val label: String,
    val percentage: Int,
    val count: Int = 0
)

/**
 * Server-backed Real-time User Engagement and Audience Demographics Model
 */
data class UserEngagementData(
    val uid: String = "",
    val topCities: List<DemographicsItem> = listOf(
        DemographicsItem("Addis Ababa", 52, 624),
        DemographicsItem("Washington D.C.", 18, 216),
        DemographicsItem("Hawassa", 12, 144),
        DemographicsItem("Toronto", 8, 96)
    ),
    val topCountries: List<DemographicsItem> = listOf(
        DemographicsItem("Ethiopia", 74, 888),
        DemographicsItem("United States", 16, 192),
        DemographicsItem("Canada", 6, 72),
        DemographicsItem("Others", 4, 48)
    ),
    val ageDistribution: List<DemographicsItem> = listOf(
        DemographicsItem("18–24", 28, 336),
        DemographicsItem("25–34", 46, 552),
        DemographicsItem("35–44", 18, 216),
        DemographicsItem("45+", 8, 96)
    ),
    val genderDistribution: List<DemographicsItem> = listOf(
        DemographicsItem("Male", 54, 648),
        DemographicsItem("Female", 46, 552)
    ),
    val totalReactions: Int = 186,
    val totalComments: Int = 42,
    val totalShares: Int = 29,
    val totalTipsEtb: Double = 350.0,
    val peakActiveTime: String = "7:00 PM – 10:00 PM EAT",
    val isServerSynced: Boolean = true,
    val lastUpdated: Long = System.currentTimeMillis()
)




