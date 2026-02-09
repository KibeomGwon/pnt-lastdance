package com.d104.pnt.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.d104.pnt.ui.theme.TextPrimary

enum class NetworkErrorState {
    RECONNECTING,
    FAILED
}

@Composable
fun NetworkErrorScreen(
    state: NetworkErrorState,
    retryCount: Int,
    onRetry: () -> Unit,
    onGoToMain: () -> Unit,
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
            when (state) {
                NetworkErrorState.RECONNECTING -> {
                    ReconnectingView(retryCount)
                }

                NetworkErrorState.FAILED -> {
                    ConnectionFailedView(onRetry, onGoToMain, onExit)
                }
            }
        }
    }
}

// 재접속 시도 중 화면
@Composable
private fun ReconnectingView(retryCount: Int) {
    PixelLoading(
        size = 64,
        message = "재접속중입니다...",
        fontSize = 18.sp,
        textColor = TextPrimary
    )
}

// 재접속 실패 화면
@Composable
private fun ConnectionFailedView(
    onRetry: () -> Unit,
    onGoToMain: () -> Unit,
    onExit: () -> Unit
) {
    PixelAlertDialog(
        title = "네트워크 접속 실패",
        message = "서버와의 연결이 끊겼습니다."
    ) {
        PixelButtonCode(
            text = "재접속 시도",
            onClick = onRetry,
            modifier = Modifier.fillMaxWidth(),
            mainColor = Color.White,
            textColor = Color.Black,
            blockHeight = 12,
            fontSize = 14
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Box(modifier = Modifier.weight(1f)) {
                PixelButtonCode(
                    text = "메인으로",
                    onClick = onGoToMain,
                    modifier = Modifier.fillMaxWidth(),
                    mainColor = Color.White,
                    textColor = Color.Black,
                    blockHeight = 12,
                    fontSize = 14
                )
            }

            Box(modifier = Modifier.weight(1f)) {
                PixelButtonCode(
                    text = "접속종료",
                    onClick = onExit,
                    modifier = Modifier.fillMaxWidth(),
                    mainColor = Color.White,
                    textColor = Color.Black,
                    blockHeight = 12,
                    fontSize = 14
                )
            }
        }
    }
}
