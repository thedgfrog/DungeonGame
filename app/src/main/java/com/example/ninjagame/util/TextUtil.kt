package com.example.ninjagame.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle

object TextUtil {
    // Trong thực tế, map này có thể được fetch từ Firebase Remote Config
    private var externalEmojiMap = mapOf(
        "{medal}" to "🥇",
        "{ninja}" to "🥷",
        "{fire}" to "🔥",
        "{crown}" to "👑",
        "{swords}" to "⚔️",
        "{star}" to "⭐"
    )

    fun updateEmojiMap(newMap: Map<String, String>) {
        externalEmojiMap = newMap
    }

    fun parseAnnouncement(message: String): AnnotatedString {
        var processedMessage = message
        externalEmojiMap.forEach { (token, emoji) ->
            processedMessage = processedMessage.replace(token, emoji)
        }

        return buildAnnotatedString {
            val words = processedMessage.split(" ")
            words.forEachIndexed { index, word ->
                when {
                    // Highlight tên Ninja
                    word.startsWith("Ninja", ignoreCase = true) -> {
                        withStyle(style = SpanStyle(color = Color(0xFFFFD700), fontWeight = FontWeight.ExtraBold)) {
                            append(word)
                        }
                    }
                    // Highlight thông số thời gian (ví dụ: 120s)
                    word.contains(Regex("\\d+s")) -> {
                        withStyle(style = SpanStyle(color = Color(0xFFE57373), fontWeight = FontWeight.Bold)) {
                            append(word)
                        }
                    }
                    // Highlight độ khó
                    word.equals("Hard", true) || word.equals("Medium", true) -> {
                        withStyle(style = SpanStyle(color = Color(0xFF81C784), fontWeight = FontWeight.Bold)) {
                            append(word)
                        }
                    }
                    else -> append(word)
                }
                if (index < words.size - 1) append(" ")
            }
        }
    }
}
