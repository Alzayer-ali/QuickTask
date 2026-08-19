package com.example.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.RemoteInput
import com.example.MainActivity
import com.example.R

/**
 * Class dedicated solely to creating and displaying the Quick Task notification
 * with an inline text input (RemoteInput) and an Add Task action button.
 */
class TaskNotificationManager(private val context: Context) {

    companion object {
        const val CHANNEL_ID = "quick_task_notification_channel_v3"
        const val CHANNEL_NAME = "Quick Task Entry"
        const val NOTIFICATION_ID = 1001

        const val ACTION_ADD_TASK_FROM_NOTIFICATION = "com.example.notification.ACTION_ADD_TASK"
        const val KEY_TEXT_REPLY = "key_task_title_reply"
    }

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Remove legacy gentle channel if present
            try {
                notificationManager.deleteNotificationChannel("quick_task_notification_channel")
            } catch (_: Exception) {}

            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notification with direct text reply to quickly capture tasks"
                setShowBadge(true)
                enableLights(true)
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Posts a normal one-time Quick Task notification with text box and Add button.
     * When the user submits a task, the notification will be dismissed automatically.
     */
    fun showQuickTaskNotification() {
        // 1. PendingIntent when tapping notification body -> opens MainActivity
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            context,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 2. Direct Reply RemoteInput
        val remoteInput = RemoteInput.Builder(KEY_TEXT_REPLY)
            .setLabel("Type task title...")
            .build()

        // 3. PendingIntent for the BroadcastReceiver to handle text submission
        val replyIntent = Intent(context, TaskNotificationReceiver::class.java).apply {
            action = ACTION_ADD_TASK_FROM_NOTIFICATION
        }
        val replyPendingIntent = PendingIntent.getBroadcast(
            context,
            1,
            replyIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0)
        )

        // 4. Notification Action with RemoteInput
        val replyAction = NotificationCompat.Action.Builder(
            android.R.drawable.ic_input_add,
            "Add Task",
            replyPendingIntent
        )
            .addRemoteInput(remoteInput)
            .setAllowGeneratedReplies(true)
            .build()

        // 5. Build Notification as standard high-priority alert notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_task_notification)
            .setContentTitle("Add New Task")
            .setContentText("Tap 'Add Task' to add your task")
            .setContentIntent(openAppPendingIntent)
            .addAction(replyAction)
            .setOngoing(false) // One-time notification, dismissible
            .setAutoCancel(true)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    /**
     * Cancels the quick capture notification.
     */
    fun cancelNotification() {
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
