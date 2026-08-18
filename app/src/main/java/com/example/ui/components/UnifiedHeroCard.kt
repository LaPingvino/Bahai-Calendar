package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.badi.BadiCalendarEngine
import com.example.badi.BadiDate
import com.example.badi.ElementalTimeEngine
import com.example.badi.ElementalTimeSeason
import com.example.badi.TimeSystemMode
import com.example.devotional.CityLocation
import com.example.devotional.DevotionalRepository
import com.example.ui.YearNotationSystem
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.FeastTeal
import com.example.ui.theme.HolyDayGold
import com.example.ui.theme.LavenderPrimary
import kotlinx.coroutines.delay
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun UnifiedHeroCard(
    selectedDate: BadiDate,
    todayDate: BadiDate,
    isEveningMode: Boolean,
    yearNotationSystem: YearNotationSystem,
    currentLocation: CityLocation,
    hasLocationPermission: Boolean,
    isDetectingLocation: Boolean,
    onRequestLocationPermission: () -> Unit,
    onDetectLocation: () -> Unit,
    onSelectLocation: (CityLocation) -> Unit,
    onOpenYearPicker: () -> Unit,
    onToggleNotationSystem: () -> Unit,
    onOpenFastingPrayers: () -> Unit,
    timeSystemMode: TimeSystemMode = TimeSystemMode.STANDARD_CIVIL,
    onOpenTimeSystemPicker: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val isToday = selectedDate.day == todayDate.day &&
            selectedDate.month == todayDate.month &&
            selectedDate.year == todayDate.year

    val targetGregorianDate = selectedDate.gregorianDate

    // Real-time ticking clock if today, otherwise static for selected date
    var currentTime by remember(targetGregorianDate) { mutableStateOf(LocalTime.now()) }
    var currentInstant by remember(targetGregorianDate) { mutableStateOf(Instant.now()) }
    var showLocationPicker by remember { mutableStateOf(false) }

    LaunchedEffect(targetGregorianDate) {
        if (targetGregorianDate == LocalDate.now()) {
            while (true) {
                currentTime = LocalTime.now()
                currentInstant = Instant.now()
                delay(1000)
            }
        }
    }

    val elementalTime = remember(currentInstant) {
        ElementalTimeEngine.calculate(currentInstant)
    }

    val badiSeason = remember(selectedDate.month) {
        ElementalTimeEngine.getBadiSeasonForMonth(selectedDate.month)
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

    val isAfterSunset = isEveningMode || selectedDate.isAfterSunset || (isToday && currentTime.isAfter(sunsetTime))
    val isFastingMonth = selectedDate.month == 19 // 'Alá' (The Fast)

    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm:ss a", Locale.ENGLISH) }
    val shortTimeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH) }
    val gregFormatter = remember { DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.ENGLISH) }

    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isAfterSunset) {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)
            } else {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            }
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag("unified_hero_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Row 1: Badí' Weekday & Season on Left, Time Mode & Clock on Right
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badí' Weekday + Gregorian day
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(badiSeason.emoji, fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    val weekdayDisplay = if (selectedDate.isAfterSunset) {
                        "${selectedDate.weekdayInfo.transliteration} (${selectedDate.weekdayInfo.translation}) • ${selectedDate.civilWeekdayInfo.gregorianDayName} eve"
                    } else {
                        "${selectedDate.weekdayInfo.transliteration} (${selectedDate.weekdayInfo.translation}) • ${selectedDate.civilWeekdayInfo.gregorianDayName}"
                    }
                    Text(
                        text = weekdayDisplay,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Live Clock & Time System Chip (Clickable to switch time mode)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.6f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)),
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onOpenTimeSystemPicker() }
                        .testTag("hero_clock_chip")
                ) {
                    Text(
                        text = when (timeSystemMode) {
                            TimeSystemMode.STANDARD_CIVIL -> currentTime.format(timeFormatter)
                            TimeSystemMode.ELEMENTAL_ETIME -> "${elementalTime.blockHour}:${String.format(Locale.US, "%02d:%02d", elementalTime.minute, elementalTime.second)} ${elementalTime.season.emoji}"
                            TimeSystemMode.DUAL_DISPLAY -> "${currentTime.format(shortTimeFormatter)} • ${elementalTime.blockHour}:${String.format(Locale.US, "%02d", elementalTime.minute)} ${elementalTime.season.emoji}"
                        },
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            // Quick 4-Elements Planning Reference Strip if in Elemental / Dual mode
            if (timeSystemMode == TimeSystemMode.ELEMENTAL_ETIME || timeSystemMode == TimeSystemMode.DUAL_DISPLAY) {
                val sequenceBlocks = remember(currentTime, selectedDate.gregorianDate, zoneId) {
                    ElementalTimeEngine.getChronologicalSequence(Instant.now(), selectedDate.gregorianDate, zoneId)
                }
                val shortLocalTimeFormatter = remember { DateTimeFormatter.ofPattern("h a", Locale.ENGLISH) }

                Spacer(modifier = Modifier.height(8.dp))

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(elementalTime.season.accentColorHex).copy(alpha = 0.12f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(elementalTime.season.accentColorHex).copy(alpha = 0.3f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { onOpenTimeSystemPicker() }
                        .testTag("etime_quick_planning_strip")
                ) {
                    Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Universal etime: ${elementalTime.season.englishName} (${elementalTime.season.arabicName})",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(elementalTime.season.accentColorHex)
                            )
                            Text(
                                text = "${elementalTime.blockProgressPercent}%",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(elementalTime.season.accentColorHex)
                            )
                        }

                        Spacer(modifier = Modifier.height(5.dp))

                        // 4 Consecutive Elements Sequence with Local Time references for planning
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            sequenceBlocks.forEach { block ->
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (block.isCurrent) Color(block.season.accentColorHex).copy(alpha = 0.28f)
                                        else MaterialTheme.colorScheme.surface.copy(alpha = 0.5f),
                                    border = if (block.isCurrent) androidx.compose.foundation.BorderStroke(1.dp, Color(block.season.accentColorHex)) else null,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 3.dp),
                                        horizontalArrangement = Arrangement.Center,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(text = block.season.emoji, fontSize = 11.sp)
                                        Spacer(modifier = Modifier.width(3.dp))
                                        Text(
                                            text = if (block.isCurrent) "NOW" else block.localStart.format(shortLocalTimeFormatter),
                                            fontSize = 9.sp,
                                            fontWeight = if (block.isCurrent) FontWeight.ExtraBold else FontWeight.SemiBold,
                                            color = if (block.isCurrent) Color(block.season.accentColorHex) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Row 2: Big Bold Day Number & Month / Year Hero Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Day Number Pill / Display
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)),
                    modifier = Modifier.size(68.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = "${selectedDate.day}",
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(14.dp))

                // Month, Year, Translation, Vahid
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (selectedDate.month == 0) "Ayyám-i-Há" else "${selectedDate.monthInfo.transliteration} ${selectedDate.year} B.E.",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = if (selectedDate.month == 0) "Intercalary Days" else "${selectedDate.monthInfo.translation} (${selectedDate.monthInfo.arabic})",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    // Year Notation (Short / Long System) - Clickable to open picker or toggle
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable { onOpenYearPicker() }
                            .padding(vertical = 2.dp)
                    ) {
                        val cycleText = if (yearNotationSystem == YearNotationSystem.SHORT_SYSTEM) {
                            "Year ${selectedDate.year} B.E. • Váḥid ${selectedDate.vahid} (${selectedDate.vahidYearInfo.transliteration})"
                        } else {
                            selectedDate.fullLongYearString
                        }
                        Text(
                            text = cycleText,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.EditCalendar,
                            contentDescription = "Change Year",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(13.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Row 3: Solar Times & Location Chip (Auto-detect or pick)
            val sunriseEtime = remember(sunriseTime, selectedDate.gregorianDate, zoneId) {
                ElementalTimeEngine.calculateForLocalTime(sunriseTime, selectedDate.gregorianDate, zoneId)
            }
            val sunsetEtime = remember(sunsetTime, selectedDate.gregorianDate, zoneId) {
                ElementalTimeEngine.calculateForLocalTime(sunsetTime, selectedDate.gregorianDate, zoneId)
            }
            val sunriseDisplay = when (timeSystemMode) {
                TimeSystemMode.STANDARD_CIVIL -> sunriseTime.format(shortTimeFormatter)
                TimeSystemMode.ELEMENTAL_ETIME -> "${sunriseEtime.blockHour}:${String.format(Locale.US, "%02d", sunriseEtime.minute)} ${sunriseEtime.season.emoji}"
                TimeSystemMode.DUAL_DISPLAY -> "${sunriseTime.format(shortTimeFormatter)} (${sunriseEtime.blockHour}:${String.format(Locale.US, "%02d", sunriseEtime.minute)} ${sunriseEtime.season.emoji})"
            }
            val sunsetDisplay = when (timeSystemMode) {
                TimeSystemMode.STANDARD_CIVIL -> sunsetTime.format(shortTimeFormatter)
                TimeSystemMode.ELEMENTAL_ETIME -> "${sunsetEtime.blockHour}:${String.format(Locale.US, "%02d", sunsetEtime.minute)} ${sunsetEtime.season.emoji}"
                TimeSystemMode.DUAL_DISPLAY -> "${sunsetTime.format(shortTimeFormatter)} (${sunsetEtime.blockHour}:${String.format(Locale.US, "%02d", sunsetEtime.minute)} ${sunsetEtime.season.emoji})"
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Location Button / Chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showLocationPicker = true }
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (hasLocationPermission) Icons.Default.MyLocation else Icons.Default.LocationOn,
                            contentDescription = "Location",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = currentLocation.cityName.split(" / ").first(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                }

                // Solar Times: Sunrise & Sunset
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "🌅 $sunriseDisplay",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text("•", color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        text = "🌇 $sunsetDisplay",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Row 4: Sunset countdown or status detail
            Spacer(modifier = Modifier.height(6.dp))
            val detailMessage = if (!isAfterSunset) {
                val diffMinutes = Duration.between(currentTime, sunsetTime).toMinutes()
                val hours = diffMinutes / 60
                val mins = diffMinutes % 60
                "Badí' day in progress • Sunset at $sunsetDisplay (in ${hours}h ${mins}m)"
            } else {
                "Evening begun at sunset ($sunsetDisplay). Next Badí' date is now active!"
            }

            Text(
                text = detailMessage,
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Row 5: Holy Day, Feast, or Fasting Notification Banner
            if (selectedDate.holyDay != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HolyDayGold.copy(alpha = 0.18f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = HolyDayGold,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "✨ ${selectedDate.holyDay.name}",
                                color = HolyDayGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            if (selectedDate.holyDay.isWorkSuspended) {
                                Text(
                                    text = "Work & School Suspended • ${selectedDate.holyDay.commemorationTime}",
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            } else if (selectedDate.isFeastDay) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = FeastTeal.copy(alpha = 0.18f),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = FeastTeal,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "🕊️ Feast of ${selectedDate.monthInfo.transliteration} (${selectedDate.monthInfo.translation})",
                            color = FeastTeal,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Fasting banner if month is 'Alá'
            if (isFastingMonth || selectedDate.month == 19) {
                Spacer(modifier = Modifier.height(10.dp))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(8.dp))

                val isDuringFastHours = currentTime.isAfter(sunriseTime) && currentTime.isBefore(sunsetTime)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "✨ Nineteen Day Fast ('Alá')",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = HolyDayGold
                        )
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
                        modifier = Modifier.height(28.dp)
                    ) {
                        Text("Fasting Prayers", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Gregorian Date Footer
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = selectedDate.gregorianDate.format(gregFormatter),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (isToday) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "TODAY",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }

    // Location Picker & GPS Dialog
    if (showLocationPicker) {
        Dialog(onDismissRequest = { showLocationPicker = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.8f)
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
                            text = "Location & Solar Times",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { showLocationPicker = false }) {
                            Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                        }
                    }

                    Text(
                        text = "Calculates exact sunset, sunrise, 19-Day Fast hours, and Qiblih bearing.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Auto-Detect GPS Button
                    Button(
                        onClick = {
                            if (hasLocationPermission) {
                                onDetectLocation()
                                showLocationPicker = false
                            } else {
                                onRequestLocationPermission()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MyLocation,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (hasLocationPermission) "Use Current GPS Location" else "Grant Location Permission",
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "Or Choose Preset City:",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(8.dp))

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
