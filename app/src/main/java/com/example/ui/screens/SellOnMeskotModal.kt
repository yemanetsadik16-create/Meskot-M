package com.example.ui.screens

import android.annotation.SuppressLint
import android.content.Context
import android.net.Uri
import android.webkit.JavascriptInterface
import android.webkit.ValueCallback
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.data.Category
import com.example.data.MarketplaceLocation
import com.example.util.LocationHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Exact Color Tokens from the HTML:
 * :root{--bg:#fbf8ef;--paper:#fffdf8;--ink:#1d2419;--mut:#7a7360;--line:#eadfc2;--g1:#efd06c;--g2:#a87b25;--bz:#96691f;--mar:#8a1c2b;--tint:#f6edd2;--sh:rgba(120,90,20,.14)}
 * @media (prefers-color-scheme:dark){--bg:#15120b;--paper:#211c11;--ink:#f6ecd2;--mut:#b6ac90;--line:#3a3220;--tint:#2d2615;--sh:rgba(0,0,0,.5)}
 */
private data class HtmlColors(
    val bg: Color,
    val paper: Color,
    val ink: Color,
    val mut: Color,
    val line: Color,
    val g1: Color = Color(0xFFEFD06C),
    val g2: Color = Color(0xFFA87B25),
    val bz: Color = Color(0xFF96691F),
    val mar: Color = Color(0xFF8A1C2B),
    val tint: Color
) {
    val goldGrad: Brush get() = Brush.linearGradient(listOf(g1, g2))
    val goldHoriz: Brush get() = Brush.horizontalGradient(listOf(g1, g2))
}

private val LightHtmlColors = HtmlColors(
    bg = Color(0xFFFBF8EF),
    paper = Color(0xFFFFFDF8),
    ink = Color(0xFF1D2419),
    mut = Color(0xFF7A7360),
    line = Color(0xFFEADFC2),
    tint = Color(0xFFF6EDD2)
)

private val DarkHtmlColors = HtmlColors(
    bg = Color(0xFF15120B),
    paper = Color(0xFF211C11),
    ink = Color(0xFFF6ECD2),
    mut = Color(0xFFB6AC90),
    line = Color(0xFF3A3220),
    tint = Color(0xFF2D2615)
)

// Exact CATS array from the HTML (28 categories)
val MESKOT_HTML_CATS: List<Pair<String, String>> = listOf(
    "Vehicles" to "🚗",
    "Rentals" to "🏠",
    "Women's clothing & shoes" to "👗",
    "Men's clothing & shoes" to "👔",
    "Furniture" to "🛋️",
    "Electronics & computers" to "💻",
    "Mobile phones" to "📱",
    "Home sales" to "🏡",
    "Video Games" to "🎮",
    "Toys & Games" to "🧸",
    "Appliances" to "🔌",
    "Antiques & Collectibles" to "🏺",
    "Arts & Crafts" to "🎨",
    "Auto parts" to "🔧",
    "Baby & kids" to "🍼",
    "Bags & Luggage" to "🧳",
    "Bicycles" to "🚲",
    "Books, Movies & Music" to "📚",
    "Garage Sale" to "🏷️",
    "Garden" to "🌿",
    "Health & beauty" to "💄",
    "Household" to "🧺",
    "Jewelry & Accessories" to "💍",
    "Miscellaneous" to "✨",
    "Musical Instruments" to "🎸",
    "Pet Supplies" to "🐾",
    "Sports & Outdoors" to "⚽",
    "Tools" to "🛠️"
)

// Exact VT array from the HTML (Vehicle types)
private data class VehicleTypeItem(val name: String, val desc: String, val icon: String)

private val MESKOT_HTML_VT = listOf(
    VehicleTypeItem("Cars & Trucks", "Sedans, SUVs, trucks, vans, etc.", "🚗"),
    VehicleTypeItem("Motorcycles", "Street, dirt bikes, scooters, etc.", "🏍️"),
    VehicleTypeItem("Powersports", "ATVs, snowmobiles, watercrafts, etc.", "🛺"),
    VehicleTypeItem("RVs & Campers", "Motorhomes, towable RVs, etc.", "🚐"),
    VehicleTypeItem("Boats", "Sailboats, motorboats, etc.", "⛵"),
    VehicleTypeItem("Commercial & Industrial", "Semi trucks, cranes, tractors, etc.", "🚛"),
    VehicleTypeItem("Trailers", "Livestock, cargo vehicles, etc.", "🛻"),
    VehicleTypeItem("Other", "Golf carts, aircrafts, etc.", "🚜")
)

// Exact YEARS array from the HTML: Array.from({length:128},(_,i)=>String(2027-i))
private val MESKOT_HTML_YEARS: List<String> = List(128) { i -> (2027 - i).toString() }

// Exact COL array from the HTML (20 interior/exterior color swatches)
private data class ColorSwatchItem(val name: String, val color: Color, val isRainbow: Boolean = false)

private val MESKOT_HTML_COL = listOf(
    ColorSwatchItem("Black", Color(0xFF111111)),
    ColorSwatchItem("Charcoal", Color(0xFF36454F)),
    ColorSwatchItem("Grey", Color(0xFF8A8D91)),
    ColorSwatchItem("Silver", Color(0xFFC4C7CC)),
    ColorSwatchItem("White", Color(0xFFFFFFFF)),
    ColorSwatchItem("Off white", Color(0xFFF3EFE4)),
    ColorSwatchItem("Tan", Color(0xFFD2B48C)),
    ColorSwatchItem("Beige", Color(0xFFE0D4B8)),
    ColorSwatchItem("Yellow", Color(0xFFF4D03F)),
    ColorSwatchItem("Gold", Color(0xFFC9A227)),
    ColorSwatchItem("Brown", Color(0xFF7B4A2A)),
    ColorSwatchItem("Orange", Color(0xFFF08A24)),
    ColorSwatchItem("Red", Color(0xFFD32F2F)),
    ColorSwatchItem("Burgundy", Color(0xFF7B1E2B)),
    ColorSwatchItem("Pink", Color(0xFFF48FB1)),
    ColorSwatchItem("Purple", Color(0xFF7E57C2)),
    ColorSwatchItem("Blue", Color(0xFF1E6FD9)),
    ColorSwatchItem("Turquoise", Color(0xFF26C6B4)),
    ColorSwatchItem("Green", Color(0xFF2E8B4E)),
    ColorSwatchItem("Other", Color(0xFFD32F2F), isRainbow = true)
)

// Exact LOCS array from the HTML
val MESKOT_HTML_LOCS: List<String> = listOf(
    "Mekelle",
    "Wukro",
    "Adwa",
    "Axum, Ethiopia",
    "Addis Ababa, Ethiopia",
    "Temben, Tigray, Ethiopia",
    "Anseba, Tigray, Ethiopia",
    "Shehet, Tigray, Ethiopia",
    "Megab, Tigray, Ethiopia",
    "Calgary, Alberta",
    "Perth, Western Australia",
    "Berlin, Germany",
    "Bangkok, Thailand",
    "Makurdu, Benue, Nigeria",
    "Dhaka, Bangladesh",
    "Baghdad, Iraq",
    "Bangalore, India"
)

// Exact F field definitions from the HTML
private val BODY_CHIPS = listOf(
    "Convertible", "Coupe", "Hatchback", "Minivan", "Sedan", "Wagon", "SUV", "Truck", "Small Car", "Other"
)
private val TRANS_CHIPS = listOf(
    "Automatic transmission", "Manual transmission"
)
private val FUEL_CHIPS = listOf(
    "Diesel", "Electric", "Gasoline", "Flex", "Hybrid", "Other", "Petrol", "Plug-in hybrid"
)
private val COND_CHIPS = listOf(
    "New", "Used - Like New", "Used - Good", "Used - Fair"
)
private val DEV_CHIPS = listOf(
    "iPhone XS", "iPhone XS Max", "iPhone XR", "iPhone 8 Plus", "iPhone 11", "iPhone 12", "Samsung Galaxy S21", "Google Pixel 6"
)
private val PLAT_CHIPS = listOf(
    "Playstation+4", "Nintendo+Switch", "Xbox+One", "PC", "Nintendo+DS", "Nintendo+WiiU", "Nintendo+Wii", "Nintendo+GameCube", "Playstation+3", "Xbox+360"
)
private val AVAIL_CHIPS = listOf(
    "List as Single Item", "List as In Stock"
)

/**
 * Exact implementation of the user's "Sell on Meskot" HTML/CSS/JS specification
 * with full working Category bottom sheet (28 categories + dynamic conditional fields for
 * Vehicles, Clothing, Mobile phones, Video Games, Toys & Games, etc.) and
 * Location bottom sheet (instant filter over LOCS + live OpenStreetMap Nominatim search).
 */
@Composable
fun SellOnMeskotModal(
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
        condition: String,
        isNegotiable: Boolean,
        deliveryOption: String,
        sellerPhone: String,
        allowChat: Boolean,
        allowCall: Boolean,
        allowWhatsApp: Boolean,
        isPromoted: Boolean
    ) -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val c = LightHtmlColors

    // State matching `S` in the HTML
    var cat by remember { mutableStateOf("") }
    var type by remember { mutableStateOf("") }
    var year by remember { mutableStateOf("") }
    var make by remember { mutableStateOf("") }
    var model by remember { mutableStateOf("") }
    var trim by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var trans by remember { mutableStateOf("") }
    var fuel by remember { mutableStateOf("") }
    var icol by remember { mutableStateOf("") }
    var ecol by remember { mutableStateOf("") }
    var cond by remember { mutableStateOf("") }
    var carrier by remember { mutableStateOf("") }
    var dev by remember { mutableStateOf("") }
    var brand by remember { mutableStateOf("") }
    var size by remember { mutableStateOf("") }
    var plat by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var price by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var avail by remember { mutableStateOf("List as Single Item") }
    var loc by remember {
        mutableStateOf(
            if (defaultLocation.name.isNotBlank() && defaultLocation.name != "Ariena") {
                defaultLocation.name
            } else {
                "Mekelle"
            }
        )
    }
    var ship by remember { mutableStateOf(false) }
    var hide by remember { mutableStateOf(false) }
    var cm by remember { mutableStateOf(true) }
    val photos = remember { mutableStateListOf<String>() }

    // Sheet & modal state (`ov`, `ok`, `toast`)
    var activePickKey by remember { mutableStateOf<String?>(null) } // "cat", "type", "year", "icol", "ecol", "loc"
    var showOkModal by remember { mutableStateOf(false) }
    var toastText by remember { mutableStateOf<String?>(null) }
    var toastJob by remember { mutableStateOf<Job?>(null) }

    fun showToast(msg: String) {
        toastJob?.cancel()
        toastText = msg
        toastJob = coroutineScope.launch {
            delay(2200)
            toastText = null
        }
    }

    // Photo picker (`<input type="file" id="file" accept="image/*" multiple hidden onchange="addPh(event)">`)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 10)
    ) { uris ->
        for (uri in uris) {
            if (photos.size >= 10) {
                showToast("You can add up to 10 photos")
                break
            }
            photos.add(uri.toString())
        }
    }

    // Exact `layout()` function from the HTML:
    // function layout(){
    //   const c=S.cat;
    //   if(c==='Vehicles')return ['cat','type','year','make','model',...(S.type==='Cars & Trucks'?['trim','body','trans','fuel']:[]),'icol','ecol','price','loc','desc'];
    //   const x=/clothing/.test(c)?['cond','brand','size']:c==='Mobile phones'?['cond','carrier','dev']:c==='Video Games'?['cond','plat']:c==='Toys & Games'?['cond','brand']:(c==='Rentals'||c==='Home sales'||!c)?[]:['cond'];
    //   return ['cat',...x,'title','price','loc','desc','avail'];
    // }
    val currentLayout = remember(cat, type) {
        if (cat == "Vehicles") {
            buildList {
                add("cat")
                add("type")
                add("year")
                add("make")
                add("model")
                if (type == "Cars & Trucks") {
                    add("trim")
                    add("body")
                    add("trans")
                    add("fuel")
                }
                add("icol")
                add("ecol")
                add("price")
                add("loc")
                add("desc")
            }
        } else {
            val extra = when {
                cat.contains("clothing") -> listOf("cond", "brand", "size")
                cat == "Mobile phones" -> listOf("cond", "carrier", "dev")
                cat == "Video Games" -> listOf("cond", "plat")
                cat == "Toys & Games" -> listOf("cond", "brand")
                cat == "Rentals" || cat == "Home sales" || cat.isEmpty() -> emptyList()
                else -> listOf("cond")
            }
            buildList {
                add("cat")
                addAll(extra)
                add("title")
                add("price")
                add("loc")
                add("desc")
                add("avail")
            }
        }
    }

    val priceIndex = currentLayout.indexOf("price")
    val aboutFields = remember(currentLayout, priceIndex) {
        if (priceIndex >= 0) currentLayout.subList(0, priceIndex) else listOf("cat", "title")
    }
    val descFields = remember(currentLayout, priceIndex) {
        if (priceIndex >= 0 && priceIndex + 2 <= currentLayout.size) {
            currentLayout.subList(priceIndex + 2, currentLayout.size)
        } else {
            listOf("desc", "avail")
        }
    }

    // Exact `meter()` function from the HTML:
    // const v=S.cat==='Vehicles',done=[S.photos.length>0,!!S.cat,v?(S.type&&S.year):(S.title||'').trim(),S.price!==''].filter(Boolean).length;
    // $('mt').style.width=done*25+'%';
    val doneCount = remember(photos.size, cat, type, year, title, price) {
        val isVeh = cat == "Vehicles"
        var cnt = 0
        if (photos.isNotEmpty()) cnt++
        if (cat.isNotEmpty()) cnt++
        if (isVeh) {
            if (type.isNotEmpty() && year.isNotEmpty()) cnt++
        } else {
            if (title.trim().isNotEmpty()) cnt++
        }
        if (price.isNotEmpty()) cnt++
        cnt
    }
    val meterProgress by animateFloatAsState(
        targetValue = (doneCount * 0.25f).coerceIn(0f, 1f),
        animationSpec = tween(250),
        label = "meter"
    )

    // Exact `choose(k, v)` function from the HTML:
    // function choose(k,v){
    //   if(k==='cat'&&v!==S.cat)S={...S,...BLANK,cat:v,price:S.price,title:S.title,desc:S.desc};
    //   else S[k]=v;
    //   if(k==='type')S.trans=v==='Cars & Trucks'?'Automatic transmission':'';
    // }
    fun chooseOption(k: String, v: String) {
        if (k == "cat" && v != cat) {
            cat = v
            type = ""
            year = ""
            make = ""
            model = ""
            trim = ""
            body = ""
            trans = ""
            fuel = ""
            icol = ""
            ecol = ""
            cond = ""
            carrier = ""
            dev = ""
            brand = ""
            size = ""
            plat = ""
        } else {
            when (k) {
                "cat" -> cat = v
                "type" -> {
                    type = v
                    trans = if (v == "Cars & Trucks") "Automatic transmission" else ""
                }
                "year" -> year = v
                "icol" -> icol = v
                "ecol" -> ecol = v
            }
        }
    }

    // Exact `publish()` function from the HTML:
    // function publish(){
    //   const v=S.cat==='Vehicles';
    //   if(!S.cat)return toast('Choose a category first');
    //   if(v&&(!S.type||!S.year))return toast('Choose the vehicle type and year');
    //   if(!v&&!(S.title||'').trim())return toast('Tell us what you are selling');
    //   if(S.price==='')return toast('Add a price');
    //   $('ok').classList.add('on');
    // }
    fun triggerPublish() {
        val isVeh = cat == "Vehicles"
        if (cat.isEmpty()) {
            showToast("Choose a category first")
            return
        }
        if (isVeh && (type.isEmpty() || year.isEmpty())) {
            showToast("Choose the vehicle type and year")
            return
        }
        if (!isVeh && title.trim().isEmpty()) {
            showToast("Tell us what you are selling")
            return
        }
        if (price.isEmpty()) {
            showToast("Add a price")
            return
        }
        showOkModal = true
    }

    // Complete submission & `reset()` when user taps "Done" on the published modal
    fun finishAndSubmit() {
        val isVeh = cat == "Vehicles"
        val computedTitle = if (isVeh) {
            listOf(year, make, model, trim).filter { it.isNotBlank() }.joinToString(" ")
                .ifBlank { "$year $type" }
        } else {
            title.trim()
        }
        val numPrice = price.toDoubleOrNull() ?: 0.0
        val mappedCatId = when (cat) {
            "Vehicles", "Auto parts", "Bicycles" -> "vehicles"
            "Rentals", "Home sales" -> "property"
            "Electronics & computers", "Mobile phones", "Video Games", "Appliances" -> "electronics"
            "Furniture", "Household", "Garden", "Tools" -> "furniture"
            "Women's clothing & shoes", "Men's clothing & shoes", "Bags & Luggage", "Jewelry & Accessories", "Health & beauty", "Baby & kids" -> "fashion"
            "Musical Instruments" -> "musical"
            "Antiques & Collectibles", "Arts & Crafts" -> "coffee_cultural"
            else -> "classifieds"
        }
        val presetMatch = LocationHelper.defaultVerifiedPlaces().find {
            loc.contains(it.name, ignoreCase = true) || it.name.contains(loc.substringBefore(","), ignoreCase = true)
        }
        val finalLat = presetMatch?.latitude ?: defaultLocation.latitude
        val finalLng = presetMatch?.longitude ?: defaultLocation.longitude

        val effectiveCondition = cond.ifBlank { "New" }
        val fullDescription = buildString {
            if (desc.isNotBlank()) append(desc.trim())
            else append("$computedTitle listed in $loc ($cat).")
            if (isVeh) {
                val specs = listOfNotNull(
                    type.takeIf { it.isNotBlank() }?.let { "Type: $it" },
                    year.takeIf { it.isNotBlank() }?.let { "Year: $it" },
                    make.takeIf { it.isNotBlank() }?.let { "Make: $it" },
                    model.takeIf { it.isNotBlank() }?.let { "Model: $it" },
                    body.takeIf { it.isNotBlank() }?.let { "Body: $it" },
                    trans.takeIf { it.isNotBlank() }?.let { "Transmission: $it" },
                    fuel.takeIf { it.isNotBlank() }?.let { "Fuel: $it" },
                    ecol.takeIf { it.isNotBlank() }?.let { "Exterior: $it" },
                    icol.takeIf { it.isNotBlank() }?.let { "Interior: $it" }
                )
                if (specs.isNotEmpty()) {
                    append("\n\n")
                    append(specs.joinToString(" · "))
                }
            }
        }

        showOkModal = false
        onSubmit(
            computedTitle,
            numPrice,
            "USD",
            mappedCatId,
            fullDescription,
            photos.toList(),
            MarketplaceLocation(
                latitude = finalLat,
                longitude = finalLng,
                name = loc,
                radiusKm = defaultLocation.radiusKm
            ),
            effectiveCondition,
            true,
            if (ship) "Shipping & Meetup · $loc" else "Meetup · $loc",
            "+251 911 234 567",
            cm,
            true,
            false,
            false
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        BackHandler {
            when {
                activePickKey != null -> activePickKey = null
                showOkModal -> finishAndSubmit()
                else -> onDismiss()
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(c.bg)
                .testTag("sell_on_meskot_modal")
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top 4px Gold Gradient Border (`border-top:4px solid;border-image:linear-gradient(90deg,var(--g1),var(--g2)) 1`)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .height(4.dp)
                        .background(c.goldHoriz)
                )

                // Sticky Header (`.hd`)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(c.bg)
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Back button (`.rb`)
                    Surface(
                        onClick = {
                            showToast("Listing discarded")
                            onDismiss()
                        },
                        shape = CircleShape,
                        color = c.paper,
                        border = BorderStroke(2.dp, c.g1),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Back",
                                tint = c.bz,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                    }

                    // Title & Subtitle (`.tt`)
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sell on Meskot",
                            fontSize = 19.sp,
                            fontWeight = FontWeight.Bold,
                            color = c.ink,
                            lineHeight = 22.sp
                        )
                        Text(
                            text = "መስኮት · New listing",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = c.bz
                        )
                    }

                    // Top "Publish" button (`.gp`)
                    Box(
                        modifier = Modifier
                            .height(44.dp)
                            .shadow(6.dp, RoundedCornerShape(22.dp))
                            .clip(RoundedCornerShape(22.dp))
                            .background(c.goldGrad)
                            .clickable { triggerPublish() }
                            .padding(horizontal = 20.dp)
                            .testTag("publish_listing_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Publish",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }
                }

                // Progress Meter (`.mt`)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(c.line)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(meterProgress)
                            .background(c.goldHoriz)
                    )
                }

                // Main Scrollable Form (`main#form`)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(start = 14.dp, end = 14.dp, top = 14.dp, bottom = 120.dp)
                ) {
                    // =========================================================
                    // SECTION 1: PHOTOS
                    // =========================================================
                    HtmlCard(c = c) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Photos",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = c.ink
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(c.tint)
                                    .padding(horizontal = 10.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "${photos.size} / 10",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = c.bz
                                )
                            }
                        }

                        if (photos.isEmpty()) {
                            // Dashed Dropzone (`.drop`)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(22.dp))
                                    .background(c.bg)
                                    .border(2.dp, c.g1, RoundedCornerShape(22.dp))
                                    .clickable {
                                        photoPickerLauncher.launch(
                                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                        )
                                    }
                                    .padding(horizontal = 12.dp, vertical = 26.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(c.goldGrad),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.CameraAlt,
                                            contentDescription = "Add photos",
                                            tint = Color.White,
                                            modifier = Modifier.size(26.dp)
                                        )
                                    }
                                    Text(
                                        text = "Add photos",
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = c.ink
                                    )
                                    Text(
                                        text = "Good light and a clean background sell faster. First photo is the cover.",
                                        fontSize = 13.sp,
                                        color = c.mut,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        } else {
                            // 3-Column Photo Grid (`.pg`)
                            val rows = (photos.mapIndexed { idx, u -> idx to u } +
                                    if (photos.size < 10) listOf(-1 to "") else emptyList()).chunked(3)
                            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                rows.forEach { rowItems ->
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        rowItems.forEach { (idx, url) ->
                                            if (idx == -1) {
                                                // Add more button (`.ad`)
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .aspectRatio(1f)
                                                        .clip(RoundedCornerShape(18.dp))
                                                        .border(2.dp, c.g1, RoundedCornerShape(18.dp))
                                                        .clickable {
                                                            photoPickerLauncher.launch(
                                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                            )
                                                        },
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Text(
                                                        text = "+",
                                                        fontSize = 28.sp,
                                                        color = c.bz
                                                    )
                                                }
                                            } else {
                                                // Photo item (`.ph`)
                                                Box(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                        .aspectRatio(1f)
                                                        .clip(RoundedCornerShape(18.dp))
                                                        .background(c.tint)
                                                ) {
                                                    AsyncImage(
                                                        model = url,
                                                        contentDescription = null,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                    // Remove button (`.x`)
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.TopEnd)
                                                            .padding(5.dp)
                                                            .size(26.dp)
                                                            .clip(CircleShape)
                                                            .background(Color(0xB31D2419))
                                                            .clickable { photos.removeAt(idx) },
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = "×",
                                                            color = Color.White,
                                                            fontSize = 17.sp
                                                        )
                                                    }
                                                    // Cover badge (`.cv`)
                                                    if (idx == 0) {
                                                        Box(
                                                            modifier = Modifier
                                                                .align(Alignment.BottomStart)
                                                                .padding(6.dp)
                                                                .clip(RoundedCornerShape(10.dp))
                                                                .background(c.goldGrad)
                                                                .padding(horizontal = 9.dp, vertical = 1.dp)
                                                        ) {
                                                            Text(
                                                                text = "Cover",
                                                                color = Color.White,
                                                                fontSize = 11.sp,
                                                                fontWeight = FontWeight.SemiBold
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        repeat(3 - rowItems.size) {
                                            Spacer(modifier = Modifier.weight(1f))
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // =========================================================
                    // SECTION 2: ABOUT THE ITEM
                    // =========================================================
                    HtmlCard(c = c) {
                        Text(
                            text = "About the item",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = c.ink,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            aboutFields.forEach { key ->
                                RenderHtmlField(
                                    key = key,
                                    c = c,
                                    cat = cat,
                                    type = type,
                                    year = year,
                                    make = make,
                                    onMakeChange = { make = it },
                                    model = model,
                                    onModelChange = { model = it },
                                    trim = trim,
                                    onTrimChange = { trim = it },
                                    body = body,
                                    onBodyChange = { body = if (body == it) "" else it },
                                    trans = trans,
                                    onTransChange = { trans = if (trans == it) "" else it },
                                    fuel = fuel,
                                    onFuelChange = { fuel = if (fuel == it) "" else it },
                                    icol = icol,
                                    ecol = ecol,
                                    cond = cond,
                                    onCondChange = { cond = if (cond == it) "" else it },
                                    carrier = carrier,
                                    onCarrierChange = { carrier = it },
                                    dev = dev,
                                    onDevChange = { dev = if (dev == it) "" else it },
                                    brand = brand,
                                    onBrandChange = { brand = it },
                                    size = size,
                                    onSizeChange = { size = it },
                                    plat = plat,
                                    onPlatChange = { plat = if (plat == it) "" else it },
                                    title = title,
                                    onTitleChange = { title = it },
                                    price = price,
                                    onPriceChange = { price = it },
                                    loc = loc,
                                    desc = desc,
                                    onDescChange = { desc = it },
                                    avail = avail,
                                    onAvailChange = { avail = if (avail == it) "" else it },
                                    onOpenPick = { pickKey -> activePickKey = pickKey }
                                )
                            }
                        }
                    }

                    // =========================================================
                    // SECTION 3: PRICE & PLACE
                    // =========================================================
                    HtmlCard(c = c) {
                        Text(
                            text = "Price & place",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = c.ink,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            listOf("price", "loc").forEach { key ->
                                RenderHtmlField(
                                    key = key,
                                    c = c,
                                    cat = cat,
                                    type = type,
                                    year = year,
                                    make = make,
                                    onMakeChange = { make = it },
                                    model = model,
                                    onModelChange = { model = it },
                                    trim = trim,
                                    onTrimChange = { trim = it },
                                    body = body,
                                    onBodyChange = { body = if (body == it) "" else it },
                                    trans = trans,
                                    onTransChange = { trans = if (trans == it) "" else it },
                                    fuel = fuel,
                                    onFuelChange = { fuel = if (fuel == it) "" else it },
                                    icol = icol,
                                    ecol = ecol,
                                    cond = cond,
                                    onCondChange = { cond = if (cond == it) "" else it },
                                    carrier = carrier,
                                    onCarrierChange = { carrier = it },
                                    dev = dev,
                                    onDevChange = { dev = if (dev == it) "" else it },
                                    brand = brand,
                                    onBrandChange = { brand = it },
                                    size = size,
                                    onSizeChange = { size = it },
                                    plat = plat,
                                    onPlatChange = { plat = if (plat == it) "" else it },
                                    title = title,
                                    onTitleChange = { title = it },
                                    price = price,
                                    onPriceChange = { price = it },
                                    loc = loc,
                                    desc = desc,
                                    onDescChange = { desc = it },
                                    avail = avail,
                                    onAvailChange = { avail = if (avail == it) "" else it },
                                    onOpenPick = { pickKey -> activePickKey = pickKey }
                                )
                            }
                        }
                    }

                    // =========================================================
                    // SECTION 4: DESCRIPTION
                    // =========================================================
                    HtmlCard(c = c) {
                        Text(
                            text = "Description",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = c.ink,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            descFields.forEach { key ->
                                RenderHtmlField(
                                    key = key,
                                    c = c,
                                    cat = cat,
                                    type = type,
                                    year = year,
                                    make = make,
                                    onMakeChange = { make = it },
                                    model = model,
                                    onModelChange = { model = it },
                                    trim = trim,
                                    onTrimChange = { trim = it },
                                    body = body,
                                    onBodyChange = { body = if (body == it) "" else it },
                                    trans = trans,
                                    onTransChange = { trans = if (trans == it) "" else it },
                                    fuel = fuel,
                                    onFuelChange = { fuel = if (fuel == it) "" else it },
                                    icol = icol,
                                    ecol = ecol,
                                    cond = cond,
                                    onCondChange = { cond = if (cond == it) "" else it },
                                    carrier = carrier,
                                    onCarrierChange = { carrier = it },
                                    dev = dev,
                                    onDevChange = { dev = if (dev == it) "" else it },
                                    brand = brand,
                                    onBrandChange = { brand = it },
                                    size = size,
                                    onSizeChange = { size = it },
                                    plat = plat,
                                    onPlatChange = { plat = if (plat == it) "" else it },
                                    title = title,
                                    onTitleChange = { title = it },
                                    price = price,
                                    onPriceChange = { price = it },
                                    loc = loc,
                                    desc = desc,
                                    onDescChange = { desc = it },
                                    avail = avail,
                                    onAvailChange = { avail = if (avail == it) "" else it },
                                    onOpenPick = { pickKey -> activePickKey = pickKey }
                                )
                            }
                        }
                    }

                    // =========================================================
                    // SECTION 5: WHO CAN SEE IT
                    // =========================================================
                    HtmlCard(c = c) {
                        Text(
                            text = "Who can see it",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = c.ink,
                            modifier = Modifier.padding(bottom = 12.dp)
                        )

                        if (cat != "Vehicles") {
                            HtmlToggleRow(
                                c = c,
                                title = "Offer shipping",
                                subtitle = "Buyers can ask you to ship this item.",
                                checked = ship,
                                onCheckedChange = { ship = it },
                                isFirst = true
                            )
                        }

                        HtmlToggleRow(
                            c = c,
                            title = "Hide from friends",
                            subtitle = "This listing is still public. If you hide it from friends, they won't see it in most cases.",
                            linkText = "Learn more",
                            checked = hide,
                            onCheckedChange = { hide = it },
                            isFirst = cat == "Vehicles"
                        )

                        HtmlToggleRow(
                            c = c,
                            title = "Allow comments",
                            subtitle = "People can comment on your listing.",
                            checked = cm,
                            onCheckedChange = { cm = it },
                            isFirst = false
                        )
                    }

                    // Commerce Policy Note (`.note`)
                    Text(
                        text = "Every listing goes through a quick review to make sure it follows our Commerce policies. Animals, drugs, weapons and counterfeits are not allowed.",
                        fontSize = 12.sp,
                        color = c.mut,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 10.dp)
                    )
                }
            }

            // Fixed Bottom Footer (`footer.ft > button.big`)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, c.bg.copy(alpha = 0.9f), c.bg)
                        )
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 14.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .shadow(10.dp, RoundedCornerShape(27.dp))
                        .clip(RoundedCornerShape(27.dp))
                        .background(c.goldGrad)
                        .clickable { triggerPublish() }
                        .testTag("sell_bottom_publish_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Publish listing",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Bottom Sheet Overlay (`#ov` -> `.sheet`) for Category, Vehicle Type, Year, Colors, and Location
            AnimatedVisibility(
                visible = activePickKey != null,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(150)),
                modifier = Modifier.fillMaxSize()
            ) {
                val pickKey = activePickKey ?: ""
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x801D1405))
                        .clickable { activePickKey = null },
                    contentAlignment = Alignment.BottomCenter
                ) {
                    Surface(
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        color = c.paper,
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(0.86f)
                            .clickable(enabled = false) {}
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .navigationBarsPadding()
                        ) {
                            // Drag handle (`.sheet:before`)
                            Box(
                                modifier = Modifier
                                    .padding(top = 10.dp)
                                    .width(44.dp)
                                    .height(5.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(c.line)
                                    .align(Alignment.CenterHorizontally)
                            )

                            // Sheet header (`.sh`)
                            val sheetTitle = when (pickKey) {
                                "cat" -> "Category"
                                "type" -> "Vehicle types"
                                "year" -> "Year"
                                "icol" -> "Interior color"
                                "ecol" -> "Exterior color"
                                "loc" -> "Change location"
                                else -> "Select"
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = sheetTitle,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = c.ink
                                )
                                Box(
                                    modifier = Modifier
                                        .size(34.dp)
                                        .clip(CircleShape)
                                        .background(c.tint)
                                        .clickable { activePickKey = null },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "×",
                                        fontSize = 20.sp,
                                        color = c.bz,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Sheet body (`.sb`)
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp)
                                    .padding(bottom = 18.dp)
                            ) {
                                when (pickKey) {
                                    "cat" -> {
                                        // 2-Column Grid of all 28 Categories (`<div class="g2">`)
                                        LazyVerticalGrid(
                                            columns = GridCells.Fixed(2),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            items(MESKOT_HTML_CATS) { (catName, emoji) ->
                                                val isSelected = cat == catName
                                                Surface(
                                                    onClick = {
                                                        chooseOption("cat", catName)
                                                        activePickKey = null
                                                    },
                                                    shape = RoundedCornerShape(20.dp),
                                                    color = if (isSelected) c.tint else c.bg,
                                                    border = BorderStroke(
                                                        width = if (isSelected) 2.dp else 1.5.dp,
                                                        color = if (isSelected) c.g2 else c.line
                                                    ),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .heightIn(min = 96.dp)
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(12.dp),
                                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Text(text = emoji, fontSize = 26.sp)
                                                        Text(
                                                            text = catName,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = c.ink,
                                                            lineHeight = 18.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    "type" -> {
                                        // 2-Column Grid of Vehicle Types (`<div class="g2">`)
                                        LazyVerticalGrid(
                                            columns = GridCells.Fixed(2),
                                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                                            verticalArrangement = Arrangement.spacedBy(10.dp),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            items(MESKOT_HTML_VT) { vt ->
                                                val isSelected = type == vt.name
                                                Surface(
                                                    onClick = {
                                                        chooseOption("type", vt.name)
                                                        activePickKey = null
                                                    },
                                                    shape = RoundedCornerShape(20.dp),
                                                    color = if (isSelected) c.tint else c.bg,
                                                    border = BorderStroke(
                                                        width = if (isSelected) 2.dp else 1.5.dp,
                                                        color = if (isSelected) c.g2 else c.line
                                                    ),
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .heightIn(min = 96.dp)
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(12.dp),
                                                        verticalArrangement = Arrangement.spacedBy(2.dp)
                                                    ) {
                                                        Text(text = vt.icon, fontSize = 26.sp)
                                                        Text(
                                                            text = vt.name,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = c.ink,
                                                            lineHeight = 18.sp
                                                        )
                                                        Text(
                                                            text = vt.desc,
                                                            fontSize = 11.5.sp,
                                                            color = c.mut,
                                                            lineHeight = 15.sp
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    "year" -> {
                                        // 4-Column Grid of Years (`<div class="g4">`)
                                        LazyVerticalGrid(
                                            columns = GridCells.Fixed(4),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            items(MESKOT_HTML_YEARS) { yr ->
                                                val isSelected = year == yr
                                                Surface(
                                                    onClick = {
                                                        chooseOption("year", yr)
                                                        activePickKey = null
                                                    },
                                                    shape = RoundedCornerShape(14.dp),
                                                    color = if (isSelected) c.tint else c.bg,
                                                    border = BorderStroke(
                                                        width = if (isSelected) 2.dp else 1.5.dp,
                                                        color = if (isSelected) c.g2 else c.line
                                                    )
                                                ) {
                                                    Box(
                                                        modifier = Modifier.padding(vertical = 10.dp),
                                                        contentAlignment = Alignment.Center
                                                    ) {
                                                        Text(
                                                            text = yr,
                                                            fontSize = 14.sp,
                                                            fontWeight = FontWeight.SemiBold,
                                                            color = c.ink
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    "icol", "ecol" -> {
                                        val currentVal = if (pickKey == "icol") icol else ecol
                                        // 4-Column Grid of Color Swatches (`<div class="g4">`)
                                        LazyVerticalGrid(
                                            columns = GridCells.Fixed(4),
                                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                                            verticalArrangement = Arrangement.spacedBy(8.dp),
                                            contentPadding = PaddingValues(vertical = 4.dp)
                                        ) {
                                            items(MESKOT_HTML_COL) { colItem ->
                                                val isSelected = currentVal == colItem.name
                                                Surface(
                                                    onClick = {
                                                        chooseOption(pickKey, colItem.name)
                                                        activePickKey = null
                                                    },
                                                    shape = RoundedCornerShape(18.dp),
                                                    color = if (isSelected) c.tint else c.bg,
                                                    border = BorderStroke(
                                                        width = if (isSelected) 2.dp else 1.5.dp,
                                                        color = if (isSelected) c.g2 else c.line
                                                    )
                                                ) {
                                                    Column(
                                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
                                                        horizontalAlignment = Alignment.CenterHorizontally,
                                                        verticalArrangement = Arrangement.spacedBy(6.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .size(34.dp)
                                                                .clip(CircleShape)
                                                                .then(
                                                                    if (colItem.isRainbow) {
                                                                        Modifier.background(
                                                                            Brush.sweepGradient(
                                                                                listOf(
                                                                                    Color(0xFFD32F2F),
                                                                                    Color(0xFFF4D03F),
                                                                                    Color(0xFF2E8B4E),
                                                                                    Color(0xFF1E6FD9),
                                                                                    Color(0xFF7E57C2),
                                                                                    Color(0xFFD32F2F)
                                                                                )
                                                                            )
                                                                        )
                                                                    } else {
                                                                        Modifier.background(colItem.color)
                                                                    }
                                                                )
                                                                .border(1.dp, Color.Black.copy(alpha = 0.15f), CircleShape)
                                                        )
                                                        Text(
                                                            text = colItem.name,
                                                            fontSize = 12.sp,
                                                            fontWeight = FontWeight.Medium,
                                                            color = c.ink,
                                                            textAlign = TextAlign.Center,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    "loc" -> {
                                        // Exact Location Picker from HTML (`openLoc`, `filt`, `showLocs`)
                                        HtmlLocationPickerSheetContent(
                                            c = c,
                                            onSelectLocation = { selectedPlace ->
                                                loc = selectedPlace
                                                activePickKey = null
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Published Confirmation Modal (`#ok`)
            AnimatedVisibility(
                visible = showOkModal,
                enter = fadeIn(tween(150)),
                exit = fadeOut(tween(150)),
                modifier = Modifier.fillMaxSize()
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0x801D1405))
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Surface(
                        shape = RoundedCornerShape(28.dp),
                        color = c.paper,
                        modifier = Modifier.widthIn(max = 340.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .background(c.goldHoriz)
                            )
                            Column(
                                modifier = Modifier.padding(top = 22.dp, start = 22.dp, end = 22.dp, bottom = 22.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(c.goldGrad),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "M",
                                        color = Color.White,
                                        fontSize = 34.sp,
                                        fontWeight = FontWeight.Bold,
                                        fontStyle = FontStyle.Italic,
                                        fontFamily = FontFamily.Serif
                                    )
                                }
                                Spacer(modifier = Modifier.height(12.dp))
                                Text(
                                    text = "Listing published",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = c.ink
                                )
                                Text(
                                    text = "We review every listing quickly before it appears on Meskot Market.",
                                    fontSize = 14.sp,
                                    color = c.mut,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.padding(top = 6.dp, bottom = 18.dp)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(54.dp)
                                        .shadow(8.dp, RoundedCornerShape(27.dp))
                                        .clip(RoundedCornerShape(27.dp))
                                        .background(c.goldGrad)
                                        .clickable { finishAndSubmit() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Done",
                                        color = Color.White,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Toast notification (`#toast`)
            AnimatedVisibility(
                visible = toastText != null,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .navigationBarsPadding()
                    .padding(bottom = 92.dp)
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = c.mar,
                    shadowElevation = 8.dp
                ) {
                    Text(
                        text = toastText.orEmpty(),
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.padding(horizontal = 18.dp, vertical = 11.dp)
                    )
                }
            }
        }
    }
}

/**
 * Location Picker Sheet matching the HTML `openLoc()`, `filt(q)`, `search(q)`, and `showLocs(list, note)`
 */
@Composable
private fun HtmlLocationPickerSheetContent(
    c: HtmlColors,
    onSelectLocation: (String) -> Unit
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var displayedLocations by remember { mutableStateOf(MESKOT_HTML_LOCS) }
    var fallbackNote by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(searchQuery) {
        val q = searchQuery.trim()
        if (q.isEmpty()) {
            displayedLocations = MESKOT_HTML_LOCS
            fallbackNote = null
            return@LaunchedEffect
        }
        // Immediate local filter (`showLocs(local(q))`)
        val localMatches = MESKOT_HTML_LOCS.filter { it.contains(q, ignoreCase = true) }
        displayedLocations = localMatches
        fallbackNote = null

        // Debounced live search (`_t=setTimeout(async()=>{...}, 350)`)
        delay(350)
        try {
            val remoteResults = LocationHelper.searchPlaces(context, q)
            if (remoteResults.isNotEmpty()) {
                val names = remoteResults.map { place ->
                    place.fullAddress.ifBlank { place.displayLabel }
                }.distinct()
                displayedLocations = (localMatches + names).distinct()
                fallbackNote = null
            } else if (localMatches.isEmpty()) {
                displayedLocations = emptyList()
            }
        } catch (_: Exception) {
            displayedLocations = localMatches
            fallbackNote = "Live place search is not available here. Showing saved places."
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Search input (`<input class="srch" id="ls" placeholder="Search any city or place">`)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(c.bg)
                .border(1.5.dp, c.line, RoundedCornerShape(24.dp))
                .padding(horizontal = 18.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            if (searchQuery.isEmpty()) {
                Text(
                    text = "Search any city or place",
                    fontSize = 15.sp,
                    color = c.mut
                )
            }
            BasicTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                singleLine = true,
                textStyle = TextStyle(
                    fontSize = 15.sp,
                    color = c.ink
                ),
                cursorBrush = SolidColor(c.bz),
                modifier = Modifier.fillMaxWidth()
            )
        }

        // Location list (`<div id="ll"></div>`)
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            if (displayedLocations.isEmpty()) {
                item {
                    Text(
                        text = "No places found",
                        fontSize = 13.sp,
                        color = c.mut,
                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 4.dp)
                    )
                }
            } else {
                items(displayedLocations) { locItem ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectLocation(locItem) }
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
                                color = c.ink
                            )
                        }
                        HorizontalDivider(color = c.line, thickness = 1.dp)
                    }
                }
            }

            if (fallbackNote != null) {
                item {
                    Text(
                        text = fallbackNote.orEmpty(),
                        fontSize = 13.sp,
                        color = c.mut,
                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Renders any field `k` matching `fld(k)` in the HTML
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun RenderHtmlField(
    key: String,
    c: HtmlColors,
    cat: String,
    type: String,
    year: String,
    make: String,
    onMakeChange: (String) -> Unit,
    model: String,
    onModelChange: (String) -> Unit,
    trim: String,
    onTrimChange: (String) -> Unit,
    body: String,
    onBodyChange: (String) -> Unit,
    trans: String,
    onTransChange: (String) -> Unit,
    fuel: String,
    onFuelChange: (String) -> Unit,
    icol: String,
    ecol: String,
    cond: String,
    onCondChange: (String) -> Unit,
    carrier: String,
    onCarrierChange: (String) -> Unit,
    dev: String,
    onDevChange: (String) -> Unit,
    brand: String,
    onBrandChange: (String) -> Unit,
    size: String,
    onSizeChange: (String) -> Unit,
    plat: String,
    onPlatChange: (String) -> Unit,
    title: String,
    onTitleChange: (String) -> Unit,
    price: String,
    onPriceChange: (String) -> Unit,
    loc: String,
    desc: String,
    onDescChange: (String) -> Unit,
    avail: String,
    onAvailChange: (String) -> Unit,
    onOpenPick: (String) -> Unit
) {
    val label = when (key) {
        "cat" -> "Category"
        "type" -> "Vehicle type"
        "year" -> "Year"
        "make" -> "Make"
        "model" -> "Model"
        "trim" -> "Trim"
        "body" -> "Body style"
        "trans" -> "Transmission"
        "fuel" -> "Fuel type"
        "icol" -> "Interior color"
        "ecol" -> "Exterior color"
        "cond" -> "Condition"
        "carrier" -> "Carrier"
        "dev" -> "Device name"
        "brand" -> "Brand"
        "size" -> "Size"
        "plat" -> "Platform"
        "title" -> "What are you selling?"
        "price" -> "Price"
        "loc" -> "Location"
        "desc" -> "Description"
        "avail" -> "Availability"
        else -> key
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        // Label (`.lb`)
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = c.bz,
            modifier = Modifier.padding(start = 4.dp, bottom = 6.dp)
        )

        when (key) {
            "cat" -> {
                // Category Selector Button (`.cat`)
                val emoji = MESKOT_HTML_CATS.find { it.first == cat }?.second ?: "🪟"
                Surface(
                    onClick = { onOpenPick("cat") },
                    shape = RoundedCornerShape(20.dp),
                    color = c.tint,
                    border = BorderStroke(1.5.dp, c.line),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("sell_category_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(c.paper),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = emoji, fontSize = 26.sp)
                        }
                        Text(
                            text = cat.ifEmpty { "Choose a category" },
                            fontSize = 16.sp,
                            fontWeight = if (cat.isNotEmpty()) FontWeight.SemiBold else FontWeight.Medium,
                            color = if (cat.isNotEmpty()) c.ink else c.mut,
                            modifier = Modifier.weight(1f)
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = c.ink,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            "loc" -> {
                // Location Selector Button (`.sl[data-act="loc"]`)
                Surface(
                    onClick = { onOpenPick("loc") },
                    shape = RoundedCornerShape(16.dp),
                    color = c.bg,
                    border = BorderStroke(1.5.dp, c.line),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 50.dp)
                        .testTag("sell_location_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "📍 $loc",
                            fontSize = 16.sp,
                            color = c.ink
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = c.g2,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            "type", "year", "icol", "ecol" -> {
                val currentVal = when (key) {
                    "type" -> type
                    "year" -> year
                    "icol" -> icol
                    "ecol" -> ecol
                    else -> ""
                }
                val swatchItem = if (key == "icol" || key == "ecol") {
                    MESKOT_HTML_COL.find { it.name == currentVal }
                } else null

                Surface(
                    onClick = { onOpenPick(key) },
                    shape = RoundedCornerShape(16.dp),
                    color = c.bg,
                    border = BorderStroke(1.5.dp, c.line),
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            if (swatchItem != null) {
                                Box(
                                    modifier = Modifier
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(swatchItem.color)
                                        .border(1.dp, c.line, CircleShape)
                                )
                            }
                            Text(
                                text = currentVal.ifEmpty { "Select" },
                                fontSize = 16.sp,
                                color = if (currentVal.isNotEmpty()) c.ink else c.mut
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = null,
                            tint = c.g2,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            "body", "trans", "fuel", "cond", "dev", "plat", "avail" -> {
                val (chipsList, selectedVal, onSelect) = when (key) {
                    "body" -> Triple(BODY_CHIPS, body, onBodyChange)
                    "trans" -> Triple(TRANS_CHIPS, trans, onTransChange)
                    "fuel" -> Triple(FUEL_CHIPS, fuel, onFuelChange)
                    "cond" -> Triple(COND_CHIPS, cond, onCondChange)
                    "dev" -> Triple(DEV_CHIPS, dev, onDevChange)
                    "plat" -> Triple(PLAT_CHIPS, plat, onPlatChange)
                    else -> Triple(AVAIL_CHIPS, avail, onAvailChange)
                }
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    chipsList.forEach { option ->
                        val isOn = selectedVal == option
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(20.dp))
                                .then(
                                    if (isOn) Modifier.background(c.goldGrad)
                                    else Modifier
                                        .background(c.bg)
                                        .border(1.5.dp, c.line, RoundedCornerShape(20.dp))
                                )
                                .clickable { onSelect(option) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = option,
                                fontSize = 14.sp,
                                fontWeight = if (isOn) FontWeight.SemiBold else FontWeight.Medium,
                                color = if (isOn) Color.White else c.ink
                            )
                        }
                    }
                }
            }

            "price" -> {
                // Price input with `$` prefix (`.pre`)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(c.bg)
                        .border(1.5.dp, c.line, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "$",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = c.bz,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                        BasicTextField(
                            value = price,
                            onValueChange = { input ->
                                onPriceChange(input.filter { ch -> ch.isDigit() || ch == '.' })
                            },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            textStyle = TextStyle(
                                fontSize = 16.sp,
                                color = c.ink
                            ),
                            cursorBrush = SolidColor(c.bz),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sell_price_input")
                        )
                    }
                }
            }

            "desc" -> {
                // Textarea (`textarea.in`)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(c.bg)
                        .border(1.5.dp, c.line, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp, vertical = 12.dp)
                ) {
                    BasicTextField(
                        value = desc,
                        onValueChange = onDescChange,
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = c.ink
                        ),
                        cursorBrush = SolidColor(c.bz),
                        modifier = Modifier
                            .fillMaxSize()
                            .testTag("sell_description_input")
                    )
                }
            }

            else -> {
                // Standard text input (`input.in`)
                val (textVal, onValChange) = when (key) {
                    "make" -> make to onMakeChange
                    "model" -> model to onModelChange
                    "trim" -> trim to onTrimChange
                    "carrier" -> carrier to onCarrierChange
                    "brand" -> brand to onBrandChange
                    "size" -> size to onSizeChange
                    else -> title to onTitleChange
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(c.bg)
                        .border(1.5.dp, c.line, RoundedCornerShape(16.dp))
                        .padding(horizontal = 16.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = textVal,
                        onValueChange = onValChange,
                        singleLine = true,
                        textStyle = TextStyle(
                            fontSize = 16.sp,
                            color = c.ink
                        ),
                        cursorBrush = SolidColor(c.bz),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag(if (key == "title") "sell_title_input" else "sell_${key}_input")
                    )
                }
            }
        }
    }
}

/**
 * Toggle Row matching `.tg` and `.sw` in the HTML
 */
@Composable
private fun HtmlToggleRow(
    c: HtmlColors,
    title: String,
    subtitle: String,
    linkText: String? = null,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isFirst: Boolean
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        if (!isFirst) {
            HorizontalDivider(color = c.line, thickness = 1.dp)
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = if (isFirst) 0.dp else 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = c.ink
                )
                Text(
                    text = subtitle,
                    fontSize = 13.sp,
                    color = c.mut
                )
                if (linkText != null) {
                    Text(
                        text = linkText,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = c.bz,
                        textDecoration = TextDecoration.Underline
                    )
                }
            }

            // Custom Gold Gradient Switch matching `.sw` (50dp x 30dp)
            Box(
                modifier = Modifier
                    .width(50.dp)
                    .height(30.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .then(
                        if (checked) Modifier.background(c.goldGrad)
                        else Modifier.background(c.line)
                    )
                    .clickable { onCheckedChange(!checked) }
                    .padding(3.dp),
                contentAlignment = if (checked) Alignment.CenterEnd else Alignment.CenterStart
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .shadow(2.dp, CircleShape)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
        }
    }
}

/**
 * Card matching `.card` and `.card:before` in the HTML
 */
@Composable
private fun HtmlCard(
    c: HtmlColors,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = c.paper,
        border = BorderStroke(1.dp, c.line),
        shadowElevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // `.card:before` 6px gold gradient top bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(c.goldHoriz)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 18.dp, top = 16.dp, bottom = 18.dp),
                content = content
            )
        }
    }
}
