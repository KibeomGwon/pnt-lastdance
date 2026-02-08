package com.pnt.pnt_spring.domain.games.game.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pnt.pnt_spring.domain.games.game.api.req.GameResultRequest;
import com.pnt.pnt_spring.domain.games.game.api.resp.GameResultResponse;
import com.pnt.pnt_spring.domain.games.game.application.GameResultService;
import com.pnt.pnt_spring.global.api.response.CommonResponse;
import com.pnt.pnt_spring.global.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Game Result", description = "게임 결과 및 통계 API")
@RestController
@RequestMapping("/games")
@RequiredArgsConstructor
public class GameResultController {

	private final GameResultService gameResultService;

	// 게임 종료 시 결과 저장 및 AI 뉴스 요청
	@Operation(summary = "게임 결과 저장 (Node 호출)", description = "게임 종료 후 통계 데이터를 저장하고 AI 뉴스 생성을 요청합니다.")
	@PostMapping("/result")
	public CommonResponse<Void> saveGameResult(@RequestBody GameResultRequest request) {
		gameResultService.saveGameResult(request);
		return new CommonResponse<>(null, "게임 결과 저장 및 뉴스 요청 완료", HttpStatus.OK);
	}

	// 클라이언트가 결과 화면 조회 (앱이 호출)
	@Operation(summary = "게임 결과 조회", description = "저장된 게임 결과를 바탕으로 승리 팀, MVP, 통계, 그리고 내 스탯을 조회합니다.")
	@GetMapping("/{gameId}/result")
	public CommonResponse<GameResultResponse> getGameResult(@PathVariable Long gameId) {
		// 현재 로그인한 멤버 ID 추출
		Long memberId = SecurityUtils.currentMemberId();

		// memberId를 서비스에 전달
		GameResultResponse response = gameResultService.getGameResult(gameId, memberId);
		return new CommonResponse<>(response, "게임 결과 조회 성공", HttpStatus.OK);
	}
}