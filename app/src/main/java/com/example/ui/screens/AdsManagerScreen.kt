package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.*
import com.example.ui.MeskotViewModel
import com.example.ui.components.UserAvatar
import java.net.URI
import java.util.Locale
import kotlin.math.roundToInt

// Facebook Ads Manager Color Tokens
private val FbBlue = Color(0xFF1877F2)
private val FbDarkText = Color(0xFF050505)
private val FbSecondaryText = Color(0xFF65676B)
private val FbLightGrayBg = Color(0xFFF0F2F5)
private val FbCardBg = Color(0xFFFFFFFF)
private val FbDivider = Color(0xFFCED0D4)
private val FbErrorRed = Color(0xFFD93025)
private val FbErrorBannerBg = Color(0xFFFCE8E6)
private val FbSuccessGreen = Color(0xFF2E7D32)

// ============================================================================
// CLEAN STATE MANAGEMENT, VALIDATION & PAYMENT CALCULATION ENGINE
// ============================================================================

enum class AdsFlowRoute {
    OVERVIEW_DASHBOARD,
    CAMPAIGN_SETUP
}

enum class AdGoalOption(
    val id: String,
    val title: String,
    val subtitle: String,
    val objectiveCode: String
) {
    WEBSITE_VISITORS(
        id = "website_visitors",
        title = "Get more website visitors",
        subtitle = "Send people to a destination on or off Meskot",
        objectiveCode = "TRAFFIC"
    ),
    BOOST_CONTENT(
        id = "boost_content",
        title = "Boost Facebook content",
        subtitle = "Get more people to see and engage with your posts",
        objectiveCode = "ENGAGEMENT"
    ),
    PAGE_LIKES(
        id = "page_likes",
        title = "Get more Page likes",
        subtitle = "Build your audience and community presence",
        objectiveCode = "AWARENESS"
    )
}

enum class AdCtaButtonOption(val label: String) {
    NO_BUTTON("No button"),
    LEARN_MORE("Learn More"),
    SHOP_NOW("Shop Now"),
    SIGN_UP("Sign Up"),
    BOOK_NOW("Book Now"),
    CONTACT_US("Contact Us")
}

data class AudienceTargetingConfig(
    val genderLabel: String = "Men/Women",
    val minAge: Int = 18,
    val maxAge: Int = 65,
    val locations: List<String> = listOf("Ethiopia")
) {
    val summaryLabel: String
        get() {
            val agePlus = if (maxAge >= 65) "65+" else maxAge.toString()
            val locCount = locations.size.coerceAtLeast(1)
            val locSuffix = if (locCount == 1) "1 location" else "$locCount locations"
            return "$genderLabel, $minAge-$agePlus, $locSuffix"
        }
}

data class PaymentMethodInfo(
    val brandName: String = "MasterCard",
    val last4Digits: String = "7228",
    val cardholderName: String = "Yemane Tsadik",
    val cardNumber: String = "",
    val expiryDate: String = "08/29",
    val cvv: String = "",
    val isVerified: Boolean = true,
    val currency: PaymentCurrency = PaymentCurrency.USD
) {
    val maskedCardLabel: String
        get() = "$brandName •••• $last4Digits"

    companion object {
        fun detectCardBrand(digits: String): String {
            val clean = digits.filter { it.isDigit() }
            return when {
                clean.startsWith("4") -> "Visa"
                clean.startsWith("5") || clean.startsWith("2") -> "MasterCard"
                clean.startsWith("34") || clean.startsWith("37") -> "AMEX"
                clean.startsWith("62") -> "UnionPay"
                clean.startsWith("6011") || clean.startsWith("65") -> "Discover"
                else -> "MasterCard"
            }
        }
    }
}

/**
 * Pure business logic & validation state holder for Campaign Setup (`CreateAdScreen`).
 * Separates URL regex validation, budget-to-reach estimation, and 5% tax calculations from the UI.
 */
data class CampaignSetupUiState(
    val selectedGoal: AdGoalOption = AdGoalOption.WEBSITE_VISITORS,
    val destinationUrl: String = "https://example.com",
    val hasInteractedWithUrl: Boolean = true,
    val advertiserName: String = "Yemane Tsadik",
    val advertiserAvatar: String = "",
    val postCaption: String = "Engineer is a problem solver",
    val adMediaUrl: String = "https://images.unsplash.com/photo-1581091226825-a6a2a5aee158?w=900&auto=format&fit=crop&q=80",
    val selectedCta: AdCtaButtonOption = AdCtaButtonOption.NO_BUTTON,
    val audience: AudienceTargetingConfig = AudienceTargetingConfig(),
    val dailyBudgetUsd: Double = 5.00,
    val durationDays: Int = 5,
    val paymentMethod: PaymentMethodInfo = PaymentMethodInfo(),
    val selectedCurrency: PaymentCurrency = PaymentCurrency.USD
) {
    companion object {
        // Strict HTTP/HTTPS or domain URL regex validation
        private val URL_REGEX = Regex(
            pattern = "^(https?://)?([a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}(/.*)?$",
            option = RegexOption.IGNORE_CASE
        )

        fun isValidDestinationUrl(input: String): Boolean {
            val trimmed = input.trim()
            if (trimmed.isEmpty() || trimmed.contains(" ")) return false
            return URL_REGEX.matches(trimmed)
        }

        fun extractDisplayDomain(input: String): String {
            val trimmed = input.trim()
            if (trimmed.isBlank()) return "EXAMPLE.COM"
            return try {
                val normalized = if (trimmed.startsWith("http://", true) || trimmed.startsWith("https://", true)) {
                    trimmed
                } else {
                    "https://$trimmed"
                }
                val host = URI(normalized).host ?: trimmed.substringBefore("/")
                host.removePrefix("www.").uppercase(Locale.US).ifBlank { "EXAMPLE.COM" }
            } catch (_: Exception) {
                trimmed.removePrefix("https://")
                    .removePrefix("http://")
                    .removePrefix("www.")
                    .substringBefore("/")
                    .uppercase(Locale.US)
                    .ifBlank { "EXAMPLE.COM" }
            }
        }
    }

    val isUrlValid: Boolean
        get() = isValidDestinationUrl(destinationUrl)

    val showUrlErrorBanner: Boolean
        get() = !isUrlValid

    val displayDomain: String
        get() = extractDisplayDomain(destinationUrl)

    // Estimated daily reach scales dynamically with daily budget
    val estimatedMinDailyReach: Int
        get() = (dailyBudgetUsd * 290).roundToInt().coerceAtLeast(350)

    val estimatedMaxDailyReach: Int
        get() = (dailyBudgetUsd * 840).roundToInt().coerceAtLeast(1000)

    val estimatedReachText: String
        get() = "${String.format(Locale.US, "%,d", estimatedMinDailyReach)} - ${String.format(Locale.US, "%,d", estimatedMaxDailyReach)} people per day"

    // Payment Summary Calculations (Auto-Calculated)
    // Total Budget = Daily Budget * Duration
    val totalBudgetUsd: Double
        get() = dailyBudgetUsd * durationDays

    // Estimated Tax = Total Budget * 5% Tax
    val estimatedTaxUsd: Double
        get() = totalBudgetUsd * 0.05

    // Total Amount = Total Budget + Estimated Tax
    val totalAmountUsd: Double
        get() = totalBudgetUsd + estimatedTaxUsd

    // Real National Bank of Ethiopia (NBE) daily market rate conversion for ETB secondary option
    val nbeRate: Double
        get() = ExchangeRateManager.usdToEtbRate

    val dailyBudgetEtb: Double
        get() = dailyBudgetUsd * nbeRate

    val totalBudgetEtb: Double
        get() = totalBudgetUsd * nbeRate

    val estimatedTaxEtb: Double
        get() = estimatedTaxUsd * nbeRate

    val totalAmountEtb: Double
        get() = totalAmountUsd * nbeRate

    fun formatAmount(amountUsd: Double): String {
        return if (selectedCurrency == PaymentCurrency.USD) {
            "$${String.format(Locale.US, "%,.2f", amountUsd)}"
        } else {
            val etb = amountUsd * nbeRate
            "ETB ${String.format(Locale.US, "%,.2f", etb)}"
        }
    }

    fun formatDualAmount(amountUsd: Double): String {
        val usdStr = "$${String.format(Locale.US, "%,.2f", amountUsd)}"
        val etbStr = "ETB ${String.format(Locale.US, "%,.0f", amountUsd * nbeRate)}"
        return if (selectedCurrency == PaymentCurrency.USD) {
            "$usdStr (≈ $etbStr)"
        } else {
            "$etbStr (≈ $usdStr)"
        }
    }

    val canPromoteNow: Boolean
        get() = isUrlValid && paymentMethod.isVerified && dailyBudgetUsd > 0 && durationDays >= 1
}

/**
 * Entry Composable for the Ads Manager section in Meskot.
 * Hosts the two-screen Facebook Ads Manager flow:
 * 1. `AdsDashboardScreen` ("Ads Overview Dashboard")
 * 2. `CreateAdScreen` ("Campaign Setup & Promotion Screen")
 */
@Composable
fun AdsManagerScreen(
    viewModel: MeskotViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val campaigns by viewModel.adCampaigns.collectAsState()

    var currentRoute by remember { mutableStateOf(AdsFlowRoute.OVERVIEW_DASHBOARD) }
    var selectedTimeframe by remember { mutableStateOf("Last 7 days") }

    // Campaign Setup State initialized with current user ("Yemane Tsadik" fallback)
    var setupState by remember(currentUser?.displayName, currentUser?.photoUrl) {
        mutableStateOf(
            CampaignSetupUiState(
                advertiserName = currentUser?.displayName?.ifBlank { "Yemane Tsadik" } ?: "Yemane Tsadik",
                advertiserAvatar = currentUser?.photoUrl.orEmpty()
            )
        )
    }

    BackHandler {
        if (currentRoute == AdsFlowRoute.CAMPAIGN_SETUP) {
            currentRoute = AdsFlowRoute.OVERVIEW_DASHBOARD
        } else {
            onBack()
        }
    }

    when (currentRoute) {
        AdsFlowRoute.OVERVIEW_DASHBOARD -> {
            AdsDashboardScreen(
                campaigns = campaigns,
                selectedTimeframe = selectedTimeframe,
                onTimeframeSelected = { selectedTimeframe = it },
                onBack = onBack,
                onSelectGoal = { goal ->
                    setupState = setupState.copy(selectedGoal = goal)
                    currentRoute = AdsFlowRoute.CAMPAIGN_SETUP
                },
                onToggleCampaignStatus = { campaignId ->
                    viewModel.toggleAdCampaignStatus(campaignId)
                }
            )
        }

        AdsFlowRoute.CAMPAIGN_SETUP -> {
            CreateAdScreen(
                state = setupState,
                onStateChange = { setupState = it },
                onBack = { currentRoute = AdsFlowRoute.OVERVIEW_DASHBOARD },
                onPromoteNow = { finalState ->
                    viewModel.createAdCampaign(
                        name = "${finalState.selectedGoal.title} (${finalState.displayDomain})",
                        objective = finalState.selectedGoal.objectiveCode,
                        dailyBudgetEtb = finalState.dailyBudgetEtb,
                        headline = finalState.displayDomain,
                        primaryText = finalState.postCaption,
                        mediaUrl = finalState.adMediaUrl,
                        ctaText = finalState.selectedCta.label,
                        destinationUrl = finalState.destinationUrl.trim(),
                        targetAudience = finalState.audience.summaryLabel,
                        deductFromWallet = false
                    )
                    currentRoute = AdsFlowRoute.OVERVIEW_DASHBOARD
                }
            )
        }
    }
}

// ============================================================================
// 1. ADS OVERVIEW DASHBOARD SCREEN (`AdsDashboardScreen`)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdsDashboardScreen(
    campaigns: List<AdCampaign>,
    selectedTimeframe: String,
    onTimeframeSelected: (String) -> Unit,
    onBack: () -> Unit,
    onSelectGoal: (AdGoalOption) -> Unit,
    onToggleCampaignStatus: (String) -> Unit
) {
    var isTimeframeMenuExpanded by remember { mutableStateOf(false) }
    var showSummaryInfoDialog by remember { mutableStateOf(false) }

    val timeframes = remember {
        listOf("Last 7 days", "Last 28 days", "Last 90 days", "Lifetime")
    }

    // Timeframe multiplier for summary metrics when campaigns exist
    val timeframeMultiplier = when (selectedTimeframe) {
        "Last 7 days" -> 1.0
        "Last 28 days" -> 2.4
        "Last 90 days" -> 4.5
        else -> 5.0
    }

    val hasAds = campaigns.isNotEmpty()
    val reachValue = if (hasAds) {
        String.format(Locale.US, "%,d", (campaigns.sumOf { it.impressions } * timeframeMultiplier).roundToInt())
    } else {
        "--"
    }
    val engagementsValue = if (hasAds) {
        val baseEngagements = campaigns.sumOf { it.clicks + it.conversions * 3 }
        String.format(Locale.US, "%,d", (baseEngagements * timeframeMultiplier).roundToInt())
    } else {
        "--"
    }
    val linkClicksValue = if (hasAds) {
        String.format(Locale.US, "%,d", (campaigns.sumOf { it.clicks } * timeframeMultiplier).roundToInt())
    } else {
        "--"
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("ads_dashboard_screen"),
        containerColor = FbLightGrayBg,
        topBar = {
            Surface(
                color = FbCardBg,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("ads_dashboard_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = FbDarkText
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Ads",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = FbDarkText
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            // SECTION 1: "Create ad" Section
            item {
                Surface(
                    color = FbCardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 14.dp)
                    ) {
                        Text(
                            text = "Create ad",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = FbDarkText,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // 1. "Boost Facebook content" (Rocket icon)
                        CreateAdOptionTile(
                            icon = Icons.Default.RocketLaunch,
                            iconBgColor = Color(0xFFE7F3FF),
                            iconTint = FbBlue,
                            title = "Boost Facebook content",
                            onClick = { onSelectGoal(AdGoalOption.BOOST_CONTENT) },
                            testTag = "tile_boost_content"
                        )

                        // 2. "Get more Page likes" (Flag icon)
                        CreateAdOptionTile(
                            icon = Icons.Default.Flag,
                            iconBgColor = Color(0xFFE7F3FF),
                            iconTint = FbBlue,
                            title = "Get more Page likes",
                            onClick = { onSelectGoal(AdGoalOption.PAGE_LIKES) },
                            testTag = "tile_page_likes"
                        )

                        // 3. "Get more website visitors" (Cursor/Click icon) -> Navigates to Campaign Setup
                        CreateAdOptionTile(
                            icon = Icons.Default.TouchApp,
                            iconBgColor = Color(0xFFE7F3FF),
                            iconTint = FbBlue,
                            title = "Get more website visitors",
                            onClick = { onSelectGoal(AdGoalOption.WEBSITE_VISITORS) },
                            testTag = "tile_website_visitors"
                        )
                    }
                }
            }

            // SECTION 2: "Advertising summary" Section
            item {
                Surface(
                    color = FbCardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        // Header with Info icon
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Advertising summary",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = FbDarkText
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            IconButton(
                                onClick = { showSummaryInfoDialog = true },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Info,
                                    contentDescription = "Advertising summary info",
                                    tint = FbSecondaryText,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Subtitle
                        Text(
                            text = "After you create ads, you'll get information on how they're performing.",
                            fontSize = 14.sp,
                            color = FbSecondaryText,
                            lineHeight = 19.sp
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Timeframe selector dropdown (Default: "Last 7 days")
                        Box {
                            Surface(
                                onClick = { isTimeframeMenuExpanded = true },
                                shape = RoundedCornerShape(8.dp),
                                color = FbLightGrayBg,
                                modifier = Modifier.testTag("ads_timeframe_dropdown")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = selectedTimeframe,
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = FbDarkText
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Select timeframe",
                                        tint = FbDarkText
                                    )
                                }
                            }

                            DropdownMenu(
                                expanded = isTimeframeMenuExpanded,
                                onDismissRequest = { isTimeframeMenuExpanded = false }
                            ) {
                                timeframes.forEach { tf ->
                                    DropdownMenuItem(
                                        text = {
                                            Text(
                                                text = tf,
                                                fontWeight = if (tf == selectedTimeframe) FontWeight.Bold else FontWeight.Normal
                                            )
                                        },
                                        onClick = {
                                            onTimeframeSelected(tf)
                                            isTimeframeMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Horizontally scrollable summary cards showing Reach, Post engagements, Link clicks
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            AdvertisingSummaryMetricCard(
                                label = "Reach",
                                value = reachValue
                            )
                            AdvertisingSummaryMetricCard(
                                label = "Post engagements",
                                value = engagementsValue
                            )
                            AdvertisingSummaryMetricCard(
                                label = "Link clicks",
                                value = linkClicksValue
                            )
                        }
                    }
                }
            }

            // SECTION 3: "Manage ads" Section
            item {
                Surface(
                    color = FbCardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Manage ads",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = FbDarkText
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Once you create an ad, you can check on its performance here.",
                            fontSize = 14.sp,
                            color = FbSecondaryText,
                            lineHeight = 19.sp
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        if (campaigns.isEmpty()) {
                            // Empty state illustration with placeholder text: "You have not created any ads yet"
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 24.dp)
                                    .testTag("manage_ads_empty_state"),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                ManageAdsEmptyIllustration()

                                Spacer(modifier = Modifier.height(16.dp))

                                Text(
                                    text = "You have not created any ads yet",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = FbSecondaryText,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                campaigns.forEach { campaign ->
                                    ManagedAdCampaignCard(
                                        campaign = campaign,
                                        onToggleStatus = { onToggleCampaignStatus(campaign.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showSummaryInfoDialog) {
        AlertDialog(
            onDismissRequest = { showSummaryInfoDialog = false },
            title = { Text("About Advertising Summary", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Reach shows the number of unique people who saw your ads. Post engagements measure likes, comments, shares, and saves. Link clicks track taps on your destination URL."
                )
            },
            confirmButton = {
                TextButton(onClick = { showSummaryInfoDialog = false }) {
                    Text("OK", color = FbBlue, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}

@Composable
private fun CreateAdOptionTile(
    icon: ImageVector,
    iconBgColor: Color,
    iconTint: Color,
    title: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconBgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = FbDarkText
            )
        }

        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = FbSecondaryText
        )
    }
}

@Composable
private fun AdvertisingSummaryMetricCard(
    label: String,
    value: String
) {
    OutlinedCard(
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, FbDivider.copy(alpha = 0.7f)),
        colors = CardDefaults.outlinedCardColors(containerColor = FbCardBg),
        modifier = Modifier.width(150.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Text(
                text = label,
                fontSize = 13.sp,
                color = FbSecondaryText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = FbDarkText
            )
        }
    }
}

@Composable
private fun ManageAdsEmptyIllustration() {
    Box(
        modifier = Modifier.size(110.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Soft circular backdrop
            drawCircle(
                color = Color(0xFFE7F3FF),
                radius = size.minDimension * 0.46f
            )
            // Secondary accent bubble
            drawCircle(
                color = Color(0xFFD0E7FF),
                radius = size.minDimension * 0.28f,
                center = Offset(size.width * 0.65f, size.height * 0.35f)
            )
        }
        Icon(
            imageVector = Icons.Outlined.Campaign,
            contentDescription = "No ads created yet",
            tint = FbBlue,
            modifier = Modifier.size(52.dp)
        )
    }
}

@Composable
private fun ManagedAdCampaignCard(
    campaign: AdCampaign,
    onToggleStatus: () -> Unit
) {
    val isActive = campaign.status == "ACTIVE"
    OutlinedCard(
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, FbDivider.copy(alpha = 0.7f)),
        colors = CardDefaults.outlinedCardColors(containerColor = FbCardBg),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = campaign.mediaUrl,
                        contentDescription = campaign.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(FbLightGrayBg)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = campaign.name,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = FbDarkText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${PaymentCurrency.USD.formatFromEtb(campaign.dailyBudgetEtb)}/day (≈ ETB ${campaign.dailyBudgetEtb.roundToInt()}) · ${campaign.targetAudience}",
                            fontSize = 12.sp,
                            color = FbSecondaryText,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                AssistChip(
                    onClick = onToggleStatus,
                    label = {
                        Text(
                            text = if (isActive) "Active" else "Paused",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isActive) FbSuccessGreen else FbSecondaryText
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = if (isActive) Icons.Default.CheckCircle else Icons.Default.PauseCircle,
                            contentDescription = null,
                            tint = if (isActive) FbSuccessGreen else FbSecondaryText,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = FbDivider.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Reach", fontSize = 11.sp, color = FbSecondaryText)
                    Text(
                        String.format(Locale.US, "%,d", campaign.impressions),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = FbDarkText
                    )
                }
                Column {
                    Text("Link clicks", fontSize = 11.sp, color = FbSecondaryText)
                    Text(
                        String.format(Locale.US, "%,d", campaign.clicks),
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = FbDarkText
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Amount spent", fontSize = 11.sp, color = FbSecondaryText)
                    Text(
                        "${PaymentCurrency.USD.formatFromEtb(campaign.totalSpentEtb)} (≈ ETB ${campaign.totalSpentEtb.roundToInt()})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = FbBlue
                    )
                }
            }
        }
    }
}

// ============================================================================
// 2. CAMPAIGN SETUP & PROMOTION SCREEN (`CreateAdScreen`)
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateAdScreen(
    state: CampaignSetupUiState,
    onStateChange: (CampaignSetupUiState) -> Unit,
    onBack: () -> Unit,
    onPromoteNow: (CampaignSetupUiState) -> Unit
) {
    var showCtaSheet by remember { mutableStateOf(false) }
    var showAudienceDialog by remember { mutableStateOf(false) }
    var showPaymentDialog by remember { mutableStateOf(false) }
    var showEditCreativeDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("create_ad_screen"),
        containerColor = FbLightGrayBg,
        topBar = {
            Surface(
                color = FbCardBg,
                shadowElevation = 1.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("create_ad_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = FbDarkText
                        )
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = state.selectedGoal.title,
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = FbDarkText,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        },
        bottomBar = {
            // Sticky Bottom Action Bar: "Promote now" + Legal disclaimer footer
            Surface(
                color = FbCardBg,
                shadowElevation = 8.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    Text(
                        text = buildAnnotatedString {
                            append("By tapping Promote now, you agree to Meskot's ")
                            withStyle(SpanStyle(color = FbBlue, fontWeight = FontWeight.SemiBold)) {
                                append("Self-Serve Ad Terms")
                            }
                            append(" and ")
                            withStyle(SpanStyle(color = FbBlue, fontWeight = FontWeight.SemiBold)) {
                                append("Advertising Policies & Guidelines")
                            }
                            append(".")
                        },
                        fontSize = 11.sp,
                        color = FbSecondaryText,
                        lineHeight = 15.sp,
                        modifier = Modifier.padding(bottom = 10.dp)
                    )

                    Button(
                        onClick = { onPromoteNow(state) },
                        enabled = state.canPromoteNow,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = FbBlue,
                            contentColor = Color.White,
                            disabledContainerColor = Color(0xFFE4E6EB),
                            disabledContentColor = Color(0xFFBCC0C4)
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("promote_now_button")
                    ) {
                        Text(
                            text = "Promote now",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 28.dp)
        ) {
            // 1. Destination URL Input Field + Live Regex URL Validation Error Banner
            item {
                Surface(
                    color = FbCardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "Add a link to your website",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = FbDarkText
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = state.destinationUrl,
                            onValueChange = { newUrl ->
                                onStateChange(
                                    state.copy(
                                        destinationUrl = newUrl,
                                        hasInteractedWithUrl = true
                                    )
                                )
                            },
                            label = { Text("Website URL") },
                            placeholder = { Text("https://example.com") },
                            isError = state.showUrlErrorBanner,
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Uri,
                                imeAction = ImeAction.Done
                            ),
                            trailingIcon = {
                                if (state.destinationUrl.isNotEmpty()) {
                                    IconButton(
                                        onClick = {
                                            onStateChange(state.copy(destinationUrl = ""))
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Cancel,
                                            contentDescription = "Clear URL",
                                            tint = FbSecondaryText
                                        )
                                    }
                                }
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = if (state.showUrlErrorBanner) FbErrorRed else FbBlue,
                                unfocusedBorderColor = if (state.showUrlErrorBanner) FbErrorRed else FbDivider
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("destination_url_input")
                        )

                        // Live Regex URL validation red error banner
                        AnimatedVisibility(
                            visible = state.showUrlErrorBanner,
                            enter = fadeIn() + expandVertically(),
                            exit = fadeOut() + shrinkVertically()
                        ) {
                            Surface(
                                color = FbErrorBannerBg,
                                shape = RoundedCornerShape(8.dp),
                                border = BorderStroke(1.dp, FbErrorRed.copy(alpha = 0.4f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 10.dp)
                                    .testTag("url_error_banner")
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Error,
                                        contentDescription = "Error",
                                        tint = FbErrorRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Please add a valid URL.",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = FbErrorRed
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 2. Live Ad Preview Card
            item {
                Surface(
                    color = FbCardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Ad preview",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = FbDarkText
                            )
                            TextButton(onClick = { showEditCreativeDialog = true }) {
                                Text("Edit creative", color = FbBlue, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        OutlinedCard(
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, FbDivider.copy(alpha = 0.8f)),
                            colors = CardDefaults.outlinedCardColors(containerColor = FbCardBg),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("live_ad_preview_card")
                        ) {
                            Column(modifier = Modifier.fillMaxWidth()) {
                                // Preview Header: Profile picture, User Name ("Yemane Tsadik"), "Sponsored" + globe icon
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        UserAvatar(
                                            photoUrl = state.advertiserAvatar,
                                            name = state.advertiserName,
                                            size = 40
                                        )
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Column {
                                            Text(
                                                text = state.advertiserName,
                                                fontSize = 15.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = FbDarkText
                                            )
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = "Sponsored",
                                                    fontSize = 12.sp,
                                                    color = FbSecondaryText
                                                )
                                                Text(
                                                    text = " · ",
                                                    fontSize = 12.sp,
                                                    color = FbSecondaryText
                                                )
                                                Icon(
                                                    imageVector = Icons.Default.Public,
                                                    contentDescription = "Public",
                                                    tint = FbSecondaryText,
                                                    modifier = Modifier.size(13.dp)
                                                )
                                            }
                                        }
                                    }

                                    Icon(
                                        imageVector = Icons.Default.MoreHoriz,
                                        contentDescription = "More options",
                                        tint = FbSecondaryText
                                    )
                                }

                                // Post Caption: Dynamic string (e.g., "Engineer is a problem solver")
                                Text(
                                    text = state.postCaption,
                                    fontSize = 14.sp,
                                    color = FbDarkText,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Ad Media: Image preview display
                                AsyncImage(
                                    model = state.adMediaUrl,
                                    contentDescription = "Ad media preview",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(210.dp)
                                        .background(FbLightGrayBg)
                                )

                                // Link Footer: Target domain ("EXAMPLE.COM"), user title, and optional CTA button
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .background(Color(0xFFF0F2F5))
                                        .padding(horizontal = 12.dp, vertical = 10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = state.displayDomain,
                                            fontSize = 11.sp,
                                            color = FbSecondaryText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = state.advertiserName,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = FbDarkText,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }

                                    if (state.selectedCta != AdCtaButtonOption.NO_BUTTON) {
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = Color(0xFFE4E6EB)
                                        ) {
                                            Text(
                                                text = state.selectedCta.label,
                                                fontSize = 13.sp,
                                                fontWeight = FontWeight.SemiBold,
                                                color = FbDarkText,
                                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                                            )
                                        }
                                    }
                                }

                                HorizontalDivider(color = FbDivider.copy(alpha = 0.5f))

                                // Mock Interaction Buttons (Like, Comment, Share)
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp),
                                    horizontalArrangement = Arrangement.SpaceEvenly,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    PreviewSocialActionItem(
                                        icon = Icons.Outlined.ThumbUp,
                                        label = "Like"
                                    )
                                    PreviewSocialActionItem(
                                        icon = Icons.Outlined.ChatBubbleOutline,
                                        label = "Comment"
                                    )
                                    PreviewSocialActionItem(
                                        icon = Icons.Outlined.Share,
                                        label = "Share"
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. CTA Button Selector (`Add button (optional)`)
            item {
                Surface(
                    color = FbCardBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showCtaSheet = true }
                        .testTag("cta_button_selector")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Add button (optional)",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = FbDarkText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = state.selectedCta.label,
                                fontSize = 14.sp,
                                color = FbSecondaryText
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Choose button",
                            tint = FbSecondaryText
                        )
                    }
                }
            }

            // 4. Audience Targeting Selector
            item {
                Surface(
                    color = FbCardBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAudienceDialog = true }
                        .testTag("audience_targeting_selector")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Audience",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = FbDarkText
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = state.audience.summaryLabel,
                                fontSize = 14.sp,
                                color = FbSecondaryText
                            )
                        }
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Edit audience",
                            tint = FbSecondaryText
                        )
                    }
                }
            }

            // 5. Daily Budget Selector & 6. Duration Selector
            item {
                Surface(
                    color = FbCardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Currency Switcher (USD Primary, ETB Secondary with real NBE daily market rate)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Daily budget",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FbDarkText
                                )
                                Text(
                                    text = "1 USD = ${ExchangeRateManager.formattedRate()} ETB (NBE Daily Rate)",
                                    fontSize = 11.sp,
                                    color = FbSecondaryText
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                PaymentCurrency.values().forEach { currency ->
                                    val isSelected = state.selectedCurrency == currency
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            onStateChange(state.copy(selectedCurrency = currency))
                                        },
                                        label = {
                                            Text(
                                                text = currency.code,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    )
                                }
                            }
                        }

                        // Interactive Daily Budget Controller (- / + and Slider)
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.Center
                            ) {
                                FilledIconButton(
                                    onClick = {
                                        val next = (state.dailyBudgetUsd - 1.0).coerceAtLeast(1.0)
                                        onStateChange(state.copy(dailyBudgetUsd = next))
                                    },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = FbLightGrayBg,
                                        contentColor = FbDarkText
                                    ),
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("budget_decrease_button")
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease budget")
                                }

                                Spacer(modifier = Modifier.width(20.dp))

                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = "${state.formatAmount(state.dailyBudgetUsd)}/day",
                                        fontSize = 24.sp,
                                        fontWeight = FontWeight.ExtraBold,
                                        color = FbDarkText
                                    )
                                    val secondaryHint = if (state.selectedCurrency == PaymentCurrency.USD) {
                                        "≈ ETB ${String.format(Locale.US, "%,.0f", state.dailyBudgetEtb)}/day"
                                    } else {
                                        "≈ $${String.format(Locale.US, "%,.2f", state.dailyBudgetUsd)}/day"
                                    }
                                    Text(
                                        text = secondaryHint,
                                        fontSize = 12.sp,
                                        color = FbSecondaryText
                                    )
                                }

                                Spacer(modifier = Modifier.width(20.dp))

                                FilledIconButton(
                                    onClick = {
                                        val next = (state.dailyBudgetUsd + 1.0).coerceAtMost(500.0)
                                        onStateChange(state.copy(dailyBudgetUsd = next))
                                    },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = FbLightGrayBg,
                                        contentColor = FbDarkText
                                    ),
                                    modifier = Modifier
                                        .size(40.dp)
                                        .testTag("budget_increase_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase budget")
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Slider(
                                value = state.dailyBudgetUsd.toFloat(),
                                onValueChange = { newVal ->
                                    onStateChange(state.copy(dailyBudgetUsd = newVal.roundToInt().toDouble().coerceAtLeast(1.0)))
                                },
                                valueRange = 1f..100f,
                                colors = SliderDefaults.colors(
                                    thumbColor = FbBlue,
                                    activeTrackColor = FbBlue
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("daily_budget_slider")
                            )

                            // Dynamic "Estimated reach" indicator text based on budget value
                            Surface(
                                color = Color(0xFFE7F3FF),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Estimated daily reach",
                                        fontSize = 13.sp,
                                        color = FbDarkText
                                    )
                                    Text(
                                        text = state.estimatedReachText,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FbBlue
                                    )
                                }
                            }
                        }

                        HorizontalDivider(color = FbDivider.copy(alpha = 0.5f))

                        // Duration Selector (Default: "5 days")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Duration",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FbDarkText
                                )
                                Text(
                                    text = "How many days your ad will run",
                                    fontSize = 13.sp,
                                    color = FbSecondaryText
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                FilledIconButton(
                                    onClick = {
                                        val next = (state.durationDays - 1).coerceAtLeast(1)
                                        onStateChange(state.copy(durationDays = next))
                                    },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = FbLightGrayBg,
                                        contentColor = FbDarkText
                                    ),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("duration_decrease_button")
                                ) {
                                    Icon(Icons.Default.Remove, contentDescription = "Decrease days", modifier = Modifier.size(18.dp))
                                }

                                Text(
                                    text = "${state.durationDays} ${if (state.durationDays == 1) "day" else "days"}",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FbDarkText,
                                    modifier = Modifier.testTag("duration_value_text")
                                )

                                FilledIconButton(
                                    onClick = {
                                        val next = (state.durationDays + 1).coerceAtMost(60)
                                        onStateChange(state.copy(durationDays = next))
                                    },
                                    colors = IconButtonDefaults.filledIconButtonColors(
                                        containerColor = FbLightGrayBg,
                                        contentColor = FbDarkText
                                    ),
                                    modifier = Modifier
                                        .size(36.dp)
                                        .testTag("duration_increase_button")
                                ) {
                                    Icon(Icons.Default.Add, contentDescription = "Increase days", modifier = Modifier.size(18.dp))
                                }
                            }
                        }
                    }
                }
            }

            // 7. Payment Method Section (MasterCard icon + •••• 7228 + Verify / Change action)
            item {
                Surface(
                    color = FbCardBg,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Payment method",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = FbDarkText
                            )
                            TextButton(
                                onClick = { showPaymentDialog = true },
                                modifier = Modifier.testTag("change_payment_method_button")
                            ) {
                                Text(
                                    text = if (state.paymentMethod.isVerified) "Change" else "Verify",
                                    color = FbBlue,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(FbLightGrayBg.copy(alpha = 0.6f))
                                .clickable { showPaymentDialog = true }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                PaymentBrandSmallBadge(brandName = state.paymentMethod.brandName)
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "${state.paymentMethod.brandName} •••• ${state.paymentMethod.last4Digits}",
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = FbDarkText
                                    )
                                    Text(
                                        text = if (state.paymentMethod.isVerified) {
                                            "${state.paymentMethod.cardholderName} · Exp ${state.paymentMethod.expiryDate} · Verified (${state.selectedCurrency.code})"
                                        } else {
                                            "Tap to enter or verify debit/credit card"
                                        },
                                        fontSize = 12.sp,
                                        color = if (state.paymentMethod.isVerified) FbSuccessGreen else FbErrorRed
                                    )
                                }
                            }

                            Surface(
                                onClick = { showPaymentDialog = true },
                                shape = RoundedCornerShape(16.dp),
                                color = if (state.paymentMethod.isVerified) {
                                    Color(0xFFE8F5E9)
                                } else {
                                    FbBlue
                                },
                                modifier = Modifier.testTag("verify_payment_badge")
                            ) {
                                Text(
                                    text = if (state.paymentMethod.isVerified) "Verified ✓" else "Verify",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (state.paymentMethod.isVerified) FbSuccessGreen else Color.White,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            }

            // 8. Payment Summary Breakdown (Auto-Calculated)
            item {
                Surface(
                    color = FbCardBg,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("payment_summary_section")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "Payment summary",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = FbDarkText
                        )

                        Text(
                            text = "Your ad will run for ${state.durationDays} ${if (state.durationDays == 1) "day" else "days"}.",
                            fontSize = 14.sp,
                            color = FbSecondaryText
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        // Total budget: Daily Budget * Duration
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Total budget (${state.formatAmount(state.dailyBudgetUsd)} × ${state.durationDays} days)",
                                fontSize = 14.sp,
                                color = FbSecondaryText
                            )
                            Text(
                                text = state.formatAmount(state.totalBudgetUsd),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FbDarkText,
                                modifier = Modifier.testTag("summary_total_budget")
                            )
                        }

                        // Estimated tax: Total Budget * 5% Tax
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Estimated tax (5%)",
                                fontSize = 14.sp,
                                color = FbSecondaryText
                            )
                            Text(
                                text = state.formatAmount(state.estimatedTaxUsd),
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FbDarkText,
                                modifier = Modifier.testTag("summary_estimated_tax")
                            )
                        }

                        HorizontalDivider(color = FbDivider.copy(alpha = 0.6f))

                        // Total amount: Total Budget + Estimated Tax
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Total amount",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FbDarkText
                                )
                                val secondaryTotal = if (state.selectedCurrency == PaymentCurrency.USD) {
                                    "≈ ETB ${String.format(Locale.US, "%,.2f", state.totalAmountEtb)} at NBE Daily Rate"
                                } else {
                                    "≈ $${String.format(Locale.US, "%,.2f", state.totalAmountUsd)} USD"
                                }
                                Text(
                                    text = secondaryTotal,
                                    fontSize = 12.sp,
                                    color = FbSecondaryText
                                )
                            }

                            Text(
                                text = state.formatAmount(state.totalAmountUsd),
                                fontSize = 18.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = FbDarkText,
                                modifier = Modifier.testTag("summary_total_amount")
                            )
                        }
                    }
                }
            }
        }
    }

    // =========================================================================
    // MODALS & BOTTOM SHEETS FOR CAMPAIGN SETUP
    // =========================================================================

    // CTA Button Selector Modal Sheet
    if (showCtaSheet) {
        ModalBottomSheet(
            onDismissRequest = { showCtaSheet = false },
            containerColor = FbCardBg
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 28.dp)
            ) {
                Text(
                    text = "Choose a button",
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Bold,
                    color = FbDarkText,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                AdCtaButtonOption.values().forEach { option ->
                    val isSelected = state.selectedCta == option
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                onStateChange(state.copy(selectedCta = option))
                                showCtaSheet = false
                            }
                            .padding(vertical = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = option.label,
                            fontSize = 16.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = FbDarkText
                        )
                        RadioButton(
                            selected = isSelected,
                            onClick = {
                                onStateChange(state.copy(selectedCta = option))
                                showCtaSheet = false
                            },
                            colors = RadioButtonDefaults.colors(selectedColor = FbBlue)
                        )
                    }
                }
            }
        }
    }

    // Audience Targeting Dialog
    if (showAudienceDialog) {
        var genderSelection by remember { mutableStateOf(state.audience.genderLabel) }
        var minAge by remember { mutableIntStateOf(state.audience.minAge) }
        var maxAge by remember { mutableIntStateOf(state.audience.maxAge) }
        var locationText by remember { mutableStateOf(state.audience.locations.joinToString(", ")) }

        AlertDialog(
            onDismissRequest = { showAudienceDialog = false },
            containerColor = FbCardBg,
            title = { Text("Edit Audience Targeting", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Gender", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = FbSecondaryText)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Men/Women", "Men", "Women").forEach { g ->
                            FilterChip(
                                selected = genderSelection == g,
                                onClick = { genderSelection = g },
                                label = { Text(g, fontSize = 12.sp) }
                            )
                        }
                    }

                    Text(
                        text = "Age Range: $minAge - ${if (maxAge >= 65) "65+" else maxAge}",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FbSecondaryText
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { minAge = (minAge - 2).coerceAtLeast(18) }) { Text("- Min") }
                        OutlinedButton(onClick = { minAge = (minAge + 2).coerceAtMost(maxAge - 1) }) { Text("+ Min") }
                        OutlinedButton(onClick = { maxAge = (maxAge - 5).coerceAtLeast(minAge + 1) }) { Text("- Max") }
                        OutlinedButton(onClick = { maxAge = (maxAge + 5).coerceAtMost(65) }) { Text("+ Max") }
                    }

                    OutlinedTextField(
                        value = locationText,
                        onValueChange = { locationText = it },
                        label = { Text("Locations (comma-separated)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val locs = locationText.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                            .ifEmpty { listOf("Ethiopia") }
                        onStateChange(
                            state.copy(
                                audience = AudienceTargetingConfig(
                                    genderLabel = genderSelection,
                                    minAge = minAge,
                                    maxAge = maxAge,
                                    locations = locs
                                )
                            )
                        )
                        showAudienceDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FbBlue)
                ) {
                    Text("Save Audience")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAudienceDialog = false }) {
                    Text("Cancel", color = FbSecondaryText)
                }
            }
        )
    }

    // Full-screen "Debit or credit card" Screen (Matches Screenshot)
    if (showPaymentDialog) {
        DebitOrCreditCardDialog(
            initialPaymentMethod = state.paymentMethod,
            onDismiss = { showPaymentDialog = false },
            onSaveCard = { updatedPaymentMethod ->
                onStateChange(state.copy(paymentMethod = updatedPaymentMethod))
                showPaymentDialog = false
            }
        )
    }

    // Edit Ad Creative Caption & Media Dialog
    if (showEditCreativeDialog) {
        var captionDraft by remember { mutableStateOf(state.postCaption) }
        var mediaUrlDraft by remember { mutableStateOf(state.adMediaUrl) }

        AlertDialog(
            onDismissRequest = { showEditCreativeDialog = false },
            containerColor = FbCardBg,
            title = { Text("Edit Ad Creative", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = captionDraft,
                        onValueChange = { captionDraft = it },
                        label = { Text("Post Caption") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = mediaUrlDraft,
                        onValueChange = { mediaUrlDraft = it },
                        label = { Text("Ad Media Image URL") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onStateChange(
                            state.copy(
                                postCaption = captionDraft.ifBlank { "Engineer is a problem solver" },
                                adMediaUrl = mediaUrlDraft.ifBlank { state.adMediaUrl }
                            )
                        )
                        showEditCreativeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FbBlue)
                ) {
                    Text("Apply")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditCreativeDialog = false }) {
                    Text("Cancel", color = FbSecondaryText)
                }
            }
        )
    }
}

@Composable
private fun PreviewSocialActionItem(
    icon: ImageVector,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = FbSecondaryText,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = FbSecondaryText
        )
    }
}

@Composable
private fun MasterCardBrandBadge() {
    Surface(
        shape = RoundedCornerShape(6.dp),
        color = Color(0xFF1A1F2C),
        modifier = Modifier.size(width = 44.dp, height = 30.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = size.height * 0.32f
            drawCircle(
                color = Color(0xFFEB001B),
                radius = r,
                center = Offset(size.width * 0.38f, size.height * 0.5f)
            )
            drawCircle(
                color = Color(0xFFF79E1B).copy(alpha = 0.9f),
                radius = r,
                center = Offset(size.width * 0.62f, size.height * 0.5f)
            )
        }
    }
}

@Composable
private fun PaymentBrandSmallBadge(brandName: String) {
    when (brandName.uppercase(Locale.US)) {
        "VISA" -> VisaBrandLogoBadge()
        "AMEX" -> AmexBrandLogoBadge()
        "UNIONPAY" -> UnionPayBrandLogoBadge()
        "DISCOVER" -> DiscoverBrandLogoBadge()
        else -> MasterCardBrandBadge()
    }
}

@Composable
private fun VisaBrandLogoBadge(isHighlighted: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(5.dp),
        color = Color(0xFF1434CB),
        border = if (isHighlighted) BorderStroke(1.5.dp, FbBlue) else null,
        modifier = Modifier.size(width = 42.dp, height = 28.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "VISA",
                color = Color.White,
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun MasterCardLogoBadge(isHighlighted: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(5.dp),
        color = Color(0xFF192A56),
        border = if (isHighlighted) BorderStroke(1.5.dp, FbBlue) else null,
        modifier = Modifier.size(width = 42.dp, height = 28.dp)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val r = size.height * 0.30f
            drawCircle(
                color = Color(0xFFEB001B),
                radius = r,
                center = Offset(size.width * 0.39f, size.height * 0.5f)
            )
            drawCircle(
                color = Color(0xFFF79E1B).copy(alpha = 0.92f),
                radius = r,
                center = Offset(size.width * 0.61f, size.height * 0.5f)
            )
        }
    }
}

@Composable
private fun AmexBrandLogoBadge(isHighlighted: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(5.dp),
        color = Color(0xFF006FCF),
        border = if (isHighlighted) BorderStroke(1.5.dp, FbBlue) else null,
        modifier = Modifier.size(width = 42.dp, height = 28.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "AM\nEX",
                color = Color.White,
                fontSize = 7.5.sp,
                lineHeight = 8.sp,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun UnionPayBrandLogoBadge(isHighlighted: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(5.dp),
        color = Color.White,
        border = BorderStroke(if (isHighlighted) 1.5.dp else 1.dp, if (isHighlighted) FbBlue else Color(0xFFDADDE1)),
        modifier = Modifier.size(width = 42.dp, height = 28.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 3.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFFE21836), RoundedCornerShape(topStart = 2.dp, bottomStart = 2.dp))
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF00447C))
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(Color(0xFF007B84), RoundedCornerShape(topEnd = 2.dp, bottomEnd = 2.dp))
            )
        }
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            Text(
                text = "UnionPay",
                color = Color.White,
                fontSize = 6.5.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

@Composable
private fun DiscoverBrandLogoBadge(isHighlighted: Boolean = false) {
    Surface(
        shape = RoundedCornerShape(5.dp),
        color = Color.White,
        border = BorderStroke(if (isHighlighted) 1.5.dp else 1.dp, if (isHighlighted) FbBlue else Color(0xFFF58220)),
        modifier = Modifier.size(width = 46.dp, height = 28.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = "DISCOVER",
                color = Color(0xFF231F20),
                fontSize = 6.5.sp,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

/**
 * Full-Screen "Debit or credit card" Form matching the exact layout & styling of the screenshot:
 * - Top bar with Back (`<`), centered "Debit or credit card" title, and Close (`X`)
 * - "Card details" header row with VISA, MasterCard, AMEX, UnionPay, and DISCOVER badges
 * - 4 large rounded OutlinedTextFields:
 *   1. "Name on card"
 *   2. "Card number" (auto-formats 4-digit groups and detects card brand)
 *   3. "MM/YY" (auto-formats slash and validates month/year)
 *   4. "CVV" (3-4 digits)
 * - Lock icon + "Your payment methods are saved and stored securely." + blue "Terms and privacy policies apply."
 * - Bottom pill-shaped primary blue "Save" button
 */
@Composable
private fun DebitOrCreditCardDialog(
    initialPaymentMethod: PaymentMethodInfo,
    onDismiss: () -> Unit,
    onSaveCard: (PaymentMethodInfo) -> Unit
) {
    var nameOnCard by remember { mutableStateOf("") }
    var cardNumberDigits by remember { mutableStateOf(initialPaymentMethod.cardNumber.filter { it.isDigit() }) }
    var expiryDigits by remember { mutableStateOf("") }
    var cvvDigits by remember { mutableStateOf("") }
    var validationError by remember { mutableStateOf<String?>(null) }
    var showTermsDialog by remember { mutableStateOf(false) }

    // Format card number with spaces every 4 digits
    val formattedCardNumber = remember(cardNumberDigits) {
        cardNumberDigits.chunked(4).joinToString(" ")
    }

    // Format expiry date as MM/YY
    val formattedExpiry = remember(expiryDigits) {
        when {
            expiryDigits.length <= 2 -> expiryDigits
            else -> "${expiryDigits.take(2)}/${expiryDigits.drop(2).take(2)}"
        }
    }

    val detectedBrand = remember(cardNumberDigits, initialPaymentMethod.brandName) {
        if (cardNumberDigits.isNotEmpty()) {
            PaymentMethodInfo.detectCardBrand(cardNumberDigits)
        } else {
            initialPaymentMethod.brandName
        }
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("debit_or_credit_card_screen"),
            containerColor = Color.White,
            topBar = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("card_screen_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowLeft,
                            contentDescription = "Back",
                            tint = FbDarkText,
                            modifier = Modifier.size(30.dp)
                        )
                    }

                    Text(
                        text = "Debit or credit card",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = FbDarkText
                    )

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(48.dp)
                            .testTag("card_screen_close_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = FbDarkText,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            },
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.White)
                        .padding(horizontal = 20.dp, vertical = 18.dp)
                ) {
                    Button(
                        onClick = {
                            val cleanName = nameOnCard.trim()
                            val cleanNumber = cardNumberDigits.trim()
                            val cleanExp = expiryDigits.trim()
                            val cleanCvv = cvvDigits.trim()

                            val month = cleanExp.take(2).toIntOrNull() ?: 0
                            when {
                                cleanName.length < 2 -> {
                                    validationError = "Please enter the name on your card."
                                }
                                cleanNumber.length < 12 -> {
                                    validationError = "Please enter a valid 13–19 digit card number."
                                }
                                cleanExp.length < 4 || month !in 1..12 -> {
                                    validationError = "Please enter a valid expiration date (MM/YY)."
                                }
                                cleanCvv.length < 3 -> {
                                    validationError = "Please enter a valid 3 or 4 digit CVV security code."
                                }
                                else -> {
                                    validationError = null
                                    val brand = PaymentMethodInfo.detectCardBrand(cleanNumber)
                                    val last4 = cleanNumber.takeLast(4)
                                    val expFormatted = "${cleanExp.take(2)}/${cleanExp.drop(2).take(2)}"
                                    onSaveCard(
                                        initialPaymentMethod.copy(
                                            brandName = brand,
                                            last4Digits = last4,
                                            cardholderName = cleanName,
                                            cardNumber = cleanNumber,
                                            expiryDate = expFormatted,
                                            cvv = cleanCvv,
                                            isVerified = true
                                        )
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(28.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0064E0),
                            contentColor = Color.White
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("save_card_button")
                    ) {
                        Text(
                            text = "Save",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Spacer(modifier = Modifier.height(6.dp))

                // Header row: "Card details" + VISA, MasterCard, AMEX, UnionPay, DISCOVER badges
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Card details",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = FbDarkText
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        VisaBrandLogoBadge(isHighlighted = detectedBrand == "Visa")
                        MasterCardLogoBadge(isHighlighted = detectedBrand == "MasterCard")
                        AmexBrandLogoBadge(isHighlighted = detectedBrand == "AMEX")
                        UnionPayBrandLogoBadge(isHighlighted = detectedBrand == "UnionPay")
                        DiscoverBrandLogoBadge(isHighlighted = detectedBrand == "Discover")
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // 1. Name on card
                OutlinedTextField(
                    value = nameOnCard,
                    onValueChange = {
                        nameOnCard = it
                        validationError = null
                    },
                    label = {
                        Text(
                            text = "Name on card",
                            color = Color(0xFF606770)
                        )
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Text,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1C2B33),
                        unfocusedBorderColor = Color(0xFFCCD0D5),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 66.dp)
                        .testTag("card_name_input")
                )

                // 2. Card number
                OutlinedTextField(
                    value = formattedCardNumber,
                    onValueChange = { input ->
                        cardNumberDigits = input.filter { it.isDigit() }.take(19)
                        validationError = null
                    },
                    label = {
                        Text(
                            text = "Card number",
                            color = Color(0xFF606770)
                        )
                    },
                    placeholder = { Text("4532 •••• •••• 7228") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1C2B33),
                        unfocusedBorderColor = Color(0xFFCCD0D5),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 66.dp)
                        .testTag("card_number_input")
                )

                // 3. MM/YY
                OutlinedTextField(
                    value = formattedExpiry,
                    onValueChange = { input ->
                        expiryDigits = input.filter { it.isDigit() }.take(4)
                        validationError = null
                    },
                    label = {
                        Text(
                            text = "MM/YY",
                            color = Color(0xFF606770)
                        )
                    },
                    placeholder = { Text("08/29") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1C2B33),
                        unfocusedBorderColor = Color(0xFFCCD0D5),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 66.dp)
                        .testTag("card_expiry_input")
                )

                // 4. CVV
                OutlinedTextField(
                    value = cvvDigits,
                    onValueChange = { input ->
                        cvvDigits = input.filter { it.isDigit() }.take(4)
                        validationError = null
                    },
                    label = {
                        Text(
                            text = "CVV",
                            color = Color(0xFF606770)
                        )
                    },
                    placeholder = { Text("123") },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.NumberPassword,
                        imeAction = ImeAction.Done
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF1C2B33),
                        unfocusedBorderColor = Color(0xFFCCD0D5),
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 66.dp)
                        .testTag("card_cvv_input")
                )

                // Validation error feedback if user taps Save with incomplete fields
                AnimatedVisibility(visible = validationError != null) {
                    Surface(
                        color = FbErrorBannerBg,
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, FbErrorRed.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = FbErrorRed,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = validationError.orEmpty(),
                                fontSize = 13.sp,
                                color = FbErrorRed,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(22.dp))

                // Security Lock Icon + Notice + Blue Link (Matches Screenshot)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Secure payment",
                        tint = Color(0xFF5F6368),
                        modifier = Modifier.size(22.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "Your payment methods are saved and stored securely.",
                        fontSize = 15.sp,
                        color = Color(0xFF1C1E21),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Terms and privacy policies apply.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF0064E0),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.clickable { showTermsDialog = true }
                    )
                }

                Spacer(modifier = Modifier.height(28.dp))
            }
        }

        if (showTermsDialog) {
            AlertDialog(
                onDismissRequest = { showTermsDialog = false },
                containerColor = Color.White,
                title = { Text("Payment Terms & Privacy", fontWeight = FontWeight.Bold) },
                text = {
                    Text(
                        "Your debit or credit card details are encrypted and stored securely for Meskot Ads billing in USD or ETB (converted using the daily National Bank of Ethiopia exchange rate)."
                    )
                },
                confirmButton = {
                    TextButton(onClick = { showTermsDialog = false }) {
                        Text("Close", color = Color(0xFF0064E0), fontWeight = FontWeight.Bold)
                    }
                }
            )
        }
    }
}

