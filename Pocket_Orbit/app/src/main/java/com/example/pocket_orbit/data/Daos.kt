// ==========================================
// IDENTITY: The Guards / Room DB DAOs
// FILEPATH: app/src/main/java/com/example/pocket_orbit/data/Daos.kt
// VERSION: 1.0.1
// ==========================================

package com.example.pocket_orbit.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface StudyTaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<StudyTaskEntity>)

    @Query("SELECT * FROM study_tasks WHERE isCompleted = 0")
    fun getPendingTasks(): Flow<List<StudyTaskEntity>>

    @Query("DELETE FROM study_tasks")
    suspend fun clearTasks()

    // 🔥 THE FIX: SQL query to actually check off the task in local memory
    @Query("UPDATE study_tasks SET isCompleted = 1 WHERE id = :taskId")
    suspend fun markTaskCompleted(taskId: Int)
}

@Dao
interface ForexLogDao {
    @Insert
    suspend fun insertLog(log: ForexLogEntity)

    @Query("SELECT * FROM forex_logs ORDER BY timestamp DESC LIMIT 10")
    fun getRecentLogs(): Flow<List<ForexLogEntity>>
}