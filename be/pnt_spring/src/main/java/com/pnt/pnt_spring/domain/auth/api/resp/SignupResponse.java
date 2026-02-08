package com.pnt.pnt_spring.domain.auth.api.resp;

import java.time.OffsetDateTime;

import com.pnt.pnt_spring.domain.members.member.entity.Member;
import com.pnt.pnt_spring.domain.members.member.entity.MemberProfile;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class SignupResponse {
	private Long memberId;
	private String id;
	private String nickname;
	private OffsetDateTime createdAt; // BaseEntity의 createdAt 타입에 맞춤

	public static SignupResponse from(Member member, MemberProfile profile) {
		return SignupResponse.builder()
			.memberId(member.getId())
			.id(member.getLoginId())
			.nickname(profile.getNickname())
			.createdAt(member.getCreatedAt())
			.build();
	}
}
