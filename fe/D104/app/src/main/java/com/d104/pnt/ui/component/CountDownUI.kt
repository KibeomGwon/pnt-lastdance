package com.d104.pnt.ui.component

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.d104.pnt.ui.theme.PixelFont

@Composable
fun ContDownUI(
    remainingSeconds: Int,
    themeColor: Color
) {
    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeText = String.format("%02d:%02d", minutes, seconds)

    val haptic = LocalHapticFeedback.current

    LaunchedEffect(remainingSeconds) {
        if (remainingSeconds in 1..10) {
            haptic.performHapticFeedback(HapticFeedbackType.Reject)
        }
    }

    Text(
        text = timeText,
        color = themeColor,
        fontSize = 60.sp,
        fontFamily = PixelFont,
        fontWeight = FontWeight.Bold,
        letterSpacing = 2.sp,
    )
}
