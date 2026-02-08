package com.pnt.pnt_spring.domain.games.game.api.resp;

import com.pnt.pnt_spring.domain.games.game.enums.GameStatus;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GameRoomCreateResponse {
	private Long roomId;      // gameId
	private String roomCode;
	private GameStatus status;
}
