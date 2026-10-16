// ================================================================================
// FILE: Pocket_Orbit/app/src/main/java/com/example/pocket_orbit/ui/navigation/OrbitDestinations.kt
// VERSION: 5.0.0 | SYSTEM: Orbit (The Life-OS Protocol)
// IDENTITY: The Map / Navigation Routes
// ================================================================================

package com.example.pocket_orbit.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Dashboard : BottomNavItem("dashboard", "Dashboard", Icons.Default.Dashboard)
    object Tracker : BottomNavItem("tracker", "Tasks", Icons.Default.Checklist)
    object Terminal : BottomNavItem("terminal", "Terminal", Icons.Default.Terminal)
    object Assistant : BottomNavItem("assistant", "Orbit AI", Icons.Default.Face)
    object Chill : BottomNavItem("chill", "Chill", Icons.Default.SportsEsports)
}