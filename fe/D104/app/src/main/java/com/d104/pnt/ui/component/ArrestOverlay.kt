package com.d104.pnt.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d104.pnt.ui.theme.Blinds
import com.d104.pnt.ui.theme.PixelFont

@Composable
fun ArrestOverlay(
    modifier: Modifier,
    isVisible: Boolean,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
    ) {
        if (isVisible) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Blinds)
                    .pointerInput(Unit) {
                        detectTapGestures { }
                        detectDragGestures { _, _ -> }
                    }
            )
        }
        AnimatedVisibility(
            visible = isVisible,
            enter = scaleIn(
                initialScale = 2f,
                animationSpec = tween(300, delayMillis = 500)
            ) + fadeIn(tween(300, delayMillis = 500)),
            modifier = Modifier.align(Alignment.Center)
        ) {
            Text(
                text = "WASTED",
                color = Color.Red,
                fontSize = 50.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = PixelFont,
                modifier = Modifier
                    .rotate(-15f)
                    .border(4.dp, Color.Red, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }

    }
}