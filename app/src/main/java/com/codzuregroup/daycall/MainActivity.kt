package com.codzuregroup.daycall

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codzuregroup.daycall.ui.AlarmViewModel
import com.codzuregroup.daycall.ui.alarm.AddAlarmScreen
import com.codzuregroup.daycall.ui.alarm.AlarmListScreen
import com.codzuregroup.daycall.ui.alarm.EditAlarmScreen
import com.codzuregroup.daycall.ui.theme.DayCallTheme
import com.codzuregroup.daycall.ui.vibes.VibesScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
    var currentScreen by remember { mutableStateOf<Screen>(Screen.AlarmList) }
    val viewModel: AlarmViewModel = viewModel()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        when (currentScreen) {
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
                    onVibesPressed = { currentScreen = Screen.Vibes }
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
                    },
                    onTestSound = { soundName ->
                        // Handle sound testing
                    }
                )
            }
            Screen.Vibes -> {
                VibesScreen(
                    onBackPressed = { currentScreen = Screen.AlarmList },
                    onVibeSelected = { vibe ->
                        // Handle vibe selection - could be used to set default vibe for alarms
                        // For now, we'll just navigate back
                        currentScreen = Screen.AlarmList
                    }
                )
            }
        }
    }
}

sealed class Screen {
    object AlarmList : Screen()
    object AddAlarm : Screen()
    data class EditAlarm(val alarmId: Long) : Screen()
    object Vibes : Screen()
}