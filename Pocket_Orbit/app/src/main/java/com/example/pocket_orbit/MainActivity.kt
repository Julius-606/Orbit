// ================================================================================
// FILE: Pocket_Orbit/app/src/main/java/com/example/pocket_orbit/MainActivity.kt
// VERSION: 4.2.0 | SYSTEM: Orbit (The Life-OS Protocol)
// IDENTITY: The Anchor / Main Entry Point & Navigation
// VIBE: 28 errors wiped out like a bad trade setup. Navigation is locked in. 🎯
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

// 🔥 Every single navigation import explicitly defined so Android Studio stops panicking
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

import com.example.pocket_orbit.data.AppDatabase
import com.example.pocket_orbit.data.OrbitRepository
import com.example.pocket_orbit.network.RetrofitClient
import com.example.pocket_orbit.ui.navigation.BottomNavItem
import com.example.pocket_orbit.ui.screens.ChatScreen
import com.example.pocket_orbit.ui.screens.ChatViewModel
import com.example.pocket_orbit.ui.screens.DashboardViewModel
import com.example.pocket_orbit.ui.screens.GameScreen
import com.example.pocket_orbit.ui.screens.TrackerScreen
import com.example.pocket_orbit.ui.theme.OrbitTheme // Bringing in your custom drip 🦇

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // Dark Mode only.
            OrbitTheme {
                val navController = rememberNavController()
                val context = LocalContext.current

                // Manual Dependency Injection (Securing the bag)
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
                val chatViewModel = remember { ChatViewModel(apiService) }

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
                        // 🔥 The bulletproof way to pop back stack without using deprecated code
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