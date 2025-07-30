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
    @ColumnInfo(name = "enabled") val enabled: Boolean = true
) {
    fun toLocalTime(): LocalTime = LocalTime.of(hour, minute)
    
    fun getChallengeTypeEnum(): ChallengeType {
        return try {
            ChallengeType.valueOf(challengeType)
        } catch (e: IllegalArgumentException) {
            ChallengeType.MATH // Default fallback
        }
    }
} 