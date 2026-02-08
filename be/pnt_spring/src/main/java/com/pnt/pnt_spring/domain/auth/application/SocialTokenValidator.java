package com.pnt.pnt_spring.domain.auth.application;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.pnt.pnt_spring.global.api.code.ErrorCode;
import com.pnt.pnt_spring.global.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class SocialTokenValidator {

	@Value("${oauth2.kakao.app-id}")
	private String kakaoAppId;

	@Value("${oauth2.google.client-id}")
	private String googleClientId;

	RestTemplate restTemplate = new RestTemplate();

	public String validateAndGetId(String provider, String token) {

		if ("KAKAO".equalsIgnoreCase(provider)) {
			return validateKakao(token);
		} else if ("GOOGLE".equalsIgnoreCase(provider)) {
			return validateGoogle(token);
		}
		throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
	}

	// google 토큰 검증
	private String validateGoogle(String token) {
		try {
			String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + token;
			ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
			Map body = response.getBody();
			String aud = (String)body.get("aud");

			// 앱/웹 클라이언트 ID가 여러 개라면 리스트로 관리해서 contains로 체크해도 됨
			if (!googleClientId.equals(aud)) {
				log.error("Google Client ID Mismatch! req: {}, my: {}", aud, googleClientId);
				throw new BusinessException(ErrorCode.INVALID_TOKEN);
			}

			// 구글의 고유 사용자 ID는 sub 필드
			return (String)body.get("sub");
		} catch (Exception e) {
			log.error("Google Token Validation Failed", e);
			throw new BusinessException(ErrorCode.INVALID_TOKEN, "구글 로그인 실패: 토큰이 유효하지 않습니다.");
		}
	}

	// kakao 토큰 검증
	private String validateKakao(String token) {
		try {
			HttpHeaders headers = new HttpHeaders();
			headers.add("Authorization", "Bearer " + token);
			headers.add("Content-type", "application/x-www-form-urlencoded;charset=utf-8");

			HttpEntity<Void> request = new HttpEntity<>(headers);

			// 토큰 유효성 및 앱 ID 검증 (더 안전함)
			ResponseEntity<Map> response = restTemplate.exchange(
				"https://kapi.kakao.com/v1/user/access_token_info",
				HttpMethod.GET,
				request,
				Map.class
			);

			Map body = response.getBody();
			String appId = String.valueOf(body.get("appId"));

			if (!appId.equals(kakaoAppId)) {
				throw new BusinessException(ErrorCode.INVALID_TOKEN, "유효하지 않은 카카오 토큰입니다(App ID 불일치).");
			}

			return String.valueOf(body.get("id"));

		} catch (HttpClientErrorException e) {
			throw new BusinessException(ErrorCode.INVALID_TOKEN, "유효하지 않은 카카오 토큰입니다.");
		} catch (BusinessException e) {
			// 이미 발생한 비즈니스 예외(INVALID_TOKEN 등)는 그대로 던짐
			throw e;
		} catch (Exception e) {
			// 그 외 예측 못한 에러만 500으로 처리
			throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "카카오 로그인 중 알 수 없는 오류가 발생했습니다.");
		}
	}
}