package com.example.ui.theme

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemeMode(val label: String, val arabicLabel: String) {
    SYSTEM("System Default", "تلقائي (حسب النظام)"),
    LIGHT("Light Mode", "الوضع الفاتح"),
    DARK("Dark Mode", "الوضع الداكن")
}

enum class ThemePalette(val label: String, val arabicLabel: String, val previewColorHex: Long) {
    DYNAMIC("Material You", "ديناميكي (Material You)", 0xFF6750A4),
    AMOLED("AMOLED Pure Black", "أسود فاحم (AMOLED)", 0xFF000000),
    MIDNIGHT("Midnight Blue", "أزرق منتصف الليل", 0xFF1E293B),
    EMERALD("Emerald Green", "أخضر زمردي", 0xFF059669),
    SUNSET("Sunset Amber", "غروب دافئ", 0xFFD97706),
    LAVENDER("Pastel Lavender", "خزامى هادئ", 0xFF8B5CF6),
    OCEAN("Ocean Cyan", "أزرق محيطي", 0xFF0284C7)
}

data class ThemeSettings(
    val mode: ThemeMode = ThemeMode.SYSTEM,
    val palette: ThemePalette = ThemePalette.DYNAMIC
)

class ThemePreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("app_theme_preferences", Context.MODE_PRIVATE)

    private val _themeSettings = MutableStateFlow(loadSettings())
    val themeSettings: StateFlow<ThemeSettings> = _themeSettings.asStateFlow()

    private fun loadSettings(): ThemeSettings {
        val modeName = prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.name) ?: ThemeMode.SYSTEM.name
        val paletteName = prefs.getString(KEY_THEME_PALETTE, ThemePalette.DYNAMIC.name) ?: ThemePalette.DYNAMIC.name

        val mode = try { ThemeMode.valueOf(modeName) } catch (_: Exception) { ThemeMode.SYSTEM }
        val palette = try { ThemePalette.valueOf(paletteName) } catch (_: Exception) { ThemePalette.DYNAMIC }

        return ThemeSettings(mode = mode, palette = palette)
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.name).apply()
        _themeSettings.value = _themeSettings.value.copy(mode = mode)
    }

    fun setThemePalette(palette: ThemePalette) {
        prefs.edit().putString(KEY_THEME_PALETTE, palette.name).apply()
        _themeSettings.value = _themeSettings.value.copy(palette = palette)
    }

    companion object {
        private const val KEY_THEME_MODE = "key_theme_mode"
        private const val KEY_THEME_PALETTE = "key_theme_palette"

        @Volatile
        private var instance: ThemePreferences? = null

        fun getInstance(context: Context): ThemePreferences {
            return instance ?: synchronized(this) {
                instance ?: ThemePreferences(context.applicationContext).also { instance = it }
            }
        }
    }
}
