package com.pnt.pnt_spring.domain.games.mission.api.resp;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MissionResponse {
	private Long missionId;
	private String title;
	private String description;
	private String keyword;
}