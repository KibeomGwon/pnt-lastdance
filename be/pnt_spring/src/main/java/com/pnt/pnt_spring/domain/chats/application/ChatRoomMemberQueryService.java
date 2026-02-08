package com.pnt.pnt_spring.domain.chats.application;

import java.util.List;

import com.pnt.pnt_spring.domain.chats.api.resp.ChatRoomMemberInfoResponse;
import com.pnt.pnt_spring.domain.chats.api.resp.ChatRoomResponse;

public interface ChatRoomMemberQueryService {
	List<ChatRoomMemberInfoResponse> listMembers(Long requesterId, Long chatRoomId);

	List<ChatRoomResponse> myJoinedRooms(Long memberId);
}
