package com.d104.pnt.ui.game.wait

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.d104.pnt.domain.model.WaitingPlayer
import com.d104.pnt.ui.chatroom.chat.ProfileData
import com.d104.pnt.ui.component.PixelContainer
import com.d104.pnt.ui.component.PixelIconButton
import com.d104.pnt.ui.component.UserProfileCard
import com.d104.pnt.ui.theme.AccentYellow
import com.d104.pnt.ui.theme.DarkSurface
import com.d104.pnt.ui.theme.PixelFont
import com.d104.pnt.ui.theme.RoomBorder
import com.d104.pnt.ui.theme.RoomContainer

@Composable
fun PlayerInfoDialog(
    profile: ProfileData,
    isLoading: Boolean = false,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White)
            } else {
                UserProfileCard(
                    nickname = profile.nickname,
                    avatarUrl = profile.avatarUrl,
                    policeGrade = profile.policeGrade,
                    thiefGrade = profile.thiefGrade,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
fun KickConfirmDialog(player: WaitingPlayer, onConfirm: (String) -> Unit, onDismiss: () -> Unit) {
    val reasons = listOf("욕설", "폭행", "비매너", "구역 이탈", "기타")
    var selectedReason by remember { mutableStateOf("욕설") }

    Dialog(onDismissRequest = onDismiss) {
        PixelContainer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            backgroundColor = Color(0xFF2D3242),
            borderColor = Color(0xFF8D90B3),
            borderWidth = 4f,
            cornerSize = 16f
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 24.dp, horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "정말 강퇴하시겠습니까?",
                    fontFamily = PixelFont,
                    color = AccentYellow,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = player.nickname,
                    fontFamily = PixelFont,
                    color = Color.White,
                    fontSize = 16.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))

                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "강퇴 사유",
                        fontFamily = PixelFont,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ReasonButtonRow(reasons.subList(0, 3), selectedReason) { selectedReason = it }
                    Spacer(modifier = Modifier.height(8.dp))
                    ReasonButtonRow(reasons.subList(3, 5), selectedReason) { selectedReason = it }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        PixelIconButton(
                            onClick = { onConfirm(selectedReason) },
                            modifier = Modifier.fillMaxWidth(),
                            mainColor = Color.White,
                            borderColor = Color.Black,
                            pixelSize = 3.dp,
                            blockHeight = 12,
                            content = {
                                Text(
                                    text = "강퇴하기",
                                    fontFamily = PixelFont,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            })
                    }
                    Box(modifier = Modifier.weight(1f)) {
                        PixelIconButton(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            mainColor = Color.White,
                            borderColor = Color.Black,
                            pixelSize = 3.dp,
                            blockHeight = 12,
                            content = {
                                Text(
                                    text = "취소",
                                    fontFamily = PixelFont,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            })
                    }
                }
            }
        }
    }
}

@Composable
fun ReasonButtonRow(items: List<String>, selectedItem: String, onSelect: (String) -> Unit) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { reason ->
            val isSelected = (reason == selectedItem)
            Box(modifier = Modifier.weight(1f)) {
                PixelIconButton(
                    onClick = { onSelect(reason) }, modifier = Modifier.fillMaxWidth(),
                    mainColor = if (isSelected) Color(0xFFD84315) else Color(0xFF37474F),
                    borderColor = if (isSelected) Color(0xFFFFCC80) else Color(0xFF78909C),
                    pixelSize = 2.dp, blockHeight = 10,
                    content = {
                        Text(
                            text = reason,
                            fontFamily = PixelFont,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                )
            }
        }
        if (items.size < 3) Spacer(modifier = Modifier.weight(1f))
    }
}

@Composable
fun DelegateHostConfirmDialog(
    nickname: String,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        PixelContainer(
            backgroundColor = DarkSurface,
            borderColor = Color.White,
            borderWidth = 3f,
            cornerSize = 8f,
            innerVerticalPadding = 30,
            innerHorizontalPadding = 20,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "방장을 위임하시겠습니까?",
                    fontFamily = PixelFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = AccentYellow,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = nickname,
                    fontFamily = PixelFont,
                    fontSize = 18.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PixelIconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        mainColor = Color.White,
                        borderColor = Color.Black,
                        pixelSize = 3.dp,
                        blockHeight = 12,
                        content = {
                            Text(
                                text = "취소",
                                fontFamily = PixelFont,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    PixelIconButton(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        mainColor = Color.White,
                        borderColor = Color.Black,
                        pixelSize = 3.dp,
                        blockHeight = 12,
                        content = {
                            Text(
                                text = "확인",
                                fontFamily = PixelFont,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun LeaveRoomConfirmDialog(
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        PixelContainer(
            backgroundColor = RoomContainer,
            borderColor = RoomBorder,
            borderWidth = 3f,
            cornerSize = 8f,
            innerVerticalPadding = 30,
            innerHorizontalPadding = 20,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "정말 나가시겠습니까?",
                    fontFamily = PixelFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PixelIconButton(
                        onClick = onDismissRequest,
                        modifier = Modifier.weight(1f),
                        mainColor = Color.White,
                        borderColor = Color.Black,
                        pixelSize = 3.dp,
                        blockHeight = 12,
                        content = {
                            Text(
                                text = "취소",
                                fontFamily = PixelFont,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    PixelIconButton(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f),
                        mainColor = Color.White,
                        borderColor = Color.Black,
                        pixelSize = 3.dp,
                        blockHeight = 12,
                        content = {
                            Text(
                                text = "확인",
                                fontFamily = PixelFont,
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    )
                }
            }
        }
    }
}