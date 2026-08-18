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
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId

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

    private fun getSavedTimeSystemMode(): TimeSystemMode {
        val prefs = getApplication<Application>().getSharedPreferences("badi_settings_prefs", android.content.Context.MODE_PRIVATE)
        val modeName = prefs.getString("key_time_system_mode", TimeSystemMode.STANDARD_CIVIL.name)
        return try {
            TimeSystemMode.valueOf(modeName ?: TimeSystemMode.STANDARD_CIVIL.name)
        } catch (_: Exception) {
            TimeSystemMode.STANDARD_CIVIL
        }
    }

    private fun saveTimeSystemMode(mode: TimeSystemMode) {
        val prefs = getApplication<Application>().getSharedPreferences("badi_settings_prefs", android.content.Context.MODE_PRIVATE)
        prefs.edit().putString("key_time_system_mode", mode.name).apply()
    }

    private fun createInitialState(): BadiUiState {
        val now = LocalDate.now()
        val time = LocalTime.now()
        val todayBadi = BadiCalendarEngine.gregorianToBadi(now, time)
        val days = BadiCalendarEngine.getDaysInMonth(todayBadi.year, todayBadi.month)
        val allHolyDays = BadiCalendarEngine.getAllHolyDaysForYear(todayBadi.year)
        val savedLocation = LocationHelper.getSavedLocation(getApplication())
        val savedTimeSystem = getSavedTimeSystemMode()

        return BadiUiState(
            todayBadiDate = todayBadi,
            selectedBadiDate = todayBadi,
            displayedBadiMonth = todayBadi.month,
            displayedBadiYear = todayBadi.year,
            daysInMonth = days,
            allYearHolyDays = allHolyDays,
            currentLocation = savedLocation,
            timeSystemMode = savedTimeSystem,
            hasCalendarPermission = repository.hasCalendarPermissions(),
            hasLocationPermission = LocationHelper.hasLocationPermission(getApplication())
        )
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
                _uiState.update {
                    it.copy(
                        currentLocation = detected,
                        isDetectingLocation = false,
                        snackbarMessage = "Location updated: ${detected.cityName}"
                    )
                }
                BadiAppWidgetProvider.updateAllWidgets(getApplication())
            } else {
                _uiState.update { it.copy(isDetectingLocation = false) }
            }
        }
    }

    fun onPermissionResult(isGranted: Boolean) {
        _uiState.update { it.copy(hasCalendarPermission = isGranted) }
        if (isGranted) {
            refreshCalendarAccountsAndEvents()
        }
    }

    fun selectDate(badiDate: BadiDate) {
        _uiState.update { it.copy(selectedBadiDate = badiDate) }
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
        _uiState.update { it.copy(yearNotationSystem = system) }
    }

    fun toggleYearNotationSystem() {
        _uiState.update {
            val next = if (it.yearNotationSystem == YearNotationSystem.SHORT_SYSTEM) {
                YearNotationSystem.LONG_SYSTEM
            } else {
                YearNotationSystem.SHORT_SYSTEM
            }
            it.copy(yearNotationSystem = next)
        }
    }

    fun goToToday() {
        val now = LocalDate.now()
        val time = LocalTime.now()
        val todayBadi = BadiCalendarEngine.gregorianToBadi(now, time)
        navigateToYearMonth(todayBadi.year, todayBadi.month)
        _uiState.update { it.copy(selectedBadiDate = todayBadi) }
    }

    private fun navigateToYearMonth(year: Int, month: Int) {
        val days = BadiCalendarEngine.getDaysInMonth(year, month)
        val allHolyDays = BadiCalendarEngine.getAllHolyDaysForYear(year)
        val defaultSelected = days.firstOrNull { it.day == _uiState.value.selectedBadiDate.day }
            ?: days.firstOrNull()
            ?: _uiState.value.todayBadiDate

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
        val current = _uiState.value.isAfterSunsetMode
        val newMode = !current
        val now = LocalDate.now()
        val fakeTime = if (newMode) LocalTime.of(22, 0) else LocalTime.of(12, 0)
        val todayBadi = BadiCalendarEngine.gregorianToBadi(now, fakeTime)

        _uiState.update {
            it.copy(
                isAfterSunsetMode = newMode,
                todayBadiDate = todayBadi,
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
            val accounts = repository.getCalendarAccounts()
            val primaryId = accounts.firstOrNull { it.isPrimary }?.id ?: accounts.firstOrNull()?.id

            withContext(Dispatchers.Main) {
                _uiState.update {
                    it.copy(
                        calendarAccounts = accounts,
                        selectedCalendarId = it.selectedCalendarId ?: primaryId,
                        hasCalendarPermission = repository.hasCalendarPermissions()
                    )
                }
                loadEventsForMonth(_uiState.value.daysInMonth)
            }
        }
    }

    private fun loadEventsForMonth(days: List<BadiDate>) {
        if (days.isEmpty() || !repository.hasCalendarPermissions()) return

        viewModelScope.launch(Dispatchers.IO) {
            val firstDate = days.first().gregorianDate
            val lastDate = days.last().gregorianDate

            val startMillis = firstDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            val endMillis = lastDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val events = repository.getEventsForRange(
                startMillis,
                endMillis,
                _uiState.value.selectedCalendarId
            )

            val grouped = events.groupBy { event ->
                val badi = if (event.isAllDay) {
                    BadiCalendarEngine.gregorianToBadi(event.startDateTime.toLocalDate(), null)
                } else {
                    BadiCalendarEngine.gregorianToBadi(event.startDateTime.toLocalDate(), event.startDateTime.toLocalTime())
                }
                badi.gregorianDate
            }

            withContext(Dispatchers.Main) {
                _uiState.update { it.copy(eventsByDate = grouped) }
                BadiAppWidgetProvider.updateAllWidgets(getApplication())
            }
        }
    }

    private fun loadUpcomingEvents() {
        viewModelScope.launch(Dispatchers.IO) {
            val upcoming = BadiCalendarEngine.getUpcomingEvents(LocalDate.now(), count = 12)
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
        targetDate: LocalDate = _uiState.value.selectedBadiDate.gregorianDate
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val startMillis = if (isAllDay) {
                targetDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } else {
                targetDate.atTime(startTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }

            val endMillis = if (isAllDay) {
                targetDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } else {
                targetDate.atTime(endTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }

            val result = repository.insertEvent(
                NewCalendarEvent(
                    title = title,
                    description = description,
                    location = location,
                    startMillis = startMillis,
                    endMillis = endMillis,
                    isAllDay = isAllDay,
                    calendarId = calendarId
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
        targetDate: LocalDate = _uiState.value.selectedBadiDate.gregorianDate
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            val startMillis = if (isAllDay) {
                targetDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } else {
                targetDate.atTime(startTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }

            val endMillis = if (isAllDay) {
                targetDate.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            } else {
                targetDate.atTime(endTime).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }

            val result = repository.updateEvent(
                eventId = eventId,
                calendarId = calendarId,
                title = title,
                description = description,
                location = location,
                startMillis = startMillis,
                endMillis = endMillis,
                isAllDay = isAllDay
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
        _uiState.update { it.copy(currentLocation = location) }
        BadiAppWidgetProvider.updateAllWidgets(getApplication())
    }
}
