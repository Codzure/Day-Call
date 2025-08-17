package com.codzuregroup.daycall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.app.AlarmManager
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.compose.foundation.background
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codzuregroup.daycall.R
import kotlinx.coroutines.delay
import com.codzuregroup.daycall.ui.AlarmViewModel
import com.codzuregroup.daycall.ui.alarm.AddAlarmScreen
import com.codzuregroup.daycall.ui.alarm.AlarmListScreen
import com.codzuregroup.daycall.ui.alarm.EditAlarmScreen
import com.codzuregroup.daycall.ui.theme.DayCallTheme
import com.codzuregroup.daycall.ui.vibes.VibesScreen
import com.codzuregroup.daycall.ui.todo.ModernTodoScreen
import com.codzuregroup.daycall.ui.todo.TodoItem
import com.codzuregroup.daycall.ui.todo.ModernAddTodoScreen
import com.codzuregroup.daycall.ui.todo.TodoViewModel
import com.codzuregroup.daycall.ui.todo.TodoEvent
import com.codzuregroup.daycall.ui.settings.SettingsScreen
import com.codzuregroup.daycall.ui.settings.SettingsManager
import com.codzuregroup.daycall.ui.login.LoginScreen
import com.codzuregroup.daycall.ui.login.UserManager
import com.codzuregroup.daycall.data.DayCallDatabase
import com.codzuregroup.daycall.ui.vibes.VibeManager
import com.codzuregroup.daycall.ui.alarm.HomeBottomNavigation
import com.codzuregroup.daycall.alarm.AlarmService
import com.codzuregroup.daycall.alarm.AlarmRingingActivity
import android.app.ActivityManager
import android.content.Context
import androidx.annotation.RequiresApi
import com.codzuregroup.daycall.ui.todo.AddTaskScreenMinimal
import com.codzuregroup.daycall.ui.todo.CompletedTodosScreen

class MainActivity : ComponentActivity() {
    
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // Permission granted, notifications should work now
        }
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Enable edge-to-edge display
        androidx.core.view.WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // Request notification permission for Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        
        // Check and request exact alarm permission for Android 12+
        checkExactAlarmPermission()
        
        // Check if alarm is currently active
        checkActiveAlarm()
        
        UserManager.initialize(this)
        VibeManager.initialize(this)
        setContent {
            DayCallTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    DayCallApp()
                }
            }
        }
    }
    
    override fun onResume() {
        super.onResume()
        // Check for active alarm when app resumes
        checkActiveAlarm()
    }
    
    private fun checkExactAlarmPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val alarmManager = getSystemService(AlarmManager::class.java)
            if (!alarmManager.canScheduleExactAlarms()) {
                // Show dialog to guide user to settings
                showExactAlarmPermissionDialog()
            }
        }
    }
    
    private fun showExactAlarmPermissionDialog() {
        // This would typically show a dialog, but for now we'll just log
        // In a real app, you'd show an AlertDialog here
        android.util.Log.w("MainActivity", "Exact alarm permission not granted. Please enable in settings.")
    }
    
    fun openAlarmSettings() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM).apply {
                data = Uri.parse("package:$packageName")
            }
            startActivity(intent)
        }
    }
    
    private fun checkActiveAlarm() {
        // Check if alarm service is running using the companion object
        if (AlarmService.isAlarmRunning()) {
            android.util.Log.d("MainActivity", "Alarm service is running, launching challenge screen")
            
            // Get stored alarm details
            val alarmDetails = AlarmService.getCurrentAlarmDetails()
            
            // Launch the alarm ringing activity
            val intent = Intent(this, AlarmRingingActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                
                if (alarmDetails != null) {
                    val (alarmId, alarmLabel, alarmSound) = alarmDetails
                    putExtra("ALARM_ID", alarmId)
                    putExtra("ALARM_LABEL", alarmLabel)
                    putExtra("SOUND", alarmSound)
                    putExtra("CHALLENGE_TYPE", "MATH") // Default challenge type
                    putExtra("VIBE", "default")
                } else {
                    // Fallback values
                    putExtra("ALARM_ID", -1L)
                    putExtra("ALARM_LABEL", "Active Alarm")
                    putExtra("SOUND", "Ascent Braam")
                    putExtra("CHALLENGE_TYPE", "MATH")
                    putExtra("VIBE", "default")
                }
                
                action = "ALARM_ACTIVE_ON_APP_OPEN"
            }
            startActivity(intent)
        }
    }
}

@Composable
fun DayCallApp() {
    val backstack = remember { mutableStateListOf<Screen>() }
    var selectedTab by remember { mutableStateOf(0) }
    val viewModel: AlarmViewModel = viewModel()
    var isForward by remember { mutableStateOf(true) }

    fun navigate(screen: Screen) {
        isForward = true
        backstack.add(screen)
    }
    fun replaceRoot(screen: Screen) {
        isForward = true
        backstack.clear()
        backstack.add(screen)
    }
    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    fun popBack() {
        if (backstack.size > 1) {
            isForward = false
            backstack.removeLast()
        }
    }

    // Start with splash screen
    LaunchedEffect(Unit) {
        replaceRoot(Screen.Splash)
    }

    var currentScreen: Screen? = backstack.lastOrNull()

    // System back handling
    BackHandler(enabled = backstack.size > 1) {
        popBack()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                if (isForward) {
                    (slideInHorizontally(initialOffsetX = { it / 3 }) + fadeIn()) togetherWith
                    (slideOutHorizontally(targetOffsetX = { -it / 3 }) + fadeOut())
                } else {
                    (slideInHorizontally(initialOffsetX = { -it / 3 }) + fadeIn()) togetherWith
                    (slideOutHorizontally(targetOffsetX = { it / 3 }) + fadeOut())
                }
            },
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                null -> {
                    // Show loading
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                Screen.Splash -> {
                    SplashScreen(
                        onSplashComplete = { hasUser ->
                            if (hasUser) replaceRoot(Screen.Main) else replaceRoot(Screen.Login)
                        }
                    )
                }
                Screen.Login -> {
                    LoginScreen(
                        onLoginComplete = { replaceRoot(Screen.Main) }
                    )
                }
                Screen.Main -> {
                    MainContainer(
                        selectedTab = selectedTab,
                        onTabSelected = { selectedTab = it },
                        viewModel = viewModel,
                        onAddAlarm = { navigate(Screen.AddAlarm) },
                        onEditAlarm = { alarmId ->
                            navigate(Screen.EditAlarm(alarmId))
                        },
                        onAlarmRinging = { alarmLabel ->
                            // Handle alarm ringing - this would typically launch AlarmRingingActivity
                            // For now, we'll just show a message or handle it differently
                        },
                        onAddTodo = { navigate(Screen.AddTodo) },
                        onEditTodo = { todo ->
                            navigate(Screen.EditTodo(todo))
                        },
                        onNavigateToCompleted = { navigate(Screen.CompletedTodos) }
                    )
                }
                Screen.AddAlarm -> {
                    AddAlarmScreen(
                        onBackPressed = { popBack() },
                        onSaveAlarm = { alarm ->
                            viewModel.saveAlarm(alarm)
                            currentScreen = Screen.Main
                        },
                        onTestSound = { soundName ->
                            // Handle sound testing
                        }
                    )
                }
                is Screen.EditAlarm -> {
                    EditAlarmScreen(
                        alarmId = (screen as Screen.EditAlarm).alarmId,
                        viewModel = viewModel,
                        onBackPressed = { popBack() },
                        onSaveAlarm = { alarm ->
                            viewModel.updateAlarm(alarm)
                            currentScreen = Screen.Main
                        },
                        onDeleteAlarm = { alarm ->
                            viewModel.deleteAlarm(alarm)
                            currentScreen = Screen.Main
                        }
                    )
                }
                Screen.AddTodo -> {
                    val todoViewModel: TodoViewModel = viewModel()
                    AddTaskScreenMinimal(
                        onBack = { popBack() },
                        onSave = { item ->
                            todoViewModel.handleEvent(TodoEvent.AddTodo(item))
                            popBack()
                        }
                    )
                }
                is Screen.EditTodo -> {
                    val todoViewModel: TodoViewModel = viewModel()
                    val editing = (screen as Screen.EditTodo).todo
                    AddTaskScreenMinimal(
                        onBack = { popBack() },
                        onSave = { item ->
                            todoViewModel.handleEvent(TodoEvent.UpdateTodo(item))
                            popBack()
                        },
                        editing = editing
                    )
                }
                Screen.CompletedTodos -> {
                    val todoViewModel: TodoViewModel = viewModel()
                    CompletedTodosScreen(
                        onBackPressed = { popBack() },
                        onEditTodo = { todo -> navigate(Screen.EditTodo(todo)) },
                        viewModel = todoViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun SplashScreen(
    onSplashComplete: (Boolean) -> Unit
) {
    LaunchedEffect(Unit) {
        // Show splash for 2.5 seconds
        kotlinx.coroutines.delay(2500)
        // Check if user exists
        val hasUser = UserManager.hasExistingUser()
        onSplashComplete(hasUser)
    }
    
    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.bg),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )
        
        // Content overlay
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App icon or logo placeholder
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "⏰",
                    fontSize = 48.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // App name
            Text(
                text = "Day Call",
                style = MaterialTheme.typography.displayMedium.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Tagline
            Text(
                text = "Wake with vibes. Live with intention.",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White.copy(alpha = 0.9f),
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Loading indicator
            CircularProgressIndicator(
                color = Color.White,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

@Composable
fun MainContainer(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit,
    viewModel: AlarmViewModel,
    onAddAlarm: () -> Unit,
    onEditAlarm: (Long) -> Unit,
    onAlarmRinging: (String) -> Unit,
    onAddTodo: () -> Unit,
    onEditTodo: (TodoItem) -> Unit,
    onNavigateToCompleted: () -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager.getInstance(context) }
    
    Scaffold(
        bottomBar = {
            HomeBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        },
        contentWindowInsets = WindowInsets(0.dp)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedTab) {
                0 -> {
                    // Alarms Tab
                    AlarmListScreen(
                        viewModel = viewModel,
                        onAddAlarm = onAddAlarm,
                        onEditAlarm = onEditAlarm,
                        onAlarmRinging = onAlarmRinging,
                        showBottomNavigation = false // Don't show bottom nav here since it's handled by MainContainer
                    )
                }
                1 -> {
                    // Vibes Tab
                    VibesScreen(
                        onBackPressed = { onTabSelected(0) },
                        onVibeSelected = { vibe ->
                            // Navigate back to alarms to show effects
                            onTabSelected(0)
                        }
                    )
                }
                2 -> {
                    ModernTodoScreen(
                        onNavigateToAddTodo = onAddTodo,
                        onNavigateToEditTodo = onEditTodo,
                        onNavigateToCompleted = onNavigateToCompleted
                    )
                }
                3 -> {
                    // Settings Tab
                    SettingsScreen(
                        onBackPressed = { onTabSelected(0) },
                        settingsManager = settingsManager
                    )
                }
            }
        }
    }
}

sealed class Screen {
    object Splash : Screen()
    object Login : Screen()
    object Main : Screen()
    object AddAlarm : Screen()
    data class EditAlarm(val alarmId: Long) : Screen()
    object AddTodo : Screen()
    data class EditTodo(val todo: TodoItem) : Screen()
    object CompletedTodos : Screen()
}
