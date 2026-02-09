package com.d104.pnt.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import com.d104.pnt.domain.model.ChatRoomData

@Composable
fun RoomList(
    rooms: List<ChatRoomData>,
    onItemClick: (ChatRoomData) -> Unit = {}
) {
    LazyColumn(
        contentPadding = PaddingValues(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(rooms) { room ->
            RoomListItem(
                data = room,
                onJoinClick = { onItemClick(room) }
            )
        }
    }
}