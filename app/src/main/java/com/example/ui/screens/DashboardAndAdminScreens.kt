package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.CloudDone
import com.example.data.AppLanguage
import com.example.data.GroupItem
import com.example.data.MeskotStrings
import com.example.data.Post
import com.example.data.User
import com.example.data.UserInsightsData
import com.example.data.UserEngagementData
import com.example.data.DemographicsItem
import com.example.ui.MeskotViewModel
import com.example.ui.components.UserAvatar
import com.example.ui.theme.CardBg
import com.example.ui.theme.CrossRed
import com.example.ui.theme.Gold
import com.example.ui.theme.GoldDeep
import com.example.ui.theme.Ink
import com.example.ui.theme.LineBorder
import com.example.ui.theme.MutedText
import com.example.ui.theme.Paper2

enum class DashboardTab {
    HOME,
    INSIGHTS,
    CONTENT,
    ENGAGEMENT
}

@Composable
fun DashboardScreen(
    viewModel: MeskotViewModel,
    currentUser: User?,
    myPosts: List<Post>,
    friendsCount: Int,
    currentLanguage: AppLanguage
) {
    var activeTab by remember { mutableStateOf(DashboardTab.HOME) }

    val userInsights by viewModel.userInsights.collectAsState()
    val userEngagement by viewModel.userEngagement.collectAsState()
    val isAnalyticsLoading by viewModel.isAnalyticsLoading.collectAsState()
    val isAnalyticsServerSynced by viewModel.isAnalyticsServerSynced.collectAsState()
    val timeRange by viewModel.insightsTimeRange.collectAsState()

    val totalLikes = myPosts.sumOf { it.reactions.size }
    val totalComments = myPosts.sumOf { it.commentCount }

    LaunchedEffect(activeTab) {
        if (activeTab == DashboardTab.INSIGHTS || activeTab == DashboardTab.ENGAGEMENT) {
            viewModel.refreshAnalytics()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("dashboard_screen")
    ) {
        // Header
        Column(modifier = Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Professional Dashboard",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Ink
                    )
                    Text(
                        text = "Track your engagement, audience growth, and community tips",
                        fontSize = 12.sp,
                        color = MutedText
                    )
                }
                if (isAnalyticsLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        strokeWidth = 2.dp,
                        color = GoldDeep
                    )
                }
            }
        }

        // Subtabs
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            shape = RoundedCornerShape(12.dp),
            color = CardBg,
            border = androidx.compose.foundation.BorderStroke(1.2.dp, com.example.ui.theme.GoldBorder.copy(alpha = 0.6f))
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                DashboardTab.values().forEach { tab ->
                    val isSelected = activeTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) com.example.ui.theme.MeskotLogoBrush else androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
                            .clickable { activeTab = tab }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MutedText
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            when (activeTab) {
                DashboardTab.HOME -> {
                    item {
                        // 4 Big Stat Cards
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                title = "Posts",
                                value = myPosts.size.toString(),
                                emoji = "📝",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Friends",
                                value = friendsCount.toString(),
                                emoji = "👥",
                                modifier = Modifier.weight(1f)
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(
                                title = "Total Likes",
                                value = totalLikes.toString(),
                                emoji = "❤️",
                                modifier = Modifier.weight(1f)
                            )
                            StatCard(
                                title = "Comments",
                                value = totalComments.toString(),
                                emoji = "💬",
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }

                DashboardTab.INSIGHTS -> {
                    // Server Connection & Time Range Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2E7D32))
                                )
                                Text(
                                    text = if (isAnalyticsServerSynced) "Firestore Server Synced (Live)" else "Connecting to Server...",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isAnalyticsServerSynced) Color(0xFF2E7D32) else MutedText
                                )
                            }
                            IconButton(
                                onClick = { viewModel.refreshAnalytics() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh from Server",
                                    tint = GoldDeep,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))

                        // Time Range Filters
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            listOf("Last 7 Days", "Last 28 Days", "Last 90 Days", "All Time").forEach { range ->
                                val selected = timeRange == range
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (selected) GoldDeep else CardBg)
                                        .border(1.dp, if (selected) GoldDeep else LineBorder, RoundedCornerShape(8.dp))
                                        .clickable { viewModel.setInsightsTimeRange(range) }
                                        .padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = range,
                                        fontSize = 10.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selected) Color.White else Ink,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Main Summary Card (Matches the UI)
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "$timeRange Summary",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 16.sp,
                                        color = Ink
                                    )
                                    Text(
                                        text = "Cloud Verified",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF2E7D32)
                                    )
                                }
                                Spacer(modifier = Modifier.height(10.dp))
                                InsightRow(
                                    label = "Post Reach",
                                    value = "+${userInsights.postReachGrowthPercent.toInt()}% vs previous month",
                                    isPositive = userInsights.postReachGrowthPercent >= 0
                                )
                                InsightRow(
                                    label = "Accounts Reached",
                                    value = "${java.text.NumberFormat.getIntegerInstance().format(userInsights.postReach)} accounts",
                                    isPositive = true
                                )
                                InsightRow(
                                    label = "Profile Views",
                                    value = "+${userInsights.profileViewsWeek} views this week",
                                    isPositive = true
                                )
                                InsightRow(
                                    label = "Interaction Rate",
                                    value = "${userInsights.interactionRate}% (${userInsights.interactionRateStatus})",
                                    isPositive = true
                                )
                                InsightRow(
                                    label = "Top Language",
                                    value = userInsights.topLanguages.joinToString(", ") { "${it.first} (${it.second}%)" },
                                    isPositive = true
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Reach by Content Format Card
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Reach by Content Format",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Ink
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                val totalReach = maxOf(1L, userInsights.reelsReachCount + userInsights.postReachCount + userInsights.storiesReachCount)

                                FormatReachRow(
                                    format = "📹 Reels",
                                    count = userInsights.reelsReachCount,
                                    percentage = ((userInsights.reelsReachCount.toDouble() / totalReach) * 100).toInt(),
                                    barColor = Color(0xFFE91E63)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                FormatReachRow(
                                    format = "📝 Posts & Photos",
                                    count = userInsights.postReachCount,
                                    percentage = ((userInsights.postReachCount.toDouble() / totalReach) * 100).toInt(),
                                    barColor = GoldDeep
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                FormatReachRow(
                                    format = "⏱️ Stories",
                                    count = userInsights.storiesReachCount,
                                    percentage = ((userInsights.storiesReachCount.toDouble() / totalReach) * 100).toInt(),
                                    barColor = Color(0xFF1976D2)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Audience Reach Split Card
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Audience Reach Distribution",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Ink
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Followers: ${userInsights.followersReachPercent}%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Ink
                                    )
                                    Text(
                                        text = "Non-followers: ${userInsights.nonFollowersReachPercent}%",
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MutedText
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(userInsights.followersReachPercent.toFloat())
                                            .fillMaxSize()
                                            .background(GoldDeep)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(userInsights.nonFollowersReachPercent.toFloat())
                                            .fillMaxSize()
                                            .background(Color(0xFF64B5F6))
                                    )
                                }
                            }
                        }
                    }
                }

                DashboardTab.CONTENT -> {
                    if (myPosts.isEmpty()) {
                        item {
                            EmptyNotice(text = "You haven't posted yet.")
                        }
                    } else {
                        items(myPosts.sortedByDescending { it.reactions.size + it.commentCount }) { post ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = CardBg),
                                border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Text(
                                        text = post.text.ifBlank { "Photo Post" },
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Ink
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                                        Text(text = "❤️ ${post.reactions.size} reactions", fontSize = 11.sp, color = MutedText)
                                        Text(text = "💬 ${post.commentCount} comments", fontSize = 11.sp, color = MutedText)
                                        if (post.tipTotal > 0) {
                                            Text(text = "💰 ${post.tipTotal.toInt()} ETB", fontSize = 11.sp, color = GoldDeep, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                DashboardTab.ENGAGEMENT -> {
                    // Server Connection & Refresh Header
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFF2E7D32))
                                )
                                Text(
                                    text = if (isAnalyticsServerSynced) "Real Firestore Demographics (Live)" else "Syncing Server Demographics...",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = if (isAnalyticsServerSynced) Color(0xFF2E7D32) else MutedText
                                )
                            }
                            IconButton(
                                onClick = { viewModel.refreshAnalytics() },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Demographics",
                                    tint = GoldDeep,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }

                    // Audience Demographics Card (Matches UI Screenshot)
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Audience Demographics",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Ink
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = "Top Cities:",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Ink
                                )
                                Spacer(modifier = Modifier.height(6.dp))

                                userEngagement.topCities.forEachIndexed { index, city ->
                                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(
                                                text = "${index + 1}. ${city.label}",
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = Ink
                                            )
                                            Text(
                                                text = "${city.percentage}%" + (if (city.count > 0) " (${city.count})" else ""),
                                                fontSize = 12.sp,
                                                color = MutedText
                                            )
                                        }
                                        Spacer(modifier = Modifier.height(4.dp))
                                        LinearProgressIndicator(
                                            progress = { (city.percentage / 100f).coerceIn(0f, 1f) },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(5.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = GoldDeep,
                                            trackColor = LineBorder
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Content Interactions Card
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Real-time Content Interactions",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Ink
                                )
                                Spacer(modifier = Modifier.height(10.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StatCard(
                                        title = "Reactions",
                                        value = userEngagement.totalReactions.toString(),
                                        emoji = "❤️",
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatCard(
                                        title = "Comments",
                                        value = userEngagement.totalComments.toString(),
                                        emoji = "💬",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                Spacer(modifier = Modifier.height(8.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    StatCard(
                                        title = "Shares",
                                        value = userEngagement.totalShares.toString(),
                                        emoji = "🔄",
                                        modifier = Modifier.weight(1f)
                                    )
                                    StatCard(
                                        title = "Tips (ETB)",
                                        value = "${userEngagement.totalTipsEtb.toInt()} ETB",
                                        emoji = "💰",
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Age & Gender Demographics Card
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Age & Gender Distribution",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Ink
                                )
                                Spacer(modifier = Modifier.height(10.dp))

                                Text(text = "Age Groups:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    userEngagement.ageDistribution.forEach { ageItem ->
                                        Column(
                                            modifier = Modifier
                                                .weight(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Paper2)
                                                .padding(vertical = 8.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(text = ageItem.label, fontSize = 11.sp, color = MutedText)
                                            Text(text = "${ageItem.percentage}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
                                        }
                                    }
                                }

                                Spacer(modifier = Modifier.height(12.dp))
                                Text(text = "Gender Split:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                                Spacer(modifier = Modifier.height(4.dp))
                                val malePct = userEngagement.genderDistribution.find { it.label == "Male" }?.percentage ?: 54
                                val femalePct = userEngagement.genderDistribution.find { it.label == "Female" }?.percentage ?: 46
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(text = "Male: $malePct%", fontSize = 11.sp, color = Ink)
                                    Text(text = "Female: $femalePct%", fontSize = 11.sp, color = Ink)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .weight(malePct.toFloat())
                                            .fillMaxSize()
                                            .background(Color(0xFF1976D2))
                                    )
                                    Box(
                                        modifier = Modifier
                                            .weight(femalePct.toFloat())
                                            .fillMaxSize()
                                            .background(Color(0xFFE91E63))
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }

                    // Peak Activity Window Card
                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Best Time to Post (Peak Activity)",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = Ink
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(text = "🕒", fontSize = 20.sp)
                                    Column {
                                        Text(
                                            text = userEngagement.peakActiveTime,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp,
                                            color = GoldDeep
                                        )
                                        Text(
                                            text = "Ethiopian & Diaspora audiences are most active during these hours.",
                                            fontSize = 11.sp,
                                            color = MutedText
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun FormatReachRow(
    format: String,
    count: Long,
    percentage: Int,
    barColor: Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = format, fontSize = 12.sp, color = Ink, fontWeight = FontWeight.Medium)
            Text(
                text = "${java.text.NumberFormat.getIntegerInstance().format(count)} viewers ($percentage%)",
                fontSize = 12.sp,
                color = MutedText
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (percentage / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = barColor,
            trackColor = LineBorder
        )
    }
}


@Composable
fun StatCard(
    title: String,
    value: String,
    emoji: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 12.sp, color = MutedText, fontWeight = FontWeight.Medium)
                Text(text = emoji, fontSize = 18.sp)
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = value, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Ink)
        }
    }
}

@Composable
fun InsightRow(
    label: String,
    value: String,
    isPositive: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 13.sp, color = MutedText)
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isPositive) GoldDeep else CrossRed
        )
    }
}

enum class AdminTab {
    OVERVIEW,
    USERS,
    POSTS,
    GROUPS
}

@Composable
fun AdminScreen(
    viewModel: MeskotViewModel,
    users: List<User>,
    posts: List<Post>,
    groups: List<GroupItem>,
    currentLanguage: AppLanguage
) {
    var adminTab by remember { mutableStateOf(AdminTab.OVERVIEW) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("admin_screen")
    ) {
        // Header
        Column(modifier = Modifier.padding(start = 16.dp, top = 14.dp, end = 16.dp)) {
            Text(
                text = "🛡️ " + MeskotStrings.get("adminPanel", currentLanguage),
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Serif,
                color = Ink
            )
            Text(
                text = MeskotStrings.get("systemManagement", currentLanguage),
                fontSize = 12.sp,
                color = MutedText
            )
        }

        // Subtabs
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            shape = RoundedCornerShape(12.dp),
            color = CardBg,
            border = androidx.compose.foundation.BorderStroke(1.2.dp, com.example.ui.theme.GoldBorder.copy(alpha = 0.6f))
        ) {
            Row(modifier = Modifier.padding(4.dp)) {
                AdminTab.values().forEach { tab ->
                    val isSelected = adminTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) com.example.ui.theme.MeskotLogoBrush else androidx.compose.ui.graphics.Brush.linearGradient(listOf(Color.Transparent, Color.Transparent)))
                            .clickable { adminTab = tab }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = tab.name.lowercase().replaceFirstChar { it.uppercase() },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isSelected) Color.White else MutedText
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp)
        ) {
            when (adminTab) {
                AdminTab.OVERVIEW -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(title = "Total Users", value = users.size.toString(), emoji = "👥", modifier = Modifier.weight(1f))
                            StatCard(title = "Active Posts", value = posts.size.toString(), emoji = "📝", modifier = Modifier.weight(1f))
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            StatCard(title = "Total Groups", value = groups.size.toString(), emoji = "👪", modifier = Modifier.weight(1f))
                            val totalChapaTips = posts.sumOf { it.tipTotal }
                            StatCard(title = "Chapa Tips", value = "${totalChapaTips.toInt()} ETB", emoji = "💰", modifier = Modifier.weight(1f))
                        }
                    }
                }

                AdminTab.USERS -> {
                    items(users) { user ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                UserAvatar(photoUrl = user.photoUrl, name = user.displayName, size = 44)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(text = user.displayName, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                                        if (user.isAdmin) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "🛡️ Admin", fontSize = 10.sp, color = GoldDeep, fontWeight = FontWeight.Bold)
                                        }
                                        if (user.isSuspended) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(text = "🚫 Suspended", fontSize = 10.sp, color = CrossRed, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    Text(text = user.email, fontSize = 11.sp, color = MutedText)
                                }

                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    OutlinedButton(
                                        onClick = { viewModel.adminToggleSuspend(user.uid) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(
                                            text = if (user.isSuspended) "Restore" else "Suspend",
                                            fontSize = 10.sp,
                                            color = if (user.isSuspended) GoldDeep else CrossRed
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { viewModel.adminToggleAdmin(user.uid) },
                                        shape = RoundedCornerShape(8.dp),
                                        modifier = Modifier.height(32.dp)
                                    ) {
                                        Text(
                                            text = if (user.isAdmin) "Revoke" else "Make Admin",
                                            fontSize = 10.sp,
                                            color = Ink
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                AdminTab.POSTS -> {
                    items(posts) { post ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = post.authorName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
                                    Text(
                                        text = post.text.ifBlank { "[Media attachment]" },
                                        fontSize = 12.sp,
                                        color = MutedText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                Button(
                                    onClick = { viewModel.deletePost(post.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrossRed, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(text = "Delete", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }

                AdminTab.GROUPS -> {
                    items(groups) { group ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = group.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Ink)
                                    Text(text = "${group.memberCount} members", fontSize = 12.sp, color = MutedText)
                                }

                                Button(
                                    onClick = { viewModel.adminDeleteGroup(group.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = CrossRed, contentColor = Color.White),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(text = "Delete", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}
