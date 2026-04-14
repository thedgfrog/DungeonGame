package com.example.ninjagame.game.domain

import com.google.firebase.Timestamp

data class UserProfile(
    val userId: String = "",
    val displayName: String = "",
    val bestSurvivalTime: Long = 0L
)

data class GameSession(
    val sessionId: String = "",
    val userId: String = "",
    val survivalTime: Long = 0L,
    val createdAt: Timestamp = Timestamp.now()
)
