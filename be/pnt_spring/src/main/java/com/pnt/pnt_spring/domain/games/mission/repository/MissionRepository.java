package com.pnt.pnt_spring.domain.games.mission.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pnt.pnt_spring.domain.games.mission.entity.Mission;

public interface MissionRepository extends JpaRepository<Mission, Long> {

}
