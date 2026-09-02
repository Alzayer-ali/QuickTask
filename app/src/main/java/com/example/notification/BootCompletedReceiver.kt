package com.example.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.TaskApplication
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * Reschedules all pending task reminders when the device finishes booting.
 */
class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED ||
            intent.action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val app = context.applicationContext as? TaskApplication ?: return
            val repository = app.repository
            val scheduler = TaskReminderScheduler(context)

            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val pendingTasks = repository.pendingTasks.firstOrNull() ?: emptyList()
                    val now = System.currentTimeMillis()
                    for (task in pendingTasks) {
                        if (task.dueDateMillis != null) {
                            val triggerTime = scheduler.calculateTriggerTime(task)
                            if (triggerTime != null && triggerTime > now) {
                                scheduler.scheduleReminder(task)
                            }
                        }
                    }

                    // Update all widgets on home screen after boot
                    com.example.widget.WidgetUpdateHelper.updateAllWidgets(context)
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
