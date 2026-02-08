package com.pnt.pnt_spring.domain.games.game.api.controller;

import com.pnt.pnt_spring.domain.games.game.api.req.*;
import com.pnt.pnt_spring.domain.games.game.api.resp.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pnt.pnt_spring.domain.games.game.application.GameRoomMemberService;
import com.pnt.pnt_spring.global.api.response.CommonResponse;
import com.pnt.pnt_spring.global.utils.SecurityUtils;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/rooms")
@Tag(name = "Game Room Member", description = "게임방 멤버(입장/퇴장/준비/포지션/강퇴) API")
public class GameRoomMemberController {

	private final GameRoomMemberService gameRoomMemberService;

	/**
	 * 게임방 참여 (roomCode로 입장)
	 */
	@Operation(summary = "게임방 참여")
	@PostMapping("/join")
	public CommonResponse<GameRoomJoinResponse> join(
		@Valid @RequestBody GameRoomJoinRequest req
	) {
		Long memberId = SecurityUtils.currentMemberId();
		GameRoomJoinResponse data = gameRoomMemberService.joinRoom(memberId, req);

		return new CommonResponse<>(
			data,
			"게임방 참여 성공",
			HttpStatus.OK
		);
	}

	@Operation(summary = "게임방 나가기")
	@DeleteMapping("/{roomId}/members/me")
	public CommonResponse<Void> leave(@PathVariable Long roomId) {
		Long memberId = SecurityUtils.currentMemberId();
		gameRoomMemberService.leave(memberId, roomId);
		return new CommonResponse<>(
			null,
			"게임방 나가기 성공",
			HttpStatus.OK
		);
	}

	/**
	 * 게임방 멤버 목록 조회
	 */
	@Operation(summary = "게임방 멤버 목록 조회")
	@GetMapping("/{roomId}/members")
	public CommonResponse<GameRoomMemberListResponse> getMembers(@PathVariable Long roomId) {
		GameRoomMemberListResponse data = gameRoomMemberService.getRoomMembers(roomId);

		return new CommonResponse<>(
			data,
			"게임방 멤버 목록 조회 성공",
			HttpStatus.OK
		);
	}

	/**
	 * 준비 상태 변경 (토글)
	 */
	@Operation(summary = "게임방 준비 상태 변경")
	@PatchMapping("/{roomId}/ready")
	public CommonResponse<GameRoomReadyResponse> ready(
		@PathVariable Long roomId,
		@Valid @RequestBody GameRoomReadyRequest req
	) {
		Long memberId = SecurityUtils.currentMemberId();
		GameRoomReadyResponse data = gameRoomMemberService.updateReady(roomId, memberId, req);

		return new CommonResponse<>(
			data,
			"게임 준비 상태 변경 성공",
			HttpStatus.OK
		);
	}

	/**
	 * 플레이 역할(포지션) 선택
	 */
	@Operation(summary = "플레이 역할 선택")
	@PostMapping("/{roomId}/position")
	public CommonResponse<GameRoomPositionResponse> pickPosition(
		@PathVariable Long roomId,
		@Valid @RequestBody GameRoomPositionRequest req
	) {
		Long memberId = SecurityUtils.currentMemberId();
		GameRoomPositionResponse data = gameRoomMemberService.pickPosition(memberId, roomId, req);

		return new CommonResponse<>(
			data,
			"플레이 역할 선택 성공",
			HttpStatus.OK
		);
	}

	@Operation(summary = "게임방 강퇴")
	@PostMapping("/{roomId}/members/kick")
	public CommonResponse<Void> kick(
		@PathVariable Long roomId,
		@Valid @RequestBody GameRoomKickRequest req
	) {
		Long actorId = SecurityUtils.currentMemberId();
		gameRoomMemberService.kick(actorId, roomId, req.getTargetMemberId(), req.getReason());
		return new CommonResponse<>(
			null,
			"게임방 강퇴 처리 성공",
			HttpStatus.OK
		);
	}

	@Operation(summary = "게임방 방장 위임")
	@PostMapping("/{roomId}/delegate-host")
	public CommonResponse<GameRoomHostDelegateResponse> delegateHost(
			@PathVariable Long roomId,
			@Valid @RequestBody GameRoomHostDelegateRequest req
	) {
		Long actorId = SecurityUtils.currentMemberId();

		GameRoomHostDelegateResponse data =
				gameRoomMemberService.delegateHost(actorId, roomId, req.getTargetMemberId());

		return new CommonResponse<>(
				data,
				"게임방 방장 위임 성공",
				HttpStatus.OK
		);
	}

}
