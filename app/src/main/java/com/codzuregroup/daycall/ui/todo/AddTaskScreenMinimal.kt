package com.codzuregroup.daycall.ui.todo

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class, ExperimentalAnimationApi::class)
@Composable
fun AddTaskScreenMinimal(
    onBack: () -> Unit,
    onSave: (TodoItem) -> Unit,
    editing: TodoItem? = null
) {
    var title by remember { mutableStateOf(editing?.title ?: "") }
    var desc by remember { mutableStateOf(editing?.description ?: "") }
var priority by remember { mutableStateOf(editing?.priority ?: TodoPriority.MEDIUM) }
    var category by remember { mutableStateOf(editing?.category ?: TodoCategory.PERSONAL) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        if (editing != null) "Edit Task" else "New Task",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                actions = {
TextButton(
                        enabled = title.isNotBlank(),
                        onClick = {
                            if (title.isNotBlank()) {
                                val item = (editing ?: TodoItem(
                                    title = title.trim(),
                                    description = desc.trim().ifBlank { "" },
                                    priority = priority,
                                    category = category
                                )).copy(
                                    title = title.trim(),
                                    description = desc.trim().ifBlank { "" },
                                    priority = priority,
                                    category = category,
                                    dueDate = null,
                                    reminderTime = null,
                                    isRecurring = false,
                                    recurrencePattern = null
                                )
                                onSave(item)
                            }
                        }
                    ) { Text(if (editing != null) "Save" else "Add") }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Task title") },
                placeholder = { Text("What needs to be done?") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = desc,
                onValueChange = { desc = it },
                label = { Text("Description (optional)") },
                placeholder = { Text("Add more details about your task…") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )

            SectionHeaderMinimal("Priority")
Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TodoPriority.values().forEach { p ->
                    val selected = priority == p
                    val scale by animateFloatAsState(targetValue = if (selected) 1.05f else 1f, label = "prio_scale")
                    FilterChip(
                        selected = selected,
                        onClick = { priority = p },
                        label = { Text(p.displayName) },
                        modifier = Modifier.scale(scale)
                    )
                }
            }

Divider()

SectionHeaderMinimal("Category")
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TodoCategory.values().forEach { c ->
                    val selected = category == c
                    val scale by animateFloatAsState(targetValue = if (selected) 1.05f else 1f, label = "cat_scale")
                    FilterChip(
                        selected = selected,
                        onClick = { category = c },
                        label = { Text(c.displayName) },
                        modifier = Modifier.scale(scale)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Bottom action (single emphasis)
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        val item = (editing ?: TodoItem(
                            title = title.trim(),
                            description = desc.trim().ifBlank { "" },
                            priority = priority,
                            category = category
                        )).copy(
                            title = title.trim(),
                            description = desc.trim().ifBlank { "" },
                            priority = priority,
                            category = category,
                            dueDate = null,
                            reminderTime = null,
                            isRecurring = false,
                            recurrencePattern = null
                        )
                        onSave(item)
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            ) {
                AnimatedContent(targetState = (editing != null), label = "save_label") { isEdit ->
                    Text(if (isEdit) "Save Task" else "Add Task")
                }
            }
        }
    }
}

@Composable
private fun SectionHeaderMinimal(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

