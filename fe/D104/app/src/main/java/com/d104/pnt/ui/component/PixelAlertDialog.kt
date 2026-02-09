package com.d104.pnt.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d104.pnt.ui.theme.AccentYellow
import com.d104.pnt.ui.theme.PixelFont
import com.d104.pnt.ui.theme.TextPrimary

@Composable
fun PixelAlertDialog(
    title: String,
    message: String,
    borderColor: Color = Color.Gray,
    spacerHeight: Dp = 16.dp,
    buttonContent: @Composable ColumnScope.() -> Unit
) {
    PixelContainer(
        modifier = Modifier.fillMaxWidth(0.85f),
        backgroundColor = Color(0xFF374151),
        borderColor = borderColor
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                color = AccentYellow,
                fontFamily = PixelFont,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(spacerHeight))

            Text(
                text = message,
                color = TextPrimary,
                fontFamily = PixelFont,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                content = buttonContent
            )
        }
    }
}