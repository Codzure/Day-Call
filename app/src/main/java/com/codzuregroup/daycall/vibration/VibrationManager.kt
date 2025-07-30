package com.codzuregroup.daycall.vibration

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

class VibrationManager(private val context: Context) {
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        vibratorManager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }
    
    private val scope = CoroutineScope(Dispatchers.Main)
    
    fun vibrateCorrectAnswer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(200, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(200)
        }
    }
    
    fun vibrateIncorrectAnswer() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val pattern = longArrayOf(0, 100, 50, 100, 50, 100)
            val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
            val effect = VibrationEffect.createWaveform(pattern, amplitudes, -1)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            val pattern = longArrayOf(0, 100, 50, 100, 50, 100)
            vibrator.vibrate(pattern, -1)
        }
    }
    
    fun vibrateAlarmUrgency(intensity: Float = 0.7f) {
        scope.launch {
            val amplitude = (intensity * 255).toInt()
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Urgency pattern: strong pulses with increasing frequency
                val pattern = longArrayOf(0, 300, 200, 300, 200, 300, 200, 300, 200, 300)
                val amplitudes = IntArray(pattern.size) { if (it % 2 == 1) amplitude else 0 }
                val effect = VibrationEffect.createWaveform(pattern, amplitudes, -1)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0, 300, 200, 300, 200, 300, 200, 300, 200, 300)
                vibrator.vibrate(pattern, -1)
            }
        }
    }
    
    fun vibrateAlarmStart() {
        scope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Wake-up pattern: gentle start, then strong
                val pattern = longArrayOf(0, 100, 50, 200, 50, 300, 50, 400)
                val amplitudes = intArrayOf(0, 128, 0, 255, 0, 255, 0, 255)
                val effect = VibrationEffect.createWaveform(pattern, amplitudes, -1)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0, 100, 50, 200, 50, 300, 50, 400)
                vibrator.vibrate(pattern, -1)
            }
        }
    }
    
    fun vibrateVolumeIncrease() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(100)
        }
    }
    
    fun vibrateChallengeTimeout() {
        scope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                // Warning pattern: three strong pulses
                val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
                val amplitudes = intArrayOf(0, 255, 0, 255, 0, 255)
                val effect = VibrationEffect.createWaveform(pattern, amplitudes, -1)
                vibrator.vibrate(effect)
            } else {
                @Suppress("DEPRECATION")
                val pattern = longArrayOf(0, 500, 200, 500, 200, 500)
                vibrator.vibrate(pattern, -1)
            }
        }
    }
    
    fun vibrateButtonPress() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val effect = VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE)
            vibrator.vibrate(effect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(50)
        }
    }
    
    fun stopVibration() {
        vibrator.cancel()
    }
    
    fun isVibrationSupported(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.hasVibrator()
        } else {
            @Suppress("DEPRECATION")
            vibrator.hasVibrator()
        }
    }
} 