package com.d104.pnt.ui.chatroomlist

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.d104.pnt.ui.component.PixelButtonCode
import com.d104.pnt.ui.component.PixelContainer
import com.d104.pnt.ui.theme.BorderDefault
import com.d104.pnt.ui.theme.CustomBlue
import com.d104.pnt.ui.theme.RoomBorder
import com.d104.pnt.ui.theme.RoomContainer
import com.d104.pnt.ui.theme.TextPrimary


@Composable
fun RegionSelectionDialog(
    majors: List<String>,
    middles: List<String>,
    selectedMajor: String,
    selectedMiddle: String,
    onMajorSelected: (String) -> Unit,
    onMiddleSelected: (String) -> Unit,
    onSearch: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        PixelContainer(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .padding(16.dp),
            backgroundColor = RoomContainer,
            borderColor = RoomBorder
        ) {
            Column(modifier = Modifier.padding(bottom = 16.dp)) {
                Text(
                    text = "지역 선택",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    modifier = Modifier
                        .padding(vertical = 16.dp)
                        .fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(Modifier.height(10.dp))
                Row(modifier = Modifier.weight(1f)) {
                    // 왼쪽: 대분류
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                    ) {
                        items(majors) { major ->
                            val isSelected = major == selectedMajor
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onMajorSelected(major) }
                                    .background(if (isSelected) CustomBlue else Color.Transparent)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = major,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // 오른쪽: 중분류
                    LazyColumn(
                        modifier = Modifier.weight(1f)
                    ) {
                        items(middles) { middle ->
                            val isSelected = middle == selectedMiddle
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onMiddleSelected(middle) }
                                    .background(if (isSelected) CustomBlue else Color.Transparent)
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = middle,
                                    color = Color.White,
                                    style = MaterialTheme.typography.bodyMedium,
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
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

                    Spacer(modifier = Modifier.width(12.dp))

                    PixelButtonCode(
                        modifier = Modifier.weight(1f),
                        text = "확인",
                        onClick = {
                            onDismiss()
                            onSearch()
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