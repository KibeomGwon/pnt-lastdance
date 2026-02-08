package com.pnt.pnt_spring.domain.games.news.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pnt.pnt_spring.domain.games.news.api.resp.AiNewsResponse;
import com.pnt.pnt_spring.domain.games.news.api.resp.GameNewsResponse;
import com.pnt.pnt_spring.domain.games.news.application.Impl.GameNewsServiceImpl;
import com.pnt.pnt_spring.global.api.response.CommonResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Tag(name = "Game NEWS", description = "결과 뉴스 관련 API")
@RestController
@RequestMapping("/games/news")
@RequiredArgsConstructor
@Slf4j
public class GameNewsController {

	private final GameNewsServiceImpl gameNewsService;

	@Operation(summary = "AI 뉴스 저장", description = "FastAPI 서버로부터 생성된 뉴스를 받아 DB에 저장합니다.")
	@PostMapping("/result")
	public CommonResponse<Void> saveGameNews(@RequestBody AiNewsResponse response) {
		gameNewsService.saveNews(response);
		return new CommonResponse<>(null, "AI 뉴스 저장 성공", HttpStatus.OK);
	}

	@Operation(summary = "게임 뉴스 조회", description = "특정 게임의 AI 생성 뉴스를 조회합니다.")
	@GetMapping("/{gameId}")
	public CommonResponse<GameNewsResponse> getGameNews(@PathVariable Long gameId) {
		GameNewsResponse response = gameNewsService.getNewsByGameId(gameId);
		return new CommonResponse<>(response, "뉴스 조회 성공", HttpStatus.OK);
	}
}