package com.pnt.pnt_spring.domain.games.mission.application;

import java.util.List;

import com.pnt.pnt_spring.domain.games.mission.api.resp.GameMissionResponse;
import com.pnt.pnt_spring.domain.games.mission.api.resp.MissionResponse;

public interface MissionService {

	List<MissionResponse> getAllMissions();

	MissionResponse getMission(Long missionId);

	List<GameMissionResponse> getGameAllMissions(Long gameId);

	GameMissionResponse getGameMission(Long gameId, Long missionId);

}
