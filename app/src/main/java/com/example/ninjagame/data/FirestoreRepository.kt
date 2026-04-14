package com.example.ninjagame.data

import com.example.ninjagame.game.domain.GameSession
import com.example.ninjagame.game.domain.UserProfile
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirestoreRepository {
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    suspend fun saveGameSession(survivalTime: Long) {
        val userId = auth.currentUser?.uid ?: return
        val sessionId = firestore.collection("game_sessions").document().id
        
        val session = GameSession(
            sessionId = sessionId,
            userId = userId,
            survivalTime = survivalTime
        )

        try {
            firestore.collection("game_sessions")
                .document(sessionId)
                .set(session)
                .await()

            updateBestScore(userId, survivalTime)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun updateBestScore(userId: String, currentTime: Long) {
        val profileRef = firestore.collection("profiles").document(userId)
        
        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(profileRef)
            val currentBest = snapshot.getLong("bestSurvivalTime") ?: 0L
            
            if (currentTime > currentBest) {
                transaction.set(
                    profileRef,
                    mapOf("bestSurvivalTime" to currentTime),
                    SetOptions.merge()
                )
            }
        }.await()
    }
    
    suspend fun getOrCreateProfile(): UserProfile? {
        val user = auth.currentUser ?: return null
        val profileRef = firestore.collection("profiles").document(user.uid)
        
        return try {
            val snapshot = profileRef.get().await()
            if (snapshot.exists()) {
                snapshot.toObject(UserProfile::class.java)
            } else {
                val newProfile = UserProfile(
                    userId = user.uid,
                    displayName = user.displayName ?: user.email?.split("@")?.get(0) ?: "Ninja",
                    bestSurvivalTime = 0L
                )
                profileRef.set(newProfile).await()
                newProfile
            }
        } catch (e: Exception) {
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
            firestore.collection("profiles")
                .orderBy("bestSurvivalTime", Query.Direction.DESCENDING)
                .limit(10)
                .get()
                .await()
                .toObjects(UserProfile::class.java)
        } catch (e: Exception) {
            emptyList()
        }
    }
}
