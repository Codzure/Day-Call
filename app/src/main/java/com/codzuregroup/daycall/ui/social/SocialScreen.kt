package com.codzuregroup.daycall.ui.social

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codzuregroup.daycall.ui.theme.*
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SocialScreen(
    onBackPressed: () -> Unit,
    viewModel: SocialViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Social",
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
        ) {
            // Content based on selected tab
            when (selectedTab) {
                0 -> CirclesTab(uiState, viewModel)
                1 -> ProfileTab(uiState, viewModel)
            }
        }
    }
}

@Composable
fun CirclesTab(uiState: SocialUiState, viewModel: SocialViewModel) {
    val selectedCircle = uiState.selectedCircle
    var messageText by remember { mutableStateOf("") }
    
    if (selectedCircle != null) {
        // Show group chat for selected circle
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Circle header with back button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { viewModel.handleEvent(SocialEvent.SelectCircle(null)) }
                    ) {
                        Icon(
                            Icons.Default.ArrowBack,
                            contentDescription = "Back to Circles",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = selectedCircle.name,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Text(
                            text = "${selectedCircle.members.size} members • ${selectedCircle.alarmTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Messages
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 8.dp)
            ) {
                items(uiState.messages.filter { it.circleId == selectedCircle.id }) { message ->
                    MessageCard(
                        message = message,
                        isOwnMessage = message.senderId == uiState.currentUser?.id,
                        onReaction = { emoji ->
                            viewModel.handleEvent(SocialEvent.ReactToMessage(message.id, emoji))
                        }
                    )
                }
            }
            
            // Message input
            ChatInput(
                messageText = messageText,
                onMessageTextChange = { messageText = it },
                onSendMessage = {
                    if (messageText.isNotBlank()) {
                        viewModel.handleEvent(SocialEvent.SendMessage(messageText))
                        messageText = ""
                    }
                },
                onSendEmoji = { emoji ->
                    viewModel.handleEvent(SocialEvent.SendMessage(emoji, MessageType.EMOJI))
                }
            )
        }
    } else {
        // Show circles list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            item {
                Text(
                    text = "Wake-Up Circles",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = "Join or create circles to wake up with friends",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }
            
            items(uiState.circles) { circle ->
                CircleCard(
                    circle = circle,
                    isSelected = false,
                    currentUserId = uiState.currentUser?.id,
                    onClick = { viewModel.handleEvent(SocialEvent.SelectCircle(circle)) },
                    onJoinLeave = { isMember ->
                        if (isMember) {
                            viewModel.handleEvent(SocialEvent.LeaveCircle(circle.id))
                        } else {
                            viewModel.handleEvent(SocialEvent.JoinCircle(circle.id))
                        }
                    }
                )
            }
            
            item {
                CreateCircleCard(
                    onClick = {
                        // TODO: Show create circle dialog
                    }
                )
            }
        }
    }
}

@Composable
fun CircleCard(
    circle: WakeCircle,
    isSelected: Boolean,
    currentUserId: String?,
    onClick: () -> Unit,
    onJoinLeave: (Boolean) -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        animationSpec = tween(durationMillis = 200),
        label = "scale"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .graphicsLayer(scaleX = scale, scaleY = scale),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = circle.name,
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    Text(
                        text = circle.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                Spacer(modifier = Modifier.width(16.dp))
                
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = circle.alarmTime,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "Wake Time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Members
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${circle.members.size}/${circle.maxMembers} members",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Member avatars
                Row {
                    circle.members.take(3).forEach { member ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    color = if (member.isOnline) 
                                        Color.Green 
                                    else 
                                        MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = member.avatar,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(-8.dp))
                    }
                }
            }
            
            // Join/Leave button
            val isMember = circle.members.any { it.id == currentUserId }
            Button(
                onClick = { onJoinLeave(isMember) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isMember) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.primary
                )
            ) {
                Text(
                    text = if (isMember) "Leave Circle" else "Join Circle"
                )
            }
        }
    }
}

@Composable
fun CreateCircleCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.Add,
                contentDescription = "Create Circle",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "Create New Circle",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}



@Composable
fun MessageCard(
    message: MessageDrop,
    isOwnMessage: Boolean,
    onReaction: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOwnMessage) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = message.senderName,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.weight(1f)
                )
                Text(
                    text = message.createdAt.format(DateTimeFormatter.ofPattern("HH:mm")),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Text(
                text = message.message,
                style = MaterialTheme.typography.bodyMedium
            )
            
            if (message.reactions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    message.reactions.forEach { reaction ->
                        Text(
                            text = reaction.emoji,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .clickable { onReaction(reaction.emoji) }
                                .padding(end = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ProfileTab(uiState: SocialUiState, viewModel: SocialViewModel) {
    val currentUser = uiState.currentUser
    var selectedSection by remember { mutableStateOf("overview") }
    
    if (currentUser == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator()
        }
    } else {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Profile header
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        MaterialTheme.colorScheme.primary,
                                        MaterialTheme.colorScheme.primaryContainer
                                    )
                                ),
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = currentUser.avatar,
                            fontSize = 40.sp
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = currentUser.name,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontWeight = FontWeight.Bold
                        )
                    )
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(top = 4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(
                                    color = if (currentUser.isOnline) Color.Green else Color.Gray,
                                    shape = CircleShape
                                )
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (currentUser.isOnline) "Online" else "Offline",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Section tabs
            TabRow(
                selectedTabIndex = if (selectedSection == "overview") 0 else 1,
                containerColor = Color.Transparent,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Tab(
                    selected = selectedSection == "overview",
                    onClick = { selectedSection = "overview" },
                    text = { Text("Overview") },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Tab(
                    selected = selectedSection == "achievements",
                    onClick = { selectedSection = "achievements" },
                    text = { Text("Achievements") },
                    selectedContentColor = MaterialTheme.colorScheme.primary,
                    unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Content based on selected section
            when (selectedSection) {
                "overview" -> OverviewSection(uiState, currentUser)
                "achievements" -> AchievementsSection(currentUser)
            }
        }
    }
}

@Composable
fun OverviewSection(uiState: SocialUiState, currentUser: SocialUser) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            // Stats
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentUser.wakeStreak.toString(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Day Streak",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    HorizontalDivider(
                        modifier = Modifier
                            .height(40.dp)
                            .width(1.dp)
                    )
                    
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = currentUser.totalWakes.toString(),
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.Bold
                            ),
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Total Wakes",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        item {
            // Quick actions
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Quick Actions",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        QuickActionButton(
                            icon = Icons.Default.Add,
                            text = "Create Circle",
                            onClick = { /* TODO */ },
                            modifier = Modifier.weight(1f)
                        )
                        QuickActionButton(
                            icon = Icons.Default.People,
                            text = "Find Friends",
                            onClick = { /* TODO */ },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
        
        item {
            // Circles joined
            Text(
                text = "Your Circles",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        items(uiState.circles.filter { circle ->
            circle.members.any { it.id == currentUser.id }
        }) { circle ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = circle.name,
                            style = MaterialTheme.typography.titleSmall.copy(
                                fontWeight = FontWeight.Medium
                            )
                        )
                        Text(
                            text = "${circle.members.size} members",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Text(
                        text = circle.alarmTime,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun AchievementsSection(currentUser: SocialUser) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Your Achievements",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
        
        item {
            AchievementCard(
                title = "Early Bird",
                description = "Wake up 7 days in a row",
                icon = "🌅",
                isUnlocked = currentUser.wakeStreak >= 7,
                progress = minOf(currentUser.wakeStreak, 7),
                maxProgress = 7
            )
        }
        
        item {
            AchievementCard(
                title = "Social Butterfly",
                description = "Join 3 different circles",
                icon = "🦋",
                isUnlocked = false, // TODO: Calculate based on circles joined
                progress = 1,
                maxProgress = 3
            )
        }
        
        item {
            AchievementCard(
                title = "Motivator",
                description = "Send 50 encouraging messages",
                icon = "💪",
                isUnlocked = false, // TODO: Calculate based on messages sent
                progress = 12,
                maxProgress = 50
            )
        }
        
        item {
            AchievementCard(
                title = "Consistency King",
                description = "Maintain a 30-day streak",
                icon = "👑",
                isUnlocked = currentUser.wakeStreak >= 30,
                progress = minOf(currentUser.wakeStreak, 30),
                maxProgress = 30
            )
        }
    }
}

@Composable
fun QuickActionButton(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = text,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun AchievementCard(
    title: String,
    description: String,
    icon: String,
    isUnlocked: Boolean,
    progress: Int,
    maxProgress: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (isUnlocked) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.surfaceVariant,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = icon,
                    fontSize = 24.sp
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    )
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (!isUnlocked) {
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = progress.toFloat() / maxProgress.toFloat(),
                        modifier = Modifier.fillMaxWidth(),
                        color = MaterialTheme.colorScheme.primary,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                    Text(
                        text = "$progress/$maxProgress",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            if (isUnlocked) {
                Icon(
                    Icons.Default.Check,
                    contentDescription = "Unlocked",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
} 

@Composable
fun ChatInput(
    messageText: String,
    onMessageTextChange: (String) -> Unit,
    onSendMessage: () -> Unit,
    onSendEmoji: (String) -> Unit
) {
    var showEmojiPicker by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column {
            // Emoji picker
            if (showEmojiPicker) {
                EmojiPicker(
                    onEmojiSelected = { emoji ->
                        onSendEmoji(emoji)
                        showEmojiPicker = false
                    },
                    onDismiss = { showEmojiPicker = false }
                )
            }
            
            // Input row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Emoji button
                IconButton(
                    onClick = { showEmojiPicker = !showEmojiPicker }
                ) {
                    Icon(
                        Icons.Default.EmojiEmotions,
                        contentDescription = "Add Emoji",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                
                // Text input
                OutlinedTextField(
                    value = messageText,
                    onValueChange = onMessageTextChange,
                    placeholder = {
                        Text("Type a message...")
                    },
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color.Transparent,
                        unfocusedBorderColor = Color.Transparent,
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent
                    ),
                    textStyle = MaterialTheme.typography.bodyMedium,
                    maxLines = 3
                )
                
                // Send button
                IconButton(
                    onClick = onSendMessage,
                    enabled = messageText.isNotBlank()
                ) {
                    Icon(
                        Icons.Default.Send,
                        contentDescription = "Send Message",
                        tint = if (messageText.isNotBlank()) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun EmojiPicker(
    onEmojiSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val commonEmojis = listOf(
        "👍", "❤️", "🔥", "💪", "☀️", "🌅", "😊", "🎉", 
        "✨", "🌟", "💯", "🚀", "🎯", "💡", "🌈", "🍀"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quick Emojis",
                    style = MaterialTheme.typography.titleSmall.copy(
                        fontWeight = FontWeight.Medium
                    ),
                    modifier = Modifier.weight(1f)
                )
                IconButton(onClick = onDismiss) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(commonEmojis) { emoji ->
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onEmojiSelected(emoji) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emoji,
                            fontSize = 20.sp
                        )
                    }
                }
            }
        }
    }
} 