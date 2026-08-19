package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class TaskReminderReceiver : BroadcastReceiver() {

    companion object {
        const val REMINDER_CHANNEL_ID = "task_due_reminders_channel_v1"
        const val REMINDER_CHANNEL_NAME = "Task Due Reminders"
        const val ACTION_MARK_TASK_COMPLETE = "com.example.notification.ACTION_MARK_COMPLETE"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(TaskReminderScheduler.EXTRA_TASK_ID, -1L)
        val taskTitle = intent.getStringExtra(TaskReminderScheduler.EXTRA_TASK_TITLE) ?: "Task Reminder"
        val taskNotes = intent.getStringExtra(TaskReminderScheduler.EXTRA_TASK_NOTES) ?: ""
        val taskPriority = intent.getStringExtra(TaskReminderScheduler.EXTRA_TASK_PRIORITY) ?: "MEDIUM"

        if (taskId == -1L) return

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        createReminderChannel(notificationManager)

        // 1. PendingIntent when tapping notification -> opens MainActivity
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_HIGHLIGHT_TASK_ID", taskId)
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            taskId.toInt(),
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Action: Mark as Completed directly from notification
        val markCompleteIntent = Intent(context, TaskReminderActionReceiver::class.java).apply {
            action = ACTION_MARK_TASK_COMPLETE
            putExtra(TaskReminderScheduler.EXTRA_TASK_ID, taskId)
        }
        val markCompletePendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.toInt() + 10000,
            markCompleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val priorityText = if (taskPriority == "HIGH") "🔥 HIGH PRIORITY: " else ""
        val contentText = if (taskNotes.isNotBlank()) taskNotes else "This task is scheduled for now"

        val defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)

        val notification = NotificationCompat.Builder(context, REMINDER_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_task_notification)
            .setContentTitle("$priorityText$taskTitle")
            .setContentText(contentText)
            .setStyle(NotificationCompat.BigTextStyle().bigText(contentText))
            .setContentIntent(openAppPendingIntent)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setSound(defaultSoundUri)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .addAction(
                android.R.drawable.checkbox_on_background,
                "✓ Mark Done",
                markCompletePendingIntent
            )
            .build()

        notificationManager.notify(taskId.toInt(), notification)
    }

    private fun createReminderChannel(notificationManager: NotificationManager) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val soundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            val audioAttributes = AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                .build()

            val channel = NotificationChannel(
                REMINDER_CHANNEL_ID,
                REMINDER_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "High-priority notifications for task due dates and reminders"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
                setSound(soundUri, audioAttributes)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }
}
