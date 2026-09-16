package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light Mode"),
    DARK("Dark Mode")
}

enum class ThemePalette(val label: String, val previewColorHex: Long) {
    DYNAMIC("Material You", 0xFF6750A4),
    AMOLED("AMOLED Pure Black", 0xFF000000),
    MIDNIGHT("Midnight Blue", 0xFF1E293B),
    EMERALD("Emerald Green", 0xFF059669),
    SUNSET("Sunset Amber", 0xFFD97706),
    LAVENDER("Pastel Lavender", 0xFF8B5CF6),
    OCEAN("Ocean Cyan", 0xFF0284C7)
}

enum class WidgetThemeStyle(val label: String, val subtitle: String) {
    FOLLOW_APP("نفس مظهر التطبيق", "Match App Theme"),
    LIQUID_GLASS("مظهر زجاجي شفاف (Liquid glass)", "Liquid Glass")
}

data class ThemeSettings(
    val mode: ThemeMode = ThemeMode.SYSTEM,
    val palette: ThemePalette = ThemePalette.DYNAMIC,
    val widgetStyle: WidgetThemeStyle = WidgetThemeStyle.FOLLOW_APP
)

class ThemePreferences(private val context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_theme_preferences", Context.MODE_PRIVATE)

    private val _themeSettings = MutableStateFlow(loadSettings())
    val themeSettings: StateFlow<ThemeSettings> = _themeSettings.asStateFlow()

    private fun loadSettings(): ThemeSettings {
        val modeName = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val paletteName = prefs.getString(KEY_THEME_PALETTE, ThemePalette.DYNAMIC.name) ?: ThemePalette.DYNAMIC.name
        val widgetStyleName = prefs.getString(KEY_WIDGET_STYLE, WidgetThemeStyle.FOLLOW_APP.name) ?: WidgetThemeStyle.FOLLOW_APP.name

        val mode = try { ThemeMode.valueOf(modeName) } catch (_: Exception) { ThemeMode.SYSTEM }
        val palette = try { ThemePalette.valueOf(paletteName) } catch (_: Exception) { ThemePalette.DYNAMIC }
        val widgetStyle = try { WidgetThemeStyle.valueOf(widgetStyleName) } catch (_: Exception) { WidgetThemeStyle.FOLLOW_APP }

        return ThemeSettings(mode = mode, palette = palette, widgetStyle = widgetStyle)
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeSettings.value = _themeSettings.value.copy(mode = mode)
        com.example.widget.WidgetUpdateHelper.updateAllWidgets(context)
    }

    fun setThemePalette(palette: ThemePalette) {
        prefs.edit().putString(KEY_THEME_PALETTE, palette.name).apply()
        _themeSettings.value = _themeSettings.value.copy(palette = palette)
        com.example.widget.WidgetUpdateHelper.updateAllWidgets(context)
    }

    fun setWidgetThemeStyle(style: WidgetThemeStyle) {
        prefs.edit().putString(KEY_WIDGET_STYLE, style.name).apply()
        _themeSettings.value = _themeSettings.value.copy(widgetStyle = style)
        com.example.widget.WidgetUpdateHelper.updateAllWidgets(context)
    }

    companion object {
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_THEME_PALETTE = "key_theme_palette"
        private const val KEY_WIDGET_STYLE = "key_widget_style"

        @Volatile
        private var instance: ThemePreferences? = null

        fun getInstance(context: Context): ThemePreferences {
            return instance ?: synchronized(this) {
                instance ?: ThemePreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
