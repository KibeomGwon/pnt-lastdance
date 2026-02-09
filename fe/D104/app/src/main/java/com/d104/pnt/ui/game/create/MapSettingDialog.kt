package com.d104.pnt.ui.game.create

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
import com.google.maps.android.PolyUtil
import com.google.maps.android.SphericalUtil

@Composable
fun MapSettingDialog(
    modifier: Modifier,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    viewModel: MapSettingViewModel = hiltViewModel()
) {
    val userLocation by viewModel.userLocation.collectAsStateWithLifecycle()
    val originPoints by viewModel.polygonPoints.collectAsStateWithLifecycle()
    val originPrisonLocation by viewModel.prisonLocation.collectAsStateWithLifecycle()
    val tempPolygonPoints = remember(originPoints) {
        originPoints.map {
            DraggableLatLng(position = it)
        }.toMutableStateList()
    }
    var prisonLocation by remember(originPrisonLocation, userLocation) {
        mutableStateOf(
            originPrisonLocation ?: userLocation?.let { LatLng(it.latitude, it.longitude) }
        )
    }
    var isNotValid by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf(null as String?) }

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

                userLocation?.let { loc ->
                    GoogleMaps(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .height(300.dp),
                        currentLocation = LatLng(
                            userLocation!!.latitude,
                            userLocation!!.longitude
                        ),
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
                }

                if (isNotValid) {
                    Text(
                        text = errorMessage ?: "",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Red,
                    )
                }

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "- 지도를 클릭하여 마커를 추가하거나 제거할 수 있습니다.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                )

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = "- 마커와 감옥을 꾹 눌러 드래그할 수 있습니다.",
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
                            errorMessage =
                                isValid(prisonLocation, tempPolygonPoints.map { it.position })
                            if (errorMessage == null) {
                                viewModel.setPolygonPoints(tempPolygonPoints.map { it.position })
                                viewModel.setPrisonLocation(prisonLocation)
                                onConfirm()
                            } else {
                                isNotValid = true
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

private fun isValid(
    prison: LatLng?,
    polygon: List<LatLng>
): String? {
    if (!PolyUtil.containsLocation(prison, polygon, false)) return "감옥은 반드시 영역 안에 있어야 합니다."
    if (SphericalUtil.computeArea(polygon) < 5000f) return "설정한 영역이 너무 작습니다"
    return null
}