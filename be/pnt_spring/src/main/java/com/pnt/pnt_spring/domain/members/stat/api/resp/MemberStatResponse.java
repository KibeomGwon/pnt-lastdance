package com.pnt.pnt_spring.domain.members.stat.api.resp;

import com.pnt.pnt_spring.domain.members.stat.entity.MemberStat;
import com.pnt.pnt_spring.domain.members.stat.entity.MemberStatPolice;
import com.pnt.pnt_spring.domain.members.stat.entity.MemberStatThief;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MemberStatResponse {

	private Integer totalGames;
	private Integer wins;
	private Integer loses;
	private Integer policeGame;
	private Integer thiefGame;
	private String policeGrade;
	private String thiefGrade;

	public static MemberStatResponse of(MemberStat stat, MemberStatPolice policeStat, MemberStatThief thiefStat) {

		// 기본 전적 정보 설정
		Integer totalGames = 0, wins = 0, loses = 0, policeGame = 0, thiefGame = 0;
		if (stat != null) {
			totalGames = stat.getTotalGames();
			wins = stat.getWins();
			loses = stat.getLoses();
			policeGame = stat.getPoliceGame();
			thiefGame = stat.getThiefGame();
		}

		// 경찰 등급 추출
		String policeGradeName = "순경"; // 기본값
		if (policeStat != null && policeStat.getGradePolice() != null) {
			policeGradeName = policeStat.getGradePolice().getName();
		}

		// 도둑 등급 추출
		String thiefGradeName = "좀도둑"; // 기본값
		if (thiefStat != null && thiefStat.getGradeThief() != null) {
			thiefGradeName = thiefStat.getGradeThief().getName();
		}

		return MemberStatResponse.builder()
			.totalGames(totalGames)
			.wins(wins)
			.loses(loses)
			.policeGame(policeGame)
			.thiefGame(thiefGame)
			.policeGrade(policeGradeName) // 추가됨
			.thiefGrade(thiefGradeName)   // 추가됨
			.build();
	}

}
