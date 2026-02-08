package com.pnt.pnt_spring.domain.auth.api.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequest {

	@NotBlank(message = "아이디는 필수값입니다.")
	private String id;
	@NotBlank(message = "패스워드는 필수값입니다.")
	private String password;

}