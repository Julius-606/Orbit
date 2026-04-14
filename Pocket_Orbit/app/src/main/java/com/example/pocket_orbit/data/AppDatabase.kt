// ==========================================
// IDENTITY: The Pocket Vault / Android Room DB
// FILEPATH: app/src/main/java/com/example/pocket_orbit/data/AppDatabase.kt
// VERSION: 1.1.0
// VIBE: Chat history and tasks now fully persistent. 🧠
// ==========================================

package com.example.pocket_orbit.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        StudyTaskEntity::class, 
        ForexLogEntity::class, 
        ChatMessageEntity::class
    ],
    version = 2, // Bumped version for new schema
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studyTaskDao(): StudyTaskDao
    abstract fun forexLogDao(): ForexLogDao
    abstract fun chatDao(): ChatDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pocket_orbit_db"
                )
                .fallbackToDestructiveMigration() // Simple for dev, handles schema changes
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}