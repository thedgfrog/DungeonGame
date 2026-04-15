package com.example.ninjagame

import androidx.compose.runtime.*
import com.google.firebase.auth.FirebaseAuth

@Composable
fun GameRoot() {

    val auth = FirebaseAuth.getInstance()

    var isLoggedIn by remember { mutableStateOf(auth.currentUser != null) }
    var screen by remember { mutableStateOf("login") }

    if (isLoggedIn) {

        Game1App(
            onLogout = {
                auth.signOut()
                isLoggedIn = false
                screen = "login"
            }
        )

    } else {

        when (screen) {

            "login" -> LoginScreen(
                onLoginSuccess = { isLoggedIn = true },
                onNavigateRegister = { screen = "register" },
                onNavigateForgot = { screen = "forgot" }
            )

            "register" -> RegisterScreen(
                onRegisterSuccess = { screen = "login" },
                onBackToLogin = { screen = "login" }
            )

            "forgot" -> ForgotPasswordScreen(
                onBackToLogin = { screen = "login" }
            )
        }
    }
}