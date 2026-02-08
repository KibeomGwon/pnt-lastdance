package com.pnt.pnt_spring.domain.members.member.api.req;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class FcmTokenUpdateRequest {
	private boolean isActive;
}
