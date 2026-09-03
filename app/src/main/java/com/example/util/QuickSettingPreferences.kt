package com.example.util

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class QuickSettingClickAction(
    val title: String,
    val titleAr: String,
    val description: String,
    val descriptionAr: String
) {
    NOTIFICATION(
        title = "Show Notification",
        titleAr = "إظهار إشعار إضافة مهمة",
        description = "Closes quick settings and posts an inline-reply notification in status bar",
        descriptionAr = "إغلاق شريط الإعدادات السريعة وإظهار إشعار مع إمكانية الكتابة والرد السريع"
    ),
    DIALOG(
        title = "Open Add Task Dialog",
        titleAr = "فتح نافذة إضافة مهمة",
        description = "Closes quick settings and opens the new task dialog directly",
        descriptionAr = "إغلاق شريط الإعدادات السريعة وفتح نافذة إضافة مهمة جديدة مباشرة"
    )
}

class QuickSettingPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _actionFlow = MutableStateFlow(loadAction())
    val actionFlow: StateFlow<QuickSettingClickAction> = _actionFlow.asStateFlow()

    private fun loadAction(): QuickSettingClickAction {
        val name = prefs.getString(KEY_ACTION, QuickSettingClickAction.NOTIFICATION.name)
            ?: QuickSettingClickAction.NOTIFICATION.name
        return try {
            QuickSettingClickAction.valueOf(name)
        } catch (_: Exception) {
            QuickSettingClickAction.NOTIFICATION
        }
    }

    fun getAction(): QuickSettingClickAction = _actionFlow.value

    fun setAction(action: QuickSettingClickAction) {
        prefs.edit().putString(KEY_ACTION, action.name).apply()
        _actionFlow.value = action
    }

    companion object {
        private const val PREFS_NAME = "quick_setting_preferences"
        private const val KEY_ACTION = "key_qs_tile_action"

        @Volatile
        private var instance: QuickSettingPreferences? = null

        fun getInstance(context: Context): QuickSettingPreferences {
            return instance ?: synchronized(this) {
                instance ?: QuickSettingPreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
