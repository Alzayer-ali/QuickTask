package com.example.data.sync

import android.Manifest
import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.CalendarContract
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.TaskEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.TimeZone

class DeviceCalendarSyncManager(private val context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("device_calendar_sync_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_AUTO_CALENDAR_SYNC = "auto_calendar_sync_enabled"
        private const val TAG = "CalendarSyncManager"
    }

    private val _isAutoSyncEnabled = MutableStateFlow(prefs.getBoolean(KEY_AUTO_CALENDAR_SYNC, true))
    val isAutoSyncEnabled: StateFlow<Boolean> = _isAutoSyncEnabled.asStateFlow()

    private val _statusMessage = MutableStateFlow<String>("Ready")
    val statusMessage: StateFlow<String> = _statusMessage.asStateFlow()

    fun setAutoSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AUTO_CALENDAR_SYNC, enabled).apply()
        _isAutoSyncEnabled.value = enabled
        _statusMessage.value = if (enabled) "Auto-sync to Phone Calendar is ON" else "Auto-sync to Phone Calendar is OFF"
    }

    fun hasCalendarPermissions(): Boolean {
        val writePerm = ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_CALENDAR)
        val readPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR)
        return writePerm == PackageManager.PERMISSION_GRANTED && readPerm == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Gets the primary writable calendar ID on the user's device.
     */
    private fun getPrimaryCalendarId(): Long? {
        if (!hasCalendarPermissions()) return null

        val projection = arrayOf(
            CalendarContract.Calendars._ID,
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,
            CalendarContract.Calendars.IS_PRIMARY,
            CalendarContract.Calendars.VISIBLE
        )

        var cursor: Cursor? = null
        try {
            cursor = context.contentResolver.query(
                CalendarContract.Calendars.CONTENT_URI,
                projection,
                "${CalendarContract.Calendars.VISIBLE} = 1",
                null,
                "${CalendarContract.Calendars.IS_PRIMARY} DESC, ${CalendarContract.Calendars._ID} ASC"
            )

            if (cursor != null && cursor.moveToFirst()) {
                val idCol = cursor.getColumnIndexOrThrow(CalendarContract.Calendars._ID)
                return cursor.getLong(idCol)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying primary calendar", e)
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * Calculates start and end timestamps in millis for a task.
     */
    fun calculateEventTimes(
        dueDateMillis: Long?,
        startHour: Int?,
        startMinute: Int?,
        endHour: Int? = null,
        endMinute: Int? = null
    ): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        if (dueDateMillis != null) {
            calendar.timeInMillis = dueDateMillis
        }

        if (startHour != null && startMinute != null) {
            calendar.set(Calendar.HOUR_OF_DAY, startHour)
            calendar.set(Calendar.MINUTE, startMinute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis

            val endTime = if (endHour != null && endMinute != null) {
                val endCal = Calendar.getInstance().apply {
                    timeInMillis = dueDateMillis ?: System.currentTimeMillis()
                    set(Calendar.HOUR_OF_DAY, endHour)
                    set(Calendar.MINUTE, endMinute)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                if (endCal.timeInMillis < startTime) {
                    endCal.add(Calendar.DAY_OF_YEAR, 1)
                }
                endCal.timeInMillis
            } else {
                // Default event duration: 30 minutes
                startTime + (30 * 60 * 1000L)
            }
            return Pair(startTime, endTime)
        } else {
            // All-day event start at beginning of day
            calendar.set(Calendar.HOUR_OF_DAY, 9)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startTime = calendar.timeInMillis
            val endTime = startTime + (60 * 60 * 1000L)
            return Pair(startTime, endTime)
        }
    }

    /**
     * Inserts the task directly into the device's native Calendar database.
     * Returns the calendar event ID if successful.
     */
    suspend fun syncTaskToDeviceCalendar(task: TaskEntity): Long? = withContext(Dispatchers.IO) {
        if (!_isAutoSyncEnabled.value) return@withContext null
        if (!hasCalendarPermissions()) {
            Log.d(TAG, "Calendar permission not granted for direct insert")
            return@withContext null
        }

        val calendarId = getPrimaryCalendarId() ?: return@withContext null
        val (startTime, endTime) = calculateEventTimes(
            task.dueDateMillis,
            task.dueTimeHour,
            task.dueTimeMinute,
            task.endTimeHour,
            task.endTimeMinute
        )

        try {
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startTime)
                put(CalendarContract.Events.DTEND, endTime)
                put(CalendarContract.Events.TITLE, task.title)
                put(CalendarContract.Events.DESCRIPTION, if (task.notes.isNotBlank()) task.notes else "Created via Quick Task")
                put(CalendarContract.Events.CALENDAR_ID, calendarId)
                put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
                put(CalendarContract.Events.HAS_ALARM, 1)
            }

            val uri: Uri? = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
            val eventId = uri?.lastPathSegment?.toLongOrNull()

            if (eventId != null) {
                // Add a 10-minute reminder alarm to the event
                val reminderValues = ContentValues().apply {
                    put(CalendarContract.Reminders.EVENT_ID, eventId)
                    put(CalendarContract.Reminders.MINUTES, 10)
                    put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                }
                context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
                _statusMessage.value = "Added to Calendar: ${task.title}"
                Log.d(TAG, "Successfully added task to device calendar with Event ID: $eventId")
                return@withContext eventId
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error inserting event to calendar", e)
            _statusMessage.value = "Calendar error: ${e.localizedMessage}"
        }
        null
    }

    /**
     * Updates an existing event in the device calendar.
     */
    suspend fun updateCalendarEvent(task: TaskEntity): Boolean = withContext(Dispatchers.IO) {
        val eventId = task.calendarEventId ?: return@withContext false
        if (!hasCalendarPermissions()) return@withContext false

        val (startTime, endTime) = calculateEventTimes(
            task.dueDateMillis,
            task.dueTimeHour,
            task.dueTimeMinute,
            task.endTimeHour,
            task.endTimeMinute
        )

        try {
            val values = ContentValues().apply {
                put(CalendarContract.Events.DTSTART, startTime)
                put(CalendarContract.Events.DTEND, endTime)
                put(CalendarContract.Events.TITLE, task.title)
                put(CalendarContract.Events.DESCRIPTION, task.notes)
            }

            val updateUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            val rows = context.contentResolver.update(updateUri, values, null, null)
            return@withContext rows > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error updating calendar event $eventId", e)
            return@withContext false
        }
    }

    /**
     * Deletes an event from the device calendar.
     */
    suspend fun deleteCalendarEvent(eventId: Long?): Boolean = withContext(Dispatchers.IO) {
        if (eventId == null || !hasCalendarPermissions()) return@withContext false

        try {
            val deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            val rows = context.contentResolver.delete(deleteUri, null, null)
            return@withContext rows > 0
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting calendar event $eventId", e)
            return@withContext false
        }
    }

    data class CalendarEventData(
        val eventId: Long,
        val title: String,
        val description: String,
        val startMillis: Long,
        val endMillis: Long,
        val isAllDay: Boolean
    )

    /**
     * Reads calendar event details by event ID directly from the device's Calendar provider.
     */
    fun getCalendarEvent(eventId: Long): CalendarEventData? {
        if (!hasCalendarPermissions()) return null

        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.ALL_DAY
        )

        var cursor: Cursor? = null
        try {
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            cursor = context.contentResolver.query(uri, projection, null, null, null)
            if (cursor != null && cursor.moveToFirst()) {
                val titleCol = cursor.getColumnIndexOrThrow(CalendarContract.Events.TITLE)
                val descCol = cursor.getColumnIndexOrThrow(CalendarContract.Events.DESCRIPTION)
                val startCol = cursor.getColumnIndexOrThrow(CalendarContract.Events.DTSTART)
                val endCol = cursor.getColumnIndexOrThrow(CalendarContract.Events.DTEND)
                val allDayCol = cursor.getColumnIndexOrThrow(CalendarContract.Events.ALL_DAY)

                val title = cursor.getString(titleCol) ?: ""
                val desc = cursor.getString(descCol) ?: ""
                val dtStart = cursor.getLong(startCol)
                val dtEnd = cursor.getLong(endCol)
                val allDay = cursor.getInt(allDayCol) == 1

                return CalendarEventData(
                    eventId = eventId,
                    title = title,
                    description = desc,
                    startMillis = dtStart,
                    endMillis = dtEnd,
                    isAllDay = allDay
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error reading calendar event $eventId", e)
        } finally {
            cursor?.close()
        }
        return null
    }

    /**
     * Checks if a calendar event has been deleted from the device calendar.
     */
    fun isEventDeleted(eventId: Long): Boolean {
        if (!hasCalendarPermissions()) return false
        var cursor: Cursor? = null
        try {
            val uri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
            cursor = context.contentResolver.query(
                uri,
                arrayOf(CalendarContract.Events._ID, CalendarContract.Events.DELETED),
                null,
                null,
                null
            )
            if (cursor != null && cursor.moveToFirst()) {
                val deletedCol = cursor.getColumnIndex(CalendarContract.Events.DELETED)
                if (deletedCol != -1) {
                    return cursor.getInt(deletedCol) == 1
                }
                return false
            }
            return true // Not found means deleted
        } catch (e: Exception) {
            return false
        } finally {
            cursor?.close()
        }
    }

    /**
     * Launches the default phone Calendar app with a pre-filled event using Android Intent.
     * Works on ANY Android device without requiring runtime permissions!
     */
    fun launchAddToCalendarIntent(context: Context, task: TaskEntity) {
        val (startTime, endTime) = calculateEventTimes(
            task.dueDateMillis,
            task.dueTimeHour,
            task.dueTimeMinute,
            task.endTimeHour,
            task.endTimeMinute
        )

        val intent = Intent(Intent.ACTION_INSERT).apply {
            data = CalendarContract.Events.CONTENT_URI
            putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startTime)
            putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endTime)
            putExtra(CalendarContract.Events.TITLE, task.title)
            putExtra(CalendarContract.Events.DESCRIPTION, task.notes)
            putExtra(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_BUSY)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Could not launch calendar intent", e)
        }
    }

    /**
     * Opens the device's native Calendar app directly.
     */
    fun openCalendarApp(context: Context, timeMillis: Long? = null) {
        val targetTime = timeMillis ?: System.currentTimeMillis()
        val builder = CalendarContract.CONTENT_URI.buildUpon().appendPath("time").appendPath(targetTime.toString())
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = builder.build()
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        try {
            context.startActivity(intent)
        } catch (e: Exception) {
            Log.e(TAG, "Could not open calendar app", e)
        }
    }
}
