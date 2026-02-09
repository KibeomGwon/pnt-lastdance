package com.d104.pnt.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.d104.pnt.ui.theme.AccentYellow
import com.d104.pnt.ui.theme.PixelFont

@Composable
fun KickedNoticeDialog(
    reason: String,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = {}) {
        PixelContainer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            backgroundColor = Color(0xFF2D3242),
            borderColor = Color(0xFF8D90B3),
            borderWidth = 4f,
            cornerSize = 16f
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "강퇴되었습니다!",
                    fontFamily = PixelFont,
                    color = AccentYellow,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "강퇴 사유 : $reason",
                    fontFamily = PixelFont,
                    color = Color.White,
                    fontSize = 16.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                PixelIconButton(
                    onClick = onConfirm,
                    modifier = Modifier.width(120.dp),
                    mainColor = Color.White,
                    borderColor = Color.Black,
                    pixelSize = 3.dp,
                    blockHeight = 12,
                    content = {
                        Text(
                            text = "확인",
                            fontFamily = PixelFont,
                            color = Color.Black,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                )
            }
        }
    }
}