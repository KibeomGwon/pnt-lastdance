package com.pnt.pnt_spring.domain.games.game.api.req;

import com.pnt.pnt_spring.domain.games.game.enums.PreferPosition;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class GameRoomPositionRequest {

	@NotNull(message = "preferPosition은 필수입니다.")
	private PreferPosition preferPosition; // POLICE / THIEF / ANY
}
