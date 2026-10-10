package com.example.notifications

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

/**
 * Step 2: Firebase Cloud Messaging Service (`FirebaseMessagingService`) that parses incoming
 * remote push payloads and triggers the Facebook-style rich notification via [NotificationHelper].
 *
 * Expected FCM `data` payload keys:
 * - `senderName` or `title`: Display name of the sender (e.g. "Selamawit Tadesse")
 * - `messageBody` or `body`: Message or social notification text (e.g. "Is this still available?")
 * - `senderAvatarUrl` or `avatarUrl`: URL of the sender's profile picture
 * - `timestamp`: Optional epoch milliseconds string
 * - `targetType`: Destination screen type ("chat", "post", "friends", "notifications")
 * - `targetId`: Destination entity ID (e.g. sender UID or post ID)
 * - `notificationId`: Optional stable integer ID for grouping/updating
 */
class MeskotFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        val notification = remoteMessage.notification

        // Extract sender name, body text, and avatar URL from either `data` or `notification` payload
        val senderName = data["senderName"]
            ?: data["title"]
            ?: notification?.title
            ?: "Meskot"

        val messageBody = data["messageBody"]
            ?: data["body"]
            ?: notification?.body
            ?: return

        val senderAvatarUrl = data["senderAvatarUrl"]
            ?: data["avatarUrl"]
            ?: notification?.imageUrl?.toString()

        val timestampMs = data["timestamp"]?.toLongOrNull()
            ?: remoteMessage.sentTime.takeIf { it > 0L }
            ?: System.currentTimeMillis()

        val targetType = data["targetType"] ?: "chat"
        val targetId = data["targetId"] ?: data["senderId"] ?: ""

        val notificationId = data["notificationId"]?.toIntOrNull()
            ?: (targetId.ifBlank { senderName }.hashCode() and 0x7FFFFFFF)

        // Download & circular-crop the sender's profile picture on the background FCM worker thread
        val circularAvatar = NotificationHelper.loadCircularBitmapFromUrl(
            imageUrl = senderAvatarUrl,
            fallbackName = senderName
        )

        // Trigger the Facebook-style rich notification
        NotificationHelper.showSocialPushNotification(
            context = applicationContext,
            notificationId = notificationId,
            senderName = senderName,
            messageBody = messageBody,
            circularAvatarBitmap = circularAvatar,
            timestampMs = timestampMs,
            targetType = targetType,
            targetId = targetId
        )
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Refreshed FCM registration token: $token")
    }

    companion object {
        private const val TAG = "MeskotFCMService"
    }
}
