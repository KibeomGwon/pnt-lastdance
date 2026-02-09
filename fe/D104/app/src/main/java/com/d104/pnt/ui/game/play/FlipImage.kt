package com.d104.pnt.ui.game.play

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.d104.pnt.R
import com.d104.pnt.domain.model.GameRole
import com.d104.pnt.ui.component.QRcodeContainer
import kotlinx.coroutines.launch

@Composable
fun FlipImage(
    role: GameRole,
    memberId: Long,
    size: Dp = 280.dp,
    canFlip: Boolean = true,
    helicopterEnabled: Boolean = true,
    onHelicopterClick: () -> Unit = {}
) {
    val rotation = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .size(size)
            .clip(if (role == GameRole.POLICE) CircleShape else RectangleShape)
            .then(
                if (role == GameRole.POLICE && !canFlip) Modifier
                else Modifier
                    .clickable {
                        scope.launch {
                            val target = if (rotation.value > -90f) -180f else 0f
                            rotation.animateTo(
                                target,
                                animationSpec = tween(
                                    durationMillis = 450,
                                    easing = FastOutSlowInEasing
                                )
                            )
                        }
                    }
                    .pointerInput(Unit) {
                        detectDragGestures(
                            onDrag = { _, dragAmount ->
                                scope.launch {
                                    val newRotation =
                                        (rotation.value + dragAmount.x * 0.4f)
                                            .coerceIn(-180f, 0f)
                                    rotation.snapTo(newRotation)
                                }
                            },
                            onDragEnd = {
                                scope.launch {
                                    val target =
                                        if (rotation.value < -90f) -180f else 0f

                                    rotation.animateTo(
                                        target,
                                        animationSpec = tween(
                                            durationMillis = 300,
                                            easing = FastOutSlowInEasing
                                        )
                                    )
                                }
                            }
                        )
                    }
            )
            .graphicsLayer {
                rotationY = rotation.value
                cameraDistance = 16 * density

                val absRotation = kotlin.math.abs(rotation.value)
                scaleX = if (absRotation in 70f..110f) 0.92f else 1f
            },
        contentAlignment = Alignment.Center
    ) {
        val showFront = rotation.value > -90f

        if (role == GameRole.POLICE) {
            if (showFront) {
                // 앞면: 배지
                Image(
                    painter = painterResource(role.badge),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                // 뒷면: 헬기
                Image(
                    painter = painterResource(R.drawable.img_helicopter),
                    contentDescription = "헬기 스킬",
                    contentScale = ContentScale.Crop,
                    colorFilter = grayscaleColorFilter(helicopterEnabled),
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(-1f, 1f)
                        .clickable(enabled = helicopterEnabled) {
                            onHelicopterClick()
                        }
                )
            }
        } else {
            // 도둑
            if (showFront) {
                Image(
                    painter = painterResource(role.badge),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Image(
                    painter = painterResource(R.drawable.ic_thief_bg),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .scale(-1f, 1f)
                )
                Box(
                    modifier = Modifier.padding(
                        top = 45.dp,
                        bottom = 70.dp,
                        start = 55.dp,
                        end = 55.dp
                    )
                ) {
                    QRcodeContainer(
                        modifier = Modifier.fillMaxSize(),
                        data = memberId.toString()
                    )
                }
            }
        }
    }
}

fun grayscaleColorFilter(enabled: Boolean): ColorFilter? {
    if (enabled) return null

    val matrix = ColorMatrix().apply {
        setToSaturation(0f)
    }
    return ColorFilter.colorMatrix(matrix)
}

@Preview
@Composable
fun PreviewFlip() {
    FlipImage(
        role = GameRole.THIEF,
        memberId = 16L
    )
}

