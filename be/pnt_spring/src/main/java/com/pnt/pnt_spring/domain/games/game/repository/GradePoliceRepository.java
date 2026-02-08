package com.pnt.pnt_spring.domain.games.game.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pnt.pnt_spring.domain.members.stat.entity.GradePolice;

@Repository
public interface GradePoliceRepository extends JpaRepository<GradePolice, Long> {
}
