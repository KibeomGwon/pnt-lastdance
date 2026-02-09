package com.d104.pnt.ui.game.wait

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.d104.pnt.R
import com.d104.pnt.ui.component.PixelContainer
import com.d104.pnt.ui.theme.AccentYellow
import com.d104.pnt.ui.theme.PixelFont

@Composable
fun GameRoomHeader(
    roomCode: String,
    currentCount: Int,
    maxCount: Int,
    timeLeft: String,
    isHost: Boolean,
    onSettingsClick: () -> Unit,
    onLeaveClick: () -> Unit
) {
    PixelContainer(
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color(0xFF2D3242),
        borderColor = Color(0xFF6591E9),
        borderWidth = 4f,
        cornerSize = 20f,
        innerHorizontalPadding = 4
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp, horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // 뒤로가기 및 방 코드
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "방 나가기",
                    tint = Color.White,
                    modifier = Modifier
                        .size(16.dp)
                        .clickable { onLeaveClick() }
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = roomCode,
                    fontFamily = FontFamily.Default,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = AccentYellow,
                    letterSpacing = 1.sp
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // 인원, 시간, 설정
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                InfoIconText(icon = Icons.Default.Groups, text = "$currentCount/$maxCount")
                InfoIconText(icon = Icons.Default.AccessTime, text = timeLeft)

                if (isHost) {
                    Icon(
                        imageVector = Icons.Default.Settings,
                        contentDescription = "설정",
                        tint = Color(0xFF6591E9),
                        modifier = Modifier
                            .size(20.dp)
                            .clickable { onSettingsClick() }
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoIconText(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = text, fontFamily = PixelFont, color = Color.White, fontSize = 16.sp)
    }
}