package com.pnt.pnt_spring.domain.members.member.api.resp;

import java.time.OffsetDateTime;

import com.pnt.pnt_spring.domain.members.member.entity.MemberProfile;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Setter
public class MemberProfileUpdateResponse {
	private Long memberId;
	private String nickname;
	private String avatarUrl;
	private OffsetDateTime createdAt;
	private OffsetDateTime updatedAt;

	public static MemberProfileUpdateResponse from(MemberProfile profile) {
		return MemberProfileUpdateResponse.builder()
			.memberId(profile.getId()) // @MapsId로 memberId와 동일
			.nickname(profile.getNickname())
			.avatarUrl(profile.getAvatarUrl())
			.createdAt(profile.getCreatedAt())
			.updatedAt(profile.getUpdatedAt())
			.build();
	}
}
