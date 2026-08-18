package com.example.ui

import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.badi.BadiCalendarEngine
import com.example.badi.BadiDate
import com.example.badi.ElementalTimeEngine
import com.example.badi.ElementalTimeSeason
import com.example.badi.TimeSystemMode
import com.example.calendar.GoogleCalendarAccount
import com.example.devotional.CityLocation
import com.example.ui.theme.HolyDayGold
import com.example.calendar.CalendarEvent
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

enum class BadiDayPreset {
    START_OF_BADI_DAY,         // After sunset start of Badí' day (previous civil evening)
    AFTER_MIDNIGHT_BADI_DAY,   // After midnight Badí' day (daytime)
    NEXT_BADI_DAY_SAME_CIVIL,  // After sunset next Badí' day (same civil day evening)
    ELEMENTAL_FIRE_BLOCK,      // Universal Fire (00:00-06:00 UTC)
    ELEMENTAL_AIR_BLOCK,       // Universal Air (06:00-12:00 UTC)
    ELEMENTAL_WATER_BLOCK,     // Universal Water (12:00-18:00 UTC)
    ELEMENTAL_EARTH_BLOCK      // Universal Earth (18:00-24:00 UTC)
}

@Composable
fun AddEventDialog(
    selectedDate: BadiDate,
    calendarAccounts: List<GoogleCalendarAccount>,
    selectedCalendarId: Long?,
    currentLocation: CityLocation,
    editingEvent: CalendarEvent? = null,
    timeSystemMode: TimeSystemMode = TimeSystemMode.STANDARD_CIVIL,
    onDismiss: () -> Unit,
    onAddEvent: (
        title: String,
        description: String,
        location: String,
        targetDate: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        isAllDay: Boolean,
        calendarId: Long
    ) -> Unit,
    onUpdateEvent: ((
        eventId: Long,
        title: String,
        description: String,
        location: String,
        targetDate: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        isAllDay: Boolean,
        calendarId: Long
    ) -> Unit)? = null,
    onOpenNativeCalendar: (
        title: String,
        description: String,
        targetDate: LocalDate,
        startTime: LocalTime,
        endTime: LocalTime,
        isAllDay: Boolean
    ) -> Unit
) {
    val context = LocalContext.current
    val zoneId = remember(currentLocation) {
        try {
            ZoneId.of(currentLocation.timeZoneId)
        } catch (_: Exception) {
            ZoneId.systemDefault()
        }
    }

    val initialDate = remember(editingEvent, selectedDate) {
        if (editingEvent != null) {
            Instant.ofEpochMilli(editingEvent.startMillis).atZone(zoneId).toLocalDate()
        } else {
            selectedDate.gregorianDate
        }
    }

    val initialStartTime = remember(editingEvent) {
        if (editingEvent != null) {
            Instant.ofEpochMilli(editingEvent.startMillis).atZone(zoneId).toLocalTime()
        } else {
            LocalTime.of(10, 0)
        }
    }

    val initialEndTime = remember(editingEvent) {
        if (editingEvent != null) {
            Instant.ofEpochMilli(editingEvent.endMillis).atZone(zoneId).toLocalTime()
        } else {
            LocalTime.of(11, 0)
        }
    }

    var title by remember(editingEvent) { mutableStateOf(editingEvent?.title ?: "") }
    var description by remember(editingEvent) { mutableStateOf(editingEvent?.description ?: "") }
    var location by remember(editingEvent) { mutableStateOf(editingEvent?.location ?: "") }
    var isAllDay by remember(editingEvent) { mutableStateOf(editingEvent?.isAllDay ?: false) }

    var targetDate by remember(initialDate) { mutableStateOf(initialDate) }
    var startTime by remember(initialStartTime) { mutableStateOf(initialStartTime) }
    var endTime by remember(initialEndTime) { mutableStateOf(initialEndTime) }

    var selectedPreset by remember { mutableStateOf<BadiDayPreset?>(if (editingEvent != null) null else BadiDayPreset.AFTER_MIDNIGHT_BADI_DAY) }

    var chosenCalendarId by remember(editingEvent, selectedCalendarId) {
        mutableStateOf(editingEvent?.calendarId ?: selectedCalendarId ?: calendarAccounts.firstOrNull()?.id ?: 1L)
    }
    var accountMenuExpanded by remember { mutableStateOf(false) }

    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a") }
    val dateDisplayFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d, yyyy") }
    val shortDateFormatter = remember { DateTimeFormatter.ofPattern("EEE, MMM d") }

    val effectiveSunset = remember(targetDate, currentLocation) {
        BadiCalendarEngine.calculateSunset(
            date = targetDate,
            latitude = currentLocation.latitude,
            longitude = currentLocation.longitude,
            zoneId = zoneId
        )
    }
    val isPostSunset = !startTime.isBefore(effectiveSunset)

    // Dynamically calculate the active Badí' date for the selected civil target date and time
    val activeBadiDate = remember(targetDate, startTime, isAllDay, currentLocation) {
        if (isAllDay) {
            BadiCalendarEngine.gregorianToBadi(targetDate, LocalTime.of(12, 0), currentLocation.latitude, currentLocation.longitude, zoneId)
        } else {
            BadiCalendarEngine.gregorianToBadi(targetDate, startTime, currentLocation.latitude, currentLocation.longitude, zoneId)
        }
    }

    val activeAccount = calendarAccounts.firstOrNull { it.id == chosenCalendarId }
        ?: calendarAccounts.firstOrNull()

    // Helper sunset calculation for previous day and current day
    val prevCivilDate = remember(selectedDate) { selectedDate.gregorianDate.minusDays(1) }
    val sunsetPrevDay = remember(prevCivilDate, currentLocation) {
        BadiCalendarEngine.calculateSunset(
            date = prevCivilDate,
            latitude = currentLocation.latitude,
            longitude = currentLocation.longitude,
            zoneId = zoneId
        )
    }
    val sunsetCurrDay = remember(selectedDate, currentLocation) {
        BadiCalendarEngine.calculateSunset(
            date = selectedDate.gregorianDate,
            latitude = currentLocation.latitude,
            longitude = currentLocation.longitude,
            zoneId = zoneId
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .padding(vertical = 16.dp)
                .navigationBarsPadding()
                .testTag("add_appointment_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (editingEvent != null) "Edit Appointment" else "New Appointment",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (editingEvent != null) "Updates directly in Google Calendar" else "Syncs with Google Calendar",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_add_dialog_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Badí' Date & Sunset Split Demarcation Banner (Dynamically reflects date and time changes)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isAllDay) {
                                        targetDate.format(dateDisplayFormatter) + " (All-Day)"
                                    } else {
                                        "${targetDate.format(dateDisplayFormatter)} at ${startTime.format(timeFormatter)}"
                                    },
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp
                                )
                                val badiDateDisplay = if (activeBadiDate.month == 0) {
                                    "Badí' Date: Ayyám-i-Há (Day ${activeBadiDate.day}) ${activeBadiDate.year} B.E."
                                } else {
                                    "Badí' Date: ${activeBadiDate.day} ${activeBadiDate.monthInfo.transliteration} ${activeBadiDate.year} B.E."
                                }
                                Text(
                                    text = badiDateDisplay,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        // Badí' Day Attribute & Translation
                        val monthMeaning = if (activeBadiDate.month == 0) "Intercalary Days" else activeBadiDate.monthInfo.translation
                        val dayMeaning = "${activeBadiDate.dayInfo.transliteration} (${activeBadiDate.dayInfo.translation})"
                        Text(
                            text = "Month: $monthMeaning • Day: $dayMeaning",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp,
                            maxLines = 1
                        )

                        // Holy Day or Feast Eve Badge if active
                        if (activeBadiDate.holyDay != null || activeBadiDate.isFeastDay) {
                            Spacer(modifier = Modifier.height(6.dp))
                            val badgeLabel = when {
                                activeBadiDate.holyDay != null -> {
                                    if (isPostSunset && !isAllDay) "✨ Eve of ${activeBadiDate.holyDay.name}"
                                    else "✨ ${activeBadiDate.holyDay.name}"
                                }
                                activeBadiDate.isFeastDay -> {
                                    if (isPostSunset && !isAllDay) "🕊️ Eve of Feast of ${activeBadiDate.monthInfo.transliteration}"
                                    else "🕊️ Feast of ${activeBadiDate.monthInfo.transliteration}"
                                }
                                else -> ""
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(HolyDayGold.copy(alpha = 0.2f))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = badgeLabel,
                                    color = HolyDayGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Sunset Demarcation Indicator
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isAllDay) {
                                        MaterialTheme.colorScheme.surface
                                    } else if (isPostSunset) {
                                        MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                    } else {
                                        HolyDayGold.copy(alpha = 0.15f)
                                    }
                                )
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (isAllDay) Icons.Default.CalendarToday else if (isPostSunset) Icons.Default.Nightlight else Icons.Default.WbSunny,
                                contentDescription = null,
                                tint = if (isAllDay) MaterialTheme.colorScheme.primary else if (isPostSunset) MaterialTheme.colorScheme.primary else HolyDayGold,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = if (isAllDay) {
                                        "All-Day Event (Spans full Bahá'í Date)"
                                    } else if (isPostSunset) {
                                        "🌙 Post-Sunset / Eve (Sunset at ${effectiveSunset.format(timeFormatter)})"
                                    } else {
                                        "☀️ Daylight Period (Sunset at ${effectiveSunset.format(timeFormatter)})"
                                    },
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 11.sp
                                )
                                if (!isAllDay) {
                                    Text(
                                        text = if (isPostSunset) {
                                            when (selectedPreset) {
                                                BadiDayPreset.START_OF_BADI_DAY -> "Marks evening start of ${activeBadiDate.day} ${activeBadiDate.monthInfo.transliteration} (previous sunset)."
                                                BadiDayPreset.NEXT_BADI_DAY_SAME_CIVIL -> "Marks evening start of next day (${activeBadiDate.day} ${activeBadiDate.monthInfo.transliteration}, today's sunset)."
                                                else -> "In the Badí' calendar, this falls into the day of ${activeBadiDate.day} ${activeBadiDate.monthInfo.transliteration} (${activeBadiDate.weekdayInfo.transliteration})."
                                            }
                                        } else {
                                            "Scheduled during the daytime portion of ${activeBadiDate.day} ${activeBadiDate.monthInfo.transliteration} (${activeBadiDate.weekdayInfo.transliteration})."
                                        },
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title field
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    placeholder = { Text("e.g. 19-Day Feast, Study Circle, Devotional") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("event_title_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Calendar Account Selector
                if (calendarAccounts.isNotEmpty()) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = "${activeAccount?.displayName ?: "Google Calendar"} (${activeAccount?.accountName ?: ""})",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Target Google Calendar") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Calendar",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedTextColor = MaterialTheme.colorScheme.onSurface,
                                unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                                focusedLabelColor = MaterialTheme.colorScheme.primary,
                                unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { accountMenuExpanded = true }
                        )

                        DropdownMenu(
                            expanded = accountMenuExpanded,
                            onDismissRequest = { accountMenuExpanded = false },
                            modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                        ) {
                            calendarAccounts.forEach { acc ->
                                DropdownMenuItem(
                                    text = {
                                        Column {
                                            Text(acc.displayName, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium)
                                            Text(acc.accountName, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                        }
                                    },
                                    onClick = {
                                        chosenCalendarId = acc.id
                                        accountMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }

                // All-day switch
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("All-Day Event", color = MaterialTheme.colorScheme.onSurface, fontSize = 14.sp)
                    Switch(
                        checked = isAllDay,
                        onCheckedChange = { isAllDay = it },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.onPrimary,
                            checkedTrackColor = MaterialTheme.colorScheme.primary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.outline
                        ),
                        modifier = Modifier.testTag("all_day_switch")
                    )
                }

                if (!isAllDay) {
                    Spacer(modifier = Modifier.height(14.dp))

                    // Three Badí' Day Preset Buttons (Sunset-Aware)
                    Text(
                        text = "BADÍ' DAY PERIOD PRESETS",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Preset 1: After sunset start of Badí' day
                        val startOfBadiTime = remember(sunsetPrevDay) { sunsetPrevDay.plusMinutes(15) }
                        val isPreset1Selected = selectedPreset == BadiDayPreset.START_OF_BADI_DAY

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isPreset1Selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isPreset1Selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPreset = BadiDayPreset.START_OF_BADI_DAY
                                    targetDate = prevCivilDate
                                    startTime = startOfBadiTime
                                    endTime = startOfBadiTime.plusHours(1)
                                }
                                .testTag("preset_start_of_badi_day")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isPreset1Selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Nightlight,
                                        contentDescription = null,
                                        tint = if (isPreset1Selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "After sunset start of Badí' day",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = if (isPreset1Selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Eve • ${prevCivilDate.format(shortDateFormatter)} at ${startOfBadiTime.format(timeFormatter)}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Preset 2: After midnight Badí' day
                        val isPreset2Selected = selectedPreset == BadiDayPreset.AFTER_MIDNIGHT_BADI_DAY

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isPreset2Selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isPreset2Selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPreset = BadiDayPreset.AFTER_MIDNIGHT_BADI_DAY
                                    targetDate = selectedDate.gregorianDate
                                    startTime = LocalTime.of(10, 0)
                                    endTime = LocalTime.of(11, 0)
                                }
                                .testTag("preset_after_midnight_badi_day")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isPreset2Selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WbSunny,
                                        contentDescription = null,
                                        tint = if (isPreset2Selected) MaterialTheme.colorScheme.onPrimary else HolyDayGold,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Daytime (Sunrise to Sunset)",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = if (isPreset2Selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Daytime • ${selectedDate.gregorianDate.format(shortDateFormatter)} at 10:00 AM",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }

                        // Preset 3: After sunset next Badí' day (same civil day)
                        val nextBadiTime = remember(sunsetCurrDay) { sunsetCurrDay.plusMinutes(15) }
                        val isPreset3Selected = selectedPreset == BadiDayPreset.NEXT_BADI_DAY_SAME_CIVIL

                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (isPreset3Selected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isPreset3Selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    selectedPreset = BadiDayPreset.NEXT_BADI_DAY_SAME_CIVIL
                                    targetDate = selectedDate.gregorianDate
                                    startTime = nextBadiTime
                                    endTime = nextBadiTime.plusHours(1)
                                }
                                .testTag("preset_next_badi_day_same_civil")
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .clip(CircleShape)
                                        .background(if (isPreset3Selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Nightlight,
                                        contentDescription = null,
                                        tint = if (isPreset3Selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "After sunset next Badí' day (same civil day)",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp,
                                        color = if (isPreset3Selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "Evening • ${selectedDate.gregorianDate.format(shortDateFormatter)} at ${nextBadiTime.format(timeFormatter)}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Elemental Time (etime) Universal 6-Hour Blocks Quick Selector
                    Text(
                        text = "UNIVERSAL ELEMENTAL BLOCKS (ETIME)",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    val elementalBlocks = listOf(
                        Pair(ElementalTimeSeason.FIRE, BadiDayPreset.ELEMENTAL_FIRE_BLOCK),
                        Pair(ElementalTimeSeason.AIR, BadiDayPreset.ELEMENTAL_AIR_BLOCK),
                        Pair(ElementalTimeSeason.WATER, BadiDayPreset.ELEMENTAL_WATER_BLOCK),
                        Pair(ElementalTimeSeason.EARTH, BadiDayPreset.ELEMENTAL_EARTH_BLOCK)
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        elementalBlocks.forEach { (season, preset) ->
                            val isSelected = selectedPreset == preset
                            val localStart: LocalTime = remember(season, targetDate, zoneId) {
                                ElementalTimeEngine.elementalTimeToLocalTime(season, 0, 0, targetDate, zoneId)
                            }
                            val localEnd: LocalTime = remember(season, targetDate, zoneId) {
                                ElementalTimeEngine.elementalTimeToLocalTime(season, 5, 59, targetDate, zoneId)
                            }

                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) Color(season.accentColorHex).copy(alpha = 0.28f)
                                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (isSelected) Color(season.accentColorHex)
                                    else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        selectedPreset = preset
                                        targetDate = selectedDate.gregorianDate
                                        startTime = localStart
                                        endTime = localStart.plusHours(1L)
                                    }
                                    .testTag("elemental_preset_${season.name.lowercase()}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = season.emoji,
                                        fontSize = 16.sp
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = season.englishName,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (isSelected) Color(season.accentColorHex) else MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = localStart.format(DateTimeFormatter.ofPattern("h:mm a")),
                                        fontSize = 9.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Start & End Time Pickers (Custom Tuning)
                    Text(
                        text = "CUSTOM TIME ADJUSTMENT",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    val startEtime = remember(startTime, targetDate, zoneId) {
                        ElementalTimeEngine.calculateForLocalTime(startTime, targetDate, zoneId)
                    }
                    val endEtime = remember(endTime, targetDate, zoneId) {
                        ElementalTimeEngine.calculateForLocalTime(endTime, targetDate, zoneId)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Start Time
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    showTimePicker(context, startTime) {
                                        startTime = it
                                        selectedPreset = null
                                    }
                                }
                                .padding(12.dp)
                                .testTag("start_time_picker_button")
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Starts", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                    Text(
                                        text = "${startEtime.blockHour}:${String.format(Locale.US, "%02d", startEtime.minute)} ${startEtime.season.emoji}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(startEtime.season.accentColorHex)
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = startTime.format(timeFormatter),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }

                        // End Time
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    showTimePicker(context, endTime) {
                                        endTime = it
                                        selectedPreset = null
                                    }
                                }
                                .padding(12.dp)
                                .testTag("end_time_picker_button")
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text("Ends", color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 11.sp)
                                    Text(
                                        text = "${endEtime.blockHour}:${String.format(Locale.US, "%02d", endEtime.minute)} ${endEtime.season.emoji}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(endEtime.season.accentColorHex)
                                    )
                                }
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(top = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = endTime.format(timeFormatter),
                                        color = MaterialTheme.colorScheme.onSurface,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    placeholder = { Text("e.g. Bahá'í Center / Online / Home") },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Description
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description & Notes") },
                    placeholder = { Text("Add devotional readings, feast agenda, notes") },
                    maxLines = 3,
                    leadingIcon = {
                        Icon(Icons.AutoMirrored.Filled.Notes, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline,
                        focusedLabelColor = MaterialTheme.colorScheme.primary,
                        unfocusedLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Primary Action Button
                Button(
                    onClick = {
                        if (title.isNotBlank()) {
                            if (editingEvent != null && onUpdateEvent != null) {
                                onUpdateEvent(
                                    editingEvent.id,
                                    title.trim(),
                                    description.trim(),
                                    location.trim(),
                                    targetDate,
                                    startTime,
                                    endTime,
                                    isAllDay,
                                    chosenCalendarId
                                )
                            } else {
                                onAddEvent(
                                    title.trim(),
                                    description.trim(),
                                    location.trim(),
                                    targetDate,
                                    startTime,
                                    endTime,
                                    isAllDay,
                                    chosenCalendarId
                                )
                            }
                        }
                    },
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
                        disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .testTag("save_appointment_button")
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (editingEvent != null) "Update Appointment" else "Save to Google Calendar", fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Secondary Open in Google Calendar App
                OutlinedButton(
                    onClick = {
                        onOpenNativeCalendar(
                            title.trim(),
                            description.trim(),
                            targetDate,
                            startTime,
                            endTime,
                            isAllDay
                        )
                        onDismiss()
                    },
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(46.dp)
                        .testTag("open_in_gcal_button")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Open in Google Calendar App")
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }
}

private fun showTimePicker(
    context: Context,
    initialTime: LocalTime,
    onTimeSelected: (LocalTime) -> Unit
) {
    TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            onTimeSelected(LocalTime.of(hourOfDay, minute))
        },
        initialTime.hour,
        initialTime.minute,
        false
    ).show()
}
