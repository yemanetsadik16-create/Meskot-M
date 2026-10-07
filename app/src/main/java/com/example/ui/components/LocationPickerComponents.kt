package com.example.ui.components

import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.util.LocationHelper
import com.example.util.RealPlaceResult
import kotlinx.coroutines.delay
import java.net.URLEncoder
import java.util.Locale

private val FbBluePin = Color(0xFF1877F2)
private val FbRedPin = Color(0xFFE41E3F)
private val FbDarkInk = Color(0xFF050505)
private val FbSubtleText = Color(0xFF65676B)
private val FbLightSurface = Color(0xFFF0F2F5)

/**
 * Interactive embedded Google Maps card with live pin, coordinates, and a direct "Open in Google Maps" button.
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun InteractiveGoogleMapCard(
    latitude: Double,
    longitude: Double,
    placeName: String,
    modifier: Modifier = Modifier,
    height: Dp = 190.dp,
    zoom: Int = 14,
    showOpenButton: Boolean = true
) {
    val context = LocalContext.current
    val mapUrl = remember(latitude, longitude, placeName, zoom) {
        val q = if (latitude != 0.0 || longitude != 0.0) {
            "$latitude,$longitude"
        } else {
            URLEncoder.encode(placeName.ifBlank { "Mekelle" }, "UTF-8")
        }
        "https://maps.google.com/maps?q=$q&z=$zoom&output=embed"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FbLightSurface),
        border = BorderStroke(1.dp, Color(0xFFEADFC2))
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(height)
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.cacheMode = WebSettings.LOAD_DEFAULT
                            webViewClient = WebViewClient()
                            loadUrl(mapUrl)
                        }
                    },
                    update = { webView ->
                        if (webView.url != mapUrl) {
                            webView.loadUrl(mapUrl)
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Floating Coordinates & Pin Badge
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFFFDF8).copy(alpha = 0.96f),
                    border = BorderStroke(1.dp, Color(0xFFEADFC2)),
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "📍", fontSize = 13.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = placeName.ifBlank {
                                String.format(Locale.US, "%.4f, %.4f", latitude, longitude)
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF1D2419),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            if (showOpenButton) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFFFFFDF8))
                        .clickable {
                            LocationHelper.openInGoogleMaps(
                                context = context,
                                latitude = latitude,
                                longitude = longitude,
                                placeName = placeName
                            )
                        }
                        .padding(horizontal = 12.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = null,
                            tint = Color(0xFF96691F),
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "📍 " + placeName.ifBlank { "Mekelle" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF1D2419),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = String.format(Locale.US, "GPS: %.4f, %.4f · Google Maps", latitude, longitude),
                                fontSize = 11.sp,
                                color = Color(0xFF7A7360)
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Open Map",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF96691F)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open in Google Maps",
                            tint = Color(0xFF96691F),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Reusable "Sell on Meskot" Location Selector Button matching `<button class="sl" data-act="loc"><span>📍 ${S.loc}</span>${CH}</button>`
 * Used across all parts of Meskot (Marketplace, Post Check-in, Profile Current City/Hometown, Boost Post, Ads Manager).
 */
@Composable
fun MeskotLocationSelectorButton(
    locationName: String,
    placeholder: String = "Mekelle",
    label: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = Color(0xFFFBF8EF)
    val ink = Color(0xFF1D2419)
    val mut = Color(0xFF7A7360)
    val line = Color(0xFFEADFC2)
    val g2 = Color(0xFFA87B25)
    val bz = Color(0xFF96691F)

    Column(modifier = modifier.fillMaxWidth()) {
        if (label != null) {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = bz,
                modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
            )
        }
        Surface(
            onClick = onClick,
            shape = RoundedCornerShape(16.dp),
            color = bg,
            border = BorderStroke(1.5.dp, line),
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 50.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val displayLoc = locationName.trim().ifBlank { placeholder }
                Text(
                    text = "📍 $displayLoc",
                    fontSize = 16.sp,
                    color = if (locationName.isNotBlank()) ink else mut,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Change location",
                    tint = g2,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * Exact "Change location" Bottom Sheet Dialog from "Sell on Meskot" HTML (`openLoc()`, `filt(q)`, `search(q)`, `showLocs(list, note)`).
 *
 * Used across ALL parts of Meskot:
 * - Sell on Meskot (`SellOnMeskotModal`)
 * - Marketplace feed location picker ("Today's picks · 📍 Mekelle")
 * - Marketplace `CreateListingModal`
 * - Post Check-Ins ("is in 📍 Mekelle")
 * - Edit Profile ("Current City / Lives in" & "Hometown / From")
 * - Boost Post & Ads Manager Audience Location Targeting
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealLocationPickerDialog(
    title: String = "Change location",
    initialPlaceName: String = "Mekelle",
    initialLatitude: Double = 13.4967,
    initialLongitude: Double = 39.4753,
    showRadiusSlider: Boolean = false,
    initialRadiusKm: Double = 65.0,
    onDismiss: () -> Unit,
    onLocationSelected: (place: RealPlaceResult, radiusKm: Double) -> Unit
) {
    val context = LocalContext.current

    // Exact light CSS color variables from Sell on Meskot HTML:
    // :root{--bg:#fbf8ef;--paper:#fffdf8;--ink:#1d2419;--mut:#7a7360;--line:#eadfc2;--g1:#efd06c;--g2:#a87b25;--bz:#96691f;--mar:#8a1c2b;--tint:#f6edd2}
    val bg = Color(0xFFFBF8EF)
    val paper = Color(0xFFFFFDF8)
    val ink = Color(0xFF1D2419)
    val mut = Color(0xFF7A7360)
    val line = Color(0xFFEADFC2)
    val g2 = Color(0xFFA87B25)
    val bz = Color(0xFF96691F)
    val tint = Color(0xFFF6EDD2)

    var searchQuery by remember { mutableStateOf("") }
    var displayedLocations by remember { mutableStateOf(LocationHelper.MESKOT_HTML_LOCS) }
    var livePlaceCache by remember { mutableStateOf<Map<String, RealPlaceResult>>(emptyMap()) }
    var fallbackNote by remember { mutableStateOf<String?>(null) }

    // Exact HTML `filt(q)` logic:
    // 1. If empty, show `LOCS`
    // 2. Immediately filter `LOCS` locally: `showLocs(local(q))`
    // 3. After 350ms debounce, query live Nominatim / Geocoder search (`search(q)`), fallback to `local(q)` with note on error
    LaunchedEffect(searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) {
            displayedLocations = LocationHelper.MESKOT_HTML_LOCS
            fallbackNote = null
            return@LaunchedEffect
        }
        val localMatches = LocationHelper.MESKOT_HTML_LOCS.filter {
            it.contains(q, ignoreCase = true)
        }
        displayedLocations = localMatches
        fallbackNote = null

        delay(350)
        try {
            val remoteResults = LocationHelper.searchPlaces(context, q)
            if (remoteResults.isNotEmpty()) {
                val newMap = mutableMapOf<String, RealPlaceResult>()
                val remoteNames = remoteResults.map { place ->
                    val label = place.fullAddress.ifBlank { place.displayLabel }
                    newMap[label] = place
                    newMap[place.name] = place
                    label
                }.distinct()
                livePlaceCache = livePlaceCache + newMap
                displayedLocations = (localMatches + remoteNames).distinct()
                fallbackNote = null
            } else if (localMatches.isEmpty()) {
                displayedLocations = emptyList()
            }
        } catch (_: Exception) {
            displayedLocations = localMatches
            fallbackNote = "Live place search is not available here. Showing saved places."
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        // `.ov.on` overlay backdrop (`background:rgba(29,20,5,.5)`) aligned at bottom (`align-items:flex-end`)
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x801D1405))
                .clickable { onDismiss() }
                .testTag("real_location_picker_dialog"),
            contentAlignment = Alignment.BottomCenter
        ) {
            // `.sheet` container (`border-radius:28px 28px 0 0; max-height:86vh; background:var(--paper)`)
            Surface(
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                color = paper,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 380.dp, max = 640.dp)
                    .clickable(enabled = false) {}
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                ) {
                    // Drag handle (`.sheet:before { width:44px; height:5px; border-radius:3px; background:var(--line); margin:10px auto 0 }`)
                    Box(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .width(44.dp)
                            .height(5.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(line)
                            .align(Alignment.CenterHorizontally)
                    )

                    // Sheet header (`.sh { display:flex; justify-content:space-between; align-items:center; padding:12px 20px 10px }`)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (title.isBlank() || title.contains("Choose", ignoreCase = true) || title.contains("Select", ignoreCase = true) || title.contains("Check In", ignoreCase = true)) {
                                "Change location"
                            } else {
                                title
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ink
                        )
                        // Close button (`.sh button { width:34px; height:34px; border-radius:50%; background:var(--tint); color:var(--bz); font-size:20px }`)
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
                                color = bz,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    // Sheet body (`.sb { overflow-y:auto; padding:4px 16px 18px }`)
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .padding(top = 4.dp, bottom = 18.dp)
                    ) {
                        // Search input (`<input class="srch" id="ls" placeholder="Search any city or place" oninput="filt(this.value)">`)
                        var isSearchFocused by remember { mutableStateOf(false) }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 8.dp)
                                .height(48.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(bg)
                                .border(
                                    width = 1.5.dp,
                                    color = if (isSearchFocused) g2 else line,
                                    shape = RoundedCornerShape(24.dp)
                                )
                                .padding(horizontal = 18.dp),
                            contentAlignment = Alignment.CenterStart
                        ) {
                            if (searchQuery.isEmpty()) {
                                Text(
                                    text = "Search any city or place",
                                    fontSize = 15.sp,
                                    color = mut
                                )
                            }
                            BasicTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                singleLine = true,
                                textStyle = TextStyle(
                                    fontSize = 15.sp,
                                    color = ink
                                ),
                                cursorBrush = SolidColor(bz),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("location_search_input")
                            )
                        }

                        // Location list (`<div id="ll"></div>` -> `<button class="li" data-act="setloc" data-v="${esc(l)}">📍 ${l}</button>`)
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false)
                        ) {
                            if (displayedLocations.isEmpty()) {
                                item {
                                    Text(
                                        text = "No places found",
                                        fontSize = 13.sp,
                                        color = mut,
                                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                                    )
                                }
                            } else {
                                items(displayedLocations) { locItem ->
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .clickable {
                                                val cached = livePlaceCache[locItem]
                                                val resolved = cached?.copy(
                                                    name = locItem,
                                                    fullAddress = locItem
                                                ) ?: LocationHelper.resolvePlaceByName(
                                                    placeName = locItem,
                                                    fallbackLat = if (initialLatitude == 0.0) 13.4967 else initialLatitude,
                                                    fallbackLng = if (initialLongitude == 0.0) 39.4753 else initialLongitude
                                                )
                                                onLocationSelected(resolved, initialRadiusKm)
                                            }
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 6.dp, vertical = 13.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Text(text = "📍", fontSize = 15.sp)
                                            Text(
                                                text = locItem,
                                                fontSize = 15.sp,
                                                color = ink
                                            )
                                        }
                                        HorizontalDivider(color = line, thickness = 1.dp)
                                    }
                                }
                            }

                            if (fallbackNote != null) {
                                item {
                                    Text(
                                        text = fallbackNote.orEmpty(),
                                        fontSize = 13.sp,
                                        color = mut,
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
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
