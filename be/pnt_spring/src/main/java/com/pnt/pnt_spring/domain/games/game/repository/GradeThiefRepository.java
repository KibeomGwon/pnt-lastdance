package com.pnt.pnt_spring.domain.games.game.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pnt.pnt_spring.domain.members.stat.entity.GradeThief;

@Repository
public interface GradeThiefRepository extends JpaRepository<GradeThief, Long> {
}
