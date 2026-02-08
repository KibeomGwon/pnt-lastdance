package com.pnt.pnt_spring.domain.games.game.entity;

import org.locationtech.jts.geom.Geometry;

import com.pnt.pnt_spring.domain.utils.BaseEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "game_setting")
public class GameSetting extends BaseEntity {

	@Id
	private Long gameId;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "game_id")
	private Game game;

	// === 게임 설정 ===
	private Integer timeLimit;     // 분 단위
	private Integer playerCount;
	private Integer policeCount;
	private Integer thiefCount;

	@Column(name = "mission_count")
	private Integer missionCount;

	@Column(name = "cctv_interval")
	private Integer cctvInterval;

	// === 지도 정보 ===
	@Column(columnDefinition = "geometry")
	private Geometry boundaryGeo;


	private Double prisonLat;
	private Double prisonLng;

    /* =========================
       생성 메서드
       ========================= */

	public static GameSetting create(
		Game game,
		Integer timeLimit,
		Integer playerCount,
		Integer policeCount,
		Integer thiefCount,
		Integer cctvInterval,
		Geometry boundaryGeo,
		Double prisonLat,
		Double prisonLng,
		Integer missionCount
	) {
		GameSetting s = new GameSetting();
		s.game = game;
		s.timeLimit = timeLimit;
		s.playerCount = playerCount;
		s.policeCount = policeCount;
		s.thiefCount = thiefCount;
		s.cctvInterval = cctvInterval;
		s.boundaryGeo = boundaryGeo;
		s.prisonLat = prisonLat;
		s.prisonLng = prisonLng;
		s.missionCount = missionCount;
		return s;
	}

    /* =========================
       변경 메서드 (PATCH용)
       ========================= */

	/**
	 * 게임 규칙 관련 설정 수정
	 */
	public void updateSetting(
		Integer timeLimit,
		Integer playerCount,
		Integer policeCount,
		Integer thiefCount,
		Integer cctvInterval,
		Integer missionCount
	) {
		if (timeLimit != null)
			this.timeLimit = timeLimit;
		if (playerCount != null)
			this.playerCount = playerCount;
		if (policeCount != null)
			this.policeCount = policeCount;
		if (thiefCount != null)
			this.thiefCount = thiefCount;
		if (cctvInterval != null)
			this.cctvInterval = cctvInterval;
		if (missionCount != null)
			this.missionCount = missionCount;
	}

	/**
	 * 지도/좌표 관련 수정
	 */
	public void updateMap(
		Geometry boundaryGeo,
		Double prisonLat,
		Double prisonLng
	) {
		if (boundaryGeo != null)
			this.boundaryGeo = boundaryGeo;
		if (prisonLat != null)
			this.prisonLat = prisonLat;
		if (prisonLng != null)
			this.prisonLng = prisonLng;
	}
}
