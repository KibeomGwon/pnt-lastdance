package com.d104.pnt.ui.chatroom.create

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.People
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d104.pnt.R
import com.d104.pnt.ui.component.PixelButtonCode
import com.d104.pnt.ui.component.PixelContainer
import com.d104.pnt.ui.component.PixelInputField
import com.d104.pnt.ui.game.create.CounterControl
import com.d104.pnt.ui.game.create.SectionTitle
import com.d104.pnt.ui.theme.BorderDefault
import com.d104.pnt.ui.theme.CustomBlue
import com.d104.pnt.ui.theme.DialogBorderColor
import com.d104.pnt.ui.theme.RoomBorder
import com.d104.pnt.ui.theme.RoomContainer
import com.d104.pnt.ui.theme.TextPrimary

@Composable
fun ChatRoomCreateScreen(
    onCancel: () -> Unit,
    onConfirm: (chatRoomId: Long) -> Unit,
    viewModel: ChatRoomCreateViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    val title by viewModel.title.collectAsStateWithLifecycle()
    val description by viewModel.description.collectAsStateWithLifecycle()
    val maxMember by viewModel.maxMember.collectAsStateWithLifecycle()
    val currentAddress by viewModel.currentAddress.collectAsStateWithLifecycle()

    val createChatRoomStats by viewModel.createChatRoomStats.collectAsStateWithLifecycle()

    val joinRoomState by viewModel.joinRoomState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.getLocationInfo(context)
    }

    LaunchedEffect(joinRoomState) {
        when (val state = joinRoomState) {
            is ChatRoomCreateViewModel.JoinRoomState.Success -> {
                onConfirm(state.chatRoomId)
            }

            is ChatRoomCreateViewModel.JoinRoomState.Error -> {
                Toast.makeText(
                    context,
                    "입장 실패: ${state.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }

            else -> {}
        }
    }

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.img_background2),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 게임 생성 컨테이너
                PixelContainer(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = RoomContainer,
                    borderColor = RoomBorder,
                    borderWidth = 8f,
                    cornerSize = 16f
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp, horizontal = 10.dp)
                    ) {
                        Text(
                            text = "채팅방 생성하기",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        SectionTitle(text = "채팅방 제목 설정")
                        Spacer(modifier = Modifier.height(8.dp))
                        PixelInputField(
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = "채팅방 제목을 입력하세요",
                            borderColor = DialogBorderColor,
                            value = title,
                            onValueChange = { viewModel.updateTitle(it) }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        SectionTitle(text = "채팅방 설명")

                        Spacer(modifier = Modifier.height(8.dp))

                        PixelInputField(
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = "채팅방 설명을 입력하세요",
                            borderColor = DialogBorderColor,
                            value = description,
                            onValueChange = { viewModel.updateDescription(it) },
                            singleLine = false,
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        SectionTitle(text = "채팅방 최대 인원수")

                        Spacer(modifier = Modifier.height(8.dp))

                        CounterControl(
                            modifier = Modifier.padding(horizontal = 80.dp),
                            label = "최대 인원수",
                            icon = Icons.Default.People,
                            value = maxMember.toString(),
                            unit = "명",
                            onDecrease = { viewModel.updateMaxMember(false) },
                            onIncrease = { viewModel.updateMaxMember(true) }
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        SectionTitle(text = "게시 지역")

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "현재 위치를 기반으로 게시 장소가 결정됩니다.",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimary,
                        )
                        Spacer(modifier = Modifier.height(8.dp))

                        PixelContainer(
                            modifier = Modifier.fillMaxWidth(),
                            backgroundColor = TextPrimary,
                            borderColor = DialogBorderColor,
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    modifier = Modifier.weight(0.4f),
                                    text = currentAddress.major,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BorderDefault
                                )
                                Text(
                                    modifier = Modifier.weight(0.2f),
                                    text = "-",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BorderDefault
                                )
                                Text(
                                    modifier = Modifier.weight(0.4f),
                                    text = currentAddress.middle,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BorderDefault
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(30.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            PixelButtonCode(
                                modifier = Modifier.weight(1f),
                                text = "취소",
                                onClick = { onCancel() },
                                mainColor = Color.Gray,
                                borderColor = BorderDefault,
                                textColor = Color.White,
                                fontSize = 16,
                                blockHeight = 10
                            )

                            PixelButtonCode(
                                modifier = Modifier.weight(1f),
                                text = "확인",
                                onClick = {
                                    if (viewModel.isValid()) {
                                        viewModel.createChatRoom()
                                    }
                                },
                                mainColor = CustomBlue,
                                borderColor = BorderDefault,
                                textColor = Color.White,
                                fontSize = 16,
                                blockHeight = 10
                            )
                        }
                    }
                }
            }
        }
    }
}