package com.example.ninjagame.data

import com.example.ninjagame.game.domain.Difficulty
import com.example.ninjagame.game.domain.GameSession
import com.example.ninjagame.game.domain.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun saveGameSession(survivalTime: Long, coinsEarned: Int, difficulty: Difficulty) {
        val userId = auth.currentUser?.uid ?: return
        val sessionId = firestore.collection("game_sessions").document().id

        val session = GameSession(
            sessionId = sessionId,
            userId = userId,
            survivalTime = survivalTime,
            coinsEarned = coinsEarned,
            difficulty = difficulty.name // lưu dưới dạng String
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

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(profileRef)
            val currentBest = snapshot.getLong(bestField) ?: 0L

            val updates = mutableMapOf<String, Any>(
                "coins" to FieldValue.increment(coins.toLong())
            )

            if (currentTime > currentBest) {
                updates[bestField] = currentTime
            }

            transaction.set(profileRef, updates, SetOptions.merge())
        }.await()
    }
    
    suspend fun buyItem(itemId: String, price: Int): Boolean {
        val userId = auth.currentUser?.uid ?: return false
        val profileRef = firestore.collection("profiles").document(userId)

        return try {
            firestore.runTransaction { transaction ->
                val snapshot = transaction.get(profileRef)
                val currentCoins = snapshot.getLong("coins") ?: 0L
                val unlocked = snapshot.get("unlockedWeapons") as? List<String> ?: listOf("default_kunai")

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
                // Nếu profile đã tồn tại, map dữ liệu cũ sang UserProfile mới
                val bestTimes = mapOf(
                    "Easy" to (snapshot.getLong("bestEasyTime") ?: 0L),
                    "Medium" to (snapshot.getLong("bestMediumTime") ?: 0L),
                    "Hard" to (snapshot.getLong("bestHardTime") ?: 0L)
                )
                UserProfile(
                    userId = snapshot.id,
                    displayName = snapshot.getString("displayName") ?: "Unknown",
                    coins = snapshot.getLong("coins") ?: 0L,
                    unlockedWeapons = snapshot.get("unlockedWeapons") as? List<String> ?: listOf("default_kunai"),
                    currentWeaponId = snapshot.getString("currentWeaponId") ?: "default_kunai",
                    bestTimes = bestTimes
                )
            } else {
                // Nếu chưa có profile, tạo mới
                val newProfile = UserProfile(
                    userId = user.uid,
                    displayName = user.displayName ?: user.email?.split("@")?.get(0) ?: "Ninja",
                    coins = 0L,
                    unlockedWeapons = listOf("default_kunai"),
                    currentWeaponId = "default_kunai",
                    bestTimes = mapOf(
                        "Easy" to 0L,
                        "Medium" to 0L,
                        "Hard" to 0L
                    )
                )
                // Lưu các giá trị mặc định vào Firestore
                val data = mapOf(
                    "displayName" to newProfile.displayName,
                    "coins" to newProfile.coins,
                    "unlockedWeapons" to newProfile.unlockedWeapons,
                    "currentWeaponId" to newProfile.currentWeaponId,
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
            val snapshot = firestore.collection("profiles")
                .get()
                .await()

            snapshot.documents.map { doc ->
                val bestTimes = mapOf(
                    "Easy" to (doc.getLong("bestEasyTime") ?: 0L),
                    "Medium" to (doc.getLong("bestMediumTime") ?: 0L),
                    "Hard" to (doc.getLong("bestHardTime") ?: 0L)
                )
                UserProfile(
                    userId = doc.id,
                    displayName = doc.getString("displayName") ?: "Unknown",
                    coins = doc.getLong("coins") ?: 0L,
                    unlockedWeapons = doc.get("unlockedWeapons") as? List<String> ?: listOf("default_kunai"),
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
