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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.AppLanguage
import com.example.data.CreatorPayoutRecord
import com.example.data.MembershipTier
import com.example.data.MeskotStrings
import com.example.ui.MeskotViewModel
import com.example.ui.components.CreatorPayoutModal
import com.example.ui.theme.CardBg
import com.example.ui.theme.Gold
import com.example.ui.theme.GoldDeep
import com.example.ui.theme.GoldSurface
import com.example.ui.theme.Ink
import com.example.ui.theme.LineBorder
import com.example.ui.theme.MutedText
import com.example.ui.theme.Paper2

enum class CreatorTab(val title: String) {
    OVERVIEW("Overview & Streams"),
    PAYOUTS("Payout History"),
    MEMBERSHIPS("VIP Tiers")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreatorDashboardScreen(
    viewModel: MeskotViewModel,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    val payoutHistory by viewModel.payoutHistory.collectAsState()
    val grossEarnings by viewModel.creatorGrossEarnings.collectAsState()
    val netBalance by viewModel.creatorNetBalance.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showPayoutModal by remember { mutableStateOf(false) }

    if (showPayoutModal) {
        CreatorPayoutModal(
            netBalanceEtb = netBalance,
            currentLanguage = currentLanguage,
            onDismiss = { showPayoutModal = false },
            onRequestPayout = { method, amount ->
                viewModel.requestPayout(method, amount)
                showPayoutModal = false
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "👑 Creator Monetization Studio",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Ink
                        )
                        Text(
                            text = "Subscriptions · Stars · Automated Payouts",
                            fontSize = 11.sp,
                            color = MutedText
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Ink
                        )
                    }
                },
                actions = {
                    Button(
                        onClick = { showPayoutModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldDeep),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(text = "💸 Cash Out", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = CardBg)
            )
        },
        containerColor = Paper2,
        modifier = modifier.fillMaxSize().testTag("creator_dashboard_screen")
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(2.dp))
                // KPI Financial Summary Card
                Card(
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "NET CREATOR BALANCE",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF94A3B8)
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFF065F46))
                                    .padding(horizontal = 8.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Ready to Withdraw",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "${String.format(java.util.Locale.US, "%,.2f", netBalance)} ETB",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFBBF24),
                            fontFamily = FontFamily.Serif
                        )

                        Spacer(modifier = Modifier.height(14.dp))
                        HorizontalDivider(color = Color(0xFF334155), thickness = 0.8.dp)
                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(text = "Gross Revenue", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(
                                    text = "${String.format(java.util.Locale.US, "%,.2f", grossEarnings)} ETB",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Column {
                                Text(text = "Platform Fee (20%)", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                val fee = grossEarnings * 0.20
                                Text(
                                    text = "-${String.format(java.util.Locale.US, "%,.2f", fee)} ETB",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF87171)
                                )
                            }
                            Column {
                                Text(text = "Virtual Stars", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                Text(
                                    text = "4,850 ⭐",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFBBF24)
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Tab Selection
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CardBg,
                    contentColor = GoldDeep,
                    indicator = { tabPositions ->
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                            color = GoldDeep
                        )
                    },
                    modifier = Modifier.clip(RoundedCornerShape(12.dp))
                ) {
                    CreatorTab.values().forEachIndexed { index, tab ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = tab.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        )
                    }
                }
            }

            when (CreatorTab.values()[selectedTab]) {
                CreatorTab.OVERVIEW -> {
                    item {
                        Text(
                            text = "Revenue Breakdown by Channel",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                    }

                    item {
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = CardBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                StreamRow(
                                    icon = "👑",
                                    title = "Tiered VIP Memberships",
                                    subtitle = "42 Active Recurring Fan Subscribers",
                                    amountEtb = grossEarnings * 0.52,
                                    pct = "52%"
                                )
                                HorizontalDivider(color = LineBorder, thickness = 0.5.dp)
                                StreamRow(
                                    icon = "⭐",
                                    title = "Virtual Stars & Animated Gifts",
                                    subtitle = "4,850 Stars received on live streams & feed",
                                    amountEtb = grossEarnings * 0.28,
                                    pct = "28%"
                                )
                                HorizontalDivider(color = LineBorder, thickness = 0.5.dp)
                                StreamRow(
                                    icon = "💰",
                                    title = "Direct Tips (Chapa / CBE)",
                                    subtitle = "One-off reader tips and donations",
                                    amountEtb = grossEarnings * 0.14,
                                    pct = "14%"
                                )
                                HorizontalDivider(color = LineBorder, thickness = 0.5.dp)
                                StreamRow(
                                    icon = "📢",
                                    title = "Ad Revenue Sharing & In-Stream",
                                    subtitle = "Monetized views and sponsored impressions",
                                    amountEtb = grossEarnings * 0.06,
                                    pct = "6%"
                                )
                            }
                        }
                    }

                    item {
                        // Automated Payout Threshold Info Card
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF9C3)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                                Text(text = "⚡", fontSize = 24.sp)
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Automated Threshold Payouts Enabled",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp,
                                        color = Color(0xFF854D0E)
                                    )
                                    Text(
                                        text = "Balances reaching 1,000 ETB automatically settle to your primary Telebirr/CBE account every Monday. Instant cash out is always available above 100 ETB.",
                                        fontSize = 11.sp,
                                        color = Color(0xFFA16207),
                                        lineHeight = 15.sp
                                    )
                                }
                            }
                        }
                    }
                }

                CreatorTab.PAYOUTS -> {
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Disbursement History (${payoutHistory.size})",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            Text(
                                text = "Min Limit: 100 ETB",
                                fontSize = 11.sp,
                                color = MutedText
                            )
                        }
                    }

                    items(payoutHistory) { record ->
                        PayoutHistoryCard(record = record)
                    }
                }

                CreatorTab.MEMBERSHIPS -> {
                    item {
                        Text(
                            text = "Fan Subscription Tiers & Perks",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                    }

                    val tiers = listOf(MembershipTier.BRONZE, MembershipTier.SILVER, MembershipTier.GOLD)
                    items(tiers) { tier ->
                        MembershipTierManagementCard(tier = tier)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

@Composable
fun StreamRow(
    icon: String,
    title: String,
    subtitle: String,
    amountEtb: Double,
    pct: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Text(text = icon, fontSize = 24.sp)
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
                Text(text = subtitle, fontSize = 11.sp, color = MutedText)
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${String.format(java.util.Locale.US, "%,.2f", amountEtb)} ETB",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = GoldDeep
            )
            Text(text = pct, fontSize = 10.sp, color = MutedText, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun PayoutHistoryCard(record: CreatorPayoutRecord) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
        modifier = Modifier.fillMaxWidth()
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
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (record.status == "COMPLETED") Color(0xFFD1FAE5) else Color(0xFFFEF3C7)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (record.status == "COMPLETED") "✓" else "⏳",
                        color = if (record.status == "COMPLETED") Color(0xFF059669) else Color(0xFFD97706),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "${record.method}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                    Text(
                        text = "Ref: ${record.referenceId} · ${record.createdAt.take(10)}",
                        fontSize = 11.sp,
                        color = MutedText
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format(java.util.Locale.US, "%,.2f", record.amountEtb)} ETB",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (record.status == "COMPLETED") Color(0xFFD1FAE5) else Color(0xFFFEF3C7))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = record.status,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (record.status == "COMPLETED") Color(0xFF065F46) else Color(0xFF92400E)
                    )
                }
            }
        }
    }
}

@Composable
fun MembershipTierManagementCard(tier: MembershipTier) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = tier.badge, fontSize = 22.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = tier.label, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Ink)
                        Text(text = "Recurring fan billing model", fontSize = 11.sp, color = MutedText)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldSurface)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "${tier.monthlyPriceEtb.toInt()} ETB / month",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldDeep
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Text(text = "Member Privileges:", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = MutedText)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = tier.perksSummary,
                fontSize = 12.sp,
                color = Ink,
                lineHeight = 16.sp
            )
        }
    }
}
