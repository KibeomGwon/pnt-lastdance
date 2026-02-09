package com.d104.pnt.ui.game.load

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d104.pnt.R
import com.d104.pnt.domain.model.GameRole
import com.d104.pnt.ui.component.ContDownUI
import com.d104.pnt.ui.theme.BackgroundWhite
import com.d104.pnt.ui.theme.PixelFont

@Composable
fun GameLoadingScreen(
    roomId: Long,
    role: GameRole,
    viewModel: GameLoadingViewModel = hiltViewModel(),
    onLoadingComplete: (gameId: Long) -> Unit
) {
    val remainingTime by viewModel.remainingTime.collectAsStateWithLifecycle()
    val isFinished by viewModel.isFinished.collectAsStateWithLifecycle()
    val message by viewModel.message.collectAsStateWithLifecycle()

    val totalSeconds = 60f
    val progress = ((totalSeconds - remainingTime) / totalSeconds).coerceIn(0f, 1f)


    val isDanger = remainingTime <= 10
    val isWarning = remainingTime in 11..30

    val themeColor = when {
        isDanger -> Color(0xFFFF3B30)
        isWarning -> Color(0xFFFFCC00)
        else -> BackgroundWhite
    }

    LaunchedEffect(isFinished) {
        if (isFinished) {
            onLoadingComplete(roomId)
        }
    }

    val timerLabel = when (role) {
        GameRole.POLICE -> "작전 투입까지"
        GameRole.THIEF -> "경찰 출동까지"
        else -> "게임 시작까지"
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_dawn),
            contentDescription = "Background",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.6f))
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = timerLabel,
                fontFamily = PixelFont,
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(16.dp))

            ContDownUI(
                remainingSeconds = remainingTime,
                themeColor = themeColor
            )
            Spacer(Modifier.height(20.dp))

            Text(
                text = message,
                color = Color.White,
                textAlign = TextAlign.Center,
                fontSize = 16.sp,
                fontFamily = PixelFont,
                modifier = Modifier.height(50.dp)
            )

            Spacer(Modifier.height(24.dp))

            ChaseProgressBar(
                progress = progress,
                thiefIcon = R.drawable.img_thief_run,
                policeIcon = R.drawable.img_police_run,
                laneWidth = 320.dp,
                progressBarColor = themeColor
            )
        }
    }
}

@Composable
fun ChaseProgressBar(
    modifier: Modifier = Modifier,
    progress: Float,
    thiefIcon: Int,
    policeIcon: Int,
    laneWidth: Dp = 300.dp,
    iconSize: Dp = 48.dp,
    gap: Dp = 40.dp,
    progressBarColor: Color
) {
    Column(
        modifier = modifier.width(laneWidth),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(iconSize + 12.dp)
        ) {
            // 진행 바 배경 및 테두리
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .align(Alignment.BottomCenter)
                    .background(Color.Black.copy(alpha = 0.5f))
                    .border(2.dp, progressBarColor)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress)
                        .fillMaxHeight()
                        .background(progressBarColor)
                )
            }

            // 위치 계산
            val maxTravel = laneWidth - iconSize
            val policeX = (maxTravel - gap) * progress
            val thiefX = policeX + gap

            Image(
                painter = painterResource(policeIcon),
                contentDescription = null,
                modifier = Modifier
                    .size(iconSize)
                    .offset(x = policeX, y = (-8).dp)
            )

            Image(
                painter = painterResource(thiefIcon),
                contentDescription = null,
                modifier = Modifier
                    .size(iconSize)
                    .offset(x = thiefX, y = (-8).dp)
            )
        }
    }
}