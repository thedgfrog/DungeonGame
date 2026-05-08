package com.example.ninjagame.game.domain.target

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.graphics.Color


interface Target {
    val x:Float
    val y: Animatable<Float, AnimationVector1D>
    val radius: Float
    val fallingSpeed: Float
    val color: Color


}