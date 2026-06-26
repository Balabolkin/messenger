package com.eltex.messengerapp.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.eltex.messengerapp.ui.theme.BrandMinor

@Composable
fun ProfileAvatar(
    initials: String,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(118.dp)
            .clip(CircleShape)
            .background(Color(0xFFB8B8B8))
            .border(
                color = BrandMinor,
                width = 2.dp,
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = initials,
            color = Color.Black,
            fontSize = 32.sp,
            lineHeight = 60.sp,
            letterSpacing = 0.sp,
            fontWeight = FontWeight(600),
            textAlign = TextAlign.Center

        )
    }
}