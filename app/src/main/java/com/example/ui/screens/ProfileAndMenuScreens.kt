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

    // Dynamic friends list populated from real Firestore users
    val displayFriends = remember(allUsers, user.uid) {
        allUsers.filter { it.uid != user.uid }.take(4)
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
        // TOP BAR: Modern Luxury Meskot Profile Navigation Bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color.White.copy(alpha = 0.98f),
            shadowElevation = 3.dp
        ) {
            Column {
                // Top micro luxury gold line
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(2.5.dp)
                        .background(
                            Brush.horizontalGradient(
                                listOf(GoldLight, Gold, GoldDeep, Gold, GoldLight)
                            )
                        )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldSurface)
                            .border(1.dp, GoldBorder.copy(alpha = 0.7f), CircleShape)
                            .clickable { viewModel.navigateTo(ScreenTab.FEED) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Ink,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    // User name with gold badge and dropdown
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(GoldSurface)
                            .border(1.dp, GoldBorder.copy(alpha = 0.7f), RoundedCornerShape(12.dp))
                            .clickable { isAccountMenuOpen = !isAccountMenuOpen }
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (user.displayName.length > 14) user.displayName.take(13) + "…" else user.displayName,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .size(18.dp)
                                .clip(CircleShape)
                                .background(CrossRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "1",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Dropdown",
                            tint = GoldDeep,
                            modifier = Modifier.size(18.dp)
                        )

                        DropdownMenu(
                            expanded = isAccountMenuOpen,
                            onDismissRequest = { isAccountMenuOpen = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text(user.displayName, fontWeight = FontWeight.Bold) },
                                onClick = { isAccountMenuOpen = false }
                            )
                            if (isMe) {
                                DropdownMenuItem(
                                    text = { Text("Switch Profile") },
                                    onClick = {
                                        isAccountMenuOpen = false
                                        viewModel.showMessage("Switch profile options")
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
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // Edit Profile Icon Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldSurface)
                            .border(1.dp, GoldBorder.copy(alpha = 0.6f), CircleShape)
                            .clickable {
                                if (isMe) viewModel.openEditProfile() else isSeeMoreDetailsOpen = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Profile",
                            tint = GoldDeep,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Search In Profile Icon Button
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldSurface)
                            .border(1.dp, GoldBorder.copy(alpha = 0.6f), CircleShape)
                            .clickable { isSearchOpen = !isSearchOpen },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Profile",
                            tint = GoldDeep,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // More Options Icon Button (...)
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(GoldSurface)
                            .border(1.dp, GoldBorder.copy(alpha = 0.6f), CircleShape)
                            .clickable { isMoreOptionsOpen = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More Options",
                            tint = GoldDeep,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // Micro hairline divider
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(0.5.dp)
                        .background(LineBorder.copy(alpha = 0.5f))
                )

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
                            placeholder = { Text("Search posts & details…", fontSize = 13.sp) },
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
            // COVER PHOTO AND OVERLAPPING PROFILE AVATAR
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(245.dp)
                ) {
                    // Cover Banner Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(175.dp)
                            .background(com.example.ui.theme.MeskotLogoHorizontalBrush)
                    ) {
                        if (user.coverPhotoUrl.isNotBlank()) {
                            AsyncImage(
                                model = user.coverPhotoUrl,
                                contentDescription = "Cover Photo",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            // Meskot logo brand watermark in cover
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                MeskotLogoBadge(size = 56.dp, showBorder = true, elevation = 4.dp)
                            }
                        }

                        // Camera Icon Button on Cover Photo (Bottom Right)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(10.dp)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.95f))
                                .border(1.dp, fbLightGray, CircleShape)
                                .clickable {
                                    if (isMe) viewModel.openEditProfile() else viewModel.showMessage("Cover photo")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Change Cover Photo",
                                tint = fbDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Large Profile Avatar Overlapping Cover Photo
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .size(126.dp)
                    ) {
                        UserAvatar(
                            photoUrl = user.photoUrl,
                            name = user.displayName,
                            size = 126,
                            modifier = Modifier
                                .size(126.dp)
                                .border(4.dp, Color.White, CircleShape)
                        )

                        // Camera Icon Button on Avatar (Bottom Right)
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .offset(x = (-2).dp, y = (-2).dp)
                                .size(34.dp)
                                .clip(CircleShape)
                                .background(fbLightGray)
                                .border(2.dp, Color.White, CircleShape)
                                .clickable {
                                    if (isMe) viewModel.openEditProfile() else viewModel.showMessage("Profile photo")
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.PhotoCamera,
                                contentDescription = "Change Profile Photo",
                                tint = fbDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // NAME, STATS, BIO AND QUICK BADGES
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Full Display Name
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        ProfileName(
                            name = user.displayName,
                            isVerified = user.isVerified,
                            isAdmin = user.isAdmin,
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = fbDark,
                            badgeSize = 20.dp
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Stats: Followers · Following · Posts (Real Meskot metrics)
                    val realFollowers = if (user.followersCount == 8500) {
                        if (isMe) friendUids.size else 0
                    } else if (user.followersCount > 0) {
                        user.followersCount
                    } else {
                        if (isMe) friendUids.size else 0
                    }
                    val realFollowing = if (user.followingCount == 3700 || user.followingCount == 370) {
                        if (isMe) friendUids.size else 0
                    } else if (user.followingCount > 0) {
                        user.followingCount
                    } else {
                        if (isMe) friendUids.size else 0
                    }
                    val followersFormatted = formatStats(realFollowers)
                    val followingFormatted = formatStats(realFollowing)
                    val postsCount = userPosts.size
                    val postsWord = if (postsCount == 1) "post" else "posts"
                    val followersWord = if (realFollowers == 1) "follower" else "followers"
                    Text(
                        text = "$followersFormatted $followersWord · $followingFormatted following · $postsCount $postsWord",
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = fbTextGray
                    )

                    // Bio Text (only show if set)
                    val cleanBio = if (user.bio.trim().equals("Engineer is a problem solver", ignoreCase = true)) "" else user.bio.trim()
                    if (cleanBio.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = cleanBio,
                            fontSize = 14.5.sp,
                            color = fbDark,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 18.dp)
                        )
                    }

                    // Quick Meta line: Profession · City · Alma Mater (only show real entries)
                    val cleanProfession = if (user.profession.trim().equals("Public figure", ignoreCase = true)) "" else user.profession.trim()
                    val cleanLocation = user.location.trim()
                    val cleanEdu = if (user.education.trim().equals("Adigrat University", ignoreCase = true)) "" else user.education.trim()

                    val metaList = mutableListOf<String>()
                    if (cleanProfession.isNotBlank()) metaList.add("💼 $cleanProfession")
                    if (cleanLocation.isNotBlank()) metaList.add("📍 $cleanLocation")
                    if (cleanEdu.isNotBlank()) metaList.add("🏛️ $cleanEdu")

                    if (metaList.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = metaList.joinToString(" · "),
                            fontSize = 12.5.sp,
                            color = fbTextGray,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 18.dp)
                        )
                    }
                }
            }

            // PRIMARY ACTION BUTTONS
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isMe) {
                        // Blue "Dashboard" Button
                        Button(
                            onClick = { viewModel.navigateTo(ScreenTab.DASHBOARD) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = fbBlue,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Assessment,
                                contentDescription = "Dashboard",
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Dashboard",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Gray "Add to story" Button
                        Button(
                            onClick = { viewModel.openComposer() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = fbLightGray,
                                contentColor = fbDark
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add to story",
                                modifier = Modifier.size(18.dp),
                                tint = fbDark
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Add to story",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = fbDark
                            )
                        }

                        // Gray 3-Dots Options Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
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
                    } else {
                        // Viewing other profile: Friend/Add Friend Button
                        Button(
                            onClick = {
                                if (isFriend) viewModel.unfriend(user) else viewModel.sendFriendRequest(user)
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFriend) fbLightGray else fbBlue,
                                contentColor = if (isFriend) fbDark else Color.White
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Text(
                                text = if (isFriend) "✓ Friends" else "+ Add friend",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        // Message Button
                        Button(
                            onClick = { viewModel.openChat(user) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = fbLightGray,
                                contentColor = fbDark
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Chat,
                                contentDescription = "Message",
                                modifier = Modifier.size(16.dp),
                                tint = fbDark
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Message",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Bold,
                                color = fbDark
                            )
                        }

                        // VIP Fan Club Button
                        OutlinedButton(
                            onClick = { viewModel.openSubscriptionModal(user) },
                            shape = RoundedCornerShape(8.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, GoldDeep),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text(
                                text = "👑 VIP",
                                color = GoldDeep,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }

                        // 3-Dots Button
                        Box(
                            modifier = Modifier
                                .size(40.dp)
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
            if (isMe) {
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
                                        text = "${user.starBalance} Stars Balance",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = fbDark
                                    )
                                    Text(
                                        text = "Send tips & support creators via Chapa",
                                        fontSize = 11.sp,
                                        color = fbTextGray
                                    )
                                }
                            }

                            Button(
                                onClick = { viewModel.buyStarsViaChapa(500, 200.0) },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(8.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "+ Top Up (Chapa)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
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

            // PERSONAL DETAILS SECTION (Facebook style, only shown in 'All' tab)
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
                        if (isMe) {
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

                    val cleanLocation = user.location.trim()
                    val cleanHometown = user.hometown.trim()
                    val cleanBirthDate = user.birthDate.trim()
                    val cleanGender = user.gender.trim()

                    // 1. Current City / Location (Facebook style: "Lives in <City>")
                    if (cleanLocation.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isMe) viewModel.openEditProfile() else isSeeMoreDetailsOpen = true
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
                                text = buildAnnotatedString {
                                    append("Lives in ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = fbDark)) {
                                        append(cleanLocation)
                                    }
                                },
                                fontSize = 14.5.sp,
                                color = fbDark
                            )
                        }
                    } else if (isMe) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openEditProfile() }
                                .padding(vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Home,
                                contentDescription = "Add current city",
                                tint = fbBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "+ Add current city",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = fbBlue
                            )
                        }
                    }

                    // 2. Hometown (Facebook style: "From <Hometown>")
                    if (cleanHometown.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (isMe) viewModel.openEditProfile() else isSeeMoreDetailsOpen = true
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
                                text = buildAnnotatedString {
                                    append("From ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = fbDark)) {
                                        append(cleanHometown)
                                    }
                                },
                                fontSize = 14.5.sp,
                                color = fbDark
                            )
                        }
                    } else if (isMe) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { viewModel.openEditProfile() }
                                .padding(vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Add hometown",
                                tint = fbBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "+ Add hometown",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.Medium,
                                color = fbBlue
                            )
                        }
                    }

                    // 3. Birthday
                    if (cleanBirthDate.isNotBlank()) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
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
                                text = buildAnnotatedString {
                                    append("Born on ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = fbDark)) {
                                        append(cleanBirthDate)
                                    }
                                },
                                fontSize = 14.5.sp,
                                color = fbDark
                            )
                        }
                    }

                    // 4. Gender
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

                    // 5. Followers count (Facebook style)
                    if (user.followersCount > 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 5.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.RssFeed,
                                contentDescription = "Followers",
                                tint = fbTextGray,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = buildAnnotatedString {
                                    append("Followed by ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.Bold, color = fbDark)) {
                                        append("${user.followersCount} people")
                                    }
                                },
                                fontSize = 14.5.sp,
                                color = fbDark
                            )
                        }
                    }

                    // 6. See more details link
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { isSeeMoreDetailsOpen = true }
                            .padding(vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "See more",
                            tint = fbTextGray,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "See your About info",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = fbDark
                        )
                    }

                    // 7. Facebook-style "Edit public details" button for profile owner
                    if (isMe) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Button(
                            onClick = { viewModel.openEditProfile() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(38.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = fbLightGray,
                                contentColor = fbDark
                            ),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                        ) {
                            Text(
                                text = "Edit public details",
                                fontSize = 14.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = fbDark
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = fbLightGray, thickness = 1.dp)
                }
            }

            // WORK SECTION (Screenshot 2)
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
                            onClick = {
                                if (isMe) viewModel.openEditProfile() else isSeeMoreWorkOpen = true
                            },
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

                    val cleanWorkplace = user.workplace.trim()
                    val cleanWorkRole = user.workRole.trim()

                    if (cleanWorkplace.isBlank() && cleanWorkRole.isBlank()) {
                        Text(
                            text = if (isMe) "No work experience listed yet. Tap the edit icon to add your workplace." else "No workplace listed.",
                            fontSize = 13.5.sp,
                            color = fbTextGray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
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
                                if (cleanWorkplace.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = cleanWorkplace,
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = fbDark
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "🔒", fontSize = 12.sp)
                                    }
                                }
                                if (cleanWorkRole.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = cleanWorkRole,
                                        fontSize = 13.sp,
                                        color = fbTextGray
                                    )
                                }
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
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = fbLightGray, thickness = 1.dp)
                }
            }

            // EDUCATION SECTION (Screenshot 2)
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
                            onClick = {
                                if (isMe) viewModel.openEditProfile() else isSeeMoreDetailsOpen = true
                            },
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

                    val cleanEdu = user.education.trim()
                    val cleanEduClass = user.educationClass.trim()

                    if (cleanEdu.isBlank() && cleanEduClass.isBlank()) {
                        Text(
                            text = if (isMe) "No education info added yet. Tap the edit icon to add your school." else "No education info listed.",
                            fontSize = 13.5.sp,
                            color = fbTextGray,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
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
                                if (cleanEdu.isNotBlank()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = cleanEdu,
                                            fontSize = 14.5.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            color = fbDark
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(text = "🔒", fontSize = 12.sp)
                                    }
                                }
                                if (cleanEduClass.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = cleanEduClass,
                                        fontSize = 13.sp,
                                        color = fbTextGray
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider(color = fbLightGray, thickness = 1.dp)
                }
            }

            // FRIENDS SECTION (Screenshot 2)
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
                            text = "Friends",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = fbDark
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        TextButton(onClick = { viewModel.navigateTo(ScreenTab.FRIENDS) }) {
                            Text(
                                text = "See all",
                                color = fbBlue,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // 4 Friends matching screenshot with online indicators and mutual friend counts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        displayFriends.forEach { friend ->
                            val mutualText = when {
                                friendUids.contains(friend.uid) -> "Friend"
                                friend.location.isNotBlank() -> friend.location
                                friend.profession.isNotBlank() -> friend.profession
                                else -> "Member"
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
                                    // Green Online Indicator Dot
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

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = friend.displayName,
                                    fontSize = 12.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = fbDark,
                                    textAlign = TextAlign.Center,
                                    maxLines = 2
                                )

                                Spacer(modifier = Modifier.height(2.dp))

                                Text(
                                    text = mutualText,
                                    fontSize = 10.5.sp,
                                    color = fbTextGray,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    HorizontalDivider(color = fbLightGray, thickness = 1.dp)
                }
            }

            // POSTS SECTION (Screenshot 2)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "Posts",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = fbDark
                    )
                }
            }

            // "WHAT'S ON YOUR MIND?" COMPOSER BOX (Screenshot 2)
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

    // MORE OPTIONS DIALOG (Facebook Lite Style)
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

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                isMoreOptionsOpen = false
                                viewModel.showMessage("Profile link copied to clipboard")
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

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp)
            .testTag("menu_screen")
    ) {
        // User Profile Banner
        if (currentUser != null) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.openProfile(currentUser) },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = CardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UserAvatar(photoUrl = currentUser.photoUrl, name = currentUser.displayName, size = 52)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            ProfileName(
                                name = currentUser.displayName,
                                isVerified = currentUser.isVerified,
                                isAdmin = currentUser.isAdmin,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            Text(text = MeskotStrings.get("viewProfile", currentLanguage), fontSize = 12.sp, color = GoldDeep, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            // Dedicated Meskot Verified Shortcut in Menu
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showMetaVerifiedModal = true },
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (currentUser.isVerified) GoldSurface else CardBg
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (currentUser.isVerified) GoldBorder else LineBorder
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(Gold),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Meskot Verified",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Meskot Verified",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (currentUser.isVerified) GoldDeep else Ink
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(if (currentUser.isVerified) ActiveGreen else Gold)
                                        .padding(horizontal = 5.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = if (currentUser.isVerified) "SUBSCRIBED" else "NEW",
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                            Text(
                                text = if (currentUser.isVerified) "Active golden badge & account protection" else "Subscribe for a golden badge, protection & support",
                                fontSize = 12.sp,
                                color = MutedText
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForwardIos,
                            contentDescription = null,
                            tint = if (currentUser.isVerified) GoldDeep else MutedText,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }
        }

        // Grid Menu Options
        item {
            val menuItems = listOf(
                Triple("🏠", MeskotStrings.get("navFeed", currentLanguage), ScreenTab.FEED),
                Triple("👥", MeskotStrings.get("navFriends", currentLanguage), ScreenTab.FRIENDS),
                Triple("💬", MeskotStrings.get("navMessages", currentLanguage), ScreenTab.MESSAGES),
                Triple("👪", MeskotStrings.get("navGroups", currentLanguage), ScreenTab.GROUPS),
                Triple("🖼️", MeskotStrings.get("navPhotos", currentLanguage), ScreenTab.PHOTOS),
                Triple("🔔", MeskotStrings.get("navNotifs", currentLanguage), ScreenTab.NOTIFICATIONS),
                Triple("📊", "Dashboard", ScreenTab.DASHBOARD),
                Triple("🔖", MeskotStrings.get("savedPosts", currentLanguage), ScreenTab.SAVED),
                Triple("📢", "Ads Manager", ScreenTab.ADS_MANAGER),
                Triple("👑", "Creator Studio", ScreenTab.CREATOR_STUDIO),
                Triple("🔴", "Live Streaming", ScreenTab.LIVE)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                menuItems.take(2).forEach { item ->
                    MenuShortcutCard(
                        emoji = item.first,
                        title = item.second,
                        onClick = { viewModel.navigateTo(item.third) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                menuItems.drop(2).take(2).forEach { item ->
                    MenuShortcutCard(
                        emoji = item.first,
                        title = item.second,
                        onClick = { viewModel.navigateTo(item.third) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                menuItems.drop(4).take(2).forEach { item ->
                    MenuShortcutCard(
                        emoji = item.first,
                        title = item.second,
                        onClick = { viewModel.navigateTo(item.third) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                menuItems.drop(6).take(2).forEach { item ->
                    MenuShortcutCard(
                        emoji = item.first,
                        title = item.second,
                        onClick = { viewModel.navigateTo(item.third) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                menuItems.drop(8).take(2).forEach { item ->
                    MenuShortcutCard(
                        emoji = item.first,
                        title = item.second,
                        onClick = { viewModel.navigateTo(item.third) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (menuItems.size > 10) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    menuItems.drop(10).forEach { item ->
                        MenuShortcutCard(
                            emoji = item.first,
                            title = item.second,
                            onClick = { viewModel.navigateTo(item.third) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (menuItems.size % 2 != 0) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // Admin Panel if Admin
        if (currentUser?.isAdmin == true) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                MenuShortcutCard(
                    emoji = "🛡️",
                    title = MeskotStrings.get("adminPanel", currentLanguage),
                    onClick = { viewModel.navigateTo(ScreenTab.ADMIN) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }


        // Brand Badge in Menu
        item {
            Spacer(modifier = Modifier.height(24.dp))

            // Modern Meskot Brand Badge in Menu
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                MeskotLogoBadge(size = 42.dp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Meskot",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Text(
                    text = "መስኮት · Ethiopian & Habesha Community Network\nVersion 2.4",
                    fontSize = 11.sp,
                    color = MutedText,
                    textAlign = TextAlign.Center
                )
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

@Composable
fun MenuShortcutCard(
    emoji: String,
    title: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = emoji, fontSize = 20.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Ink)
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
    var isSignUpPasswordVisible by remember { mutableStateOf(false) }

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
                                onValueChange = { signUpPassword = it },
                                label = { Text(MeskotStrings.get("password", currentLanguage)) },
                                textStyle = TextStyle(color = Ink, fontSize = 14.sp),
                                colors = meskotTextFieldColors(),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                singleLine = true,
                                enabled = !isLoading,
                                visualTransformation = if (isSignUpPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                trailingIcon = {
                                    IconButton(onClick = { isSignUpPasswordVisible = !isSignUpPasswordVisible }) {
                                        Icon(
                                            imageVector = if (isSignUpPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                            contentDescription = null,
                                            tint = MutedText
                                        )
                                    }
                                }
                            )

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
