package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Bookmark
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Report
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.example.data.MembershipTier
import com.example.data.ChapaGatewayConfig
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.AppLanguage
import com.example.data.MeskotStrings
import com.example.data.Post
import com.example.data.User
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
import com.example.ui.theme.PostGradientList

@Composable
fun ComposerDialog(
    currentUser: User,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSubmit: (text: String, mediaUrls: List<String>, bgColorIndex: Int, visibility: String) -> Unit
) {
    var text by remember { mutableStateOf("") }
    var selectedBgIndex by remember { mutableStateOf(0) }
    var selectedVisibility by remember { mutableStateOf("public") }
    var isPrivacyDropdownOpen by remember { mutableStateOf(false) }
    var selectedMediaUrls by remember { mutableStateOf<List<String>>(emptyList()) }

    // Preset Ethiopian Cultural Visuals for testing photo posts easily
    val samplePhotos = listOf(
        "https://images.unsplash.com/photo-1547471080-7cc2caa01a7e?w=800&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1514432324607-a09d9b4aefdd?w=800&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1578922746465-3a80a228f223?w=800&auto=format&fit=crop&q=80",
        "https://images.unsplash.com/photo-1509316975850-ff9c5deb0cd9?w=800&auto=format&fit=crop&q=80"
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Paper
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Ink)
                    }

                    Text(
                        text = MeskotStrings.get("createPost", currentLanguage),
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Ink
                    )

                    Button(
                        onClick = {
                            if (text.isNotBlank() || selectedMediaUrls.isNotEmpty()) {
                                onSubmit(text, selectedMediaUrls, selectedBgIndex, selectedVisibility)
                            }
                        },
                        enabled = text.isNotBlank() || selectedMediaUrls.isNotEmpty(),
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("submit_post_btn")
                    ) {
                        Text(text = MeskotStrings.get("post", currentLanguage), fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = LineBorder, modifier = Modifier.padding(vertical = 8.dp))

                // Author & Privacy Selector
                Row(
                    modifier = Modifier.padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    UserAvatar(photoUrl = currentUser.photoUrl, name = currentUser.displayName, size = 48)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(text = currentUser.displayName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Ink)

                        Box {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Paper2)
                                    .border(1.dp, LineBorder, RoundedCornerShape(14.dp))
                                    .clickable { isPrivacyDropdownOpen = true }
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val visLabel = when (selectedVisibility) {
                                    "public" -> "🌍 " + MeskotStrings.get("public", currentLanguage)
                                    "friends" -> "👥 " + MeskotStrings.get("friendsLabel", currentLanguage)
                                    else -> "🔒 " + MeskotStrings.get("onlyMe", currentLanguage)
                                }
                                Text(text = "$visLabel ▾", fontSize = 12.sp, color = Ink, fontWeight = FontWeight.SemiBold)
                            }

                            DropdownMenu(
                                expanded = isPrivacyDropdownOpen,
                                onDismissRequest = { isPrivacyDropdownOpen = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("🌍 " + MeskotStrings.get("public", currentLanguage)) },
                                    onClick = { selectedVisibility = "public"; isPrivacyDropdownOpen = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("👥 " + MeskotStrings.get("friendsLabel", currentLanguage)) },
                                    onClick = { selectedVisibility = "friends"; isPrivacyDropdownOpen = false }
                                )
                                DropdownMenuItem(
                                    text = { Text("🔒 " + MeskotStrings.get("onlyMe", currentLanguage)) },
                                    onClick = { selectedVisibility = "onlyme"; isPrivacyDropdownOpen = false }
                                )
                            }
                        }
                    }
                }

                // Format label & Quick Paste helper
                val clipboardManager = LocalClipboardManager.current
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedBgIndex == 0) "✏️ Meskot Standard" else "🎨 Royal Wallpaper",
                        fontSize = 11.sp,
                        color = MutedText,
                        fontWeight = FontWeight.Medium
                    )
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Paper2,
                        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
                        modifier = Modifier.clickable {
                            val clip = clipboardManager.getText()?.text
                            if (!clip.isNullOrEmpty()) {
                                text = if (text.isEmpty()) clip else "$text $clip"
                            }
                        }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "📋 " + MeskotStrings.get("paste", currentLanguage),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                        }
                    }
                }

                // Text Body
                val bgBrush = PostGradientList.getOrNull(selectedBgIndex)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 8.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .then(if (bgBrush != null) Modifier.background(bgBrush) else Modifier.background(Color.White))
                        .border(1.dp, LineBorder, RoundedCornerShape(14.dp))
                        .padding(16.dp),
                    contentAlignment = if (bgBrush != null) Alignment.Center else Alignment.TopStart
                ) {
                    OutlinedTextField(
                        value = text,
                        onValueChange = { text = it },
                        textStyle = androidx.compose.ui.text.TextStyle(
                            color = if (bgBrush != null) Color.White else Ink,
                            fontSize = if (bgBrush != null) 20.sp else 16.sp,
                            fontWeight = if (bgBrush != null) FontWeight.Bold else FontWeight.Normal,
                            textAlign = if (bgBrush != null) androidx.compose.ui.text.style.TextAlign.Center else androidx.compose.ui.text.style.TextAlign.Start
                        ),
                        placeholder = {
                            Text(
                                text = MeskotStrings.get("composerPh", currentLanguage),
                                fontSize = if (bgBrush != null) 20.sp else 16.sp,
                                color = if (bgBrush != null) Color.White.copy(alpha = 0.85f) else MutedText,
                                fontWeight = if (bgBrush != null) FontWeight.Bold else FontWeight.Normal,
                                textAlign = if (bgBrush != null) androidx.compose.ui.text.style.TextAlign.Center else androidx.compose.ui.text.style.TextAlign.Start
                            )
                        },
                        colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            focusedBorderColor = Color.Transparent,
                            unfocusedBorderColor = Color.Transparent,
                            focusedTextColor = if (bgBrush != null) Color.White else Ink,
                            unfocusedTextColor = if (bgBrush != null) Color.White else Ink,
                            cursorColor = if (bgBrush != null) Color.White else GoldDeep
                        ),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("composer_text_input")
                    )
                }

                // Attached Photos Preview
                if (selectedMediaUrls.isNotEmpty()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(selectedMediaUrls.size) { i ->
                            Box(
                                modifier = Modifier
                                    .size(72.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            ) {
                                AsyncImage(
                                    model = selectedMediaUrls[i],
                                    contentDescription = null,
                                    modifier = Modifier.fillMaxSize()
                                )
                                Box(
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(20.dp)
                                        .clip(CircleShape)
                                        .background(Color.Black.copy(alpha = 0.7f))
                                        .clickable {
                                            selectedMediaUrls = selectedMediaUrls.toMutableList().also { it.removeAt(i) }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "✕", color = Color.White, fontSize = 10.sp)
                                }
                            }
                        }
                    }
                }

                // Gradient Background Swatches
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PostGradientList.forEachIndexed { index, brush ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .then(
                                    if (brush != null) Modifier.background(brush)
                                    else Modifier
                                        .background(Paper2)
                                        .border(1.dp, LineBorder, RoundedCornerShape(8.dp))
                                )
                                .border(
                                    width = if (selectedBgIndex == index) 2.5.dp else 0.dp,
                                    color = if (selectedBgIndex == index) Gold else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    selectedBgIndex = index
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            if (index == 0) {
                                Text(text = "Aa", fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink)
                            }
                        }
                    }
                }

                // Quick photo selector
                Text(
                    text = "🖼️ " + MeskotStrings.get("addPhoto", currentLanguage) + " (Habesha Gallery):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MutedText,
                    modifier = Modifier.padding(top = 6.dp, bottom = 4.dp)
                )

                LazyRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(samplePhotos.size) { idx ->
                        val url = samplePhotos[idx]
                        AsyncImage(
                            model = url,
                            contentDescription = "Sample",
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = if (selectedMediaUrls.contains(url)) 2.dp else 0.5.dp,
                                    color = if (selectedMediaUrls.contains(url)) Gold else LineBorder,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable {
                                    if (selectedMediaUrls.contains(url)) {
                                        selectedMediaUrls = selectedMediaUrls - url
                                    } else {
                                        selectedMediaUrls = selectedMediaUrls + url
                                        selectedBgIndex = 0 // photos disable gradient mode
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PostOptionsMenu(
    post: Post,
    isAuthor: Boolean,
    isAdmin: Boolean,
    isSaved: Boolean,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSaveToggle: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onHide: () -> Unit,
    onReport: () -> Unit,
    onInterested: () -> Unit,
    onNotInterested: () -> Unit,
    onToggleNotifs: () -> Unit,
    onCopyText: () -> Unit = {},
    onCopyLink: () -> Unit,
    onBoostPost: () -> Unit = {}
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                if (post.text.isNotBlank()) {
                    MenuOptionItem(
                        emoji = "📋",
                        title = MeskotStrings.get("copyText", currentLanguage),
                        onClick = { onCopyText(); onDismiss() }
                    )
                }

                if (!isAuthor) {
                    MenuOptionItem(
                        emoji = "➕",
                        title = MeskotStrings.get("interested", currentLanguage),
                        onClick = { onInterested(); onDismiss() }
                    )
                    MenuOptionItem(
                        emoji = "➖",
                        title = MeskotStrings.get("notInterested", currentLanguage),
                        onClick = { onNotInterested(); onDismiss() }
                    )
                }

                MenuOptionItem(
                    emoji = "🔖",
                    title = if (isSaved) MeskotStrings.get("unsavePostAction", currentLanguage) else MeskotStrings.get("savePostAction", currentLanguage),
                    onClick = { onSaveToggle(); onDismiss() }
                )

                MenuOptionItem(
                    emoji = "🔗",
                    title = MeskotStrings.get("share", currentLanguage),
                    onClick = { onShare(); onDismiss() }
                )

                if (isAuthor) {
                    MenuOptionItem(
                        emoji = "🚀",
                        title = MeskotStrings.get("boostPost", currentLanguage) + if (post.isBoosted) " (Boost Active)" else "",
                        onClick = { onBoostPost(); onDismiss() }
                    )
                    MenuOptionItem(
                        emoji = "✏️",
                        title = MeskotStrings.get("editPostAction", currentLanguage),
                        onClick = { onEdit() }
                    )
                }

                if (isAuthor || isAdmin) {
                    MenuOptionItem(
                        emoji = "🗑️",
                        title = MeskotStrings.get("deletePost", currentLanguage),
                        isDanger = true,
                        onClick = { onDelete(); onDismiss() }
                    )
                } else {
                    MenuOptionItem(
                        emoji = "🚫",
                        title = MeskotStrings.get("postHidden", currentLanguage),
                        onClick = { onHide(); onDismiss() }
                    )
                    MenuOptionItem(
                        emoji = "⚠️",
                        title = MeskotStrings.get("reportPostAction", currentLanguage),
                        isDanger = true,
                        onClick = { onReport(); onDismiss() }
                    )
                }

                MenuOptionItem(
                    emoji = "🔔",
                    title = MeskotStrings.get("turnOnNotifs", currentLanguage),
                    onClick = { onToggleNotifs(); onDismiss() }
                )

                MenuOptionItem(
                    emoji = "📋",
                    title = MeskotStrings.get("copyLink", currentLanguage),
                    onClick = { onCopyLink(); onDismiss() }
                )
            }
        }
    }
}

@Composable
private fun MenuOptionItem(
    emoji: String,
    title: String,
    isDanger: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp, horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = emoji, fontSize = 18.sp, modifier = Modifier.width(32.dp))
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (isDanger) CrossRed else Ink
        )
    }
}

@Composable
fun TipModal(
    post: Post,
    currentLanguage: AppLanguage,
    userBalance: Double = 0.0,
    userStarBalance: Int = 1250,
    onDismiss: () -> Unit,
    onConfirmTip: (amount: Double, payFromBalance: Boolean) -> Unit,
    onSendStars: (starCount: Int, giftName: String) -> Unit = { _, _ -> },
    onDepositClick: () -> Unit = {}
) {
    var isStarsTab by remember { mutableStateOf(false) }
    var selectedAmount by remember { mutableStateOf(25.0) }
    var customAmountText by remember { mutableStateOf("") }

    val presetAmounts = listOf(10.0, 25.0, 50.0, 100.0)
    val hasEnoughBalance = userBalance >= selectedAmount

    val virtualGifts = listOf(
        Triple("☕ Coffee Cheer", 50, "☕"),
        Triple("👏 Big Applause", 100, "👏"),
        Triple("🌟 Super Star", 250, "🌟"),
        Triple("👑 Royal Crown", 500, "👑"),
        Triple("🦁 Lion of Judah", 1000, "🦁"),
        Triple("💎 Diamond Trophy", 2500, "💎")
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                // Segmented Tab for Cash Tip vs Virtual Stars
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Paper2)
                        .padding(3.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!isStarsTab) Color.White else Color.Transparent)
                            .clickable { isStarsTab = false }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "💰 Chapa Tip (Birr)",
                            fontSize = 12.sp,
                            fontWeight = if (!isStarsTab) FontWeight.Bold else FontWeight.Medium,
                            color = if (!isStarsTab) GoldDeep else MutedText
                        )
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isStarsTab) Color.White else Color.Transparent)
                            .clickable { isStarsTab = true }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⭐ Virtual Stars",
                            fontSize = 12.sp,
                            fontWeight = if (isStarsTab) FontWeight.Bold else FontWeight.Medium,
                            color = if (isStarsTab) GoldDeep else MutedText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                if (!isStarsTab) {
                    Text(
                        text = "💰 " + MeskotStrings.get("supportCreator", currentLanguage),
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Ink
                    )

                    Text(
                        text = MeskotStrings.get("supportSub", currentLanguage),
                        fontSize = 12.sp,
                        color = MutedText,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )

                    // Amount Chips
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetAmounts.forEach { amount ->
                            val isSelected = selectedAmount == amount && customAmountText.isEmpty()
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(if (isSelected) Gold else Paper2)
                                    .border(1.dp, if (isSelected) GoldDeep else LineBorder, RoundedCornerShape(10.dp))
                                    .clickable {
                                        selectedAmount = amount
                                        customAmountText = ""
                                    }
                                    .padding(vertical = 10.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${amount.toInt()} ETB",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = if (isSelected) Color.White else Ink
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = customAmountText,
                        onValueChange = {
                            customAmountText = it
                            val parsed = it.toDoubleOrNull()
                            if (parsed != null && parsed > 0) selectedAmount = parsed
                        },
                        label = { Text(MeskotStrings.get("customAmount", currentLanguage)) },
                        placeholder = { Text("e.g. 150") },
                        textStyle = androidx.compose.ui.text.TextStyle(color = Ink, fontSize = 14.sp),
                        colors = com.example.ui.theme.meskotTextFieldColors(),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Real Account Balance Audit Status Card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (hasEnoughBalance) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (hasEnoughBalance) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
                        )
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = if (hasEnoughBalance) "💳 " else "⚠️ ", fontSize = 16.sp)
                            Spacer(modifier = Modifier.width(6.dp))
                            Column {
                                Text(
                                    text = "Your Balance: ${String.format(java.util.Locale.US, "%,.2f", userBalance)} ETB",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.5.sp,
                                    color = if (hasEnoughBalance) Color(0xFF166534) else Color(0xFF991B1B)
                                )
                                Text(
                                    text = if (hasEnoughBalance)
                                        "${selectedAmount.toInt()} ETB will decrease from your balance."
                                    else
                                        "Insufficient balance! Required: ${selectedAmount.toInt()} ETB. Please deposit funds.",
                                    fontSize = 11.sp,
                                    color = if (hasEnoughBalance) Color(0xFF15803D) else Color(0xFFB91C1C),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(text = MeskotStrings.get("cancel", currentLanguage), color = MutedText)
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            if (!hasEnoughBalance) {
                                Button(
                                    onClick = { onDepositClick() },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White),
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text(text = "+ Deposit ETB", fontWeight = FontWeight.Bold, fontSize = 11.sp)
                                }
                            }

                            Button(
                                onClick = {
                                    if (hasEnoughBalance) {
                                        onConfirmTip(selectedAmount, true)
                                    } else {
                                        onDepositClick()
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (hasEnoughBalance) Gold else Color(0xFFDC2626),
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (hasEnoughBalance)
                                        "Support (${selectedAmount.toInt()} ETB)"
                                    else
                                        "Insufficient Balance",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                } else {
                    // Virtual Stars & Gifts Tab
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "⭐ Virtual Stars & Gifting",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Ink
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .background(GoldSurface)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = "Balance: $userStarBalance ⭐",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = GoldDeep
                            )
                        }
                    }

                    Text(
                        text = "Send Facebook-like animated gifts to reward the creator! 1 Star = 1.50 ETB.",
                        fontSize = 11.sp,
                        color = MutedText,
                        modifier = Modifier.padding(top = 2.dp, bottom = 10.dp)
                    )

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        virtualGifts.chunked(2).forEach { rowGifts ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                rowGifts.forEach { (giftTitle, starCost, _) ->
                                    val canAfford = userStarBalance >= starCost
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clip(RoundedCornerShape(10.dp))
                                            .background(if (canAfford) Paper2 else Color(0xFFF1F5F9))
                                            .border(1.dp, LineBorder, RoundedCornerShape(10.dp))
                                            .clickable(enabled = canAfford) {
                                                onSendStars(starCost, giftTitle)
                                            }
                                            .padding(vertical = 10.dp, horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(text = giftTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "$starCost ⭐",
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = if (canAfford) GoldDeep else Color.Gray
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text(text = MeskotStrings.get("cancel", currentLanguage), color = MutedText)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun BoostPostModal(
    post: Post,
    currentLanguage: AppLanguage,
    userBalance: Double = 0.0,
    onDismiss: () -> Unit,
    onConfirmBoost: (
        dailyBudgetEtb: Double,
        durationDays: Int,
        locations: List<String>,
        minAge: Int,
        maxAge: Int,
        interests: List<String>
    ) -> Unit,
    onDepositClick: () -> Unit = {}
) {
    var dailyBudget by remember { mutableStateOf(200.0) }
    var selectedDuration by remember { mutableStateOf(7) }
    var selectedLocation by remember { mutableStateOf("Addis Ababa + Hawassa") }

    val durations = listOf(1, 3, 7, 14, 30)
    val locations = listOf("Addis Ababa + Hawassa", "All Ethiopia", "Global Diaspora (USA/Europe)")

    val totalBudget = dailyBudget * selectedDuration
    val hasEnoughBalance = userBalance >= totalBudget
    val estimatedReach = (dailyBudget * 120).toInt()
    val multiplier = String.format(java.util.Locale.US, "%.1f", 1.0 + (dailyBudget / 50.0).coerceAtMost(5.0))

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🚀 ", fontSize = 22.sp)
                    Column {
                        Text(
                            text = MeskotStrings.get("boostPost", currentLanguage),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Ink
                        )
                        Text(
                            text = "Promote into high-priority algorithmic feed",
                            fontSize = 11.sp,
                            color = MutedText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Post Preview Snippet
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Paper2)
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "Post by ${post.authorName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Ink
                        )
                        Text(
                            text = post.text.ifBlank { "[Media Post]" },
                            fontSize = 12.sp,
                            color = MutedText,
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Budget Slider
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "Daily Budget:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                    Text(text = "${dailyBudget.toInt()} ETB/day", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = GoldDeep)
                }

                Slider(
                    value = dailyBudget.toFloat(),
                    onValueChange = { dailyBudget = it.toDouble() },
                    valueRange = 50f..1500f,
                    steps = 28,
                    colors = SliderDefaults.colors(thumbColor = GoldDeep, activeTrackColor = GoldDeep)
                )

                // Duration Selector
                Text(text = "Duration:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    durations.forEach { days ->
                        val isSel = selectedDuration == days
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) GoldDeep else Paper2)
                                .clickable { selectedDuration = days }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${days}d",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSel) Color.White else Ink
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Targeting Location
                Text(text = "Target Audience Geography:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                Spacer(modifier = Modifier.height(6.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    locations.forEach { loc ->
                        val isSel = selectedLocation == loc
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) GoldSurface else Paper2)
                                .border(1.dp, if (isSel) GoldDeep else LineBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedLocation = loc }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = (if (isSel) "✓ " else "") + loc,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) GoldDeep else Ink
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Forecast Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFFFEF3C7))
                        .padding(12.dp)
                ) {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(text = "⚡ Estimated Reach: ~$estimatedReach people/day", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                        Text(text = "📈 Algorithm Weight: ${multiplier}x Feed Priority", fontSize = 11.sp, color = Color(0xFF92400E))
                        Text(text = "💳 Total Spend: ${totalBudget.toInt()} ETB ($selectedDuration days)", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF92400E))
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Real Account Balance Audit Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (hasEnoughBalance) Color(0xFFF0FDF4) else Color(0xFFFEF2F2)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (hasEnoughBalance) Color(0xFF86EFAC) else Color(0xFFFCA5A5)
                    )
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = if (hasEnoughBalance) "💳 " else "⚠️ ", fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Account Balance: ${String.format(java.util.Locale.US, "%,.2f", userBalance)} ETB",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = if (hasEnoughBalance) Color(0xFF166534) else Color(0xFF991B1B)
                            )
                            Text(
                                text = if (hasEnoughBalance)
                                    "${totalBudget.toInt()} ETB will decrease from your balance upon confirmation."
                                else
                                    "Insufficient balance! Required: ${totalBudget.toInt()} ETB. Need ${(totalBudget - userBalance).toInt()} ETB more.",
                                fontSize = 11.5.sp,
                                color = if (hasEnoughBalance) Color(0xFF15803D) else Color(0xFFB91C1C),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = MeskotStrings.get("cancel", currentLanguage), color = MutedText)
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        if (!hasEnoughBalance) {
                            Button(
                                onClick = { onDepositClick() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(text = "+ Deposit ETB", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                            }
                        }

                        Button(
                            onClick = {
                                if (hasEnoughBalance) {
                                    onConfirmBoost(
                                        dailyBudget,
                                        selectedDuration,
                                        listOf(selectedLocation),
                                        18,
                                        55,
                                        listOf("General", "Culture", "Tech")
                                    )
                                } else {
                                    onDepositClick()
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (hasEnoughBalance) GoldDeep else Color(0xFFDC2626),
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (hasEnoughBalance)
                                    "Boost Post Now (${totalBudget.toInt()} ETB) 🚀"
                                else
                                    "Insufficient Balance (${userBalance.toInt()} ETB)",
                                fontWeight = FontWeight.Bold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SubscriptionModal(
    creator: User,
    currentLanguage: AppLanguage,
    currentUser: User? = null,
    onDismiss: () -> Unit,
    onSubscribe: (tier: MembershipTier) -> Unit
) {
    var selectedTier by remember { mutableStateOf(MembershipTier.SILVER) }
    val currentTierCode = currentUser?.vipMemberships?.get(creator.uid) ?: "FREE"
    val currentTier = MembershipTier.fromCode(currentTierCode)

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(androidx.compose.foundation.rememberScrollState())
            ) {
                // Header with Creator
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = creator.photoUrl,
                        contentDescription = null,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${creator.displayName}'s VIP Fan Club",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Ink
                        )
                        Text(
                            text = "Monthly Recurring Tiered Subscription",
                            fontSize = 11.sp,
                            color = MutedText
                        )
                    }
                }

                if (currentTier != MembershipTier.FREE) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(GoldSurface)
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⭐ Current Active Plan: ${currentTier.badge} ${currentTier.label}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldDeep
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Tier Selection Cards
                val tiers = listOf(MembershipTier.BRONZE, MembershipTier.SILVER, MembershipTier.GOLD)
                tiers.forEach { tier ->
                    val isSel = selectedTier == tier
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSel) GoldSurface else Paper2),
                        border = androidx.compose.foundation.BorderStroke(if (isSel) 2.dp else 1.dp, if (isSel) GoldDeep else LineBorder),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { selectedTier = tier }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${tier.badge} ${tier.label}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = Ink
                                )
                                Text(
                                    text = "${tier.monthlyPriceEtb.toInt()} ETB / mo",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = GoldDeep
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = tier.perksSummary, fontSize = 11.sp, color = MutedText, lineHeight = 15.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = MeskotStrings.get("cancel", currentLanguage), color = MutedText)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = { onSubscribe(selectedTier) },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldDeep, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Subscribe (${selectedTier.monthlyPriceEtb.toInt()} ETB)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun CreatorPayoutModal(
    netBalanceEtb: Double,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onRequestPayout: (method: String, amount: Double, destinationAccount: String) -> Unit
) {
    var amountText by remember { mutableStateOf(if (netBalanceEtb > 0) netBalanceEtb.toInt().toString() else "100") }
    var destinationAccount by remember { mutableStateOf("") }
    var selectedMethod by remember { mutableStateOf("Telebirr (Chapa Payout API)") }

    val methods = listOf(
        "Telebirr (Chapa Payout API)",
        "CBE Birr (Commercial Bank of Ethiopia)",
        "Dashen Bank / Amole (Chapa Rail)",
        "Awash Bank Direct Transfer",
        "Stripe Connect (Global Diaspora)"
    )

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().testTag("creator_payout_modal")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "💸 Real Payout Settlement",
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Ink
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFE8F5E9))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "CHAPA DISBURSAL",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                Text(
                    text = "Net Available Balance: ${String.format(java.util.Locale.US, "%,.2f", netBalanceEtb)} ETB",
                    fontSize = 12.sp,
                    color = GoldDeep,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 2.dp, bottom = 12.dp)
                )

                Text(text = "Disbursement Channel:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                Spacer(modifier = Modifier.height(4.dp))
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    methods.forEach { method ->
                        val isSel = selectedMethod == method
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSel) GoldSurface else Paper2)
                                .border(1.dp, if (isSel) GoldDeep else LineBorder, RoundedCornerShape(8.dp))
                                .clickable { selectedMethod = method }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = (if (isSel) "✓ " else "") + method,
                                fontSize = 12.sp,
                                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSel) GoldDeep else Ink
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = destinationAccount,
                    onValueChange = { destinationAccount = it },
                    label = { Text("Recipient Phone / Account Number") },
                    placeholder = { Text("e.g. 0911234567 or 100023456789") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Ink, fontSize = 13.sp),
                    colors = com.example.ui.theme.meskotTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = amountText,
                    onValueChange = { amountText = it },
                    label = { Text("Payout Amount (ETB)") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Ink, fontSize = 14.sp),
                    colors = com.example.ui.theme.meskotTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Text(
                    text = "Automated settlement via Chapa Transfer API to National Bank of Ethiopia rails. Clearing time: 5-15 mins.",
                    fontSize = 10.sp,
                    color = MutedText,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = MeskotStrings.get("cancel", currentLanguage), color = MutedText)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    val parsedAmount = amountText.toDoubleOrNull() ?: 0.0
                    Button(
                        onClick = {
                            if (parsedAmount > 0 && parsedAmount <= netBalanceEtb) {
                                onRequestPayout(selectedMethod, parsedAmount, destinationAccount)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldDeep, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp),
                        enabled = parsedAmount > 0 && parsedAmount <= netBalanceEtb
                    ) {
                        Text(text = "Confirm Disbursal", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun EditProfileDialog(
    currentUser: User,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (
        name: String,
        bio: String,
        photoUrl: String,
        gender: String,
        birthDate: String,
        coverPhotoUrl: String,
        profession: String,
        location: String,
        hometown: String,
        workplace: String,
        workRole: String,
        education: String,
        educationClass: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(currentUser.displayName) }
    var bio by remember { mutableStateOf(if (currentUser.bio.trim().equals("Engineer is a problem solver", ignoreCase = true)) "" else currentUser.bio) }
    var photoUrl by remember { mutableStateOf(currentUser.photoUrl) }
    var coverPhotoUrl by remember { mutableStateOf(currentUser.coverPhotoUrl) }
    var gender by remember { mutableStateOf(currentUser.gender) }
    var birthDate by remember { mutableStateOf(if (currentUser.birthDate.trim().equals("May 11, 1994", ignoreCase = true)) "" else currentUser.birthDate) }
    var profession by remember { mutableStateOf(if (currentUser.profession.trim().equals("Public figure", ignoreCase = true)) "" else currentUser.profession) }
    var location by remember { mutableStateOf(if (currentUser.location.trim().equals("Calgary, Alberta", ignoreCase = true) || currentUser.location.trim().equals("Calgary", ignoreCase = true)) "" else currentUser.location) }
    var hometown by remember { mutableStateOf(if (currentUser.hometown.trim().equals("Calgary, Alberta", ignoreCase = true) || currentUser.hometown.trim().equals("Calgary", ignoreCase = true)) "" else currentUser.hometown) }
    var workplace by remember { mutableStateOf(if (currentUser.workplace.trim().equals("Adigrat university _Engineering Sciences", ignoreCase = true)) "" else currentUser.workplace) }
    var workRole by remember { mutableStateOf(if (currentUser.workRole.trim().equals("Civil Engineering", ignoreCase = true)) "" else currentUser.workRole) }
    var education by remember { mutableStateOf(if (currentUser.education.trim().equals("Adigrat University", ignoreCase = true)) "" else currentUser.education) }
    var educationClass by remember { mutableStateOf(if (currentUser.educationClass.trim().equals("Class of 2018", ignoreCase = true)) "" else currentUser.educationClass) }

    Dialog(onDismissRequest = onDismiss) {
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
                    text = MeskotStrings.get("editProfile", currentLanguage),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Serif,
                    color = Ink
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text(MeskotStrings.get("fullName", currentLanguage)) },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Ink, fontSize = 14.sp),
                    colors = com.example.ui.theme.meskotTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = bio,
                    onValueChange = { bio = it },
                    label = { Text(MeskotStrings.get("bio", currentLanguage)) },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Ink, fontSize = 14.sp),
                    colors = com.example.ui.theme.meskotTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = profession,
                    onValueChange = { profession = it },
                    label = { Text("Profession / Category") },
                    placeholder = { Text("e.g. Software Engineer, Artist, Public figure") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Ink, fontSize = 14.sp),
                    colors = com.example.ui.theme.meskotTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Current City / Location") },
                    placeholder = { Text("e.g. Addis Ababa, Ethiopia") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Ink, fontSize = 14.sp),
                    colors = com.example.ui.theme.meskotTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = hometown,
                    onValueChange = { hometown = it },
                    label = { Text("Hometown") },
                    placeholder = { Text("e.g. Asmara, Eritrea") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Ink, fontSize = 14.sp),
                    colors = com.example.ui.theme.meskotTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = workplace,
                    onValueChange = { workplace = it },
                    label = { Text("Workplace / Organization") },
                    placeholder = { Text("e.g. Company or University") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Ink, fontSize = 14.sp),
                    colors = com.example.ui.theme.meskotTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = workRole,
                    onValueChange = { workRole = it },
                    label = { Text("Work Position / Role") },
                    placeholder = { Text("e.g. Project Manager, Designer") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Ink, fontSize = 14.sp),
                    colors = com.example.ui.theme.meskotTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = education,
                    onValueChange = { education = it },
                    label = { Text("College / University") },
                    placeholder = { Text("e.g. Addis Ababa University") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Ink, fontSize = 14.sp),
                    colors = com.example.ui.theme.meskotTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = educationClass,
                    onValueChange = { educationClass = it },
                    label = { Text("Graduation Class / Year") },
                    placeholder = { Text("e.g. Class of 2022") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Ink, fontSize = 14.sp),
                    colors = com.example.ui.theme.meskotTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = photoUrl,
                    onValueChange = { photoUrl = it },
                    label = { Text(MeskotStrings.get("photo", currentLanguage) + " URL") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Ink, fontSize = 14.sp),
                    colors = com.example.ui.theme.meskotTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = coverPhotoUrl,
                    onValueChange = { coverPhotoUrl = it },
                    label = { Text("Cover Banner URL") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Ink, fontSize = 14.sp),
                    colors = com.example.ui.theme.meskotTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Gender Selection
                Text(
                    text = MeskotStrings.get("gender", currentLanguage),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Ink
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Female", "Male", "Custom").forEach { g ->
                        val isSelected = gender.equals(g, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Ink else Paper2)
                                .clickable { gender = g }
                                .padding(vertical = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (g) {
                                    "Female" -> MeskotStrings.get("female", currentLanguage)
                                    "Male" -> MeskotStrings.get("male", currentLanguage)
                                    else -> MeskotStrings.get("custom", currentLanguage)
                                },
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) Color.White else Ink
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = birthDate,
                    onValueChange = { birthDate = it },
                    label = { Text(MeskotStrings.get("birthday", currentLanguage)) },
                    placeholder = { Text("e.g. Sep 11, 1995") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Ink, fontSize = 14.sp),
                    colors = com.example.ui.theme.meskotTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = MeskotStrings.get("cancel", currentLanguage), color = MutedText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            onSave(
                                name, bio, photoUrl, gender, birthDate,
                                coverPhotoUrl, profession, location, hometown,
                                workplace, workRole, education, educationClass
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = MeskotStrings.get("save", currentLanguage), fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Modal to deposit real Ethiopian Birr into Meskot using Chapa inline checkout.
 */
@Composable
fun ChapaDepositModal(
    config: ChapaGatewayConfig,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onProceed: (amount: Double) -> Unit
) {
    val presets = listOf(50.0, 100.0, 250.0, 500.0, 1000.0, 2500.0)
    var selectedAmount by remember { mutableStateOf(100.0) }
    var customAmountText by remember { mutableStateOf("") }
    var isCustom by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().testTag("chapa_deposit_modal")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚡ Deposit Real ETB",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Ink
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (config.isLiveMode) Color(0xFFE8F5E9) else Color(0xFFFEF3C7))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (config.isLiveMode) "CHAPA LIVE" else "CHAPA TEST",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (config.isLiveMode) Color(0xFF2E7D32) else Color(0xFFB45309)
                        )
                    }
                }

                Text(
                    text = "Add real funds directly to your creator balance or fan wallet using Telebirr, CBE Birr, eBirr, M-Pesa, or Debit/Credit cards.",
                    fontSize = 12.sp,
                    color = MutedText,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                Text(text = "Choose Amount (ETB):", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                Spacer(modifier = Modifier.height(8.dp))

                // Presets Grid
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (row in presets.chunked(3)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            row.forEach { amt ->
                                val isSel = !isCustom && selectedAmount == amt
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (isSel) Color(0xFF2E7D32) else Paper2)
                                        .border(1.dp, if (isSel) Color(0xFF2E7D32) else LineBorder, RoundedCornerShape(8.dp))
                                        .clickable {
                                            selectedAmount = amt
                                            isCustom = false
                                        }
                                        .padding(vertical = 10.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "${amt.toInt()} ETB",
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSel) Color.White else Ink
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Custom Amount Input
                OutlinedTextField(
                    value = customAmountText,
                    onValueChange = {
                        customAmountText = it
                        val parsed = it.toDoubleOrNull()
                        if (parsed != null && parsed > 0) {
                            selectedAmount = parsed
                            isCustom = true
                        }
                    },
                    label = { Text("Or Enter Custom Amount (ETB)") },
                    placeholder = { Text("e.g. 350") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Ink, fontSize = 14.sp),
                    colors = com.example.ui.theme.meskotTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Supported Payment Rails Banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFF1F5F9))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(
                            text = "Supported Channels in Chapa Checkout:",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF475569)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "📱 Telebirr · 🏦 CBE Birr · 💳 eBirr · 📲 M-Pesa · 💳 Visa / MasterCard",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = MeskotStrings.get("cancel", currentLanguage), color = MutedText)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (selectedAmount > 0) {
                                onProceed(selectedAmount)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32), contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "Pay ${selectedAmount.toInt()} ETB via Chapa →",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Modal to configure Chapa API keys and switch between Live Mode (Real Money) and Sandbox Test Mode.
 */
@Composable
fun ChapaConfigModal(
    currentConfig: ChapaGatewayConfig,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onSave: (publicKey: String, secretKey: String, isLiveMode: Boolean) -> Unit,
    onResetDemoBalance: () -> Unit
) {
    var publicKey by remember { mutableStateOf(currentConfig.publicKey) }
    var secretKey by remember { mutableStateOf(currentConfig.secretKey) }
    var isLiveMode by remember { mutableStateOf(currentConfig.isLiveMode) }
    var showResetConfirm by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().testTag("chapa_config_modal")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⚙️ Chapa Gateway Settings",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Ink
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (isLiveMode) Color(0xFFE8F5E9) else Color(0xFFFEF3C7))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (isLiveMode) "LIVE REAL MONEY" else "SANDBOX TEST",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (isLiveMode) Color(0xFF2E7D32) else Color(0xFFB45309)
                        )
                    }
                }

                Text(
                    text = "Configure your merchant credentials to process real payments or test in the Chapa Sandbox.",
                    fontSize = 12.sp,
                    color = MutedText,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                // Live Mode Toggle Card
                Card(
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isLiveMode) Color(0xFFF0FDF4) else Color(0xFFFFFBEB)
                    ),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isLiveMode) Color(0xFF86EFAC) else Color(0xFFFDE68A)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isLiveMode) "🟢 Live Mode (Real Money Active)" else "🟡 Sandbox Test Mode",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isLiveMode) Color(0xFF166534) else Color(0xFF92400E)
                            )
                            Text(
                                text = if (isLiveMode)
                                    "Transactions deduct real Ethiopian Birr via Telebirr, CBE, or Cards."
                                else
                                    "Transactions use Chapa sandbox simulated funds.",
                                fontSize = 11.sp,
                                color = if (isLiveMode) Color(0xFF15803D) else Color(0xFFB45309)
                            )
                        }
                        Switch(
                            checked = isLiveMode,
                            onCheckedChange = { isLiveMode = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFF2E7D32),
                                checkedTrackColor = Color(0xFFA7F3D0)
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Public Key Field
                OutlinedTextField(
                    value = publicKey,
                    onValueChange = { publicKey = it },
                    label = { Text("Chapa Public Key") },
                    placeholder = { Text("CHAPUBK_TEST-... or CHAPUBK_LIVE-...") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Ink, fontSize = 12.sp),
                    colors = com.example.ui.theme.meskotTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Secret Key Field
                OutlinedTextField(
                    value = secretKey,
                    onValueChange = { secretKey = it },
                    label = { Text("Chapa Secret Key (Optional / Backend Disbursals)") },
                    placeholder = { Text("CHASECK_TEST-... or CHASECK_LIVE-...") },
                    textStyle = androidx.compose.ui.text.TextStyle(color = Ink, fontSize = 12.sp),
                    colors = com.example.ui.theme.meskotTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Clear Mock Funds Section
                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF2F2)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFECACA)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "🧹 Switch from Demo Money to 100% Real Funds",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF991B1B)
                        )
                        Text(
                            text = "Reset mock creator earnings and start fresh with zero balance so all numbers reflect actual Chapa receipts.",
                            fontSize = 10.sp,
                            color = Color(0xFF7F1D1D),
                            modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                        )
                        OutlinedButton(
                            onClick = {
                                onResetDemoBalance()
                                onDismiss()
                            },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626)),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDC2626)),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Reset to Zero Demo Money", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = MeskotStrings.get("cancel", currentLanguage), color = MutedText)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onSave(publicKey, secretKey, isLiveMode)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldDeep, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Save Configuration", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * Modal to buy Virtual Stars with Chapa payment.
 */
@Composable
fun BuyStarsModal(
    currentStars: Int,
    currentLanguage: AppLanguage,
    onDismiss: () -> Unit,
    onBuy: (starCount: Int, priceEtb: Double) -> Unit
) {
    data class StarPack(val stars: Int, val priceEtb: Double, val badge: String = "")
    val packs = listOf(
        StarPack(100, 50.0),
        StarPack(500, 200.0, "POPULAR"),
        StarPack(1500, 500.0, "BEST VALUE"),
        StarPack(4000, 1200.0, "VIP CREATOR")
    )
    var selectedPack by remember { mutableStateOf(packs[1]) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth().testTag("buy_stars_modal")
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "⭐ Top Up Stars",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Ink
                    )
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFFFEF3C7))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Current: $currentStars ⭐",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldDeep
                        )
                    }
                }

                Text(
                    text = "Send virtual gifts and support your favorite creators. Pay instantly via Chapa with Telebirr, CBE, or Cards.",
                    fontSize = 12.sp,
                    color = MutedText,
                    modifier = Modifier.padding(top = 4.dp, bottom = 14.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    packs.forEach { pack ->
                        val isSel = selectedPack == pack
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSel) GoldSurface else Paper2)
                                .border(1.5.dp, if (isSel) GoldDeep else LineBorder, RoundedCornerShape(10.dp))
                                .clickable { selectedPack = pack }
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = "⭐", fontSize = 18.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text(
                                            text = "${pack.stars} Stars",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Ink
                                        )
                                        if (pack.badge.isNotEmpty()) {
                                            Text(
                                                text = pack.badge,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = GoldDeep
                                            )
                                        }
                                    }
                                }
                                Text(
                                    text = "${pack.priceEtb.toInt()} ETB",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) GoldDeep else Ink
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = MeskotStrings.get("cancel", currentLanguage), color = MutedText)
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            onBuy(selectedPack.stars, selectedPack.priceEtb)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldDeep, contentColor = Color.White),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Pay ${selectedPack.priceEtb.toInt()} ETB via Chapa", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
