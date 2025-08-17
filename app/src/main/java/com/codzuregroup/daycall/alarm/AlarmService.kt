package com.codzuregroup.daycall.alarm

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.codzuregroup.daycall.R
import com.codzuregroup.daycall.audio.AudioManager
import com.codzuregroup.daycall.vibration.VibrationManager

class AlarmService : Service() {
    private lateinit var audioManager: AudioManager
    private lateinit var vibrationManager: VibrationManager
    private var isPlaying = false
    private var wakeLock: android.os.PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        createChannel()
        audioManager = AudioManager(this)
        vibrationManager = VibrationManager(this)
        
        // Acquire wake lock to keep device awake during alarm
        val powerManager = getSystemService(Context.POWER_SERVICE) as android.os.PowerManager
        wakeLock = powerManager.newWakeLock(
            android.os.PowerManager.PARTIAL_WAKE_LOCK,
            "DayCall:AlarmServiceWakeLock"
        )
        wakeLock?.acquire(20 * 60 * 1000L) // 20 minutes timeout
        
        Log.d("AlarmService", "Service created with wake lock")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("AlarmService", "onStartCommand called")
        
        val soundFileName = intent?.getStringExtra("sound_file") ?: "Ascent Braam"
        val vibeCategory = intent?.getStringExtra("vibe_category") ?: "default"
        val alarmTime = intent?.getStringExtra("alarm_time")
        
        // Store alarm details for retrieval
        val alarmId = intent?.getLongExtra("ALARM_ID", -1) ?: -1
        val alarmLabel = intent?.getStringExtra("ALARM_LABEL") ?: "Alarm"
        val challengeType = intent?.getStringExtra("CHALLENGE_TYPE") ?: "MATH"
        val vibe = intent?.getStringExtra("VIBE") ?: "default"
        
        // Store in companion object
        currentAlarmId = alarmId
        currentAlarmLabel = alarmLabel
        currentAlarmSound = soundFileName
        currentAlarmChallengeType = challengeType
        currentAlarmVibe = vibe
        isAlarmActive = true
        
        Log.d("AlarmService", "Starting alarm with sound: $soundFileName, vibe: $vibeCategory")
        
        try {
            // Start playing the alarm sound
            startAlarmSound(soundFileName, vibeCategory)
            
            // Create and show notification with alarm details
            val notification = createNotification(soundFileName, alarmId, alarmLabel, soundFileName, challengeType, vibe)
            startForeground(ALARM_NOTIFICATION_ID, notification)
            
            Log.d("AlarmService", "Alarm service started successfully")
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to start alarm service", e)
        }
        
        return START_NOT_STICKY
    }
    
    private fun startAlarmSound(soundFileName: String, vibeCategory: String) {
        try {
            // Stop any existing audio
            audioManager.stopAudio()
            
            // Start playing the alarm sound in a loop
            audioManager.playAudio(soundFileName, loop = true)
            isPlaying = true
            
            // Start vibration pattern based on vibe
            startVibration(vibeCategory)
            
            Log.d("AlarmService", "Started alarm sound: $soundFileName")
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to start alarm sound", e)
        }
    }
    
    private fun startVibration(vibeCategory: String) {
        try {
            when (vibeCategory.lowercase()) {
                "chill" -> vibrationManager.vibrateButtonPress()
                "energetic" -> vibrationManager.vibrateButtonPress()
                "focused" -> vibrationManager.vibrateButtonPress()
                "relaxed" -> vibrationManager.vibrateButtonPress()
                else -> vibrationManager.vibrateButtonPress()
            }
            Log.d("AlarmService", "Started vibration for vibe: $vibeCategory")
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to start vibration", e)
        }
    }
    
    private fun createNotification(
        soundFileName: String,
        alarmId: Long,
        alarmLabel: String,
        sound: String,
        challengeType: String,
        vibe: String
    ): Notification {
        // Create intent to launch AlarmRingingActivity when notification is clicked
        val alarmIntent = Intent(this, AlarmRingingActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            // Pass the alarm details
            putExtra("ALARM_ID", alarmId)
            putExtra("ALARM_LABEL", alarmLabel)
            putExtra("SOUND", sound)
            putExtra("CHALLENGE_TYPE", challengeType)
            putExtra("VIBE", vibe)
            // Add action to distinguish from other intents
            action = "ALARM_NOTIFICATION_CLICK"
        }
        
        val pendingIntent = android.app.PendingIntent.getActivity(
            this,
            ALARM_NOTIFICATION_ID,
            alarmIntent,
            android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
        )
        
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Day Call Alarm")
            .setContentText("Tap to solve challenge and stop alarm")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
            .setFullScreenIntent(pendingIntent, true) // Show as heads-up notification
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setVibrate(longArrayOf(0, 500, 200, 500)) // Custom vibration pattern
            .setLights(0xFF0000FF.toInt(), 1000, 1000) // Blue light
            .build()
    }
    
    fun stopAlarm() {
        try {
            audioManager.stopAudio()
            vibrationManager.stopVibration()
            isPlaying = false
            
            // Clear stored alarm details
            isAlarmActive = false
            currentAlarmId = -1
            currentAlarmLabel = ""
            currentAlarmSound = ""
            currentAlarmChallengeType = ""
            currentAlarmVibe = ""
            
            Log.d("AlarmService", "Alarm stopped")
        } catch (e: Exception) {
            Log.e("AlarmService", "Failed to stop alarm", e)
        }
    }

    override fun onDestroy() {
        Log.d("AlarmService", "Service destroyed")
        stopAlarm()
        
        // Release wake lock
        wakeLock?.let { wl ->
            if (wl.isHeld) {
                wl.release()
                Log.d("AlarmService", "Wake lock released")
            }
        }
        
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Day Call Alarms",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alarm notifications for Day Call app"
                enableLights(true)
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
            Log.d("AlarmService", "Notification channel created")
        }
    }

    companion object {
        private const val CHANNEL_ID = "day_call_alarm_channel"
        private const val ALARM_NOTIFICATION_ID = 1001
        
        // Store current alarm details for retrieval
        @Volatile
        private var currentAlarmId: Long = -1
        @Volatile
        private var currentAlarmLabel: String = ""
        @Volatile
        private var currentAlarmSound: String = ""
        @Volatile
        private var currentAlarmChallengeType: String = ""
        @Volatile
        private var currentAlarmVibe: String = ""
        @Volatile
        private var isAlarmActive: Boolean = false
        
        fun getCurrentAlarmDetails(): Triple<Long, String, String>? {
            return if (isAlarmActive) {
                Triple(currentAlarmId, currentAlarmLabel, currentAlarmSound)
            } else {
                null
            }
        }
        
        fun isAlarmRunning(): Boolean = isAlarmActive
    }
} 