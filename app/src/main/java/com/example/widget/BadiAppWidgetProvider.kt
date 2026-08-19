package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.view.View
import android.widget.RemoteViews
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Standard Badí' Date & Schedule Widget (3x2 or 4x2)
 */
class BadiAppWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val data = WidgetDataManager.getDisplayData(context)
            val views = RemoteViews(context.packageName, com.example.R.layout.widget_badi_date)

            // 1. Day number and Month / Year
            views.setTextViewText(com.example.R.id.widget_badi_day_number, data.badiDate.day.toString())
            views.setTextViewText(
                com.example.R.id.widget_badi_month_year,
                "${data.badiDate.monthInfo.transliteration} ${data.badiDate.year} B.E."
            )

            // 2. Month translation & Vahid
            val monthDesc = if (data.badiDate.month == 0) {
                "Ayyám-i-Há • Váḥid ${data.badiDate.vahid} (${data.badiDate.vahidYearInfo.transliteration})"
            } else {
                "${data.badiDate.monthInfo.translation} • Váḥid ${data.badiDate.vahid} (${data.badiDate.vahidYearInfo.transliteration})"
            }
            views.setTextViewText(com.example.R.id.widget_badi_translation, monthDesc)

            // 3. Badí' Weekday & Gregorian Day
            val weekdayText = if (data.isAfterSunset) {
                "${data.badiDate.weekdayInfo.transliteration} (${data.badiDate.weekdayInfo.translation}) • ${data.badiDate.civilWeekdayInfo.gregorianDayName} eve"
            } else {
                "${data.badiDate.weekdayInfo.transliteration} (${data.badiDate.weekdayInfo.translation}) • ${data.badiDate.civilWeekdayInfo.gregorianDayName}"
            }
            views.setTextViewText(com.example.R.id.widget_badi_weekday, weekdayText)

            // 4. Sunset transition status and Solar times
            views.setTextViewText(com.example.R.id.widget_sunset_badge, data.sunsetBadgeText)
            views.setTextViewText(com.example.R.id.widget_location_solar, data.locationSolarText)

            // Configure live TextClocks (civil and live UTC etime)
            WidgetDataManager.configureWidgetClocks(views, data)

            // 5. Today's Upcoming Event / Appointment
            views.setTextViewText(com.example.R.id.widget_event_text, data.nextEventSummary)

            // 6. Gregorian Date
            val gregFmt = DateTimeFormatter.ofPattern("EEE, MMM d, yyyy", Locale.getDefault())
            views.setTextViewText(com.example.R.id.widget_gregorian_date, data.now.format(gregFmt))

            // 7. Holy Day / Feast Badge
            if (data.holyDayOrFeastText != null) {
                views.setViewVisibility(com.example.R.id.widget_holy_day_badge, View.VISIBLE)
                views.setTextViewText(com.example.R.id.widget_holy_day_badge, data.holyDayOrFeastText)
            } else {
                views.setViewVisibility(com.example.R.id.widget_holy_day_badge, View.GONE)
            }

            // Click to open MainActivity
            views.setOnClickPendingIntent(
                com.example.R.id.widget_container,
                WidgetDataManager.getAppOpenPendingIntent(context)
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        /**
         * Refreshes all active Badí' calendar widgets across all variations on the device.
         */
        fun updateAllWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)

            // 1. Standard Date Widget
            val stdComponent = ComponentName(context, BadiAppWidgetProvider::class.java)
            for (id in appWidgetManager.getAppWidgetIds(stdComponent)) {
                updateAppWidget(context, appWidgetManager, id)
            }

            // 2. Big Clock Widget
            val clockComponent = ComponentName(context, BadiClockWidgetProvider::class.java)
            for (id in appWidgetManager.getAppWidgetIds(clockComponent)) {
                BadiClockWidgetProvider.updateAppWidget(context, appWidgetManager, id)
            }

            // 3. Agenda Widget
            val agendaComponent = ComponentName(context, BadiAgendaWidgetProvider::class.java)
            for (id in appWidgetManager.getAppWidgetIds(agendaComponent)) {
                BadiAgendaWidgetProvider.updateAppWidget(context, appWidgetManager, id)
            }

            // 4. Compact Widget
            val compactComponent = ComponentName(context, BadiCompactWidgetProvider::class.java)
            for (id in appWidgetManager.getAppWidgetIds(compactComponent)) {
                BadiCompactWidgetProvider.updateAppWidget(context, appWidgetManager, id)
            }

            // 5. Next Event Widget (2x1)
            val nextEventComponent = ComponentName(context, BadiNextEventWidgetProvider::class.java)
            for (id in appWidgetManager.getAppWidgetIds(nextEventComponent)) {
                BadiNextEventWidgetProvider.updateAppWidget(context, appWidgetManager, id)
            }

            // Schedule next hourly refresh for etime synchronization
            WidgetHourlyAlarmManager.scheduleNextHourAlarm(context)
        }
    }
}
