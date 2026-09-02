package com.example.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.TaskEntity
import java.util.Calendar

class TaskReminderScheduler(private val context: Context) {

    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager

    companion object {
        const val EXTRA_TASK_ID = "extra_task_id"
        const val EXTRA_TASK_TITLE = "extra_task_title"
        const val EXTRA_TASK_NOTES = "extra_task_notes"
        const val EXTRA_TASK_PRIORITY = "extra_task_priority"
    }

    /**
     * Calculates the exact trigger timestamp in milliseconds for a task's due date and time.
     */
    fun calculateTriggerTime(task: TaskEntity): Long? {
        val dueDate = task.dueDateMillis ?: return null

        val calendar = Calendar.getInstance().apply {
            timeInMillis = dueDate
            if (task.dueTimeHour != null && task.dueTimeMinute != null) {
                set(Calendar.HOUR_OF_DAY, task.dueTimeHour)
                set(Calendar.MINUTE, task.dueTimeMinute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            } else {
                // Default to 9:00 AM on the due date
                set(Calendar.HOUR_OF_DAY, 9)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        }
        return calendar.timeInMillis
    }

    /**
     * Schedules an exact system alarm for the task if its due date/time is in the future.
     */
    fun scheduleReminder(task: TaskEntity) {
        if (task.isCompleted) {
            cancelReminder(task.id)
            return
        }

        val triggerTime = calculateTriggerTime(task) ?: return
        val currentTime = System.currentTimeMillis()

        // Only schedule if due time is in the future
        if (triggerTime <= currentTime) {
            Log.d("TaskReminder", "Trigger time is in the past ($triggerTime <= $currentTime), skipping alarm for task ${task.id}")
            return
        }

        val intent = Intent(context, TaskReminderReceiver::class.java).apply {
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_TASK_TITLE, task.title)
            putExtra(EXTRA_TASK_NOTES, task.notes)
            putExtra(EXTRA_TASK_PRIORITY, task.priority.name)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            if (alarmManager != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    if (alarmManager.canScheduleExactAlarms()) {
                        alarmManager.setExactAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    } else {
                        alarmManager.setAndAllowWhileIdle(
                            AlarmManager.RTC_WAKEUP,
                            triggerTime,
                            pendingIntent
                        )
                    }
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setExact(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
                Log.d("TaskReminder", "Scheduled reminder for task '${task.title}' at $triggerTime")
            }
        } catch (e: Exception) {
            Log.e("TaskReminder", "Failed to schedule alarm for task ${task.id}", e)
        }
    }

    /**
     * Cancels any scheduled reminder for the specified task.
     */
    fun cancelReminder(taskId: Long) {
        val intent = Intent(context, TaskReminderReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt(),
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null && alarmManager != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
            Log.d("TaskReminder", "Cancelled reminder for task ID $taskId")
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager
        notificationManager?.cancel(TaskReminderReceiver.getReminderNotificationId(taskId))
    }
}
