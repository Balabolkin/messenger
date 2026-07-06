package com.eltex.messengerapp.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * StyleGuide - цвета приложения
 */
object AppColors {
    // Main colors
    val Primary = Color(0xFF2196F3)
    val PrimaryDark = Color(0xFF0D47A1)
    val White = Color.White
    val Black = Color.Black

    val BackgroundLight = Color(0xFFF7F8F9)
    val Divider = Color(0xFFEEEEEE)
    val OnlineGreen = Color(0xFF25CBA3)
    val ErrorBackground = Color(0xFFFFEBEE)

    // Avatars gradients
    val AvatarGradients = listOf(
        Color(0xFFEF5350) to Color(0xFFE53935),
        Color(0xFFEC407A) to Color(0xFFD81B60),
        Color(0xFFAB47BC) to Color(0xFF8E24AA),
        Color(0xFF7E57C2) to Color(0xFF5E35B1),
        Color(0xFF5C6BC0) to Color(0xFF3949AB),
        Color(0xFF42A5F5) to Color(0xFF1E88E5),
        Color(0xFF26A69A) to Color(0xFF00897B),
        Color(0xFF66BB6A) to Color(0xFF43A047),
        Color(0xFFFFB74D) to Color(0xFFF57C00),
        Color(0xFFFF8A65) to Color(0xFFE64A19)
    )

    // Text
    val TextPrimary = Color(0xFF1F1F1F)      // hsba(0, 0%, 12%, 1) - темный
    val TextSecondary = Color(0xFF878787)    // hsba(0, 0%, 53%, 1) - серый
    val TextWhite = Color.White

    // Errors
    val Error = Color.Red
    val ErrorContainer = Color.White.copy(alpha = 0.9f)

    // Opacity
    val OverlayDark = Color(0xCC000000)      // 80% черный
    val OverlayMedium = Color(0x88000000)    // 53% черный

    // Buttons
    val ButtonEnabled = Color.White
    val ButtonDisabled = Color.White.copy(alpha = 0.5f)
    val ButtonTextEnabled = Color(0xFF1F1F1F)    // темный
    val ButtonTextDisabled = Color(0xFF1F1F1F).copy(alpha = 0.5f)

    // Inputs
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