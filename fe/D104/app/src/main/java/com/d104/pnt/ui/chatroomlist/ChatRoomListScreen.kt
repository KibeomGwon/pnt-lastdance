package com.d104.pnt.ui.chatroomlist

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.d104.pnt.R
import com.d104.pnt.domain.model.common.UiState
import com.d104.pnt.ui.component.PixelContainer
import com.d104.pnt.ui.component.PixelInputField
import com.d104.pnt.ui.component.RoomList
import com.d104.pnt.ui.theme.BorderDefault
import com.d104.pnt.ui.theme.ButtonHighlight
import com.d104.pnt.ui.theme.ButtonPrimary

@Composable
fun ChatRoomListScreen(
    navigateToChatCreate: () -> Unit,
    navigateToChatRoom: (Long) -> Unit,
    viewModel: ChatRoomListViewModel = hiltViewModel()
) {
    val majors = viewModel.majorList
    val middles by viewModel.middleList.collectAsStateWithLifecycle()

    val selectedMajor by viewModel.selectedMajor.collectAsStateWithLifecycle()
    val selectedMiddle by viewModel.selectedMiddle.collectAsStateWithLifecycle()

    val uiState by viewModel.listState.collectAsStateWithLifecycle()
    val viewMode by viewModel.viewMode.collectAsStateWithLifecycle()
    val searchText by viewModel.searchQuery.collectAsStateWithLifecycle()

    var showRegionDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.getJoinedChatRoom()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.img_background2),
            contentDescription = "배경 화면",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Scaffold(
            containerColor = Color.Transparent,
            floatingActionButton = {
                Box(
                    modifier = Modifier.padding(end = 16.dp, bottom = 72.dp)
                ) {
                    PixelContainer(
                        modifier = Modifier
                            .size(50.dp)
                            .clickable(onClick = { navigateToChatCreate() })
                            .navigationBarsPadding(),
                        backgroundColor = ButtonPrimary,
                        borderColor = ButtonHighlight
                    ) {

                        Icon(
                            painterResource(R.drawable.ic_add),
                            contentDescription = "채팅방 만들기",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }
                }
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Transparent)
                    .padding(paddingValues)
                    .padding(16.dp)
                    .statusBarsPadding()
            ) {
                ChatRoomListHeader(
                    viewMode = viewMode,
                    regionText = if (selectedMajor.isEmpty()) "지역 선택" else "$selectedMajor $selectedMiddle",
                    onJoinedRoomClicked = {
                        viewModel.getJoinedChatRoom()
                    },
                    onRegionTabClick = {
                        if (selectedMajor.isEmpty()) {
                            showRegionDialog = true
                        } else {
                            viewModel.switchToRegionMode()
                        }
                    },
                    onPinClick = {
                        showRegionDialog = true
                    }
                )

                Spacer(modifier = Modifier.height(5.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PixelInputField(
                        modifier = Modifier
                            .weight(1f),
                        value = searchText,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = "검색어를 입력해주세요",
                        imeAction = ImeAction.Search,
                        onAction = {
                            if (searchText.isNotBlank()) {
                                viewModel.searchByTitle(searchText)
                            }
                        },
                        borderColor = BorderDefault,
                        backgroundColor = Color.White.copy(alpha = 0.8f),
                    )
                }

                when (uiState) {
                    is UiState.Idle -> {}
                    is UiState.Loading -> {}
                    is UiState.Success -> {
                        val data = (uiState as UiState.Success).data
                        if (data.chats.isEmpty()) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = "검색 결과가 없습니다.",
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }
                        } else {
                            Box(modifier = Modifier.weight(1f)) {
                                RoomList(
                                    rooms = data.chats,
                                    onItemClick = { room ->
                                        viewModel.joinChatRoomFromList(
                                            chatRoomId = room.id,
                                            onSuccess = {
                                                navigateToChatRoom(room.id)
                                            }
                                        )
                                    }
                                )
                            }
                        }
                    }

                    is UiState.Error -> {}
                }

                Spacer(Modifier.height(60.dp))
            }
        }
    }

    // 다이얼로그 표시 로직
    if (showRegionDialog) {
        RegionSelectionDialog(
            majors = majors,
            middles = middles,
            selectedMajor = selectedMajor,
            selectedMiddle = selectedMiddle,
            onMajorSelected = { viewModel.selectMajor(it) },
            onMiddleSelected = {
                viewModel.selectMiddle(it)
            },
            onSearch = {
                val code =
                    if (viewModel.searchRegionQuery.value != -1) viewModel.searchRegionQuery.value else null
                viewModel.searchChatRoom(searchText, code)
            },
            onDismiss = { showRegionDialog = false }
        )
    }
}
