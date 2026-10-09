package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.CachePolicy
import coil.request.ImageRequest
import com.example.data.*
import com.example.ui.MeskotViewModel
import com.example.ui.components.InteractiveGoogleMapCard
import com.example.ui.components.RealLocationPickerDialog
import com.example.ui.components.UserAvatar
import com.example.ui.components.VerifiedBadge
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.*

private val FbBlue = Color(0xFF96691F) // Meskot Bronze Gold
private val FbPillBg = Color(0xFFF6EDD2) // Meskot Warm Gold Tint
private val FbDarkText = Color(0xFF1D2419) // Meskot Deep Ink
private val FbSecondaryText = Color(0xFF7A7360) // Meskot Warm Muted Text
private val FbSurfaceBg = Color(0xFFFBF8EF) // Meskot Warm Parchment Background
private val FbLightDivider = Color(0xFFEADFC2) // Meskot Gold Line Border
private val MeskotCardPaper = Color(0xFFFFFDF8)
private val MeskotGold1 = Color(0xFFEFD06C)
private val MeskotGold2 = Color(0xFFA87B25)
private val MeskotMaroon = Color(0xFF8A1C2B)

/**
 * Facebook Marketplace-style Main Screen (`MarketplaceScreen`)
 *
 * Implements:
 * 1. Top App Bar with back button, "Marketplace" bold title, and search icon button.
 * 2. Horizontal scrollable Action Pill Bar:
 *    - Profile / User Account Icon Chip
 *    - "Inbox" Chip (buyer-seller chat history with unread/thread badge)
 *    - "Sell" Chip (opens item listing creation modal)
 *    - "Categories" Chip (opens category filter bottom sheet)
 *    - "Search" Chip (opens full search view)
 * 3. Feed Header:
 *    - "Today's picks" section title
 *    - Interactive Location Badge ("Ariena · 65 km") with location pin icon
 * 4. Responsive 2-Column Product Grid:
 *    - Square/tailored aspect-ratio cached product thumbnail (`Coil` with disk/memory cache)
 *    - Formatted currency price tag + 1-line truncated title with ellipsis
 * 5. Product Details Modal with direct real-time Seller Chat & Inbox integration.
 */
enum class MarketplaceSortOption(val label: String) {
    RECOMMENDED("Recommended"),
    NEWEST("Newest First"),
    NEAREST("Distance: Nearest"),
    PRICE_LOW_HIGH("Price: Low to High"),
    PRICE_HIGH_LOW("Price: High to Low")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MarketplaceScreen(
    viewModel: MeskotViewModel,
    onBack: () -> Unit
) {
    val currentUser by viewModel.currentUser.collectAsState()
    val categories by viewModel.marketplaceCategories.collectAsState()
    val allListings by viewModel.marketplaceListings.collectAsState()
    val savedListingIds by viewModel.savedListingIds.collectAsState()
    val marketplaceChatList by viewModel.marketplaceChats.collectAsState()
    val currentLocation by viewModel.marketplaceCurrentLocation.collectAsState()
    val radiusKm = currentLocation.radiusKm.toDouble()

    // Group flat list of MarketplaceChatMessage by chatId
    val marketplaceChats = remember(marketplaceChatList) {
        marketplaceChatList.groupBy { it.chatId.ifBlank { "chat_${it.itemId}" } }
    }

    // Filter & Search States
    var selectedCategoryId by remember { mutableStateOf("all") }
    var searchQuery by remember { mutableStateOf("") }
    var isSearchBarVisible by remember { mutableStateOf(false) }
    var showOnlySaved by remember { mutableStateOf(false) }
    var selectedConditionFilter by remember { mutableStateOf("All") }
    var selectedSortOption by remember { mutableStateOf(MarketplaceSortOption.RECOMMENDED) }
    var minPriceFilter by remember { mutableStateOf("") }
    var maxPriceFilter by remember { mutableStateOf("") }

    // Modal / Sheet States
    var showCategoriesSheet by remember { mutableStateOf(false) }
    var showFilterSortSheet by remember { mutableStateOf(false) }
    var showLocationDialog by remember { mutableStateOf(false) }
    var showSellModal by remember { mutableStateOf(false) }
    var showInboxModal by remember { mutableStateOf(false) }
    var showSellerProfileSheet by remember { mutableStateOf(false) }
    var selectedListingId by remember { mutableStateOf<String?>(null) }
    var activeChatThreadId by remember { mutableStateOf<String?>(null) }

    val selectedListing = remember(allListings, selectedListingId, savedListingIds) {
        allListings.find { it.id == selectedListingId }?.let {
            it.copy(isSaved = savedListingIds.contains(it.id))
        }
    }

    val focusManager = LocalFocusManager.current

    BackHandler {
        when {
            activeChatThreadId != null -> activeChatThreadId = null
            selectedListingId != null -> selectedListingId = null
            showSellModal -> showSellModal = false
            showInboxModal -> showInboxModal = false
            showSellerProfileSheet -> showSellerProfileSheet = false
            showFilterSortSheet -> showFilterSortSheet = false
            showOnlySaved -> showOnlySaved = false
            isSearchBarVisible -> {
                isSearchBarVisible = false
                searchQuery = ""
            }
            else -> onBack()
        }
    }

    val activeFilterCount = remember(selectedConditionFilter, selectedSortOption, minPriceFilter, maxPriceFilter) {
        var count = 0
        if (selectedConditionFilter != "All") count++
        if (selectedSortOption != MarketplaceSortOption.RECOMMENDED) count++
        if (minPriceFilter.isNotBlank() || maxPriceFilter.isNotBlank()) count++
        count
    }

    // Filtered & proximity/date/price sorted listings for "Today's picks"
    val filteredListings = remember(
        allListings,
        savedListingIds,
        selectedCategoryId,
        searchQuery,
        currentLocation,
        radiusKm,
        showOnlySaved,
        selectedConditionFilter,
        selectedSortOption,
        minPriceFilter,
        maxPriceFilter
    ) {
        val minUsd = minPriceFilter.toDoubleOrNull()
        val maxUsd = maxPriceFilter.toDoubleOrNull()
        val nbeRate = ExchangeRateManager.usdToEtbRate

        val baseList = allListings
            .map { item ->
                val computedDist = calculateHaversineDistanceKm(
                    currentLocation.latitude,
                    currentLocation.longitude,
                    item.location.latitude,
                    item.location.longitude
                )
                val effectiveDist = if (item.distanceKm > 0 && (currentLocation.name.equals("Ariena", ignoreCase = true) || currentLocation.name.equals("Mekelle", ignoreCase = true))) {
                    item.distanceKm
                } else {
                    computedDist
                }
                item.copy(
                    distanceKm = effectiveDist,
                    isSaved = savedListingIds.contains(item.id)
                )
            }
            .filter { item ->
                val matchesSaved = !showOnlySaved || item.isSaved
                val matchesCategory = selectedCategoryId == "all" ||
                        item.categoryId.equals(selectedCategoryId, ignoreCase = true)
                val matchesCondition = selectedConditionFilter == "All" ||
                        item.condition.equals(selectedConditionFilter, ignoreCase = true)
                val q = searchQuery.trim().lowercase()
                val matchesQuery = q.isEmpty() ||
                        item.title.lowercase().contains(q) ||
                        item.description.lowercase().contains(q) ||
                        item.location.name.lowercase().contains(q) ||
                        item.categoryId.lowercase().contains(q) ||
                        item.sellerName.lowercase().contains(q)
                val matchesRadius = radiusKm >= 250.0 || item.distanceKm <= radiusKm

                val itemPriceInUsd = if (item.currency.equals("USD", ignoreCase = true)) {
                    item.price
                } else {
                    item.price / nbeRate
                }
                val matchesMinPrice = minUsd == null || itemPriceInUsd >= minUsd
                val matchesMaxPrice = maxUsd == null || itemPriceInUsd <= maxUsd

                matchesSaved && matchesCategory && matchesCondition && matchesQuery && matchesRadius && matchesMinPrice && matchesMaxPrice
            }

        when (selectedSortOption) {
            MarketplaceSortOption.RECOMMENDED -> baseList.sortedWith(
                compareByDescending<ListingItem> { it.isAvailable }
                    .thenByDescending { it.isPromoted }
                    .thenBy { (it.distanceKm / 15.0).toInt() }
                    .thenByDescending { it.createdAt }
            )
            MarketplaceSortOption.NEWEST -> baseList.sortedByDescending { it.createdAt }
            MarketplaceSortOption.NEAREST -> baseList.sortedBy { it.distanceKm }
            MarketplaceSortOption.PRICE_LOW_HIGH -> baseList.sortedBy {
                if (it.currency.equals("USD", ignoreCase = true)) it.price else it.price / nbeRate
            }
            MarketplaceSortOption.PRICE_HIGH_LOW -> baseList.sortedByDescending {
                if (it.currency.equals("USD", ignoreCase = true)) it.price else it.price / nbeRate
            }
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("marketplace_screen"),
        containerColor = FbSurfaceBg,
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(FbSurfaceBg)
            ) {
                // Meskot Brand Gold Top Accent Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .background(
                            androidx.compose.ui.graphics.Brush.horizontalGradient(
                                listOf(MeskotGold1, MeskotGold2, FbBlue)
                            )
                        )
                )

                // 1. Top App Bar (Meskot Brand Styled)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Surface(
                            onClick = onBack,
                            shape = CircleShape,
                            color = MeskotCardPaper,
                            border = BorderStroke(1.8.dp, MeskotGold1),
                            modifier = Modifier
                                .size(42.dp)
                                .testTag("marketplace_back_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = FbBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text(
                                text = "Meskot Marketplace",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = FbDarkText
                            )
                            Text(
                                text = "ገበያ በመስኮት · Buy, Sell & Escrow Locally",
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = FbBlue
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            onClick = { showFilterSortSheet = true },
                            shape = CircleShape,
                            color = if (activeFilterCount > 0) MeskotGold2 else FbPillBg,
                            border = BorderStroke(1.5.dp, MeskotGold1),
                            modifier = Modifier
                                .size(42.dp)
                                .testTag("marketplace_filter_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "Filter & Sort Marketplace",
                                    tint = if (activeFilterCount > 0) Color.White else FbBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Surface(
                            onClick = { isSearchBarVisible = !isSearchBarVisible },
                            shape = CircleShape,
                            color = FbPillBg,
                            border = BorderStroke(1.5.dp, MeskotGold1),
                            modifier = Modifier
                                .size(42.dp)
                                .testTag("marketplace_top_search_button")
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "Search Marketplace",
                                    tint = FbBlue,
                                    modifier = Modifier.size(21.dp)
                                )
                            }
                        }
                    }
                }

                // Expandable Full Search View Bar
                AnimatedVisibility(
                    visible = isSearchBarVisible,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = {
                            Text(
                                "Search Marketplace for vehicles, phones, houses...",
                                fontSize = 14.sp,
                                color = FbSecondaryText
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = FbBlue
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear search",
                                        tint = FbBlue
                                    )
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = MeskotCardPaper,
                            unfocusedContainerColor = MeskotCardPaper,
                            focusedBorderColor = MeskotGold2,
                            unfocusedBorderColor = FbLightDivider
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { focusManager.clearFocus() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 4.dp)
                            .testTag("marketplace_search_input")
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 165.dp),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(FbSurfaceBg)
                .testTag("marketplace_product_grid"),
            contentPadding = PaddingValues(bottom = 28.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 2. Action Pill Bar (Horizontal Scroll Row)
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp)
                            .testTag("marketplace_action_pill_bar"),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Pill 1: Profile / Seller Commerce Hub Chip
                        item {
                            Surface(
                                onClick = { showSellerProfileSheet = true },
                                shape = RoundedCornerShape(22.dp),
                                color = MeskotCardPaper,
                                border = BorderStroke(1.2.dp, FbLightDivider),
                                modifier = Modifier
                                    .height(38.dp)
                                    .testTag("pill_profile")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Your Marketplace Profile",
                                        tint = FbBlue,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "You",
                                        fontSize = 13.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FbDarkText
                                    )
                                }
                            }
                        }

                        // Pill 2: "Sell" Chip
                        item {
                            MarketplaceActionChip(
                                label = "Sell",
                                onClick = { showSellModal = true },
                                testTag = "pill_sell"
                            )
                        }

                        // Pill 3: "Inbox" Chip
                        item {
                            MarketplaceActionChip(
                                label = "Inbox",
                                badgeCount = marketplaceChats.size,
                                onClick = { showInboxModal = true },
                                testTag = "pill_inbox"
                            )
                        }

                        // Pill 4: "Saved" Chip
                        item {
                            MarketplaceActionChip(
                                label = "Saved",
                                badgeCount = savedListingIds.size,
                                isSelected = showOnlySaved,
                                onClick = { showOnlySaved = !showOnlySaved },
                                testTag = "pill_saved"
                            )
                        }

                        // Pill 5: "Categories" Chip
                        item {
                            val activeCategory = categories.find { it.id == selectedCategoryId }
                            val categoryChipLabel = if (selectedCategoryId == "all" || activeCategory == null) {
                                "Categories"
                            } else {
                                activeCategory.name
                            }
                            MarketplaceActionChip(
                                label = categoryChipLabel,
                                isSelected = selectedCategoryId != "all",
                                onClick = { showCategoriesSheet = true },
                                testTag = "pill_categories"
                            )
                        }

                        // Pill 6: "Sort & Filter" Chip
                        item {
                            MarketplaceActionChip(
                                label = if (selectedSortOption == MarketplaceSortOption.RECOMMENDED) "Sort & Filter" else selectedSortOption.label,
                                badgeCount = activeFilterCount,
                                isSelected = activeFilterCount > 0,
                                onClick = { showFilterSortSheet = true },
                                testTag = "pill_filter_sort"
                            )
                        }
                    }

                    // Quick Horizontal Category Strip (Facebook Marketplace style)
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 6.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(categories.take(9), key = { it.id }) { cat ->
                            val isSelected = selectedCategoryId == cat.id
                            Surface(
                                onClick = {
                                    selectedCategoryId = if (isSelected && cat.id != "all") "all" else cat.id
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = if (isSelected) FbPillBg else Color.Transparent,
                                border = BorderStroke(
                                    width = 1.dp,
                                    color = if (isSelected) MeskotGold2 else FbLightDivider.copy(alpha = 0.8f)
                                )
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(text = cat.icon, fontSize = 13.sp)
                                    Text(
                                        text = cat.name,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        color = if (isSelected) FbBlue else FbSecondaryText
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = FbLightDivider.copy(alpha = 0.6f), thickness = 0.8.dp)
                }
            }

            // 3. Feed Header ("Today's picks" / "Saved items" + Location Badge)
            item(span = { GridItemSpan(maxLineSpan) }) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = if (showOnlySaved) "Saved Marketplace Items" else "Today's picks",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = FbDarkText
                            )
                            Text(
                                text = "${filteredListings.size} items available · NBE Rate 1 USD = ${String.format(Locale.US, "%.0f", ExchangeRateManager.usdToEtbRate)} ETB",
                                fontSize = 11.5.sp,
                                color = FbSecondaryText
                            )
                        }

                        // Interactive Location Pill matching Sell on Meskot (`📍 ${loc}` + gold chevron)
                        Surface(
                            onClick = { showLocationDialog = true },
                            shape = RoundedCornerShape(16.dp),
                            color = Color(0xFFFBF8EF),
                            border = BorderStroke(1.5.dp, Color(0xFFEADFC2)),
                            modifier = Modifier.testTag("marketplace_location_badge")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = "📍 ${currentLocation.name.ifBlank { "Mekelle" }}",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF1D2419),
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Icon(
                                    imageVector = Icons.Default.KeyboardArrowDown,
                                    contentDescription = "Change location",
                                    tint = Color(0xFFA87B25),
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Active filter chips banner if user filtered by category, saved, condition, or search
                    if (selectedCategoryId != "all" || searchQuery.isNotBlank() || showOnlySaved || activeFilterCount > 0) {
                        Spacer(modifier = Modifier.height(6.dp))
                        LazyRow(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (showOnlySaved) {
                                item {
                                    InputChip(
                                        selected = true,
                                        onClick = { showOnlySaved = false },
                                        label = { Text("Saved Only", fontSize = 12.sp) },
                                        trailingIcon = {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                                        }
                                    )
                                }
                            }
                            if (selectedCategoryId != "all") {
                                item {
                                    val catName = categories.find { it.id == selectedCategoryId }?.name ?: selectedCategoryId
                                    InputChip(
                                        selected = true,
                                        onClick = { selectedCategoryId = "all" },
                                        label = { Text(catName, fontSize = 12.sp) },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear category",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    )
                                }
                            }
                            if (selectedConditionFilter != "All") {
                                item {
                                    InputChip(
                                        selected = true,
                                        onClick = { selectedConditionFilter = "All" },
                                        label = { Text(selectedConditionFilter, fontSize = 12.sp) },
                                        trailingIcon = {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                                        }
                                    )
                                }
                            }
                            if (selectedSortOption != MarketplaceSortOption.RECOMMENDED) {
                                item {
                                    InputChip(
                                        selected = true,
                                        onClick = { selectedSortOption = MarketplaceSortOption.RECOMMENDED },
                                        label = { Text(selectedSortOption.label, fontSize = 12.sp) },
                                        trailingIcon = {
                                            Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(14.dp))
                                        }
                                    )
                                }
                            }
                            if (searchQuery.isNotBlank()) {
                                item {
                                    InputChip(
                                        selected = true,
                                        onClick = { searchQuery = "" },
                                        label = { Text("\"$searchQuery\"", fontSize = 12.sp) },
                                        trailingIcon = {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Clear search",
                                                modifier = Modifier.size(14.dp)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Empty state if filter returns no items
            if (filteredListings.isEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (showOnlySaved) Icons.Outlined.BookmarkBorder else Icons.Outlined.Storefront,
                            contentDescription = null,
                            tint = FbSecondaryText,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (showOnlySaved) "No saved listings yet" else "No listings match your filter",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = FbDarkText
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (showOnlySaved) "Tap the bookmark icon on any Marketplace listing to save it for later."
                            else "Try expanding your distance radius or clearing the category and price filters.",
                            fontSize = 14.sp,
                            color = FbSecondaryText
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                selectedCategoryId = "all"
                                searchQuery = ""
                                showOnlySaved = false
                                selectedConditionFilter = "All"
                                selectedSortOption = MarketplaceSortOption.RECOMMENDED
                                minPriceFilter = ""
                                maxPriceFilter = ""
                                viewModel.updateMarketplaceLocationFilter(
                                    MarketplaceLocation(9.0192, 38.7525, "Ariena", 65),
                                    65.0
                                )
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FbBlue)
                        ) {
                            Text("Reset All Filters")
                        }
                    }
                }
            }

            // 4. 2-Column Product Grid Cards with Save Toggle & Sold/Promoted Badges
            items(
                items = filteredListings,
                key = { it.id }
            ) { item ->
                MarketplaceProductGridCard(
                    item = item,
                    onToggleSave = { viewModel.toggleSaveListing(item.id) },
                    onClick = {
                        viewModel.incrementMarketplaceListingViews(item.id)
                        selectedListingId = item.id
                    }
                )
            }
        }
    }

    // =========================================================================
    // MODALS & BOTTOM SHEETS
    // =========================================================================

    // Categories Filter Bottom Sheet
    if (showCategoriesSheet) {
        MarketplaceCategoriesBottomSheet(
            categories = categories,
            selectedCategoryId = selectedCategoryId,
            onSelectCategory = { catId ->
                selectedCategoryId = catId
                showCategoriesSheet = false
            },
            onDismiss = { showCategoriesSheet = false }
        )
    }

    // Sort & Filter Bottom Sheet (Price range, Condition, Sort order)
    if (showFilterSortSheet) {
        MarketplaceFilterSortBottomSheet(
            selectedSort = selectedSortOption,
            selectedCondition = selectedConditionFilter,
            minPriceUsd = minPriceFilter,
            maxPriceUsd = maxPriceFilter,
            onApply = { sort, cond, minP, maxP ->
                selectedSortOption = sort
                selectedConditionFilter = cond
                minPriceFilter = minP
                maxPriceFilter = maxP
                showFilterSortSheet = false
            },
            onReset = {
                selectedSortOption = MarketplaceSortOption.RECOMMENDED
                selectedConditionFilter = "All"
                minPriceFilter = ""
                maxPriceFilter = ""
                showFilterSortSheet = false
            },
            onDismiss = { showFilterSortSheet = false }
        )
    }

    // Location & Radius Picker Dialog
    if (showLocationDialog) {
        MarketplaceLocationDialog(
            currentLocation = currentLocation,
            currentRadiusKm = radiusKm,
            onApply = { newLocation, newRadius ->
                viewModel.updateMarketplaceLocationFilter(newLocation, newRadius)
                showLocationDialog = false
            },
            onDismiss = { showLocationDialog = false }
        )
    }

    // "Sell" Flow (Sell on Meskot · ሽያጭ በመስኮት Modal)
    if (showSellModal) {
        SellOnMeskotModal(
            categories = categories.filter { it.id != "all" },
            defaultLocation = currentLocation,
            onDismiss = { showSellModal = false },
            onSubmit = { title, price, currency, categoryId, desc, imageUrls, loc, condition, isNegotiable, deliveryOption, sellerPhone, allowChat, allowCall, allowWhatsApp, isPromoted ->
                viewModel.createMarketplaceListing(
                    title = title,
                    price = price,
                    currency = currency,
                    categoryId = categoryId,
                    description = desc,
                    imageUrls = imageUrls,
                    location = loc,
                    condition = condition,
                    isNegotiable = isNegotiable,
                    deliveryOption = deliveryOption,
                    sellerPhone = sellerPhone,
                    allowChat = allowChat,
                    allowCall = allowCall,
                    allowWhatsApp = allowWhatsApp,
                    isPromoted = isPromoted
                ) {
                    showSellModal = false
                }
            }
        )
    }

    // Marketplace Buyer-Seller Inbox Modal
    if (showInboxModal) {
        MarketplaceInboxModal(
            chatsMap = marketplaceChats,
            listings = allListings,
            onOpenThread = { chatId ->
                activeChatThreadId = chatId
            },
            onDismiss = { showInboxModal = false }
        )
    }

    // Active Buyer-Seller Real-Time Chat Modal
    activeChatThreadId?.let { chatId ->
        val threadMessages = marketplaceChats[chatId] ?: emptyList()
        val firstMsg = threadMessages.firstOrNull()
        val relatedItem = allListings.find { it.id == firstMsg?.itemId } ?: selectedListing
        if (relatedItem != null) {
            MarketplaceConversationDialog(
                currentUser = currentUser,
                item = relatedItem,
                messages = threadMessages,
                onSendMessage = { msgText ->
                    viewModel.sendMarketplaceInquiry(relatedItem, msgText)
                },
                onOpenFullMessenger = {
                    activeChatThreadId = null
                    selectedListingId = null
                    showInboxModal = false
                    viewModel.startChatWithSeller(relatedItem)
                },
                onDismiss = { activeChatThreadId = null }
            )
        }
    }

    // Product Detail & Direct Seller Chat Trigger Modal
    selectedListing?.let { listing ->
        val similarItems = remember(allListings, listing.id, listing.categoryId) {
            allListings.filter { it.id != listing.id && it.categoryId == listing.categoryId }.take(6)
        }
        MarketplaceProductDetailModal(
            item = listing,
            currentUser = currentUser,
            similarItems = similarItems,
            onDismiss = { selectedListingId = null },
            onToggleSave = { viewModel.toggleSaveListing(listing.id) },
            onSendQuickMessage = { messageText ->
                viewModel.sendMarketplaceInquiry(listing, messageText)
                val uid = currentUser?.uid ?: "usr_current"
                activeChatThreadId = "mkt_chat_${uid}_${listing.sellerId}_${listing.id}"
            },
            onSendOffer = { offerAmt, offerCurr ->
                viewModel.sendMarketplaceOffer(listing, offerAmt, offerCurr)
                val uid = currentUser?.uid ?: "usr_current"
                activeChatThreadId = "mkt_chat_${uid}_${listing.sellerId}_${listing.id}"
            },
            onBuyWithChapa = {
                viewModel.buyMarketplaceItemWithChapa(listing)
            },
            onMessageSellerInMessenger = { initialText ->
                selectedListingId = null
                viewModel.startChatWithSeller(listing, initialText)
            },
            onToggleSoldStatus = {
                viewModel.toggleMarketplaceListingAvailability(listing.id)
            },
            onUpdateListing = { newTitle, newPrice, newCurrency, newCond, newDesc, newNeg ->
                viewModel.updateMarketplaceListingDetails(
                    itemId = listing.id,
                    newTitle = newTitle,
                    newPrice = newPrice,
                    newCurrency = newCurrency,
                    newCondition = newCond,
                    newDescription = newDesc,
                    newIsNegotiable = newNeg
                )
            },
            onBoostListing = {
                viewModel.boostMarketplaceListing(listing)
            },
            onDeleteListing = {
                selectedListingId = null
                viewModel.deleteMarketplaceListing(listing.id, listing.title)
            },
            onSelectSimilarItem = { sim ->
                viewModel.incrementMarketplaceListingViews(sim.id)
                selectedListingId = sim.id
            }
        )
    }

    // Seller / Account Commerce Profile Sheet
    if (showSellerProfileSheet) {
        val myListings = allListings.filter { it.sellerId == currentUser?.uid || it.sellerId == "usr_current" }
        val savedListings = allListings.filter { savedListingIds.contains(it.id) }
        MarketplaceAccountSheet(
            currentUser = currentUser,
            myListings = myListings,
            savedListings = savedListings,
            inboxCount = marketplaceChats.size,
            onOpenCreateListing = {
                showSellerProfileSheet = false
                showSellModal = true
            },
            onOpenInbox = {
                showSellerProfileSheet = false
                showInboxModal = true
            },
            onSelectListing = { item ->
                showSellerProfileSheet = false
                selectedListingId = item.id
            },
            onToggleSold = { item ->
                viewModel.toggleMarketplaceListingAvailability(item.id)
            },
            onBoostListing = { item ->
                showSellerProfileSheet = false
                viewModel.boostMarketplaceListing(item)
            },
            onDeleteListing = { item ->
                viewModel.deleteMarketplaceListing(item.id, item.title)
            },
            onDismiss = { showSellerProfileSheet = false }
        )
    }
}

/**
 * Action Pill Chip matching the Facebook Marketplace screenshot
 */
@Composable
private fun MarketplaceActionChip(
    label: String,
    badgeCount: Int = 0,
    isSelected: Boolean = false,
    onClick: () -> Unit,
    testTag: String
) {
    val isPrimarySell = label.equals("Sell", ignoreCase = true)
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(22.dp),
        color = when {
            isPrimarySell -> MeskotGold2
            isSelected -> FbPillBg
            else -> MeskotCardPaper
        },
        border = BorderStroke(
            width = if (isSelected || isPrimarySell) 1.5.dp else 1.2.dp,
            color = if (isSelected || isPrimarySell) MeskotGold2 else FbLightDivider
        ),
        modifier = Modifier
            .height(38.dp)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = if (isPrimarySell) "+ Sell" else label,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    isPrimarySell -> Color.White
                    isSelected -> FbBlue
                    else -> FbDarkText
                }
            )
            if (badgeCount > 0) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(MeskotMaroon),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = badgeCount.toString(),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * 2-Column Product Grid Card styled with Meskot Brand Palette:
 * - Warm ivory card with delicate gold border & cached product thumbnail
 * - Save/Bookmark heart icon in top-right + Sold/Promoted badges
 * - Formatted Price in Meskot Gold + 1-line truncated Title
 */
@Composable
private fun MarketplaceProductGridCard(
    item: ListingItem,
    onToggleSave: () -> Unit,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val primaryImageUrl = item.primaryImageUrl

    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MeskotCardPaper),
        border = BorderStroke(
            width = if (item.isPromoted) 1.5.dp else 1.dp,
            color = if (item.isPromoted) MeskotGold2 else FbLightDivider
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 4.dp)
            .testTag("marketplace_item_${item.id}")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Square / Aspect-Ratio Tailored Product Thumbnail
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .background(FbPillBg)
            ) {
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(primaryImageUrl)
                        .crossfade(true)
                        .diskCachePolicy(CachePolicy.ENABLED)
                        .memoryCachePolicy(CachePolicy.ENABLED)
                        .build(),
                    contentDescription = item.title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Top-start badge: SOLD or Promoted
                if (!item.isAvailable) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MeskotMaroon.copy(alpha = 0.94f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "SOLD",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                    }
                } else if (item.isPromoted) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = FbBlue.copy(alpha = 0.94f),
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "✨ Golden Meskot",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                        )
                    }
                }

                // Top-end Bookmark / Save Button
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(6.dp)
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.48f))
                        .clickable { onToggleSave() }
                        .testTag("save_listing_${item.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (item.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = if (item.isSaved) "Remove from Saved" else "Save Listing",
                        tint = if (item.isSaved) MeskotGold1 else Color.White,
                        modifier = Modifier.size(17.dp)
                    )
                }

                // Bottom-start photo count pill if multiple images
                if (item.imageUrls.size > 1) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.55f),
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(6.dp)
                    ) {
                        Text(
                            text = "📷 ${item.imageUrls.size}",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                // Price Tag in Meskot Bronze Gold + Condition badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (item.isAvailable) item.formattedPrice else "SOLD · ${item.formattedPrice}",
                        fontSize = 14.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (item.isAvailable) FbBlue else MeskotMaroon,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    if (item.sellerVerified) {
                        VerifiedBadge(size = 13.dp)
                    }
                }

                // Title (1-line truncation with ellipsis)
                Text(
                    text = item.title,
                    fontSize = 13.5.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FbDarkText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                // Secondary Currency Conversion / Location Hint
                Text(
                    text = "📍 ${item.location.name} · ${item.secondaryPriceFormatted}",
                    fontSize = 11.5.sp,
                    color = FbSecondaryText,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Sort & Filter Bottom Sheet (Sort Order, Condition, and USD Price Range)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarketplaceFilterSortBottomSheet(
    selectedSort: MarketplaceSortOption,
    selectedCondition: String,
    minPriceUsd: String,
    maxPriceUsd: String,
    onApply: (MarketplaceSortOption, String, String, String) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var tempSort by remember { mutableStateOf(selectedSort) }
    var tempCondition by remember { mutableStateOf(selectedCondition) }
    var tempMin by remember { mutableStateOf(minPriceUsd) }
    var tempMax by remember { mutableStateOf(maxPriceUsd) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = MeskotCardPaper,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 18.dp, vertical = 8.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Sort & Filter Marketplace",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = FbDarkText
                )
                TextButton(onClick = onReset) {
                    Text("Reset", color = MeskotMaroon, fontWeight = FontWeight.Bold)
                }
            }

            Text(
                text = "Sort By",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = FbBlue
            )

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                MarketplaceSortOption.values().forEach { opt ->
                    val isSelected = tempSort == opt
                    Surface(
                        onClick = { tempSort = opt },
                        shape = RoundedCornerShape(14.dp),
                        color = if (isSelected) FbPillBg else FbSurfaceBg,
                        border = BorderStroke(
                            width = if (isSelected) 1.5.dp else 1.dp,
                            color = if (isSelected) MeskotGold2 else FbLightDivider
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = opt.label,
                                fontSize = 14.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = FbDarkText
                            )
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = FbBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }

            Text(
                text = "Condition",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = FbBlue
            )

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(listOf("All", "New", "Used - Like New", "Used - Good", "Used - Fair")) { cond ->
                    FilterChip(
                        selected = tempCondition == cond,
                        onClick = { tempCondition = cond },
                        label = { Text(cond) }
                    )
                }
            }

            Text(
                text = "Price Range (USD Equivalent)",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = FbBlue
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = tempMin,
                    onValueChange = { tempMin = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Min ($)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = tempMax,
                    onValueChange = { tempMax = it.filter { ch -> ch.isDigit() || ch == '.' } },
                    label = { Text("Max ($)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = { onApply(tempSort, tempCondition, tempMin, tempMax) },
                colors = ButtonDefaults.buttonColors(containerColor = FbBlue),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                Text("Apply Filters", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
        }
    }
}

/**
 * Categories Filter Bottom Sheet matching the HTML `.sheet` & `.g2 > .cd` design
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarketplaceCategoriesBottomSheet(
    categories: List<Category>,
    selectedCategoryId: String,
    onSelectCategory: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val bg = Color(0xFFFBF8EF)
    val paper = Color(0xFFFFFDF8)
    val ink = Color(0xFF1D2419)
    val line = Color(0xFFEADFC2)
    val g2 = Color(0xFFA87B25)
    val bz = Color(0xFF96691F)
    val tint = Color(0xFFF6EDD2)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = paper,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(top = 10.dp)
                    .width(44.dp)
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(line)
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
                .padding(bottom = 18.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Category",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = ink
                )
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(tint)
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "×",
                        fontSize = 20.sp,
                        color = bz
                    )
                }
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(categories, key = { it.id }) { category ->
                    val isSelected = category.id == selectedCategoryId
                    Surface(
                        onClick = { onSelectCategory(category.id) },
                        shape = RoundedCornerShape(20.dp),
                        color = if (isSelected) tint else bg,
                        border = BorderStroke(
                            width = if (isSelected) 2.dp else 1.5.dp,
                            color = if (isSelected) g2 else line
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 96.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = category.icon,
                                fontSize = 26.sp
                            )
                            Text(
                                text = category.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = ink,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Location Picker Bottom Sheet matching the HTML `openLoc()` (`Change location`, `.srch`, `#ll`)
 */
@Composable
private fun MarketplaceLocationDialog(
    currentLocation: MarketplaceLocation,
    currentRadiusKm: Double,
    onApply: (MarketplaceLocation, Double) -> Unit,
    onDismiss: () -> Unit
) {
    RealLocationPickerDialog(
        title = "Change location",
        initialPlaceName = currentLocation.name.ifBlank { "Mekelle" },
        initialLatitude = currentLocation.latitude,
        initialLongitude = currentLocation.longitude,
        showRadiusSlider = false,
        initialRadiusKm = currentRadiusKm,
        onDismiss = onDismiss,
        onLocationSelected = { place, radius ->
            onApply(
                MarketplaceLocation(
                    latitude = place.latitude,
                    longitude = place.longitude,
                    name = place.name,
                    radiusKm = radius.toInt()
                ),
                radius
            )
        }
    )
}

/**
 * "Sell" Flow — Create Listing Modal
 * Form fields: Title, Category selection, Price, Currency (USD primary, ETB secondary),
 * Description, Location coordinates, and Multiple Image uploads.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreateListingModal(
    categories: List<Category>,
    defaultLocation: MarketplaceLocation,
    onDismiss: () -> Unit,
    onSubmit: (
        title: String,
        price: Double,
        currency: String,
        categoryId: String,
        description: String,
        imageUrls: List<String>,
        location: MarketplaceLocation,
        condition: String
    ) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var priceInput by remember { mutableStateOf("") }
    var selectedCurrency by remember { mutableStateOf("USD") } // USD primary, ETB secondary
    var selectedCategoryId by remember { mutableStateOf(categories.firstOrNull()?.id ?: "vehicles") }
    var condition by remember { mutableStateOf("Used - Like New") }
    var description by remember { mutableStateOf("") }
    var locationName by remember { mutableStateOf(defaultLocation.name) }
    var latitudeInput by remember { mutableStateOf(defaultLocation.latitude.toString()) }
    var longitudeInput by remember { mutableStateOf(defaultLocation.longitude.toString()) }
    var imageUrlInput by remember { mutableStateOf("") }

    val selectedPhotos = remember {
        mutableStateListOf(
            "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&auto=format&fit=crop&q=80"
        )
    }

    val sampleGalleryPhotos = remember {
        listOf(
            "https://images.unsplash.com/photo-1552519507-da3b142c6e3d?w=800&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?w=800&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1560448204-e02f11c3d0e2?w=800&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1555041469-a586c61ea9bc?w=800&auto=format&fit=crop&q=80",
            "https://images.unsplash.com/photo-1593359677879-a4bb92f829d1?w=800&auto=format&fit=crop&q=80"
        )
    }

    // Zero-permission Android Photo Picker for multiple images
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 6)
    ) { uris ->
        uris.forEach { uri ->
            val uriStr = uri.toString()
            if (!selectedPhotos.contains(uriStr)) {
                selectedPhotos.add(0, uriStr)
            }
        }
    }

    val nbeRate = ExchangeRateManager.usdToEtbRate
    val numericPrice = priceInput.toDoubleOrNull() ?: 0.0
    val convertedPreview = if (selectedCurrency == "USD") {
        "≈ ETB ${String.format(Locale.US, "%,.0f", numericPrice * nbeRate)} (NBE rate: ${String.format(Locale.US, "%.2f", nbeRate)})"
    } else {
        "≈ $${String.format(Locale.US, "%,.2f", numericPrice / nbeRate.coerceAtLeast(1.0))} USD"
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = FbSurfaceBg,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            "New Marketplace Listing",
                            fontWeight = FontWeight.Bold,
                            fontSize = 19.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel")
                        }
                    },
                    actions = {
                        Button(
                            onClick = {
                                if (title.isNotBlank() && numericPrice > 0) {
                                    onSubmit(
                                        title.trim(),
                                        numericPrice,
                                        selectedCurrency,
                                        selectedCategoryId,
                                        description.trim().ifBlank { "Available for pickup in $locationName." },
                                        selectedPhotos.toList(),
                                        MarketplaceLocation(
                                            latitude = latitudeInput.toDoubleOrNull() ?: defaultLocation.latitude,
                                            longitude = longitudeInput.toDoubleOrNull() ?: defaultLocation.longitude,
                                            name = locationName.trim().ifBlank { "Ariena" }
                                        ),
                                        condition
                                    )
                                }
                            },
                            enabled = title.isNotBlank() && numericPrice > 0,
                            colors = ButtonDefaults.buttonColors(containerColor = FbBlue),
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .testTag("publish_listing_button")
                        ) {
                            Text("Publish", fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = FbSurfaceBg)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Multiple Photos Section
                Text(
                    text = "Photos (${selectedPhotos.size}/6)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = FbDarkText
                )

                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    item {
                        OutlinedCard(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.5.dp, FbBlue),
                            modifier = Modifier.size(96.dp)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.Center,
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = Icons.Default.AddAPhoto,
                                    contentDescription = "Add Photos",
                                    tint = FbBlue
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Add Photo",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FbBlue
                                )
                            }
                        }
                    }

                    items(selectedPhotos) { photoUrl ->
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(RoundedCornerShape(10.dp))
                        ) {
                            AsyncImage(
                                model = photoUrl,
                                contentDescription = "Selected listing photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                            if (selectedPhotos.size > 1) {
                                IconButton(
                                    onClick = { selectedPhotos.remove(photoUrl) },
                                    modifier = Modifier
                                        .align(Alignment.TopEnd)
                                        .size(26.dp)
                                        .padding(2.dp)
                                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove photo",
                                        tint = Color.White,
                                        modifier = Modifier.size(14.dp)
                                    )
                                }
                            }
                        }
                    }
                }

                // Quick Preset Sample Photos + Image URL input
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = imageUrlInput,
                        onValueChange = { imageUrlInput = it },
                        label = { Text("Or paste Image URL") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    FilledTonalButton(
                        onClick = {
                            if (imageUrlInput.isNotBlank()) {
                                selectedPhotos.add(0, imageUrlInput.trim())
                                imageUrlInput = ""
                            } else {
                                val nextSample = sampleGalleryPhotos.firstOrNull { !selectedPhotos.contains(it) }
                                    ?: sampleGalleryPhotos.random()
                                selectedPhotos.add(0, nextSample)
                            }
                        }
                    ) {
                        Text(if (imageUrlInput.isNotBlank()) "Add URL" else "+ Sample")
                    }
                }

                // Title Field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title (e.g., 2022 Toyota Hilux, iPhone 15 Pro)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sell_title_input")
                )

                // Price & Currency (USD Primary, ETB Secondary)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = priceInput,
                        onValueChange = { priceInput = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Price ($selectedCurrency)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("sell_price_input")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("USD", "ETB").forEach { curr ->
                            FilterChip(
                                selected = selectedCurrency == curr,
                                onClick = { selectedCurrency = curr },
                                label = {
                                    Text(
                                        text = if (curr == "USD") "USD ($)" else "ETB (Birr)",
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            )
                        }
                    }
                }

                if (numericPrice > 0) {
                    Text(
                        text = convertedPreview,
                        fontSize = 12.sp,
                        color = FbBlue,
                        fontWeight = FontWeight.Medium
                    )
                }

                // Category Selector
                Text(
                    text = "Category",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FbDarkText
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        FilterChip(
                            selected = selectedCategoryId == cat.id,
                            onClick = { selectedCategoryId = cat.id },
                            label = { Text("${cat.icon} ${cat.name}") }
                        )
                    }
                }

                // Condition Selector
                Text(
                    text = "Condition",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = FbDarkText
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(listOf("New", "Used - Like New", "Used - Good", "Used - Fair")) { cond ->
                        FilterChip(
                            selected = condition == cond,
                            onClick = { condition = cond },
                            label = { Text(cond) }
                        )
                    }
                }

                // Location Selector matching Sell on Meskot (`📍 ${S.loc}` + Change location sheet)
                var showSellMapPicker by remember { mutableStateOf(false) }

                com.example.ui.components.MeskotLocationSelectorButton(
                    locationName = locationName,
                    placeholder = "Mekelle",
                    label = "Location",
                    onClick = { showSellMapPicker = true }
                )

                if (showSellMapPicker) {
                    RealLocationPickerDialog(
                        title = "Change location",
                        initialPlaceName = locationName,
                        initialLatitude = latitudeInput.toDoubleOrNull() ?: defaultLocation.latitude,
                        initialLongitude = longitudeInput.toDoubleOrNull() ?: defaultLocation.longitude,
                        showRadiusSlider = false,
                        onDismiss = { showSellMapPicker = false },
                        onLocationSelected = { place, _ ->
                            locationName = place.name
                            latitudeInput = String.format(Locale.US, "%.4f", place.latitude)
                            longitudeInput = String.format(Locale.US, "%.4f", place.longitude)
                            showSellMapPicker = false
                        }
                    )
                }

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sell_description_input")
                )
            }
        }
    }
}

/**
 * Product Details Modal with Full Buyer & Seller Commerce Actions:
 * - Multi-photo gallery viewer
 * - Save/Bookmark, Make Offer Dialog, Chapa Escrow Buy Now, Call Seller, Share Listing
 * - Quick canned replies + custom inquiry chat trigger
 * - Seller Management Toolbar (Mark as Sold/Available, Edit Listing, Boost Listing, Delete Listing)
 * - Similar Items recommendation carousel
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarketplaceProductDetailModal(
    item: ListingItem,
    currentUser: User?,
    similarItems: List<ListingItem>,
    onDismiss: () -> Unit,
    onToggleSave: () -> Unit,
    onSendQuickMessage: (String) -> Unit,
    onSendOffer: (Double, String) -> Unit,
    onBuyWithChapa: () -> Unit,
    onMessageSellerInMessenger: (String?) -> Unit,
    onToggleSoldStatus: () -> Unit,
    onUpdateListing: (String, Double, String, String, String, Boolean) -> Unit,
    onBoostListing: () -> Unit,
    onDeleteListing: () -> Unit,
    onSelectSimilarItem: (ListingItem) -> Unit
) {
    val context = LocalContext.current
    var quickMessage by remember(item.id) { mutableStateOf("Hi ${item.sellerName}, is this still available?") }
    var selectedImageIndex by remember(item.id) { mutableIntStateOf(0) }
    var showMakeOfferDialog by remember { mutableStateOf(false) }
    var showEditListingDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }

    val isOwner = item.sellerId == currentUser?.uid || item.sellerId == "usr_current"

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = FbSurfaceBg,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = item.title,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = onToggleSave) {
                            Icon(
                                imageVector = if (item.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = if (item.isSaved) "Remove from Saved" else "Save Listing",
                                tint = if (item.isSaved) FbBlue else FbDarkText
                            )
                        }
                        IconButton(
                            onClick = {
                                val sendIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(
                                        android.content.Intent.EXTRA_TEXT,
                                        "Check out \"${item.title}\" (${item.formattedPrice}) on Meskot Marketplace in ${item.location.name}!"
                                    )
                                }
                                context.startActivity(android.content.Intent.createChooser(sendIntent, "Share Listing"))
                            }
                        ) {
                            Icon(Icons.Default.Share, contentDescription = "Share Listing", tint = FbDarkText)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = FbSurfaceBg)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
            ) {
                // Hero Image Viewer
                val images = item.imageUrls.ifEmpty {
                    listOf(item.primaryImageUrl)
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(310.dp)
                        .background(Color(0xFF111111))
                ) {
                    AsyncImage(
                        model = images.getOrElse(selectedImageIndex) { images.first() },
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    if (!item.isAvailable) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MeskotMaroon,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "SOLD",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    } else if (item.isPromoted) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = FbBlue,
                            modifier = Modifier
                                .align(Alignment.TopStart)
                                .padding(12.dp)
                        ) {
                            Text(
                                text = "✨ Golden Meskot Featured",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    if (images.size > 1) {
                        LazyRow(
                            modifier = Modifier
                                .align(Alignment.BottomCenter)
                                .padding(10.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(images.size) { idx ->
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .border(
                                            width = if (idx == selectedImageIndex) 2.dp else 1.dp,
                                            color = if (idx == selectedImageIndex) FbBlue else Color.White,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .clickable { selectedImageIndex = idx }
                                ) {
                                    AsyncImage(
                                        model = images[idx],
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Title & Price
                    Text(
                        text = item.title,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = FbDarkText
                    )

                    Row(
                        verticalAlignment = Alignment.Bottom,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (item.isAvailable) item.formattedPrice else "SOLD · ${item.formattedPrice}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = if (item.isAvailable) FbBlue else MeskotMaroon
                        )
                        Text(
                            text = "(${item.secondaryPriceFormatted})",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = FbSecondaryText
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = FbBlue,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Listed in ${item.location.name} · ${item.distanceKm.toInt()} km away · ${item.condition} · ${item.viewsCount} views",
                            fontSize = 13.sp,
                            color = FbSecondaryText
                        )
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = Color(0xFFF6EDD2)
                        ) {
                            Text(
                                text = "📍 ${item.deliveryOption}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color(0xFF96691F),
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (item.isNegotiable) FbPillBg else Color(0xFFF3EFE4),
                            border = BorderStroke(1.dp, FbLightDivider)
                        ) {
                            Text(
                                text = if (item.isNegotiable) "🤝 Negotiable" else "Fixed Price",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (item.isNegotiable) FbBlue else FbSecondaryText,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // Primary Buyer Action Bar (Facebook Marketplace Action Buttons)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (item.isAvailable) {
                            Button(
                                onClick = onBuyWithChapa,
                                colors = ButtonDefaults.buttonColors(containerColor = FbBlue),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .weight(1.25f)
                                    .height(44.dp)
                                    .testTag("marketplace_buy_chapa_button")
                            ) {
                                Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Buy via Chapa", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            OutlinedButton(
                                onClick = { showMakeOfferDialog = true },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.4.dp, MeskotGold2),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(44.dp)
                                    .testTag("marketplace_make_offer_button")
                            ) {
                                Icon(Icons.Default.LocalOffer, contentDescription = null, tint = FbBlue, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Make Offer", color = FbDarkText, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        OutlinedButton(
                            onClick = onToggleSave,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.2.dp, FbLightDivider),
                            modifier = Modifier.height(44.dp)
                        ) {
                            Icon(
                                imageVector = if (item.isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                contentDescription = "Save",
                                tint = FbBlue,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        if (item.allowCall && item.sellerPhone.isNotBlank()) {
                            OutlinedButton(
                                onClick = {
                                    val dialIntent = android.content.Intent(
                                        android.content.Intent.ACTION_DIAL,
                                        android.net.Uri.parse("tel:${item.sellerPhone}")
                                    )
                                    context.startActivity(dialIntent)
                                },
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.2.dp, FbLightDivider),
                                modifier = Modifier.height(44.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call Seller",
                                    tint = FbBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    // Seller Management Card (Visible if current user is the seller, or via Seller Tools)
                    if (isOwner) {
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = FbPillBg),
                            border = BorderStroke(1.2.dp, MeskotGold2),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = "Seller Listing Controls",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FbDarkText
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = onToggleSoldStatus,
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = if (item.isAvailable) MeskotMaroon else FbBlue
                                        ),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Text(
                                            text = if (item.isAvailable) "Mark as Sold" else "Mark Available",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { showEditListingDialog = true },
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Edit", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Button(
                                        onClick = onBoostListing,
                                        colors = ButtonDefaults.buttonColors(containerColor = MeskotGold2),
                                        shape = RoundedCornerShape(12.dp),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(Icons.Default.RocketLaunch, contentDescription = null, modifier = Modifier.size(15.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (item.isPromoted) "Boosted ✨" else "Boost Listing",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    OutlinedButton(
                                        onClick = { showDeleteConfirmDialog = true },
                                        shape = RoundedCornerShape(12.dp),
                                        border = BorderStroke(1.dp, MeskotMaroon),
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        Icon(
                                            Icons.Default.DeleteOutline,
                                            contentDescription = null,
                                            tint = MeskotMaroon,
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Delete", color = MeskotMaroon, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // Direct Real-Time Chat Trigger Card (Send Seller a Message)
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MeskotCardPaper),
                        border = BorderStroke(1.dp, FbLightDivider),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.ChatBubbleOutline,
                                    contentDescription = null,
                                    tint = FbBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Send seller a message",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FbDarkText
                                )
                            }

                            // Quick canned message pills (Facebook Marketplace style)
                            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                val quickTemplates = listOf(
                                    "Is this still available?",
                                    "What is your lowest price?",
                                    "Can we meet today in ${item.location.name}?",
                                    "Do you accept Chapa or Telebirr?"
                                )
                                items(quickTemplates) { tpl ->
                                    Surface(
                                        onClick = { quickMessage = tpl },
                                        shape = RoundedCornerShape(14.dp),
                                        color = FbPillBg,
                                        border = BorderStroke(1.dp, FbLightDivider)
                                    ) {
                                        Text(
                                            text = tpl,
                                            fontSize = 11.5.sp,
                                            fontWeight = FontWeight.Medium,
                                            color = FbDarkText,
                                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                        )
                                    }
                                }
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedTextField(
                                    value = quickMessage,
                                    onValueChange = { quickMessage = it },
                                    singleLine = true,
                                    shape = RoundedCornerShape(20.dp),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White,
                                        unfocusedContainerColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("seller_quick_message_input")
                                )

                                Button(
                                    onClick = {
                                        if (quickMessage.isNotBlank()) {
                                            onSendQuickMessage(quickMessage.trim())
                                        }
                                    },
                                    shape = RoundedCornerShape(20.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = FbBlue),
                                    modifier = Modifier.testTag("send_seller_message_button")
                                ) {
                                    Text("Send", fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    HorizontalDivider(color = FbLightDivider.copy(alpha = 0.6f))

                    // Seller Information Section
                    Text(
                        text = "Seller Information",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = FbDarkText
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            UserAvatar(
                                photoUrl = item.sellerPhoto,
                                name = item.sellerName,
                                size = 46
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = item.sellerName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FbDarkText
                                    )
                                    if (item.sellerVerified) {
                                        Spacer(modifier = Modifier.width(4.dp))
                                        VerifiedBadge(size = 15.dp)
                                    }
                                }
                                Text(
                                    text = "Joined Meskot Marketplace · Active Seller",
                                    fontSize = 12.sp,
                                    color = FbSecondaryText
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = { onMessageSellerInMessenger(quickMessage) },
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.Send,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Direct Chat")
                        }
                    }

                    HorizontalDivider(color = FbLightDivider.copy(alpha = 0.6f))

                    // Description
                    Text(
                        text = "Description",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = FbDarkText
                    )
                    Text(
                        text = item.description,
                        fontSize = 14.sp,
                        color = FbDarkText,
                        lineHeight = 20.sp
                    )

                    HorizontalDivider(color = FbLightDivider.copy(alpha = 0.6f))

                    // Seller Location on Real Google Maps (Facebook Marketplace Style)
                    Text(
                        text = "Seller Location",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = FbDarkText
                    )
                    InteractiveGoogleMapCard(
                        latitude = item.location.latitude,
                        longitude = item.location.longitude,
                        placeName = item.location.name,
                        height = 180.dp,
                        showOpenButton = true
                    )

                    // Similar Items Carousel (Facebook Marketplace "Similar listings")
                    if (similarItems.isNotEmpty()) {
                        HorizontalDivider(color = FbLightDivider.copy(alpha = 0.6f))
                        Text(
                            text = "Similar Items Nearby",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = FbDarkText
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            items(similarItems, key = { it.id }) { sim ->
                                Card(
                                    onClick = { onSelectSimilarItem(sim) },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = CardDefaults.cardColors(containerColor = MeskotCardPaper),
                                    border = BorderStroke(1.dp, FbLightDivider),
                                    modifier = Modifier.width(148.dp)
                                ) {
                                    Column {
                                        AsyncImage(
                                            model = sim.primaryImageUrl,
                                            contentDescription = sim.title,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(110.dp)
                                        )
                                        Column(modifier = Modifier.padding(8.dp)) {
                                            Text(
                                                text = sim.formattedPrice,
                                                fontSize = 13.5.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = FbBlue,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = sim.title,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Medium,
                                                color = FbDarkText,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = "📍 ${sim.location.name}",
                                                fontSize = 11.sp,
                                                color = FbSecondaryText,
                                                maxLines = 1
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
    }

    // Make an Offer Dialog
    if (showMakeOfferDialog) {
        val suggested90 = (item.price * 0.90).roundToInt().toDouble()
        val suggested85 = (item.price * 0.85).roundToInt().toDouble()
        val suggested80 = (item.price * 0.80).roundToInt().toDouble()
        var offerText by remember { mutableStateOf(suggested90.toLong().toString()) }

        AlertDialog(
            onDismissRequest = { showMakeOfferDialog = false },
            containerColor = MeskotCardPaper,
            title = {
                Text(
                    text = "Make an Offer to ${item.sellerName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text(
                        text = "Listed price: ${item.formattedPrice} (${item.secondaryPriceFormatted})",
                        fontSize = 13.sp,
                        color = FbSecondaryText
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(
                            "-10%" to suggested90,
                            "-15%" to suggested85,
                            "-20%" to suggested80
                        ).forEach { (pct, amt) ->
                            FilterChip(
                                selected = offerText == amt.toLong().toString(),
                                onClick = { offerText = amt.toLong().toString() },
                                label = { Text("$pct (${item.currency} ${amt.toLong()})") }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = offerText,
                        onValueChange = { offerText = it.filter { ch -> ch.isDigit() || ch == '.' } },
                        label = { Text("Your Offer (${item.currency})") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amt = offerText.toDoubleOrNull() ?: 0.0
                        if (amt > 0) {
                            showMakeOfferDialog = false
                            onSendOffer(amt, item.currency)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FbBlue)
                ) {
                    Text("Send Offer", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMakeOfferDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Edit Listing Dialog
    if (showEditListingDialog) {
        var editTitle by remember { mutableStateOf(item.title) }
        var editPrice by remember { mutableStateOf(item.price.toString()) }
        var editCurrency by remember { mutableStateOf(item.currency) }
        var editCondition by remember { mutableStateOf(item.condition) }
        var editDesc by remember { mutableStateOf(item.description) }
        var editNeg by remember { mutableStateOf(item.isNegotiable) }

        AlertDialog(
            onDismissRequest = { showEditListingDialog = false },
            containerColor = MeskotCardPaper,
            title = { Text("Edit Listing", fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = editTitle,
                        onValueChange = { editTitle = it },
                        label = { Text("Title") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editPrice,
                            onValueChange = { editPrice = it.filter { ch -> ch.isDigit() || ch == '.' } },
                            label = { Text("Price") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = editCurrency == "USD",
                            onClick = { editCurrency = if (editCurrency == "USD") "ETB" else "USD" },
                            label = { Text(editCurrency) }
                        )
                    }
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(listOf("New", "Used - Like New", "Used - Good", "Used - Fair")) { cond ->
                            FilterChip(
                                selected = editCondition == cond,
                                onClick = { editCondition = cond },
                                label = { Text(cond, fontSize = 11.sp) }
                            )
                        }
                    }
                    OutlinedTextField(
                        value = editDesc,
                        onValueChange = { editDesc = it },
                        label = { Text("Description") },
                        minLines = 3,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val p = editPrice.toDoubleOrNull() ?: item.price
                        onUpdateListing(editTitle, p, editCurrency, editCondition, editDesc, editNeg)
                        showEditListingDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FbBlue)
                ) {
                    Text("Save Changes", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditListingDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Delete Listing Confirmation Dialog
    if (showDeleteConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            containerColor = MeskotCardPaper,
            title = { Text("Delete Listing?", fontWeight = FontWeight.Bold) },
            text = {
                Text("Are you sure you want to permanently remove \"${item.title}\" from Meskot Marketplace?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showDeleteConfirmDialog = false
                        onDeleteListing()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MeskotMaroon)
                ) {
                    Text("Delete", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

/**
 * Buyer-Seller Inbox Modal showing all Marketplace conversation threads
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarketplaceInboxModal(
    chatsMap: Map<String, List<MarketplaceChatMessage>>,
    listings: List<ListingItem>,
    onOpenThread: (String) -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = FbSurfaceBg,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "Marketplace Inbox",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = FbSurfaceBg)
                )
            }
        ) { paddingValues ->
            val threadList = remember(chatsMap) {
                chatsMap.entries
                    .mapNotNull { entry ->
                        val lastMsg = entry.value.maxByOrNull { it.timestamp }
                        if (lastMsg != null) Pair(entry.key, lastMsg) else null
                    }
                    .sortedByDescending { it.second.timestamp }
            }

            if (threadList.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Outlined.Forum,
                            contentDescription = null,
                            tint = FbSecondaryText,
                            modifier = Modifier.size(54.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No Marketplace messages yet",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = FbDarkText
                        )
                        Text(
                            text = "Tap any item in Today's picks to message the seller.",
                            fontSize = 13.sp,
                            color = FbSecondaryText
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentPadding = PaddingValues(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(threadList, key = { it.first }) { (chatId, lastMsg) ->
                        val item = listings.find { it.id == lastMsg.itemId }
                        val thumbUrl = lastMsg.itemThumbUrl.ifBlank { item?.primaryImageUrl.orEmpty() }
                        Card(
                            onClick = { onOpenThread(chatId) },
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = MeskotCardPaper),
                            border = BorderStroke(1.dp, FbLightDivider),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AsyncImage(
                                    model = thumbUrl,
                                    contentDescription = lastMsg.itemTitle,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(56.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(FbPillBg)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = lastMsg.itemTitle.ifBlank { item?.title ?: "Marketplace Item" },
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FbDarkText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${lastMsg.senderName}: ${lastMsg.messageText}",
                                        fontSize = 13.sp,
                                        color = FbSecondaryText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ChevronRight,
                                    contentDescription = "Open chat",
                                    tint = FbSecondaryText
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Real-Time Buyer-Seller Conversation Modal for a specific Marketplace Listing
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarketplaceConversationDialog(
    currentUser: User?,
    item: ListingItem,
    messages: List<MarketplaceChatMessage>,
    onSendMessage: (String) -> Unit,
    onOpenFullMessenger: () -> Unit,
    onDismiss: () -> Unit
) {
    var draft by remember { mutableStateOf("") }
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.US) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = FbSurfaceBg,
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = item.primaryImageUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = item.title,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${item.formattedPrice} · ${item.sellerName}",
                                    fontSize = 12.sp,
                                    color = FbSecondaryText
                                )
                            }
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        TextButton(onClick = onOpenFullMessenger) {
                            Text("Messenger", color = FbBlue, fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = FbSurfaceBg)
                )
            },
            bottomBar = {
                Surface(
                    color = FbSurfaceBg,
                    tonalElevation = 3.dp
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Quick negotiation chips inside chat
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val chatQuickReplies = listOf(
                                "I can pick it up today",
                                "Would you accept ${(item.price * 0.9).toInt()} ${item.currency}?",
                                "Please share exact location pin",
                                "I'll pay via Chapa Escrow"
                            )
                            items(chatQuickReplies) { qr ->
                                Surface(
                                    onClick = { onSendMessage(qr) },
                                    shape = RoundedCornerShape(14.dp),
                                    color = FbPillBg,
                                    border = BorderStroke(1.dp, FbLightDivider)
                                ) {
                                    Text(
                                        text = qr,
                                        fontSize = 11.sp,
                                        color = FbDarkText,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = draft,
                                onValueChange = { draft = it },
                                placeholder = { Text("Write a message to ${item.sellerName}...") },
                                singleLine = true,
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = {
                                    if (draft.isNotBlank()) {
                                        onSendMessage(draft.trim())
                                        draft = ""
                                    }
                                },
                                modifier = Modifier
                                    .size(46.dp)
                                    .background(FbBlue, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.Send,
                                    contentDescription = "Send",
                                    tint = Color.White
                                )
                            }
                        }
                    }
                }
            }
        ) { paddingValues ->
            val sortedMessages = remember(messages) { messages.sortedBy { it.timestamp } }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 14.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(sortedMessages, key = { "${it.chatId}_${it.timestamp}" }) { msg ->
                    val isMe = msg.senderId == currentUser?.uid || msg.senderId == "usr_current"
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
                    ) {
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = if (isMe) FbBlue else FbPillBg
                        ) {
                            Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)) {
                                if (!isMe) {
                                    Text(
                                        text = msg.senderName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = FbSecondaryText
                                    )
                                }
                                Text(
                                    text = msg.messageText,
                                    fontSize = 14.sp,
                                    color = if (isMe) Color.White else FbDarkText
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = timeFormatter.format(Date(msg.timestamp)),
                            fontSize = 10.sp,
                            color = FbSecondaryText
                        )
                    }
                }
            }
        }
    }
}

/**
 * Seller Commerce Profile Bottom Sheet (triggered by clicking the "You" Profile Chip)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun MarketplaceAccountSheet(
    currentUser: User?,
    myListings: List<ListingItem>,
    savedListings: List<ListingItem>,
    inboxCount: Int,
    onOpenCreateListing: () -> Unit,
    onOpenInbox: () -> Unit,
    onSelectListing: (ListingItem) -> Unit,
    onToggleSold: (ListingItem) -> Unit,
    onBoostListing: (ListingItem) -> Unit,
    onDeleteListing: (ListingItem) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = Your Listings, 1 = Saved Items

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = FbSurfaceBg
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(
                    photoUrl = currentUser?.photoUrl.orEmpty(),
                    name = currentUser?.displayName ?: "Meskot User",
                    size = 52
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = currentUser?.displayName ?: "Commerce Profile",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = FbDarkText
                    )
                    val totalViews = myListings.sumOf { it.viewsCount }
                    Text(
                        text = "${myListings.size} Listings · ${savedListings.size} Saved · $totalViews Views",
                        fontSize = 12.5.sp,
                        color = FbSecondaryText
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onOpenCreateListing,
                    colors = ButtonDefaults.buttonColors(containerColor = FbBlue),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create Listing")
                }
                OutlinedButton(
                    onClick = onOpenInbox,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Outlined.Forum, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Inbox ($inboxCount)")
                }
            }

            // Tab Switcher: Your Listings vs Saved Items
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    label = { Text("Your Listings (${myListings.size})", fontWeight = FontWeight.Bold) }
                )
                FilterChip(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    label = { Text("Saved Items (${savedListings.size})", fontWeight = FontWeight.Bold) }
                )
            }

            val listToShow = if (selectedTab == 0) myListings else savedListings

            if (listToShow.isEmpty()) {
                Text(
                    text = if (selectedTab == 0) "You haven't posted any listings yet. Tap 'Create Listing' to start selling!"
                    else "You haven't saved any items yet.",
                    fontSize = 13.sp,
                    color = FbSecondaryText,
                    modifier = Modifier.padding(vertical = 12.dp)
                )
            } else {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(listToShow, key = { it.id }) { item ->
                        Card(
                            onClick = { onSelectListing(item) },
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MeskotCardPaper),
                            border = BorderStroke(1.dp, FbLightDivider),
                            modifier = Modifier.width(165.dp)
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(105.dp)
                                ) {
                                    AsyncImage(
                                        model = item.primaryImageUrl,
                                        contentDescription = item.title,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    if (!item.isAvailable) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = MeskotMaroon,
                                            modifier = Modifier
                                                .align(Alignment.TopStart)
                                                .padding(6.dp)
                                        ) {
                                            Text(
                                                text = "SOLD",
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                                Column(
                                    modifier = Modifier.padding(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Text(
                                        text = item.formattedPrice,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.5.sp,
                                        color = FbBlue,
                                        maxLines = 1
                                    )
                                    Text(
                                        text = item.title,
                                        fontSize = 12.sp,
                                        color = FbDarkText,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    if (selectedTab == 0) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            TextButton(
                                                onClick = { onToggleSold(item) },
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                                modifier = Modifier.height(26.dp)
                                            ) {
                                                Text(
                                                    text = if (item.isAvailable) "Mark Sold" else "Relist",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = FbBlue
                                                )
                                            }
                                            TextButton(
                                                onClick = { onBoostListing(item) },
                                                contentPadding = PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                                                modifier = Modifier.height(26.dp)
                                            ) {
                                                Text(
                                                    text = "Boost",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MeskotGold2
                                                )
                                            }
                                            IconButton(
                                                onClick = { onDeleteListing(item) },
                                                modifier = Modifier.size(24.dp)
                                            ) {
                                                Icon(
                                                    Icons.Default.DeleteOutline,
                                                    contentDescription = "Delete",
                                                    tint = MeskotMaroon,
                                                    modifier = Modifier.size(15.dp)
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
        }
    }
}

private fun calculateHaversineDistanceKm(
    lat1: Double,
    lon1: Double,
    lat2: Double,
    lon2: Double
): Double {
    val earthRadiusKm = 6371.0
    val dLat = Math.toRadians(lat2 - lat1)
    val dLon = Math.toRadians(lon2 - lon1)
    val a = sin(dLat / 2).pow(2.0) +
            cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
            sin(dLon / 2).pow(2.0)
    val c = 2 * atan2(sqrt(a), sqrt(1 - a))
    return earthRadiusKm * c
}
