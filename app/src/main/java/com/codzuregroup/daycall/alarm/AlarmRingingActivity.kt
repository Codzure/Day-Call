package com.codzuregroup.daycall.alarm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.OnBackPressedCallback
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codzuregroup.daycall.audio.AudioManager
import com.codzuregroup.daycall.ui.alarm.AlarmRingingScreen
import com.codzuregroup.daycall.ui.theme.DayCallTheme
import android.os.PowerManager
import android.view.WindowManager
import android.content.Intent
import android.util.Log

class AlarmRingingActivity : ComponentActivity() {
    private var audioManager: AudioManager? = null
    private var isChallengeSolved = false
    private var wakeLock: PowerManager.WakeLock? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        Log.d("AlarmRingingActivity", "Activity created")
        
        // Keep screen on and acquire wake lock
        window.addFlags(
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON or
            WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD or
            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
            WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
        )
        
        // Acquire wake lock
        val powerManager = getSystemService(PowerManager::class.java)
        wakeLock = powerManager.newWakeLock(
            PowerManager.FULL_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP or PowerManager.ON_AFTER_RELEASE,
            "DayCall:AlarmRingingWakeLock"
        )
        wakeLock?.acquire()
        
        // Prevent back navigation until challenge is solved
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                // Do nothing - prevent back navigation
            }
        })
        
        val alarmId = intent.getLongExtra("ALARM_ID", -1)
        val alarmLabel = intent.getStringExtra("ALARM_LABEL") ?: "Alarm"
        val sound = intent.getStringExtra("SOUND") ?: "Ascent Braam"
        val challengeType = intent.getStringExtra("CHALLENGE_TYPE") ?: "MATH"
        val vibe = intent.getStringExtra("VIBE") ?: "default"
        val action = intent.action
        
        Log.d("AlarmRingingActivity", "Alarm triggered - ID: $alarmId, Label: $alarmLabel, Sound: $sound, Action: $action")
        
        audioManager = AudioManager(this)
        
        setContent {
            DayCallTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AlarmRingingScreen(
                        alarmLabel = alarmLabel,
                        audioFile = sound,
                        audioManager = audioManager,
                        onDismiss = {
                            Log.d("AlarmRingingActivity", "Alarm dismissed")
                            stopAlarm()
                            releaseWakeLock()
                            finish()
                        },
                        onSnooze = {
                            Log.d("AlarmRingingActivity", "Alarm snoozed")
                            stopAlarm()
                            // TODO: Implement snooze logic
                            releaseWakeLock()
                            finish()
                        },
                        onChallengeSolved = {
                            Log.d("AlarmRingingActivity", "Challenge solved")
                            isChallengeSolved = true
                            // Enable back navigation after challenge is solved
                            onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
                                override fun handleOnBackPressed() {
                                    Log.d("AlarmRingingActivity", "Back pressed after challenge solved")
                                    stopAlarm()
                                    releaseWakeLock()
                                    finish()
                                }
                            })
                        }
                    )
                }
            }
        }
    }
    
    private fun stopAlarm() {
        try {
            // Stop the alarm service
            val serviceIntent = Intent(this, AlarmService::class.java)
            stopService(serviceIntent)
            
            // Stop local audio manager
            audioManager?.stopAudio()
            
            Log.d("AlarmRingingActivity", "Alarm stopped successfully")
        } catch (e: Exception) {
            Log.e("AlarmRingingActivity", "Failed to stop alarm", e)
        }
    }
    
    private fun releaseWakeLock() {
        wakeLock?.let {
            if (it.isHeld) {
                it.release()
            }
        }
        wakeLock = null
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d("AlarmRingingActivity", "Activity destroyed")
        stopAlarm()
        releaseWakeLock()
    }
}
