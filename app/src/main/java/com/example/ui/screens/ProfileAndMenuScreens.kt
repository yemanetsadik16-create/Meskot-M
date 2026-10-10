package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.text.style.TextOverflow
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import com.example.ui.components.saveStoryBitmapToCache
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.graphicsLayer
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Verified
import com.example.ui.components.MetaVerifiedBottomSheetModal
import com.example.ui.components.ProfileName
import com.example.ui.components.VerifiedBadge
import com.example.data.ExchangeRateManager
import com.example.data.PaymentCurrency
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.School
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VideoCameraBack
import androidx.compose.material.icons.filled.Work
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.ui.components.MeskotHeroEmblem
import com.example.ui.components.MeskotLogoBadge
import com.example.ui.components.MeskotLogoHeader
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Diversity3
import androidx.compose.material.icons.filled.DynamicFeed
import androidx.compose.material.icons.filled.Forum
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PeopleAlt
import androidx.compose.material.icons.filled.Podcasts
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.WorkspacePremium
import com.example.ui.theme.MeskotLogoBrush
import com.example.ui.theme.MeskotLogoHorizontalBrush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.MeskotStrings
import com.example.data.Post
import com.example.data.User
import com.example.ui.MeskotViewModel
import com.example.ui.ScreenTab
import com.example.ui.components.PostCard
import com.example.ui.components.UserAvatar
import com.example.ui.theme.ActiveGreen
import com.example.ui.theme.CardBg
import com.example.ui.theme.CrossRed
import com.example.ui.theme.Gold
import com.example.ui.theme.GoldBorder
import com.example.ui.theme.GoldDeep
import com.example.ui.theme.GoldLight
import com.example.ui.theme.GoldSurface
import com.example.ui.theme.Ink
import com.example.ui.theme.LineBorder
import com.example.ui.theme.MutedText
import com.example.ui.theme.Paper
import com.example.ui.theme.Paper2
import androidx.compose.ui.text.TextStyle
import com.example.ui.theme.meskotTextFieldColors

@Composable
fun ProfileFollowButton(
    isFollowing: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val coroutineScope = rememberCoroutineScope()
    val scaleAnim = remember { Animatable(1f) }
    var showBurst by remember { mutableStateOf(false) }

    val bgColor by animateColorAsState(
        targetValue = if (isFollowing) Color(0xFFE4E6EB) else Color(0xFF1877F2),
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "follow_bg_color"
    )
    val contentColor by animateColorAsState(
        targetValue = if (isFollowing) Color(0xFF050505) else Color.White,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "follow_content_color"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        if (showBurst) {
            FollowBurstEffect(
                onAnimationEnd = { showBurst = false }
            )
        }

        Button(
            onClick = {
                val willFollow = !isFollowing
                coroutineScope.launch {
                    // Tactile bouncy spring animation
                    scaleAnim.animateTo(
                        targetValue = 0.88f,
                        animationSpec = tween(durationMillis = 80, easing = LinearOutSlowInEasing)
                    )
                    scaleAnim.animateTo(
                        targetValue = 1.08f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                    scaleAnim.animateTo(
                        targetValue = 1f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioNoBouncy,
                            stiffness = Spring.StiffnessHigh
                        )
                    )
                }
                if (willFollow) {
                    showBurst = true
                }
                onToggle()
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = bgColor,
                contentColor = contentColor
            ),
            shape = RoundedCornerShape(8.dp),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = if (isFollowing) 0.dp else 1.5.dp,
                pressedElevation = 0.dp
            ),
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    scaleX = scaleAnim.value
                    scaleY = scaleAnim.value
                }
                .testTag("profile_follow_button")
        ) {
            AnimatedContent(
                targetState = isFollowing,
                transitionSpec = {
                    (slideInVertically { height -> height } + fadeIn(tween(180)))
                        .togetherWith(slideOutVertically { height -> -height } + fadeOut(tween(150)))
                },
                label = "follow_button_content"
            ) { following ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = if (following) Icons.Default.Check else Icons.Default.PersonAdd,
                        contentDescription = if (following) "Following" else "Follow",
                        tint = contentColor,
                        modifier = Modifier.size(17.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (following) "Following" else "Follow",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = contentColor
                    )
                }
            }
        }
    }
}

@Composable
private fun FollowBurstEffect(
    onAnimationEnd: () -> Unit
) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
        onAnimationEnd()
    }

    val particleCount = 8
    val colors = listOf(Color(0xFF1877F2), Color(0xFFFFD700), Color(0xFF00C853), Color(0xFFE41E3F))

    Canvas(modifier = Modifier.fillMaxSize()) {
        val centerX = size.width / 2f
        val centerY = size.height / 2f
        val currentP = progress.value
        val alpha = (1f - currentP).coerceIn(0f, 1f)

        for (i in 0 until particleCount) {
            val angle = (i.toFloat() / particleCount) * (2f * Math.PI.toFloat())
            val maxDistance = size.height * 0.9f
            val distance = currentP * maxDistance
            val px = centerX + (Math.cos(angle.toDouble()).toFloat() * distance)
            val py = centerY + (Math.sin(angle.toDouble()).toFloat() * distance)
            val particleRadius = (4.dp.toPx() * (1f - (currentP * 0.6f))).coerceAtLeast(1.dp.toPx())
            val color = colors[i % colors.size].copy(alpha = alpha)

            drawCircle(
                color = color,
                radius = particleRadius,
                center = androidx.compose.ui.geometry.Offset(px, py)
            )
        }
    }
}

@Composable
fun ProfileScreen(
    viewModel: MeskotViewModel,
    user: User,
    currentUser: User?,
    userPosts: List<Post>,
    friendUids: Set<String>,
    currentLanguage: AppLanguage,
    allUsers: List<User> = emptyList()
) {
    val isMe = currentUser?.uid == user.uid
    val isFriend = friendUids.contains(user.uid)
    val followingUids by viewModel.followingUids.collectAsState()
    val isFollowing = followingUids.contains(user.uid)
    val nowMs by viewModel.tickerTimeMs.collectAsState()
    val isTargetUserOnline = remember(user.lastSeen, nowMs, isMe) {
        isMe || MeskotStrings.isOnline(user.lastSeen, nowMs)
    }

    val fbBlue = Color(0xFF1877F2)
    val fbLightGray = Color(0xFFE4E6EB)
    val fbTextGray = Color(0xFF65676B)
    val fbDark = Color(0xFF050505)
    val fbBadgeRed = Color(0xFFE41E3F)
    val fbGreen = Color(0xFF31A24C)

    var selectedTab by remember { mutableStateOf(0) } // 0: All, 1: Reels, 2: Photos
    var isSearchOpen by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }
    var isMoreOptionsOpen by remember { mutableStateOf(false) }
    var isSeeMoreDetailsOpen by remember { mutableStateOf(false) }
    var isSeeMoreWorkOpen by remember { mutableStateOf(false) }
    var isAccountMenuOpen by remember { mutableStateOf(false) }
    var showMetaVerifiedModal by remember { mutableStateOf(false) }
    var viewAsOtherMode by remember { mutableStateOf(false) }
    var showPhotoSourceSheet by remember { mutableStateOf<String?>(null) } // "AVATAR" or "COVER"
    val effectiveIsMe = isMe && !viewAsOtherMode
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current

    fun saveUploadedPhoto(newUrl: String, isCover: Boolean) {
        if (currentUser == null) return
        viewModel.saveProfile(
            name = currentUser.displayName,
            bio = currentUser.bio,
            photoUrl = if (!isCover) newUrl else currentUser.photoUrl,
            gender = currentUser.gender,
            birthDate = currentUser.birthDate,
            coverPhotoUrl = if (isCover) newUrl else currentUser.coverPhotoUrl,
            profession = currentUser.profession,
            location = currentUser.location,
            hometown = currentUser.hometown,
            workplace = currentUser.workplace,
            workRole = currentUser.workRole,
            education = currentUser.education,
            educationClass = currentUser.educationClass
        )
    }

    val avatarGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            saveUploadedPhoto(uri.toString(), isCover = false)
            Toast.makeText(context, "Profile picture updated!", Toast.LENGTH_SHORT).show()
        }
    }

    val avatarCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val savedPath = saveStoryBitmapToCache(context, bitmap)
            if (savedPath != null) {
                saveUploadedPhoto(savedPath, isCover = false)
                Toast.makeText(context, "Profile picture captured!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    val coverGalleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: Exception) {}
            saveUploadedPhoto(uri.toString(), isCover = true)
            Toast.makeText(context, "Cover photo updated!", Toast.LENGTH_SHORT).show()
        }
    }

    val coverCameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            val savedPath = saveStoryBitmapToCache(context, bitmap)
            if (savedPath != null) {
                saveUploadedPhoto(savedPath, isCover = true)
                Toast.makeText(context, "Cover photo captured!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Dynamic friends list populated from real Firestore users
    val displayFriends = remember(allUsers, user.uid) {
        allUsers.filter { it.uid != user.uid }.take(6)
    }

    fun formatStats(count: Int): String {
        return when {
            count >= 1_000_000 -> String.format("%.1fM", count / 1_000_000.0)
            count >= 1_000 -> String.format("%.1fK", count / 1_000.0)
            else -> count.toString()
        }
    }

    var viewingPhotoUrl by remember { mutableStateOf<String?>(null) }

    // Filter posts if search query is active
    val allPosts = remember(userPosts, searchQuery) {
        if (searchQuery.isBlank()) userPosts else userPosts.filter {
            it.text.contains(searchQuery, ignoreCase = true)
        }
    }

    val userReels = remember(allPosts) {
        allPosts.filter { it.postType.equals("REEL", ignoreCase = true) || it.videoUrl.isNotBlank() }
    }

    val userPhotos = remember(allPosts) {
        allPosts.filter {
            (it.mediaUrls.isNotEmpty() || it.postType.equals("PHOTO", ignoreCase = true)) &&
            !it.postType.equals("REEL", ignoreCase = true) &&
            it.videoUrl.isBlank()
        }
    }

    val allPhotosList = remember(userPhotos) {
        userPhotos.flatMap { post ->
            post.mediaUrls.map { url -> url to post }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("profile_screen")
    ) {
        // TOP BAR: Clean Facebook-Style Profile Navigation Bar (Screenshot 1 & 2)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White,
            shadowElevation = 1.dp
        ) {
            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = fbDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    if (effectiveIsMe) {
                        // Screenshot 2: Own profile top bar with name, red badge, dropdown, edit, search
                        Row(
                            modifier = Modifier
                                .clickable { isAccountMenuOpen = !isAccountMenuOpen }
                                .padding(horizontal = 4.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = user.displayName,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = fbDark,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(fbBadgeRed),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "9+",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.ArrowDropDown,
                                contentDescription = "Dropdown",
                                tint = fbDark,
                                modifier = Modifier.size(20.dp)
                            )

                            DropdownMenu(
                                expanded = isAccountMenuOpen,
                                onDismissRequest = { isAccountMenuOpen = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(user.displayName, fontWeight = FontWeight.Bold) },
                                    onClick = { isAccountMenuOpen = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("View as Visitor (1st Image Style)") },
                                    onClick = {
                                        isAccountMenuOpen = false
                                        viewAsOtherMode = true
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Edit Profile") },
                                    onClick = {
                                        isAccountMenuOpen = false
                                        viewModel.openEditProfile()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Log Out", color = CrossRed) },
                                    onClick = {
                                        isAccountMenuOpen = false
                                        viewModel.logout()
                                    }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        IconButton(
                            onClick = { viewModel.openEditProfile() },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Edit,
                                contentDescription = "Edit Profile",
                                tint = fbDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = { isSearchOpen = !isSearchOpen },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Profile",
                                tint = fbDark,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    } else {
                        // Screenshot 1: Visitor / Other's profile top bar (Back | Centered Name | Search | More)
                        Text(
                            text = user.displayName,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = fbDark,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )

                        IconButton(
                            onClick = { isSearchOpen = !isSearchOpen },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search Profile",
                                tint = fbDark,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        IconButton(
                            onClick = { isMoreOptionsOpen = true },
                            modifier = Modifier.size(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More Options",
                                tint = fbDark,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }

                // Visitor Preview Banner when owner is previewing as visitor
                if (isMe && viewAsOtherMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color(0xFFE7F3FF))
                            .padding(horizontal = 14.dp, vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "👁️ Viewing how others see your profile",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = fbBlue
                        )
                        Text(
                            text = "Exit View As",
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = fbBlue,
                            modifier = Modifier.clickable { viewAsOtherMode = false }
                        )
                    }
                }

                // Optional Expandable Search Input Field
                if (isSearchOpen) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = { Text("Search in ${user.displayName}'s profile…", fontSize = 13.sp) },
                            singleLine = true,
                            colors = meskotTextFieldColors(),
                            modifier = Modifier
                                .weight(1f)
                                .height(46.dp),
                            shape = RoundedCornerShape(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            searchQuery = ""
                            isSearchOpen = false
                        }) {
                            Text("Clear", color = fbBlue, fontSize = 13.sp)
                        }
                    }
                }
                HorizontalDivider(color = fbLightGray, thickness = 0.5.dp)
            }
        }

        // MAIN SCROLLABLE CONTENT
        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            // COVER PHOTO AND LEFT-OVERLAPPING PROFILE AVATAR (Matching Screenshot 1 & 2)
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                ) {
                    // Cover Banner Image (Facebook 16:9 cover aspect ratio)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 52.dp)
                            .aspectRatio(16f / 9f)
                            .background(com.example.ui.theme.MeskotLogoHorizontalBrush)
                            .clickable {
                                if (user.coverPhotoUrl.isNotBlank()) {
                                    viewingPhotoUrl = user.coverPhotoUrl
                                } else if (effectiveIsMe) {
                                    showPhotoSourceSheet = "COVER"
                                }
                            }
                    ) {
                        if (user.coverPhotoUrl.isNotBlank()) {
                            AsyncImage(
                                model = user.coverPhotoUrl,
                                contentDescription = "Cover Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                MeskotLogoBadge(size = 56.dp, showBorder = true, elevation = 4.dp)
                            }
                        }

                        // Camera Icon Button on Cover Photo (Bottom Right - Only when viewing own profile like Screenshot 2)
                        if (effectiveIsMe) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(12.dp)
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(fbLightGray)
                                    .border(1.5.dp, Color.White, CircleShape)
                                    .clickable { showPhotoSourceSheet = "COVER" },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Change Cover Photo",
                                    tint = fbDark,
                                    modifier = Modifier.size(19.dp)
                                )
                            }
                        }
                    }

                    // Left-Aligned Overlapping Profile Avatar (Matching Screenshot 1 & 2)
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(start = 16.dp)
                            .size(116.dp)
                            .clickable {
                                if (user.photoUrl.isNotBlank()) {
                                    viewingPhotoUrl = user.photoUrl
                                } else if (effectiveIsMe) {
                                    showPhotoSourceSheet = "AVATAR"
                                }
                            }
                    ) {
                        UserAvatar(
                            photoUrl = user.photoUrl,
                            name = user.displayName,
                            size = 116,
                            modifier = Modifier
                                .size(116.dp)
                                .border(4.dp, Color.White, CircleShape)
                        )

                        if (effectiveIsMe) {
                            // Camera Icon Button on Avatar (Bottom Right - Screenshot 2)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-2).dp, y = (-2).dp)
                                    .size(34.dp)
                                    .clip(CircleShape)
                                    .background(fbLightGray)
                                    .border(2.dp, Color.White, CircleShape)
                                    .clickable { showPhotoSourceSheet = "AVATAR" },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoCamera,
                                    contentDescription = "Change Profile Photo",
                                    tint = fbDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        } else if (isTargetUserOnline) {
                            // Green Online Status Dot on Avatar (Bottom Right - only when user is actively online)
                            Box(
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .offset(x = (-6).dp, y = (-6).dp)
                                    .size(20.dp)
                                    .clip(CircleShape)
                                    .background(fbGreen)
                                    .border(3.dp, Color.White, CircleShape)
                            )
                        }
                    }
                }
            }

            // LEFT-ALIGNED NAME, FRIENDS/FOLLOWERS STATS, BIO (Matching Screenshot 1 & 2)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    // Full Display Name
                    ProfileName(
                        name = user.displayName,
                        isVerified = user.isVerified,
                        isAdmin = user.isAdmin,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = fbDark,
                        badgeSize = 20.dp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    val baseFollowers = if (user.followersCount == 8500) {
                        friendUids.size.coerceAtLeast(displayFriends.size)
                    } else if (user.followersCount > 0) {
                        user.followersCount
                    } else {
                        friendUids.size.coerceAtLeast(displayFriends.size)
                    }
                    val realFollowers = if (isFollowing && baseFollowers == 0) 1 else baseFollowers
                    val realFollowing = if (user.followingCount == 3700 || user.followingCount == 370) {
                        friendUids.size
                    } else if (user.followingCount > 0) {
                        user.followingCount
                    } else {
                        friendUids.size
                    }

                    if (effectiveIsMe) {
                        // Screenshot 2: "X followers • Y following"
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.clickable { viewModel.navigateTo(ScreenTab.FRIENDS) }
                        ) {
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = fbDark)) {
                                        append(formatStats(realFollowers))
                                    }
                                    append(" followers • ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = fbDark)) {
                                        append(formatStats(realFollowing))
                                    }
                                    append(" following")
                                },
                                fontSize = 14.sp,
                                color = fbTextGray
                            )
                        }
                    } else {
                        // Screenshot 1: "X friends"
                        val friendTotal = realFollowers.coerceAtLeast(displayFriends.size)
                        Text(
                            text = buildAnnotatedString {
                                withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = fbDark)) {
                                    append("$friendTotal")
                                }
                                append(" friends")
                            },
                            fontSize = 14.sp,
                            color = fbTextGray,
                            modifier = Modifier.clickable { viewModel.navigateTo(ScreenTab.FRIENDS) }
                        )
                    }

                    // Bio Text
                    val cleanBio = if (user.bio.trim().equals("Engineer is a problem solver", ignoreCase = true)) "" else user.bio.trim()
                    if (cleanBio.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = cleanBio,
                            fontSize = 14.5.sp,
                            color = fbDark
                        )
                    } else if (effectiveIsMe) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "+ Add bio",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = fbBlue,
                            modifier = Modifier.clickable { viewModel.openEditProfile() }
                        )
                    }
                }
            }

            // PRIMARY ACTION BUTTONS (Screenshot 1 vs Screenshot 2)
            item {
                if (effectiveIsMe) {
                    // Screenshot 2: Row 1 [Professional dashboard] (Full Width Blue), Row 2 [+ Add to story] [Edit profile] [...]
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { viewModel.navigateTo(ScreenTab.DASHBOARD) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = fbBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Professional dashboard",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Professional dashboard",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Button(
                                onClick = { viewModel.openComposer() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = fbLightGray,
                                    contentColor = fbDark
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Add to story",
                                    modifier = Modifier.size(18.dp),
                                    tint = fbDark
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Add to story",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = fbDark
                                )
                            }

                            Button(
                                onClick = { viewModel.openEditProfile() },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = fbLightGray,
                                    contentColor = fbDark
                                ),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 0.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(38.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit profile",
                                    modifier = Modifier.size(16.dp),
                                    tint = fbDark
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Edit profile",
                                    fontSize = 13.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = fbDark
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(fbLightGray)
                                    .clickable { isMoreOptionsOpen = true },
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "More",
                                    tint = fbDark,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                } else {
                    // Screenshot 1: Single Row [Friends / Add friend] [Message] [...]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = {
                                if (isFriend) viewModel.unfriend(user) else viewModel.sendFriendRequest(user)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFriend) fbLightGray else fbBlue,
                                contentColor = if (isFriend) fbDark else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Icon(
                                imageVector = if (isFriend) Icons.Default.Person else Icons.Default.PersonAdd,
                                contentDescription = if (isFriend) "Friends" else "Add friend",
                                modifier = Modifier.size(17.dp),
                                tint = if (isFriend) fbDark else Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isFriend) "Friends" else "Add friend",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Button(
                            onClick = { viewModel.openChat(user) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = fbBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Message",
                                modifier = Modifier.size(16.dp),
                                tint = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Message",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(fbLightGray)
                                .clickable { isMoreOptionsOpen = true },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "More",
                                tint = fbDark,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // CREATOR MONETIZATION STUDIO QUICK ENTRY
            if (effectiveIsMe) {
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { viewModel.navigateTo(ScreenTab.CREATOR_STUDIO) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF334155)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "👑", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Creator Monetization Studio",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.White
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(Color(0xFF065F46))
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(text = "ACTIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color(0xFF34D399))
                                        }
                                    }
                                    Text(
                                        text = "Net Balance: ${String.format(java.util.Locale.US, "%,.2f", user.creatorNetBalance)} ETB · Ledger & Payouts",
                                        fontSize = 11.sp,
                                        color = Color(0xFF94A3B8)
                                    )
                                }
                            }

                            Text(
                                text = "Open →",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBBF24)
                            )
                        }
                    }
                }

                // Meskot Verified Subscription Card
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (user.isVerified) GoldSurface else CardBg
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (user.isVerified) GoldBorder else LineBorder
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                            .clickable { showMetaVerifiedModal = true }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(Gold),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = "Meskot Verified",
                                        tint = Color.White,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = if (user.isVerified) "Meskot Verified · Active Subscriber" else "Meskot Verified",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = if (user.isVerified) GoldDeep else Ink
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (user.isVerified) ActiveGreen else Gold)
                                                .padding(horizontal = 5.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = if (user.isVerified) "VERIFIED" else "GET BADGE",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White
                                            )
                                        }
                                    }
                                    Text(
                                        text = if (user.isVerified) "Identity protected · Tap to view benefits or manage" else "Golden verified badge, impersonation protection & direct support",
                                        fontSize = 11.sp,
                                        color = MutedText
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                contentDescription = null,
                                tint = if (user.isVerified) GoldDeep else MutedText,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }

                // Chapa Real ETB & Stars Wallet Card
                item {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "⭐", fontSize = 20.sp)
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = "${user.starBalance} Stars  •  ${PaymentCurrency.USD.formatFromEtb(user.creatorNetBalance)}",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = fbDark
                                    )
                                    Text(
                                        text = "≈ ${String.format(java.util.Locale.US, "%,.0f", user.creatorNetBalance)} ETB (1 USD = ${ExchangeRateManager.formattedRate()} ETB NBE)",
                                        fontSize = 10.5.sp,
                                        color = fbTextGray
                                    )
                                }
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { viewModel.buyStarsViaChapa(500, 200.0, "USD") },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldDeep),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "+ USD ($)",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Button(
                                    onClick = { viewModel.buyStarsViaChapa(500, 200.0, "ETB") },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = "+ ETB",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // NAVIGATION TABS: All | Reels | Photos
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Start
                    ) {
                        listOf("All", "Reels", "Photos").forEachIndexed { index, title ->
                            val isSelected = selectedTab == index
                            Column(
                                modifier = Modifier
                                    .clickable { selectedTab = index }
                                    .padding(horizontal = 16.dp, vertical = 10.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = title,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) fbBlue else fbTextGray
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .width(36.dp)
                                        .height(3.dp)
                                        .clip(RoundedCornerShape(2.dp))
                                        .background(if (isSelected) fbBlue else Color.Transparent)
                                )
                            }
                        }
                    }
                    HorizontalDivider(color = fbLightGray, thickness = 1.dp)
                }
            }

            // PERSONAL DETAILS SECTION (Facebook style Screenshot 1 & 2, shown in 'All' tab)
            if (selectedTab == 0) {
                item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Personal details",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = fbDark
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        if (effectiveIsMe) {
                            IconButton(
                                onClick = { viewModel.openEditProfile() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit personal details",
                                    tint = fbDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val cleanProfession = user.profession.trim().ifBlank { "Public figure" }
                    val cleanLocation = user.location.trim().ifBlank { "Addis Ababa, Ethiopia" }
                    val cleanHometown = user.hometown.trim().ifBlank { cleanLocation }
                    val cleanBirthDate = user.birthDate.trim().ifBlank { "August 29, 2002" }
                    val cleanGender = user.gender.trim()

                    // 1. Category / Profession (Screenshot 2: "Profile · Public figure")
                    if (effectiveIsMe || user.profession.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (effectiveIsMe) viewModel.openEditProfile() else isSeeMoreDetailsOpen = true
                                }
                                .padding(vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Profile category",
                                tint = fbTextGray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = fbDark)) {
                                        append("Profile")
                                    }
                                    append(" · ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold, color = fbDark)) {
                                        append(cleanProfession)
                                    }
                                },
                                fontSize = 14.5.sp,
                                color = fbDark
                            )
                        }
                    }

                    // 2. Current City / Location (Screenshot 1 & 2: Home icon + City)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (effectiveIsMe) viewModel.openEditProfile() else isSeeMoreDetailsOpen = true
                            }
                            .padding(vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Current city",
                            tint = fbTextGray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (effectiveIsMe) {
                                buildAnnotatedString { append(cleanLocation) }
                            } else {
                                buildAnnotatedString {
                                    append("Lives in ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = fbDark)) {
                                        append(cleanLocation)
                                    }
                                }
                            },
                            fontSize = 14.5.sp,
                            color = fbDark
                        )
                    }

                    // 3. Hometown (Screenshot 1 & 2: Pin icon + Hometown)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                if (effectiveIsMe) viewModel.openEditProfile() else isSeeMoreDetailsOpen = true
                            }
                            .padding(vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Hometown",
                            tint = fbTextGray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (effectiveIsMe) {
                                buildAnnotatedString { append(cleanHometown) }
                            } else {
                                buildAnnotatedString {
                                    append("From ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = fbDark)) {
                                        append(cleanHometown)
                                    }
                                }
                            },
                            fontSize = 14.5.sp,
                            color = fbDark
                        )
                    }

                    // 4. Birthday (Screenshot 2: Cake icon + Date + Lock icon)
                    if (effectiveIsMe || user.birthDate.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (effectiveIsMe) viewModel.openEditProfile() else isSeeMoreDetailsOpen = true
                                }
                                .padding(vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Cake,
                                contentDescription = "Birthday",
                                tint = fbTextGray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = cleanBirthDate,
                                fontSize = 14.5.sp,
                                color = fbDark
                            )
                            if (effectiveIsMe) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = "Only me",
                                    tint = fbTextGray,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }

                    // 5. Work & Education lines inside Personal details when viewing Other's Profile (Screenshot 1)
                    if (!effectiveIsMe) {
                        val visitorWork = user.workplace.trim().ifBlank { "Engineering & Technology" }
                        val visitorEdu = user.education.trim().ifBlank { "Adigrat University" }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isSeeMoreDetailsOpen = true }
                                .padding(vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Work,
                                contentDescription = "Work",
                                tint = fbTextGray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = buildAnnotatedString {
                                    append("Works at ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = fbDark)) {
                                        append(visitorWork)
                                    }
                                },
                                fontSize = 14.5.sp,
                                color = fbDark
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isSeeMoreDetailsOpen = true }
                                .padding(vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.School,
                                contentDescription = "Education",
                                tint = fbTextGray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = buildAnnotatedString {
                                    append("Studied at ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = fbDark)) {
                                        append(visitorEdu)
                                    }
                                },
                                fontSize = 14.5.sp,
                                color = fbDark
                            )
                        }
                    }

                    // 6. Gender if set
                    if (cleanGender.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Gender",
                                tint = fbTextGray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = cleanGender,
                                fontSize = 14.5.sp,
                                color = fbDark
                            )
                        }
                    }

                    // 7. "See more personal details" / "See more about <FirstName>" link
                    val firstName = user.displayName.split(" ").firstOrNull() ?: user.displayName
                    Text(
                        text = if (effectiveIsMe) "See more personal details" else "See more about $firstName",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Medium,
                        color = fbTextGray,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isSeeMoreDetailsOpen = true }
                            .padding(vertical = 6.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    HorizontalDivider(color = fbLightGray, thickness = 1.dp)
                }
            }

            // WORK SECTION (Screenshot 2: Dedicated section when viewing Own Profile)
            if (effectiveIsMe) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Work",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = fbDark
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { viewModel.openEditProfile() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit work",
                                    tint = fbDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val cleanWorkplace = user.workplace.trim().ifBlank { "Adigrat University _Engineering Sciences" }
                        val cleanWorkRole = user.workRole.trim().ifBlank { "Civil Engineering" }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openEditProfile() }
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(fbLightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Work,
                                    contentDescription = "Work",
                                    tint = fbDark,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = cleanWorkplace,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = fbDark
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Private",
                                        tint = fbTextGray,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = cleanWorkRole,
                                    fontSize = 13.sp,
                                    color = fbTextGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "See more work",
                            fontSize = 14.sp,
                            color = fbTextGray,
                            modifier = Modifier
                                .clickable { isSeeMoreWorkOpen = true }
                                .padding(vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = fbLightGray, thickness = 1.dp)
                    }
                }

                // EDUCATION SECTION (Screenshot 2: Dedicated section when viewing Own Profile)
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Education",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = fbDark
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            IconButton(
                                onClick = { viewModel.openEditProfile() },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "Edit education",
                                    tint = fbDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        val cleanEdu = user.education.trim().ifBlank { "Adigrat University" }
                        val cleanEduClass = user.educationClass.trim().ifBlank { "Class of 2025" }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openEditProfile() }
                                .padding(vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(fbLightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.School,
                                    contentDescription = "Education",
                                    tint = fbDark,
                                    modifier = Modifier.size(22.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = cleanEdu,
                                        fontSize = 14.5.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = fbDark
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Lock,
                                        contentDescription = "Private",
                                        tint = fbTextGray,
                                        modifier = Modifier.size(13.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = cleanEduClass,
                                    fontSize = 13.sp,
                                    color = fbTextGray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider(color = fbLightGray, thickness = 1.dp)
                    }
                }
            }

            // FRIENDS SECTION (Screenshot 1: 3x2 Rectangular Card Grid | Screenshot 2: 4 Circular Avatars Row)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Friends",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = fbDark
                            )
                            if (!effectiveIsMe) {
                                val totalFriendsCount = user.followersCount.takeIf { it > 0 && it != 8500 } ?: displayFriends.size.coerceAtLeast(1)
                                Text(
                                    text = "$totalFriendsCount friends",
                                    fontSize = 13.sp,
                                    color = fbTextGray
                                )
                            }
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { viewModel.navigateTo(ScreenTab.FRIENDS) }) {
                            Text(
                                text = if (effectiveIsMe) "See all" else "Find Friends",
                                color = fbBlue,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (effectiveIsMe) {
                        // Screenshot 2: 4 Circular Avatars with green online dot and mutual friend count
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            displayFriends.take(4).forEachIndexed { idx, friend ->
                                val mutualCount = (idx + 1) * 2
                                val isFriendOnline = remember(friend.lastSeen, nowMs) {
                                    MeskotStrings.isOnline(friend.lastSeen, nowMs)
                                }
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .padding(horizontal = 4.dp)
                                        .clickable { viewModel.openProfile(friend) },
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(modifier = Modifier.size(68.dp)) {
                                        UserAvatar(
                                            photoUrl = friend.photoUrl,
                                            name = friend.displayName,
                                            size = 68,
                                            modifier = Modifier.clip(CircleShape)
                                        )
                                        if (isFriendOnline) {
                                            Box(
                                                modifier = Modifier
                                                    .align(Alignment.BottomEnd)
                                                    .offset(x = (-2).dp, y = (-2).dp)
                                                    .size(14.dp)
                                                    .clip(CircleShape)
                                                    .background(fbGreen)
                                                    .border(2.dp, Color.White, CircleShape)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(6.dp))

                                    Text(
                                        text = friend.displayName,
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = fbDark,
                                        textAlign = TextAlign.Center,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    Spacer(modifier = Modifier.height(2.dp))

                                    Text(
                                        text = "$mutualCount mutual friends",
                                        fontSize = 10.5.sp,
                                        color = fbTextGray,
                                        textAlign = TextAlign.Center,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    } else {
                        // Screenshot 1: 3x2 Rectangular Card Grid + "See all friends" gray full-width button
                        val gridFriends = displayFriends.take(6)
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            gridFriends.chunked(3).forEach { rowList ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    rowList.forEachIndexed { idx, friend ->
                                        Card(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { viewModel.openProfile(friend) },
                                            shape = RoundedCornerShape(8.dp),
                                            colors = CardDefaults.cardColors(containerColor = Color.White),
                                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                                        ) {
                                            Column {
                                                Box(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .aspectRatio(1f)
                                                        .background(Color(0xFFF0F2F5)),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    if (friend.photoUrl.isNotBlank()) {
                                                        AsyncImage(
                                                            model = friend.photoUrl,
                                                            contentDescription = friend.displayName,
                                                            modifier = Modifier.fillMaxSize(),
                                                            contentScale = ContentScale.Crop
                                                        )
                                                    } else {
                                                        UserAvatar(
                                                            photoUrl = "",
                                                            name = friend.displayName,
                                                            size = 64
                                                        )
                                                    }
                                                }
                                                Column(modifier = Modifier.padding(6.dp)) {
                                                    Text(
                                                        text = friend.displayName,
                                                        fontSize = 12.5.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = fbDark,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                    Text(
                                                        text = "${(idx + 1) * 3} mutual friends",
                                                        fontSize = 10.5.sp,
                                                        color = fbTextGray,
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }
                                    }
                                    repeat(3 - rowList.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Button(
                                onClick = { viewModel.navigateTo(ScreenTab.FRIENDS) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = fbLightGray,
                                    contentColor = fbDark
                                ),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(38.dp)
                            ) {
                                Text(
                                    text = "See all friends",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = fbDark
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = fbLightGray, thickness = 1.dp)
                }
            }

            // POSTS HEADER SECTION (Screenshot 1 & 2)
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (effectiveIsMe) "Posts" else "${user.displayName.split(" ").firstOrNull() ?: user.displayName}'s posts",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = fbDark
                    )
                    if (effectiveIsMe) {
                        Text(
                            text = "Filters",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = fbBlue,
                            modifier = Modifier.clickable { isSearchOpen = !isSearchOpen }
                        )
                    }
                }
            }

            // "WHAT'S ON YOUR MIND?" COMPOSER BOX (Only on Own Profile - Screenshot 2)
            if (effectiveIsMe) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 6.dp),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        border = androidx.compose.foundation.BorderStroke(1.dp, fbLightGray)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(
                                    photoUrl = currentUser?.photoUrl ?: user.photoUrl,
                                    name = currentUser?.displayName ?: user.displayName,
                                    size = 38
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(38.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(Color(0xFFF0F2F5))
                                        .clickable { viewModel.openComposer() }
                                        .padding(horizontal = 14.dp),
                                    contentAlignment = Alignment.CenterStart
                                ) {
                                    Text(
                                        text = "What's on your mind?",
                                        color = fbTextGray,
                                        fontSize = 14.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(onClick = { viewModel.openComposer() }) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = "Upload Photo",
                                        tint = fbGreen,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }

                            HorizontalDivider(
                                color = fbLightGray,
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(vertical = 10.dp)
                            )

                            // Composer actions: Photo | Reel | Check In | Life Event
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable { viewModel.openComposer() }
                                        .padding(vertical = 4.dp, horizontal = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = "Photo",
                                        tint = fbGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Photo",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = fbDark
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable { viewModel.openCreateReel() }
                                        .padding(vertical = 4.dp, horizontal = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.VideoCameraBack,
                                        contentDescription = "Reel",
                                        tint = GoldDeep,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Reel",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = fbDark
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable { viewModel.openComposer() }
                                        .padding(vertical = 4.dp, horizontal = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Place,
                                        contentDescription = "Check In",
                                        tint = CrossRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Check In",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = fbDark
                                    )
                                }

                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .clickable { viewModel.openComposer() }
                                        .padding(vertical = 4.dp, horizontal = 6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.EmojiEvents,
                                        contentDescription = "Life Event",
                                        tint = Color(0xFF9C27B0),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = "Life Event",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = fbDark
                                    )
                                }
                            }
                        }
                    }
                }
            }
            } // End of if (selectedTab == 0) for Personal details & composer

            // TABS CONTENT: ALL (POSTS) | REELS | PHOTOS
            if (selectedTab == 0) {
                // ALL / POSTS TAB
                if (allPosts.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "No posts yet",
                                color = fbTextGray,
                                fontSize = 14.sp
                            )
                        }
                    }
                } else {
                    items(allPosts) { post ->
                        val comments = viewModel.getComments(post.id)
                        val userTier = viewModel.getUserMembershipTier(post.uid)
                        PostCard(
                            post = post,
                            currentUserId = currentUser?.uid,
                            comments = comments,
                            currentLanguage = currentLanguage,
                            currentUserTier = userTier,
                            onBoostClick = { viewModel.openBoostModal(it) },
                            onUnlockVip = { viewModel.openSubscriptionModal(it) },
                            onAuthorClick = { viewModel.openProfileByUid(it) },
                            onToggleReaction = { pid, type -> viewModel.toggleReaction(pid, type) },
                            onShare = { viewModel.sharePost(it) },
                            onToggleSave = { viewModel.toggleSavePost(it) },
                            onTipClick = { viewModel.openTipModal(it) },
                            onOpenMenu = { viewModel.openPostMenu(it) },
                            onAddComment = { pid, text, parentId -> viewModel.addComment(pid, text, parentId) },
                            onToggleCommentLike = { pid, cid -> viewModel.toggleCommentLike(pid, cid) },
                            onDeleteComment = { pid, cid -> viewModel.deleteComment(pid, cid) },
                            onReelClick = { viewModel.viewReel(it) }
                        )
                    }
                }
            } else if (selectedTab == 1) {
                // REELS TAB
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.VideoCameraBack,
                                contentDescription = null,
                                tint = GoldDeep,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Reels",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = fbDark
                            )
                            if (userReels.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${userReels.size})",
                                    fontSize = 14.sp,
                                    color = fbTextGray
                                )
                            }
                        }
                        if (isMe) {
                            Surface(
                                onClick = { viewModel.openCreateReel() },
                                shape = RoundedCornerShape(16.dp),
                                color = GoldSurface,
                                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = GoldDeep,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Create Reel",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = GoldDeep
                                    )
                                }
                            }
                        }
                    }
                }

                if (userReels.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(GoldSurface)
                                    .border(1.5.dp, GoldBorder, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.VideoCameraBack,
                                    contentDescription = null,
                                    tint = GoldDeep,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (isMe) "No reels posted yet" else "No reels posted by ${user.displayName}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = fbDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isMe) "Share short video clips, music vibes, and cultural moments with your audience" else "When they share a reel, it will appear here.",
                                fontSize = 13.sp,
                                color = fbTextGray,
                                textAlign = TextAlign.Center
                            )
                            if (isMe) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.openCreateReel() },
                                    colors = ButtonDefaults.buttonColors(containerColor = GoldDeep),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Create First Reel", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    items(userReels.chunked(3)) { rowReels ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            rowReels.forEach { reel ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(9f / 16f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(Color.Black)
                                        .clickable { viewModel.viewReel(reel) }
                                ) {
                                    val mediaUrl = reel.videoUrl.ifBlank { reel.mediaUrls.firstOrNull() }
                                    if (!mediaUrl.isNullOrBlank()) {
                                        AsyncImage(
                                            model = mediaUrl,
                                            contentDescription = reel.text,
                                            modifier = Modifier.fillMaxSize(),
                                            contentScale = ContentScale.Crop
                                        )
                                    }

                                    // Vertical dark gradient overlay
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(
                                                Brush.verticalGradient(
                                                    0.0f to Color.Black.copy(alpha = 0.35f),
                                                    0.6f to Color.Transparent,
                                                    1.0f to Color.Black.copy(alpha = 0.85f)
                                                )
                                            )
                                    )

                                    // Top Views count
                                    Row(
                                        modifier = Modifier
                                            .align(Alignment.TopStart)
                                            .padding(6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(12.dp)
                                        )
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(
                                            text = if (reel.viewsCount > 0) "${reel.viewsCount}" else "1.2K",
                                            color = Color.White,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    // Bottom caption or audio title
                                    Column(
                                        modifier = Modifier
                                            .align(Alignment.BottomStart)
                                            .padding(6.dp)
                                    ) {
                                        if (reel.audioTrackTitle.isNotBlank()) {
                                            Text(
                                                text = "🎵 ${reel.audioTrackTitle}",
                                                color = GoldLight,
                                                fontSize = 9.sp,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                        if (reel.text.isNotBlank()) {
                                            Text(
                                                text = reel.text,
                                                color = Color.White,
                                                fontSize = 10.sp,
                                                maxLines = 2,
                                                overflow = TextOverflow.Ellipsis,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                            repeat(3 - rowReels.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            } else if (selectedTab == 2) {
                // PHOTOS TAB
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                tint = fbDark,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Photos",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = fbDark
                            )
                            if (allPhotosList.isNotEmpty()) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "(${allPhotosList.size})",
                                    fontSize = 14.sp,
                                    color = fbTextGray
                                )
                            }
                        }
                        if (isMe) {
                            Surface(
                                onClick = { viewModel.openComposer() },
                                shape = RoundedCornerShape(16.dp),
                                color = fbLightGray.copy(alpha = 0.6f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Add,
                                        contentDescription = null,
                                        tint = fbDark,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = "Add Photo",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = fbDark
                                    )
                                }
                            }
                        }
                    }
                }

                if (allPhotosList.isEmpty()) {
                    item {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 24.dp, vertical = 40.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(CircleShape)
                                    .background(fbLightGray.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhotoLibrary,
                                    contentDescription = null,
                                    tint = fbTextGray,
                                    modifier = Modifier.size(36.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(14.dp))
                            Text(
                                text = if (isMe) "No photos uploaded yet" else "No photos uploaded by ${user.displayName}",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = fbDark
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (isMe) "Photos and memories you share with your friends will appear here" else "When they upload photos, they will appear here.",
                                fontSize = 13.sp,
                                color = fbTextGray,
                                textAlign = TextAlign.Center
                            )
                            if (isMe) {
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(
                                    onClick = { viewModel.openComposer() },
                                    colors = ButtonDefaults.buttonColors(containerColor = fbBlue),
                                    shape = RoundedCornerShape(20.dp)
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Upload Photo", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                } else {
                    items(allPhotosList.chunked(3)) { rowPhotos ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 3.dp),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            rowPhotos.forEach { (url, post) ->
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .aspectRatio(1f)
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(Color(0xFFF0F2F5))
                                        .clickable { viewingPhotoUrl = url }
                                ) {
                                    AsyncImage(
                                        model = url,
                                        contentDescription = post.text.ifBlank { "User Photo" },
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                            repeat(3 - rowPhotos.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(90.dp))
            }
        }
    }

    // FULLSCREEN PHOTO PREVIEW DIALOG
    viewingPhotoUrl?.let { photoUrl ->
        Dialog(
            onDismissRequest = { viewingPhotoUrl = null },
            properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = photoUrl,
                    contentDescription = "Full photo preview",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    contentScale = ContentScale.Fit
                )
                IconButton(
                    onClick = { viewingPhotoUrl = null },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(20.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close photo",
                        tint = Color.White
                    )
                }
            }
        }
    }

    // PHOTO SOURCE PICKER DIALOG (Avatar or Cover Photo)
    showPhotoSourceSheet?.let { targetType ->
        val isCoverTarget = targetType == "COVER"
        val currentTargetUrl = if (isCoverTarget) user.coverPhotoUrl else user.photoUrl
        Dialog(onDismissRequest = { showPhotoSourceSheet = null }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = if (isCoverTarget) "Cover Photo" else "Profile Picture",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = fbDark
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPhotoSourceSheet = null
                                if (isCoverTarget) {
                                    coverGalleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                } else {
                                    avatarGalleryLauncher.launch(
                                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                    )
                                }
                            }
                            .padding(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = fbBlue)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = if (isCoverTarget) "Upload cover photo from Gallery" else "Select profile picture from Gallery",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = fbDark
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                showPhotoSourceSheet = null
                                if (isCoverTarget) {
                                    coverCameraLauncher.launch(null)
                                } else {
                                    avatarCameraLauncher.launch(null)
                                }
                            }
                            .padding(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = fbGreen)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text(
                            text = if (isCoverTarget) "Take new cover photo with Camera" else "Take new profile photo with Camera",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium,
                            color = fbDark
                        )
                    }

                    if (currentTargetUrl.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    showPhotoSourceSheet = null
                                    viewingPhotoUrl = currentTargetUrl
                                }
                                .padding(vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.Visibility, contentDescription = null, tint = fbDark)
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = if (isCoverTarget) "View cover photo" else "View profile picture",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Medium,
                                color = fbDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showPhotoSourceSheet = null }) {
                            Text("Cancel", color = fbTextGray, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // MORE OPTIONS DIALOG (Facebook Style)
    if (isMoreOptionsOpen) {
        Dialog(onDismissRequest = { isMoreOptionsOpen = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Profile Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = fbDark
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (isMe) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isMoreOptionsOpen = false
                                    viewAsOtherMode = !viewAsOtherMode
                                }
                                .padding(vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = if (viewAsOtherMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = fbBlue
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = if (viewAsOtherMode) "Exit View As (Return to Own Profile)" else "View as (See how others see your profile)",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = fbBlue
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isMoreOptionsOpen = false
                                    viewModel.openEditProfile()
                                }
                                .padding(vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, tint = fbDark)
                            Spacer(modifier = Modifier.width(14.dp))
                            Text("Edit Profile", fontSize = 15.sp, color = fbDark)
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isMoreOptionsOpen = false
                                    viewModel.toggleFollow(user.uid)
                                }
                                .padding(vertical = 10.dp)
                        ) {
                            Icon(
                                imageVector = if (isFollowing) Icons.Default.Check else Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = fbBlue
                            )
                            Spacer(modifier = Modifier.width(14.dp))
                            Text(
                                text = if (isFollowing) "Unfollow ${user.displayName}" else "Follow ${user.displayName}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = fbBlue
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isMoreOptionsOpen = false
                                    viewModel.openSubscriptionModal(user)
                                }
                                .padding(vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.Verified, contentDescription = null, tint = GoldDeep)
                            Spacer(modifier = Modifier.width(14.dp))
                            Text("Join VIP Fan Club", fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = GoldDeep)
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isMoreOptionsOpen = false
                                val profileLink = "https://meskot.app/profile/${user.uid}"
                                clipboardManager.setText(AnnotatedString(profileLink))
                                viewModel.showMessage("Profile link copied: $profileLink")
                            }
                            .padding(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null, tint = fbDark)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("Copy link to profile", fontSize = 15.sp, color = fbDark)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isMoreOptionsOpen = false
                                isSearchOpen = true
                            }
                            .padding(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = fbDark)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("Search in profile", fontSize = 15.sp, color = fbDark)
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isMoreOptionsOpen = false
                                isSeeMoreDetailsOpen = true
                            }
                            .padding(vertical = 10.dp)
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = fbDark)
                        Spacer(modifier = Modifier.width(14.dp))
                        Text("About this profile", fontSize = 15.sp, color = fbDark)
                    }

                    if (!isMe) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    isMoreOptionsOpen = false
                                    viewModel.showMessage("Report submitted for review")
                                }
                                .padding(vertical = 10.dp)
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = CrossRed)
                            Spacer(modifier = Modifier.width(14.dp))
                            Text("Find support or report profile", fontSize = 15.sp, color = CrossRed)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isMoreOptionsOpen = false }) {
                            Text("Close", color = fbBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // SEE MORE DETAILS DIALOG
    if (isSeeMoreDetailsOpen) {
        Dialog(onDismissRequest = { isSeeMoreDetailsOpen = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "About " + user.displayName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = fbDark
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text("Overview", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = fbTextGray)
                    Spacer(modifier = Modifier.height(6.dp))
                    val catText = user.profession.trim()
                    if (catText.isNotBlank()) {
                        Text("• Category: $catText", fontSize = 14.sp, color = fbDark)
                    }
                    val cityText = user.location.trim()
                    if (cityText.isNotBlank()) {
                        Text("• Lives in $cityText", fontSize = 14.sp, color = fbDark)
                    }
                    val homeText = user.hometown.trim()
                    if (homeText.isNotBlank()) {
                        Text("• From $homeText", fontSize = 14.sp, color = fbDark)
                    }
                    val bdayText = user.birthDate.trim()
                    if (bdayText.isNotBlank()) {
                        Text("• Birthday: $bdayText", fontSize = 14.sp, color = fbDark)
                    }
                    if (user.gender.isNotBlank()) {
                        Text("• Gender: ${user.gender}", fontSize = 14.sp, color = fbDark)
                    }
                    if (catText.isBlank() && cityText.isBlank() && homeText.isBlank() && bdayText.isBlank() && user.gender.isBlank()) {
                        Text("• No overview details provided yet", fontSize = 14.sp, color = fbTextGray)
                    }

                    val workP = user.workplace.trim()
                    val workR = user.workRole.trim()
                    val eduU = user.education.trim()
                    val eduC = user.educationClass.trim()

                    if (workP.isNotBlank() || workR.isNotBlank() || eduU.isNotBlank() || eduC.isNotBlank()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        Text("Work & Education", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = fbTextGray)
                        Spacer(modifier = Modifier.height(6.dp))
                        if (workP.isNotBlank()) Text("• Works at: $workP", fontSize = 14.sp, color = fbDark)
                        if (workR.isNotBlank()) Text("• Role: $workR", fontSize = 14.sp, color = fbDark)
                        if (eduU.isNotBlank()) Text("• Studied at: $eduU", fontSize = 14.sp, color = fbDark)
                        if (eduC.isNotBlank()) Text("• Graduation: $eduC", fontSize = 14.sp, color = fbDark)
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text("Contact Info", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = fbTextGray)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("• Email: ${user.email.ifBlank { "Not provided" }}", fontSize = 14.sp, color = fbDark)
                    if (user.phoneNumber.isNotBlank()) {
                        Text("• Phone: ${user.phoneNumber}", fontSize = 14.sp, color = fbDark)
                    }
                    Text("• Privacy: Protected", fontSize = 14.sp, color = fbDark)

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        if (isMe) {
                            Button(
                                onClick = {
                                    isSeeMoreDetailsOpen = false
                                    viewModel.openEditProfile()
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.White),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Edit Details", fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        TextButton(onClick = { isSeeMoreDetailsOpen = false }) {
                            Text("Done", color = fbBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    // SEE MORE WORK DIALOG
    if (isSeeMoreWorkOpen) {
        Dialog(onDismissRequest = { isSeeMoreWorkOpen = false }) {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .padding(20.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    Text(
                        text = "Work Experience",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = fbDark
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    val workP = if (user.workplace.equals("Adigrat university _Engineering Sciences", ignoreCase = true)) "" else user.workplace.trim()
                    val workR = if (user.workRole.equals("Civil Engineering", ignoreCase = true)) "" else user.workRole.trim()

                    if (workP.isBlank() && workR.isBlank()) {
                        Text(
                            text = "No work experience listed yet.",
                            fontSize = 14.sp,
                            color = fbTextGray
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(fbLightGray),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.Work, contentDescription = null, tint = fbDark)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                if (workP.isNotBlank()) {
                                    Text(
                                        text = workP,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = fbDark
                                    )
                                }
                                if (workR.isNotBlank()) {
                                    Text(
                                        text = workR,
                                        fontSize = 13.sp,
                                        color = fbTextGray
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { isSeeMoreWorkOpen = false }) {
                            Text("Close", color = fbBlue, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }

    if (showMetaVerifiedModal) {
        MetaVerifiedBottomSheetModal(
            currentUser = currentUser,
            onDismiss = { showMetaVerifiedModal = false },
            onSubscribeConfirmed = { paymentMethod, planId ->
                viewModel.subscribeMetaVerified(paymentMethod, planId)
            },
            onCancelSubscription = {
                viewModel.cancelMetaVerified()
            }
        )
    }
}

@Composable
fun MenuScreen(
    viewModel: MeskotViewModel,
    currentUser: User?,
    allUsers: List<User>,
    currentLanguage: AppLanguage
) {
    var showMetaVerifiedModal by remember { mutableStateOf(false) }

    val friends by viewModel.friends.collectAsState()
    val groups by viewModel.groups.collectAsState()
    val savedPostIds by viewModel.savedPostIds.collectAsState()
    val adCampaigns by viewModel.adCampaigns.collectAsState()
    val walletBalanceEtb by viewModel.creatorNetBalance.collectAsState()
    val unreadNotifsCount by viewModel.unreadNotifsCount.collectAsState()
    val unreadMsgCount by viewModel.unreadMsgCount.collectAsState()

    val darkObsidianTop = Color(0xFF132019)
    val darkObsidianBottom = Color(0xFF1D3126)
    val darkCardBrush = Brush.linearGradient(listOf(darkObsidianTop, darkObsidianBottom))

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Paper,
                        GoldSurface.copy(alpha = 0.65f),
                        Paper
                    )
                )
            )
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("menu_screen"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. ROYAL IDENTITY & WALLET PASSPORT HERO CARD (Distinctive Meskot Luxury vs Facebook)
        if (currentUser != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.openProfile(currentUser) },
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = darkObsidianTop),
                    elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, GoldLight.copy(alpha = 0.65f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(darkCardBrush)
                    ) {
                        // Top Tibeb Royal Gold Shimmer Strip
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(GoldDeep, GoldLight, CrossRed, GoldLight, GoldDeep)
                                    )
                                )
                        )

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    // Double-ringed Gold Medallion Avatar
                                    Box(
                                        modifier = Modifier
                                            .size(62.dp)
                                            .clip(CircleShape)
                                            .background(MeskotLogoBrush)
                                            .padding(2.5.dp)
                                            .clip(CircleShape)
                                            .background(darkObsidianTop)
                                            .padding(2.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        UserAvatar(
                                            photoUrl = currentUser.photoUrl,
                                            name = currentUser.displayName,
                                            size = 54
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(14.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(6.dp),
                                                color = Gold.copy(alpha = 0.22f),
                                                border = androidx.compose.foundation.BorderStroke(
                                                    0.8.dp,
                                                    GoldLight.copy(alpha = 0.6f)
                                                )
                                            ) {
                                                Text(
                                                    text = "✦ MESKOT ROYAL PASSPORT",
                                                    fontSize = 9.sp,
                                                    fontWeight = FontWeight.ExtraBold,
                                                    color = GoldLight,
                                                    letterSpacing = 0.8.sp,
                                                    modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(5.dp))

                                        ProfileName(
                                            name = currentUser.displayName,
                                            isVerified = currentUser.isVerified,
                                            isAdmin = currentUser.isAdmin,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White
                                        )

                                        Spacer(modifier = Modifier.height(2.dp))

                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = MeskotStrings.get("viewProfile", currentLanguage),
                                                fontSize = 12.sp,
                                                color = GoldLight,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                                contentDescription = null,
                                                tint = GoldLight,
                                                modifier = Modifier.size(11.dp)
                                            )
                                        }
                                    }
                                }

                                // Luxury Gold Crest Emblem on the right
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(Color.White.copy(alpha = 0.08f))
                                        .border(1.dp, GoldLight.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    MeskotLogoBadge(size = 28.dp)
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Live Royal Telemetry & Wallet Strip inside the Passport Card
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = Color.Black.copy(alpha = 0.28f),
                                border = androidx.compose.foundation.BorderStroke(
                                    0.8.dp,
                                    GoldBorder.copy(alpha = 0.35f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    LuxuryPassportMetricPill(
                                        label = "CIRCLE",
                                        value = "${friends.size} Friends",
                                        onClick = { viewModel.navigateTo(ScreenTab.FRIENDS) }
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(24.dp)
                                            .background(GoldBorder.copy(alpha = 0.3f))
                                    )
                                    LuxuryPassportMetricPill(
                                        label = "STARS",
                                        value = "${currentUser.starBalance} ⭐",
                                        onClick = { viewModel.openBuyStars() }
                                    )
                                    Box(
                                        modifier = Modifier
                                            .width(1.dp)
                                            .height(24.dp)
                                            .background(GoldBorder.copy(alpha = 0.3f))
                                    )
                                    LuxuryPassportMetricPill(
                                        label = "WALLET",
                                        value = PaymentCurrency.USD.formatFromEtb(walletBalanceEtb),
                                        onClick = { viewModel.openChapaDeposit() }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. MESKOT VERIFIED IMPERIAL CROWN BANNER
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showMetaVerifiedModal = true },
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = GoldSurface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                    border = androidx.compose.foundation.BorderStroke(1.4.dp, Gold)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.horizontalGradient(
                                    listOf(
                                        Color(0xFFFDF6E4),
                                        Color(0xFFFFFBF0),
                                        Color(0xFFF9EBD0)
                                    )
                                )
                            )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(MeskotLogoBrush)
                                    .border(1.dp, GoldLight, RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Meskot Verified",
                                    tint = Color.White,
                                    modifier = Modifier.size(25.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Meskot Verified",
                                        fontSize = 15.5.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = Ink
                                    )
                                    Spacer(modifier = Modifier.width(7.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (currentUser.isVerified) {
                                                    Brush.horizontalGradient(listOf(ActiveGreen, ActiveGreen))
                                                } else {
                                                    MeskotLogoHorizontalBrush
                                                }
                                            )
                                            .padding(horizontal = 7.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (currentUser.isVerified) "IMPERIAL VIP" else "GOLD SEAL",
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = Color.White,
                                            letterSpacing = 0.5.sp
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (currentUser.isVerified) {
                                        "Active golden seal, priority ranking & account protection"
                                    } else {
                                        "Unlock the royal golden seal, priority feed reach & VIP support"
                                    },
                                    fontSize = 12.sp,
                                    color = GoldDeep,
                                    fontWeight = FontWeight.Medium,
                                    lineHeight = 16.sp
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Gold.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                                    contentDescription = null,
                                    tint = GoldDeep,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. EXECUTIVE BUSINESS & MONETIZATION LOUNGE (3 Distinctive Pillar Cards — completely unlike Facebook's flat grid)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                LuxuryMenuSectionHeader(
                    eyebrow = "EXECUTIVE SUITE · የንግድ ማዕከል",
                    title = "Commerce & Creator Lounge"
                )
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    LuxuryExecutivePillarCard(
                        icon = Icons.Default.Storefront,
                        title = "Marketplace",
                        amharicTag = "ገበያ",
                        metricBadge = "NBE Live",
                        isDarkObsidian = false,
                        onClick = { viewModel.navigateTo(ScreenTab.MARKETPLACE) },
                        modifier = Modifier.weight(1f)
                    )
                    LuxuryExecutivePillarCard(
                        icon = Icons.Default.Campaign,
                        title = "Ads Manager",
                        amharicTag = "ማስታወቂያ",
                        metricBadge = "${adCampaigns.count { it.status == "ACTIVE" }} Active",
                        isDarkObsidian = true,
                        onClick = { viewModel.navigateTo(ScreenTab.ADS_MANAGER) },
                        modifier = Modifier.weight(1f)
                    )
                    LuxuryExecutivePillarCard(
                        icon = Icons.Default.WorkspacePremium,
                        title = "Creator Studio",
                        amharicTag = "ፈጣሪዎች",
                        metricBadge = "Chapa Pay",
                        isDarkObsidian = false,
                        onClick = { viewModel.navigateTo(ScreenTab.CREATOR_STUDIO) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // 4. COMMUNITY & CULTURAL PORTALS (Curated Luxury 2-Column Gallery with Vector Medallions & Subtitles)
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                LuxuryMenuSectionHeader(
                    eyebrow = "MESKOT PORTALS · የማህበረሰብ መስኮቶች",
                    title = "Community & Experiences"
                )
                Spacer(modifier = Modifier.height(10.dp))

                val communityItems = listOf(
                    LuxuryPortalItem(
                        icon = Icons.Default.DynamicFeed,
                        title = MeskotStrings.get("navFeed", currentLanguage),
                        subtitle = "Stories, Reels & Pulse",
                        badgeText = "LIVE",
                        tab = ScreenTab.FEED,
                        isFeaturedGold = false
                    ),
                    LuxuryPortalItem(
                        icon = Icons.Default.Diversity3,
                        title = MeskotStrings.get("navGroups", currentLanguage),
                        subtitle = "${groups.size} Cultural Circles",
                        badgeText = "HUB",
                        tab = ScreenTab.GROUPS,
                        isFeaturedGold = true
                    ),
                    LuxuryPortalItem(
                        icon = Icons.Default.PeopleAlt,
                        title = MeskotStrings.get("navFriends", currentLanguage),
                        subtitle = "${friends.size} Connected",
                        badgeText = null,
                        tab = ScreenTab.FRIENDS,
                        isFeaturedGold = false
                    ),
                    LuxuryPortalItem(
                        icon = Icons.Default.Forum,
                        title = MeskotStrings.get("navMessages", currentLanguage),
                        subtitle = "Direct & Voice Chat",
                        badgeText = if (unreadMsgCount > 0) "$unreadMsgCount NEW" else null,
                        tab = ScreenTab.MESSAGES,
                        isFeaturedGold = false
                    ),
                    LuxuryPortalItem(
                        icon = Icons.Default.PhotoLibrary,
                        title = MeskotStrings.get("navPhotos", currentLanguage),
                        subtitle = "Visual Heritage Albums",
                        badgeText = null,
                        tab = ScreenTab.PHOTOS,
                        isFeaturedGold = false
                    ),
                    LuxuryPortalItem(
                        icon = Icons.Default.Podcasts,
                        title = "Live Streaming",
                        subtitle = "Broadcast & Coffee Gifts",
                        badgeText = "ON AIR",
                        tab = ScreenTab.LIVE,
                        isFeaturedGold = true
                    ),
                    LuxuryPortalItem(
                        icon = Icons.Default.NotificationsActive,
                        title = MeskotStrings.get("navNotifs", currentLanguage),
                        subtitle = "Alerts & Activity",
                        badgeText = if (unreadNotifsCount > 0) "$unreadNotifsCount" else null,
                        tab = ScreenTab.NOTIFICATIONS,
                        isFeaturedGold = false
                    ),
                    LuxuryPortalItem(
                        icon = Icons.Default.Insights,
                        title = "Dashboard",
                        subtitle = "Audience & Reach Analytics",
                        badgeText = "PRO",
                        tab = ScreenTab.DASHBOARD,
                        isFeaturedGold = false
                    ),
                    LuxuryPortalItem(
                        icon = Icons.Default.BookmarkAdded,
                        title = MeskotStrings.get("savedPosts", currentLanguage),
                        subtitle = "${savedPostIds.size} Curated Bookmarks",
                        badgeText = null,
                        tab = ScreenTab.SAVED,
                        isFeaturedGold = false
                    ),
                    LuxuryPortalItem(
                        icon = Icons.Default.VerifiedUser,
                        title = "Ads & Promotions",
                        subtitle = "Boost Meskot Content",
                        badgeText = "GOLD",
                        tab = ScreenTab.ADS_MANAGER,
                        isFeaturedGold = true
                    )
                )

                communityItems.chunked(2).forEachIndexed { index, rowPair ->
                    if (index > 0) {
                        Spacer(modifier = Modifier.height(10.dp))
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        rowPair.forEach { portal ->
                            LuxuryPortalShortcutCard(
                                item = portal,
                                onClick = { viewModel.navigateTo(portal.tab) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                        if (rowPair.size == 1) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }

        // Admin Panel if Admin
        if (currentUser?.isAdmin == true) {
            item {
                MenuShortcutCard(
                    emoji = "🛡️",
                    title = MeskotStrings.get("adminPanel", currentLanguage),
                    onClick = { viewModel.navigateTo(ScreenTab.ADMIN) },
                    modifier = Modifier.fillMaxWidth(),
                    isBrandFeatured = true
                )
            }
        }

        // 5. LUXURY ROYAL SEAL FOOTER
        item {
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                shape = RoundedCornerShape(20.dp),
                color = GoldSurface.copy(alpha = 0.85f),
                border = androidx.compose.foundation.BorderStroke(1.dp, GoldBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp, horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Decorative Ethiopian Tibeb Diamond Divider
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(1.dp)
                                .background(GoldBorder)
                        )
                        Text(
                            text = "❖",
                            fontSize = 12.sp,
                            color = GoldDeep
                        )
                        MeskotLogoBadge(size = 44.dp)
                        Text(
                            text = "❖",
                            fontSize = 12.sp,
                            color = GoldDeep
                        )
                        Box(
                            modifier = Modifier
                                .width(36.dp)
                                .height(1.dp)
                                .background(GoldBorder)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "MESKOT ROYAL CONCIERGE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = GoldDeep,
                        letterSpacing = 1.2.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "መስኮት · Premier Ethiopian & Habesha Global Network\nEdition 2.4 · Crafted with Heritage & Gold",
                        fontSize = 11.sp,
                        color = MutedText,
                        textAlign = TextAlign.Center,
                        lineHeight = 16.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showMetaVerifiedModal) {
        MetaVerifiedBottomSheetModal(
            currentUser = currentUser,
            onDismiss = { showMetaVerifiedModal = false },
            onSubscribeConfirmed = { paymentMethod, planId ->
                viewModel.subscribeMetaVerified(paymentMethod, planId)
            },
            onCancelSubscription = {
                viewModel.cancelMetaVerified()
            }
        )
    }
}

private data class LuxuryPortalItem(
    val icon: ImageVector,
    val title: String,
    val subtitle: String,
    val badgeText: String?,
    val tab: ScreenTab,
    val isFeaturedGold: Boolean
)

@Composable
private fun LuxuryPassportMetricPill(
    label: String,
    value: String,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(horizontal = 6.dp, vertical = 2.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = GoldBorder,
            letterSpacing = 0.7.sp
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color.White
        )
    }
}

@Composable
private fun LuxuryMenuSectionHeader(
    eyebrow: String,
    title: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = eyebrow,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                color = GoldDeep,
                letterSpacing = 0.9.sp
            )
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Ink
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(GoldSurface)
                .border(1.dp, GoldBorder, RoundedCornerShape(10.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = "✦ मेስኮት",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = GoldDeep
            )
        }
    }
}

@Composable
private fun LuxuryExecutivePillarCard(
    icon: ImageVector,
    title: String,
    amharicTag: String,
    metricBadge: String,
    isDarkObsidian: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgBrush = if (isDarkObsidian) {
        Brush.verticalGradient(listOf(Color(0xFF14221A), Color(0xFF21362B)))
    } else {
        Brush.verticalGradient(listOf(Color(0xFFFFFDF8), Color(0xFFF9EFE0)))
    }

    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(18.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isDarkObsidian) 5.dp else 2.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.3.dp,
            color = if (isDarkObsidian) GoldLight.copy(alpha = 0.75f) else GoldBorder
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(bgBrush)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(MeskotLogoHorizontalBrush)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(
                            if (isDarkObsidian) MeskotLogoBrush
                            else Brush.linearGradient(listOf(GoldSurface, Color(0xFFF3E2C3)))
                        )
                        .border(
                            1.dp,
                            if (isDarkObsidian) GoldLight else Gold,
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = if (isDarkObsidian) Color.White else GoldDeep,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = title,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isDarkObsidian) Color.White else Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = amharicTag,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (isDarkObsidian) GoldLight else GoldDeep
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDarkObsidian) Gold.copy(alpha = 0.25f) else GoldSurface,
                    border = androidx.compose.foundation.BorderStroke(
                        0.7.dp,
                        if (isDarkObsidian) GoldLight.copy(alpha = 0.5f) else GoldBorder
                    )
                ) {
                    Text(
                        text = metricBadge,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isDarkObsidian) GoldLight else GoldDeep,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                        maxLines = 1
                    )
                }
            }
        }
    }
}

@Composable
private fun LuxuryPortalShortcutCard(
    item: LuxuryPortalItem,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardBgBrush = if (item.isFeaturedGold) {
        Brush.linearGradient(listOf(Color(0xFFFFFBF2), Color(0xFFF9EDD6)))
    } else {
        Brush.linearGradient(listOf(CardBg, Color(0xFFFAF6EE)))
    }

    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (item.isFeaturedGold) 3.dp else 1.5.dp),
        border = androidx.compose.foundation.BorderStroke(
            width = if (item.isFeaturedGold) 1.4.dp else 1.dp,
            color = if (item.isFeaturedGold) Gold else GoldBorder.copy(alpha = 0.75f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardBgBrush)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(2.5.dp)
                    .background(
                        if (item.isFeaturedGold) {
                            MeskotLogoHorizontalBrush
                        } else {
                            Brush.horizontalGradient(listOf(GoldBorder.copy(alpha = 0.5f), GoldLight.copy(alpha = 0.5f)))
                        }
                    )
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Luxury Arched Medallion Icon Container
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 10.dp, bottomEnd = 10.dp))
                            .background(
                                if (item.isFeaturedGold) MeskotLogoBrush
                                else Brush.linearGradient(listOf(GoldSurface, Color(0xFFF5E6C8)))
                            )
                            .border(
                                width = 1.dp,
                                color = if (item.isFeaturedGold) GoldLight else GoldBorder,
                                shape = RoundedCornerShape(topStart = 14.dp, topEnd = 14.dp, bottomStart = 10.dp, bottomEnd = 10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = item.title,
                            tint = if (item.isFeaturedGold) Color.White else GoldDeep,
                            modifier = Modifier.size(19.dp)
                        )
                    }

                    if (item.badgeText != null) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = if (item.badgeText == "ON AIR") CrossRed else GoldSurface,
                            border = androidx.compose.foundation.BorderStroke(
                                0.8.dp,
                                if (item.badgeText == "ON AIR") CrossRed else GoldBorder
                            )
                        ) {
                            Text(
                                text = item.badgeText,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (item.badgeText == "ON AIR") Color.White else GoldDeep,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = GoldDeep.copy(alpha = 0.55f),
                            modifier = Modifier.size(11.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Text(
                    text = item.title,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (item.isFeaturedGold) GoldDeep else Ink,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = item.subtitle,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = MutedText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
fun MenuShortcutCard(
    emoji: String,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isBrandFeatured: Boolean = false
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isBrandFeatured) com.example.ui.theme.GoldSurface else CardBg
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isBrandFeatured) 1.4.dp else 1.dp,
            color = if (isBrandFeatured) com.example.ui.theme.Gold else com.example.ui.theme.GoldBorder
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(com.example.ui.theme.MeskotLogoHorizontalBrush)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(com.example.ui.theme.GoldSurface)
                            .border(1.dp, com.example.ui.theme.GoldBorder, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 17.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = title,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = if (isBrandFeatured) com.example.ui.theme.GoldDeep else Ink
                    )
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                    contentDescription = null,
                    tint = com.example.ui.theme.GoldDeep,
                    modifier = Modifier.size(13.dp)
                )
            }
        }
    }
}

enum class AuthMode {
    SIGN_IN,
    SIGN_UP,
    FORGOT_PASSWORD
}

@Composable
fun AuthScreen(
    viewModel: MeskotViewModel,
    allUsers: List<User> = emptyList(),
    currentLanguage: AppLanguage
) {
    var authMode by remember { mutableStateOf(AuthMode.SIGN_IN) }

    // Sign in state
    var signInEmail by remember { mutableStateOf("") }
    var signInPassword by remember { mutableStateOf("") }
    var isSignInPasswordVisible by remember { mutableStateOf(false) }

    // Facebook-style Sign up state
    var firstName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var signUpEmail by remember { mutableStateOf("") }
    var signUpPassword by remember { mutableStateOf("") }
    var signUpConfirmPassword by remember { mutableStateOf("") }
    var isSignUpPasswordVisible by remember { mutableStateOf(false) }
    var isSignUpConfirmPasswordVisible by remember { mutableStateOf(false) }

    // Birthday state
    val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    var selectedMonth by remember { mutableStateOf("Jan") }
    var isMonthMenuExpanded by remember { mutableStateOf(false) }

    val days = (1..31).map { it.toString() }
    var selectedDay by remember { mutableStateOf("1") }
    var isDayMenuExpanded by remember { mutableStateOf(false) }

    val years = (2012 downTo 1940).map { it.toString() }
    var selectedYear by remember { mutableStateOf("2000") }
    var isYearMenuExpanded by remember { mutableStateOf(false) }

    // Gender state (Facebook style: Female, Male, Custom)
    var selectedGender by remember { mutableStateOf("Female") }
    var customGenderDescription by remember { mutableStateOf("") }

    // Forgot password state
    var forgotEmail by remember { mutableStateOf("") }
    var resetSuccessMessage by remember { mutableStateOf<String?>(null) }

    // Common feedback state
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 28.dp)
            .testTag("auth_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Modern Meskot Hero Emblem
        MeskotHeroEmblem(size = 80.dp)

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Meskot",
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Serif,
            color = Ink,
            letterSpacing = (-0.5).sp
        )
        Text(
            text = "መስኮት · Ethiopian & Habesha Community Network",
            fontSize = 12.sp,
            color = MutedText,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = CardBg),
            border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                when (authMode) {
                    AuthMode.SIGN_IN -> {
                        // Tab switcher (Sign in vs Register)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(com.example.ui.theme.GoldSurface)
                                .border(1.dp, com.example.ui.theme.GoldBorder, RoundedCornerShape(12.dp))
                                .padding(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(com.example.ui.theme.MeskotLogoBrush)
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = MeskotStrings.get("signIn", currentLanguage),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = Color.White
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(9.dp))
                                    .background(Color.Transparent)
                                    .clickable {
                                        authMode = AuthMode.SIGN_UP
                                        errorMessage = null
                                    }
                                    .padding(vertical = 9.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = MeskotStrings.get("createAccount", currentLanguage),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = com.example.ui.theme.GoldDeep
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = signInEmail,
                            onValueChange = { signInEmail = it },
                            label = { Text(MeskotStrings.get("email", currentLanguage)) },
                            placeholder = { Text("email@example.com") },
                            textStyle = TextStyle(color = Ink, fontSize = 14.sp),
                            colors = meskotTextFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            enabled = !isLoading
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        OutlinedTextField(
                            value = signInPassword,
                            onValueChange = { signInPassword = it },
                            label = { Text(MeskotStrings.get("password", currentLanguage)) },
                            textStyle = TextStyle(color = Ink, fontSize = 14.sp),
                            colors = meskotTextFieldColors(),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            singleLine = true,
                            enabled = !isLoading,
                            visualTransformation = if (isSignInPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { isSignInPasswordVisible = !isSignInPasswordVisible }) {
                                    Icon(
                                        imageVector = if (isSignInPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                        contentDescription = null,
                                        tint = MutedText
                                    )
                                }
                            }
                        )

                        // Facebook-style "Forgot password?" Link
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Text(
                                text = MeskotStrings.get("forgotPassword", currentLanguage),
                                color = GoldDeep,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier
                                    .clickable {
                                        authMode = AuthMode.FORGOT_PASSWORD
                                        forgotEmail = signInEmail.trim()
                                        errorMessage = null
                                        resetSuccessMessage = null
                                    }
                                    .padding(vertical = 6.dp)
                            )
                        }

                        if (errorMessage != null) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = errorMessage!!, color = CrossRed, fontSize = 12.sp)
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Button(
                            onClick = {
                                errorMessage = null
                                if (signInEmail.isBlank() || signInPassword.isBlank()) {
                                    errorMessage = "Please enter email and password"
                                } else {
                                    isLoading = true
                                    viewModel.login(signInEmail, signInPassword) { ok, err ->
                                        isLoading = false
                                        if (!ok) {
                                            errorMessage = err ?: "Invalid email or password"
                                        }
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.White),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = MeskotStrings.get("signIn", currentLanguage),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            HorizontalDivider(modifier = Modifier.weight(1f), color = LineBorder)
                            Text(
                                text = "  or  ",
                                fontSize = 12.sp,
                                color = MutedText
                            )
                            HorizontalDivider(modifier = Modifier.weight(1f), color = LineBorder)
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Facebook-style "Create new account" Green Button
                        Button(
                            onClick = {
                                authMode = AuthMode.SIGN_UP
                                errorMessage = null
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ActiveGreen, contentColor = Color.White),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                        ) {
                            Text(
                                text = MeskotStrings.get("createAccount", currentLanguage),
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    AuthMode.FORGOT_PASSWORD -> {
                        // Facebook-style "Find Your Account" Recovery
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(Gold.copy(alpha = 0.15f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = GoldDeep,
                                    modifier = Modifier.size(26.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = MeskotStrings.get("findYourAccount", currentLanguage),
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = MeskotStrings.get("resetPasswordInstructions", currentLanguage),
                                fontSize = 13.sp,
                                color = MutedText,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (resetSuccessMessage != null) {
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = ActiveGreen.copy(alpha = 0.12f),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, ActiveGreen.copy(alpha = 0.3f)),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = ActiveGreen,
                                            modifier = Modifier.size(22.dp)
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = resetSuccessMessage!!,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = Ink
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "Please check your inbox (and spam folder) for the password reset link.",
                                                fontSize = 12.sp,
                                                color = MutedText
                                            )
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = {
                                        authMode = AuthMode.SIGN_IN
                                        errorMessage = null
                                        resetSuccessMessage = null
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = Ink, contentColor = Color.White),
                                    shape = RoundedCornerShape(10.dp),
                                    modifier = Modifier.fillMaxWidth().height(46.dp)
                                ) {
                                    Text(
                                        text = MeskotStrings.get("backToLogin", currentLanguage),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
                                OutlinedTextField(
                                    value = forgotEmail,
                                    onValueChange = { forgotEmail = it },
                                    label = { Text(MeskotStrings.get("email", currentLanguage)) },
                                    placeholder = { Text("email@example.com") },
                                    textStyle = TextStyle(color = Ink, fontSize = 14.sp),
                                    colors = meskotTextFieldColors(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    enabled = !isLoading
                                )

                                if (errorMessage != null) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(text = errorMessage!!, color = CrossRed, fontSize = 12.sp)
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    OutlinedButton(
                                        onClick = {
                                            authMode = AuthMode.SIGN_IN
                                            errorMessage = null
                                        },
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f).height(46.dp),
                                        enabled = !isLoading
                                    ) {
                                        Text(
                                            text = MeskotStrings.get("cancel", currentLanguage),
                                            color = MutedText
                                        )
                                    }

                                    Button(
                                        onClick = {
                                            errorMessage = null
                                            if (forgotEmail.isBlank() || !forgotEmail.contains("@")) {
                                                errorMessage = "Please enter a valid email address"
                                            } else {
                                                isLoading = true
                                                viewModel.forgotPassword(forgotEmail) { ok, err ->
                                                    isLoading = false
                                                    if (ok) {
                                                        resetSuccessMessage = MeskotStrings.get("resetEmailSent", currentLanguage)
                                                        errorMessage = null
                                                    } else {
                                                        errorMessage = err ?: "Could not send reset email"
                                                    }
                                                }
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.White),
                                        shape = RoundedCornerShape(10.dp),
                                        modifier = Modifier.weight(1f).height(46.dp),
                                        enabled = !isLoading
                                    ) {
                                        if (isLoading) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = Color.White,
                                                strokeWidth = 2.dp
                                            )
                                        } else {
                                            Text(
                                                text = MeskotStrings.get("sendResetLink", currentLanguage),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AuthMode.SIGN_UP -> {
                        // Facebook-style Create Account Registration
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = MeskotStrings.get("joinTitle", currentLanguage),
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            Text(
                                text = MeskotStrings.get("joinSub", currentLanguage),
                                fontSize = 12.sp,
                                color = MutedText
                            )

                            Spacer(modifier = Modifier.height(14.dp))

                            // Side-by-side First name & Last name (Facebook style)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = firstName,
                                    onValueChange = { firstName = it },
                                    label = { Text(MeskotStrings.get("firstName", currentLanguage)) },
                                    textStyle = TextStyle(color = Ink, fontSize = 14.sp),
                                    colors = meskotTextFieldColors(),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    enabled = !isLoading
                                )
                                OutlinedTextField(
                                    value = lastName,
                                    onValueChange = { lastName = it },
                                    label = { Text(MeskotStrings.get("lastName", currentLanguage)) },
                                    textStyle = TextStyle(color = Ink, fontSize = 14.sp),
                                    colors = meskotTextFieldColors(),
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(10.dp),
                                    singleLine = true,
                                    enabled = !isLoading
                                )
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Email / Mobile
                            OutlinedTextField(
                                value = signUpEmail,
                                onValueChange = { signUpEmail = it },
                                label = { Text(MeskotStrings.get("email", currentLanguage)) },
                                placeholder = { Text("email@example.com") },
                                textStyle = TextStyle(color = Ink, fontSize = 14.sp),
                                colors = meskotTextFieldColors(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                enabled = !isLoading
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // New Password
                            OutlinedTextField(
                                value = signUpPassword,
                                onValueChange = {
                                    signUpPassword = it
                                    if (errorMessage != null) errorMessage = null
                                },
                                label = { Text(MeskotStrings.get("password", currentLanguage)) },
                                textStyle = TextStyle(color = Ink, fontSize = 14.sp),
                                colors = meskotTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_password_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                enabled = !isLoading,
                                visualTransformation = if (isSignUpPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { isSignUpPasswordVisible = !isSignUpPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isSignUpPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = "Toggle password visibility",
                                            tint = MutedText
                                        )
                                    }
                                }
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            // Confirm Password
                            val passwordsMatch = signUpConfirmPassword.isNotEmpty() && signUpPassword == signUpConfirmPassword
                            val passwordsMismatch = signUpConfirmPassword.isNotEmpty() && signUpPassword != signUpConfirmPassword

                            OutlinedTextField(
                                value = signUpConfirmPassword,
                                onValueChange = {
                                    signUpConfirmPassword = it
                                    if (errorMessage != null) errorMessage = null
                                },
                                label = { Text(MeskotStrings.get("confirmPassword", currentLanguage)) },
                                textStyle = TextStyle(color = Ink, fontSize = 14.sp),
                                isError = passwordsMismatch,
                                colors = meskotTextFieldColors(),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("signup_confirm_password_input"),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                enabled = !isLoading,
                                visualTransformation = if (isSignUpConfirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.padding(end = 4.dp)
                                    ) {
                                        if (signUpConfirmPassword.isNotEmpty()) {
                                            Icon(
                                                imageVector = if (passwordsMatch) Icons.Default.CheckCircle else Icons.Default.Info,
                                                contentDescription = if (passwordsMatch) "Passwords match" else "Passwords do not match",
                                                tint = if (passwordsMatch) ActiveGreen else CrossRed,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        IconButton(onClick = { isSignUpConfirmPasswordVisible = !isSignUpConfirmPasswordVisible }) {
                                            Icon(
                                                imageVector = if (isSignUpConfirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                                contentDescription = "Toggle confirm password visibility",
                                                tint = MutedText
                                            )
                                        }
                                    }
                                }
                            )

                            if (signUpConfirmPassword.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(5.dp),
                                    modifier = Modifier.padding(horizontal = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = if (passwordsMatch) Icons.Default.CheckCircle else Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (passwordsMatch) ActiveGreen else CrossRed,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = if (passwordsMatch) {
                                            MeskotStrings.get("passwordsMatch", currentLanguage)
                                        } else {
                                            MeskotStrings.get("passwordsDoNotMatch", currentLanguage)
                                        },
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (passwordsMatch) ActiveGreen else CrossRed
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Facebook-style Birthday Selection
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = MeskotStrings.get("birthday", currentLanguage),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Ink
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = null,
                                    tint = MutedText,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            Text(
                                text = MeskotStrings.get("birthdaySub", currentLanguage),
                                fontSize = 11.sp,
                                color = MutedText
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            // 3 Dropdown Pickers: Month, Day, Year
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Month Selector
                                Box(modifier = Modifier.weight(1.1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
                                        color = Paper2,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { isMonthMenuExpanded = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = selectedMonth, fontSize = 13.sp, color = Ink, fontWeight = FontWeight.Medium)
                                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Ink)
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = isMonthMenuExpanded,
                                        onDismissRequest = { isMonthMenuExpanded = false }
                                    ) {
                                        months.forEach { m ->
                                            DropdownMenuItem(
                                                text = { Text(m) },
                                                onClick = {
                                                    selectedMonth = m
                                                    isMonthMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Day Selector
                                Box(modifier = Modifier.weight(0.9f)) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
                                        color = Paper2,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { isDayMenuExpanded = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = selectedDay, fontSize = 13.sp, color = Ink, fontWeight = FontWeight.Medium)
                                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Ink)
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = isDayMenuExpanded,
                                        onDismissRequest = { isDayMenuExpanded = false }
                                    ) {
                                        days.forEach { d ->
                                            DropdownMenuItem(
                                                text = { Text(d) },
                                                onClick = {
                                                    selectedDay = d
                                                    isDayMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }

                                // Year Selector
                                Box(modifier = Modifier.weight(1.1f)) {
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
                                        color = Paper2,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable { isYearMenuExpanded = true }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 10.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = selectedYear, fontSize = 13.sp, color = Ink, fontWeight = FontWeight.Medium)
                                            Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null, tint = Ink)
                                        }
                                    }
                                    DropdownMenu(
                                        expanded = isYearMenuExpanded,
                                        onDismissRequest = { isYearMenuExpanded = false }
                                    ) {
                                        years.forEach { y ->
                                            DropdownMenuItem(
                                                text = { Text(y) },
                                                onClick = {
                                                    selectedYear = y
                                                    isYearMenuExpanded = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Facebook-style Gender Selection (Female, Male, Custom)
                            Text(
                                text = MeskotStrings.get("gender", currentLanguage),
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Ink
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf("Female", "Male", "Custom").forEach { g ->
                                    val isSelected = selectedGender == g
                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        border = androidx.compose.foundation.BorderStroke(
                                            1.dp,
                                            if (isSelected) Ink else LineBorder
                                        ),
                                        color = if (isSelected) Paper2 else Color.Transparent,
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable { selectedGender = g }
                                    ) {
                                        Row(
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = when (g) {
                                                    "Female" -> MeskotStrings.get("female", currentLanguage)
                                                    "Male" -> MeskotStrings.get("male", currentLanguage)
                                                    else -> MeskotStrings.get("custom", currentLanguage)
                                                },
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Ink
                                            )
                                            RadioButton(
                                                selected = isSelected,
                                                onClick = { selectedGender = g },
                                                colors = RadioButtonDefaults.colors(
                                                    selectedColor = Ink,
                                                    unselectedColor = MutedText
                                                )
                                            )
                                        }
                                    }
                                }
                            }

                            // Custom Gender / Pronoun description
                            if (selectedGender == "Custom") {
                                Spacer(modifier = Modifier.height(8.dp))
                                OutlinedTextField(
                                    value = customGenderDescription,
                                    onValueChange = { customGenderDescription = it },
                                    label = { Text(MeskotStrings.get("customGenderPrompt", currentLanguage)) },
                                    placeholder = { Text("e.g. She/Her, He/Him, They/Them") },
                                    textStyle = TextStyle(color = Ink, fontSize = 14.sp),
                                    colors = meskotTextFieldColors(),
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = RoundedCornerShape(8.dp),
                                    singleLine = true,
                                    enabled = !isLoading
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Facebook-style Terms Notice
                            Text(
                                text = MeskotStrings.get("termsAgreement", currentLanguage),
                                fontSize = 11.sp,
                                color = MutedText,
                                lineHeight = 15.sp
                            )

                            if (errorMessage != null) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = errorMessage!!, color = CrossRed, fontSize = 12.sp)
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            // Facebook-style Sign Up Button
                            Button(
                                onClick = {
                                    errorMessage = null
                                    val full = "${firstName.trim()} ${lastName.trim()}".trim()
                                    if (firstName.isBlank() || lastName.isBlank()) {
                                        errorMessage = "Please enter your first and last name"
                                    } else if (signUpEmail.isBlank() || !signUpEmail.contains("@")) {
                                        errorMessage = "Please enter a valid email address"
                                    } else if (signUpPassword.length < 6) {
                                        errorMessage = "Password must be at least 6 characters"
                                    } else if (signUpConfirmPassword.isBlank()) {
                                        errorMessage = "Please confirm your password"
                                    } else if (signUpPassword != signUpConfirmPassword) {
                                        errorMessage = MeskotStrings.get("passwordsDoNotMatch", currentLanguage)
                                    } else {
                                        isLoading = true
                                        val finalGender = if (selectedGender == "Custom" && customGenderDescription.isNotBlank()) {
                                            customGenderDescription.trim()
                                        } else {
                                            selectedGender
                                        }
                                        val birthDate = "$selectedMonth $selectedDay, $selectedYear"
                                        viewModel.signup(
                                            fullName = full,
                                            email = signUpEmail,
                                            pass = signUpPassword,
                                            gender = finalGender,
                                            birthDate = birthDate,
                                            phoneNumber = ""
                                        ) { ok, err ->
                                            isLoading = false
                                            if (!ok) {
                                                errorMessage = err ?: "Failed to create account"
                                            }
                                        }
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.White),
                                shape = RoundedCornerShape(12.dp),
                                enabled = !isLoading,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                            ) {
                                if (isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color.White,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = MeskotStrings.get("createAccount", currentLanguage),
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // "Already have an account? Log in"
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = MeskotStrings.get("hasAccount", currentLanguage) + " ",
                                    fontSize = 13.sp,
                                    color = MutedText
                                )
                                Text(
                                    text = MeskotStrings.get("logInLink", currentLanguage),
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = GoldDeep,
                                    modifier = Modifier.clickable {
                                        authMode = AuthMode.SIGN_IN
                                        errorMessage = null
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
