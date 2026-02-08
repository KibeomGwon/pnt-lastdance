package com.pnt.pnt_spring.domain.members.member.repository.mongo;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pnt.pnt_spring.domain.members.member.entity.document.MemberDoc;

public interface MemberMongoRepository extends MongoRepository<MemberDoc, String> {
	Optional<MemberDoc> findByMemberId(Long memberId);
}
