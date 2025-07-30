package com.codzuregroup.daycall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.codzuregroup.daycall.ui.social.SocialScreen
import com.codzuregroup.daycall.ui.settings.SettingsScreen
import com.codzuregroup.daycall.ui.settings.SettingsManager
import com.codzuregroup.daycall.ui.login.LoginScreen
import com.codzuregroup.daycall.ui.login.UserManager
import com.codzuregroup.daycall.data.AlarmDatabase
import com.codzuregroup.daycall.ui.vibes.VibeManager
import com.codzuregroup.daycall.ui.alarm.HomeBottomNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        UserManager.initialize(this)
        VibeManager.initializeWithDefault()
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
}

@Composable
fun DayCallApp() {
    var currentScreen by remember { mutableStateOf<Screen?>(null) }
    var selectedTab by remember { mutableStateOf(0) }
    val viewModel: AlarmViewModel = viewModel()
    
    // Start with splash screen
    LaunchedEffect(Unit) {
        currentScreen = Screen.Splash
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when (currentScreen) {
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
                        currentScreen = if (hasUser) {
                            Screen.Main
                        } else {
                            Screen.Login
                        }
                    }
                )
            }
            Screen.Login -> {
                LoginScreen(
                    onLoginComplete = { currentScreen = Screen.Main }
                )
            }
            Screen.Main -> {
                MainContainer(
                    selectedTab = selectedTab,
                    onTabSelected = { selectedTab = it },
                    viewModel = viewModel,
                    onAddAlarm = { currentScreen = Screen.AddAlarm },
                    onEditAlarm = { alarmId ->
                        currentScreen = Screen.EditAlarm(alarmId)
                    },
                    onAlarmRinging = { alarmLabel ->
                        // Handle alarm ringing - this would typically launch AlarmRingingActivity
                        // For now, we'll just show a message or handle it differently
                    }
                )
            }
            Screen.AddAlarm -> {
                AddAlarmScreen(
                    onBackPressed = { currentScreen = Screen.Main },
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
                    alarmId = (currentScreen as Screen.EditAlarm).alarmId,
                    viewModel = viewModel,
                    onBackPressed = { currentScreen = Screen.Main },
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
    onAlarmRinging: (String) -> Unit
) {
    val context = LocalContext.current
    val settingsManager = remember { SettingsManager.getInstance(context) }
    
    Scaffold(
        bottomBar = {
            HomeBottomNavigation(
                selectedTab = selectedTab,
                onTabSelected = onTabSelected
            )
        }
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
                    // Social Tab
                    SocialScreen(
                        onBackPressed = { onTabSelected(0) }
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
}