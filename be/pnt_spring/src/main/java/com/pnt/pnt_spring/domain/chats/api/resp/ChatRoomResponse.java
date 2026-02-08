package com.pnt.pnt_spring.domain.chats.api.resp;

import java.time.OffsetDateTime;

import com.pnt.pnt_spring.domain.chats.entity.ChatRoom;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ChatRoomResponse {

	private Long id;
	private String title;
	private Long ownerId;
	private Integer regionCode;
	private String description;
	private int maxMembers;
	private int currentMembers;
	private boolean isDeleted;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;

	public static ChatRoomResponse from(ChatRoom entity) {
		return ChatRoomResponse.builder()
			.id(entity.getId())
			.title(entity.getTitle())
			.ownerId(entity.getOwnerId())
			.regionCode(entity.getRegionCode())
			.description(entity.getDescription())
			.maxMembers(entity.getMaxMembers())
			.currentMembers(entity.getCurrentMembers())
			.isDeleted(entity.isDeleted())
			.createdAt(entity.getCreatedAt())
			.updatedAt(entity.getUpdatedAt())
			.build();
	}

}
