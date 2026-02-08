package com.pnt.pnt_spring.domain.chats.api.resp;

import java.time.OffsetDateTime;
import java.util.List;

import com.pnt.pnt_spring.domain.chats.entity.ChatRoom;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MyChatRoomListResponse {

	private final List<MyChatRoomInfo> rooms;

	@Getter
	@Builder
	public static class MyChatRoomInfo {
		private Long chatRoomId;
		private String title;
		private String description;
		private Integer currentMembers;
		private Integer maxMembers;
		private Integer regionCode;
		private OffsetDateTime updatedAt;

		public static MyChatRoomInfo from(ChatRoom room) {
			return MyChatRoomInfo.builder()
				.chatRoomId(room.getId())
				.title(room.getTitle())
				.description(room.getDescription())
				.currentMembers(room.getCurrentMembers())
				.maxMembers(room.getMaxMembers())
				.regionCode(room.getRegionCode())
				.updatedAt(room.getUpdatedAt())
				.build();
		}
	}

	public static MyChatRoomListResponse from(List<ChatRoom> rooms) {
		return new MyChatRoomListResponse(
			rooms.stream().map(MyChatRoomInfo::from).toList()
		);
	}
}
