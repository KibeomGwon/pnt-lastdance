package com.pnt.pnt_spring.domain.games.game.application.impl;

import com.pnt.pnt_spring.domain.games.game.api.req.GameRoomCreateRequest;
import com.pnt.pnt_spring.domain.games.game.api.resp.GameRoomCreateResponse;
import com.pnt.pnt_spring.domain.games.game.api.resp.GameRoomStartableResponse;
import com.pnt.pnt_spring.domain.games.game.api.resp.GameStartResponse;
import com.pnt.pnt_spring.domain.games.game.application.GameRoomCodeGenerator;
import com.pnt.pnt_spring.domain.games.game.application.GameRoomService;
import com.pnt.pnt_spring.domain.games.game.entity.*;
import com.pnt.pnt_spring.domain.games.game.enums.GameStatus;
import com.pnt.pnt_spring.domain.games.game.enums.Position;
import com.pnt.pnt_spring.domain.games.game.enums.PreferPosition;
import com.pnt.pnt_spring.domain.games.game.repository.*;
import com.pnt.pnt_spring.domain.games.maps.entity.GameMap;
import com.pnt.pnt_spring.domain.games.maps.repository.GameMapRepository;
import com.pnt.pnt_spring.domain.games.mission.entity.GameMission;
import com.pnt.pnt_spring.domain.games.mission.entity.Mission;
import com.pnt.pnt_spring.domain.games.mission.repository.GameMissionRepository;
import com.pnt.pnt_spring.domain.games.mission.repository.MissionRepository;
import com.pnt.pnt_spring.domain.games.news.repository.GameNewsRepository;
import com.pnt.pnt_spring.domain.games.utils.GeoConverter;
import com.pnt.pnt_spring.domain.members.member.entity.Member;
import com.pnt.pnt_spring.domain.members.member.repository.jpa.MemberRepository;
import com.pnt.pnt_spring.global.api.code.ErrorCode;
import com.pnt.pnt_spring.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Polygon;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
@Transactional
public class GameRoomServiceImpl implements GameRoomService {

	private final GameRepository gameRepository;
	private final GameMemberRepository gameMemberRepository;
	private final GameSettingRepository gameSettingRepository;
	private final MemberRepository memberRepository;
	private final GameRoomCodeGenerator gameRoomCodeGenerator;
	private final GameMemberStatRepository gameMemberStatRepository;
	private final GameSkillRepository gameSkillRepository;
	private final GameMissionRepository gameMissionRepository;
	private final GameNewsRepository gameNewsRepository;
	private final MissionRepository missionRepository;
	private final GameMapRepository gameMapRepository;

	@Override
	public GameRoomCreateResponse createRoom(Long hostMemberId, GameRoomCreateRequest req) {
		if (gameMemberRepository.existsByMemberIdAndIsDeletedFalse(hostMemberId)) {
			throw new BusinessException(ErrorCode.ROOM_ALREADY_JOINED);
		}
		if (req == null) {
			throw new IllegalArgumentException("방 생성 요청 바디가 필요합니다.");
		}

		if (!req.getPlayerCount().equals(req.getPoliceCount() + req.getThiefCount())) {
			throw new IllegalArgumentException("playerCount는 policeCount + thiefCount와 같아야 합니다.");
		}

		Double prisonLat;
		Double prisonLng;
		Polygon boundary;

		if (req.getMapId() != null) {
			GameMap map = gameMapRepository.findByIdAndOwnerIdAndIsDeletedFalse(req.getMapId(), hostMemberId)
					.orElseThrow(() -> new BusinessException(ErrorCode.MAP_NOT_FOUND));

			prisonLat = map.getPrisonLat();
			prisonLng = map.getPrisonLng();
			boundary = map.getPolygon();

		} else {
			if (req.getPrison() == null || req.getPolygon() == null) {
				throw new IllegalArgumentException("방 생성에 필요한 세팅 값(prison/polygon)이 누락되었습니다.");
			}

			prisonLat = req.getPrison().getLat();
			prisonLng = req.getPrison().getLng();
			if (prisonLat == null || prisonLng == null) {
				throw new IllegalArgumentException("감옥 좌표(prison.lat/lng)가 필요합니다.");
			}

			boundary = GeoConverter.toPolygon(req.getPolygon());

			if (Boolean.TRUE.equals(req.getSaveMap())) {
				String name = req.getMapName();

				// mapName이 null/blank면 서버에서 기본 이름 생성
				if (name == null || name.trim().isEmpty()) {
					name = "저장 맵 " + java.time.LocalDateTime.now()
							.format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
				} else {
					name = name.trim();
				}

				GameMap saved = GameMap.create(
						hostMemberId,
						name,
						req.getMapDescription(), // null OK
						boundary,
						prisonLat,
						prisonLng
				);
				gameMapRepository.save(saved);
			}

		}

		Member hostRef = memberRepository.getReferenceById(hostMemberId);
		String roomCode = gameRoomCodeGenerator.generateUniqueCode();

		Game game = Game.createWaitingRoom(hostRef, roomCode);
		gameRepository.save(game);

		GameSetting setting = GameSetting.create(
				game,
				req.getTimeLimit(),
				req.getPlayerCount(),
				req.getPoliceCount(),
				req.getThiefCount(),
				req.getCctvInterval(),
				boundary, // Polygon은 Geometry로 자동 업캐스팅됨
				prisonLat,
				prisonLng,
				req.getMissionCount()
		);
		gameSettingRepository.save(setting);

		GameMember hostMember = GameMember.join(game, hostRef);
		gameMemberRepository.save(hostMember);

		return new GameRoomCreateResponse(game.getId(), game.getRoomCode(), GameStatus.WAITING);
	}



	@Override
	public GameStartResponse start(Long actorMemberId, Long roomId) {

		Game game = gameRepository.findByIdForUpdate(roomId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

		if (!game.isHost(actorMemberId)) {
			throw new BusinessException(ErrorCode.ROOM_NOT_HOST);
		}

		if (!game.isWaiting()) {
			throw new BusinessException(ErrorCode.ROOM_ALREADY_STARTED);
		}

		GameSetting setting = gameSettingRepository.findByGameIdAndIsDeletedFalse(roomId)
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

		long joined = gameMemberRepository.countByGameIdAndIsDeletedFalse(roomId);
		if (!joinedEqualsSetting(joined, setting.getPlayerCount())) {
			throw new BusinessException(ErrorCode.ROOM_NOT_READY);
		}

		long notReadyExceptHost = gameMemberRepository.countByGameIdAndIsDeletedFalseAndMemberIdNotAndReadyFalse(
			roomId,
			actorMemberId
		);
		if (notReadyExceptHost > 0) {
			throw new BusinessException(ErrorCode.ROOM_NOT_READY);
		}

		// active 멤버 로드(닉네임 응답용 memberProfile까지 fetch)
		List<GameMember> members = gameMemberRepository.findAllActiveByGameIdWithMember(roomId);

		// 포지션 확정(PreferPosition 반영)
		assignPositions(setting, members);

		// 확정된 포지션을 DB에 즉시 저장(포지션 믹스업 방지)
		gameMemberRepository.saveAll(members);

		// 경찰 중 랜덤 1명(=경찰청장) 선정 + 스킬 생성 (givenPosition은 POLICE 유지)
		Long chiefMemberId = assignChiefSkill(game, members);

		// 미션 할당 로직
		assignMissions(game, setting.getMissionCount());

		// 게임 시작
		game.start();

		for (GameMember member : members) {
			// 기존 스탯을 가져오거나, 없으면 새로 만듦 (GameMemberStat.create 내부에서 member.givenPosition 사용)
			GameMemberStat stat = gameMemberStatRepository.findByGameMemberId(member.getId())
				.orElseGet(() -> GameMemberStat.create(member));

			// 가져온 스탯의 포지션을 이번 판에 배정된 포지션(givenPosition)으로 강제 동기화
			// 이렇게 해야 이전 판 데이터가 남아있어도 이번 판 포지션으로 덮어씌워짐
			stat.syncPosition(member.getGivenPosition());

			// 저장
			gameMemberStatRepository.save(stat);
		}

		// chiefMemberId 포함해서 반환
		return GameStartResponse.from(game, members, chiefMemberId);
	}

	// 게임 미션 할당 메서드
	private void assignMissions(Game game, Integer missionCount) {
		if (missionCount == null || missionCount <= 0) {
			return;
		}

		// 1. 전체 미션 로드
		List<Mission> allMissions = missionRepository.findAll();

		if (allMissions.isEmpty()) {
			// 미션 데이터가 하나도 없으면 예외 처리 혹은 빈 리스트 반환
			return;
		}

		List<GameMission> gameMissions = new ArrayList<>();
		int remainingCount = missionCount;

		// 2. 요청한 개수(remainingCount)가 채워질 때까지 반복
		while (remainingCount > 0) {
			// 미션 리스트를 매번 섞어줍니다 (최대한 다양한 미션이 배정되도록)
			Collections.shuffle(allMissions);

			// 이번 턴에 가져올 개수: 남은 필요량 vs 전체 미션 개수 중 작은 값
			int take = Math.min(remainingCount, allMissions.size());

			for (int i = 0; i < take; i++) {
				gameMissions.add(GameMission.create(game, allMissions.get(i)));
			}

			remainingCount -= take;
		}
		// 3. 저장
		gameMissionRepository.saveAll(gameMissions);
	}

	@Override
	@Transactional(readOnly = true)
	public GameRoomStartableResponse getStartable(Long actorMemberId, Long roomId) {

		Game game = gameRepository.findByIdAndIsDeletedFalse(roomId)
			.orElseThrow(() -> new BusinessException(ErrorCode.ROOM_NOT_FOUND));

		boolean isHost = game.isHost(actorMemberId);

		GameSetting setting = gameSettingRepository.findById(roomId)
			.orElseThrow(() -> new BusinessException(ErrorCode.INVALID_REQUEST));

		long joined = gameMemberRepository.countByGameIdAndIsDeletedFalse(roomId);
		long notReady = gameMemberRepository
			.countByGameIdAndIsDeletedFalseAndMemberIdNotAndReadyFalse(
				roomId,
				game.getHost().getId()
			);

		boolean canStart =
			isHost &&
				game.isWaiting() &&
				joined == setting.getPlayerCount() &&
				notReady == 0;

		return new GameRoomStartableResponse(
			canStart,
			joined,
			setting.getPlayerCount(),
			notReady
		);
	}

	private boolean joinedEqualsSetting(long joined, Integer playerCount) {
		if (playerCount == null)
			return false;
		return joined == playerCount.longValue();
	}

//	private Polygon toPolygon(List<GameRoomCreateRequest.LatLng> polygon) {
//		if (polygon == null || polygon.size() < 3) {
//			throw new IllegalArgumentException("polygon은 최소 3개 좌표가 필요합니다.");
//		}
//
//		Coordinate[] raw = new Coordinate[polygon.size()];
//		for (int i = 0; i < polygon.size(); i++) {
//			GameRoomCreateRequest.LatLng p = polygon.get(i);
//			if (p == null || p.getLat() == null || p.getLng() == null) {
//				throw new IllegalArgumentException("polygon 좌표에 null이 포함되어 있습니다.");
//			}
//			raw[i] = new Coordinate(p.getLng(), p.getLat()); // x=lng, y=lat
//		}
//
//		Coordinate first = raw[0];
//		Coordinate last = raw[raw.length - 1];
//		boolean alreadyClosed = first.equals2D(last);
//
//		Coordinate[] coords;
//		if (alreadyClosed) {
//			coords = raw;
//		} else {
//			coords = new Coordinate[raw.length + 1];
//			System.arraycopy(raw, 0, coords, 0, raw.length);
//			coords[raw.length] = new Coordinate(first.x, first.y);
//		}
//
//		if (coords.length < 4)
//			throw new IllegalArgumentException("polygon 좌표가 올바르지 않습니다.");
//
//		LinearRing shell = GF.createLinearRing(coords);
//		Polygon poly = GF.createPolygon(shell);
//
//		if (!poly.isValid())
//			throw new IllegalArgumentException("polygon 형태가 유효하지 않습니다.");
//
//		return poly;
//	}

	private void assignPositions(GameSetting setting, List<GameMember> members) {
		int thiefNeed = setting.getThiefCount();
		int policeNeed = setting.getPoliceCount();

		if (thiefNeed + policeNeed != members.size()) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}

		// 1) 선호로 후보군 나누기
		List<GameMember> preferThief = members.stream()
			.filter(gm -> gm.getPreferPosition() == PreferPosition.THIEF)
			.toList();

		List<GameMember> preferAny = members.stream()
			.filter(gm -> gm.getPreferPosition() == PreferPosition.ANY)
			.toList();

		List<GameMember> preferPolice = members.stream()
			.filter(gm -> gm.getPreferPosition() == PreferPosition.POLICE)
			.toList();

		// 2) thiefNeed 만큼 도둑 선정: THIEF 선호 → ANY → POLICE(강제 전환)
		LinkedHashSet<GameMember> thieves = new java.util.LinkedHashSet<>();

		for (GameMember gm : preferThief) {
			if (thieves.size() >= thiefNeed)
				break;
			thieves.add(gm);
		}
		for (GameMember gm : preferAny) {
			if (thieves.size() >= thiefNeed)
				break;
			thieves.add(gm);
		}
		for (GameMember gm : preferPolice) {
			if (thieves.size() >= thiefNeed)
				break;
			thieves.add(gm);
		}

		if (thieves.size() != thiefNeed) {
			throw new BusinessException(ErrorCode.INVALID_REQUEST);
		}

		// 3) 확정 배정
		for (GameMember gm : members) {
			if (thieves.contains(gm)) {
				gm.assignPosition(Position.THIEF);
			} else {
				gm.assignPosition(Position.POLICE);
			}
		}
	}

	private Long assignChiefSkill(Game game, List<GameMember> members) {

		List<GameMember> polices = members.stream()
			.filter(m -> m.getGivenPosition() == Position.POLICE)
			.toList();

		if (polices.isEmpty())
			return null;

		GameMember chief = polices.get(ThreadLocalRandom.current().nextInt(polices.size()));

		Long gameId = game.getId();
		Long memberId = chief.getMember().getId();

		// 중복 방지
		if (!gameSkillRepository.existsByGame_IdAndMember_Id(gameId, memberId)) {
			gameSkillRepository.save(GameSkill.create(game, chief.getMember()));
		}

		return memberId;
	}

	@Override
	@Transactional
	public void resetGame(Long gameId) {
		Game game = gameRepository.findById(gameId)
			.orElseThrow(() -> new BusinessException(ErrorCode.GAME_NOT_FOUND));

		if (game.getStatus() == GameStatus.IN_GAME) {
			throw new BusinessException(ErrorCode.GAME_ALREADY_STARTED);
		}

		// 1. 하위 데이터 초기화 (이 과정에서 영속성 컨텍스트가 비워짐)
		gameSkillRepository.resetAllByGameId(gameId);
		gameMissionRepository.resetAllByGameId(gameId);
		gameMemberStatRepository.resetAllByGameId(gameId);
		gameNewsRepository.softDeleteAllByGameId(gameId);

		// 2. 게임 상태 초기화
		game.reset();

		gameRepository.save(game);

		// 3. 모든 멤버 나가기 처리
		List<GameMember> members = gameMemberRepository.findAllByGameId(gameId);

		for (GameMember member : members) {
			member.leave();
		}
		// 멤버들의 변경사항도 반영하기 위해 리스트 저장 (혹은 Dirty Checking이 안될 수 있으므로 saveAll 권장)
		gameMemberRepository.saveAll(members);

		}
}

