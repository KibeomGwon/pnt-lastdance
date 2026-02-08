package com.pnt.pnt_spring.domain.games.game.application.impl;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pnt.pnt_spring.domain.games.game.api.req.GameResultRequest;
import com.pnt.pnt_spring.domain.games.game.api.resp.GameResultResponse;
import com.pnt.pnt_spring.domain.games.game.application.GameResultService;
import com.pnt.pnt_spring.domain.games.game.entity.Game;
import com.pnt.pnt_spring.domain.games.game.entity.GameMember;
import com.pnt.pnt_spring.domain.games.game.entity.GameMemberStat;
import com.pnt.pnt_spring.domain.games.game.entity.GameSetting;
import com.pnt.pnt_spring.domain.games.game.enums.GameStatus;
import com.pnt.pnt_spring.domain.games.game.enums.Position;
import com.pnt.pnt_spring.domain.games.game.enums.WinTeam;
import com.pnt.pnt_spring.domain.games.game.repository.GameMemberRepository;
import com.pnt.pnt_spring.domain.games.game.repository.GameMemberStatRepository;
import com.pnt.pnt_spring.domain.games.game.repository.GameRepository;
import com.pnt.pnt_spring.domain.games.game.repository.GameSettingRepository;
import com.pnt.pnt_spring.domain.games.game.repository.GradePoliceRepository;
import com.pnt.pnt_spring.domain.games.game.repository.GradeThiefRepository;
import com.pnt.pnt_spring.domain.games.mission.entity.GameMission;
import com.pnt.pnt_spring.domain.games.mission.entity.MissionStatus;
import com.pnt.pnt_spring.domain.games.mission.repository.GameMissionRepository;
import com.pnt.pnt_spring.domain.games.news.api.req.AiNewsRequest;
import com.pnt.pnt_spring.domain.members.member.entity.Member;
import com.pnt.pnt_spring.domain.members.stat.entity.GradePolice;
import com.pnt.pnt_spring.domain.members.stat.entity.GradeThief;
import com.pnt.pnt_spring.domain.members.stat.entity.MemberStat;
import com.pnt.pnt_spring.domain.members.stat.entity.MemberStatPolice;
import com.pnt.pnt_spring.domain.members.stat.entity.MemberStatThief;
import com.pnt.pnt_spring.domain.members.stat.repository.MemberStatPoliceRepository;
import com.pnt.pnt_spring.domain.members.stat.repository.MemberStatRepository;
import com.pnt.pnt_spring.domain.members.stat.repository.MemberStatThiefRepository;
import com.pnt.pnt_spring.global.api.code.ErrorCode;
import com.pnt.pnt_spring.global.exception.BusinessException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class GameResultServiceImpl implements GameResultService {

	private final GameRepository gameRepository;
	private final GameMemberRepository gameMemberRepository;
	private final GameMemberStatRepository gameMemberStatRepository;
	private final RabbitTemplate rabbitTemplate;
	private final MemberStatRepository memberStatRepository;
	private final GameSettingRepository gameSettingRepository;

	private final MemberStatPoliceRepository memberStatPoliceRepository;
	private final MemberStatThiefRepository memberStatThiefRepository;
	private final GradePoliceRepository gradePoliceRepository;
	private final GradeThiefRepository gradeThiefRepository;
	private final GameMissionRepository gameMissionRepository;

	private static final long MIN_GRADE_ID = 1L;
	private static final long MAX_GRADE_ID = 11L;

	@Override
	public void saveGameResult(GameResultRequest request) {

		log.info("request = {}", request.toString());
		System.out.println(request);

		Game game = gameRepository.findById(request.getGameId())
			.orElseThrow(() -> new BusinessException(ErrorCode.GAME_NOT_FOUND));

		if (game.getStatus() == GameStatus.ENDED) {
			throw new BusinessException(ErrorCode.GAME_ALREADY_ENDED);
		}

		game.end(request.getWinTeam());

		List<GameMission> completedMissions = gameMissionRepository.findByGameId(game.getId()).stream()
			.filter(gm -> gm.getStatus() == MissionStatus.SUCCESS && gm.getCompletedBy() != null)
			.toList();

		Map<Long, Integer> missionCountMap = completedMissions.stream()
			.collect(Collectors.groupingBy(
				gm -> gm.getCompletedBy().getId(),
				Collectors.summingInt(e -> 1)
			));

		List<GameMember> allMembers = gameMemberRepository.findAllByGameId(request.getGameId());
		Map<Long, GameMember> memberMap = allMembers.stream()
			.collect(Collectors.toMap(gm -> gm.getMember().getId(), Function.identity()));
		// 요청된 멤버 스탯 정보를 순회하며 처리
		for (GameResultRequest.MemberStat statReq : request.getMemberStats()) {
			GameMember gameMember = memberMap.get(statReq.getMemberId());

			if (gameMember == null) {
				log.warn("GameMember not found for id: {}", statReq.getMemberId());
				continue;
			}

			// 1. isConnected가 false인 경우 -> 방 나가기 처리 (isDeleted = true) 후 스킵
			if (Boolean.FALSE.equals(statReq.getIsConnected())) {
				gameMember.leave(); // 상태 초기화 및 isDeleted = true
				continue; // 스탯 업데이트 로직 수행 안 함
			}

			// 2. isConnected가 true인 경우 -> 스탯 업데이트 진행


			int calculatedMissionCount = missionCountMap.getOrDefault(statReq.getMemberId(), 0);


			// 2-2. GameMemberStat 업데이트
			GameMemberStat stat = gameMemberStatRepository.findByGameMemberId(gameMember.getId())
				.orElseGet(() -> gameMemberStatRepository.save(GameMemberStat.createInitialStat(gameMember)));

			stat.updateResultStats(statReq.getWalk(), statReq.getLongestSurvived(), calculatedMissionCount);

			gameMemberStatRepository.save(stat);

			// 2-3. 누적 스탯 및 등급 업데이트
			updateMemberGradeAndStats(stat, request.getWinTeam(), statReq.getPosition(), calculatedMissionCount);
		}

		GameSetting setting = gameSettingRepository.findByGameIdAndIsDeletedFalse(game.getId())
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

		triggerAiNewsGeneration(game, setting.getPrisonLat(), setting.getPrisonLng());
	}

	@Override
	@Transactional(readOnly = true)
	public GameResultResponse getGameResult(Long gameId, Long memberId) {
		Game game = gameRepository.findById(gameId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GAME_NOT_FOUND));

		List<GameMemberStat> allStats = gameMemberStatRepository.findAllByGameId(gameId);

		long totalMissionsClearedLong = gameMissionRepository.findByGameId(gameId).stream()
			.filter(gm -> gm.getStatus() == MissionStatus.SUCCESS)
			.count();
		int totalMissionsCleared = (int) totalMissionsClearedLong;

		List<GameMemberStat> policeStats = new ArrayList<>();
		List<GameMemberStat> thiefStats = new ArrayList<>();
		int totalArrests = 0;

		for (GameMemberStat stat : allStats) {
			if (stat.getPosition() == Position.POLICE) {
				policeStats.add(stat);
			} else if (stat.getPosition() == Position.THIEF) {
				thiefStats.add(stat);
			}

			if (stat.getArrestCount() != null)
				totalArrests += stat.getArrestCount();
		}

		// MVP 산정 로직
		policeStats.sort((a, b) -> {
			int result = compareStats(b.getArrestCount(), a.getArrestCount());
			if (result == 0) {
				return compareStats(b.getWalk(), a.getWalk());
			}
			return result;
		});
		thiefStats.sort((a, b) -> compareStats(b.getLongestSurvived(), a.getLongestSurvived()));

		List<GameMemberStat> winnerStats;
		List<GameMemberStat> loserStats;
		boolean isPoliceWin = (game.getWinTeam() == WinTeam.POLICE);

		if (isPoliceWin) {
			winnerStats = policeStats;
			loserStats = thiefStats;
		} else {
			winnerStats = thiefStats;
			loserStats = policeStats;
		}

		GameMemberStat mvpStat = winnerStats.isEmpty() ? null : winnerStats.get(0);
		GameMemberStat winningSecondStat = (winnerStats.size() > 1) ? winnerStats.get(1) : null;
		GameMemberStat losingFirstStat = loserStats.isEmpty() ? null : loserStats.get(0);

		int durationSec = 0;
		OffsetDateTime endGame = game.getEndTime();
		if(game.getEndTime() == null){
			endGame = OffsetDateTime.now();
		}
		durationSec = (int)Duration.between(game.getStartTime(), endGame).toSeconds();

		// 내 스탯 + 등급/최고기록 조회 로직 추가
		GameMemberStat myGameStat = allStats.stream()
			.filter(stat -> stat.getGameMember().getMember().getId().equals(memberId))
			.findFirst()
			.orElse(null);

		GameResultResponse.GameMemberStat myStatResponse = null;

		if (myGameStat != null) {
			Member member = myGameStat.getGameMember().getMember();
			String rankName = "Unknown";
			Integer maxArrest = null;
			Integer maxSurvival = null;

			// 역할에 따라 DB에서 등급과 최고 기록 조회
			if (myGameStat.getPosition() == Position.POLICE) {
				MemberStatPolice policeInfo = memberStatPoliceRepository.findById(member.getId())
					.orElse(null);
				if (policeInfo != null) {
					rankName = policeInfo.getGradePolice().getName();
					maxArrest = policeInfo.getMostArrestsInGame();
				}
			} else if (myGameStat.getPosition() == Position.THIEF) {
				MemberStatThief thiefInfo = memberStatThiefRepository.findById(member.getId())
					.orElse(null);
				if (thiefInfo != null) {
					rankName = thiefInfo.getGradeThief().getName();
					maxSurvival = thiefInfo.getLongestSurvivalSec();
				}
			}

			myStatResponse = GameResultResponse.GameMemberStat.builder()
				.memberId(member.getId())
				.nickname(getNickname(myGameStat))
				.role(myGameStat.getPosition().name())
				.walk(myGameStat.getWalk())
				.arrestCount(myGameStat.getArrestCount())
				.longestSurvived(myGameStat.getLongestSurvived())
				// 추가된 필드 매핑
				.rank(rankName)
				.maxArrestCount(maxArrest)
				.maxSurvivalTime(maxSurvival)
				.isMissionCleared(myGameStat.getMissionCount() != null && myGameStat.getMissionCount() > 0)
				.build();
		}

		return GameResultResponse.builder()
			.gameId(game.getId())
			.winner(game.getWinTeam().toString())
			.endedAt(game.getEndTime())
			.mvp(toMvpResponse(mvpStat, "MVP"))
			.winningSecond(toMvpResponse(winningSecondStat, "승리팀 2위"))
			.losingFirst(toMvpResponse(losingFirstStat, "패배팀 1위"))
			.stats(GameResultResponse.TotalStats.builder()
				.arrests(totalArrests)
				.missionsCleared(totalMissionsCleared) // 전체 개수만 전달
				.durationSec(durationSec)
				.build())
			.myStat(myStatResponse)
			.build();
	}

	private void triggerAiNewsGeneration(Game game, Double lat, Double lng) {
		// AI 뉴스 생성 로직에서도 동일한 정렬 로직 사용
		// (getGameResult와 로직이 유사하지만, 여기서는 문자열 데이터만 추출하여 MQ로 보냄)
		List<GameMemberStat> allStats = gameMemberStatRepository.findAllByGameId(game.getId());

		List<GameMemberStat> policeStats = new ArrayList<>();
		List<GameMemberStat> thiefStats = new ArrayList<>();

		for (GameMemberStat stat : allStats) {
			if (stat.getPosition() == Position.POLICE) {
				policeStats.add(stat);
			} else if (stat.getPosition() == Position.THIEF) {
				thiefStats.add(stat);
			}
		}

		policeStats.sort((a, b) -> compareStats(b.getArrestCount(), a.getArrestCount()));
		thiefStats.sort((a, b) -> compareStats(b.getLongestSurvived(), a.getLongestSurvived()));

		String mvpNickname = "없음";
		String winnerTopMember = "없음";
		String loserTopMember = "없음";

		List<GameMemberStat> winnerStats;
		List<GameMemberStat> loserStats;

		boolean isPoliceWin = (game.getWinTeam() == WinTeam.POLICE);

		if (isPoliceWin) {
			winnerStats = policeStats;
			loserStats = thiefStats;
		} else {
			winnerStats = thiefStats;
			loserStats = policeStats;
		}

		if (!winnerStats.isEmpty()) {
			mvpNickname = getNickname(winnerStats.get(0));
		}
		if (winnerStats.size() > 1) {
			winnerTopMember = getNickname(winnerStats.get(1));
		}
		if (!loserStats.isEmpty()) {
			loserTopMember = getNickname(loserStats.get(0));
		}

		int durationSec = (int)Duration.between(game.getStartTime(), game.getEndTime()).toSeconds();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

		AiNewsRequest aiRequest = AiNewsRequest.builder()
			.gameId(game.getId())
			.startTime(game.getStartTime().format(formatter))
			.winningTeam(isPoliceWin ? "경찰" : "도둑")
			.playTime(durationSec)
			.latitude(lat)
			.longitude(lng)
			.policeCount(policeStats.size())
			.thiefCount(thiefStats.size())
			.mvp(mvpNickname)
			.winnerTopMember(winnerTopMember)
			.loserTopMember(loserTopMember)
			.build();

		rabbitTemplate.convertAndSend("NEWS", aiRequest);
	}

	private GameResultResponse.MvpResponse toMvpResponse(GameMemberStat stat, String description) {
		if (stat == null)
			return null;

		Member member = stat.getGameMember().getMember();
		String rankName = "Unknown";
		Integer maxArrest = null;
		Integer maxSurvival = null;

		// DB에서 등급 및 최고 기록 조회
		if (stat.getPosition() == Position.POLICE) {
			MemberStatPolice policeInfo = memberStatPoliceRepository.findById(member.getId())
				.orElse(null);
			if (policeInfo != null) {
				rankName = policeInfo.getGradePolice().getName();
				maxArrest = policeInfo.getMostArrestsInGame();
			}
		} else if (stat.getPosition() == Position.THIEF) {
			MemberStatThief thiefInfo = memberStatThiefRepository.findById(member.getId())
				.orElse(null);
			if (thiefInfo != null) {
				rankName = thiefInfo.getGradeThief().getName();
				maxSurvival = thiefInfo.getLongestSurvivalSec();
			}
		}

		return GameResultResponse.MvpResponse.builder()
			.memberId(member.getId())
			.nickname(getNickname(stat))
			.role(stat.getPosition().name())
			.description(description)
			// 추가된 스탯 필드 매핑
			.walk(stat.getWalk())
			.arrestCount(stat.getArrestCount())
			.longestSurvived(stat.getLongestSurvived())
			.rank(rankName)
			.maxArrestCount(maxArrest)
			.maxSurvivalTime(maxSurvival)
			.isMissionCleared(stat.getMissionCount() != null && stat.getMissionCount() > 0)
			.build();
	}

	// 닉네임 추출 헬퍼
	private String getNickname(GameMemberStat stat) {
		return stat.getGameMember().getMember().getMemberProfile().getNickname();
	}

	// null-safe Integer 비교 헬퍼
	private int compareStats(Integer v1, Integer v2) {
		int val1 = (v1 == null) ? 0 : v1;
		int val2 = (v2 == null) ? 0 : v2;
		return Integer.compare(val1, val2);
	}

	private void updateMemberGradeAndStats(GameMemberStat gameStat, WinTeam winTeam, Position position, Integer missionCount) {
		GameMember gameMember = gameStat.getGameMember();
		Member member = gameMember.getMember();

		// 해당 판에서 이겼는지 여부
		boolean isWin = (position == Position.POLICE && winTeam == WinTeam.POLICE) ||
			(position == Position.THIEF && winTeam == WinTeam.THIEF);

		// 1. MemberStat (전체 통계) 먼저 업데이트
		MemberStat memberStat = memberStatRepository.findById(member.getId())
			.orElseGet(() -> memberStatRepository.save(MemberStat.createInitial(member)));

		// 여기서 totalGames, thiefGame 등이 +1 됨
		memberStat.updateGameStats(isWin, position);

		memberStatRepository.save(memberStat);

		if (position == Position.POLICE) {
			// 1. 경찰 누적 스탯 조회 (없으면 초기 생성)
			MemberStatPolice policeStat = memberStatPoliceRepository.findById(member.getId())
				.orElseGet(() -> {
					GradePolice initial = gradePoliceRepository.findById(MIN_GRADE_ID)
						.orElseThrow(() -> new BusinessException(ErrorCode.GRADE_NOT_FOUND));
					return memberStatPoliceRepository.save(MemberStatPolice.createInitial(member, initial));
				});

			// 2. 스탯 업데이트 (DB에 있던 gameStat.getArrestCount() 사용)
			policeStat.updateAfterGame(isWin, gameStat.getArrestCount());

			// 3. 등급 변경 계산
			long currentGradeId = policeStat.getGradePolice().getId();
			long nextGradeId = calculateNextGradeId(currentGradeId, isWin);

			if (currentGradeId != nextGradeId) {
				GradePolice nextGrade = gradePoliceRepository.findById(nextGradeId)
					.orElse(policeStat.getGradePolice()); // 없으면 유지
				policeStat.changeGrade(nextGrade);
			}

			memberStatPoliceRepository.save(policeStat);

		} else if (position == Position.THIEF) {
			// 1. 도둑 누적 스탯 조회
			MemberStatThief thiefStat = memberStatThiefRepository.findById(member.getId())
				.orElseGet(() -> {
					GradeThief initial = gradeThiefRepository.findById(MIN_GRADE_ID)
						.orElseThrow(() -> new BusinessException(ErrorCode.GRADE_NOT_FOUND));
					return memberStatThiefRepository.save(MemberStatThief.createInitial(member, initial));
				});

			// 도둑 스탯과 평균시간 업데이트
			thiefStat.updateAfterGame(
				isWin,  // 이겼는지 졌는지
				gameStat.getLongestSurvived(),
				memberStat.getThiefGame(), // MemberStat에서 가져온 총 도둑 판수 전달
				missionCount
			);

			// 3. 등급 변경 계산
			long currentGradeId = thiefStat.getGradeThief().getId();
			long nextGradeId = calculateNextGradeId(currentGradeId, isWin);

			if (currentGradeId != nextGradeId) {
				GradeThief nextGrade = gradeThiefRepository.findById(nextGradeId)
					.orElse(thiefStat.getGradeThief());
				thiefStat.changeGrade(nextGrade);
			}

			memberStatThiefRepository.save(thiefStat);
		}
	}

	// 등급 ID 계산 (1 ~ 11 범위 고정)
	private long calculateNextGradeId(long currentId, boolean isWin) {
		if (isWin) {
			// 승리 시 1단계 승급 (최대 11)
			return Math.min(currentId + 1, MAX_GRADE_ID);
		} else {
			// 패배 시 1단계 강등 (최소 1)
			return Math.max(currentId - 1, MIN_GRADE_ID);
		}
	}

}