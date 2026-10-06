package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlin.coroutines.resume

/**
 * Represents a real geographical place resolved from Google Location Services / Geocoder / OpenStreetMap.
 */
data class RealPlaceResult(
    val name: String,
    val fullAddress: String,
    val latitude: Double,
    val longitude: Double,
    val city: String = "",
    val country: String = ""
) {
    val displayLabel: String
        get() = when {
            name.isNotBlank() && city.isNotBlank() && !name.contains(city, ignoreCase = true) -> "$name, $city"
            name.isNotBlank() -> name
            fullAddress.isNotBlank() -> fullAddress
            else -> String.format(Locale.US, "%.4f, %.4f", latitude, longitude)
        }
}

/**
 * Facebook-style Real Location & Google Maps Integration Helper for Meskot.
 *
 * Capabilities:
 * 1. Real GPS Location Detection via Google Play Services `FusedLocationProviderClient`.
 * 2. Reverse Geocoding (Coordinates -> Real Neighborhood/City Name) via Android `Geocoder` + Nominatim fallback.
 * 3. Forward Place Search (Query -> Real Places with Lat/Lng) via Android `Geocoder` + OpenStreetMap Nominatim API.
 * 4. Interactive Map Preview & Direct "Open in Google Maps" Intent launcher.
 */
object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * Fetches the device's real current GPS coordinates using Google Play Services FusedLocationProviderClient
     * and reverse-geocodes it into a human-readable place name (like Facebook Check-In / Marketplace).
     */
    @SuppressLint("MissingPermission")
    suspend fun fetchCurrentDevicePlace(context: Context): RealPlaceResult? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) return@withContext null

        val fusedClient = LocationServices.getFusedLocationProviderClient(context)

        // Try current high-accuracy location first
        val location = suspendCancellableCoroutine<android.location.Location?> { cont ->
            val cts = CancellationTokenSource()
            fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                .addOnSuccessListener { loc ->
                    if (loc != null) {
                        cont.resume(loc)
                    } else {
                        // Fallback to lastLocation if current fix is null
                        fusedClient.lastLocation
                            .addOnSuccessListener { lastLoc -> cont.resume(lastLoc) }
                            .addOnFailureListener { cont.resume(null) }
                    }
                }
                .addOnFailureListener {
                    fusedClient.lastLocation
                        .addOnSuccessListener { lastLoc -> cont.resume(lastLoc) }
                        .addOnFailureListener { cont.resume(null) }
                }
        }

        if (location != null) {
            reverseGeocode(context, location.latitude, location.longitude)
        } else {
            null
        }
    }

    /**
     * Reverse-geocodes latitude & longitude into a real neighborhood/city name.
     */
    suspend fun reverseGeocode(
        context: Context,
        latitude: Double,
        longitude: Double
    ): RealPlaceResult = withContext(Dispatchers.IO) {
        // 1. Try Android native Geocoder (powered by Google Play Services on device)
        try {
            if (Geocoder.isPresent()) {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                val addr = addresses?.firstOrNull()
                if (addr != null) {
                    val subLocality = addr.subLocality ?: addr.featureName ?: ""
                    val locality = addr.locality ?: addr.subAdminArea ?: addr.adminArea ?: ""
                    val country = addr.countryName ?: ""
                    val primaryName = when {
                        subLocality.isNotBlank() && locality.isNotBlank() && subLocality != locality ->
                            "$subLocality, $locality"
                        locality.isNotBlank() -> locality
                        subLocality.isNotBlank() -> subLocality
                        country.isNotBlank() -> country
                        else -> String.format(Locale.US, "%.4f, %.4f", latitude, longitude)
                    }
                    val fullLine = (0..addr.maxAddressLineIndex)
                        .mapNotNull { addr.getAddressLine(it) }
                        .joinToString(", ")
                        .ifBlank { "$primaryName, $country".trim(',', ' ') }

                    return@withContext RealPlaceResult(
                        name = primaryName,
                        fullAddress = fullLine,
                        latitude = latitude,
                        longitude = longitude,
                        city = locality,
                        country = country
                    )
                }
            }
        } catch (_: Exception) {
            // Fall through to network geocoder
        }

        // 2. Fallback to live Nominatim reverse geocoding API
        try {
            val urlStr = "https://nominatim.openstreetmap.org/reverse?format=jsonv2&lat=$latitude&lon=$longitude&zoom=14&addressdetails=1"
            val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", "MeskotAndroidApp/1.0")
                setRequestProperty("Accept-Language", "en")
                connectTimeout = 6000
                readTimeout = 6000
            }
            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(body)
                val addressObj = json.optJSONObject("address")
                val suburb = addressObj?.optString("suburb").orEmpty()
                    .ifBlank { addressObj?.optString("neighbourhood").orEmpty() }
                val city = addressObj?.optString("city").orEmpty()
                    .ifBlank { addressObj?.optString("town").orEmpty() }
                    .ifBlank { addressObj?.optString("state").orEmpty() }
                val country = addressObj?.optString("country").orEmpty()
                val displayName = json.optString("display_name").orEmpty()

                val shortName = when {
                    suburb.isNotBlank() && city.isNotBlank() -> "$suburb, $city"
                    city.isNotBlank() && country.isNotBlank() -> "$city, $country"
                    city.isNotBlank() -> city
                    displayName.isNotBlank() -> displayName.split(",").take(2).joinToString(",").trim()
                    else -> String.format(Locale.US, "%.4f, %.4f", latitude, longitude)
                }

                return@withContext RealPlaceResult(
                    name = shortName,
                    fullAddress = displayName.ifBlank { shortName },
                    latitude = latitude,
                    longitude = longitude,
                    city = city,
                    country = country
                )
            }
        } catch (_: Exception) {
        }

        RealPlaceResult(
            name = String.format(Locale.US, "%.4f, %.4f", latitude, longitude),
            fullAddress = String.format(Locale.US, "Lat %.4f, Lng %.4f", latitude, longitude),
            latitude = latitude,
            longitude = longitude
        )
    }

    /**
     * Searches real-world places, neighborhoods, cities, and landmarks matching [query]
     * using Android Geocoder and live OpenStreetMap / Nominatim search.
     */
    suspend fun searchPlaces(context: Context, query: String): List<RealPlaceResult> = withContext(Dispatchers.IO) {
        val trimmed = query.trim()
        if (trimmed.length < 2) return@withContext defaultVerifiedPlaces()

        val results = mutableListOf<RealPlaceResult>()

        // 1. Live network search for rich place names & coordinates
        try {
            val encoded = URLEncoder.encode(trimmed, "UTF-8")
            val urlStr = "https://nominatim.openstreetmap.org/search?q=$encoded&format=jsonv2&addressdetails=1&limit=8"
            val conn = (URL(urlStr).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                setRequestProperty("User-Agent", "MeskotAndroidApp/1.0")
                setRequestProperty("Accept-Language", "en")
                connectTimeout = 6000
                readTimeout = 6000
            }
            if (conn.responseCode == 200) {
                val body = conn.inputStream.bufferedReader().use { it.readText() }
                val arr = JSONArray(body)
                for (i in 0 until arr.length()) {
                    val item = arr.getJSONObject(i)
                    val lat = item.optString("lat").toDoubleOrNull() ?: continue
                    val lon = item.optString("lon").toDoubleOrNull() ?: continue
                    val displayName = item.optString("display_name")
                    val nameProp = item.optString("name")
                    val addr = item.optJSONObject("address")
                    val city = addr?.optString("city").orEmpty()
                        .ifBlank { addr?.optString("town").orEmpty() }
                        .ifBlank { addr?.optString("state").orEmpty() }
                    val country = addr?.optString("country").orEmpty()

                    val primary = when {
                        nameProp.isNotBlank() && city.isNotBlank() && !nameProp.equals(city, ignoreCase = true) ->
                            "$nameProp, $city"
                        nameProp.isNotBlank() && country.isNotBlank() ->
                            "$nameProp, $country"
                        displayName.isNotBlank() ->
                            displayName.split(",").take(2).joinToString(",").trim()
                        else -> trimmed
                    }

                    results.add(
                        RealPlaceResult(
                            name = primary,
                            fullAddress = displayName.ifBlank { primary },
                            latitude = lat,
                            longitude = lon,
                            city = city,
                            country = country
                        )
                    )
                }
            }
        } catch (_: Exception) {
        }

        // 2. Also query Android native Geocoder if network returned empty
        if (results.isEmpty()) {
            try {
                if (Geocoder.isPresent()) {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    @Suppress("DEPRECATION")
                    val addresses = geocoder.getFromLocationName(trimmed, 6).orEmpty()
                    addresses.forEach { addr ->
                        if (addr.hasLatitude() && addr.hasLongitude()) {
                            val locality = addr.locality ?: addr.subAdminArea ?: addr.featureName ?: trimmed
                            val country = addr.countryName ?: ""
                            val label = if (country.isNotBlank() && !locality.contains(country, ignoreCase = true)) {
                                "$locality, $country"
                            } else {
                                locality
                            }
                            val fullLine = (0..addr.maxAddressLineIndex)
                                .mapNotNull { addr.getAddressLine(it) }
                                .joinToString(", ")
                                .ifBlank { label }
                            results.add(
                                RealPlaceResult(
                                    name = label,
                                    fullAddress = fullLine,
                                    latitude = addr.latitude,
                                    longitude = addr.longitude,
                                    city = locality,
                                    country = country
                                )
                            )
                        }
                    }
                }
            } catch (_: Exception) {
            }
        }

        // 3. Include matching verified landmarks if still few results
        if (results.size < 3) {
            val matchingPresets = defaultVerifiedPlaces().filter {
                it.name.contains(trimmed, ignoreCase = true) ||
                        it.fullAddress.contains(trimmed, ignoreCase = true)
            }
            results.addAll(matchingPresets)
        }

        results.distinctBy { "${it.name}_${String.format(Locale.US, "%.3f", it.latitude)}" }
    }

    /**
     * Opens the official Google Maps app (or browser fallback) pinned to the exact coordinates/place name,
     * just like tapping a location check-in or Marketplace map on Facebook.
     */
    fun openInGoogleMaps(
        context: Context,
        latitude: Double?,
        longitude: Double?,
        placeName: String
    ) {
        val encodedLabel = Uri.encode(placeName.ifBlank { "Selected Location" })
        val geoUri = if (latitude != null && longitude != null && (latitude != 0.0 || longitude != 0.0)) {
            Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encodedLabel)")
        } else {
            Uri.parse("geo:0,0?q=$encodedLabel")
        }

        val mapIntent = Intent(Intent.ACTION_VIEW, geoUri).apply {
            setPackage("com.google.android.apps.maps")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
                return
            }
        } catch (_: Exception) {
        }

        // Fallback to universal Google Maps search URL in browser or any map handler
        val webUrl = if (latitude != null && longitude != null && (latitude != 0.0 || longitude != 0.0)) {
            "https://www.google.com/maps/search/?api=1&query=$latitude,$longitude"
        } else {
            "https://www.google.com/maps/search/?api=1&query=$encodedLabel"
        }
        try {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
        } catch (_: Exception) {
        }
    }

    fun defaultVerifiedPlaces(): List<RealPlaceResult> = listOf(
        RealPlaceResult(
            name = "Bole, Addis Ababa",
            fullAddress = "Bole Sub-City, Addis Ababa, Ethiopia",
            latitude = 8.9980,
            longitude = 38.7890,
            city = "Addis Ababa",
            country = "Ethiopia"
        ),
        RealPlaceResult(
            name = "Meskel Square, Addis Ababa",
            fullAddress = "Meskel Square, Kirkos, Addis Ababa, Ethiopia",
            latitude = 9.0104,
            longitude = 38.7612,
            city = "Addis Ababa",
            country = "Ethiopia"
        ),
        RealPlaceResult(
            name = "Kazanchis, Addis Ababa",
            fullAddress = "Kazanchis Business District, Addis Ababa, Ethiopia",
            latitude = 9.0192,
            longitude = 38.7665,
            city = "Addis Ababa",
            country = "Ethiopia"
        ),
        RealPlaceResult(
            name = "Piassa, Addis Ababa",
            fullAddress = "Arada (Piassa), Addis Ababa, Ethiopia",
            latitude = 9.0331,
            longitude = 38.7501,
            city = "Addis Ababa",
            country = "Ethiopia"
        ),
        RealPlaceResult(
            name = "CMC, Addis Ababa",
            fullAddress = "CMC Michael, Yeka, Addis Ababa, Ethiopia",
            latitude = 9.0205,
            longitude = 38.8350,
            city = "Addis Ababa",
            country = "Ethiopia"
        ),
        RealPlaceResult(
            name = "Mekelle, Ethiopia",
            fullAddress = "Mekelle, Tigray, Ethiopia",
            latitude = 13.4967,
            longitude = 39.4753,
            city = "Mekelle",
            country = "Ethiopia"
        ),
        RealPlaceResult(
            name = "Hawassa, Ethiopia",
            fullAddress = "Hawassa, Sidama, Ethiopia",
            latitude = 7.0504,
            longitude = 38.4955,
            city = "Hawassa",
            country = "Ethiopia"
        ),
        RealPlaceResult(
            name = "Bahir Dar, Ethiopia",
            fullAddress = "Bahir Dar, Amhara, Ethiopia",
            latitude = 11.5936,
            longitude = 37.3908,
            city = "Bahir Dar",
            country = "Ethiopia"
        ),
        RealPlaceResult(
            name = "Adama, Ethiopia",
            fullAddress = "Adama (Nazret), Oromia, Ethiopia",
            latitude = 8.5400,
            longitude = 39.2700,
            city = "Adama",
            country = "Ethiopia"
        ),
        RealPlaceResult(
            name = "Asmara, Eritrea",
            fullAddress = "Asmara, Maekel, Eritrea",
            latitude = 15.3229,
            longitude = 38.9251,
            city = "Asmara",
            country = "Eritrea"
        ),
        RealPlaceResult(
            name = "Washington, D.C.",
            fullAddress = "Washington, District of Columbia, United States",
            latitude = 38.9072,
            longitude = -77.0369,
            city = "Washington",
            country = "United States"
        )
    )
}
