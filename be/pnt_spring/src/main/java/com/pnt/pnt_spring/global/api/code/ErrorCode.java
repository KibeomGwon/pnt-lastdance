package com.pnt.pnt_spring.global.api.code;

import org.springframework.http.HttpStatus;

import lombok.Getter;

@Getter
public enum ErrorCode {

	/*
	 * ===== Code Rule
	 * 1xxx: AUTH
	 * 2xxx: MEMBER
	 * 3xxx: ROOM / MATCHING
	 * 4xxx: GAME
	 * 5xxx: CHAT
	 * 6xxx: NEWS
	 * 9xxx: COMMON / SERVER
	 */

	// =========================
	// COMMON / SERVER (9xxx)
	// =========================
	INVALID_REQUEST(HttpStatus.BAD_REQUEST, 9, "잘못된 요청입니다."),
	VALIDATION_ERROR(HttpStatus.BAD_REQUEST, 9, "입력값이 유효하지 않습니다."),
	PARAM_BIND_ERROR(HttpStatus.BAD_REQUEST, 9, "파라미터 바인딩에 실패했습니다."),

	METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, 9, "지원하지 않는 HTTP 메서드입니다."),

	INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, 9, "서버 내부 오류가 발생했습니다."),

	// =========================
	// AUTH (1xxx)
	// =========================
	UNAUTHORIZED(HttpStatus.UNAUTHORIZED, 1, "인증되지 않은 사용자입니다."),
	INVALID_TOKEN(HttpStatus.UNAUTHORIZED, 1, "유효하지 않은 토큰입니다."),
	EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, 1, "만료된 토큰입니다."),
	INVALID_INPUT_VALUE(HttpStatus.NOT_FOUND, 1, "지원하지 않는 소셜 플랫폼입니다"),
	REFRESH_TOKEN_NOT_FOUND(HttpStatus.UNAUTHORIZED, 1, "로그아웃 된 사용자입니다."),
	INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, 1, "리프레시 토큰이 일치하지 않습니다."),

	FORBIDDEN(HttpStatus.FORBIDDEN, 1, "접근 권한이 없습니다."),

	// =========================
	// MEMBER (2xxx)
	// =========================
	MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, 2, "사용자를 찾을 수 없습니다."),
	DUPLICATE_USER_ID(HttpStatus.CONFLICT, 2, "이미 존재하는 아이디입니다."),
	DUPLICATE_NICKNAME(HttpStatus.CONFLICT, 2, "이미 사용 중인 닉네임입니다."),
	INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, 2, "비밀번호가 일치하지 않습니다."),

	// 프로필/상태
	PROFILE_NOT_FOUND(HttpStatus.NOT_FOUND, 2, "프로필 정보를 찾을 수 없습니다."),

	// 등급
	GRADE_NOT_FOUND(HttpStatus.NOT_FOUND, 2, "등급을 찾을 수 없습니다."),

	// FCM
	FCM_TOKEN_NOT_FOUND(HttpStatus.NOT_FOUND, 2, "등록된 FCM 토큰이 없습니다."),

	// =========================
	// ROOM / MATCHING (3xxx)
	// =========================
	ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, 3, "존재하지 않는 방입니다."),
	ROOM_FULL(HttpStatus.CONFLICT, 3, "방의 정원이 초과되었습니다."),
	ROOM_ALREADY_JOINED(HttpStatus.CONFLICT, 3, "이미 다른 방에 참여 중입니다."),
	ROOM_NOT_JOINED(HttpStatus.CONFLICT, 3, "참여하지 않은 방에 대한 요청입니다."),

	ROOM_NOT_HOST(HttpStatus.FORBIDDEN, 3, "방장 권한이 필요합니다."),
	ROOM_ALREADY_STARTED(HttpStatus.CONFLICT, 3, "이미 게임이 시작된 방입니다."),

	// Ready/Start 흐름
	ROOM_NOT_READY(HttpStatus.CONFLICT, 3, "아직 준비 상태가 아닙니다."),
	ROOM_ALREADY_READY(HttpStatus.CONFLICT, 3, "이미 준비 완료 상태입니다."),

	// =========================
	// GAME (4xxx)
	// =========================
	GAME_NOT_FOUND(HttpStatus.NOT_FOUND, 4, "진행 중인 게임을 찾을 수 없습니다."),
	GAME_ALREADY_STARTED(HttpStatus.CONFLICT, 4, "이미 게임이 시작되었습니다."),
	GAME_ALREADY_ENDED(HttpStatus.CONFLICT, 4, "이미 종료된 게임입니다."),

	// 경찰/도둑 역할 기반 상태 충돌
	INVALID_ROLE_ACTION(HttpStatus.CONFLICT, 4, "해당 역할로는 수행할 수 없는 작업입니다."),
	INVALID_GAME_STATE(HttpStatus.CONFLICT, 4, "현재 게임 상태에서는 불가능한 요청입니다."),
	TARGET_NOT_FOUND(HttpStatus.NOT_FOUND, 4, "대상 플레이어를 찾을 수 없습니다."),

	// 미션
	MISSION_NOT_FOUND(HttpStatus.NOT_FOUND, 4, "대상 미션을 찾을 수 없습니다."),

	//map
	MAP_NOT_FOUND(HttpStatus.NOT_FOUND, 4, "맵을 찾을 수 없습니다."),

	// =========================
	// CHAT (5xxx)
	// =========================
	CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, 5, "채팅방을 찾을 수 없습니다."),
	CHAT_FORBIDDEN(HttpStatus.FORBIDDEN, 5, "채팅 권한이 없습니다."),
	MESSAGE_SEND_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, 5, "메시지 전송에 실패했습니다."),

	CHAT_ROOM_FORBIDDEN(HttpStatus.FORBIDDEN, 5, "채팅방 권한이 없습니다."),
	CHAT_ROOM_BANNED(HttpStatus.FORBIDDEN, 5, "강퇴(밴) 상태로 재입장할 수 없습니다."),
	CHAT_ROOM_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, 5, "채팅방 멤버가 아닙니다."),
	CHAT_ROOM_CREATE_INVALIDATE(HttpStatus.BAD_REQUEST, 5, "잘못된 채팅방 생성 요청입니다."),

	// =========================
	// NEWS (6xxx)
	// =========================
	NEWS_NOT_FOUND(HttpStatus.NOT_FOUND, 6, "뉴스를 찾을 수 없습니다.");

	private final HttpStatus statusCode;
	private final int customCode;
	private final String message;

	ErrorCode(HttpStatus statusCode, int domainCode, String message) {
		this.statusCode = statusCode;
		this.customCode = domainCode * 1000 + statusCode.value();
		this.message = message;
	}
}