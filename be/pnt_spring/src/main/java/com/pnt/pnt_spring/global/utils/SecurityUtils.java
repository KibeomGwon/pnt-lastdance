package com.pnt.pnt_spring.global.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.pnt.pnt_spring.domain.auth.jwt.CustomUserDetails;
import com.pnt.pnt_spring.global.api.code.ErrorCode;
import com.pnt.pnt_spring.global.exception.BusinessException;

public final class SecurityUtils {

	private SecurityUtils() {
	}

	public static Long currentMemberId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();

		if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() == null) {
			throw new BusinessException(ErrorCode.UNAUTHORIZED, "인증 정보가 없습니다.");
		}

		Object principal = auth.getPrincipal();

		// JwtTokenProvider에서 CustomUserDetails로 넣어줄 예정
		if (principal instanceof CustomUserDetails userDetails) {
			return userDetails.getMemberId();
		}

		throw new BusinessException(ErrorCode.UNAUTHORIZED, "principal 타입이 CustomUserDetails가 아닙니다.");
	}
}
