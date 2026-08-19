package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.View
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.R
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Badí' Agenda & Events Widget (4x3 / 4x2)
 * Features a scrollable list of deduplicated appointments with wrapping text.
 */
class BadiAgendaWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }

    companion object {
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val displayData = WidgetDataManager.getDisplayData(context)
            val views = RemoteViews(context.packageName, R.layout.widget_badi_agenda)

            // 1. Header: Date, Sunset, Location
            val dateStr = if (displayData.badiDate.month == 0) {
                "Ayyám-i-Há (Day ${displayData.badiDate.day}) ${displayData.badiDate.year} B.E."
            } else {
                "${displayData.badiDate.day} ${displayData.badiDate.monthInfo.transliteration} ${displayData.badiDate.year} B.E."
            }
            views.setTextViewText(R.id.widget_badi_month_year, dateStr)
            views.setTextViewText(R.id.widget_sunset_badge, displayData.sunsetBadgeText)
            views.setTextViewText(R.id.widget_location_solar, displayData.locationSolarText)

            // Configure live TextClocks (civil and live UTC etime)
            WidgetDataManager.configureWidgetClocks(views, displayData)

            // Holy Day badge in header if present
            if (displayData.holyDayOrFeastText != null) {
                views.setViewVisibility(R.id.widget_holy_day_badge, View.VISIBLE)
                views.setTextViewText(R.id.widget_holy_day_badge, displayData.holyDayOrFeastText)
            } else {
                views.setViewVisibility(R.id.widget_holy_day_badge, View.GONE)
            }

            // 2. Set up RemoteViewsService for the scrollable ListView
            val serviceIntent = Intent(context, BadiAgendaWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                setData(Uri.parse(toUri(Intent.URI_INTENT_SCHEME)))
            }
            views.setRemoteAdapter(R.id.widget_agenda_list, serviceIntent)
            views.setEmptyView(R.id.widget_agenda_list, R.id.widget_agenda_empty)

            // PendingIntent template for clicking list items
            val itemClickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val itemClickPendingIntent = PendingIntent.getActivity(
                context,
                0,
                itemClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_agenda_list, itemClickPendingIntent)

            // 3. Gregorian date footer
            val gregFmt = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy", Locale.getDefault())
            views.setTextViewText(
                R.id.widget_gregorian_date,
                "${displayData.now.format(gregFmt)} • Tap to open"
            )

            // Click header or container to open MainActivity
            views.setOnClickPendingIntent(
                R.id.widget_container_header,
                WidgetDataManager.getAppOpenPendingIntent(context)
            )
            views.setOnClickPendingIntent(
                R.id.widget_agenda_empty,
                WidgetDataManager.getAppOpenPendingIntent(context)
            )

            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_agenda_list)
        }
    }
}
