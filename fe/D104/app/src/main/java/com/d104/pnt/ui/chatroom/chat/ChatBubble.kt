package com.d104.pnt.ui.chatroom.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.d104.pnt.R
import com.d104.pnt.domain.model.ChatMessage
import com.d104.pnt.ui.component.PixelContainer
import com.d104.pnt.ui.theme.TextSecondary

@Composable
fun ChatBubble(
    modifier: Modifier = Modifier,
    message: ChatMessage,
    isMe: Boolean,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = if (isMe) Arrangement.End else Arrangement.Start
    ) {
        if (!isMe) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(TextSecondary)
            ) {
                AsyncImage(
                    model = if (message.avatarUrl.isNullOrEmpty() || message.avatarUrl == "string") {
                        R.drawable.profile_img_default
                    } else {
                        if (message.avatarUrl.startsWith("http")) message.avatarUrl
                        else "https://i14d104.p.ssafy.io/spring/${message.avatarUrl}"
                    },
                    contentDescription = "프로필 이미지",
                    placeholder = painterResource(R.drawable.profile_img_default),
                    error = painterResource(R.drawable.profile_img_default),
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            modifier = Modifier.widthIn(max = 260.dp),
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start
        ) {
            if (!isMe) {
                Text(
                    text = message.senderNickname,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                )
            }

            // 말풍선
            PixelContainer(
                modifier = Modifier,
                cornerSize = 20f,
                backgroundColor = Color.White.copy(alpha = 0.8f)
            ) {
                Text(
                    text = message.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    modifier = Modifier.padding(4.dp)
                )
            }
        }
    }
}
