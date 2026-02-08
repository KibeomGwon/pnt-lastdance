package com.pnt.pnt_spring.domain.games.game.api.req;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class GameRoomReadyRequest {

	@NotNull
	private Boolean ready;
}
