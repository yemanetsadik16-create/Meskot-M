package com.example.notifications

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.Person
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.IconCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * Production-ready helper for Facebook-style rich push notifications on Android 12+ and Android 13+.
 *
 * Features:
 * 1. High-importance Notification Channel (API 26+) with sound & vibration enabled.
 * 2. NotificationCompat.MessagingStyle with bold sender name, message body, and timestamp.
 * 3. Small app icon on the left + circular sender profile picture thumbnail on the right (`setLargeIcon`).
 * 4. Deep-link `Intent` & `PendingIntent` (`FLAG_IMMUTABLE | FLAG_UPDATE_CURRENT`) to open a chat or post screen.
 * 5. Android 13+ (`TIRAMISU`) `POST_NOTIFICATIONS` permission check and request helper.
 */
object NotificationHelper {

    const val CHANNEL_ID_SOCIAL = "meskot_social_high_importance_channel"
    private const val CHANNEL_NAME_SOCIAL = "Messages & Social Alerts"
    private const val CHANNEL_DESC_SOCIAL = "Real-time notifications for messages, comments, likes, and friend requests"

    const val EXTRA_TARGET_TYPE = "extra_notification_target_type" // e.g., "chat", "post", "friends", "notifications"
    const val EXTRA_TARGET_ID = "extra_notification_target_id"     // e.g., senderUid or postId
    const val EXTRA_SENDER_NAME = "extra_notification_sender_name"

    const val REQUEST_CODE_POST_NOTIFICATIONS = 1002

    /**
     * Step 1A: Creates a High-Importance Notification Channel with sound and vibration enabled (Android 8.0+ / API 26+).
     * Safe to call multiple times; the system no-ops if the channel already exists.
     */
    fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                .build()

            val channel = NotificationChannel(
                CHANNEL_ID_SOCIAL,
                CHANNEL_NAME_SOCIAL,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = CHANNEL_DESC_SOCIAL
                enableLights(true)
                lightColor = Color.parseColor("#D4AF37") // Meskot Royal Gold
                enableVibration(true)
                vibrationPattern = longArrayOf(0L, 250L, 150L, 250L)
                setSound(soundUri, audioAttributes)
                setShowBadge(true)
            }

            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * Step 1B: Checks whether `android.permission.POST_NOTIFICATIONS` is granted (Required on Android 13+ / API 33+).
     */
    fun hasNotificationPermission(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        } else {
            NotificationManagerCompat.from(context).areNotificationsEnabled()
        }
    }

    /**
     * Step 1C: Requests `android.permission.POST_NOTIFICATIONS` from an Activity on Android 13+ (API 33+).
     */
    fun requestNotificationPermissionIfNeeded(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (!hasNotificationPermission(activity)) {
                ActivityCompat.requestPermissions(
                    activity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    REQUEST_CODE_POST_NOTIFICATIONS
                )
            }
        }
    }

    /**
     * Step 1D: Suspending helper that downloads a remote avatar URL (if provided), crops it into a circle,
     * and displays the Facebook-style rich push notification.
     */
    suspend fun showSocialPushNotificationAsync(
        context: Context,
        notificationId: Int,
        senderName: String,
        messageBody: String,
        senderAvatarUrl: String? = null,
        timestampMs: Long = System.currentTimeMillis(),
        targetType: String = "chat",
        targetId: String = ""
    ) {
        val avatarBitmap = withContext(Dispatchers.IO) {
            loadCircularBitmapFromUrl(senderAvatarUrl, senderName)
        }
        showSocialPushNotification(
            context = context,
            notificationId = notificationId,
            senderName = senderName,
            messageBody = messageBody,
            circularAvatarBitmap = avatarBitmap,
            timestampMs = timestampMs,
            targetType = targetType,
            targetId = targetId
        )
    }

    /**
     * Step 1E: Builds and dispatches the Facebook-style rich notification using `NotificationCompat.MessagingStyle`.
     *
     * Visual layout on Android 12+:
     * - Left: Small app icon (`R.drawable.ic_launcher_foreground`)
     * - Top/Center: Bold Sender Name + Timestamp + Message body text
     * - Right: Circular sender profile picture thumbnail (`setLargeIcon`)
     */
    fun showSocialPushNotification(
        context: Context,
        notificationId: Int,
        senderName: String,
        messageBody: String,
        circularAvatarBitmap: Bitmap? = null,
        timestampMs: Long = System.currentTimeMillis(),
        targetType: String = "chat",
        targetId: String = ""
    ) {
        createNotificationChannel(context)

        // Android 13+ (API 33+) runtime permission guard
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        val finalCircularAvatar = circularAvatarBitmap ?: generateInitialsCircularBitmap(senderName)

        // Tap Action: Open MainActivity and pass deep-link extras for the specific chat or post screen
        val tapIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_TARGET_TYPE, targetType)
            putExtra(EXTRA_TARGET_ID, targetId)
            putExtra(EXTRA_SENDER_NAME, senderName)
        }

        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        val contentPendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            tapIntent,
            pendingIntentFlags
        )

        // Configure Sender Person with circular icon for NotificationCompat.MessagingStyle
        val senderPerson = Person.Builder()
            .setName(senderName)
            .setKey(targetId.ifBlank { senderName })
            .setIcon(IconCompat.createWithBitmap(finalCircularAvatar))
            .build()

        // MessagingStyle renders the Bold Sender Name, Timestamp, Message Body, and Avatar
        val messagingStyle = NotificationCompat.MessagingStyle(senderPerson)
            .setGroupConversation(false)
            .addMessage(
                NotificationCompat.MessagingStyle.Message(
                    messageBody,
                    timestampMs,
                    senderPerson
                )
            )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID_SOCIAL)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // App icon on the left
            .setContentTitle(senderName)                     // Bold sender name fallback
            .setContentText(messageBody)                     // Message body text
            .setLargeIcon(finalCircularAvatar)               // Circular profile picture on the right
            .setStyle(messagingStyle)
            .setWhen(timestampMs)
            .setShowWhen(true)
            .setColor(Color.parseColor("#D4AF37"))           // Brand accent color
            .setPriority(NotificationCompat.PRIORITY_HIGH)   // Heads-up display on pre-Oreo
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setDefaults(NotificationCompat.DEFAULT_ALL)     // Sound + Vibration
            .setAutoCancel(true)
            .setContentIntent(contentPendingIntent)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)
    }

    /**
     * Downloads an image from a remote URL and crops it into a circle.
     * Falls back to a clean circular monogram avatar if the URL is null or network fails.
     */
    fun loadCircularBitmapFromUrl(imageUrl: String?, fallbackName: String): Bitmap {
        if (!imageUrl.isNullOrBlank() && (imageUrl.startsWith("http://") || imageUrl.startsWith("https://"))) {
            var connection: HttpURLConnection? = null
            try {
                val url = URL(imageUrl)
                connection = (url.openConnection() as HttpURLConnection).apply {
                    connectTimeout = 5000
                    readTimeout = 5000
                    doInput = true
                    connect()
                }
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    connection.inputStream.use { input ->
                        val rawBitmap = BitmapFactory.decodeStream(input)
                        if (rawBitmap != null) {
                            return cropToCircle(rawBitmap)
                        }
                    }
                }
            } catch (_: Exception) {
                // Fallback to generated circular initials avatar below
            } finally {
                connection?.disconnect()
            }
        }
        return generateInitialsCircularBitmap(fallbackName)
    }

    /**
     * Crops any rectangular Bitmap into a clean anti-aliased circular Bitmap.
     */
    fun cropToCircle(source: Bitmap, targetSizePx: Int = 192): Bitmap {
        val minEdge = minOf(source.width, source.height)
        val xOffset = (source.width - minEdge) / 2
        val yOffset = (source.height - minEdge) / 2
        val square = Bitmap.createBitmap(source, xOffset, yOffset, minEdge, minEdge)
        val scaled = if (minEdge != targetSizePx) {
            Bitmap.createScaledBitmap(square, targetSizePx, targetSizePx, true)
        } else {
            square
        }

        val output = Bitmap.createBitmap(targetSizePx, targetSizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val rect = Rect(0, 0, targetSizePx, targetSizePx)

        canvas.drawARGB(0, 0, 0, 0)
        val radius = targetSizePx / 2f
        canvas.drawCircle(radius, radius, radius, paint)

        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(scaled, rect, rect, paint)
        return output
    }

    /**
     * Generates a circular avatar thumbnail with the sender's initials if no remote image is available.
     */
    fun generateInitialsCircularBitmap(name: String, sizePx: Int = 192): Bitmap {
        val output = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val radius = sizePx / 2f

        val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#1C160E") // Meskot Obsidian
            style = Paint.Style.FILL
        }
        canvas.drawCircle(radius, radius, radius, bgPaint)

        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#D4AF37") // Meskot Gold ring
            style = Paint.Style.STROKE
            strokeWidth = sizePx * 0.05f
        }
        canvas.drawCircle(radius, radius, radius - borderPaint.strokeWidth / 2f, borderPaint)

        val initials = name.trim()
            .split("\\s+".toRegex())
            .filter { it.isNotEmpty() }
            .take(2)
            .joinToString("") { it.first().uppercaseChar().toString() }
            .ifEmpty { "M" }

        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#F5D77F")
            textSize = sizePx * 0.38f
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        val textY = radius - (textPaint.descent() + textPaint.ascent()) / 2f
        canvas.drawText(initials, radius, textY, textPaint)

        return output
    }
}
