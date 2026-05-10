package com.example.ninjagame.game.domain

import com.google.firebase.Timestamp

data class UserSettings(
    val musicVolume: Float = 0.5f,
    val sfxVolume: Float = 0.8f,
    val vibrationEnabled: Boolean = true
)

data class UserProfile(
    val userId: String = "",
    val displayName: String = "",
    val profileImage: String? = null,
    val coins: Long = 0L,
    val unlockedWeapons: List<String> = listOf("default_kunai"),
    val currentWeaponId: String = "default_kunai",
    val bestTimes: Map<String, Long> = mapOf(
        "Easy" to 0L,
        "Medium" to 0L,
        "Hard" to 0L
    ),
    val settings: UserSettings = UserSettings()
)

data class Announcement(
    val id: String = "",
    val message: String = "",
    val type: String = "INFO", // "RECORD", "STORE", "INFO"
    val createdAt: Timestamp = Timestamp.now()
)

data class GameSession(
    val sessionId: String = "",
    val userId: String = "",
    val survivalTime: Long = 0L,
    val coinsEarned: Int = 0,
    val createdAt: Timestamp = Timestamp.now(),
    val difficulty: String = "EASY"
)

data class StoreItem(
    val id: String,
    val name: String,
    val price: Int,
    val drawableRes: Int
)
