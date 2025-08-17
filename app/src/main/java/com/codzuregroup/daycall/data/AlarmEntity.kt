package com.codzuregroup.daycall.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import com.codzuregroup.daycall.ui.challenges.ChallengeType
import java.time.LocalTime

@Entity(tableName = "alarms")
data class AlarmEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "hour") val hour: Int,
    @ColumnInfo(name = "minute") val minute: Int,
    /** Repeat days represented as bitmask: 0bMonTueWedThuFriSatSun starting from Monday bit0 */
    @ColumnInfo(name = "repeat_days") val repeatDays: Int = 0,
    @ColumnInfo(name = "label") val label: String? = null,
    @ColumnInfo(name = "sound") val sound: String = "Default",
    @ColumnInfo(name = "audio_file") val audioFile: String? = null,
    @ColumnInfo(name = "challenge_type") val challengeType: String = "MATH",
    @ColumnInfo(name = "vibe") val vibe: String = "chill",
    @ColumnInfo(name = "enabled") val enabled: Boolean = true
) {
    fun toLocalTime(): LocalTime {
        // Handle edge case where minute might be 60 (should be 0 of next hour)
        val validMinute = if (minute >= 60) 0 else minute
        val validHour = if (minute >= 60) (hour + 1) % 24 else hour
        return LocalTime.of(validHour, validMinute)
    }
    
    fun getChallengeTypeEnum(): ChallengeType {
        return try {
            ChallengeType.valueOf(challengeType)
        } catch (e: IllegalArgumentException) {
            ChallengeType.MATH // Default fallback
        }
    }
    
    companion object {
        fun create(
            hour: Int,
            minute: Int,
            repeatDays: Int = 0,
            label: String? = null,
            sound: String = "Default",
            audioFile: String? = null,
            challengeType: String = "MATH",
            vibe: String = "chill",
            enabled: Boolean = true
        ): AlarmEntity {
            // Validate and normalize time values
            val normalizedMinute = minute.coerceIn(0, 59)
            val normalizedHour = hour.coerceIn(0, 23)
            
            return AlarmEntity(
                hour = normalizedHour,
                minute = normalizedMinute,
                repeatDays = repeatDays,
                label = label,
                sound = sound,
                audioFile = audioFile,
                challengeType = challengeType,
                vibe = vibe,
                enabled = enabled
            )
        }
    }
} 