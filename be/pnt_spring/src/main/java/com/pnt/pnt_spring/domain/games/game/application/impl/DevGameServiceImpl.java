package com.pnt.pnt_spring.domain.games.game.application.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pnt.pnt_spring.domain.games.game.application.DevGameService;
import com.pnt.pnt_spring.domain.games.game.entity.GameMemberStat;
import com.pnt.pnt_spring.domain.games.game.repository.GameMemberRepository;
import com.pnt.pnt_spring.domain.games.game.repository.GameMemberStatRepository;
import com.pnt.pnt_spring.domain.games.game.repository.GameRepository;
import com.pnt.pnt_spring.domain.games.game.repository.GameSettingRepository;
import com.pnt.pnt_spring.domain.games.game.repository.GameSkillRepository;
import com.pnt.pnt_spring.domain.games.mission.repository.GameMissionRepository;
import com.pnt.pnt_spring.domain.games.news.repository.GameNewsRepository;
import com.pnt.pnt_spring.global.api.code.ErrorCode;
import com.pnt.pnt_spring.global.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class DevGameServiceImpl implements DevGameService {

	private final GameRepository gameRepository;
	private final GameSettingRepository gameSettingRepository;
	private final GameMemberRepository gameMemberRepository;
	private final GameMemberStatRepository gameMemberStatRepository;
	private final GameSkillRepository gameSkillRepository;
	private final GameMissionRepository gameMissionRepository;
	private final GameNewsRepository gameNewsRepository;

	public void hardDeleteGame(Long gameId) {
		// 게임 존재 확인
		if (!gameRepository.existsById(gameId)) {
			throw new BusinessException(ErrorCode.GAME_NOT_FOUND, "존재하지 않는 게임입니다.");
		}

		// 1. 자식 테이블 데이터 삭제 (Foreign Key 제약조건 순서 고려)

		// 1-1. 뉴스 삭제
		gameNewsRepository.deleteByGameId(gameId);

		// 1-2. 미션 상태 삭제
		gameMissionRepository.deleteByGameId(gameId);

		// 1-3. 스킬 정보 삭제
		gameSkillRepository.deleteByGameId(gameId);

		// 1-4. 게임 멤버 스탯 삭제
		// (GameMemberStat은 GameMember에 의존하므로 먼저 삭제)
		List<GameMemberStat> stats = gameMemberStatRepository.findAllByGameId(gameId);
		gameMemberStatRepository.deleteAll(stats);

		// 1-5. 게임 멤버 삭제
		gameMemberRepository.deleteByGameId(gameId);

		// 1-6. 게임 설정 삭제 (MapsId로 연결되어 있어 ID가 동일)
		if (gameSettingRepository.existsById(gameId)) {
			gameSettingRepository.deleteById(gameId);
		}

		// 2. 부모 테이블(Game) 삭제
		gameRepository.deleteById(gameId);
	}
}