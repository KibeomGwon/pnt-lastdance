package com.d104.pnt.ui.game.play

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d104.pnt.R
import com.d104.pnt.domain.model.GameRole

@Composable
fun GameRoleScreen(
    viewModel: GameRoleViewModel = hiltViewModel(),
    role: GameRole,
    isChief: Boolean = false,
    onIntroFinished: () -> Unit,
) {
    val status by viewModel.flowStatus.collectAsStateWithLifecycle()
    val connectionStatus by viewModel.connectionStatus.collectAsStateWithLifecycle()

    val memberCount by viewModel.memberCount.collectAsStateWithLifecycle()
    val connectedCount by viewModel.connectedCount.collectAsStateWithLifecycle()

    LaunchedEffect(status) {
        if (status is GameFlowStatus.CountDown) {
            onIntroFinished()
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_night),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 역할 정보
            RoleRevealContent(role = role, isChief = isChief)

            Spacer(modifier = Modifier.height(24.dp))

            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.BottomCenter
            ) {
                when (val connStatus = connectionStatus) {
                    is ConnectionStatus.Error -> {
                        Text("⚠️ ${connStatus.message}", color = Color.Red)
                    }

                    is ConnectionStatus.WillStart -> {
                        Text("곧 게임이 시작됩니다...", color = Color.White)
                    }

                    else -> {}
                }

                Spacer(modifier = Modifier.height(40.dp))
            }

        }
    }
}

@Composable
private fun RoleRevealContent(role: GameRole, isChief: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Transparent)
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(role.emoji),
            contentDescription = null,
            modifier = Modifier.size(120.dp)
        )

        Spacer(Modifier.height(24.dp))

        val goldColor = Color(0xFFFFD700)

        if (isChief) {
            val styledText = buildAnnotatedString {
                withStyle(style = SpanStyle(color = Color.White)) {
                    append("당신은 ")
                }
                withStyle(style = SpanStyle(color = goldColor, fontWeight = FontWeight.Bold)) {
                    append("경찰청장")
                }
                withStyle(style = SpanStyle(color = Color.White)) {
                    append("입니다")
                }
            }

            Text(
                text = styledText,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )
        } else {
            Text(
                text = "당신은 ${role.roleName}입니다",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                textAlign = TextAlign.Center
            )
        }

        Spacer(Modifier.height(16.dp))

        val baseTextColor = Color.White.copy(alpha = 0.9f)

        val finalDescription = if (isChief) {
            buildAnnotatedString {
                withStyle(style = SpanStyle(color = baseTextColor)) {
                    append(role.description)
                    append("\n\n\n\n")
                }
                withStyle(style = SpanStyle(color = goldColor, fontWeight = FontWeight.Bold)) {
                    append("경찰청장 특권")
                }
                append("\n")
                withStyle(style = SpanStyle(color = baseTextColor)) {
                    append("단 한 번, 경찰 헬기를 호출해\n모든 도둑의 위치를 확인할 수 있습니다.\n경찰 뱃지를 뒤집어 헬기를 호출하세요!")
                }
            }
        } else {
            buildAnnotatedString {
                withStyle(style = SpanStyle(color = baseTextColor)) {
                    append(role.description)
                }
            }
        }

        Text(
            text = finalDescription,
            style = MaterialTheme.typography.bodyLarge,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
    }
}