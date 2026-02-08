package com.pnt.pnt_spring.domain.games.game.api.resp;

import com.pnt.pnt_spring.domain.games.game.enums.Position;
import com.pnt.pnt_spring.domain.games.game.enums.PreferPosition;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GameRoomJoinResponse {
	private Long roomId;
	private String roomCode;

	private Long memberId;

	private PreferPosition preferPosition; // 기본 ANY
	private Position givenPosition;        // 배정 전 null 가능
	private Boolean ready;                 // join하면 false 권장
}
