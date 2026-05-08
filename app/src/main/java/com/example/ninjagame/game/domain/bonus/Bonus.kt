package com.example.ninjagame.game.domain.bonus

sealed class BonusType {
    object Heart : BonusType()
    object Speed : BonusType()
}

data class Bonus(
    val type: BonusType,
    val x: Float,
    val y: Float,
    val radius: Float = 30f
)