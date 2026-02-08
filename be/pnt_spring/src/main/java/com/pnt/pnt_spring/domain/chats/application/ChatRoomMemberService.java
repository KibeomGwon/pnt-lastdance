package com.pnt.pnt_spring.domain.chats.application;

import com.pnt.pnt_spring.domain.chats.api.resp.ChatRoomMemberResponse;

public interface ChatRoomMemberService {

	ChatRoomMemberResponse join(Long memberId, Long chatRoomId);

	void leave(Long memberId, Long chatRoomId);

	void connect(Long memberId, Long chatRoomId);

	void disconnect(Long memberId, Long chatRoomId);

}