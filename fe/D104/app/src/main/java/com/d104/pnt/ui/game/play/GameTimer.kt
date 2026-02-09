package com.d104.pnt.ui.game.play

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.delay

@Composable
fun GameTimer(
    totalSeconds: Int = 2, //
    onTimeOver: () -> Unit
) {
    var remainingSeconds by remember { mutableIntStateOf(totalSeconds) }

    LaunchedEffect(Unit) {
        while (remainingSeconds > 0) {
            delay(1_000L)
            remainingSeconds--
        }
        onTimeOver()
    }

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60

    Text(
        text = String.format("%02d:%02d", minutes, seconds),
        color = Color(0xFFFFE082),
        style = MaterialTheme.typography.titleLarge
    )
}
