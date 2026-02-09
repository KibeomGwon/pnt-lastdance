package com.d104.pnt.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.d104.pnt.ui.theme.PixelFont

@Composable
fun TierGuideDialog(
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(500.dp)
                .background(Color(0xFF232323), RoundedCornerShape(16.dp))
                .border(2.dp, Color(0xFF8D90B3), RoundedCornerShape(16.dp))
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "계급이란?",
                    fontFamily = PixelFont,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "경기를 승리하면 승급하고,\n패배하면 강등됩니다.",
                    fontFamily = PixelFont,
                    color = Color(0xFFC4C4C4),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "경찰",
                        modifier = Modifier.weight(1f),
                        fontFamily = PixelFont,
                        color = Color(0xFF6591E9),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "도둑",
                        modifier = Modifier.weight(1f),
                        fontFamily = PixelFont,
                        color = Color(0xFFFF6B6B),
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                HorizontalDivider(color = Color.Gray, thickness = 1.dp)

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                ) {
                    val policeRanks = listOf(
                        "순경", "경장", "경사", "경위", "경감", "경정",
                        "총경", "경무관", "치안감", "치안정감", "치안총감"
                    )
                    val thiefRanks = listOf(
                        "바늘도둑", "좀도둑", "소매치기", "빈집털이", "소도둑", "금고털이",
                        "은행털이", "홍길동", "인비저블", "괴도", "대도"
                    )

                    for (i in 0 until 11) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = policeRanks.getOrElse(i) { "" },
                                modifier = Modifier.weight(1f),
                                fontFamily = PixelFont,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )

                            Text(
                                text = "Lv.${i + 1}",
                                fontFamily = PixelFont,
                                color = Color.Gray,
                                fontSize = 10.sp
                            )

                            Text(
                                text = thiefRanks.getOrElse(i) { "" },
                                modifier = Modifier.weight(1f),
                                fontFamily = PixelFont,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                fontSize = 14.sp
                            )
                        }
                        if (i < 10) {
                            HorizontalDivider(color = Color(0xFF3F3F3F), thickness = 1.dp)
                        }
                    }
                }
            }

            IconButton(
                onClick = onDismissRequest,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 12.dp, y = (-12).dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "닫기",
                    tint = Color.White
                )
            }
        }
    }
}