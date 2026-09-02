package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.example.TaskApplication
import com.example.data.Priority
import com.example.data.TaskEntity
import com.example.util.SmartTimeParser
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles direct text reply input from the quick task notification with smart natural language time parsing.
 */
class TaskNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TaskNotificationManager.ACTION_ADD_TASK_FROM_NOTIFICATION) {
            val remoteInputResults = RemoteInput.getResultsFromIntent(intent)
            val rawInput = remoteInputResults?.getCharSequence(TaskNotificationManager.KEY_TEXT_REPLY)?.toString()?.trim()

            if (!rawInput.isNullOrBlank()) {
                val app = context.applicationContext as TaskApplication
                val repository = app.repository
                val notificationManager = TaskNotificationManager(context)

                // Smart parse natural language date, start time, end time, and clean title in 24h format
                val parsed = SmartTimeParser.parse(rawInput)

                // Insert into database via coroutine with goAsync to prevent premature process death
                val pendingResult = goAsync()
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val newTask = TaskEntity(
                            title = parsed.cleanTitle,
                            notes = "",
                            dueDateMillis = parsed.dueDateMillis,
                            dueTimeHour = parsed.startHour,
                            dueTimeMinute = parsed.startMinute,
                            endTimeHour = parsed.endHour,
                            endTimeMinute = parsed.endMinute,
                            priority = Priority.MEDIUM,
                            category = "General",
                            recurrence = parsed.recurrence
                        )
                        repository.insertTask(newTask)

                        // Format time confirmation for user feedback
                        val scheduleText = if (parsed.dueDateMillis != null) {
                            val dateFormatted = com.example.util.DateTimeUtils.formatDueDate(
                                parsed.dueDateMillis,
                                parsed.startHour,
                                parsed.startMinute,
                                parsed.endHour,
                                parsed.endMinute
                            )
                            if (parsed.recurrence != com.example.data.RecurrenceType.NONE) {
                                "$dateFormatted • ${parsed.recurrence.label}"
                            } else {
                                dateFormatted
                            }
                        } else {
                            if (parsed.recurrence != com.example.data.RecurrenceType.NONE) {
                                "No due date • ${parsed.recurrence.label}"
                            } else {
                                "No specific due date"
                            }
                        }

                        // Update notification with title and parsed time confirmation
                        notificationManager.showTaskAddedConfirmation(
                            taskTitle = parsed.cleanTitle,
                            scheduleText = scheduleText
                        )
                    } finally {
                        pendingResult.finish()
                    }
                }
            }
        }
    }
}
