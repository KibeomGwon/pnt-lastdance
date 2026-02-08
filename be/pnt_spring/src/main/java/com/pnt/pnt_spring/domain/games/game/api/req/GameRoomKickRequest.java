package com.pnt.pnt_spring.domain.games.game.api.req;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class GameRoomKickRequest {

	@NotNull
	private Long targetMemberId;

	private String reason; // optional
}
