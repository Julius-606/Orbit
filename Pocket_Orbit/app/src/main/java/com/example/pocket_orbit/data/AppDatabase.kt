// ==========================================
// IDENTITY: The Pocket Vault / Android Room DB
// FILEPATH: app/src/main/java/com/example/pocket_orbit/data/AppDatabase.kt
// VIBE: All imports included. Hit "Sync Now" in Gradle and watch the red lines disappear! 📉->📈
// ==========================================

package com.example.pocket_orbit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [StudyTaskEntity::class, ForexLogEntity::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studyTaskDao(): StudyTaskDao
    abstract fun forexLogDao(): ForexLogDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pocket_orbit_db"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}