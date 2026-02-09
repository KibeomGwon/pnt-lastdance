package com.d104.pnt.ui.game.wait

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.d104.pnt.data.remote.model.request.Location
import com.d104.pnt.domain.model.DraggableLatLng
import com.d104.pnt.domain.model.GameRoomInfoState
import com.d104.pnt.ui.component.GoogleMaps
import com.d104.pnt.ui.component.PixelButtonCode
import com.d104.pnt.ui.component.PixelContainer
import com.d104.pnt.ui.game.create.CounterControl
import com.d104.pnt.ui.game.create.FactionRatioBar
import com.d104.pnt.ui.game.create.GameGuideDialog
import com.d104.pnt.ui.game.create.SectionTitle
import com.d104.pnt.ui.theme.BorderDefault
import com.d104.pnt.ui.theme.CustomBlue
import com.d104.pnt.ui.theme.DarkSurface
import com.d104.pnt.ui.theme.DialogBorderColor
import com.d104.pnt.ui.theme.PixelFont
import com.google.android.gms.maps.model.LatLng

@Composable
fun GameSettingsDialog(
    initialState: GameRoomInfoState,
    onDismiss: () -> Unit,
    onUpdateSettings: (Int, Int, Int, Int, Int, Location, List<Location>) -> Unit
) {
    var totalPlayers by remember { mutableStateOf(initialState.maxCount.coerceAtLeast(5)) }
    var gameTime by remember { mutableStateOf(initialState.timeLimit.coerceAtLeast(5)) }

    var missionCount by remember { mutableStateOf(initialState.missionCount) }
    var cctvCycle by remember { mutableStateOf(initialState.cctvCycle) }

    var policeCount by remember { mutableStateOf(initialState.policeCount) }
    val thiefCount = totalPlayers - policeCount

    val scrollState = rememberScrollState()

    var prisonLocation by remember { mutableStateOf(initialState.prison) }
    var polygonPoints by remember { mutableStateOf(initialState.polygon) }

    var showMapPopup by remember { mutableStateOf(false) }

    var showMissionHelp by remember { mutableStateOf(false) }
    var showCCTVHelp by remember { mutableStateOf(false) }

    LaunchedEffect(thiefCount) {
        if (missionCount > thiefCount) {
            missionCount = thiefCount
        }
    }

    LaunchedEffect(gameTime) {
        if (cctvCycle >= gameTime) {
            cctvCycle = (gameTime - 1).coerceAtLeast(0)
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
            content = "특정 주기마다,\n미션을 클리어하지 않은 도둑 중\n무작위로 1명의 위치를\n지도에 10초간 보여줍니다.",
            onDismissRequest = { showCCTVHelp = false }
        )
    }

    Dialog(onDismissRequest = onDismiss) {
        PixelContainer(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 700.dp),
            backgroundColor = DarkSurface,
            borderColor = DialogBorderColor,
            borderWidth = 8f,
            cornerSize = 16f
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "게임 설정 변경",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(12.dp))

                // 맵 설정 & 감옥 설정
                SectionTitle(text = "맵 설정 & 감옥 설정")
                Spacer(modifier = Modifier.height(8.dp))

                // 지도 표시 영역
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFEEEEEE))
                ) {
                    GoogleMaps(
                        modifier = Modifier.fillMaxSize(),
                        inGameMinimap = false,
                        isPreview = true,
                        polygonPoints = polygonPoints?.map {
                            DraggableLatLng(
                                position = LatLng(
                                    it.lat,
                                    it.lng
                                )
                            )
                        } ?: emptyList(),
                        prisonLocation = LatLng(
                            prisonLocation?.lat ?: 37.56681969564895,
                            prisonLocation?.lng ?: 126.97864094105321
                        ),
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .clickable { showMapPopup = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "지도 수정하려면 클릭",
                            fontFamily = PixelFont,
                            color = Color.Black.copy(alpha = 0.5f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 게임 규칙
                SectionTitle(text = "게임 규칙")
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CounterControl(
                        Modifier.weight(1f), "플레이어", Icons.Default.Person,
                        totalPlayers.toString(), "명",
                        onDecrease = {
                            if (totalPlayers > 5) {
                                totalPlayers--
                                if (policeCount >= totalPlayers) {
                                    policeCount = totalPlayers - 1
                                }
                            }
                        },
                        onIncrease = {
                            if (totalPlayers < 30) {
                                totalPlayers++
                            }
                        }
                    )
                    CounterControl(
                        Modifier.weight(1f), "게임 시간", Icons.Default.Schedule,
                        gameTime.toString(), "분",
                        onDecrease = { if (gameTime > 5) gameTime -= 5 },
                        onIncrease = { if (gameTime < 60) gameTime += 5 }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CounterControl(
                        Modifier.weight(1f),
                        "전체 미션 수",
                        Icons.Default.List,
                        value = if (missionCount == 0) "없음" else missionCount.toString(),
                        unit = if (missionCount == 0) "" else "개",
                        onDecrease = {
                            if (missionCount > 0) missionCount--
                        },
                        onIncrease = {
                            if (missionCount < thiefCount) missionCount++
                        },
                        onHelpClick = { showMissionHelp = true }
                    )
                    CounterControl(
                        Modifier.weight(1f),
                        "CCTV 주기",
                        Icons.Default.Videocam,
                        value = if (cctvCycle == 0) "없음" else cctvCycle.toString(),
                        unit = if (cctvCycle == 0) "" else "분",
                        onDecrease = {
                            if (cctvCycle > 0) cctvCycle--
                        },
                        onIncrease = {
                            if (cctvCycle < gameTime - 1) cctvCycle++
                        },
                        onHelpClick = { showCCTVHelp = true }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 진영 비율
                SectionTitle(text = "진영 인원")
                Spacer(modifier = Modifier.height(8.dp))

                FactionRatioBar(
                    totalCount = totalPlayers,
                    policeCount = policeCount,
                    thiefCount = thiefCount,
                    onPoliceCountChange = { newPolice ->
                        if (newPolice in 1 until totalPlayers) {
                            policeCount = newPolice
                        }
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))


                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    PixelButtonCode(
                        text = "취소",
                        onClick = { onDismiss() },
                        modifier = Modifier.weight(1f),
                        mainColor = Color.Gray,
                        borderColor = BorderDefault,
                        textColor = Color.White,
                        fontSize = 16,
                        blockHeight = 10
                    )

                    PixelButtonCode(
                        text = "확인",
                        onClick = {
                            onUpdateSettings(
                                totalPlayers, gameTime, missionCount, cctvCycle, policeCount,
                                Location(prisonLocation!!.lat, prisonLocation!!.lng),
                                polygonPoints!!.map { Location(it.lat, it.lng) }
                            )

                            onDismiss()
                        },
                        modifier = Modifier.weight(1f),
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
    if (showMapPopup) {
        GameRoomMapSettingDialog(
            modifier = Modifier,
            onDismiss = { showMapPopup = false },
            onConfirm = { prison, polygon ->
                prisonLocation = prison
                polygonPoints = polygon
                showMapPopup = false
            }
        )
    }
}