package com.codzuregroup.daycall.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.codzuregroup.daycall.ui.challenges.ChallengeType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

@Database(
    entities = [
        AlarmEntity::class, 
        UserEntity::class, 
        TodoEntity::class,
        UserStatsEntity::class,
        AchievementEntity::class,
        TaskTemplateEntity::class
    ],
    version = 7,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class DayCallDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    abstract fun userDao(): UserDao
    abstract fun todoDao(): TodoDao
    abstract fun userStatsDao(): UserStatsDao
    abstract fun achievementDao(): AchievementDao
    abstract fun taskTemplateDao(): TaskTemplateDao

    companion object {
        @Volatile
        private var INSTANCE: DayCallDatabase? = null

        fun getInstance(context: Context): DayCallDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    DayCallDatabase::class.java,
                    "daycall.db"
                )
                .fallbackToDestructiveMigration()
                    .addCallback(object : Callback() {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            super.onCreate(db)
                            // Pre-populate with sample data
                            val scope = CoroutineScope(Dispatchers.IO)
                            scope.launch {
                                val dao = getInstance(context).alarmDao()
                                insertSampleAlarms(dao)
                            }
                        }
                    })
                    .build()
                INSTANCE = instance
                instance
            }
        }
        private suspend fun insertSampleAlarms(dao: AlarmDao) {
            // No sample alarms - start with empty database
            // Users will create their own alarms
            Log.d("DayCallDatabase", "Database created successfully - ready for user alarms")
        }
        
        fun clearDatabase(context: Context) {
            context.deleteDatabase("daycall.db")
            INSTANCE = null
        }
    }
} 