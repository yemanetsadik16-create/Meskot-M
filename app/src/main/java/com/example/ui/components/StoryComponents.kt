package com.example.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
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
import com.example.data.Comment
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

val StoryQuickReactions = listOf("❤️", "🔥", "😍", "👏", "☕", "✨")

/**
 * Create Story Dialog allowing photo capture via device camera, filter styling,
 * captioning, and Firebase expiration timer selection — styled in Meskot Royal Gold & Crimson.
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

    // Camera Capture Launcher
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
                .background(InkDark.copy(alpha = 0.94f))
                .padding(horizontal = 16.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBg),
                border = BorderStroke(1.2.dp, GoldBorder),
                elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Meskot Royal Header Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(InkDark, Color(0xFF281918), InkDark)
                                )
                            )
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Brush.linearGradient(listOf(GoldLight, Gold, CrossRed)))
                                    .border(1.dp, GoldLight, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            Column {
                                Text(
                                    text = if (photoPathOrUrl == null) "Create Meskot Story" else "Meskot Story Studio",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = GoldLight
                                )
                                Text(
                                    text = "Share cultural & daily moments with your community",
                                    fontSize = 11.sp,
                                    color = Color.White.copy(alpha = 0.78f)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            enabled = !isUploading,
                            modifier = Modifier
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.12f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = GoldLight,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    HorizontalDivider(color = GoldBorder, thickness = 1.dp)

                    // Body
                    if (photoPathOrUrl == null) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(20.dp)
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            // Camera Viewfinder Box (9:16 vertical story proportion)
                            Box(
                                modifier = Modifier
                                    .width(205.dp)
                                    .aspectRatio(9f / 16f)
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(InkDark, Color(0xFF2B181A), Ink)
                                        )
                                    )
                                    .border(2.dp, Brush.linearGradient(listOf(GoldLight, Gold, CrossRed)), RoundedCornerShape(22.dp))
                                    .clickable { triggerCamera() },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.padding(22.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(72.dp)
                                            .clip(CircleShape)
                                            .background(Brush.linearGradient(listOf(GoldLight, Gold, CrossRed)))
                                            .border(2.dp, Color.White.copy(alpha = 0.85f), CircleShape),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PhotoCamera,
                                            contentDescription = "Capture with Camera",
                                            tint = Color.White,
                                            modifier = Modifier.size(36.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.height(14.dp))

                                    Text(
                                        text = "Open Meskot Camera",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = GoldLight,
                                        textAlign = TextAlign.Center
                                    )

                                    Spacer(modifier = Modifier.height(4.dp))

                                    Text(
                                        text = "Tap to capture a live moment for your story",
                                        fontSize = 11.sp,
                                        color = Color.White.copy(alpha = 0.78f),
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Surface(
                                    color = CrossRed.copy(alpha = 0.85f),
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, GoldLight.copy(alpha = 0.6f)),
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .padding(12.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.AutoAwesome,
                                            contentDescription = null,
                                            tint = GoldLight,
                                            modifier = Modifier.size(13.dp)
                                        )
                                        Text("MESKOT HD", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }

                            // Capture Button
                            Button(
                                onClick = { triggerCamera() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(52.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = CrossRed),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    tint = GoldLight,
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
                                border = BorderStroke(1.2.dp, Gold),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Ink)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.PhotoLibrary,
                                    contentDescription = null,
                                    tint = GoldDeep,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Choose Photo from Gallery",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink
                                )
                            }

                            // Quick Inspiration Presets
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "Meskot Cultural & Scenic Presets:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldDeep
                                )
                                LazyRow(
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    items(PresetStorySamples) { (sampleUrl, title) ->
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier
                                                .width(84.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(GoldSurface)
                                                .border(1.dp, GoldBorder, RoundedCornerShape(12.dp))
                                                .clickable { photoPathOrUrl = sampleUrl }
                                                .padding(6.dp)
                                        ) {
                                            AsyncImage(
                                                model = sampleUrl,
                                                contentDescription = title,
                                                modifier = Modifier
                                                    .size(70.dp)
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
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }

                            if (showPermissionRationale) {
                                Surface(
                                    color = GoldSurface,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, GoldBorder),
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
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(330.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .aspectRatio(9f / 16f)
                                        .clip(RoundedCornerShape(18.dp))
                                        .background(InkDark)
                                        .border(1.5.dp, Gold, RoundedCornerShape(18.dp)),
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
                                        contentDescription = null,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        alpha = 0.35f,
                                        colorFilter = colorFilter
                                    )

                                    AsyncImage(
                                        model = photoPathOrUrl,
                                        contentDescription = "Story Media Preview",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Fit,
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

                                    Surface(
                                        onClick = { photoPathOrUrl = null },
                                        color = InkDark.copy(alpha = 0.78f),
                                        shape = CircleShape,
                                        border = BorderStroke(1.dp, GoldLight.copy(alpha = 0.6f)),
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
                                                tint = GoldLight,
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

                                    Surface(
                                        color = CrossRed.copy(alpha = 0.85f),
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(12.dp)
                                    ) {
                                        Text(
                                            text = "Tone: $selectedFilter",
                                            color = GoldLight,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }
                                }
                            }

                            // Filter Picker Row
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Meskot Visual Tone",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
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
                                            color = if (isSelected) CrossRed else GoldSurface,
                                            border = BorderStroke(
                                                1.dp,
                                                if (isSelected) GoldLight else GoldBorder
                                            )
                                        ) {
                                            Text(
                                                text = filter,
                                                fontSize = 12.sp,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                                color = if (isSelected) Color.White else Ink,
                                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                                            )
                                        }
                                    }
                                }
                            }

                            // Caption Input
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(
                                    text = "Story Caption (Optional)",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Ink
                                )
                                OutlinedTextField(
                                    value = captionText,
                                    onValueChange = { if (it.length <= 140) captionText = it },
                                    placeholder = { Text("Share what's happening in this moment…", fontSize = 13.sp) },
                                    modifier = Modifier.fillMaxWidth(),
                                    maxLines = 3,
                                    shape = RoundedCornerShape(12.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedBorderColor = GoldDeep,
                                        unfocusedBorderColor = GoldBorder
                                    ),
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                                    keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() })
                                )
                            }

                            // Expiration Duration Chips
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = GoldSurface),
                                border = BorderStroke(1.dp, GoldBorder)
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
                                            tint = CrossRed,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = "Story Duration Timer",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Ink
                                        )
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        StoryExpirationOptions.forEach { (hours, _) ->
                                            val isSelected = selectedExpirationHours == hours
                                            Surface(
                                                onClick = { selectedExpirationHours = hours },
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (isSelected) CrossRed else CardBg,
                                                border = BorderStroke(
                                                    1.dp,
                                                    if (isSelected) GoldLight else GoldBorder
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
                                                        color = if (isSelected) GoldLight else Ink
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
                                colors = ButtonDefaults.buttonColors(containerColor = CrossRed)
                            ) {
                                if (isUploading) {
                                    CircularProgressIndicator(
                                        color = GoldLight,
                                        modifier = Modifier.size(22.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text("Publishing to Meskot Stories…", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                } else {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = null,
                                        tint = GoldLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Publish Meskot Story (${selectedExpirationHours}h) ✨",
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
 * Story Viewer Dialog: Full-screen luxury Meskot story experience with:
 * - Segmented progress bars across active stories & tap left/right navigation
 * - Double-tap to Like with animated Meskot Crimson/Gold heart burst
 * - Interactive Quick Emoji Reactions bar (❤️ 🔥 😍 👏 ☕ ✨)
 * - Fully functional Story Comments bottom sheet (add, like, delete comments in real time)
 * - Direct Story Reply composer that posts to comments AND messages the story creator
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoryViewerDialog(
    story: StoryItem,
    allStories: List<StoryItem> = listOf(story),
    currentUser: User?,
    users: List<User> = emptyList(),
    comments: List<Comment> = emptyList(),
    onDismiss: () -> Unit,
    onSelectStory: (StoryItem) -> Unit = {},
    onDeleteStory: (String) -> Unit,
    onToggleLike: (String) -> Unit,
    onReactStory: (String, String) -> Unit = { id, _ -> onToggleLike(id) },
    onAddComment: (String, String) -> Unit = { _, _ -> },
    onReplyStory: (StoryItem, String) -> Unit = { s, txt -> onAddComment(s.id, txt) },
    onToggleCommentLike: (String, String) -> Unit = { _, _ -> },
    onDeleteComment: (String, String) -> Unit = { _, _ -> }
) {
    val focusManager = LocalFocusManager.current
    var showViewersSheet by remember { mutableStateOf(false) }
    var showCommentsSheet by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var replyText by remember(story.id) { mutableStateOf("") }
    var sheetCommentText by remember(story.id) { mutableStateOf("") }

    // Animated heart burst state when liking or double-tapping
    var showHeartBurst by remember { mutableStateOf(false) }
    var burstEmoji by remember { mutableStateOf("❤️") }

    val isAuthor = currentUser?.uid == story.uid
    val isLiked = currentUser?.let { story.likes[it.uid] == true } ?: false
    val myReaction = currentUser?.let { story.reactions[it.uid] }
    val likeCount = maxOf(story.likes.values.count { it }, story.reactions.size)
    val totalCommentsCount = maxOf(story.commentCount, comments.size)

    val orderedStories = remember(allStories, story.id) {
        if (allStories.isEmpty()) listOf(story)
        else if (allStories.any { it.id == story.id }) allStories
        else listOf(story) + allStories
    }
    val currentIndex = orderedStories.indexOfFirst { it.id == story.id }.coerceAtLeast(0)

    LaunchedEffect(showHeartBurst) {
        if (showHeartBurst) {
            delay(850L)
            showHeartBurst = false
        }
    }

    // Real-time ticking remaining calculation
    var currentNowMs by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(story.id) {
        while (isActive) {
            delay(1000L)
            currentNowMs = System.currentTimeMillis()
        }
    }

    // Pause story timer when typing a reply or viewing sheets
    val isPaused = showCommentsSheet || showViewersSheet || showDeleteConfirm || replyText.isNotEmpty()

    val progress = remember(story.id) { Animatable(0f) }
    LaunchedEffect(story.id, isPaused) {
        if (!isPaused) {
            val remainingFraction = (1f - progress.value).coerceIn(0.05f, 1f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = (10_000 * remainingFraction).toInt(),
                    easing = LinearEasing
                )
            )
            if (currentIndex < orderedStories.lastIndex) {
                onSelectStory(orderedStories[currentIndex + 1])
            } else {
                onDismiss()
            }
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(InkDark)
        ) {
            val colorFilter = when (story.filterName) {
                "Noir" -> ColorFilter.colorMatrix(ColorMatrix().apply { setToSaturation(0f) })
                "Warm" -> ColorFilter.tint(Color(0xFFFFA500).copy(alpha = 0.2f), androidx.compose.ui.graphics.BlendMode.Darken)
                "Vibrant" -> ColorFilter.tint(Color(0xFF00AAFF).copy(alpha = 0.15f), androidx.compose.ui.graphics.BlendMode.Overlay)
                else -> null
            }

            // Story Media Container with Left/Right Tap Navigation & Double-Tap to Like
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(story.id, currentIndex, isLiked) {
                        detectTapGestures(
                            onDoubleTap = {
                                burstEmoji = "❤️"
                                showHeartBurst = true
                                if (!isLiked) {
                                    onToggleLike(story.id)
                                }
                            },
                            onTap = { offset ->
                                val width = size.width
                                if (offset.x < width * 0.28f) {
                                    if (currentIndex > 0) {
                                        onSelectStory(orderedStories[currentIndex - 1])
                                    }
                                } else if (offset.x > width * 0.72f) {
                                    if (currentIndex < orderedStories.lastIndex) {
                                        onSelectStory(orderedStories[currentIndex + 1])
                                    } else {
                                        onDismiss()
                                    }
                                }
                            }
                        )
                    }
            ) {
                // Ambient blurred backdrop
                AsyncImage(
                    model = story.mediaUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    alpha = 0.30f,
                    colorFilter = colorFilter
                )

                // Framed 9:16 Story Canvas with subtle Meskot Gold border
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(9f / 16f)
                        .align(Alignment.Center)
                        .padding(horizontal = 6.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF101713))
                        .border(
                            width = 1.2.dp,
                            brush = Brush.verticalGradient(
                                listOf(GoldLight.copy(alpha = 0.65f), Gold.copy(alpha = 0.35f), CrossRed.copy(alpha = 0.65f))
                            ),
                            shape = RoundedCornerShape(20.dp)
                        )
                ) {
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
                }

                // Double-tap / Reaction Burst Animation in Center
                AnimatedVisibility(
                    visible = showHeartBurst,
                    enter = scaleIn(animationSpec = spring(dampingRatio = 0.45f, stiffness = 400f)) + fadeIn(),
                    exit = scaleOut() + fadeOut(),
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CrossRed.copy(alpha = 0.88f),
                        border = BorderStroke(2.5.dp, GoldLight),
                        shadowElevation = 16.dp,
                        modifier = Modifier.size(104.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = burstEmoji,
                                fontSize = 48.sp
                            )
                        }
                    }
                }
            }

            // Top Luxury Gradient Scrim
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(155.dp)
                    .align(Alignment.TopCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(InkDark.copy(alpha = 0.90f), Color.Transparent)
                        )
                    )
            )

            // Bottom Luxury Gradient Scrim
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp)
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            listOf(Color.Transparent, InkDark.copy(alpha = 0.88f), InkDark.copy(alpha = 0.98f))
                        )
                    )
            )

            // Top Header: Multi-segment Meskot Gold Progress Bar & Story Author Info
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Segmented Progress Bars across all active stories
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    orderedStories.forEachIndexed { index, item ->
                        val segProgress = when {
                            index < currentIndex -> 1f
                            index == currentIndex -> progress.value
                            else -> 0f
                        }
                        LinearProgressIndicator(
                            progress = { segProgress },
                            modifier = Modifier
                                .weight(1f)
                                .height(3.5.dp)
                                .clip(RoundedCornerShape(2.dp)),
                            color = GoldLight,
                            trackColor = Color.White.copy(alpha = 0.28f)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Author Avatar with Meskot Gold/Crimson Ring + Name + Timer
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(Brush.sweepGradient(listOf(GoldLight, Gold, CrossRed, GoldDeep, GoldLight)))
                                .padding(2.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clip(CircleShape)
                                    .background(InkDark)
                                    .padding(1.5.dp)
                            ) {
                                UserAvatar(
                                    photoUrl = story.authorPhoto,
                                    name = story.authorName,
                                    size = 37
                                )
                            }
                        }

                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = story.authorName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color.White
                                )

                                Surface(
                                    color = CrossRed.copy(alpha = 0.85f),
                                    shape = RoundedCornerShape(6.dp),
                                    border = BorderStroke(0.8.dp, GoldLight.copy(alpha = 0.7f))
                                ) {
                                    Text(
                                        text = if (story.filterName != "Normal") story.filterName else "MESKOT",
                                        fontSize = 9.sp,
                                        color = GoldLight,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }

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
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }

                    // Top Right Actions (Comments count, Delete if author, Close)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        if (isAuthor) {
                            IconButton(
                                onClick = { showDeleteConfirm = true },
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(CrossRed.copy(alpha = 0.75f))
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Story",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.16f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Bottom Section: Caption, Quick Reactions, Likes, Comments Sheet Trigger & Reply Input
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(horizontal = 14.dp, vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Story Caption Banner in Meskot Glass Card
                if (story.caption.isNotBlank()) {
                    Surface(
                        color = InkDark.copy(alpha = 0.78f),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, Gold.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(24.dp)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(Brush.verticalGradient(listOf(GoldLight, CrossRed)))
                            )
                            Text(
                                text = story.caption,
                                fontSize = 14.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Medium,
                                lineHeight = 19.sp
                            )
                        }
                    }
                }

                // Engagement Summary & Quick Emoji Reactions Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    // Left: Likes & Comments Counter Pills
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Likes / Reactions Pill
                        Surface(
                            onClick = {
                                if (isAuthor) {
                                    showViewersSheet = true
                                } else {
                                    burstEmoji = "❤️"
                                    if (!isLiked) showHeartBurst = true
                                    onToggleLike(story.id)
                                }
                            },
                            color = if (isLiked) CrossRed.copy(alpha = 0.90f) else Color.White.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, if (isLiked) GoldLight else Gold.copy(alpha = 0.45f))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                    contentDescription = "Story Likes",
                                    tint = if (isLiked) GoldLight else Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "$likeCount ${if (likeCount == 1) "Like" else "Likes"}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        // Comments Drawer Trigger Pill
                        Surface(
                            onClick = { showCommentsSheet = true },
                            color = Color.White.copy(alpha = 0.14f),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.dp, Gold.copy(alpha = 0.45f)),
                            modifier = Modifier.testTag("story_comments_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(5.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.ChatBubbleOutline,
                                    contentDescription = "Story Comments",
                                    tint = GoldLight,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = "$totalCommentsCount ${if (totalCommentsCount == 1) "Comment" else "Comments"}",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }

                        if (isAuthor) {
                            Surface(
                                onClick = { showViewersSheet = true },
                                color = Gold.copy(alpha = 0.22f),
                                shape = RoundedCornerShape(20.dp),
                                border = BorderStroke(1.dp, GoldLight.copy(alpha = 0.5f))
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Visibility,
                                        contentDescription = null,
                                        tint = GoldLight,
                                        modifier = Modifier.size(15.dp)
                                    )
                                    Text(
                                        text = "${story.viewers.size}",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldLight
                                    )
                                }
                            }
                        }
                    }

                    // Right: Quick Emoji Reactions Strip
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        StoryQuickReactions.take(4).forEach { emoji ->
                            val isSelectedEmoji = myReaction == emoji
                            Surface(
                                onClick = {
                                    burstEmoji = emoji
                                    showHeartBurst = true
                                    onReactStory(story.id, emoji)
                                },
                                shape = CircleShape,
                                color = if (isSelectedEmoji) CrossRed else Color.White.copy(alpha = 0.14f),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelectedEmoji) GoldLight else Color.White.copy(alpha = 0.22f)
                                ),
                                modifier = Modifier.size(34.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Text(text = emoji, fontSize = 15.sp)
                                }
                            }
                        }
                    }
                }

                // Bottom Interactive Story Comment & Direct Reply Bar + Like Heart Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = replyText,
                        onValueChange = { replyText = it },
                        placeholder = {
                            Text(
                                text = if (isAuthor) "Add a comment to your story…" else "Comment or reply to ${story.authorName.split(" ").firstOrNull()}…",
                                fontSize = 13.sp,
                                color = Color.White.copy(alpha = 0.72f),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(26.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White,
                            focusedContainerColor = Color.White.copy(alpha = 0.14f),
                            unfocusedContainerColor = Color.White.copy(alpha = 0.12f),
                            focusedBorderColor = GoldLight,
                            unfocusedBorderColor = Gold.copy(alpha = 0.55f),
                            cursorColor = GoldLight
                        ),
                        trailingIcon = {
                            if (replyText.isNotBlank()) {
                                IconButton(
                                    onClick = {
                                        val clean = replyText.trim()
                                        if (clean.isNotEmpty()) {
                                            onReplyStory(story, clean)
                                            replyText = ""
                                            focusManager.clearFocus()
                                        }
                                    },
                                    modifier = Modifier.testTag("story_send_reply_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Send,
                                        contentDescription = "Send Story Comment",
                                        tint = GoldLight
                                    )
                                }
                            } else {
                                IconButton(
                                    onClick = { showCommentsSheet = true }
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.ModeComment,
                                        contentDescription = "Open Story Comments",
                                        tint = GoldLight.copy(alpha = 0.85f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        },
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(
                            onSend = {
                                val clean = replyText.trim()
                                if (clean.isNotEmpty()) {
                                    onReplyStory(story, clean)
                                    replyText = ""
                                    focusManager.clearFocus()
                                }
                            }
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("story_reply_input")
                    )

                    // Meskot Crimson & Gold Like Button
                    val heartScale by animateFloatAsState(
                        targetValue = if (isLiked) 1.12f else 1.0f,
                        animationSpec = spring(dampingRatio = 0.45f, stiffness = 500f),
                        label = "story_heart_scale"
                    )
                    IconButton(
                        onClick = {
                            if (!isLiked) {
                                burstEmoji = "❤️"
                                showHeartBurst = true
                            }
                            onToggleLike(story.id)
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .scale(heartScale)
                            .clip(CircleShape)
                            .background(
                                if (isLiked) Brush.linearGradient(listOf(CrossRed, GoldDeep))
                                else Brush.linearGradient(listOf(Color.White.copy(alpha = 0.18f), Color.White.copy(alpha = 0.12f)))
                            )
                            .border(
                                width = 1.5.dp,
                                color = if (isLiked) GoldLight else Gold.copy(alpha = 0.55f),
                                shape = CircleShape
                            )
                            .testTag("story_like_button")
                    ) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                            contentDescription = "Like Story",
                            tint = if (isLiked) GoldLight else Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            // ==========================================================
            // STORY COMMENTS & REACTIONS BOTTOM SHEET (MESKOT BRANDED)
            // ==========================================================
            if (showCommentsSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showCommentsSheet = false },
                    containerColor = CardBg,
                    dragHandle = { BottomSheetDefaults.DragHandle(color = Gold) }
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.75f)
                            .navigationBarsPadding()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        // Sheet Header
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(Brush.linearGradient(listOf(GoldLight, Gold, CrossRed))),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ChatBubble,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(17.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Story Comments & Reactions",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Ink
                                    )
                                    Text(
                                        text = "$likeCount likes • ${comments.size} comments on ${story.authorName}'s story",
                                        fontSize = 11.sp,
                                        color = MutedText
                                    )
                                }
                            }

                            IconButton(onClick = { showCommentsSheet = false }) {
                                Icon(Icons.Default.Close, contentDescription = "Close", tint = Ink)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Quick Emoji Reaction Bar inside Sheet
                        Surface(
                            color = GoldSurface,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, GoldBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Quick React:",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldDeep
                                )
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    StoryQuickReactions.forEach { emoji ->
                                        val isSelected = myReaction == emoji
                                        Surface(
                                            onClick = { onReactStory(story.id, emoji) },
                                            shape = CircleShape,
                                            color = if (isSelected) CrossRed else Color.White,
                                            border = BorderStroke(1.dp, if (isSelected) GoldLight else GoldBorder),
                                            modifier = Modifier.size(34.dp)
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Text(text = emoji, fontSize = 16.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        HorizontalDivider(color = LineBorder)

                        // Comments List
                        if (comments.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Outlined.Forum,
                                        contentDescription = null,
                                        tint = Gold,
                                        modifier = Modifier.size(40.dp)
                                    )
                                    Text(
                                        text = "No comments on this story yet",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Ink
                                    )
                                    Text(
                                        text = "Be the first to leave a comment or reaction below!",
                                        fontSize = 12.sp,
                                        color = MutedText
                                    )
                                }
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth(),
                                contentPadding = PaddingValues(vertical = 10.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                items(comments, key = { it.id }) { comment ->
                                    val isCommentLiked = currentUser?.uid?.let { comment.likes[it] == true } ?: false
                                    val commentLikesCount = comment.likes.values.count { it }
                                    val canDelete = currentUser?.uid == comment.uid || isAuthor

                                    Surface(
                                        color = Paper,
                                        shape = RoundedCornerShape(14.dp),
                                        border = BorderStroke(1.dp, LineBorder),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(12.dp),
                                            verticalAlignment = Alignment.Top,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            UserAvatar(
                                                photoUrl = comment.authorPhoto,
                                                name = comment.authorName,
                                                size = 36
                                            )

                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                                ) {
                                                    Text(
                                                        text = comment.authorName,
                                                        fontSize = 13.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = Ink
                                                    )
                                                    if (comment.isAuthorVerified) {
                                                        Icon(
                                                            imageVector = Icons.Default.Verified,
                                                            contentDescription = "Verified",
                                                            tint = GoldDeep,
                                                            modifier = Modifier.size(14.dp)
                                                        )
                                                    }
                                                    Text(
                                                        text = MeskotStrings.timeAgo(comment.createdAt, AppLanguage.EN),
                                                        fontSize = 11.sp,
                                                        color = MutedText
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(3.dp))

                                                Text(
                                                    text = comment.text,
                                                    fontSize = 13.sp,
                                                    color = Ink,
                                                    lineHeight = 18.sp
                                                )

                                                Spacer(modifier = Modifier.height(6.dp))

                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                                ) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                        modifier = Modifier.clickable {
                                                            onToggleCommentLike(story.id, comment.id)
                                                        }
                                                    ) {
                                                        Icon(
                                                            imageVector = if (isCommentLiked) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                                            contentDescription = "Like Comment",
                                                            tint = if (isCommentLiked) CrossRed else MutedText,
                                                            modifier = Modifier.size(15.dp)
                                                        )
                                                        Text(
                                                            text = if (commentLikesCount > 0) "Like ($commentLikesCount)" else "Like",
                                                            fontSize = 11.sp,
                                                            fontWeight = if (isCommentLiked) FontWeight.Bold else FontWeight.Medium,
                                                            color = if (isCommentLiked) CrossRed else MutedText
                                                        )
                                                    }

                                                    if (canDelete) {
                                                        Text(
                                                            text = "Delete",
                                                            fontSize = 11.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = CrossRed,
                                                            modifier = Modifier.clickable {
                                                                onDeleteComment(story.id, comment.id)
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        HorizontalDivider(color = LineBorder)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Add Comment Input Row inside Sheet
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = sheetCommentText,
                                onValueChange = { sheetCommentText = it },
                                placeholder = { Text("Write a comment on this story…", fontSize = 13.sp) },
                                singleLine = true,
                                shape = RoundedCornerShape(24.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = GoldDeep,
                                    unfocusedBorderColor = GoldBorder,
                                    focusedContainerColor = GoldSurface.copy(alpha = 0.5f),
                                    unfocusedContainerColor = Paper
                                ),
                                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                                keyboardActions = KeyboardActions(
                                    onSend = {
                                        val clean = sheetCommentText.trim()
                                        if (clean.isNotEmpty()) {
                                            onAddComment(story.id, clean)
                                            sheetCommentText = ""
                                            focusManager.clearFocus()
                                        }
                                    }
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("story_sheet_comment_input")
                            )

                            Button(
                                onClick = {
                                    val clean = sheetCommentText.trim()
                                    if (clean.isNotEmpty()) {
                                        onAddComment(story.id, clean)
                                        sheetCommentText = ""
                                        focusManager.clearFocus()
                                    }
                                },
                                enabled = sheetCommentText.isNotBlank(),
                                shape = CircleShape,
                                contentPadding = PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = CrossRed,
                                    disabledContainerColor = LineBorder
                                ),
                                modifier = Modifier
                                    .size(46.dp)
                                    .testTag("story_sheet_post_comment_button")
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Post Story Comment",
                                    tint = GoldLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Viewers & Likers Dialog
            if (showViewersSheet) {
                AlertDialog(
                    onDismissRequest = { showViewersSheet = false },
                    containerColor = CardBg,
                    title = {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = CrossRed)
                            Text(
                                "Story Activity (${story.viewers.size} views • $likeCount likes)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                        }
                    },
                    text = {
                        val allParticipantIds = (story.viewers + story.likes.keys + story.reactions.keys).distinct()
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(max = 260.dp)
                                .verticalScroll(rememberScrollState()),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (allParticipantIds.isEmpty()) {
                                Text("No viewers or reactions yet.", fontSize = 13.sp, color = MutedText)
                            } else {
                                allParticipantIds.forEach { participantUid ->
                                    val matchedUser = users.find { it.uid == participantUid }
                                    val displayName = when {
                                        participantUid == currentUser?.uid -> "${currentUser.displayName} (You)"
                                        matchedUser != null -> matchedUser.displayName
                                        participantUid.startsWith("community_") -> participantUid.removePrefix("community_").replaceFirstChar { it.uppercase() }
                                        else -> "Meskot Member"
                                    }
                                    val emoji = story.reactions[participantUid] ?: if (story.likes[participantUid] == true) "❤️" else null

                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(GoldSurface)
                                            .padding(horizontal = 10.dp, vertical = 8.dp)
                                    ) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            UserAvatar(
                                                photoUrl = matchedUser?.photoUrl ?: "",
                                                name = displayName,
                                                size = 32
                                            )
                                            Text(
                                                text = displayName,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Ink
                                            )
                                        }
                                        if (emoji != null) {
                                            Text(text = emoji, fontSize = 16.sp)
                                        }
                                    }
                                }
                            }
                        }
                    },
                    confirmButton = {
                        TextButton(onClick = { showViewersSheet = false }) {
                            Text("Close", color = CrossRed, fontWeight = FontWeight.Bold)
                        }
                    }
                )
            }

            // Delete Confirmation
            if (showDeleteConfirm) {
                AlertDialog(
                    onDismissRequest = { showDeleteConfirm = false },
                    containerColor = CardBg,
                    title = { Text("Delete Story?", fontWeight = FontWeight.Bold, color = Ink) },
                    text = { Text("This will permanently remove your story from Firebase Firestore and delete it from all friends' feeds.", fontSize = 13.sp, color = MutedText) },
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
 * Story Avatar Ring Item: Displays an avatar surrounded by a Meskot Gold & Crimson gradient ring
 * in the horizontal story feed.
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
            GoldLight,
            Gold,
            CrossRed,
            GoldDeep,
            GoldLight
        )
    )

    val viewedGradient = Brush.linearGradient(
        listOf(GoldBorder, LineBorder)
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
                            .background(CardBg)
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

            if (isCurrentUser) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(Brush.linearGradient(listOf(CrossRed, GoldDeep)))
                        .border(1.5.dp, GoldLight, CircleShape)
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
            } else if (!hasStory && MeskotStrings.isOnline(user.lastSeen)) {
                Box(
                    modifier = Modifier
                        .size(13.dp)
                        .clip(CircleShape)
                        .background(ActiveGreen)
                        .border(1.5.dp, Color.White, CircleShape)
                        .align(Alignment.BottomEnd)
                )
            } else if (expirationText != null) {
                Surface(
                    color = CrossRed,
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, GoldLight),
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .offset(y = 2.dp)
                ) {
                    Text(
                        text = expirationText,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldLight,
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
 * Inspiring Ethiopian community story samples for the Meskot story tray.
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
        expiresAt = System.currentTimeMillis() + 22 * 3600 * 1000L,
        likes = mapOf("community_meskot" to true, "community_dawit" to true),
        reactions = mapOf("community_meskot" to "❤️", "community_dawit" to "🔥"),
        commentCount = 2
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
        expiresAt = System.currentTimeMillis() + 20 * 3600 * 1000L,
        likes = mapOf("community_selam" to true),
        reactions = mapOf("community_selam" to "☕"),
        commentCount = 2
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
        expiresAt = System.currentTimeMillis() + 18 * 3600 * 1000L,
        likes = mapOf("community_selam" to true),
        reactions = mapOf("community_selam" to "😍"),
        commentCount = 1
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
        expiresAt = System.currentTimeMillis() + 16 * 3600 * 1000L,
        likes = mapOf("community_dawit" to true),
        reactions = mapOf("community_dawit" to "✨"),
        commentCount = 1
    )
)

/**
 * Meskot Royal Stories Rail:
 * Features a luxury Meskot section header with gold/crimson accents followed by the
 * "Create Story" card and active community story cards with live like & comment badges.
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
            val existingIds = filtered.map { it.id }.toSet()
            filtered + PresetCommunityStories.filterNot { it.id in existingIds }.take(4 - filtered.size)
        } else {
            filtered
        }
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = BorderStroke(1.2.dp, GoldBorder.copy(alpha = 0.85f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        listOf(GoldSurface.copy(alpha = 0.7f), CardBg)
                    )
                )
                .padding(vertical = 12.dp)
        ) {
            // Meskot Stories Brand Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(Brush.linearGradient(listOf(GoldLight, Gold, CrossRed))),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Text(
                        text = "Meskot Stories",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Ink
                    )
                    Surface(
                        color = CrossRed.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(0.8.dp, CrossRed.copy(alpha = 0.35f))
                    ) {
                        Text(
                            text = "${myActiveStories.size + displayStories.size} LIVE",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = CrossRed,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                Text(
                    text = "+ Add Moment",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = GoldDeep,
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .clickable { onCreateStoryClick() }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                contentPadding = PaddingValues(horizontal = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // 1. Meskot Branded "Create story" Card
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
                            currentUser = currentUser,
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
                        currentUser = currentUser,
                        isMyStory = false,
                        isViewed = isViewed,
                        onClick = { onStoryClick(story) }
                    )
                }
            }
        }
    }
}

/**
 * Meskot "Create story" card styled with Meskot Gold, Crimson, and Warm Linen.
 */
@Composable
fun FacebookCreateStoryCard(
    currentUser: User?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardWidth = 112.dp

    Card(
        modifier = modifier
            .width(cardWidth)
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = GoldSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(
            width = 1.4.dp,
            brush = Brush.verticalGradient(listOf(GoldLight, Gold, CrossRed.copy(alpha = 0.7f)))
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top ~66%: Logged-in User Profile Photo
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(126.dp)
                        .background(InkDark),
                    contentAlignment = Alignment.Center
                ) {
                    if (!currentUser?.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = currentUser!!.photoUrl,
                            contentDescription = "Your Profile",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(Color.Transparent, InkDark.copy(alpha = 0.45f))
                                    )
                                )
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(
                                    Brush.verticalGradient(
                                        listOf(InkDark, Color(0xFF2B181A), GoldDeep.copy(alpha = 0.8f))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = GoldLight,
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                }

                // Bottom: Warm Meskot Gold & Linen footer
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(
                            Brush.verticalGradient(
                                listOf(GoldSurface, Paper)
                            )
                        )
                        .padding(top = 18.dp, start = 6.dp, end = 6.dp, bottom = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Create Story",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Ink,
                            textAlign = TextAlign.Center,
                            lineHeight = 13.sp
                        )
                        Text(
                            text = "Share moment",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldDeep
                        )
                    }
                }
            }

            // Meskot Royal Gold & Crimson Overlapping "+" Medallion
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = 108.dp)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(listOf(GoldLight, Gold, CrossRed)))
                    .border(2.5.dp, GoldSurface, CircleShape),
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
 * Meskot Story Card for individual story previews with Meskot Gold/Crimson ring
 * and live like/comment badge indicators.
 */
@Composable
fun FacebookStoryCard(
    story: StoryItem,
    currentUser: User? = null,
    isMyStory: Boolean,
    isViewed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardWidth = 112.dp
    val likeCount = maxOf(story.likes.values.count { it }, story.reactions.size)
    val isLikedByMe = currentUser?.uid?.let { story.likes[it] == true || story.reactions.containsKey(it) } ?: false

    Card(
        modifier = modifier
            .width(cardWidth)
            .aspectRatio(9f / 16f)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = InkDark),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(
            width = 1.4.dp,
            brush = if (isViewed) {
                Brush.verticalGradient(listOf(GoldBorder, LineBorder))
            } else {
                Brush.verticalGradient(listOf(GoldLight, Gold, CrossRed))
            }
        )
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Full-bleed Story Media Background
            AsyncImage(
                model = story.mediaUrl,
                contentDescription = "Story from ${story.authorName}",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Scrim Gradients: Dark top for avatar readability, rich Meskot dark bottom for author & stats
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            0.0f to InkDark.copy(alpha = 0.55f),
                            0.3f to Color.Transparent,
                            0.55f to Color.Transparent,
                            1.0f to InkDark.copy(alpha = 0.88f)
                        )
                    )
            )

            // Top-left: Author Avatar with Meskot Gold & Crimson ring
            Box(
                modifier = Modifier
                    .padding(8.dp)
                    .align(Alignment.TopStart)
            ) {
                val ringBrush = if (isViewed) {
                    Brush.linearGradient(listOf(GoldBorder, LineBorder))
                } else {
                    Brush.sweepGradient(listOf(GoldLight, Gold, CrossRed, GoldDeep, GoldLight))
                }
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(ringBrush)
                        .padding(2.5.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(CircleShape)
                            .background(InkDark)
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

            // Top-right: Live Likes & Comments Pill Badge
            if (likeCount > 0 || story.commentCount > 0) {
                Surface(
                    color = if (isLikedByMe) CrossRed.copy(alpha = 0.88f) else InkDark.copy(alpha = 0.72f),
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(0.8.dp, GoldLight.copy(alpha = 0.65f)),
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = GoldLight,
                            modifier = Modifier.size(10.dp)
                        )
                        Text(
                            text = "$likeCount",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }

            // Bottom: Author Name & Caption Preview
            Column(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = if (isMyStory) "Your Story" else story.authorName,
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.ExtraBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    lineHeight = 13.sp
                )
                if (story.caption.isNotBlank()) {
                    Text(
                        text = story.caption,
                        color = GoldLight.copy(alpha = 0.92f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 11.sp
                    )
                }
            }
        }
    }
}
