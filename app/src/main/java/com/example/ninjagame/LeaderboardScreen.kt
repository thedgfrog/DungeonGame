package com.example.ninjagame

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
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
                title = { Text("LEADERBOARD", fontWeight = FontWeight.Light, letterSpacing = 2.sp) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    titleContentColor = Color.White
                )
            )
        },
        containerColor = Color(0xFF0F0F0F)
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            // Difficulty Selection Tabs
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DifficultyTab(
                    text = "ALL",
                    isSelected = selectedDifficulty == null,
                    onClick = { selectedDifficulty = null },
                    modifier = Modifier.weight(1f)
                )
                Difficulty.values().forEach { diff ->
                    DifficultyTab(
                        text = diff.displayName.uppercase(),
                        isSelected = selectedDifficulty == diff,
                        onClick = { selectedDifficulty = diff },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                }
            } else {
                val displayList = remember(leaders, selectedDifficulty) {
                    if (selectedDifficulty == null) {
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
                }

                if (displayList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No records found", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        itemsIndexed(displayList) { index, profile ->
                            LeaderItem(
                                rank = index + 1,
                                profile = profile,
                                currentDifficulty = selectedDifficulty
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DifficultyTab(text: String, isSelected: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) Color.White.copy(alpha = 0.1f) else Color.Transparent,
        border = if (isSelected) null else androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                color = if (isSelected) Color.White else Color.Gray,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                letterSpacing = 1.sp
            )
        }
    }
}

@Composable
fun LeaderItem(
    rank: Int,
    profile: UserProfile,
    currentDifficulty: Difficulty?
) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> Color.White.copy(alpha = 0.2f)
    }

    Surface(
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(16.dp),
        border = if (rank <= 3) androidx.compose.foundation.BorderStroke(1.dp, rankColor.copy(alpha = 0.1f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rank",
                color = rankColor,
                fontSize = 18.sp,
                fontWeight = FontWeight.Light,
                modifier = Modifier.width(40.dp)
            )

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.displayName.ifBlank { "Anonymous" }.uppercase(),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                
                if (currentDifficulty == null) {
                    val maxTime = profile.bestTimes?.values?.maxOrNull() ?: 0L
                    Text(
                        text = "BEST: ${maxTime / 1000}s",
                        color = Color.Gray,
                        fontSize = 11.sp
                    )
                }
            }

            val displayTime = if (currentDifficulty != null) {
                profile.bestTimes?.get(currentDifficulty.displayName) ?: 0L
            } else {
                profile.bestTimes?.values?.maxOrNull() ?: 0L
            }

            Text(
                text = "${displayTime / 1000}s",
                color = Color.White,
                fontSize = 16.sp,
                fontWeight = FontWeight.Light
            )
            
            if (rank <= 3) {
                Spacer(Modifier.width(8.dp))
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = rankColor, modifier = Modifier.size(16.dp))
            }
        }
    }
}
