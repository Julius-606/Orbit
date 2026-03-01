// ==========================================
// IDENTITY: The Guards / Room DB DAOs
// FILEPATH: app/src/main/java/com/example/pocket_orbit/data/Daos.kt
// COMPONENT: Android Offline Database
// ROLE: The SQL queries for your phone. How we read/write the data.
// VIBE: Fetching data faster than a high-frequency trading bot. ⚡
// ==========================================

package com.example.pocket_orbit.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyTaskDao {
    // If a task already exists, just replace it (upsert)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<StudyTaskEntity>)

    // Flow emits real-time updates to the UI when the DB changes!
    @Query("SELECT * FROM study_tasks WHERE isCompleted = 0")
    fun getPendingTasks(): Flow<List<StudyTaskEntity>>

    @Query("DELETE FROM study_tasks")
    suspend fun clearTasks()
}

@Dao
interface ForexLogDao {
    @Insert
    suspend fun insertLog(log: ForexLogEntity)

    // Get the last 10 trades so you can review your bad decisions 💀
    @Query("SELECT * FROM forex_logs ORDER BY timestamp DESC LIMIT 10")
    fun getRecentLogs(): Flow<List<ForexLogEntity>>
}