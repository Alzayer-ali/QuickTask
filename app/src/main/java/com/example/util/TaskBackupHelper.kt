package com.example.util

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.data.Priority
import com.example.data.RecurrenceType
import com.example.data.TaskEntity
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TaskBackupHelper {

    fun exportToJson(tasks: List<TaskEntity>): String {
        val rootObj = JSONObject()
        rootObj.put("version", 1)
        rootObj.put("appName", "DailyTasks")
        rootObj.put("exportedAt", System.currentTimeMillis())
        rootObj.put("totalTasks", tasks.size)

        val taskArray = JSONArray()
        for (task in tasks) {
            val item = JSONObject().apply {
                put("id", task.id)
                put("title", task.title)
                put("notes", task.notes)
                if (task.dueDateMillis != null) put("dueDateMillis", task.dueDateMillis)
                if (task.dueTimeHour != null) put("dueTimeHour", task.dueTimeHour)
                if (task.dueTimeMinute != null) put("dueTimeMinute", task.dueTimeMinute)
                if (task.endTimeHour != null) put("endTimeHour", task.endTimeHour)
                if (task.endTimeMinute != null) put("endTimeMinute", task.endTimeMinute)
                put("priority", task.priority.name)
                put("category", task.category)
                put("recurrence", task.recurrence.name)
                put("isCompleted", task.isCompleted)
                put("createdAt", task.createdAt)
                if (task.completedAt != null) put("completedAt", task.completedAt)
            }
            taskArray.put(item)
        }

        rootObj.put("tasks", taskArray)
        return rootObj.toString(2)
    }

    fun parseFromJson(jsonString: String): List<TaskEntity> {
        val tasks = mutableListOf<TaskEntity>()
        val trimmed = jsonString.trim()
        if (trimmed.isEmpty()) return tasks

        try {
            val taskArray = if (trimmed.startsWith("{")) {
                val root = JSONObject(trimmed)
                root.optJSONArray("tasks") ?: JSONArray()
            } else if (trimmed.startsWith("[")) {
                JSONArray(trimmed)
            } else {
                JSONArray()
            }

            for (i in 0 until taskArray.length()) {
                val obj = taskArray.getJSONObject(i)
                val title = obj.optString("title", "").trim()
                if (title.isEmpty()) continue

                val task = TaskEntity(
                    id = obj.optLong("id", 0L),
                    title = title,
                    notes = obj.optString("notes", ""),
                    dueDateMillis = if (obj.has("dueDateMillis")) obj.optLong("dueDateMillis") else null,
                    dueTimeHour = if (obj.has("dueTimeHour")) obj.optInt("dueTimeHour") else null,
                    dueTimeMinute = if (obj.has("dueTimeMinute")) obj.optInt("dueTimeMinute") else null,
                    endTimeHour = if (obj.has("endTimeHour")) obj.optInt("endTimeHour") else null,
                    endTimeMinute = if (obj.has("endTimeMinute")) obj.optInt("endTimeMinute") else null,
                    priority = Priority.fromString(obj.optString("priority", Priority.MEDIUM.name)),
                    category = obj.optString("category", "General"),
                    recurrence = RecurrenceType.fromString(obj.optString("recurrence", RecurrenceType.NONE.name)),
                    isCompleted = obj.optBoolean("isCompleted", false),
                    createdAt = obj.optLong("createdAt", System.currentTimeMillis()),
                    completedAt = if (obj.has("completedAt")) obj.optLong("completedAt") else null
                )
                tasks.add(task)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return tasks
    }

    fun shareBackup(context: Context, jsonString: String) {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "tasks_backup_$timeStamp.json"
            val cacheDir = File(context.cacheDir, "backups").apply { mkdirs() }
            val file = File(cacheDir, fileName)
            file.writeText(jsonString)

            val contentUri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "Tasks Backup ($timeStamp)")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Tasks Backup"))
        } catch (e: Exception) {
            // Fallback to text intent if file provider fails
            val textIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, jsonString)
                putExtra(Intent.EXTRA_SUBJECT, "Tasks Backup")
            }
            context.startActivity(Intent.createChooser(textIntent, "Share Tasks Backup"))
        }
    }

    fun writeToUri(context: Context, uri: Uri, content: String): Boolean {
        return try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.write(content)
                    writer.flush()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun readFromUri(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    reader.readText()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun copyToClipboard(context: Context, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Tasks Backup JSON", text)
        clipboard.setPrimaryClip(clip)
    }
}
