package com.example.ninjagame.game_screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ninjagame.data.FirestoreRepository
import com.example.ninjagame.game.domain.UserSettings
import com.example.ninjagame.util.SoundManager
import kotlinx.coroutines.launch


@Composable
fun SettingScreen(soundManager: SoundManager, onBack: () -> Unit) {
    val repository = remember { FirestoreRepository() }
    val scope = rememberCoroutineScope()
    
    var musicVolume by remember { mutableFloatStateOf(soundManager.getMusicVolume()) }
    var sfxVolume by remember { mutableFloatStateOf(soundManager.getSFXVolume()) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFF0F0F0F)
    ) {
        Column(
            modifier = Modifier.padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("SETTINGS", color = Color.White, fontSize = 32.sp, fontWeight = FontWeight.Light)

            Spacer(modifier = Modifier.height(48.dp))

            // Chỉnh nhạc nền (BGM)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("MUSIC VOLUME", color = Color.Gray, fontSize = 12.sp)
                Text("${(musicVolume * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
            }
            Slider(
                value = musicVolume,
                onValueChange = { newValue ->
                    musicVolume = newValue
                    soundManager.setMusicVolume(newValue)
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Chỉnh hiệu ứng (SFX)
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("SFX VOLUME", color = Color.Gray, fontSize = 12.sp)
                Text("${(sfxVolume * 100).toInt()}%", color = Color.White, fontSize = 12.sp)
            }
            Slider(
                value = sfxVolume,
                onValueChange = { newValue ->
                    sfxVolume = newValue
                    soundManager.setSFXVolume(newValue)
                },
                colors = SliderDefaults.colors(
                    thumbColor = Color.White,
                    activeTrackColor = Color.White,
                    inactiveTrackColor = Color.White.copy(alpha = 0.2f)
                )
            )

            Spacer(modifier = Modifier.height(48.dp))

            Button(
                onClick = {
                    scope.launch {
                        repository.updateSettings(
                            UserSettings(
                                musicVolume = musicVolume,
                                sfxVolume = sfxVolume
                            )
                        )
                        onBack()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.White, contentColor = Color.Black)
            ) {
                Text("DONE", fontWeight = FontWeight.Bold)
            }
        }
    }
}
