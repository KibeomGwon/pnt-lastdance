package com.pnt.pnt_spring.domain.games.news.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pnt.pnt_spring.domain.games.news.entity.GameNews;

@Repository
public interface GameNewsRepository extends JpaRepository<GameNews, Long> {
	// 게임 ID로 뉴스를 조회
	Optional<GameNews> findByGameId(Long gameId);

	// 뉴스는 로그 성격이므로 이번 판 기록을 안 보이게 처리 (Soft Delete)
	@Modifying(clearAutomatically = true)
	@Query("UPDATE GameNews n SET n.isDeleted = true WHERE n.game.id = :gameId")
	void softDeleteAllByGameId(@Param("gameId") Long gameId);

	// 개발 용, 게임방 삭제할때 함께 지워지도록
	void deleteByGameId(Long gameId);
}