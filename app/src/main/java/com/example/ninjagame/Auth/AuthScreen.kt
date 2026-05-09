package com.example.ninjagame.Auth

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Color

// Định nghĩa các tên màn hình (Routes)
object AuthRoute {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val FORGOT_PASSWORD = "forgot"
}

@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {
    val navController = rememberNavController()

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0F0F0F))) {
        NavHost(
            navController = navController,
            startDestination = AuthRoute.LOGIN,
        ) {
            // Màn hình Đăng nhập
            composable(AuthRoute.LOGIN) {
                LoginScreen(
                    onLoginSuccess = onLoginSuccess,
                    onNavigateRegister = { navController.navigate(AuthRoute.REGISTER) },
                    onNavigateForgot = { navController.navigate(AuthRoute.FORGOT_PASSWORD) }
                )
            }

            // Màn hình Đăng ký
            composable(AuthRoute.REGISTER) {
                RegisterScreen(
                    onRegisterSuccess = {
                        // Sau khi đăng ký xong quay về login
                        navController.popBackStack()
                    },
                    onBackToLogin = { navController.popBackStack() }
                )
            }

            // Màn hình Quên mật khẩu
            composable(AuthRoute.FORGOT_PASSWORD) {
                ForgotPasswordScreen(
                    onBackToLogin = { navController.popBackStack() }
                )
            }
        }
    }
}