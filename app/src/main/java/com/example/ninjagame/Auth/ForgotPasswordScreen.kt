package com.example.ninjagame.Auth

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit
) {

    val auth = FirebaseAuth.getInstance()

    var email by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {

        Text("RESET PASSWORD")

        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {

                if (email.isBlank()) {
                    msg = "Email required"
                    return@Button
                }

                auth.sendPasswordResetEmail(email)
                    .addOnCompleteListener {
                        msg = if (it.isSuccessful) {
                            "Reset email sent"
                        } else {
                            it.exception?.message ?: "Error"
                        }
                    }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Send reset email")
        }

        TextButton(onClick = onBackToLogin) {
            Text("Back to login")
        }

        if (msg.isNotEmpty()) {
            Spacer(Modifier.height(10.dp))
            Text(msg)
        }
    }
}