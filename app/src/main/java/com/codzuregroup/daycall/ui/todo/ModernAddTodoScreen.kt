package com.codzuregroup.daycall.ui.todo

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import com.codzuregroup.daycall.ui.components.DayCallCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernAddTodoScreen(
    onBackPressed: () -> Unit,
    onSaveTodo: (TodoItem) -> Unit,
    editingTodo: TodoItem? = null
) {
    var title by remember { mutableStateOf(editingTodo?.title ?: "") }
    var description by remember { mutableStateOf(editingTodo?.description ?: "") }
    var selectedPriority by remember { mutableStateOf(editingTodo?.priority ?: TodoPriority.MEDIUM) }
    var selectedCategory by remember { mutableStateOf(editingTodo?.category ?: TodoCategory.PERSONAL) }
    var tags by remember { mutableStateOf(editingTodo?.tags?.joinToString(", ") ?: "") }
    
    val context = LocalContext.current

    // Toggle states matching your image
    var hasDueDate by remember { mutableStateOf(editingTodo?.dueDate != null) }
    var hasReminder by remember { mutableStateOf(editingTodo?.reminderTime != null) }
    var isRecurring by remember { mutableStateOf(editingTodo?.isRecurring ?: false) }
    
    var selectedDate by remember { mutableStateOf(editingTodo?.dueDate ?: LocalDateTime.now().plusDays(1)) }
    var reminderTime by remember { mutableStateOf(editingTodo?.reminderTime ?: LocalDateTime.now().plusHours(1)) }
    var recurrencePattern by remember { mutableStateOf(editingTodo?.recurrencePattern ?: RecurrencePattern.DAILY) }

    fun pickDate(initial: LocalDate = LocalDate.now(), onPicked: (LocalDate) -> Unit) {
        android.app.DatePickerDialog(
            context,
            { _, y, m, d -> onPicked(LocalDate.of(y, m + 1, d)) },
            initial.year,
            initial.monthValue - 1,
            initial.dayOfMonth
        ).show()
    }

    fun pickTime(initial: LocalTime = LocalTime.now(), onPicked: (LocalTime) -> Unit) {
        android.app.TimePickerDialog(
            context,
            { _, h, min -> onPicked(LocalTime.of(h, min)) },
            initial.hour,
            initial.minute,
            true
        ).show()
    }

    // Handle system back button
    BackHandler {
        onBackPressed()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (editingTodo != null) "Edit Task" else "Add New Task",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                val dueDate = if (hasDueDate) selectedDate else null
                                val reminder = if (hasReminder) reminderTime else null
                                
                                val todo = TodoItem(
                                    id = editingTodo?.id ?: 0,
                                    title = title,
                                    description = description,
                                    priority = selectedPriority,
                                    category = selectedCategory,
                                    dueDate = dueDate,
                                    reminderTime = reminder,
                                    tags = tags.split(",").map { it.trim() }.filter { it.isNotEmpty() },
                                    isRecurring = isRecurring,
                                    recurrencePattern = if (isRecurring) recurrencePattern else null
                                )
                                onSaveTodo(todo)
                            }
                        },
                        enabled = title.isNotBlank()
                    ) {
                        Text(
                            text = if (editingTodo != null) "Update" else "Save",
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets.statusBars
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Task Title
            ModernInputField(
                value = title,
                onValueChange = { title = it },
                label = "Task Title",
                placeholder = "Enter task title",
                leadingIcon = Icons.Outlined.Assignment
            )
            
            // Task Description
            ModernInputField(
                value = description,
                onValueChange = { description = it },
                label = "Description (Optional)",
                placeholder = "Enter task description",
                leadingIcon = Icons.Outlined.Description,
                maxLines = 3,
                minLines = 3
            )
            
            // Priority Selection
            DayCallCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4
            ) {
                Text(
                    text = "Priority",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                ModernChipGroup(
                    items = TodoPriority.values().map { it.displayName },
                    selectedItems = listOf(selectedPriority.displayName),
                    onSelectionChange = { selected ->
                        selected.firstOrNull()?.let { priorityName ->
                            selectedPriority = TodoPriority.values().find { it.displayName == priorityName } ?: TodoPriority.MEDIUM
                        }
                    },
                    multiSelect = false
                )
            }
            
            // Category Selection
            DayCallCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 4
            ) {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                ModernChipGroup(
                    items = TodoCategory.values().map { it.displayName },
                    selectedItems = listOf(selectedCategory.displayName),
                    onSelectionChange = { selected ->
                        selected.firstOrNull()?.let { categoryName ->
                            selectedCategory = TodoCategory.values().find { it.displayName == categoryName } ?: TodoCategory.PERSONAL
                        }
                    },
                    multiSelect = false
                )
            }
            
            // Bottom Section - Matching your image exactly
            DayCallCard(
                modifier = Modifier.fillMaxWidth(),
                elevation = 6
            ) {
                // Tags Input Field (matching your image)
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    placeholder = { 
                        Text(
                            "Tags (comma separated)",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ) 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Due Date Toggle (exactly matching your image)
                ModernToggleCard(
                    title = "Set due date",
                    icon = Icons.Outlined.CalendarToday,
                    isEnabled = hasDueDate,
                    onToggle = { hasDueDate = it }
                )
                if (hasDueDate) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            pickDate(selectedDate.toLocalDate()) { d ->
                                selectedDate = selectedDate.withYear(d.year).withMonth(d.monthValue).withDayOfMonth(d.dayOfMonth)
                            }
                        }) { Text("Pick date") }
                        OutlinedButton(onClick = {
                            pickTime(selectedDate.toLocalTime()) { t ->
                                selectedDate = selectedDate.withHour(t.hour).withMinute(t.minute).withSecond(0)
                            }
                        }) { Text("Pick time") }
                    }
                    Text(
                        text = selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Reminder Toggle (exactly matching your image)
                ModernToggleCard(
                    title = "Set reminder",
                    icon = Icons.Outlined.Notifications,
                    isEnabled = hasReminder,
                    onToggle = { hasReminder = it }
                )
                if (hasReminder) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = {
                            pickDate(reminderTime.toLocalDate()) { d ->
                                reminderTime = reminderTime.withYear(d.year).withMonth(d.monthValue).withDayOfMonth(d.dayOfMonth)
                            }
                        }) { Text("Pick date") }
                        OutlinedButton(onClick = {
                            pickTime(reminderTime.toLocalTime()) { t ->
                                reminderTime = reminderTime.withHour(t.hour).withMinute(t.minute).withSecond(0)
                            }
                        }) { Text("Pick time") }
                    }
                    Text(
                        text = reminderTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // Recurring Task Toggle (exactly matching your image)
                ModernToggleCard(
                    title = "Recurring task",
                    icon = Icons.Outlined.Refresh,
                    isEnabled = isRecurring,
                    onToggle = { isRecurring = it }
                )
                if (isRecurring) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(selected = recurrencePattern == RecurrencePattern.DAILY, onClick = { recurrencePattern = RecurrencePattern.DAILY }, label = { Text("Daily") })
                        FilterChip(selected = recurrencePattern == RecurrencePattern.WEEKLY, onClick = { recurrencePattern = RecurrencePattern.WEEKLY }, label = { Text("Weekly") })
                        FilterChip(selected = recurrencePattern == RecurrencePattern.MONTHLY, onClick = { recurrencePattern = RecurrencePattern.MONTHLY }, label = { Text("Monthly") })
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}