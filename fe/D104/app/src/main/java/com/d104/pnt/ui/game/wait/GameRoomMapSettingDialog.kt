package com.d104.pnt.ui.game.wait

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d104.pnt.data.remote.model.request.Location
import com.d104.pnt.domain.model.DraggableLatLng
import com.d104.pnt.ui.component.GoogleMaps
import com.d104.pnt.ui.component.PixelButtonCode
import com.d104.pnt.ui.component.PixelContainer
import com.d104.pnt.ui.theme.BorderDefault
import com.d104.pnt.ui.theme.CustomBlue
import com.d104.pnt.ui.theme.DarkSurface
import com.d104.pnt.ui.theme.DialogBorderColor
import com.d104.pnt.ui.theme.TextPrimary
import com.google.android.gms.maps.model.LatLng

@Composable
fun GameRoomMapSettingDialog(
    modifier: Modifier,
    onDismiss: () -> Unit,
    onConfirm: (Location, List<Location>) -> Unit,
    viewModel: GameRoomViewModel = hiltViewModel()
) {
    val originInfo by viewModel.roomInfo.collectAsStateWithLifecycle()
    val originPoints = originInfo.polygon ?: emptyList()
    val originPrisonLocation = originInfo.prison ?: Location(lat = 0.0, lng = 0.0)
    val tempPolygonPoints = remember(originPoints) {
        originPoints.map {
            DraggableLatLng(position = LatLng(it.lat, it.lng))
        }.toMutableStateList()
    }
    var prisonLocation by remember(originPrisonLocation) {
        mutableStateOf(LatLng(originPrisonLocation.lat, originPrisonLocation.lng))
    }

    Dialog(
        onDismissRequest = onDismiss,
    ) {
        PixelContainer(
            modifier = Modifier.padding(vertical = 10.dp),
            backgroundColor = DarkSurface,
            borderColor = DialogBorderColor,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    modifier = Modifier
                        .background(Color.Transparent)
                        .padding(vertical = 4.dp),
                    text = "경기 구역 수정",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                )

                Spacer(Modifier.height(2.dp))


                GoogleMaps(
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .height(300.dp),
                    inGameMinimap = false,
                    isPreview = false,
                    polygonPoints = tempPolygonPoints,
                    onPointChange = { index, newPos ->
                        if (index in tempPolygonPoints.indices) {
                            tempPolygonPoints[index] =
                                tempPolygonPoints[index].copy(position = newPos)
                        }
                    },
                    onPointDelete = { index ->
                        viewModel.deletePolygonPoint(tempPolygonPoints, index)
                    },
                    onAddPoint = { newPoint ->
                        viewModel.addPointToList(tempPolygonPoints, newPoint)
                    },
                    prisonLocation = prisonLocation,
                    onPrisonChange = { newLoc ->
                        prisonLocation = newLoc
                    }
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "- 마커를 꾹 눌러 드래그로 경기구역을 수정할 수 있습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                )
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "- 감옥을 드래그하여 이동시킬 수 있습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                )


                Spacer(Modifier.height(2.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    PixelButtonCode(
                        modifier = Modifier.weight(1f),
                        text = "취소",
                        onClick = { onDismiss() },
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
                            onConfirm(
                                Location(prisonLocation.latitude, prisonLocation.longitude),
                                tempPolygonPoints.map {
                                    Location(it.position.latitude, it.position.longitude)
                                }
                            )
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