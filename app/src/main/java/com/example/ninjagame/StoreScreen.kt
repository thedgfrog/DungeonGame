package com.example.ninjagame

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.MonetizationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ninjagame.data.FirestoreRepository
import com.example.ninjagame.game.domain.StoreItem
import com.example.ninjagame.game.domain.UserProfile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(onBack: () -> Unit) {
    val repository = remember { FirestoreRepository() }
    val scope = rememberCoroutineScope()
    var profile by remember { mutableStateOf<UserProfile?>(null) }
    
    // Giả sử bạn có các drawable này. Nếu chưa có hãy thay bằng R.drawable.kunai
    val storeItems = listOf(
        StoreItem("default_kunai", "Classic Kunai", 0, R.drawable.kunai),
        StoreItem("shuriken", "Ninja Shuriken", 50, R.drawable.kunai), // Thay bằng shuriken nếu có
        StoreItem("fire_kunai", "Fire Kunai", 150, R.drawable.kunai),
        StoreItem("golden_blade", "Golden Blade", 500, R.drawable.kunai)
    )

    LaunchedEffect(Unit) {
        profile = repository.getOrCreateProfile()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("NINJA STORE", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(end = 16.dp)) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color.Yellow)
                        Text(" ${profile?.coins ?: 0}", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black, titleContentColor = Color.White)
            )
        },
        containerColor = Color(0xFF121212)
    ) { padding ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = Modifier.padding(padding).padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(storeItems) { item ->
                StoreItemCard(
                    item = item,
                    isUnlocked = profile?.unlockedWeapons?.contains(item.id) == true,
                    isUsing = profile?.currentWeaponId == item.id,
                    onBuy = {
                        scope.launch {
                            if (repository.buyItem(item.id, item.price)) {
                                profile = repository.getOrCreateProfile()
                            }
                        }
                    },
                    onUse = {
                        scope.launch {
                            if (repository.useWeapon(item.id)) {
                                profile = repository.getOrCreateProfile()
                            }
                        }
                    },
                    onUnuse = {
                        scope.launch {
                            if (repository.useWeapon("default_kunai")) {
                                profile = repository.getOrCreateProfile()
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun StoreItemCard(
    item: StoreItem,
    isUnlocked: Boolean,
    isUsing: Boolean,
    onBuy: () -> Unit,
    onUse: () -> Unit,
    onUnuse: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.05f)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = item.drawableRes),
                contentDescription = null,
                modifier = Modifier.size(80.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(item.name, color = Color.White, fontWeight = FontWeight.Bold)
            Text("${item.price} Coins", color = Color.Yellow, fontSize = 12.sp)
            Spacer(modifier = Modifier.height(12.dp))

            when {
                isUsing -> {
                    Button(
                        onClick = onUnuse,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Unuse", fontSize = 12.sp)
                    }
                }
                isUnlocked -> {
                    Button(
                        onClick = onUse,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Blue),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Use", fontSize = 12.sp)
                    }
                }
                else -> {
                    Button(
                        onClick = onBuy,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Buy", fontSize = 12.sp)
                    }
                }
            }
        }
    }
}
