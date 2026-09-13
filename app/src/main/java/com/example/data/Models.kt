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
    val uid: String,
    val displayName: String,
    val email: String = "",
    val bio: String = "Engineer is a problem solver",
    val photoUrl: String = "",
    val coverPhotoUrl: String = "",
    val isAdmin: Boolean = false,
    val isSuspended: Boolean = false,
    val lastSeen: Long = System.currentTimeMillis(),
    val createdAt: Long = System.currentTimeMillis(),
    val gender: String = "",
    val birthDate: String = "May 11, 1994",
    val phoneNumber: String = "",
    val starBalance: Int = 1250,
    val creatorGrossEarnings: Double = 6200.0,
    val creatorNetBalance: Double = 4960.0,
    val vipMemberships: Map<String, String> = emptyMap(), // creatorUid -> Tier code
    val followersCount: Int = 8500,
    val followingCount: Int = 3700,
    val profession: String = "Public figure",
    val location: String = "Calgary, Alberta",
    val hometown: String = "Calgary, Alberta",
    val workplace: String = "Adigrat university _Engineering Sciences",
    val workRole: String = "Civil Engineering",
    val education: String = "Adigrat University",
    val educationClass: String = "Class of 2018"
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
    val isSaved: Boolean = false
)

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
    val editedAt: Long? = null
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


