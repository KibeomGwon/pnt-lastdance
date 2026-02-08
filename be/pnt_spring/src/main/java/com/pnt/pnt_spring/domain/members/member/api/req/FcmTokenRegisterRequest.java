package com.pnt.pnt_spring.domain.members.member.api.req;

import com.pnt.pnt_spring.domain.members.member.entity.Member;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FcmTokenRegisterRequest {

	@Schema(description = "토큰명", example = "token....")
	private String value;

	@Schema(description = "푸시 알림 활성화 여부", example = "true || false")
	private boolean isActive;
}
