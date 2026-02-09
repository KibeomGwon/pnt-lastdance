package com.d104.pnt.ui.game.play

import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d104.pnt.R
import com.d104.pnt.data.repository.ArrestStatus
import com.d104.pnt.data.repository.CctvPhase
import com.d104.pnt.data.repository.GameSessionEvent
import com.d104.pnt.data.repository.HelicopterPhase
import com.d104.pnt.data.repository.MissionStatus
import com.d104.pnt.data.repository.WalkieConnectionState
import com.d104.pnt.domain.model.GameRole
import com.d104.pnt.service.location.LocationService
import com.d104.pnt.ui.component.AlertOverlay
import com.d104.pnt.ui.component.ArrestOverlay
import com.d104.pnt.ui.component.ContDownUI
import com.d104.pnt.ui.component.ExpandableCard
import com.d104.pnt.ui.component.GameEndOverlay
import com.d104.pnt.ui.component.OutlinedText
import com.d104.pnt.ui.component.PixelContainer
import com.d104.pnt.ui.component.PixelIconButton
import com.d104.pnt.ui.component.PixelLoading
import com.d104.pnt.ui.component.WarningOverlay
import com.d104.pnt.ui.game.play.mission.BottomSheetState
import com.d104.pnt.ui.game.play.mission.MissionBottomSheet
import com.d104.pnt.ui.game.play.walkietalkie.WalkieBottomSheet
import com.d104.pnt.ui.game.play.walkietalkie.WalkieTalkieContent
import com.d104.pnt.ui.theme.BackgroundWhite
import com.d104.pnt.ui.theme.ButtonDisabled
import com.d104.pnt.ui.theme.MissionYellow
import com.d104.pnt.ui.theme.PixelFont
import com.d104.pnt.util.GameFeedbackManager
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun GamePlayScreen(
    gameId: Long,
    role: GameRole,
    onBackToHome: () -> Unit,
    onNavigateToLoading: (Long) -> Unit,
    goToCamera: (Long) -> Unit,
    viewModel: GamePlayViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var backPressedTime by remember { mutableLongStateOf(0L) }
    var clicked by remember { mutableStateOf(false) }
    var phoneScreen by remember { mutableStateOf(PhoneScreen.NO_SIGNAL) }
    var showGameOverOverlay by remember { mutableStateOf(false) }
    var showThiefEscaped by remember { mutableStateOf(false) }
    var escapedThiefNickname by remember { mutableStateOf("") }
    val feedbackManager = remember { GameFeedbackManager(context.applicationContext) }

    val currentLocation = viewModel.userLocation.collectAsStateWithLifecycle().value
    val areaPoints = viewModel.polygonPoints.collectAsStateWithLifecycle().value
    val prisonLocation = viewModel.prisonLocation.collectAsStateWithLifecycle().value
    val memberLocation by viewModel.memberLocation.collectAsStateWithLifecycle()

    val remainingTime = viewModel.remainingTime.collectAsStateWithLifecycle()

    val onBoundaryWarning = viewModel.onBoundaryWarning.collectAsStateWithLifecycle()
    val thiefMembers by viewModel.thiefMembers.collectAsStateWithLifecycle()
    val escapeQueue by viewModel.escapeQueue.collectAsStateWithLifecycle()

    val missions by viewModel.missions.collectAsStateWithLifecycle()
    val missionState by viewModel.missionState.collectAsStateWithLifecycle()
    val missionFailReason by viewModel.missionFailReason.collectAsStateWithLifecycle()

    val myMemberId by viewModel.myMemberId.collectAsStateWithLifecycle()
    val myState by viewModel.myState.collectAsStateWithLifecycle()

    val helicopterState by viewModel.helicopterState.collectAsStateWithLifecycle()
    val isChief by viewModel.isChief.collectAsStateWithLifecycle()
    val helicopterEnabled by viewModel.helicopterButtonEnabled.collectAsStateWithLifecycle()

    val walkieState by viewModel.walkieState.collectAsStateWithLifecycle()
    val walkieConnected by viewModel.walkieConnected.collectAsStateWithLifecycle()
    val walkieMicEnabled by viewModel.walkieMicEnabled.collectAsStateWithLifecycle()
    val walkieParticipantCount by viewModel.walkieParticipantCount.collectAsStateWithLifecycle()
    val isSomeoneTalking by viewModel.isSomeoneTalking.collectAsStateWithLifecycle()

    val cctvPhase by viewModel.cctvPhase.collectAsStateWithLifecycle()
    val cctvThief by viewModel.cctvThief.collectAsStateWithLifecycle()

    val arrestStatus by viewModel.arrestStatus.collectAsStateWithLifecycle()
    val arrestFailReason by viewModel.arrestFailReason.collectAsStateWithLifecycle()

    var previousWalkieState by remember { mutableStateOf(BottomSheetState.COLLAPSED) }

    var showProximityAlert by remember { mutableStateOf(false) }
    var proximityMessage by remember { mutableStateOf("") }
    var proximityColor by remember { mutableStateOf(Color.Transparent) }
    var proximityAlertJob by remember { mutableStateOf<kotlinx.coroutines.Job?>(null) }

    LaunchedEffect(Unit) {
        viewModel.initGame()

        // 무전기 연결
        if (role == GameRole.POLICE) {
            delay(500)
            viewModel.connectWalkie()
        }

        viewModel.uiEvent.collect { event ->
            if (event is GameSessionEvent.NavigateToLoading) {
                showGameOverOverlay = true

                delay(5000L)

                onNavigateToLoading(event.gameId)
            }
        }
    }

    BackHandler {
        if (System.currentTimeMillis() - backPressedTime <= 1500) {
            viewModel.manualLeaveGame()
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

    // 위치서비스 시작 / 종료
    DisposableEffect(Unit) {
        val serviceIntent = Intent(context, LocationService::class.java).apply {
            putExtra(LocationService.EXTRA_GAME_MODE, true)
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(serviceIntent)
        } else {
            context.startService(serviceIntent)
        }

        onDispose {
            context.stopService(serviceIntent)
            viewModel.disconnectWalkie()
        }
    }

    LaunchedEffect(role) {
        if (role != GameRole.THIEF) return@LaunchedEffect

        viewModel.beepEvent.collect { beep ->

            if (viewModel.myMemberId.value == beep.thiefId) {
                feedbackManager.playBeepAlert(distance = beep.distance)

                val distance = beep.distance ?: 999.0

                if (distance <= 10.0) {
                    proximityMessage = "인기척이 느껴집니다..."
                    proximityColor = Color(0xFFFF3D00)
                    showProximityAlert = true
                } else if (distance <= 35.0) {
                    proximityMessage = "누군가 있는 것 같은 기분이 듭니다..."
                    proximityColor = Color(0xFFFF9800)
                    showProximityAlert = true
                }

                if (showProximityAlert) {
                    proximityAlertJob?.cancel()
                    proximityAlertJob = launch {
                        delay(3000L)
                        showProximityAlert = false
                    }
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            if (escapeQueue.isNotEmpty() && !showThiefEscaped) {
                escapedThiefNickname = escapeQueue.first()
                showThiefEscaped = true

                delay(3000)

                showThiefEscaped = false

                viewModel.dequeEscape()

                delay(500)
            } else {
                delay(100)
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        // 배경
        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.bg_night),
            contentDescription = null,
            contentScale = ContentScale.Crop
        )

        Box(modifier = Modifier.fillMaxSize()) {

            // 상단 버튼
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 24.dp)
                    .systemBarsPadding()
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 지도
                PixelIconButton(
                    modifier = Modifier
                        .size(50.dp)
                        .zIndex(100f),
                    borderColor = ButtonDisabled,
                    pixelSize = 3.dp,
                    onClick = {
                        phoneScreen = PhoneScreen.MAP
                        clicked = !clicked
                    }
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_map),
                        contentDescription = null,
                        tint = Color.Unspecified
                    )
                }

                // 경찰 기능 버튼들
                if (role == GameRole.POLICE) {
                    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {

                        // 카메라
                        PixelIconButton(
                            modifier = Modifier.size(50.dp),
                            borderColor = ButtonDisabled,
                            pixelSize = 3.dp,
                            onClick = {
                                phoneScreen = PhoneScreen.CAMERA
                                clicked = !clicked
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_camera),
                                contentDescription = null,
                                tint = Color.Unspecified
                            )
                        }

                        // 도둑 리스트
                        PixelIconButton(
                            modifier = Modifier.size(50.dp),
                            borderColor = ButtonDisabled,
                            pixelSize = 3.dp,
                            onClick = {
                                phoneScreen = PhoneScreen.THIEF_LIST
                                clicked = !clicked
                            }
                        ) {
                            Icon(
                                painter = painterResource(R.drawable.ic_thief_list),
                                contentDescription = null,
                                tint = Color.Unspecified
                            )
                        }
                    }
                }
            }

            // 중앙 컨텐츠
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .align(Alignment.Center),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                ContDownUI(remainingSeconds = remainingTime.value, themeColor = BackgroundWhite)
                Spacer(modifier = Modifier.height(30.dp))

                FlipImage(
                    role = role,
                    memberId = myMemberId,
                    canFlip = isChief,
                    helicopterEnabled = helicopterEnabled,
                    onHelicopterClick = { viewModel.useHelicopterSkill() }
                )

            }

            // 경찰 근접 경고
            val isCriticalOverlayActive = (myMemberId in onBoundaryWarning.value) ||
                    (arrestStatus != ArrestStatus.IDLE) ||
                    (myState == "PRISON" || myState == "TRANSFER")

            val showWarningUI = showProximityAlert && !clicked && !isCriticalOverlayActive

            AnimatedVisibility(
                visible = showWarningUI,
                enter = fadeIn(),
                exit = fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 150.dp)
                    .zIndex(1f)
            ) {
                PixelContainer(
                    backgroundColor = Color.Black.copy(alpha = 0.7f),
                    borderColor = proximityColor,
                    borderWidth = 4f,
                    innerVerticalPadding = 12,
                    innerHorizontalPadding = 20
                ) {
                    Text(
                        text = proximityMessage,
                        fontFamily = PixelFont,
                        color = proximityColor,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        if (role == GameRole.THIEF) {
            MissionBottomSheet {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "미션 성공 시",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "CCTV에 포착되지 않습니다",
                        style = MaterialTheme.typography.titleMedium,
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(20.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    userScrollEnabled = true
                ) {
                    items(missions) { mission ->
                        ExpandableCard(
                            modifier = Modifier,
                            title = mission.Mission.title,
                            disabled = mission.status == "SUCCESS",
                        ) {
                            Column(
                                verticalArrangement = Arrangement.spacedBy(20.dp)
                            ) {
                                Text(
                                    modifier = Modifier.fillMaxWidth(),
                                    text = mission.Mission.description,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )

                                PixelContainer(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable(onClick = {
                                            if (mission.status == "IN_PROGRESS") goToCamera(
                                                mission.id
                                            )
                                        }),
                                    backgroundColor = Color.Transparent,
                                    borderColor = MissionYellow,
                                    borderWidth = 8f
                                ) {
                                    Text(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 2.dp),
                                        text = "미션 수행하기",
                                        textAlign = TextAlign.Center,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = MissionYellow
                                    )
                                }
                            }
                        }
                    }

                    item { Spacer(modifier = Modifier.height(100.dp)) }
                }
            }

        } else {
            WalkieBottomSheet(
                isSomeoneTalking = isSomeoneTalking,
                channel = when (walkieState) {
                    is WalkieConnectionState.Idle -> "CH 00 · 대기 중"
                    is WalkieConnectionState.Connecting -> "CH 00 · 연결 중..."
                    is WalkieConnectionState.Connected -> "CH 00 · 전체 (${walkieParticipantCount + 1}명)"
                    is WalkieConnectionState.Error -> "CH 00 · 오류 발생"
                },
                onSheetStateChanged = { newState ->
                    if (previousWalkieState != newState) {
                        when (newState) {
                            BottomSheetState.EXPANDED -> {
                                viewModel.playWalkieOpenSound()
                            }

                            BottomSheetState.COLLAPSED -> {
                                viewModel.playWalkieCloseSound()
                            }
                        }
                        previousWalkieState = newState
                    }
                }
            ) {
                WalkieTalkieContent(
                    channel = when (walkieState) {
                        is WalkieConnectionState.Idle -> "CH 00 · 대기 중"
                        is WalkieConnectionState.Connecting -> "CH 00 · 연결 중..."
                        is WalkieConnectionState.Connected -> "CH 00 · 전체 "
                        is WalkieConnectionState.Error -> "CH 00 · 오류 발생"
                    },
                    isTalking = walkieMicEnabled,
                    isSomeoneTalking = isSomeoneTalking,
                    onPttDown = {
                        if (walkieConnected && !isSomeoneTalking) {
                            viewModel.startTalking()
                        } else if (isSomeoneTalking) {
                        }
                    },
                    onPttUp = { if (walkieConnected) viewModel.stopTalking() },
                )

            }

        }

    }

    // 경찰 헬기 안내 오버레이
    if (helicopterState == HelicopterPhase.NOTIFY && role == GameRole.POLICE) {

        LaunchedEffect(helicopterState) {
            viewModel.playHelicopterSound()
        }

        WarningOverlay(
            onWarning = false,
            success = true,
            warningTitle = "경찰 헬기 지원!",
            warningMessage = "곧 공중 지원이\n도착합니다!"
        )
    }
    if (helicopterState == HelicopterPhase.REVEAL && role == GameRole.POLICE) {
        WarningOverlay(
            onWarning = false,
            success = true,
            warningTitle = "경찰 헬기 도착!",
            warningMessage = "모든 도둑의 위치가\n잠시동안 노출 됩니다!"
        )
    }

    // 경찰 CCTV 안내 오버레이
    if (cctvPhase == CctvPhase.NOTIFY && role == GameRole.POLICE) {
        WarningOverlay(
            onWarning = false,
            warningTitle = "CCTV 이상징후 포착!",
            warningMessage = "잠시 후 도둑의\n위치가 공개 됩니다"
        )
    }
    if (cctvPhase == CctvPhase.REVEAL && role == GameRole.POLICE) {
        WarningOverlay(
            onWarning = false,
            warningTitle = "수배범 포착!",
            warningMessage = "지도에 위치가\n표시 됩니다"
        )
    }
    if (arrestStatus != ArrestStatus.IDLE) {
        AlertOverlay(
            title = if (arrestStatus == ArrestStatus.SUCCESS) "체포 성공!" else "체포 실패",
            message = if (arrestStatus == ArrestStatus.SUCCESS) "" else when (arrestFailReason) {
                "NOT_THIEF" -> "도둑이 아닙니다"
                "ARRESTER_NOT_POLICE" -> "경찰만 체포할 수\n있습니다"
                "ALREADY_CAUGHT" -> "이미 체포된\n도둑입니다"
                else -> ""
            },
            success = arrestStatus == ArrestStatus.SUCCESS
        )
    }


    // PhoneFrame
    if (clicked && currentLocation != null && prisonLocation != null) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            PhoneFrame(
                screen = phoneScreen,
                onScanSuccess = { thiefId ->
                    viewModel.arrestThief(thiefId.toLong())
                    clicked = false
                },
                role = role,
                myMemberId = myMemberId,
                currentLocation = LatLng(currentLocation.latitude, currentLocation.longitude),
                areaPoints = areaPoints,
                prisonLocation = LatLng(prisonLocation.latitude, prisonLocation.longitude),
                thiefMembers = thiefMembers,
                playerLocations = if (role == GameRole.POLICE) memberLocation else emptyList()
            )
        }
    }

    // 경기구역이탈 오버레이
    if (myMemberId in onBoundaryWarning.value) {
        WarningOverlay(
            onWarning = true,
            warningTitle = "경기구역이탈!",
            warningMessage = "경기 구역으로\n복귀하세요"
        )
    }

    // 도둑 CCTV 발각 경고 오버레이
    if (cctvPhase == CctvPhase.REVEAL && role == GameRole.THIEF && myMemberId == cctvThief) {
        WarningOverlay(
            onWarning = true,
            warningTitle = "위치 노출!",
            warningMessage = "CCTV에 찍혔습니다!\n위치가 노출 됩니다!"
        )
    }

    // 도둑 탈출 알림
    if (showThiefEscaped && role == GameRole.POLICE) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .zIndex(100f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.fillMaxHeight(0.75f))

            OutlinedText(
                text = "도둑이 탈출에\n성공했습니다!",
                fontSize = 40.sp,
                success = false
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedText(
                text = escapedThiefNickname,
                fontSize = 32.sp,
                success = false
            )
        }
    }

    // 미션 제출 결과 알림창
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        when (missionState) {
            MissionStatus.IDLE -> {}
            MissionStatus.IN_ANALYZE -> {
                PixelLoading(message = "미션 수행중...")
            }

            MissionStatus.SUCCESS -> {
                AlertOverlay(
                    title = "미션 수행 성공!",
                    message = "CCTV에 노출되지 않습니다",
                    success = true
                )
            }

            MissionStatus.FAIL -> {
                AlertOverlay(
                    title = "미션 실패!!",
                    message = missionFailReason,
                    success = false
                )
            }
        }
    }

    ArrestOverlay(
        modifier = Modifier.zIndex(50f),
        isVisible = myState == "TRANSFER" || myState == "PRISON"
    )

    AnimatedVisibility(
        modifier = Modifier.zIndex(100f),
        visible = showGameOverOverlay,
        enter = slideInVertically() + fadeIn()
    ) {
        GameEndOverlay()
    }
}

enum class PhoneScreen {
    NO_SIGNAL,
    MAP,
    CAMERA,
    THIEF_LIST,
}
