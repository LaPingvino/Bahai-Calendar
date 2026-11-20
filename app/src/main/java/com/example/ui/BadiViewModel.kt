package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.badi.BadiCalendarEngine
import com.example.badi.BadiDate
import com.example.badi.BadiHolyDay
import com.example.badi.TimeSystemMode
import com.example.calendar.CalendarEvent
import com.example.calendar.CalendarRepository
import com.example.calendar.GoogleCalendarAccount
import com.example.calendar.NewCalendarEvent
import com.example.widget.BadiAppWidgetProvider
import com.example.ui.theme.AppThemeMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime

import com.example.devotional.CityLocation
import com.example.devotional.DevotionalRepository
import com.example.location.LocationHelper

enum class CalendarTab {
    MONTH_VIEW,
    HOLY_DAYS,
    DEVOTIONALS,
    YEAR_CYCLES,
    APPOINTMENTS
}

enum class YearNotationSystem {
    SHORT_SYSTEM, // e.g. 183 B.E.
    LONG_SYSTEM   // e.g. Year 12 (Javáb), Váḥid 10, Kull-i-Shay' 1
}

data class BadiUiState(
    val todayBadiDate: BadiDate,
    val selectedBadiDate: BadiDate,
    val displayedBadiMonth: Int, // 1..19 or 0
    val displayedBadiYear: Int,
    val yearNotationSystem: YearNotationSystem = YearNotationSystem.SHORT_SYSTEM,
    val daysInMonth: List<BadiDate> = emptyList(),
    val eventsByDate: Map<LocalDate, List<CalendarEvent>> = emptyMap(),
    val calendarAccounts: List<GoogleCalendarAccount> = emptyList(),
    val selectedCalendarId: Long? = null,
    val hasCalendarPermission: Boolean = false,
    val hasLocationPermission: Boolean = false,
    val isDetectingLocation: Boolean = false,
    val upcomingHolyDaysAndFeasts: List<BadiDate> = emptyList(),
    val allYearHolyDays: List<Pair<BadiHolyDay, LocalDate>> = emptyList(),
    val isAfterSunsetMode: Boolean = false,
    val showAddEventDialog: Boolean = false,
    val editingEvent: CalendarEvent? = null,
    val selectedHolyDayDetail: BadiHolyDay? = null,
    val showYearPicker: Boolean = false,
    val showMonthPicker: Boolean = false,
    val activeTab: CalendarTab = CalendarTab.MONTH_VIEW,
    val currentTheme: AppThemeMode = AppThemeMode.ELEGANT_DARK,
    val showThemePicker: Boolean = false,
    val timeSystemMode: TimeSystemMode = TimeSystemMode.STANDARD_CIVIL,
    val showTimeSystemPicker: Boolean = false,
    val currentLocation: CityLocation = DevotionalRepository.PRESET_LOCATIONS[0],
    val snackbarMessage: String? = null
)

class BadiViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = CalendarRepository(application)

    private val _uiState = MutableStateFlow(createInitialState())
    val uiState: StateFlow<BadiUiState> = _uiState.asStateFlow()

    init {
        loadUpcomingEvents()
        refreshCalendarAccountsAndEvents()
        if (LocationHelper.hasLocationPermission(application)) {
            _uiState.update { it.copy(hasLocationPermission = true) }
            detectUserLocation()
        }
    }

    private fun settingsPrefs() =
        getApplication<Application>().getSharedPreferences("badi_settings_prefs", android.content.Context.MODE_PRIVATE)

    private fun resolveZoneId(location: CityLocation): ZoneId =
        try {
            ZoneId.of(location.timeZoneId)
        } catch (_: Exception) {
            ZoneId.systemDefault()
        }

    private fun computeTodayBadiDate(
        location: CityLocation,
        overrideTime: LocalTime? = null
    ): BadiDate {
        val zoneId = resolveZoneId(location)
        val zdt = ZonedDateTime.now(zoneId)
        return BadiCalendarEngine.gregorianToBadi(
            date = zdt.toLocalDate(),
            time = overrideTime ?: zdt.toLocalTime(),
            latitude = location.latitude,
            longitude = location.longitude,
            zoneId = zoneId
        )
    }

    private fun getSavedTimeSystemMode(): TimeSystemMode {
        val modeName = settingsPrefs().getString("key_time_system_mode", TimeSystemMode.STANDARD_CIVIL.name)
        return try {
            TimeSystemMode.valueOf(modeName ?: TimeSystemMode.STANDARD_CIVIL.name)
        } catch (_: Exception) {
            TimeSystemMode.STANDARD_CIVIL
        }
    }

    private fun saveTimeSystemMode(mode: TimeSystemMode) {
        settingsPrefs().edit().putString("key_time_system_mode", mode.name).apply()
    }

    private fun getSavedThemeMode(): AppThemeMode {
        val modeName = settingsPrefs().getString("key_app_theme_mode", AppThemeMode.ELEGANT_DARK.name)
        return try {
            AppThemeMode.valueOf(modeName ?: AppThemeMode.ELEGANT_DARK.name)
        } catch (_: Exception) {
            AppThemeMode.ELEGANT_DARK
        }
    }

    private fun saveThemeMode(mode: AppThemeMode) {
        settingsPrefs().edit().putString("key_app_theme_mode", mode.name).apply()
    }

    private fun getSavedYearNotationSystem(): YearNotationSystem {
        val modeName = settingsPrefs().getString("key_year_notation_system", YearNotationSystem.SHORT_SYSTEM.name)
        return try {
            YearNotationSystem.valueOf(modeName ?: YearNotationSystem.SHORT_SYSTEM.name)
        } catch (_: Exception) {
            YearNotationSystem.SHORT_SYSTEM
        }
    }

    private fun saveYearNotationSystem(system: YearNotationSystem) {
        settingsPrefs().edit().putString("key_year_notation_system", system.name).apply()
    }

    private fun createInitialState(): BadiUiState {
        val savedLocation = LocationHelper.getSavedLocation(getApplication())
        val todayBadi = computeTodayBadiDate(savedLocation)
        val days = BadiCalendarEngine.getDaysInMonth(todayBadi.year, todayBadi.month)
        val allHolyDays = BadiCalendarEngine.getAllHolyDaysForYear(todayBadi.year)

        return BadiUiState(
            todayBadiDate = todayBadi,
            selectedBadiDate = todayBadi,
            displayedBadiMonth = todayBadi.month,
            displayedBadiYear = todayBadi.year,
            yearNotationSystem = getSavedYearNotationSystem(),
            daysInMonth = days,
            allYearHolyDays = allHolyDays,
            currentLocation = savedLocation,
            currentTheme = getSavedThemeMode(),
            timeSystemMode = getSavedTimeSystemMode(),
            hasCalendarPermission = repository.hasCalendarPermissions(),
            hasLocationPermission = LocationHelper.hasLocationPermission(getApplication())
        )
    }

    private fun applyLocationAndRefresh(
        location: CityLocation,
        snackbarMessage: String? = null
    ) {
        val prevState = _uiState.value
        val overrideTime = if (prevState.isAfterSunsetMode) LocalTime.of(22, 0) else null
        val newToday = computeTodayBadiDate(location, overrideTime)
        val wasViewingToday = prevState.selectedBadiDate.year == prevState.todayBadiDate.year &&
            prevState.selectedBadiDate.month == prevState.todayBadiDate.month &&
            prevState.selectedBadiDate.day == prevState.todayBadiDate.day

        val updatedDays = if (wasViewingToday) {
            BadiCalendarEngine.getDaysInMonth(newToday.year, newToday.month)
        } else {
            prevState.daysInMonth
        }
        val updatedHolyDays = if (wasViewingToday) {
            BadiCalendarEngine.getAllHolyDaysForYear(newToday.year)
        } else {
            prevState.allYearHolyDays
        }

        _uiState.update { state ->
            state.copy(
                currentLocation = location,
                isDetectingLocation = false,
                todayBadiDate = newToday,
                selectedBadiDate = if (wasViewingToday) newToday else state.selectedBadiDate,
                displayedBadiYear = if (wasViewingToday) newToday.year else state.displayedBadiYear,
                displayedBadiMonth = if (wasViewingToday) newToday.month else state.displayedBadiMonth,
                daysInMonth = updatedDays,
                allYearHolyDays = updatedHolyDays,
                snackbarMessage = snackbarMessage ?: state.snackbarMessage
            )
        }
        loadUpcomingEvents()
        loadEventsForMonth(updatedDays)
        BadiAppWidgetProvider.updateAllWidgets(getApplication())
    }

    fun refreshTodayIfNeeded() {
        val state = _uiState.value
        if (state.isAfterSunsetMode) return

        val newToday = computeTodayBadiDate(state.currentLocation)
        if (newToday != state.todayBadiDate) {
            val wasViewingToday = state.selectedBadiDate.year == state.todayBadiDate.year &&
                state.selectedBadiDate.month == state.todayBadiDate.month &&
                state.selectedBadiDate.day == state.todayBadiDate.day

            val updatedDays = if (wasViewingToday) {
                BadiCalendarEngine.getDaysInMonth(newToday.year, newToday.month)
            } else {
                state.daysInMonth
            }
            val updatedHolyDays = if (wasViewingToday) {
                BadiCalendarEngine.getAllHolyDaysForYear(newToday.year)
            } else {
                state.allYearHolyDays
            }

            _uiState.update { current ->
                current.copy(
                    todayBadiDate = newToday,
                    selectedBadiDate = if (wasViewingToday) newToday else current.selectedBadiDate,
                    displayedBadiYear = if (wasViewingToday) newToday.year else current.displayedBadiYear,
                    displayedBadiMonth = if (wasViewingToday) newToday.month else current.displayedBadiMonth,
                    daysInMonth = updatedDays,
                    allYearHolyDays = updatedHolyDays
                )
            }
            loadUpcomingEvents()
            loadEventsForMonth(updatedDays)
            BadiAppWidgetProvider.updateAllWidgets(getApplication())
        }
    }

    fun onLocationPermissionResult(isGranted: Boolean) {
        _uiState.update { it.copy(hasLocationPermission = isGranted) }
        if (isGranted) {
            detectUserLocation()
        }
    }

    fun detectUserLocation() {
        viewModelScope.launch {
            _uiState.update { it.copy(isDetectingLocation = true) }
            val detected = LocationHelper.getCurrentOrLastKnownLocation(getApplication())
            if (detected != null) {
                applyLocationAndRefresh(detected, snackbarMessage = "Location updated: ${detected.cityName}")
            } else {
                _uiState.update { it.copy(isDetectingLocation = false) }
            }
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        _uiState.update { it.copy(hasCalendarPermission = isGranted) }
        if (isGranted) {
            refreshCalendarAccountsAndEvents()
        } else {
            _uiState.update { it.copy(eventsByDate = emptyMap(), calendarAccounts = emptyList()) }
        }
    }

    fun selectDate(badiDate: BadiDate) {
        _uiState.update { state ->
            val isTodayCell = badiDate.year == state.todayBadiDate.year &&
                badiDate.month == state.todayBadiDate.month &&
                badiDate.day == state.todayBadiDate.day
            state.copy(selectedBadiDate = if (isTodayCell) state.todayBadiDate else badiDate)
        }
    }

    fun selectTab(tab: CalendarTab) {
        _uiState.update { it.copy(activeTab = tab) }
    }

    fun nextMonth() {
        val currentMonth = _uiState.value.displayedBadiMonth
        val currentYear = _uiState.value.displayedBadiYear

        val (newYear, newMonth) = when (currentMonth) {
            18 -> currentYear to 0 // Mulk -> Ayyám-i-Há
            0 -> currentYear to 19 // Ayyám-i-Há -> 'Alá'
            19 -> (currentYear + 1) to 1 // 'Alá' -> Bahá (next year)
            else -> currentYear to (currentMonth + 1)
        }

        navigateToYearMonth(newYear, newMonth)
    }

    fun previousMonth() {
        val currentMonth = _uiState.value.displayedBadiMonth
        val currentYear = _uiState.value.displayedBadiYear

        val (newYear, newMonth) = when (currentMonth) {
            1 -> (currentYear - 1) to 19 // Bahá -> 'Alá' (prev year)
            19 -> currentYear to 0 // 'Alá' -> Ayyám-i-Há
            0 -> currentYear to 18 // Ayyám-i-Há -> Mulk
            else -> currentYear to (currentMonth - 1)
        }

        navigateToYearMonth(newYear, newMonth)
    }

    fun setMonth(month: Int) {
        navigateToYearMonth(_uiState.value.displayedBadiYear, month)
    }

    fun setYear(year: Int) {
        val safeYear = year.coerceAtLeast(1)
        navigateToYearMonth(safeYear, _uiState.value.displayedBadiMonth)
    }

    fun nextYear() {
        setYear(_uiState.value.displayedBadiYear + 1)
    }

    fun previousYear() {
        setYear((_uiState.value.displayedBadiYear - 1).coerceAtLeast(1))
    }

    fun setYearNotationSystem(system: YearNotationSystem) {
        saveYearNotationSystem(system)
        _uiState.update { it.copy(yearNotationSystem = system) }
    }

    fun toggleYearNotationSystem() {
        val next = if (_uiState.value.yearNotationSystem == YearNotationSystem.SHORT_SYSTEM) {
            YearNotationSystem.LONG_SYSTEM
        } else {
            YearNotationSystem.SHORT_SYSTEM
        }
        saveYearNotationSystem(next)
        _uiState.update { it.copy(yearNotationSystem = next) }
    }

    fun goToToday() {
        val savedLocation = _uiState.value.currentLocation
        val todayBadi = computeTodayBadiDate(savedLocation)
        _uiState.update { it.copy(isAfterSunsetMode = false, todayBadiDate = todayBadi) }
        navigateToYearMonth(todayBadi.year, todayBadi.month)
        _uiState.update { it.copy(selectedBadiDate = todayBadi, todayBadiDate = todayBadi) }
    }

    private fun navigateToYearMonth(year: Int, month: Int) {
        val days = BadiCalendarEngine.getDaysInMonth(year, month)
        val allHolyDays = BadiCalendarEngine.getAllHolyDaysForYear(year)
        val today = _uiState.value.todayBadiDate
        val matched = days.firstOrNull { it.day == _uiState.value.selectedBadiDate.day }
            ?: days.firstOrNull()
            ?: today
        val defaultSelected = if (matched.year == today.year && matched.month == today.month && matched.day == today.day) {
            today
        } else {
            matched
        }

        _uiState.update {
            it.copy(
                displayedBadiYear = year,
                displayedBadiMonth = month,
                daysInMonth = days,
                allYearHolyDays = allHolyDays,
                selectedBadiDate = defaultSelected,
                showMonthPicker = false,
                showYearPicker = false
            )
        }
        loadEventsForMonth(days)
    }

    fun toggleSunsetEveningSimulation() {
        val newMode = !_uiState.value.isAfterSunsetMode
        val savedLocation = _uiState.value.currentLocation
        val fakeTime = if (newMode) LocalTime.of(22, 0) else LocalTime.of(12, 0)
        val todayBadi = computeTodayBadiDate(savedLocation, overrideTime = fakeTime)

        _uiState.update {
            it.copy(
                isAfterSunsetMode = newMode,
                todayBadiDate = todayBadi,
                selectedBadiDate = todayBadi,
                displayedBadiYear = todayBadi.year,
                displayedBadiMonth = todayBadi.month,
                daysInMonth = BadiCalendarEngine.getDaysInMonth(todayBadi.year, todayBadi.month),
                allYearHolyDays = BadiCalendarEngine.getAllHolyDaysForYear(todayBadi.year),
                snackbarMessage = if (newMode) "Switched to Evening view (Post-sunset)" else "Switched to Daytime view"
            )
        }
    }

    fun showAddEventDialog(show: Boolean, eventToEdit: CalendarEvent? = null) {
        _uiState.update { it.copy(showAddEventDialog = show, editingEvent = eventToEdit) }
    }

    fun showHolyDayDetail(holyDay: BadiHolyDay?) {
        _uiState.update { it.copy(selectedHolyDayDetail = holyDay) }
    }

    fun toggleYearPicker(show: Boolean) {
        _uiState.update { it.copy(showYearPicker = show) }
    }

    fun toggleMonthPicker(show: Boolean) {
        _uiState.update { it.copy(showMonthPicker = show) }
    }

    fun setThemeMode(mode: AppThemeMode) {
        saveThemeMode(mode)
        _uiState.update {
            it.copy(
                currentTheme = mode,
                showThemePicker = false,
                snackbarMessage = "Theme changed to ${mode.displayName}"
            )
        }
    }

    fun showThemePicker(show: Boolean) {
        _uiState.update { it.copy(showThemePicker = show) }
    }

    fun setTimeSystemMode(mode: TimeSystemMode) {
        saveTimeSystemMode(mode)
        _uiState.update {
            it.copy(
                timeSystemMode = mode,
                showTimeSystemPicker = false,
                snackbarMessage = "Time system set to ${mode.displayName}"
            )
        }
        BadiAppWidgetProvider.updateAllWidgets(getApplication())
    }

    fun showTimeSystemPicker(show: Boolean) {
        _uiState.update { it.copy(showTimeSystemPicker = show) }
    }

    fun setSelectedCalendar(calendarId: Long?) {
        _uiState.update { it.copy(selectedCalendarId = calendarId) }
        loadEventsForMonth(_uiState.value.daysInMonth)
    }

    fun dismissSnackbar() {
        _uiState.update { it.copy(snackbarMessage = null) }
    }

    fun refreshCalendarAccountsAndEvents() {
        viewModelScope.launch(Dispatchers.IO) {
            val hasPerm = repository.hasCalendarPermissions()
            val accounts = if (hasPerm) repository.getCalendarAccounts() else emptyList()
            val primaryId = accounts.firstOrNull { it.isPrimary }?.id ?: accounts.firstOrNull()?.id

            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        calendarAccounts = accounts,
                        selectedCalendarId = it.selectedCalendarId ?: primaryId,
                        hasCalendarPermission = hasPerm
                    )
                }
                loadEventsForMonth(_uiState.value.daysInMonth)
            }
        }
    }

    private fun loadEventsForMonth(days: List<BadiDate>) {
        if (days.isEmpty()) return
        if (!repository.hasCalendarPermissions()) {
            _uiState.update { it.copy(eventsByDate = emptyMap()) }
            return
        }

        val location = _uiState.value.currentLocation
        val zoneId = resolveZoneId(location)
        val selectedCalendarId = _uiState.value.selectedCalendarId

        viewModelScope.launch(Dispatchers.IO) {
            val firstDate = days.first().primaryBadiGregorianDate
            val lastDate = days.last().primaryBadiGregorianDate

            // Pad by -1 day to +3 days so Badí' evening-before and +2 prep blocks are covered
            val startMillis = firstDate.minusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
            val endMillis = lastDate.plusDays(3).atStartOfDay(zoneId).toInstant().toEpochMilli()

            val events = repository.getEventsForRange(
                startMillis,
                endMillis,
                selectedCalendarId
            )

            val grouped = events.groupBy { event ->
                val badi = if (event.isAllDay) {
                    val utcDate = Instant.ofEpochMilli(event.startMillis).atZone(ZoneOffset.UTC).toLocalDate()
                    BadiCalendarEngine.gregorianToBadi(
                        date = utcDate,
                        time = null,
                        latitude = location.latitude,
                        longitude = location.longitude,
                        zoneId = zoneId
                    )
                } else {
                    val zdt = Instant.ofEpochMilli(event.startMillis).atZone(zoneId)
                    BadiCalendarEngine.gregorianToBadi(
                        date = zdt.toLocalDate(),
                        time = zdt.toLocalTime(),
                        latitude = location.latitude,
                        longitude = location.longitude,
                        zoneId = zoneId
                    )
                }
                badi.primaryBadiGregorianDate
            }

            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(eventsByDate = grouped) }
                BadiAppWidgetProvider.updateAllWidgets(getApplication())
            }
        }
    }

    private fun loadUpcomingEvents() {
        val location = _uiState.value.currentLocation
        val zoneId = resolveZoneId(location)
        val fromDate = _uiState.value.todayBadiDate.primaryBadiGregorianDate

        viewModelScope.launch(Dispatchers.IO) {
            val upcoming = BadiCalendarEngine.getUpcomingEvents(
                fromDate = fromDate,
                count = 12,
                latitude = location.latitude,
                longitude = location.longitude,
                zoneId = zoneId
            )
            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(upcomingHolyDaysAndFeasts = upcoming) }
            }
        }
    }

    fun addAppointment(
        title: String,
        description: String,
        location: String,
        startTime: LocalTime,
        endTime: LocalTime,
        isAllDay: Boolean,
        calendarId: Long,
        targetDate: LocalDate = _uiState.value.selectedBadiDate.primaryBadiGregorianDate
    ) {
        val zoneId = resolveZoneId(_uiState.value.currentLocation)
        viewModelScope.launch(Dispatchers.IO) {
            val startMillis = if (isAllDay) {
                targetDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            } else {
                targetDate.atTime(startTime).atZone(zoneId).toInstant().toEpochMilli()
            }

            val endMillis = if (isAllDay) {
                targetDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            } else {
                val endDate = if (!endTime.isAfter(startTime)) targetDate.plusDays(1) else targetDate
                endDate.atTime(endTime).atZone(zoneId).toInstant().toEpochMilli()
            }

            val result = repository.insertEvent(
                NewCalendarEvent(
                    title = title,
                    description = description,
                    location = location,
                    startMillis = startMillis,
                    endMillis = endMillis,
                    isAllDay = isAllDay,
                    calendarId = calendarId,
                    timeZoneId = if (isAllDay) "UTC" else zoneId.id
                )
            )

            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            showAddEventDialog = false,
                            editingEvent = null,
                            snackbarMessage = "Appointment added to Google Calendar!"
                        )
                    }
                    loadEventsForMonth(_uiState.value.daysInMonth)
                    BadiAppWidgetProvider.updateAllWidgets(getApplication())
                } else {
                    _uiState.update {
                        it.copy(
                            snackbarMessage = "Failed to add event: ${result.exceptionOrNull()?.localizedMessage ?: "Unknown error"}"
                        )
                    }
                }
            }
        }
    }

    fun editAppointment(
        eventId: Long,
        title: String,
        description: String,
        location: String,
        startTime: LocalTime,
        endTime: LocalTime,
        isAllDay: Boolean,
        calendarId: Long,
        targetDate: LocalDate = _uiState.value.selectedBadiDate.primaryBadiGregorianDate
    ) {
        val zoneId = resolveZoneId(_uiState.value.currentLocation)
        viewModelScope.launch(Dispatchers.IO) {
            val startMillis = if (isAllDay) {
                targetDate.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            } else {
                targetDate.atTime(startTime).atZone(zoneId).toInstant().toEpochMilli()
            }

            val endMillis = if (isAllDay) {
                targetDate.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            } else {
                val endDate = if (!endTime.isAfter(startTime)) targetDate.plusDays(1) else targetDate
                endDate.atTime(endTime).atZone(zoneId).toInstant().toEpochMilli()
            }

            val result = repository.updateEvent(
                eventId = eventId,
                calendarId = calendarId,
                title = title,
                description = description,
                location = location,
                startMillis = startMillis,
                endMillis = endMillis,
                isAllDay = isAllDay,
                timeZoneId = if (isAllDay) "UTC" else zoneId.id
            )

            withContext(Dispatchers.Main) {
                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(
                            showAddEventDialog = false,
                            editingEvent = null,
                            snackbarMessage = "Appointment updated in Google Calendar!"
                        )
                    }
                    loadEventsForMonth(_uiState.value.daysInMonth)
                    BadiAppWidgetProvider.updateAllWidgets(getApplication())
                } else {
                    _uiState.update {
                        it.copy(
                            snackbarMessage = "Failed to update event: ${result.exceptionOrNull()?.localizedMessage ?: "Unknown error"}"
                        )
                    }
                }
            }
        }
    }

    fun deleteAppointment(eventId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            val success = repository.deleteEvent(eventId)
            withContext(Dispatchers.Main) {
                if (success) {
                    _uiState.update { it.copy(snackbarMessage = "Event deleted") }
                    loadEventsForMonth(_uiState.value.daysInMonth)
                    BadiAppWidgetProvider.updateAllWidgets(getApplication())
                } else {
                    _uiState.update { it.copy(snackbarMessage = "Failed to delete event") }
                }
            }
        }
    }

    fun setLocation(location: CityLocation) {
        LocationHelper.saveLocation(getApplication(), location)
        applyLocationAndRefresh(location)
    }
}
