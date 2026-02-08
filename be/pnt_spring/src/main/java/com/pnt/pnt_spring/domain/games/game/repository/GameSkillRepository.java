package com.pnt.pnt_spring.domain.games.game.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.pnt.pnt_spring.domain.games.game.entity.GameSkill;

public interface GameSkillRepository extends JpaRepository<GameSkill, Long> {

	@Modifying(clearAutomatically = true)
	@Query("UPDATE GameSkill s SET s.isUsed = false, s.usedAt = null WHERE s.game.id = :gameId")
	void resetAllByGameId(@Param("gameId") Long gameId);

	boolean existsByGame_IdAndMember_Id(Long gameId, Long memberId);

	// 개발용, 게임 ID로 미션 상태 완전 삭제
	void deleteByGameId(Long gameId);
}
