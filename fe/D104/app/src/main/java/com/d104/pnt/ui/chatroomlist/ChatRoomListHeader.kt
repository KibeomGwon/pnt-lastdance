package com.d104.pnt.ui.chatroomlist

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import com.d104.pnt.ui.component.PixelButtonCode
import com.d104.pnt.ui.theme.BorderDefault
import com.d104.pnt.ui.theme.CustomBlue
import com.d104.pnt.ui.theme.TextPrimary


@Composable
fun ChatRoomListHeader(
    viewMode: ChatRoomListViewModel.ViewMode,
    regionText: String,
    onRegionTabClick: () -> Unit,
    onPinClick: () -> Unit,
    onJoinedRoomClicked: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .padding(horizontal = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 왼쪽 버튼
        PixelButtonCode(
            modifier = Modifier.weight(4f),
            text = "내 채팅방",
            fontSize = 14,
            onClick = onJoinedRoomClicked,
            mainColor = if (viewMode == ChatRoomListViewModel.ViewMode.Me) CustomBlue else TextPrimary,
            pixelSize = 3.dp,
            blockHeight = 14,
            textColor = if (viewMode == ChatRoomListViewModel.ViewMode.Me) TextPrimary else BorderDefault
        )

        Spacer(modifier = Modifier.width(12.dp))

        PixelButtonCode(
            modifier = Modifier.weight(6f),
            text = regionText,
            fontSize = 14,
            onClick = onRegionTabClick,
            mainColor = if (viewMode == ChatRoomListViewModel.ViewMode.Region) CustomBlue else TextPrimary,
            textColor = if (viewMode == ChatRoomListViewModel.ViewMode.Region) TextPrimary else BorderDefault,
            pixelSize = 3.dp,
            blockHeight = 14,
        )

        Spacer(modifier = Modifier.width(12.dp))

        Icon(
            painter = painterResource(R.drawable.ic_loc_pin),
            contentDescription = "검색",
            tint = Color.White,
            modifier = Modifier
                .size(28.dp)
                .clickable(
                    onClick = { onPinClick() }
                )
        )
    }
}
