package com.pnt.pnt_spring.domain.auth.api.resp;

import com.pnt.pnt_spring.domain.auth.api.req.TokenDto;
import com.pnt.pnt_spring.domain.members.member.entity.Member;
import com.pnt.pnt_spring.domain.members.member.entity.MemberProfile;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

	private String accessToken;
	private String refreshToken;
	private LoginMemberInfo member;

	@Getter
	@Builder
	public static class LoginMemberInfo {
		private Long memberId;
		private String id; // 로그인 아이디
		private String nickname;
		private String avatarUrl;
		private String role;
	}

	// 응답 객체 생성
	public static LoginResponse of(TokenDto tokenDto, Member member, MemberProfile profile) {
		return LoginResponse.builder()
				.accessToken(tokenDto.getAccessToken())
				.refreshToken(tokenDto.getRefreshToken())
				.member(LoginMemberInfo.builder()
						.memberId(member.getId())
						.id(member.getLoginId())
						.nickname(profile.getNickname())
						.avatarUrl(profile.getAvatarUrl())
						.role("USER")
						.build())
				.build();
	}
}