package com.example.widget

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.R
import com.example.data.AppDatabase
import com.example.data.Priority
import com.example.data.TaskEntity
import com.example.util.DateTimeUtils

class TasksWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TasksRemoteViewsFactory(this.applicationContext)
    }
}

class TasksRemoteViewsFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {

    private var tasksList: List<TaskEntity> = emptyList()

    override fun onCreate() {
        loadData()
    }

    override fun onDataSetChanged() {
        loadData()
    }

    private fun loadData() {
        try {
            val db = AppDatabase.getInstance(context)
            tasksList = db.taskDao().getPendingTasksDirect()
        } catch (e: Exception) {
            tasksList = emptyList()
        }
    }

    override fun onDestroy() {
        tasksList = emptyList()
    }

    override fun getCount(): Int = tasksList.size

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_task_item)
        if (position < 0 || position >= tasksList.size) return views

        val task = tasksList[position]

        // 1. Title
        views.setTextViewText(R.id.widget_item_title, task.title)

        // 2. Subtitle with Due info & Recurrence
        val dueStr = if (task.dueDateMillis != null) {
            DateTimeUtils.formatDueDate(
                task.dueDateMillis,
                task.dueTimeHour,
                task.dueTimeMinute,
                task.endTimeHour,
                task.endTimeMinute
            )
        } else {
            "No due date"
        }
        val recurrenceStr = if (task.recurrence != com.example.data.RecurrenceType.NONE) " • 🔁 ${task.recurrence.arabicLabel}" else ""
        views.setTextViewText(R.id.widget_item_subtitle, "📅 $dueStr$recurrenceStr")

        // 3. Priority indicator color
        val indicatorColor = when (task.priority) {
            Priority.HIGH -> Color.parseColor("#E11D48")   // High red
            Priority.MEDIUM -> Color.parseColor("#F59E0B") // Medium amber
            Priority.LOW -> Color.parseColor("#10B981")    // Low green
        }
        views.setInt(R.id.widget_item_priority_indicator, "setBackgroundColor", indicatorColor)

        // 4. Fill-in intent for interactive Check button -> complete task directly from widget
        val completeIntent = Intent().apply {
            putExtra(TasksListAppWidgetProvider.EXTRA_TASK_ACTION, TasksListAppWidgetProvider.ACTION_COMPLETE_TASK)
            putExtra(TasksListAppWidgetProvider.EXTRA_TASK_ID, task.id)
            putExtra(TasksListAppWidgetProvider.EXTRA_TASK_TITLE, task.title)
        }
        views.setOnClickFillInIntent(R.id.widget_item_check_btn, completeIntent)

        // 5. Fill-in intent for task content click -> open app and view task
        val openIntent = Intent().apply {
            putExtra(TasksListAppWidgetProvider.EXTRA_TASK_ACTION, TasksListAppWidgetProvider.ACTION_OPEN_TASK)
            putExtra(TasksListAppWidgetProvider.EXTRA_TASK_ID, task.id)
        }
        views.setOnClickFillInIntent(R.id.widget_item_content, openIntent)
        views.setOnClickFillInIntent(R.id.widget_task_item_root, openIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = tasksList.getOrNull(position)?.id ?: position.toLong()

    override fun hasStableIds(): Boolean = true
}
