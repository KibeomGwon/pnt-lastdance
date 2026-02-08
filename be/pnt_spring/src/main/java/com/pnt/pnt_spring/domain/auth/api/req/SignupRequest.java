package com.pnt.pnt_spring.domain.auth.api.req;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {
	@NotBlank(message = "아이디는 필수입니다.")
	@Size(min = 5, message = "아이디는 5자 이상") // 5~12자 제한
	private String id; // user ID 아니고 로그인 ID

	@NotBlank(message = "비밀번호는 필수입니다.")
	@Size(min = 5, message = "비밀번호는 5자 이상") // 8~16자 제한
	private String password;

	@NotBlank(message = "비밀번호 확인은 필수입니다.")
	private String passwordConfirm;

	@NotBlank(message = "닉네임은 필수입니다.")
	@Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 합니다.") // 2~10자 제한
	private String nickname;

	private String email;
	private LocalDate birth;
	private String avatarUrl;

	public String getAvatarUrl() {
		if (avatarUrl == null) {
			return "default.png";
		}
		return avatarUrl;
	}
}