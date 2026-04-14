// ================================================================================
// FILE: Pocket_Orbit/app/src/main/java/com/example/pocket_orbit/MainActivity.kt
// VERSION: 4.4.1 | SYSTEM: Orbit (The Life-OS Protocol)
// IDENTITY: The Anchor / Main Entry Point & Dependency Injection
// VIBE: Background sync scheduled. Orbit is always on high alert. 🚨
// ================================================================================

package com.example.pocket_orbit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.pocket_orbit.data.AppDatabase
import com.example.pocket_orbit.data.OrbitRepository
import com.example.pocket_orbit.network.RetrofitClient
import com.example.pocket_orbit.network.SyncWorker
import com.example.pocket_orbit.ui.navigation.BottomNavItem
import com.example.pocket_orbit.ui.screens.ChatScreen
import com.example.pocket_orbit.ui.screens.ChatViewModel
import com.example.pocket_orbit.ui.screens.DashboardViewModel
import com.example.pocket_orbit.ui.screens.GameScreen
import com.example.pocket_orbit.ui.screens.TrackerScreen
import com.example.pocket_orbit.ui.theme.OrbitTheme
import java.util.concurrent.TimeUnit

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 🔥 Orbit High Alert: Scheduling the background sync
        scheduleSync(this)

        setContent {
            OrbitTheme {
                val navController = rememberNavController()
                val context = LocalContext.current

                val database = remember { AppDatabase.getDatabase(context) }
                val apiService = remember { RetrofitClient.apiService }

                val repository = remember {
                    OrbitRepository(
                        database.studyTaskDao(),
                        apiService,
                        "3ATLNDwN6SfiTQfyfEjxQpxsRtj_6dzR8QzKxpXeZn8Nn76n4"
                    )
                }

                val dashboardViewModel = remember { DashboardViewModel(repository) }
                val chatViewModel = remember { 
                    ChatViewModel(
                        apiService = apiService,
                        chatDao = database.chatDao()
                    ) 
                }

                Scaffold(
                    bottomBar = { OrbitBottomNav(navController = navController) },
                    containerColor = MaterialTheme.colorScheme.background
                ) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = BottomNavItem.Assistant.route,
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable(BottomNavItem.Assistant.route) {
                            ChatScreen(viewModel = chatViewModel)
                        }
                        composable(BottomNavItem.Tracker.route) {
                            TrackerScreen(viewModel = dashboardViewModel)
                        }
                        composable(BottomNavItem.Chill.route) {
                            GameScreen()
                        }
                    }
                }
            }
        }
    }

    private fun scheduleSync(context: android.content.Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncRequest = PeriodicWorkRequestBuilder<SyncWorker>(15, TimeUnit.MINUTES)
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            "OrbitSync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncRequest
        )
    }
}

@Composable
fun OrbitBottomNav(navController: NavHostController) {
    val items = listOf(
        BottomNavItem.Assistant,
        BottomNavItem.Tracker,
        BottomNavItem.Chill
    )

    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        items.forEach { item ->
            NavigationBarItem(
                icon = { Icon(item.icon, contentDescription = item.title) },
                label = { Text(item.title) },
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}