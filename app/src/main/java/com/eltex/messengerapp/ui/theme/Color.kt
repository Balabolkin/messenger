package com.eltex.messengerapp.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * StyleGuide - цвета приложения
 */
object AppColors {
    // Основные цвета
    val Primary = Color(0xFF2196F3)
    val PrimaryDark = Color(0xFF0D47A1)
    val White = Color.White
    val Black = Color.Black

    // Текст
    val TextPrimary = Color(0xFF1F1F1F)      // hsba(0, 0%, 12%, 1) - темный
    val TextSecondary = Color(0xFF878787)    // hsba(0, 0%, 53%, 1) - серый
    val TextWhite = Color.White

    // Ошибки
    val Error = Color.Red
    val ErrorContainer = Color.White.copy(alpha = 0.9f)

    // Прозрачности
    val OverlayDark = Color(0xCC000000)      // 80% черный
    val OverlayMedium = Color(0x88000000)    // 53% черный

    // Кнопка
    val ButtonEnabled = Color.White
    val ButtonDisabled = Color.White.copy(alpha = 0.5f)
    val ButtonTextEnabled = Color(0xFF1F1F1F)    // темный
    val ButtonTextDisabled = Color(0xFF1F1F1F).copy(alpha = 0.5f)

    // Поля ввода
    val InputBackground = Color.White.copy(alpha = 0.9f)
    val InputBackgroundUnfocused = Color.White.copy(alpha = 0.8f)
    val InputLabel = Color(0xFF878787)       // серый
    val InputText = Color.Black
}
val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val BrandDark = Color(0xFF1B416B)

val BrandMinor = Color(0xFFEDF4F7)

val BrandPrimary = Color(0xFF329BDE)