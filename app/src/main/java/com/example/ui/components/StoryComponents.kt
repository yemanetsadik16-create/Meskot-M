package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.PickVisualMediaRequest
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.example.data.AppLanguage
import com.example.data.MeskotStrings
import com.example.data.StoryItem
import com.example.data.User
import com.example.ui.theme.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Saves a captured camera bitmap to an application cache file and returns the local file path.
 */
fun saveStoryBitmapToCache(context: Context, bitmap: Bitmap): String {
    val file = File(context.cacheDir, "story_camera_${System.currentTimeMillis()}_${UUID.randomUUID().toString().take(6)}.jpg")
    FileOutputStream(file).use { out ->
        bitmap.compress(Bitmap.CompressFormat.JPEG, 92, out)
    }
    return file.absolutePath
}

// Preset inspiration samples if device camera is unavailable
val PresetStorySamples = listOf(
    "https://images.unsplash.com/photo-1544717305-2782549b5136?auto=format&fit=crop&w=800&q=80" to "Addis Skyline",
    "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=800&q=80" to "Ethiopian Coffee",
    "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=800&q=80" to "Sunset Highlands",
    "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=80" to "Mountain Lake"
)

val StoryFilters = listOf("Normal", "Warm", "Vibrant", "Noir", "Sunset")

val StoryExpirationOptions = listOf(
    24 to "24 Hours (Standard)",
    12 to "12 Hours (Half Day)",
    6 to "6 Hours (Evening)",
    1 to "1 Hour (Flash Story)"
)

/**
 * Create Story Dialog allowing photo capture via device camera, filter styling,
 * captioning, and Firebase expiration timer selection.
 */
@Composable
fun CreateStoryDialog(
    currentUser: User,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSubmitStory: (mediaUrl: String, caption: String, filterName: String, expirationHours: Int) -> Unit
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var photoPathOrUrl by remember { mutableStateOf<String?>(null) }
    var captionText by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf("Normal") }
    var selectedExpirationHours by remember { mutableStateOf(24) }
    var isUploading by remember { mutableStateOf(false) }
    var showPermissionRationale by remember { mutableStateOf(false) }

    // Camera Capture Launcher (Takes high resolution preview bitmap from device camera)
    val takePictureLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            try {
                val cachedPath = saveStoryBitmapToCache(context, bitmap)
                photoPathOrUrl = cachedPath
            } catch (e: Exception) {
                Toast.makeText(context, "Failed to save photo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Permission Launcher for Camera
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            takePictureLauncher.launch(null)
        } else {
            showPermissionRationale = true
        }
    }

    // Gallery Picker Launcher
    val pickMediaLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            photoPathOrUrl = uri.toString()
        }
    }

    fun triggerCamera() {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPermission) {
            takePictureLauncher.launch(null)
        } else {
            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Dialog(
        onDismissRequest = { if (!isUploading) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.95f))
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    // Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Paper)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(Gold, GoldDeep))),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (photoPathOrUrl == null) "Capture Story" else "Story Editor",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink
                                )
                                Text(
                                    text = "Share moments that expire automatically",
                                    fontSize = 11.sp,
                                    color = MutedText
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            enabled = !isUploading
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Ink
                            )
                        }
                    }

                    Divider(color = LineBorder, thickness = 1.dp)

                    // Body
                    if (photoPathOrUrl == null) {
                        // Viewfinder / Capture Selector Screen
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))

                            // Camera Viewfinder Box
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(InkDark)
                                    .border(2.dp, Gold.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                                    .clickable { triggerCamera() },
                                contentAlignment = Alignment.Center
                            ) {
                                // Decorative Viewfinder Corner brackets
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(24.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(listOf(GoldLight, Gold, GoldDeep)))
                                            .padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoCamera,
                                            contentDescription = "Capture with Camera",
                                            tint = Color.White,
                                            modifier = Modifier.size(38.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        text = "Open Device Camera",
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "Tap to take a live photo for your story feed",
                                        fontSize = 12.sp,
                                        color = Color.White.copy(alpha = 0.7f),
                                        textAlign = TextAlign.Center
                                    )
                                }

                                // Top badges on viewfinder
                                Row(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Surface(
                                        color = Color.Black.copy(alpha = 0.6f),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.FlashAuto,
                                                contentDescription = null,
                                                tint = GoldLight,
                                                modifier = Modifier.size(14.dp)
                                            )
                                            Text("HDR", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // Capture Button
                            Button(
                                onClick = { triggerCamera() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldDeep),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Capture Photo Now",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }

                            // Or Choose from Gallery
                            OutlinedButton(
                                onClick = {
                                    pickMediaLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PhotoLibrary,
                                    contentDescription = null,
                                    tint = Gold,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Choose Existing Photo from Gallery",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Or Preset Quick Inspiration
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Quick Demo / Inspiration Presets:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MutedText
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(PresetStorySamples) { (sampleUrl, title) ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .width(80.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(Paper)
                                                .border(1.dp, LineBorder, RoundedCornerShape(12.dp))
                                                .clickable { photoPathOrUrl = sampleUrl }
                                                .padding(6.dp)
                                        ) {
                                            AsyncImage(
                                                model = sampleUrl,
                                                contentDescription = title,
                                                modifier = Modifier
                                                    .size(68.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                contentScale = ContentScale.Crop
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = title,
                                                fontSize = 10.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = Ink,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }

                            if (showPermissionRationale) {
                                Surface(
                                    color = GoldSurface,
                                    shape = RoundedCornerShape(12.dp),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(12.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Info,
                                            contentDescription = null,
                                            tint = GoldDeep,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Camera permission allows you to snap high quality photos. You can also pick photos from the gallery.",
                                            fontSize = 12.sp,
                                            color = Ink
                                        )
                                    }
                                }
                            }
                        }
                    } else {
                        // Photo Captured Preview & Story Editor
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Image Preview Container with Filter
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(280.dp)
                                    .clip(RoundedCornerShape(18.dp))
                                    .background(InkDark),
                                contentAlignment = Alignment.Center
                            ) {
                                val colorFilter = when (selectedFilter) {
                                    "Noir" -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                                    "Warm" -> ColorFilter.tint(Color(0xFFFFA500).copy(alpha = 0.2f), androidx.compose.ui.graphics.BlendMode.Darken)
                                    "Vibrant" -> ColorFilter.tint(Color(0xFF00AAFF).copy(alpha = 0.15f), androidx.compose.ui.graphics.BlendMode.Overlay)
                                    else -> null
                                }

                                AsyncImage(
                                    model = photoPathOrUrl,
                                    contentDescription = "Story Media Preview",
                                    modifier = Modifier.fillMaxSize(),
                                    contentScale = ContentScale.Crop,
                                    colorFilter = colorFilter
                                )

                                if (selectedFilter == "Sunset") {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    listOf(
                                                        Color(0xFFFF7E5F).copy(alpha = 0.28f),
                                                        Color(0xFFFEB47B).copy(alpha = 0.15f)
                                                    )
                                                )
                                            )
                                    )
                                }

                                // Retake / Change Button Floating on top
                                Surface(
                                    onClick = { photoPathOrUrl = null },
                                    color = Color.Black.copy(alpha = 0.65f),
                                    shape = CircleShape,
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Refresh,
                                            contentDescription = "Retake",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = "Retake",
                                            color = Color.White,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Filter Badge on preview
                                Surface(
                                    color = Color.Black.copy(alpha = 0.55f),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .align(Alignment.BottomStart)
                                        .padding(12.dp)
                                ) {
                                    Text(
                                        text = "Filter: $selectedFilter",
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            // Filter Picker Row
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Visual Style & Filter",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Ink
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(StoryFilters) { filter ->
                                        val isSelected = selectedFilter == filter
                                        Surface(
                                            onClick = { selectedFilter = filter },
                                            shape = RoundedCornerShape(16.dp),
                                            color = if (isSelected) GoldDeep else Paper,
                                            border = androidx.compose.foundation.BorderStroke(
                                                1.dp,
                                                if (isSelected) GoldDeep else LineBorder
                                            )
                                        ) {
                                            Text(
                                                text = filter,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) Color.White else Ink,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Caption Input
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Add Caption (Optional)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Ink
                                )
                                OutlinedTextField(
                                    value = captionText,
                                    onValueChange = { if (it.length <= 140) captionText = it },
                                    placeholder = { Text("What's happening in this moment?", fontSize = 13.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = Gold,
                                        unfocusedBorderColor = LineBorder
                                    ),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                                )
                                Text(
                                    text = "${captionText.length}/140",
                                    fontSize = 11.sp,
                                    color = MutedText,
                                    modifier = Modifier.align(Alignment.End)
                                )
                            }

                            // ==========================================
                            // FIREBASE EXPIRATION TIMER LOGIC SECTION
                            // ==========================================
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = GoldSurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(14.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Timer,
                                            contentDescription = null,
                                            tint = GoldDeep,
                                            modifier = Modifier.size(20.dp)
                                        )
                                        Text(
                                            text = "Firebase Expiration Timer",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Ink
                                        )
                                    }

                                    Text(
                                        text = "Your story is stored in Firebase Firestore and will automatically self-destruct and expire from everyone's feed when the timer ends.",
                                        fontSize = 11.sp,
                                        color = MutedText,
                                        lineHeight = 16.sp
                                    )

                                    // Expiration Duration Chips
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        StoryExpirationOptions.forEach { (hours, label) ->
                                            val isSelected = selectedExpirationHours == hours
                                            Surface(
                                                onClick = { selectedExpirationHours = hours },
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isSelected) Gold else CardBg,
                                                border = androidx.compose.foundation.BorderStroke(
                                                    1.dp,
                                                    if (isSelected) GoldDeep else LineBorder
                                                ),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Column(
                                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                                    horizontalAlignment = Alignment.CenterHorizontally
                                                ) {
                                                    Text(
                                                        text = "${hours}h",
                                                        fontSize = 14.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = if (isSelected) Color.White else Ink
                                                    )
                                                    Text(
                                                        text = if (hours == 24) "Standard" else if (hours == 1) "Flash" else "Quick",
                                                        fontSize = 10.sp,
                                                        color = if (isSelected) Color.White.copy(alpha = 0.9f) else MutedText
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            // Submit Button
                            Button(
                                onClick = {
                                    val finalPath = photoPathOrUrl ?: return@Button
                                    isUploading = true
                                    onSubmitStory(
                                        finalPath,
                                        captionText.trim(),
                                        selectedFilter,
                                        selectedExpirationHours
                                    )
                                },
                                enabled = !isUploading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = GoldDeep)
                            ) {
                                if (isUploading) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(24.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Uploading to Firebase…", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Send,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Share to Story (${selectedExpirationHours}h Timer) 🚀",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Story Viewer Dialog: Full screen immersive display of active story with
 * real-time expiration badge, progress playback, viewer count, and likes.
 */
@Composable
fun StoryViewerDialog(
    story: StoryItem,
    currentUser: User?,
    onDismiss: () -> Unit,
    onDeleteStory: (String) -> Unit,
    onToggleLike: (String) -> Unit
) {
    val context = LocalContext.current
    var showViewersDialog by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    val isAuthor = currentUser?.uid == story.uid
    val isLiked = currentUser?.let { story.likes[it.uid] == true } ?: false
    val likeCount = story.likes.values.count { it }

    // Real-time ticking remaining calculation
    var currentNowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(story.id) {
        while (isActive) {
            delay(1000L)
            currentNowMs = System.currentTimeMillis()
        }
    }

    // Auto-advancing progress bar (10 seconds duration per story view)
    val progress = remember { Animatable(0f) }
    LaunchedEffect(story.id) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 10_000, easing = LinearEasing)
        )
        // Auto-close when progress finishes
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            // Main Media Image
            val colorFilter = when (story.filterName) {
                "Noir" -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                "Warm" -> ColorFilter.tint(Color(0xFFFFA500).copy(alpha = 0.2f), androidx.compose.ui.graphics.BlendMode.Darken)
                "Vibrant" -> ColorFilter.tint(Color(0xFF00AAFF).copy(alpha = 0.15f), androidx.compose.ui.graphics.BlendMode.Overlay)
                else -> null
            }

            AsyncImage(
                model = story.mediaUrl,
                contentDescription = "Story by ${story.authorName}",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                colorFilter = colorFilter
            )

            if (story.filterName == "Sunset") {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xFFFF7E5F).copy(alpha = 0.28f),
                                    Color(0xFFFEB47B).copy(alpha = 0.15f)
                                )
                            )
                        )
                )
            }

            // Top scrim shadow for legibility
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Black.copy(alpha = 0.75f), Color.Transparent)
                        )
                    )
            )

            // Bottom scrim shadow for caption & buttons
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, Color.Black.copy(alpha = 0.85f))
                        )
                    )
            )

            // Top Header: Progress Bar & Story Author Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Segmented Progress Bar
                LinearProgressIndicator(
                    progress = { progress.value },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(3.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = Color.White,
                    trackColor = Color.White.copy(alpha = 0.35f),
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Author info + Expiration badge
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        UserAvatar(
                            photoUrl = story.authorPhoto,
                            name = story.authorName,
                            size = 38
                        )

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = story.authorName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                if (story.filterName != "Normal") {
                                    Surface(
                                        color = GoldDeep.copy(alpha = 0.75f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = story.filterName,
                                            fontSize = 9.sp,
                                            color = Color.White,
                                            fontWeight = FontWeight.SemiBold,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }

                            // Dynamic remaining expiration timer badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Timer,
                                    contentDescription = null,
                                    tint = GoldLight,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = story.formattedRemaining(currentNowMs),
                                    fontSize = 11.sp,
                                    color = GoldLight,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Action buttons (Delete if own story, Close X)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (isAuthor) {
                            IconButton(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Story",
                                    tint = Color.White.copy(alpha = 0.85f),
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Section: Caption & Engagement
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Caption Pill
                if (story.caption.isNotBlank()) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.55f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = story.caption,
                            fontSize = 15.sp,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            lineHeight = 20.sp
                        )
                    }
                }

                // Controls Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isAuthor) {
                        // Viewer Count Pill
                        Surface(
                            onClick = { showViewersDialog = true },
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Visibility,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "${story.viewers.size} viewers",
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Surface(
                            color = Gold.copy(alpha = 0.25f),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Favorite,
                                    contentDescription = null,
                                    tint = Color(0xFFFF4B6E),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "$likeCount likes",
                                    fontSize = 13.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    } else {
                        // Quick Reaction Pill & Like Button for viewers
                        Surface(
                            color = Color.White.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Send,
                                    contentDescription = null,
                                    tint = Color.White.copy(alpha = 0.8f),
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "Send reply to ${story.authorName.split(" ").firstOrNull()}…",
                                    fontSize = 13.sp,
                                    color = Color.White.copy(alpha = 0.8f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        // Heart Button
                        IconButton(
                            onClick = { onToggleLike(story.id) },
                            modifier = Modifier
                                .size(46.dp)
                                .clip(CircleShape)
                                .background(if (isLiked) Color(0xFFFF4B6E) else Color.White.copy(alpha = 0.2f))
                        ) {
                            Icon(
                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Like Story",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }

            // Viewers Bottom Sheet / Dialog
            if (showViewersDialog) {
                AlertDialog(
                    onDismissRequest = { showViewersDialog = false },
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = GoldDeep)
                            Text("Story Viewers (${story.viewers.size})", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    },
                    text = {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 240.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (story.viewers.isEmpty()) {
                                Text("No viewers yet. Friends will appear here as they watch.", fontSize = 13.sp, color = MutedText)
                            } else {
                                story.viewers.forEach { viewerId ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(CircleShape)
                                                .background(Paper2),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(Icons.Default.Person, contentDescription = null, tint = Ink, modifier = Modifier.size(18.dp))
                                        }
                                        Text(
                                            text = if (viewerId == currentUser?.uid) "You (Author)" else "Community Member ($viewerId)",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = Ink
                                        )
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showViewersDialog = false }) {
                            Text("Close", color = GoldDeep, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Delete Confirmation
            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    title = { Text("Delete Story?", fontWeight = FontWeight.Bold) },
                    text = { Text("This will permanently remove your story from Firebase Firestore and delete it from all friends' feeds.", fontSize = 13.sp) },
                    confirmButton = {
                        Button(
                            onClick = {
                                showDeleteConfirm = false
                                onDeleteStory(story.id)
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CrossRed)
                        ) {
                            Text("Delete Story", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showDeleteConfirm = false }) {
                            Text("Cancel", color = Ink)
                        }
                    }
                )
            }
        }
    }
}

/**
 * Story Avatar Ring Item: Displays an avatar surrounded by a colorful gradient ring
 * (or plus badge for the user's "Your Story") in the horizontal story feed.
 */
@Composable
fun StoryAvatarRingItem(
    user: User,
    hasStory: Boolean,
    isCurrentUser: Boolean,
    isStoryViewed: Boolean = false,
    expirationText: String? = null,
    onClick: () -> Unit
) {
    val activeGradient = Brush.sweepGradient(
        listOf(
            Color(0xFFF3CA68),
            Color(0xFFC48F37),
            Color(0xFFE85D04),
            Color(0xFFD00000),
            Color(0xFF9D0208),
            Color(0xFFF3CA68)
        )
    )

    val viewedGradient = Brush.linearGradient(
        listOf(Color(0xFFB0B7B3), Color(0xFFD4DAD6))
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(68.dp)
            .clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.size(62.dp),
            contentAlignment = Alignment.Center
        ) {
            if (hasStory) {
                // Gradient Story Ring
                Box(
                    modifier = Modifier
                        .size(62.dp)
                        .clip(CircleShape)
                        .background(if (isStoryViewed) viewedGradient else activeGradient)
                        .padding(2.5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(2.dp)
                    ) {
                        UserAvatar(
                            photoUrl = user.photoUrl,
                            name = user.displayName,
                            size = 52
                        )
                    }
                }
            } else {
                UserAvatar(
                    photoUrl = user.photoUrl,
                    name = user.displayName,
                    size = 56
                )
            }

            // If Current User: Plus Badge to Create Story
            if (isCurrentUser) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(GoldDeep)
                        .border(1.5.dp, Color.White, CircleShape)
                        .align(Alignment.BottomEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create Story",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            } else if (!hasStory) {
                // Green online presence dot
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(ActiveGreen)
                        .border(1.5.dp, Color.White, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            } else if (expirationText != null) {
                // Expiration badge
                Surface(
                    color = GoldDeep,
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.White),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 2.dp)
                ) {
                    Text(
                        text = expirationText,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = if (isCurrentUser) "Your Story" else user.displayName.split(" ").firstOrNull() ?: user.displayName,
            fontSize = 11.sp,
            fontWeight = if (hasStory && !isStoryViewed) FontWeight.Bold else FontWeight.Medium,
            color = Ink,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Inspiring Ethiopian community story samples for the Facebook story tray.
 */
val PresetCommunityStories = listOf(
    StoryItem(
        id = "preset_story_1",
        uid = "community_selam",
        authorName = "Selamawit T.",
        authorPhoto = "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=200&q=80",
        mediaUrl = "https://images.unsplash.com/photo-1544717305-2782549b5136?auto=format&fit=crop&w=800&q=80",
        caption = "Addis skyline looking radiant today ✨",
        filterName = "Sunset",
        createdAt = System.currentTimeMillis() - 2 * 3600 * 1000L,
        expiresAt = System.currentTimeMillis() + 22 * 3600 * 1000L
    ),
    StoryItem(
        id = "preset_story_2",
        uid = "community_meskot",
        authorName = "Meskot Official",
        authorPhoto = "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=200&q=80",
        mediaUrl = "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?auto=format&fit=crop&w=800&q=80",
        caption = "Fresh roast traditional Buna ☕",
        filterName = "Warm",
        createdAt = System.currentTimeMillis() - 4 * 3600 * 1000L,
        expiresAt = System.currentTimeMillis() + 20 * 3600 * 1000L
    ),
    StoryItem(
        id = "preset_story_3",
        uid = "community_dawit",
        authorName = "Dawit Gebre",
        authorPhoto = "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=200&q=80",
        mediaUrl = "https://images.unsplash.com/photo-1470071459604-3b5ec3a7fe05?auto=format&fit=crop&w=800&q=80",
        caption = "Sunset over the Ethiopian highlands 🌄",
        filterName = "Vibrant",
        createdAt = System.currentTimeMillis() - 6 * 3600 * 1000L,
        expiresAt = System.currentTimeMillis() + 18 * 3600 * 1000L
    ),
    StoryItem(
        id = "preset_story_4",
        uid = "community_kalkidan",
        authorName = "Kalkidan M.",
        authorPhoto = "https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=200&q=80",
        mediaUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?auto=format&fit=crop&w=800&q=80",
        caption = "Serene lake breezes 🌿",
        filterName = "Normal",
        createdAt = System.currentTimeMillis() - 8 * 3600 * 1000L,
        expiresAt = System.currentTimeMillis() + 16 * 3600 * 1000L
    )
)

/**
 * Facebook-style Stories Tray.
 * Features the signature Facebook "Create story" card (user avatar taking top ~68%,
 * overlapping circular blue + button, white bottom area) followed by vertical story cards
 * with author avatar at top-left and name at bottom.
 */
@Composable
fun FacebookStoriesRail(
    currentUser: User?,
    activeStories: List<StoryItem>,
    onStoryClick: (StoryItem) -> Unit,
    onCreateStoryClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val myActiveStories = remember(activeStories, currentUser?.uid) {
        if (currentUser == null) emptyList()
        else activeStories.filter { it.uid == currentUser.uid }
    }

    val displayStories = remember(activeStories, currentUser?.uid) {
        val filtered = activeStories.filter { it.uid != currentUser?.uid }
        if (filtered.isEmpty()) {
            PresetCommunityStories
        } else if (filtered.size < 4) {
            filtered + PresetCommunityStories.take(4 - filtered.size)
        } else {
            filtered
        }
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 1. Signature Facebook "Create story" Card
            item(key = "fb_create_story_card") {
                FacebookCreateStoryCard(
                    currentUser = currentUser,
                    onClick = onCreateStoryClick
                )
            }

            // 2. User's Own Active Story (if published)
            myActiveStories.firstOrNull()?.let { myStory ->
                item(key = "my_active_story_${myStory.id}") {
                    FacebookStoryCard(
                        story = myStory,
                        isMyStory = true,
                        isViewed = false,
                        onClick = { onStoryClick(myStory) }
                    )
                }
            }

            // 3. Friends & Community Stories
            items(displayStories, key = { "fb_story_${it.id}" }) { story ->
                val isViewed = currentUser?.uid?.let { story.viewers.contains(it) } ?: false
                FacebookStoryCard(
                    story = story,
                    isMyStory = false,
                    isViewed = isViewed,
                    onClick = { onStoryClick(story) }
                )
            }
        }
    }
}

/**
 * Facebook "Create story" card matching the iconic Facebook mobile app design.
 */
@Composable
fun FacebookCreateStoryCard(
    currentUser: User?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardWidth = 106.dp
    val cardHeight = 188.dp
    val topImageHeight = 128.dp

    Card(
        modifier = modifier
            .width(cardWidth)
            .height(cardHeight)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE4E6EB))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top ~68%: Logged-in User Profile Photo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(topImageHeight)
                        .background(Color(0xFFE4E6EB)),
                    contentAlignment = Alignment.Center
                ) {
                    if (!currentUser?.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = currentUser!!.photoUrl,
                            contentDescription = "Your Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color(0xFFEAEFF5), Color(0xFFD6E2EE))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = Color(0xFF65676B),
                                modifier = Modifier.size(54.dp)
                            )
                        }
                    }
                }

                // Bottom: Clean White surface with "Create story"
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color.White)
                        .padding(top = 18.dp, start = 4.dp, end = 4.dp, bottom = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Create story",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF050505),
                        textAlign = TextAlign.Center,
                        lineHeight = 14.sp
                    )
                }
            }

            // Facebook Blue Overlapping "+" Button
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = topImageHeight - 17.dp)
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1877F2)) // Official Facebook Blue
                    .border(3.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Create story",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Facebook Story Card for individual story previews.
 */
@Composable
fun FacebookStoryCard(
    story: StoryItem,
    isMyStory: Boolean,
    isViewed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardWidth = 106.dp
    val cardHeight = 188.dp

    Card(
        modifier = modifier
            .width(cardWidth)
            .height(cardHeight)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1C1E21)),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE4E6EB).copy(alpha = 0.6f))
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Full-bleed Story Media Background
            AsyncImage(
                model = story.mediaUrl,
                contentDescription = "Story from ${story.authorName}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Scrim Gradients: Dark top for avatar readability, dark bottom for author name
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to Color(0x66000000),
                            0.3f to Color.Transparent,
                            0.55f to Color.Transparent,
                            1.0f to Color(0xB3000000)
                        )
                    )
            )

            // Top-left: Author Avatar with Facebook Blue ring
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopStart)
            ) {
                val ringColor = if (isViewed) Color(0xFFB0B3B8) else Color(0xFF1877F2)
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ringColor)
                        .padding(2.5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(1.5.dp)
                    ) {
                        UserAvatar(
                            photoUrl = story.authorPhoto,
                            name = story.authorName,
                            size = 30
                        )
                    }
                }
            }

            // Bottom: Author Name
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isMyStory) "Your story" else story.authorName,
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 14.sp
                )
            }
        }
    }
}

