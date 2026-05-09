package com.example.ninjagame.game_screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import com.example.ninjagame.R
import com.example.ninjagame.data.FirestoreRepository
import com.example.ninjagame.game.domain.StoreItem
import com.example.ninjagame.game.domain.UserProfile
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StoreScreen(onBack: () -> Unit) {
    val repository = remember { FirestoreRepository() }
    val scope = rememberCoroutineScope()
    var profileState by remember { mutableStateOf<UserProfile?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Thay thế sword1 bằng black_sword vì sword1 gây lỗi nạp resource (quá lớn hoặc hỏng)
    val storeItems = remember {
        listOf(
            StoreItem("default_kunai", "Classic Kunai", 0, R.drawable.kunai),
            StoreItem("shuriken", "Ninja Shuriken", 5, R.drawable.dragon_fang_sword),
            StoreItem("fire_kunai", "Fire Kunai", 15, R.drawable.lightning_spear),
            StoreItem("golden_blade", "Golden Blade", 17, R.drawable.forged_iron_scimitar)
        )
    }

    LaunchedEffect(Unit) {
        profileState = repository.getOrCreateProfile()
        isLoading = false
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F0F0F)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                }
                Text(
                    "STORE", 
                    color = Color.White, 
                    fontWeight = FontWeight.Light, 
                    letterSpacing = 2.sp,
                    modifier = Modifier.weight(1f)
                )
                
                Surface(
                    color = Color.White.copy(alpha = 0.1f), 
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), 
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("${profileState?.coins ?: 0}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = Color.White, strokeWidth = 2.dp)
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(storeItems) { item ->
                        StoreItemCard(
                            item = item,
                            isUnlocked = profileState?.unlockedWeapons?.contains(item.id) == true || item.id == "default_kunai",
                            isUsing = profileState?.currentWeaponId == item.id,
                            onBuy = {
                                scope.launch {
                                    if (repository.buyItem(item.id, item.price)) {
                                        profileState = repository.getOrCreateProfile()
                                    }
                                }
                            },
                            onUse = {
                                scope.launch {
                                    if (repository.useWeapon(item.id)) {
                                        profileState = repository.getOrCreateProfile()
                                    }
                                }
                            },
                            onUnuse = {
                                scope.launch {
                                    if (repository.useWeapon("default_kunai")) {
                                        profileState = repository.getOrCreateProfile()
                                    }
                                }
                            }
                        )
                    }
                }
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
    Surface(
        color = Color.White.copy(alpha = 0.05f),
        shape = RoundedCornerShape(24.dp),
        border = if (isUsing) BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)) else null
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(Color.White.copy(alpha = 0.02f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = item.drawableRes),
                    contentDescription = null,
                    modifier = Modifier.size(50.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                item.name.uppercase(), 
                color = Color.White, 
                fontWeight = FontWeight.SemiBold, 
                fontSize = 12.sp, 
                letterSpacing = 1.sp,
                maxLines = 1
            )
            
            if (!isUnlocked) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.MonetizationOn, contentDescription = null, tint = Color(0xFFFFD700), modifier = Modifier.size(10.dp))
                    Text(" ${item.price}", color = Color.Gray, fontSize = 11.sp)
                }
            } else {
                Text(
                    text = if (isUsing) "ACTIVE" else "OWNED", 
                    color = if (isUsing) Color(0xFF81C784) else Color.Gray, 
                    fontSize = 9.sp, 
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val buttonModifier = Modifier.fillMaxWidth().height(36.dp)

            if (isUsing) {
                OutlinedButton(
                    onClick = onUnuse,
                    modifier = buttonModifier,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("REMOVE", fontSize = 10.sp, color = Color.White)
                }
            } else if (isUnlocked) {
                Button(
                    onClick = onUse,
                    modifier = buttonModifier,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("EQUIP", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            } else {
                Button(
                    onClick = onBuy,
                    modifier = buttonModifier,
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.1f)),
                    contentPadding = PaddingValues(0.dp)
                ) {
                    Text("BUY", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
