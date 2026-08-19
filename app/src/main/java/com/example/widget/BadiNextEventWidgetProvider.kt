package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews
import com.example.R

/**
 * 2x1 Badí' Next Event Widget
 * Combines current Badí' Date & Sunset badge with the next upcoming calendar appointment.
 */
class BadiNextEventWidgetProvider : AppWidgetProvider() {

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
            val displayData = WidgetDataManager.getDisplayData(context)
            val agendaEvents = WidgetDataManager.getDistinctAgendaEvents(context)

            val views = RemoteViews(context.packageName, R.layout.widget_badi_next_event)

            // Badí' Day number and month
            views.setTextViewText(R.id.widget_badi_day_number, displayData.badiDate.day.toString())
            val monthStr = if (displayData.badiDate.month == 0) {
                "Ayyám-i-Há"
            } else {
                displayData.badiDate.monthInfo.transliteration
            }
            views.setTextViewText(R.id.widget_badi_month_year, monthStr)

            // Sunset badge
            views.setTextViewText(R.id.widget_sunset_badge, displayData.sunsetBadgeText)

            // Configure live TextClocks (civil and live UTC etime)
            WidgetDataManager.configureWidgetClocks(views, displayData)

            // Next event title
            val nextEvent = agendaEvents.firstOrNull()
            val eventText = if (nextEvent != null) {
                "${nextEvent.timeText} ${nextEvent.title}"
            } else {
                "📅 No upcoming appointments"
            }
            views.setTextViewText(R.id.widget_next_event_title, eventText)

            // Tap to open app
            views.setOnClickPendingIntent(
                R.id.widget_container,
                WidgetDataManager.getAppOpenPendingIntent(context)
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
