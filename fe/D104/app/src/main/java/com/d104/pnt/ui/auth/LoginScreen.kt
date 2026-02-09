package com.d104.pnt.ui.auth

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.d104.pnt.R
import com.d104.pnt.data.remote.model.response.LoginResponse
import com.d104.pnt.domain.model.common.UiState
import com.d104.pnt.ui.component.PixelButtonCode
import com.d104.pnt.ui.component.PixelInputField
import com.d104.pnt.ui.theme.AccentRed
import com.d104.pnt.ui.theme.BorderDefault

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    goToSignup: () -> Unit,
    onLoginSuccess: (String) -> Unit
) {

    val id by viewModel.id.collectAsState()
    val password by viewModel.password.collectAsState()
    val loginState by viewModel.loginState.collectAsState()

    val context = LocalContext.current

    LaunchedEffect(loginState) {
        if (loginState is UiState.Success) {
            val response = (loginState as UiState.Success<LoginResponse>).data
            onLoginSuccess(response.member.id)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModel.updateId("")
            viewModel.updatePassword("")
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "NeonFlicker")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2000
                0.9f at 1500
                0.2f at 1600
                1.0f at 1650
                0.4f at 1700
                1.0f at 1750
            },
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    Surface(modifier = Modifier.fillMaxSize()) {

        // 배경 이미지
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.img_login_bg),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp)
                .windowInsetsPadding(
                    WindowInsets
                        .statusBars
                        .only(WindowInsetsSides.Top)
                )
                .windowInsetsPadding(
                    WindowInsets
                        .navigationBars
                        .only(WindowInsetsSides.Bottom)
                ),
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "게임 시작하기",
                style = MaterialTheme.typography.titleLarge.copy(
                    fontSize = 28.sp,
                    shadow = androidx.compose.ui.graphics.Shadow(
                        color = AccentRed.copy(alpha = alpha),
                        blurRadius = 10f
                    )
                ),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 40.dp),
                color = Color.White.copy(alpha = alpha.coerceAtLeast(0.5f)),
            )

            // 로그인 섹션
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min)
                    .padding(bottom = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .weight(0.7f)
                        .fillMaxHeight(),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    PixelInputField(
                        value = id,
                        onValueChange = { viewModel.updateId(it) },
                        placeholder = "아이디",
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White.copy(alpha = 0.9f),
                        borderColor = BorderDefault
                    )

                    PixelInputField(
                        value = password,
                        onValueChange = { viewModel.updatePassword(it) },
                        placeholder = "비밀번호",
                        modifier = Modifier.fillMaxWidth(),
                        backgroundColor = Color.White.copy(alpha = 0.9f),
                        borderColor = BorderDefault,
                        isPassword = true
                    )
                }

                PixelButtonCode(
                    modifier = Modifier
                        .weight(0.3f)
                        .fillMaxHeight(),
                    text = "로그인",
                    fontSize = 16,
                    onClick = {
                        if (!viewModel.isLoginEnabled()) {
                            return@PixelButtonCode
                        }

                        viewModel.login()
                    },
                    mainColor = AccentRed.copy(alpha = 0.9f),
                    borderColor = BorderDefault,
                )
            }

            if (loginState is UiState.Error) {
                Text(
                    text = (loginState as UiState.Error).message,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                )
            }

            // 로딩 표시
            if (loginState is UiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(8.dp)
                )
            }

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .clickable { goToSignup() },
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    textAlign = TextAlign.End,
                    text = "계정이 없다면?",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f),
                )
                IconButton(
                    onClick = { goToSignup() }
                ) {
                    Image(
                        painter = painterResource(R.drawable.arrow),
                        contentDescription = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(15.dp))


            // Divider
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(R.drawable.divider),
                    contentDescription = null
                )

                Text(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    text = "OR",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )

                Image(
                    painter = painterResource(R.drawable.divider),
                    contentDescription = null
                )
            }

            Spacer(modifier = Modifier.height(25.dp))

            Image(
                modifier = Modifier
                    .aspectRatio(7f)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null
                    ) {
                        viewModel.loginWithKakao(context)
                    },
                painter = painterResource(R.drawable.center),
                contentDescription = "카카오 로그인",
            )
        }
    }

}

