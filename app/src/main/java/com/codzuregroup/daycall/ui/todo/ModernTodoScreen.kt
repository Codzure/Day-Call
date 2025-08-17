package com.codzuregroup.daycall.ui.todo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codzuregroup.daycall.ui.components.DayCallCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTodoScreen(
    onBackPressed: (() -> Unit)? = null,
    onNavigateToAddTodo: () -> Unit,
    onNavigateToEditTodo: (TodoItem) -> Unit,
    onNavigateToCompleted: () -> Unit = {},
    viewModel: TodoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var currentFilter by remember { mutableStateOf(TodoFilter.ALL) }
    
    // Mock data for demonstration - in real app this would come from viewModel
    val streakData = remember { StreakData(currentStreak = 5, bestStreak = 12) }
    val todayProgress = remember { 
        if (uiState.todos.isEmpty()) 0f 
        else uiState.todos.count { it.isCompleted }.toFloat() / uiState.todos.size.toFloat()
    }
    
    // Filter todos based on current filter
    val filteredTodos = remember(uiState.todos, currentFilter) {
        when (currentFilter) {
            TodoFilter.ALL -> uiState.todos.filter { !it.isCompleted }
            TodoFilter.COMPLETED -> uiState.todos.filter { it.isCompleted }
            TodoFilter.OVERDUE -> uiState.todos.filter {
                it.dueDate != null &&
                it.dueDate.isBefore(java.time.LocalDateTime.now()) &&
                !it.isCompleted
            }
            TodoFilter.SCHEDULED -> uiState.todos.filter {
                !it.isCompleted && (
                    (it.dueDate != null && it.dueDate.isAfter(java.time.LocalDateTime.now())) ||
                    (it.reminderTime != null && it.reminderTime.isAfter(java.time.LocalDateTime.now()))
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "My Tasks",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                ),
                windowInsets = WindowInsets.statusBars
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onNavigateToAddTodo,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Add Task",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Task")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Hero Dashboard
            item {
                ModernTodoDashboard(
                    stats = viewModel.getStats(),
                    streakData = streakData,
                    todayProgress = todayProgress
                )
            }
            
            // Quick Actions Section
            item {
                QuickActionsSection(
                    onAddTask = onNavigateToAddTodo,
                    onViewCompleted = { currentFilter = TodoFilter.COMPLETED },
                    onViewOverdue = { currentFilter = TodoFilter.OVERDUE },
                    onViewScheduled = { currentFilter = TodoFilter.SCHEDULED }
                )
            }
            
            // Today's Tasks Section
            item {
                SectionHeader(
                    title = when (currentFilter) {
                        TodoFilter.ALL -> "Today's Tasks"
                        TodoFilter.COMPLETED -> "Completed Tasks"
                        TodoFilter.OVERDUE -> "Overdue Tasks"
                        TodoFilter.SCHEDULED -> "Scheduled Tasks"
                    },
                    subtitle = when (currentFilter) {
                        TodoFilter.ALL -> "${filteredTodos.count { !it.isCompleted }} remaining"
                        TodoFilter.COMPLETED -> "${filteredTodos.size} completed"
                        TodoFilter.OVERDUE -> "${filteredTodos.size} overdue"
                        TodoFilter.SCHEDULED -> "${filteredTodos.size} scheduled"
                    }
                )
            }
            
            // Task List
            if (filteredTodos.isEmpty()) {
                item {
                    ModernEmptyState(
                        onAddTask = onNavigateToAddTodo
                    )
                }
            } else {
                items(filteredTodos) { todo ->
                    ModernTaskCard(
                        todo = todo,
                        onToggleComplete = { 
                            viewModel.handleEvent(TodoEvent.ToggleComplete(todo.id)) 
                        },
                        onEdit = { onNavigateToEditTodo(todo) },
                        onDelete = { 
                            viewModel.handleEvent(TodoEvent.DeleteTodo(todo.id)) 
                        }
                    )
                }
                
                // Completed Tasks Section (if any and when showing active list)
                val completedTasks = uiState.todos.filter { it.isCompleted }
                if (completedTasks.isNotEmpty() && currentFilter == TodoFilter.ALL) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(
                            title = "Completed",
                            subtitle = "${completedTasks.size} task${if (completedTasks.size != 1) "s" else ""}"
                        )
                    }
                    
                    items(completedTasks.take(3)) { todo -> // Show only first 3 completed
                        ModernTaskCard(
                            todo = todo,
                            onToggleComplete = { 
                                viewModel.handleEvent(TodoEvent.ToggleComplete(todo.id)) 
                            },
                            onEdit = { onNavigateToEditTodo(todo) },
                            onDelete = { 
                                viewModel.handleEvent(TodoEvent.DeleteTodo(todo.id)) 
                            }
                        )
                    }
                    
                    if (completedTasks.size > 3) {
                        item {
                            TextButton(
                                onClick = onNavigateToCompleted,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("View ${completedTasks.size - 3} more completed tasks")
                            }
                        }
                    }
                }
            }
            
            // Bottom spacing for FAB
            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
fun QuickActionsSection(
    onAddTask: () -> Unit,
    onViewCompleted: () -> Unit,
    onViewOverdue: () -> Unit,
    onViewScheduled: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    DayCallCard(
        modifier = modifier.fillMaxWidth(),
        elevation = 6
    ) {
            Text(
                text = "Quick Actions",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                QuickActionButton(
                    text = "Add Task",
                    onClick = onAddTask,
                    modifier = Modifier.weight(1f)
                )
                
                QuickActionButton(
                    text = "Completed",
                    onClick = onViewCompleted,
                    modifier = Modifier.weight(1f)
                )
                
                QuickActionButton(
                    text = "Overdue",
                    onClick = onViewOverdue,
                    modifier = Modifier.weight(1f)
                )
                
                QuickActionButton(
                    text = "Scheduled",
                    onClick = onViewScheduled,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }

@Composable
fun QuickActionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ),
        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1
        )
    }
}

@Composable
fun SectionHeader(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun ModernEmptyState(
    onAddTask: () -> Unit,
    modifier: Modifier = Modifier
) {
    DayCallCard(
        modifier = modifier.fillMaxWidth(),
        elevation = 4
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(40.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "📝",
                style = MaterialTheme.typography.displayMedium
            )
            
            Text(
                text = "No tasks yet",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface
            )
            
            Text(
                text = "Add your first task to get started with your productive day!",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            
            Button(
                onClick = onAddTask,
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add Your First Task")
            }
        }
    }
}