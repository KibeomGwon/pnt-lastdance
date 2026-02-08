package com.pnt.pnt_spring.domain.games.game.api.resp;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GameRoomStartableResponse {

	private boolean canStart;

	private long joinedCount;     // 현재 참가 인원
	private long requiredCount;   // 설정상 필요한 인원
	private long notReadyCount;   // (방장 제외) 아직 준비 안 된 인원
}
