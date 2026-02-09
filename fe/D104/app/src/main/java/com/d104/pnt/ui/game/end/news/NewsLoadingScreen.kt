package com.d104.pnt.ui.game.end.news

import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import com.d104.pnt.R
import com.d104.pnt.ui.theme.PixelFont
import kotlinx.coroutines.delay

@Composable
fun NewsLoadingScreen(
    gameId: Long,
    onNewsReady: (Long, Long) -> Unit,
    viewModel: NewsLoadingViewModel = hiltViewModel()
) {
    LaunchedEffect(Unit) {
        viewModel.playNewsLoadingSound()
    }

    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is NewsLoadingUiEvent.NavigateToActualNews -> {
                    onNewsReady(event.gameId, event.newsId)
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_waitingroom),
            contentDescription = "방송 대기실 배경",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .offset(y = (-50).dp)
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f))
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val context = LocalContext.current

            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(R.drawable.news_anchor)
                    .decoderFactory { result, options, _ ->
                        if (Build.VERSION.SDK_INT >= 28) {
                            ImageDecoderDecoder(result.source, options)
                        } else {
                            GifDecoder(result.source, options)
                        }
                    }
                    .build(),
                contentDescription = "방송 준비 중인 아나운서",
                placeholder = painterResource(R.drawable.example_anchor),
                error = painterResource(R.drawable.example_anchor),
                modifier = Modifier.size(280.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            TypewriterText(
                texts = listOf(
                    "AI가 경기 데이터를 분석하고 있습니다...",
                    "결과를 집계중입니다...",
                    "잠시 후 뉴스가 시작됩니다..."
                )
            )
        }

        Box(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            NewsTickerBar(
                text = "속보: 도심 추격 상황 종료... 경찰청, 검거 현황 브리핑 준비 중... 현장 CCTV 정밀 분석 영상 입수... 잠시 후 단독 보도!      "
            )
        }
    }
}

@Composable
fun TypewriterText(texts: List<String>) {
    var textIndex by remember { mutableIntStateOf(0) }
    var textToDisplay by remember { mutableStateOf("") }

    LaunchedEffect(textIndex) {
        val fullText = texts[textIndex]

        for (i in 1..fullText.length) {
            textToDisplay = fullText.substring(0, i) + " ▌"
            delay(100)
        }

        delay(1500)

        textToDisplay = ""
        textIndex = (textIndex + 1) % texts.size
    }

    Text(
        text = textToDisplay,
        color = Color.White,
        fontFamily = PixelFont,
        fontSize = 17.sp,
        lineHeight = 30.sp
    )
}
