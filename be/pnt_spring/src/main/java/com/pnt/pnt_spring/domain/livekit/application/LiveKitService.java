package com.pnt.pnt_spring.domain.livekit.application;

import com.pnt.pnt_spring.domain.livekit.api.resp.LiveKitResponse;

public interface LiveKitService {
	LiveKitResponse getPoliceToken(Long memberId, String roomCode);

	String generateToken(String roomName, String identity, int durationMinutes);

}
