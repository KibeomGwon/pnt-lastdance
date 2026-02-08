package com.pnt.pnt_spring.domain.members.stat.api.resp;

import com.pnt.pnt_spring.domain.members.stat.entity.MemberStatThief;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ThiefStatResponse { // 별도 파일로 독립
	private Integer escapeCount;
	private Integer averageSurvivalTimeSec;
	private Integer missionClearCount;
	private Integer longestSurvivalSec;
	private String grade;

	public static ThiefStatResponse from(MemberStatThief stat) {
		// 방어 로직: 스탯 데이터 자체가 아예 없는 경우 (회원가입 로직이 꼬였을 때 대비)
		if (stat == null) {
			return ThiefStatResponse.builder()
				.escapeCount(0)
				.averageSurvivalTimeSec(0)
				.missionClearCount(0)
				.longestSurvivalSec(0)
				.grade("바늘도둑") // SQL 초기 데이터(ID 1)와 이름을 맞춤
				.build();
		}

		// 등급 이름 가져오기 (혹시 등급 객체가 깨져있으면 기본값 사용)
		String gradeName = "바늘도둑";
		if (stat.getGradeThief() != null && stat.getGradeThief().getName() != null) {
			gradeName = stat.getGradeThief().getName();
		}

		return ThiefStatResponse.builder()
			.averageSurvivalTimeSec(stat.getAverageSurvivalSec())
			.missionClearCount(stat.getTotalMissionCount())
			.longestSurvivalSec(stat.getLongestSurvivalSec())
			.grade(gradeName)
			.build();
	}
}