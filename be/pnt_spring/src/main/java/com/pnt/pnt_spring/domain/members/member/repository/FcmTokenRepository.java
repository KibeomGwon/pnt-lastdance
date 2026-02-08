package com.pnt.pnt_spring.domain.members.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.pnt.pnt_spring.domain.members.member.entity.FcmToken;
import com.pnt.pnt_spring.domain.members.member.entity.Member;

public interface FcmTokenRepository extends JpaRepository<FcmToken, Long> {
	@Query("select f from FcmToken f where f.member = :member and f.isDeleted = false")
	Optional<FcmToken> findFcmTokenByMember(Member member);
}
