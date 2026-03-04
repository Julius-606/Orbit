// ==========================================
// IDENTITY: The Front Door / MainActivity
// FILEPATH: app/src/main/java/com/example/pocket_orbit/MainActivity.kt
// COMPONENT: Android Entry Point
// VERSION: 1.1.0
// VIBE: "Wake up Neo. We are wiring the UI to the Matrix." 🔌
// ==========================================

package com.example.pocket_orbit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.pocket_orbit.data.AppDatabase
import com.example.pocket_orbit.data.OrbitRepository
import com.example.pocket_orbit.network.BlastClient
import com.example.pocket_orbit.network.RetrofitClient
import com.example.pocket_orbit.ui.screens.DashboardScreen
import com.example.pocket_orbit.ui.screens.DashboardViewModel
import com.example.pocket_orbit.ui.theme.OrbitTheme

class MainActivity : ComponentActivity() {

    private lateinit var blastClient: BlastClient

    // 🧠 Creating the ViewModel and injecting the Repository (The Translator)
    private val viewModel: DashboardViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val database = AppDatabase.getDatabase(applicationContext)
                val repository = OrbitRepository(
                    studyTaskDao = database.studyTaskDao(),
                    apiService = RetrofitClient.apiService,
                    secretToken = "3ATLNDwN6SfiTQfyfEjxQpxsRtj_6dzR8QzKxpXeZn8Nn76n4" // Must match VM .env!
                )
                return DashboardViewModel(repository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // To your Ngrok WebSocket address (Note the 'wss' for secure websockets):
        blastClient = BlastClient("wss://untropic-rozanne-noncomprehendingly.ngrok-free.dev/ws/blast")
        blastClient.connect()

        setContent {
            OrbitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // Injecting the Brain into the UI right here 👇
                    DashboardScreen(viewModel = viewModel)
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        blastClient.disconnect()
    }
}