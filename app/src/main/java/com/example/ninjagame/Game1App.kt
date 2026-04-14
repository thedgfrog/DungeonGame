package com.example.ninjagame

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
        val gso = GoogleSignInOptions.Builder(
            GoogleSignInOptions.DEFAULT_SIGN_IN
        )
            .requestIdToken("984213734238-p9dpe3np4uhgn4icr1k4jlbo83eeguc4.apps.googleusercontent.com")
            .requestEmail()
            .build()

        GoogleSignIn.getClient(context, gso)
    }

    when (currentScreen) {
        "leaderboard" -> LeaderboardScreen(onBack = { currentScreen = "game" })
        "profile" -> ProfileScreen(onBack = { currentScreen = "game" })
        else -> {
            Box(
                modifier = Modifier.fillMaxSize()
            ) {

                MainGameScreen()

                Row(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Nút Profile
                    IconButton(
                        onClick = { currentScreen = "profile" },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Icon(Icons.Default.Person, contentDescription = "Profile")
                    }

                    // Nút Leaderboard
                    IconButton(
                        onClick = { currentScreen = "leaderboard" },
                        colors = IconButtonDefaults.iconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Icon(Icons.Default.List, contentDescription = "Leaderboard")
                    }

                    // Nút Logout
                    Button(
                        onClick = {
                            googleSignInClient.revokeAccess().addOnCompleteListener {
                                FirebaseAuth.getInstance().signOut()
                                onLogout()
                            }
                        }
                    ) {
                        Text("Logout")
                    }
                }
            }
        }
    }
}
