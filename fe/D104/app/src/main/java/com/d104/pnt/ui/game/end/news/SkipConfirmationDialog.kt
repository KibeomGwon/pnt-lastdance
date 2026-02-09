package com.d104.pnt.ui.game.end.news

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.d104.pnt.ui.component.PixelButtonCode
import com.d104.pnt.ui.component.PixelContainer
import com.d104.pnt.ui.theme.PixelFont

@Composable
fun SkipConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        PixelContainer(
            modifier = Modifier
                .width(320.dp)
                .wrapContentHeight(),
            backgroundColor = Color(0xFF383838),
            borderColor = Color(0xFF555555),
            borderWidth = 6f,
            cornerSize = 16f,
            innerVerticalPadding = 30,
            innerHorizontalPadding = 24
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "스킵하시겠습니까?",
                    fontFamily = PixelFont,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PixelButtonCode(
                        text = "아니요",
                        onClick = onDismiss,
                        mainColor = Color.White,
                        textColor = Color.Black,
                        blockWidth = 30,
                        blockHeight = 12,
                        fontSize = 16
                    )

                    PixelButtonCode(
                        text = "네",
                        onClick = onConfirm,
                        mainColor = Color.White,
                        textColor = Color.Black,
                        blockWidth = 30,
                        blockHeight = 12,
                        fontSize = 16
                    )

                }
            }
        }
    }
}