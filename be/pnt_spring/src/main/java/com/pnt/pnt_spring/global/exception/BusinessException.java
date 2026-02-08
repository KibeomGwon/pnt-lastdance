package com.pnt.pnt_spring.global.exception;

import com.pnt.pnt_spring.global.api.code.ErrorCode;

import lombok.Getter;

/**
 * 서비스/도메인 계층에서 ErrorCode 기반으로 던지는 공통 예외
 */
@Getter
public class BusinessException extends RuntimeException {

	private final ErrorCode errorCode;

	public BusinessException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	public BusinessException(ErrorCode errorCode, String message) {
		super(message);
		this.errorCode = errorCode;
	}
}
