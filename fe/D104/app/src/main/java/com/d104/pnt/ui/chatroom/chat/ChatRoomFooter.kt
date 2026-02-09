package com.d104.pnt.ui.chatroom.chat

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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.d104.pnt.R
import com.d104.pnt.ui.component.PixelContainer
import com.d104.pnt.ui.component.PixelInputField
import com.d104.pnt.ui.theme.DarkBackground

@Composable
fun ChatRoomFooter(
    modifier: Modifier = Modifier,
    message: String,
    onSendMessage: (String) -> Unit,
    onValueChange: (String) -> Unit
) {
    val isMessageEmpty = message.isBlank()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 4.dp)
            .background(Color.Transparent),
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PixelInputField(
                modifier = Modifier
                    .weight(1f),
                placeholder = "메시지를 입력하세요...",
                backgroundColor = Color.White,
                borderColor = Color.White,
                value = message,
                onValueChange = onValueChange
            )
            Spacer(Modifier.width(16.dp))
            PixelContainer(
                modifier = Modifier
                    .size(44.dp)
                    .clickable(enabled = !isMessageEmpty) {
                        onSendMessage(message)
                    },
                backgroundColor = Color.White,
                borderColor = Color.White,
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.fillMaxSize()
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_send),
                        contentDescription = "전송",
                        modifier = Modifier.size(32.dp),
                        tint = if (isMessageEmpty) Color.Gray else DarkBackground
                    )
                }
            }
        }

    }
}