package com.d104.pnt.ui.chatroom.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d104.pnt.ui.component.PixelContainer
import com.d104.pnt.ui.component.PixelIconButton
import com.d104.pnt.ui.component.UserProfileCard
import com.d104.pnt.ui.game.wait.DelegateHostConfirmDialog
import com.d104.pnt.ui.game.wait.ReasonButtonRow
import com.d104.pnt.ui.theme.AccentYellow
import com.d104.pnt.ui.theme.ChatBg
import com.d104.pnt.ui.theme.DarkBackground
import com.d104.pnt.ui.theme.PixelFont
import com.d104.pnt.ui.theme.TextPrimary
import com.d104.pnt.ui.theme.TextSecondary

@Composable
fun ChatRoomMemberDrawer(
    visible: Boolean,
    members: List<ChatRoomMemberUi>,
    myMemberId: Long,
    onDismiss: () -> Unit,
    onLeaveRoom: () -> Unit,
    onDelegate: (Long) -> Unit,
    onKick: (Long, String) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ChatRoomViewModel = hiltViewModel()
) {
    var delegateTarget by remember { mutableStateOf<ChatRoomMemberUi?>(null) }
    var kickTarget by remember { mutableStateOf<ChatRoomMemberUi?>(null) }
    var profileTarget by remember { mutableStateOf<ChatRoomMemberUi?>(null) }

    val isProfileLoading by viewModel.isProfileLoading.collectAsStateWithLifecycle()
    val selectedProfile by viewModel.selectedProfile.collectAsStateWithLifecycle()

    val amIHost = members.find { it.memberId == myMemberId }?.isHost == true

    // 바깥 클릭 시 닫기
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(160)),
        exit = fadeOut(animationSpec = tween(160)),
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Transparent)
                .clickable { onDismiss() }
        )
    }

    // 우측 패널
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            animationSpec = tween(220),
            initialOffsetX = { fullWidth -> fullWidth }
        ),
        exit = slideOutHorizontally(
            animationSpec = tween(220),
            targetOffsetX = { fullWidth -> fullWidth }
        )
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterEnd
        ) {
            PixelContainer(
                modifier = Modifier
                    .width(260.dp)
                    .fillMaxHeight()
                    .clickable(enabled = false) {},
                cornerSize = 10f,
                backgroundColor = ChatBg,
                borderColor = Color.Transparent,
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // 멤버 목록
                    Column {
                        Text(
                            text = "멤버 목록",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )

                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = TextSecondary)
                        Spacer(Modifier.height(12.dp))

                        LazyColumn(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(members, key = { it.memberId }) { member ->
                                ChatMemberRow(
                                    member = member,
                                    isAmIHost = amIHost,
                                    isMe = member.memberId == myMemberId,
                                    onAction = { action ->
                                        when (action) {
                                            "PROFILE" -> {
                                                profileTarget = member
                                                viewModel.loadUserProfile(member.memberId)
                                            }

                                            "DELEGATE" -> delegateTarget = member
                                            "KICK" -> kickTarget = member
                                        }
                                    }
                                )
                            }
                        }
                    }

                    // 나가기 버튼
                    Column {
                        Spacer(Modifier.height(12.dp))
                        HorizontalDivider(color = TextSecondary)
                        Spacer(Modifier.height(12.dp))

                        PixelContainer(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .clickable { onLeaveRoom() },
                            backgroundColor = DarkBackground,
                            borderColor = TextSecondary,
                            innerVerticalPadding = 12,
                            innerHorizontalPadding = 12
                        ) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "채팅방 나가기",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimary
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // --- 다이얼로그 호출 ---

    // 프로필 다이얼로그
    if (profileTarget != null) {
        ChatPlayerInfoDialog(
            profile = selectedProfile,
            isLoading = isProfileLoading,
            onDismiss = {
                profileTarget = null
                viewModel.clearSelectedProfile()
            }
        )
    }

    // 방장 위임 다이얼로그 (GameRoomDialog 재사용 불가 시 Chat 전용 사용)
    if (delegateTarget != null) {
        DelegateHostConfirmDialog(
            nickname = delegateTarget!!.nickname,
            onDismissRequest = { delegateTarget = null },
            onConfirm = {
                onDelegate(delegateTarget!!.memberId)
                delegateTarget = null
            }
        )
    }

    // 강퇴 다이얼로그
    if (kickTarget != null) {
        ChatKickConfirmDialog(
            member = kickTarget!!,
            onDismiss = { kickTarget = null },
            onConfirm = { reason ->
                onKick(kickTarget!!.memberId, reason)
                kickTarget = null
            }
        )
    }
}

@Composable
private fun ChatMemberRow(
    member: ChatRoomMemberUi,
    isAmIHost: Boolean,
    isMe: Boolean,
    onAction: (String) -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.nickname,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary
            )
            Text(
                text = if (member.isHost) "방장" else "멤버",
                style = MaterialTheme.typography.bodySmall,
                color = if (member.isHost) AccentYellow else TextSecondary
            )
        }

        // 메뉴 버튼 (점 3개)
        Box {
            IconButton(
                onClick = { showMenu = true },
                modifier = Modifier.size(48.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "메뉴",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            if (showMenu) {
                ChatMemberActionMenu(
                    isAmIHost = isAmIHost,
                    isTargetMe = isMe,
                    onDismiss = { showMenu = false },
                    onProfileClick = { showMenu = false; onAction("PROFILE") },
                    onDelegateHostClick = { showMenu = false; onAction("DELEGATE") },
                    onKickClick = { showMenu = false; onAction("KICK") }
                )
            }
        }
    }
}

@Composable
private fun ChatMemberActionMenu(
    isAmIHost: Boolean,
    isTargetMe: Boolean,
    onDismiss: () -> Unit,
    onProfileClick: () -> Unit,
    onDelegateHostClick: () -> Unit,
    onKickClick: () -> Unit
) {
    val density = LocalDensity.current
    val yOffset = remember(density) { with(density) { (32.dp).roundToPx() } }

    Popup(
        alignment = Alignment.TopEnd,
        offset = IntOffset(0, yOffset),
        onDismissRequest = onDismiss
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            ChatMenuButton(text = "프로필 조회", textColor = Color.Black, onClick = onProfileClick)

            // 내가 방장이고, 대상이 내가 아닐 때만 관리 기능 표시
            if (isAmIHost && !isTargetMe) {
                ChatMenuButton(
                    text = "방장 위임",
                    textColor = Color.Black,
                    onClick = onDelegateHostClick
                )
                ChatMenuButton(text = "강퇴하기", textColor = Color(0xFFFF5252), onClick = onKickClick)
            }
        }
    }
}

@Composable
private fun ChatMenuButton(text: String, textColor: Color, onClick: () -> Unit) {
    PixelContainer(
        modifier = Modifier.width(120.dp),
        backgroundColor = Color(0xFFC5C5C5),
        borderColor = Color.Gray,
        borderWidth = 2f,
        cornerSize = 8f,
        innerVerticalPadding = 2,
        innerHorizontalPadding = 0
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                fontFamily = PixelFont,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }
    }
}


// --- 채팅방 전용 다이얼로그 ---

@Composable
fun ChatPlayerInfoDialog(
    profile: ProfileData?,
    isLoading: Boolean,
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
                profile?.let { data ->
                    UserProfileCard(
                        nickname = data.nickname,
                        avatarUrl = data.avatarUrl,
                        policeGrade = data.policeGrade,
                        thiefGrade = data.thiefGrade,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

@Composable
fun ChatKickConfirmDialog(
    member: ChatRoomMemberUi,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
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
                    text = member.nickname,
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
                                    "강퇴하기",
                                    fontFamily = PixelFont,
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            }
                        )
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
                                    "취소",
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
}

// Data class (기존과 동일)
data class ChatRoomMemberUi(
    val memberId: Long,
    val nickname: String,
    val isHost: Boolean = false
)