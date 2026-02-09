package com.d104.pnt.data.repository

import com.d104.pnt.data.remote.api.ChatApiService
import com.d104.pnt.data.remote.model.request.ChatCreateRequest
import com.d104.pnt.data.remote.model.request.ChatKickRequest
import com.d104.pnt.data.remote.model.response.ChatCreateResponse
import com.d104.pnt.data.remote.model.response.ChatRoomMemberResponse
import com.d104.pnt.data.remote.model.response.ChatRoomResponse
import com.d104.pnt.data.remote.model.response.ChatSearchResponse
import com.d104.pnt.data.remote.model.response.JoinChatRoomResponse
import com.d104.pnt.domain.model.common.BaseResult
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val chatApiService: ChatApiService
) : ChatRepository, BaseRepository() {

    private val _currentChatRoomMember = MutableStateFlow<Int?>(null)

    override var currentChatRoom: Long? = null
    override var currentChatRoomTitle: String? = null
    override var currentChatRoomDescription: String? = null
    override var currentChatRoomRegionCode: Int? = null
    override var currentChatRoomMaxMember: Int? = null
    override val currentChatRoomMember = _currentChatRoomMember.asStateFlow()

    override suspend fun getChatRoomInfo(chatRoomId: Long): BaseResult<ChatRoomResponse> {
        return safeApiCall(
            onSuccess = { chatRoomResponse ->
                currentChatRoom = chatRoomResponse.chatRoomId
                currentChatRoomTitle = chatRoomResponse.title
                currentChatRoomDescription = chatRoomResponse.description
                currentChatRoomRegionCode = chatRoomResponse.regionCode
                currentChatRoomMaxMember = chatRoomResponse.maxMembers
                _currentChatRoomMember.value = chatRoomResponse.currentMembers
            }
        ) {
            chatApiService.getChatRoom(chatRoomId)
        }
    }

    override suspend fun getChatRoomMembers(
        chatRoomId: Long
    ): BaseResult<List<ChatRoomMemberResponse>> {
        return safeApiCall {
            chatApiService.getChatRoomMembers(chatRoomId)
        }
    }

    override suspend fun createChatRoom(
        title: String,
        regionCode: Int,
        description: String,
        maxMembers: Int
    ): BaseResult<ChatCreateResponse> {
        return safeApiCall(
            onSuccess = { chatCreateResponse ->
                joinChatRoom(
                    chatRoomId = chatCreateResponse.chatRoomId
                )
            }
        ) {
            chatApiService.createChatRoom(
                ChatCreateRequest(
                    title,
                    description,
                    regionCode,
                    maxMembers
                )
            )
        }
    }

    override suspend fun joinChatRoom(chatRoomId: Long): BaseResult<JoinChatRoomResponse> {
        return safeApiCall(
            onSuccess = { joinResponse ->
                currentChatRoom = joinResponse.chatRoomId
                _currentChatRoomMember.value = null
            }
        ) {
            chatApiService.joinChatRoom(chatRoomId)
        }
    }

    override suspend fun searchChatRoom(
        title: String?,
        regionCode: Int?
    ): BaseResult<ChatSearchResponse> {
        return safeApiCall {
            chatApiService.getFilteredChatRoom(title, regionCode)
        }
    }

    override suspend fun getJoinedChatRoom(): BaseResult<ChatSearchResponse> {
        return safeApiCall {
            chatApiService.getJoinedChatRoom()
        }
    }

    override suspend fun connectChatRoom(chatRoomId: Long): BaseResult<Unit> {
        return safeApiCall {
            chatApiService.connectChatRoom(chatRoomId)
        }
    }

    override suspend fun leaveChatRoom() {
        _currentChatRoomMember.value = null
        currentChatRoom = null
        currentChatRoomTitle = null
        currentChatRoomDescription = null
        currentChatRoomRegionCode = null
        currentChatRoomMaxMember = null
    }

    override suspend fun leaveChatRoom(chatRoomId: Long): BaseResult<Unit> {
        return safeApiCall(
            onSuccess = {
                leaveChatRoom()
            }
        ) {
            chatApiService.leaveChatRoom(chatRoomId)
        }
    }

    override suspend fun disconnectChatRoom(chatRoomId: Long): BaseResult<Unit> {
        return safeApiCall {
            chatApiService.disconnectChatRoom(chatRoomId)
        }
    }

    override suspend fun kickChatRoomMember(
        chatRoomId: Long,
        targetMemberId: Long,
        reason: String
    ): BaseResult<Unit> {
        return safeApiCall {
            chatApiService.kickChatRoomMember(
                chatRoomId,
                ChatKickRequest(targetMemberId, reason)
            )
        }
    }
}