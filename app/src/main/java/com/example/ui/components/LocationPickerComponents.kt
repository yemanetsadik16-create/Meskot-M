package com.example.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
import kotlinx.coroutines.launch
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
            URLEncoder.encode(placeName.ifBlank { "Addis Ababa" }, "UTF-8")
        }
        "https://maps.google.com/maps?q=$q&z=$zoom&output=embed"
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = FbLightSurface),
        border = BorderStroke(1.dp, Color(0xFFDADDE1))
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
                    color = Color.White.copy(alpha = 0.94f),
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = FbRedPin,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = placeName.ifBlank {
                                String.format(Locale.US, "%.4f, %.4f", latitude, longitude)
                            },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FbDarkInk,
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
                        .background(Color.White)
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
                            tint = FbBluePin,
                            modifier = Modifier.size(17.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = placeName.ifBlank { "Pinned Location" },
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = FbDarkInk,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = String.format(Locale.US, "GPS: %.4f, %.4f · Google Maps", latitude, longitude),
                                fontSize = 11.sp,
                                color = FbSubtleText
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Open Map",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = FbBluePin
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.OpenInNew,
                            contentDescription = "Open in Google Maps",
                            tint = FbBluePin,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Full-screen Facebook-style "Search for location" / Google Maps Picker Dialog.
 *
 * Used across Meskot for:
 * - Post Check-Ins ("is in Addis Ababa, Ethiopia")
 * - Marketplace Location & Radius filter ("Today's picks" feed)
 * - Marketplace Create Listing ("Sell" item location)
 * - Profile "Current City / Hometown" selection
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RealLocationPickerDialog(
    title: String = "Choose Location",
    initialPlaceName: String = "",
    initialLatitude: Double = 9.0192,
    initialLongitude: Double = 38.7525,
    showRadiusSlider: Boolean = false,
    initialRadiusKm: Double = 65.0,
    onDismiss: () -> Unit,
    onLocationSelected: (place: RealPlaceResult, radiusKm: Double) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var searchQuery by remember { mutableStateOf("") }
    var selectedPlace by remember {
        mutableStateOf(
            RealPlaceResult(
                name = initialPlaceName.ifBlank { "Addis Ababa, Ethiopia" },
                fullAddress = initialPlaceName.ifBlank { "Addis Ababa, Ethiopia" },
                latitude = if (initialLatitude == 0.0 && initialLongitude == 0.0) 9.0192 else initialLatitude,
                longitude = if (initialLatitude == 0.0 && initialLongitude == 0.0) 38.7525 else initialLongitude
            )
        )
    }
    var radiusSlider by remember { mutableFloatStateOf(initialRadiusKm.toFloat().coerceIn(5f, 250f)) }
    var searchResults by remember { mutableStateOf(LocationHelper.defaultVerifiedPlaces()) }
    var isSearching by remember { mutableStateOf(false) }
    var isDetectingGps by remember { mutableStateOf(false) }
    var gpsStatusMessage by remember { mutableStateOf<String?>(null) }

    // Live debounced geocoding search as user types
    LaunchedEffect(searchQuery) {
        if (searchQuery.trim().length < 2) {
            searchResults = LocationHelper.defaultVerifiedPlaces()
            isSearching = false
            return@LaunchedEffect
        }
        isSearching = true
        delay(350)
        val places = LocationHelper.searchPlaces(context, searchQuery)
        searchResults = places
        isSearching = false
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            isDetectingGps = true
            gpsStatusMessage = "Detecting your live GPS location..."
            scope.launch {
                val place = LocationHelper.fetchCurrentDevicePlace(context)
                isDetectingGps = false
                if (place != null) {
                    selectedPlace = place
                    gpsStatusMessage = "📍 Live GPS location detected: ${place.name}"
                } else {
                    gpsStatusMessage = "Could not lock GPS signal in emulator; showing map pin."
                }
            }
        } else {
            gpsStatusMessage = "Location permission denied. You can search any city or landmark below."
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .testTag("real_location_picker_dialog"),
            containerColor = Color.White,
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = title,
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = FbDarkInk
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = FbDarkInk
                            )
                        }
                    },
                    actions = {
                        Button(
                            onClick = {
                                onLocationSelected(selectedPlace, radiusSlider.toDouble())
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = FbBluePin),
                            shape = RoundedCornerShape(20.dp),
                            modifier = Modifier
                                .padding(end = 10.dp)
                                .testTag("confirm_location_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Apply", fontWeight = FontWeight.Bold)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // 1. Live Google Map Preview at Top
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    InteractiveGoogleMapCard(
                        latitude = selectedPlace.latitude,
                        longitude = selectedPlace.longitude,
                        placeName = selectedPlace.name,
                        height = 185.dp,
                        showOpenButton = true
                    )
                }

                // 2. "Use my current GPS location" Button + Search Bar
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            if (LocationHelper.hasLocationPermission(context)) {
                                isDetectingGps = true
                                gpsStatusMessage = "Fetching real GPS coordinates..."
                                scope.launch {
                                    val place = LocationHelper.fetchCurrentDevicePlace(context)
                                    isDetectingGps = false
                                    if (place != null) {
                                        selectedPlace = place
                                        gpsStatusMessage = "📍 Live GPS location locked: ${place.name}"
                                    } else {
                                        gpsStatusMessage = "GPS sensor unavailable; tap any place or search below."
                                    }
                                }
                            } else {
                                locationPermissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.2.dp, FbBluePin),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("use_current_gps_button")
                    ) {
                        if (isDetectingGps) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                strokeWidth = 2.dp,
                                color = FbBluePin
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Use current GPS location",
                                tint = FbBluePin,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isDetectingGps) "Detecting GPS Location..." else "Use My Current GPS Location (Google Location)",
                            color = FbBluePin,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.5.sp
                        )
                    }

                    if (gpsStatusMessage != null) {
                        Text(
                            text = gpsStatusMessage!!,
                            fontSize = 12.sp,
                            color = FbBluePin,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Search Places Input Field
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search any city, neighborhood, or landmark on Google Maps...") },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = FbSubtleText
                            )
                        },
                        trailingIcon = {
                            if (isSearching) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    strokeWidth = 2.dp,
                                    color = FbBluePin
                                )
                            } else if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear")
                                }
                            }
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(24.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedContainerColor = FbLightSurface,
                            unfocusedContainerColor = FbLightSurface,
                            focusedBorderColor = FbBluePin,
                            unfocusedBorderColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("location_search_input")
                    )

                    // Optional Distance Radius Slider (used in Marketplace)
                    if (showRadiusSlider) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Search Radius",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = FbDarkInk
                                )
                                Text(
                                    text = "${radiusSlider.toInt()} km",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = FbBluePin
                                )
                            }
                            Slider(
                                value = radiusSlider,
                                onValueChange = { radiusSlider = it },
                                valueRange = 5f..250f,
                                colors = SliderDefaults.colors(
                                    thumbColor = FbBluePin,
                                    activeTrackColor = FbBluePin
                                ),
                                modifier = Modifier.height(28.dp)
                            )
                        }
                    }
                }

                HorizontalDivider(color = Color(0xFFE4E6EB))

                // 3. Real Geocoded Place Results List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    items(searchResults, key = { "${it.name}_${it.latitude}_${it.longitude}" }) { place ->
                        val isSelected = place.name.equals(selectedPlace.name, ignoreCase = true)
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedPlace = place }
                                .background(if (isSelected) FbBluePin.copy(alpha = 0.08f) else Color.Transparent)
                                .padding(horizontal = 16.dp, vertical = 11.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (isSelected) FbBluePin else FbLightSurface),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = null,
                                    tint = if (isSelected) Color.White else FbRedPin,
                                    modifier = Modifier.size(20.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = place.name,
                                    fontSize = 15.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                                    color = if (isSelected) FbBluePin else FbDarkInk,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = place.fullAddress,
                                    fontSize = 12.sp,
                                    color = FbSubtleText,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = String.format(Locale.US, "%.4f° N, %.4f° E", place.latitude, place.longitude),
                                    fontSize = 11.sp,
                                    color = FbSubtleText.copy(alpha = 0.8f)
                                )
                            }

                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = "Selected",
                                    tint = FbBluePin
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
