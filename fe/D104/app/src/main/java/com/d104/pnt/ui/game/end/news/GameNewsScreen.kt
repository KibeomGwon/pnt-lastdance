package com.d104.pnt.ui.game.end.news

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d104.pnt.R
import com.d104.pnt.data.remote.model.response.GameNewsResponse
import com.d104.pnt.domain.model.common.UiState
import com.d104.pnt.ui.theme.PixelFont

@Composable
fun GameNewsScreen(
    gameId: Long,
    newsId: Long,
    viewModel: GameNewsViewModel = hiltViewModel(),
    onNextClick: () -> Unit
) {
    val newsState by viewModel.newsState.collectAsStateWithLifecycle()
    var showSkipDialog by remember { mutableStateOf(false) }

    BackHandler { showSkipDialog = true }

    when (newsState) {
        is UiState.Success -> {
            val news = (newsState as UiState.Success<GameNewsResponse>).data

            // 메인 뉴스 화면
            Box(modifier = Modifier.fillMaxSize()) {
                // 배경
                Image(
                    painter = painterResource(id = R.drawable.img_breaking_news),
                    contentDescription = "뉴스 스튜디오",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.2f))
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Transparent)
                ) {
                    Spacer(Modifier.weight(0.4f))

                    // 아나운서 + 스크립트
                    Column(
                        modifier = Modifier
                            .weight(0.6f)
                            .fillMaxWidth(),
                        verticalArrangement = Arrangement.Top,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        NewsAnchor(modifier = Modifier.size(250.dp), isSpeaking = true)

                        // 뉴스 본문
                        NewsScriptBox(
                            soundPlayer = viewModel.soundPlayer,
                            content = news.content,
                            onFinish = { onNextClick() }
                        )
                    }
                }
                // 하단 뉴스 티커
                val tickerText = news.title

                Box(modifier = Modifier.align(Alignment.BottomCenter)) {
                    NewsTickerBar(text = tickerText)
                }

                // 스킵 버튼
                SkipButton(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(top = 48.dp, end = 24.dp),
                    onClick = { showSkipDialog = true }
                )
            }
            // 스킵 확인 다이얼로그
            if (showSkipDialog) {
                SkipConfirmationDialog(
                    onConfirm = {
                        showSkipDialog = false
                        onNextClick()
                    },
                    onDismiss = { showSkipDialog = false }
                )
            }
        }

        is UiState.Error -> {

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "⚠️ 뉴스를 불러오지 못했습니다.", color = Color.White, fontFamily = PixelFont)
                    Text(
                        text = (newsState as? UiState.Error)?.message ?: "알 수 없는 오류",
                        color = Color.Red.copy(alpha = 0.7f),
                        fontSize = 12.sp
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "돌아가기",
                        color = Color.Yellow,
                        modifier = Modifier.clickable { onNextClick() }
                    )
                }
            }
        }

        else -> {

        }
    }

}

@Composable
private fun SkipButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Text(
        text = "SKIP >>",
        color = Color.White,
        fontFamily = PixelFont,
        fontSize = 18.sp,
        modifier = modifier
            .clickable { onClick() }
            .padding(12.dp)
    )
}
