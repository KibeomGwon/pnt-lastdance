package com.pnt.pnt_spring.domain.auth.api.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pnt.pnt_spring.domain.auth.api.req.IdDuplicateRequest;
import com.pnt.pnt_spring.domain.auth.api.req.LoginRequest;
import com.pnt.pnt_spring.domain.auth.api.req.SignupRequest;
import com.pnt.pnt_spring.domain.auth.api.req.SocialLoginRequest;
import com.pnt.pnt_spring.domain.auth.api.req.TokenDto;
import com.pnt.pnt_spring.domain.auth.api.resp.LoginResponse;
import com.pnt.pnt_spring.domain.auth.api.resp.SignupResponse;
import com.pnt.pnt_spring.domain.auth.application.AuthService;
import com.pnt.pnt_spring.global.api.response.CommonResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@Tag(name = "Auth", description = "회원관리 API")
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

	private final AuthService authService;

	// 회원가입
	@Operation(summary = "회원가입", description = "회원가입 합니다.")
	@PostMapping("/signup")
	public CommonResponse<SignupResponse> signup(@Valid @RequestBody SignupRequest request) {
		SignupResponse signupResponse = authService.signup(request);
		return new CommonResponse<>(signupResponse, "회원가입에 성공했습니다.", HttpStatus.OK);
	}

	// 로그인
	@Operation(summary = "로그인", description = "유저 로그인을 진행합니다.")
	@PostMapping("/login")
	public CommonResponse<LoginResponse> signin(@Valid @RequestBody LoginRequest request) {
		LoginResponse loginResponse = authService.login(request);
		return new CommonResponse<>(loginResponse, "로그인에 성공했습니다.", HttpStatus.OK);
	}

	// 로그아웃
	@Operation(summary = "로그아웃", description = "유저 액세스 토큰을 만료합니다.")
	@PostMapping("/logout")
	public CommonResponse<String> logout(HttpServletRequest request) {

		String accessToken = resolveToken(request);

		authService.logout(accessToken);

		return new CommonResponse<>("로그아웃 되었습니다.", "로그아웃 성공", HttpStatus.OK);
	}

	// 아이디 중복 체크
	@Operation(summary = "아이디 중복체크", description = "DB내 동일한 아이디가 있는지 확인합니다.")
	@PostMapping("/duplicate")
	public CommonResponse<Map<String, Boolean>> checkDuplicate(@RequestBody IdDuplicateRequest request) {

		boolean isDuplicate = authService.checkIdDuplicate(request.getId());

		Map<String, Boolean> responseData = Map.of("duplicated", isDuplicate);

		return new CommonResponse<>(responseData, "아이디 중복 확인 완료", HttpStatus.OK);
	}

	@Operation(summary = "소셜 로그인", description = "카카오 소셜로그인을 지원합니다.(Provider : KAKAO)")
	@PostMapping("/social-login")
	public CommonResponse<LoginResponse> socialLogin(@RequestBody SocialLoginRequest request) {
		LoginResponse loginResponse = authService.socialLogin(request);
		return new CommonResponse<>(loginResponse, "소셜 로그인 성공", HttpStatus.OK);
	}

	private String resolveToken(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7);
		}
		return null; // 혹은 예외 처리
	}

	@Operation(summary = "토큰 재발급", description = "사용자 토큰을 재발급합니다.")
	@PostMapping("/reissue")
	public CommonResponse<TokenDto> reissue(@RequestBody TokenDto tokenDto) {
		TokenDto newToken = authService.reissue(tokenDto);
		return new CommonResponse<>(newToken, "토큰이 성공적으로 재발급되었습니다.", HttpStatus.OK);
	}
}
