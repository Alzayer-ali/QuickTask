package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.data.Priority
import com.example.data.RecurrenceType
import com.example.data.TaskEntity
import com.example.util.DateTimeUtils
import com.example.util.SmartTimeParser
import java.util.Calendar

/**
 * Simplified, ultra-fast Quick Task Dialog for external invocations (Quick Settings tile & widgets).
 * 
 * Features:
 * - No notes input (streamlined for instant capture)
 * - Start and End time buttons with automatic +30min end time calculation
 * - Single recurrence dropdown button aligned alongside Start and End
 * - No priority selector (defaults to Medium)
 * - Compact, minimal, and fast UI
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuickTaskDialog(
    onDismiss: () -> Unit,
    onSave: (TaskEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    var title by remember { mutableStateOf("") }
    var dueDateMillis by remember { mutableStateOf<Long?>(DateTimeUtils.getTodayStartMillis()) }
    var dueTimeHour by remember { mutableStateOf<Int?>(null) }
    var dueTimeMinute by remember { mutableStateOf<Int?>(null) }
    var endTimeHour by remember { mutableStateOf<Int?>(null) }
    var endTimeMinute by remember { mutableStateOf<Int?>(null) }
    var recurrence by remember { mutableStateOf(RecurrenceType.NONE) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }
    var showRepeatMenu by remember { mutableStateOf(false) }

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        try {
            kotlinx.coroutines.delay(120)
            focusRequester.requestFocus()
        } catch (_: Exception) {
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismiss
            )
            .systemBarsPadding()
            .imePadding()
            .padding(horizontal = 16.dp, vertical = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 410.dp)
                .fillMaxWidth()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { /* Stop click propagation */ }
                )
                .testTag("quick_task_dialog_card"),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
            border = CardDefaults.outlinedCardBorder().copy(
                brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f))
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Compact Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(17.dp)
                                )
                            }
                        }
                        Text(
                            text = "Quick Task",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = (-0.2).sp
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Title Input with Smart Time Parsing
                OutlinedTextField(
                    value = title,
                    onValueChange = { newTitle ->
                        title = newTitle
                        // Smart Natural Language Time & Date Detection
                        val parseResult = SmartTimeParser.parse(newTitle)
                        if (parseResult.dueDateMillis != null) {
                            dueDateMillis = parseResult.dueDateMillis
                        }
                        if (parseResult.startHour != null) {
                            dueTimeHour = parseResult.startHour
                            dueTimeMinute = parseResult.startMinute ?: 0
                            if (parseResult.endHour != null) {
                                endTimeHour = parseResult.endHour
                                endTimeMinute = parseResult.endMinute ?: 0
                            } else {
                                endTimeHour = null
                                endTimeMinute = null
                            }
                        }
                        if (parseResult.recurrence != RecurrenceType.NONE) {
                            recurrence = parseResult.recurrence
                        }
                    },
                    placeholder = {
                        Text(
                            text = stringResource(R.string.task_title_hint),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester)
                        .testTag("quick_task_title_input"),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    maxLines = 2,
                    singleLine = false
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date Selector Chips Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val isToday = DateTimeUtils.isDueToday(dueDateMillis)
                    val isTomorrow = dueDateMillis == DateTimeUtils.getTomorrowStartMillis()

                    FilterChip(
                        selected = isToday,
                        onClick = { dueDateMillis = DateTimeUtils.getTodayStartMillis() },
                        label = { Text("Today", fontSize = 12.sp) },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )

                    FilterChip(
                        selected = isTomorrow,
                        onClick = { dueDateMillis = DateTimeUtils.getTomorrowStartMillis() },
                        label = { Text("Tomorrow", fontSize = 12.sp) },
                        shape = RoundedCornerShape(50),
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                        )
                    )

                    Surface(
                        onClick = { showDatePicker = true },
                        shape = RoundedCornerShape(50),
                        color = if (!isToday && !isTomorrow && dueDateMillis != null) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (!isToday && !isTomorrow && dueDateMillis != null) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            }
                        ),
                        modifier = Modifier.weight(1f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarMonth,
                                contentDescription = null,
                                tint = if (!isToday && !isTomorrow && dueDateMillis != null) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (!isToday && !isTomorrow && dueDateMillis != null) {
                                    DateTimeUtils.formatDueDate(dueDateMillis, null, null)
                                } else "Pick Date",
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontWeight = if (!isToday && !isTomorrow && dueDateMillis != null) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (!isToday && !isTomorrow && dueDateMillis != null) {
                                    MaterialTheme.colorScheme.onPrimaryContainer
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant
                                },
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Unified 3-Button Row: [ Start ]  [ End ]  [ Repeat ▾ ]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 1. Start Button
                    Surface(
                        onClick = { showStartTimePicker = true },
                        shape = RoundedCornerShape(14.dp),
                        color = if (dueTimeHour != null) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (dueTimeHour != null) 1.5.dp else 1.dp,
                            color = if (dueTimeHour != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_task_start_btn")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "START",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = if (dueTimeHour != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (dueTimeHour != null) {
                                    DateTimeUtils.format24Hour(dueTimeHour!!, dueTimeMinute ?: 0)
                                } else "--:--",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                ),
                                color = if (dueTimeHour != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // 2. End Button
                    Surface(
                        onClick = { showEndTimePicker = true },
                        shape = RoundedCornerShape(14.dp),
                        color = if (endTimeHour != null) {
                            MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                        } else {
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        },
                        border = androidx.compose.foundation.BorderStroke(
                            width = if (endTimeHour != null) 1.5.dp else 1.dp,
                            color = if (endTimeHour != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("quick_task_end_btn")
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 9.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "END",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.8.sp
                                ),
                                color = if (endTimeHour != null) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (endTimeHour != null) {
                                    DateTimeUtils.format24Hour(endTimeHour!!, endTimeMinute ?: 0)
                                } else "--:--",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 13.sp
                                ),
                                color = if (endTimeHour != null) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                    }

                    // 3. Repeat Dropdown Button (Aligned in same row)
                    Box(modifier = Modifier.weight(1f)) {
                        Surface(
                            onClick = { showRepeatMenu = true },
                            shape = RoundedCornerShape(14.dp),
                            color = if (recurrence != RecurrenceType.NONE) {
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.45f)
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            },
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (recurrence != RecurrenceType.NONE) 1.5.dp else 1.dp,
                                color = if (recurrence != RecurrenceType.NONE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                            ),
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("quick_task_repeat_btn")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 9.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "REPEAT",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = if (recurrence != RecurrenceType.NONE) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = if (recurrence != RecurrenceType.NONE) recurrence.label else "None",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.SemiBold,
                                            fontSize = 12.sp
                                        ),
                                        color = if (recurrence != RecurrenceType.NONE) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        // Recurrence Dropdown Menu
                        DropdownMenu(
                            expanded = showRepeatMenu,
                            onDismissRequest = { showRepeatMenu = false }
                        ) {
                            RecurrenceType.entries.forEach { rec ->
                                val isSelected = recurrence == rec
                                DropdownMenuItem(
                                    text = {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            if (rec != RecurrenceType.NONE) {
                                                Icon(
                                                    imageVector = Icons.Default.Repeat,
                                                    contentDescription = null,
                                                    modifier = Modifier.size(16.dp),
                                                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                            Text(
                                                text = rec.label,
                                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                                            )
                                        }
                                    },
                                    trailingIcon = if (isSelected) {
                                        {
                                            Icon(
                                                imageVector = Icons.Default.Check,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                        }
                                    } else null,
                                    onClick = {
                                        recurrence = rec
                                        if (rec != RecurrenceType.NONE && dueDateMillis == null) {
                                            dueDateMillis = DateTimeUtils.getTodayStartMillis()
                                        }
                                        showRepeatMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                // Save Action Button
                Button(
                    onClick = {
                        val cleanTitle = title.trim()
                        if (cleanTitle.isNotEmpty()) {
                            val newTask = TaskEntity(
                                title = cleanTitle,
                                notes = "", // No notes in quick capture
                                dueDateMillis = dueDateMillis,
                                dueTimeHour = dueTimeHour,
                                dueTimeMinute = dueTimeMinute,
                                endTimeHour = endTimeHour,
                                endTimeMinute = endTimeMinute,
                                priority = Priority.MEDIUM, // Default to Medium
                                recurrence = recurrence,
                                category = "General"
                            )
                            onSave(newTask)
                        }
                    },
                    enabled = title.isNotBlank(),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("quick_task_save_btn")
                ) {
                    Text(
                        text = stringResource(R.string.save_task),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    )
                }
            }
        }
    }

    // Material 3 Date Picker Dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let {
                            dueDateMillis = it
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Start Time Picker Dialog
    if (showStartTimePicker) {
        val initialCal = Calendar.getInstance()
        val currentH = dueTimeHour ?: initialCal.get(Calendar.HOUR_OF_DAY)
        val currentM = dueTimeMinute ?: 0
        val startTimePickerState = rememberTimePickerState(
            initialHour = currentH,
            initialMinute = currentM,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showStartTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        val h = startTimePickerState.hour
                        val m = startTimePickerState.minute
                        dueTimeHour = h
                        dueTimeMinute = m

                        showStartTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartTimePicker = false }) {
                    Text("Cancel")
                }
            },
            title = {
                Text(
                    text = "Select Start Time",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = startTimePickerState)
                }
            }
        )
    }

    // End Time Picker Dialog
    if (showEndTimePicker) {
        val initialCal = Calendar.getInstance()
        val defaultEndH = endTimeHour ?: (((dueTimeHour ?: initialCal.get(Calendar.HOUR_OF_DAY)) + 1) % 24)
        val defaultEndM = endTimeMinute ?: (dueTimeMinute ?: 0)
        val endTimePickerState = rememberTimePickerState(
            initialHour = defaultEndH,
            initialMinute = defaultEndM,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { showEndTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        endTimeHour = endTimePickerState.hour
                        endTimeMinute = endTimePickerState.minute
                        showEndTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndTimePicker = false }) {
                    Text("Cancel")
                }
            },
            title = {
                Text(
                    text = "Select End Time",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = endTimePickerState)
                }
            }
        )
    }
}
