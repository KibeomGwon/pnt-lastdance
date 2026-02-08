package com.pnt.pnt_spring.domain.members.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pnt.pnt_spring.domain.members.member.entity.Member;
import com.pnt.pnt_spring.domain.members.member.entity.MemberProfile;

public interface MemberProfileRepository extends JpaRepository<MemberProfile, Long> {
	Optional<MemberProfile> findByMember(Member member);
}
