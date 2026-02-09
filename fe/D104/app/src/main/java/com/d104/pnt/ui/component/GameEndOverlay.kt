package com.d104.pnt.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.d104.pnt.ui.theme.PixelFont

@Composable
fun GameEndOverlay() {
    val policeRed = Color(0xFFB71C1C)
    val policeNavy = Color(0xFF1A237E)
    val amberLight = Color(0xFFFFB300)

    val arcadeColors = listOf(
        policeRed,
        amberLight,
        policeRed.copy(alpha = 0.8f),
        Color(0xFF880E4F)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.8f))
            .zIndex(100f),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "게임 종료!",
                fontFamily = PixelFont,
                fontSize = 48.sp,
                fontWeight = FontWeight.ExtraBold,
                style = TextStyle(
                    brush = Brush.verticalGradient(arcadeColors),
                    shadow = Shadow(
                        color = policeRed,
                        offset = Offset(0f, 0f),
                        blurRadius = 35f
                    )
                ),
                modifier = Modifier
                    .zIndex(1f)
            )
        }
    }
}