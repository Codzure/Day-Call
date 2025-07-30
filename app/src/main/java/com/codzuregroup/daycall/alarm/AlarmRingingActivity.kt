package com.codzuregroup.daycall.alarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codzuregroup.daycall.audio.AudioManager
import com.codzuregroup.daycall.ui.alarm.AlarmRingingScreen
import com.codzuregroup.daycall.ui.theme.DayCallTheme

class AlarmRingingActivity : ComponentActivity() {
    private var audioManager: AudioManager? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val alarmId = intent.getLongExtra("ALARM_ID", -1)
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"
        val audioFile = intent.getStringExtra("AUDIO_FILE")
        val challengeType = intent.getStringExtra("CHALLENGE_TYPE") ?: "MATH"
        
        audioManager = AudioManager(this)
        
        setContent {
            DayCallTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AlarmRingingScreen(
                        alarmLabel = alarmLabel,
                        audioFile = audioFile,
                        audioManager = audioManager,
                        onDismiss = {
                            audioManager?.stopAudio()
                            finish()
                        },
                        onSnooze = {
                            audioManager?.stopAudio()
                            // Implement snooze logic here
                            finish()
                        }
                    )
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        audioManager?.stopAudio()
    }
}
