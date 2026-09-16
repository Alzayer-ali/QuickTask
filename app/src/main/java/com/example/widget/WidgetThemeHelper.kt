package com.example.widget

import android.content.Context
import android.content.res.Configuration
import android.graphics.Color
import android.widget.RemoteViews
import com.example.R
import com.example.data.TaskEntity
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemePalette
import com.example.ui.theme.ThemePreferences
import com.example.ui.theme.WidgetThemeStyle

object WidgetThemeHelper {

    data class ResolvedWidgetTheme(
        val bgDrawableRes: Int,
        val itemBgDrawableRes: Int,
        val primaryBtnDrawableRes: Int,
        val secondaryBtnDrawableRes: Int,
        val titleTextColor: Int,
        val subtitleTextColor: Int,
        val primaryBtnTextColor: Int,
        val secondaryBtnTextColor: Int,
        val isLiquidGlass: Boolean
    )

    fun resolveTheme(context: Context): ResolvedWidgetTheme {
        val prefs = ThemePreferences.getInstance(context)
        val settings = prefs.themeSettings.value

        if (settings.widgetStyle == WidgetThemeStyle.LIQUID_GLASS) {
            return ResolvedWidgetTheme(
                bgDrawableRes = R.drawable.widget_bg_liquid_glass,
                itemBgDrawableRes = R.drawable.widget_task_item_bg_glass,
                primaryBtnDrawableRes = R.drawable.widget_button_glass_pill,
                secondaryBtnDrawableRes = R.drawable.widget_button_glass_secondary,
                titleTextColor = Color.WHITE,
                subtitleTextColor = Color.parseColor("#E2E8F0"),
                primaryBtnTextColor = Color.WHITE,
                secondaryBtnTextColor = Color.WHITE,
                isLiquidGlass = true
            )
        }

        // Follow App Theme
        val isSystemDark = (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        val isDark = when (settings.mode) {
            ThemeMode.SYSTEM -> isSystemDark
            ThemeMode.DARK -> true
            ThemeMode.LIGHT -> false
        }

        if (!isDark) {
            // Light mode per palette
            val (btnDrawable, btnText) = when (settings.palette) {
                ThemePalette.AMOLED -> Pair(R.drawable.widget_btn_amoled, Color.BLACK)
                ThemePalette.MIDNIGHT -> Pair(R.drawable.widget_btn_midnight, Color.parseColor("#0B0F19"))
                ThemePalette.EMERALD -> Pair(R.drawable.widget_btn_emerald, Color.parseColor("#062016"))
                ThemePalette.SUNSET -> Pair(R.drawable.widget_btn_sunset, Color.parseColor("#1C1308"))
                ThemePalette.LAVENDER -> Pair(R.drawable.widget_btn_lavender, Color.parseColor("#171324"))
                ThemePalette.OCEAN -> Pair(R.drawable.widget_btn_ocean, Color.parseColor("#081826"))
                ThemePalette.DYNAMIC -> Pair(R.drawable.widget_button_pill, Color.parseColor("#381E72"))
            }
            return ResolvedWidgetTheme(
                bgDrawableRes = R.drawable.widget_bg_light,
                itemBgDrawableRes = R.drawable.widget_item_bg_light,
                primaryBtnDrawableRes = btnDrawable,
                secondaryBtnDrawableRes = R.drawable.widget_btn_sec_light,
                titleTextColor = Color.parseColor("#0F172A"),
                subtitleTextColor = Color.parseColor("#64748B"),
                primaryBtnTextColor = btnText,
                secondaryBtnTextColor = Color.parseColor("#334155"),
                isLiquidGlass = false
            )
        }

        // Dark mode per palette
        return when (settings.palette) {
            ThemePalette.AMOLED -> ResolvedWidgetTheme(
                bgDrawableRes = R.drawable.widget_bg_amoled,
                itemBgDrawableRes = R.drawable.widget_item_bg_amoled,
                primaryBtnDrawableRes = R.drawable.widget_btn_amoled,
                secondaryBtnDrawableRes = R.drawable.widget_btn_sec_amoled,
                titleTextColor = Color.WHITE,
                subtitleTextColor = Color.parseColor("#A1A1AA"),
                primaryBtnTextColor = Color.BLACK,
                secondaryBtnTextColor = Color.parseColor("#E4E4E7"),
                isLiquidGlass = false
            )
            ThemePalette.MIDNIGHT -> ResolvedWidgetTheme(
                bgDrawableRes = R.drawable.widget_bg_midnight,
                itemBgDrawableRes = R.drawable.widget_item_bg_midnight,
                primaryBtnDrawableRes = R.drawable.widget_btn_midnight,
                secondaryBtnDrawableRes = R.drawable.widget_btn_sec_midnight,
                titleTextColor = Color.parseColor("#F8FAFC"),
                subtitleTextColor = Color.parseColor("#94A3B8"),
                primaryBtnTextColor = Color.parseColor("#0B0F19"),
                secondaryBtnTextColor = Color.parseColor("#E2E8F0"),
                isLiquidGlass = false
            )
            ThemePalette.EMERALD -> ResolvedWidgetTheme(
                bgDrawableRes = R.drawable.widget_bg_emerald,
                itemBgDrawableRes = R.drawable.widget_item_bg_emerald,
                primaryBtnDrawableRes = R.drawable.widget_btn_emerald,
                secondaryBtnDrawableRes = R.drawable.widget_btn_sec_emerald,
                titleTextColor = Color.parseColor("#F0FDF4"),
                subtitleTextColor = Color.parseColor("#A7F3D0"),
                primaryBtnTextColor = Color.parseColor("#062016"),
                secondaryBtnTextColor = Color.parseColor("#E6F4EA"),
                isLiquidGlass = false
            )
            ThemePalette.SUNSET -> ResolvedWidgetTheme(
                bgDrawableRes = R.drawable.widget_bg_sunset,
                itemBgDrawableRes = R.drawable.widget_item_bg_sunset,
                primaryBtnDrawableRes = R.drawable.widget_btn_sunset,
                secondaryBtnDrawableRes = R.drawable.widget_btn_sec_sunset,
                titleTextColor = Color.parseColor("#FFFBEB"),
                subtitleTextColor = Color.parseColor("#FDE68A"),
                primaryBtnTextColor = Color.parseColor("#1C1308"),
                secondaryBtnTextColor = Color.parseColor("#FEF3C7"),
                isLiquidGlass = false
            )
            ThemePalette.LAVENDER -> ResolvedWidgetTheme(
                bgDrawableRes = R.drawable.widget_bg_lavender,
                itemBgDrawableRes = R.drawable.widget_item_bg_lavender,
                primaryBtnDrawableRes = R.drawable.widget_btn_lavender,
                secondaryBtnDrawableRes = R.drawable.widget_btn_sec_lavender,
                titleTextColor = Color.parseColor("#FAF5FF"),
                subtitleTextColor = Color.parseColor("#DDD6FE"),
                primaryBtnTextColor = Color.parseColor("#171324"),
                secondaryBtnTextColor = Color.parseColor("#EDE9FE"),
                isLiquidGlass = false
            )
            ThemePalette.OCEAN -> ResolvedWidgetTheme(
                bgDrawableRes = R.drawable.widget_bg_ocean,
                itemBgDrawableRes = R.drawable.widget_item_bg_ocean,
                primaryBtnDrawableRes = R.drawable.widget_btn_ocean,
                secondaryBtnDrawableRes = R.drawable.widget_btn_sec_ocean,
                titleTextColor = Color.parseColor("#F0F9FF"),
                subtitleTextColor = Color.parseColor("#BAE6FD"),
                primaryBtnTextColor = Color.parseColor("#081826"),
                secondaryBtnTextColor = Color.parseColor("#E0F2FE"),
                isLiquidGlass = false
            )
            ThemePalette.DYNAMIC -> ResolvedWidgetTheme(
                bgDrawableRes = R.drawable.widget_background_card,
                itemBgDrawableRes = R.drawable.widget_task_item_bg,
                primaryBtnDrawableRes = R.drawable.widget_button_pill,
                secondaryBtnDrawableRes = R.drawable.widget_button_secondary_pill,
                titleTextColor = Color.parseColor("#E6E0E9"),
                subtitleTextColor = Color.parseColor("#CAC4D0"),
                primaryBtnTextColor = Color.parseColor("#381E72"),
                secondaryBtnTextColor = Color.parseColor("#E6E0E9"),
                isLiquidGlass = false
            )
        }
    }

    fun applyQuickBarStyle(context: Context, views: RemoteViews) {
        val theme = resolveTheme(context)
        views.setInt(R.id.widget_bar_root, "setBackgroundResource", theme.bgDrawableRes)
        views.setTextColor(R.id.widget_bar_app_title, theme.titleTextColor)
        views.setTextColor(R.id.widget_bar_task_count, theme.subtitleTextColor)

        views.setInt(R.id.widget_bar_btn_notify, "setBackgroundResource", theme.secondaryBtnDrawableRes)
        views.setTextColor(R.id.widget_bar_btn_notify, theme.secondaryBtnTextColor)

        views.setInt(R.id.widget_bar_btn_add, "setBackgroundResource", theme.primaryBtnDrawableRes)
        views.setTextColor(R.id.widget_bar_btn_add, theme.primaryBtnTextColor)
    }

    fun applyQuickPillStyle(context: Context, views: RemoteViews) {
        val theme = resolveTheme(context)
        views.setInt(R.id.widget_pill_root, "setBackgroundResource", theme.bgDrawableRes)
        views.setTextColor(R.id.widget_pill_title, theme.titleTextColor)

        views.setInt(R.id.widget_pill_circle, "setBackgroundResource", theme.primaryBtnDrawableRes)
        views.setTextColor(R.id.widget_pill_circle, theme.primaryBtnTextColor)

        views.setInt(R.id.widget_pill_count_badge, "setBackgroundResource", theme.secondaryBtnDrawableRes)
        views.setTextColor(R.id.widget_pill_count_badge, theme.secondaryBtnTextColor)
    }

    fun applyTasksListStyle(context: Context, views: RemoteViews) {
        val theme = resolveTheme(context)
        views.setInt(R.id.widget_list_root, "setBackgroundResource", theme.bgDrawableRes)
        views.setTextColor(R.id.widget_list_title, theme.titleTextColor)

        views.setInt(R.id.widget_list_count_badge, "setBackgroundResource", theme.secondaryBtnDrawableRes)
        views.setTextColor(R.id.widget_list_count_badge, theme.secondaryBtnTextColor)

        views.setInt(R.id.widget_list_btn_add, "setBackgroundResource", theme.primaryBtnDrawableRes)
        views.setTextColor(R.id.widget_list_btn_add, theme.primaryBtnTextColor)

        views.setTextColor(R.id.widget_list_empty_view, theme.subtitleTextColor)
    }

    fun applyTaskItemStyle(context: Context, views: RemoteViews, task: TaskEntity) {
        val theme = resolveTheme(context)
        views.setInt(R.id.widget_task_item_root, "setBackgroundResource", theme.itemBgDrawableRes)
        views.setTextColor(R.id.widget_item_title, theme.titleTextColor)
        views.setTextColor(R.id.widget_item_subtitle, theme.subtitleTextColor)
        if (theme.isLiquidGlass) {
            views.setInt(R.id.widget_item_check_btn, "setColorFilter", Color.parseColor("#E2E8F0"))
        } else {
            views.setInt(R.id.widget_item_check_btn, "setColorFilter", Color.parseColor("#938F99"))
        }
    }
}
