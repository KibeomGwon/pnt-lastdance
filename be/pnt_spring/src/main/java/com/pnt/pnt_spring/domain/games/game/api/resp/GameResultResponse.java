package com.pnt.pnt_spring.domain.games.game.api.resp;

import java.time.OffsetDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GameResultResponse {
	private Long gameId;
	private String winner;
	private OffsetDateTime endedAt;
	private TotalStats stats;
	private MvpResponse mvp;
	private MvpResponse winningSecond;
	private MvpResponse losingFirst;

	private GameMemberStat myStat;

	@Getter
	@Builder
	public static class TotalStats {
		private int arrests;
		private int missionsCleared;
		private int durationSec;
	}

	@Getter
	@Builder
	public static class MvpResponse {
		private Long memberId;
		private String nickname;
		private String role;
		private String description;

		private Integer walk;
		private Integer arrestCount;
		private Integer longestSurvived;

		private Boolean isMissionCleared;

		private String rank;
		private Integer maxArrestCount;
		private Integer maxSurvivalTime;
	}

	@Getter
	@Builder
	public static class GameMemberStat {
		private Long memberId;
		private String nickname;
		private String role;

		// 이번 게임 기록
		private Integer walk;
		private Integer arrestCount;     // 이번 판 체포 수
		private Integer longestSurvived; // 이번 판 생존 시간

		private Boolean isMissionCleared;

		private String rank;             // 현재 등급 (예: "브론즈", "실버")
		private Integer maxArrestCount;  // (경찰일 때) 개인 최고 체포 기록
		private Integer maxSurvivalTime; // (도둑일 때) 개인 최고 생존 시간
	}

	@Getter
	@Builder
	public static class ClearedMissionResponse {
		private Long missionId;
		private String content;
		private String completedByNickname;
	}
}