package com.example.notification

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.TaskApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Handles actions from the Reminder Notification (e.g. marking a task as done).
 */
class TaskReminderActionReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(TaskReminderScheduler.EXTRA_TASK_ID, -1L)
        if (taskId == -1L) return

        val app = context.applicationContext as? TaskApplication ?: return
        val repository = app.repository

        // Dismiss the reminder notification
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(taskId.toInt())

        // Mark task as completed in DB
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                repository.updateCompletionStatus(taskId, true)
            } finally {
                pendingResult.finish()
            }
        }
    }
}
