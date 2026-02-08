package com.pnt.pnt_spring.domain.members.member.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.pnt.pnt_spring.domain.members.member.entity.MemberAuthProvider;

public interface MemberAuthProviderRepository extends JpaRepository<MemberAuthProvider, Long> {
	Optional<MemberAuthProvider> findByProviderAndProviderUserKey(String provider, String providerUserKey);
}
