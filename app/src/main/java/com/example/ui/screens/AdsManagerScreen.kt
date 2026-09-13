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
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.data.AdCampaign
import com.example.data.AppLanguage
import com.example.data.MeskotStrings
import com.example.ui.MeskotViewModel
import com.example.ui.ScreenTab
import com.example.ui.theme.CardBg
import com.example.ui.theme.Gold
import com.example.ui.theme.GoldDeep
import com.example.ui.theme.GoldSurface
import com.example.ui.theme.Ink
import com.example.ui.theme.LineBorder
import com.example.ui.theme.MutedText
import com.example.ui.theme.Paper
import com.example.ui.theme.Paper2
import java.util.Locale

enum class AdsHierarchyTab(val label: String) {
    CAMPAIGNS("Campaigns"),
    AD_SETS("Ad Sets & Targeting"),
    CREATIVES("Ad Creatives")
}

@Composable
fun AdsManagerScreen(
    viewModel: MeskotViewModel,
    onBack: () -> Unit
) {
    val campaigns by viewModel.adCampaigns.collectAsState()
    val currentLanguage by viewModel.currentLanguage.collectAsState()
    var selectedHierarchyTab by remember { mutableStateOf(AdsHierarchyTab.CAMPAIGNS) }
    var isCreateDialogOpen by remember { mutableStateOf(false) }

    // Summary KPIs
    val totalSpent = campaigns.sumOf { it.totalSpentEtb }
    val totalImpressions = campaigns.sumOf { it.impressions }
    val totalClicks = campaigns.sumOf { it.clicks }
    val overallCtr = if (totalImpressions > 0) (totalClicks.toDouble() / totalImpressions * 100) else 0.0
    val avgCpc = if (totalClicks > 0) (totalSpent / totalClicks) else 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Paper)
    ) {
        // Top Navigation Bar
        Surface(
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Ink
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = MeskotStrings.get("adsManager", currentLanguage),
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif,
                            color = Ink
                        )
                        Text(
                            text = "Meta / Facebook Ad Auction Engine · Meskot",
                            fontSize = 11.sp,
                            color = MutedText
                        )
                    }
                }

                Button(
                    onClick = { isCreateDialogOpen = true },
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldDeep),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    modifier = Modifier.testTag("btn_create_campaign")
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "New Campaign", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(10.dp))
                // KPI Metrics Dashboard
                Text(
                    text = "Account Performance Overview",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Ink,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricKpiCard(
                        title = "Total Spend",
                        value = "${String.format(Locale.US, "%,.0f", totalSpent)} ETB",
                        sub = "Simulated Chapa/Stripe balance",
                        icon = "💳",
                        modifier = Modifier.weight(1f)
                    )
                    MetricKpiCard(
                        title = "Impressions",
                        value = String.format(Locale.US, "%,d", totalImpressions),
                        sub = "Feed & Story Auction placements",
                        icon = "👁️",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricKpiCard(
                        title = "Link Clicks",
                        value = String.format(Locale.US, "%,d", totalClicks),
                        sub = "CTR: ${String.format(Locale.US, "%.2f", overallCtr)}%",
                        icon = "🖱️",
                        modifier = Modifier.weight(1f)
                    )
                    MetricKpiCard(
                        title = "Avg CPC",
                        value = "${String.format(Locale.US, "%.2f", avgCpc)} ETB",
                        sub = "Cost per link click",
                        icon = "📈",
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // Hierarchy Tab Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Paper2)
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    AdsHierarchyTab.values().forEach { tab ->
                        val isSelected = selectedHierarchyTab == tab
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) Color.White else Color.Transparent)
                                .clickable { selectedHierarchyTab = tab }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = tab.label,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) GoldDeep else MutedText
                            )
                        }
                    }
                }
            }

            // Content according to hierarchy tab
            when (selectedHierarchyTab) {
                AdsHierarchyTab.CAMPAIGNS -> {
                    item {
                        Text(
                            text = "Active & Scheduled Campaigns (${campaigns.size})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(campaigns, key = { it.id }) { campaign ->
                        CampaignRowCard(
                            campaign = campaign,
                            onToggleStatus = { viewModel.toggleAdCampaignStatus(campaign.id) }
                        )
                    }
                }

                AdsHierarchyTab.AD_SETS -> {
                    item {
                        Text(
                            text = "Ad Sets & Audience Segments",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(campaigns, key = { "adset_" + it.id }) { campaign ->
                        AdSetRowCard(campaign = campaign)
                    }
                }

                AdsHierarchyTab.CREATIVES -> {
                    item {
                        Text(
                            text = "Live Native Ad Creatives & Feed Previews",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Ink,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                    }

                    items(campaigns, key = { "creative_" + it.id }) { campaign ->
                        CreativePreviewCard(campaign = campaign)
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }

    // Create Campaign Dialog
    if (isCreateDialogOpen) {
        CreateCampaignDialog(
            onDismiss = { isCreateDialogOpen = false },
            onCreate = { name, obj, budget, headline, body, media, cta, dest ->
                viewModel.createAdCampaign(name, obj, budget, headline, body, media, cta, dest)
                isCreateDialogOpen = false
            }
        )
    }
}

@Composable
fun MetricKpiCard(
    title: String,
    value: String,
    sub: String,
    icon: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = title, fontSize = 11.sp, color = MutedText, fontWeight = FontWeight.SemiBold)
                Text(text = icon, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = value, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Ink)
            Text(text = sub, fontSize = 10.sp, color = MutedText, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

@Composable
fun CampaignRowCard(
    campaign: AdCampaign,
    onToggleStatus: () -> Unit
) {
    val isActive = campaign.status == "ACTIVE"

    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
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
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isActive) Color(0xFF10B981) else Color(0xFF94A3B8))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = campaign.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Ink,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Switch(
                    checked = isActive,
                    onCheckedChange = { onToggleStatus() },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = Color.White,
                        checkedTrackColor = GoldDeep,
                        uncheckedThumbColor = Color.White,
                        uncheckedTrackColor = Color(0xFFCBD5E1)
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GoldSurface)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "🎯 " + campaign.objective,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = GoldDeep
                    )
                }

                Text(
                    text = "Daily Budget: ${campaign.dailyBudgetEtb.toInt()} ETB/day",
                    fontSize = 11.sp,
                    color = MutedText
                )
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = LineBorder, thickness = 0.8.dp)

            // Metrics Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(text = "Spent", fontSize = 10.sp, color = MutedText)
                    Text(text = "${campaign.totalSpentEtb.toInt()} ETB", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
                }
                Column {
                    Text(text = "Impressions", fontSize = 10.sp, color = MutedText)
                    Text(text = String.format(Locale.US, "%,d", campaign.impressions), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
                }
                Column {
                    Text(text = "Clicks", fontSize = 10.sp, color = MutedText)
                    Text(text = String.format(Locale.US, "%,d", campaign.clicks), fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Ink)
                }
                Column {
                    Text(text = "CTR", fontSize = 10.sp, color = MutedText)
                    Text(text = "${String.format(Locale.US, "%.2f", campaign.ctr)}%", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                }
            }
        }
    }
}

@Composable
fun AdSetRowCard(campaign: AdCampaign) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(
                text = "Ad Set: ${campaign.name} (Audience Pool)",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Ink
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "📍 Target Audience:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = campaign.targetAudience, fontSize = 12.sp, color = MutedText)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "⚡ Auction Bidding:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = "Highest Value 2nd-Price Auction · CPM ~${campaign.cpmEtb.toInt()} ETB", fontSize = 12.sp, color = MutedText)
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "🎯 Objective Goal:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                Spacer(modifier = Modifier.width(6.dp))
                Text(text = campaign.objective + " (Optimized for feed clicks)", fontSize = 12.sp, color = GoldDeep)
            }
        }
    }
}

@Composable
fun CreativePreviewCard(campaign: AdCampaign) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, LineBorder),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // Header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = campaign.advertiserAvatar,
                        contentDescription = null,
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = campaign.advertiserName, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink)
                        Text(text = "Sponsored · 🌐", fontSize = 11.sp, color = MutedText)
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(GoldSurface)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(text = "Ad Creative", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = GoldDeep)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = campaign.primaryText, fontSize = 13.sp, color = Ink, lineHeight = 18.sp)

            Spacer(modifier = Modifier.height(8.dp))

            if (campaign.mediaUrl.isNotBlank()) {
                AsyncImage(
                    model = campaign.mediaUrl,
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Headline & CTA banner
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(Paper2)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = campaign.headline, fontWeight = FontWeight.Bold, fontSize = 13.sp, color = Ink, maxLines = 1, overflow = TextOverflow.Ellipsis)
                    Text(text = campaign.destinationUrl, fontSize = 10.sp, color = MutedText, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }

                Button(
                    onClick = { /* Simulated external link click */ },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = GoldDeep),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp)
                ) {
                    Text(text = campaign.ctaText, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun CreateCampaignDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, obj: String, budget: Double, headline: String, body: String, media: String, cta: String, dest: String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedObjective by remember { mutableStateOf("TRAFFIC") }
    var dailyBudget by remember { mutableFloatStateOf(600f) }
    var headline by remember { mutableStateOf("") }
    var primaryText by remember { mutableStateOf("") }
    var mediaUrl by remember { mutableStateOf("https://images.unsplash.com/photo-1509785307050-d4066910ec1e?w=800") }
    var ctaText by remember { mutableStateOf("Learn More") }
    var destinationUrl by remember { mutableStateOf("https://meskot.app") }

    val objectives = listOf("AWARENESS" to "Brand Awareness", "TRAFFIC" to "Website Traffic & Clicks", "CONVERSIONS" to "Leads & Conversions")
    val ctaOptions = listOf("Learn More", "Shop Now", "Sign Up", "Contact Us")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            modifier = Modifier.fillMaxWidth()
        ) {
            LazyColumn(
                modifier = Modifier.padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                item {
                    Text(
                        text = "📢 Create Facebook-Style Ad Campaign",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif,
                        color = Ink
                    )
                    Text(
                        text = "Publish targeted ads into the Meskot real-time feed auction.",
                        fontSize = 11.sp,
                        color = MutedText
                    )
                }

                item {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Campaign Name") },
                        placeholder = { Text("e.g. Addis Spring Promo") },
                        colors = com.example.ui.theme.meskotTextFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Text(text = "Campaign Objective:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        objectives.forEach { (code, label) ->
                            val isSel = selectedObjective == code
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) GoldSurface else Paper2)
                                    .border(1.dp, if (isSel) GoldDeep else LineBorder, RoundedCornerShape(8.dp))
                                    .clickable { selectedObjective = code }
                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSel) GoldDeep else Ink,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Daily Budget: ${dailyBudget.toInt()} ETB / day",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Ink
                    )
                    Slider(
                        value = dailyBudget,
                        onValueChange = { dailyBudget = it },
                        valueRange = 100f..5000f,
                        steps = 49,
                        colors = SliderDefaults.colors(thumbColor = GoldDeep, activeTrackColor = GoldDeep)
                    )
                }

                item {
                    OutlinedTextField(
                        value = headline,
                        onValueChange = { headline = it },
                        label = { Text("Ad Headline") },
                        placeholder = { Text("e.g. Authentic Ethiopian Spices") },
                        colors = com.example.ui.theme.meskotTextFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    OutlinedTextField(
                        value = primaryText,
                        onValueChange = { primaryText = it },
                        label = { Text("Primary Text Description") },
                        placeholder = { Text("Describe your product or announcement…") },
                        colors = com.example.ui.theme.meskotTextFieldColors(),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3
                    )
                }

                item {
                    Text(text = "Call-to-Action Button:", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = Ink)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ctaOptions.forEach { cta ->
                            val isSel = ctaText == cta
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSel) GoldDeep else Paper2)
                                    .clickable { ctaText = cta }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = cta,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSel) Color.White else Ink,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onDismiss) {
                            Text("Cancel", color = MutedText)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                if (name.isNotBlank() && headline.isNotBlank()) {
                                    onCreate(name, selectedObjective, dailyBudget.toDouble(), headline, primaryText, mediaUrl, ctaText, destinationUrl)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = GoldDeep),
                            enabled = name.isNotBlank() && headline.isNotBlank()
                        ) {
                            Text("Launch Campaign", fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}
