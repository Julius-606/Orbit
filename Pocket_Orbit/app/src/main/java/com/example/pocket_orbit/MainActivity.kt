// ==========================================
// IDENTITY: The Front Door / MainActivity
// FILEPATH: app/src/main/java/com/example/pocket_orbit/MainActivity.kt
// COMPONENT: Android Entry Point
// VIBE: "Wake up Neo. The matrix has you." 💊
// ==========================================

package com.example.pocket_orbit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.pocket_orbit.ui.theme.OrbitTheme
import com.example.pocket_orbit.ui.screens.DashboardScreen
import com.example.pocket_orbit.network.BlastClient

class MainActivity : ComponentActivity() {

    private lateinit var blastClient: BlastClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 10.0.2.2 is the magic IP for Android Emulator to hit localhost
        blastClient = BlastClient("ws://102.215.33.12:8000/ws/blast")
        blastClient.connect()

        setContent {
            OrbitTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DashboardScreen()
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        blastClient.disconnect()
    }
}