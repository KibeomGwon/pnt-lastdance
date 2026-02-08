package com.pnt.pnt_spring.domain.games.game.api.resp;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Getter
@AllArgsConstructor
public class GameRoomSettingResponse {
	private Long roomId;
	private String status;

	private Integer timeLimit;
	private Integer playerCount;
	private Integer policeCount;
	private Integer thiefCount;
	private Integer cctvInterval;
	private Integer missionCount;
	private String roomCode;

	private Double prisonLat;
	private Double prisonLng;

	private List<LatLng> polygon; // 폴리곤 정보 추가

	@Getter
	@Builder
	@ToString
	public static class LatLng {
		private Double lat;
		private Double lng;
	}
}
