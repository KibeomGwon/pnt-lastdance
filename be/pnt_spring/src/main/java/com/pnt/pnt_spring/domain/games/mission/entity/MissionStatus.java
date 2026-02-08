package com.pnt.pnt_spring.domain.games.mission.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MissionStatus {

	IN_PROGRESS("IN_PROGRESS", "진행중"),
	SUCCESS("SUCCESS", "성공");

	private final String key;
	private final String title;

}
