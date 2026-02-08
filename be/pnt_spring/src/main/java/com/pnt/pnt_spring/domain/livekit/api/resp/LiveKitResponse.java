package com.pnt.pnt_spring.domain.livekit.api.resp;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LiveKitResponse {
	private String token;
	private String roomCode;
	private String identity;
	private int durationMinutes;
}