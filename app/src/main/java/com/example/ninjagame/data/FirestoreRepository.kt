package com.example.ninjagame.data

import com.example.ninjagame.game.domain.Difficulty
import com.example.ninjagame.game.domain.GameSession
import com.example.ninjagame.game.domain.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    // Hàm hỗ trợ đọc Long an toàn
    private fun getSafeLong(data: Any?): Long {
        return when (data) {
            is Long -> data
            is Int -> data.toLong()
            is Double -> data.toLong()
            is String -> data.toLongOrNull() ?: 0L
            else -> 0L
        }
    }

    suspend fun saveGameSession(survivalTime: Long, coinsEarned: Int, difficulty: Difficulty) {
        val userId = auth.currentUser?.uid ?: return
        val sessionId = firestore.collection("game_sessions").document().id

        val session = GameSession(
            sessionId = sessionId,
            userId = userId,
            survivalTime = survivalTime,
            coinsEarned = coinsEarned,
            difficulty = difficulty.name
        )

        try {
            firestore.collection("game_sessions")
                .document(sessionId)
                .set(session)
                .await()

            updateBestScoreAndCoins(userId, survivalTime, coinsEarned, difficulty)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun updateBestScoreAndCoins(userId: String, currentTime: Long, coins: Int, difficulty: Difficulty) {
        val profileRef = firestore.collection("profiles").document(userId)

        val bestField = when(difficulty) {
            Difficulty.EASY -> "bestEasyTime"
            Difficulty.MEDIUM -> "bestMediumTime"
            Difficulty.HARD -> "bestHardTime"
        }

        try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(profileRef)
                val currentBest = getSafeLong(snapshot.get(bestField))

                val updates = mutableMapOf<String, Any>(
                    "coins" to FieldValue.increment(coins.toLong())
                )

                if (currentTime > currentBest) {
                    updates[bestField] = currentTime
                }

                transaction.set(profileRef, updates, SetOptions.merge())
            }.await()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    suspend fun buyItem(itemId: String, price: Int): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val profileRef = firestore.collection("profiles").document(userId)

        return try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(profileRef)
                val currentCoins = getSafeLong(snapshot.get("coins"))
                val unlocked = snapshot.get("unlockedWeapons") as? List<*> ?: listOf("default_kunai")

                if (currentCoins >= price && !unlocked.contains(itemId)) {
                    transaction.update(profileRef, "coins", currentCoins - price)
                    transaction.update(profileRef, "unlockedWeapons", FieldValue.arrayUnion(itemId))
                    true
                } else {
                    false
                }
            }.await() ?: false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun useWeapon(itemId: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        return try {
            firestore.collection("profiles")
                .document(userId)
                .update("currentWeaponId", itemId)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getOrCreateProfile(): UserProfile? {
        val user = auth.currentUser ?: return null
        val profileRef = firestore.collection("profiles").document(user.uid)

        return try {
            val snapshot = profileRef.get().await()
            if (snapshot.exists()) {
                val bestTimes = mapOf(
                    "Easy" to getSafeLong(snapshot.get("bestEasyTime")),
                    "Medium" to getSafeLong(snapshot.get("bestMediumTime")),
                    "Hard" to getSafeLong(snapshot.get("bestHardTime"))
                )
                UserProfile(
                    userId = snapshot.id,
                    displayName = snapshot.getString("displayName") ?: "Unknown",
                    coins = getSafeLong(snapshot.get("coins")),
                    unlockedWeapons = (snapshot.get("unlockedWeapons") as? List<*>)?.mapNotNull { it.toString() } ?: listOf("default_kunai"),
                    currentWeaponId = snapshot.getString("currentWeaponId") ?: "default_kunai",
                    bestTimes = bestTimes
                )
            } else {
                val newProfile = UserProfile(
                    userId = user.uid,
                    displayName = user.displayName ?: user.email?.split("@")?.get(0) ?: "Ninja",
                    coins = 0L,
                    unlockedWeapons = listOf("default_kunai"),
                    currentWeaponId = "default_kunai",
                    bestTimes = mapOf("Easy" to 0L, "Medium" to 0L, "Hard" to 0L)
                )
                val data = mapOf(
                    "displayName" to newProfile.displayName,
                    "coins" to 0L,
                    "unlockedWeapons" to listOf("default_kunai"),
                    "currentWeaponId" to "default_kunai",
                    "bestEasyTime" to 0L,
                    "bestMediumTime" to 0L,
                    "bestHardTime" to 0L
                )
                profileRef.set(data).await()
                newProfile
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun updateDisplayName(newName: String): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        return try {
            firestore.collection("profiles")
                .document(userId)
                .update("displayName", newName)
                .await()
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getLeaderboard(): List<UserProfile> {
        return try {
            val snapshot = firestore.collection("profiles").get().await()
            snapshot.documents.map { doc ->
                val bestTimes = mapOf(
                    "Easy" to getSafeLong(doc.get("bestEasyTime")),
                    "Medium" to getSafeLong(doc.get("bestMediumTime")),
                    "Hard" to getSafeLong(doc.get("bestHardTime"))
                )
                UserProfile(
                    userId = doc.id,
                    displayName = doc.getString("displayName") ?: "Unknown",
                    coins = getSafeLong(doc.get("coins")),
                    unlockedWeapons = (doc.get("unlockedWeapons") as? List<*>)?.mapNotNull { it.toString() } ?: listOf("default_kunai"),
                    currentWeaponId = doc.getString("currentWeaponId") ?: "default_kunai",
                    bestTimes = bestTimes
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }
}
