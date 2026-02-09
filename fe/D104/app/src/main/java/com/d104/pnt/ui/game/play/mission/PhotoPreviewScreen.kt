package com.d104.pnt.ui.game.play.mission

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.d104.pnt.ui.theme.CheckGreen
import com.d104.pnt.ui.theme.ThiefRed
import java.io.File

@Composable
fun PhotoPreviewScreen(
    photoFile: File,
    onConfirm: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 촬영한 사진 표시
        Image(
            painter = rememberAsyncImagePainter(photoFile),
            contentDescription = "촬영한 사진",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
        )

        // 하단 버튼
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp, start = 32.dp, end = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            // 취소 버튼
            FloatingActionButton(
                onClick = onCancel,
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                containerColor = ThiefRed
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "취소",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }

            // 확인 버튼
            FloatingActionButton(
                onClick = onConfirm,
                modifier = Modifier.size(64.dp),
                shape = CircleShape,
                containerColor = CheckGreen
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "확인",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}
