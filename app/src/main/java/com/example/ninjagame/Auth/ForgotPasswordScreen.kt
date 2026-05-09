package com.example.ninjagame.Auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ForgotPasswordScreen(
    onBackToLogin: () -> Unit
) {
    val auth = FirebaseAuth.getInstance()
    var email by remember { mutableStateOf("") }
    var msg by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F0F0F)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(32.dp))
            
            IconButton(onClick = onBackToLogin) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "RECOVER",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Light,
                    color = Color.White,
                    letterSpacing = 4.sp
                )
                
                Text(
                    "RESET YOUR PASSWORD",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 2.sp
                )

                Spacer(Modifier.height(48.dp))

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("EMAIL", fontSize = 12.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedBorderColor = Color.White,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedLabelColor = Color.White,
                        unfocusedLabelColor = Color.Gray
                    )
                )

                if (msg.isNotEmpty()) {
                    Spacer(Modifier.height(16.dp))
                    Text(msg, color = if (msg.contains("sent")) Color.Green else Color.Red, fontSize = 12.sp)
                }

                Spacer(Modifier.height(32.dp))

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
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
                ) {
                    Text("SEND RESET LINK", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
