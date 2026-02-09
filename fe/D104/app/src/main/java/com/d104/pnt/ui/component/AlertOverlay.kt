package com.d104.pnt.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d104.pnt.ui.theme.BorderDefault
import kotlinx.coroutines.delay

@Composable
fun AlertOverlay(
    title: String = "",
    message: String = "",
    success: Boolean = true,
) {
    var isVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isVisible = true
        delay(3500)
        isVisible = false
        delay(500)
    }

    AnimatedVisibility(
        modifier = Modifier.fillMaxSize(),
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 500)),
        exit = fadeOut(animationSpec = tween(durationMillis = 500))
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                OutlinedText(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = title,
                    fontSize = 40.sp,
                    success = success,
                    outlineColor = BorderDefault
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedText(
                    modifier = Modifier
                        .fillMaxWidth(),
                    text = message,
                    fontSize = 32.sp,
                    success = success,
                    outlineColor = BorderDefault
                )
            }
        }
    }
}