package com.d104.pnt.ui.game.end.news

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d104.pnt.R
import com.d104.pnt.ui.component.PixelContainer
import com.d104.pnt.ui.theme.PixelFont
import com.d104.pnt.util.SoundPlayer
import kotlinx.coroutines.delay

@Composable
fun NewsScriptBox(
    soundPlayer: SoundPlayer,
    content: String,
    modifier: Modifier = Modifier,
    onFinish: () -> Unit
) {
    val scrollState = rememberScrollState()
    var textToDisplay by remember { mutableStateOf("") }

    LaunchedEffect(content) {
        textToDisplay = ""

        soundPlayer.playBgm(R.raw.news_speaking, tag = "news_speaking", isLooping = true)

        content.forEachIndexed { index, _ ->
            textToDisplay = content.substring(0, index + 1)
            try {
                scrollState.animateScrollTo(scrollState.maxValue)
            } catch (e: Exception) {
            }

            delay(60)

        }

        soundPlayer.stopBgm("news_speaking")
        delay(5000L)

        onFinish()
    }

    PixelContainer(
        modifier = modifier
            .fillMaxWidth(0.9f)
            .heightIn(min = 80.dp, max = 120.dp),
        backgroundColor = Color.White,
        borderColor = Color.Gray,
        borderWidth = 8f,
        cornerSize = 16f,
        innerHorizontalPadding = 16,
        innerVerticalPadding = 12
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            Text(
                text = textToDisplay,
                color = Color.Black,
                fontFamily = PixelFont,
                fontSize = 15.sp,
                lineHeight = 20.sp,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}