package com.codzuregroup.daycall.ui.todo

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTodoScreen(
    onBackPressed: () -> Unit,
    onSaveTodo: (TodoItem) -> Unit,
    editingTodo: TodoItem? = null
) {
    var title by remember { mutableStateOf(editingTodo?.title ?: "") }
    var description by remember { mutableStateOf(editingTodo?.description ?: "") }
    var selectedPriority by remember { mutableStateOf(editingTodo?.priority ?: TodoPriority.MEDIUM) }
    var selectedCategory by remember { mutableStateOf(editingTodo?.category ?: TodoCategory.PERSONAL) }
    var hasDueDate by remember { mutableStateOf(editingTodo?.dueDate != null) }
    var selectedDate by remember { mutableStateOf(editingTodo?.dueDate ?: LocalDateTime.now().plusDays(1)) }
    var selectedTime by remember { mutableStateOf(editingTodo?.dueDate ?: LocalDateTime.now().withHour(9).withMinute(0)) }
    var hasReminder by remember { mutableStateOf(editingTodo?.reminderTime != null) }
    var reminderTime by remember { mutableStateOf(editingTodo?.reminderTime ?: LocalDateTime.now().plusHours(1)) }
    var tags by remember { mutableStateOf(editingTodo?.tags?.joinToString(", ") ?: "") }
    var isRecurring by remember { mutableStateOf(editingTodo?.isRecurring ?: false) }
    var recurrencePattern by remember { mutableStateOf(editingTodo?.recurrencePattern ?: RecurrencePattern.DAILY) }
    var selectedColor by remember { mutableStateOf(editingTodo?.color) }
    
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    var showReminderTimePicker by remember { mutableStateOf(false) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (editingTodo != null) "Edit Task" else "Add New Task",
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                actions = {
                    TextButton(
                        onClick = {
                            if (title.isNotBlank()) {
                                val dueDate = if (hasDueDate) {
                                    selectedDate.withHour(selectedTime.hour).withMinute(selectedTime.minute)
                                } else null
                                
                                val reminder = if (hasReminder) reminderTime else null
                                
                                val todo = TodoItem(
                                    id = editingTodo?.id ?: 0,
                                    title = title,
                                    description = description,
                                    priority = selectedPriority,
                                    category = selectedCategory,
                                    dueDate = dueDate,
                                    reminderTime = reminder,
                                    color = selectedColor,
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
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))
            
            // Title Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Task Details",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Title
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Task Title *") },
                        placeholder = { Text("Enter task title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    
                    // Description
                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description (Optional)") },
                        placeholder = { Text("Enter task description") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        maxLines = 5,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
            
            // Priority & Category Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Priority & Category",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Priority Selection
                    Text(
                        text = "Priority",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(TodoPriority.values()) { priority ->
                            PriorityChip(
                                priority = priority,
                                isSelected = selectedPriority == priority,
                                onClick = { selectedPriority = priority }
                            )
                        }
                    }
                    
                    // Category Selection
                    Text(
                        text = "Category",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(TodoCategory.values()) { category ->
                            CategoryChip(
                                category = category,
                                isSelected = selectedCategory == category,
                                onClick = { selectedCategory = category }
                            )
                        }
                    }
                }
            }
            
            // Tags Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Tags",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    OutlinedTextField(
                        value = tags,
                        onValueChange = { tags = it },
                        label = { Text("Tags (comma separated)") },
                        placeholder = { Text("work, urgent, project") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline
                        )
                    )
                }
            }
            
            // Due Date Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Due Date",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Due Date Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = hasDueDate,
                            onCheckedChange = { hasDueDate = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = "Set due date",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    // Due Date/Time Selection (if enabled)
                    AnimatedVisibility(
                        visible = hasDueDate,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Date
                                OutlinedTextField(
                                    value = selectedDate.format(DateTimeFormatter.ofPattern("MMM dd, yyyy")),
                                    onValueChange = { },
                                    label = { Text("Date") },
                                    modifier = Modifier.weight(1f),
                                    readOnly = true,
                                    trailingIcon = {
                                        IconButton(onClick = { showDatePicker = true }) {
                                            Icon(Icons.Outlined.CalendarToday, contentDescription = "Date")
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                                
                                // Time
                                OutlinedTextField(
                                    value = selectedTime.format(DateTimeFormatter.ofPattern("HH:mm")),
                                    onValueChange = { },
                                    label = { Text("Time") },
                                    modifier = Modifier.weight(1f),
                                    readOnly = true,
                                    trailingIcon = {
                                        IconButton(onClick = { showTimePicker = true }) {
                                            Icon(Icons.Outlined.Schedule, contentDescription = "Time")
                                        }
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                )
                            }
                        }
                    }
                }
            }
            
            // Reminder Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Reminder",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Reminder Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = hasReminder,
                            onCheckedChange = { hasReminder = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = "Set reminder",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    // Reminder Time Selection (if enabled)
                    AnimatedVisibility(
                        visible = hasReminder,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        OutlinedTextField(
                            value = reminderTime.format(DateTimeFormatter.ofPattern("MMM dd, yyyy HH:mm")),
                            onValueChange = { },
                            label = { Text("Reminder Time") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                IconButton(onClick = { showReminderTimePicker = true }) {
                                    Icon(Icons.Outlined.Notifications, contentDescription = "Reminder")
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                }
            }
            
            // Recurrence Section
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Recurrence",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    
                    // Recurrence Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Switch(
                            checked = isRecurring,
                            onCheckedChange = { isRecurring = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = MaterialTheme.colorScheme.primary,
                                checkedTrackColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        Text(
                            text = "Recurring task",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                    
                    // Recurrence Pattern Selection (if enabled)
                    AnimatedVisibility(
                        visible = isRecurring,
                        enter = slideInVertically() + fadeIn(),
                        exit = slideOutVertically() + fadeOut()
                    ) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(RecurrencePattern.values()) { pattern ->
                                RecurrenceChip(
                                    pattern = pattern,
                                    isSelected = recurrencePattern == pattern,
                                    onClick = { recurrencePattern = pattern }
                                )
                            }
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
    
    // Date and Time Pickers would be implemented here
    // For now, we'll use simple state updates
}

@Composable
fun PriorityChip(
    priority: TodoPriority,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                priority.color.copy(alpha = 0.2f) 
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, priority.color)
        } else null,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(
                        color = priority.color,
                        shape = CircleShape
                    )
            )
            
            Spacer(modifier = Modifier.width(6.dp))
            
            Text(
                text = priority.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) priority.color else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun CategoryChip(
    category: TodoCategory,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                category.color.copy(alpha = 0.2f) 
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, category.color)
        } else null,
        shape = RoundedCornerShape(20.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = category.icon,
                fontSize = 16.sp
            )
            
            Spacer(modifier = Modifier.width(6.dp))
            
            Text(
                text = category.displayName,
                style = MaterialTheme.typography.bodySmall,
                color = if (isSelected) category.color else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun RecurrenceChip(
    pattern: RecurrencePattern,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) 
            else MaterialTheme.colorScheme.surface
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else null,
        shape = RoundedCornerShape(20.dp)
    ) {
        Text(
            text = pattern.name,
            style = MaterialTheme.typography.bodySmall,
            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
} 