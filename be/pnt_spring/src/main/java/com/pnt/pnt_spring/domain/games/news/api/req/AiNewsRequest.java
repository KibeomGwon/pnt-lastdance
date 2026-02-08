package com.pnt.pnt_spring.domain.games.news.api.req;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class AiNewsRequest {
	private Long gameId;             // 어떤 게임의 결과 뉴스인지

	private String startTime;        // yyyy-MM-dd HH:mm
	private String winningTeam;      // "경찰" 또는 "도둑"
	private int playTime;         // 플레이 경과 시간(초 정보)

	// 좌표 정보로 변경
	private Double latitude;
	private Double longitude;

	private int thiefCount;
	private int policeCount;
	private String mvp;               // MVP 닉네임
	private String winnerTopMember; // 승리팀 최고 공헌자
	private String loserTopMember;  // 패배팀 최고 공헌자
}