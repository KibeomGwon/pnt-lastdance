package com.pnt.pnt_spring.domain.games.game.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pnt.pnt_spring.domain.games.game.entity.Game;
import com.pnt.pnt_spring.domain.games.game.enums.GameStatus;

import jakarta.persistence.LockModeType;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {

	// 활성 방만 조회
	Optional<Game> findByRoomCodeAndIsDeletedFalse(String roomCode);

	// 활성 방만 기준으로 존재 여부
	boolean existsByRoomCodeAndIsDeletedFalse(String roomCode);

	// WAITING 방 코드 중복 방지(활성방 기준)
	boolean existsByRoomCodeAndStatusAndIsDeletedFalse(String roomCode, GameStatus status);

	// 시작/설정 변경 등 "게임 1개"를 강하게 잡고 처리할 때
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		    select g
		    from Game g
		    where g.id = :gameId
		      and g.isDeleted = false
		""")
	Optional<Game> findByIdForUpdate(@Param("gameId") Long gameId);

	// roomCode로 락 + 활성방만
	@Lock(LockModeType.PESSIMISTIC_WRITE)
	@Query("""
		    select g
		    from Game g
		    where g.roomCode = :roomCode
		      and g.isDeleted = false
		""")
	Optional<Game> findByRoomCodeForUpdate(@Param("roomCode") String roomCode);

	Optional<Game> findByIdAndIsDeletedFalse(Long id);

}
