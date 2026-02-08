package com.pnt.pnt_spring.domain.members.stat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pnt.pnt_spring.domain.members.stat.entity.MemberStat;

public interface MemberStatRepository extends JpaRepository<MemberStat, Long> {

}
