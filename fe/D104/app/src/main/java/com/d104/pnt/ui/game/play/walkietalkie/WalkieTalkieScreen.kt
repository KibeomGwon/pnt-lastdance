package com.d104.pnt.ui.game.play.walkietalkie

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

object WalkieColor {
    val Panel = Color(0xFF2A2A2A)
    val Button = Color(0xFF3A3A3A)
    val TextPrimary = Color(0xFFE0E0E0)
    val Danger = Color(0xFFE53935)
}

/**
 * 무전기 UI
 */
@Composable
fun WalkieTalkieContent(
    channel: String,
    isTalking: Boolean,
    isSomeoneTalking: Boolean,
    onPttDown: () -> Unit,
    onPttUp: () -> Unit,
) {
    val density = LocalDensity.current
    val configuration = LocalConfiguration.current
    val screenHeight = with(density) { configuration.screenHeightDp.dp.toPx() }

    val expandedOffset = screenHeight * 0.15f


    Column(
        modifier = Modifier
            .height(expandedOffset.dp)
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(WalkieColor.Panel, RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = channel,
                color = WalkieColor.TextPrimary,
                fontSize = 14.sp,
                letterSpacing = 1.sp
            )
        }

        Spacer(Modifier.height(24.dp))

        PttCircle(
            isTalking = isTalking,
            isSomeoneTalking = isSomeoneTalking,
            onDown = onPttDown,
            onUp = onPttUp
        )

        Spacer(Modifier.height(24.dp))

    }
}

@Composable
private fun PttCircle(
    isTalking: Boolean,
    isSomeoneTalking: Boolean,
    onDown: () -> Unit,
    onUp: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed && !isSomeoneTalking) 1.1f else 1f,
        animationSpec = tween(durationMillis = 80),
        label = "ptt-scale"
    )

    val circleColor = when {
        isPressed || isTalking -> WalkieColor.Danger
        isSomeoneTalking -> Color(0xFFFFA500)
        else -> WalkieColor.Panel
    }

    val displayText = when {
        isPressed || isTalking -> "송신 중…"
        isSomeoneTalking -> "다른 경찰 송신 중"
        else -> "누르고 말하세요"
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp, horizontal = 24.dp)
            .aspectRatio(1f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                alpha = if (isSomeoneTalking) 0.6f else 1f
            }
            .then(
                if (!isSomeoneTalking) {
                    Modifier.pointerInput(Unit) {
                        awaitPointerEventScope {
                            while (true) {
                                awaitFirstDown()
                                isPressed = true
                                onDown()

                                waitForUpOrCancellation()

                                isPressed = false
                                onUp()
                            }
                        }
                    }
                } else {
                    Modifier
                }
            )
            .background(color = circleColor, shape = CircleShape),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Mic,
            contentDescription = null,
            tint = WalkieColor.TextPrimary,
            modifier = Modifier.size(48.dp)
        )

        Spacer(Modifier.height(8.dp))

        Text(
            text = displayText,
            color = WalkieColor.TextPrimary,
            fontSize = 12.sp
        )
    }
}