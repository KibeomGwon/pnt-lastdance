package com.pnt.pnt_spring.domain.chats.api.req;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class ChatRoomCreateRequest {

	@NotBlank(message = "채팅방 제목은 필수입니다.")
	private String title;

	@NotNull(message = "지역 코드는 필수입니다.")
	private Integer regionCode;

	private String description;

	@NotNull(message = "최대 인원은 필수입니다.")
	@Min(value = 1, message = "최대 인원은 최소 1명 이상이어야 합니다.")
	private Integer maxMembers;

}
