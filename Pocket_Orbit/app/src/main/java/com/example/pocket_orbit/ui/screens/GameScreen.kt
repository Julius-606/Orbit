// ================================================================================
// FILE: Pocket_Orbit/app/src/main/java/com/example/pocket_orbit/ui/screens/GameScreen.kt
// VERSION: 4.1.1 | SYSTEM: Orbit (The Life-OS Protocol)
// IDENTITY: The Rec Room / Compose Mini-Game
// ================================================================================

package com.example.pocket_orbit.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive

@Composable
fun GameScreen() {
    var isPlaying by remember { mutableStateOf(false) }
    var score by remember { mutableStateOf(0) }
    var playerY by remember { mutableStateOf(0f) }
    var playerVelocity by remember { mutableStateOf(0f) }
    var obstacleX by remember { mutableStateOf(1000f) }
    var gameOver by remember { mutableStateOf(false) }

    val gravity = 2f
    val jumpStrength = -30f
    val groundY = 0f

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isActive) {
                delay(16)

                playerVelocity += gravity
                playerY += playerVelocity

                if (playerY > groundY) {
                    playerY = groundY
                    playerVelocity = 0f
                }

                obstacleX -= 20f + (score * 0.5f)

                if (obstacleX < -100f) {
                    obstacleX = 1000f
                    score += 1
                }

                if (obstacleX in 120f..280f && playerY > -100f) {
                    gameOver = true
                    isPlaying = false
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .clickable {
                if (gameOver) {
                    gameOver = false
                    score = 0
                    obstacleX = 1000f
                    playerY = 0f
                    isPlaying = true
                } else if (!isPlaying) {
                    isPlaying = true
                } else {
                    if (playerY == groundY) {
                        playerVelocity = jumpStrength
                    }
                }
            },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "Dodge the Margin Call \uD83D\uDCC9",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "Score: $score",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )

        if (gameOver) {
            Text(
                text = "ACCOUNT BLOWN! Tap to restart. \uD83D\uDC80",
                color = Color.Red,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(top = 16.dp)
            )
        } else if (!isPlaying) {
            Text(
                text = "Tap anywhere to jump!",
                color = Color.Gray,
                modifier = Modifier.padding(top = 16.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color(0xFF1E1E1E))
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            drawLine(
                color = Color.White,
                start = Offset(0f, canvasHeight - 50f),
                end = Offset(canvasWidth, canvasHeight - 50f),
                strokeWidth = 5f
            )

            drawCircle(
                color = Color(0xFF00E676),
                radius = 40f,
                center = Offset(200f, canvasHeight - 90f + playerY)
            )

            drawRect(
                color = Color(0xFFFF1744),
                topLeft = Offset(obstacleX, canvasHeight - 150f),
                size = Size(60f, 100f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}