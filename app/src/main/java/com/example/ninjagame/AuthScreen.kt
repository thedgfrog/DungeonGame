package com.example.ninjagame

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.background

@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {
    var screen by remember { mutableStateOf("login") }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F))) {
        AnimatedContent(
            targetState = screen,
            transitionSpec = {
                if (targetState == "register" || targetState == "forgot") {
                    slideInHorizontally(animationSpec = tween(400), initialOffsetX = { it }) + fadeIn() togetherWith
                            slideOutHorizontally(animationSpec = tween(400), targetOffsetX = { -it }) + fadeOut()
                } else {
                    slideInHorizontally(animationSpec = tween(400), initialOffsetX = { -it }) + fadeIn() togetherWith
                            slideOutHorizontally(animationSpec = tween(400), targetOffsetX = { it }) + fadeOut()
                }
            },
            label = "auth_transition"
        ) { target ->
            when (target) {
                "login" -> LoginScreen(
                    onLoginSuccess = onLoginSuccess,
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
}
