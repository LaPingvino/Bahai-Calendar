package com.example.location

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.example.devotional.CityLocation
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import java.util.TimeZone

object LocationHelper {

    private const val PREFS_NAME = "badi_location_prefs"
    private const val KEY_CITY = "key_city"
    private const val KEY_COUNTRY = "key_country"
    private const val KEY_LAT = "key_lat"
    private const val KEY_LON = "key_lon"
    private const val KEY_TIMEZONE = "key_timezone"

    fun hasLocationPermission(context: Context): Boolean {
        val fineGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseGranted = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineGranted || coarseGranted
    }

    fun saveLocation(context: Context, location: CityLocation) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_CITY, location.cityName)
            .putString(KEY_COUNTRY, location.country)
            .putFloat(KEY_LAT, location.latitude.toFloat())
            .putFloat(KEY_LON, location.longitude.toFloat())
            .putString(KEY_TIMEZONE, location.timeZoneId)
            .apply()
    }

    fun getSavedLocation(context: Context): CityLocation {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val city = prefs.getString(KEY_CITY, null)
        val country = prefs.getString(KEY_COUNTRY, null)
        val tz = prefs.getString(KEY_TIMEZONE, null) ?: TimeZone.getDefault().id

        if (city != null && prefs.contains(KEY_LAT) && prefs.contains(KEY_LON)) {
            val lat = prefs.getFloat(KEY_LAT, 0f)
            val lon = prefs.getFloat(KEY_LON, 0f)
            return CityLocation(
                cityName = city,
                country = country ?: "",
                latitude = lat.toDouble(),
                longitude = lon.toDouble(),
                timeZoneId = tz
            )
        }

        // Default smart fallback based on device TimeZone
        return getDefaultLocationForTimeZone(tz)
    }

    internal fun getDefaultLocationForTimeZone(tzId: String): CityLocation {
        return when {
            tzId.contains("Los_Angeles", ignoreCase = true) || tzId.contains("Vancouver", ignoreCase = true) ||
                tzId.equals("US/Pacific", ignoreCase = true) || tzId.equals("Canada/Pacific", ignoreCase = true) || tzId.equals("PST8PDT", ignoreCase = true) ->
                CityLocation("San Francisco", "USA", 37.7749, -122.4194, tzId)
            tzId.contains("Denver", ignoreCase = true) || tzId.contains("Phoenix", ignoreCase = true) ||
                tzId.equals("US/Mountain", ignoreCase = true) || tzId.equals("Canada/Mountain", ignoreCase = true) || tzId.equals("MST7MDT", ignoreCase = true) ->
                CityLocation("Denver", "USA", 39.7392, -104.9903, tzId)
            tzId.contains("Chicago", ignoreCase = true) || tzId.contains("Mexico_City", ignoreCase = true) ||
                tzId.equals("US/Central", ignoreCase = true) || tzId.equals("Canada/Central", ignoreCase = true) || tzId.equals("CST6CDT", ignoreCase = true) ->
                CityLocation("Chicago", "USA", 41.8781, -87.6298, tzId)
            tzId.contains("New_York", ignoreCase = true) || tzId.contains("Toronto", ignoreCase = true) ||
                tzId.equals("US/Eastern", ignoreCase = true) || tzId.equals("Canada/Eastern", ignoreCase = true) || tzId.equals("EST5EDT", ignoreCase = true) ->
                CityLocation("New York", "USA", 40.7128, -74.0060, tzId)
            tzId.contains("Honolulu", ignoreCase = true) ->
                CityLocation("Honolulu", "USA", 21.3069, -157.8583, tzId)
            tzId.contains("Sao_Paulo", ignoreCase = true) || tzId.contains("Buenos_Aires", ignoreCase = true) ->
                CityLocation("São Paulo", "Brazil", -23.5505, -46.6333, tzId)
            tzId.contains("London", ignoreCase = true) || tzId.contains("Dublin", ignoreCase = true) || tzId.contains("GMT", ignoreCase = true) || tzId.equals("UTC", ignoreCase = true) || tzId.equals("Etc/UTC", ignoreCase = true) ->
                CityLocation("London", "United Kingdom", 51.5074, -0.1278, tzId)
            tzId.contains("Amsterdam", ignoreCase = true) || tzId.contains("Brussels", ignoreCase = true) ->
                CityLocation("Amsterdam", "Netherlands", 52.3676, 4.9041, tzId)
            tzId.contains("Paris", ignoreCase = true) || tzId.contains("Madrid", ignoreCase = true) ->
                CityLocation("Paris", "France", 48.8566, 2.3522, tzId)
            tzId.contains("Berlin", ignoreCase = true) || tzId.contains("Zurich", ignoreCase = true) || tzId.contains("Rome", ignoreCase = true) || tzId.contains("Vienna", ignoreCase = true) || tzId.contains("Stockholm", ignoreCase = true) || tzId.contains("Warsaw", ignoreCase = true) ->
                CityLocation("Frankfurt", "Germany", 50.1109, 8.6821, tzId)
            tzId.contains("Jerusalem", ignoreCase = true) || tzId.contains("Tel_Aviv", ignoreCase = true) ->
                CityLocation("Haifa / Acre", "Holy Land", 32.8191, 34.9983, tzId)
            tzId.contains("Tehran", ignoreCase = true) ->
                CityLocation("Tehran", "Iran", 35.6892, 51.3890, tzId)
            tzId.contains("Kolkata", ignoreCase = true) || tzId.contains("Calcutta", ignoreCase = true) ->
                CityLocation("New Delhi", "India", 28.5535, 77.2588, tzId)
            tzId.contains("Tokyo", ignoreCase = true) || tzId.contains("Japan", ignoreCase = true) || tzId.contains("Seoul", ignoreCase = true) ->
                CityLocation("Tokyo", "Japan", 35.6762, 139.6503, tzId)
            tzId.contains("Sydney", ignoreCase = true) || tzId.contains("Melbourne", ignoreCase = true) || tzId.contains("Australia", ignoreCase = true) ->
                CityLocation("Sydney", "Australia", -33.8688, 151.2093, tzId)
            tzId.contains("Auckland", ignoreCase = true) ->
                CityLocation("Auckland", "New Zealand", -36.8485, 174.7633, tzId)
            else -> {
                // Derive approximate longitude from timezone's standard UTC offset so solar times
                // match the local timezone instead of prematurely flipping the Badí' date.
                val tz = TimeZone.getTimeZone(tzId)
                val offsetHours = tz.rawOffset / 3_600_000.0
                val approxLon = (offsetHours * 15.0).coerceIn(-180.0, 180.0)
                val approxLat = if (tzId.startsWith("Australia/") || tzId.startsWith("Antarctica/") || tzId.startsWith("Pacific/Auckland")) -33.0 else 35.0
                val cityLabel = tzId.substringAfterLast('/').replace('_', ' ').ifBlank { "Current Location" }
                CityLocation(cityLabel, tz.displayName, approxLat, approxLon, tzId)
            }
        }
    }

    suspend fun getCurrentOrLastKnownLocation(context: Context): CityLocation? = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            return@withContext null
        }

        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                ?: return@withContext null

            var bestLocation: Location? = null

            val providers = locationManager.getProviders(true)
            for (provider in providers) {
                try {
                    val loc = locationManager.getLastKnownLocation(provider)
                    if (loc != null) {
                        if (bestLocation == null || loc.accuracy < (bestLocation.accuracy)) {
                            bestLocation = loc
                        }
                    }
                } catch (_: SecurityException) {
                    // Handled
                }
            }

            if (bestLocation == null) {
                return@withContext null
            }

            val lat = bestLocation.latitude
            val lon = bestLocation.longitude
            val timeZoneId = TimeZone.getDefault().id

            // Reverse geocode to get City and Country name
            var cityName = ""
            var country = ""

            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                val addresses = geocoder.getFromLocation(lat, lon, 1)
                if (!addresses.isNullOrEmpty()) {
                    val address = addresses[0]
                    val locality = address.locality ?: address.subAdminArea ?: address.adminArea
                    val countryName = address.countryName ?: ""
                    if (!locality.isNullOrBlank()) {
                        cityName = locality
                    }
                    if (countryName.isNotBlank()) {
                        country = countryName
                    }
                }
            } catch (_: Exception) {
                // If geocoder fails, fallback to formatted coordinates
                cityName = String.format(Locale.US, "%.2f°, %.2f°", lat, lon)
            }

            if (cityName.isBlank()) {
                cityName = "Current Location"
            }

            val result = CityLocation(
                cityName = cityName,
                country = country.ifBlank { "Current Location" },
                latitude = lat,
                longitude = lon,
                timeZoneId = timeZoneId
            )

            // Save to prefs
            saveLocation(context, result)

            result
        } catch (_: Exception) {
            null
        }
    }
}
