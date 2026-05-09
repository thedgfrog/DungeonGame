package com.example.ninjagame

import androidx.compose.runtime.*
import com.example.ninjagame.game_screen.Game1App
import com.google.firebase.auth.FirebaseAuth

@Composable
fun GameRoot() {
    val auth = FirebaseAuth.getInstance()
    var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }

    if (isLoggedIn) {
        Game1App(
            onLogout = {
                isLoggedIn = false
            }
        )
    } else {
        _root_ide_package_.com.example.ninjagame.Auth.AuthScreen(
            onLoginSuccess = {
                isLoggedIn = true
            }
        )
    }
}
