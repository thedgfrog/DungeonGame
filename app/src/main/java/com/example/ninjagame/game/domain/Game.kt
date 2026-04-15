package com.example.ninjagame.game.domain

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue

class Game(
    status: GameStatus = GameStatus.Idle,
    val score: Int = 0,
    val setting: GameSettings = GameSettings()
) {
    var status by mutableStateOf(status)
}

data class GameSettings (
    val ninjaSpeed: Float = 15f,
    val weaponSpeed: Float = 20f,
    val targetSpeed: Float = 30f
)
