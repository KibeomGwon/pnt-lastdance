package com.pnt.pnt_spring.domain.games.game.enums;

public enum Position {
	POLICE,
	POLICE_CHIEF,
	THIEF;

	public boolean isPolice() {
		return this == POLICE || this == POLICE_CHIEF;
	}

	public Position toStatPosition() {
		return this == POLICE_CHIEF ? POLICE : this;
	}
}