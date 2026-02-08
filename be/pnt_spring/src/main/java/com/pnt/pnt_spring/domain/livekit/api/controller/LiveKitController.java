package com.pnt.pnt_spring.domain.livekit.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pnt.pnt_spring.domain.auth.jwt.CustomUserDetails;
import com.pnt.pnt_spring.domain.livekit.api.req.LiveKitRequest;
import com.pnt.pnt_spring.domain.livekit.api.resp.LiveKitResponse;
import com.pnt.pnt_spring.domain.livekit.application.LiveKitService;
import com.pnt.pnt_spring.global.api.response.CommonResponse;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/livekit")
@RequiredArgsConstructor
public class LiveKitController {

	private final LiveKitService liveKitService;

	@Operation(summary = "LiveKit 토큰 발급", description = "게임 시작 시 경찰 유저인지 검증하고 LiveKit 접속 토큰을 발급합니다.")
	@PostMapping("/token")
	public CommonResponse<LiveKitResponse> getLiveKitToken(
		@AuthenticationPrincipal CustomUserDetails userDetails,
		@RequestBody LiveKitRequest request // Map -> DTO 변경
	) {
		String roomCode = request.getRoomCode(); // getter 사용
		Long memberId = userDetails.getMemberId();

		LiveKitResponse response = liveKitService.getPoliceToken(memberId, roomCode);

		return new CommonResponse<>(response, "LiveKit 토큰 발급 완료", HttpStatus.OK);
	}
}