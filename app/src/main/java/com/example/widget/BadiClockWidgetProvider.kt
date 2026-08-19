package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.view.View
import android.widget.RemoteViews

/**
 * Big Clock & Badí' Date Widget (4x2 / 3x2)
 */
class BadiClockWidgetProvider : AppWidgetProvider() {

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
            val views = RemoteViews(context.packageName, com.example.R.layout.widget_badi_big_clock)

            // Sunset Status & Badí' Weekday
            views.setTextViewText(com.example.R.id.widget_sunset_badge, data.sunsetBadgeText)
            val weekdayText = if (data.isAfterSunset) {
                "${data.badiDate.weekdayInfo.transliteration} • ${data.badiDate.civilWeekdayInfo.gregorianDayName} eve"
            } else {
                "${data.badiDate.weekdayInfo.transliteration} • ${data.badiDate.civilWeekdayInfo.gregorianDayName}"
            }
            views.setTextViewText(com.example.R.id.widget_badi_weekday, weekdayText)

            // Full Badí' Date: e.g. "17 Asmá' 183 B.E."
            val dateText = if (data.badiDate.month == 0) {
                "Ayyám-i-Há (Day ${data.badiDate.day}) ${data.badiDate.year} B.E."
            } else {
                "${data.badiDate.day} ${data.badiDate.monthInfo.transliteration} ${data.badiDate.year} B.E."
            }
            views.setTextViewText(com.example.R.id.widget_badi_full_date, dateText)

            // Location & Sunset time / Solar info
            views.setTextViewText(
                com.example.R.id.widget_location_solar,
                data.locationSolarText
            )

            // Configure live TextClocks (civil and live UTC etime)
            WidgetDataManager.configureWidgetClocks(views, data, isBigClock = true)

            // Event & Holy day badge
            if (data.holyDayOrFeastText != null) {
                views.setViewVisibility(com.example.R.id.widget_holy_day_badge, View.VISIBLE)
                views.setTextViewText(com.example.R.id.widget_holy_day_badge, data.holyDayOrFeastText)
            } else {
                views.setViewVisibility(com.example.R.id.widget_holy_day_badge, View.GONE)
            }

            // Populate the 3 Logical Time Blocks
            val blocks = data.logicalBlocks
            if (blocks.isNotEmpty()) {
                val b1 = blocks[0]
                views.setTextViewText(com.example.R.id.widget_block_1_title, b1.blockTitle)
                views.setTextViewText(com.example.R.id.widget_block_1_time, b1.timeRangeStr)
                views.setTextViewText(com.example.R.id.widget_block_1_event, b1.eventSummary)
            }
            if (blocks.size > 1) {
                val b2 = blocks[1]
                views.setTextViewText(com.example.R.id.widget_block_2_title, b2.blockTitle)
                views.setTextViewText(com.example.R.id.widget_block_2_time, b2.timeRangeStr)
                views.setTextViewText(com.example.R.id.widget_block_2_event, b2.eventSummary)
            }
            if (blocks.size > 2) {
                val b3 = blocks[2]
                views.setTextViewText(com.example.R.id.widget_block_3_title, b3.blockTitle)
                views.setTextViewText(com.example.R.id.widget_block_3_time, b3.timeRangeStr)
                views.setTextViewText(com.example.R.id.widget_block_3_event, b3.eventSummary)
            }

            // Click to open MainActivity
            views.setOnClickPendingIntent(
                com.example.R.id.widget_container,
                WidgetDataManager.getAppOpenPendingIntent(context)
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
