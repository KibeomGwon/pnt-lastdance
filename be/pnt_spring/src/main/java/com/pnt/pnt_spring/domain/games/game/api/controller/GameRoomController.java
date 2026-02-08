package com.pnt.pnt_spring.domain.games.game.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pnt.pnt_spring.domain.games.game.api.req.GameRoomCreateRequest;
import com.pnt.pnt_spring.domain.games.game.api.resp.GameRoomCreateResponse;
import com.pnt.pnt_spring.domain.games.game.api.resp.GameRoomStartableResponse;
import com.pnt.pnt_spring.domain.games.game.api.resp.GameStartResponse;
import com.pnt.pnt_spring.domain.games.game.application.GameRoomService;
import com.pnt.pnt_spring.global.api.response.CommonResponse;
import com.pnt.pnt_spring.global.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
@Tag(name = "Game Room", description = "게임방 생성/시작 관련 API")
public class GameRoomController {

	private final GameRoomService gameRoomService;

	/**
	 * 게임방 생성
	 */
	@Operation(summary = "게임방 생성")
	@PostMapping
	public CommonResponse<GameRoomCreateResponse> createRoom(
		@Valid @RequestBody GameRoomCreateRequest req
	) {
		Long hostMemberId = SecurityUtils.currentMemberId();
		GameRoomCreateResponse data = gameRoomService.createRoom(hostMemberId, req);

		return new CommonResponse<>(
			data,
			"게임방 생성 성공",
			HttpStatus.CREATED
		);
	}

	@Operation(summary = "게임 시작")
	@PostMapping("/{roomId}/start")
	public CommonResponse<GameStartResponse> start(@PathVariable Long roomId) {
		Long actorId = SecurityUtils.currentMemberId();
		GameStartResponse data = gameRoomService.start(actorId, roomId);

		return new CommonResponse<>(
			data,
			"게임 시작 성공",
			HttpStatus.OK
		);
	}

	@Operation(summary = "게임 시작 가능 여부 조회")
	@GetMapping("/{roomId}/startable")
	public CommonResponse<GameRoomStartableResponse> startable(@PathVariable Long roomId) {

		Long actorId = SecurityUtils.currentMemberId();
		GameRoomStartableResponse data =
			gameRoomService.getStartable(actorId, roomId);

		return new CommonResponse<>(
			data,
			"게임 시작 가능 여부 조회 성공",
			HttpStatus.OK
		);
	}

	@PostMapping("/{gameId}/reset")
	@Operation(summary = "게임방 초기화 (재시작 준비)", description = "게임 종료 후, 같은 멤버끼리 다시 하기 위해 데이터를 초기화합니다.")
	public CommonResponse<Void> resetGame(@PathVariable Long gameId) {
		gameRoomService.resetGame(gameId);
		return new CommonResponse<>(null, "재시작 준비 완료", HttpStatus.OK);
	}
}

