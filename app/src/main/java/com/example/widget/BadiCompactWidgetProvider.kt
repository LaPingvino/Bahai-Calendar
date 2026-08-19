package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.widget.RemoteViews

/**
 * Minimalist Compact Badí' Date Widget (2x1)
 */
class BadiCompactWidgetProvider : AppWidgetProvider() {

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
            val views = RemoteViews(context.packageName, com.example.R.layout.widget_badi_compact)

            views.setTextViewText(com.example.R.id.widget_badi_day_number, data.badiDate.day.toString())
            views.setTextViewText(
                com.example.R.id.widget_badi_month_year,
                "${data.badiDate.monthInfo.transliteration} ${data.badiDate.year} B.E."
            )
            val weekdayText = if (data.isAfterSunset) {
                "${data.badiDate.weekdayInfo.transliteration} • ${data.badiDate.civilWeekdayInfo.gregorianDayName} eve"
            } else {
                "${data.badiDate.weekdayInfo.transliteration} • ${data.badiDate.civilWeekdayInfo.gregorianDayName}"
            }
            views.setTextViewText(com.example.R.id.widget_badi_weekday, weekdayText)
            views.setTextViewText(com.example.R.id.widget_sunset_badge, data.sunsetBadgeText)

            // Configure live TextClocks (civil and live UTC etime)
            WidgetDataManager.configureWidgetClocks(views, data)

            views.setOnClickPendingIntent(
                com.example.R.id.widget_container,
                WidgetDataManager.getAppOpenPendingIntent(context)
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
