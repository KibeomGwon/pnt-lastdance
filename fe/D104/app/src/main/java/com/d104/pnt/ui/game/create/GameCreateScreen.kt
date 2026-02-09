package com.d104.pnt.ui.game.create

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.d104.pnt.data.remote.model.request.Location
import com.d104.pnt.domain.model.DraggableLatLng
import com.d104.pnt.domain.model.common.UiState
import com.d104.pnt.ui.component.CustomTextField
import com.d104.pnt.ui.component.GoogleMaps
import com.d104.pnt.ui.component.PixelButtonCode
import com.d104.pnt.ui.component.PixelContainer
import com.d104.pnt.ui.theme.BorderDefault
import com.d104.pnt.ui.theme.CustomBlue
import com.d104.pnt.ui.theme.RoomBorder
import com.d104.pnt.ui.theme.RoomContainer
import com.google.android.gms.maps.model.LatLng


@Composable
fun GameCreateScreen(
    onCancel: () -> Unit,
    onConfirm: (Long) -> Unit,
    viewModel: GameCreateViewModel = hiltViewModel()
) {
    val gameName by viewModel.gameName.collectAsStateWithLifecycle()
    val totalPlayers by viewModel.totalPlayers.collectAsStateWithLifecycle()
    val gameTime by viewModel.gameTime.collectAsStateWithLifecycle()
    val missionCount by viewModel.missionCount.collectAsStateWithLifecycle()
    val cctvCycle by viewModel.cctvCycle.collectAsStateWithLifecycle()
    val policeCount by viewModel.policeCount.collectAsStateWithLifecycle()
    val thiefCount by viewModel.thiefCount.collectAsStateWithLifecycle()

    val context = LocalContext.current

    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
    val polygonPoints by viewModel.polygonPoints.collectAsStateWithLifecycle()
    val prisonLocation by viewModel.prisonLocation.collectAsStateWithLifecycle()

    val gameRoomState by viewModel.gameRoomState.collectAsStateWithLifecycle()

    var showMapPopup by remember { mutableStateOf(false) }

    var showMissionHelp by remember { mutableStateOf(false) }
    var showCCTVHelp by remember { mutableStateOf(false) }

    val myMapsState by viewModel.myMaps.collectAsStateWithLifecycle()
    var showMapLoadPopup by remember { mutableStateOf(false) }

    val saveMap by viewModel.saveMap.collectAsStateWithLifecycle()
    val mapName by viewModel.mapName.collectAsStateWithLifecycle()

    val selectedMapId by viewModel.selectedMapId.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.setDefaultSettings(context)
    }

    LaunchedEffect(gameRoomState) {
        if (gameRoomState is UiState.Success) {
            val roomId = (gameRoomState as UiState.Success).data.roomId
            onConfirm(roomId)
        }
    }

    if (showMissionHelp) {
        GameGuideDialog(
            title = "미션이란?",
            content = "미션을 클리어한 도둑은\n경찰의 능력인 CCTV에\n더 이상 발각되지 않습니다.",
            onDismissRequest = { showMissionHelp = false }
        )
    }

    if (showCCTVHelp) {
        GameGuideDialog(
            title = "CCTV란?",
            content = "일정한 주기마다\n미션을 클리어하지 않은 도둑 중\n무작위로 1명의 위치를\n지도에 10초간 보여줍니다.",
            onDismissRequest = { showCCTVHelp = false }
        )
    }

    Surface(modifier = Modifier.fillMaxSize()) {

        Image(
            modifier = Modifier.fillMaxSize(),
            painter = painterResource(R.drawable.img_main_bg),
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
                    .padding(horizontal = 20.dp, vertical = 10.dp),
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
                            .padding(horizontal = 12.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = "게임 생성하기",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            modifier = Modifier.align(Alignment.CenterHorizontally)
                        )

                        Spacer(modifier = Modifier.height(30.dp))

                        SectionTitle(text = "맵 설정 & 감옥 설정")
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(140.dp)
                        ) {
                            userLocation?.let { loc ->
                                GoogleMaps(
                                    modifier = Modifier.fillMaxSize(),
                                    inGameMinimap = false,
                                    isPreview = true,
                                    polygonPoints = polygonPoints.map { DraggableLatLng(position = it) },
                                    currentLocation = LatLng(
                                        viewModel.userLocation.value!!.latitude,
                                        viewModel.userLocation.value!!.longitude
                                    ),
                                    prisonLocation = prisonLocation,
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .clickable { showMapPopup = true }
                            )

                            PixelContainer(
                                modifier = Modifier
                                    .padding(10.dp)
                                    .align(Alignment.TopEnd)
                                    .clickable {
                                        viewModel.fetchMyMaps()
                                        showMapLoadPopup = true
                                    },
                                borderColor = Color.Gray,
                                backgroundColor = Color.White,
                                cornerSize = 10f,
                                borderWidth = 5f,
                                innerVerticalPadding = 6,
                                innerHorizontalPadding = 6
                            ) {
                                Text(
                                    text = "맵 불러오기",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Black
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                Text(
                                    text = if (saveMap) "▣" else "□",
                                    color = if (saveMap) CustomBlue else Color.White,
                                    style = MaterialTheme.typography.bodyLarge,
                                    modifier = Modifier.clickable {
                                        viewModel.setSaveMap(!saveMap)
                                    }
                                )
                                Text(
                                    text = "현재 설정한 맵 구역 저장하기",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.White
                                )
                            }

                            if (saveMap) {
                                Spacer(modifier = Modifier.height(8.dp))
                                CustomTextField(
                                    value = mapName,
                                    onValueChange = { viewModel.setMapName(it) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(44.dp),
                                    placeholder = "저장할 맵 이름을 입력하세요",
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        SectionTitle(text = "게임 규칙")
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CounterControl(
                                modifier = Modifier.weight(1f),
                                label = "플레이어",
                                icon = Icons.Default.Person,
                                value = totalPlayers.toString(),
                                unit = "명",
                                onDecrease = { viewModel.updateTotalPlayers(false) },
                                onIncrease = { viewModel.updateTotalPlayers(true) }
                            )
                            CounterControl(
                                modifier = Modifier.weight(1f),
                                label = "게임 시간",
                                icon = Icons.Default.Schedule,
                                value = gameTime.toString(),
                                unit = "분",
                                onDecrease = { viewModel.updateGameTime(false) },
                                onIncrease = { viewModel.updateGameTime(true) }
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            CounterControl(
                                modifier = Modifier.weight(1f),
                                label = "전체 미션 수",
                                icon = Icons.Default.List,
                                value = if (missionCount == 0) "없음" else missionCount.toString(),
                                unit = if (missionCount == 0) "" else "개",
                                onDecrease = { viewModel.updateMissionCount(false) },
                                onIncrease = { viewModel.updateMissionCount(true) },
                                onHelpClick = { showMissionHelp = true }
                            )
                            CounterControl(
                                modifier = Modifier.weight(1f),
                                label = "CCTV 주기",
                                icon = Icons.Default.Videocam,
                                value = if (cctvCycle == 0) "없음" else cctvCycle.toString(),
                                unit = if (cctvCycle == 0) "" else "분",
                                onDecrease = { viewModel.updateCctvCycle(false) },
                                onIncrease = { viewModel.updateCctvCycle(true) },
                                onHelpClick = { showCCTVHelp = true }
                            )
                        }

                        Spacer(modifier = Modifier.height(24.dp))

                        SectionTitle(text = "진영 인원")
                        Spacer(modifier = Modifier.height(8.dp))

                        FactionRatioBar(
                            totalCount = totalPlayers,
                            policeCount = policeCount,
                            thiefCount = thiefCount,
                            onPoliceCountChange = { newCount -> viewModel.updatePoliceCount(newCount) }
                        )

                        Spacer(modifier = Modifier.height(36.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(20.dp)
                        ) {
                            PixelButtonCode(
                                text = "취소",
                                onClick = {
                                    viewModel.dismissCreateGame()
                                    onCancel()
                                },
                                modifier = Modifier.weight(1f),
                                mainColor = Color.Gray,
                                borderColor = BorderDefault,
                                textColor = Color.White,
                                fontSize = 15,
                                blockHeight = 15,
                                pixelSize = 2.8.dp
                            )

                            PixelButtonCode(
                                text = "확인",
                                onClick = {
                                    val polyPoint = polygonPoints.map {
                                        Location(lat = it.latitude, lng = it.longitude)
                                    }

                                    val closedPolygon = if (polyPoint.isNotEmpty()) {
                                        polyPoint + polyPoint.first()
                                    } else {
                                        polyPoint
                                    }

                                    val isValid = viewModel.isValid(
                                        playerCount = totalPlayers,
                                        timeLimit = gameTime,
                                        policeCount = policeCount,
                                        thiefCount = thiefCount,
                                        polygon = polyPoint
                                    )

                                    if (isValid) {
                                        viewModel.createGameRoom(
                                            playerCount = totalPlayers,
                                            timeLimit = gameTime,
                                            cctvInterval = cctvCycle,
                                            missionCount = missionCount,
                                            policeCount = policeCount,
                                            thiefCount = thiefCount,
                                            prison = Location(
                                                lat = prisonLocation!!.latitude,
                                                lng = prisonLocation!!.longitude
                                            ),
                                            polygon = closedPolygon
                                        )
                                    } else {

                                    }

                                },
                                modifier = Modifier.weight(1f),
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
        }
    }
    if (showMapPopup) {
        MapSettingDialog(
            modifier = Modifier,
            onDismiss = { showMapPopup = false },
            onConfirm = { showMapPopup = false }
        )
    }

    if (showMapLoadPopup) {
        MapLoadDialog(
            uiState = myMapsState,
            selectedMapId = selectedMapId,
            onDismiss = {
                viewModel.clearSelectedMap()
                showMapLoadPopup = false
            },
            onMapSelect = { selectedMap ->
                viewModel.selectMap(selectedMap.mapId)
            },
            onConfirm = {
                viewModel.applySelectedMap()
                showMapLoadPopup = false
            }
        )
    }
}

