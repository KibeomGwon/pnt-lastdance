package com.pnt.pnt_spring.domain.members.member.api.resp;

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
public class FcmTokenUpdateResponse {
	private boolean isActive;

	public static FcmTokenUpdateResponse from(FcmToken token) {
		return FcmTokenUpdateResponse.builder()
			.isActive(token.isActive())
			.build();
	}
}
