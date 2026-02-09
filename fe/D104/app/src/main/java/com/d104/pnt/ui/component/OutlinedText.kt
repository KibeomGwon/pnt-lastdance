package com.d104.pnt.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.em
import com.d104.pnt.ui.theme.PixelFont
import com.d104.pnt.ui.theme.PoliceBlue
import com.d104.pnt.ui.theme.ThiefRed

@Composable
fun OutlinedText(
    text: String,
    modifier: Modifier = Modifier,
    success: Boolean,
    outlineColor: Color = Color.White,
    outlineWidth: Float = 10f,
    fontSize: TextUnit = TextUnit.Unspecified,
    fontFamily: FontFamily = PixelFont,
    textAlign: TextAlign = TextAlign.Center
) {
    Box(modifier = modifier) {
        Text(
            modifier = modifier,
            text = text,
            color = outlineColor,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            textAlign = textAlign,
            lineHeight = 1.2.em,
            style = LocalTextStyle.current.merge(
                TextStyle(
                    drawStyle = Stroke(
                        width = outlineWidth,
                        join = StrokeJoin.Miter
                    )
                )
            )
        )

        Text(
            modifier = modifier,
            text = text,
            color = if (success) PoliceBlue else ThiefRed,
            fontSize = fontSize,
            fontFamily = fontFamily,
            fontWeight = FontWeight.Bold,
            textAlign = textAlign,
            lineHeight = 1.2.em
        )
    }
}