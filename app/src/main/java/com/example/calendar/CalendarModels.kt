package com.example.calendar

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

/**
 * Calendar account information (e.g. Google Calendar).
 */
data class GoogleCalendarAccount(
    val id: Long,
    val displayName: String,
    val accountName: String,
    val accountType: String,
    val color: Int,
    val isPrimary: Boolean = false,
    val isGoogle: Boolean = accountType.equals("com.google", ignoreCase = true)
)

/**
 * Appointment or event from Google Calendar / CalendarContract.
 */
data class CalendarEvent(
    val id: Long,
    val calendarId: Long,
    val title: String,
    val description: String? = null,
    val location: String? = null,
    val startMillis: Long,
    val endMillis: Long,
    val isAllDay: Boolean = false,
    val color: Int? = null,
    val calendarName: String? = null,
    val accountName: String? = null
) {
    val startDateTime: LocalDateTime
        get() = LocalDateTime.ofInstant(Instant.ofEpochMilli(startMillis), ZoneId.systemDefault())

    val endDateTime: LocalDateTime
        get() = LocalDateTime.ofInstant(Instant.ofEpochMilli(endMillis), ZoneId.systemDefault())

    val formattedTimeRange: String
        get() = if (isAllDay) {
            "All Day"
        } else {
            val formatter = DateTimeFormatter.ofPattern("h:mm a")
            "${startDateTime.format(formatter)} – ${endDateTime.format(formatter)}"
        }

    fun getFormattedTimeRange(timeSystemMode: com.example.badi.TimeSystemMode): String {
        if (isAllDay) return "All Day"
        val formatter = DateTimeFormatter.ofPattern("h:mm a")
        val civilStr = "${startDateTime.format(formatter)} – ${endDateTime.format(formatter)}"
        val startEtime = com.example.badi.ElementalTimeEngine.calculate(Instant.ofEpochMilli(startMillis))
        val endEtime = com.example.badi.ElementalTimeEngine.calculate(Instant.ofEpochMilli(endMillis))
        val etimeStr = "${startEtime.formattedShort} – ${endEtime.formattedShort}"

        return when (timeSystemMode) {
            com.example.badi.TimeSystemMode.STANDARD_CIVIL -> civilStr
            com.example.badi.TimeSystemMode.ELEMENTAL_ETIME -> etimeStr
            com.example.badi.TimeSystemMode.DUAL_DISPLAY -> "$civilStr • $etimeStr"
        }
    }

    fun isEvening(sunsetTime: java.time.LocalTime?): Boolean {
        if (isAllDay || sunsetTime == null) return false
        return !startDateTime.toLocalTime().isBefore(sunsetTime)
    }
}

/**
 * Payload for adding a new appointment/event.
 */
data class NewCalendarEvent(
    val title: String,
    val description: String = "",
    val location: String = "",
    val startMillis: Long,
    val endMillis: Long,
    val isAllDay: Boolean = false,
    val calendarId: Long
)
