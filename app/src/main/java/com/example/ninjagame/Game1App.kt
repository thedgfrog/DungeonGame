package com.example.ninjagame

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ninjagame.game_screen.BouncyGameButton
import com.example.ninjagame.game_screen.BouncyIconButton
import com.example.ninjagame.game_screen.LeaderboardScreen
import com.example.ninjagame.game_screen.MainGameScreen
import com.example.ninjagame.game_screen.ProfileScreen
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth

@Composable
fun Game1App(onLogout: () -> Unit) {
    val context = LocalContext.current
    val navController = rememberNavController()

    val googleSignInClient = remember {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken("984213734238-p9dpe3np4uhgn4icr1k4jlbo83eeguc4.apps.googleusercontent.com")
            .requestEmail()
            .build()
        GoogleSignIn.getClient(context, gso)
    }

    NavHost(
        navController = navController,
        startDestination = "game"
    ) {
        composable("game") {
            DashboardScreen(
                onNavigate = { route -> navController.navigate(route) },
                onLogout = {
                    googleSignInClient.revokeAccess().addOnCompleteListener {
                        FirebaseAuth.getInstance().signOut()
                        onLogout()
                    }
                }
            )
        }

        composable("leaderboard") {
            LeaderboardScreen(onBack = { navController.popBackStack() })
        }

        composable("profile") {
            ProfileScreen(onBack = { navController.popBackStack() },
                onNavigateToStore = { navController.navigate("store") }
            )
        }

        composable("store") {
            StoreScreen(onBack = { navController.popBackStack() })
        }

        composable("settings") {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Settings Screen")
                    Button(onClick = { navController.popBackStack() }) { Text("Back") }
                }
            }
        }
    }
}

@Composable
fun DashboardScreen(
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Ảnh nền
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_background),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Game Screen chính
        MainGameScreen()

        // Cụm điều hướng
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(top = 20.dp, end = 20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            BouncyIconButton(
                icon = Icons.Default.Settings,
                containerColor = MaterialTheme.colorScheme.tertiaryContainer,
                onClick = { onNavigate("settings") }
            )

            Spacer(modifier = Modifier.width(12.dp))

            BouncyIconButton(
                icon = Icons.Default.Person,
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                onClick = { onNavigate("profile") }
            )

            Spacer(modifier = Modifier.width(12.dp))

            BouncyIconButton(
                icon = Icons.Default.List,
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = { onNavigate("leaderboard") }
            )

            Spacer(modifier = Modifier.width(12.dp))

            BouncyGameButton(text = "Logout", isLarge = false) {
                onLogout()
            }
        }
    }
}