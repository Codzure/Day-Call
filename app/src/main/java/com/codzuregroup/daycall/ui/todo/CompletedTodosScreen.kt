package com.codzuregroup.daycall.ui.todo

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CompletedTodosScreen(
    onBackPressed: () -> Unit,
    onEditTodo: (TodoItem) -> Unit,
    viewModel: TodoViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val completed = uiState.todos.filter { it.isCompleted }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Completed Tasks", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (completed.isEmpty()) {
                item { Text("No completed tasks yet.") }
            } else {
                items(completed) { todo ->
                    ModernTaskCard(
                        todo = todo,
                        onToggleComplete = { viewModel.handleEvent(TodoEvent.ToggleComplete(todo.id)) },
                        onEdit = { onEditTodo(todo) },
                        onDelete = { viewModel.handleEvent(TodoEvent.DeleteTodo(todo.id)) }
                    )
                }
            }
        }
    }
}
