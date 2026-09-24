package com.example.ui.screens

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ColorLens
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Report
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SentimentSatisfiedAlt
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import com.example.data.AppLanguage
import com.example.data.ChatMessage
import com.example.data.MeskotStrings
import com.example.data.StoryItem
import com.example.data.User
import com.example.ui.MeskotViewModel
import com.example.ui.ScreenTab
import com.example.ui.components.StoryAvatarRingItem
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CardBg
import com.example.ui.theme.CrossRed
import com.example.ui.theme.Gold
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldDeep
import com.example.ui.theme.GoldSurface
import com.example.ui.theme.Ink
import com.example.ui.theme.LineBorder
import com.example.ui.theme.MutedText
import com.example.ui.theme.Paper
import com.example.ui.theme.Paper2
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun MessagesScreen(
    viewModel: MeskotViewModel,
    currentUser: User?,
    allUsers: List<User>,
    friendUids: Set<String>,
    currentLanguage: AppLanguage
) {
    val conversations by viewModel.conversations.collectAsState()
    val lastReadTimestamps by viewModel.lastReadTimestamps.collectAsState()
    val nowMs by viewModel.tickerTimeMs.collectAsStateWithLifecycle()
    val friendsList = allUsers.filter { friendUids.contains(it.uid) }

    val chatPartnersFromMessages = remember(conversations, currentUser?.uid) {
        conversations.values.flatten()
            .mapNotNull { msg ->
                when {
                    msg.fromUid == currentUser?.uid && msg.toUid.isNotBlank() -> msg.toUid
                    msg.toUid == currentUser?.uid && msg.fromUid.isNotBlank() -> msg.fromUid
                    else -> null
                }
            }
    }
    val activeChatUids = remember(friendUids, conversations, chatPartnersFromMessages, currentUser?.uid) {
        val nonConvoKeys = conversations.filter { it.value.isNotEmpty() }.keys.filter { !it.contains("_") }
        (friendUids + nonConvoKeys + chatPartnersFromMessages)
            .filter { it.isNotBlank() && it != currentUser?.uid }
            .distinct()
    }
    val activeChatUsers = remember(activeChatUids, allUsers, conversations) {
        activeChatUids.map { uid ->
            allUsers.find { it.uid == uid } ?: if (uid == "user_gebreslassie") {
                User(
                    uid = "user_gebreslassie",
                    displayName = "Gebreslassie Tsadik",
                    bio = "Living life with faith, courage, and purpose.",
                    photoUrl = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300",
                    lastSeen = System.currentTimeMillis() - 12 * 60 * 1000L
                )
            } else {
                User(uid = uid, displayName = "Meskot User", bio = "")
            }
        }.sortedByDescending { user ->
            viewModel.getMessagesForUser(user.uid).lastOrNull()?.createdAt ?: 0L
        }
    }

    val stories by viewModel.stories.collectAsState()
    val tickerTimeMs by viewModel.tickerTimeMs.collectAsState()
    val activeStories = remember(stories, tickerTimeMs) {
        stories.filter { !it.isExpired(tickerTimeMs) }
    }
    val myStories = remember(activeStories, currentUser?.uid) {
        if (currentUser == null) emptyList() else activeStories.filter { it.uid == currentUser.uid }
    }
    val otherStoriesByUser = remember(activeStories, currentUser?.uid) {
        activeStories.filter { it.uid != currentUser?.uid }.groupBy { it.uid }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("messages_screen")
    ) {
        Text(
            text = MeskotStrings.get("navMessages", currentLanguage),
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = Ink,
            modifier = Modifier.padding(start = 16.dp, top = 14.dp, bottom = 8.dp)
        )

        // Stories & Friends Instant Chat Rail
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Current user "Your Story"
            if (currentUser != null) {
                item {
                    StoryAvatarRingItem(
                        user = currentUser,
                        hasStory = myStories.isNotEmpty(),
                        isCurrentUser = true,
                        isStoryViewed = false,
                        expirationText = myStories.firstOrNull()?.formattedRemaining(tickerTimeMs),
                        onClick = {
                            if (myStories.isNotEmpty()) {
                                viewModel.viewStory(myStories.first())
                            } else {
                                viewModel.openCreateStory()
                            }
                        }
                    )
                }
            }

            // 2. Active Stories from community & friends
            otherStoriesByUser.forEach { (authorUid, userStories) ->
                val latestStory = userStories.first()
                val friendObj = friendsList.find { it.uid == authorUid } ?: User(
                    uid = authorUid,
                    displayName = latestStory.authorName,
                    photoUrl = latestStory.authorPhoto
                )
                val isAllViewed = userStories.all { it.viewers.contains(currentUser?.uid) }

                item(key = "msg_story_$authorUid") {
                    StoryAvatarRingItem(
                        user = friendObj,
                        hasStory = true,
                        isCurrentUser = false,
                        isStoryViewed = isAllViewed,
                        expirationText = latestStory.formattedRemaining(tickerTimeMs),
                        onClick = { viewModel.viewStory(latestStory) }
                    )
                }
            }

            // 3. Friends without active stories
            val friendsWithoutStories = friendsList.filterNot { otherStoriesByUser.containsKey(it.uid) }
            items(friendsWithoutStories, key = { "msg_friend_${it.uid}" }) { friend ->
                StoryAvatarRingItem(
                    user = friend,
                    hasStory = false,
                    isCurrentUser = false,
                    onClick = { viewModel.openChat(friend) }
                )
            }
        }
        HorizontalDivider(color = LineBorder, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))

        // Conversations List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            if (activeChatUsers.isEmpty()) {
                item {
                    EmptyNotice(text = MeskotStrings.get("noConvos", currentLanguage))
                }
            } else {
                items(activeChatUsers) { user ->
                    val messages = remember(conversations, user.uid, currentUser?.uid) {
                        viewModel.getMessagesForUser(user.uid)
                    }
                    val lastMessage = messages.lastOrNull()
                    val lastRead = lastReadTimestamps[user.uid] ?: 0L
                    val hasUnseen = remember(messages, lastRead, user.uid) {
                        messages.any { msg ->
                            msg.fromUid == user.uid && (!msg.isSeen || msg.createdAt > lastRead) && !msg.isCallLog
                        }
                    }

                    val isUserOnline = remember(user.lastSeen, nowMs) {
                        MeskotStrings.isOnline(user.lastSeen, nowMs)
                    }
                    val userActiveStatus = remember(user.lastSeen, nowMs, currentLanguage) {
                        MeskotStrings.formatActiveStatus(user.lastSeen, currentLanguage, nowMs)
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .clickable { viewModel.openChat(user) },
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (hasUnseen) Color(0xFFF0F7FF) else CardBg
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (hasUnseen) Color(0xFFC7E0FF) else LineBorder
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box {
                                UserAvatar(photoUrl = user.photoUrl, name = user.displayName, size = 48)
                                if (isUserOnline) {
                                    Box(
                                        modifier = Modifier
                                            .size(12.dp)
                                            .align(Alignment.BottomEnd)
                                            .clip(CircleShape)
                                            .background(Color(0xFF31A24C))
                                            .border(2.dp, Color.White, CircleShape)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = user.displayName,
                                        fontSize = 15.sp,
                                        fontWeight = if (hasUnseen) FontWeight.Bold else FontWeight.SemiBold,
                                        color = Ink
                                    )
                                    if (isUserOnline) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(7.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF31A24C))
                                        )
                                    }
                                }
                                val previewText = when {
                                    lastMessage == null -> userActiveStatus
                                    lastMessage.mediaType == "image" -> "📷 Photo"
                                    lastMessage.mediaType == "file" -> "📎 ${lastMessage.fileName ?: "Attachment"}"
                                    lastMessage.mediaType == "audio" -> "🎤 Voice message (${lastMessage.fileSize ?: ""})"
                                    lastMessage.mediaType == "like" || lastMessage.text == "👍" -> "👍 Thumbs up"
                                    else -> lastMessage.text
                                }
                                Text(
                                    text = previewText,
                                    fontSize = 13.sp,
                                    color = if (lastMessage == null) {
                                        if (isUserOnline) Color(0xFF31A24C) else MutedText
                                    } else if (hasUnseen) Ink else MutedText,
                                    fontWeight = if (hasUnseen) FontWeight.Bold else FontWeight.Normal,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            if (lastMessage != null) {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = MeskotStrings.formatNotificationTime(lastMessage.createdAt, currentLanguage, nowMs),
                                        fontSize = 11.sp,
                                        color = if (hasUnseen) Color(0xFF0084FF) else MutedText,
                                        fontWeight = if (hasUnseen) FontWeight.Bold else FontWeight.Normal
                                    )
                                    if (hasUnseen) {
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Box(
                                            modifier = Modifier
                                                .size(9.dp)
                                                .clip(CircleShape)
                                                .background(Color(0xFF0084FF))
                                        )
                                    }
                                }
                            } else {
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = if (isUserOnline) "Online" else userActiveStatus,
                                        fontSize = 11.sp,
                                        color = if (isUserOnline) Color(0xFF31A24C) else MutedText,
                                        fontWeight = if (isUserOnline) FontWeight.Medium else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun ChatScreen(
    viewModel: MeskotViewModel,
    currentUser: User?,
    recipient: User,
    currentLanguage: AppLanguage
) {
    val conversations by viewModel.conversations.collectAsState()
    val rawMessages = remember(conversations, recipient.uid, currentUser?.uid) {
        viewModel.getMessagesForUser(recipient.uid)
    }

    LaunchedEffect(recipient.uid) {
        viewModel.markConversationAsRead(recipient.uid)
    }

    LaunchedEffect(rawMessages) {
        if (rawMessages.any { it.fromUid == recipient.uid && !it.isSeen }) {
            viewModel.markConversationAsRead(recipient.uid)
        }
    }

    var textInput by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity
    val clipboardManager = LocalClipboardManager.current

    // Chat Customization & State
    var themeColor by remember { mutableStateOf(Color(0xFF0084FF)) } // Default Messenger Blue matching screenshot
    var quickReaction by remember { mutableStateOf("👍") }
    var isMuted by remember { mutableStateOf(false) }
    var disappearingDays by remember { mutableIntStateOf(0) } // 0 = off

    // Dialog & UI Visibility State
    var showSettings by remember { mutableStateOf(false) }
    var showSharedMedia by remember { mutableStateOf(false) }
    var showClearConfirm by remember { mutableStateOf(false) }
    var showReportDialog by remember { mutableStateOf(false) }
    var showEmojiPicker by remember { mutableStateOf(false) }
    var selectedImageForView by remember { mutableStateOf<String?>(null) }
    var isSearchActive by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    // Staged Attachments
    var stagedImageUri by remember { mutableStateOf<String?>(null) }
    var stagedFileUri by remember { mutableStateOf<String?>(null) }
    var stagedFileName by remember { mutableStateOf<String?>(null) }
    var stagedFileSize by remember { mutableStateOf<String?>(null) }

    // Voice Note Recording
    var isRecordingVoice by remember { mutableStateOf(false) }
    var voiceDurationSec by remember { mutableIntStateOf(0) }

    // Voice Playback Simulation State
    var playingAudioMsgId by remember { mutableStateOf<String?>(null) }

    // Filter messages if search is active
    val displayMessages = remember(rawMessages, isSearchActive, searchQuery) {
        if (isSearchActive && searchQuery.isNotBlank()) {
            rawMessages.filter { msg ->
                msg.text.contains(searchQuery, ignoreCase = true) ||
                    (msg.fileName?.contains(searchQuery, ignoreCase = true) == true)
            }
        } else {
            rawMessages
        }
    }

    // Voice recording timer loop
    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            voiceDurationSec = 0
            while (isRecordingVoice) {
                delay(1000)
                voiceDurationSec++
            }
        }
    }

    // Voice playback simulation loop
    LaunchedEffect(playingAudioMsgId) {
        if (playingAudioMsgId != null) {
            delay(4000)
            playingAudioMsgId = null
        }
    }

    // Mark conversation read on enter and on new messages
    LaunchedEffect(recipient.uid) {
        viewModel.markConversationAsRead(recipient.uid)
    }

    LaunchedEffect(rawMessages.size) {
        if (rawMessages.isNotEmpty()) {
            listState.animateScrollToItem(rawMessages.size - 1)
        }
        viewModel.markConversationAsRead(recipient.uid)
    }

    // Android Call Permission Request Launcher
    val callPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Gracefully handled */ }

    // Android Photo Picker Launcher
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            stagedImageUri = uri.toString()
            stagedFileUri = null
            stagedFileName = null
            stagedFileSize = null
        }
    }

    // Android Document/File Picker Launcher
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null) {
            var name = "Document"
            var sizeStr = "File"
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIdx != -1) name = cursor.getString(nameIdx)
                        if (sizeIdx != -1) {
                            val bytes = cursor.getLong(sizeIdx)
                            sizeStr = if (bytes > 1024 * 1024) {
                                String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
                            } else {
                                "${bytes / 1024} KB"
                            }
                        }
                    }
                }
            } catch (_: Exception) {
                name = uri.lastPathSegment ?: "Document"
            }
            stagedFileUri = uri.toString()
            stagedFileName = name
            stagedFileSize = sizeStr
            stagedImageUri = null
        }
    }

    // Send logic
    val handleSendMessage = {
        if (stagedImageUri != null) {
            viewModel.sendMessage(
                otherUid = recipient.uid,
                text = textInput.trim(),
                mediaUrl = stagedImageUri,
                mediaType = "image"
            )
            stagedImageUri = null
            textInput = ""
        } else if (stagedFileUri != null) {
            viewModel.sendMessage(
                otherUid = recipient.uid,
                text = textInput.trim(),
                mediaUrl = stagedFileUri,
                mediaType = "file",
                fileName = stagedFileName ?: "document.pdf",
                fileSize = stagedFileSize ?: "File"
            )
            stagedFileUri = null
            stagedFileName = null
            stagedFileSize = null
            textInput = ""
        } else if (textInput.isNotBlank()) {
            viewModel.sendMessage(recipient.uid, textInput.trim())
            textInput = ""
        }
        showEmojiPicker = false
    }

    // Calculate dynamic active status string synced with ticker
    val nowMs by viewModel.tickerTimeMs.collectAsStateWithLifecycle()
    val isRecipientOnline = remember(recipient.lastSeen, nowMs) {
        MeskotStrings.isOnline(recipient.lastSeen, nowMs)
    }
    val activeStatusText = remember(recipient.lastSeen, nowMs, currentLanguage) {
        MeskotStrings.formatActiveStatus(recipient.lastSeen, currentLanguage, nowMs)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("chat_screen")
    ) {
        // TOP APP BAR: Matching user's screenshot exactly!
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 6.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Back button + Avatar + Name & Active status
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { showSettings = true }
                    ) {
                        IconButton(onClick = { viewModel.navigateTo(ScreenTab.MESSAGES) }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.Black,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Box {
                            UserAvatar(photoUrl = recipient.photoUrl, name = recipient.displayName, size = 42)
                            if (isRecipientOnline) {
                                Box(
                                    modifier = Modifier
                                        .size(11.dp)
                                        .align(Alignment.BottomEnd)
                                        .clip(CircleShape)
                                        .background(Color(0xFF31A24C))
                                        .border(2.dp, Color.White, CircleShape)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Column {
                            Text(
                                text = recipient.displayName,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF050505),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                if (isRecipientOnline) {
                                    Box(
                                        modifier = Modifier
                                            .size(6.dp)
                                            .clip(CircleShape)
                                            .background(Color(0xFF31A24C))
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = activeStatusText,
                                    fontSize = 12.sp,
                                    color = if (isRecipientOnline) Color(0xFF31A24C) else Color(0xFF65676B),
                                    fontWeight = if (isRecipientOnline) FontWeight.Medium else FontWeight.Normal
                                )
                            }
                        }
                    }

                    // Right: Action buttons (Audio Call 📞, Video Call 📹, Settings ⚙️)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(
                            onClick = {
                                val permissions = arrayOf(android.Manifest.permission.RECORD_AUDIO)
                                activity?.let {
                                    ActivityCompat.requestPermissions(it, permissions, 101)
                                } ?: run {
                                    callPermissionLauncher.launch(permissions)
                                }
                                viewModel.startCall(recipient, "audio")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Call,
                                contentDescription = "Audio call",
                                tint = Color.Black,
                                modifier = Modifier.size(23.dp)
                            )
                        }

                        IconButton(
                            onClick = {
                                val permissions = arrayOf(
                                    android.Manifest.permission.CAMERA,
                                    android.Manifest.permission.RECORD_AUDIO
                                )
                                activity?.let {
                                    ActivityCompat.requestPermissions(it, permissions, 102)
                                } ?: run {
                                    callPermissionLauncher.launch(permissions)
                                }
                                viewModel.startCall(recipient, "video")
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Video call",
                                tint = Color.Black,
                                modifier = Modifier.size(25.dp)
                            )
                        }

                        IconButton(
                            onClick = { showSettings = true },
                            modifier = Modifier.testTag("chat_settings_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = "Settings",
                                tint = Color.Black,
                                modifier = Modifier.size(23.dp)
                            )
                        }
                    }
                }

                // In-Chat Search Bar (visible when toggled)
                AnimatedVisibility(visible = isSearchActive) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFF0F2F5))
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = Color(0xFF65676B),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            textStyle = TextStyle(fontSize = 14.sp, color = Color.Black),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text("Search in conversation...", fontSize = 14.sp, color = Color(0xFF8E8E93))
                                }
                                innerTextField()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            Text(
                                text = "${displayMessages.size} found",
                                fontSize = 12.sp,
                                color = themeColor,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )
                        }
                        IconButton(
                            onClick = {
                                isSearchActive = false
                                searchQuery = ""
                            },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Close, contentDescription = "Close search", tint = Color.Black, modifier = Modifier.size(16.dp))
                        }
                    }
                }
            }
        }

        // CHAT FEED: Messages & Timestamp Clusters
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            if (displayMessages.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            UserAvatar(photoUrl = recipient.photoUrl, name = recipient.displayName, size = 64)
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(text = recipient.displayName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color(0xFF050505))
                            Text(text = "Meskot Friend", fontSize = 13.sp, color = Color(0xFF65676B))
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(text = "Say hello! 👋", color = Color(0xFF8E8E93), fontSize = 14.sp)
                        }
                    }
                }
            } else {
                items(displayMessages.size) { index ->
                    val msg = displayMessages[index]
                    val prevMsg = if (index > 0) displayMessages[index - 1] else null
                    val isMine = msg.fromUid == currentUser?.uid

                    // Centered timestamp chip between clusters (e.g. Aug 08 13:40)
                    val showDateHeader = prevMsg == null || (msg.createdAt - prevMsg.createdAt > 15 * 60 * 1000L)
                    if (showDateHeader) {
                        CenteredDateChip(timestamp = msg.createdAt)
                    }

                    if (msg.isCallLog) {
                        CallLogBubble(
                            msg = msg,
                            currentLanguage = currentLanguage,
                            onCallBack = {
                                if (msg.callType == "video") {
                                    callPermissionLauncher.launch(
                                        arrayOf(
                                            android.Manifest.permission.RECORD_AUDIO,
                                            android.Manifest.permission.CAMERA
                                        )
                                    )
                                } else {
                                    callPermissionLauncher.launch(arrayOf(android.Manifest.permission.RECORD_AUDIO))
                                }
                                viewModel.startCall(recipient, msg.callType)
                            }
                        )
                    } else {
                        EnhancedChatMessageRow(
                            msg = msg,
                            isMine = isMine,
                            themeColor = themeColor,
                            recipientPhotoUrl = recipient.photoUrl,
                            recipientName = recipient.displayName,
                            isPlayingAudio = playingAudioMsgId == msg.id,
                            onToggleAudio = {
                                playingAudioMsgId = if (playingAudioMsgId == msg.id) null else msg.id
                            },
                            onImageClick = { url ->
                                selectedImageForView = url
                            },
                            onDelete = { viewModel.deleteMessage(recipient.uid, msg.id) },
                            onCopyText = {
                                clipboardManager.setText(AnnotatedString(msg.text))
                                viewModel.showMessage("Copied to clipboard")
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(6.dp))
            }
        }

        // Staged Attachment Preview Strip (Images / Files before sending)
        if (stagedImageUri != null || stagedFileUri != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF7F8FA),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (stagedImageUri != null) {
                            AsyncImage(
                                model = stagedImageUri,
                                contentDescription = "Staged photo",
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp)),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = "Photo ready to send", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                        } else if (stagedFileUri != null) {
                            Box(
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(themeColor.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Description, contentDescription = null, tint = themeColor)
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(text = stagedFileName ?: "Document", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.Black, maxLines = 1)
                                Text(text = stagedFileSize ?: "Ready to send", fontSize = 11.sp, color = Color(0xFF65676B))
                            }
                        }
                    }

                    IconButton(
                        onClick = {
                            stagedImageUri = null
                            stagedFileUri = null
                            stagedFileName = null
                            stagedFileSize = null
                        },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Remove", tint = Color.Gray, modifier = Modifier.size(18.dp))
                    }
                }
            }
        }

        // Voice Note Recording Strip (When mic is active)
        AnimatedVisibility(visible = isRecordingVoice) {
            val infiniteTransition = rememberInfiniteTransition(label = "pulse")
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.4f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(tween(600), RepeatMode.Reverse),
                label = "pulseAlpha"
            )

            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFFFF0F2),
                tonalElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(12.dp)
                                .clip(CircleShape)
                                .background(CrossRed.copy(alpha = pulseAlpha))
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = String.format(Locale.US, "%02d:%02d", voiceDurationSec / 60, voiceDurationSec % 60),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = CrossRed
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(text = "Recording voice message...", fontSize = 13.sp, color = Color(0xFF65676B))
                    }

                    Row {
                        IconButton(
                            onClick = {
                                isRecordingVoice = false
                                voiceDurationSec = 0
                            }
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Cancel", tint = CrossRed)
                        }

                        IconButton(
                            onClick = {
                                val durStr = String.format(Locale.US, "%02d:%02d", voiceDurationSec / 60, voiceDurationSec % 60)
                                viewModel.sendMessage(
                                    otherUid = recipient.uid,
                                    text = "Voice message ($durStr)",
                                    mediaType = "audio",
                                    fileSize = durStr
                                )
                                isRecordingVoice = false
                                voiceDurationSec = 0
                            }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send voice note", tint = themeColor)
                        }
                    }
                }
            }
        }

        // Quick Emoji Strip (when smiley face is toggled)
        AnimatedVisibility(visible = showEmojiPicker) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = Color(0xFFF7F8FA),
                shadowElevation = 2.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val popularEmojis = listOf("👍", "❤️", "😂", "🔥", "🎉", "👏", "💯", "🤝", "✨", "😍", "🙌", "🙏", "🚀", "💡", "☕")
                    popularEmojis.forEach { emoji ->
                        Text(
                            text = emoji,
                            fontSize = 24.sp,
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .clickable {
                                    textInput += emoji
                                }
                                .padding(4.dp)
                        )
                    }
                }
            }
        }

        // BOTTOM INPUT BAR: Matching screenshot layout & icons
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Action 1: Gallery / Photo Picker
                IconButton(
                    onClick = {
                        photoPickerLauncher.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Image,
                        contentDescription = "Send Photo",
                        tint = themeColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Left Action 2: Mic / Voice Recorder
                IconButton(
                    onClick = {
                        isRecordingVoice = !isRecordingVoice
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Voice note",
                        tint = if (isRecordingVoice) CrossRed else themeColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Left Action 3: File / Document Attachment
                IconButton(
                    onClick = {
                        filePickerLauncher.launch("*/*")
                    },
                    modifier = Modifier.size(38.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.AttachFile,
                        contentDescription = "Send File",
                        tint = themeColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Center: Rounded Pill Text Input Field
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFF0F2F5), RoundedCornerShape(22.dp))
                        .padding(horizontal = 14.dp, vertical = 9.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        BasicTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            textStyle = TextStyle(
                                color = Color(0xFF050505),
                                fontSize = 15.sp,
                                fontFamily = FontFamily.Default
                            ),
                            cursorBrush = SolidColor(themeColor),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("chat_message_input"),
                            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Send),
                            keyboardActions = KeyboardActions(onSend = { handleSendMessage() }),
                            decorationBox = { innerTextField ->
                                if (textInput.isEmpty()) {
                                    Text(
                                        text = "Message...",
                                        fontSize = 15.sp,
                                        color = Color(0xFF8E8E93)
                                    )
                                }
                                innerTextField()
                            }
                        )

                        // Smiley Face / Emoji Button inside pill
                        IconButton(
                            onClick = { showEmojiPicker = !showEmojiPicker },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SentimentSatisfiedAlt,
                                contentDescription = "Emoji picker",
                                tint = themeColor,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Right Action: Quick Like button (👍) when empty, or Send button when text/media is present
                val hasContentToSend = textInput.isNotBlank() || stagedImageUri != null || stagedFileUri != null

                if (hasContentToSend) {
                    IconButton(
                        onClick = { handleSendMessage() },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("chat_send_btn")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send",
                            tint = themeColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                } else {
                    IconButton(
                        onClick = {
                            viewModel.sendMessage(
                                otherUid = recipient.uid,
                                text = quickReaction,
                                mediaType = "like"
                            )
                        },
                        modifier = Modifier
                            .size(38.dp)
                            .testTag("chat_like_btn")
                    ) {
                        if (quickReaction == "👍") {
                            Icon(
                                imageVector = Icons.Default.ThumbUp,
                                contentDescription = "Quick Like",
                                tint = themeColor,
                                modifier = Modifier.size(24.dp)
                            )
                        } else {
                            Text(text = quickReaction, fontSize = 22.sp)
                        }
                    }
                }
            }
        }
    }

    // 1. CONVERSATION SETTINGS MODAL DIALOG
    if (showSettings) {
        ChatSettingsDialog(
            recipient = recipient,
            isOnline = isRecipientOnline,
            activeStatusText = activeStatusText,
            themeColor = themeColor,
            onThemeChange = { themeColor = it },
            quickReaction = quickReaction,
            onQuickReactionChange = { quickReaction = it },
            isMuted = isMuted,
            onToggleMute = {
                isMuted = !isMuted
                viewModel.showMessage(if (isMuted) "Notifications muted for this chat" else "Notifications unmuted")
            },
            disappearingDays = disappearingDays,
            onDisappearingDaysChange = { days ->
                disappearingDays = days
                viewModel.showMessage(if (days == 0) "Disappearing messages turned off" else "Messages will disappear after $days days")
            },
            onAudioCall = {
                showSettings = false
                viewModel.startCall(recipient, "audio")
            },
            onVideoCall = {
                showSettings = false
                viewModel.startCall(recipient, "video")
            },
            onViewProfile = {
                showSettings = false
                viewModel.openProfile(recipient)
            },
            onOpenSharedMedia = {
                showSettings = false
                showSharedMedia = true
            },
            onSearchChat = {
                showSettings = false
                isSearchActive = true
            },
            onClearHistory = {
                showClearConfirm = true
            },
            onReport = {
                showSettings = false
                showReportDialog = true
            },
            onDismiss = { showSettings = false }
        )
    }

    // 2. SHARED MEDIA, PHOTOS & FILES DIALOG
    if (showSharedMedia) {
        SharedMediaDialog(
            messages = rawMessages,
            recipient = recipient,
            themeColor = themeColor,
            onImageClick = { url ->
                selectedImageForView = url
            },
            onDismiss = { showSharedMedia = false }
        )
    }

    // 3. FULL-SCREEN IMAGE VIEWER DIALOG
    selectedImageForView?.let { imageUrl ->
        FullScreenImageViewerDialog(
            imageUrl = imageUrl,
            recipientName = recipient.displayName,
            onDismiss = { selectedImageForView = null }
        )
    }

    // 4. CLEAR CONVERSATION CONFIRMATION ALERT
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            title = { Text(text = "Clear conversation history?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to clear all messages with ${recipient.displayName}? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.clearConversation(recipient.uid)
                        showClearConfirm = false
                        showSettings = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CrossRed)
                ) {
                    Text("Clear All", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = Ink)
                }
            }
        )
    }

    // 5. REPORT CONVERSATION DIALOG
    if (showReportDialog) {
        var reportReason by remember { mutableStateOf("Spam or fraudulent messages") }
        AlertDialog(
            onDismissRequest = { showReportDialog = false },
            title = { Text(text = "Report Conversation", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(text = "Why are you reporting ${recipient.displayName}?", fontSize = 14.sp, color = MutedText)
                    Spacer(modifier = Modifier.height(10.dp))
                    val reasons = listOf(
                        "Spam or fraudulent messages",
                        "Harassment or bullying",
                        "Inappropriate content",
                        "Impersonation"
                    )
                    reasons.forEach { reason ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { reportReason = reason }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, if (reportReason == reason) themeColor else LineBorder, CircleShape)
                                    .background(if (reportReason == reason) themeColor else Color.Transparent)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(text = reason, fontSize = 13.sp, color = Ink)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReportDialog = false
                        viewModel.showMessage("Thank you. Your report has been submitted.")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = themeColor)
                ) {
                    Text("Submit Report", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportDialog = false }) {
                    Text("Cancel", color = Ink)
                }
            }
        )
    }
}

// -----------------------------------------------------------------------------------------
// ENHANCED CHAT ROW: BUBBLES, IMAGES, FILES, AUDIO & LIKES
// -----------------------------------------------------------------------------------------

@Composable
fun EnhancedChatMessageRow(
    msg: ChatMessage,
    isMine: Boolean,
    themeColor: Color,
    recipientPhotoUrl: String?,
    recipientName: String,
    isPlayingAudio: Boolean,
    onToggleAudio: () -> Unit,
    onImageClick: (String) -> Unit,
    onDelete: () -> Unit,
    onCopyText: () -> Unit
) {
    var showActionMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Bottom
    ) {
        // Partner circular avatar on bottom left
        if (!isMine) {
            UserAvatar(
                photoUrl = recipientPhotoUrl,
                name = recipientName,
                size = 28,
                modifier = Modifier.padding(end = 8.dp, bottom = 2.dp)
            )
        }

        // Like / Thumbs Up Sticker (Rendered large with no bubble background)
        if (msg.mediaType == "like" || msg.text == "👍") {
            Column(
                horizontalAlignment = if (isMine) Alignment.End else Alignment.Start,
                modifier = Modifier
                    .clickable { showActionMenu = true }
                    .padding(horizontal = 4.dp, vertical = 2.dp)
            ) {
                if (msg.text == "👍") {
                    Icon(
                        imageVector = Icons.Default.ThumbUp,
                        contentDescription = "Like",
                        tint = themeColor,
                        modifier = Modifier.size(42.dp)
                    )
                } else {
                    Text(text = msg.text, fontSize = 38.sp)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = MeskotStrings.formatClockTime(msg.createdAt),
                    fontSize = 10.sp,
                    color = Color(0xFF8E8E93)
                )
            }
        } else {
            // Standard speech bubble
            Surface(
                shape = RoundedCornerShape(
                    topStart = 18.dp,
                    topEnd = 18.dp,
                    bottomStart = if (isMine) 18.dp else 4.dp,
                    bottomEnd = if (isMine) 4.dp else 18.dp
                ),
                color = if (isMine) themeColor else Color(0xFFF0F2F5),
                shadowElevation = 0.dp,
                modifier = Modifier
                    .widthIn(max = 280.dp)
                    .clickable { showActionMenu = true }
            ) {
                Column(modifier = Modifier.padding(10.dp)) {
                    // 1. Photo / Image message
                    if (msg.mediaType == "image" && !msg.mediaUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = msg.mediaUrl,
                            contentDescription = "Chat photo",
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onImageClick(msg.mediaUrl) },
                            contentScale = ContentScale.Crop
                        )
                        if (msg.text.isNotBlank()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = msg.text,
                                color = if (isMine) Color.White else Color(0xFF050505),
                                fontSize = 15.sp,
                                lineHeight = 20.sp
                            )
                        }
                    }
                    // 2. File / Document message
                    else if (msg.mediaType == "file") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isMine) Color.White.copy(alpha = 0.18f) else Color.White)
                                .padding(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isMine) Color.White.copy(alpha = 0.25f) else themeColor.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Description,
                                    contentDescription = "Document",
                                    tint = if (isMine) Color.White else themeColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = msg.fileName ?: "Document",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isMine) Color.White else Color(0xFF050505),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = msg.fileSize ?: "File attachment",
                                    fontSize = 11.sp,
                                    color = if (isMine) Color.White.copy(alpha = 0.75f) else Color(0xFF65676B)
                                )
                            }
                        }
                        if (msg.text.isNotBlank() && msg.text != msg.fileName) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = msg.text,
                                color = if (isMine) Color.White else Color(0xFF050505),
                                fontSize = 14.sp
                            )
                        }
                    }
                    // 3. Audio / Voice message
                    else if (msg.mediaType == "audio") {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 2.dp)
                        ) {
                            IconButton(
                                onClick = onToggleAudio,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(if (isMine) Color.White.copy(alpha = 0.25f) else themeColor.copy(alpha = 0.15f))
                            ) {
                                Icon(
                                    imageVector = if (isPlayingAudio) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = "Play voice note",
                                    tint = if (isMine) Color.White else themeColor,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            // Simulated wave equalizer bars
                            Row(
                                modifier = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(2.5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val heights = listOf(8, 14, 20, 10, 16, 24, 18, 12, 22, 14, 8)
                                heights.forEachIndexed { i, h ->
                                    val active = isPlayingAudio && (i < 6)
                                    Box(
                                        modifier = Modifier
                                            .width(3.dp)
                                            .height(h.dp)
                                            .clip(RoundedCornerShape(2.dp))
                                            .background(
                                                if (isMine) {
                                                    if (active) Color.White else Color.White.copy(alpha = 0.45f)
                                                } else {
                                                    if (active) themeColor else Color(0xFFB0B3B8)
                                                }
                                            )
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg.fileSize ?: "0:14",
                                fontSize = 11.sp,
                                color = if (isMine) Color.White.copy(alpha = 0.85f) else Color(0xFF65676B)
                            )
                        }
                    }
                    // 4. Standard Text message
                    else {
                        Text(
                            text = msg.text,
                            color = if (isMine) Color.White else Color(0xFF050505),
                            fontSize = 15.sp,
                            lineHeight = 20.sp,
                            fontWeight = if (!isMine && !msg.isSeen) FontWeight.Bold else FontWeight.Normal
                        )
                    }

                    // Message Timestamp & Delivery Indicator (Always visible on all messages)
                    Spacer(modifier = Modifier.height(3.dp))
                    Row(
                        modifier = Modifier.align(Alignment.End),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = MeskotStrings.formatClockTime(msg.createdAt),
                            fontSize = 10.sp,
                            color = if (isMine) Color.White.copy(alpha = 0.75f) else Color(0xFF8E8E93)
                        )
                        if (isMine) {
                            Spacer(modifier = Modifier.width(3.dp))
                            Text(
                                text = if (msg.isSeen) "✓✓" else "✓",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (msg.isSeen) Color(0xFF0084FF) else Color.White.copy(alpha = 0.85f)
                            )
                        }
                    }
                }
            }
        }
    }

    // Message options dialog on tap
    if (showActionMenu) {
        AlertDialog(
            onDismissRequest = { showActionMenu = false },
            title = { Text(text = "Message Options", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Sent: " + MeskotStrings.formatFullDateTime(msg.createdAt),
                        fontSize = 12.sp,
                        color = Color(0xFF65676B),
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (msg.text.isNotBlank()) {
                        Text(
                            text = "“${msg.text.take(60)}”",
                            fontSize = 13.sp,
                            color = Color.Gray,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onCopyText()
                                showActionMenu = false
                            }
                            .padding(vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = Ink)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Copy Message Text", fontSize = 14.sp)
                    }
                    if (isMine) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    onDelete()
                                    showActionMenu = false
                                }
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = CrossRed)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text("Delete Message", color = CrossRed, fontSize = 14.sp)
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showActionMenu = false }) {
                    Text("Close")
                }
            }
        )
    }
}

// -----------------------------------------------------------------------------------------
// DATE CHIP & CALL LOG BUBBLE
// -----------------------------------------------------------------------------------------

@Composable
fun CenteredDateChip(timestamp: Long) {
    val dateText = remember(timestamp) {
        val calNow = java.util.Calendar.getInstance()
        val calThen = java.util.Calendar.getInstance().apply { timeInMillis = timestamp }
        val isToday = calNow.get(java.util.Calendar.YEAR) == calThen.get(java.util.Calendar.YEAR) &&
                calNow.get(java.util.Calendar.DAY_OF_YEAR) == calThen.get(java.util.Calendar.DAY_OF_YEAR)
        val isYesterday = calNow.get(java.util.Calendar.YEAR) == calThen.get(java.util.Calendar.YEAR) &&
                calNow.get(java.util.Calendar.DAY_OF_YEAR) - calThen.get(java.util.Calendar.DAY_OF_YEAR) == 1

        val timeStr = SimpleDateFormat("h:mm a", Locale.US).format(Date(timestamp))
        when {
            isToday -> "Today at $timeStr"
            isYesterday -> "Yesterday at $timeStr"
            calNow.get(java.util.Calendar.YEAR) == calThen.get(java.util.Calendar.YEAR) -> {
                val dateStr = SimpleDateFormat("MMM d", Locale.US).format(Date(timestamp))
                "$dateStr at $timeStr"
            }
            else -> {
                val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.US).format(Date(timestamp))
                "$dateStr at $timeStr"
            }
        }
    }
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF2F4F7)
        ) {
            Text(
                text = dateText,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF65676B),
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun CallLogBubble(
    msg: ChatMessage,
    currentLanguage: AppLanguage,
    onCallBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFF0F2F5),
            modifier = Modifier.clickable { onCallBack() }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = if (msg.callType == "video") "📹" else "📞", fontSize = 16.sp)
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    val label = if (msg.callStatus == "missed") {
                        if (msg.callType == "video") "Missed video call" else "Missed audio call"
                    } else {
                        val m = msg.callDurationSec / 60
                        val s = msg.callDurationSec % 60
                        "${if (msg.callType == "video") "Video call" else "Audio call"} (${String.format(Locale.US, "%02d:%02d", m, s)})"
                    }
                    Text(text = label, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Ink)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Tap to call back", fontSize = 10.sp, color = Color(0xFF0084FF))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "• ${MeskotStrings.formatClockTime(msg.createdAt)}",
                            fontSize = 10.sp,
                            color = MutedText
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// CHAT SETTINGS & CONVERSATION CONTROLS DIALOG
// -----------------------------------------------------------------------------------------

@Composable
fun ChatSettingsDialog(
    recipient: User,
    isOnline: Boolean = false,
    activeStatusText: String = "Active recently",
    themeColor: Color,
    onThemeChange: (Color) -> Unit,
    quickReaction: String,
    onQuickReactionChange: (String) -> Unit,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    disappearingDays: Int,
    onDisappearingDaysChange: (Int) -> Unit,
    onAudioCall: () -> Unit,
    onVideoCall: () -> Unit,
    onViewProfile: () -> Unit,
    onOpenSharedMedia: () -> Unit,
    onSearchChat: () -> Unit,
    onClearHistory: () -> Unit,
    onReport: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .clip(RoundedCornerShape(20.dp)),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Header: Recipient Profile Info
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box {
                        UserAvatar(photoUrl = recipient.photoUrl, name = recipient.displayName, size = 64)
                        if (isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(14.dp)
                                    .align(Alignment.BottomEnd)
                                    .clip(CircleShape)
                                    .background(Color(0xFF31A24C))
                                    .border(2.dp, Color.White, CircleShape)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = recipient.displayName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF050505)
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (isOnline) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFF31A24C))
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                        }
                        Text(
                            text = activeStatusText,
                            fontSize = 12.sp,
                            color = if (isOnline) Color(0xFF31A24C) else Color(0xFF65676B),
                            fontWeight = if (isOnline) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = onViewProfile,
                        shape = RoundedCornerShape(20.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("View Profile", fontSize = 13.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = LineBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // Quick Action Shortcuts Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ChatActionIconButton(
                        icon = Icons.Default.Call,
                        label = "Audio",
                        onClick = onAudioCall
                    )
                    ChatActionIconButton(
                        icon = Icons.Default.Videocam,
                        label = "Video",
                        onClick = onVideoCall
                    )
                    ChatActionIconButton(
                        icon = if (isMuted) Icons.Default.NotificationsOff else Icons.Default.Notifications,
                        label = if (isMuted) "Unmute" else "Mute",
                        color = if (isMuted) CrossRed else Color.Black,
                        onClick = onToggleMute
                    )
                    ChatActionIconButton(
                        icon = Icons.Default.Search,
                        label = "Search",
                        onClick = onSearchChat
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = LineBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 1: CUSTOMIZATION
                Text(text = "Chat Customization", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF65676B))
                Spacer(modifier = Modifier.height(8.dp))

                // Theme Color Palette
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Theme Color", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        val colors = listOf(
                            Color(0xFF0084FF), // Messenger Blue (Default)
                            Color(0xFFC48F37), // Meskot Gold
                            Color(0xFF10B981), // Emerald
                            Color(0xFF8B5CF6), // Royal Purple
                            Color(0xFFE11D48), // Crimson Rose
                            Color(0xFF1E293B)  // Midnight Slate
                        )
                        colors.forEach { col ->
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .background(col)
                                    .border(2.dp, if (themeColor == col) Color.Black else Color.Transparent, CircleShape)
                                    .clickable { onThemeChange(col) },
                                contentAlignment = Alignment.Center
                            ) {
                                if (themeColor == col) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Quick Reaction Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Quick Reaction", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        val reactions = listOf("👍", "❤️", "🔥", "😂", "🎉", "👏")
                        reactions.forEach { r ->
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(if (quickReaction == r) themeColor.copy(alpha = 0.2f) else Color(0xFFF0F2F5))
                                    .clickable { onQuickReactionChange(r) },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = r, fontSize = 14.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(color = LineBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 2: SHARED CONTENT & MEDIA
                Text(text = "Shared Content", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF65676B))
                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenSharedMedia() }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Folder, contentDescription = null, tint = themeColor, modifier = Modifier.size(22.dp))
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Photos & Files", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                            Text("View images and attachments shared here", fontSize = 12.sp, color = Color(0xFF65676B))
                        }
                    }
                    Text("View", fontSize = 13.sp, color = themeColor, fontWeight = FontWeight.SemiBold)
                }

                Spacer(modifier = Modifier.height(14.dp))
                HorizontalDivider(color = LineBorder)
                Spacer(modifier = Modifier.height(14.dp))

                // SECTION 3: PRIVACY & ACTIONS
                Text(text = "Privacy & Support", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF65676B))
                Spacer(modifier = Modifier.height(8.dp))

                // Disappearing Messages
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Disappearing Messages", fontSize = 14.sp, fontWeight = FontWeight.Medium, color = Color.Black)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf(0 to "Off", 1 to "24h", 7 to "7d").forEach { (days, label) ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (disappearingDays == days) themeColor else Color(0xFFF0F2F5))
                                    .clickable { onDisappearingDaysChange(days) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    color = if (disappearingDays == days) Color.White else Color.Black,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Clear History
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onClearHistory() }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = CrossRed, modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Clear Chat History", fontSize = 14.sp, color = CrossRed, fontWeight = FontWeight.Medium)
                }

                // Report
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onReport() }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Report, contentDescription = null, tint = Color(0xFF65676B), modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Report Conversation", fontSize = 14.sp, color = Color(0xFF050505), fontWeight = FontWeight.Medium)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF0F2F5))
                ) {
                    Text("Done", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ChatActionIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color = Color.Black,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(Color(0xFFF0F2F5)),
            contentAlignment = Alignment.Center
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = label, fontSize = 12.sp, color = color, fontWeight = FontWeight.Medium)
    }
}

// -----------------------------------------------------------------------------------------
// SHARED MEDIA GALLERY (PHOTOS & FILES)
// -----------------------------------------------------------------------------------------

@Composable
fun SharedMediaDialog(
    messages: List<ChatMessage>,
    recipient: User,
    themeColor: Color,
    onImageClick: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Photos, 1 = Files
    val photos = remember(messages) {
        messages.filter { it.mediaType == "image" && !it.mediaUrl.isNullOrBlank() }
    }
    val files = remember(messages) {
        messages.filter { it.mediaType == "file" }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .height(520.dp)
                .clip(RoundedCornerShape(20.dp)),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Shared Media",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Button(
                        onClick = { selectedTab = 0 },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 0) themeColor else Color(0xFFF0F2F5)
                        )
                    ) {
                        Text("Photos (${photos.size})", color = if (selectedTab == 0) Color.White else Color.Black)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { selectedTab = 1 },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedTab == 1) themeColor else Color(0xFFF0F2F5)
                        )
                    ) {
                        Text("Files (${files.size})", color = if (selectedTab == 1) Color.White else Color.Black)
                    }
                }

                if (selectedTab == 0) {
                    if (photos.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No photos shared in this chat yet", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(photos) { p ->
                                AsyncImage(
                                    model = p.mediaUrl,
                                    contentDescription = "Shared photo",
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { p.mediaUrl?.let { onImageClick(it) } },
                                    contentScale = ContentScale.Crop
                                )
                            }
                        }
                    }
                } else {
                    if (files.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No documents shared in this chat yet", color = Color.Gray, fontSize = 14.sp)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(files) { f ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF7F8FA))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Default.Description, contentDescription = null, tint = themeColor)
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = f.fileName ?: "File", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                            Text(text = f.fileSize ?: "Attachment", color = Color.Gray, fontSize = 11.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------------------
// FULL-SCREEN IMAGE VIEWER
// -----------------------------------------------------------------------------------------

@Composable
fun FullScreenImageViewerDialog(
    imageUrl: String,
    recipientName: String,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            AsyncImage(
                model = imageUrl,
                contentDescription = "Full Image Preview",
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                contentScale = ContentScale.Fit
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 24.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(28.dp))
                }
                Text(text = recipientName, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                IconButton(onClick = { /* Share action */ }) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White, modifier = Modifier.size(24.dp))
                }
            }
        }
    }
}
