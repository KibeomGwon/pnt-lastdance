package com.pnt.pnt_spring.domain.games.news.api.resp;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class AiNewsResponse {
	private Long gameId;      // 어떤 게임의 뉴스인지 식별 (필수)
	private String title;  // 뉴스 제목
	private String content;   // 뉴스 본문

}