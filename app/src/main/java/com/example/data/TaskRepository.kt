package com.example.data

import android.content.Context
import com.example.data.sync.DeviceCalendarSyncManager
import com.example.notification.TaskReminderScheduler
import com.example.widget.WidgetUpdateHelper
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class TaskRepository(
    private val taskDao: TaskDao,
    private val context: Context,
    val calendarSyncManager: DeviceCalendarSyncManager = DeviceCalendarSyncManager(context),
    private val reminderScheduler: TaskReminderScheduler = TaskReminderScheduler(context),
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    val allTasks: Flow<List<TaskEntity>> = taskDao.getAllTasks()
    val pendingTasks: Flow<List<TaskEntity>> = taskDao.getPendingTasks()
    val completedTasks: Flow<List<TaskEntity>> = taskDao.getCompletedTasks()

    fun getTask(id: Long): Flow<TaskEntity?> = taskDao.getTaskById(id)

    suspend fun insertTask(task: TaskEntity): Long = withContext(ioDispatcher) {
        val insertedId = taskDao.insertTask(task)
        val createdTask = task.copy(id = insertedId)

        // 1. Schedule exact due reminder if applicable
        if (createdTask.dueDateMillis != null && !createdTask.isCompleted) {
            reminderScheduler.scheduleReminder(createdTask)
        }

        // 2. Update all Home Screen Widgets
        WidgetUpdateHelper.updateAllWidgets(context)

        // 3. Background sync to Device Calendar if enabled and task has a date
        if (createdTask.dueDateMillis != null) {
            CoroutineScope(ioDispatcher).launch {
                try {
                    val eventId = calendarSyncManager.syncTaskToDeviceCalendar(createdTask)
                    if (eventId != null) {
                        taskDao.updateTask(createdTask.copy(calendarEventId = eventId, isCalendarSynced = true))
                    }
                } catch (_: Exception) {}
            }
        }

        insertedId
    }

    suspend fun updateTask(task: TaskEntity) = withContext(ioDispatcher) {
        taskDao.updateTask(task)

        // 1. Reschedule or cancel reminder
        if (task.isCompleted || task.dueDateMillis == null) {
            reminderScheduler.cancelReminder(task.id)
        } else {
            reminderScheduler.scheduleReminder(task)
        }

        // 2. Update Widgets
        WidgetUpdateHelper.updateAllWidgets(context)

        // 3. Sync update to Device Calendar
        CoroutineScope(ioDispatcher).launch {
            try {
                if (task.calendarEventId != null) {
                    calendarSyncManager.updateCalendarEvent(task)
                } else if (task.dueDateMillis != null && !task.isCompleted) {
                    val eventId = calendarSyncManager.syncTaskToDeviceCalendar(task)
                    if (eventId != null) {
                        taskDao.updateTask(task.copy(calendarEventId = eventId, isCalendarSynced = true))
                    }
                }
            } catch (_: Exception) {}
        }
    }

    suspend fun deleteTask(task: TaskEntity) = withContext(ioDispatcher) {
        reminderScheduler.cancelReminder(task.id)
        taskDao.deleteTask(task)
        WidgetUpdateHelper.updateAllWidgets(context)

        CoroutineScope(ioDispatcher).launch {
            try {
                calendarSyncManager.deleteCalendarEvent(task.calendarEventId)
            } catch (_: Exception) {}
        }
    }

    suspend fun deleteTaskById(id: Long) = withContext(ioDispatcher) {
        reminderScheduler.cancelReminder(id)
        val task = taskDao.getTaskById(id).firstOrNull()
        taskDao.deleteTaskById(id)
        WidgetUpdateHelper.updateAllWidgets(context)

        if (task?.calendarEventId != null) {
            CoroutineScope(ioDispatcher).launch {
                try {
                    calendarSyncManager.deleteCalendarEvent(task.calendarEventId)
                } catch (_: Exception) {}
            }
        }
    }

    suspend fun toggleTaskCompletion(task: TaskEntity) = withContext(ioDispatcher) {
        val newStatus = !task.isCompleted
        
        if (newStatus && task.recurrence != RecurrenceType.NONE) {
            // Advance recurring task to next occurrence
            val nextDateMillis = com.example.util.DateTimeUtils.calculateNextRecurrenceDate(task.dueDateMillis, task.recurrence)
            val updatedTask = task.copy(
                dueDateMillis = nextDateMillis,
                isCompleted = false,
                completedAt = null
            )
            taskDao.updateTask(updatedTask)
            reminderScheduler.scheduleReminder(updatedTask)
            
            CoroutineScope(ioDispatcher).launch {
                try {
                    if (updatedTask.calendarEventId != null) {
                        calendarSyncManager.updateCalendarEvent(updatedTask)
                    }
                } catch (_: Exception) {}
            }
        } else {
            val completedAt = if (newStatus) System.currentTimeMillis() else null
            taskDao.updateCompletionStatus(task.id, newStatus, completedAt)

            if (newStatus) {
                reminderScheduler.cancelReminder(task.id)
            } else if (task.dueDateMillis != null) {
                reminderScheduler.scheduleReminder(task.copy(isCompleted = false))
            }
        }

        WidgetUpdateHelper.updateAllWidgets(context)
    }

    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean) = withContext(ioDispatcher) {
        val task = taskDao.getTaskById(id).firstOrNull()
        
        if (isCompleted && task != null && task.recurrence != RecurrenceType.NONE) {
            // Advance recurring task to next occurrence
            val nextDateMillis = com.example.util.DateTimeUtils.calculateNextRecurrenceDate(task.dueDateMillis, task.recurrence)
            val updatedTask = task.copy(
                dueDateMillis = nextDateMillis,
                isCompleted = false,
                completedAt = null
            )
            taskDao.updateTask(updatedTask)
            reminderScheduler.scheduleReminder(updatedTask)
            
            CoroutineScope(ioDispatcher).launch {
                try {
                    if (updatedTask.calendarEventId != null) {
                        calendarSyncManager.updateCalendarEvent(updatedTask)
                    }
                } catch (_: Exception) {}
            }
        } else {
            val completedAt = if (isCompleted) System.currentTimeMillis() else null
            taskDao.updateCompletionStatus(id, isCompleted, completedAt)

            if (isCompleted) {
                reminderScheduler.cancelReminder(id)
            } else if (task?.dueDateMillis != null) {
                reminderScheduler.scheduleReminder(task.copy(isCompleted = false))
            }
        }

        WidgetUpdateHelper.updateAllWidgets(context)
    }

    suspend fun getAllTasksDirect(): List<TaskEntity> = withContext(ioDispatcher) {
        taskDao.getAllTasksDirect()
    }

    suspend fun restoreTasks(tasks: List<TaskEntity>, replaceExisting: Boolean) = withContext(ioDispatcher) {
        if (replaceExisting) {
            // Cancel all existing reminders first
            val currentTasks = taskDao.getAllTasksDirect()
            currentTasks.forEach { reminderScheduler.cancelReminder(it.id) }
            taskDao.deleteAllTasks()
        }

        // Clean foreign calendar IDs and reset local IDs if merging
        val preparedTasks = tasks.map { task ->
            if (replaceExisting) task.copy(calendarEventId = null, isCalendarSynced = false)
            else task.copy(id = 0, calendarEventId = null, isCalendarSynced = false)
        }

        taskDao.insertTasks(preparedTasks)

        // Reschedule reminders for all pending dated tasks
        val newTasks = taskDao.getAllTasksDirect()
        newTasks.filter { !it.isCompleted && it.dueDateMillis != null }.forEach {
            reminderScheduler.scheduleReminder(it)
        }

        WidgetUpdateHelper.updateAllWidgets(context)
    }

    suspend fun syncAllDatedTasksToCalendar(): Int = withContext(ioDispatcher) {
        val tasks = allTasks.firstOrNull() ?: emptyList()
        var syncedCount = 0
        for (task in tasks) {
            if (task.dueDateMillis != null && !task.isCompleted) {
                try {
                    if (task.calendarEventId == null) {
                        val eventId = calendarSyncManager.syncTaskToDeviceCalendar(task)
                        if (eventId != null) {
                            taskDao.updateTask(task.copy(calendarEventId = eventId, isCalendarSynced = true))
                            syncedCount++
                        }
                    } else {
                        calendarSyncManager.updateCalendarEvent(task)
                        syncedCount++
                    }
                } catch (_: Exception) {}
            }
        }
        syncedCount
    }

    /**
     * Performs a bidirectional sync: reads events from the device calendar and updates
     * local QuickTask tasks if their title, notes, or time were modified in Google Calendar / phone calendar.
     */
    suspend fun syncFromDeviceCalendar(): Int = withContext(ioDispatcher) {
        if (!calendarSyncManager.hasCalendarPermissions()) return@withContext 0

        val tasksWithEvents = taskDao.getAllTasksDirect().filter { it.calendarEventId != null && !it.isCompleted }
        var updatedCount = 0

        for (task in tasksWithEvents) {
            val eventId = task.calendarEventId ?: continue
            val eventData = calendarSyncManager.getCalendarEvent(eventId)

            if (eventData != null) {
                // Extract date and time from calendar event dtStart
                val cal = Calendar.getInstance().apply { timeInMillis = eventData.startMillis }
                val newStartHour = if (eventData.isAllDay) null else cal.get(Calendar.HOUR_OF_DAY)
                val newStartMin = if (eventData.isAllDay) null else cal.get(Calendar.MINUTE)

                // Extract end time
                val endCal = Calendar.getInstance().apply { timeInMillis = eventData.endMillis }
                val newEndHour = if (eventData.isAllDay) null else endCal.get(Calendar.HOUR_OF_DAY)
                val newEndMin = if (eventData.isAllDay) null else endCal.get(Calendar.MINUTE)

                val newDueDateMillis = com.example.util.DateTimeUtils.getStartOfDay(eventData.startMillis)
                val newTitle = if (eventData.title.isNotBlank()) eventData.title else task.title
                val newNotes = if (eventData.description.isNotBlank() && eventData.description != "Created via Quick Task") {
                    eventData.description
                } else {
                    task.notes
                }

                // Check if any significant property changed
                val titleChanged = task.title != newTitle
                val notesChanged = task.notes != newNotes
                val dateChanged = task.dueDateMillis != newDueDateMillis
                val timeChanged = task.dueTimeHour != newStartHour || task.dueTimeMinute != newStartMin ||
                        task.endTimeHour != newEndHour || task.endTimeMinute != newEndMin

                if (titleChanged || notesChanged || dateChanged || timeChanged) {
                    val updatedTask = task.copy(
                        title = newTitle,
                        notes = newNotes,
                        dueDateMillis = newDueDateMillis,
                        dueTimeHour = newStartHour,
                        dueTimeMinute = newStartMin,
                        endTimeHour = newEndHour,
                        endTimeMinute = newEndMin
                    )
                    taskDao.updateTask(updatedTask)
                    reminderScheduler.scheduleReminder(updatedTask)
                    updatedCount++
                }
            }
        }

        if (updatedCount > 0) {
            WidgetUpdateHelper.updateAllWidgets(context)
        }
        return@withContext updatedCount
    }
}
