package com.pnt.pnt_spring.domain.games.game.application;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

import com.pnt.pnt_spring.domain.games.game.repository.GameRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class GameRoomCodeGenerator {

	private final GameRepository gameRepository;
	private final SecureRandom random = new SecureRandom();
	private static final String CHARS = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"; // 헷갈리는 문자 제거

	public String generateUniqueCode() {
		for (int i = 0; i < 30; i++) {
			String code = randomCode(6);
			if (!gameRepository.existsByRoomCodeAndIsDeletedFalse(code))
				return code;
		}
		throw new IllegalStateException("roomCode 생성 실패");
	}

	private String randomCode(int len) {
		StringBuilder sb = new StringBuilder(len);
		for (int i = 0; i < len; i++) {
			sb.append(CHARS.charAt(random.nextInt(CHARS.length())));
		}
		return sb.toString();
	}
}
