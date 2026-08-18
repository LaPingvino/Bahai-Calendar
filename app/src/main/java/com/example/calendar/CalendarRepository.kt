package com.example.calendar

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.time.LocalDate
import java.time.ZoneId
import java.util.TimeZone

/**
 * Repository to query and manipulate Google Calendar / Android Calendar provider.
 */
class CalendarRepository(private val context: Context) {

    fun hasCalendarPermissions(): Boolean {
        val read = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
        val write = ContextCompat.checkSelfPermission(
            context,
            android.Manifest.permission.WRITE_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
        return read && write
    }

    /**
     * Queries all available calendar accounts on device (prioritizing Google accounts).
     */
    fun getCalendarAccounts(): List<GoogleCalendarAccount> {
        if (!hasCalendarPermissions()) return emptyList()

        val list = mutableListOf<GoogleCalendarAccount>()
        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.ACCOUNT_NAME,
            CalendarContract.Calendars.ACCOUNT_TYPE,
            CalendarContract.Calendars.CALENDAR_COLOR,
            CalendarContract.Calendars.IS_PRIMARY
        )

        try {
            val uri = CalendarContract.Calendars.CONTENT_URI
            val cursor: Cursor? = context.contentResolver.query(
                uri,
                projection,
                CalendarContract.Calendars.VISIBLE + " = 1",
                null,
                "${CalendarContract.Calendars.IS_PRIMARY} DESC, ${CalendarContract.Calendars.CALENDAR_DISPLAY_NAME} ASC"
            )

            cursor?.use {
                val idIdx = it.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
                val nameIdx = it.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME)
                val accNameIdx = it.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_NAME)
                val accTypeIdx = it.getColumnIndexOrThrow(CalendarContract.Calendars.ACCOUNT_TYPE)
                val colorIdx = it.getColumnIndexOrThrow(CalendarContract.Calendars.CALENDAR_COLOR)
                val primaryIdx = it.getColumnIndex(CalendarContract.Calendars.IS_PRIMARY)

                while (it.moveToNext()) {
                    val id = it.getLong(idIdx)
                    val name = it.getString(nameIdx) ?: "Calendar"
                    val accName = it.getString(accNameIdx) ?: ""
                    val accType = it.getString(accTypeIdx) ?: ""
                    val color = it.getInt(colorIdx)
                    val isPrimary = if (primaryIdx >= 0) it.getInt(primaryIdx) == 1 else false

                    list.add(
                        GoogleCalendarAccount(
                            id = id,
                            displayName = name,
                            accountName = accName,
                            accountType = accType,
                            color = color,
                            isPrimary = isPrimary
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return list
    }

    /**
     * Queries events / appointments occurring within a given time range.
     */
    fun getEventsForRange(
        startMillis: Long,
        endMillis: Long,
        calendarId: Long? = null
    ): List<CalendarEvent> {
        if (!hasCalendarPermissions()) return emptyList()

        val events = mutableListOf<CalendarEvent>()
        val builder = CalendarContract.Instances.CONTENT_URI.buildUpon()
        ContentUris.appendId(builder, startMillis)
        ContentUris.appendId(builder, endMillis)

        val projection = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.CALENDAR_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.DESCRIPTION,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.ALL_DAY,
            CalendarContract.Instances.DISPLAY_COLOR
        )

        var selection = ""
        val selectionArgs = if (calendarId != null) {
            selection = "${CalendarContract.Instances.CALENDAR_ID} = ?"
            arrayOf(calendarId.toString())
        } else {
            null
        }

        try {
            val cursor = context.contentResolver.query(
                builder.build(),
                projection,
                if (selection.isNotEmpty()) selection else null,
                selectionArgs,
                "${CalendarContract.Instances.BEGIN} ASC"
            )

            cursor?.use {
                val idIdx = it.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
                val calIdIdx = it.getColumnIndexOrThrow(CalendarContract.Instances.CALENDAR_ID)
                val titleIdx = it.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
                val descIdx = it.getColumnIndex(CalendarContract.Instances.DESCRIPTION)
                val locIdx = it.getColumnIndex(CalendarContract.Instances.EVENT_LOCATION)
                val beginIdx = it.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
                val endIdx = it.getColumnIndexOrThrow(CalendarContract.Instances.END)
                val allDayIdx = it.getColumnIndex(CalendarContract.Instances.ALL_DAY)
                val colorIdx = it.getColumnIndex(CalendarContract.Instances.DISPLAY_COLOR)

                while (it.moveToNext()) {
                    val id = it.getLong(idIdx)
                    val cId = it.getLong(calIdIdx)
                    val title = it.getString(titleIdx) ?: "(No title)"
                    val desc = if (descIdx >= 0) it.getString(descIdx) else null
                    val loc = if (locIdx >= 0) it.getString(locIdx) else null
                    val begin = it.getLong(beginIdx)
                    val end = it.getLong(endIdx)
                    val isAllDay = if (allDayIdx >= 0) it.getInt(allDayIdx) == 1 else false
                    val color = if (colorIdx >= 0) it.getInt(colorIdx) else null

                    events.add(
                        CalendarEvent(
                            id = id,
                            calendarId = cId,
                            title = title,
                            description = desc,
                            location = loc,
                            startMillis = begin,
                            endMillis = end,
                            isAllDay = isAllDay,
                            color = color
                        )
                    )
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return events
    }

    /**
     * Inserts an appointment / event directly into the selected Google Calendar.
     */
    fun insertEvent(event: NewCalendarEvent): Result<Long> {
        if (!hasCalendarPermissions()) {
            return Result.failure(SecurityException("Calendar permissions not granted"))
        }

        return try {
            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, event.calendarId)
                put(CalendarContract.Events.TITLE, event.title)
                put(CalendarContract.Events.DESCRIPTION, event.description)
                put(CalendarContract.Events.EVENT_LOCATION, event.location)
                put(CalendarContract.Events.DTSTART, event.startMillis)
                put(CalendarContract.Events.DTEND, event.endMillis)
                put(CalendarContract.Events.ALL_DAY, if (event.isAllDay) 1 else 0)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }

            val uri: Uri? = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            val eventId = uri?.lastPathSegment?.toLongOrNull()
            if (eventId != null) {
                Result.success(eventId)
            } else {
                Result.failure(Exception("Failed to insert event"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Deletes an event by ID.
     */
    fun deleteEvent(eventId: Long): Boolean {
        if (!hasCalendarPermissions()) return false
        return try {
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            val rows = context.contentResolver.delete(uri, null, null)
            rows > 0
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Updates an existing event in Google Calendar.
     */
    fun updateEvent(
        eventId: Long,
        calendarId: Long,
        title: String,
        description: String?,
        location: String?,
        startMillis: Long,
        endMillis: Long,
        isAllDay: Boolean
    ): Result<Boolean> {
        if (!hasCalendarPermissions()) {
            return Result.failure(SecurityException("Calendar permissions not granted"))
        }

        return try {
            val values = ContentValues().apply {
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.TITLE, title)
                put(CalendarContract.Events.DESCRIPTION, description)
                put(CalendarContract.Events.EVENT_LOCATION, location)
                put(CalendarContract.Events.DTSTART, startMillis)
                put(CalendarContract.Events.DTEND, endMillis)
                put(CalendarContract.Events.ALL_DAY, if (isAllDay) 1 else 0)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            }
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            val rows = context.contentResolver.update(uri, values, null, null)
            if (rows > 0) {
                Result.success(true)
            } else {
                Result.failure(Exception("Failed to update event in calendar"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Creates an Intent to open the native calendar app for inserting an event on a given date.
     */
    fun createInsertCalendarIntent(
        title: String = "",
        startMillis: Long,
        endMillis: Long,
        isAllDay: Boolean = false,
        description: String = ""
    ): Intent {
        return Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.Events.TITLE, title)
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
            putExtra(CalendarContract.EXTRA_EVENT_ALL_DAY, isAllDay)
            putExtra(CalendarContract.Events.DESCRIPTION, description)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
    }
}
