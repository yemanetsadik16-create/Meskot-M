package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.HeadsetMic
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.data.MeskotStrings
import com.example.data.User
import com.example.data.VerificationStatus
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Official Meta Verified Blue Palette
val MetaBlue = Color(0xFF0866FF)
val MetaBlueDark = Color(0xFF0053D6)
val MetaDarkBg = Color(0xFF1C1C1E)
val MetaDarkCard = Color(0xFF2C2C2E)
val MetaDarkSecondary = Color(0xFF8E8E93)
val MetaDarkBorder = Color(0xFF3A3A3C)

// -----------------------------------------------------------------------------
// 1. DYNAMIC BADGE DISPLAY: VerifiedBadge
// -----------------------------------------------------------------------------
@Composable
fun VerifiedBadge(
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    tint: Color = MetaBlue,
    onClick: (() -> Unit)? = null
) {
    var showExplanation by remember { mutableStateOf(false) }

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .clickable(enabled = onClick != null || true) {
                if (onClick != null) onClick() else showExplanation = true
            }
    ) {
        Icon(
            imageVector = Icons.Default.Verified,
            contentDescription = "Meta Verified Badge",
            tint = tint,
            modifier = Modifier.fillMaxSize()
        )
    }

    if (showExplanation) {
        AlertDialog(
            onDismissRequest = { showExplanation = false },
            confirmButton = {
                TextButton(onClick = { showExplanation = false }) {
                    Text("OK", color = MetaBlue, fontWeight = FontWeight.Bold)
                }
            },
            icon = {
                Icon(
                    imageVector = Icons.Default.Verified,
                    contentDescription = null,
                    tint = MetaBlue,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Meta Verified",
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "This account has subscribed to Meta Verified and confirmed their authenticity with government ID verification and proactive protection.",
                    fontSize = 13.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            },
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// -----------------------------------------------------------------------------
// 2. REUSABLE UI WRAPPER: ProfileName
// -----------------------------------------------------------------------------
@Composable
fun ProfileName(
    name: String,
    modifier: Modifier = Modifier,
    isVerified: Boolean = false,
    isAdmin: Boolean = false,
    fontSize: TextUnit = 15.sp,
    fontWeight: FontWeight = FontWeight.Bold,
    color: Color = Color.Unspecified,
    badgeSize: Dp = 15.dp,
    maxLines: Int = 1,
    overflow: TextOverflow = TextOverflow.Ellipsis,
    onBadgeClick: (() -> Unit)? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Text(
            text = name,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color,
            maxLines = maxLines,
            overflow = overflow
        )

        if (isVerified) {
            Spacer(modifier = Modifier.width(4.dp))
            VerifiedBadge(
                size = badgeSize,
                onClick = onBadgeClick
            )
        }

        if (isAdmin) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(text = "🛡️", fontSize = (fontSize.value - 2).sp)
        }
    }
}

// -----------------------------------------------------------------------------
// 3. REUSABLE UI WRAPPER: PostHeader
// -----------------------------------------------------------------------------
@Composable
fun PostHeader(
    authorName: String,
    authorPhotoUrl: String,
    isVerified: Boolean,
    timestampText: String,
    modifier: Modifier = Modifier,
    isAdmin: Boolean = false,
    isBoosted: Boolean = false,
    onAuthorClick: () -> Unit = {},
    onOptionsClick: () -> Unit = {}
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clickable { onAuthorClick() }
                .weight(1f)
        ) {
            UserAvatar(photoUrl = authorPhotoUrl, name = authorName, size = 42)

            Spacer(modifier = Modifier.width(10.dp))

            Column {
                ProfileName(
                    name = authorName,
                    isVerified = isVerified,
                    isAdmin = isAdmin,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timestampText,
                        fontSize = 12.sp,
                        color = Color.Gray
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.Public,
                        contentDescription = "Public",
                        modifier = Modifier.size(11.dp),
                        tint = Color.Gray
                    )
                    if (isBoosted) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(Color(0xFFB8863A))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "🚀 Boosted",
                                fontSize = 9.sp,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        IconButton(onClick = onOptionsClick, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.MoreVert,
                contentDescription = "Options",
                tint = Color.Gray,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// 4. FRONTEND REQUIREMENTS: Dark-Themed Meta Verified Bottom-Sheet Modal
//    Background: #1C1C1E, Top-Left 'X', Blue Checkmark Badge Hero Icon,
//    Benefits List, Native IAP / Billing Handling, Disclaimers, CTA with States.
// -----------------------------------------------------------------------------
@Composable
fun MetaVerifiedBottomSheetModal(
    currentUser: User?,
    onDismiss: () -> Unit,
    onSubscribeConfirmed: (paymentMethod: String, planId: String) -> Unit,
    onCancelSubscription: () -> Unit = {}
) {
    val coroutineScope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(false) }
    var selectedPaymentMethod by remember { mutableStateOf("GOOGLE_PLAY") } // "GOOGLE_PLAY", "APPLE_IAP", "CHAPA"
    var showSuccessCelebration by remember { mutableStateOf(false) }

    val isAlreadyVerified = currentUser?.isVerified == true || currentUser?.verificationStatus == VerificationStatus.VERIFIED
    val isPending = currentUser?.verificationStatus == VerificationStatus.PENDING

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.65f)),
            contentAlignment = Alignment.BottomCenter
        ) {
            Surface(
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                color = MetaDarkBg,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.92f)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 22.dp, vertical = 16.dp)
                ) {
                    // Top Bar: Drag handle & Top-Left 'X' close button
                    Box(modifier = Modifier.fillMaxWidth()) {
                        // Drag Indicator
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .width(36.dp)
                                .height(4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(Color.DarkGray)
                        )

                        // Top-left 'X' Close Button
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(top = 4.dp)
                                .size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        // Meta Verified Top Pill
                        Box(
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(top = 8.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(MetaDarkCard)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "SUBSCRIPTION",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MetaBlue,
                                letterSpacing = 1.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Scrollable Body Content
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // HERO SECTION: Blue checkmark badge hero icon with glowing ring
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(80.dp)
                                .shadow(24.dp, CircleShape, spotColor = MetaBlue)
                                .clip(CircleShape)
                                .background(
                                    Brush.radialGradient(
                                        colors = listOf(
                                            MetaBlue,
                                            MetaBlueDark
                                        )
                                    )
                                )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Verified,
                                contentDescription = "Meta Verified Hero",
                                tint = Color.White,
                                modifier = Modifier.size(46.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Title & Subtitle
                        Text(
                            text = "Build trust with Meta Verified",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            lineHeight = 28.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "A verified badge, increased account protection, direct support and more for ${currentUser?.displayName ?: "your account"}.",
                            fontSize = 13.sp,
                            color = MetaDarkSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(horizontal = 12.dp)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // SUCCESS OR PENDING BANNER
                        if (isAlreadyVerified) {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF0D3823)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = "Active Meta Verified Subscriber",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Your badge is live and identity protections are active on your profile.",
                                            fontSize = 11.sp,
                                            color = Color(0xFFA7F3D0)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        } else if (isPending) {
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF38290D)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = Color(0xFFF59E0B),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Verification In Progress",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp,
                                            color = Color.White
                                        )
                                        Text(
                                            text = "Your submitted ID and profile details are currently undergoing standard review.",
                                            fontSize = 11.sp,
                                            color = Color(0xFFFDE68A)
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // LIST OF BENEFITS
                        BenefitItem(
                            icon = Icons.Default.Verified,
                            title = "A verified badge",
                            description = "Your audience can trust that you're a real person sharing your real stories with government-backed identity confirmation."
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        BenefitItem(
                            icon = Icons.Default.Security,
                            title = "Proactive account protection",
                            description = "Get protection from impersonation with proactive monitoring for accounts that might target growing audiences."
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        BenefitItem(
                            icon = Icons.Default.HeadsetMic,
                            title = "Direct customer support",
                            description = "Get help when you need it from a real person on common account issues that matter to creators."
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        BenefitItem(
                            icon = Icons.Default.Star,
                            title = "Increased reach in comments & search",
                            description = "Stand out with increased visibility in comments, search results, and recommended explore reels."
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // PAYMENT GATEWAY / STORE SELECTION
                        if (!isAlreadyVerified && !isPending) {
                            Text(
                                text = "Choose Billing Gateway",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.align(Alignment.Start)
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                PaymentOptionCard(
                                    title = "Google Play",
                                    subtitle = "$14.99 / mo",
                                    isSelected = selectedPaymentMethod == "GOOGLE_PLAY",
                                    onClick = { selectedPaymentMethod = "GOOGLE_PLAY" },
                                    modifier = Modifier.weight(1f)
                                )

                                PaymentOptionCard(
                                    title = "Apple / Web",
                                    subtitle = "$14.99 / mo",
                                    isSelected = selectedPaymentMethod == "APPLE_IAP",
                                    onClick = { selectedPaymentMethod = "APPLE_IAP" },
                                    modifier = Modifier.weight(1f)
                                )

                                PaymentOptionCard(
                                    title = "Chapa / Birr",
                                    subtitle = "499 ETB / mo",
                                    isSelected = selectedPaymentMethod == "CHAPA",
                                    onClick = { selectedPaymentMethod = "CHAPA" },
                                    modifier = Modifier.weight(1f)
                                )
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                        }

                        // LEGAL DISCLAIMERS
                        Text(
                            text = "Subscriptions renew automatically each month unless cancelled at least 24 hours prior to the renewal date. Government ID verification is required to complete enrollment. Cancel anytime in Google Play Store subscriptions or Account Settings.",
                            fontSize = 11.sp,
                            color = MetaDarkSecondary,
                            textAlign = TextAlign.Center,
                            lineHeight = 15.sp,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // BOTTOM ACTION BAR & PROMINENT CTA BUTTON
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                    ) {
                        if (isAlreadyVerified) {
                            Button(
                                onClick = { /* Already verified */ },
                                enabled = false,
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = Color(0xFF2C2C2E),
                                    disabledContentColor = Color(0xFF10B981)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Verified,
                                        contentDescription = null,
                                        tint = Color(0xFF10B981),
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Subscribed · Verified Badge Active ✓",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            TextButton(
                                onClick = {
                                    onCancelSubscription()
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Cancel Meta Verified Subscription",
                                    color = Color(0xFFEF4444),
                                    fontSize = 12.sp
                                )
                            }
                        } else if (isPending) {
                            Button(
                                onClick = { /* Pending review */ },
                                enabled = false,
                                colors = ButtonDefaults.buttonColors(
                                    disabledContainerColor = Color(0xFF2C2C2E),
                                    disabledContentColor = Color(0xFFF59E0B)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                Text(
                                    text = "Verification Under Review · Pending",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                        } else {
                            val buttonText = when (selectedPaymentMethod) {
                                "CHAPA" -> "Subscribe for 499 ETB / month"
                                else -> "Subscribe for $14.99 / month"
                            }

                            Button(
                                onClick = {
                                    if (!isProcessing) {
                                        isProcessing = true
                                        coroutineScope.launch {
                                            // Simulate Native Billing SDK Purchase Flow
                                            delay(1200)
                                            isProcessing = false
                                            onSubscribeConfirmed(selectedPaymentMethod, "meta_verified_monthly")
                                            onDismiss()
                                        }
                                    }
                                },
                                enabled = !isProcessing,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MetaBlue,
                                    contentColor = Color.White
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                            ) {
                                if (isProcessing) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        strokeWidth = 2.dp,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Connecting to Google Play Billing...",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                } else {
                                    Text(
                                        text = buttonText,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold
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

// -----------------------------------------------------------------------------
// HELPER COMPONENT: Benefit Item Row
// -----------------------------------------------------------------------------
@Composable
private fun BenefitItem(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(MetaDarkCard)
            .padding(14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MetaDarkBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MetaBlue,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = description,
                fontSize = 12.sp,
                color = MetaDarkSecondary,
                lineHeight = 16.sp
            )
        }
    }
}

// -----------------------------------------------------------------------------
// HELPER COMPONENT: Payment Option Card
// -----------------------------------------------------------------------------
@Composable
private fun PaymentOptionCard(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(10.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MetaBlue.copy(alpha = 0.15f) else MetaDarkCard
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.5.dp,
            if (isSelected) MetaBlue else MetaDarkBorder
        ),
        modifier = modifier
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp, horizontal = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) Color.White else MetaDarkSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) MetaBlue else Color.White
            )
        }
    }
}
