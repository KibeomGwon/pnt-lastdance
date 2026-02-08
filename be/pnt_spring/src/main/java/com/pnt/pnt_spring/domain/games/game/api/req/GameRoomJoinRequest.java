package com.pnt.pnt_spring.domain.games.game.api.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

@Getter
public class GameRoomJoinRequest {

	@NotBlank(message = "roomCode는 필수입니다.")
	private String roomCode;
}
