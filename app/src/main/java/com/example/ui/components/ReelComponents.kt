package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.AppLanguage
import com.example.data.Comment
import com.example.data.MeskotStrings
import com.example.data.Post
import com.example.data.User
import com.example.ui.theme.*
import kotlinx.coroutines.delay

/**
 * Authentic Ethiopian / Habesha preset reels for testing & rich discovery.
 */
data class PresetReelItem(
    val id: String,
    val title: String,
    val mediaUrl: String,
    val audioTitle: String,
    val description: String,
    val category: String
)

val PresetReelsCollection = listOf(
    PresetReelItem(
        id = "reel_buna",
        title = "Traditional Coffee Ceremony",
        mediaUrl = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=900&auto=format&fit=crop&q=80",
        audioTitle = "Mahmoud Ahmed - Tezeta",
        description = "Freshly roasted beans, burning incense & jebena brewing in Bole ☕✨ #AddisAbaba #CoffeeCeremony #HabeshaCulture",
        category = "Culture"
    ),
    PresetReelItem(
        id = "reel_eskista",
        title = "Gojjam & Gondar Eskista",
        mediaUrl = "https://images.unsplash.com/photo-1547471080-7cc2caa01a7e?w=900&auto=format&fit=crop&q=80",
        audioTitle = "Teddy Afro - Tikur Sew",
        description = "Fast-tempo shoulder dance rhythm at the cultural hall! 💃🇪🇹 #Eskista #MeskotReels #EthiopianDance",
        category = "Dance"
    ),
    PresetReelItem(
        id = "reel_simien",
        title = "Simien Highlands Sunset",
        mediaUrl = "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?w=900&auto=format&fit=crop&q=80",
        audioTitle = "Aster Aweke - Hagere",
        description = "Breathtaking peaks over 4,000m with the gelada baboons roaming free 🏔️✨ #Ethiopia #SimienMountains #TravelReels",
        category = "Nature"
    ),
    PresetReelItem(
        id = "reel_lalibela",
        title = "Rock-Hewn Churches of Lalibela",
        mediaUrl = "https://images.unsplash.com/photo-1578922746465-3a80a228f223?w=900&auto=format&fit=crop&q=80",
        audioTitle = "Mulatu Astatke - Yekermo Sew",
        description = "Centuries of timeless heritage carved into living red volcanic rock ⛪🙏 #Lalibela #Heritage #EthiopiaHistory",
        category = "Heritage"
    ),
    PresetReelItem(
        id = "reel_nightlife",
        title = "Addis City Lights & Jazz",
        mediaUrl = "https://images.unsplash.com/photo-1517457373958-b7bdd4587205?w=900&auto=format&fit=crop&q=80",
        audioTitle = "Rophnan - Chereqa",
        description = "Live Ethio-Jazz night with saxophone vibes in Kazanchis 🎷🌃 #EthioJazz #AddisNightlife #MeskotVibes",
        category = "Music"
    )
)

val PopularAudioTracks = listOf(
    "Teddy Afro - Tikur Sew",
    "Aster Aweke - Hagere",
    "Rophnan - Chereqa",
    "Mahmoud Ahmed - Tezeta",
    "Mulatu Astatke - Yekermo Sew",
    "Tilahun Gessesse - Sew Menen",
    "Original Sound"
)

/**
 * Interactive Create Reel Dialog:
 * - Pick video / media from phone or choose an authentic preset
 * - Set caption with Ethiopian cultural hashtag helpers
 * - Pick audio track
 * - Apply visual color styling
 * - Live vertical 9:16 interactive preview
 */
@Composable
fun CreateReelDialog(
    currentUser: User,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSubmitReel: (videoUrl: String, caption: String, audioTrackTitle: String, thumbnailUrl: String, visibility: String) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var selectedMediaUrl by remember { mutableStateOf<String?>(PresetReelsCollection.first().mediaUrl) }
    var captionText by remember { mutableStateOf(PresetReelsCollection.first().description) }
    var selectedAudioTrack by remember { mutableStateOf(PresetReelsCollection.first().audioTitle) }
    var customAudioInput by remember { mutableStateOf("") }
    var isCustomAudioOpen by remember { mutableStateOf(false) }
    var selectedVisibility by remember { mutableStateOf("public") }
    var isPrivacyDropdownOpen by remember { mutableStateOf(false) }
    var selectedFilter by remember { mutableStateOf("Normal") }
    var isPlayingPreview by remember { mutableStateOf(true) }
    var isUploading by remember { mutableStateOf(false) }

    // Picker for local media (videos/photos)
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedMediaUrl = uri.toString()
        }
    }

    // Camera launcher for quick reel video capture or snap
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            try {
                val cachedPath = saveStoryBitmapToCache(context, bitmap)
                selectedMediaUrl = cachedPath
            } catch (e: Exception) {
                Toast.makeText(context, "Could not record snapshot: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val colorFilter = remember(selectedFilter) {
        when (selectedFilter) {
            "Warm Gold" -> ColorFilter.colorMatrix(ColorMatrix().apply {
                setToScale(1.18f, 1.05f, 0.88f, 1.0f)
            })
            "Vintage" -> ColorFilter.colorMatrix(ColorMatrix().apply {
                setToSaturation(0.65f)
            })
            "Vivid" -> ColorFilter.colorMatrix(ColorMatrix().apply {
                setToSaturation(1.4f)
            })
            "Monochrome" -> ColorFilter.colorMatrix(ColorMatrix().apply {
                setToSaturation(0f)
            })
            else -> null
        }
    }

    Dialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF0F0E0C)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .statusBarsPadding()
                    .navigationBarsPadding()
            ) {
                // Top Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, tint = Gold, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Create Meskot Reel",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontFamily = FontFamily.Serif
                        )
                    }

                    Button(
                        onClick = {
                            val url = selectedMediaUrl ?: PresetReelsCollection.first().mediaUrl
                            val audio = if (customAudioInput.isNotBlank()) customAudioInput else selectedAudioTrack
                            isUploading = true
                            onSubmitReel(url, captionText, audio, url, selectedVisibility)
                        },
                        enabled = !isUploading && (!selectedMediaUrl.isNullOrBlank() || captionText.isNotBlank()),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.White),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        if (isUploading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Post Reel", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.1f))

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    item {
                        Spacer(modifier = Modifier.height(4.dp))
                        // Author Row & Audience selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                UserAvatar(photoUrl = currentUser.photoUrl, name = currentUser.displayName, size = 44)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = currentUser.displayName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                    Text(
                                        text = "Sharing to Meskot Reels Feed",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.6f)
                                    )
                                }
                            }

                            // Audience Dropdown
                            Box {
                                Surface(
                                    onClick = { isPrivacyDropdownOpen = true },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.12f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val visLabel = when (selectedVisibility) {
                                            "public" -> "🌍 Public"
                                            "friends" -> "👥 Friends"
                                            else -> "🔒 Only Me"
                                        }
                                        Text(text = "$visLabel ▾", fontSize = 12.sp, color = Color.White, fontWeight = FontWeight.Medium)
                                    }
                                }

                                DropdownMenu(
                                    expanded = isPrivacyDropdownOpen,
                                    onDismissRequest = { isPrivacyDropdownOpen = false }
                                ) {
                                    DropdownMenuItem(
                                        text = { Text("🌍 Public") },
                                        onClick = { selectedVisibility = "public"; isPrivacyDropdownOpen = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("👥 Friends Only") },
                                        onClick = { selectedVisibility = "friends"; isPrivacyDropdownOpen = false }
                                    )
                                    DropdownMenuItem(
                                        text = { Text("🔒 Only Me") },
                                        onClick = { selectedVisibility = "onlyme"; isPrivacyDropdownOpen = false }
                                    )
                                }
                            }
                        }
                    }

                    // Interactive 9:16 Vertical Reel Preview Box
                    item {
                        Text(
                            text = "PREVIEW (VERTICAL 9:16)",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gold,
                            letterSpacing = 1.sp
                        )

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(380.dp)
                                .clip(RoundedCornerShape(18.dp))
                                .background(Color.Black)
                                .border(1.5.dp, GoldBorder.copy(alpha = 0.6f), RoundedCornerShape(18.dp))
                                .clickable { isPlayingPreview = !isPlayingPreview },
                            contentAlignment = Alignment.Center
                        ) {
                            if (!selectedMediaUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = selectedMediaUrl,
                                    contentDescription = "Reel Visual",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    colorFilter = colorFilter
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .background(Brush.verticalGradient(listOf(Color(0xFF2C2418), Color(0xFF141210)))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(imageVector = Icons.Default.VideoCameraBack, contentDescription = null, tint = Gold, modifier = Modifier.size(48.dp))
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Select a video or preset below", color = Color.White, fontSize = 13.sp)
                                    }
                                }
                            }

                            // Dark gradient overlay for bottom text
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(
                                        Brush.verticalGradient(
                                            0.0f to Color.Black.copy(alpha = 0.35f),
                                            0.55f to Color.Transparent,
                                            1.0f to Color.Black.copy(alpha = 0.85f)
                                        )
                                    )
                            )

                            // Play / Pause Icon overlay
                            if (!isPlayingPreview) {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.65f))
                                        .border(1.5.dp, Gold, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(36.dp))
                                }
                            }

                            // Reel Badge top left
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(12.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black.copy(alpha = 0.6f))
                                    .border(1.dp, Gold.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, tint = Gold, modifier = Modifier.size(13.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("REEL", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }

                            // Overlay details bottom
                            Column(
                                modifier = Modifier
                                    .align(Alignment.BottomStart)
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    UserAvatar(photoUrl = currentUser.photoUrl, name = currentUser.displayName, size = 28)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = currentUser.displayName,
                                        color = Color.White,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(Gold)
                                            .padding(horizontal = 4.dp, vertical = 1.dp)
                                    ) {
                                        Text("Creator", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Ink)
                                    }
                                }

                                if (captionText.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = captionText,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Spacer(modifier = Modifier.height(6.dp))
                                // Audio Track Pill with Equalizer Waveform
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.Black.copy(alpha = 0.5f))
                                        .border(0.8.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 8.dp, vertical = 3.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = Gold, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (customAudioInput.isNotBlank()) customAudioInput else selectedAudioTrack,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        maxLines = 1
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    EqualizerBarsAnimation(isPlaying = isPlayingPreview)
                                }
                            }
                        }
                    }

                    // Video Source Selectors: Camera / Device Gallery / Presets
                    item {
                        Text(
                            text = "CHOOSE REEL VIDEO SOURCE",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Pick from Device Gallery button
                            Surface(
                                onClick = {
                                    pickMediaLauncher.launch(
                                        androidx.activity.result.PickVisualMediaRequest(
                                            ActivityResultContracts.PickVisualMedia.ImageAndVideo
                                        )
                                    )
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(imageVector = Icons.Default.PhotoLibrary, contentDescription = null, tint = Gold, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("From Gallery", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }

                            // Capture Camera button
                            Surface(
                                onClick = {
                                    val hasCamera = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                                    if (hasCamera) {
                                        takePictureLauncher.launch(null)
                                    } else {
                                        takePictureLauncher.launch(null)
                                    }
                                },
                                shape = RoundedCornerShape(12.dp),
                                color = Color.White.copy(alpha = 0.08f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.2f)),
                                modifier = Modifier.weight(1f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Icon(imageVector = Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Camera Snap", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Habesha Cultural Preset Reels Library
                    item {
                        Text(
                            text = "🇪🇹 HABESHA CULTURAL REEL PRESETS:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Gold,
                            letterSpacing = 0.5.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(PresetReelsCollection) { preset ->
                                val isSelected = selectedMediaUrl == preset.mediaUrl
                                Column(
                                    modifier = Modifier
                                        .width(100.dp)
                                        .clickable {
                                            selectedMediaUrl = preset.mediaUrl
                                            captionText = preset.description
                                            selectedAudioTrack = preset.audioTitle
                                        },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(100.dp, 130.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .border(
                                                width = if (isSelected) 2.5.dp else 0.8.dp,
                                                color = if (isSelected) Gold else Color.White.copy(alpha = 0.25f),
                                                shape = RoundedCornerShape(12.dp)
                                            )
                                    ) {
                                        AsyncImage(
                                            model = preset.mediaUrl,
                                            contentDescription = preset.title,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                        Box(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .fillMaxWidth()
                                                .background(Color.Black.copy(alpha = 0.7f))
                                                .padding(vertical = 3.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = preset.category,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = if (isSelected) Gold else Color.White
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = preset.title,
                                        fontSize = 11.sp,
                                        color = if (isSelected) Gold else Color.White.copy(alpha = 0.8f),
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }

                    // Caption Input with Ethiopian Cultural Hashtag Chips
                    item {
                        Text(
                            text = "CAPTION & HASHTAGS",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedTextField(
                            value = captionText,
                            onValueChange = { captionText = it },
                            placeholder = { Text("Write a catchy caption for your Reel...", color = Color.White.copy(alpha = 0.5f)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(95.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedContainerColor = Color.White.copy(alpha = 0.06f),
                                unfocusedContainerColor = Color.White.copy(alpha = 0.06f),
                                focusedBorderColor = Gold,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(fontSize = 14.sp)
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        // Quick Hashtag Chips
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            val tags = listOf("#MeskotReels", "#HabeshaVibes", "#AddisAbaba", "#EthiopianCulture", "#ViralReels")
                            items(tags) { tag ->
                                Surface(
                                    onClick = {
                                        if (!captionText.contains(tag)) {
                                            captionText = if (captionText.isBlank()) tag else "$captionText $tag"
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color.White.copy(alpha = 0.1f),
                                    border = androidx.compose.foundation.BorderStroke(0.8.dp, GoldBorder.copy(alpha = 0.6f))
                                ) {
                                    Text(
                                        text = tag,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Gold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Audio Sound Track Selector
                    item {
                        Text(
                            text = "AUDIO & MUSIC SOUNDTRACK",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(PopularAudioTracks) { track ->
                                val isSelected = selectedAudioTrack == track && !isCustomAudioOpen
                                Surface(
                                    onClick = {
                                        selectedAudioTrack = track
                                        isCustomAudioOpen = false
                                        customAudioInput = ""
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) GoldSurface else Color.White.copy(alpha = 0.08f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 1.5.dp else 0.8.dp,
                                        color = if (isSelected) Gold else Color.White.copy(alpha = 0.2f)
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.MusicNote,
                                            contentDescription = null,
                                            tint = if (isSelected) GoldDeep else Color.White.copy(alpha = 0.7f),
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = track,
                                            fontSize = 12.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                            color = if (isSelected) GoldDeep else Color.White
                                        )
                                    }
                                }
                            }

                            // Custom Audio Track button
                            item {
                                Surface(
                                    onClick = { isCustomAudioOpen = !isCustomAudioOpen },
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isCustomAudioOpen) GoldSurface else Color.White.copy(alpha = 0.08f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Gold)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Gold, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Custom Track", fontSize = 12.sp, color = Gold, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }

                        if (isCustomAudioOpen) {
                            Spacer(modifier = Modifier.height(6.dp))
                            OutlinedTextField(
                                value = customAudioInput,
                                onValueChange = { customAudioInput = it },
                                placeholder = { Text("e.g. Mahmoud Ahmed - Ere Mela Mela", color = Color.White.copy(alpha = 0.5f)) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = Gold,
                                    unfocusedBorderColor = Color.White.copy(alpha = 0.3f),
                                    focusedTextColor = Color.White,
                                    unfocusedTextColor = Color.White
                                )
                            )
                        }
                    }

                    // Visual Filter Style
                    item {
                        Text(
                            text = "REEL COLOR FILTER",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            val filters = listOf("Normal", "Warm Gold", "Vivid", "Vintage", "Monochrome")
                            filters.forEach { f ->
                                val isSelected = selectedFilter == f
                                Surface(
                                    onClick = { selectedFilter = f },
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) GoldSurface else Color.White.copy(alpha = 0.08f),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 1.5.dp else 0.8.dp,
                                        color = if (isSelected) Gold else Color.White.copy(alpha = 0.2f)
                                    ),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 7.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = f,
                                            fontSize = 11.sp,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) GoldDeep else Color.White,
                                            textAlign = TextAlign.Center
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
    }
}

/**
 * Animated Equalizer Bars for Reels Audio Pill
 */
@Composable
fun EqualizerBarsAnimation(isPlaying: Boolean = true) {
    val infiniteTransition = rememberInfiniteTransition(label = "equalizer")

    val bar1 by infiniteTransition.animateFloat(
        initialValue = 4f,
        targetValue = 14f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val bar2 by infiniteTransition.animateFloat(
        initialValue = 12f,
        targetValue = 5f,
        animationSpec = infiniteRepeatable(
            animation = tween(420, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val bar3 by infiniteTransition.animateFloat(
        initialValue = 6f,
        targetValue = 16f,
        animationSpec = infiniteRepeatable(
            animation = tween(290, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Row(
        verticalAlignment = Alignment.Bottom,
        horizontalArrangement = Arrangement.spacedBy(2.dp),
        modifier = Modifier.height(16.dp)
    ) {
        Box(modifier = Modifier.width(2.5.dp).height(if (isPlaying) bar1.dp else 6.dp).clip(RoundedCornerShape(1.dp)).background(Gold))
        Box(modifier = Modifier.width(2.5.dp).height(if (isPlaying) bar2.dp else 10.dp).clip(RoundedCornerShape(1.dp)).background(Gold))
        Box(modifier = Modifier.width(2.5.dp).height(if (isPlaying) bar3.dp else 7.dp).clip(RoundedCornerShape(1.dp)).background(Gold))
    }
}

/**
 * Full Screen 9:16 Immersive Reel Player:
 * - Full vertical layout with Ken Burns motion & tap to play/pause
 * - Author metadata with follow pill
 * - Rotating vinyl record with music track title marquee
 * - Floating vertical interaction bar (Like, Comment, Share, Tip Birr)
 */
@Composable
fun ReelViewerDialog(
    reel: Post,
    currentUser: User?,
    currentLanguage: AppLanguage,
    comments: List<Comment> = emptyList(),
    isFollowing: Boolean = false,
    isSaved: Boolean = false,
    isLiked: Boolean = currentUser != null && reel.reactions.containsKey(currentUser.uid),
    onDismiss: () -> Unit,
    onToggleLike: () -> Unit,
    onToggleFollow: () -> Unit = {},
    onToggleSave: () -> Unit = {},
    onShare: () -> Unit,
    onTip: () -> Unit,
    onComment: () -> Unit = {},
    onAddComment: (text: String, parentId: String?) -> Unit = { _, _ -> },
    onToggleCommentLike: (commentId: String) -> Unit = {},
    onDeleteComment: (commentId: String) -> Unit = {},
    onAuthorClick: (uid: String) -> Unit = {}
) {
    var isPlaying by remember { mutableStateOf(true) }
    var showHeartPop by remember { mutableStateOf(false) }
    var isCommentsSheetOpen by remember { mutableStateOf(false) }

    LaunchedEffect(showHeartPop) {
        if (showHeartPop) {
            delay(800)
            showHeartPop = false
        }
    }

    // Vinyl record rotation
    val infiniteTransition = rememberInfiniteTransition(label = "vinyl")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "disc_rotate"
    )

    // Progress scrubber simulation
    var progress by remember { mutableStateOf(0f) }
    LaunchedEffect(isPlaying) {
        while (isPlaying) {
            delay(100)
            progress = (progress + 0.01f)
            if (progress >= 1f) progress = 0f
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {
                                if (isCommentsSheetOpen) {
                                    isCommentsSheetOpen = false
                                } else {
                                    isPlaying = !isPlaying
                                }
                            },
                            onDoubleTap = {
                                if (!isLiked) {
                                    showHeartPop = true
                                    onToggleLike()
                                } else {
                                    showHeartPop = true
                                }
                            }
                        )
                    }
            ) {
                // Reel Background Visual
                val mediaUrl = reel.videoUrl.ifBlank { reel.mediaUrls.firstOrNull() }
                if (!mediaUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = mediaUrl,
                        contentDescription = "Reel Content",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(listOf(Color(0xFF2E2419), Color(0xFF15120E)))),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(reel.text, color = Color.White, fontSize = 20.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(24.dp))
                    }
                }

                // Vertical Dark Vignette Gradient
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                0.0f to Color.Black.copy(alpha = 0.5f),
                                0.25f to Color.Transparent,
                                0.65f to Color.Transparent,
                                1.0f to Color.Black.copy(alpha = 0.9f)
                            )
                        )
                )

                // Top Navigation Bar
                Row(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }

                        Spacer(modifier = Modifier.width(10.dp))

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.6f))
                                .border(1.dp, Gold.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(imageVector = Icons.Default.PlayCircle, contentDescription = null, tint = Gold, modifier = Modifier.size(13.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("REEL", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            }
                        }
                    }

                    // Sound icon
                    IconButton(
                        onClick = { isPlaying = !isPlaying },
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.5f))
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = "Audio Mute",
                            tint = Color.White
                        )
                    }
                }

                // Play / Pause Splash Overlay
                if (!isPlaying && !isCommentsSheetOpen) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(Color.Black.copy(alpha = 0.65f))
                            .border(2.dp, Gold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = "Paused", tint = Color.White, modifier = Modifier.size(42.dp))
                    }
                }

                // Double-tap / like Heart Pop Animation
                AnimatedVisibility(
                    visible = showHeartPop,
                    enter = scaleIn(tween(250)) + fadeIn(),
                    exit = scaleOut(tween(250)) + fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = CrossRed, modifier = Modifier.size(90.dp))
                }

                // Floating Vertical Action Bar (Right Side)
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(end = 12.dp, bottom = 80.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Like Action
                    val displayLikes = if (reel.reactions.isNotEmpty()) reel.reactions.size else if (isLiked) 1 else 0
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                if (!isLiked) showHeartPop = true
                                onToggleLike()
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Like",
                                tint = if (isLiked) CrossRed else Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Text(
                            text = "$displayLikes",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Comments Action
                    val displayCommentsCount = if (comments.isNotEmpty()) comments.size else reel.commentCount
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = {
                                isCommentsSheetOpen = true
                                onComment()
                            },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = "Comments",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "$displayCommentsCount",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Tip Creator (Birr)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = onTip,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(GoldSurface)
                                .border(1.2.dp, Gold, CircleShape)
                        ) {
                            Text(text = "ብር", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GoldDeep)
                        }
                        Text(
                            text = "Tip",
                            color = Gold,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Save / Bookmark Action
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = onToggleSave,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = if (isSaved) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                                contentDescription = "Save",
                                tint = if (isSaved) Gold else Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = if (isSaved) "Saved" else "Save",
                            color = if (isSaved) Gold else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Share Action
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        IconButton(
                            onClick = onShare,
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(Color.Black.copy(alpha = 0.5f))
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Share,
                                contentDescription = "Share",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Text(
                            text = "${reel.sharesCount}",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Rotating Vinyl Music Disc
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .rotate(if (isPlaying) rotationAngle else 0f)
                            .clip(CircleShape)
                            .background(Color(0xFF1E1E1E))
                            .border(2.dp, Gold, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(14.dp)
                                .clip(CircleShape)
                                .background(Gold)
                        )
                    }
                }

                // Bottom Content Metadata: Author, Caption & Audio Marquee
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth(0.78f)
                        .padding(start = 16.dp, bottom = 28.dp)
                ) {
                    // Author info
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(
                            photoUrl = reel.authorPhoto,
                            name = reel.authorName,
                            size = 38,
                            modifier = Modifier.clickable { onAuthorClick(reel.uid) }
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = reel.authorName,
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onAuthorClick(reel.uid) }
                        )
                        if (reel.isAuthorVerified) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Verified",
                                tint = Gold,
                                modifier = Modifier.size(16.dp)
                            )
                        }

                        // Follow / Following pill
                        if (currentUser?.uid != reel.uid) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                onClick = onToggleFollow,
                                shape = RoundedCornerShape(12.dp),
                                color = if (isFollowing) Color.White.copy(alpha = 0.2f) else GoldDeep,
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isFollowing) Color.White.copy(alpha = 0.4f) else Gold
                                )
                            ) {
                                Text(
                                    text = if (isFollowing) "Following" else "Follow",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }

                    // Caption
                    if (reel.text.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = reel.text,
                            color = Color.White,
                            fontSize = 13.sp,
                            maxLines = 3,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Audio Track Marquee
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color.Black.copy(alpha = 0.5f))
                            .border(0.8.dp, Gold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(imageVector = Icons.Default.MusicNote, contentDescription = null, tint = Gold, modifier = Modifier.size(13.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = reel.audioTrackTitle.ifBlank { "Original Audio" },
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        EqualizerBarsAnimation(isPlaying = isPlaying)
                    }
                }

                // Bottom Progress Bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .height(2.5.dp),
                    color = Gold,
                    trackColor = Color.White.copy(alpha = 0.2f)
                )

                // Reel Comments Bottom Sheet
                if (isCommentsSheetOpen) {
                    ReelCommentsBottomSheet(
                        reel = reel,
                        comments = comments,
                        currentUser = currentUser,
                        currentLanguage = currentLanguage,
                        onDismiss = { isCommentsSheetOpen = false },
                        onAddComment = onAddComment,
                        onToggleCommentLike = onToggleCommentLike,
                        onDeleteComment = onDeleteComment,
                        onAuthorClick = onAuthorClick
                    )
                }
            }
        }
    }
}

/**
 * Modern Facebook / Instagram style bottom sheet for Reels comments:
 * - Header with count and close
 * - Scrollable comments with likes, replies, and author info
 * - Quick emoji shortcuts (❤️, 🔥, 👏, 😂, 🇪🇹, ☕)
 * - Real-time add comment input with user avatar
 */
@Composable
fun ReelCommentsBottomSheet(
    reel: Post,
    comments: List<Comment>,
    currentUser: User?,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onAddComment: (text: String, parentId: String?) -> Unit,
    onToggleCommentLike: (commentId: String) -> Unit,
    onDeleteComment: (commentId: String) -> Unit,
    onAuthorClick: (uid: String) -> Unit
) {
    var commentText by remember { mutableStateOf("") }
    var replyingTo by remember { mutableStateOf<Comment?>(null) }
    val focusManager = LocalFocusManager.current

    val quickEmojis = listOf("❤️", "🔥", "👏", "😂", "🇪🇹", "☕", "🙌", "😍")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(onClick = onDismiss),
        contentAlignment = Alignment.BottomCenter
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.72f)
                .clickable(enabled = false) {},
            shape = RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                // Drag handle
                Box(
                    modifier = Modifier
                        .padding(top = 10.dp)
                        .size(width = 38.dp, height = 4.dp)
                        .clip(CircleShape)
                        .background(Color.White.copy(alpha = 0.3f))
                        .align(Alignment.CenterHorizontally)
                )

                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Comments (${comments.size})",
                        color = Color.White,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White.copy(alpha = 0.8f),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.12f), thickness = 0.8.dp)

                // Comments List
                if (comments.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.ChatBubbleOutline,
                                contentDescription = null,
                                tint = Gold.copy(alpha = 0.7f),
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "No comments yet",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Be the first to share your thoughts on this reel! ✨",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 13.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        val topLevel = comments.filter { it.parentId == null }
                        items(topLevel, key = { it.id }) { comment ->
                            ReelCommentItemCard(
                                comment = comment,
                                currentUserId = currentUser?.uid,
                                isReply = false,
                                onAuthorClick = onAuthorClick,
                                onToggleLike = { onToggleCommentLike(comment.id) },
                                onReply = {
                                    replyingTo = comment
                                    commentText = "@${comment.authorName} "
                                },
                                onDelete = { onDeleteComment(comment.id) }
                            )

                            // Threaded replies
                            val replies = comments.filter { it.parentId == comment.id }
                            replies.forEach { reply ->
                                ReelCommentItemCard(
                                    comment = reply,
                                    currentUserId = currentUser?.uid,
                                    isReply = true,
                                    onAuthorClick = onAuthorClick,
                                    onToggleLike = { onToggleCommentLike(reply.id) },
                                    onReply = {
                                        replyingTo = comment
                                        commentText = "@${reply.authorName} "
                                    },
                                    onDelete = { onDeleteComment(reply.id) }
                                )
                            }
                        }
                    }
                }

                // Replying Indicator Banner
                if (replyingTo != null) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFF282828))
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Replying to ${replyingTo?.authorName}",
                            color = Gold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                        IconButton(
                            onClick = { replyingTo = null; commentText = "" },
                            modifier = Modifier.size(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Cancel reply",
                                tint = Color.White.copy(alpha = 0.6f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }

                // Quick Emojis
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF181818))
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(quickEmojis) { emoji ->
                        Surface(
                            onClick = { commentText += emoji },
                            shape = CircleShape,
                            color = Color.White.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = emoji,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color.White.copy(alpha = 0.08f), thickness = 0.8.dp)

                // Bottom Input Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E))
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(
                        photoUrl = currentUser?.photoUrl,
                        name = currentUser?.displayName ?: "User",
                        size = 34
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    OutlinedTextField(
                        value = commentText,
                        onValueChange = { commentText = it },
                        modifier = Modifier
                            .weight(1f)
                            .heightIn(min = 40.dp, max = 100.dp),
                        placeholder = {
                            Text(
                                text = if (replyingTo != null) "Reply to ${replyingTo?.authorName}..." else "Add a comment...",
                                color = Color.White.copy(alpha = 0.45f),
                                fontSize = 13.sp
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color(0xFF2B2B2B),
                            unfocusedContainerColor = Color(0xFF2B2B2B),
                            focusedBorderColor = Gold,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    IconButton(
                        onClick = {
                            if (commentText.isNotBlank()) {
                                onAddComment(commentText.trim(), replyingTo?.id)
                                commentText = ""
                                replyingTo = null
                                focusManager.clearFocus()
                            }
                        },
                        enabled = commentText.isNotBlank(),
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (commentText.isNotBlank()) Gold else Color.White.copy(alpha = 0.15f))
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = "Send comment",
                            tint = if (commentText.isNotBlank()) Color.Black else Color.White.copy(alpha = 0.4f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReelCommentItemCard(
    comment: Comment,
    currentUserId: String?,
    isReply: Boolean,
    onAuthorClick: (String) -> Unit,
    onToggleLike: () -> Unit,
    onReply: () -> Unit,
    onDelete: () -> Unit
) {
    val isLiked = currentUserId?.let { comment.likes[it] } == true
    val likeCount = comment.likes.size

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = if (isReply) 36.dp else 0.dp),
        verticalAlignment = Alignment.Top
    ) {
        UserAvatar(
            photoUrl = comment.authorPhoto,
            name = comment.authorName,
            size = if (isReply) 26 else 32,
            modifier = Modifier.clickable { onAuthorClick(comment.uid) }
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Surface(
                shape = RoundedCornerShape(14.dp),
                color = Color(0xFF2A2A2A)
            ) {
                Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = comment.authorName,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.clickable { onAuthorClick(comment.uid) }
                            )
                            if (comment.isAuthorVerified) {
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Verified",
                                    tint = Gold,
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                        }

                        if (comment.uid == currentUserId) {
                            IconButton(onClick = onDelete, modifier = Modifier.size(20.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    tint = CrossRed.copy(alpha = 0.8f),
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    Text(
                        text = comment.text,
                        color = Color.White.copy(alpha = 0.95f),
                        fontSize = 13.sp,
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }

            // Action Row: Like button + count, Reply
            Row(
                modifier = Modifier.padding(start = 6.dp, top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Like Button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable(onClick = onToggleLike)
                ) {
                    Icon(
                        imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = "Like comment",
                        tint = if (isLiked) CrossRed else Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(14.dp)
                    )
                    if (likeCount > 0) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$likeCount",
                            color = if (isLiked) CrossRed else Color.White.copy(alpha = 0.6f),
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Reply Button
                Text(
                    text = "Reply",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.clickable(onClick = onReply)
                )
            }
        }
    }
}

/**
 * Horizontal Carousel for discovering and posting Meskot Reels.
 */
@Composable
fun MeskotReelsRail(
    reels: List<Post>,
    onReelClick: (Post) -> Unit,
    onCreateReelClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        // Section Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(GoldSurface)
                        .border(1.dp, GoldBorder, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = GoldDeep,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Meskot Reels",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GoldSurface)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "NEW",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldDeep
                    )
                }
            }

            Surface(
                onClick = onCreateReelClick,
                shape = RoundedCornerShape(16.dp),
                color = GoldSurface,
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = GoldDeep,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Create Reel",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldDeep
                    )
                }
            }
        }

        // Horizontal Reels List
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // First Item: "+ Create Reel" Quick Action Card
            item {
                Box(
                    modifier = Modifier
                        .width(115.dp)
                        .height(180.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.verticalGradient(
                                listOf(Color(0xFF2E2218), Color(0xFF140E0A))
                            )
                        )
                        .border(1.2.dp, GoldBorder, RoundedCornerShape(14.dp))
                        .clickable { onCreateReelClick() }
                        .padding(10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(GoldDeep)
                                .border(2.dp, Gold, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Create Reel",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Create Reel",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Share Moments",
                            color = Gold.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                    }
                }
            }

            // Real posts with videoUrl or postType == "REEL"
            items(reels, key = { "reel_card_${it.id}" }) { reel ->
                ReelRailCard(
                    reel = reel,
                    onClick = { onReelClick(reel) }
                )
            }

            // If user has few or no reels, also display Ethiopian cultural preset reels
            if (reels.size < 3) {
                val samplePresets = PresetReelsCollection.take(3)
                items(samplePresets, key = { "preset_rail_${it.id}" }) { preset ->
                    val mockPost = Post(
                        id = "preset_${preset.id}",
                        uid = "meskot_official",
                        authorName = "Meskot Culture",
                        authorPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=200",
                        text = preset.description,
                        postType = "REEL",
                        videoUrl = preset.mediaUrl,
                        audioTrackTitle = preset.audioTitle,
                        viewsCount = 1840,
                        createdAt = System.currentTimeMillis()
                    )
                    ReelRailCard(
                        reel = mockPost,
                        onClick = { onReelClick(mockPost) }
                    )
                }
            }
        }
    }
}

@Composable
fun ReelRailCard(
    reel: Post,
    onClick: () -> Unit
) {
    val mediaUrl = reel.videoUrl.ifBlank { reel.mediaUrls.firstOrNull() }
    Box(
        modifier = Modifier
            .width(115.dp)
            .height(180.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black)
            .border(1.dp, LineBorder.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
            .clickable { onClick() }
    ) {
        if (!mediaUrl.isNullOrBlank()) {
            AsyncImage(
                model = mediaUrl,
                contentDescription = reel.text,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        }

        // Vertical gradient
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        0.0f to Color.Black.copy(alpha = 0.3f),
                        0.5f to Color.Transparent,
                        1.0f to Color.Black.copy(alpha = 0.85f)
                    )
                )
        )

        // Top Views Badge
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(6.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black.copy(alpha = 0.55f))
                .padding(horizontal = 5.dp, vertical = 2.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = if (reel.viewsCount > 0) "${reel.viewsCount}" else "1.2K",
                    color = Color.White,
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bottom Content
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
        ) {
            Text(
                text = reel.authorName.split(" ").firstOrNull() ?: reel.authorName,
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (reel.text.isNotBlank()) {
                Text(
                    text = reel.text,
                    color = Color.White.copy(alpha = 0.85f),
                    fontSize = 9.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}
