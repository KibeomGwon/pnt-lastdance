package com.pnt.pnt_spring.domain.auth.filter;

import java.io.IOException;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pnt.pnt_spring.global.api.code.ErrorCode;
import com.pnt.pnt_spring.global.api.response.CommonResponse;
import com.pnt.pnt_spring.global.exception.BusinessException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtExceptionFilter extends OncePerRequestFilter {
	private final ObjectMapper objectMapper;

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		try {
			// 다음 필터를 실행하라고 명령
			filterChain.doFilter(request, response);
		} catch (BusinessException e) {
			// JwtAuthenticationFilter or JwtTokenProvider 예외 던질 시
			log.warn("[JwtExceptionFilter] BusinessException: {}", e.getMessage());
			setErrorResponse(response, e.getErrorCode());
		} catch (Exception e) {
			// 그 외 에러
			log.error("[JwtExceptionFilter] Unexpected error: ", e);
			setErrorResponse(response, ErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	// to JSON
	private void setErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {

		response.setStatus(errorCode.getStatusCode().value());

		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		// 공통 응답 포맷 생성
		CommonResponse<?> commonResponse = new CommonResponse<>(null, errorCode.getMessage(), errorCode);

		// JSON 변환 후 응답 쓰기
		response.getWriter().write(objectMapper.writeValueAsString(commonResponse.getBody()));
	}
}
