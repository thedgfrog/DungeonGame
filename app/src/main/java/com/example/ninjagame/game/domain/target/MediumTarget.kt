package com.example.ninjagame.game.domain.target

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.ui.graphics.Color

data class MediumTarget (
    override val x: Float = 0f,
    override val y: Animatable<Float, AnimationVector1D> = Animatable(0f),
    override val radius: Float = 0f,
    override val fallingSpeed: Float = 0f,
    override val color: Color = Color(0xFF7F51FF),
    val lives:Int=2//how many weapon throw to destroy target.
) :Target