package com.d104.pnt.data.remote.api

import com.d104.pnt.data.remote.model.request.ChatCreateRequest
import com.d104.pnt.data.remote.model.request.ChatDelegateRequest
import com.d104.pnt.data.remote.model.request.ChatKickRequest
import com.d104.pnt.data.remote.model.response.BaseResponse
import com.d104.pnt.data.remote.model.response.ChatCreateResponse
import com.d104.pnt.data.remote.model.response.ChatRoomMemberResponse
import com.d104.pnt.data.remote.model.response.ChatRoomResponse
import com.d104.pnt.data.remote.model.response.ChatSearchResponse
import com.d104.pnt.data.remote.model.response.JoinChatRoomResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface ChatApiService {

    /**
     * 채팅방 생성 API
     */
    @POST("chats")
    suspend fun createChatRoom(
        @Body request: ChatCreateRequest
    ): Response<BaseResponse<ChatCreateResponse>>

    /**
     * 채팅방 단건 조회 API
     */
    @GET("chats/{id}")
    suspend fun getChatRoom(
        @Path("id") chatRoomId: Long
    ): Response<BaseResponse<ChatRoomResponse>>

    /**
     * 채팅방 참여 API
     */
    @POST("chats/{id}/join")
    suspend fun joinChatRoom(
        @Path("id") chatRoomId: Long
    ): Response<BaseResponse<JoinChatRoomResponse>>

    /**
     * 채팅방 연결 API
     */
    @POST("chats/{id}/connect")
    suspend fun connectChatRoom(
        @Path("id") chatRoomId: Long
    ): Response<BaseResponse<Unit>>

    /**
     * 채팅방 검색 API
     */
    @GET("chats")
    suspend fun getFilteredChatRoom(
        @Query("title") title: String?,
        @Query("regionCode") regionCode: Int?,
    ): Response<BaseResponse<ChatSearchResponse>>

    /**
     * 내가 속한 채팅방 조회 API
     */
    @GET("chats/me/rooms")
    suspend fun getJoinedChatRoom(): Response<BaseResponse<ChatSearchResponse>>

    @GET("chats/{chatRoomId}/members")
    suspend fun getChatRoomMembers(
        @Path("chatRoomId") chatRoomId: Long
    ): Response<BaseResponse<List<ChatRoomMemberResponse>>>

    /**
     * 채팅방 나가기 API
     */
    @POST("chats/{id}/leave")
    suspend fun leaveChatRoom(
        @Path("id") chatRoomId: Long
    ): Response<BaseResponse<Unit>>

    /**
     * 채팅방 연결 해제 API
     */
    @POST("chats/{id}/disconnect")
    suspend fun disconnectChatRoom(
        @Path("id") chatRoomId: Long
    ): Response<BaseResponse<Unit>>

    /**
     * 채팅방 방장 위임 API
     */
    @POST("chats/{chatRoomId}/owner")
    suspend fun delegateChatRoomOwner(
        @Path("chatRoomId") chatRoomId: Long,
        @Body request: ChatDelegateRequest
    ): Response<BaseResponse<Unit>>

    /**
     * 채팅방 강퇴 API
     */
    @POST("chats/{chatRoomId}/kick")
    suspend fun kickChatRoomMember(
        @Path("chatRoomId") chatRoomId: Long,
        @Body request: ChatKickRequest
    ): Response<BaseResponse<Unit>>
}
