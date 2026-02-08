package com.pnt.pnt_spring.domain.members.member.api.resp;

import java.time.OffsetDateTime;

import com.pnt.pnt_spring.domain.members.member.entity.Member;
import com.pnt.pnt_spring.domain.members.stat.api.resp.MemberStatResponse;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class MemberProfileResponse {

	private Long memberId;
	private String loginId;
	private String nickname;
	private String avatarUrl;
	private MemberStatResponse stat;
	private OffsetDateTime createdAt;

	public static MemberProfileResponse from(Member member) {
		return MemberProfileResponse.builder()
			.memberId(member.getId())
			.loginId(member.getLoginId())
			// Member 안에 있는 Profile에서 꺼내기
			.nickname(member.getMemberProfile() != null ? member.getMemberProfile().getNickname() : null)
			.avatarUrl(member.getMemberProfile() != null ? member.getMemberProfile().getAvatarUrl() : null)
			// 전적 정보 매핑
			.stat(MemberStatResponse.of(
				member.getMemberStat(),
				member.getMemberStatPolice(),
				member.getMemberStatThief()
			))
			.createdAt(member.getCreatedAt())
			.build();
	}

}
