package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.LiveStreamSession
import com.example.data.MeskotStrings
import kotlinx.coroutines.delay

/**
 * TikTok-style Live Broadcast Alert Banner:
 * Slides down smoothly from the top of the screen when a followed creator or friend enters live.
 * Displays glowing pulsating red LIVE indicator, creator avatar, title, and an instant "Watch" action.
 */
@Composable
fun TikTokLiveAlertBanner(
    session: LiveStreamSession?,
    currentLanguage: AppLanguage = AppLanguage.EN,
    onWatch: (LiveStreamSession) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Auto-dismiss after 8 seconds
    LaunchedEffect(session?.id) {
        if (session != null) {
            delay(8000L)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = session != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = tween(350, easing = FastOutSlowInEasing)
        ) + fadeIn(tween(250)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(300, easing = FastOutSlowInEasing)
        ) + fadeOut(tween(200)),
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 12.dp, vertical = 8.dp)
            .testTag("tiktok_live_alert_banner")
    ) {
        if (session != null) {
            val infiniteTransition = rememberInfiniteTransition(label = "live_pulse")
            val pulseScale by infiniteTransition.animateFloat(
                initialValue = 1f,
                targetValue = 1.18f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "scale"
            )
            val pulseAlpha by infiniteTransition.animateFloat(
                initialValue = 0.9f,
                targetValue = 0.25f,
                animationSpec = infiniteRepeatable(
                    animation = tween(900, easing = FastOutSlowInEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "alpha"
            )

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onWatch(session) },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xF5181818) // Deep translucent obsidian
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    Brush.horizontalGradient(
                        listOf(
                            Color(0xFFFF1744), // Vibrant Live Red
                            Color(0xFFD4AF37), // Meskot Gold
                            Color(0xFFFF1744)
                        )
                    )
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = 10.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pulsing Avatar Container
                    Box(contentAlignment = Alignment.Center) {
                        // Pulsing outer halo
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .scale(pulseScale)
                                .clip(CircleShape)
                                .background(Color(0xFFFF1744).copy(alpha = pulseAlpha))
                        )

                        // Avatar
                        UserAvatar(
                            photoUrl = session.hostPhoto,
                            name = session.hostName,
                            size = 46,
                            modifier = Modifier.border(2.dp, Color(0xFFFF1744), CircleShape)
                        )

                        // Tiny LIVE tag badge below avatar
                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .clip(RoundedCornerShape(6.dp))
                                .background(Color(0xFFFF1744))
                                .padding(horizontal = 4.dp, vertical = 1.dp)
                        ) {
                            Text(
                                text = "LIVE",
                                color = Color.White,
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 0.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    // Host & Stream Info
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // Blinking Live Dot
                            Box(
                                modifier = Modifier
                                    .size(7.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFF1744))
                            )
                            Text(
                                text = session.hostName,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = if (session.title.isNotBlank()) session.title else MeskotStrings.get("startedLive", currentLanguage),
                            color = Color.White.copy(alpha = 0.85f),
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // "Watch" Action Button (TikTok-style pill button)
                    Button(
                        onClick = { onWatch(session) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFFFF1744),
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(18.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 12.dp,
                            vertical = 6.dp
                        ),
                        modifier = Modifier.testTag("tiktok_live_watch_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = MeskotStrings.get("watchLive", currentLanguage),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(2.dp))

                    // Dismiss Button (✕)
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("tiktok_live_dismiss_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Dismiss",
                            tint = Color.White.copy(alpha = 0.65f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}
