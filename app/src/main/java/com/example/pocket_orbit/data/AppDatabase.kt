// ==========================================
// IDENTITY: The Pocket Vault / Android Room DB
// FILEPATH: app/src/main/java/com/example/pocket_orbit/data/AppDatabase.kt
// VERSION: 2.0.0 | SYSTEM: Added Chat Session Entity support
// VIBE: Full persistent multi-session chat storage framework. 🧠🗄️
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
        ChatMessageEntity::class,
        ChatSessionEntity::class,
        TerminalSessionEntity::class,
        TerminalLogEntryEntity::class,
        TerminalPilotChatEntity::class,
        TerminalCommandVaultEntity::class
    ],
    version = 4, // Bumped for VS Code Terminal session persistence framework
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun studyTaskDao(): StudyTaskDao
    abstract fun forexLogDao(): ForexLogDao
    abstract fun chatDao(): ChatDao
    abstract fun terminalDao(): TerminalDao

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
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
