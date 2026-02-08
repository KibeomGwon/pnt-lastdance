package com.pnt.pnt_spring.domain.members.stat.entity;

import com.pnt.pnt_spring.domain.members.member.entity.Member;
import com.pnt.pnt_spring.domain.utils.BaseEntity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member_stat_thief")
@Builder
@AllArgsConstructor
public class MemberStatThief extends BaseEntity {

	@Id
	private Long id;

	@MapsId
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "member_id")
	private Member member;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "grade_thief_id")
	private GradeThief gradeThief;

	private Integer totalMissionCount;
	private Integer longestSurvivalSec;
	private Integer averageSurvivalSec;

	public static MemberStatThief createInitial(Member member, GradeThief initialGrade) {
		return MemberStatThief.builder()
			.member(member)
			.gradeThief(initialGrade)// 바늘도둑(ID:1) 객체를 주입받아야 함
			.totalMissionCount(0)
			.longestSurvivalSec(0)
			.averageSurvivalSec(0)
			.build();
	}

	public void updateAfterGame(boolean isWin, Integer survivalSec, Integer totalThiefGames, Integer missionCount) {
		int currentSurvival = (survivalSec == null) ? 0 : survivalSec;
		int totalGames = (totalThiefGames == null || totalThiefGames == 0) ? 1 : totalThiefGames; // 0으로 나누기 방지

		// 1. 평균 생존 시간 계산 (MemberStat에서 가져온 totalThiefGames 사용)
		// 공식: ((기존 평균 * (현재판수 - 1)) + 이번 생존 시간) / 현재판수
		int previousAvg = (this.averageSurvivalSec == null) ? 0 : this.averageSurvivalSec;

		// totalGames는 이미 1 증가된 상태로 넘어온다고 가정하므로 (totalGames - 1)이 이전 판수
		long totalSurvivalTime = ((long)previousAvg * (totalGames - 1)) + currentSurvival;

		this.averageSurvivalSec = (int)(totalSurvivalTime / totalGames);

		// 2. 최대 생존 시간 갱신
		if (this.longestSurvivalSec == null || currentSurvival > this.longestSurvivalSec) {
			this.longestSurvivalSec = currentSurvival;
		}

		int currentMissions = (missionCount == null) ? 0 : missionCount;
		this.totalMissionCount = (this.totalMissionCount == null ? 0 : this.totalMissionCount) + currentMissions;
	}

	public void changeGrade(GradeThief newGrade) {
		this.gradeThief = newGrade;
	}

}