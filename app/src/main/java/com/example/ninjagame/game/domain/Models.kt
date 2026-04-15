package com.example.ninjagame.game.domain

import com.google.firebase.Timestamp

data class UserProfile(
    val userId: String = "",
    val displayName: String = "",
    val bestSurvivalTime: Long = 0L,
    val coins: Long = 0L,
    val unlockedWeapons: List<String> = listOf("default_kunai"),
    val currentWeaponId: String = "default_kunai",
    val avatarUrl: String = ""
)

data class GameSession(
    val sessionId: String = "",
    val userId: String = "",
    val survivalTime: Long = 0L,
    val coinsEarned: Int = 0,
    val createdAt: Timestamp = Timestamp.now()
)

data class StoreItem(
    val id: String,
    val name: String,
    val price: Int,
    val drawableRes: Int
)
