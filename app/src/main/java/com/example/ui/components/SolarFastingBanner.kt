package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.badi.BadiCalendarEngine
import com.example.badi.BadiDate
import com.example.devotional.CityLocation
import com.example.devotional.DevotionalRepository
import com.example.ui.theme.HolyDayGold
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun SolarFastingBanner(
    currentBadiDate: BadiDate,
    currentLocation: CityLocation,
    onSelectLocation: (CityLocation) -> Unit,
    onOpenFastingPrayers: () -> Unit,
    modifier: Modifier = Modifier
) {
    val targetGregorianDate = currentBadiDate.gregorianDate
    val isToday = targetGregorianDate == LocalDate.now()

    // Live ticking clock state if today, otherwise static for selected date
    var currentTime by remember(targetGregorianDate) { mutableStateOf(LocalTime.now()) }
    var showLocationPicker by remember { mutableStateOf(false) }

    LaunchedEffect(targetGregorianDate) {
        if (targetGregorianDate == LocalDate.now()) {
            while (true) {
                currentTime = LocalTime.now()
                delay(1000)
            }
        }
    }

    val zoneId = remember(currentLocation) {
        try {
            ZoneId.of(currentLocation.timeZoneId)
        } catch (_: Exception) {
            ZoneId.systemDefault()
        }
    }

    val sunsetTime = remember(targetGregorianDate, currentLocation) {
        BadiCalendarEngine.calculateSunset(
            date = targetGregorianDate,
            latitude = currentLocation.latitude,
            longitude = currentLocation.longitude,
            zoneId = zoneId
        )
    }

    val sunriseTime = remember(targetGregorianDate, currentLocation) {
        BadiCalendarEngine.calculateSunrise(
            date = targetGregorianDate,
            latitude = currentLocation.latitude,
            longitude = currentLocation.longitude,
            zoneId = zoneId
        )
    }

    val isAfterSunset = currentBadiDate.isAfterSunset || (isToday && currentTime.isAfter(sunsetTime))
    val isFastingMonth = currentBadiDate.month == 19 // 'Alá' (The Fast)

    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm:ss a", Locale.ENGLISH) }
    val shortTimeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("solar_fasting_banner"),
        colors = CardDefaults.cardColors(
            containerColor = if (isAfterSunset) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            }
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Row 1: Live Real-Time Clock & Location Selector Chip
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (isAfterSunset) Color(0xFF81D4FA) else HolyDayGold)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = currentTime.format(timeFormatter),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Location selector pill
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .clickable { showLocationPicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = currentLocation.cityName.split(" / ").first(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Row 2: Before / After Sunset Indicator Badge & Solar Times
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sunset State Badge
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = if (isAfterSunset) {
                        Color(0xFF3949AB).copy(alpha = 0.2f)
                    } else {
                        HolyDayGold.copy(alpha = 0.2f)
                    }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isAfterSunset) Icons.Default.Nightlight else Icons.Default.WbSunny,
                            contentDescription = null,
                            tint = if (isAfterSunset) Color(0xFF5C6BC0) else HolyDayGold,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isAfterSunset) "🌙 AFTER SUNSET" else "☀️ BEFORE SUNSET",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (isAfterSunset) MaterialTheme.colorScheme.primary else HolyDayGold
                        )
                    }
                }

                // Solar times: Sunrise & Sunset
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🌅 ${sunriseTime.format(shortTimeFormatter)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("•", color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        text = "🌇 ${sunsetTime.format(shortTimeFormatter)}",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Row 3: Sunset Countdown / Detail Explanation
            Spacer(modifier = Modifier.height(6.dp))
            val detailMessage = if (!isAfterSunset) {
                val diffMinutes = Duration.between(currentTime, sunsetTime).toMinutes()
                val hours = diffMinutes / 60
                val mins = diffMinutes % 60
                "Badí' day changes at sunset (in ${hours}h ${mins}m). Daytime activities in effect."
            } else {
                "Evening begun at sunset (${sunsetTime.format(shortTimeFormatter)}). Next Badí' date is now active!"
            }

            Text(
                text = detailMessage,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Row 4: FASTING TRACKER (During Month of 'Alá' or clickable)
            if (isFastingMonth || currentBadiDate.month == 19) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(8.dp))

                val isDuringFastHours = currentTime.isAfter(sunriseTime) && currentTime.isBefore(sunsetTime)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "✨ Nineteen Day Fast ('Alá')",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = HolyDayGold
                            )
                        }
                        Text(
                            text = if (isDuringFastHours) {
                                val remaining = Duration.between(currentTime, sunsetTime).toMinutes()
                                "Fasting active • ${remaining / 60}h ${remaining % 60}m to Iftar"
                            } else if (currentTime.isBefore(sunriseTime)) {
                                val startIn = Duration.between(currentTime, sunriseTime).toMinutes()
                                "Fast begins at sunrise (${startIn / 60}h ${startIn % 60}m)"
                            } else {
                                "Fast ended for today at sunset"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    TextButton(
                        onClick = onOpenFastingPrayers,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                        modifier = Modifier.height(30.dp)
                    ) {
                        Text(
                            text = "Fasting Prayers",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    // Location Picker Dialog
    if (showLocationPicker) {
        Dialog(onDismissRequest = { showLocationPicker = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.75f)
                    .padding(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Location",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showLocationPicker = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text(
                        text = "Used for precise sunset, sunrise, fasting times, and Qiblih direction.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    androidx.compose.foundation.lazy.LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(DevotionalRepository.PRESET_LOCATIONS.size) { index ->
                            val loc = DevotionalRepository.PRESET_LOCATIONS[index]
                            val isSelected = loc.cityName == currentLocation.cityName

                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onSelectLocation(loc)
                                        showLocationPicker = false
                                    },
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isSelected) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                    }
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(
                                            text = loc.cityName,
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                        Text(
                                            text = "${loc.country} • ${loc.latitude.toInt()}°, ${loc.longitude.toInt()}°",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (isSelected) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
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
