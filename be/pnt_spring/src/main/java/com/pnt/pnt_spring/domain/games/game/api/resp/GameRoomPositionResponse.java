package com.pnt.pnt_spring.domain.games.game.api.resp;

import com.pnt.pnt_spring.domain.games.game.enums.Position;
import com.pnt.pnt_spring.domain.games.game.enums.PreferPosition;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GameRoomPositionResponse {
	private Long roomId;
	private Long memberId;

	private PreferPosition preferPosition; // 내가 선택한 픽
	private Position givenPosition;        // 아직 배정 전이면 null

	private Boolean ready;
}
