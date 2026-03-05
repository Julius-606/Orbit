// ================================================================================
// FILE: Pocket_Orbit/app/src/main/java/com/example/pocket_orbit/ui/navigation/OrbitDestinations.kt
// VERSION: 4.1.1 | SYSTEM: Orbit (The Life-OS Protocol)
// IDENTITY: The Map / Navigation Routes
// ================================================================================

package com.example.pocket_orbit.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.ui.graphics.vector.ImageVector

sealed class BottomNavItem(val route: String, val title: String, val icon: ImageVector) {
    object Assistant : BottomNavItem("assistant", "Orbit AI", Icons.Default.Face)
    object Tracker : BottomNavItem("tracker", "Tracker", Icons.Default.CheckCircle)
    object Chill : BottomNavItem("chill", "Chill", Icons.Default.PlayArrow)
}
