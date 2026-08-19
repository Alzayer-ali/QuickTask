package com.example.widget

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import com.example.R

object WidgetUpdateHelper {

    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)

        // 1. Update Quick Bar Widget
        val barComponent = ComponentName(context, QuickBarAppWidgetProvider::class.java)
        val barIds = appWidgetManager.getAppWidgetIds(barComponent)
        if (barIds.isNotEmpty()) {
            val intent = Intent(context, QuickBarAppWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, barIds)
            }
            context.sendBroadcast(intent)
        }

        // 2. Update Tasks List Widget & trigger RemoteViewsService refresh
        val listComponent = ComponentName(context, TasksListAppWidgetProvider::class.java)
        val listIds = appWidgetManager.getAppWidgetIds(listComponent)
        if (listIds.isNotEmpty()) {
            val intent = Intent(context, TasksListAppWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, listIds)
            }
            context.sendBroadcast(intent)
            appWidgetManager.notifyAppWidgetViewDataChanged(listIds, R.id.widget_tasks_list_view)
        }

        // 3. Update Quick Pill Widget
        val pillComponent = ComponentName(context, QuickCapturePillWidgetProvider::class.java)
        val pillIds = appWidgetManager.getAppWidgetIds(pillComponent)
        if (pillIds.isNotEmpty()) {
            val intent = Intent(context, QuickCapturePillWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, pillIds)
            }
            context.sendBroadcast(intent)
        }
    }
}
