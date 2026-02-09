package com.d104.pnt.ui.game.end

import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import com.d104.pnt.ui.component.PixelButtonCode
import com.d104.pnt.ui.component.PixelContainer
import com.d104.pnt.ui.theme.AccentRed
import com.d104.pnt.ui.theme.AccentYellow
import com.d104.pnt.ui.theme.DarkSurface
import com.d104.pnt.ui.theme.PixelFont

@Composable
fun ReportDialog(
    initialTargetUser: String = "",
    initialReason: String = "욕설",
    initialDescription: String = "",
    validNicknames: List<String>,
    onDismissRequest: () -> Unit,
    onReport: (String, String, String) -> Unit
) {
    val reasons = listOf("욕설", "폭행", "비매너", "구역 이탈", "기타")

    var targetUser by remember { mutableStateOf(initialTargetUser) }
    var selectedReason by remember { mutableStateOf(if (initialReason.isNotEmpty()) initialReason else reasons[0]) }
    var reportDescription by remember { mutableStateOf(initialDescription) }
    var errorText by remember { mutableStateOf("") }
    val selectedColor = Color(0xFFD35400)

    var isDropdownExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismissRequest) {
        PixelContainer(
            backgroundColor = DarkSurface,
            borderColor = Color.White,
            borderWidth = 3f,
            cornerSize = 8f,
            innerVerticalPadding = 24,
            innerHorizontalPadding = 24,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "유저명",
                        fontFamily = PixelFont,
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(24.dp))

                    Box(modifier = Modifier.width(160.dp)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { isDropdownExpanded = !isDropdownExpanded }
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (targetUser.isEmpty()) "닉네임 선택" else targetUser,
                                    fontFamily = PixelFont,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (targetUser.isEmpty()) Color.Gray else Color.White,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )

                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "유저 선택",
                                    tint = Color.White,
                                    modifier = Modifier.padding(start = 4.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Canvas(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(2.dp)
                            ) {
                                drawLine(
                                    color = Color.White,
                                    start = Offset(0f, 0f),
                                    end = Offset(size.width, 0f),
                                    pathEffect = PathEffect.dashPathEffect(
                                        floatArrayOf(10f, 10f),
                                        0f
                                    ),
                                    strokeWidth = 3f
                                )
                            }
                        }

                        if (isDropdownExpanded) {
                            Popup(
                                alignment = Alignment.TopStart,
                                offset = androidx.compose.ui.unit.IntOffset(0, 100),
                                onDismissRequest = { isDropdownExpanded = false },
                                properties = PopupProperties(focusable = true)
                            ) {
                                PixelContainer(
                                    backgroundColor = Color(0xFF2D3242),
                                    borderColor = Color.Gray,
                                    borderWidth = 2f,
                                    cornerSize = 4f,
                                    modifier = Modifier
                                        .width(160.dp)
                                        .heightIn(max = 200.dp)
                                ) {
                                    LazyColumn(
                                        modifier = Modifier.padding(4.dp)
                                    ) {
                                        items(validNicknames) { name ->
                                            Text(
                                                text = name,
                                                fontFamily = PixelFont,
                                                color = Color.White,
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .clickable {
                                                        targetUser = name
                                                        errorText = ""
                                                        isDropdownExpanded = false
                                                    }
                                                    .padding(vertical = 8.dp, horizontal = 4.dp),
                                                textAlign = TextAlign.Center
                                            )
                                            Spacer(
                                                modifier = Modifier
                                                    .height(1.dp)
                                                    .background(Color.Gray.copy(alpha = 0.3f))
                                                    .fillMaxWidth()
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 신고 사유
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "신고 사유",
                        fontFamily = PixelFont,
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        reasons.forEach { reason ->
                            ReasonChip(
                                text = reason,
                                isSelected = reason == selectedReason,
                                selectedColor = selectedColor,
                                onClick = { selectedReason = reason }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 설명
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "설명",
                        fontFamily = PixelFont,
                        fontSize = 16.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    PixelContainer(
                        backgroundColor = Color.White,
                        borderColor = Color.Gray,
                        borderWidth = 2f,
                        cornerSize = 8f,
                        innerVerticalPadding = 12,
                        innerHorizontalPadding = 12,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp)
                    ) {
                        BasicTextField(
                            value = reportDescription,
                            onValueChange = { reportDescription = it },
                            textStyle = MaterialTheme.typography.bodyMedium.copy(
                                color = Color.Black,
                                fontFamily = PixelFont
                            ),
                            modifier = Modifier.fillMaxSize(),
                            decorationBox = { innerTextField ->
                                if (reportDescription.isEmpty()) {
                                    Text(
                                        text = "상세 내용을 입력하세요.",
                                        color = Color.Gray,
                                        fontFamily = PixelFont,
                                        fontSize = 12.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (errorText.isNotEmpty()) {
                    Text(
                        text = errorText,
                        fontFamily = PixelFont,
                        fontSize = 14.sp,
                        color = AccentRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                } else {
                    Spacer(modifier = Modifier.height(17.dp))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 하단 버튼
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PixelButtonCode(
                        text = "취소",
                        onClick = onDismissRequest,
                        mainColor = Color.White,
                        textColor = Color.Black,
                        blockWidth = 24,
                        blockHeight = 10,
                        fontSize = 14
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    PixelButtonCode(
                        text = "신고",
                        onClick = {
                            if (targetUser.isBlank()) {
                                errorText = "유저명을 입력해주세요."
                            } else if (!validNicknames.contains(targetUser)) {
                                errorText = "게임에 참여하지 않은 유저입니다."
                            } else {
                                errorText = ""
                                onReport(targetUser, selectedReason, reportDescription)
                            }
                        },
                        mainColor = Color.White,
                        textColor = Color.Black,
                        blockWidth = 24,
                        blockHeight = 10,
                        fontSize = 14
                    )
                }
            }
        }
    }
}

@Composable
fun ReasonChip(
    text: String,
    isSelected: Boolean,
    selectedColor: Color,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.clickable { onClick() }) {
        PixelContainer(
            backgroundColor = if (isSelected) selectedColor else Color.Transparent,
            borderColor = Color.White,
            borderWidth = 2f,
            cornerSize = 6f,
            innerVerticalPadding = 6,
            innerHorizontalPadding = 8,
            modifier = Modifier.wrapContentSize()
        ) {
            Text(
                text = text,
                fontFamily = PixelFont,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

// 신고 확인
@Composable
fun ConfirmReportDialog(
    isLoading: Boolean,
    errorText: String?,
    onDismissRequest: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = { if (!isLoading) onDismissRequest() }) {
        PixelContainer(
            backgroundColor = DarkSurface,
            borderColor = Color.White,
            borderWidth = 3f,
            cornerSize = 8f,
            innerVerticalPadding = 30,
            innerHorizontalPadding = 20,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "정말 신고하시겠습니까?",
                    fontFamily = PixelFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = AccentYellow,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "허위신고시 제재대상에 포함될 수 있습니다.",
                    fontFamily = PixelFont,
                    fontSize = 14.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                if (!errorText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = errorText,
                        fontFamily = PixelFont,
                        fontSize = 14.sp,
                        color = AccentRed,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))

                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    PixelButtonCode(
                        text = "취소",
                        onClick = { if (!isLoading) onDismissRequest() },
                        mainColor = Color.White,
                        textColor = Color.Black,
                        blockWidth = 24,
                        blockHeight = 10,
                        fontSize = 14
                    )

                    Spacer(modifier = Modifier.width(20.dp))

                    PixelButtonCode(
                        text = if (isLoading) "전송 중..." else "신고하기",
                        onClick = { if (!isLoading) onConfirm() },
                        mainColor = Color.White,
                        textColor = Color.Black,
                        blockWidth = 24,
                        blockHeight = 10,
                        fontSize = 14
                    )
                }
            }
        }
    }
}

// 신고 완료
@Composable
fun SuccessReportDialog(
    onDismissRequest: () -> Unit
) {
    Dialog(onDismissRequest = onDismissRequest) {
        PixelContainer(
            backgroundColor = DarkSurface,
            borderColor = Color.White,
            borderWidth = 3f,
            cornerSize = 8f,
            innerVerticalPadding = 30,
            innerHorizontalPadding = 20,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "신고가 접수되었습니다.",
                    fontFamily = PixelFont,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(30.dp))

                PixelButtonCode(
                    text = "확인",
                    onClick = onDismissRequest,
                    mainColor = Color.White,
                    textColor = Color.Black,
                    blockWidth = 24,
                    blockHeight = 10,
                    fontSize = 14
                )
            }
        }
    }
}
