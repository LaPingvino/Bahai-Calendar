package com.example.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarViewMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import java.time.Instant
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.AutoStories
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Public
import com.example.badi.ElementalTimeEngine
import com.example.badi.ElementalTimeSeason
import com.example.badi.TimeSystemMode
import com.example.ui.components.SolarFastingBanner
import com.example.ui.components.UnifiedHeroCard
import com.example.location.LocationHelper
import java.time.ZoneId
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import com.example.badi.BadiCalendarEngine
import com.example.badi.BadiDate
import com.example.badi.BadiHolyDay
import com.example.calendar.CalendarEvent
import com.example.calendar.CalendarRepository
import com.example.ui.theme.ActiveNavPill
import com.example.ui.theme.BorderColor
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardSurface
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.FeastTeal
import com.example.ui.theme.HolyDayGold
import com.example.ui.theme.LavenderContainer
import com.example.ui.theme.LavenderOnPrimary
import com.example.ui.theme.LavenderPrimary
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.TextTertiary
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@Composable
fun BadiCalendarScreen(viewModel: BadiViewModel) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    // Calendar Permission Launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val readGranted = permissions[Manifest.permission.READ_CALENDAR] ?: false
        val writeGranted = permissions[Manifest.permission.WRITE_CALENDAR] ?: false
        viewModel.onPermissionResult(readGranted && writeGranted)
    }

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        viewModel.onLocationPermissionResult(fineGranted || coarseGranted)
    }

    // Request Location Permission on start if not already granted
    LaunchedEffect(Unit) {
        if (!LocationHelper.hasLocationPermission(context)) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            viewModel.detectUserLocation()
        }
    }

    LaunchedEffect(uiState.snackbarMessage) {
        uiState.snackbarMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.dismissSnackbar()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            AppTopBar(
                todayDate = uiState.todayBadiDate,
                onGoToToday = { viewModel.goToToday() },
                onOpenThemePicker = { viewModel.showThemePicker(true) },
                timeSystemMode = uiState.timeSystemMode,
                onOpenTimeSystemPicker = { viewModel.showTimeSystemPicker(true) }
            )
        },
        floatingActionButton = {
            if (uiState.activeTab == CalendarTab.MONTH_VIEW || uiState.activeTab == CalendarTab.APPOINTMENTS) {
                FloatingActionButton(
                    onClick = { viewModel.showAddEventDialog(true) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .size(56.dp)
                        .testTag("add_appointment_fab")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Appointment",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        },
        bottomBar = {
            BadiBottomNavigationBar(
                currentTab = uiState.activeTab,
                onSelectTab = { viewModel.selectTab(it) }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Body content according to active tab
            when (uiState.activeTab) {
                CalendarTab.MONTH_VIEW -> {
                    MonthViewContent(
                        uiState = uiState,
                        viewModel = viewModel,
                        onRequestCalendarPermissions = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.READ_CALENDAR,
                                    Manifest.permission.WRITE_CALENDAR
                                )
                            )
                        },
                        onRequestLocationPermissions = {
                            locationPermissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION
                                )
                            )
                        }
                    )
                }
                CalendarTab.HOLY_DAYS -> {
                    HolyDaysTabContent(
                        uiState = uiState,
                        onHolyDayClick = { viewModel.showHolyDayDetail(it) }
                    )
                }
                CalendarTab.DEVOTIONALS -> {
                    DevotionalScreen(
                        currentLocation = uiState.currentLocation,
                        onSelectLocation = { viewModel.setLocation(it) }
                    )
                }
                CalendarTab.YEAR_CYCLES -> {
                    YearCyclesTabContent(uiState = uiState)
                }
                CalendarTab.APPOINTMENTS -> {
                    AppointmentsTabContent(
                        uiState = uiState,
                        viewModel = viewModel,
                        onRequestPermissions = {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.READ_CALENDAR,
                                    Manifest.permission.WRITE_CALENDAR
                                )
                            )
                        },
                        onOpenThemePicker = { viewModel.showThemePicker(true) },
                        onOpenTimeSystemPicker = { viewModel.showTimeSystemPicker(true) }
                    )
                }
            }
        }
    }

    // Year Picker Dialog
    if (uiState.showYearPicker) {
        YearPickerDialog(
            currentSelectedYear = uiState.displayedBadiYear,
            todayBadiYear = uiState.todayBadiDate.year,
            currentNotationSystem = uiState.yearNotationSystem,
            onSelectYear = { viewModel.setYear(it) },
            onToggleNotationSystem = { viewModel.setYearNotationSystem(it) },
            onDismiss = { viewModel.toggleYearPicker(false) }
        )
    }

    // Theme Picker Dialog
    if (uiState.showThemePicker) {
        ThemePickerDialog(
            currentTheme = uiState.currentTheme,
            onSelectTheme = { viewModel.setThemeMode(it) },
            onDismiss = { viewModel.showThemePicker(false) }
        )
    }

    // Time System Picker Dialog
    if (uiState.showTimeSystemPicker) {
        val currentZoneId = remember(uiState.currentLocation.timeZoneId) {
            try {
                ZoneId.of(uiState.currentLocation.timeZoneId)
            } catch (_: Exception) {
                ZoneId.systemDefault()
            }
        }
        TimeSystemPickerDialog(
            currentTimeSystem = uiState.timeSystemMode,
            zoneId = currentZoneId,
            onSelectTimeSystem = { viewModel.setTimeSystemMode(it) },
            onDismiss = { viewModel.showTimeSystemPicker(false) }
        )
    }

    // Add / Edit Appointment Dialog
    if (uiState.showAddEventDialog) {
        val calendarRepo = remember { CalendarRepository(context) }
        AddEventDialog(
            selectedDate = uiState.selectedBadiDate,
            calendarAccounts = uiState.calendarAccounts,
            selectedCalendarId = uiState.selectedCalendarId,
            currentLocation = uiState.currentLocation,
            editingEvent = uiState.editingEvent,
            timeSystemMode = uiState.timeSystemMode,
            onDismiss = { viewModel.showAddEventDialog(false) },
            onAddEvent = { title, desc, loc, targetDate, start, end, isAllDay, calId ->
                viewModel.addAppointment(title, desc, loc, start, end, isAllDay, calId, targetDate)
            },
            onUpdateEvent = { eventId, title, desc, loc, targetDate, start, end, isAllDay, calId ->
                viewModel.editAppointment(eventId, title, desc, loc, start, end, isAllDay, calId, targetDate)
            },
            onOpenNativeCalendar = { title, desc, targetDate, start, end, isAllDay ->
                val startMillis = if (isAllDay) {
                    targetDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                } else {
                    targetDate.atTime(start).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                }
                val endMillis = if (isAllDay) {
                    targetDate.plusDays(1).atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                } else {
                    targetDate.atTime(end).atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
                }
                val intent = calendarRepo.createInsertCalendarIntent(title, startMillis, endMillis, isAllDay, desc)
                context.startActivity(intent)
            }
        )
    }

    // Holy Day Detail Dialog
    uiState.selectedHolyDayDetail?.let { holyDay ->
        HolyDayDetailDialog(
            holyDay = holyDay,
            badiYear = uiState.displayedBadiYear,
            onDismiss = { viewModel.showHolyDayDetail(null) }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    todayDate: BadiDate,
    onGoToToday: () -> Unit,
    onOpenThemePicker: () -> Unit,
    timeSystemMode: TimeSystemMode = TimeSystemMode.STANDARD_CIVIL,
    onOpenTimeSystemPicker: () -> Unit = {}
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(15.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Column {
                    Text(
                        text = "Badí' Calendar",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${todayDate.year} B.E. • Váḥid ${todayDate.vahid}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        },
        actions = {
            // Time System toggle button
            IconButton(
                onClick = onOpenTimeSystemPicker,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("time_system_topbar_button")
            ) {
                Surface(
                    shape = CircleShape,
                    color = when (timeSystemMode) {
                        TimeSystemMode.STANDARD_CIVIL -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        TimeSystemMode.ELEMENTAL_ETIME -> Color(0xFFFF7043).copy(alpha = 0.25f)
                        TimeSystemMode.DUAL_DISPLAY -> MaterialTheme.colorScheme.primaryContainer
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = when (timeSystemMode) {
                                TimeSystemMode.STANDARD_CIVIL -> Icons.Default.Schedule
                                TimeSystemMode.ELEMENTAL_ETIME -> Icons.Default.Public
                                TimeSystemMode.DUAL_DISPLAY -> Icons.Default.Schedule
                            },
                            contentDescription = "Time System Mode (${timeSystemMode.displayName})",
                            tint = when (timeSystemMode) {
                                TimeSystemMode.STANDARD_CIVIL -> MaterialTheme.colorScheme.primary
                                TimeSystemMode.ELEMENTAL_ETIME -> Color(0xFFFF7043)
                                TimeSystemMode.DUAL_DISPLAY -> MaterialTheme.colorScheme.primary
                            },
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(2.dp))

            // Theme picker button
            IconButton(
                onClick = onOpenThemePicker,
                modifier = Modifier
                    .size(36.dp)
                    .testTag("theme_picker_button")
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f),
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Select Theme",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Quick jump to Today button with proper padding and clean styling
            FilledTonalButton(
                onClick = onGoToToday,
                shape = RoundedCornerShape(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 0.dp),
                modifier = Modifier
                    .height(32.dp)
                    .padding(end = 8.dp)
                    .testTag("today_button")
            ) {
                Text(
                    text = "Today",
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.testTag("app_top_bar")
    )
}

@Composable
fun MonthViewContent(
    uiState: BadiUiState,
    viewModel: BadiViewModel,
    onRequestCalendarPermissions: () -> Unit,
    onRequestLocationPermissions: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Unified Master Hero Date Card (Merged Badí' Date + Live Clock + Sunset & Solar + Fasting + GPS Location)
        UnifiedHeroCard(
            selectedDate = uiState.selectedBadiDate,
            todayDate = uiState.todayBadiDate,
            isEveningMode = uiState.isAfterSunsetMode,
            yearNotationSystem = uiState.yearNotationSystem,
            currentLocation = uiState.currentLocation,
            hasLocationPermission = uiState.hasLocationPermission,
            isDetectingLocation = uiState.isDetectingLocation,
            onRequestLocationPermission = onRequestLocationPermissions,
            onDetectLocation = { viewModel.detectUserLocation() },
            onSelectLocation = { viewModel.setLocation(it) },
            onOpenYearPicker = { viewModel.toggleYearPicker(true) },
            onToggleNotationSystem = { viewModel.toggleYearNotationSystem() },
            onOpenFastingPrayers = { viewModel.selectTab(CalendarTab.DEVOTIONALS) },
            timeSystemMode = uiState.timeSystemMode,
            onOpenTimeSystemPicker = { viewModel.showTimeSystemPicker(true) }
        )

        // Main Calendar Section (28dp radius, #25232A)
        MainCalendarSection(
            uiState = uiState,
            viewModel = viewModel
        )

        // Google Calendar Events Section
        GoogleCalendarEventsSection(
            uiState = uiState,
            viewModel = viewModel,
            onRequestPermissions = onRequestCalendarPermissions
        )

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun HeroDateCard(
    selectedDate: BadiDate,
    todayDate: BadiDate,
    isEveningMode: Boolean,
    yearNotationSystem: YearNotationSystem,
    onOpenYearPicker: () -> Unit,
    onToggleNotationSystem: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(28.dp))
            .padding(22.dp)
            .testTag("hero_date_card")
    ) {
        // Watermark icon in top-right
        Icon(
            imageVector = Icons.Default.WbSunny,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f),
            modifier = Modifier
                .size(110.dp)
                .align(Alignment.TopEnd)
        )

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "CURRENT DATE",
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                // Sunset Badge
                val sunsetText = selectedDate.sunsetTime?.let {
                    val fmt = DateTimeFormatter.ofPattern("h:mm a")
                    if (isEveningMode) "Evening mode" else "Sunset at ${it.format(fmt)}"
                } ?: "Sunset transition"

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.8f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(12.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = sunsetText,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Main Large Date: e.g. "14 'Izzat" or "17 Asmá'"
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${selectedDate.day} ${selectedDate.monthInfo.transliteration}",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light
                )
                Text(
                    text = selectedDate.monthInfo.arabic,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Normal
                )
            }

            // Year details row with click to toggle notation or pick year
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onToggleNotationSystem() }
                    .padding(vertical = 2.dp)
            ) {
                val yearFormatted = if (yearNotationSystem == YearNotationSystem.SHORT_SYSTEM) {
                    "Year ${selectedDate.year} B.E. • Váḥid ${selectedDate.vahid} (${selectedDate.vahidYearInfo.transliteration})"
                } else {
                    "Year ${selectedDate.vahidYearNumber} (${selectedDate.vahidYearInfo.transliteration}) • Váḥid ${selectedDate.vahid} • Kull-i-Shay' ${selectedDate.kullIShay}"
                }

                Text(
                    text = yearFormatted,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 13.sp
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Toggle notation",
                    tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                    modifier = Modifier.size(12.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Gregorian Date row
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "${selectedDate.weekdayInfo.transliteration} (${selectedDate.weekdayInfo.translation}) • ${selectedDate.formattedGregorianDate}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp
                )
            }

            // Holy Day or Feast notification banner
            if (selectedDate.holyDay != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF382300))
                        .border(1.dp, HolyDayGold.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    text = "Work Suspended • ${selectedDate.holyDay.commemorationTime}",
                                    color = Color(0xFFFFECB3),
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                }
            } else if (selectedDate.isFeastDay) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF00363A))
                        .border(1.dp, FeastTeal.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
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
        }
    }
}

@Composable
fun MainCalendarSection(
    uiState: BadiUiState,
    viewModel: BadiViewModel
) {
    var monthMenuOpen by remember { mutableStateOf(false) }

    val yearDetails = remember(uiState.displayedBadiYear) {
        BadiCalendarEngine.getYearDetails(uiState.displayedBadiYear)
    }

    val monthTitle = if (uiState.displayedBadiMonth == 0) {
        "Ayyám-i-Há (Intercalary Days)"
    } else {
        val m = BadiDate.MONTHS.getOrNull(uiState.displayedBadiMonth - 1)
        "${m?.transliteration ?: ""} (${m?.translation ?: ""})"
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(28.dp))
            .padding(16.dp)
            .testTag("main_calendar_section")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Year Selector Row (Short / Long notation + Year Picker Dialog trigger)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Clickable Year label that opens YearPickerDialog
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .weight(1f, fill = false)
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.toggleYearPicker(true) }
                        .padding(horizontal = 4.dp, vertical = 2.dp)
                        .testTag("open_year_picker_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarMonth,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        val yearText = if (uiState.yearNotationSystem == YearNotationSystem.SHORT_SYSTEM) {
                            "${yearDetails.year} B.E. (${yearDetails.gregorianYearSpan})"
                        } else {
                            "Year ${yearDetails.vahidYearNumber} (${yearDetails.vahidYearInfo.transliteration}) • V.${yearDetails.vahidNumber} • K.${yearDetails.kullIShayNumber}"
                        }
                        Text(
                            text = yearText,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    Text(
                        text = " ▾",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Year navigation controls & notation toggle
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Notation switch button (Short vs Long)
                    IconButton(
                        onClick = { viewModel.toggleYearNotationSystem() },
                        modifier = Modifier.size(30.dp).testTag("toggle_year_notation_inline_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Sync,
                            contentDescription = "Toggle notation",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.previousYear() },
                        modifier = Modifier.size(30.dp).testTag("prev_year_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Year",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = { viewModel.nextYear() },
                        modifier = Modifier.size(30.dp).testTag("next_year_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Year",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Month Header with Prev/Next
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { monthMenuOpen = true }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = monthTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "▾",
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 14.sp
                        )
                    }

                    DropdownMenu(
                        expanded = monthMenuOpen,
                        onDismissRequest = { monthMenuOpen = false },
                        modifier = Modifier.background(MaterialTheme.colorScheme.surface)
                    ) {
                        BadiDate.MONTHS.forEach { m ->
                            DropdownMenuItem(
                                text = {
                                    Text(
                                        text = "${m.number}. ${m.transliteration} (${m.translation})",
                                        color = if (m.number == uiState.displayedBadiMonth) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                    )
                                },
                                onClick = {
                                    viewModel.setMonth(m.number)
                                    monthMenuOpen = false
                                }
                            )
                        }
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "0. Ayyám-i-Há (Intercalary Days)",
                                    color = if (uiState.displayedBadiMonth == 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            },
                            onClick = {
                                viewModel.setMonth(0)
                                monthMenuOpen = false
                            }
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    IconButton(
                        onClick = { viewModel.previousMonth() },
                        modifier = Modifier.size(32.dp).testTag("prev_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronLeft,
                            contentDescription = "Previous Month",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = { viewModel.nextMonth() },
                        modifier = Modifier.size(32.dp).testTag("next_month_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Next Month",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 7-Column Badí' Weekdays Header: J, J, K, F, ‘I, I, I
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                val dayHeaders = listOf(
                    "J" to "Jalál (Sat)",
                    "J" to "Jamál (Sun)",
                    "K" to "Kamál (Mon)",
                    "F" to "Fiḍál (Tue)",
                    "‘I" to "‘Idál (Wed)",
                    "I" to "Istijbáb (Thu)",
                    "I" to "Istiqlál (Fri)"
                )
                dayHeaders.forEach { (abbr, fullName) ->
                    Box(
                        modifier = Modifier.width(36.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = abbr,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Grid of days (1..19 or 1..4/5)
            val days = uiState.daysInMonth
            val firstDayWeekday = if (days.isNotEmpty()) {
                // Determine day of week index for Badí' (0 = Saturday, 1 = Sunday, ..., 6 = Friday)
                val javaDay = days.first().gregorianDate.dayOfWeek.value // 1 = Mon ... 7 = Sun
                when (javaDay) {
                    6 -> 0 // Sat
                    7 -> 1 // Sun
                    1 -> 2 // Mon
                    2 -> 3 // Tue
                    3 -> 4 // Wed
                    4 -> 5 // Thu
                    5 -> 6 // Fri
                    else -> 0
                }
            } else 0

            // Grid cells with leading offset
            LazyVerticalGrid(
                columns = GridCells.Fixed(7),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (days.size <= 14) 110.dp else if (days.size <= 21) 160.dp else 210.dp),
                userScrollEnabled = false,
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Empty leading cells
                items(firstDayWeekday) {
                    Spacer(modifier = Modifier.size(36.dp))
                }

                // Days
                items(days) { badiDate ->
                    val isSelected = badiDate.day == uiState.selectedBadiDate.day &&
                            badiDate.month == uiState.selectedBadiDate.month &&
                            badiDate.year == uiState.selectedBadiDate.year
                    val isToday = badiDate.day == uiState.todayBadiDate.day &&
                            badiDate.month == uiState.todayBadiDate.month &&
                            badiDate.year == uiState.todayBadiDate.year

                    val hasEvents = (uiState.eventsByDate[badiDate.gregorianDate]?.size ?: 0) > 0
                    val isHolyDay = badiDate.holyDay != null
                    val isFeast = badiDate.isFeastDay

                    DayGridCell(
                        badiDate = badiDate,
                        isSelected = isSelected,
                        isToday = isToday,
                        hasEvents = hasEvents,
                        isHolyDay = isHolyDay,
                        isFeast = isFeast,
                        onClick = { viewModel.selectDate(badiDate) }
                    )
                }
            }
        }
    }
}

@Composable
fun DayGridCell(
    badiDate: BadiDate,
    isSelected: Boolean,
    isToday: Boolean,
    hasEvents: Boolean,
    isHolyDay: Boolean,
    isFeast: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(
                when {
                    isSelected -> MaterialTheme.colorScheme.primary
                    isToday -> MaterialTheme.colorScheme.primaryContainer
                    else -> Color.Transparent
                }
            )
            .border(
                width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                color = if (isToday && !isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
                shape = CircleShape
            )
            .clickable { onClick() }
            .testTag("badi_day_cell_${badiDate.day}"),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = badiDate.day.toString(),
                color = when {
                    isSelected -> MaterialTheme.colorScheme.onPrimary
                    isHolyDay -> MaterialTheme.colorScheme.tertiary
                    isFeast -> FeastTeal
                    isToday -> MaterialTheme.colorScheme.onPrimaryContainer
                    else -> MaterialTheme.colorScheme.onSurface
                },
                fontWeight = if (isSelected || isToday || isHolyDay) FontWeight.Bold else FontWeight.Normal,
                fontSize = 13.sp
            )

            // Indicators dot row
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 1.dp)
            ) {
                if (hasEvents) {
                    Box(
                        modifier = Modifier
                            .size(3.5.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary)
                    )
                }
                if (isHolyDay) {
                    Spacer(modifier = Modifier.width(2.dp))
                    Box(
                        modifier = Modifier
                            .size(3.5.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.tertiary)
                    )
                }
            }
        }
    }
}

@Composable
fun GoogleCalendarEventsSection(
    uiState: BadiUiState,
    viewModel: BadiViewModel,
    onRequestPermissions: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(DarkSurface)
            .border(1.dp, BorderColor, RoundedCornerShape(28.dp))
            .padding(18.dp)
            .testTag("calendar_events_section")
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "BADÍ' LOGICAL BLOCKS & APPOINTMENTS",
                    color = TextSecondary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.1.sp
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (uiState.hasCalendarPermission) {
                        IconButton(
                            onClick = { viewModel.refreshCalendarAccountsAndEvents() },
                            modifier = Modifier.size(28.dp).testTag("refresh_gcal_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Sync Calendar",
                                tint = LavenderPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    IconButton(
                        onClick = { viewModel.showAddEventDialog(true) },
                        modifier = Modifier.size(28.dp).testTag("add_event_section_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "New Appointment",
                            tint = LavenderPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (!uiState.hasCalendarPermission) {
                // Permission card prompt
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(DarkCardSurface)
                        .padding(14.dp)
                ) {
                    Column {
                        Text(
                            text = "Sync with Google Calendar",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Grant calendar access to view and add appointments synced across your Google accounts directly from the Badí' view.",
                            color = TextSecondary,
                            fontSize = 12.sp,
                            modifier = Modifier.padding(vertical = 4.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(
                            onClick = onRequestPermissions,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = LavenderPrimary,
                                contentColor = LavenderOnPrimary
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.testTag("grant_calendar_permission_button")
                        ) {
                            Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Connect Google Calendar", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Always show the 5 Badí' logical blocks with appointments embedded inside
            BadiLogicalBlocksPrepSection(
                uiState = uiState,
                onEditEvent = { event -> viewModel.showAddEventDialog(true, event) },
                onDeleteEvent = { eventId -> viewModel.deleteAppointment(eventId) },
                onAddEventForDate = { viewModel.showAddEventDialog(true) }
            )
        }
    }
}

@Composable
fun BadiLogicalBlocksPrepSection(
    uiState: BadiUiState,
    onEditEvent: (CalendarEvent) -> Unit,
    onDeleteEvent: (Long) -> Unit,
    onAddEventForDate: () -> Unit
) {
    val targetDate = uiState.selectedBadiDate.gregorianDate
    val zoneId = remember(uiState.currentLocation) {
        try { ZoneId.of(uiState.currentLocation.timeZoneId) } catch (_: Exception) { ZoneId.systemDefault() }
    }
    val timeFormatter = remember { DateTimeFormatter.ofPattern("h:mm a") }

    val sunsetToday = remember(targetDate, uiState.currentLocation) {
        BadiCalendarEngine.calculateSunset(targetDate, uiState.currentLocation.latitude, uiState.currentLocation.longitude, zoneId)
    }
    val sunriseToday = remember(targetDate, uiState.currentLocation) {
        BadiCalendarEngine.calculateSunrise(targetDate, uiState.currentLocation.latitude, uiState.currentLocation.longitude, zoneId)
    }
    val sunsetPrev = remember(targetDate, uiState.currentLocation) {
        BadiCalendarEngine.calculateSunset(targetDate.minusDays(1), uiState.currentLocation.latitude, uiState.currentLocation.longitude, zoneId)
    }
    val sunriseTomorrow = remember(targetDate, uiState.currentLocation) {
        BadiCalendarEngine.calculateSunrise(targetDate.plusDays(1), uiState.currentLocation.latitude, uiState.currentLocation.longitude, zoneId)
    }
    val sunsetTomorrow = remember(targetDate, uiState.currentLocation) {
        BadiCalendarEngine.calculateSunset(targetDate.plusDays(1), uiState.currentLocation.latitude, uiState.currentLocation.longitude, zoneId)
    }
    val sunriseDayAfter = remember(targetDate, uiState.currentLocation) {
        BadiCalendarEngine.calculateSunrise(targetDate.plusDays(2), uiState.currentLocation.latitude, uiState.currentLocation.longitude, zoneId)
    }

    // Epoch millisecond bounds for accurate event mapping
    val b1Start = remember(targetDate, sunsetPrev) { targetDate.minusDays(1).atTime(sunsetPrev).atZone(zoneId).toInstant().toEpochMilli() }
    val b1End = remember(targetDate, sunriseToday) { targetDate.atTime(sunriseToday).atZone(zoneId).toInstant().toEpochMilli() }

    val b2Start = b1End
    val b2End = remember(targetDate, sunsetToday) { targetDate.atTime(sunsetToday).atZone(zoneId).toInstant().toEpochMilli() }

    val b3Start = b2End
    val b3End = remember(targetDate, sunriseTomorrow) { targetDate.plusDays(1).atTime(sunriseTomorrow).atZone(zoneId).toInstant().toEpochMilli() }

    val b4Start = b3End
    val b4End = remember(targetDate, sunsetTomorrow) { targetDate.plusDays(1).atTime(sunsetTomorrow).atZone(zoneId).toInstant().toEpochMilli() }

    val b5Start = b4End
    val b5End = remember(targetDate, sunriseDayAfter) { targetDate.plusDays(2).atTime(sunriseDayAfter).atZone(zoneId).toInstant().toEpochMilli() }

    // Combine all events across date window
    val allEvents = remember(uiState.eventsByDate, targetDate) {
        val days = listOf(targetDate.minusDays(1), targetDate, targetDate.plusDays(1), targetDate.plusDays(2))
        days.flatMap { uiState.eventsByDate[it] ?: emptyList() }.distinctBy { it.id }
    }

    data class BlockDefinition(
        val title: String,
        val timeRangeStr: String,
        val badge: String,
        val startMillis: Long,
        val endMillis: Long,
        val isCurrentPeriod: Boolean
    )

    val nowEpoch = Instant.now().toEpochMilli()

    val blockDefs = listOf(
        BlockDefinition("🌙 Evening Start (Badí' Day Begin)", "${sunsetPrev.format(timeFormatter)} (Yesterday) – ${sunriseToday.format(timeFormatter)} (Today)", "Current Badí' Eve", b1Start, b1End, nowEpoch in b1Start until b1End),
        BlockDefinition("☀️ Daytime Portion (Sunrise to Sunset)", "${sunriseToday.format(timeFormatter)} – ${sunsetToday.format(timeFormatter)}", "Current Day Portion", b2Start, b2End, nowEpoch in b2Start until b2End),
        BlockDefinition("🌙 Next Evening Start (Next Badí' Day)", "${sunsetToday.format(timeFormatter)} – ${sunriseTomorrow.format(timeFormatter)} (Tomorrow)", "Sunset Transition", b3Start, b3End, nowEpoch in b3Start until b3End),
        BlockDefinition("☀️ +1 Block Ahead (Tomorrow Daytime)", "${sunriseTomorrow.format(timeFormatter)} – ${sunsetTomorrow.format(timeFormatter)}", "Prep Tomorrow", b4Start, b4End, nowEpoch in b4Start until b4End),
        BlockDefinition("🌙 +2 Blocks Ahead (Day After Evening)", "${sunsetTomorrow.format(timeFormatter)} – ${sunriseDayAfter.format(timeFormatter)}", "Prep Upcoming", b5Start, b5End, nowEpoch in b5Start until b5End)
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        blockDefs.forEach { block ->
            val blockEvents = allEvents.filter { it.startMillis < block.endMillis && it.endMillis > block.startMillis }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(if (block.isCurrentPeriod) LavenderPrimary.copy(alpha = 0.12f) else DarkCardSurface.copy(alpha = 0.8f))
                    .border(
                        1.dp,
                        if (block.isCurrentPeriod) LavenderPrimary.copy(alpha = 0.4f) else BorderColor.copy(alpha = 0.6f),
                        RoundedCornerShape(16.dp)
                    )
                    .padding(12.dp)
            ) {
                // Block Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = block.title,
                                color = if (block.isCurrentPeriod) LavenderPrimary else TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            if (block.isCurrentPeriod) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(LavenderPrimary)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "NOW",
                                        color = LavenderOnPrimary,
                                        fontSize = 8.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = block.timeRangeStr,
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(LavenderPrimary.copy(alpha = 0.18f))
                            .padding(horizontal = 6.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = block.badge,
                            color = LavenderPrimary,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Appointments inside this block
                if (blockEvents.isNotEmpty()) {
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        blockEvents.forEach { event ->
                            EventItemCard(
                                event = event,
                                badiDate = uiState.selectedBadiDate,
                                timeSystemMode = uiState.timeSystemMode,
                                onEdit = { onEditEvent(event) },
                                onDelete = { onDeleteEvent(event.id) }
                            )
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(DarkSurface.copy(alpha = 0.5f))
                            .padding(horizontal = 10.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (block.badge.startsWith("Prep")) "No appointments scheduled • Ready to prep" else "No appointments scheduled for this period",
                            color = TextTertiary,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EventItemCard(
    event: CalendarEvent,
    badiDate: BadiDate,
    timeSystemMode: TimeSystemMode = TimeSystemMode.STANDARD_CIVIL,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(DarkSurface)
            .border(1.dp, BorderColor.copy(alpha = 0.4f), RoundedCornerShape(14.dp))
            .padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left accent pill (#D0BCFF)
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(36.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(LavenderPrimary)
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = event.title,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.5.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = event.getFormattedTimeRange(timeSystemMode),
                color = LavenderPrimary,
                fontWeight = FontWeight.Medium,
                fontSize = 11.sp
            )
            if (!event.location.isNullOrBlank()) {
                Text(
                    text = "📍 ${event.location}",
                    color = TextSecondary,
                    fontSize = 10.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        // Action Buttons: Edit and Delete
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onEdit,
                modifier = Modifier.size(28.dp).testTag("edit_event_${event.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit Event",
                    tint = LavenderPrimary,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(28.dp).testTag("delete_event_${event.id}")
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete Event",
                    tint = TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun HolyDaysTabContent(
    uiState: BadiUiState,
    onHolyDayClick: (BadiHolyDay) -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            text = "Bahá'í Holy Days (${uiState.displayedBadiYear} B.E.)",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "On nine of the eleven Holy Days, work and school are suspended.",
            color = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(4.dp))

        uiState.allYearHolyDays.forEach { (holyDay, gregDate) ->
            val fmt = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, BorderColor, RoundedCornerShape(20.dp))
                    .clickable { onHolyDayClick(holyDay) }
                    .testTag("holy_day_card_${holyDay.id}"),
                colors = CardDefaults.cardColors(containerColor = DarkSurface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = holyDay.name,
                            color = HolyDayGold,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.weight(1f)
                        )
                        if (holyDay.isWorkSuspended) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF4C3600))
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = "Work Suspended",
                                    color = HolyDayGold,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = gregDate.format(fmt),
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Commemoration: ${holyDay.commemorationTime}",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun YearCyclesTabContent(uiState: BadiUiState) {
    val scrollState = rememberScrollState()
    val today = uiState.todayBadiDate

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Badí' Calendar Architecture",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )

        // Structure Overview Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "Current Era & Cycles",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                CycleItem("Bahá'í Era (B.E.)", "${today.year} B.E. (Started 1844 CE)")
                CycleItem("Kull-i-Shay'", "Cycle ${today.kullIShay} of 361 Years")
                CycleItem("Váḥid", "Váḥid ${today.vahid} of 19 (Year in Váḥid: ${today.vahidYearNumber})")
                CycleItem("Year of Váḥid", "${today.vahidYearInfo.transliteration} (${today.vahidYearInfo.translation})")
                CycleItem("Months in Year", "19 Months of 19 Days = 361 Days")
                CycleItem("Ayyám-i-Há", "${BadiCalendarEngine.getAyyamIHaLength(today.year)} Intercalary Days")
            }
        }

        // The 4 Badí' Elemental Seasons
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    text = "The 4 Elemental Seasons of the Year",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
                Text(
                    text = "The Badí' year and universal etime are divided into four elemental stations:",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(top = 2.dp, bottom = 8.dp)
                )

                ElementalTimeSeason.values().forEach { season ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(season.emoji, fontSize = 16.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${season.englishName} (${season.arabicName})",
                                fontWeight = FontWeight.Bold,
                                color = Color(season.accentColorHex),
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Months: ${season.badiMonths}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }

        // 19 Months list
        Text(
            text = "The 19 Months of the Year",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )

        BadiDate.MONTHS.forEach { m ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(14.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "${m.number}. ${m.transliteration}",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "• ${m.translation}",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Text(
                        text = m.meaning,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        lineHeight = 14.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = m.arabic,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = m.translation,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun CycleItem(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, color = MaterialTheme.colorScheme.onSurfaceVariant, fontSize = 13.sp)
        Text(value, color = MaterialTheme.colorScheme.onSurface, fontWeight = FontWeight.Medium, fontSize = 13.sp)
    }
}

@Composable
fun AppointmentsTabContent(
    uiState: BadiUiState,
    viewModel: BadiViewModel,
    onRequestPermissions: () -> Unit,
    onOpenThemePicker: () -> Unit,
    onOpenTimeSystemPicker: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    val etime = remember { ElementalTimeEngine.calculate() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Time System & Elemental Universal Time (etime / tenpo ko) Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Time System & Universal Time",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Elemental etime (4×6h Blocks) & Civil Time",
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 11.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { onOpenTimeSystemPicker() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("change_time_system_button")
                    ) {
                        Text(
                            text = "Switch",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Current Active Time Mode Pill
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onOpenTimeSystemPicker() }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (uiState.timeSystemMode) {
                                TimeSystemMode.STANDARD_CIVIL -> Icons.Default.WbSunny
                                TimeSystemMode.ELEMENTAL_ETIME -> Icons.Default.Public
                                TimeSystemMode.DUAL_DISPLAY -> Icons.Default.Schedule
                            },
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = uiState.timeSystemMode.displayName,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = uiState.timeSystemMode.shortBadge,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = uiState.timeSystemMode.description,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Mode Selector Options
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TimeSystemMode.values().forEach { mode ->
                        val isSelected = uiState.timeSystemMode == mode
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { viewModel.setTimeSystemMode(mode) }
                        ) {
                            Column(
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = when (mode) {
                                        TimeSystemMode.STANDARD_CIVIL -> "☀️ Civil"
                                        TimeSystemMode.ELEMENTAL_ETIME -> "🔥 etime"
                                        TimeSystemMode.DUAL_DISPLAY -> "⏱️ Dual"
                                    },
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // App Theme & Styling Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Column(modifier = Modifier.padding(18.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Appearance & Theme",
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(MaterialTheme.colorScheme.primaryContainer)
                            .clickable { onOpenThemePicker() }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("change_theme_button")
                    ) {
                        Text(
                            text = "Change",
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onOpenThemePicker() }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(uiState.currentTheme.previewBg)
                            .border(1.dp, uiState.currentTheme.previewColor, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(uiState.currentTheme.previewColor)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = uiState.currentTheme.displayName,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = uiState.currentTheme.description,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }

        Text(
            text = "Google Calendar Sync",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )

        if (!uiState.hasCalendarPermission) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(DarkSurface)
                    .border(1.dp, BorderColor, RoundedCornerShape(24.dp))
                    .padding(20.dp)
            ) {
                Column {
                    Text(
                        text = "Connect Google Calendar Accounts",
                        color = LavenderPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Connect your on-device Google accounts to seamlessly view and manage events mapped against the 19 Badí' months.",
                        color = TextSecondary,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Button(
                        onClick = onRequestPermissions,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = LavenderPrimary,
                            contentColor = LavenderOnPrimary
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Sync, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Grant Calendar Access")
                    }
                }
            }
        } else {
            // Connected Accounts
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Connected Google Accounts",
                            color = LavenderPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        IconButton(
                            onClick = { viewModel.refreshCalendarAccountsAndEvents() },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = "Refresh", tint = LavenderPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    if (uiState.calendarAccounts.isEmpty()) {
                        Text(
                            text = "No Google Calendar accounts found on device.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    } else {
                        uiState.calendarAccounts.forEach { acc ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(Color(acc.color))
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(acc.displayName, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                                    Text(acc.accountName, color = TextTertiary, fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }

            // Upcoming Feasts & Holy Days
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Upcoming Feasts & Holy Days",
                        color = HolyDayGold,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))

                    uiState.upcomingHolyDaysAndFeasts.forEach { item ->
                        val fmt = DateTimeFormatter.ofPattern("EEE, MMM d")
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                val label = when {
                                    item.holyDay != null -> "✨ ${item.holyDay.name}"
                                    item.isFeastDay -> "🕊️ Feast of ${item.monthInfo.transliteration}"
                                    item.isAyyamIHa -> "🎁 Ayyám-i-Há Day ${item.day}"
                                    else -> item.formattedBadiDate
                                }
                                Text(label, color = TextPrimary, fontWeight = FontWeight.Medium, fontSize = 13.sp)
                                Text("${item.day} ${item.monthInfo.transliteration} ${item.year} B.E.", color = TextTertiary, fontSize = 11.sp)
                            }
                            Text(
                                text = item.gregorianDate.format(fmt),
                                color = LavenderPrimary,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Synced Google Calendar Appointments List
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor)
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Month Appointments (${uiState.eventsByDate.values.flatten().size})",
                            color = LavenderPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                        IconButton(
                            onClick = { viewModel.showAddEventDialog(true) },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add", tint = LavenderPrimary)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    val allMonthEvents = uiState.eventsByDate.entries
                        .flatMap { (date, events) -> events.map { date to it } }
                        .sortedBy { it.second.startMillis }

                    if (allMonthEvents.isEmpty()) {
                        Text(
                            text = "No appointments found for this Badí' month.",
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            allMonthEvents.forEach { (_, event) ->
                                EventItemCard(
                                    event = event,
                                    badiDate = uiState.selectedBadiDate,
                                    onEdit = { viewModel.showAddEventDialog(true, event) },
                                    onDelete = { viewModel.deleteAppointment(event.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(60.dp))
    }
}

@Composable
fun BadiBottomNavigationBar(
    currentTab: CalendarTab,
    onSelectTab: (CalendarTab) -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        tonalElevation = 6.dp,
        modifier = Modifier.testTag("bottom_nav_bar")
    ) {
        NavigationBarItem(
            selected = currentTab == CalendarTab.MONTH_VIEW,
            onClick = { onSelectTab(CalendarTab.MONTH_VIEW) },
            icon = {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "Calendar"
                )
            },
            label = {
                Text(
                    text = "Calendar",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == CalendarTab.MONTH_VIEW) FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_tab_calendar")
        )

        NavigationBarItem(
            selected = currentTab == CalendarTab.HOLY_DAYS,
            onClick = { onSelectTab(CalendarTab.HOLY_DAYS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "Holy Days"
                )
            },
            label = {
                Text(
                    text = "Holy Days",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == CalendarTab.HOLY_DAYS) FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_tab_holy_days")
        )

        NavigationBarItem(
            selected = currentTab == CalendarTab.DEVOTIONALS,
            onClick = { onSelectTab(CalendarTab.DEVOTIONALS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.AutoStories,
                    contentDescription = "Devotions"
                )
            },
            label = {
                Text(
                    text = "Devotions",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == CalendarTab.DEVOTIONALS) FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_tab_devotionals")
        )

        NavigationBarItem(
            selected = currentTab == CalendarTab.YEAR_CYCLES,
            onClick = { onSelectTab(CalendarTab.YEAR_CYCLES) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Cycles"
                )
            },
            label = {
                Text(
                    text = "Cycles",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == CalendarTab.YEAR_CYCLES) FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_tab_cycles")
        )

        NavigationBarItem(
            selected = currentTab == CalendarTab.APPOINTMENTS,
            onClick = { onSelectTab(CalendarTab.APPOINTMENTS) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Sync,
                    contentDescription = "Sync & Setup"
                )
            },
            label = {
                Text(
                    text = "Sync & Setup",
                    fontSize = 11.sp,
                    fontWeight = if (currentTab == CalendarTab.APPOINTMENTS) FontWeight.Bold else FontWeight.Medium
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                selectedTextColor = MaterialTheme.colorScheme.primary,
                indicatorColor = MaterialTheme.colorScheme.primaryContainer,
                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            modifier = Modifier.testTag("nav_tab_sync")
        )
    }
}

@Composable
fun HolyDayDetailDialog(
    holyDay: BadiHolyDay,
    badiYear: Int,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = DarkSurface,
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderColor),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = holyDay.name,
                        color = HolyDayGold,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                Text(
                    text = holyDay.arabicName,
                    color = LavenderPrimary,
                    fontSize = 18.sp,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkCardSurface)
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = if (holyDay.isWorkSuspended) "Work & School Suspended" else "Work Not Suspended",
                            color = if (holyDay.isWorkSuspended) HolyDayGold else TextSecondary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Standard Commemoration Time: ${holyDay.commemorationTime}",
                            color = TextPrimary,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = holyDay.description,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    lineHeight = 18.sp
                )

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = onDismiss,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = LavenderPrimary,
                        contentColor = LavenderOnPrimary
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
