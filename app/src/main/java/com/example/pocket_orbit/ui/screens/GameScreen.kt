// ================================================================================
// FILE: app/src/main/java/com/example/pocket_orbit/ui/screens/GameScreen.kt
// VERSION: 6.0.0 | SYSTEM: Orbit Chill Zone - Cyber Arcade Deck
// IDENTITY: Multi-game arcade hub featuring Space Scalper, Cyber Matrix Memory, and Orbit Breaker. 🎮⚡
// ================================================================================

package com.example.pocket_orbit.ui.screens

import android.content.Context
import androidx.compose.animation.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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

// Cyberpunk Arcade Palette
private val SpaceBgDark = Color(0xFF0A0E17)
private val CyberBlue = Color(0xFF00F0FF)
private val NeonPink = Color(0xFFFF007F)
private val PipGold = Color(0xFFFFD700)
private val BullishGreen = Color(0xFF00E676)
private val BearishRed = Color(0xFFFF1744)
private val MatrixPurple = Color(0xFFBD00FF)

// ================================================================================
// ROOT GAME DECK (CHILL ZONE)
// ================================================================================
@Composable
fun GameScreen() {
    var selectedGameTab by remember { mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SpaceBgDark)
    ) {
        // Game Selector Header
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = Color(0xFF101726),
            tonalElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(top = 10.dp, bottom = 8.dp)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🎮", fontSize = 20.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                text = "Orbit Chill Arcade",
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                            Text(
                                text = "Take a break • Boost brain dopamine",
                                fontSize = 11.sp,
                                color = Color(0xFF00FFCC)
                            )
                        }

                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Game Selector Pills
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val games = listOf(
                        Triple(0, "🚀 Space Scalper", CyberBlue),
                        Triple(1, "⚡ Memory Matrix", NeonPink),
                        Triple(2, "🧱 Orbit Breaker", PipGold),
                        Triple(3, "🎯 Reaction Rush", BullishGreen),
                        Triple(4, "🎲 Zen Dice", MatrixPurple)
                    )

                    games.forEach { (index, title, accent) ->
                        val isSelected = selectedGameTab == index
                        Surface(
                            shape = RoundedCornerShape(20.dp),
                            color = if (isSelected) accent.copy(alpha = 0.2f) else Color(0xFF182234),
                            border = androidx.compose.foundation.BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) accent else Color(0xFF2C3E5A)
                            ),
                            modifier = Modifier.clickable { selectedGameTab = index }
                        ) {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) accent else Color.LightGray,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            )
                        }
                    }
                }
            }
        }

        // Active Game Container
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            when (selectedGameTab) {
                0 -> SpaceScalperGame()
                1 -> CyberMatrixGame()
                2 -> OrbitBreakerGame()
                3 -> ReactionRushGame()
                4 -> ZenDiceGame()
            }
        }
    }
}

@Composable
private fun ReactionRushGame() {
    var score by remember { mutableIntStateOf(0) }
    var target by remember { mutableIntStateOf(Random.nextInt(0, 9)) }
    var rounds by remember { mutableIntStateOf(0) }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Reaction Rush", color = BullishGreen, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("Tap the highlighted pad before the round changes.", color = Color.LightGray)
        Spacer(Modifier.height(12.dp))
        Text("Score: $score  •  Round: $rounds", color = Color.White)
        Spacer(Modifier.height(20.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { row ->
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(3) { column ->
                        val index = row * 3 + column
                        Box(
                            Modifier.size(72.dp).clip(RoundedCornerShape(12.dp))
                                .background(if (index == target) BullishGreen else Color(0xFF182234))
                                .clickable {
                                    score += if (index == target) 10 else -2
                                    rounds++
                                    target = Random.nextInt(0, 9)
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ZenDiceGame() {
    var roll by remember { mutableIntStateOf(0) }
    var total by remember { mutableIntStateOf(0) }
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Zen Dice", color = MatrixPurple, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text("A tiny luck break for your brain.", color = Color.LightGray)
        Spacer(Modifier.height(24.dp))
        Text(if (roll == 0) "🎲" else roll.toString(), fontSize = 64.sp, color = Color.White)
        Text("Session total: $total", color = Color.LightGray)
        Spacer(Modifier.height(20.dp))
        Button(onClick = {
            roll = Random.nextInt(1, 7)
            total += roll
        }) { Text("ROLL") }
    }
}

// ================================================================================
// GAME 1: SPACE SCALPER (FLAPPY CANDLESTICK RUNNER)
// ================================================================================
data class CandleObstacle(
    var x: Float,
    val topHeight: Float,
    val gap: Float = 280f,
    val isBullish: Boolean = false,
    var passed: Boolean = false,
    var hasCoin: Boolean = true,
    var coinCollected: Boolean = false
)

data class StarParticle(
    val x: Float,
    val y: Float,
    val size: Float,
    val speed: Float
)

@Composable
fun SpaceScalperGame() {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("orbit_game_prefs", Context.MODE_PRIVATE) }
    var highScore by remember { mutableIntStateOf(sharedPrefs.getInt("scalper_high_score", 0)) }

    var isPlaying by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var pipsCollected by remember { mutableIntStateOf(0) }
    var comboMultiplier by remember { mutableIntStateOf(1) }

    var droneY by remember { mutableFloatStateOf(400f) }
    var droneVelocity by remember { mutableFloatStateOf(0f) }
    var engineThrust by remember { mutableStateOf(false) }

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
        obstacles.add(CandleObstacle(x = 1000f, topHeight = 220f, gap = 320f))
        obstacles.add(CandleObstacle(x = 1500f, topHeight = 350f, gap = 310f))
        obstacles.add(CandleObstacle(x = 2000f, topHeight = 180f, gap = 300f))
        isGameOver = false
        isPlaying = true
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isActive && !isGameOver) {
                delay(16)
                if (engineThrust) {
                    droneVelocity += jumpForce * 0.28f
                } else {
                    droneVelocity += gravity
                }
                droneVelocity = droneVelocity.coerceIn(-16f, 18f)
                droneY += droneVelocity

                for (i in stars.indices) {
                    val s = stars[i]
                    var newX = s.x - s.speed * (2f + comboMultiplier * 0.5f)
                    if (newX < 0) newX = 1080f
                    stars[i] = s.copy(x = newX)
                }

                val moveSpeed = 6f + (score * 0.15f).coerceAtMost(8f)
                val droneX = 160f
                val droneRadius = 18f

                val iterator = obstacles.iterator()
                while (iterator.hasNext()) {
                    val obs = iterator.next()
                    obs.x -= moveSpeed

                    if (!obs.passed && obs.x + 60f < droneX) {
                        obs.passed = true
                        score += 1 * comboMultiplier
                        if (score > highScore) {
                            highScore = score
                            sharedPrefs.edit().putInt("scalper_high_score", highScore).apply()
                        }
                    }

                    if (obs.hasCoin && !obs.coinCollected) {
                        val coinX = obs.x + 30f
                        val coinY = obs.topHeight + obs.gap / 2f
                        val distSq = (droneX - coinX) * (droneX - coinX) + (droneY - coinY) * (droneY - coinY)
                        if (distSq < 1600f) {
                            obs.coinCollected = true
                            pipsCollected++
                            comboMultiplier = (comboMultiplier + 1).coerceAtMost(5)
                            score += 5 * comboMultiplier
                        }
                    }

                    val candleWidth = 60f
                    val hitX = droneX + droneRadius > obs.x && droneX - droneRadius < obs.x + candleWidth
                    val hitTop = droneY - droneRadius < obs.topHeight
                    val hitBottom = droneY + droneRadius > obs.topHeight + obs.gap
                    if (hitX && (hitTop || hitBottom)) {
                        isGameOver = true
                        isPlaying = false
                    }
                }

                if (obstacles.isNotEmpty() && obstacles.first().x < -100f) {
                    obstacles.removeAt(0)
                    val lastX = obstacles.last().x
                    val randomGap = (260f - (score * 2f)).coerceAtLeast(180f)
                    val randomTop = Random.nextFloat() * 450f + 100f
                    obstacles.add(
                        CandleObstacle(
                            x = lastX + 500f,
                            topHeight = randomTop,
                            gap = randomGap,
                            isBullish = Random.nextBoolean()
                        )
                    )
                }

                if (droneY > 1600f || droneY < 20f) {
                    isGameOver = true
                    isPlaying = false
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(isPlaying, isGameOver) {
                detectTapGestures(
                    onPress = {
                        if (!isPlaying && !isGameOver) {
                            resetGame()
                        } else if (isGameOver) {
                            resetGame()
                        } else {
                            engineThrust = true
                            tryAwaitRelease()
                            engineThrust = false
                        }
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val canvasWidth = size.width
            val canvasHeight = size.height

            // Background Stars
            stars.forEach { star ->
                drawCircle(
                    color = Color.White.copy(alpha = (star.size / 3.5f).coerceIn(0.2f, 0.9f)),
                    radius = star.size,
                    center = Offset(star.x % canvasWidth, star.y % canvasHeight)
                )
            }

            // Grid Lines
            for (y in 0 until canvasHeight.toInt() step 80) {
                drawLine(
                    color = Color(0xFF152238).copy(alpha = 0.35f),
                    start = Offset(0f, y.toFloat()),
                    end = Offset(canvasWidth, y.toFloat()),
                    strokeWidth = 1f
                )
            }

            // Candlestick Obstacles
            obstacles.forEach { obs ->
                val candleWidth = 60f
                val wickWidth = 6f
                val wickColor = if (obs.isBullish) BullishGreen else BearishRed
                val bodyColor = if (obs.isBullish) BullishGreen.copy(alpha = 0.85f) else BearishRed.copy(alpha = 0.85f)

                // Top Candlestick
                drawLine(
                    color = wickColor,
                    start = Offset(obs.x + candleWidth / 2f, 0f),
                    end = Offset(obs.x + candleWidth / 2f, obs.topHeight),
                    strokeWidth = wickWidth
                )
                drawRect(
                    color = bodyColor,
                    topLeft = Offset(obs.x, 0f),
                    size = Size(candleWidth, obs.topHeight - 20f)
                )

                // Bottom Candlestick
                val bottomY = obs.topHeight + obs.gap
                drawLine(
                    color = wickColor,
                    start = Offset(obs.x + candleWidth / 2f, bottomY),
                    end = Offset(obs.x + candleWidth / 2f, canvasHeight),
                    strokeWidth = wickWidth
                )
                drawRect(
                    color = bodyColor,
                    topLeft = Offset(obs.x, bottomY + 20f),
                    size = Size(candleWidth, canvasHeight - bottomY - 20f)
                )

                // Pip Coin
                if (obs.hasCoin && !obs.coinCollected) {
                    val coinX = obs.x + candleWidth / 2f
                    val coinY = obs.topHeight + obs.gap / 2f
                    drawCircle(color = PipGold, radius = 12f, center = Offset(coinX, coinY))
                    drawCircle(color = Color.White, radius = 5f, center = Offset(coinX, coinY))
                }
            }

            // Player Drone
            val droneX = 160f
            val droneRadius = 18f
            drawCircle(
                color = if (engineThrust) NeonPink else CyberBlue,
                radius = droneRadius,
                center = Offset(droneX, droneY)
            )
            drawCircle(color = Color.White, radius = droneRadius * 0.4f, center = Offset(droneX, droneY))

            // Drone Engine Jet Particle
            if (engineThrust) {
                drawCircle(
                    color = PipGold.copy(alpha = 0.8f),
                    radius = 9f,
                    center = Offset(droneX - 22f, droneY + 6f)
                )
            }
        }

        // HUD Overlay
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "PIPS: $score",
                    color = PipGold,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "HIGH: $highScore",
                    color = Color.Gray,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace
                )
            }

            if (comboMultiplier > 1) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = NeonPink.copy(alpha = 0.25f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, NeonPink)
                ) {
                    Text(
                        text = "${comboMultiplier}X LEVERAGE",
                        color = NeonPink,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Start / Game Over Overlay
        if (!isPlaying && !isGameOver) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF101726).copy(alpha = 0.9f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberBlue),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🚀", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "SPACE SCALPER",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = CyberBlue,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Navigate between bullish and bearish candlesticks.\nCollect golden pips to multiply your leverage.",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { resetGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberBlue)
                        ) {
                            Text("TAP TO LAUNCH", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }

        if (isGameOver) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF1A111E).copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BearishRed),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("💀", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "STOP LOSS HIT",
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            color = BearishRed,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Score: $score Pips • High: $highScore",
                            fontSize = 14.sp,
                            color = Color.White,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { resetGame() },
                            colors = ButtonDefaults.buttonColors(containerColor = BullishGreen)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("TRY AGAIN", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}

// ================================================================================
// GAME 2: CYBER MATRIX MEMORY (NEURAL SEQUENCE PUZZLE)
// ================================================================================
@Composable
fun CyberMatrixGame() {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("orbit_game_prefs", Context.MODE_PRIVATE) }
    var highScore by remember { mutableIntStateOf(sharedPrefs.getInt("matrix_high_score", 0)) }

    val padColors = listOf(CyberBlue, NeonPink, BullishGreen, PipGold)
    val padLabels = listOf("ALPHA", "BETA", "GAMMA", "DELTA")

    var sequence by remember { mutableStateOf<List<Int>>(emptyList()) }
    var playerStep by remember { mutableIntStateOf(0) }
    var isDemonstrating by remember { mutableStateOf(false) }
    var activeHighlightedPad by remember { mutableIntStateOf(-1) }
    var isGameOver by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var gameStarted by remember { mutableStateOf(false) }

    fun advanceRound() {
        val nextPad = Random.nextInt(4)
        sequence = sequence + nextPad
        playerStep = 0
    }

    fun startNewGame() {
        sequence = emptyList()
        score = 0
        isGameOver = false
        gameStarted = true
        advanceRound()
    }

    // Playback sequence to player
    LaunchedEffect(sequence) {
        if (sequence.isNotEmpty() && !isGameOver) {
            isDemonstrating = true
            delay(400)
            for (pad in sequence) {
                activeHighlightedPad = pad
                delay(450)
                activeHighlightedPad = -1
                delay(180)
            }
            isDemonstrating = false
        }
    }

    fun handlePadClick(index: Int) {
        if (isDemonstrating || isGameOver || !gameStarted) return

        if (index == sequence[playerStep]) {
            playerStep++
            if (playerStep == sequence.size) {
                score++
                if (score > highScore) {
                    highScore = score
                    sharedPrefs.edit().putInt("matrix_high_score", highScore).apply()
                }
                advanceRound()
            }
        } else {
            isGameOver = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Score Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("LEVEL: ${sequence.size}", color = NeonPink, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
                Text("SCORE: $score", color = Color.White, fontWeight = FontWeight.ExtraBold, fontSize = 20.sp, fontFamily = FontFamily.Monospace)
            }
            Text("RECORD: $highScore", color = Color.Gray, fontSize = 13.sp, fontFamily = FontFamily.Monospace)
        }

        // Status pill
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = if (isDemonstrating) Color(0xFFFF9800).copy(alpha = 0.2f) else BullishGreen.copy(alpha = 0.2f),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isDemonstrating) Color(0xFFFF9800) else BullishGreen)
        ) {
            Text(
                text = if (!gameStarted) "INITIALIZE SEQUENCE" else if (isDemonstrating) "OBSERVE MATRIX PATTERN..." else "YOUR TURN: REPEAT SEQUENCE",
                color = if (isDemonstrating) Color(0xFFFF9800) else BullishGreen,
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
            )
        }

        // 2x2 Matrix Button Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (row in 0..1) {
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    for (col in 0..1) {
                        val padIndex = row * 2 + col
                        val padColor = padColors[padIndex]
                        val isLit = activeHighlightedPad == padIndex

                        Surface(
                            modifier = Modifier
                                .size(135.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .clickable(enabled = !isDemonstrating && gameStarted && !isGameOver) {
                                    handlePadClick(padIndex)
                                },
                            shape = RoundedCornerShape(24.dp),
                            color = if (isLit) padColor else padColor.copy(alpha = 0.22f),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isLit) 3.dp else 1.5.dp,
                                color = if (isLit) Color.White else padColor
                            )
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = padLabels[padIndex],
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = if (isLit) Color.Black else padColor,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Button
        if (!gameStarted || isGameOver) {
            Button(
                onClick = { startNewGame() },
                colors = ButtonDefaults.buttonColors(containerColor = NeonPink),
                modifier = Modifier
                    .fillMaxWidth(0.8f)
                    .height(48.dp)
            ) {
                Text(
                    text = if (isGameOver) "RETRY MATRIX" else "START MATRIX",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }
        } else {
            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}

// ================================================================================
// GAME 3: ORBIT BREAKER (RETRO BRICK BREAKER)
// ================================================================================
data class Brick(
    val x: Float,
    val y: Float,
    val width: Float,
    val height: Float,
    val color: Color,
    var isDestroyed: Boolean = false
)

@Composable
fun OrbitBreakerGame() {
    val context = LocalContext.current
    val sharedPrefs = remember { context.getSharedPreferences("orbit_game_prefs", Context.MODE_PRIVATE) }
    var highScore by remember { mutableIntStateOf(sharedPrefs.getInt("breaker_high_score", 0)) }

    var isPlaying by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }
    var isWon by remember { mutableStateOf(false) }
    var score by remember { mutableIntStateOf(0) }
    var lives by remember { mutableIntStateOf(3) }

    var paddleX by remember { mutableFloatStateOf(400f) }
    var ballX by remember { mutableFloatStateOf(450f) }
    var ballY by remember { mutableFloatStateOf(900f) }
    var ballVx by remember { mutableFloatStateOf(5f) }
    var ballVy by remember { mutableFloatStateOf(-7f) }

    val bricks = remember { mutableStateListOf<Brick>() }

    fun initBricks() {
        bricks.clear()
        val rows = 4
        val cols = 6
        val brickWidth = 140f
        val brickHeight = 36f
        val startX = 60f
        val startY = 120f
        val colors = listOf(NeonPink, CyberBlue, PipGold, BullishGreen)

        for (r in 0 until rows) {
            for (c in 0 until cols) {
                bricks.add(
                    Brick(
                        x = startX + c * (brickWidth + 12f),
                        y = startY + r * (brickHeight + 12f),
                        width = brickWidth,
                        height = brickHeight,
                        color = colors[r % colors.size]
                    )
                )
            }
        }
    }

    fun startBreaker() {
        initBricks()
        paddleX = 400f
        ballX = 450f
        ballY = 900f
        ballVx = 5f
        ballVy = -7f
        score = 0
        lives = 3
        isGameOver = false
        isWon = false
        isPlaying = true
    }

    LaunchedEffect(isPlaying) {
        if (isPlaying) {
            while (isActive && !isGameOver && !isWon) {
                delay(16)

                ballX += ballVx
                ballY += ballVy

                // Wall collisions
                if (ballX < 15f || ballX > 1000f) {
                    ballVx = -ballVx
                }
                if (ballY < 20f) {
                    ballVy = -ballVy
                }

                // Paddle collision
                val paddleY = 1100f
                val paddleWidth = 180f
                if (ballY + 12f >= paddleY && ballY - 12f <= paddleY + 24f &&
                    ballX >= paddleX - 10f && ballX <= paddleX + paddleWidth + 10f
                ) {
                    ballVy = -kotlin.math.abs(ballVy)
                    val hitOffset = (ballX - (paddleX + paddleWidth / 2f)) / (paddleWidth / 2f)
                    ballVx = hitOffset * 8f
                }

                // Brick collisions
                bricks.forEach { brick ->
                    if (!brick.isDestroyed) {
                        if (ballX >= brick.x && ballX <= brick.x + brick.width &&
                            ballY >= brick.y && ballY <= brick.y + brick.height
                        ) {
                            brick.isDestroyed = true
                            ballVy = -ballVy
                            score += 10
                            if (score > highScore) {
                                highScore = score
                                sharedPrefs.edit().putInt("breaker_high_score", highScore).apply()
                            }
                        }
                    }
                }

                // Check victory
                if (bricks.all { it.isDestroyed }) {
                    isWon = true
                    isPlaying = false
                }

                // Ball fell below screen
                if (ballY > 1250f) {
                    lives--
                    if (lives <= 0) {
                        isGameOver = true
                        isPlaying = false
                    } else {
                        ballX = paddleX + 90f
                        ballY = 900f
                        ballVx = 5f
                        ballVy = -7f
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { _, dragAmount ->
                    paddleX = (paddleX + dragAmount.x).coerceIn(20f, 820f)
                }
            }
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // Draw Bricks
            bricks.forEach { brick ->
                if (!brick.isDestroyed) {
                    drawRoundRect(
                        color = brick.color,
                        topLeft = Offset(brick.x, brick.y),
                        size = Size(brick.width, brick.height),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f, 6f)
                    )
                }
            }

            // Draw Paddle
            val paddleY = 1100f
            val paddleWidth = 180f
            drawRoundRect(
                color = CyberBlue,
                topLeft = Offset(paddleX, paddleY),
                size = Size(paddleWidth, 22f),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
            )

            // Draw Ball
            drawCircle(
                color = PipGold,
                radius = 12f,
                center = Offset(ballX, ballY)
            )
            drawCircle(
                color = Color.White,
                radius = 4f,
                center = Offset(ballX, ballY)
            )
        }

        // Top HUD
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("SCORE: $score", color = PipGold, fontWeight = FontWeight.Bold, fontSize = 16.sp, fontFamily = FontFamily.Monospace)
                Text("LIVES: " + "❤️ ".repeat(lives.coerceAtLeast(0)), fontSize = 12.sp)
            }
            Text("RECORD: $highScore", color = Color.Gray, fontSize = 12.sp, fontFamily = FontFamily.Monospace)
        }

        // Overlay: Start or Game Over
        if (!isPlaying) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF101726).copy(alpha = 0.95f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, PipGold),
                    modifier = Modifier.padding(24.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(if (isWon) "🏆" else if (isGameOver) "💥" else "🧱", fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (isWon) "ORBIT CLEARED!" else if (isGameOver) "DECK DESTROYED" else "ORBIT BREAKER",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = PipGold,
                            fontFamily = FontFamily.Monospace
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (isWon) "All bricks vaporized! Outstanding reflexes." else if (isGameOver) "Final Score: $score" else "Drag paddle to bounce energy orb & break all bricks.",
                            fontSize = 12.sp,
                            color = Color.LightGray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { startBreaker() },
                            colors = ButtonDefaults.buttonColors(containerColor = PipGold)
                        ) {
                            Text(if (isGameOver || isWon) "PLAY AGAIN" else "START BREAKER", fontWeight = FontWeight.Bold, color = Color.Black)
                        }
                    }
                }
            }
        }
    }
}
