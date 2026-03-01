// ==========================================
// IDENTITY: The HUD / Dashboard Screen
// FILEPATH: app/src/main/java/com/example/pocket_orbit/ui/screens/DashboardScreen.kt
// COMPONENT: Jetpack Compose UI
// VIBE: Flexing on everyone else's basic to-do list apps. 💅
// ==========================================

package com.example.pocket_orbit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun DashboardScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- HEADER ---
        Text(
            text = "🪐 ORBIT v3.0",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(top = 32.dp, bottom = 24.dp)
        )

        // --- FOREX GUARDIAN CARD ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "📈 Forex Guardian", fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Status: Monitoring XAUUSD", color = Color.Gray)
                Text(text = "Risk Level: Chilling (0.5% Exposure)", color = MaterialTheme.colorScheme.primary)
            }
        }

        // --- MED-SCHOLAR CARD ---
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "🩺 Med-Scholar", fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = "Pending: Internal Medicine (Cardiology)", color = Color.Gray)
                Text(text = "Brain Rot Level: MID", color = MaterialTheme.colorScheme.tertiary)
            }
        }

        // --- CATE / VIBE CHECK ---
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF222222))
                .padding(16.dp)
        ) {
            Column {
                Text(text = "🤖 CATE Vibe Check:", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Bro, it's almost 2 AM. The markets are dead. Go to sleep before you revenge trade.",
                    color = Color.White,
                    fontSize = 14.sp
                )
            }
        }
    }
}