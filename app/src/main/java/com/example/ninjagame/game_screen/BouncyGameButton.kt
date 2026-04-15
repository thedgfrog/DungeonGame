package com.example.ninjagame.game_screen

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BouncyGameButton(
    text: String,
    isLarge: Boolean = false,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Hiệu ứng scale: Khi nhấn giảm xuống 0.9 (nhún), khi thả ra nảy về 1.0
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "BouncyAnimation"
    )

    Button(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .scale(scale)
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Red,
            contentColor = Color.White
        )
    ) {
        Text(
            text = text,
            fontSize = if (isLarge) 28.sp else 18.sp,
            fontWeight = if (isLarge) FontWeight.Bold else FontWeight.Normal,
            modifier = Modifier.padding(
                horizontal = if (isLarge) 32.dp else 16.dp,
                vertical = if (isLarge) 12.dp else 8.dp
            )
        )
    }
}

@Composable
fun BouncyIconButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.85f else 1f,
        animationSpec = spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow)
    )

    IconButton(
        onClick = onClick,
        interactionSource = interactionSource,
        modifier = Modifier
            .scale(scale)
            .size(56.dp), // Kích thước nút tròn
        colors = IconButtonDefaults.iconButtonColors(containerColor = containerColor)
    ) {
        Icon(icon, contentDescription = null, tint = Color.White)
    }
}