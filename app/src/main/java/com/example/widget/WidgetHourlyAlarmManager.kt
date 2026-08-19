package com.example.widget

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.badi.TimeSystemMode
import java.time.Instant
import java.time.temporal.ChronoUnit

class WidgetHourlyUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        BadiAppWidgetProvider.updateAllWidgets(context)
        WidgetHourlyAlarmManager.scheduleNextAlarm(context)
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

            val prefs = context.getSharedPreferences("badi_settings_prefs", Context.MODE_PRIVATE)
            val modeName = prefs.getString("key_time_system_mode", TimeSystemMode.STANDARD_CIVIL.name)
            val timeSystemMode = try {
                TimeSystemMode.valueOf(modeName ?: TimeSystemMode.STANDARD_CIVIL.name)
            } catch (_: Exception) {
                TimeSystemMode.STANDARD_CIVIL
            }

            // Schedule alarm for the next top of the UTC hour change to refresh the etime prefix and dates
            val now = Instant.now()
            val nextHour = now.truncatedTo(ChronoUnit.HOURS).plus(1, ChronoUnit.HOURS)
            val triggerAtMillis = nextHour.toEpochMilli()

            alarmManager.set(
                AlarmManager.RTC,
                triggerAtMillis,
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

