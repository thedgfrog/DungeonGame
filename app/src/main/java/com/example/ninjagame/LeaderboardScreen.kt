package com.example.ninjagame

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ninjagame.data.FirestoreRepository
import com.example.ninjagame.game.domain.Difficulty
import com.example.ninjagame.game.domain.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    val repository = remember { FirestoreRepository() }
    var leaders by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedDifficulty by remember { mutableStateOf<Difficulty?>(null) }

    LaunchedEffect(Unit) {
        leaders = repository.getLeaderboard()
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("LEADERBOARD", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Black,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Buttons chọn độ khó
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // All button
                Button(
                    onClick = { selectedDifficulty = null },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedDifficulty == null) Color.Green else Color.Gray
                    )
                ) {
                    Text("All")
                }

                // Buttons cho từng độ khó
                Difficulty.values().forEach { diff ->
                    Button(
                        onClick = { selectedDifficulty = diff },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selectedDifficulty == diff) Color.Green else Color.Gray
                        )
                    ) {
                        Text(diff.displayName)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.Red)
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    // Lọc leaderboard theo độ khó
                    val displayList = if (selectedDifficulty == null) {
                        // All: chỉ lấy profile có ít nhất 1 thời gian > 0
                        leaders.filter { profile ->
                            profile.bestTimes?.values?.any { it > 0L } == true
                        }.sortedByDescending { profile ->
                            profile.bestTimes?.values?.maxOrNull() ?: 0L
                        }

                    } else {
                        leaders.filter { profile ->
                            (profile.bestTimes?.get(selectedDifficulty!!.displayName) ?: 0L) > 0L
                        }.sortedByDescending { profile ->
                            profile.bestTimes?.get(selectedDifficulty!!.displayName) ?: 0L
                        }
                    }

                    itemsIndexed(displayList) { index, profile ->
                        LeaderItem(
                            rank = index + 1,
                            profile = profile,
                            showDifficultyTimes = selectedDifficulty == null,
                            difficulty = selectedDifficulty ?: Difficulty.EASY
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun LeaderItem(
    rank: Int,
    profile: UserProfile,
    difficulty: Difficulty = Difficulty.EASY,
    showDifficultyTimes: Boolean = false // true nếu đang All
) {
    val bgColor = when (rank) {
        1 -> Color(0xFFFFD700).copy(alpha = 0.2f)
        2 -> Color(0xFFC0C0C0).copy(alpha = 0.2f)
        3 -> Color(0xFFCD7F32).copy(alpha = 0.2f)
        else -> Color.White.copy(alpha = 0.05f)
    }

    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> Color.Gray
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor, RoundedCornerShape(12.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "#$rank",
            color = rankColor,
            fontSize = 20.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.width(50.dp)
        )

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = profile.displayName,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )

            if (showDifficultyTimes) {
                Difficulty.values().forEach { diff ->
                    val time = profile.bestTimes?.get(diff.displayName) ?: 0L
                    if (time > 0L) {
                        Text(
                            text = "${diff.displayName}: ${time / 1000}s",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        if (!showDifficultyTimes) {
            val bestTime = profile.bestTimes?.get(difficulty.displayName) ?: 0L
            if (bestTime > 0L) {
                Text(
                    text = "${bestTime / 1000}s",
                    color = Color.Red,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}