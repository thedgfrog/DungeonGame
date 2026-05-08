package com.example.ninjagame

import androidx.compose.runtime.*
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
        AuthScreen(
            onLoginSuccess = { 
                isLoggedIn = true 
            }
        )
    }
}
