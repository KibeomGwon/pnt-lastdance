package com.d104.pnt.ui.chatroom.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import com.d104.pnt.domain.model.ChatMessage
import kotlinx.coroutines.flow.distinctUntilChanged

@Composable
fun Chats(
    modifier: Modifier = Modifier,
    chatMessages: List<ChatMessage>,
    myMemberId: Long,
    isLoading: Boolean = false,
    onLoadMore: () -> Unit
) {
    val listState = rememberLazyListState()
    val density = LocalDensity.current

    var previousMessageCount by remember { mutableIntStateOf(0) }
    var shouldMaintainScrollPosition by remember { mutableStateOf(false) }
    var hasLoadedInitial by remember { mutableStateOf(false) }

    // 키보드 높이 감지
    val imeInsets = WindowInsets.ime
    val imeHeight = with(density) { imeInsets.getBottom(density).toDp() }

    LaunchedEffect(chatMessages.size) {
        if (chatMessages.isEmpty()) return@LaunchedEffect

        if (!hasLoadedInitial) {
            listState.scrollToItem(chatMessages.size - 1)
            previousMessageCount = chatMessages.size
            hasLoadedInitial = true
        } else if (chatMessages.size > previousMessageCount) {
            val newMessagesCount = chatMessages.size - previousMessageCount

            val isLoadingOldMessages = shouldMaintainScrollPosition

            if (isLoadingOldMessages) {
                listState.scrollToItem(newMessagesCount)
                shouldMaintainScrollPosition = false
            } else {
                listState.animateScrollToItem(chatMessages.size - 1)
            }

            previousMessageCount = chatMessages.size
        }
    }

    LaunchedEffect(imeHeight) {
        if (imeHeight > 0.dp && chatMessages.isNotEmpty()) {
            listState.scrollToItem(chatMessages.size - 1)
        }
    }

    LaunchedEffect(chatMessages.size) {
        snapshotFlow {
            val firstIndex = listState.layoutInfo.visibleItemsInfo.firstOrNull()?.index ?: -1
            val messageCount = chatMessages.size

            Triple(firstIndex, messageCount, isLoading)
        }
            .distinctUntilChanged()
            .collect { (firstVisibleIndex, messageCount, loading) ->
                val hasMessages = messageCount > 0

                val messageIndex = if (loading && firstVisibleIndex > 0) {
                    firstVisibleIndex - 1
                } else {
                    firstVisibleIndex
                }

                if (messageIndex in 0..3 && hasMessages && !loading) {
                    shouldMaintainScrollPosition = true
                    onLoadMore()
                }
            }
    }

    Box(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Transparent),
            state = listState,
            contentPadding = PaddingValues(
                horizontal = 16.dp,
                vertical = 8.dp
            ),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // 로딩 인디케이터
            if (isLoading && chatMessages.isNotEmpty()) {
                item(key = "loading_indicator") {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = com.d104.pnt.ui.theme.AccentYellow
                        )
                    }
                }
            }

            items(
                items = chatMessages,
                key = { it.id }
            ) { chatMessage ->
                ChatBubble(
                    message = chatMessage,
                    isMe = chatMessage.memberId == myMemberId
                )
            }
        }
    }
}