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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Stars
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.window.Dialog
import com.example.data.AppLanguage
import com.example.data.ContentFormatMetric
import com.example.data.CreatorPayoutAccount
import com.example.data.CreatorPayoutRecord
import com.example.data.DailyEarningsMetric
import com.example.data.EarningsLedgerEntry
import com.example.data.LedgerEntryType
import com.example.data.MembershipTier
import com.example.data.MeskotStrings
import com.example.data.MonetizationTool
import com.example.data.ProgramStatus
import com.example.ui.MeskotViewModel
import com.example.ui.components.CreatorPayoutModal
import com.example.ui.components.ChapaDepositModal
import com.example.ui.components.ChapaConfigModal
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Bolt
import com.example.ui.theme.CardBg
import com.example.ui.theme.Gold
import com.example.ui.theme.GoldDeep
import com.example.ui.theme.GoldSurface
import com.example.ui.theme.Ink
import com.example.ui.theme.LineBorder
import com.example.ui.theme.MutedText
import com.example.ui.theme.Paper2

enum class CreatorTab(val title: String, val icon: String) {
    OVERVIEW("Overview & Streams", "📊"),
    ELIGIBILITY("Eligibility & Tools", "🛡️"),
    LEDGER("Financial Ledger", "📜"),
    PAYOUTS("Disbursements", "💸"),
    MEMBERSHIPS("VIP Tiers", "👑")
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
    val currentUser by viewModel.currentUser.collectAsState()

    val monetizationTools by viewModel.monetizationTools.collectAsState()
    val earningsLedger by viewModel.earningsLedger.collectAsState()
    val contentFormatMetrics by viewModel.contentFormatMetrics.collectAsState()
    val dailyEarnings by viewModel.dailyEarnings.collectAsState()
    val payoutAccounts by viewModel.payoutAccounts.collectAsState()
    val chapaConfig by viewModel.chapaConfig.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    var showPayoutModal by remember { mutableStateOf(false) }
    var showDepositModal by remember { mutableStateOf(false) }
    var showConfigModal by remember { mutableStateOf(false) }
    var showInviteModalTool by remember { mutableStateOf<MonetizationTool?>(null) }
    var showApplyModalTool by remember { mutableStateOf<MonetizationTool?>(null) }

    if (showPayoutModal) {
        CreatorPayoutModal(
            netBalanceEtb = netBalance,
            currentLanguage = currentLanguage,
            onDismiss = { showPayoutModal = false },
            onRequestPayout = { method, amount, destAccount ->
                viewModel.requestPayout(method, amount, destAccount)
                showPayoutModal = false
            }
        )
    }

    if (showDepositModal) {
        ChapaDepositModal(
            config = chapaConfig,
            currentLanguage = currentLanguage,
            onDismiss = { showDepositModal = false },
            onProceed = { amount ->
                showDepositModal = false
                viewModel.depositViaChapa(amount)
            }
        )
    }

    if (showConfigModal) {
        ChapaConfigModal(
            currentConfig = chapaConfig,
            currentLanguage = currentLanguage,
            onDismiss = { showConfigModal = false },
            onSave = { pubKey, secKey, isLive ->
                viewModel.updateChapaConfig(pubKey, secKey, isLive)
                showConfigModal = false
            },
            onResetDemoBalance = {
                viewModel.resetToRealChapaBalance()
                showConfigModal = false
            }
        )
    }

    showInviteModalTool?.let { tool ->
        RedeemInviteCodeModal(
            tool = tool,
            onDismiss = { showInviteModalTool = null },
            onRedeem = { code ->
                viewModel.redeemInviteCode(tool.id, code)
                showInviteModalTool = null
            }
        )
    }

    showApplyModalTool?.let { tool ->
        ApplyMonetizationProgramModal(
            tool = tool,
            onDismiss = { showApplyModalTool = null },
            onConfirmApply = {
                viewModel.applyForMonetizationTool(tool.id)
                showApplyModalTool = null
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Meskot Partner Studio",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Serif,
                                color = Ink
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(0xFF065F46))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "PROD",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF34D399)
                                )
                            }
                        }
                        Text(
                            text = "Multi-tier Monetization · Ledger · Gateways",
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
                        onClick = { showDepositModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = "Add Funds",
                            tint = Color.White,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "+ Add ETB", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Button(
                        onClick = { showPayoutModal = true },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldDeep),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(text = "💸 Cash Out", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    IconButton(onClick = { showConfigModal = true }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Chapa Gateway Settings",
                            tint = Ink
                        )
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
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "CHAPA REAL FINANCIAL BALANCE",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(0xFF94A3B8)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.Verified,
                                    contentDescription = "Ledger Reconciled",
                                    tint = Color(0xFF34D399),
                                    modifier = Modifier.size(13.dp)
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (chapaConfig.isLiveMode) Color(0xFF065F46) else Color(0xFF78350F))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (chapaConfig.isLiveMode) "🟢 LIVE CHAPA (REAL ETB)" else "🟡 CHAPA TEST GATEWAY",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (chapaConfig.isLiveMode) Color(0xFF34D399) else Color(0xFFFDE68A)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Text(
                                text = "${String.format(java.util.Locale.US, "%,.2f", netBalance)} ETB",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFFBBF24),
                                fontFamily = FontFamily.Serif
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                Button(
                                    onClick = { showDepositModal = true },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "+ Deposit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }

                                OutlinedButton(
                                    onClick = { showConfigModal = true },
                                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF93C5FD)),
                                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF3B82F6)),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 6.dp)
                                ) {
                                    Text(text = "⚙️ Chapa", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

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
                                    text = "${currentUser?.starBalance ?: 4850} ⭐",
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
                // Chapa Payment Rails Card
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (chapaConfig.isLiveMode) Color(0xFF86EFAC) else LineBorder),
                    modifier = Modifier.fillMaxWidth().testTag("chapa_rails_card")
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF2E7D32)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(text = "C", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold, color = Color.White)
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "Chapa Payment Gateway",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = Ink
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(if (chapaConfig.isLiveMode) Color(0xFFE8F5E9) else Color(0xFFFEF3C7))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (chapaConfig.isLiveMode) "LIVE ACTIVE" else "SANDBOX",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.ExtraBold,
                                                color = if (chapaConfig.isLiveMode) Color(0xFF2E7D32) else Color(0xFFB45309)
                                            )
                                        }
                                    }
                                    Text(
                                        text = "Telebirr · CBE Birr · eBirr · M-Pesa · Debit/Credit Cards",
                                        fontSize = 11.sp,
                                        color = MutedText
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { showDepositModal = true },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1.2f)
                            ) {
                                Text(text = "+ Add Real ETB", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }

                            OutlinedButton(
                                onClick = { viewModel.openTestChapaCheckout(100.0) },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(text = "Test Checkout", fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                            }

                            OutlinedButton(
                                onClick = { showConfigModal = true },
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(text = "⚙️", fontSize = 13.sp)
                            }
                        }
                    }
                }
            }

            item {
                // Scrollable 5-Tab Bar
                ScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = CardBg,
                    contentColor = GoldDeep,
                    edgePadding = 6.dp,
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(text = tab.icon, fontSize = 13.sp)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = tab.title,
                                        fontSize = 12.sp,
                                        fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Medium
                                    )
                                }
                            }
                        )
                    }
                }
            }

            when (CreatorTab.values()[selectedTab]) {
                CreatorTab.OVERVIEW -> {
                    // 1. 7-Day Performance Bar Chart
                    item {
                        DailyEarningsBarChartCard(dailyEarnings = dailyEarnings)
                    }

                    // 2. Revenue Breakdown by Channel
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
                                    title = "Tiered VIP Fan Memberships",
                                    subtitle = "42 Active Recurring Monthly Supporters",
                                    amountEtb = grossEarnings * 0.52,
                                    pct = "52%"
                                )
                                HorizontalDivider(color = LineBorder, thickness = 0.5.dp)
                                StreamRow(
                                    icon = "⭐",
                                    title = "Virtual Stars & Micro-Gifts",
                                    subtitle = "Direct live room and post micro-tips",
                                    amountEtb = grossEarnings * 0.28,
                                    pct = "28%"
                                )
                                HorizontalDivider(color = LineBorder, thickness = 0.5.dp)
                                StreamRow(
                                    icon = "💰",
                                    title = "Direct Tips (Chapa / CBE / Telebirr)",
                                    subtitle = "One-off reader tips and donations",
                                    amountEtb = grossEarnings * 0.14,
                                    pct = "14%"
                                )
                                HorizontalDivider(color = LineBorder, thickness = 0.5.dp)
                                StreamRow(
                                    icon = "📢",
                                    title = "Ad Revenue Sharing (Reels & In-Stream)",
                                    subtitle = "Monetized plays, mid-rolls, and overlay banners",
                                    amountEtb = grossEarnings * 0.06,
                                    pct = "6%"
                                )
                            }
                        }
                    }

                    // 3. Content Format Analytics
                    item {
                        Text(
                            text = "Content Format Performance (28-Day Window)",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            contentFormatMetrics.forEach { metric ->
                                ContentFormatMetricCard(metric = metric)
                            }
                        }
                    }
                }

                CreatorTab.ELIGIBILITY -> {
                    // Eligibility & Program Onboarding
                    item {
                        PartnerEligibilityCard(
                            currentUser = currentUser,
                            onRedeemAnyInvite = {
                                val reelsTool = monetizationTools.find { it.code == "REELS_OVERLAY" }
                                    ?: monetizationTools.firstOrNull { it.status == ProgramStatus.INVITE_ONLY }
                                reelsTool?.let { showInviteModalTool = it }
                            }
                        )
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Monetization Tools & Programs",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = Ink
                            )
                            Text(
                                text = "${monetizationTools.count { it.status == ProgramStatus.ACTIVE }} Active",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF059669)
                            )
                        }
                    }

                    items(monetizationTools) { tool ->
                        MonetizationToolCard(
                            tool = tool,
                            onApply = { showApplyModalTool = tool },
                            onRedeemInvite = { showInviteModalTool = tool },
                            onRegisterInterest = { viewModel.registerInterest(tool.id) }
                        )
                    }
                }

                CreatorTab.LEDGER -> {
                    // Financial Ledger & Audit Trail
                    item {
                        FinancialLedgerSection(
                            ledgerEntries = earningsLedger,
                            currentNetBalance = netBalance
                        )
                    }
                }

                CreatorTab.PAYOUTS -> {
                    // Disbursements & Settlement Gateways
                    item {
                        PayoutsAndGatewaysSection(
                            payoutAccounts = payoutAccounts,
                            payoutHistory = payoutHistory,
                            netBalance = netBalance,
                            onCashOutClick = { showPayoutModal = true }
                        )
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

// -------------------------------------------------------------
// COMPONENT: 7-Day Performance Bar Chart
// -------------------------------------------------------------
@Composable
fun DailyEarningsBarChartCard(dailyEarnings: List<DailyEarningsMetric>) {
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
                Column {
                    Text(
                        text = "Daily Approximate Earnings",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                    Text(
                        text = "Real-time estimated daily accruals (ETB)",
                        fontSize = 11.sp,
                        color = MutedText
                    )
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GoldSurface)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Last 7 Days",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldDeep
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            val maxVal = dailyEarnings.maxOfOrNull { it.totalEtb } ?: 2000.0
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(110.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                dailyEarnings.forEach { item ->
                    val ratio = (item.totalEtb / maxVal).coerceIn(0.1, 1.0)
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "${item.totalEtb.toInt()}",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldDeep
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Box(
                            modifier = Modifier
                                .width(18.dp)
                                .height((80 * ratio).dp)
                                .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                .background(
                                    Brush.verticalGradient(
                                        listOf(GoldDeep, Gold)
                                    )
                                )
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = item.dayLabel,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MutedText
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = LineBorder, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ChartLegendItem(color = GoldDeep, label = "Ad Revenue")
                ChartLegendItem(color = Color(0xFFFBBF24), label = "Stars Gifts")
                ChartLegendItem(color = Color(0xFF10B981), label = "VIP Subscriptions")
            }
        }
    }
}

@Composable
fun ChartLegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 10.sp, color = MutedText)
    }
}

// -------------------------------------------------------------
// COMPONENT: Content Format Metric Card
// -------------------------------------------------------------
@Composable
fun ContentFormatMetricCard(metric: ContentFormatMetric) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
        modifier = Modifier.fillMaxWidth()
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
                        .size(36.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(GoldSurface),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = metric.icon, fontSize = 18.sp)
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(text = metric.formatName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
                    Text(
                        text = "${String.format(java.util.Locale.US, "%,d", metric.views)} views · ${String.format(java.util.Locale.US, "%,d", metric.monetizableImpressions)} monetized",
                        fontSize = 11.sp,
                        color = MutedText
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "${String.format(java.util.Locale.US, "%,.2f", metric.netCreatorRevenueEtb)} ETB",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF059669)
                )
                Text(
                    text = "RPM: ${String.format(java.util.Locale.US, "%,.2f", metric.rpmEtb)} ETB",
                    fontSize = 10.sp,
                    color = MutedText,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

// -------------------------------------------------------------
// COMPONENT: Partner Program Eligibility Card
// -------------------------------------------------------------
@Composable
fun PartnerEligibilityCard(
    currentUser: com.example.data.User?,
    onRedeemAnyInvite: () -> Unit
) {
    val followers = currentUser?.followersCount ?: 0
    val targetFollowers = 10000
    val watchHours = currentUser?.watchHours ?: 0.0
    val targetWatchHours = 4000.0

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
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Partner Status",
                        tint = GoldDeep,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Partner Program Standing",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Ink
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color(0xFFD1FAE5))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "Good Standing",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF065F46)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Followers requirement
            RequirementProgressRow(
                title = "Followers",
                current = "$followers",
                target = "$targetFollowers",
                progress = (followers.toFloat() / targetFollowers.toFloat()).coerceIn(0f, 1f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Watch Hours requirement
            RequirementProgressRow(
                title = "Watch Hours",
                current = "${watchHours.toInt()} hrs",
                target = "${targetWatchHours.toInt()} hrs",
                progress = (watchHours.toFloat() / targetWatchHours.toFloat()).coerceIn(0f, 1f)
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = LineBorder, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Clean strikes",
                        tint = Color(0xFF059669),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "0 Policy Strikes", fontSize = 11.sp, color = Ink, fontWeight = FontWeight.Medium)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Verified,
                        contentDescription = "KYC verified",
                        tint = Color(0xFF3B82F6),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "KYC Verified", fontSize = 11.sp, color = Ink, fontWeight = FontWeight.Medium)
                }
                TextButton(
                    onClick = onRedeemAnyInvite,
                    modifier = Modifier.height(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = "Invite token",
                        tint = GoldDeep,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "Enter Token", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = GoldDeep)
                }
            }
        }
    }
}

@Composable
fun RequirementProgressRow(
    title: String,
    current: String,
    target: String,
    progress: Float
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = title, fontSize = 11.sp, color = MutedText)
            Text(
                text = "$current / $target (${(progress * 100).toInt()}%)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Ink
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp)),
            color = GoldDeep,
            trackColor = Color(0xFFE2E8F0)
        )
    }
}

// -------------------------------------------------------------
// COMPONENT: Monetization Tool Card
// -------------------------------------------------------------
@Composable
fun MonetizationToolCard(
    tool: MonetizationTool,
    onApply: () -> Unit,
    onRedeemInvite: () -> Unit,
    onRegisterInterest: () -> Unit
) {
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
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(text = tool.icon, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = tool.name,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Text(
                            text = "${tool.revSharePercent.toInt()}% Creator Rev Share",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = GoldDeep
                        )
                    }
                }

                StatusBadge(status = tool.status)
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = tool.description,
                fontSize = 11.sp,
                color = MutedText,
                lineHeight = 15.sp
            )

            if (tool.eligibilityNote.isNotBlank()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = tool.eligibilityNote,
                    fontSize = 10.sp,
                    color = Color(0xFF475569),
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (tool.status) {
                ProgramStatus.ACTIVE -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Active",
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = tool.enrolledDate ?: "Active and earning",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFF059669)
                        )
                    }
                }
                ProgramStatus.INVITE_ONLY -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = onRegisterInterest,
                            modifier = Modifier.height(32.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Register Interest", fontSize = 11.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = onRedeemInvite,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldDeep),
                            modifier = Modifier.height(32.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Redeem Code", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                ProgramStatus.READY_TO_APPLY -> {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        Button(
                            onClick = onApply,
                            colors = ButtonDefaults.buttonColors(containerColor = GoldDeep),
                            modifier = Modifier.height(32.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(text = "Apply Now", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
                ProgramStatus.UNDER_REVIEW -> {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "⏳ Under automated KYC & policy review (usually 24-48 hrs)", fontSize = 11.sp, color = Color(0xFFD97706))
                    }
                }
                ProgramStatus.CRITERIA_NOT_MET -> {
                    Text(text = "Requires ${tool.minFollowers} followers and ${tool.minWatchHours} hrs watch time", fontSize = 11.sp, color = MutedText)
                }
                ProgramStatus.SUSPENDED -> {
                    Text(text = "Monetization suspended due to community guidelines violation", fontSize = 11.sp, color = Color(0xFFDC2626))
                }
            }
        }
    }
}

@Composable
fun StatusBadge(status: ProgramStatus) {
    val (bgColor, textColor, text) = when (status) {
        ProgramStatus.ACTIVE -> Triple(Color(0xFFD1FAE5), Color(0xFF065F46), "Active")
        ProgramStatus.INVITE_ONLY -> Triple(Color(0xFFEDE9FE), Color(0xFF5B21B6), "Invite Only")
        ProgramStatus.READY_TO_APPLY -> Triple(Color(0xFFFEF3C7), Color(0xFF92400E), "Criteria Met")
        ProgramStatus.UNDER_REVIEW -> Triple(Color(0xFFFFFBEB), Color(0xFFB45309), "In Review")
        ProgramStatus.CRITERIA_NOT_MET -> Triple(Color(0xFFF1F5F9), Color(0xFF64748B), "Ineligible")
        ProgramStatus.SUSPENDED -> Triple(Color(0xFFFEE2E2), Color(0xFF991B1B), "Suspended")
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
    ) {
        Text(text = text, fontSize = 10.sp, fontWeight = FontWeight.Bold, color = textColor)
    }
}

// -------------------------------------------------------------
// COMPONENT: Financial Ledger Section
// -------------------------------------------------------------
@Composable
fun FinancialLedgerSection(
    ledgerEntries: List<EarningsLedgerEntry>,
    currentNetBalance: Double
) {
    var selectedFilter by remember { mutableStateOf("ALL") }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        // Ledger Summary Card
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
                    Column {
                        Text(
                            text = "Real-Time Double-Entry Ledger",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink
                        )
                        Text(
                            text = "Immutable transaction audit trail & running balances",
                            fontSize = 11.sp,
                            color = MutedText
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFFD1FAE5))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Reconciled ✓",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Total Entries", fontSize = 10.sp, color = MutedText)
                        Text(text = "${ledgerEntries.size}", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
                    }
                    Column {
                        Text(text = "Latest Running Balance", fontSize = 10.sp, color = MutedText)
                        Text(
                            text = "${String.format(java.util.Locale.US, "%,.2f", currentNetBalance)} ETB",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = GoldDeep
                        )
                    }
                    Column {
                        Text(text = "Audit Currency", fontSize = 10.sp, color = MutedText)
                        Text(text = "ETB / Birr", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
                    }
                }
            }
        }

        // Filter chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = selectedFilter == "ALL",
                onClick = { selectedFilter = "ALL" },
                label = { Text("All (${ledgerEntries.size})", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = GoldSurface)
            )
            FilterChip(
                selected = selectedFilter == "CREDITS",
                onClick = { selectedFilter = "CREDITS" },
                label = { Text("Credits (+)", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFD1FAE5))
            )
            FilterChip(
                selected = selectedFilter == "DEBITS",
                onClick = { selectedFilter = "DEBITS" },
                label = { Text("Debits (-)", fontSize = 11.sp) },
                colors = FilterChipDefaults.filterChipColors(selectedContainerColor = Color(0xFFFEE2E2))
            )
        }

        val filtered = when (selectedFilter) {
            "CREDITS" -> ledgerEntries.filter { it.entryType.isCredit }
            "DEBITS" -> ledgerEntries.filter { !it.entryType.isCredit }
            else -> ledgerEntries
        }

        filtered.forEach { entry ->
            LedgerEntryCard(entry = entry)
        }
    }
}

@Composable
fun LedgerEntryCard(entry: EarningsLedgerEntry) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = CardBg),
        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (entry.entryType.isCredit) Color(0xFFD1FAE5) else Color(0xFFFEE2E2))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = if (entry.entryType.isCredit) "CREDIT" else "DEBIT",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (entry.entryType.isCredit) Color(0xFF065F46) else Color(0xFF991B1B)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = entry.transactionRef,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF475569)
                    )
                }

                Text(
                    text = (if (entry.amountEtb > 0) "+" else "") + "${String.format(java.util.Locale.US, "%,.2f", entry.amountEtb)} ETB",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (entry.amountEtb >= 0) Color(0xFF059669) else Color(0xFFDC2626)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))
            Text(text = entry.sourceTitle, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Ink)

            if (entry.metadata.isNotBlank()) {
                Text(text = entry.metadata, fontSize = 10.sp, color = MutedText)
            }

            Spacer(modifier = Modifier.height(6.dp))
            HorizontalDivider(color = LineBorder, thickness = 0.5.dp)
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = entry.formattedDate, fontSize = 10.sp, color = MutedText)
                Text(
                    text = "Balance: ${String.format(java.util.Locale.US, "%,.2f", entry.balanceAfterEtb)} ETB",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF334155)
                )
            }
        }
    }
}

// -------------------------------------------------------------
// COMPONENT: Payouts & Gateways Section
// -------------------------------------------------------------
@Composable
fun PayoutsAndGatewaysSection(
    payoutAccounts: List<CreatorPayoutAccount>,
    payoutHistory: List<CreatorPayoutRecord>,
    netBalance: Double,
    onCashOutClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        // Settlement Schedule Info Card
        Card(
            shape = RoundedCornerShape(14.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFEF9C3)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                Text(text = "⚡", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "Automated Monday Settlement Active",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = Color(0xFF854D0E)
                    )
                    Text(
                        text = "Balances reaching 1,000 ETB automatically settle every Monday morning. You can also request instant cash out anytime above 100 ETB.",
                        fontSize = 11.sp,
                        color = Color(0xFFA16207),
                        lineHeight = 15.sp
                    )
                }
            }
        }

        // Payout Gateways Accounts
        Text(
            text = "Settlement Accounts & Gateways",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Ink
        )

        payoutAccounts.forEach { account ->
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
                        Text(
                            text = when {
                                account.gatewayName.contains("Telebirr", ignoreCase = true) -> "📱"
                                account.gatewayName.contains("CBE", ignoreCase = true) -> "🏦"
                                else -> "💳"
                            },
                            fontSize = 22.sp
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(text = account.gatewayName, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
                                if (account.isDefault) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(GoldSurface)
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(text = "PRIMARY", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = GoldDeep)
                                    }
                                }
                            }
                            Text(text = "${account.accountNumber} · ${account.holderName}", fontSize = 11.sp, color = MutedText)
                        }
                    }

                    if (account.isVerified) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Verified account",
                            tint = Color(0xFF059669),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // History
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
            Button(
                onClick = onCashOutClick,
                colors = ButtonDefaults.buttonColors(containerColor = GoldDeep),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Instant Cash Out", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        payoutHistory.forEach { record ->
            PayoutHistoryCard(record = record)
        }
    }
}

// -------------------------------------------------------------
// MODAL: Redeem Invite Code
// -------------------------------------------------------------
@Composable
fun RedeemInviteCodeModal(
    tool: MonetizationTool,
    onDismiss: () -> Unit,
    onRedeem: (code: String) -> Unit
) {
    var codeText by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "🎟️", fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Redeem Partner Invite Code",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Ink
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Unlock ${tool.name} with an exclusive creator invite token issued by Meskot or partner telecom networks.",
                    fontSize = 12.sp,
                    color = MutedText,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                OutlinedTextField(
                    value = codeText,
                    onValueChange = {
                        codeText = it.uppercase()
                        errorMessage = ""
                    },
                    label = { Text("Invite Token (e.g. MESKOT-VIP, CREATOR2026)") },
                    singleLine = true,
                    colors = com.example.ui.theme.meskotTextFieldColors(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                )

                if (errorMessage.isNotBlank()) {
                    Text(text = errorMessage, fontSize = 11.sp, color = Color(0xFFDC2626), modifier = Modifier.padding(top = 4.dp))
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "💡 Try test codes: MESKOT-VIP, CREATOR2026, or INLINE-CHAPA. Unlocks tool & credits 500 ETB bonus.",
                    fontSize = 11.sp,
                    color = GoldDeep,
                    lineHeight = 15.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "Cancel", color = MutedText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (codeText.isNotBlank()) {
                                onRedeem(codeText.trim())
                            } else {
                                errorMessage = "Please enter an invite code"
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = GoldDeep),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Unlock Program", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// MODAL: Apply for Monetization Program
// -------------------------------------------------------------
@Composable
fun ApplyMonetizationProgramModal(
    tool: MonetizationTool,
    onDismiss: () -> Unit,
    onConfirmApply: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = tool.icon, fontSize = 24.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Apply: ${tool.name}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Ink
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "You meet all qualification standards for this monetization tool: 70% revenue share with automated weekly settlements.",
                    fontSize = 12.sp,
                    color = MutedText,
                    lineHeight = 16.sp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    shape = RoundedCornerShape(10.dp),
                    colors = CardDefaults.cardColors(containerColor = Paper2),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(text = "✓ Clean Copyright & Community Standing", fontSize = 11.sp, color = Color(0xFF065F46), fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "✓ Ethiopian Tax & Withholding Compliance", fontSize = 11.sp, color = Color(0xFF065F46), fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "✓ Verified Payout Account (Telebirr / CBE)", fontSize = 11.sp, color = Color(0xFF065F46), fontWeight = FontWeight.Medium)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(text = "Cancel", color = MutedText)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = onConfirmApply,
                        colors = ButtonDefaults.buttonColors(containerColor = GoldDeep),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(text = "Submit Application", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

// -------------------------------------------------------------
// Existing Helper Components
// -------------------------------------------------------------
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
