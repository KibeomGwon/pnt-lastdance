package com.pnt.pnt_spring.domain.games.game.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pnt.pnt_spring.domain.games.game.application.DevGameService;
import com.pnt.pnt_spring.global.api.response.CommonResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@Tag(name = "Dev Game", description = "개발/테스트용 게임 관리 API")
@RestController
@RequestMapping("/dev/games")
@RequiredArgsConstructor
public class DevGameController {

	private final DevGameService devGameService;

	@Operation(summary = "게임 데이터 완전 삭제 (Hard Delete)",
		description = "테스트를 위해 특정 게임과 연관된 모든 데이터(멤버, 설정, 미션, 뉴스 등)를 DB에서 영구 삭제합니다.")
	@DeleteMapping("/{gameId}")
	public CommonResponse<Void> hardDeleteGame(@PathVariable Long gameId) {
		devGameService.hardDeleteGame(gameId);
		return new CommonResponse<>(null, "게임 데이터 완전 삭제 성공", HttpStatus.OK);
	}
}