package com.pnt.pnt_spring.domain.chats.api.req;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ChatRoomKickRequest {

	@NotNull
	private Long targetMemberId;

	private String reason;

}
