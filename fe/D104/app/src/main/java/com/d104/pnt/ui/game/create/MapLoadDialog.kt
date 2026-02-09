package com.d104.pnt.ui.game.create

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.d104.pnt.data.remote.model.response.MapData
import com.d104.pnt.domain.model.common.UiState
import com.d104.pnt.ui.component.PixelButtonCode
import com.d104.pnt.ui.component.PixelContainer
import com.d104.pnt.ui.theme.BorderDefault
import com.d104.pnt.ui.theme.CustomBlue
import com.d104.pnt.ui.theme.DarkSurface
import com.d104.pnt.ui.theme.DialogBorderColor

@Composable
fun MapLoadDialog(
    uiState: UiState<List<MapData>>,
    selectedMapId: Long?,
    onDismiss: () -> Unit,
    onMapSelect: (MapData) -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        PixelContainer(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            backgroundColor = DarkSurface,
            borderColor = DialogBorderColor,
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "내 맵 불러오기",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.height(300.dp)) {
                    when (uiState) {
                        is UiState.Loading -> {
                            Text(
                                "불러오는 중...",
                                color = Color.White,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        is UiState.Success -> {
                            val maps = uiState.data
                            if (maps.isEmpty()) {
                                Text(
                                    "저장된 맵이 없습니다.",
                                    color = Color.Gray,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            } else {
                                LazyColumn(
                                    verticalArrangement = Arrangement.spacedBy(14.dp)
                                ) {
                                    items(maps.size) { index ->
                                        val map = maps[index]
                                        MapItem(
                                            map = map,
                                            isSelected = map.mapId == selectedMapId,
                                            onClick = { onMapSelect(map) }
                                        )
                                    }
                                }
                            }
                        }

                        is UiState.Error -> {
                            Text(
                                "에러: ${uiState.message}",
                                color = Color.Red,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }

                        else -> {}
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    PixelButtonCode(
                        text = "취소",
                        onClick = onDismiss,
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
                            if (selectedMapId != null) onConfirm()
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

@Composable
fun MapItem(
    map: MapData,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    PixelContainer(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = (if (isSelected) CustomBlue else Color(0xFF3A3A3A)),
        borderColor = Color.White.copy(alpha = 0.8f),
        cornerSize = 10f,
        borderWidth = 6f,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 10.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = map.mapName ?: "이름 없는 맵",
                        color = Color.White,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    if (!map.description.isNullOrEmpty()) {
                        Text(
                            text = map.description,
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }
}