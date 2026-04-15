package com.example.ninjagame

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.ninjagame.Auth.ForgotPasswordScreen
import com.example.ninjagame.Auth.LoginScreen
import com.example.ninjagame.Auth.RegisterScreen
import com.google.firebase.auth.FirebaseAuth
import com.example.ninjagame.game_screen.SplashScreen

@Composable
fun GameRoot() {
    val auth = FirebaseAuth.getInstance()
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "splash"
    ) {
        composable("splash") {
            SplashScreen(
                onStartGame = {
                    val destination = if (auth.currentUser != null) "game_module" else "login"

                    navController.navigate(destination) {
                        popUpTo("splash") { inclusive = true }
                    }
                }
            )
        }

        composable("login") {
            LoginScreen(
                onLoginSuccess = {
                    navController.navigate("game_module") {
                        popUpTo("login") { inclusive = true }
                    }
                },
                onNavigateRegister = { navController.navigate("register") },
                onNavigateForgot = { navController.navigate("forgot") }
            )
        }

        composable("register") {
            RegisterScreen(
                onRegisterSuccess = { navController.popBackStack() },
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable("forgot") {
            ForgotPasswordScreen(
                onBackToLogin = { navController.popBackStack() }
            )
        }

        composable("game_module") {
            Game1App(
                onLogout = {
                    auth.signOut()
                    navController.navigate("login") {
                        popUpTo("game_module") { inclusive = true }
                    }
                }
            )
        }
    }
}