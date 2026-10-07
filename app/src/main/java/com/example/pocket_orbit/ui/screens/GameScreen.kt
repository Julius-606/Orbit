// ================================================================================
// FILE: app/src/main/java/com/example/pocket_orbit/ui/screens/GameScreen.kt
// VERSION: 5.0.0 | SYSTEM: Orbit Chill Zone - Cyberpunk Space Scalper
// IDENTITY: Retro synthwave arcade runner featuring candlestick navigation and pip collecting.
// ================================================================================

package com.example.pocket_orbit.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.sin
import kotlin.random.Random

// Cyberpunk Palette
private val SpaceBgDark = Color(0xFF0A0E17)
private val CyberBlue = Color(0xFF00F0FF)
private val NeonPink = Color(0xFFFF007F)
private val PipGold = Color(0xFFFFD700)
private val BullishGreen = Color(0xFF00E676)
private val BearishRed = Color(0xFFFF1744)

// Candlestick Obstacle Model
data class CandleObstacle(
    var x: Float,
    val topHeight: Float,
    val gap: Float = 280f,
    val isBullish: Boolean = false,
    var passed: Boolean = false,
    var hasCoin: Boolean = true,
    var coinCollected: Boolean = false
)

// Background Star Particle
data class StarParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float
)

@Composable
fun GameScreen() {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("orbit_game_prefs", Context.MODE_PRIVATE) }
    var highScore by remember { mutableIntStateOf(sharedPrefs.getInt("high_score", 0)) }

    // Game state
    var isPlaying by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var pipsCollected by remember { mutableIntStateOf(0) }
    var comboMultiplier by remember { mutableIntStateOf(1) }

    // Drone Physics
    var droneY by remember { mutableFloatStateOf(400f) }
    var droneVelocity by remember { mutableFloatStateOf(0f) }
    var engineThrust by remember { mutableStateOf(false) }

    // Obstacles & Stars
    val obstacles = remember { mutableStateListOf<CandleObstacle>() }
    val stars = remember {
        mutableStateListOf<StarParticle>().apply {
            repeat(35) {
                add(
                    StarParticle(
                        x = Random.nextFloat() * 1080f,
                        y = Random.nextFloat() * 1920f,
                        size = Random.nextFloat() * 2.5f + 1f,
                        speed = Random.nextFloat() * 1.5f + 0.5f
                    )
                )
            }
        }
    }

    val gravity = 0.85f
    val jumpForce = -13f

    fun resetGame() {
        droneY = 400f
        droneVelocity = 0f
        score = 0
        pipsCollected = 0
        comboMultiplier = 1
        obstacles.clear()
        // Generate initial candles
        obstacles.add(CandleObstacle(x = 1000f, topHeight = 220f, gap = 320f))
        obstacles.add(CandleObstacle(x = 1500f, topHeight = 350f, gap = 310f))
        obstacles.add(CandleObstacle(x = 2000f, topHeight = 180f, gap = 300f))
        isGameOver = false
        isPlaying = true
    }

    // Main Game Engine Loop
    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isActive && !isGameOver) {
                delay(16) // ~60 FPS

                // Physics update
                if (engineThrust) {
                    droneVelocity += jumpForce * 0.28f
                } else {
                    droneVelocity += gravity
                }
                droneVelocity = droneVelocity.coerceIn(-16f, 18f)
                droneY += droneVelocity

                // Floor and Ceiling bounds
                if (droneY < 40f) {
                    droneY = 40f
                    droneVelocity = 0f
                }
                if (droneY > 1000f) {
                    // Crashed into liquidation floor!
                    isGameOver = true
                    isPlaying = false
                    if (score > highScore) {
                        highScore = score
                        sharedPrefs.edit().putInt("high_score", highScore).apply()
                    }
                }

                // Obstacle Movement & Collision
                val obstacleSpeed = 8.5f + (score * 0.05f).coerceAtMost(6f)
                val droneX = 180f
                val droneRadius = 24f

                val iterator = obstacles.listIterator()
                while (iterator.hasNext()) {
                    val obs = iterator.next()
                    obs.x -= obstacleSpeed

                    // Collision check
                    val obsWidth = 80f
                    if (droneX + droneRadius > obs.x && droneX - droneRadius < obs.x + obsWidth) {
                        // Check if within upper candle or lower candle
                        val gapTop = obs.topHeight
                        val gapBottom = obs.topHeight + obs.gap
                        if (droneY - droneRadius < gapTop || droneY + droneRadius > gapBottom) {
                            isGameOver = true
                            isPlaying = false
                            if (score > highScore) {
                                highScore = score
                                sharedPrefs.edit().putInt("high_score", highScore).apply()
                            }
                        }

                        // Pip Coin Collection inside the gap
                        if (obs.hasCoin && !obs.coinCollected) {
                            val coinY = obs.topHeight + (obs.gap / 2f)
                            if (kotlin.math.abs(droneY - coinY) < 45f) {
                                obs.coinCollected = true
                                pipsCollected += 10
                                score += 10 * comboMultiplier
                                comboMultiplier = (comboMultiplier + 1).coerceAtMost(5)
                            }
                        }
                    }

                    // Score point on passing obstacle
                    if (!obs.passed && obs.x + obsWidth < droneX) {
                        obs.passed = true
                        score += 5
                    }

                    // Recycle offscreen obstacles
                    if (obs.x < -120f) {
                        iterator.remove()
                    }
                }

                // Spawn new candles
                val lastX = obstacles.lastOrNull()?.x ?: 1000f
                if (lastX < 1200f) {
                    val randomTop = Random.nextFloat() * 320f + 100f
                    val currentGap = (310f - (score * 0.8f)).coerceAtLeast(240f)
                    obstacles.add(CandleObstacle(x = lastX + 480f, topHeight = randomTop, gap = currentGap))
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBgDark)
            .pointerInput(isPlaying, isGameOver) {
                detectTapGestures(
                    onPress = {
                        if (!isPlaying && !isGameOver) {
                            resetGame()
                        } else if (isGameOver) {
                            resetGame()
                        } else {
                            engineThrust = true
                            droneVelocity = jumpForce
                            tryAwaitRelease()
                            engineThrust = false
                        }
                    }
                )
            }
    ) {
        // Starfield & Arcade Graphics Canvas
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasW = size.width
            val canvasH = size.height

            // 1. Draw Starfield
            stars.forEach { star ->
                drawCircle(
                    color = Color.White.copy(alpha = 0.6f),
                    radius = star.size,
                    center = Offset(star.x % canvasW, (star.y) % canvasH)
                )
            }

            // 2. Draw Retro Synthwave Grid Horizon Lines
            val horizonY = canvasH * 0.85f
            drawLine(
                color = CyberBlue.copy(alpha = 0.3f),
                start = Offset(0f, horizonY),
                end = Offset(canvasW, horizonY),
                strokeWidth = 2f
            )

            // 3. Draw Candlestick Obstacles
            obstacles.forEach { obs ->
                val candleWidth = 72f
                val wickWidth = 6f
                val wickColor = BearishRed.copy(alpha = 0.7f)

                // Top Candlestick (Ceiling)
                val topWickX = obs.x + (candleWidth / 2f) - (wickWidth / 2f)
                drawRect(
                    color = wickColor,
                    topLeft = Offset(topWickX, 0f),
                    size = Size(wickWidth, obs.topHeight)
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(BearishRed.copy(alpha = 0.85f), BearishRed)
                    ),
                    topLeft = Offset(obs.x, 0f),
                    size = Size(candleWidth, (obs.topHeight - 30f).coerceAtLeast(10f))
                )

                // Bottom Candlestick (Floor)
                val bottomY = obs.topHeight + obs.gap
                val bottomHeight = canvasH - bottomY
                drawRect(
                    color = wickColor,
                    topLeft = Offset(topWickX, bottomY),
                    size = Size(wickWidth, bottomHeight)
                )
                drawRect(
                    brush = Brush.verticalGradient(
                        colors = listOf(BearishRed, BearishRed.copy(alpha = 0.85f))
                    ),
                    topLeft = Offset(obs.x, bottomY + 30f),
                    size = Size(candleWidth, (bottomHeight - 30f).coerceAtLeast(10f))
                )

                // TP Pip Gem inside gap
                if (obs.hasCoin && !obs.coinCollected) {
                    val gemCenterY = obs.topHeight + (obs.gap / 2f)
                    val gemCenterX = obs.x + (candleWidth / 2f)
                    drawCircle(
                        brush = Brush.radialGradient(
                            colors = listOf(PipGold, Color(0xFFFFA000))
                        ),
                        radius = 16f,
                        center = Offset(gemCenterX, gemCenterY)
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 6f,
                        center = Offset(gemCenterX - 4f, gemCenterY - 4f)
                    )
                }
            }

            // 4. Draw Orbit Drone Ship
            val droneCenter = Offset(180f, droneY.coerceIn(40f, canvasH - 60f))

            // Plasma Exhaust Trail
            if (engineThrust || isPlaying) {
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(CyberBlue, Color.Transparent)
                    ),
                    radius = 36f,
                    center = Offset(droneCenter.x - 22f, droneCenter.y)
                )
            }

            // Outer Shield Ring
            drawCircle(
                color = CyberBlue,
                radius = 24f,
                center = droneCenter,
                style = Stroke(width = 3.5f)
            )

            // Core Energy
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(Color.White, BullishGreen)
                ),
                radius = 14f,
                center = droneCenter
            )

            // Floor Danger Zone Indicator
            drawLine(
                color = BearishRed.copy(alpha = 0.8f),
                start = Offset(0f, 1000f),
                end = Offset(canvasW, 1000f),
                strokeWidth = 3f
            )
        }

        // Top HUD Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 40.dp, start = 20.dp, end = 20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Score & Pips
            Column {
                Text(
                    text = "SCORE: $score",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "PIPS: +$pipsCollected  |  COMBO x$comboMultiplier",
                    color = PipGold,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold
                )
            }

            // High Score
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF1E2633)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = PipGold,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "RECORD: $highScore",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Overlay: Initial Welcome Screen
        if (!isPlaying && !isGameOver) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF161F2E).copy(alpha = 0.95f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.horizontalGradient(listOf(CyberBlue, NeonPink)))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "🪐 ORBIT SPACE SCALPER",
                            color = CyberBlue,
                            fontSize = 20.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Navigate the volatility. Dodge the liquidation wicks. Secure pips.",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(BullishGreen))
                            Text("Take Profit (+10 Pips)", fontSize = 11.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(BearishRed))
                            Text("Margin Call Spikes (Fatal)", fontSize = 11.sp, color = Color.White, fontFamily = FontFamily.Monospace)
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { resetGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberBlue),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TAP TO LAUNCH", color = Color.Black, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }

        // Overlay: Game Over Screen
        if (isGameOver) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFF200F14).copy(alpha = 0.95f),
                    border = CardDefaults.outlinedCardBorder().copy(brush = androidx.compose.ui.graphics.SolidColor(BearishRed))
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "💀 MARGIN CALL HIT!",
                            color = BearishRed,
                            fontSize = 22.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Account liquidated by market volatility.",
                            color = Color.LightGray,
                            fontSize = 12.sp,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "FINAL SCORE: $score",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "PIPS EXTRACTED: +$pipsCollected",
                            color = PipGold,
                            fontSize = 14.sp,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = { resetGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = BullishGreen),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("DEPLOY AGAIN", color = Color.Black, fontWeight = FontWeight.Black, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }
        }
    }
}
