package com.pnt.pnt_spring.domain.games.game.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GameMemberStatus {

	PRISON("STATUS_PRISON", "수감 중"),
	TRANSFER("STATUS_TRANSFER", "이송 중"),
	FREE("STATUS_FREE", "탈옥");

	private final String key;
	private final String value;

}
