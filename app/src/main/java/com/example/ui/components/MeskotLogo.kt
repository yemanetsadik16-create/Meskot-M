package com.example.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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

