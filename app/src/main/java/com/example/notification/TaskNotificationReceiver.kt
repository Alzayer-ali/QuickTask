package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.RemoteInput
import com.example.TaskApplication
import com.example.data.Priority
import com.example.data.TaskEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles direct text reply input from the quick task notification.
 */
class TaskNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == TaskNotificationManager.ACTION_ADD_TASK_FROM_NOTIFICATION) {
            val remoteInputResults = RemoteInput.getResultsFromIntent(intent)
            val taskTitle = remoteInputResults?.getCharSequence(TaskNotificationManager.KEY_TEXT_REPLY)?.toString()?.trim()

            if (!taskTitle.isNullOrBlank()) {
                val app = context.applicationContext as TaskApplication
                val repository = app.repository
                val notificationManager = TaskNotificationManager(context)

                // Insert into database via coroutine
                CoroutineScope(Dispatchers.IO).launch {
                    val newTask = TaskEntity(
                        title = taskTitle,
                        notes = "",
                        priority = Priority.MEDIUM,
                        category = "General"
                    )
                    repository.insertTask(newTask)

                    // Immediately cancel and dismiss the notification
                    notificationManager.cancelNotification()
                }
            }
        }
    }
}
