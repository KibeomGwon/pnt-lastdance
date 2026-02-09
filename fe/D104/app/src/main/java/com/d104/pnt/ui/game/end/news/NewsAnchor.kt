package com.d104.pnt.ui.game.end.news

import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import com.d104.pnt.R
import kotlinx.coroutines.delay

@Composable
fun NewsAnchor(
    modifier: Modifier = Modifier,
    isSpeaking: Boolean = true
) {
    var currentImageRes by remember { mutableIntStateOf(R.drawable.img_anchor_closed) }

    LaunchedEffect(isSpeaking) {
        if (!isSpeaking) {
            currentImageRes = R.drawable.img_anchor_closed
            return@LaunchedEffect
        }

        while (true) {
            val talkDuration = (150..250).random().toLong()
            val pauseDuration = (50..150).random().toLong()

            val takeBreath = (0..10).random() > 8

            if (takeBreath) {
                currentImageRes = R.drawable.img_anchor_closed
                delay(600)
            } else {
                currentImageRes = R.drawable.img_anchor_open
                delay(talkDuration)
                currentImageRes = R.drawable.img_anchor_closed
                delay(pauseDuration)
            }
        }
    }

    Image(
        painter = painterResource(id = currentImageRes),
        contentDescription = "AI 뉴스 앵커",
        modifier = modifier
    )
}