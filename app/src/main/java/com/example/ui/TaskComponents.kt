package com.example.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Label
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.Priority
import com.example.data.TaskEntity
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import com.example.util.DateTimeUtils
import java.util.Calendar

@Composable
fun TaskItemCard(
    task: TaskEntity,
    onToggleComplete: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isOverdue = !task.isCompleted && DateTimeUtils.isOverdue(task.dueDateMillis, task.dueTimeHour, task.dueTimeMinute)
    val isDueToday = !task.isCompleted && DateTimeUtils.isDueToday(task.dueDateMillis)

    val cardBgColor by animateColorAsState(
        targetValue = if (task.isCompleted) {
            MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)
        } else {
            MaterialTheme.colorScheme.surface
        },
        animationSpec = tween(300),
        label = "cardBgColor"
    )

    val contentAlpha = if (task.isCompleted) 0.55f else 1f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("task_item_${task.id}"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardBgColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (task.isCompleted) 0.dp else 1.dp
        ),
        border = if (isOverdue) {
            CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(PriorityHigh.copy(alpha = 0.4f)))
        } else {
            CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(MaterialTheme.colorScheme.outlineVariant))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Clean Minimalist Checkbox (Rounded square with smooth border)
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        if (task.isCompleted) MaterialTheme.colorScheme.primary else Color.Transparent
                    )
                    .border(
                        width = 2.dp,
                        color = if (task.isCompleted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                        shape = RoundedCornerShape(7.dp)
                    )
                    .clickable { onToggleComplete() }
                    .testTag("task_checkbox_${task.id}"),
                contentAlignment = Alignment.Center
            ) {
                if (task.isCompleted) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Completed",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(14.dp))

            // Task Content
            Column(
                modifier = Modifier
                    .weight(1f)
                    .clickable { onEdit() }
            ) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (task.isCompleted) FontWeight.Normal else FontWeight.Medium,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    ),
                    color = if (task.isCompleted) {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                if (task.notes.isNotBlank()) {
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = task.notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Badges & Clean Minimalist Subtext
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Due Date Subtitle
                    if (task.dueDateMillis != null) {
                        val dueColor = when {
                            task.isCompleted -> MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            isOverdue -> PriorityHigh
                            isDueToday -> MaterialTheme.colorScheme.primary
                            else -> MaterialTheme.colorScheme.onSurfaceVariant
                        }

                        val formattedDue = DateTimeUtils.formatDueDate(task.dueDateMillis, task.dueTimeHour, task.dueTimeMinute)

                        Text(
                            text = formattedDue.uppercase(),
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = if (isDueToday || isOverdue) FontWeight.Bold else FontWeight.Medium,
                                letterSpacing = 1.1.sp
                            ),
                            color = dueColor
                        )
                    }

                    // Priority Badge
                    if (task.priority != Priority.MEDIUM || task.dueDateMillis == null) {
                        val pColor = when (task.priority) {
                            Priority.HIGH -> PriorityHigh
                            Priority.MEDIUM -> PriorityMedium
                            Priority.LOW -> PriorityLow
                        }

                        Surface(
                            shape = RoundedCornerShape(50),
                            color = pColor.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(5.dp)
                                        .clip(CircleShape)
                                        .background(pColor)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = task.priority.label.uppercase(),
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 0.8.sp
                                    ),
                                    color = pColor
                                )
                            }
                        }
                    }

                    // Category Badge
                    if (task.category.isNotBlank() && task.category != "General") {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = task.category,
                                modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp),
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Medium,
                                    letterSpacing = 0.5.sp
                                ),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Action buttons
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Direct Phone Calendar Button for dated tasks
                if (task.dueDateMillis != null) {
                    val context = androidx.compose.ui.platform.LocalContext.current
                    IconButton(
                        onClick = {
                            com.example.data.sync.DeviceCalendarSyncManager(context)
                                .launchAddToCalendarIntent(context, task)
                        },
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("task_calendar_btn_${task.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Add to Phone Calendar",
                            tint = if (task.isCalendarSynced) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onEdit,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("task_edit_btn_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit task",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier
                        .size(32.dp)
                        .testTag("task_delete_btn_${task.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Delete task",
                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.5f),
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun TaskEditDialog(
    task: TaskEntity?,
    onDismiss: () -> Unit,
    onSave: (TaskEntity) -> Unit
) {
    if (task == null) return

    var title by remember(task) { mutableStateOf(task.title) }
    var notes by remember(task) { mutableStateOf(task.notes) }
    var dueDateMillis by remember(task) { mutableStateOf(task.dueDateMillis) }
    var dueTimeHour by remember(task) { mutableStateOf(task.dueTimeHour) }
    var dueTimeMinute by remember(task) { mutableStateOf(task.dueTimeMinute) }
    var priority by remember(task) { mutableStateOf(task.priority) }
    var category by remember(task) { mutableStateOf(task.category) }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onSave(
                            task.copy(
                                title = title.trim(),
                                notes = notes.trim(),
                                dueDateMillis = dueDateMillis,
                                dueTimeHour = dueTimeHour,
                                dueTimeMinute = dueTimeMinute,
                                priority = priority,
                                category = category
                            )
                        )
                        onDismiss()
                    }
                },
                enabled = title.isNotBlank(),
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Text("Save", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                shape = RoundedCornerShape(50)
            ) {
                Text("Cancel")
            }
        },
        title = {
            Text(
                text = if (task.id == 0L) "New Task" else "Edit Task",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Medium)
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    )
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                    ),
                    minLines = 2
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Date & Time pickers
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { showDatePicker = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.CalendarToday, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (dueDateMillis != null) {
                                DateTimeUtils.formatDueDate(dueDateMillis, null, null)
                            } else "Set Date",
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }

                    OutlinedButton(
                        onClick = { showTimePicker = true },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.AccessTime, null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (dueTimeHour != null) {
                                val c = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, dueTimeHour ?: 0)
                                    set(Calendar.MINUTE, dueTimeMinute ?: 0)
                                }
                                java.text.SimpleDateFormat("h:mm a", java.util.Locale.getDefault()).format(c.time)
                            } else "Set Time",
                            fontSize = 12.sp,
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Priority
                Text("Priority", style = MaterialTheme.typography.labelMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Priority.entries.forEach { p ->
                        FilterChip(
                            selected = priority == p,
                            onClick = { priority = p },
                            shape = RoundedCornerShape(50),
                            label = { Text(p.label, fontSize = 11.sp) }
                        )
                    }
                }
            }
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = dueDateMillis ?: System.currentTimeMillis()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dueDateMillis = datePickerState.selectedDateMillis
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

    if (showTimePicker) {
        val initialCal = Calendar.getInstance()
        val timePickerState = rememberTimePickerState(
            initialHour = dueTimeHour ?: initialCal.get(Calendar.HOUR_OF_DAY),
            initialMinute = dueTimeMinute ?: initialCal.get(Calendar.MINUTE),
            is24Hour = false
        )

        DatePickerDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        dueTimeHour = timePickerState.hour
                        dueTimeMinute = timePickerState.minute
                        showTimePicker = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                TimePicker(state = timePickerState)
            }
        }
    }
}
