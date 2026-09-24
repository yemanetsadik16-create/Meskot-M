package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.recommendation.DislikedTopic
import com.example.recommendation.PrimaryInterest
import com.example.recommendation.UserEventBatch
import com.example.recommendation.UserInteractionEvent
import com.example.recommendation.UserInterestAnalysisResult
import com.example.recommendation.UserInterestTracker
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserInterestAnalysisModal(
    userId: String,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val recentEvents by UserInterestTracker.recentEvents.collectAsState()
    val activeProfile by UserInterestTracker.activeProfile.collectAsState()
    val isAnalyzing by UserInterestTracker.isAnalyzing.collectAsState()
    val personalizationEnabled by UserInterestTracker.personalizationEnabled.collectAsState()
    val lastError by UserInterestTracker.lastError.collectAsState()

    var selectedTab by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("🎯 Profile", "📊 Event Logs", "⚡ JSON Schema", "🧪 Test Engine")

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.96f)
                .fillMaxHeight(0.92f)
                .clip(RoundedCornerShape(24.dp))
                .testTag("user_interest_modal"),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Top Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .background(
                                    Brush.linearGradient(listOf(Color(0xFF6750A4), Color(0xFF009A44))),
                                    CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Psychology,
                                contentDescription = "AI Interest Engine",
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "AI Interest Engine",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Recommendation & User Interest Analysis",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("interest_modal_close")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Status Banner: Personalization Switch & Engine Tag
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Switch(
                                checked = personalizationEnabled,
                                onCheckedChange = { UserInterestTracker.togglePersonalization(it) },
                                modifier = Modifier.testTag("personalization_toggle")
                            )
                            Text(
                                text = if (personalizationEnabled) "Feed Personalization Active" else "Feed Personalization Off",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }

                        activeProfile?.let { prof ->
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFFE8F5E9), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "${prof.sourceEngine} • ${prof.latencyMs}ms",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF2E7D32)
                                )
                            }
                        }
                    }
                }

                if (lastError != null) {
                    Text(
                        text = "Notice: $lastError (Switched to on-device engine)",
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Tab Selector
                SecondaryScrollableTabRow(
                    selectedTabIndex = selectedTab,
                    edgePadding = 0.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content Views
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    if (isAnalyzing) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "Deducing interest profile with signal weighting...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        when (selectedTab) {
                            0 -> InterestProfileTab(activeProfile = activeProfile)
                            1 -> EventLogsTab(events = recentEvents)
                            2 -> JsonSchemaTab(activeProfile = activeProfile, context = context)
                            3 -> TestEngineTab(
                                userId = userId,
                                recentCount = recentEvents.size,
                                onRunLocal = { UserInterestTracker.runAnalysis(userId, preferLocal = true) },
                                onRunGemini = { UserInterestTracker.runAnalysis(userId, preferLocal = false) },
                                onLoadBenchmark = { UserInterestTracker.loadBenchmarkSample() },
                                onClearEvents = { UserInterestTracker.clearEvents() }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun InterestProfileTab(activeProfile: UserInterestAnalysisResult?) {
    if (activeProfile == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No interest profile computed yet. Run analysis to detect interests.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Feed Targets Strategy Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.TrendingUp,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "Feed Generation Strategy",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Diversity Ratio: ${activeProfile.feedGenerationStrategy.diversityRatio}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Medium
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Recommended Content Targets:",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        activeProfile.feedGenerationStrategy.recommendedContentTags.forEach { tag ->
                            SuggestionChip(
                                onClick = {},
                                label = { Text("#$tag", fontWeight = FontWeight.Bold, fontSize = 11.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(
                                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    labelColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }

                    if (activeProfile.feedGenerationStrategy.explorationTags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Exploration / Discovery Tags:",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            activeProfile.feedGenerationStrategy.explorationTags.forEach { tag ->
                                SuggestionChip(
                                    onClick = {},
                                    label = { Text("✨ #$tag", fontSize = 11.sp) },
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f),
                                        labelColor = MaterialTheme.colorScheme.secondary
                                    )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Section: Primary Interests
        item {
            Text(
                text = "Primary Interests (Affinity Scores 0.0 - 10.0)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        if (activeProfile.interestProfile.primaryInterests.isEmpty()) {
            item {
                Text(
                    text = "No positive topic affinities detected yet.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(activeProfile.interestProfile.primaryInterests) { interest ->
                PrimaryInterestCard(interest)
            }
        }

        // Section: Disliked or Fatigued Topics
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Disliked or Fatigued Topics (Penalties -10.0 to 0.0)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }

        if (activeProfile.interestProfile.dislikedOrFatiguedTopics.isEmpty()) {
            item {
                Text(
                    text = "No fast-skip or disliked penalties recorded.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(activeProfile.interestProfile.dislikedOrFatiguedTopics) { disliked ->
                DislikedTopicCard(disliked)
            }
        }
    }
}

@Composable
fun PrimaryInterestCard(interest: PrimaryInterest) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "#${interest.tag}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    ConfidenceBadge(confidence = interest.confidence)
                }

                Text(
                    text = "${interest.affinityScore} / 10",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = Color(0xFF009A44)
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (interest.affinityScore / 10.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = Color(0xFF009A44),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Primary Trigger: ${interest.primaryTrigger}",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun DislikedTopicCard(disliked: DislikedTopic) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f)),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "#${disliked.tag}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.error
                    )
                    Box(
                        modifier = Modifier
                            .background(MaterialTheme.colorScheme.error.copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = disliked.reason,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }

                Text(
                    text = "${disliked.penaltyScore}",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(6.dp))
            LinearProgressIndicator(
                progress = { (kotlin.math.abs(disliked.penaltyScore) / 10.0).toFloat().coerceIn(0f, 1f) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(CircleShape),
                color = MaterialTheme.colorScheme.error,
                trackColor = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.4f)
            )
        }
    }
}

@Composable
fun ConfidenceBadge(confidence: String) {
    val (bg, textCol) = when (confidence.uppercase()) {
        "HIGH" -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        "MEDIUM" -> Color(0xFFFFF8E1) to Color(0xFFF57F17)
        else -> Color(0xFFECEFF1) to Color(0xFF455A64)
    }

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp)
    ) {
        Text(
            text = confidence,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            color = textCol
        )
    }
}

@Composable
fun EventLogsTab(events: List<UserInteractionEvent>) {
    if (events.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No recent interaction events logged yet.")
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        items(events) { event ->
            Card(
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = event.itemId,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        ActionBadge(action = event.action)
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Dwell: ${event.dwellTimeSec}s • Watched: ${(event.watchPercentage * 100).toInt()}% • Time: ${event.timestamp.take(19)}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        event.tags.forEach { tag ->
                            Box(
                                modifier = Modifier
                                    .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(4.dp))
                                    .border(0.5.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("#$tag", fontSize = 10.sp, fontWeight = FontWeight.Medium)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ActionBadge(action: String) {
    val norm = action.uppercase()
    val (bg, fg) = when {
        norm.contains("SHARE") || norm.contains("SAVE") -> Color(0xFFE8F5E9) to Color(0xFF2E7D32)
        norm.contains("LIKE") || norm.contains("FAVORITE") -> Color(0xFFFCE4EC) to Color(0xFFC2185B)
        norm.contains("FULL_WATCH") -> Color(0xFFE3F2FD) to Color(0xFF1976D2)
        norm.contains("FAST_SKIP") -> Color(0xFFFFEBEE) to Color(0xFFD32F2F)
        else -> Color(0xFFF5F5F5) to Color(0xFF616161)
    }

    Box(
        modifier = Modifier
            .background(bg, RoundedCornerShape(6.dp))
            .padding(horizontal = 7.dp, vertical = 3.dp)
    ) {
        Text(text = norm, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, color = fg)
    }
}

@Composable
fun JsonSchemaTab(activeProfile: UserInterestAnalysisResult?, context: Context) {
    val jsonString = activeProfile?.toFormattedJson() ?: """{
  "user_id": "usr_7821",
  "interest_profile": {
    "primary_interests": [],
    "disliked_or_fatigued_topics": []
  },
  "feed_generation_strategy": {
    "recommended_content_tags": [],
    "exploration_tags": [],
    "diversity_ratio": "70% primary, 20% secondary, 10% discovery"
  }
}"""

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Standard Specification JSON Schema",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = {
                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                    clipboard.setPrimaryClip(ClipData.newPlainText("Meskot Interest Profile", jsonString))
                    Toast.makeText(context, "Copied JSON to clipboard", Toast.LENGTH_SHORT).show()
                },
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Copy JSON", fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Surface(
            color = Color(0xFF1E1E1E),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.weight(1f).fillMaxWidth()
        ) {
            LazyColumn(modifier = Modifier.padding(14.dp)) {
                item {
                    Text(
                        text = jsonString,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = Color(0xFF81C784),
                        lineHeight = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun TestEngineTab(
    userId: String,
    recentCount: Int,
    onRunLocal: () -> Unit,
    onRunGemini: () -> Unit,
    onLoadBenchmark: () -> Unit,
    onClearEvents: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Benchmark Test Suite & Engine Verification",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Verify the interest detection engine against the sample raw event logs (vid_101..vid_104) with rules: FAST_SKIP (-3.0), DWELL (+1.0), FULL_WATCH (+2.5), LIKE (+3.5), SHARE (+5.0), and timestamp decay.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        item {
            Button(
                onClick = onLoadBenchmark,
                modifier = Modifier.fillMaxWidth().testTag("btn_load_benchmark"),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Icon(imageVector = Icons.Default.Science, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Load Prompt Benchmark Batch (usr_7821)")
            }
        }

        item {
            Button(
                onClick = onRunGemini,
                modifier = Modifier.fillMaxWidth().testTag("btn_run_gemini"),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4))
            ) {
                Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Run Analysis with Gemini 3.5 Flash")
            }
        }

        item {
            OutlinedButton(
                onClick = onRunLocal,
                modifier = Modifier.fillMaxWidth().testTag("btn_run_local")
            ) {
                Icon(imageVector = Icons.Default.Speed, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Run On-Device Deterministic Engine")
            }
        }

        item {
            TextButton(
                onClick = onClearEvents,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(imageVector = Icons.Default.DeleteOutline, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Clear Current Event Logs ($recentCount recorded)")
            }
        }
    }
}
