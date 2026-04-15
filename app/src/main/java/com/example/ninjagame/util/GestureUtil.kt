package com.example.ninjagame.util

import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import com.example.ninjagame.game.domain.GameStatus

suspend fun AwaitPointerEventScope.detectMoveGesture(//suspend fun to run in coroutine without block UI
    gameStatusProvider: () -> GameStatus,
    onLeft: () -> Unit,
    onRight: () -> Unit,
    onFingerLifted: () -> Unit,
) {
    val threshold = 5f

    while (gameStatusProvider() == GameStatus.Started) {

        val downEvent = awaitPointerEvent()//wait user touch screen
        val initialDown = downEvent.changes.firstOrNull { it.pressed } ?: continue

        val primaryPointerId = initialDown.id
        var previousPosition = initialDown.position

        while (true) {//follow finger hold screen
            val event = awaitPointerEvent()
            val change = event.changes.firstOrNull {
                it.id == primaryPointerId
            }

            if (change == null || !change.pressed) {//check left of finger
                onFingerLifted()
                break
            }

            val currentPosition = change.position
            val deltaX = currentPosition.x - previousPosition.x

            if (deltaX < -threshold) {
                onLeft()
            } else if (deltaX > threshold) {
                onRight()
            }

            previousPosition = currentPosition
            change.consume()
        }
    }
}