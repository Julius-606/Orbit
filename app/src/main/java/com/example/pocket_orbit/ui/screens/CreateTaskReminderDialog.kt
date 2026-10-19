package com.example.pocket_orbit.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import java.text.SimpleDateFormat
import java.util.*

import com.example.pocket_orbit.data.StudyTaskEntity

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateTaskReminderDialog(
    initialDate: Date? = null,
    isReminderDefault: Boolean = false,
    taskToEdit: StudyTaskEntity? = null,
    onDismiss: () -> Unit,
    onSave: (title: String, subject: String, brainRotLevel: String, dueDate: Date?, isReminder: Boolean, remarks: String?) -> Unit
) {
    val context = LocalContext.current

    var title by remember { mutableStateOf(taskToEdit?.title ?: "") }
    var titleError by remember { mutableStateOf(false) }
    var isReminder by remember { mutableStateOf(taskToEdit?.isReminder ?: isReminderDefault) }
    var selectedSubject by remember { mutableStateOf(taskToEdit?.subject ?: "General Med") }
    var customSubject by remember { mutableStateOf("") }
    var showCustomSubjectInput by remember { mutableStateOf(false) }
    var selectedBrainRot by remember { mutableStateOf(taskToEdit?.brainRotLevel ?: "chill") }
    var remarks by remember { mutableStateOf(taskToEdit?.remarks ?: "") }

    // Date & Time Calendar State
    val initialCal = remember {
        Calendar.getInstance().apply {
            if (taskToEdit?.dueDate != null) {
                time = taskToEdit.dueDate
            } else if (initialDate != null) {
                time = initialDate
            } else {
                add(Calendar.HOUR_OF_DAY, 2)
            }
        }
    }
    var selectedCalendar by remember { mutableStateOf(initialCal) }
    var hasSetCustomDate by remember { mutableStateOf(taskToEdit?.dueDate != null || initialDate != null) }

    val presetSubjects = listOf("General Med", "Pathology", "Forex XAUUSD", "Coding", "Syllabus Grind", "Life Admin", "+ Custom")

    val dateDisplayFormat = remember { SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()) }
    val timeDisplayFormat = remember { SimpleDateFormat("hh:mm a", Locale.getDefault()) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 16.dp)
                .wrapContentHeight(),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = if (isReminder) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (isReminder) Icons.Default.NotificationsActive else Icons.Default.Assignment,
                                    contentDescription = null,
                                    tint = if (isReminder) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (taskToEdit != null) {
                                    if (isReminder) "Edit Reminder ⏰" else "Edit Study Task ✏️"
                                } else {
                                    if (isReminder) "Setup Reminder ⏰" else "New Study Task 🪐"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (taskToEdit != null) "Update Schedule & Parameters" else "Assistant Protocol • Manual Dispatch",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Type Toggle (Task vs Reminder)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = !isReminder,
                        onClick = { isReminder = false },
                        label = { Text("Study / Work Task", fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Icon(Icons.Default.MenuBook, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.primary,
                            selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    FilterChip(
                        selected = isReminder,
                        onClick = { isReminder = true },
                        label = { Text("Timed Alert", fontWeight = FontWeight.SemiBold) },
                        leadingIcon = { Icon(Icons.Default.Alarm, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.tertiary,
                            selectedLabelColor = MaterialTheme.colorScheme.onTertiary,
                            selectedLeadingIconColor = MaterialTheme.colorScheme.onTertiary
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }

                // Title Input
                OutlinedTextField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) titleError = false
                    },
                    label = { Text(if (isReminder) "Reminder Title *" else "Task Objective *") },
                    placeholder = {
                        Text(if (isReminder) "e.g. Check XAUUSD New York Open" else "e.g. Robbins Pathology Chapter 7")
                    },
                    isError = titleError,
                    supportingText = {
                        if (titleError) {
                            Text("Title cannot be blank", color = MaterialTheme.colorScheme.error)
                        }
                    },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp)
                )

                // Subject / Domain selector
                Column {
                    Text(
                        text = "Domain / Category",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        presetSubjects.forEach { sub ->
                            val isSelected = if (sub == "+ Custom") showCustomSubjectInput else (!showCustomSubjectInput && selectedSubject == sub)
                            InputChip(
                                selected = isSelected,
                                onClick = {
                                    if (sub == "+ Custom") {
                                        showCustomSubjectInput = true
                                    } else {
                                        showCustomSubjectInput = false
                                        selectedSubject = sub
                                    }
                                },
                                label = { Text(sub) }
                            )
                        }
                    }

                    if (showCustomSubjectInput) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = customSubject,
                            onValueChange = { customSubject = it },
                            placeholder = { Text("Enter custom category...") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp)
                        )
                    }
                }

                // Brain Rot / Intensity selector
                Column {
                    Text(
                        text = "Brain Energy Required",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val levels = listOf(
                            Triple("chill", "Chill 🧊", Color(0xFF4CAF50)),
                            Triple("mid", "Mid 🔥", Color(0xFFFF9800)),
                            Triple("cooked", "Cooked 💀", Color(0xFFF44336))
                        )

                        levels.forEach { (levelKey, label, color) ->
                            val isSelected = selectedBrainRot == levelKey
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) color.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant,
                                border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, color) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { selectedBrainRot = levelKey }
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 10.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = label,
                                        color = if (isSelected) color else MaterialTheme.colorScheme.onSurfaceVariant,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // Due Date & Time Pickers
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Due Date & Alert Time",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (hasSetCustomDate) "Scheduled" else "Optional",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))

                    // Date & Time Display Pills
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Date Picker Button
                        OutlinedCard(
                            onClick = {
                                val year = selectedCalendar.get(Calendar.YEAR)
                                val month = selectedCalendar.get(Calendar.MONTH)
                                val day = selectedCalendar.get(Calendar.DAY_OF_MONTH)
                                DatePickerDialog(context, { _, y, m, d ->
                                    val newCal = Calendar.getInstance().apply {
                                        time = selectedCalendar.time
                                        set(Calendar.YEAR, y)
                                        set(Calendar.MONTH, m)
                                        set(Calendar.DAY_OF_MONTH, d)
                                    }
                                    selectedCalendar = newCal
                                    hasSetCustomDate = true
                                }, year, month, day).show()
                            },
                            modifier = Modifier.weight(1.2f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.CalendarToday, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = dateDisplayFormat.format(selectedCalendar.time),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Time Picker Button
                        OutlinedCard(
                            onClick = {
                                val hour = selectedCalendar.get(Calendar.HOUR_OF_DAY)
                                val min = selectedCalendar.get(Calendar.MINUTE)
                                TimePickerDialog(context, { _, h, m ->
                                    val newCal = Calendar.getInstance().apply {
                                        time = selectedCalendar.time
                                        set(Calendar.HOUR_OF_DAY, h)
                                        set(Calendar.MINUTE, m)
                                        set(Calendar.SECOND, 0)
                                    }
                                    selectedCalendar = newCal
                                    hasSetCustomDate = true
                                }, hour, min, false).show()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.AccessTime, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = timeDisplayFormat.format(selectedCalendar.time),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    // Quick Schedule Preset Chips
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SuggestionChip(
                            onClick = {
                                val c = Calendar.getInstance().apply { add(Calendar.HOUR_OF_DAY, 2) }
                                selectedCalendar = c
                                hasSetCustomDate = true
                            },
                            label = { Text("In 2h", fontSize = 11.sp) }
                        )
                        SuggestionChip(
                            onClick = {
                                val c = Calendar.getInstance().apply {
                                    set(Calendar.HOUR_OF_DAY, 20)
                                    set(Calendar.MINUTE, 0)
                                }
                                selectedCalendar = c
                                hasSetCustomDate = true
                            },
                            label = { Text("Tonight 8 PM", fontSize = 11.sp) }
                        )
                        SuggestionChip(
                            onClick = {
                                val c = Calendar.getInstance().apply {
                                    add(Calendar.DAY_OF_MONTH, 1)
                                    set(Calendar.HOUR_OF_DAY, 9)
                                    set(Calendar.MINUTE, 0)
                                }
                                selectedCalendar = c
                                hasSetCustomDate = true
                            },
                            label = { Text("Tomorrow 9 AM", fontSize = 11.sp) }
                        )
                        SuggestionChip(
                            onClick = {
                                val c = Calendar.getInstance().apply {
                                    add(Calendar.DAY_OF_MONTH, 2)
                                    set(Calendar.HOUR_OF_DAY, 10)
                                    set(Calendar.MINUTE, 0)
                                }
                                selectedCalendar = c
                                hasSetCustomDate = true
                            },
                            label = { Text("In 2 Days", fontSize = 11.sp) }
                        )
                    }
                }

                // Remarks / Notes
                OutlinedTextField(
                    value = remarks,
                    onValueChange = { remarks = it },
                    label = { Text("Remarks / Action Items (Optional)") },
                    placeholder = { Text("Key focus points, chapter page numbers, or alert triggers...") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3,
                    shape = RoundedCornerShape(14.dp)
                )

                // Actions: Cancel & Create
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                titleError = true
                                Toast.makeText(context, "Please provide a title", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            val finalSubject = if (showCustomSubjectInput && customSubject.isNotBlank()) {
                                customSubject.trim()
                            } else {
                                selectedSubject
                            }

                            val dueDateToSave = if (hasSetCustomDate || isReminder) selectedCalendar.time else null

                            onSave(
                                title.trim(),
                                finalSubject,
                                selectedBrainRot,
                                dueDateToSave,
                                isReminder,
                                remarks.ifBlank { null }
                            )

                            Toast.makeText(
                                context,
                                if (taskToEdit != null) "Task updated successfully!" else if (isReminder) "Reminder set for ${timeDisplayFormat.format(selectedCalendar.time)}!" else "Task added to Ledger!",
                                Toast.LENGTH_SHORT
                            ).show()
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isReminder) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = if (taskToEdit != null) Icons.Default.Save else if (isReminder) Icons.Default.AlarmOn else Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (taskToEdit != null) "Save Changes" else if (isReminder) "Set Reminder" else "Create Task",
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
