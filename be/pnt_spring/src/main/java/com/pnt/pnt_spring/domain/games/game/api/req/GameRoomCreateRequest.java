package com.pnt.pnt_spring.domain.games.game.api.req;

import java.util.List;

import com.pnt.pnt_spring.domain.games.utils.GeoConverter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class GameRoomCreateRequest {

	@NotNull
	@Min(1)
	private Integer playerCount;

	@NotNull
	@Min(1)
	private Integer timeLimit;

	@NotNull
	@Min(0)
	private Integer policeCount;

	@NotNull
	@Min(0)
	private Integer thiefCount;

	@NotNull
	@Min(0)
	private Integer cctvInterval;

	@NotNull
	@Min(0)
	private Integer missionCount;

	private Long mapId;

	private Boolean saveMap;
	private String mapName;
	private String mapDescription;

	@NotNull
	@Valid
	private Prison prison;

//	@NotNull
	@Size(min = 3, message = "polygon은 최소 3개 좌표가 필요합니다.")
	@Valid
	private List<LatLng> polygon;

	@Getter
	public static class Prison {
		@NotNull
		private Double lat;

		@NotNull
		private Double lng;
	}


	@Getter
	public static class LatLng implements GeoConverter.LatLngLike {
		@NotNull
		private Double lat;

		@NotNull
		private Double lng;
	}
}
