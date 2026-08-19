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
        val completedAt = if (newStatus) System.currentTimeMillis() else null
        taskDao.updateCompletionStatus(task.id, newStatus, completedAt)

        if (newStatus) {
            reminderScheduler.cancelReminder(task.id)
        } else if (task.dueDateMillis != null) {
            reminderScheduler.scheduleReminder(task.copy(isCompleted = false))
        }

        WidgetUpdateHelper.updateAllWidgets(context)
    }

    suspend fun updateCompletionStatus(id: Long, isCompleted: Boolean) = withContext(ioDispatcher) {
        val completedAt = if (isCompleted) System.currentTimeMillis() else null
        taskDao.updateCompletionStatus(id, isCompleted, completedAt)

        val task = taskDao.getTaskById(id).firstOrNull()
        if (isCompleted) {
            reminderScheduler.cancelReminder(id)
        } else if (task?.dueDateMillis != null) {
            reminderScheduler.scheduleReminder(task.copy(isCompleted = false))
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
}
