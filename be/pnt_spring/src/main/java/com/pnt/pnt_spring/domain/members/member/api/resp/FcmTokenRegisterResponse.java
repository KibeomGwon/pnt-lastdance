package com.pnt.pnt_spring.domain.members.member.api.resp;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

import com.pnt.pnt_spring.domain.members.member.entity.FcmToken;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FcmTokenRegisterResponse {
	private String value;
	private boolean isActive;

	public static FcmTokenRegisterResponse from(FcmToken fcmToken) {
		return FcmTokenRegisterResponse.builder()
			.value(fcmToken.getValue())
			.isActive(fcmToken.isActive())
			.build();
	}
}
