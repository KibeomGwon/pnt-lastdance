package com.d104.pnt.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Composable
fun UnexpectedErrorScreen(
    onExit: () -> Unit
) {
    Dialog(
        onDismissRequest = { },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            usePlatformDefaultWidth = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.7f)),
            contentAlignment = Alignment.Center
        ) {
            PixelAlertDialog(
                title = "오류",
                message = "예상치 못한 오류가 발생하였습니다.",
                spacerHeight = 32.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    PixelButtonCode(
                        text = "앱 종료",
                        onClick = onExit,
                        modifier = Modifier.fillMaxWidth(0.6f),
                        mainColor = Color.White,
                        textColor = Color.Black,
                        blockHeight = 12,
                        fontSize = 14
                    )
                }
            }
        }
    }
}