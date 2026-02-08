package com.pnt.pnt_spring.domain.livekit.application.serviceImpl;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pnt.pnt_spring.domain.games.game.entity.Game;
import com.pnt.pnt_spring.domain.games.game.entity.GameMember;
import com.pnt.pnt_spring.domain.games.game.entity.GameSetting;
import com.pnt.pnt_spring.domain.games.game.repository.GameMemberRepository;
import com.pnt.pnt_spring.domain.games.game.repository.GameRepository;
import com.pnt.pnt_spring.domain.games.game.repository.GameSettingRepository;
import com.pnt.pnt_spring.domain.livekit.api.resp.LiveKitResponse;
import com.pnt.pnt_spring.domain.livekit.application.LiveKitService;
import com.pnt.pnt_spring.global.api.code.ErrorCode;
import com.pnt.pnt_spring.global.exception.BusinessException;

import io.livekit.server.AccessToken;
import io.livekit.server.RoomJoin;
import io.livekit.server.RoomName;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LiveKitServiceImpl implements LiveKitService {

	@Value("${livekit.api-key}")
	private String apiKey;

	@Value("${livekit.api-secret}")
	private String apiSecret;

	private final GameRepository gameRepository;
	private final GameMemberRepository gameMemberRepository;
	private final GameSettingRepository gameSettingRepository;

	@Override
	public LiveKitResponse getPoliceToken(Long memberId, String roomCode) {
		// 1. 게임 방 존재 확인 (메서드명 수정)
		Game game = gameRepository.findByRoomCodeAndIsDeletedFalse(roomCode)
			.orElseThrow(() -> new BusinessException(ErrorCode.GAME_NOT_FOUND));

		// 2. 해당 게임의 멤버인지 + '경찰' 포지션인지 검증 (메서드명 수정)
		GameMember gameMember = gameMemberRepository.findByGameIdAndMemberId(game.getId(), memberId)
			.orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

		if (gameMember.getGivenPosition() == null || !gameMember.getGivenPosition().isPolice()) {
			// ErrorCode 수정: FORBIDDEN 사용
			throw new BusinessException(ErrorCode.FORBIDDEN, "경찰 권한이 필요한 요청입니다.");
		}

		// 3. 게임 설정 시간 가져오기 (직접 조회로 수정)
		GameSetting setting = gameSettingRepository.findByGameIdAndIsDeletedFalse(game.getId())
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST, "게임 설정을 찾을 수 없습니다."));

		int gameDuration = setting.getTimeLimit(); // GameSetting에서는 timeLimit 필드 사용
		String identity = String.valueOf(memberId);

		// 4. LiveKit 토큰 생성
		String token = generateToken(roomCode, identity, gameDuration);

		// 5. 응답 반환
		return LiveKitResponse.builder()
			.token(token)
			.roomCode(roomCode)
			.identity(identity)
			.durationMinutes(gameDuration)
			.build();
	}

	public String generateToken(String roomName, String identity, int durationMinutes) {
		AccessToken token = new AccessToken(apiKey, apiSecret);
		token.setName(identity);
		token.setIdentity(identity);
		token.addGrants(new RoomJoin(true), new RoomName(roomName));

		long ttlSeconds = (durationMinutes + 5) * 60L;
		token.setTtl(ttlSeconds);

		return token.toJwt();
	}
}