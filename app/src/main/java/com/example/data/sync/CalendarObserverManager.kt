package com.example.data.sync

import android.content.Context
import android.database.ContentObserver
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.provider.CalendarContract
import android.util.Log
import com.example.data.TaskRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Observes changes to the Android Calendar Provider.
 * When the user edits or updates an event in Google Calendar / Samsung Calendar / Device Calendar,
 * this observer automatically triggers a reverse-sync to update the matching task in Quick Task.
 */
class CalendarObserverManager(
    private val context: Context,
    private val repository: TaskRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var debounceJob: Job? = null
    private var isRegistered = false

    private val observer = object : ContentObserver(Handler(Looper.getMainLooper())) {
        override fun onChange(selfChange: Boolean, uri: Uri?) {
            super.onChange(selfChange, uri)
            Log.d("CalendarObserver", "Calendar content changed: $uri")
            
            // Debounce rapid calendar provider events (e.g., when saving an event)
            debounceJob?.cancel()
            debounceJob = scope.launch {
                delay(800) // Wait 800ms to let provider finish writes
                try {
                    if (repository.calendarSyncManager.hasCalendarPermissions()) {
                        val updatedCount = repository.syncFromDeviceCalendar()
                        if (updatedCount > 0) {
                            Log.d("CalendarObserver", "Auto-synced $updatedCount task(s) from external calendar change")
                        }
                    }
                } catch (e: Exception) {
                    Log.e("CalendarObserver", "Error syncing from calendar observer", e)
                }
            }
        }
    }

    fun startObserving() {
        if (isRegistered) return
        
        // Guard with explicit permission check before querying/registering against Calendar ContentProvider
        if (!repository.calendarSyncManager.hasCalendarPermissions()) {
            Log.d("CalendarObserver", "Skipping calendar observer registration: calendar permissions not granted yet.")
            return
        }

        try {
            context.contentResolver.registerContentObserver(
                CalendarContract.Events.CONTENT_URI,
                true,
                observer
            )
            isRegistered = true
            Log.d("CalendarObserver", "Calendar ContentObserver registered successfully")
        } catch (e: SecurityException) {
            Log.w("CalendarObserver", "Calendar permission denied while attempting to register ContentObserver: ${e.message}")
        } catch (e: Exception) {
            Log.e("CalendarObserver", "Could not register calendar observer", e)
        }
    }

    fun stopObserving() {
        if (!isRegistered) return
        try {
            context.contentResolver.unregisterContentObserver(observer)
            isRegistered = false
            debounceJob?.cancel()
            Log.d("CalendarObserver", "Calendar ContentObserver unregistered")
        } catch (e: Exception) {
            Log.e("CalendarObserver", "Could not unregister calendar observer", e)
        }
    }
}
