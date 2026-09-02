package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.Looper
import android.widget.RemoteViews
import android.widget.Toast
import com.example.MainActivity
import com.example.QuickCaptureActivity
import com.example.R
import com.example.data.AppDatabase
import com.example.notification.TaskReminderScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TasksListAppWidgetProvider : AppWidgetProvider() {

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)

        if (intent.action == ACTION_WIDGET_ITEM_CLICK) {
            val taskAction = intent.getStringExtra(EXTRA_TASK_ACTION)
            val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
            val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task"

            if (taskId != -1L) {
                if (taskAction == ACTION_COMPLETE_TASK) {
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val db = AppDatabase.getInstance(context)
                            val task = db.taskDao().getTaskByIdDirect(taskId)
                            if (task != null) {
                                val updated = task.copy(
                                    isCompleted = true,
                                    completedAt = System.currentTimeMillis()
                                )
                                db.taskDao().updateTask(updated)
                                TaskReminderScheduler(context).cancelReminder(taskId)
                                WidgetUpdateHelper.updateAllWidgets(context)

                                Handler(Looper.getMainLooper()).post {
                                    Toast.makeText(context, "Completed: $taskTitle ✓", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } catch (_: Exception) {}
                    }
                } else if (taskAction == ACTION_OPEN_TASK) {
                    val openAppIntent = Intent(context, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                        putExtra("EXTRA_HIGHLIGHT_TASK_ID", taskId)
                    }
                    context.startActivity(openAppIntent)
                }
            }
        }
    }

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (widgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, widgetId)
        }
    }

    companion object {
        const val ACTION_WIDGET_ITEM_CLICK = "com.example.widget.ACTION_WIDGET_ITEM_CLICK"
        const val EXTRA_TASK_ACTION = "com.example.widget.EXTRA_TASK_ACTION"
        const val ACTION_COMPLETE_TASK = "ACTION_COMPLETE_TASK"
        const val ACTION_OPEN_TASK = "ACTION_OPEN_TASK"
        const val EXTRA_TASK_ID = "EXTRA_TASK_ID"
        const val EXTRA_TASK_TITLE = "EXTRA_TASK_TITLE"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_tasks_list)

            // 1. Set Remote Adapter for ListView
            val serviceIntent = Intent(context, TasksWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                data = Uri.parse("widget://tasks/list/$widgetId")
            }
            views.setRemoteAdapter(R.id.widget_tasks_list_view, serviceIntent)

            // 2. Connect empty view
            views.setEmptyView(R.id.widget_tasks_list_view, R.id.widget_list_empty_view)

            // 3. PendingIntent Template for item interactions (Complete or Open)
            val itemClickIntent = Intent(context, TasksListAppWidgetProvider::class.java).apply {
                action = ACTION_WIDGET_ITEM_CLICK
            }
            val itemClickPendingIntent = PendingIntent.getBroadcast(
                context,
                201,
                itemClickIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.widget_tasks_list_view, itemClickPendingIntent)

            // 4. Header Add Button -> opens QuickCaptureActivity
            val addIntent = Intent(context, QuickCaptureActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val addPendingIntent = PendingIntent.getActivity(
                context,
                202,
                addIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_list_btn_add, addPendingIntent)

            // 5. Header Title tap -> open MainActivity
            val headerIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            val headerPendingIntent = PendingIntent.getActivity(
                context,
                203,
                headerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_list_title, headerPendingIntent)

            // 6. Update badge count asynchronously
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val count = db.taskDao().getPendingTasksCountDirect()
                    views.setTextViewText(R.id.widget_list_count_badge, "$count")
                    appWidgetManager.partiallyUpdateAppWidget(widgetId, views)
                } catch (_: Exception) {}
            }

            // Immediately send the complete RemoteViews to AppWidgetManager
            appWidgetManager.updateAppWidget(widgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.widget_tasks_list_view)
        }
    }
}
