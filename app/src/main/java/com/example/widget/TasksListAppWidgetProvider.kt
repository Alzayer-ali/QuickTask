package com.example.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.example.MainActivity
import com.example.QuickCaptureActivity
import com.example.R
import com.example.data.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TasksListAppWidgetProvider : AppWidgetProvider() {

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
            val views = RemoteViews(context.packageName, R.layout.widget_tasks_list)

            // 1. Set Remote Adapter for ListView
            val serviceIntent = Intent(context, TasksWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)
                data = Uri.parse("widget://tasks/list/$widgetId")
            }
            views.setRemoteAdapter(R.id.widget_tasks_list_view, serviceIntent)

            // 2. Connect empty view
            views.setEmptyView(R.id.widget_tasks_list_view, R.id.widget_list_empty_view)

            // 3. PendingIntent Template for item click -> open MainActivity
            val itemClickIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val itemClickPendingIntent = PendingIntent.getActivity(
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
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val headerPendingIntent = PendingIntent.getActivity(
                context,
                203,
                headerIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_list_title, headerPendingIntent)

            // 6. Update badge count asynchronously if needed
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
