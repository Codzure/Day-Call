package com.codzuregroup.daycall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Alignment
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
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
    val viewModel: AlarmViewModel = viewModel()
    
    // Check if login is required
    LaunchedEffect(Unit) {
        val hasUser = UserManager.hasExistingUser()
        currentScreen = if (hasUser) {
            Screen.AlarmList
        } else {
            Screen.Login
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when (currentScreen) {
            null -> {
                // Show loading or splash screen
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            Screen.Login -> {
                LoginScreen(
                    onLoginComplete = { currentScreen = Screen.AlarmList }
                )
            }
            Screen.AlarmList -> {
                AlarmListScreen(
                    viewModel = viewModel,
                    onAddAlarm = { currentScreen = Screen.AddAlarm },
                    onEditAlarm = { alarmId ->
                        currentScreen = Screen.EditAlarm(alarmId)
                    },
                    onAlarmRinging = { alarmLabel ->
                        // Handle alarm ringing - this would typically launch AlarmRingingActivity
                        // For now, we'll just show a message or handle it differently
                    },
                    onVibesPressed = { currentScreen = Screen.Vibes },
                    onSocialPressed = { currentScreen = Screen.Social },
                    onSettingsPressed = { currentScreen = Screen.Settings }
                )
            }
            Screen.AddAlarm -> {
                AddAlarmScreen(
                    onBackPressed = { currentScreen = Screen.AlarmList },
                    onSaveAlarm = { alarm ->
                        viewModel.saveAlarm(alarm)
                        currentScreen = Screen.AlarmList
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
                    onBackPressed = { currentScreen = Screen.AlarmList },
                    onSaveAlarm = { alarm ->
                        viewModel.updateAlarm(alarm)
                        currentScreen = Screen.AlarmList
                    },
                    onDeleteAlarm = { alarm ->
                        viewModel.deleteAlarm(alarm)
                        currentScreen = Screen.AlarmList
                    }
                )
            }
            Screen.Vibes -> {
                VibesScreen(
                    onBackPressed = { currentScreen = Screen.AlarmList },
                    onVibeSelected = { vibe ->
                        // Navigate back to show the effects of the selected vibe
                        currentScreen = Screen.AlarmList
                    }
                )
            }
            Screen.Social -> {
                SocialScreen(
                    onBackPressed = { currentScreen = Screen.AlarmList }
                )
            }
            Screen.Settings -> {
                val context = LocalContext.current
                val settingsManager = remember { SettingsManager.getInstance(context) }
                SettingsScreen(
                    onBackPressed = { currentScreen = Screen.AlarmList },
                    settingsManager = settingsManager
                )
            }
        }
    }
}

sealed class Screen {
    object Login : Screen()
    object AlarmList : Screen()
    object AddAlarm : Screen()
    data class EditAlarm(val alarmId: Long) : Screen()
    object Vibes : Screen()
    object Social : Screen()
    object Settings : Screen()
}