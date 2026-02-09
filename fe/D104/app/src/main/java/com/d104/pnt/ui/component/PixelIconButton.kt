package com.d104.pnt.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun PixelIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    mainColor: Color = Color.White,
    borderColor: Color = Color(0xFF2B2B2B),
    pixelSize: Dp = 4.dp,
    blockHeight: Int = 16,
    blockWidth: Int? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val pressOffset = if (isPressed) pixelSize else 0.dp
    val shadowVisibility = if (isPressed) Color.Transparent else borderColor

    val buttonHeight = pixelSize * blockHeight

    val sizeModifier = if (blockWidth != null) {
        modifier
            .width(pixelSize * blockWidth)
            .height(buttonHeight)
    } else {
        modifier
            .height(buttonHeight)
    }

    Box(
        modifier = sizeModifier
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onClick() },
    ) {
        // 그림자 레이어
        FiveLayerPixelShape(
            color = shadowVisibility,
            pixelUnit = pixelSize,
            baseHorizontalPadding = 0.dp,
            modifier = Modifier.offset(x = pixelSize, y = pixelSize)
        )

        // 테두리 레이어
        FiveLayerPixelShape(
            color = borderColor,
            pixelUnit = pixelSize,
            baseHorizontalPadding = 0.dp,
            modifier = Modifier.offset(x = pressOffset, y = pressOffset)
        )

        // 메인 컬러 레이어
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = pressOffset, y = pressOffset)
                .padding(vertical = pixelSize)
        ) {
            FiveLayerPixelShape(
                color = mainColor,
                pixelUnit = pixelSize,
                baseHorizontalPadding = pixelSize,
                modifier = Modifier.fillMaxSize()
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(x = pressOffset, y = pressOffset),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}