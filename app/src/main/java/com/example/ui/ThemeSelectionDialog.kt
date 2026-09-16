package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ui.theme.ThemeMode
import com.example.ui.theme.ThemePalette
import com.example.ui.theme.ThemePreferences

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.ui.window.DialogProperties
import com.example.ui.theme.WidgetThemeStyle

@Composable
fun ThemeSelectionDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val themePreferences = ThemePreferences.getInstance(context)
    val settings by themePreferences.themeSettings.collectAsState()

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = onDismiss
                )
                .systemBarsPadding()
                .imePadding()
                .padding(horizontal = 20.dp, vertical = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 440.dp)
                    .heightIn(max = 620.dp)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { /* Stop propagation */ }
                    )
                    .testTag("theme_selection_dialog")
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    // Dialog Header (Pinned)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.primaryContainer),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Theme & Appearance",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Customize theme, colors & widgets",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Scrollable Body
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Section 1: Mode Selector (System, Light, Dark)
                        Text(
                            text = "THEME MODE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeMode.entries.forEach { mode ->
                                val isSelected = settings.mode == mode
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { themePreferences.setThemeMode(mode) },
                                    label = {
                                        Text(
                                            text = when (mode) {
                                                ThemeMode.SYSTEM -> "System"
                                                ThemeMode.LIGHT -> "Light"
                                                ThemeMode.DARK -> "Dark"
                                            },
                                            style = MaterialTheme.typography.labelMedium.copy(
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                            )
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = when (mode) {
                                                ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                                ThemeMode.LIGHT -> Icons.Default.LightMode
                                                ThemeMode.DARK -> Icons.Default.DarkMode
                                            },
                                            contentDescription = null,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("theme_mode_${mode.name.lowercase()}"),
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                                        selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Section 2: Color Palettes
                        Text(
                            text = "COLOR PALETTE",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            ThemePalette.entries.forEach { palette ->
                                val isSelected = settings.palette == palette
                                val previewColor = Color(palette.previewColorHex)

                                Card(
                                    onClick = { themePreferences.setThemePalette(palette) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        }
                                    ),
                                    border = if (isSelected) {
                                        androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                    } else {
                                        androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("theme_palette_${palette.name.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Color swatch
                                        Box(
                                            modifier = Modifier
                                                .size(28.dp)
                                                .clip(CircleShape)
                                                .background(previewColor)
                                                .border(
                                                    width = 1.5.dp,
                                                    color = if (palette == ThemePalette.AMOLED) Color(0xFF444444) else Color.White.copy(alpha = 0.4f),
                                                    shape = CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (isSelected) {
                                                Icon(
                                                    imageVector = Icons.Default.Check,
                                                    contentDescription = null,
                                                    tint = Color.White,
                                                    modifier = Modifier.size(16.dp)
                                                )
                                            }
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = palette.label,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                        }

                                        if (isSelected) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = MaterialTheme.colorScheme.primary
                                            ) {
                                                Text(
                                                    text = "Active",
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    ),
                                                    color = MaterialTheme.colorScheme.onPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(22.dp))

                        // Section 3: Widget Style (مظهر الويدجت)
                        Text(
                            text = "WIDGET STYLE / مظهر الويدجت",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            WidgetThemeStyle.entries.forEach { style ->
                                val isSelected = settings.widgetStyle == style

                                Card(
                                    onClick = { themePreferences.setWidgetThemeStyle(style) },
                                    shape = RoundedCornerShape(16.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) {
                                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                        } else {
                                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                                        }
                                    ),
                                    border = if (isSelected) {
                                        androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
                                    } else {
                                        androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f))
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("theme_widget_style_${style.name.lowercase()}")
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 14.dp, vertical = 12.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        // Style icon swatch
                                        Box(
                                            modifier = Modifier
                                                .size(38.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(
                                                    if (style == WidgetThemeStyle.LIQUID_GLASS) {
                                                        Color.White.copy(alpha = 0.22f)
                                                    } else {
                                                        MaterialTheme.colorScheme.primary
                                                    }
                                                )
                                                .border(
                                                    width = 1.dp,
                                                    color = if (style == WidgetThemeStyle.LIQUID_GLASS) Color.White.copy(alpha = 0.65f) else Color.Transparent,
                                                    shape = RoundedCornerShape(12.dp)
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = if (style == WidgetThemeStyle.LIQUID_GLASS) {
                                                    Icons.Default.WaterDrop
                                                } else {
                                                    Icons.Default.Widgets
                                                },
                                                contentDescription = null,
                                                tint = if (style == WidgetThemeStyle.LIQUID_GLASS) Color(0xFF67E8F9) else MaterialTheme.colorScheme.onPrimary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(14.dp))

                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = style.label,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                                ),
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Text(
                                                text = if (style == WidgetThemeStyle.LIQUID_GLASS) {
                                                    "زجاجي شفاف مع تأثير الضبابية والحواف الانسيابية"
                                                } else {
                                                    "مطابقة مظهر ونمط التطبيق المختار"
                                                },
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }

                                        if (isSelected) {
                                            Surface(
                                                shape = RoundedCornerShape(50),
                                                color = MaterialTheme.colorScheme.primary
                                            ) {
                                                Text(
                                                    text = "Active",
                                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                                    style = MaterialTheme.typography.labelSmall.copy(
                                                        fontWeight = FontWeight.Bold,
                                                        fontSize = 10.sp
                                                    ),
                                                    color = MaterialTheme.colorScheme.onPrimary
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Done Button (Pinned at Bottom)
                    Button(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("theme_done_btn")
                    ) {
                        Text(
                            text = "Done",
                            style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }
    }
}
