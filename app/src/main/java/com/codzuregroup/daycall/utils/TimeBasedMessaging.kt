package com.codzuregroup.daycall.utils

import java.time.LocalTime
import java.time.format.DateTimeFormatter

/**
 * Utility class for dynamic time-based messaging and greetings
 */
object TimeBasedMessaging {
    
    /**
     * Get appropriate greeting based on current time with more nuanced timing
     */
    fun getGreeting(currentTime: LocalTime = LocalTime.now()): String {
        val hour = currentTime.hour
        return when (hour) {
            in 4..6 -> "Rise and shine"      // Early morning (4-6 AM)
            in 7..11 -> "Good morning"       // Morning (7-11 AM)
            in 12..13 -> "Good afternoon"    // Early afternoon (12-1 PM)
            in 14..17 -> "Good afternoon"    // Afternoon (2-5 PM)
            in 18..20 -> "Good evening"      // Evening (6-8 PM)
            in 21..23 -> "Good night"        // Night (9-11 PM)
            else -> "Hello there"            // Late night/very early (12-3 AM)
        }
    }
    
    /**
     * Get motivational message based on time of day
     */
    fun getMotivationalMessage(currentTime: LocalTime = LocalTime.now()): String {
        val hour = currentTime.hour
        return when (hour) {
            in 4..6 -> "Time to conquer the day!"
            in 7..9 -> "Let's make today amazing!"
            in 10..11 -> "You're doing great so far!"
            in 12..13 -> "Keep up the momentum!"
            in 14..16 -> "Afternoon energy boost!"
            in 17..18 -> "Finishing strong today!"
            in 19..20 -> "Winding down nicely!"
            in 21..23 -> "Rest well, you've earned it!"
            else -> "Every moment counts!"
        }
    }
    
    /**
     * Get celebration message with dynamic timing
     */
    fun getCelebrationMessage(userName: String, currentTime: LocalTime = LocalTime.now()): String {
        val greeting = getGreeting(currentTime)
        val hour = currentTime.hour
        
        val timeSpecificMessage = when (hour) {
            in 4..6 -> "What an early achiever!"
            in 7..9 -> "Starting the day right!"
            in 10..11 -> "Morning productivity at its best!"
            in 12..13 -> "Midday success!"
            in 14..16 -> "Afternoon accomplishment!"
            in 17..18 -> "Evening excellence!"
            in 19..20 -> "Night owl achievement!"
            in 21..23 -> "Late night dedication!"
            else -> "Dedication knows no time!"
        }
        
        return if (userName.isNotBlank()) {
            "$greeting, $userName! $timeSpecificMessage Congratulations on completing the challenge!"
        } else {
            "$greeting! $timeSpecificMessage Congratulations on completing the challenge!"
        }
    }
    
    /**
     * Get productivity message based on time
     */
    fun getProductivityMessage(currentTime: LocalTime = LocalTime.now()): String {
        val hour = currentTime.hour
        return when (hour) {
            in 4..6 -> "Early bird catches the worm! 🌅"
            in 7..9 -> "Perfect time for focused work! ☀️"
            in 10..11 -> "Peak morning productivity! 💪"
            in 12..13 -> "Midday momentum! 🚀"
            in 14..16 -> "Afternoon focus time! 🎯"
            in 17..18 -> "Evening wrap-up session! 📝"
            in 19..20 -> "Peaceful evening planning! 🌙"
            in 21..23 -> "Night owl productivity! 🦉"
            else -> "Quiet hours, deep focus! ✨"
        }
    }
    
    /**
     * Get time-appropriate emoji
     */
    fun getTimeEmoji(currentTime: LocalTime = LocalTime.now()): String {
        val hour = currentTime.hour
        return when (hour) {
            in 4..6 -> "🌅"
            in 7..11 -> "☀️"
            in 12..13 -> "🌞"
            in 14..17 -> "🌤️"
            in 18..20 -> "🌇"
            in 21..23 -> "🌙"
            else -> "✨"
        }
    }
    
    /**
     * Format time in a user-friendly way
     */
    fun formatTimeForDisplay(time: LocalTime): String {
        return time.format(DateTimeFormatter.ofPattern("h:mm a"))
    }
    
    /**
     * Get time-based theme suggestion
     */
    fun getThemeSuggestion(currentTime: LocalTime = LocalTime.now()): String {
        val hour = currentTime.hour
        return when (hour) {
            in 4..6 -> "sunrise"
            in 7..11 -> "morning"
            in 12..17 -> "day"
            in 18..20 -> "sunset"
            in 21..23 -> "evening"
            else -> "night"
        }
    }
}