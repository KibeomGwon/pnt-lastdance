package com.d104.pnt.ui.chatroom.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.d104.pnt.R
import com.d104.pnt.domain.model.ChatsData
import com.d104.pnt.ui.theme.TextPrimary
import com.d104.pnt.ui.theme.TextSecondary

@Composable
fun ChatRoomHeader(
    modifier: Modifier,
    roomData: ChatsData,
    onLeaveClick: () -> Unit,
    onMenuClick: () -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(10.dp)
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {

            IconButton(
                onClick = { onLeaveClick() }
            ) {
                Icon(
                    modifier = Modifier.size(20.dp),
                    painter = painterResource(R.drawable.ic_back),
                    contentDescription = "나가기",
                    tint = Color.Unspecified,
                )
            }

            Spacer(Modifier.width(20.dp))

            // 제목
            Text(
                modifier = Modifier.weight(1f),
                text = roomData.title,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            // 인원수 + 메뉴
            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${roomData.currentMember}/${roomData.maxMember}",
                    style = MaterialTheme.typography.bodyLarge,
                    color = TextSecondary,
                )

                // 멤버 목록
                Icon(
                    modifier = Modifier
                        .size(24.dp)
                        .clickable(onClick = onMenuClick),
                    painter = painterResource(R.drawable.bars),
                    contentDescription = "멤버 목록",
                    tint = TextPrimary
                )
            }
        }
    }
}
