package com.example.ninjagame.game_screen

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ninjagame.data.FirestoreRepository
import com.example.ninjagame.game.domain.Announcement
import com.example.ninjagame.util.TextUtil
import kotlinx.coroutines.delay

@Composable
fun AnnouncementTicker(repository: FirestoreRepository) {
    val announcements by repository.getAnnouncements().collectAsState(initial = emptyList())
    var currentIndex by remember { mutableIntStateOf(0) }
    val currentAnnouncement = announcements.getOrNull(currentIndex)

    // Tự động chuyển đổi thông báo sau mỗi 5 giây
    LaunchedEffect(announcements) {
        while (announcements.isNotEmpty()) {
            delay(5000)
            currentIndex = (currentIndex + 1) % announcements.size
        }
    }

    AnimatedVisibility(
        visible = currentAnnouncement != null,
        enter = fadeIn() + slideInVertically(),
        exit = fadeOut() + slideOutVertically()
    ) {
        currentAnnouncement?.let { announcement ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp, 
                    Brush.horizontalGradient(listOf(Color.White.copy(alpha = 0.1f), Color.Transparent))
                )
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Campaign,
                        contentDescription = null,
                        tint = when(announcement.type) {
                            "RECORD" -> Color(0xFFFFD700)
                            "STORE" -> Color(0xFF81C784)
                            else -> Color.White.copy(alpha = 0.5f)
                        },
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = TextUtil.parseAnnouncement(announcement.message),
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        letterSpacing = 0.5.sp,
                        maxLines = 1
                    )
                }
            }
        }
    }
}
