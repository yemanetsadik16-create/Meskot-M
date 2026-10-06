package com.example.ui.components

import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.GoldDeep
import com.example.ui.theme.Ink
import com.example.ui.theme.MutedText

/**
 * Modern, attractive Meskot Logo Badge.
 * Features the sleek, stylized arched window & "M" emblem with modern gradient depth.
 */
@Composable
fun MeskotLogoBadge(
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    showBorder: Boolean = true,
    elevation: Dp = 1.dp
) {
    val cornerRadius = size * 0.26f

    Box(
        modifier = modifier
            .size(size)
            .shadow(elevation, RoundedCornerShape(cornerRadius), clip = false)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.White)
            .then(
                if (showBorder) {
                    Modifier.border(
                        1.dp,
                        com.example.ui.theme.GoldBorder,
                        RoundedCornerShape(cornerRadius)
                    )
                } else Modifier
            )
            .testTag("meskot_logo_badge"),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_meskot_logo),
            contentDescription = "Meskot Logo",
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(cornerRadius)),
            contentScale = ContentScale.Crop
        )
    }
}


/**
 * Full Meskot Logo with modern typography pairing.
 */
@Composable
fun MeskotLogoHeader(
    modifier: Modifier = Modifier,
    size: Dp = 34.dp,
    onClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
            .padding(end = 2.dp)
            .testTag("meskot_logo_header")
    ) {
        MeskotLogoBadge(size = size)

        Spacer(modifier = Modifier.width(8.dp))

        Column {
            Text(
                text = "Meskot",
                fontSize = 19.sp,
                fontWeight = FontWeight.ExtraBold,
                fontFamily = FontFamily.SansSerif,
                color = Ink,
                letterSpacing = (-0.4).sp
            )
            Text(
                text = "· መስኮትህ ራስህ",
                fontSize = 10.5.sp,
                color = GoldDeep,
                fontWeight = FontWeight.SemiBold,
                lineHeight = 12.sp
            )
        }
    }
}

/**
 * Large Hero Meskot Emblem for Splash, Auth, and About dialogs.
 */
@Composable
fun MeskotHeroEmblem(
    modifier: Modifier = Modifier,
    size: Dp = 80.dp
) {
    val cornerRadius = size * 0.28f

    Box(
        modifier = modifier
            .size(size)
            .shadow(6.dp, RoundedCornerShape(cornerRadius), ambientColor = Color(0x22C48F37), spotColor = Color(0x33C48F37))
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.White)
            .border(
                1.5.dp,
                com.example.ui.theme.GoldBorder,
                RoundedCornerShape(cornerRadius)
            )
            .testTag("meskot_hero_emblem"),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_meskot_logo),
            contentDescription = "Meskot Emblem",
            modifier = Modifier
                .size(size)
                .clip(RoundedCornerShape(cornerRadius)),
            contentScale = ContentScale.Crop
        )
    }
}

/**
 * Facebook-style Modern Startup Splash Animation for Meskot.
 * Features a centered circular Meskot logo medallion with a sleek animated gold ring
 * and a 5-dot sequential wave loading indicator right below it.
 */
@Composable
fun MeskotSplashScreen(
    modifier: Modifier = Modifier
) {
    val infiniteTransition = androidx.compose.animation.core.rememberInfiniteTransition(label = "meskot_splash_transition")

    // Continuous 0..5f progress to animate the 5 sequential dots smoothly like Facebook
    val dotCycleProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 5f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = 1250,
                easing = androidx.compose.animation.core.LinearEasing
            ),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "dot_cycle"
    )

    // Subtle breathing pulse on the circular logo medallion
    val logoPulseScale by infiniteTransition.animateFloat(
        initialValue = 0.97f,
        targetValue = 1.03f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = 900,
                easing = androidx.compose.animation.core.FastOutSlowInEasing
            ),
            repeatMode = androidx.compose.animation.core.RepeatMode.Reverse
        ),
        label = "logo_pulse"
    )

    // Rotating shimmer angle for the circular border ring
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = androidx.compose.animation.core.infiniteRepeatable(
            animation = androidx.compose.animation.core.tween(
                durationMillis = 2200,
                easing = androidx.compose.animation.core.LinearEasing
            ),
            repeatMode = androidx.compose.animation.core.RepeatMode.Restart
        ),
        label = "ring_rotation"
    )

    // Initial entrance scale animation when the app launches
    val entranceScale = androidx.compose.runtime.remember { androidx.compose.animation.core.Animatable(0.82f) }
    androidx.compose.runtime.LaunchedEffect(Unit) {
        entranceScale.animateTo(
            targetValue = 1f,
            animationSpec = androidx.compose.animation.core.spring(
                dampingRatio = androidx.compose.animation.core.Spring.DampingRatioMediumBouncy,
                stiffness = androidx.compose.animation.core.Spring.StiffnessLow
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .testTag("meskot_splash_screen"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            // Centered Circular Meskot Logo Medallion with subtle outer ring (matching screenshot)
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .graphicsLayer {
                        val combinedScale = entranceScale.value * logoPulseScale
                        scaleX = combinedScale
                        scaleY = combinedScale
                    },
                contentAlignment = Alignment.Center
            ) {
                // Soft ambient glow halo behind the circular logo
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip( androidx.compose.foundation.shape.CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    com.example.ui.theme.GoldLight.copy(alpha = 0.22f),
                                    Color.Transparent
                                )
                            )
                        )
                )

                // Crisp circular ring with rotating luminous highlight
                androidx.compose.foundation.Canvas(
                    modifier = Modifier.size(92.dp)
                ) {
                    val strokeWidth = 2.dp.toPx()
                    // Base subtle grey-gold ring like the Facebook circle outline
                    drawCircle(
                        color = Color(0xFFD0D5DD),
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
                    )
                    // Sweeping modern gold arc around the circle
                    drawArc(
                        brush = Brush.sweepGradient(
                            listOf(
                                Color.Transparent,
                                com.example.ui.theme.GoldLight.copy(alpha = 0.5f),
                                com.example.ui.theme.Gold,
                                com.example.ui.theme.GoldDeep,
                                Color.Transparent
                            )
                        ),
                        startAngle = ringRotation,
                        sweepAngle = 135f,
                        useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = 2.4.dp.toPx(),
                            cap = androidx.compose.ui.graphics.StrokeCap.Round
                        )
                    )
                }

                // Inner Circular Meskot Logo
                Box(
                    modifier = Modifier
                        .size(82.dp)
                        .shadow(
                            elevation = 4.dp,
                            shape = androidx.compose.foundation.shape.CircleShape,
                            ambientColor = Color(0x22C48F37),
                            spotColor = Color(0x33C48F37)
                        )
                        .clip(androidx.compose.foundation.shape.CircleShape)
                        .background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.img_meskot_logo),
                        contentDescription = "Meskot Logo",
                        modifier = Modifier
                            .size(82.dp)
                            .clip(androidx.compose.foundation.shape.CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(modifier = Modifier.height(34.dp))

            // 5-Dot Sequential Loading Indicator (matching the Facebook splash dots in screenshot)
            Row(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.testTag("meskot_splash_dots")
            ) {
                for (index in 0 until 5) {
                    // Calculate cyclic distance between current dot index and active wave position
                    val rawDiff = kotlin.math.abs(dotCycleProgress - index)
                    val cyclicDiff = kotlin.math.min(rawDiff, 5f - rawDiff)
                    // Active intensity: 1f when active, smoothly falling off to 0f for inactive dots
                    val intensity = (1f - (cyclicDiff / 0.85f)).coerceIn(0f, 1f)

                    val dotScale = 1f + (0.28f * intensity)
                    val dotColor = androidx.compose.ui.graphics.lerp(
                        start = Color(0xFFD8DADF), // Facebook inactive light grey dot
                        stop = com.example.ui.theme.Gold, // Meskot active luminous brand color
                        fraction = intensity
                    )

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .graphicsLayer {
                                scaleX = dotScale
                                scaleY = dotScale
                            }
                            .clip(androidx.compose.foundation.shape.CircleShape)
                            .background(dotColor)
                    )
                }
            }
        }
    }
}

