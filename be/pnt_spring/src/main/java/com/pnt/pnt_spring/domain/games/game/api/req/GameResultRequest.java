package com.pnt.pnt_spring.domain.games.game.api.req;

import java.util.List;

import com.pnt.pnt_spring.domain.games.game.enums.GameMemberStatus;
import com.pnt.pnt_spring.domain.games.game.enums.Position;
import com.pnt.pnt_spring.domain.games.game.enums.WinTeam;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
public class GameResultRequest {
	private Long gameId;
	private WinTeam winTeam; // "POLICE" or "THIEF"
	private List<MemberStat> memberStats;

	@Getter
	@NoArgsConstructor
	@ToString
	public static class MemberStat {
		private Long memberId;
		private Position position;
		private Integer walk;
		private Integer longestSurvived;

		private Boolean isConnected;
		private GameMemberStatus status;
	}
}