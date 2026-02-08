package com.pnt.pnt_spring.domain.members.stat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pnt.pnt_spring.domain.members.stat.entity.MemberStatThief;

public interface MemberStatThiefRepository extends JpaRepository<MemberStatThief, Long> {
}