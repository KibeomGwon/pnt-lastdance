package com.d104.pnt.ui.home

import android.widget.Toast
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d104.pnt.R
import com.d104.pnt.ui.component.PixelButtonCode
import com.d104.pnt.ui.component.PixelContainer
import com.d104.pnt.ui.component.PixelInputField
import com.d104.pnt.ui.theme.BorderDefault
import com.d104.pnt.ui.theme.CustomBlue
import com.d104.pnt.ui.theme.PixelFont
import com.d104.pnt.ui.theme.RoomBorder
import com.d104.pnt.ui.theme.RoomContainer

@Composable
fun HomeScreen(
    goToGameCreate: () -> Unit,
    navigateToGameRoom: (Long) -> Unit,
    navigateToIntro: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    var showJoinDialog by remember { mutableStateOf(false) }
    val joinCode by viewModel.joinCode.collectAsStateWithLifecycle()

    val neonAlpha by rememberInfiniteTransition(label = "neon-flicker").animateFloat(
        initialValue = 1f,
        targetValue = 0.5f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3000
                1.0f at 0
                1.0f at 2500
                0.3f at 2600
                0.8f at 2700
                0.2f at 2800
                1.0f at 2900
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "flicker"
    )

    LaunchedEffect(Unit) {

        viewModel.cleanupSocket()

        viewModel.uiEvent.collect { event ->
            when (event) {
                is HomeViewModel.HomeUiEvent.NavigateToIntro -> {
                    navigateToIntro()
                }

                is HomeViewModel.HomeUiEvent.ShowMessage -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                is HomeViewModel.HomeUiEvent.ShowError -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }

                is HomeViewModel.HomeUiEvent.NavigateToGameRoom -> {
                    showJoinDialog = false
                    navigateToGameRoom(event.roomId)
                }
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.soundPlayer.stopBgm("intro")
        }
    }

    val policeRed = Color(0xFFB71C1C)
    val policeNavy = Color(0xFF1A237E)
    val amberLight = Color(0xFFFFB300)

    val arcadeColors = listOf(
        policeRed,
        amberLight,
        policeRed.copy(alpha = 0.8f),
        Color(0xFF880E4F)
    )

    Surface(modifier = Modifier.fillMaxSize(), color = Color.Black) {
        Image(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0.5f),
            painter = painterResource(R.drawable.img_main_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "경찰과\n\t\t\t\t\t\t\t\t\t\t\t\t\t\t 도둑",
                    fontFamily = PixelFont,
                    fontSize = 80.sp,
                    fontWeight = FontWeight.ExtraBold,
                    style = TextStyle(
                        brush = Brush.verticalGradient(arcadeColors),
                        shadow = Shadow(
                            color = policeRed.copy(alpha = neonAlpha),
                            offset = Offset(0f, 0f),
                            blurRadius = 35f
                        )
                    ),
                    modifier = Modifier
                        .graphicsLayer(alpha = neonAlpha)
                        .zIndex(1f)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(32.dp)
            ) {
                // HOST 버튼
                PixelButtonCode(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f),
                    text = "생성하기",
                    textColor = Color.White,
                    fontSize = 26,
                    onClick = { goToGameCreate() },
                    mainColor = policeRed.copy(alpha = 0.675f),
                    borderColor = Color.Black
                )

                // JOIN 버튼
                PixelButtonCode(
                    modifier = Modifier
                        .weight(1f)
                        .aspectRatio(1f),
                    text = "참여하기",
                    textColor = Color.White,
                    fontSize = 26,
                    onClick = { showJoinDialog = true },
                    mainColor = policeNavy.copy(alpha = 0.675f),
                    borderColor = Color.Black
                )
            }
        }

        if (showJoinDialog) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.6f))
                    .clickable { showJoinDialog = false },
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.clickable(enabled = false) {}) {
                    JoinGameDialog(
                        onDismiss = { showJoinDialog = false },
                        onConfirm = {
                            viewModel.joinGame()
                        },
                        joinCode = joinCode,
                        onUpdateCode = {
                            viewModel.updateJoinCode(it)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun JoinGameDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    joinCode: String,
    onUpdateCode: (String) -> Unit
) {
    PixelContainer(
        modifier = Modifier.width(320.dp),
        backgroundColor = RoomContainer,
        borderColor = RoomBorder,
        borderWidth = 6f,
        cornerSize = 20f
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 30.dp, horizontal = 20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "참여 코드 입력",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(30.dp))

            PixelInputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight(Alignment.CenterVertically),
                placeholder = "참여코드를 입력해주세요",
                borderColor = BorderDefault,
                value = joinCode,
                onValueChange = {
                    onUpdateCode(it.uppercase())
                }
            )

            Spacer(modifier = Modifier.height(30.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                PixelButtonCode(
                    modifier = Modifier.weight(1f),
                    text = "취소",
                    onClick = {
                        onDismiss()
                    },
                    mainColor = Color.Gray,
                    borderColor = BorderDefault,
                    textColor = Color.White,
                    fontSize = 15,
                    blockHeight = 15,
                    pixelSize = 2.8.dp
                )

                PixelButtonCode(
                    modifier = Modifier.weight(1f),
                    text = "확인",
                    onClick = {
                        onConfirm()
                    },
                    mainColor = CustomBlue,
                    borderColor = BorderDefault,
                    textColor = Color.White,
                    fontSize = 15,
                    blockHeight = 15,
                    pixelSize = 2.8.dp
                )
            }
        }
    }
}

