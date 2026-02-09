package com.d104.pnt.data.repository

import com.d104.pnt.data.remote.model.response.ChatCreateResponse
import com.d104.pnt.data.remote.model.response.ChatRoomMemberResponse
import com.d104.pnt.data.remote.model.response.ChatRoomResponse
import com.d104.pnt.data.remote.model.response.ChatSearchResponse
import com.d104.pnt.data.remote.model.response.JoinChatRoomResponse
import com.d104.pnt.domain.model.common.BaseResult
import kotlinx.coroutines.flow.StateFlow

interface ChatRepository {
    var currentChatRoom: Long?
    var currentChatRoomTitle: String?
    var currentChatRoomDescription: String?
    var currentChatRoomRegionCode: Int?
    var currentChatRoomMaxMember: Int?
    val currentChatRoomMember: StateFlow<Int?>

    /**
     * 채팅방 생성
     */
    suspend fun createChatRoom(
        title: String,
        regionCode: Int,
        description: String,
        maxMembers: Int
    ): BaseResult<ChatCreateResponse>

    /**
     * 채팅방 상세 조회
     */
    suspend fun getChatRoomInfo(
        chatRoomId: Long
    ): BaseResult<ChatRoomResponse>

    suspend fun getChatRoomMembers(
        chatRoomId: Long
    ): BaseResult<List<ChatRoomMemberResponse>>


    /**
     * 채팅방 입장
     */
    suspend fun joinChatRoom(chatRoomId: Long): BaseResult<JoinChatRoomResponse>

    /**
     * 채팅방 연결
     */
    suspend fun connectChatRoom(chatRoomId: Long): BaseResult<Unit>

    /**
     * 채팅방 검색
     */
    suspend fun searchChatRoom(
        title: String?,
        regionCode: Int?
    ): BaseResult<ChatSearchResponse>

    /**
     * 내가 참여중인 채팅방 조회
     */
    suspend fun getJoinedChatRoom(): BaseResult<ChatSearchResponse>

    /**
     * 채팅방 퇴장
     */
    suspend fun leaveChatRoom()

    suspend fun leaveChatRoom(chatRoomId: Long): BaseResult<Unit>
    suspend fun disconnectChatRoom(chatRoomId: Long): BaseResult<Unit>

    suspend fun kickChatRoomMember(
        chatRoomId: Long,
        targetMemberId: Long,
        reason: String
    ): BaseResult<Unit>
}