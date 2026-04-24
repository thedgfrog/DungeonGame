package com.example.ninjagame

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ninjagame.game_screen.MainGameScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Game1App(onLogout: () -> Unit) {
    val context = LocalContext.current
    var currentScreen by remember { mutableStateOf("game") }

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("984213734238-p9dpe3np4uhgn4icr1k4jlbo83eeguc4.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    BackHandler(enabled = currentScreen != "game") {
        when (currentScreen) {
            "store" -> currentScreen = "profile"
            "profile", "leaderboard" -> currentScreen = "game"
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F0F0F)
    ) {
        when (currentScreen) {
            "leaderboard" -> LeaderboardScreen(onBack = { currentScreen = "game" })
            "profile" -> ProfileScreen(
                onBack = { currentScreen = "game" },
                onNavigateToStore = { currentScreen = "store" }
            )
            "store" -> StoreScreen(onBack = { currentScreen = "profile" })
            else -> {
                Box(modifier = Modifier.fillMaxSize()) {
                    MainGameScreen()

                    // Minimalistic HUD Overlays
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalAlignment = Alignment.End
                    ) {
                        HUDButton(
                            icon = Icons.Default.Person,
                            onClick = { currentScreen = "profile" }
                        )
                        HUDButton(
                            icon = Icons.Default.EmojiEvents,
                            onClick = { currentScreen = "leaderboard" }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        IconButton(
                            onClick = {
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
        }
    }
}

@Composable
fun HUDButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = CircleShape,
        color = Color.Black.copy(alpha = 0.5f),
        modifier = Modifier.size(48.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
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
