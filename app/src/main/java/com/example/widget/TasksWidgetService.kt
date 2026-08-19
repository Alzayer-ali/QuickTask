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

        // 2. Subtitle with Due info
        val dueStr = if (task.dueDateMillis != null) {
            DateTimeUtils.formatDueDate(task.dueDateMillis, task.dueTimeHour, task.dueTimeMinute)
        } else {
            "No due date"
        }
        views.setTextViewText(R.id.widget_item_subtitle, "📅 $dueStr")

        // 3. Priority indicator color
        val indicatorColor = when (task.priority) {
            Priority.HIGH -> Color.parseColor("#E11D48")   // High red
            Priority.MEDIUM -> Color.parseColor("#F59E0B") // Medium amber
            Priority.LOW -> Color.parseColor("#10B981")    // Low green
        }
        views.setInt(R.id.widget_item_priority_indicator, "setBackgroundColor", indicatorColor)

        // 4. Fill-in intent for item click -> open app and highlight task
        val fillInIntent = Intent().apply {
            putExtra("EXTRA_HIGHLIGHT_TASK_ID", task.id)
        }
        views.setOnClickFillInIntent(R.id.widget_task_item_root, fillInIntent)

        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long = tasksList.getOrNull(position)?.id ?: position.toLong()

    override fun hasStableIds(): Boolean = true
}
