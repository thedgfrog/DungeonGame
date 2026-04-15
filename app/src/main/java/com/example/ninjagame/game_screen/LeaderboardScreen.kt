package com.example.ninjagame.game_screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ninjagame.data.FirestoreRepository
import com.example.ninjagame.game.domain.UserProfile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    val repository = remember { FirestoreRepository() }
    var leaders by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

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
        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color.Red)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {
                itemsIndexed(leaders) { index, profile ->
                    LeaderItem(index + 1, profile)
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
fun LeaderItem(rank: Int, profile: UserProfile) {
    val bgColor = when (rank) {
        1 -> Color(0xFFFFD700).copy(alpha = 0.2f) // Gold
        2 -> Color(0xFFC0C0C0).copy(alpha = 0.2f) // Silver
        3 -> Color(0xFFCD7F32).copy(alpha = 0.2f) // Bronze
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
        }

        Text(
            text = "${profile.bestSurvivalTime / 1000}s",
            color = Color.Red,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
