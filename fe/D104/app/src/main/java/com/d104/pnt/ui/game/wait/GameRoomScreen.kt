package com.d104.pnt.ui.game.wait

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d104.pnt.R
import com.d104.pnt.domain.model.GameRole
import com.d104.pnt.domain.model.GameRoomUiEvent
import com.d104.pnt.domain.model.WaitingPlayer
import com.d104.pnt.domain.model.common.UiState
import com.d104.pnt.ui.component.PixelIconButton
import com.d104.pnt.ui.theme.PixelFont
import com.google.android.gms.maps.model.LatLng

@Composable
fun GameRoomScreen(
    roomId: Long,
    initialRole: GameRole,
    onChangeRole: () -> Unit,
    onStartGame: (Long, GameRole) -> Unit = { _, _ -> },
    onNavigateRole: (Long, GameRole, Boolean) -> Unit,
    viewModel: GameRoomViewModel = hiltViewModel(),
    onBackPressed: () -> Boolean = { false },
    onNavigateHome: (String?) -> Unit = { }
) {
    val context = LocalContext.current
    var backPressedTime by remember { mutableLongStateOf(0L) }

    val players by viewModel.players.collectAsStateWithLifecycle()
    val roomInfo by viewModel.roomInfo.collectAsStateWithLifecycle()
    val isHost by viewModel.isHost.collectAsStateWithLifecycle()
    val isMeReady by viewModel.isMeReady.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val myMemberId by viewModel.myMemberId.collectAsStateWithLifecycle()

    val selectedProfile by viewModel.selectedProfile.collectAsStateWithLifecycle()
    val isProfileLoading by viewModel.isProfileLoading.collectAsStateWithLifecycle()

    var selectedPlayerId by remember { mutableStateOf<Long?>(null) }
    var dismissedPlayerId by remember { mutableStateOf<Long?>(null) }
    var lastDismissTime by remember { mutableLongStateOf(0L) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    var infoDialogTarget by remember { mutableStateOf<WaitingPlayer?>(null) }
    var delegateDialogTarget by remember { mutableStateOf<WaitingPlayer?>(null) }
    var kickDialogTarget by remember { mutableStateOf<WaitingPlayer?>(null) }

    var showLeaveDialog by remember { mutableStateOf(false) }

    // 이벤트 처리
    LaunchedEffect(Unit) {
        viewModel.setInitialRole(initialRole)
        viewModel.uiEvent.collect { event ->
            when (event) {
                is GameRoomUiEvent.NavigateToHome -> {
                    if (!onBackPressed()) onNavigateHome(event.message)
                }

                is GameRoomUiEvent.NavigateToGame -> {
                    onNavigateRole(event.roomId, GameRole.fromName((event.role)), event.isChief)
                }
            }
        }
    }

    BackHandler {
        if (System.currentTimeMillis() - backPressedTime <= 1500) {
            viewModel.leaveRoom()
        } else {
            backPressedTime = System.currentTimeMillis()
            Toast.makeText(
                context,
                "한 번 더 누르면 방에서 나갑니다",
                Toast.LENGTH_SHORT
            ).show()
        }
    }


    LaunchedEffect(uiState) {
        if (uiState is UiState.Success) {
            val myRole = players.find { it.id == myMemberId }?.role ?: GameRole.THIEF

            onStartGame(roomId, myRole)
        }
    }

    val policeCount = players.count { it.role == GameRole.POLICE && !it.isChangingRole }

    val thiefCount = players.count { it.role == GameRole.THIEF && !it.isChangingRole }

    val anyCount = players.count { it.role == GameRole.ANY && !it.isChangingRole }

    val isAllReady = players.isNotEmpty()
            && players.size == roomInfo.maxCount
            && players.filter { it.id != myMemberId }.all {
        it.isReady && !it.isChangingRole
    }

    val canChangeRole = isHost || !isMeReady

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.bg_playground),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 32.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // 방 정보
            GameRoomHeader(
                roomCode = roomInfo.roomCode,
                currentCount = players.size,
                maxCount = roomInfo.maxCount,
                timeLeft = "${roomInfo.timeLimit}:00",
                isHost = isHost,
                onSettingsClick = { showSettingsDialog = true },
                onLeaveClick = { showLeaveDialog = true }
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 보드
            GameRoomBoard(
                players = players,
                myMemberId = myMemberId,
                policeCount = policeCount,
                thiefCount = thiefCount,
                anyCount = anyCount,
                isHost = isHost,
                selectedPlayerId = selectedPlayerId,
                prisonLocation = LatLng(
                    roomInfo.prison?.lat ?: 37.56681969564895,
                    roomInfo.prison?.lng ?: 126.97864094105321
                ),
                polygonPoints = roomInfo.polygon?.map { LatLng(it.lat, it.lng) } ?: emptyList(),
                maxPolice = roomInfo.policeCount,
                maxThief = roomInfo.maxCount - roomInfo.policeCount,

                onPlayerClick = { player ->
                    val now = System.currentTimeMillis()
                    if (!((dismissedPlayerId == player.id) && (now - lastDismissTime < 300))) selectedPlayerId =
                        if (selectedPlayerId == player.id) null else player.id
                },
                onMenuDismiss = {
                    dismissedPlayerId = selectedPlayerId
                    lastDismissTime = System.currentTimeMillis()
                    selectedPlayerId = null
                },
                onInfoClick = { player ->
                    dismissedPlayerId = selectedPlayerId
                    lastDismissTime = System.currentTimeMillis()
                    selectedPlayerId = null
                    viewModel.loadUserProfile(player.id)
                    infoDialogTarget = player
                },
                onDelegateHostClick = { player ->
                    dismissedPlayerId = selectedPlayerId
                    lastDismissTime = System.currentTimeMillis()
                    selectedPlayerId = null
                    delegateDialogTarget = player
                },
                onKickClick = { player ->
                    dismissedPlayerId = selectedPlayerId; lastDismissTime =
                    System.currentTimeMillis(); selectedPlayerId = null; kickDialogTarget =
                    player
                },
                onChangeRole = {
                    viewModel.resetToUndecided()
                    onChangeRole()
                },
                canChangeRole = canChangeRole,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 준비 / 시작
            val buttonText = if (isHost) "게임 시작" else if (isMeReady) "준비 취소" else "준비"
            val buttonColor = if (isHost) {
                if (isAllReady) Color.White else Color.Gray
            } else {
                if (isMeReady) Color.Gray else Color.White
            }
            val textColor = if (buttonColor == Color.Gray) Color.White else Color.Black

            PixelIconButton(
                onClick = {
                    if (isHost) {
                        if (isAllReady) viewModel.startGame()
                    } else viewModel.toggleReady()
                },
                modifier = Modifier.fillMaxWidth(),
                mainColor = buttonColor,
                borderColor = if (buttonColor == Color.Gray) Color.DarkGray else Color.Black,
                pixelSize = 3.5.dp,
                blockHeight = 16,
                content = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = buttonText,
                            fontFamily = PixelFont,
                            color = textColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp
                        )
                    }
                }
            )
            Spacer(modifier = Modifier.height(60.dp))
        }

        // 다이얼로그
        if (infoDialogTarget != null && selectedProfile != null) {
            PlayerInfoDialog(
                profile = selectedProfile!!,
                isLoading = isProfileLoading,
                onDismiss = {
                    infoDialogTarget = null
                    viewModel.clearSelectedProfile()
                }
            )
        }

        if (kickDialogTarget != null) KickConfirmDialog(
            player = kickDialogTarget!!,
            onDismiss = { kickDialogTarget = null },
            onConfirm = { reason ->
                viewModel.kickPlayer(
                    kickDialogTarget!!.id,
                    reason
                ); kickDialogTarget = null
            })

        if (delegateDialogTarget != null) {
            DelegateHostConfirmDialog(
                nickname = delegateDialogTarget!!.nickname,
                onDismissRequest = { delegateDialogTarget = null },
                onConfirm = {
                    viewModel.delegateHost(delegateDialogTarget!!.id)
                    delegateDialogTarget = null
                }
            )
        }

        if (showLeaveDialog) {
            LeaveRoomConfirmDialog(
                onDismissRequest = { showLeaveDialog = false },
                onConfirm = {
                    showLeaveDialog = false
                    viewModel.leaveRoom()
                }
            )
        }

        if (showSettingsDialog) GameSettingsDialog(
            initialState = roomInfo,
            onDismiss = { showSettingsDialog = false },
            onUpdateSettings = { total, time, mission, cctv, police, prison, polygon ->
                val calculatedThiefCount = total - police

                viewModel.updateRoomSettings(
                    total,
                    time,
                    mission,
                    cctv,
                    police,
                    prison,
                    polygon
                )
            }
        )
    }
}