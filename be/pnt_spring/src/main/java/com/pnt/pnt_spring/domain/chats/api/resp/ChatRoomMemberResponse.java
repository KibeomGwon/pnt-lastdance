package com.pnt.pnt_spring.domain.chats.api.resp;

import java.time.OffsetDateTime;

import com.pnt.pnt_spring.domain.chats.entity.MemberChatRoom;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomMemberResponse {

	private Long id;
	private Long memberId;
	private Long chatRoomId;
	private boolean isConnected;
	private boolean isDeleted;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;

	public static ChatRoomMemberResponse from(MemberChatRoom entity) {
		return ChatRoomMemberResponse.builder()
			.id(entity.getId())
			.memberId(entity.getMemberId())
			.chatRoomId(entity.getChatRoomId())
			.isConnected(entity.isConnected())
			.isDeleted(entity.isDeleted())
			.createdAt(entity.getCreatedAt())
			.updatedAt(entity.getUpdatedAt())
			.build();
	}

}
