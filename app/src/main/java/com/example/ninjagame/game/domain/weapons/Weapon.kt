package com.example.ninjagame.game.domain.weapons

data class Weapon(
    val x: Float,
    var y: Float,
    val radius:Float,
    val shootingSpeed:Float=0f
)