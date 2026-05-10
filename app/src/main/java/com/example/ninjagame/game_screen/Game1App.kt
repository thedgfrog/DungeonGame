package com.example.ninjagame.game_screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.ninjagame.data.FirestoreRepository
import com.example.ninjagame.game_screen.GameRoute
import com.example.ninjagame.util.SoundManager
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Game1App(onLogout: () -> Unit) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val lifecycleOwner = LocalLifecycleOwner.current
    val repository = remember { FirestoreRepository() }

    val soundManager = remember { SoundManager(context) }

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            // Đã đồng bộ Token chuẩn từ google-services.json
            .requestIdToken("714915977702-8j80vglknpifvtddsmlcljhc278d3aqm.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    // Load settings from Cloud on start
    LaunchedEffect(Unit) {
        val profile = repository.getOrCreateProfile()
        profile?.settings?.let {
            soundManager.setMusicVolume(it.musicVolume)
            soundManager.setSFXVolume(it.sfxVolume)
        }
    }

    // Handle lifecycle for music
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_PAUSE -> {
                    soundManager.pauseAll()
                }
                Lifecycle.Event.ON_RESUME -> {
                    soundManager.resumeAll(isIngame = currentRoute == GameRoute.GAME)
                }
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F0F0F)
    ) {
        NavHost(
            navController = navController,
            startDestination = GameRoute.GAME
        ) {
            // --- Màn hình Game chính ---
            composable(GameRoute.GAME) {
                Box(modifier = Modifier.fillMaxSize()) {
                    MainGameScreen(soundManager)

                    // HUD Overlays
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        HUDButton(
                            icon = Icons.Default.Settings,
                            onClick = { navController.navigate(GameRoute.SETTING) }
                        )

                        HUDButton(
                            icon = Icons.Default.Person,
                            onClick = { navController.navigate(GameRoute.PROFILE) }
                        )
                        HUDButton(
                            icon = Icons.Default.EmojiEvents,
                            onClick = { navController.navigate(GameRoute.LEADERBOARD) }
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        IconButton(
                            onClick = {
                                soundManager.releaseAll()
                                // Giờ đây lệnh revokeAccess sẽ chạy đúng nhờ Token đã đồng bộ
                                googleSignInClient.revokeAccess().addOnCompleteListener {
                                    FirebaseAuth.getInstance().signOut()
                                    onLogout()
                                }
                            },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White.copy(alpha = 0.3f))
                        }
                    }
                }
            }

            // --- Màn hình Bảng xếp hạng ---
            composable(GameRoute.LEADERBOARD) {
                LeaderboardScreen(onBack = { navController.popBackStack() })
            }

            // --- Màn hình Cá nhân ---
            composable(GameRoute.PROFILE) {
                ProfileScreen(
                    onBack = { navController.popBackStack() },
                    onNavigateToStore = { navController.navigate(GameRoute.STORE) }
                )
            }

            // --- Màn hình Cửa hàng ---
            composable(GameRoute.STORE) {
                StoreScreen(onBack = { navController.popBackStack() })
            }

            // --- Màn hình setting ---
            composable(GameRoute.SETTING) {
                SettingScreen(soundManager, onBack = { navController.popBackStack() })
            }
        }
    }
}

@Composable
fun HUDButton(
    icon: ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.5f),
        modifier = Modifier.size(48.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
