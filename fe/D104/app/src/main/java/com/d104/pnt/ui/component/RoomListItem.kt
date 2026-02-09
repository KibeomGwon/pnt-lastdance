package com.d104.pnt.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.d104.pnt.domain.model.ChatRoomData
import com.d104.pnt.ui.theme.BorderDefault
import com.d104.pnt.ui.theme.TextPrimary
import com.d104.pnt.ui.theme.TextSecondary

@Composable
fun RoomListItem(
    data: ChatRoomData,
    onJoinClick: (ChatRoomData) -> Unit = {}
) {
    var isExpanded by remember { mutableStateOf(false) }

    PixelContainer(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        backgroundColor = Color.White.copy(0.85f),
        borderColor = BorderDefault
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Row(
                        modifier = Modifier,
                        horizontalArrangement = Arrangement.spacedBy(5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 제목
                        Text(
                            modifier = Modifier.weight(1f),
                            text = data.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = BorderDefault,
                            maxLines = if (isExpanded) 2 else 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "${data.currentMember}/${data.maxMember}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = data.description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = BorderDefault,
                        maxLines = if (isExpanded) 5 else 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }


                if (!isExpanded) {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowDown,
                        contentDescription = "더보기",
                        tint = BorderDefault,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.KeyboardArrowUp,
                        contentDescription = "더보기",
                        tint = BorderDefault,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            if (isExpanded) {
                Spacer(
                    modifier = Modifier
                        .height(12.dp)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Spacer(modifier = Modifier.weight(0.7f))
                    PixelButtonCode(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(0.3f),
                        text = "입장",
                        fontSize = 16,
                        onClick = { onJoinClick(data) },
                        mainColor = TextPrimary,
                        pixelSize = 3.dp,
                        blockHeight = 12,
                        textColor = BorderDefault
                    )
                }
            }
        }
    }
}