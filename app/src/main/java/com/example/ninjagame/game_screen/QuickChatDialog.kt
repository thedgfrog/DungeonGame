package com.example.ninjagame.game_screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.ninjagame.data.FirestoreRepository
import com.example.ninjagame.util.TextUtil
import kotlinx.coroutines.launch

@Composable
fun QuickChatDialog(
    repository: FirestoreRepository,
    onDismiss: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var quickMessages by remember { mutableStateOf<List<Map<String, String>>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Master Touch: Load danh sách chat từ Source bên ngoài (Firestore)
    LaunchedEffect(Unit) {
        val configs = repository.getQuickChatConfigs()
        if (configs.isNotEmpty()) {
            quickMessages = configs
        } else {
            // Fallback nếu server lỗi
            quickMessages = listOf(
                mapOf("label" to "Greeting", "content" to "{ninja} Chào các đồng môn!"),
                mapOf("label" to "Challenge", "content" to "{swords} Thử thách tôi đi!")
            )
        }
        isLoading = false
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFF1A1A1A),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "NINJA SIGNALS",
                    color = Color.White,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.heightIn(max = 300.dp)
                    ) {
                        items(quickMessages) { msg ->
                            val label = msg["label"] ?: ""
                            val content = msg["content"] ?: ""
                            
                            Surface(
                                onClick = {
                                    scope.launch {
                                        val profile = repository.getOrCreateProfile()
                                        val displayName = profile?.displayName ?: "Guest"
                                        repository.postAnnouncement("Ninja $displayName: $content", "INFO")
                                        onDismiss()
                                    }
                                },
                                shape = RoundedCornerShape(16.dp),
                                color = Color.White.copy(alpha = 0.05f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                            ) {
                                Box(
                                    modifier = Modifier.padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = label.uppercase(),
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                TextButton(onClick = onDismiss) {
                    Text("CANCEL", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}
