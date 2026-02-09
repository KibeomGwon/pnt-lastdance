package com.d104.pnt.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.d104.pnt.ui.theme.BorderDefault
import com.d104.pnt.ui.theme.TextPrimary


@Composable
fun PixelContainer(
    modifier: Modifier = Modifier,
    borderWidth: Float = 10f,
    cornerSize: Float = 20f,
    backgroundColor: Color = TextPrimary,
    borderColor: Color = BorderDefault,
    innerHorizontalPadding: Int = 16,
    innerVerticalPadding: Int = 12,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .drawBehind {
                val w = size.width
                val h = size.height

                val path = Path().apply {
                    moveTo(cornerSize, 0f)
                    lineTo(w - cornerSize, 0f)
                    for (px in borderWidth.toInt() until cornerSize.toInt() + 1 step borderWidth.toInt()) {
                        lineTo(w - cornerSize + px - borderWidth, px.toFloat())
                        lineTo(w - cornerSize + px, px.toFloat())
                    }
                    lineTo(w, h - cornerSize)
                    for (px in borderWidth.toInt() until cornerSize.toInt() + 1 step borderWidth.toInt()) {
                        lineTo(w - px, h - cornerSize + px - borderWidth)
                        lineTo(w - px, h - cornerSize + px)
                    }
                    lineTo(cornerSize, h)
                    for (px in borderWidth.toInt() until cornerSize.toInt() + 1 step borderWidth.toInt()) {
                        lineTo(cornerSize - px + borderWidth, h - px)
                        lineTo(cornerSize - px, h - px)
                    }
                    lineTo(0f, cornerSize)
                    for (px in borderWidth.toInt() until cornerSize.toInt() + 1 step borderWidth.toInt()) {
                        lineTo(px.toFloat(), cornerSize - px + borderWidth)
                        lineTo(px.toFloat(), cornerSize - px)
                    }
                    close()
                }

                drawPath(path, color = backgroundColor)

                drawPath(
                    path,
                    color = borderColor,
                    style = Stroke(width = borderWidth)
                )
            }
            .padding(
                horizontal = innerHorizontalPadding.dp,
                vertical = innerVerticalPadding.dp
            )
    ) {
        content()
    }
}