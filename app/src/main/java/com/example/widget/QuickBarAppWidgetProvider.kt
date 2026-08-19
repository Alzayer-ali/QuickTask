package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.QuickCaptureActivity
import com.example.R
import com.example.SendTaskNotificationActivity
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class QuickBarAppWidgetProvider : AppWidgetProvider() {

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
        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            widgetId: Int
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_quick_bar)

            // 1. Open App Intent
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val openAppPendingIntent = PendingIntent.getActivity(
                context,
                101,
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_bar_icon, openAppPendingIntent)
            views.setOnClickPendingIntent(R.id.widget_bar_info_container, openAppPendingIntent)

            // 2. Trigger Notification Intent
            val notifyIntent = Intent(context, SendTaskNotificationActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val notifyPendingIntent = PendingIntent.getActivity(
                context,
                102,
                notifyIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_bar_btn_notify, notifyPendingIntent)

            // 3. Quick Capture Intent
            val addIntent = Intent(context, QuickCaptureActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val addPendingIntent = PendingIntent.getActivity(
                context,
                103,
                addIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_bar_btn_add, addPendingIntent)

            // Synchronous update
            appWidgetManager.updateAppWidget(widgetId, views)

            // Update task count asynchronously
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val db = AppDatabase.getInstance(context)
                    val count = db.taskDao().getPendingTasksCountDirect()
                    views.setTextViewText(R.id.widget_bar_task_count, "$count pending tasks")
                    appWidgetManager.partiallyUpdateAppWidget(widgetId, views)
                } catch (_: Exception) {}
            }
        }
    }
}
