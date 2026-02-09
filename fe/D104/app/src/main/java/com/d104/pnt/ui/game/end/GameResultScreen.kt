package com.d104.pnt.ui.game.end

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.DrawableRes
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d104.pnt.R
import com.d104.pnt.domain.model.common.UiState
import com.d104.pnt.ui.component.PixelButtonCode
import com.d104.pnt.ui.component.PixelContainer
import com.d104.pnt.ui.theme.AccentYellow
import com.d104.pnt.ui.theme.BorderDefault
import com.d104.pnt.ui.theme.CustomBlue
import com.d104.pnt.ui.theme.DarkBackground
import com.d104.pnt.ui.theme.LoseColor
import com.d104.pnt.ui.theme.PixelFont
import com.d104.pnt.ui.theme.RoomBorder
import com.d104.pnt.ui.theme.RoomContainer
import com.d104.pnt.ui.theme.TextPrimary
import com.d104.pnt.ui.theme.TextSecondary
import com.d104.pnt.ui.theme.WinColor

data class MvpData(
    val type: String,
    val role: String,
    val nickname: String,
    val statLabel: String,
    val statValue: String,
    @DrawableRes val iconRes: Int
)

enum class ReportStep { NONE, INPUT, CONFIRM, SUCCESS }

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GameResultScreen(
    gameId: Long,
    onBackToHome: () -> Unit,
    onBackToWaitingRoom: (Long) -> Unit,
    viewModel: GameResultViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val rejoinState by viewModel.rejoinState.collectAsStateWithLifecycle()
    val remainingSeconds by viewModel.remainingSeconds.collectAsStateWithLifecycle()

    val context = LocalContext.current
    var backPressedTime by remember { mutableLongStateOf(0L) }

    BackHandler {
        if (System.currentTimeMillis() - backPressedTime <= 1500) {
            onBackToHome()
        } else {
            backPressedTime = System.currentTimeMillis()
            Toast.makeText(
                context,
                "한 번 더 누르면 게임에서 나갑니다",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // 재입장 상태 처리
    LaunchedEffect(rejoinState) {
        when (rejoinState) {
            is UiState.Error -> {
                Toast.makeText(
                    context,
                    (rejoinState as UiState.Error).message,
                    Toast.LENGTH_LONG
                ).show()
                onBackToHome()
            }

            else -> {}
        }
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.cleanupGameSocket() }
    }

    when (uiState) {
        is UiState.Loading, is UiState.Idle -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground),
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.material3.CircularProgressIndicator(
                    color = AccentYellow
                )
            }
        }

        is UiState.Error -> {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(DarkBackground),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = (uiState as UiState.Error).message,
                    color = Color.White,
                    fontFamily = PixelFont
                )
            }
        }

        is UiState.Success<GameResultUiData> -> {
            val data = (uiState as UiState.Success<GameResultUiData>).data
            GameResultContent(
                gameId = gameId,
                data = data,
                onBackToHome = onBackToHome,
                onBackToWaitingRoom = onBackToWaitingRoom,
                viewModel = viewModel
            )

            // 재입장 로딩 오버레이
            if (rejoinState is UiState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f))
                        .zIndex(999f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        CircularProgressIndicator(color = AccentYellow)
                        Text(
                            text = "대기방 입장 중...",
                            color = Color.White,
                            fontFamily = PixelFont,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun GameResultContent(
    gameId: Long,
    data: GameResultUiData,
    onBackToHome: () -> Unit,
    onBackToWaitingRoom: (Long) -> Unit,
    viewModel: GameResultViewModel
) {
    val blinkAlpha by rememberInfiniteTransition(label = "winlose-blink")
        .animateFloat(
            initialValue = 1f,
            targetValue = 0.7f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 500, easing = LinearEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "alpha"
        )

    val pagerState = rememberPagerState(pageCount = { data.mvpList.size })

    val titleColor = if (data.isWin) WinColor else LoseColor
    val titleText = if (data.isWin) "WIN!" else "LOSE"
    val statsLabel = if (data.isPolice) "검거한 도둑 수" else "최장 생존 시간"
    val mvpBoxBgColor = Color(0xFF35384F)
    val participantNames = data.allPlayerNicknames

    val characterImageRes = when {
        data.isPolice && data.isWin -> R.drawable.img_police_win
        data.isPolice && !data.isWin -> R.drawable.img_police_lose
        !data.isPolice && data.isWin -> R.drawable.img_thief_win
        else -> R.drawable.img_thief_lose
    }

    val isRejoinReady = viewModel.remainingSeconds.collectAsStateWithLifecycle().value <= 0
    val isRejoinLoading =
        viewModel.rejoinState.collectAsStateWithLifecycle().value is UiState.Loading

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
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
            modifier = Modifier
                .clickable { viewModel.openReportDialog() }
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .systemBarsPadding(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_siren_end),
                contentDescription = "신고",
                modifier = Modifier
                    .size(40.dp)
                    .padding(bottom = 4.dp)
            )

            Text(
                text = "신고하기",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.clickable { viewModel.openReportDialog() }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.height(20.dp))
            Text(
                text = "결과 리포트",
                fontSize = 24.sp,
                fontFamily = PixelFont,
                color = Color.LightGray
            )

            // WIN/LOSE
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .zIndex(2f)
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = titleText,
                    fontFamily = PixelFont,
                    fontSize = 90.sp,
                    fontWeight = FontWeight.Bold,
                    color = titleColor.copy(alpha = blinkAlpha),
                    style = TextStyle(
                        shadow = Shadow(color = Color.Black)
                    ),
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .zIndex(1f)
                )

                Image(
                    painter = painterResource(id = characterImageRes),
                    contentDescription = "Character",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .height(180.dp)
                        .align(Alignment.Center)
                        .offset(y = 30.dp)
                        .zIndex(2f)
                )
            }

            // Rank / Stat
            PixelContainer(
                backgroundColor = RoomContainer.copy(alpha = 0.7f),
                borderColor = RoomBorder,
                borderWidth = 3f,
                cornerSize = 12f,
                innerVerticalPadding = 14,
                innerHorizontalPadding = 0,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "RANK",
                            fontFamily = PixelFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.LightGray
                        )
                        Spacer(modifier = Modifier.height(6.dp))

                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                        ) {
                            Image(
                                painter = painterResource(id = data.myTierIconRes),
                                contentDescription = "Tier",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .scale(1.6f)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = statsLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = data.myGameStat,
                            fontFamily = PixelFont,
                            fontWeight = FontWeight.Bold,
                            fontSize = 36.sp,
                            color = AccentYellow
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Image(
                                painter = painterResource(id = R.drawable.ic_trophy_end),
                                contentDescription = "Best Stat Trophy",
                                modifier = Modifier.size(28.dp)
                            )

                            Text(
                                textAlign = TextAlign.Center,
                                text = "최고기록 ${data.myBestStat}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            val isFirst = pagerState.currentPage == 0
            val isLast = pagerState.currentPage == pagerState.pageCount - 1

            // MVP 카드
            Box(modifier = Modifier.fillMaxWidth()) {
                HorizontalPager(
                    state = pagerState,
                    pageSpacing = 16.dp,
                    contentPadding = PaddingValues(
                        start = if (isLast) 32.dp else 0.dp,
                        end = if (isFirst) 32.dp else 0.dp
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) { page ->
                    MvpCard(
                        mvpData = data.mvpList[page],
                        backgroundColor = mvpBoxBgColor,
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            PixelButtonCode(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                text = when {
                    isRejoinLoading -> "입장 중..."
                    else -> "대기방"
                },
                onClick = {
                    viewModel.backToLobby { roomId ->
                        onBackToWaitingRoom(roomId)
                    }
                },
                mainColor = if (isRejoinReady && !isRejoinLoading) CustomBlue else Color.Gray,
                borderColor = BorderDefault,
                textColor = Color.White,
                fontSize = 16,
                blockHeight = 12,
                enabled = isRejoinReady && !isRejoinLoading
            )

            if (viewModel.remainingSeconds.collectAsStateWithLifecycle().value > 0) {
                Text(
                    modifier = Modifier.padding(top = 10.dp),
                    text = "${viewModel.remainingSeconds.collectAsStateWithLifecycle().value}초 후에 입장 가능합니다",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }


        // 신고 다이얼로그
        when (viewModel.reportStep) {
            ReportStep.INPUT -> {
                ReportDialog(
                    initialTargetUser = viewModel.draft.nickname,
                    initialReason = viewModel.draft.reasonKr,
                    initialDescription = viewModel.draft.detail,
                    validNicknames = participantNames,
                    onDismissRequest = { viewModel.closeReportDialog() },
                    onReport = { user, reason, desc ->
                        viewModel.onDraftSubmitted(user, reason, desc)
                    }
                )
            }

            ReportStep.CONFIRM -> {
                val isLoading = viewModel.reportSendState is UiState.Loading
                val errorMsg = (viewModel.reportSendState as? UiState.Error)?.message

                ConfirmReportDialog(
                    isLoading = isLoading,
                    errorText = errorMsg,
                    onDismissRequest = { viewModel.backToInput() },
                    onConfirm = { viewModel.confirmReport() }
                )
            }

            ReportStep.SUCCESS -> {
                SuccessReportDialog(
                    onDismissRequest = { viewModel.closeReportDialog() }
                )
            }

            else -> Unit
        }
    }
}

@Composable
fun MvpCard(
    mvpData: MvpData,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    PixelContainer(
        backgroundColor = backgroundColor,
        borderColor = Color(0xFF6B728E),
        borderWidth = 2f,
        cornerSize = 8f,
        innerVerticalPadding = 20,
        innerHorizontalPadding = 16,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.Center)
                .padding(start = 10.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconResId = when (mvpData.type) {
                "MVP" -> R.drawable.ic_medal_end
                "ACE" -> R.drawable.ic_passion_end
                "조력자" -> R.drawable.ic_handshake_end
                else -> R.drawable.ic_medal_end
            }

            Image(
                modifier = Modifier
                    .size(64.dp)
                    .weight(0.3f),
                painter = painterResource(id = iconResId),
                contentDescription = mvpData.type
            )

            Spacer(modifier = Modifier.width(20.dp))

            Column(
                modifier = Modifier.weight(0.7f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = mvpData.type,
                    fontFamily = PixelFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = AccentYellow
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = mvpData.role,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
                    Text(
                        text = mvpData.nickname,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = mvpData.statLabel,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.LightGray
                    )
                    Text(
                        text = mvpData.statValue,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}

data class GameResultUiData(
    val isPolice: Boolean,
    val isWin: Boolean,
    val mvpList: List<MvpData>,
    val myTierIconRes: Int,
    val myGameStat: String,
    val myBestStat: String,
    val allPlayerNicknames: List<String> = emptyList()
)