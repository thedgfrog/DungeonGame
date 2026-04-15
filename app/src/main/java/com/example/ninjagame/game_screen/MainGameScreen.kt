package com.example.ninjagame.game_screen

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Vibrator
import android.content.Context
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ninjagame.R
import com.example.ninjagame.data.FirestoreRepository
import com.example.ninjagame.game.domain.Game
import com.example.ninjagame.game.domain.GameStatus
import com.example.ninjagame.game.domain.MoveDirection
import com.example.ninjagame.game.domain.UserProfile
import com.example.ninjagame.game.domain.target.EasyTarget
import com.example.ninjagame.game.domain.target.MediumTarget
import com.example.ninjagame.game.domain.target.StrongTarget
import com.example.ninjagame.game.domain.target.Target
import com.example.ninjagame.game.domain.weapons.Weapon
import com.example.ninjagame.util.SoundManager
import com.example.ninjagame.util.detectMoveGesture
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

const val WEAPON_SPAWN_RATE = 250L

data class Explosion(val x: Float, val y: Float, val startTime: Long, val color: Color)

@Composable
fun MainGameScreen() {
    val context = LocalContext.current
    val game = remember { Game(status = GameStatus.Idle) }
    val coroutineScope = rememberCoroutineScope()
    val repository = remember { FirestoreRepository() }
    val soundManager = remember { SoundManager(context) }
    val vibrator = remember { context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator }

    val weapons = remember { mutableStateListOf<Weapon>() }
    val targets = remember { mutableStateListOf<Target>() }
    val targetLives = remember { mutableStateMapOf<Target, Int>() }
    val explosions = remember { mutableStateListOf<Explosion>() }

    var userProfile by remember { mutableStateOf<UserProfile?>(null) }
    var coinsEarned by remember { mutableIntStateOf(0) }

    var moveDirection by remember { mutableStateOf(MoveDirection.None) }
    var lastDirection by remember { mutableStateOf(MoveDirection.Right) }
    var screenWidth by remember { mutableIntStateOf(0) }
    var screenHeight by remember { mutableIntStateOf(0) }
    var ninjaX by remember { mutableFloatStateOf(-1f) }

    var startTime by remember { mutableLongStateOf(0L) }
    var elapsedTime by remember { mutableLongStateOf(0L) }

    val shakeOffset = remember { Animatable(0f) }

    val ninjaScale = 0.3f
    val ninjaSpeed = 45f

    fun decodeSampledBitmapFromResource(resId: Int, reqWidth: Int, reqHeight: Int): ImageBitmap {
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }
        BitmapFactory.decodeResource(context.resources, resId, options)
        var inSampleSize = 1
        if (options.outHeight > reqHeight || options.outWidth > reqWidth) {
            val halfHeight = options.outHeight / 2
            val halfWidth = options.outWidth / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        options.inJustDecodeBounds = false
        options.inSampleSize = inSampleSize
        return BitmapFactory.decodeResource(context.resources, resId, options).asImageBitmap()
    }

    val backgroundBitmap = remember { decodeSampledBitmapFromResource(R.drawable.background, 1080, 1920) }
    val runningBitmap = remember { decodeSampledBitmapFromResource(R.drawable.run_sprite, 500, 500) }
    val standingBitmap = remember { decodeSampledBitmapFromResource(R.drawable.standing_ninja, 300, 300) }

    val weaponBitmap = remember(userProfile?.currentWeaponId) {
        val resId = when (userProfile?.currentWeaponId) {
            "shuriken" -> R.drawable.riu
            "fire_kunai" -> R.drawable.sword
            "golden_blade" -> R.drawable.sword1
            else -> R.drawable.kunai
        }
        decodeSampledBitmapFromResource(resId, 200, 200)
    }

    LaunchedEffect(screenWidth) {
        if (screenWidth > 0 && ninjaX == -1f) {
            val ninjaWidth = (standingBitmap.width * ninjaScale)
            ninjaX = (screenWidth - ninjaWidth) / 2f
        }
    }

    LaunchedEffect(game.status) {
        if (game.status == GameStatus.Idle || game.status == GameStatus.Started) {
            userProfile = repository.getOrCreateProfile()
        }
    }

    LaunchedEffect(game.status, screenWidth, screenHeight) {
        while (game.status == GameStatus.Started) {
            if (screenWidth > 0 && screenHeight > 0) {
                val x = Random.nextFloat() * (screenWidth - 120f) + 60f
                val target: Target = when (Random.nextInt(10)) {
                    in 0..5 -> EasyTarget(x, Animatable(-100f), 45f, Random.nextFloat() * 2f + 2.5f)
                    in 6..8 -> MediumTarget(x, Animatable(-100f), 55f, Random.nextFloat() * 3f + 4.5f)
                    else -> StrongTarget(x, Animatable(-100f), 70f, Random.nextFloat() * 2f + 2f)
                }

                targets.add(target)
                targetLives[target] = when (target) {
                    is StrongTarget -> target.lives
                    is MediumTarget -> target.lives
                    else -> 1
                }

                coroutineScope.launch {
                    target.y.animateTo(
                        targetValue = screenHeight.toFloat() + 150f,
                        animationSpec = tween(
                            durationMillis = (12000f / target.fallingSpeed).toInt(),
                            easing = LinearEasing
                        )
                    )
                    targets.remove(target)
                    targetLives.remove(target)
                }
            }
            delay(Random.nextLong(500L, 1200L))
        }
    }

    LaunchedEffect(game.status, moveDirection) {
        while (game.status == GameStatus.Started && moveDirection != MoveDirection.None) {
            if (ninjaX != -1f) {
                val isMoving = moveDirection != MoveDirection.None
                val bitmap = if (isMoving) runningBitmap else standingBitmap
                val cols = if (isMoving) 3 else 1
                val frameWidth = (bitmap.width / cols) * ninjaScale

                soundManager.playThrow()
                weapons.add(
                    Weapon(
                        x = ninjaX + frameWidth / 2f,
                        y = screenHeight - (bitmap.height * ninjaScale) - 20f,
                        radius = 20f,
                        shootingSpeed = 45f
                    )
                )
            }
            delay(WEAPON_SPAWN_RATE)
        }
    }

    LaunchedEffect(game.status) {
        while (game.status == GameStatus.Started) {
            val iterator = weapons.listIterator()
            while (iterator.hasNext()) {
                val weapon = iterator.next()
                weapon.y -= weapon.shootingSpeed

                val hit = targets.firstOrNull {
                    val dx = kotlin.math.abs(weapon.x - it.x)
                    val dy = kotlin.math.abs(weapon.y - it.y.value)
                    dx < (it.radius + weapon.radius) * 0.9f && dy < (it.radius + weapon.radius) * 0.9f
                }

                if (hit != null) {
                    iterator.remove()
                    val lives = targetLives.getOrDefault(hit, 1)
                    if (lives <= 1) {
                        coinsEarned += when(hit) {
                            is EasyTarget -> Random.nextInt(1, 4)
                            is MediumTarget -> Random.nextInt(4, 8)
                            is StrongTarget -> Random.nextInt(8, 11)
                            else -> 1
                        }
                        explosions.add(Explosion(hit.x, hit.y.value, System.currentTimeMillis(), hit.color))
                        soundManager.playExplode()
                        targets.remove(hit)
                        targetLives.remove(hit)
                    } else {
                        targetLives[hit] = lives - 1
                    }
                } else if (weapon.y < -100f) {
                    iterator.remove()
                }
            }

            val now = System.currentTimeMillis()
            explosions.removeAll { now - it.startTime > 300 }

            if (targets.any { it.y.value >= screenHeight.toFloat() }) {
                elapsedTime = System.currentTimeMillis() - startTime
                game.status = GameStatus.Over
                soundManager.playGameOver()
                vibrator.vibrate(300)

                coroutineScope.launch {
                    repeat(5) {
                        shakeOffset.animateTo(20f, tween(50))
                        shakeOffset.animateTo(-20f, tween(50))
                    }
                    shakeOffset.snapTo(0f)
                }

                coroutineScope.launch {
                    repository.saveGameSession(elapsedTime, coinsEarned)
                }
            }

            delay(16L)
        }
    }

    var currentFrame by remember { mutableIntStateOf(0) }

    // Logic di chuyển Ninja - Cho phép di chuyển cả khi Game Over
    LaunchedEffect(game.status, moveDirection) {
        while (game.status == GameStatus.Started || game.status == GameStatus.Over) {
            if (moveDirection != MoveDirection.None && ninjaX != -1f) {
                lastDirection = moveDirection
                currentFrame = (currentFrame + 1) % 9

                if (moveDirection == MoveDirection.Left) {
                    ninjaX = (ninjaX - ninjaSpeed).coerceAtLeast(0f)
                } else {
                    val maxPosX = screenWidth - (runningBitmap.width / 3) * ninjaScale
                    ninjaX = (ninjaX + ninjaSpeed).coerceAtMost(maxPosX)
                }
            } else {
                currentFrame = 0
            }
            delay(32L)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned {
                screenWidth = it.size.width
                screenHeight = it.size.height
            }
            .offset { IntOffset(shakeOffset.value.toInt(), 0) }
    ) {
        if (game.status == GameStatus.Idle) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "DUNGEON GAME", color = Color.White, fontSize = 40.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(30.dp))
                    BouncyGameButton(
                        text = "Start Game",
                        isLarge = true // Để true nếu muốn nút to rõ ràng cho màn hình khởi đầu
                    ) {
                        startTime = System.currentTimeMillis()
                        coinsEarned = 0
                        game.status = GameStatus.Started
                    }
                }
            }
        }

        if (game.status == GameStatus.Started || game.status == GameStatus.Over) {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(game.status) { // FIX: Sử dụng game.status làm key để restart gesture handler khi chơi lại
                        awaitPointerEventScope {
                            detectMoveGesture(
                                gameStatusProvider = { game.status },
                                onLeft = { moveDirection = MoveDirection.Left },
                                onRight = { moveDirection = MoveDirection.Right },
                                onFingerLifted = { moveDirection = MoveDirection.None }
                            )
                        }
                    }
            ) {
                drawImage(image = backgroundBitmap, dstSize = IntSize(size.width.toInt(), size.height.toInt()))

                targets.forEach {
                    drawCircle(color = it.color, radius = it.radius, center = Offset(it.x, it.y.value))
                }

                weapons.forEach {
                    drawImage(image = weaponBitmap, dstOffset = IntOffset(it.x.toInt() - 40, it.y.toInt() - 40), dstSize = IntSize(80, 80))
                }

                explosions.forEach { explosion ->
                    val progress = (System.currentTimeMillis() - explosion.startTime) / 300f
                    drawCircle(
                        color = explosion.color.copy(alpha = 1f - progress),
                        radius = 20f + (progress * 60f),
                        center = Offset(explosion.x, explosion.y)
                    )
                }

                val isMoving = moveDirection != MoveDirection.None
                val currentFacing = if (isMoving) moveDirection else lastDirection
                val shouldFlip = currentFacing == MoveDirection.Left

                val currentNinjaX = if (ninjaX == -1f) (size.width - (standingBitmap.width * ninjaScale)) / 2f else ninjaX

                if (isMoving) {
                    val cols = 3
                    val rows = 3
                    val frameWidth = runningBitmap.width / cols
                    val frameHeight = runningBitmap.height / rows
                    val col = currentFrame % cols
                    val row = currentFrame / cols

                    val dW = (frameWidth * ninjaScale).toInt()
                    val dH = (frameHeight * ninjaScale).toInt()
                    val dX = currentNinjaX.toInt()
                    val dY = (size.height - dH - 20f).toInt()

                    if (shouldFlip) {
                        scale(scaleX = -1f, scaleY = 1f, pivot = Offset(dX + dW / 2f, dY + dH / 2f)) {
                            drawImage(image = runningBitmap, srcOffset = IntOffset(col * frameWidth, row * frameHeight), srcSize = IntSize(frameWidth, frameHeight), dstOffset = IntOffset(dX, dY), dstSize = IntSize(dW, dH))
                        }
                    } else {
                        drawImage(image = runningBitmap, srcOffset = IntOffset(col * frameWidth, row * frameHeight), srcSize = IntSize(frameWidth, frameHeight), dstOffset = IntOffset(dX, dY), dstSize = IntSize(dW, dH))
                    }
                } else {
                    val dW = (standingBitmap.width * ninjaScale).toInt()
                    val dH = (standingBitmap.height * ninjaScale).toInt()
                    val dX = currentNinjaX.toInt()
                    val dY = (size.height - dH - 20f).toInt()

                    if (shouldFlip) {
                        scale(scaleX = -1f, scaleY = 1f, pivot = Offset(dX + dW / 2f, dY + dH / 2f)) {
                            drawImage(image = standingBitmap, dstOffset = IntOffset(dX, dY), dstSize = IntSize(dW, dH))
                        }
                    } else {
                        drawImage(image = standingBitmap, dstOffset = IntOffset(dX, dY), dstSize = IntSize(dW, dH))
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize().padding(16.dp), contentAlignment = Alignment.TopStart) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.Yellow)
                    Text(" $coinsEarned", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        if (game.status == GameStatus.Over) {
            Box(
                modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "GAME OVER", color = Color.Red, fontSize = 48.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(text = "Survival Time: ${elapsedTime / 1000}s", color = Color.White, fontSize = 24.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(text = "Coins Earned: ", color = Color.White, fontSize = 24.sp)
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.Yellow, modifier = Modifier.size(24.dp))
                        Text(text = " $coinsEarned", color = Color.Yellow, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(30.dp))
                    Button(onClick = {
                        weapons.clear()
                        targets.clear()
                        targetLives.clear()
                        explosions.clear()
                        moveDirection = MoveDirection.None
                        lastDirection = MoveDirection.Right
                        startTime = System.currentTimeMillis()
                        coinsEarned = 0
                        game.status = GameStatus.Started
                    }) {
                        Text("Play Again")
                    }
                }
            }
        }
    }
}