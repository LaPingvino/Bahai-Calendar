package com.example

import android.appwidget.AppWidgetManager
import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.TextClock
import android.widget.TextView
import androidx.test.core.app.ApplicationProvider
import com.example.badi.TimeSystemMode
import com.example.widget.BadiAgendaWidgetProvider
import com.example.widget.BadiAppWidgetProvider
import com.example.widget.BadiClockWidgetProvider
import com.example.widget.BadiCompactWidgetProvider
import com.example.widget.BadiNextEventWidgetProvider
import com.example.widget.WidgetDataManager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class WidgetInflationTest {

    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Test
    fun `test standard widget inflation in elemental etime mode`() {
        val prefs = context.getSharedPreferences("badi_settings_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("key_time_system_mode", TimeSystemMode.ELEMENTAL_ETIME.name).commit()

        val data = WidgetDataManager.getDisplayData(context)
        val views = android.widget.RemoteViews(context.packageName, R.layout.widget_badi_date)
        WidgetDataManager.configureWidgetClocks(views, data)

        val inflated = views.apply(context, FrameLayout(context))
        assertNotNull(inflated)

        val textClock = inflated.findViewById<TextClock>(R.id.widget_text_clock)
        val etimeContainer = inflated.findViewById<View>(R.id.widget_etime_container)
        val etimePrefix = inflated.findViewById<TextView>(R.id.widget_etime_prefix)
        val etimeMinute = inflated.findViewById<TextClock>(R.id.widget_etime_minute)

        assertNotNull(textClock)
        assertNotNull(etimeContainer)
        assertNotNull(etimePrefix)
        assertNotNull(etimeMinute)

        assertEquals(View.GONE, textClock.visibility)
        assertEquals(View.VISIBLE, etimeContainer.visibility)
        assertTrue(etimePrefix.text.isNotEmpty())
        assertTrue(etimePrefix.text.contains(":"))
        assertEquals("mm", etimeMinute.format12Hour.toString())
        assertEquals("mm", etimeMinute.format24Hour.toString())
    }

    @Test
    fun `test big clock widget inflation in dual display mode`() {
        val prefs = context.getSharedPreferences("badi_settings_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("key_time_system_mode", TimeSystemMode.DUAL_DISPLAY.name).commit()

        val data = WidgetDataManager.getDisplayData(context)
        val views = android.widget.RemoteViews(context.packageName, R.layout.widget_badi_big_clock)
        WidgetDataManager.configureWidgetClocks(views, data, isBigClock = true)

        val inflated = views.apply(context, FrameLayout(context))
        assertNotNull(inflated)

        val textClock = inflated.findViewById<TextClock>(R.id.widget_text_clock)
        val etimeContainer = inflated.findViewById<View>(R.id.widget_etime_container)
        val etimePrefix = inflated.findViewById<TextView>(R.id.widget_etime_prefix)
        val etimeMinute = inflated.findViewById<TextClock>(R.id.widget_etime_minute)

        assertNotNull(textClock)
        assertNotNull(etimeContainer)
        assertNotNull(etimePrefix)
        assertNotNull(etimeMinute)

        assertEquals(View.VISIBLE, textClock.visibility)
        assertEquals(View.VISIBLE, etimeContainer.visibility)
        assertTrue(etimePrefix.text.isNotEmpty())
        assertEquals("mm", etimeMinute.format12Hour.toString())
        assertEquals("mm", etimeMinute.format24Hour.toString())
    }

    @Test
    fun `test compact 2x1 widget inflation`() {
        val prefs = context.getSharedPreferences("badi_settings_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("key_time_system_mode", TimeSystemMode.ELEMENTAL_ETIME.name).commit()

        val data = WidgetDataManager.getDisplayData(context)
        val views = android.widget.RemoteViews(context.packageName, R.layout.widget_badi_compact)
        WidgetDataManager.configureWidgetClocks(views, data)

        val inflated = views.apply(context, FrameLayout(context))
        assertNotNull(inflated)

        val etimeContainer = inflated.findViewById<View>(R.id.widget_etime_container)
        val etimePrefix = inflated.findViewById<TextView>(R.id.widget_etime_prefix)
        assertNotNull(etimeContainer)
        assertNotNull(etimePrefix)
        assertEquals(View.VISIBLE, etimeContainer.visibility)
    }

    @Test
    fun `test next event 2x1 widget inflation`() {
        val data = WidgetDataManager.getDisplayData(context)
        val views = android.widget.RemoteViews(context.packageName, R.layout.widget_badi_next_event)
        WidgetDataManager.configureWidgetClocks(views, data)

        val inflated = views.apply(context, FrameLayout(context))
        assertNotNull(inflated)

        val container = inflated.findViewById<View>(R.id.widget_container)
        assertNotNull(container)
    }

    @Test
    fun `test agenda 4x3 widget inflation in elemental etime mode`() {
        val prefs = context.getSharedPreferences("badi_settings_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("key_time_system_mode", TimeSystemMode.ELEMENTAL_ETIME.name).commit()

        val data = WidgetDataManager.getDisplayData(context)
        val views = android.widget.RemoteViews(context.packageName, R.layout.widget_badi_agenda)
        WidgetDataManager.configureWidgetClocks(views, data)

        val inflated = views.apply(context, FrameLayout(context))
        assertNotNull(inflated)

        val textClock = inflated.findViewById<TextClock>(R.id.widget_text_clock)
        val etimeContainer = inflated.findViewById<View>(R.id.widget_etime_container)
        val etimePrefix = inflated.findViewById<TextView>(R.id.widget_etime_prefix)
        val etimeMinute = inflated.findViewById<TextClock>(R.id.widget_etime_minute)

        assertEquals(View.GONE, textClock.visibility)
        assertEquals(View.VISIBLE, etimeContainer.visibility)
        assertTrue(etimePrefix.text.isNotEmpty())
        assertEquals("mm", etimeMinute.format12Hour.toString())
    }

    @Test
    fun `test next event 2x1 widget inflation in dual display mode`() {
        val prefs = context.getSharedPreferences("badi_settings_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("key_time_system_mode", TimeSystemMode.DUAL_DISPLAY.name).commit()

        val data = WidgetDataManager.getDisplayData(context)
        val views = android.widget.RemoteViews(context.packageName, R.layout.widget_badi_next_event)
        WidgetDataManager.configureWidgetClocks(views, data)

        val inflated = views.apply(context, FrameLayout(context))
        assertNotNull(inflated)

        val textClock = inflated.findViewById<TextClock>(R.id.widget_text_clock)
        val etimeContainer = inflated.findViewById<View>(R.id.widget_etime_container)
        val etimePrefix = inflated.findViewById<TextView>(R.id.widget_etime_prefix)

        assertEquals(View.VISIBLE, textClock.visibility)
        assertEquals(View.VISIBLE, etimeContainer.visibility)
        assertTrue(etimePrefix.text.isNotEmpty())
    }

    @Test
    fun `test standard civil mode across widgets hides elemental container`() {
        val prefs = context.getSharedPreferences("badi_settings_prefs", Context.MODE_PRIVATE)
        prefs.edit().putString("key_time_system_mode", TimeSystemMode.STANDARD_CIVIL.name).commit()

        val data = WidgetDataManager.getDisplayData(context)
        val views = android.widget.RemoteViews(context.packageName, R.layout.widget_badi_date)
        WidgetDataManager.configureWidgetClocks(views, data)

        val inflated = views.apply(context, FrameLayout(context))
        assertNotNull(inflated)

        val textClock = inflated.findViewById<TextClock>(R.id.widget_text_clock)
        val etimeContainer = inflated.findViewById<View>(R.id.widget_etime_container)

        assertEquals(View.VISIBLE, textClock.visibility)
        assertEquals(View.GONE, etimeContainer.visibility)
    }
}
