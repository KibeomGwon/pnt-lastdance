package com.pnt.pnt_spring.domain.livekit.api.req;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "LiveKit 토큰 발급 요청")
public class LiveKitRequest {

	@Schema(description = "참여 중인 게임 방 코드", example = "ABC1234")
	private String roomCode;
}