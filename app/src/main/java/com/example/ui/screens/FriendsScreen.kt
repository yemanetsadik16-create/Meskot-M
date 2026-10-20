package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.data.AppLanguage
import com.example.data.User
import com.example.data.VerificationStatus
import com.example.ui.MeskotViewModel

/**
 * Circle & Friends Screen styled to match the Meskot Royal Passport design.
 * Features connected member count, refined search, verified member checkmarks,
 * role and location metadata, and direct Chat / Add actions.
 */
@Composable
fun FriendsScreen(
    viewModel: MeskotViewModel,
    currentUser: User?,
    allUsers: List<User>,
    friendUids: Set<String>,
    incomingRequests: List<User>,
    outgoingRequests: Set<String>,
    currentLanguage: AppLanguage
) {
    var searchQuery by remember { mutableStateOf("") }

    // Only real users registered in the backend server (excluding currently authenticated user)
    val realRegisteredUsers = remember(allUsers, currentUser?.uid) {
        allUsers.filter { it.uid.isNotBlank() && it.uid != currentUser?.uid }
    }

    // Connected members strictly from backend friends collection
    val connectedMembers = realRegisteredUsers.filter { friendUids.contains(it.uid) }
    val connectedCount = connectedMembers.size

    val filteredList = remember(realRegisteredUsers, searchQuery) {
        if (searchQuery.isBlank()) {
            realRegisteredUsers
        } else {
            val q = searchQuery.trim().lowercase()
            realRegisteredUsers.filter {
                it.displayName.lowercase().contains(q) ||
                    it.profession.lowercase().contains(q) ||
                    it.location.lowercase().contains(q) ||
                    it.bio.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF0F2F5))
            .testTag("friends_screen")
    ) {
        // Top Header: "Circle & Friends" + Pill badge
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Circle & Friends",
                    fontSize = 23.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF1B2A22),
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$connectedCount Connected in your Royal Passport circle",
                    fontSize = 13.5.sp,
                    color = Color(0xFF6B7280),
                    fontWeight = FontWeight.Normal
                )
            }

            // Pill badge: "2 Connected"
            Surface(
                shape = RoundedCornerShape(50),
                color = Color(0xFFFAF5ED),
                border = BorderStroke(1.dp, Color(0xFFE2D3BF)),
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Text(
                    text = "$connectedCount Connected",
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF8B5E2B),
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }
        }

        // Search Input with Magnifier Icon & Soft Linen Container
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = {
                Text(
                    text = "Search friends, developers, cultural creators...",
                    fontSize = 13.5.sp,
                    color = Color(0xFF9E9282)
                )
            },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search",
                    tint = Color(0xFF8C7D6B),
                    modifier = Modifier.size(20.dp)
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear search",
                            tint = Color(0xFF8C7D6B),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            },
            singleLine = true,
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFFAF7F0),
                unfocusedContainerColor = Color(0xFFFAF7F0),
                focusedBorderColor = Color(0xFFD6C4AD),
                unfocusedBorderColor = Color(0xFFEADBCE),
                focusedTextColor = Color(0xFF1B2A22),
                unfocusedTextColor = Color(0xFF1B2A22)
            ),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 6.dp)
                .testTag("people_search_input")
        )

        Spacer(modifier = Modifier.height(6.dp))

        // Members List
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 80.dp)
        ) {
            if (filteredList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = if (searchQuery.isNotBlank()) {
                                    "No registered members found matching\n\"$searchQuery\""
                                } else {
                                    "No other registered members found on backend server yet.\nWhen members register, they will appear here in your circle."
                                },
                                color = Color(0xFF8C7D6B),
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                lineHeight = 20.sp
                            )
                            if (searchQuery.isNotBlank()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = Color(0xFFFAF5ED),
                                    border = BorderStroke(1.dp, Color(0xFFE2D3BF)),
                                    modifier = Modifier.clickable { searchQuery = "" }
                                ) {
                                    Text(
                                        text = "Clear search",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF8B5E2B),
                                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredList, key = { it.uid }) { user ->
                    val isConnected = friendUids.contains(user.uid)
                    val isIncoming = incomingRequests.any { it.uid == user.uid }
                    val isOutgoing = outgoingRequests.contains(user.uid)

                    CircleFriendCard(
                        user = user,
                        isConnected = isConnected,
                        isIncoming = isIncoming,
                        isOutgoing = isOutgoing,
                        onCardClick = { viewModel.openProfile(user) },
                        onChatClick = { viewModel.openChat(user) },
                        onAddClick = { viewModel.sendFriendRequest(user) },
                        onCancelClick = { viewModel.cancelFriendRequest(user) },
                        onAcceptClick = { viewModel.acceptFriendRequest(user) }
                    )
                }
            }
        }
    }
}

/**
 * Individual member card in the Circle & Friends view.
 * Features warm cream surface, 22dp corner radius, verified blue badge,
 * role/profession, location pin, and dedicated Chat or Add action.
 */
@Composable
private fun CircleFriendCard(
    user: User,
    isConnected: Boolean,
    isIncoming: Boolean,
    isOutgoing: Boolean,
    onCardClick: () -> Unit,
    onChatClick: () -> Unit,
    onAddClick: () -> Unit,
    onCancelClick: () -> Unit,
    onAcceptClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onCardClick)
            .testTag("friend_card_${user.uid}"),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFDF9)),
        border = BorderStroke(1.2.dp, Color(0xFFE6DAC8)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left: High resolution circular avatar
            CircleMemberAvatar(
                photoUrl = user.photoUrl,
                name = user.displayName,
                size = 56
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Middle: Name, Verified Badge, Role, Location
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = user.displayName,
                        fontSize = 15.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1B2A22),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    // Blue checkmark badge for verified members
                    val isVerified = user.isVerified || user.verificationStatus == VerificationStatus.VERIFIED
                    if (isVerified) {
                        Spacer(modifier = Modifier.width(5.dp))
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Verified member",
                            tint = Color(0xFF2563EB), // Vibrant blue checkmark
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Profession / Role
                val roleText = user.profession.ifBlank { user.bio.ifBlank { "Cultural Creator" } }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = roleText,
                    fontSize = 13.sp,
                    color = Color(0xFF4B5563),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Location row with red/terracotta pin icon
                val locationText = user.location.ifBlank { "Addis Ababa, Ethiopia" }
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.LocationOn,
                        contentDescription = "Location",
                        tint = Color(0xFFA33F39), // Warm terracotta red
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = locationText,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color(0xFF855829), // Warm amber bronze
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            // Right: Primary Action Button (Chat or Add)
            when {
                isConnected -> {
                    // Solid Roasted Coffee Brown [💬 Chat] Button
                    Button(
                        onClick = onChatClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF704822), // Deep warm brown
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("chat_btn_${user.uid}")
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ChatBubbleOutline,
                            contentDescription = "Chat",
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Chat",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                isIncoming -> {
                    // Friend request received: [Accept]
                    Button(
                        onClick = onAcceptClick,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF855829),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 7.dp),
                        modifier = Modifier
                            .height(38.dp)
                            .testTag("accept_btn_${user.uid}")
                    ) {
                        Text(
                            text = "Accept",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }

                isOutgoing -> {
                    // Outlined [Requested] button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFAF5ED),
                        border = BorderStroke(1.dp, Color(0xFFD8C7B0)),
                        modifier = Modifier
                            .height(38.dp)
                            .clickable(onClick = onCancelClick)
                            .testTag("requested_btn_${user.uid}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color(0xFF855829),
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Requested",
                                fontSize = 12.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF855829)
                            )
                        }
                    }
                }

                else -> {
                    // Outlined warm gold [+ Add] Button
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFCF8F2),
                        border = BorderStroke(1.2.dp, Color(0xFFDECFBA)),
                        modifier = Modifier
                            .height(38.dp)
                            .clickable(onClick = onAddClick)
                            .testTag("add_btn_${user.uid}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 14.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.PersonAdd,
                                contentDescription = "Add friend",
                                tint = Color(0xFF855829),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Add",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF855829)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Avatar for Circle members with direct support for custom drawable illustrations
 * (Selamawit, Dawit, Kalkidan, Yared) or network images and initials fallback.
 */
@Composable
private fun CircleMemberAvatar(
    photoUrl: String?,
    name: String,
    size: Int = 56,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .border(1.2.dp, Color(0xFFE2D6C5), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (!photoUrl.isNullOrBlank()) {
            AsyncImage(
                model = photoUrl,
                contentDescription = name,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CircleShape),
                contentScale = ContentScale.Crop
            )
        } else {
            val initials = name.trim().split("\\s+".toRegex())
                .take(2)
                .mapNotNull { it.firstOrNull()?.uppercase() }
                .joinToString("")
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFE8DAC7)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials.ifBlank { "?" },
                    color = Color(0xFF6B4720),
                    fontSize = (size * 0.38f).sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * Shared TabButton component used by GroupsScreen and other screens.
 */
@Composable
fun TabButton(
    title: String,
    isSelected: Boolean,
    badgeCount: Int = 0,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(
                if (isSelected) com.example.ui.theme.MeskotLogoBrush
                else androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color.White else com.example.ui.theme.MutedText
            )
            if (badgeCount > 0) {
                Spacer(modifier = Modifier.width(4.dp))
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(com.example.ui.theme.CrossRed)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = badgeCount.toString(),
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Shared EmptyNotice component used across Meskot screens.
 */
@Composable
fun EmptyNotice(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = com.example.ui.theme.MutedText,
            fontSize = 14.sp,
            textAlign = TextAlign.Center
        )
    }
}

