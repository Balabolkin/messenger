package com.eltex.messengerapp.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

// ===== Цвета для аватаров =====
private val initialsGradients = listOf(
    Brush.linearGradient(listOf(Color(0xFFEF5350), Color(0xFFE53935))),
    Brush.linearGradient(listOf(Color(0xFFEC407A), Color(0xFFD81B60))),
    Brush.linearGradient(listOf(Color(0xFFAB47BC), Color(0xFF8E24AA))),
    Brush.linearGradient(listOf(Color(0xFF7E57C2), Color(0xFF5E35B1))),
    Brush.linearGradient(listOf(Color(0xFF5C6BC0), Color(0xFF3949AB))),
    Brush.linearGradient(listOf(Color(0xFF42A5F5), Color(0xFF1E88E5))),
    Brush.linearGradient(listOf(Color(0xFF26A69A), Color(0xFF00897B))),
    Brush.linearGradient(listOf(Color(0xFF66BB6A), Color(0xFF43A047))),
    Brush.linearGradient(listOf(Color(0xFFFFB74D), Color(0xFFF57C00))),
    Brush.linearGradient(listOf(Color(0xFFFF8A65), Color(0xFFE64A19)))
)

/**
 * Получить градиент для имени
 */
fun getGradientForName(name: String): Brush {
    val index = if (name.isNotEmpty()) {
        Math.abs(name.hashCode()) % initialsGradients.size
    } else {
        0
    }
    return initialsGradients[index]
}

/**
 * Получить инициалы из имени
 */
fun getInitials(name: String?): String {
    if (name.isNullOrBlank()) return "?"
    val words = name.trim().split("\\s+".toRegex())
    return if (words.size == 1) {
        words[0].take(1).uppercase()
    } else {
        (words[0].take(1) + words[1].take(1)).uppercase()
    }
}

/**
 * Компонент: аватар с инициалами на цветном фоне
 */
@Composable
fun InitialsAvatar(
    name: String,
    modifier: Modifier = Modifier,
    fontSize: Int = 18,
    fontWeight: FontWeight = FontWeight.Bold
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .clip(CircleShape)
            .background(getGradientForName(name))
    ) {
        Text(
            text = getInitials(name),
            color = Color.White,
            fontSize = fontSize.sp,
            fontWeight = fontWeight
        )
    }
}