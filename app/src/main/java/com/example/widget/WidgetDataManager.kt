package com.example.widget

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.badi.BadiCalendarEngine
import com.example.badi.BadiDate
import com.example.badi.ElementalTimeEngine
import com.example.badi.TimeSystemMode
import com.example.calendar.CalendarEvent
import com.example.calendar.CalendarRepository
import com.example.devotional.CityLocation
import com.example.location.LocationHelper
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

data class WidgetAgendaItem(
    val id: Long,
    val title: String,
    val timeText: String,
    val dayLabel: String,
    val subtitle: String?,
    val isToday: Boolean,
    val isAllDay: Boolean,
    val startMillis: Long,
    val color: Int? = null
)

data class LogicalBlockAgendaItem(
    val blockTitle: String,
    val timeRangeStr: String,
    val eventSummary: String
)

data class WidgetDisplayData(
    val now: LocalDate,
    val time: LocalTime,
    val badiDate: BadiDate,
    val location: CityLocation,
    val sunriseTime: LocalTime,
    val sunsetTime: LocalTime,
    val isAfterSunset: Boolean,
    val sunsetBadgeText: String,
    val locationSolarText: String,
    val holyDayOrFeastText: String?,
    val todayEvents: List<CalendarEvent>,
    val nextEventSummary: String,
    val logicalBlocks: List<LogicalBlockAgendaItem>,
    val timeSystemMode: TimeSystemMode = TimeSystemMode.STANDARD_CIVIL,
    val elementalTimeText: String = ""
)

object WidgetDataManager {

    /**
     * Returns a deduplicated, sorted list of all upcoming calendar appointments.
     * Prevents duplication across multiple sync accounts / overlapping blocks.
     */
    fun getDistinctAgendaEvents(context: Context): List<WidgetAgendaItem> {
        val repo = CalendarRepository(context)
        if (!repo.hasCalendarPermissions()) {
            return emptyList()
        }

        val loc = LocationHelper.getSavedLocation(context)
        val zoneId = try {
            ZoneId.of(loc.timeZoneId)
        } catch (_: Exception) {
            ZoneId.systemDefault()
        }

        val now = LocalDate.now(zoneId)
        val nowMillis = System.currentTimeMillis()

        // Fetch from beginning of today up to 14 days ahead
        val startRange = now.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val endRange = now.plusDays(14).atStartOfDay(zoneId).toInstant().toEpochMilli()

        val rawEvents = try {
            repo.getEventsForRange(startRange, endRange)
        } catch (e: Exception) {
            emptyList()
        }

        // Deduplicate: events can share the exact same title & time across accounts or recurrences
        val seenKeys = mutableSetOf<String>()
        val uniqueEvents = mutableListOf<CalendarEvent>()

        for (event in rawEvents.sortedBy { it.startMillis }) {
            // Skip past events that finished more than 30 minutes ago (except all-day today)
            if (!event.isAllDay && event.endMillis < (nowMillis - 30 * 60 * 1000L)) {
                continue
            }
            val key = "${event.title.trim().lowercase()}__${event.startMillis}__${event.endMillis}"
            if (seenKeys.add(key)) {
                uniqueEvents.add(event)
            }
        }

        val timeFmt = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)
        val dayFmt = DateTimeFormatter.ofPattern("EEE, MMM d", Locale.ENGLISH)

        return uniqueEvents.map { ev ->
            val eventLocalDate = Instant.ofEpochMilli(ev.startMillis).atZone(zoneId).toLocalDate()
            val isToday = eventLocalDate.isEqual(now)
            val isTomorrow = eventLocalDate.isEqual(now.plusDays(1))

            val dayLabel = when {
                isToday -> "TODAY"
                isTomorrow -> "TOMORROW"
                else -> eventLocalDate.format(dayFmt).uppercase(Locale.ENGLISH)
            }

            val timeText = if (ev.isAllDay) {
                "All Day"
            } else {
                val startTime = Instant.ofEpochMilli(ev.startMillis).atZone(zoneId).toLocalTime().format(timeFmt)
                startTime
            }

            val subtitle = when {
                !ev.location.isNullOrBlank() -> "📍 ${ev.location}"
                !ev.description.isNullOrBlank() -> ev.description.trim().take(60)
                else -> null
            }

            WidgetAgendaItem(
                id = ev.id,
                title = ev.title,
                timeText = timeText,
                dayLabel = dayLabel,
                subtitle = subtitle,
                isToday = isToday,
                isAllDay = ev.isAllDay,
                startMillis = ev.startMillis,
                color = ev.color
            )
        }
    }

    fun getDisplayData(context: Context): WidgetDisplayData {
        val now = LocalDate.now()
        val time = LocalTime.now()
        val instantNow = Instant.now()
        val badiDate: BadiDate = BadiCalendarEngine.gregorianToBadi(now, time)

        val prefs = context.getSharedPreferences("badi_settings_prefs", Context.MODE_PRIVATE)
        val modeName = prefs.getString("key_time_system_mode", TimeSystemMode.STANDARD_CIVIL.name)
        val timeSystemMode = try {
            TimeSystemMode.valueOf(modeName ?: TimeSystemMode.STANDARD_CIVIL.name)
        } catch (_: Exception) {
            TimeSystemMode.STANDARD_CIVIL
        }

        val etime = ElementalTimeEngine.calculate(instantNow)
        val elementalTimeText = "${etime.formattedShort} (${etime.season.englishName})"

        val loc = LocationHelper.getSavedLocation(context)
        val zoneId = try {
            ZoneId.of(loc.timeZoneId)
        } catch (_: Exception) {
            ZoneId.systemDefault()
        }

        val sunsetToday = BadiCalendarEngine.calculateSunset(now, loc.latitude, loc.longitude, zoneId)
        val sunriseToday = BadiCalendarEngine.calculateSunrise(now, loc.latitude, loc.longitude, zoneId)
        val sunsetPrev = BadiCalendarEngine.calculateSunset(now.minusDays(1), loc.latitude, loc.longitude, zoneId)
        val sunriseTomorrow = BadiCalendarEngine.calculateSunrise(now.plusDays(1), loc.latitude, loc.longitude, zoneId)
        val sunsetTomorrow = BadiCalendarEngine.calculateSunset(now.plusDays(1), loc.latitude, loc.longitude, zoneId)
        val sunriseDayAfter = BadiCalendarEngine.calculateSunrise(now.plusDays(2), loc.latitude, loc.longitude, zoneId)

        val isAfterSunset = time.isAfter(sunsetToday)
        val timeFmt = DateTimeFormatter.ofPattern("h:mm a", Locale.ENGLISH)

        val sunsetBadgeText = if (isAfterSunset) "🌙 AFTER SUNSET" else "☀️ BEFORE SUNSET"
        val cityDisplayName = loc.cityName.split(" / ").first()
        val locationSolarText = "📍 $cityDisplayName • 🌅 ${sunriseToday.format(timeFmt)} • 🌇 ${sunsetToday.format(timeFmt)}"

        val holyDayOrFeastText = when {
            badiDate.holyDay != null -> {
                if (badiDate.holyDay.isWorkSuspended) {
                    "✨ ${badiDate.holyDay.name} (Work Suspended)"
                } else {
                    "✨ ${badiDate.holyDay.name}"
                }
            }
            badiDate.isFeastDay -> "🕊️ Feast of ${badiDate.monthInfo.transliteration}"
            badiDate.isAyyamIHa -> "🎁 Ayyám-i-Há Day ${badiDate.day}"
            badiDate.isFastPeriod -> "☀️ The Fast (Day ${badiDate.day}/19)"
            else -> null
        }

        // Query events for broad range (yesterday to day after tomorrow)
        val calendarRepo = CalendarRepository(context)
        val allEvents = mutableListOf<CalendarEvent>()
        if (calendarRepo.hasCalendarPermissions()) {
            try {
                val startRange = now.minusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
                val endRange = now.plusDays(3).atStartOfDay(zoneId).toInstant().toEpochMilli()
                val events = calendarRepo.getEventsForRange(startRange, endRange)
                // Deduplicate
                val seen = mutableSetOf<String>()
                for (ev in events.sortedBy { it.startMillis }) {
                    val k = "${ev.title.trim().lowercase()}__${ev.startMillis}__${ev.endMillis}"
                    if (seen.add(k)) {
                        allEvents.add(ev)
                    }
                }
            } catch (_: Exception) {
                // Ignore
            }
        }

        // Today events for backward compatibility
        val todayStart = now.atStartOfDay(zoneId).toInstant().toEpochMilli()
        val todayEnd = now.plusDays(1).atStartOfDay(zoneId).toInstant().toEpochMilli()
        val todayEvents = allEvents.filter { it.startMillis in todayStart until todayEnd || (it.startMillis <= todayStart && it.endMillis >= todayStart) }

        val nextEventSummary = if (todayEvents.isNotEmpty()) {
            val nowMillis = System.currentTimeMillis()
            val upcoming = todayEvents.firstOrNull { it.endMillis > nowMillis } ?: todayEvents.first()
            val eventTimeStr = if (upcoming.isAllDay) {
                "All Day"
            } else {
                Instant.ofEpochMilli(upcoming.startMillis).atZone(zoneId).toLocalTime().format(timeFmt)
            }
            "📅 $eventTimeStr ${upcoming.title}"
        } else {
            if (calendarRepo.hasCalendarPermissions()) {
                "📅 No more events today • Tap to add"
            } else {
                "📅 Tap to sync calendar & appointments"
            }
        }

        // Calculate logical blocks for Big Clock widget with strict deduplication
        val block1Start = now.minusDays(1).atTime(sunsetPrev).atZone(zoneId).toInstant().toEpochMilli()
        val block1End = now.atTime(sunriseToday).atZone(zoneId).toInstant().toEpochMilli()

        val block2Start = block1End
        val block2End = now.atTime(sunsetToday).atZone(zoneId).toInstant().toEpochMilli()

        val block3Start = block2End
        val block3End = now.plusDays(1).atTime(sunriseTomorrow).atZone(zoneId).toInstant().toEpochMilli()

        val block4Start = block3End
        val block4End = now.plusDays(1).atTime(sunsetTomorrow).atZone(zoneId).toInstant().toEpochMilli()

        val block5Start = block4End
        val block5End = now.plusDays(2).atTime(sunriseDayAfter).atZone(zoneId).toInstant().toEpochMilli()

        val rawBlocks = listOf(
            Triple("🌙 Eve Start (Badí' Day)", "${sunsetPrev.format(timeFmt)} – ${sunriseToday.format(timeFmt)}", block1Start..block1End),
            Triple("☀️ Daytime Portion", "${sunriseToday.format(timeFmt)} – ${sunsetToday.format(timeFmt)}", block2Start..block2End),
            Triple("🌙 Next Eve Start", "${sunsetToday.format(timeFmt)} – ${sunriseTomorrow.format(timeFmt)}", block3Start..block3End),
            Triple("☀️ +1 Ahead (Tomorrow)", "${sunriseTomorrow.format(timeFmt)} – ${sunsetTomorrow.format(timeFmt)}", block4Start..block4End),
            Triple("🌙 +2 Ahead (Day After)", "${sunsetTomorrow.format(timeFmt)} – ${sunriseDayAfter.format(timeFmt)}", block5Start..block5End)
        )

        val currentTimeEpoch = Instant.now().toEpochMilli()
        val currentIndex = when {
            currentTimeEpoch < block1End -> 0
            currentTimeEpoch < block2End -> 1
            currentTimeEpoch < block3End -> 2
            currentTimeEpoch < block4End -> 3
            else -> 4
        }

        val logicalBlocks = mutableListOf<LogicalBlockAgendaItem>()
        val assignedEventIds = mutableSetOf<Long>()

        for (i in 0 until 3) {
            val targetIdx = (currentIndex + i).coerceIn(0, rawBlocks.size - 1)
            val (rawTitle, rangeStr, range) = rawBlocks[targetIdx]
            val blockPrefix = when (i) {
                0 -> "Current"
                1 -> "+1 Ahead"
                else -> "+2 Ahead"
            }
            val title = "$blockPrefix: $rawTitle"
            val eventsInBlock = allEvents.filter {
                it.id !in assignedEventIds && it.startMillis < range.endInclusive && it.endMillis > range.start
            }
            // Mark these events as assigned so they are never duplicated into subsequent blocks
            for (ev in eventsInBlock) {
                assignedEventIds.add(ev.id)
            }

            val eventSummary = if (eventsInBlock.isNotEmpty()) {
                eventsInBlock.joinToString(" • ") { ev ->
                    val t = if (ev.isAllDay) "All Day" else Instant.ofEpochMilli(ev.startMillis).atZone(zoneId).toLocalTime().format(timeFmt)
                    "$t ${ev.title}"
                }
            } else {
                if (i == 0) "No appointments" else "Prep: No appointments"
            }
            logicalBlocks.add(LogicalBlockAgendaItem(title, rangeStr, eventSummary))
        }

        return WidgetDisplayData(
            now = now,
            time = time,
            badiDate = badiDate,
            location = loc,
            sunriseTime = sunriseToday,
            sunsetTime = sunsetToday,
            isAfterSunset = isAfterSunset,
            sunsetBadgeText = sunsetBadgeText,
            locationSolarText = locationSolarText,
            holyDayOrFeastText = holyDayOrFeastText,
            todayEvents = todayEvents,
            nextEventSummary = nextEventSummary,
            logicalBlocks = logicalBlocks,
            timeSystemMode = timeSystemMode,
            elementalTimeText = elementalTimeText
        )
    }

    fun getAppOpenPendingIntent(context: Context): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        return PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /**
     * Configures live native TextClocks and Elemental Time components across widgets.
     * Uses Android's native TextClock for UTC minutes ("mm") combined with an hourly prefix TextView,
     * ensuring fluid per-minute updates without DateFormat parsing bugs, reflection failures, or battery drain!
     */
    fun configureWidgetClocks(
        views: RemoteViews,
        data: WidgetDisplayData,
        isBigClock: Boolean = false
    ) {
        val etime = ElementalTimeEngine.calculate(Instant.now())
        val etimePrefixText = if (isBigClock) {
            "${etime.season.emoji} ${etime.season.englishName} ${etime.blockHour}:"
        } else {
            "${etime.season.emoji} ${etime.blockHour}:"
        }

        when (data.timeSystemMode) {
            TimeSystemMode.STANDARD_CIVIL -> {
                views.setViewVisibility(com.example.R.id.widget_text_clock, android.view.View.VISIBLE)
                views.setViewVisibility(com.example.R.id.widget_etime_container, android.view.View.GONE)
            }
            TimeSystemMode.ELEMENTAL_ETIME -> {
                views.setViewVisibility(com.example.R.id.widget_text_clock, android.view.View.GONE)
                views.setViewVisibility(com.example.R.id.widget_etime_container, android.view.View.VISIBLE)
                views.setTextViewText(com.example.R.id.widget_etime_prefix, etimePrefixText)
            }
            TimeSystemMode.DUAL_DISPLAY -> {
                views.setViewVisibility(com.example.R.id.widget_text_clock, android.view.View.VISIBLE)
                views.setViewVisibility(com.example.R.id.widget_etime_container, android.view.View.VISIBLE)
                views.setTextViewText(com.example.R.id.widget_etime_prefix, etimePrefixText)
            }
        }
    }
}
