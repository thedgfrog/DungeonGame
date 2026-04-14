import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.ninjagame.RegisterScreen
import com.example.ninjagame.LoginScreen
import com.example.ninjagame.ForgotPasswordScreen


@Composable
fun AuthScreen(onLoginSuccess: () -> Unit) {

    var screen by remember { mutableStateOf("login") }

    AnimatedContent(
        targetState = screen,
        transitionSpec = {
            slideInHorizontally(
                animationSpec = tween(400),
                initialOffsetX = { it }
            ) togetherWith slideOutHorizontally(
                animationSpec = tween(400),
                targetOffsetX = { -it }
            )
        },
        label = "auth"
    ) { target ->

        Box(
            modifier = Modifier.fillMaxSize()
        ) {

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