package com.pnt.pnt_spring.domain.games.game.api.resp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GameRoomReadyResponse {
	private Long roomId;
	private Long memberId;
	private boolean isReady;
}
