package com.pnt.pnt_spring.domain.chats.api.req;

import jakarta.validation.constraints.Min;
import lombok.Getter;

@Getter
public class ChatRoomUpdateRequest {

	private String title;

	private Integer regionCode;

	private String description;

	@Min(1)
	private Integer maxMembers;
}
