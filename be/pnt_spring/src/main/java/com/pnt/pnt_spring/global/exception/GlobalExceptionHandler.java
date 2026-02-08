package com.pnt.pnt_spring.global.exception;

import java.util.stream.Collectors;

import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.pnt.pnt_spring.global.api.code.ErrorCode;
import com.pnt.pnt_spring.global.api.response.CommonResponse;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
	/**
	 * 1) 비즈니스 예외
	 */
	@ExceptionHandler(BusinessException.class)
	public CommonResponse<Void> handleBusinessException(BusinessException e, HttpServletRequest req) {
		ErrorCode code = e.getErrorCode();

		log.warn("[BusinessException] {} {} -> {}({}) : {}",
			req.getMethod(),
			req.getRequestURI(),
			code.name(),
			code.getCustomCode(),
			e.getMessage()
		);

		return new CommonResponse<>(null, e.getMessage(), code);
	}

	/**
	 * 2) @Valid 검증 실패
	 */
	@ExceptionHandler(MethodArgumentNotValidException.class)
	public CommonResponse<Void> handleMethodArgumentNotValid(MethodArgumentNotValidException e,
		HttpServletRequest req) {
		String message = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(err -> err.getField() + ": " +
				(err.getDefaultMessage() == null ? "invalid" : err.getDefaultMessage()))
			.collect(Collectors.joining(", "));

		log.warn("[Validation] {} {} -> {}", req.getMethod(), req.getRequestURI(), message);

		return new CommonResponse<>(null, message, ErrorCode.VALIDATION_ERROR);
	}

	/**
	 * 3) 바인딩 실패
	 */
	@ExceptionHandler(BindException.class)
	public CommonResponse<Void> handleBindException(BindException e, HttpServletRequest req) {
		String message = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(err -> err.getField() + ": " +
				(err.getDefaultMessage() == null ? "invalid" : err.getDefaultMessage()))
			.collect(Collectors.joining(", "));

		log.warn("[BindException] {} {} -> {}", req.getMethod(), req.getRequestURI(), message);

		return new CommonResponse<>(null,
			message.isBlank() ? "요청 값이 올바르지 않습니다." : message,
			ErrorCode.INVALID_REQUEST);
	}

	/**
	 * 추가 1) JSON 파싱 실패, 타입 오류 (깨진 JSON 등)
	 */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public CommonResponse<Void> handleMessageNotReadable(HttpMessageNotReadableException e,
		HttpServletRequest req) {
		log.warn("[MessageNotReadable] {} {} -> {}",
			req.getMethod(), req.getRequestURI(), e.getMessage());

		return new CommonResponse<>(null,
			"요청 형식이 올바르지 않습니다.",
			ErrorCode.INVALID_REQUEST);
	}

	/**
	 *  PathVariable / RequestParam 타입 불일치
	 * 예: /members/abc (id는 Long이어야 함)
	 */
	@ExceptionHandler(MethodArgumentTypeMismatchException.class)
	public CommonResponse<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e,
		HttpServletRequest req) {
		log.warn("[TypeMismatch] {} {} -> param={}, value={}",
			req.getMethod(),
			req.getRequestURI(),
			e.getName(),
			e.getValue());

		return new CommonResponse<>(null,
			"요청 파라미터 타입이 올바르지 않습니다.",
			ErrorCode.INVALID_REQUEST);
	}

	/**
	 * 4) 잘못된 HTTP Method
	 */
	@ExceptionHandler(HttpRequestMethodNotSupportedException.class)
	public CommonResponse<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e,
		HttpServletRequest req) {
		log.warn("[MethodNotSupported] {} {} -> {}", req.getMethod(), req.getRequestURI(), e.getMessage());

		return new CommonResponse<>(null,
			"지원하지 않는 HTTP 메서드입니다.",
			ErrorCode.METHOD_NOT_ALLOWED);
	}

	/**
	 * 5) 마지막 캐치
	 */
	@ExceptionHandler(Exception.class)
	public CommonResponse<Void> handleUnexpected(Exception e, HttpServletRequest req) {
		log.error("[Unexpected] {} {} -> {}", req.getMethod(), req.getRequestURI(), e.getMessage(), e);

		return new CommonResponse<>(null,
			"서버 내부 오류가 발생했습니다.",
			ErrorCode.INTERNAL_SERVER_ERROR);
	}
}
