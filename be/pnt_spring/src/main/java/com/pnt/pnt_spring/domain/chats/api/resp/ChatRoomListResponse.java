package com.pnt.pnt_spring.domain.chats.api.resp;

import java.util.List;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomListResponse {

	private List<ChatRoomResponse> chats;

	private long totalCount;

	public static ChatRoomListResponse from(List<ChatRoomResponse> chats) {
		return ChatRoomListResponse.builder()
			.chats(chats)
			.totalCount(chats.size())
			.build();
	}
}
