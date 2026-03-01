// ==========================================
// IDENTITY: The Brain / Orbit Repository
// FILEPATH: app/src/main/java/com/example/pocket_orbit/data/OrbitRepository.kt
// COMPONENT: Android Data Management
// ROLE: Decides whether to show you data from the local Room DB or fetch fresh data from the VM.
// VIBE: The smartest guy in the room coordinating between offline and online modes. 🧠
// ==========================================

package com.example.pocket_orbit.data

import android.util.Log
import com.example.pocket_orbit.network.ApiService
import kotlinx.coroutines.flow.Flow

class OrbitRepository(
    private val studyTaskDao: StudyTaskDao,
    private val apiService: ApiService,
    private val secretToken: String // Your SECRET_KEY from the .env
) {
    // The UI observes this. It instantly updates if the local database changes.
    val pendingTasks: Flow<List<StudyTaskEntity>> = studyTaskDao.getPendingTasks()

    suspend fun refreshTasksFromVM() {
        try {
            // Attempt to fetch fresh data from the FastAPI backend
            val response = apiService.getPendingTasks("Bearer $secretToken")
            
            if (response.isSuccessful && response.body() != null) {
                // If successful, wipe the old offline data and save the new data
                val newTasks = response.body()!!
                studyTaskDao.clearTasks()
                studyTaskDao.insertTasks(newTasks)
                Log.d("OrbitRepo", "Sync successful. Fresh tasks secured. 🎯")
            } else {
                Log.e("OrbitRepo", "VM rejected us. Opps activity? Code: ${response.code()}")
            }
        } catch (e: Exception) {
            // If the VM is offline or you have no bundles, it's chill.
            // We just rely on the existing local Room DB data.
            Log.w("OrbitRepo", "Network error. Relying on offline vault. Safaricom acting up again? 📵")
        }
    }
}