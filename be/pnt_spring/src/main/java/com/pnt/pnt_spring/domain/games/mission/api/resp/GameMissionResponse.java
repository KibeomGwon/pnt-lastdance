package com.pnt.pnt_spring.domain.games.mission.api.resp;

import com.pnt.pnt_spring.domain.games.mission.entity.MissionStatus;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GameMissionResponse {
	private Long missionId;
	private String title;
	private String description;
	private String keyword;
	private MissionStatus status;
}