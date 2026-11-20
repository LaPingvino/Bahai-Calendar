package com.example.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.badi.BadiCalendarEngine
import com.example.location.LocationHelper
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit

class WidgetHourlyUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        // updateAllWidgets refreshes all active widgets and reschedules the next alarm
        BadiAppWidgetProvider.updateAllWidgets(context)
    }
}

object WidgetHourlyAlarmManager {
    fun scheduleNextAlarm(context: Context) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, WidgetHourlyUpdateReceiver::class.java).apply {
                action = "com.example.ACTION_WIDGET_HOURLY_REFRESH"
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                1001,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val now = Instant.now()
            val minFutureInstant = now.plusSeconds(2)

            // 1. Next top of the UTC hour (for etime block/hour prefix changes)
            val nextUtcHour = now.truncatedTo(ChronoUnit.HOURS).plus(1, ChronoUnit.HOURS)

            // 2. Next local sunset and next local midnight (for Badí' day & civil date rollovers)
            val loc = LocationHelper.getSavedLocation(context)
            val zoneId = try {
                ZoneId.of(loc.timeZoneId)
            } catch (_: Exception) {
                ZoneId.systemDefault()
            }
            val zdt = ZonedDateTime.ofInstant(now, zoneId)
            val todayDate = zdt.toLocalDate()
            val todaySunset = BadiCalendarEngine.calculateSunset(todayDate, loc.latitude, loc.longitude, zoneId)
            val todaySunsetInstant = todayDate.atTime(todaySunset).atZone(zoneId).toInstant()
            val nextSunsetInstant = if (todaySunsetInstant.isAfter(minFutureInstant)) {
                todaySunsetInstant
            } else {
                val tomorrowDate = todayDate.plusDays(1)
                val tomorrowSunset = BadiCalendarEngine.calculateSunset(tomorrowDate, loc.latitude, loc.longitude, zoneId)
                tomorrowDate.atTime(tomorrowSunset).atZone(zoneId).toInstant()
            }

            val nextMidnightInstant = todayDate.plusDays(1).atStartOfDay(zoneId).toInstant()

            val triggerInstant = listOf(nextUtcHour, nextSunsetInstant, nextMidnightInstant)
                .filter { it.isAfter(minFutureInstant) }
                .minOrNull() ?: nextUtcHour

            alarmManager.set(
                AlarmManager.RTC,
                triggerInstant.toEpochMilli(),
                pendingIntent
            )
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun scheduleNextHourAlarm(context: Context) {
        scheduleNextAlarm(context)
    }
}

