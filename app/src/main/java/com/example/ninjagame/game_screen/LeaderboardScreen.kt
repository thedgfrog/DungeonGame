package com.example.ninjagame.game_screen

import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ninjagame.data.FirestoreRepository
import com.example.ninjagame.game.domain.Difficulty
import com.example.ninjagame.game.domain.UserProfile
import com.example.ninjagame.util.ImageUtil
import com.example.ninjagame.util.TextUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(onBack: () -> Unit) {
    val repository = remember { FirestoreRepository() }
    var leaders by remember { mutableStateOf<List<UserProfile>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedDifficulty by remember { mutableStateOf<Difficulty?>(null) }
    
    // Master State: Quản lý hiển thị hệ thống Chat nhanh
    var showQuickChat by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Master Sync: Đồng bộ Emoji Tokens từ Firestore (External Source)
        val emojiConfig = repository.getEmojiConfig()
        if (emojiConfig.isNotEmpty()) {
            TextUtil.updateEmojiMap(emojiConfig)
        }
        
        leaders = repository.getLeaderboard()
        isLoading = false
    }

    // Overlay hệ thống Quick Chat & Emotes
    if (showQuickChat) {
        QuickChatDialog(
            repository = repository,
            onDismiss = { showQuickChat = false }
        )
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
                actions = {
                    // Master Interaction: Nút mở bảng chọn biểu cảm
                    IconButton(onClick = { showQuickChat = true }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Chat, 
                            contentDescription = "Quick Chat", 
                            tint = Color.White
                        )
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
            
            // Dải thông báo chạy chữ: Nơi các Emotes và Kỷ lục xuất hiện real-time
            AnnouncementTicker(repository)
            
            Spacer(modifier = Modifier.height(8.dp))

            // Tabs chọn độ khó
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
                Difficulty.entries.forEach { diff ->
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
                    val filtered = if (selectedDifficulty == null) {
                        leaders.filter { it.bestTimes.values.any { time -> time > 0L } }
                    } else {
                        leaders.filter { (it.bestTimes[selectedDifficulty!!.displayName] ?: 0L) > 0L }
                    }
                    
                    filtered.sortedByDescending { 
                        if (selectedDifficulty == null) it.bestTimes.values.maxOrNull() ?: 0L
                        else it.bestTimes[selectedDifficulty!!.displayName] ?: 0L
                    }
                }

                if (displayList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.SearchOff, 
                                contentDescription = null, 
                                tint = Color.White.copy(alpha = 0.05f),
                                modifier = Modifier.size(120.dp)
                            )
                            Spacer(Modifier.height(16.dp))
                            Text(
                                "NO NINJA HAS SURVIVED YET", 
                                color = Color.Gray, 
                                fontSize = 12.sp, 
                                letterSpacing = 2.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
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
fun LeaderboardAvatar(base64: String?, rank: Int) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700) // Gold
        2 -> Color(0xFFC0C0C0) // Silver
        3 -> Color(0xFFCD7F32) // Bronze
        else -> Color.White.copy(alpha = 0.1f)
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    val bitmap = remember(base64) { ImageUtil.decodeBase64ToBitmap(base64) }

    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(56.dp)) {
        if (rank == 1) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .graphicsLayer {
                        scaleX = pulseScale
                        scaleY = pulseScale
                        alpha = (1.3f - pulseScale) * 0.8f
                    }
                    .background(rankColor, CircleShape)
            )
        }

        Surface(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape),
            color = Color.Black,
            border = BorderStroke(
                width = if (rank <= 3) 2.dp else 1.dp,
                brush = if (rank <= 3) {
                    Brush.sweepGradient(listOf(rankColor, rankColor.copy(alpha = 0.3f), rankColor))
                } else {
                    SolidColor(Color.White.copy(alpha = 0.1f))
                }
            )
        ) {
            if (bitmap != null) {
                Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else {
                Icon(
                    Icons.Default.Person,
                    contentDescription = null,
                    modifier = Modifier.padding(10.dp),
                    tint = Color.White.copy(alpha = 0.1f)
                )
            }
        }

        if (rank <= 3) {
            Surface(
                color = rankColor,
                shape = CircleShape,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(16.dp)
                    .offset(x = 2.dp, y = 2.dp),
                border = BorderStroke(1.dp, Color.Black)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(text = "$rank", color = Color.Black, fontSize = 9.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun LeaderItem(rank: Int, profile: UserProfile, currentDifficulty: Difficulty?) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700)
        2 -> Color(0xFFC0C0C0)
        3 -> Color(0xFFCD7F32)
        else -> Color.White.copy(alpha = 0.2f)
    }

    Surface(
        color = Color.White.copy(alpha = 0.03f),
        shape = RoundedCornerShape(16.dp),
        border = if (rank <= 3) BorderStroke(1.dp, rankColor.copy(alpha = 0.1f)) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "$rank",
                color = rankColor,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(32.dp)
            )

            LeaderboardAvatar(base64 = profile.profileImage, rank = rank)
            
            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = profile.displayName.ifBlank { "Anonymous" }.uppercase(),
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.sp
                )
                
                val displayTime = if (currentDifficulty != null) {
                    profile.bestTimes[currentDifficulty.displayName] ?: 0L
                } else {
                    profile.bestTimes.values.maxOrNull() ?: 0L
                }

                val title = when {
                    rank == 1 -> "LEGENDARY SHOGUN"
                    rank <= 10 -> "ELITE NINJA"
                    else -> "SURVIVOR"
                }

                Text(
                    text = "$title • ${displayTime / 1000}s",
                    color = if (rank <= 3) rankColor.copy(alpha = 0.7f) else Color.Gray,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (rank <= 3) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = rankColor, modifier = Modifier.size(20.dp))
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
        border = if (isSelected) null else BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
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
