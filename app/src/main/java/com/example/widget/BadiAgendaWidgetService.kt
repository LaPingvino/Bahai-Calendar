package com.example.widget

import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.R

class BadiAgendaWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return BadiAgendaRemoteViewsFactory(applicationContext)
    }
}

class BadiAgendaRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private val items = mutableListOf<WidgetAgendaItem>()

    override fun onCreate() {
        loadData()
    }

    override fun onDataSetChanged() {
        loadData()
    }

    private fun loadData() {
        items.clear()
        items.addAll(WidgetDataManager.getDistinctAgendaEvents(context))
    }

    override fun onDestroy() {
        items.clear()
    }

    override fun getCount(): Int = items.size

    override fun getViewAt(position: Int): RemoteViews? {
        if (position < 0 || position >= items.size) return null
        val item = items[position]

        val views = RemoteViews(context.packageName, R.layout.widget_agenda_item_row)

        views.setTextViewText(R.id.widget_row_day_label, item.dayLabel)
        views.setTextViewText(R.id.widget_row_time, item.timeText)
        views.setTextViewText(R.id.widget_row_title, item.title)

        if (item.isToday) {
            views.setTextColor(R.id.widget_row_day_label, 0xFF90E0EF.toInt())
            views.setTextColor(R.id.widget_row_time, 0xFFFFD54F.toInt())
        } else {
            views.setTextColor(R.id.widget_row_day_label, 0xFFB0C4DE.toInt())
            views.setTextColor(R.id.widget_row_time, 0xFFE0E1DD.toInt())
        }

        if (!item.subtitle.isNullOrBlank()) {
            views.setViewVisibility(R.id.widget_row_subtitle, View.VISIBLE)
            views.setTextViewText(R.id.widget_row_subtitle, item.subtitle)
        } else {
            views.setViewVisibility(R.id.widget_row_subtitle, View.GONE)
        }

        // Fill-in intent for item click to open MainActivity
        val fillInIntent = Intent().apply {
            putExtra("extra_event_id", item.id)
            putExtra("extra_start_millis", item.startMillis)
        }
        views.setOnClickFillInIntent(R.id.widget_agenda_row_container, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long {
        return if (position in items.indices) items[position].id else position.toLong()
    }

    override fun hasStableIds(): Boolean = true
}
