package com.pnt.pnt_spring.domain.games.maps.repository;

import com.pnt.pnt_spring.domain.games.maps.entity.GameMap;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameMapRepository extends JpaRepository<GameMap, Long> {

    List<GameMap> findAllByOwnerIdAndIsDeletedFalseOrderByUpdatedAtDesc(Long ownerId);

    Optional<GameMap> findByIdAndOwnerIdAndIsDeletedFalse(Long id, Long ownerId);

    boolean existsByIdAndOwnerIdAndIsDeletedFalse(Long id, Long ownerId);
}
