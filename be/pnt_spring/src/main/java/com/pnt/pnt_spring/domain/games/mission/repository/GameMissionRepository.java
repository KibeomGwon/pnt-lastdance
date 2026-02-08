package com.pnt.pnt_spring.domain.games.mission.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pnt.pnt_spring.domain.games.mission.entity.GameMission;

public interface GameMissionRepository extends JpaRepository<GameMission, Long> {

	@Query("SELECT gm FROM GameMission gm JOIN FETCH gm.mission WHERE gm.game.id = :gameId")
	List<GameMission> findByGameId(@Param("gameId") Long gameId);

	Optional<GameMission> findByGameIdAndMissionId(Long gameId, Long missionId);

	// 미션 진행 상태 초기화 (재사용)
	@Modifying(clearAutomatically = true)
	@Query("UPDATE GameMission m SET m.status = 'IN_PROGRESS', m.completedBy = null, m.completedAt = null WHERE m.game.id = :gameId")
	void resetAllByGameId(@Param("gameId") Long gameId);

	// 개발용, 게임 ID로 미션 상태 완전 삭제
	void deleteByGameId(Long gameId);

}